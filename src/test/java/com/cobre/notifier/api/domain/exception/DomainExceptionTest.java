package com.cobre.notifier.api.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain Exception Tests")
class DomainExceptionTest {

    @Test
    @DisplayName("Should create NotificationNotFoundException with UUID")
    void shouldCreateNotificationNotFoundExceptionWithUUID() {
        // Given
        UUID notificationId = UUID.randomUUID();

        // When
        NotificationNotFoundException exception = new NotificationNotFoundException(notificationId);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(notificationId.toString());
        assertThat(exception.getMessage()).contains("not found");
    }

    @Test
    @DisplayName("Should create NotificationNotFoundException with message")
    void shouldCreateNotificationNotFoundExceptionWithMessage() {
        // Given
        String message = "Custom error message";

        // When
        NotificationNotFoundException exception = new NotificationNotFoundException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should create SubscriptionNotFoundException with UUID")
    void shouldCreateSubscriptionNotFoundExceptionWithUUID() {
        // Given
        UUID subscriptionId = UUID.randomUUID();

        // When
        SubscriptionNotFoundException exception = new SubscriptionNotFoundException(subscriptionId);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(subscriptionId.toString());
        assertThat(exception.getMessage()).contains("not found");
    }

    @Test
    @DisplayName("Should create SubscriptionNotFoundException with clientId")
    void shouldCreateSubscriptionNotFoundExceptionWithClientId() {
        // Given
        String clientId = "client-123";

        // When
        SubscriptionNotFoundException exception = new SubscriptionNotFoundException(clientId);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(clientId);
        assertThat(exception.getMessage()).contains("not found");
    }

    @Test
    @DisplayName("Should create SubscriptionNotFoundException with message and cause")
    void shouldCreateSubscriptionNotFoundExceptionWithMessageAndCause() {
        // Given
        String message = "Custom error message";
        Throwable cause = new RuntimeException("Root cause");

        // When
        SubscriptionNotFoundException exception = new SubscriptionNotFoundException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should create InvalidNotificationStateException with status and operation")
    void shouldCreateInvalidNotificationStateExceptionWithStatusAndOperation() {
        // Given
        com.cobre.notifier.api.domain.DeliveryStatus status = com.cobre.notifier.api.domain.DeliveryStatus.SENT;
        String operation = "replay";

        // When
        InvalidNotificationStateException exception = 
                new InvalidNotificationStateException(status, operation);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(status.toString());
        assertThat(exception.getMessage()).contains(operation);
    }

    @Test
    @DisplayName("Should create InvalidNotificationStateException with message")
    void shouldCreateInvalidNotificationStateExceptionWithMessage() {
        // Given
        String message = "Custom error message";

        // When
        InvalidNotificationStateException exception = 
                new InvalidNotificationStateException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should create InvalidNotificationStateException with message and cause")
    void shouldCreateInvalidNotificationStateExceptionWithMessageAndCause() {
        // Given
        String message = "Custom error message";
        Throwable cause = new RuntimeException("Root cause");

        // When
        InvalidNotificationStateException exception = 
                new InvalidNotificationStateException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should create InvalidSubscriptionException with message")
    void shouldCreateInvalidSubscriptionExceptionWithMessage() {
        // Given
        String message = "Custom error message";

        // When
        InvalidSubscriptionException exception = new InvalidSubscriptionException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should create InvalidSubscriptionException with message and cause")
    void shouldCreateInvalidSubscriptionExceptionWithMessageAndCause() {
        // Given
        String message = "Custom error message";
        Throwable cause = new RuntimeException("Root cause");

        // When
        InvalidSubscriptionException exception = new InvalidSubscriptionException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Should create InvalidSubscriptionException for inactive subscription")
    void shouldCreateInvalidSubscriptionExceptionForInactiveSubscription() {
        // Given
        String clientId = "client-123";

        // When
        InvalidSubscriptionException exception = 
                InvalidSubscriptionException.inactiveSubscription(clientId);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(clientId);
        assertThat(exception.getMessage()).contains("not active");
    }

    @Test
    @DisplayName("Should create InvalidSubscriptionException for missing webhook URL")
    void shouldCreateInvalidSubscriptionExceptionForMissingWebhookUrl() {
        // Given
        String clientId = "client-456";

        // When
        InvalidSubscriptionException exception = 
                InvalidSubscriptionException.missingWebhookUrl(clientId);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(clientId);
        assertThat(exception.getMessage()).contains("webhook URL");
    }

    @Test
    @DisplayName("Should create InvalidSubscriptionException for no event types")
    void shouldCreateInvalidSubscriptionExceptionForNoEventTypes() {
        // Given
        String clientId = "client-789";

        // When
        InvalidSubscriptionException exception = 
                InvalidSubscriptionException.noEventTypes(clientId);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(clientId);
        assertThat(exception.getMessage()).contains("event types");
    }

    @Test
    @DisplayName("Should create NotificationDeliveryException with webhook URL and message")
    void shouldCreateNotificationDeliveryExceptionWithWebhookUrlAndMessage() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        String message = "Connection timeout";

        // When
        NotificationDeliveryException exception = 
                new NotificationDeliveryException(webhookUrl, message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(webhookUrl);
        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getWebhookUrl()).isEqualTo(webhookUrl);
        assertThat(exception.getStatusCode()).isNull();
    }

    @Test
    @DisplayName("Should create NotificationDeliveryException with webhook URL, status code and message")
    void shouldCreateNotificationDeliveryExceptionWithWebhookUrlStatusCodeAndMessage() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        Integer statusCode = 500;
        String message = "Internal server error";

        // When
        NotificationDeliveryException exception = 
                new NotificationDeliveryException(webhookUrl, statusCode, message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(webhookUrl);
        assertThat(exception.getMessage()).contains(statusCode.toString());
        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getWebhookUrl()).isEqualTo(webhookUrl);
        assertThat(exception.getStatusCode()).isEqualTo(statusCode);
    }

    @Test
    @DisplayName("Should create NotificationDeliveryException with webhook URL and cause")
    void shouldCreateNotificationDeliveryExceptionWithWebhookUrlAndCause() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        Throwable cause = new RuntimeException("Network error");

        // When
        NotificationDeliveryException exception = 
                new NotificationDeliveryException(webhookUrl, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(webhookUrl);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getWebhookUrl()).isEqualTo(webhookUrl);
        assertThat(exception.getStatusCode()).isNull();
    }
}

