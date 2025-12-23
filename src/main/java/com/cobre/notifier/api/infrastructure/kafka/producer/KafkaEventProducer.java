package com.cobre.notifier.api.infrastructure.kafka.producer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Producer de Kafka para publicar eventos al topic platform.events.
 * Utilizado principalmente para testing y desarrollo.
 */
@Slf4j
@Component
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    
    private final Counter kafkaPublishSuccessCounter;
    private final Counter kafkaPublishErrorCounter;
    private final Timer kafkaPublishTimer;
    
    @Value("${spring.kafka.consumer.topic:platform.events}")
    private String topic;

    public KafkaEventProducer(KafkaTemplate<String, String> kafkaTemplate, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.meterRegistry = meterRegistry;
        
        // Inicializar métricas
        this.kafkaPublishSuccessCounter = Counter.builder("kafka.publish.success")
                .description("Total number of successful Kafka event publications")
                .register(meterRegistry);
        
        this.kafkaPublishErrorCounter = Counter.builder("kafka.publish.error")
                .description("Total number of failed Kafka event publications")
                .register(meterRegistry);
        
        this.kafkaPublishTimer = Timer.builder("kafka.publish.duration")
                .description("Time taken to publish events to Kafka topic")
                .register(meterRegistry);
    }

    /**
     * Publica un evento al topic de Kafka.
     * 
     * @param message Mensaje JSON serializado como String
     * @throws RuntimeException si falla la publicación síncronamente
     */
    public void publishEvent(String message) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
            
            future.whenComplete((result, ex) -> {
                sample.stop(kafkaPublishTimer);
                
                if (ex != null) {
                    log.error("Failed to publish event to topic: {}", topic, ex);
                    kafkaPublishErrorCounter.increment();
                } else if (result != null) {
                    log.info("Successfully published event to topic: {}, partition: {}, offset: {}", 
                            topic, 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    kafkaPublishSuccessCounter.increment();
                }
            });
        } catch (Exception e) {
            sample.stop(kafkaPublishTimer);
            log.error("Error publishing event to topic: {}", topic, e);
            kafkaPublishErrorCounter.increment();
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}

