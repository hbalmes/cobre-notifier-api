package com.cobre.notifier.api.infrastructure.web.controller;

import com.cobre.notifier.api.application.port.input.GetNotificationEventsUseCase;
import com.cobre.notifier.api.application.port.input.ReplayNotificationUseCase;
import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.domain.exception.NotificationNotFoundException;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventFilterRequest;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventResponse;
import com.cobre.notifier.api.infrastructure.web.mapper.NotificationEventWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventController Tests")
class NotificationEventControllerTest {

    @Mock
    private GetNotificationEventsUseCase getNotificationEventsUseCase;

    @Mock
    private ReplayNotificationUseCase replayNotificationUseCase;

    @Mock
    private NotificationEventWebMapper mapper;

    @InjectMocks
    private NotificationEventController controller;

    private NotificationEvent notificationEvent;
    private NotificationEventResponse response;

    @BeforeEach
    void setUp() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        notificationEvent = NotificationEvent.builder()
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
                .build();

        response = NotificationEventResponse.builder()
                .eventId(id.toString())
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .content("Payment received for $150.00")
                .deliveryDate(now.atZone(java.time.ZoneId.systemDefault())
                        .withZoneSameInstant(java.time.ZoneId.of("UTC"))
                        .format(java.time.format.DateTimeFormatter.ISO_INSTANT))
                .deliveryStatus("completed")
                .build();
    }

    @Test
    @DisplayName("Should get all notification events without filters")
    void shouldGetAllNotificationEventsWithoutFilters() {
        // Given
        List<NotificationEvent> notifications = Collections.singletonList(notificationEvent);

        when(getNotificationEventsUseCase.getAll(null, null, null, null))
                .thenReturn(notifications);
        when(mapper.toResponse(notificationEvent)).thenReturn(response);

        // When
        ResponseEntity<List<NotificationEventResponse>> result = controller.getAll(
                null, null, null, null, null, null, null, null);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0)).isEqualTo(response);

        verify(getNotificationEventsUseCase).getAll(null, null, null, null);
        verify(mapper).toResponse(notificationEvent);
    }

    @Test
    @DisplayName("Should get all notification events with all filters")
    void shouldGetAllNotificationEventsWithAllFilters() {
        // Given
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        List<NotificationEvent> notifications = Collections.singletonList(notificationEvent);

        when(getNotificationEventsUseCase.getAll(
                eq("CLIENT001"),
                eq(DeliveryStatus.SENT),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(notifications);
        when(mapper.toResponse(notificationEvent)).thenReturn(response);

        // When - usando snake_case parameters
        ResponseEntity<List<NotificationEventResponse>> result = controller.getAll(
                "CLIENT001", null, "completed", null, fromDate, null, toDate, null);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);

        verify(getNotificationEventsUseCase).getAll(
                eq("CLIENT001"),
                eq(DeliveryStatus.SENT),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get all notification events with multiple results")
    void shouldGetAllNotificationEventsWithMultipleResults() {
        // Given
        NotificationEvent notification2 = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .clientId("CLIENT002")
                .eventType("credit_transfer")
                .payload("Payment failed: insufficient funds")
                .status(DeliveryStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .build();
        NotificationEventResponse response2 = NotificationEventResponse.builder()
                .eventId(notification2.getId().toString())
                .clientId("CLIENT002")
                .eventType("credit_transfer")
                .content("Payment failed: insufficient funds")
                .deliveryDate(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault())
                        .withZoneSameInstant(java.time.ZoneId.of("UTC"))
                        .format(java.time.format.DateTimeFormatter.ISO_INSTANT))
                .deliveryStatus("failed")
                .build();

        List<NotificationEvent> notifications = Arrays.asList(notificationEvent, notification2);

        when(getNotificationEventsUseCase.getAll(null, null, null, null))
                .thenReturn(notifications);
        when(mapper.toResponse(notificationEvent)).thenReturn(response);
        when(mapper.toResponse(notification2)).thenReturn(response2);

        // When
        ResponseEntity<List<NotificationEventResponse>> result = controller.getAll(
                null, null, null, null, null, null, null, null);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("Should get all notification events with empty result")
    void shouldGetAllNotificationEventsWithEmptyResult() {
        // Given
        when(getNotificationEventsUseCase.getAll(null, null, null, null))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<NotificationEventResponse>> result = controller.getAll(
                null, null, null, null, null, null, null, null);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should get notification event by id")
    void shouldGetNotificationEventById() {
        // Given
        UUID id = notificationEvent.getId();
        when(getNotificationEventsUseCase.getById(id)).thenReturn(notificationEvent);
        when(mapper.toResponse(notificationEvent)).thenReturn(response);

        // When
        ResponseEntity<NotificationEventResponse> result = controller.getById(id);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).isEqualTo(response);

        verify(getNotificationEventsUseCase).getById(id);
        verify(mapper).toResponse(notificationEvent);
    }

    @Test
    @DisplayName("Should throw exception when notification not found by id")
    void shouldThrowExceptionWhenNotificationNotFoundById() {
        // Given
        UUID id = UUID.randomUUID();
        when(getNotificationEventsUseCase.getById(id))
                .thenThrow(new NotificationNotFoundException(id));

        // When/Then
        assertThatThrownBy(() -> controller.getById(id))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(getNotificationEventsUseCase).getById(id);
        verify(mapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should replay notification event")
    void shouldReplayNotificationEvent() {
        // Given
        UUID id = notificationEvent.getId();
        NotificationEvent replayedNotification = NotificationEvent.builder()
                .id(id)
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .payload("Payment received for $150.00")
                .status(DeliveryStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        NotificationEventResponse replayedResponse = NotificationEventResponse.builder()
                .eventId(id.toString())
                .clientId("CLIENT001")
                .eventType("credit_card_payment")
                .content("Payment received for $150.00")
                .deliveryDate(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault())
                        .withZoneSameInstant(java.time.ZoneId.of("UTC"))
                        .format(java.time.format.DateTimeFormatter.ISO_INSTANT))
                .deliveryStatus("pending")
                .build();

        when(replayNotificationUseCase.replay(id)).thenReturn(replayedNotification);
        when(mapper.toResponse(replayedNotification)).thenReturn(replayedResponse);

        // When
        ResponseEntity<NotificationEventResponse> result = controller.replay(id);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).isEqualTo(replayedResponse);

        verify(replayNotificationUseCase).replay(id);
        verify(mapper).toResponse(replayedNotification);
    }

    @Test
    @DisplayName("Should throw exception when replaying non-existent notification")
    void shouldThrowExceptionWhenReplayingNonExistentNotification() {
        // Given
        UUID id = UUID.randomUUID();
        when(replayNotificationUseCase.replay(id))
                .thenThrow(new NotificationNotFoundException(id));

        // When/Then
        assertThatThrownBy(() -> controller.replay(id))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(replayNotificationUseCase).replay(id);
        verify(mapper, never()).toResponse(any());
    }
}

