package com.cobre.notifier.api.infrastructure.kafka.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para Fase 5: Infrastructure - Kafka - KafkaEventMessage
 * Valida la deserialización de mensajes de Kafka.
 */
@DisplayName("Fase 5: Infrastructure - Kafka - KafkaEventMessage Tests")
class KafkaEventMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should deserialize Kafka message correctly")
    void shouldDeserializeKafkaMessageCorrectly() throws Exception {
        // Given
        String json = """
                {
                    "client_id": "client-123",
                    "event_type": "payment.completed",
                    "payload": "{\\"amount\\":100.0}",
                    "timestamp": 1234567890
                }
                """;

        // When
        KafkaEventMessage message = objectMapper.readValue(json, KafkaEventMessage.class);

        // Then
        assertThat(message.getClientId()).isEqualTo("client-123");
        assertThat(message.getEventType()).isEqualTo("payment.completed");
        assertThat(message.getPayload()).isEqualTo("{\"amount\":100.0}");
        assertThat(message.getTimestamp()).isEqualTo(1234567890L);
    }

    @Test
    @DisplayName("Should handle null values in Kafka message")
    void shouldHandleNullValuesInKafkaMessage() throws Exception {
        // Given
        String json = """
                {
                    "client_id": null,
                    "event_type": null,
                    "payload": null,
                    "timestamp": null
                }
                """;

        // When
        KafkaEventMessage message = objectMapper.readValue(json, KafkaEventMessage.class);

        // Then
        assertThat(message.getClientId()).isNull();
        assertThat(message.getEventType()).isNull();
        assertThat(message.getPayload()).isNull();
        assertThat(message.getTimestamp()).isNull();
    }

    @Test
    @DisplayName("Should serialize Kafka message correctly")
    void shouldSerializeKafkaMessageCorrectly() throws Exception {
        // Given
        KafkaEventMessage message = KafkaEventMessage.builder()
                .clientId("client-123")
                .eventType("payment.completed")
                .payload("{\"amount\":100.0}")
                .timestamp(1234567890L)
                .build();

        // When
        String json = objectMapper.writeValueAsString(message);

        // Then
        assertThat(json).contains("client-123");
        assertThat(json).contains("payment.completed");
        assertThat(json).contains("client_id");
        assertThat(json).contains("event_type");
    }
}

