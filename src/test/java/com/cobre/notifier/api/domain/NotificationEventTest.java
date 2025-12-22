package com.cobre.notifier.api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para Fase 2: Domain Layer - NotificationEvent
 * Valida la lógica de negocio de la entidad NotificationEvent.
 */
@DisplayName("Fase 2: Domain Layer - NotificationEvent Tests")
class NotificationEventTest {

    private static final String CLIENT_ID = "client-123";
    private static final String EVENT_TYPE = "payment.completed";
    private static final String PAYLOAD = "{\"amount\":100.0}";
    private static final String WEBHOOK_URL = "https://example.com/webhook";

    @Test
    @DisplayName("Should create notification event with correct initial state")
    void shouldCreateNotificationEventWithCorrectInitialState() {
        // When
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);

        // Then
        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(notification.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(notification.getPayload()).isEqualTo(PAYLOAD);
        assertThat(notification.getWebhookUrl()).isEqualTo(WEBHOOK_URL);
        assertThat(notification.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(notification.getRetryCount()).isEqualTo(0);
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
        assertThat(notification.getSentAt()).isNull();
        assertThat(notification.getFailedAt()).isNull();
    }

    @Test
    @DisplayName("Should mark notification as sent")
    void shouldMarkNotificationAsSent() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        LocalDateTime beforeUpdate = notification.getUpdatedAt();
        
        // When
        notification.markAsSent("200", "OK");

        // Then
        assertThat(notification.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isAfter(beforeUpdate);
        assertThat(notification.getResponseCode()).isEqualTo("200");
        assertThat(notification.getResponseBody()).isEqualTo("OK");
        assertThat(notification.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should mark notification as failed")
    void shouldMarkNotificationAsFailed() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        LocalDateTime beforeUpdate = notification.getUpdatedAt();
        String errorMessage = "Connection timeout";

        // When
        notification.markAsFailed(errorMessage);

        // Then
        assertThat(notification.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(notification.getFailedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isAfter(beforeUpdate);
        assertThat(notification.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("Should increment retry count and set status to RETRYING when under max")
    void shouldIncrementRetryCountAndSetRetryingWhenUnderMax() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        int maxRetries = 3;

        // When
        boolean canContinue = notification.incrementRetry(maxRetries);

        // Then
        assertThat(canContinue).isTrue();
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getStatus()).isEqualTo(DeliveryStatus.RETRYING);
    }

    @Test
    @DisplayName("Should mark as failed when max retries reached")
    void shouldMarkAsFailedWhenMaxRetriesReached() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        int maxRetries = 3;
        
        // Incrementar hasta llegar al máximo (maxRetries veces)
        // Primera vez: retryCount = 1, status = RETRYING
        notification.incrementRetry(maxRetries);
        // Segunda vez: retryCount = 2, status = RETRYING
        notification.incrementRetry(maxRetries);
        // Tercera vez: retryCount = 3, status = FAILED (alcanzó el máximo)

        // When
        boolean canContinue = notification.incrementRetry(maxRetries);

        // Then
        assertThat(canContinue).isFalse();
        assertThat(notification.getRetryCount()).isEqualTo(maxRetries);
        assertThat(notification.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(notification.getFailedAt()).isNotNull();
        assertThat(notification.getErrorMessage()).contains("Maximum retry attempts");
    }

    @Test
    @DisplayName("Should return true for canRetry when status is PENDING")
    void shouldReturnTrueForCanRetryWhenStatusIsPending() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);

        // When/Then
        assertThat(notification.canRetry(3)).isTrue();
    }

    @Test
    @DisplayName("Should return true for canRetry when status is RETRYING")
    void shouldReturnTrueForCanRetryWhenStatusIsRetrying() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        notification.incrementRetry(3);

        // When/Then
        assertThat(notification.canRetry(3)).isTrue();
    }

    @Test
    @DisplayName("Should return false for canRetry when status is SENT")
    void shouldReturnFalseForCanRetryWhenStatusIsSent() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        notification.markAsSent("200", "OK");

        // When/Then
        assertThat(notification.canRetry(3)).isFalse();
    }

    @Test
    @DisplayName("Should return true for belongsToClient when client matches")
    void shouldReturnTrueForBelongsToClientWhenClientMatches() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);

        // When/Then
        assertThat(notification.belongsToClient(CLIENT_ID)).isTrue();
    }

    @Test
    @DisplayName("Should return false for belongsToClient when client does not match")
    void shouldReturnFalseForBelongsToClientWhenClientDoesNotMatch() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);

        // When/Then
        assertThat(notification.belongsToClient("other-client")).isFalse();
    }

    @Test
    @DisplayName("Should return true for isInFinalState when status is SENT")
    void shouldReturnTrueForIsInFinalStateWhenStatusIsSent() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        notification.markAsSent("200", "OK");

        // When/Then
        assertThat(notification.isInFinalState()).isTrue();
    }

    @Test
    @DisplayName("Should return true for isInFinalState when status is FAILED")
    void shouldReturnTrueForIsInFinalStateWhenStatusIsFailed() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        notification.markAsFailed("Error");

        // When/Then
        assertThat(notification.isInFinalState()).isTrue();
    }

    @Test
    @DisplayName("Should return false for isInFinalState when status is PENDING")
    void shouldReturnFalseForIsInFinalStateWhenStatusIsPending() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);

        // When/Then
        assertThat(notification.isInFinalState()).isFalse();
    }

    @Test
    @DisplayName("Should return true for isPending when status is PENDING")
    void shouldReturnTrueForIsPendingWhenStatusIsPending() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);

        // When/Then
        assertThat(notification.isPending()).isTrue();
    }

    @Test
    @DisplayName("Should return true for isPending when status is RETRYING")
    void shouldReturnTrueForIsPendingWhenStatusIsRetrying() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        notification.incrementRetry(3);

        // When/Then
        assertThat(notification.isPending()).isTrue();
    }

    @Test
    @DisplayName("Should reset notification for replay")
    void shouldResetNotificationForReplay() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        notification.markAsFailed("Error");
        notification.incrementRetry(3);

        // When
        notification.resetForReplay();

        // Then
        assertThat(notification.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(notification.getRetryCount()).isEqualTo(0);
        assertThat(notification.getSentAt()).isNull();
        assertThat(notification.getFailedAt()).isNull();
        assertThat(notification.getErrorMessage()).isNull();
        assertThat(notification.getResponseCode()).isNull();
        assertThat(notification.getResponseBody()).isNull();
    }

    @Test
    @DisplayName("Should update error message")
    void shouldUpdateErrorMessage() {
        // Given
        NotificationEvent notification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        LocalDateTime beforeUpdate = notification.getUpdatedAt();

        // When
        notification.updateErrorMessage("New error message");

        // Then
        assertThat(notification.getErrorMessage()).isEqualTo("New error message");
        assertThat(notification.getUpdatedAt()).isAfter(beforeUpdate);
    }
}

