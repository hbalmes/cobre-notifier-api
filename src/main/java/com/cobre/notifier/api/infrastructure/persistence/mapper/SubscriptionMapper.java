package com.cobre.notifier.api.infrastructure.persistence.mapper;

import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para convertir entre Subscription (dominio) y SubscriptionEntity (persistencia).
 */
@Component
public class SubscriptionMapper {

    public SubscriptionEntity toEntity(Subscription domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionEntity.builder()
                .id(domain.getId())
                .clientId(domain.getClientId())
                .eventTypes(domain.getEventTypes() != null ? 
                           new ArrayList<>(domain.getEventTypes()) : new ArrayList<>())
                .webhookUrl(domain.getWebhookUrl())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Subscription toDomain(SubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        return Subscription.builder()
                .id(entity.getId())
                .clientId(entity.getClientId())
                .eventTypes(entity.getEventTypes() != null ? 
                           List.copyOf(entity.getEventTypes()) : List.of())
                .webhookUrl(entity.getWebhookUrl())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

