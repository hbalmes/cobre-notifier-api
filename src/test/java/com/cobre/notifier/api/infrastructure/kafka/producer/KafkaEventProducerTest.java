package com.cobre.notifier.api.infrastructure.kafka.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests para KafkaEventProducer.
 * Valida la publicación de eventos a Kafka.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventProducer Tests")
class KafkaEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaEventProducer kafkaEventProducer;

    private static final String TOPIC = "platform.events";
    private static final String TEST_MESSAGE = "{\"client_id\":\"test-client\",\"event_type\":\"test.event\"}";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaEventProducer, "topic", TOPIC);
    }

    @Test
    @DisplayName("Should publish event successfully")
    void shouldPublishEventSuccessfully() {
        // Given
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        SendResult<String, String> sendResult = mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata metadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);
        
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(1L);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        future.complete(sendResult);
        
        when(kafkaTemplate.send(eq(TOPIC), eq(TEST_MESSAGE))).thenReturn(future);

        // When
        kafkaEventProducer.publishEvent(TEST_MESSAGE);

        // Then
        verify(kafkaTemplate, times(1)).send(eq(TOPIC), eq(TEST_MESSAGE));
    }

    @Test
    @DisplayName("Should handle publish failure gracefully")
    void shouldHandlePublishFailureGracefully() throws InterruptedException {
        // Given
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        RuntimeException exception = new RuntimeException("Kafka error");
        future.completeExceptionally(exception);
        
        when(kafkaTemplate.send(eq(TOPIC), eq(TEST_MESSAGE))).thenReturn(future);

        // When - The exception is logged but doesn't propagate synchronously
        kafkaEventProducer.publishEvent(TEST_MESSAGE);
        
        // Wait for async callback to complete
        Thread.sleep(200);

        // Then - Verify the send was called
        verify(kafkaTemplate, times(1)).send(eq(TOPIC), eq(TEST_MESSAGE));
    }

    @Test
    @DisplayName("Should handle KafkaTemplate exception")
    void shouldHandleKafkaTemplateException() {
        // Given
        when(kafkaTemplate.send(eq(TOPIC), eq(TEST_MESSAGE)))
                .thenThrow(new RuntimeException("Connection error"));

        // When/Then
        assertThatThrownBy(() -> kafkaEventProducer.publishEvent(TEST_MESSAGE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to publish event");
        
        verify(kafkaTemplate, times(1)).send(eq(TOPIC), eq(TEST_MESSAGE));
    }

    @Test
    @DisplayName("Should use correct topic from configuration")
    void shouldUseCorrectTopicFromConfiguration() {
        // Given
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        SendResult<String, String> sendResult = mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata metadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);
        
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(1L);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        future.complete(sendResult);
        
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // When
        kafkaEventProducer.publishEvent(TEST_MESSAGE);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), messageCaptor.capture());
        assertThat(topicCaptor.getValue()).isEqualTo(TOPIC);
        assertThat(messageCaptor.getValue()).isEqualTo(TEST_MESSAGE);
    }
}

