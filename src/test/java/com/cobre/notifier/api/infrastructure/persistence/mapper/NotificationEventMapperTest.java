package com.cobre.notifier.api.infrastructure.persistence.mapper;

import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.persistence.entity.NotificationEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para Fase 4: Infrastructure - Persistencia - NotificationEventMapper
 * Valida la conversión entre entidades de dominio y entidades JPA.
 */
@DisplayName("Fase 4: Infrastructure - Persistencia - NotificationEventMapper Tests")
class NotificationEventMapperTest {

    private final NotificationEventMapper mapper = new NotificationEventMapper();

    private static final UUID ID = UUID.randomUUID();
    private static final String CLIENT_ID = "client-123";
    private static final String EVENT_TYPE = "payment.completed";
    private static final String PAYLOAD = "{\"amount\":100.0}";
    private static final String WEBHOOK_URL = "https://example.com/webhook";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    @DisplayName("Should map domain to entity correctly")
    void shouldMapDomainToEntityCorrectly() {
        // Given
        NotificationEvent domain = NotificationEvent.builder()
                .id(ID)
                .clientId(CLIENT_ID)
                .eventType(EVENT_TYPE)
                .payload(PAYLOAD)
                .webhookUrl(WEBHOOK_URL)
                .status(DeliveryStatus.PENDING)
                .retryCount(0)
                .createdAt(NOW)
                .updatedAt(NOW)
                .sentAt(null)
                .failedAt(null)
                .errorMessage(null)
                .responseCode(null)
                .responseBody(null)
                .build();

        // When
        NotificationEventEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(entity.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(entity.getPayload()).isEqualTo(PAYLOAD);
        assertThat(entity.getWebhookUrl()).isEqualTo(WEBHOOK_URL);
        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(entity.getRetryCount()).isEqualTo(0);
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Should map entity to domain correctly")
    void shouldMapEntityToDomainCorrectly() {
        // Given
        NotificationEventEntity entity = NotificationEventEntity.builder()
                .id(ID)
                .clientId(CLIENT_ID)
                .eventType(EVENT_TYPE)
                .payload(PAYLOAD)
                .webhookUrl(WEBHOOK_URL)
                .status(DeliveryStatus.SENT)
                .retryCount(1)
                .createdAt(NOW)
                .updatedAt(NOW)
                .sentAt(NOW)
                .failedAt(null)
                .errorMessage(null)
                .responseCode("200")
                .responseBody("OK")
                .build();

        // When
        NotificationEvent domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getId()).isEqualTo(ID);
        assertThat(domain.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(domain.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(domain.getPayload()).isEqualTo(PAYLOAD);
        assertThat(domain.getWebhookUrl()).isEqualTo(WEBHOOK_URL);
        assertThat(domain.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(domain.getRetryCount()).isEqualTo(1);
        assertThat(domain.getResponseCode()).isEqualTo("200");
        assertThat(domain.getResponseBody()).isEqualTo("OK");
    }

    @Test
    @DisplayName("Should return null when domain is null")
    void shouldReturnNullWhenDomainIsNull() {
        // When
        NotificationEventEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        // When
        NotificationEvent domain = mapper.toDomain(null);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should map all status values correctly")
    void shouldMapAllStatusValuesCorrectly() {
        // Given
        DeliveryStatus[] statuses = {
                DeliveryStatus.PENDING,
                DeliveryStatus.SENT,
                DeliveryStatus.FAILED,
                DeliveryStatus.RETRYING
        };

        for (DeliveryStatus status : statuses) {
            NotificationEvent domain = NotificationEvent.builder()
                    .id(ID)
                    .clientId(CLIENT_ID)
                    .eventType(EVENT_TYPE)
                    .payload(PAYLOAD)
                    .webhookUrl(WEBHOOK_URL)
                    .status(status)
                    .retryCount(0)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .build();

            // When
            NotificationEventEntity entity = mapper.toEntity(domain);
            NotificationEvent mappedBack = mapper.toDomain(entity);

            // Then
            assertThat(mappedBack.getStatus()).isEqualTo(status);
        }
    }
}

