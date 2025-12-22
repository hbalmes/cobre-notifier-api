package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.application.port.output.NotificationEventRepository;
import com.cobre.notifier.api.application.port.output.SubscriptionRepository;
import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.domain.exception.InvalidSubscriptionException;
import com.cobre.notifier.api.domain.exception.NotificationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para Fase 3: Application Layer - NotificationService
 * Valida la lógica de aplicación para procesar y consultar notificaciones.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fase 3: Application Layer - NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private WebhookDeliveryService webhookDeliveryService;

    @InjectMocks
    private NotificationService notificationService;

    private static final String CLIENT_ID = "client-123";
    private static final String EVENT_TYPE = "payment.completed";
    private static final String PAYLOAD = "{\"amount\":100.0}";
    private static final String WEBHOOK_URL = "https://example.com/webhook";

    private NotificationEvent notification;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        notification = NotificationEvent.create(CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        subscription = Subscription.create(CLIENT_ID, List.of(EVENT_TYPE), WEBHOOK_URL);
    }

    @Test
    @DisplayName("Should process notification successfully")
    void shouldProcessNotificationSuccessfully() {
        // Given
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscription));
        when(webhookDeliveryService.deliver(WEBHOOK_URL, PAYLOAD))
                .thenReturn("200");
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationEvent result = notificationService.process(notification);

        // Then
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(result.getSentAt()).isNotNull();
        assertThat(result.getResponseCode()).isEqualTo("200");
        verify(webhookDeliveryService, times(1)).deliver(WEBHOOK_URL, PAYLOAD);
        verify(notificationEventRepository, times(1)).save(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Should mark notification as failed when webhook delivery fails")
    void shouldMarkNotificationAsFailedWhenWebhookDeliveryFails() {
        // Given
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscription));
        when(webhookDeliveryService.deliver(WEBHOOK_URL, PAYLOAD))
                .thenThrow(new com.cobre.notifier.api.domain.exception.NotificationDeliveryException(
                        WEBHOOK_URL, "Connection timeout"));
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationEvent result = notificationService.process(notification);

        // Then
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(result.getFailedAt()).isNotNull();
        assertThat(result.getErrorMessage()).isNotNull();
        verify(webhookDeliveryService, times(1)).deliver(WEBHOOK_URL, PAYLOAD);
        verify(notificationEventRepository, times(1)).save(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Should throw InvalidSubscriptionException when subscription not found")
    void shouldThrowInvalidSubscriptionExceptionWhenSubscriptionNotFound() {
        // Given
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> notificationService.process(notification))
                .isInstanceOf(InvalidSubscriptionException.class);
        verify(webhookDeliveryService, never()).deliver(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidSubscriptionException when client not subscribed to event type")
    void shouldThrowInvalidSubscriptionExceptionWhenClientNotSubscribedToEventType() {
        // Given
        Subscription subscriptionWithoutEvent = Subscription.create(
                CLIENT_ID, List.of("other.event"), WEBHOOK_URL);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscriptionWithoutEvent));

        // When/Then
        assertThatThrownBy(() -> notificationService.process(notification))
                .isInstanceOf(InvalidSubscriptionException.class);
        verify(webhookDeliveryService, never()).deliver(anyString(), anyString());
    }

    @Test
    @DisplayName("Should update webhook URL from subscription")
    void shouldUpdateWebhookUrlFromSubscription() {
        // Given
        String subscriptionWebhookUrl = "https://subscription-webhook.com/webhook";
        Subscription subscriptionWithDifferentUrl = Subscription.create(
                CLIENT_ID, List.of(EVENT_TYPE), subscriptionWebhookUrl);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscriptionWithDifferentUrl));
        when(webhookDeliveryService.deliver(subscriptionWebhookUrl, PAYLOAD))
                .thenReturn("200");
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationEvent result = notificationService.process(notification);

        // Then
        assertThat(result.getWebhookUrl()).isEqualTo(subscriptionWebhookUrl);
        verify(webhookDeliveryService, times(1)).deliver(subscriptionWebhookUrl, PAYLOAD);
    }

    @Test
    @DisplayName("Should get notification by id")
    void shouldGetNotificationById() {
        // Given
        UUID notificationId = notification.getId();
        when(notificationEventRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        // When
        NotificationEvent result = notificationService.getById(notificationId);

        // Then
        assertThat(result).isEqualTo(notification);
        verify(notificationEventRepository, times(1)).findById(notificationId);
    }

    @Test
    @DisplayName("Should throw NotificationNotFoundException when notification not found")
    void shouldThrowNotificationNotFoundExceptionWhenNotificationNotFound() {
        // Given
        UUID notificationId = UUID.randomUUID();
        when(notificationEventRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> notificationService.getById(notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all notifications with filters")
    void shouldGetAllNotificationsWithFilters() {
        // Given
        DeliveryStatus status = DeliveryStatus.SENT;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        List<NotificationEvent> notifications = List.of(notification);
        
        when(notificationEventRepository.findAll(status, fromDate, toDate))
                .thenReturn(notifications);

        // When
        List<NotificationEvent> result = notificationService.getAll(null, status, fromDate, toDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(notification);
        verify(notificationEventRepository, times(1)).findAll(status, fromDate, toDate);
    }

    @Test
    @DisplayName("Should get notifications by client id with filters")
    void shouldGetNotificationsByClientIdWithFilters() {
        // Given
        DeliveryStatus status = DeliveryStatus.PENDING;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        List<NotificationEvent> notifications = List.of(notification);
        
        when(notificationEventRepository.findByClientId(CLIENT_ID, status, fromDate, toDate))
                .thenReturn(notifications);

        // When
        List<NotificationEvent> result = notificationService.getAll(CLIENT_ID, status, fromDate, toDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(notification);
        verify(notificationEventRepository, times(1)).findByClientId(CLIENT_ID, status, fromDate, toDate);
    }

    @Test
    @DisplayName("Should replay notification successfully")
    void shouldReplayNotificationSuccessfully() {
        // Given
        UUID notificationId = notification.getId();
        notification.markAsFailed("Previous error");
        
        when(notificationEventRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscription));
        when(webhookDeliveryService.deliver(WEBHOOK_URL, PAYLOAD))
                .thenReturn("200");
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationEvent result = notificationService.replay(notificationId);

        // Then
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(result.getRetryCount()).isEqualTo(0);
        assertThat(result.getErrorMessage()).isNull();
        verify(notificationEventRepository, times(2)).save(any(NotificationEvent.class));
        verify(webhookDeliveryService, times(1)).deliver(WEBHOOK_URL, PAYLOAD);
    }

    @Test
    @DisplayName("Should throw NotificationNotFoundException when replaying non-existent notification")
    void shouldThrowNotificationNotFoundExceptionWhenReplayingNonExistentNotification() {
        // Given
        UUID notificationId = UUID.randomUUID();
        when(notificationEventRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> notificationService.replay(notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}

