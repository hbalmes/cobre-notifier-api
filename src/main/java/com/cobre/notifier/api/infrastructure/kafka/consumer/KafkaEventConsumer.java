package com.cobre.notifier.api.infrastructure.kafka.consumer;

import com.cobre.notifier.api.application.port.input.ProcessNotificationUseCase;
import com.cobre.notifier.api.application.port.output.NotificationEventRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Consumer de Kafka que escucha eventos del topic platform.events.
 * Procesa eventos y crea notificaciones para entregar a los clientes.
 */
@Slf4j
@Component
public class KafkaEventConsumer {

    private final ProcessNotificationUseCase processNotificationUseCase;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    
    private final Counter kafkaConsumeSuccessCounter;
    private final Counter kafkaConsumeErrorCounter;
    private final Counter kafkaConsumeRetryCounter;
    private final Counter kafkaConsumeInvalidSubscriptionCounter;
    private final Counter kafkaConsumeDuplicateCounter;
    private final Timer kafkaConsumeTimer;

    public KafkaEventConsumer(ProcessNotificationUseCase processNotificationUseCase,
                             SubscriptionRepository subscriptionRepository,
                             NotificationEventRepository notificationEventRepository,
                             ObjectMapper objectMapper,
                             MeterRegistry meterRegistry) {
        this.processNotificationUseCase = processNotificationUseCase;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationEventRepository = notificationEventRepository;
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
        
        this.kafkaConsumeDuplicateCounter = Counter.builder("kafka.consume.duplicate")
                .description("Total number of duplicate Kafka events detected and skipped")
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

        try {
            // Deserializar mensaje
            KafkaEventMessage eventMessage = objectMapper.readValue(message, KafkaEventMessage.class);
            
            log.info("Processing event: clientId={}, eventType={}, eventId={}", 
                    eventMessage.getClientId(), eventMessage.getEventType(), eventMessage.getEventId());

            // Determinar kafka_event_id: usar el del mensaje o generar uno basado en contenido
            String kafkaEventId = eventMessage.getEventId();
            if (kafkaEventId == null || kafkaEventId.isEmpty()) {
                // Generar idempotency key basado en contenido del mensaje
                kafkaEventId = generateIdempotencyKey(eventMessage);
                log.debug("Generated idempotency key for event without event_id: {}", kafkaEventId);
            }

            // Verificar idempotencia: si ya existe una notificación con este kafka_event_id, skip
            if (notificationEventRepository.findByKafkaEventId(kafkaEventId).isPresent()) {
                log.warn("Duplicate event detected with kafka_event_id={}, skipping processing. clientId={}, eventType={}", 
                        kafkaEventId, eventMessage.getClientId(), eventMessage.getEventType());
                kafkaConsumeDuplicateCounter.increment();
                sample.stop(kafkaConsumeTimer);
                // Acknowledge para no reintentar
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

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

            // Crear notificación con kafka_event_id
            NotificationEvent notification = NotificationEvent.create(
                    eventMessage.getClientId(),
                    eventMessage.getEventType(),
                    eventMessage.getContent(),
                    subscription.getWebhookUrl(),
                    kafkaEventId
            );

            // Procesar notificación
            try {
                processNotificationUseCase.process(notification);
                
                // Registrar métrica de procesamiento por cliente
                Counter.builder("notification.processed.by_client.total")
                        .description("Total notifications processed by client ID")
                        .tag("client_id", eventMessage.getClientId())
                        .register(meterRegistry)
                        .increment();
                        
            } catch (DataIntegrityViolationException e) {
                // Fallback: si el constraint único falla (race condition), verificar si ya existe
                if (e.getMessage() != null && e.getMessage().contains("kafka_event_id")) {
                    log.warn("DataIntegrityViolationException for kafka_event_id={}, checking if duplicate: {}", 
                            kafkaEventId, e.getMessage());
                    if (notificationEventRepository.findByKafkaEventId(kafkaEventId).isPresent()) {
                        log.info("Duplicate confirmed after constraint violation, skipping. kafka_event_id={}", kafkaEventId);
                        kafkaConsumeDuplicateCounter.increment();
                        sample.stop(kafkaConsumeTimer);
                        if (acknowledgment != null) {
                            acknowledgment.acknowledge();
                        }
                        return;
                    }
                }
                // Si no es un error de duplicado, re-lanzar
                throw e;
            }

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

    /**
     * Genera una clave de idempotencia basada en el contenido del mensaje.
     * Usa SHA-256 hash del contenido para generar un ID único y determinístico.
     * 
     * @param eventMessage Mensaje del evento
     * @return String con el hash del contenido
     */
    private String generateIdempotencyKey(KafkaEventMessage eventMessage) {
        // Crear string único basado en contenido del mensaje
        String content = String.format("%s:%s:%s", 
                eventMessage.getClientId(), 
                eventMessage.getEventType(), 
                eventMessage.getContent());
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a base64 y tomar primeros 32 caracteres
            String base64Hash = Base64.getEncoder().encodeToString(hash);
            return base64Hash.substring(0, Math.min(32, base64Hash.length()));
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating idempotency key, using fallback", e);
            // Fallback: usar hash simple del contenido
            return String.valueOf(content.hashCode());
        }
    }
}

