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
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .payload("Payment received for $150.00")
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
        assertThat(response.getEventId()).isEqualTo(id.toString());
        assertThat(response.getClientId()).isEqualTo("CLIENT001");
        assertThat(response.getEventType()).isEqualTo("credit_card_payment");
        assertThat(response.getContent()).isEqualTo("Payment received for $150.00");
        assertThat(response.getDeliveryStatus()).isEqualTo("completed");
        assertThat(response.getDeliveryDate()).isNotNull();
        assertThat(response.getDeliveryDate()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z");
    }

    @Test
    @DisplayName("Should map NotificationEvent with FAILED status to failed delivery_status")
    void shouldMapNotificationEventWithFailedState() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("CLIENT002")
                .eventType("credit_transfer")
                .payload("Payment failed: insufficient funds")
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
        assertThat(response.getEventId()).isEqualTo(id.toString());
        assertThat(response.getClientId()).isEqualTo("CLIENT002");
        assertThat(response.getEventType()).isEqualTo("credit_transfer");
        assertThat(response.getContent()).isEqualTo("Payment failed: insufficient funds");
        assertThat(response.getDeliveryStatus()).isEqualTo("failed");
        // When failed, delivery_date should use createdAt since sentAt is null
        assertThat(response.getDeliveryDate()).isNotNull();
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
    @DisplayName("Should map NotificationEvent with RETRYING status to pending delivery_status")
    void shouldMapNotificationEventWithRetryingStatus() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("CLIENT003")
                .eventType("debit_automatic_payment")
                .payload("Payment is being processed")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.RETRYING)
                .retryCount(2)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(id.toString());
        assertThat(response.getClientId()).isEqualTo("CLIENT003");
        assertThat(response.getEventType()).isEqualTo("debit_automatic_payment");
        assertThat(response.getContent()).isEqualTo("Payment is being processed");
        assertThat(response.getDeliveryStatus()).isEqualTo("pending");
        assertThat(response.getDeliveryDate()).isNotNull();
    }

    @Test
    @DisplayName("Should map NotificationEvent with PENDING status to pending delivery_status")
    void shouldMapNotificationEventWithPendingStatus() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("CLIENT004")
                .eventType("credit_transfer")
                .payload("Bank transfer received from Account #4567 for $1,500.00")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.PENDING)
                .retryCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDeliveryStatus()).isEqualTo("pending");
        assertThat(response.getDeliveryDate()).isNotNull();
    }

    @Test
    @DisplayName("Should use sentAt for delivery_date when available")
    void shouldUseSentAtForDeliveryDateWhenAvailable() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 3, 15, 10, 0, 0);
        LocalDateTime sentAt = LocalDateTime.of(2024, 3, 15, 14, 30, 55);
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("CLIENT005")
                .eventType("debit_transfer")
                .payload("Money transfer sent to Account #8901 for $500.00")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.SENT)
                .retryCount(0)
                .createdAt(createdAt)
                .updatedAt(sentAt)
                .sentAt(sentAt)
                .build();

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDeliveryDate()).isNotNull();
        // Verify it uses sentAt - check that delivery_date is not null and formatted correctly
        String deliveryDate = response.getDeliveryDate();
        assertThat(deliveryDate).isNotNull();
        assertThat(deliveryDate).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z");
        // Verify it's different from createdAt (which would be used if sentAt was null)
        assertThat(response.getDeliveryDate()).isNotEqualTo(
                createdAt.atZone(java.time.ZoneId.systemDefault())
                        .withZoneSameInstant(java.time.ZoneId.of("UTC"))
                        .format(java.time.format.DateTimeFormatter.ISO_INSTANT));
    }

    @Test
    @DisplayName("Should use createdAt for delivery_date when sentAt is null")
    void shouldUseCreatedAtForDeliveryDateWhenSentAtIsNull() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 3, 15, 11, 20, 18);
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("CLIENT006")
                .eventType("credit_refund")
                .payload("Refund processed for order #789 for $45.99")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.FAILED)
                .retryCount(0)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .sentAt(null)
                .build();

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDeliveryDate()).isNotNull();
        String deliveryDate = response.getDeliveryDate();
        // Verify format is correct (ISO-8601 with Z, converted to UTC)
        assertThat(deliveryDate).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z");
        // Verify it's based on createdAt (converted to UTC)
        String expectedDate = createdAt.atZone(java.time.ZoneId.systemDefault())
                .withZoneSameInstant(java.time.ZoneId.of("UTC"))
                .format(java.time.format.DateTimeFormatter.ISO_INSTANT);
        assertThat(deliveryDate).isEqualTo(expectedDate);
    }

    @Test
    @DisplayName("Should format delivery_date as ISO-8601 with Z")
    void shouldFormatDeliveryDateAsISO8601WithZ() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .id(id)
                .clientId("CLIENT007")
                .eventType("credit_deposit")
                .payload("Direct deposit received from Employer XYZ for $2,500.00")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.SENT)
                .retryCount(0)
                .createdAt(now)
                .updatedAt(now)
                .sentAt(now)
                .build();

        // When
        NotificationEventResponse response = mapper.toResponse(notificationEvent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDeliveryDate()).isNotNull();
        // Verify ISO-8601 format with Z (may include milliseconds)
        assertThat(response.getDeliveryDate()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z");
    }
}
