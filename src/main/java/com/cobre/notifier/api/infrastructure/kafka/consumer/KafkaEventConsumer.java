package com.cobre.notifier.api.infrastructure.kafka.consumer;

import com.cobre.notifier.api.application.port.input.ProcessNotificationUseCase;
import com.cobre.notifier.api.application.port.output.SubscriptionRepository;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.domain.exception.InvalidSubscriptionException;
import com.cobre.notifier.api.infrastructure.kafka.dto.KafkaEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer de Kafka que escucha eventos del topic platform.events.
 * Procesa eventos y crea notificaciones para entregar a los clientes.
 */
@Slf4j
@Component
public class KafkaEventConsumer {

    private final ProcessNotificationUseCase processNotificationUseCase;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    
    private final Counter kafkaConsumeSuccessCounter;
    private final Counter kafkaConsumeErrorCounter;
    private final Counter kafkaConsumeRetryCounter;
    private final Counter kafkaConsumeInvalidSubscriptionCounter;
    private final Timer kafkaConsumeTimer;

    public KafkaEventConsumer(ProcessNotificationUseCase processNotificationUseCase,
                             SubscriptionRepository subscriptionRepository,
                             ObjectMapper objectMapper,
                             MeterRegistry meterRegistry) {
        this.processNotificationUseCase = processNotificationUseCase;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        
        // Inicializar métricas
        this.kafkaConsumeSuccessCounter = Counter.builder("kafka.consume.success")
                .description("Total number of successfully consumed Kafka events")
                .register(meterRegistry);
        
        this.kafkaConsumeErrorCounter = Counter.builder("kafka.consume.error")
                .description("Total number of failed Kafka event consumptions")
                .register(meterRegistry);
        
        this.kafkaConsumeRetryCounter = Counter.builder("kafka.consume.retry")
                .description("Total number of Kafka events that will be retried")
                .register(meterRegistry);
        
        this.kafkaConsumeInvalidSubscriptionCounter = Counter.builder("kafka.consume.invalid_subscription")
                .description("Total number of Kafka events with invalid subscriptions")
                .register(meterRegistry);
        
        this.kafkaConsumeTimer = Timer.builder("kafka.consume.duration")
                .description("Time taken to consume and process Kafka events")
                .register(meterRegistry);
    }

    @KafkaListener(
            topics = "${spring.kafka.consumer.topic:platform.events}",
            groupId = "${spring.kafka.consumer.group-id:cobre-notifier-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.debug("Received message from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);

        Timer.Sample sample = Timer.start(meterRegistry);
        boolean willRetry = false;

        try {
            // Deserializar mensaje
            KafkaEventMessage eventMessage = objectMapper.readValue(message, KafkaEventMessage.class);
            
            log.info("Processing event: clientId={}, eventType={}", 
                    eventMessage.getClientId(), eventMessage.getEventType());

            // Validar que existe una suscripción activa
            Subscription subscription = subscriptionRepository
                    .findActiveByClientId(eventMessage.getClientId())
                    .orElseThrow(() -> {
                        log.warn("No active subscription found for client: {}", 
                                eventMessage.getClientId());
                        kafkaConsumeInvalidSubscriptionCounter.increment();
                        return InvalidSubscriptionException.inactiveSubscription(eventMessage.getClientId());
                    });

            // Validar que el cliente está suscrito al tipo de evento
            if (!subscription.isSubscribedTo(eventMessage.getEventType())) {
                log.warn("Client {} is not subscribed to event type: {}", 
                        eventMessage.getClientId(), eventMessage.getEventType());
                kafkaConsumeInvalidSubscriptionCounter.increment();
                throw new InvalidSubscriptionException(
                        String.format("Client %s is not subscribed to event type %s", 
                                eventMessage.getClientId(), eventMessage.getEventType()));
            }

            // Crear notificación con kafka_event_id si está disponible
            NotificationEvent notification = NotificationEvent.create(
                    eventMessage.getClientId(),
                    eventMessage.getEventType(),
                    eventMessage.getContent(),
                    subscription.getWebhookUrl(),
                    eventMessage.getEventId() // Puede ser null si no viene del endpoint de testing
            );

            // Procesar notificación
            processNotificationUseCase.process(notification);

            // Confirmar procesamiento
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            sample.stop(kafkaConsumeTimer);
            kafkaConsumeSuccessCounter.increment();

            log.info("Successfully processed event: clientId={}, eventType={}, notificationId={}", 
                    eventMessage.getClientId(), 
                    eventMessage.getEventType(), 
                    notification.getId());

        } catch (InvalidSubscriptionException e) {
            sample.stop(kafkaConsumeTimer);
            log.error("Invalid subscription for event: {}", e.getMessage());
            // Acknowledge para no reintentar eventos con suscripciones inválidas
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            sample.stop(kafkaConsumeTimer);
            log.error("Error processing Kafka event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset, e);
            kafkaConsumeErrorCounter.increment();
            kafkaConsumeRetryCounter.increment(); // El mensaje será reintentado por Kafka
            // No hacer acknowledge para permitir retry del mensaje
            throw new RuntimeException("Failed to process Kafka event", e);
        }
    }
}

