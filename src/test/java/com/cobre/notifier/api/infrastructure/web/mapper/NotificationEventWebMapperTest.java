package com.cobre.notifier.api.infrastructure.web.mapper;

import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationEventWebMapper Tests")
class NotificationEventWebMapperTest {

    private NotificationEventWebMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationEventWebMapper();
    }

    @Test
    @DisplayName("Should map NotificationEvent to NotificationEventResponse")
    void shouldMapNotificationEventToResponse() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("client-123")
                .eventType("payment.completed")
                .payload("{\"amount\": 1000}")
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

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getClientId()).isEqualTo("client-123");
        assertThat(response.getEventType()).isEqualTo("payment.completed");
        assertThat(response.getPayload()).isEqualTo("{\"amount\": 1000}");
        assertThat(response.getWebhookUrl()).isEqualTo("https://example.com/webhook");
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(response.getRetryCount()).isEqualTo(0);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
        assertThat(response.getSentAt()).isEqualTo(now);
        assertThat(response.getFailedAt()).isNull();
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getResponseCode()).isEqualTo("200");
        assertThat(response.getResponseBody()).isEqualTo("OK");
    }

    @Test
    @DisplayName("Should map NotificationEvent with all fields including failed state")
    void shouldMapNotificationEventWithFailedState() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("client-456")
                .eventType("payment.failed")
                .payload("{\"error\": \"insufficient_funds\"}")
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

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(response.getRetryCount()).isEqualTo(3);
        assertThat(response.getFailedAt()).isEqualTo(now);
        assertThat(response.getErrorMessage()).isEqualTo("Maximum retry attempts reached");
        assertThat(response.getSentAt()).isNull();
        assertThat(response.getResponseCode()).isNull();
        assertThat(response.getResponseBody()).isNull();
    }

    @Test
    @DisplayName("Should return null when NotificationEvent is null")
    void shouldReturnNullWhenNotificationEventIsNull() {
        // When
        NotificationEventResponse response = mapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should map NotificationEvent with RETRYING status")
    void shouldMapNotificationEventWithRetryingStatus() {
        // Given
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .clientId("client-789")
                .eventType("payment.pending")
                .payload("{\"status\": \"processing\"}")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.RETRYING)
                .retryCount(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.RETRYING);
        assertThat(response.getRetryCount()).isEqualTo(2);
    }
}

