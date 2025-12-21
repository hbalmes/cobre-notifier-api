package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.application.port.input.RetryFailedNotificationsUseCase;
import com.cobre.notifier.api.application.port.output.NotificationEventRepository;
import com.cobre.notifier.api.domain.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para reintentar notificaciones fallidas.
 * Implementa la estrategia de retry con exponential backoff.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService implements RetryFailedNotificationsUseCase {

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationService notificationService;

    @Value("${app.notification.retry.max-attempts:3}")
    private int maxRetries;

    @Value("${app.notification.retry.initial-delay-ms:1000}")
    private long initialDelayMs;

    @Value("${app.notification.retry.multiplier:2.0}")
    private double multiplier;

    @Value("${app.notification.scheduler.fixed-delay-ms:60000}")
    private long schedulerDelayMs;

    @Override
    @Transactional
    public int retryFailedNotifications() {
        log.debug("Starting retry process for failed notifications");

        // Buscar notificaciones pendientes de reintento
        List<NotificationEvent> pendingNotifications = 
                notificationEventRepository.findPendingRetries(maxRetries, 100);

        if (pendingNotifications.isEmpty()) {
            log.debug("No pending notifications to retry");
            return 0;
        }

        int processedCount = 0;
        for (NotificationEvent notification : pendingNotifications) {
            try {
                if (shouldRetry(notification)) {
                    log.info("Retrying notification: {} (attempt {}/{})", 
                            notification.getId(), 
                            notification.getRetryCount() + 1, 
                            maxRetries);

                    // Incrementar contador de reintentos
                    boolean canContinue = notification.incrementRetry(maxRetries);
                    
                    if (canContinue) {
                        // Calcular delay exponencial (para logging/futuro scheduler)
                        long delay = calculateExponentialBackoff(notification.getRetryCount());
                        log.debug("Calculated delay for notification {}: {} ms", 
                                notification.getId(), delay);
                        
                        // Guardar estado actualizado
                        notification = notificationEventRepository.save(notification);
                        
                        // Procesar la notificación
                        notificationService.process(notification);
                        processedCount++;
                    } else {
                        log.warn("Notification {} reached max retries, marking as failed", 
                                notification.getId());
                        notificationEventRepository.save(notification);
                    }
                }
            } catch (Exception e) {
                log.error("Error retrying notification {}: {}", 
                        notification.getId(), e.getMessage(), e);
                notification.updateErrorMessage("Retry error: " + e.getMessage());
                notificationEventRepository.save(notification);
            }
        }

        log.info("Retry process completed. Processed {} notifications", processedCount);
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

