package com.cobre.notifier.api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para Subscription.
 * Mapea la entidad de dominio a la base de datos.
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_client_id", columnList = "client_id"),
    @Index(name = "idx_subscription_active", columnList = "is_active"),
    @Index(name = "idx_subscription_client_active", columnList = "client_id, is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "client_id", nullable = false, unique = true, length = 255)
    private String clientId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_event_types", 
                     joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "event_type", length = 100)
    private List<String> eventTypes;

    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}

