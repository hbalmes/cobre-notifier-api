package com.cobre.notifier.api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para Fase 2: Domain Layer - Subscription
 * Valida la lógica de negocio de la entidad Subscription.
 */
@DisplayName("Fase 2: Domain Layer - Subscription Tests")
class SubscriptionTest {

    private static final String CLIENT_ID = "client-123";
    private static final List<String> EVENT_TYPES = List.of("payment.completed", "payment.failed");
    private static final String WEBHOOK_URL = "https://example.com/webhook";

    @Test
    @DisplayName("Should create subscription with correct initial state")
    void shouldCreateSubscriptionWithCorrectInitialState() {
        // When
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // Then
        assertThat(subscription.getId()).isNotNull();
        assertThat(subscription.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(subscription.getEventTypes()).containsExactlyElementsOf(EVENT_TYPES);
        assertThat(subscription.getWebhookUrl()).isEqualTo(WEBHOOK_URL);
        assertThat(subscription.getIsActive()).isTrue();
        assertThat(subscription.getCreatedAt()).isPositive();
        assertThat(subscription.getUpdatedAt()).isPositive();
        assertThat(subscription.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should create subscription with empty event types list when null")
    void shouldCreateSubscriptionWithEmptyEventTypesWhenNull() {
        // When
        Subscription subscription = Subscription.create(CLIENT_ID, null, WEBHOOK_URL);

        // Then
        assertThat(subscription.getEventTypes()).isEmpty();
    }

    @Test
    @DisplayName("Should activate subscription")
    void shouldActivateSubscription() throws InterruptedException {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);
        subscription.deactivate();
        Thread.sleep(1); // Pequeño delay para asegurar diferencia de tiempo
        long beforeUpdate = subscription.getUpdatedAt();

        // When
        subscription.activate();

        // Then
        assertThat(subscription.getIsActive()).isTrue();
        assertThat(subscription.isActive()).isTrue();
        assertThat(subscription.getUpdatedAt()).isGreaterThanOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should deactivate subscription")
    void shouldDeactivateSubscription() throws InterruptedException {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);
        Thread.sleep(1); // Pequeño delay para asegurar diferencia de tiempo
        long beforeUpdate = subscription.getUpdatedAt();

        // When
        subscription.deactivate();

        // Then
        assertThat(subscription.getIsActive()).isFalse();
        assertThat(subscription.isActive()).isFalse();
        assertThat(subscription.getUpdatedAt()).isGreaterThanOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should return true for isSubscribedTo when event type exists")
    void shouldReturnTrueForIsSubscribedToWhenEventTypeExists() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // When/Then
        assertThat(subscription.isSubscribedTo("payment.completed")).isTrue();
        assertThat(subscription.isSubscribedTo("payment.failed")).isTrue();
    }

    @Test
    @DisplayName("Should return false for isSubscribedTo when event type does not exist")
    void shouldReturnFalseForIsSubscribedToWhenEventTypeDoesNotExist() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // When/Then
        assertThat(subscription.isSubscribedTo("payment.refunded")).isFalse();
    }

    @Test
    @DisplayName("Should return false for isSubscribedTo when event type is null")
    void shouldReturnFalseForIsSubscribedToWhenEventTypeIsNull() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // When/Then
        assertThat(subscription.isSubscribedTo(null)).isFalse();
    }

    @Test
    @DisplayName("Should return true for belongsToClient when client matches")
    void shouldReturnTrueForBelongsToClientWhenClientMatches() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // When/Then
        assertThat(subscription.belongsToClient(CLIENT_ID)).isTrue();
    }

    @Test
    @DisplayName("Should return false for belongsToClient when client does not match")
    void shouldReturnFalseForBelongsToClientWhenClientDoesNotMatch() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // When/Then
        assertThat(subscription.belongsToClient("other-client")).isFalse();
    }

    @Test
    @DisplayName("Should update webhook URL")
    void shouldUpdateWebhookUrl() throws InterruptedException {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);
        Thread.sleep(1); // Pequeño delay para asegurar diferencia de tiempo
        String newWebhookUrl = "https://new-example.com/webhook";
        long beforeUpdate = subscription.getUpdatedAt();

        // When
        subscription.updateWebhookUrl(newWebhookUrl);

        // Then
        assertThat(subscription.getWebhookUrl()).isEqualTo(newWebhookUrl);
        assertThat(subscription.getUpdatedAt()).isGreaterThanOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should update event types")
    void shouldUpdateEventTypes() throws InterruptedException {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);
        Thread.sleep(1); // Pequeño delay para asegurar diferencia de tiempo
        List<String> newEventTypes = List.of("payment.refunded", "payment.cancelled");
        long beforeUpdate = subscription.getUpdatedAt();

        // When
        subscription.updateEventTypes(newEventTypes);

        // Then
        assertThat(subscription.getEventTypes()).containsExactlyElementsOf(newEventTypes);
        assertThat(subscription.getUpdatedAt()).isGreaterThanOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should return true for canReceiveNotifications when active and has webhook")
    void shouldReturnTrueForCanReceiveNotificationsWhenActiveAndHasWebhook() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // When/Then
        assertThat(subscription.canReceiveNotifications()).isTrue();
    }

    @Test
    @DisplayName("Should return false for canReceiveNotifications when inactive")
    void shouldReturnFalseForCanReceiveNotificationsWhenInactive() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);
        subscription.deactivate();

        // When/Then
        assertThat(subscription.canReceiveNotifications()).isFalse();
    }

    @Test
    @DisplayName("Should return false for canReceiveNotifications when webhook URL is null")
    void shouldReturnFalseForCanReceiveNotificationsWhenWebhookUrlIsNull() {
        // Given
        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .eventTypes(EVENT_TYPES)
                .webhookUrl(null)
                .isActive(true)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        // When/Then
        assertThat(subscription.canReceiveNotifications()).isFalse();
    }

    @Test
    @DisplayName("Should return true for hasEventTypes when event types list is not empty")
    void shouldReturnTrueForHasEventTypesWhenEventTypesListIsNotEmpty() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, EVENT_TYPES, WEBHOOK_URL);

        // When/Then
        assertThat(subscription.hasEventTypes()).isTrue();
    }

    @Test
    @DisplayName("Should return false for hasEventTypes when event types list is empty")
    void shouldReturnFalseForHasEventTypesWhenEventTypesListIsEmpty() {
        // Given
        Subscription subscription = Subscription.create(CLIENT_ID, List.of(), WEBHOOK_URL);

        // When/Then
        assertThat(subscription.hasEventTypes()).isFalse();
    }
}

