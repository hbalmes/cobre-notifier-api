package com.cobre.notifier.api.infrastructure.kafka.consumer;

import com.cobre.notifier.api.application.port.input.ProcessNotificationUseCase;
import com.cobre.notifier.api.application.port.output.SubscriptionRepository;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.domain.exception.InvalidSubscriptionException;
import com.cobre.notifier.api.infrastructure.kafka.dto.KafkaEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para Fase 5: Infrastructure - Kafka - KafkaEventConsumer
 * Valida el procesamiento de eventos de Kafka.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fase 5: Infrastructure - Kafka - KafkaEventConsumer Tests")
class KafkaEventConsumerTest {

    @Mock
    private ProcessNotificationUseCase processNotificationUseCase;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private KafkaEventConsumer kafkaEventConsumer;

    private static final String CLIENT_ID = "client-123";
    private static final String EVENT_TYPE = "payment.completed";
    private static final String PAYLOAD = "{\"amount\":100.0}";
    private static final String WEBHOOK_URL = "https://example.com/webhook";

    private KafkaEventMessage eventMessage;
    private Subscription subscription;
    private NotificationEvent notification;
    private String kafkaMessageJson;

    @BeforeEach
    void setUp() throws Exception {
        eventMessage = KafkaEventMessage.builder()
                .clientId(CLIENT_ID)
                .eventType(EVENT_TYPE)
                .payload(PAYLOAD)
                .timestamp(System.currentTimeMillis())
                .build();

        subscription = Subscription.create(
                CLIENT_ID,
                List.of(EVENT_TYPE),
                WEBHOOK_URL);

        notification = NotificationEvent.create(
                CLIENT_ID,
                EVENT_TYPE,
                PAYLOAD,
                WEBHOOK_URL);

        kafkaMessageJson = """
                {
                    "client_id": "%s",
                    "event_type": "%s",
                    "payload": "%s",
                    "timestamp": %d
                }
                """.formatted(CLIENT_ID, EVENT_TYPE, PAYLOAD.replace("\"", "\\\""), 
                        System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should process Kafka event successfully")
    void shouldProcessKafkaEventSuccessfully() throws Exception {
        // Given
        when(objectMapper.readValue(kafkaMessageJson, KafkaEventMessage.class))
                .thenReturn(eventMessage);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscription));
        when(processNotificationUseCase.process(any(NotificationEvent.class)))
                .thenReturn(notification);

        // When
        kafkaEventConsumer.consumeEvent(
                kafkaMessageJson,
                "platform.events",
                0,
                1L,
                acknowledgment
        );

        // Then
        verify(subscriptionRepository, times(1)).findActiveByClientId(CLIENT_ID);
        verify(processNotificationUseCase, times(1)).process(any(NotificationEvent.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should acknowledge and skip when subscription not found")
    void shouldAcknowledgeAndSkipWhenSubscriptionNotFound() throws Exception {
        // Given
        when(objectMapper.readValue(kafkaMessageJson, KafkaEventMessage.class))
                .thenReturn(eventMessage);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.empty());

        // When
        kafkaEventConsumer.consumeEvent(
                kafkaMessageJson,
                "platform.events",
                0,
                1L,
                acknowledgment
        );

        // Then
        verify(subscriptionRepository, times(1)).findActiveByClientId(CLIENT_ID);
        verify(processNotificationUseCase, never()).process(any(NotificationEvent.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should acknowledge and skip when client not subscribed to event type")
    void shouldAcknowledgeAndSkipWhenClientNotSubscribedToEventType() throws Exception {
        // Given
        Subscription subscriptionWithoutEvent = Subscription.create(
                CLIENT_ID,
                List.of("other.event"),
                WEBHOOK_URL);
        when(objectMapper.readValue(kafkaMessageJson, KafkaEventMessage.class))
                .thenReturn(eventMessage);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscriptionWithoutEvent));

        // When
        kafkaEventConsumer.consumeEvent(
                kafkaMessageJson,
                "platform.events",
                0,
                1L,
                acknowledgment
        );

        // Then
        verify(subscriptionRepository, times(1)).findActiveByClientId(CLIENT_ID);
        verify(processNotificationUseCase, never()).process(any(NotificationEvent.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Should throw exception when JSON deserialization fails")
    void shouldThrowExceptionWhenJsonDeserializationFails() throws Exception {
        // Given
        String invalidJson = "invalid json";
        when(objectMapper.readValue(invalidJson, KafkaEventMessage.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Invalid JSON") {});

        // When/Then
        assertThatThrownBy(() -> kafkaEventConsumer.consumeEvent(
                invalidJson,
                "platform.events",
                0,
                1L,
                acknowledgment
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process Kafka event");

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should throw exception when processing fails")
    void shouldThrowExceptionWhenProcessingFails() throws Exception {
        // Given
        when(objectMapper.readValue(kafkaMessageJson, KafkaEventMessage.class))
                .thenReturn(eventMessage);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscription));
        when(processNotificationUseCase.process(any(NotificationEvent.class)))
                .thenThrow(new RuntimeException("Processing error"));

        // When/Then
        assertThatThrownBy(() -> kafkaEventConsumer.consumeEvent(
                kafkaMessageJson,
                "platform.events",
                0,
                1L,
                acknowledgment
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process Kafka event");

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Should handle null acknowledgment gracefully")
    void shouldHandleNullAcknowledgmentGracefully() throws Exception {
        // Given
        when(objectMapper.readValue(kafkaMessageJson, KafkaEventMessage.class))
                .thenReturn(eventMessage);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscription));
        when(processNotificationUseCase.process(any(NotificationEvent.class)))
                .thenReturn(notification);

        // When
        kafkaEventConsumer.consumeEvent(
                kafkaMessageJson,
                "platform.events",
                0,
                1L,
                null
        );

        // Then
        verify(processNotificationUseCase, times(1)).process(any(NotificationEvent.class));
        // No debería lanzar NullPointerException
    }

    @Test
    @DisplayName("Should use webhook URL from subscription")
    void shouldUseWebhookUrlFromSubscription() throws Exception {
        // Given
        String subscriptionWebhookUrl = "https://subscription-webhook.com/webhook";
        Subscription subscriptionWithDifferentUrl = Subscription.create(
                CLIENT_ID,
                List.of(EVENT_TYPE),
                subscriptionWebhookUrl);
        
        when(objectMapper.readValue(kafkaMessageJson, KafkaEventMessage.class))
                .thenReturn(eventMessage);
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID))
                .thenReturn(Optional.of(subscriptionWithDifferentUrl));
        when(processNotificationUseCase.process(any(NotificationEvent.class)))
                .thenAnswer(invocation -> {
                    NotificationEvent event = invocation.getArgument(0);
                    assertThat(event.getWebhookUrl()).isEqualTo(subscriptionWebhookUrl);
                    return event;
                });

        // When
        kafkaEventConsumer.consumeEvent(
                kafkaMessageJson,
                "platform.events",
                0,
                1L,
                acknowledgment
        );

        // Then
        verify(processNotificationUseCase, times(1)).process(any(NotificationEvent.class));
        verify(acknowledgment, times(1)).acknowledge();
    }
}

