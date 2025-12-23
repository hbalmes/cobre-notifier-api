package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.application.port.input.GetNotificationEventsUseCase;
import com.cobre.notifier.api.application.port.input.ProcessNotificationUseCase;
import com.cobre.notifier.api.application.port.input.ReplayNotificationUseCase;
import com.cobre.notifier.api.application.port.output.NotificationEventRepository;
import com.cobre.notifier.api.application.port.output.SubscriptionRepository;
import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.domain.exception.InvalidSubscriptionException;
import com.cobre.notifier.api.domain.exception.NotificationNotFoundException;
import com.cobre.notifier.api.domain.exception.NotificationDeliveryException;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de aplicación que implementa los casos de uso relacionados con notificaciones.
 * Coordina la lógica de negocio y la interacción con los puertos de salida.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements ProcessNotificationUseCase, 
                                          GetNotificationEventsUseCase, 
                                          ReplayNotificationUseCase {

    private final NotificationEventRepository notificationEventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final WebhookDeliveryService webhookDeliveryService;
    private final MeterRegistry meterRegistry;

    // Métricas adicionales de reintentos
    private final AtomicLong retrySuccessCount = new AtomicLong(0);
    private final AtomicLong retryTotalCount = new AtomicLong(0);
    private final DistributionSummary retryDistributionSummary;

    public NotificationService(NotificationEventRepository notificationEventRepository,
                              SubscriptionRepository subscriptionRepository,
                              WebhookDeliveryService webhookDeliveryService,
                              MeterRegistry meterRegistry) {
        this.notificationEventRepository = notificationEventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.webhookDeliveryService = webhookDeliveryService;
        this.meterRegistry = meterRegistry;

        // DistributionSummary para distribución de reintentos
        this.retryDistributionSummary = DistributionSummary.builder("notification.retry.distribution")
                .description("Distribution of retry count per notification")
                .register(meterRegistry);

        // Gauge para tasa de éxito después de reintentos
        Gauge.builder("notification.retry.success_rate", 
                () -> {
                    long total = retryTotalCount.get();
                    if (total == 0) return 0.0;
                    return (double) retrySuccessCount.get() / total * 100;
                })
                .description("Success rate after retries (percentage)")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public NotificationEvent process(NotificationEvent notificationEvent) {
        log.debug("Processing notification: {}", notificationEvent.getId());

        // Validar suscripción
        Subscription subscription = validateSubscription(notificationEvent.getClientId(), 
                                                         notificationEvent.getEventType());

        // Actualizar webhook URL si es necesario
        if (!subscription.getWebhookUrl().equals(notificationEvent.getWebhookUrl())) {
            notificationEvent = NotificationEvent.builder()
                    .id(notificationEvent.getId())
                    .clientId(notificationEvent.getClientId())
                    .eventType(notificationEvent.getEventType())
                    .payload(notificationEvent.getPayload())
                    .webhookUrl(subscription.getWebhookUrl())
                    .status(notificationEvent.getStatus())
                    .retryCount(notificationEvent.getRetryCount())
                    .createdAt(notificationEvent.getCreatedAt())
                    .updatedAt(notificationEvent.getUpdatedAt())
                    .sentAt(notificationEvent.getSentAt())
                    .failedAt(notificationEvent.getFailedAt())
                    .errorMessage(notificationEvent.getErrorMessage())
                    .responseCode(notificationEvent.getResponseCode())
                    .responseBody(notificationEvent.getResponseBody())
                    .build();
        }

        // Registrar distribución de reintentos si es un reintento
        boolean isRetry = notificationEvent.getRetryCount() > 0;
        if (isRetry) {
            retryDistributionSummary.record(notificationEvent.getRetryCount());
            retryTotalCount.incrementAndGet();
        }

        // Intentar entrega
        try {
            String responseCode = webhookDeliveryService.deliver(
                    subscription.getWebhookUrl(),
                    notificationEvent.getPayload()
            );
            
            notificationEvent.markAsSent(responseCode, "Success");
            log.info("Notification {} delivered successfully to {}", 
                    notificationEvent.getId(), subscription.getWebhookUrl());
            
            // Registrar éxito si fue un reintento
            if (isRetry) {
                retrySuccessCount.incrementAndGet();
            }
            
        } catch (NotificationDeliveryException e) {
            log.warn("Failed to deliver notification {}: {}", 
                    notificationEvent.getId(), e.getMessage());
            notificationEvent.markAsFailed(e.getMessage());
        }

        return notificationEventRepository.save(notificationEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationEvent getById(UUID id) {
        return notificationEventRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationEvent> getAll(String clientId, 
                                         DeliveryStatus status, 
                                         LocalDateTime fromDate, 
                                         LocalDateTime toDate) {
        if (clientId != null) {
            return notificationEventRepository.findByClientId(clientId, status, fromDate, toDate);
        }
        return notificationEventRepository.findAll(status, fromDate, toDate);
    }

    @Override
    @Transactional
    public NotificationEvent replay(UUID notificationId) {
        log.info("Replaying notification: {}", notificationId);
        
        NotificationEvent notification = getById(notificationId);
        
        // Validar que pertenece al cliente correcto
        if (!notification.belongsToClient(notification.getClientId())) {
            throw new IllegalArgumentException(
                    "Notification does not belong to the specified client");
        }

        // Resetear para replay
        notification.resetForReplay();
        notification = notificationEventRepository.save(notification);

        // Procesar nuevamente
        return process(notification);
    }

    /**
     * Valida que existe una suscripción activa para el cliente y tipo de evento.
     * 
     * @param clientId ID del cliente
     * @param eventType Tipo de evento
     * @return Suscripción válida
     * @throws InvalidSubscriptionException si la suscripción no es válida
     */
    private Subscription validateSubscription(String clientId, String eventType) {
        Subscription subscription = subscriptionRepository.findActiveByClientId(clientId)
                .orElseThrow(() -> InvalidSubscriptionException.inactiveSubscription(clientId));

        if (!subscription.canReceiveNotifications()) {
            throw InvalidSubscriptionException.missingWebhookUrl(clientId);
        }

        if (!subscription.isSubscribedTo(eventType)) {
            throw new InvalidSubscriptionException(
                    String.format("Client %s is not subscribed to event type %s", 
                            clientId, eventType));
        }

        return subscription;
    }
}

