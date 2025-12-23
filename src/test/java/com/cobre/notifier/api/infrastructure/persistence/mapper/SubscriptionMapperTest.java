package com.cobre.notifier.api.infrastructure.persistence.mapper;

import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.infrastructure.persistence.entity.SubscriptionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para Fase 4: Infrastructure - Persistencia - SubscriptionMapper
 * Valida la conversión entre entidades de dominio y entidades JPA.
 */
@DisplayName("Fase 4: Infrastructure - Persistencia - SubscriptionMapper Tests")
class SubscriptionMapperTest {

    private final SubscriptionMapper mapper = new SubscriptionMapper();

    private static final UUID ID = UUID.randomUUID();
    private static final String CLIENT_ID = "client-123";
    private static final List<String> EVENT_TYPES = List.of("payment.completed", "payment.failed");
    private static final String WEBHOOK_URL = "https://example.com/webhook";
    private static final long NOW = System.currentTimeMillis();

    @Test
    @DisplayName("Should map domain to entity correctly")
    void shouldMapDomainToEntityCorrectly() {
        // Given
        Subscription domain = Subscription.builder()
                .id(ID)
                .clientId(CLIENT_ID)
                .eventTypes(EVENT_TYPES)
                .webhookUrl(WEBHOOK_URL)
                .isActive(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        // When
        SubscriptionEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(entity.getEventTypes()).containsExactlyElementsOf(EVENT_TYPES);
        assertThat(entity.getWebhookUrl()).isEqualTo(WEBHOOK_URL);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Should map entity to domain correctly")
    void shouldMapEntityToDomainCorrectly() {
        // Given
        SubscriptionEntity entity = SubscriptionEntity.builder()
                .id(ID)
                .clientId(CLIENT_ID)
                .eventTypes(EVENT_TYPES)
                .webhookUrl(WEBHOOK_URL)
                .isActive(false)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        // When
        Subscription domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getId()).isEqualTo(ID);
        assertThat(domain.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(domain.getEventTypes()).containsExactlyElementsOf(EVENT_TYPES);
        assertThat(domain.getWebhookUrl()).isEqualTo(WEBHOOK_URL);
        assertThat(domain.getIsActive()).isFalse();
        assertThat(domain.getCreatedAt()).isEqualTo(NOW);
        assertThat(domain.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Should handle null event types in domain")
    void shouldHandleNullEventTypesInDomain() {
        // Given
        Subscription domain = Subscription.builder()
                .id(ID)
                .clientId(CLIENT_ID)
                .eventTypes(null)
                .webhookUrl(WEBHOOK_URL)
                .isActive(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        // When
        SubscriptionEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getEventTypes()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null event types in entity")
    void shouldHandleNullEventTypesInEntity() {
        // Given
        SubscriptionEntity entity = SubscriptionEntity.builder()
                .id(ID)
                .clientId(CLIENT_ID)
                .eventTypes(null)
                .webhookUrl(WEBHOOK_URL)
                .isActive(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        // When
        Subscription domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getEventTypes()).isEmpty();
    }

    @Test
    @DisplayName("Should return null when domain is null")
    void shouldReturnNullWhenDomainIsNull() {
        // When
        SubscriptionEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        // When
        Subscription domain = mapper.toDomain(null);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should create immutable copy of event types")
    void shouldCreateImmutableCopyOfEventTypes() {
        // Given
        Subscription domain = Subscription.builder()
                .id(ID)
                .clientId(CLIENT_ID)
                .eventTypes(EVENT_TYPES)
                .webhookUrl(WEBHOOK_URL)
                .isActive(true)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

        // When
        SubscriptionEntity entity = mapper.toEntity(domain);
        Subscription mappedBack = mapper.toDomain(entity);

        // Then
        assertThat(mappedBack.getEventTypes()).containsExactlyElementsOf(EVENT_TYPES);
        // Verificar que es una copia inmutable
        assertThat(mappedBack.getEventTypes()).isNotSameAs(EVENT_TYPES);
    }
}

