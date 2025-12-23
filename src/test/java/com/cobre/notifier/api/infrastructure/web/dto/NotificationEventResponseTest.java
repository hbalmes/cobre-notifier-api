package com.cobre.notifier.api.infrastructure.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationEventResponse Tests")
class NotificationEventResponseTest {

    @Test
    @DisplayName("Should create NotificationEventResponse with no-args constructor")
    void shouldCreateNotificationEventResponseWithNoArgsConstructor() {
        // When
        NotificationEventResponse response = new NotificationEventResponse();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isNull();
        assertThat(response.getClientId()).isNull();
        assertThat(response.getEventType()).isNull();
        assertThat(response.getContent()).isNull();
        assertThat(response.getDeliveryDate()).isNull();
        assertThat(response.getDeliveryStatus()).isNull();
    }

    @Test
    @DisplayName("Should create NotificationEventResponse with all-args constructor")
    void shouldCreateNotificationEventResponseWithAllArgsConstructor() {
        // Given
        String eventId = "123e4567-e89b-12d3-a456-426614174000";
        String clientId = "CLIENT001";
        String eventType = "credit_card_payment";
        String content = "Payment received for $150.00";
        String deliveryDate = "2024-03-15T14:30:55Z";
        String deliveryStatus = "completed";

        // When
        NotificationEventResponse response = new NotificationEventResponse(
                eventId, eventType, content, deliveryDate, deliveryStatus, clientId
        );

        // Then
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getClientId()).isEqualTo(clientId);
        assertThat(response.getEventType()).isEqualTo(eventType);
        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getDeliveryDate()).isEqualTo(deliveryDate);
        assertThat(response.getDeliveryStatus()).isEqualTo(deliveryStatus);
    }

    @Test
    @DisplayName("Should create NotificationEventResponse with builder")
    void shouldCreateNotificationEventResponseWithBuilder() {
        // Given
        String eventId = "123e4567-e89b-12d3-a456-426614174000";

        // When
        NotificationEventResponse response = NotificationEventResponse.builder()
                .eventId(eventId)
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .content("Payment received for $150.00")
                .deliveryDate("2024-03-15T14:30:55Z")
                .deliveryStatus("completed")
                .build();

        // Then
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getClientId()).isEqualTo("CLIENT001");
        assertThat(response.getEventType()).isEqualTo("credit_card_payment");
        assertThat(response.getContent()).isEqualTo("Payment received for $150.00");
        assertThat(response.getDeliveryDate()).isEqualTo("2024-03-15T14:30:55Z");
        assertThat(response.getDeliveryStatus()).isEqualTo("completed");
    }

    @Test
    @DisplayName("Should create NotificationEventResponse with builder and failed state")
    void shouldCreateNotificationEventResponseWithBuilderAndFailedState() {
        // Given
        String eventId = "123e4567-e89b-12d3-a456-426614174000";

        // When
        NotificationEventResponse response = NotificationEventResponse.builder()
                .eventId(eventId)
                .clientId("CLIENT002")
                .eventType("credit_transfer")
                .content("Payment failed: insufficient funds")
                .deliveryDate("2024-03-15T11:20:18Z")
                .deliveryStatus("failed")
                .build();

        // Then
        assertThat(response.getDeliveryStatus()).isEqualTo("failed");
        assertThat(response.getClientId()).isEqualTo("CLIENT002");
        assertThat(response.getEventType()).isEqualTo("credit_transfer");
        assertThat(response.getContent()).isEqualTo("Payment failed: insufficient funds");
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        NotificationEventResponse response = new NotificationEventResponse();
        String eventId = "123e4567-e89b-12d3-a456-426614174000";

        // When
        response.setEventId(eventId);
        response.setClientId("CLIENT003");
        response.setEventType("debit_automatic_payment");
        response.setContent("Payment is being processed");
        response.setDeliveryDate("2024-03-15T12:05:33Z");
        response.setDeliveryStatus("pending");

        // Then
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getClientId()).isEqualTo("CLIENT003");
        assertThat(response.getEventType()).isEqualTo("debit_automatic_payment");
        assertThat(response.getContent()).isEqualTo("Payment is being processed");
        assertThat(response.getDeliveryDate()).isEqualTo("2024-03-15T12:05:33Z");
        assertThat(response.getDeliveryStatus()).isEqualTo("pending");
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        String eventId = "123e4567-e89b-12d3-a456-426614174000";

        NotificationEventResponse response1 = NotificationEventResponse.builder()
                .eventId(eventId)
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .deliveryStatus("completed")
                .build();

        NotificationEventResponse response2 = NotificationEventResponse.builder()
                .eventId(eventId)
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .deliveryStatus("completed")
                .build();

        NotificationEventResponse response3 = NotificationEventResponse.builder()
                .eventId("987e6543-e21b-34d5-c678-901234567890")
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .deliveryStatus("completed")
                .build();

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1).isNotEqualTo(response3);
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        String eventId = "123e4567-e89b-12d3-a456-426614174000";
        NotificationEventResponse response = NotificationEventResponse.builder()
                .eventId(eventId)
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .deliveryStatus("completed")
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains(eventId);
        assertThat(toString).contains("CLIENT001");
        assertThat(toString).contains("credit_card_payment");
        assertThat(toString).contains("completed");
    }

    @Test
    @DisplayName("Should handle all delivery status values")
    void shouldHandleAllDeliveryStatusValues() {
        // Given & When & Then
        String[] statuses = {"completed", "failed", "pending"};
        
        for (String status : statuses) {
            NotificationEventResponse response = NotificationEventResponse.builder()
                    .eventId("123e4567-e89b-12d3-a456-426614174000")
                    .deliveryStatus(status)
                    .build();

            assertThat(response.getDeliveryStatus()).isEqualTo(status);
        }
    }
}
