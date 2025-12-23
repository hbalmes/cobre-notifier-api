package com.cobre.notifier.api.infrastructure.persistence.mapper;

import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.persistence.entity.NotificationEventEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre NotificationEvent (dominio) y NotificationEventEntity (persistencia).
 */
@Component
public class NotificationEventMapper {

    public NotificationEventEntity toEntity(NotificationEvent domain) {
        if (domain == null) {
            return null;
        }

        return NotificationEventEntity.builder()
                .id(domain.getId())
                .clientId(domain.getClientId())
                .eventType(domain.getEventType())
                .payload(domain.getPayload())
                .webhookUrl(domain.getWebhookUrl())
                .status(domain.getStatus())
                .retryCount(domain.getRetryCount())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .sentAt(domain.getSentAt())
                .failedAt(domain.getFailedAt())
                .errorMessage(domain.getErrorMessage())
                .responseCode(domain.getResponseCode())
                .responseBody(domain.getResponseBody())
                .kafkaEventId(domain.getKafkaEventId())
                .build();
    }

    public NotificationEvent toDomain(NotificationEventEntity entity) {
        if (entity == null) {
            return null;
        }

        return NotificationEvent.builder()
                .id(entity.getId())
                .clientId(entity.getClientId())
                .eventType(entity.getEventType())
                .payload(entity.getPayload())
                .webhookUrl(entity.getWebhookUrl())
                .status(entity.getStatus())
                .retryCount(entity.getRetryCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .sentAt(entity.getSentAt())
                .failedAt(entity.getFailedAt())
                .errorMessage(entity.getErrorMessage())
                .responseCode(entity.getResponseCode())
                .responseBody(entity.getResponseBody())
                .kafkaEventId(entity.getKafkaEventId())
                .build();
    }
}

