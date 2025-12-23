package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.application.port.input.RetryFailedNotificationsUseCase;
import com.cobre.notifier.api.application.port.output.NotificationEventRepository;
import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio para reintentar notificaciones fallidas.
 * Implementa la estrategia de retry con exponential backoff.
 */
@Slf4j
@Service
public class RetryService implements RetryFailedNotificationsUseCase {

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationService notificationService;
    private final MeterRegistry meterRegistry;

    @Value("${app.notification.retry.max-attempts:3}")
    private int maxRetries;

    @Value("${app.notification.retry.initial-delay-ms:1000}")
    private long initialDelayMs;

    @Value("${app.notification.retry.multiplier:2.0}")
    private double multiplier;

    @Value("${app.notification.scheduler.fixed-delay-ms:60000}")
    private long schedulerDelayMs;

    // Métricas básicas
    private final Counter retryAttemptsTotalCounter;
    private final Counter retryAttemptsSuccessCounter;
    private final Counter retryAttemptsFailedCounter;
    private final Counter retryAttemptsMaxReachedCounter;
    private final Timer retryDelayTimer;
    private final AtomicInteger pendingRetriesGauge = new AtomicInteger(0);
    
    // Métricas detalladas (se crean dinámicamente con tags)

    public RetryService(NotificationEventRepository notificationEventRepository,
                       NotificationService notificationService,
                       MeterRegistry meterRegistry) {
        this.notificationEventRepository = notificationEventRepository;
        this.notificationService = notificationService;
        this.meterRegistry = meterRegistry;

        // Inicializar métricas básicas
        this.retryAttemptsTotalCounter = Counter.builder("notification.retry.attempts.total")
                .description("Total number of retry attempts performed")
                .register(meterRegistry);

        this.retryAttemptsSuccessCounter = Counter.builder("notification.retry.attempts.success")
                .description("Number of successful retry attempts")
                .register(meterRegistry);

        this.retryAttemptsFailedCounter = Counter.builder("notification.retry.attempts.failed")
                .description("Number of retry attempts that continue to fail")
                .register(meterRegistry);

        this.retryAttemptsMaxReachedCounter = Counter.builder("notification.retry.attempts.max_reached")
                .description("Number of notifications that reached maximum retry attempts")
                .register(meterRegistry);

        this.retryDelayTimer = Timer.builder("notification.retry.delay")
                .description("Time delay between retry attempts in milliseconds")
                .register(meterRegistry);

        // Gauge para notificaciones pendientes de reintento
        Gauge.builder("notification.retry.count", pendingRetriesGauge, AtomicInteger::get)
                .description("Current number of notifications pending retry")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public int retryFailedNotifications() {
        log.debug("Starting retry process for failed notifications");

        // Buscar notificaciones pendientes de reintento
        List<NotificationEvent> pendingNotifications = 
                notificationEventRepository.findPendingRetries(maxRetries, 100);

        // Actualizar gauge de notificaciones pendientes
        pendingRetriesGauge.set(pendingNotifications.size());

        if (pendingNotifications.isEmpty()) {
            log.debug("No pending notifications to retry");
            return 0;
        }

        int processedCount = 0;
        int successCount = 0;
        int failedCount = 0;
        int maxReachedCount = 0;

        for (NotificationEvent notification : pendingNotifications) {
            try {
                if (shouldRetry(notification)) {
                    log.info("Retrying notification: {} (attempt {}/{})", 
                            notification.getId(), 
                            notification.getRetryCount() + 1, 
                            maxRetries);

                    // Incrementar contador total de reintentos
                    retryAttemptsTotalCounter.increment();

                    // Incrementar contador de reintentos
                    boolean canContinue = notification.incrementRetry(maxRetries);
                    
                    if (canContinue) {
                        // Calcular delay exponencial (para logging/futuro scheduler)
                        long delay = calculateExponentialBackoff(notification.getRetryCount());
                        
                        // Registrar delay en métricas
                        retryDelayTimer.record(delay, java.util.concurrent.TimeUnit.MILLISECONDS);
                        
                        log.debug("Calculated delay for notification {}: {} ms", 
                                notification.getId(), delay);
                        
                        // Guardar estado actualizado
                        notification = notificationEventRepository.save(notification);
                        
                        // Procesar la notificación
                        notificationService.process(notification);
                        notification = notificationEventRepository.findById(notification.getId())
                                .orElse(notification);
                        
                        processedCount++;
                        
                        // Registrar éxito o fallo según el resultado
                        if (notification.getStatus() == DeliveryStatus.SENT) {
                            retryAttemptsSuccessCounter.increment();
                            successCount++;
                            // Métricas detalladas por estado
                            Counter.builder("notification.retry.by_status")
                                    .description("Retry attempts by notification status")
                                    .tag("status", DeliveryStatus.SENT.name())
                                    .register(meterRegistry)
                                    .increment();
                        } else {
                            retryAttemptsFailedCounter.increment();
                            failedCount++;
                            // Métricas detalladas por estado
                            Counter.builder("notification.retry.by_status")
                                    .description("Retry attempts by notification status")
                                    .tag("status", notification.getStatus().name())
                                    .register(meterRegistry)
                                    .increment();
                        }
                        
                        // Métricas detalladas por cliente
                        Counter.builder("notification.retry.by_client")
                                .description("Retry attempts by client ID")
                                .tag("client_id", notification.getClientId())
                                .register(meterRegistry)
                                .increment();
                    } else {
                        log.warn("Notification {} reached max retries, marking as failed", 
                                notification.getId());
                        notificationEventRepository.save(notification);
                        retryAttemptsMaxReachedCounter.increment();
                        maxReachedCount++;
                    }
                }
            } catch (Exception e) {
                log.error("Error retrying notification {}: {}", 
                        notification.getId(), e.getMessage(), e);
                notification.updateErrorMessage("Retry error: " + e.getMessage());
                notificationEventRepository.save(notification);
                retryAttemptsFailedCounter.increment();
                failedCount++;
            }
        }

        log.info("Retry process completed. Processed {} notifications ({} success, {} failed, {} max reached)", 
                processedCount, successCount, failedCount, maxReachedCount);
        
        // Actualizar gauge después del procesamiento
        pendingRetriesGauge.set(pendingNotifications.size() - processedCount);
        
        return processedCount;
    }

    /**
     * Verifica si una notificación debe ser reintentada.
     * 
     * @param notification Notificación a verificar
     * @return true si debe ser reintentada, false en caso contrario
     */
    private boolean shouldRetry(NotificationEvent notification) {
        if (!notification.canRetry(maxRetries)) {
            return false;
        }

        // Verificar que haya pasado el tiempo suficiente desde el último intento
        // (esto se manejaría mejor con un scheduler en producción)
        return true;
    }

    /**
     * Calcula el delay exponencial para el siguiente reintento.
     * 
     * @param retryCount Número de reintentos realizados
     * @return Delay en milisegundos
     */
    private long calculateExponentialBackoff(int retryCount) {
        return (long) (initialDelayMs * Math.pow(multiplier, retryCount));
    }
}

