package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.application.port.output.NotificationEventRepository;
import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para Fase 3: Application Layer - RetryService
 * Valida la lógica de aplicación para reintentar notificaciones fallidas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fase 3: Application Layer - RetryService Tests")
class RetryServiceTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    @Mock
    private NotificationService notificationService;

    private SimpleMeterRegistry meterRegistry;

    private RetryService retryService;

    private static final String CLIENT_ID = "client-123";
    private static final String EVENT_TYPE = "payment.completed";
    private static final String PAYLOAD = "{\"amount\":100.0}";
    private static final String WEBHOOK_URL = "https://example.com/webhook";

    private NotificationEvent pendingNotification;
    private NotificationEvent retryingNotification;

    @BeforeEach
    void setUp() {
        // Usar SimpleMeterRegistry real para tests (más simple que mockear todo)
        meterRegistry = new SimpleMeterRegistry();
        
        // Crear instancia real de RetryService con mocks
        retryService = new RetryService(notificationEventRepository, notificationService, meterRegistry);
        
        // Configurar valores de @Value usando ReflectionTestUtils
        ReflectionTestUtils.setField(retryService, "maxRetries", 3);
        ReflectionTestUtils.setField(retryService, "initialDelayMs", 1000L);
        ReflectionTestUtils.setField(retryService, "multiplier", 2.0);
        ReflectionTestUtils.setField(retryService, "schedulerDelayMs", 60000L);
        
        pendingNotification = NotificationEvent.create(CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        retryingNotification = NotificationEvent.create(CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        retryingNotification.incrementRetry(3);
    }

    @Test
    @DisplayName("Should retry failed notifications successfully")
    void shouldRetryFailedNotificationsSuccessfully() {
        // Given
        List<NotificationEvent> pendingNotifications = List.of(pendingNotification);
        when(notificationEventRepository.findPendingRetries(3, 100))
                .thenReturn(pendingNotifications);
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.process(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int processedCount = retryService.retryFailedNotifications();

        // Then
        assertThat(processedCount).isEqualTo(1);
        verify(notificationEventRepository, times(1)).findPendingRetries(3, 100);
        verify(notificationService, times(1)).process(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Should return zero when no pending notifications")
    void shouldReturnZeroWhenNoPendingNotifications() {
        // Given
        when(notificationEventRepository.findPendingRetries(3, 100))
                .thenReturn(List.of());

        // When
        int processedCount = retryService.retryFailedNotifications();

        // Then
        assertThat(processedCount).isEqualTo(0);
        verify(notificationService, never()).process(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Should increment retry count before processing")
    void shouldIncrementRetryCountBeforeProcessing() {
        // Given
        List<NotificationEvent> pendingNotifications = List.of(pendingNotification);
        when(notificationEventRepository.findPendingRetries(3, 100))
                .thenReturn(pendingNotifications);
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.process(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        retryService.retryFailedNotifications();

        // Then
        assertThat(pendingNotification.getRetryCount()).isEqualTo(1);
        assertThat(pendingNotification.getStatus()).isEqualTo(DeliveryStatus.RETRYING);
        verify(notificationEventRepository, times(1)).save(pendingNotification);
    }

    @Test
    @DisplayName("Should not retry when max retries reached")
    void shouldNotRetryWhenMaxRetriesReached() {
        // Given
        NotificationEvent maxRetriesNotification = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        // Incrementar hasta llegar al límite (2 veces, quedando en retryCount=2, status=RETRYING)
        maxRetriesNotification.incrementRetry(3);
        maxRetriesNotification.incrementRetry(3);
        // Ahora al incrementar la tercera vez, alcanzará el máximo
        
        List<NotificationEvent> notifications = List.of(maxRetriesNotification);
        when(notificationEventRepository.findPendingRetries(3, 100))
                .thenReturn(notifications);
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int processedCount = retryService.retryFailedNotifications();

        // Then
        assertThat(processedCount).isEqualTo(0);
        assertThat(maxRetriesNotification.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(maxRetriesNotification.getRetryCount()).isEqualTo(3);
        verify(notificationService, never()).process(any(NotificationEvent.class));
        verify(notificationEventRepository, times(1)).save(maxRetriesNotification);
    }

    @Test
    @DisplayName("Should handle multiple pending notifications")
    void shouldHandleMultiplePendingNotifications() {
        // Given
        NotificationEvent notification1 = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        NotificationEvent notification2 = NotificationEvent.create(
                CLIENT_ID, EVENT_TYPE, PAYLOAD, WEBHOOK_URL);
        
        List<NotificationEvent> pendingNotifications = List.of(notification1, notification2);
        when(notificationEventRepository.findPendingRetries(3, 100))
                .thenReturn(pendingNotifications);
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.process(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int processedCount = retryService.retryFailedNotifications();

        // Then
        assertThat(processedCount).isEqualTo(2);
        verify(notificationService, times(2)).process(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("Should handle errors during retry gracefully")
    void shouldHandleErrorsDuringRetryGracefully() {
        // Given
        List<NotificationEvent> pendingNotifications = List.of(pendingNotification);
        when(notificationEventRepository.findPendingRetries(3, 100))
                .thenReturn(pendingNotifications);
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.process(any(NotificationEvent.class)))
                .thenThrow(new RuntimeException("Processing error"));

        // When
        int processedCount = retryService.retryFailedNotifications();

        // Then
        assertThat(processedCount).isEqualTo(0);
        assertThat(pendingNotification.getErrorMessage()).contains("Retry error");
        // Se guarda dos veces: una después de incrementRetry y otra después del error
        verify(notificationEventRepository, atLeast(1)).save(any(NotificationEvent.class));
    }
}

