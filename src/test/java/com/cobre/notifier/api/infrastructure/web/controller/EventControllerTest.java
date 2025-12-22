package com.cobre.notifier.api.infrastructure.web.controller;

import com.cobre.notifier.api.infrastructure.kafka.dto.KafkaEventMessage;
import com.cobre.notifier.api.infrastructure.kafka.producer.KafkaEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para EventController.
 * Valida el endpoint de publicación de eventos a Kafka.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventController Tests")
class EventControllerTest {

    @Mock
    private KafkaEventProducer kafkaEventProducer;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private KafkaEventMessage testEvent;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Inject real ObjectMapper
        ReflectionTestUtils.setField(eventController, "objectMapper", objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        
        // Set dev profile for tests
        ReflectionTestUtils.setField(eventController, "activeProfile", "dev");
        
        testEvent = KafkaEventMessage.builder()
                .clientId("test-client-123")
                .eventType("payment.completed")
                .payload("{\"amount\":1000}")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Test
    @DisplayName("Should publish event successfully in dev profile")
    void shouldPublishEventSuccessfullyInDevProfile() throws Exception {
        // Given
        doNothing().when(kafkaEventProducer).publishEvent(anyString());

        // When/Then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Event published to Kafka"))
                .andExpect(jsonPath("$.clientId").value("test-client-123"))
                .andExpect(jsonPath("$.eventType").value("payment.completed"))
                .andExpect(jsonPath("$.topic").value("platform.events"));

        verify(kafkaEventProducer, times(1)).publishEvent(anyString());
    }

    @Test
    @DisplayName("Should return 403 in non-dev profile")
    void shouldReturn403InNonDevProfile() throws Exception {
        // Given
        ReflectionTestUtils.setField(eventController, "activeProfile", "prod");

        // When/Then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("This endpoint is only available in dev profile"))
                .andExpect(jsonPath("$.activeProfile").value("prod"));

        verify(kafkaEventProducer, never()).publishEvent(anyString());
    }

    @Test
    @DisplayName("Should return 500 when publish fails")
    void shouldReturn500WhenPublishFails() throws Exception {
        // Given
        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaEventProducer).publishEvent(anyString());

        // When/Then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to publish event"))
                .andExpect(jsonPath("$.message").value("Kafka error"));

        verify(kafkaEventProducer, times(1)).publishEvent(anyString());
    }

    @Test
    @DisplayName("Should handle null clientId and eventType")
    void shouldHandleNullClientIdAndEventType() throws Exception {
        // Given
        KafkaEventMessage eventWithNulls = KafkaEventMessage.builder()
                .clientId(null)
                .eventType(null)
                .payload("{\"test\":\"data\"}")
                .timestamp(System.currentTimeMillis())
                .build();
        
        doNothing().when(kafkaEventProducer).publishEvent(anyString());

        // When/Then
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithNulls)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.clientId").value(""))
                .andExpect(jsonPath("$.eventType").value(""));

        verify(kafkaEventProducer, times(1)).publishEvent(anyString());
    }

    @Test
    @DisplayName("Should serialize event correctly")
    void shouldSerializeEventCorrectly() throws Exception {
        // Given
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(kafkaEventProducer).publishEvent(messageCaptor.capture());

        // When
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isOk());

        // Then
        String publishedMessage = messageCaptor.getValue();
        KafkaEventMessage deserialized = objectMapper.readValue(publishedMessage, KafkaEventMessage.class);
        
        assertThat(deserialized.getClientId()).isEqualTo("test-client-123");
        assertThat(deserialized.getEventType()).isEqualTo("payment.completed");
        assertThat(deserialized.getPayload()).isEqualTo("{\"amount\":1000}");
        
        verify(kafkaEventProducer, times(1)).publishEvent(anyString());
    }
}

