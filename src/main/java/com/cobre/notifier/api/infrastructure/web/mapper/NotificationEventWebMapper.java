package com.cobre.notifier.api.infrastructure.web.mapper;

import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades de dominio NotificationEvent y DTOs de respuesta.
 */
@Component
public class NotificationEventWebMapper {

    /**
     * Convierte una entidad de dominio NotificationEvent a un DTO de respuesta.
     * 
     * @param notificationEvent Entidad de dominio
     * @return DTO de respuesta
     */
    public NotificationEventResponse toResponse(NotificationEvent notificationEvent) {
        if (notificationEvent == null) {
            return null;
        }

        return NotificationEventResponse.builder()
                .id(notificationEvent.getId())
                .clientId(notificationEvent.getClientId())
                .eventType(notificationEvent.getEventType())
                .payload(notificationEvent.getPayload())
                .webhookUrl(notificationEvent.getWebhookUrl())
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
}

