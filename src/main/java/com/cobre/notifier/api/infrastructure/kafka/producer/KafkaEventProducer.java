package com.cobre.notifier.api.infrastructure.kafka.producer;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${spring.kafka.consumer.topic:platform.events}")
    private String topic;

    /**
     * Publica un evento al topic de Kafka.
     * 
     * @param message Mensaje JSON serializado como String
     * @throws RuntimeException si falla la publicación síncronamente
     */
    public void publishEvent(String message) {
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to topic: {}", topic, ex);
                } else if (result != null) {
                    log.info("Successfully published event to topic: {}, partition: {}, offset: {}", 
                            topic, 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing event to topic: {}", topic, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}

