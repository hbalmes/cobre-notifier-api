package com.cobre.notifier.api.infrastructure.web.dto;

import com.cobre.notifier.api.domain.DeliveryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

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
        assertThat(response.getId()).isNull();
        assertThat(response.getClientId()).isNull();
        assertThat(response.getEventType()).isNull();
        assertThat(response.getPayload()).isNull();
        assertThat(response.getWebhookUrl()).isNull();
        assertThat(response.getStatus()).isNull();
        assertThat(response.getRetryCount()).isNull();
    }

    @Test
    @DisplayName("Should create NotificationEventResponse with all-args constructor")
    void shouldCreateNotificationEventResponseWithAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        String clientId = "client-123";
        String eventType = "payment.completed";
        String payload = "{\"amount\":1000}";
        String webhookUrl = "https://example.com/webhook";
        DeliveryStatus status = DeliveryStatus.SENT;
        Integer retryCount = 0;

        // When
        NotificationEventResponse response = new NotificationEventResponse(
                id, clientId, eventType, payload, webhookUrl, status, retryCount,
                now, now, now, null, null, "200", "OK"
        );

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getClientId()).isEqualTo(clientId);
        assertThat(response.getEventType()).isEqualTo(eventType);
        assertThat(response.getPayload()).isEqualTo(payload);
        assertThat(response.getWebhookUrl()).isEqualTo(webhookUrl);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getRetryCount()).isEqualTo(retryCount);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
        assertThat(response.getSentAt()).isEqualTo(now);
        assertThat(response.getFailedAt()).isNull();
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getResponseCode()).isEqualTo("200");
        assertThat(response.getResponseBody()).isEqualTo("OK");
    }

    @Test
    @DisplayName("Should create NotificationEventResponse with builder")
    void shouldCreateNotificationEventResponseWithBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        NotificationEventResponse response = NotificationEventResponse.builder()
                .id(id)
                .clientId("client-123")
                .eventType("payment.completed")
                .payload("{\"amount\":1000}")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.SENT)
                .retryCount(0)
                .createdAt(now)
                .updatedAt(now)
                .sentAt(now)
                .failedAt(null)
                .errorMessage(null)
                .responseCode("200")
                .responseBody("OK")
                .build();

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getClientId()).isEqualTo("client-123");
        assertThat(response.getEventType()).isEqualTo("payment.completed");
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(response.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create NotificationEventResponse with builder and failed state")
    void shouldCreateNotificationEventResponseWithBuilderAndFailedState() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        NotificationEventResponse response = NotificationEventResponse.builder()
                .id(id)
                .clientId("client-456")
                .eventType("payment.failed")
                .payload("{\"error\":\"insufficient_funds\"}")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.FAILED)
                .retryCount(3)
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .sentAt(null)
                .failedAt(now)
                .errorMessage("Maximum retry attempts reached")
                .responseCode(null)
                .responseBody(null)
                .build();

        // Then
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(response.getRetryCount()).isEqualTo(3);
        assertThat(response.getFailedAt()).isEqualTo(now);
        assertThat(response.getErrorMessage()).isEqualTo("Maximum retry attempts reached");
        assertThat(response.getSentAt()).isNull();
        assertThat(response.getResponseCode()).isNull();
        assertThat(response.getResponseBody()).isNull();
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        NotificationEventResponse response = new NotificationEventResponse();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        response.setId(id);
        response.setClientId("client-789");
        response.setEventType("payment.pending");
        response.setPayload("{\"status\":\"processing\"}");
        response.setWebhookUrl("https://example.com/webhook");
        response.setStatus(DeliveryStatus.RETRYING);
        response.setRetryCount(2);
        response.setCreatedAt(now);
        response.setUpdatedAt(now);
        response.setSentAt(null);
        response.setFailedAt(null);
        response.setErrorMessage(null);
        response.setResponseCode(null);
        response.setResponseBody(null);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getClientId()).isEqualTo("client-789");
        assertThat(response.getEventType()).isEqualTo("payment.pending");
        assertThat(response.getPayload()).isEqualTo("{\"status\":\"processing\"}");
        assertThat(response.getWebhookUrl()).isEqualTo("https://example.com/webhook");
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.RETRYING);
        assertThat(response.getRetryCount()).isEqualTo(2);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        UUID id = UUID.randomUUID();

        NotificationEventResponse response1 = NotificationEventResponse.builder()
                .id(id)
                .clientId("client-123")
                .eventType("payment.completed")
                .status(DeliveryStatus.SENT)
                .build();

        NotificationEventResponse response2 = NotificationEventResponse.builder()
                .id(id)
                .clientId("client-123")
                .eventType("payment.completed")
                .status(DeliveryStatus.SENT)
                .build();

        NotificationEventResponse response3 = NotificationEventResponse.builder()
                .id(UUID.randomUUID())
                .clientId("client-123")
                .eventType("payment.completed")
                .status(DeliveryStatus.SENT)
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
        UUID id = UUID.randomUUID();
        NotificationEventResponse response = NotificationEventResponse.builder()
                .id(id)
                .clientId("client-123")
                .eventType("payment.completed")
                .status(DeliveryStatus.SENT)
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains(id.toString());
        assertThat(toString).contains("client-123");
        assertThat(toString).contains("payment.completed");
        assertThat(toString).contains("SENT");
    }

    @Test
    @DisplayName("Should handle all DeliveryStatus values")
    void shouldHandleAllDeliveryStatusValues() {
        // Given & When & Then
        for (DeliveryStatus status : DeliveryStatus.values()) {
            NotificationEventResponse response = NotificationEventResponse.builder()
                    .id(UUID.randomUUID())
                    .status(status)
                    .build();

            assertThat(response.getStatus()).isEqualTo(status);
        }
    }
}

