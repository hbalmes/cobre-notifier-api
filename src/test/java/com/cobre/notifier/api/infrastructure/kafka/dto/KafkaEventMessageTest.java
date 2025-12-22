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
                    "event_type": "credit_card_payment",
                    "content": "Credit card payment received for $150.00"
                }
                """;

        // When
        KafkaEventMessage message = objectMapper.readValue(json, KafkaEventMessage.class);

        // Then
        assertThat(message.getClientId()).isEqualTo("client-123");
        assertThat(message.getEventType()).isEqualTo("credit_card_payment");
        assertThat(message.getContent()).isEqualTo("Credit card payment received for $150.00");
    }

    @Test
    @DisplayName("Should handle null values in Kafka message")
    void shouldHandleNullValuesInKafkaMessage() throws Exception {
        // Given
        String json = """
                {
                    "client_id": null,
                    "event_type": null,
                    "content": null
                }
                """;

        // When
        KafkaEventMessage message = objectMapper.readValue(json, KafkaEventMessage.class);

        // Then
        assertThat(message.getClientId()).isNull();
        assertThat(message.getEventType()).isNull();
        assertThat(message.getContent()).isNull();
    }

    @Test
    @DisplayName("Should serialize Kafka message correctly")
    void shouldSerializeKafkaMessageCorrectly() throws Exception {
        // Given
        KafkaEventMessage message = KafkaEventMessage.builder()
                .clientId("client-123")
                .eventType("payment.completed")
                .content("Credit card payment received for $150.00")
                .build();

        // When
        String json = objectMapper.writeValueAsString(message);

        // Then
        assertThat(json).contains("client-123");
        assertThat(json).contains("payment.completed");
        assertThat(json).contains("client_id");
        assertThat(json).contains("event_type");
    }

    @Test
    @DisplayName("Should create KafkaEventMessage with no-args constructor")
    void shouldCreateKafkaEventMessageWithNoArgsConstructor() {
        // When
        KafkaEventMessage message = new KafkaEventMessage();

        // Then
        assertThat(message).isNotNull();
        assertThat(message.getClientId()).isNull();
        assertThat(message.getEventType()).isNull();
        assertThat(message.getContent()).isNull();
    }

    @Test
    @DisplayName("Should create KafkaEventMessage with all-args constructor")
    void shouldCreateKafkaEventMessageWithAllArgsConstructor() {
        // Given
        String clientId = "client-456";
        String eventType = "credit_transfer";
        String content = "Bank transfer received from Account #4567 for $1,500.00";

        // When
        KafkaEventMessage message = new KafkaEventMessage(clientId, eventType, content);

        // Then
        assertThat(message.getClientId()).isEqualTo(clientId);
        assertThat(message.getEventType()).isEqualTo(eventType);
        assertThat(message.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        KafkaEventMessage message = new KafkaEventMessage();
        String clientId = "client-789";
        String eventType = "debit_automatic_payment";
        String content = "Monthly utility bill payment of $85.50";

        // When
        message.setClientId(clientId);
        message.setEventType(eventType);
        message.setContent(content);

        // Then
        assertThat(message.getClientId()).isEqualTo(clientId);
        assertThat(message.getEventType()).isEqualTo(eventType);
        assertThat(message.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should deserialize with partial fields")
    void shouldDeserializeWithPartialFields() throws Exception {
        // Given
        String json = """
                {
                    "client_id": "client-999",
                    "event_type": "credit_refund"
                }
                """;

        // When
        KafkaEventMessage message = objectMapper.readValue(json, KafkaEventMessage.class);

        // Then
        assertThat(message.getClientId()).isEqualTo("client-999");
        assertThat(message.getEventType()).isEqualTo("credit_refund");
        assertThat(message.getContent()).isNull();
    }

    @Test
    @DisplayName("Should serialize with null values")
    void shouldSerializeWithNullValues() throws Exception {
        // Given
        KafkaEventMessage message = new KafkaEventMessage();
        message.setClientId("client-123");
        message.setEventType(null);
        message.setContent(null);

        // When
        String json = objectMapper.writeValueAsString(message);

        // Then
        assertThat(json).contains("client-123");
        assertThat(json).contains("client_id");
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        KafkaEventMessage message1 = KafkaEventMessage.builder()
                .clientId("client-123")
                .eventType("payment.completed")
                .content("Credit card payment received for $150.00")
                .build();

        KafkaEventMessage message2 = KafkaEventMessage.builder()
                .clientId("client-123")
                .eventType("payment.completed")
                .content("Credit card payment received for $150.00")
                .build();

        KafkaEventMessage message3 = KafkaEventMessage.builder()
                .clientId("client-456")
                .eventType("payment.completed")
                .content("Credit card payment received for $150.00")
                .build();

        // Then
        assertThat(message1).isEqualTo(message2);
        assertThat(message1.hashCode()).isEqualTo(message2.hashCode());
        assertThat(message1).isNotEqualTo(message3);
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        KafkaEventMessage message = KafkaEventMessage.builder()
                .clientId("client-123")
                .eventType("credit_card_payment")
                .content("Credit card payment received for $150.00")
                .build();

        // When
        String toString = message.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("client-123");
        assertThat(toString).contains("credit_card_payment");
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() throws Exception {
        // Given
        String json = """
                {
                    "client_id": "",
                    "event_type": "",
                    "content": ""
                }
                """;

        // When
        KafkaEventMessage message = objectMapper.readValue(json, KafkaEventMessage.class);

        // Then
        assertThat(message.getClientId()).isEmpty();
        assertThat(message.getEventType()).isEmpty();
        assertThat(message.getContent()).isNotNull();
        assertThat(message.getContent()).isEmpty();
    }

}

