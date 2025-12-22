package com.cobre.notifier.api.infrastructure.webhook;

import com.cobre.notifier.api.domain.exception.NotificationDeliveryException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookClientAdapter Tests")
class WebhookClientAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private SimpleMeterRegistry meterRegistry;
    private WebhookClientAdapter webhookClientAdapter;

    private static final String WEBHOOK_URL = "https://example.com/webhook";
    private static final String PAYLOAD = "{\"event\":\"test\"}";

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        webhookClientAdapter = new WebhookClientAdapter(restTemplate, meterRegistry);
    }

    @Test
    @DisplayName("Should deliver notification successfully")
    void shouldDeliverNotificationSuccessfully() {
        // Given
        ResponseEntity<String> response = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(response);

        // When
        String result = webhookClientAdapter.deliver(WEBHOOK_URL, PAYLOAD);

        // Then
        assertThat(result).isEqualTo("200");
        verify(restTemplate, times(1)).exchange(
                eq(WEBHOOK_URL),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("Should throw NotificationDeliveryException on client error (4xx)")
    void shouldThrowExceptionOnClientError() {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST, "Bad Request");
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(exception);

        // When/Then
        assertThatThrownBy(() -> webhookClientAdapter.deliver(WEBHOOK_URL, PAYLOAD))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("Failed to deliver notification")
                .hasMessageContaining("Client error");
    }

    @Test
    @DisplayName("Should throw NotificationDeliveryException on server error (5xx)")
    void shouldThrowExceptionOnServerError() {
        // Given
        HttpServerErrorException exception = new HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(exception);

        // When/Then
        assertThatThrownBy(() -> webhookClientAdapter.deliver(WEBHOOK_URL, PAYLOAD))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("Failed to deliver notification")
                .hasMessageContaining("Server error");
    }

    @Test
    @DisplayName("Should throw NotificationDeliveryException on timeout")
    void shouldThrowExceptionOnTimeout() {
        // Given
        ResourceAccessException exception = new ResourceAccessException("Connection timeout");
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(exception);

        // When/Then
        assertThatThrownBy(() -> webhookClientAdapter.deliver(WEBHOOK_URL, PAYLOAD))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("Failed to deliver notification");
    }

    @Test
    @DisplayName("Should throw NotificationDeliveryException on unexpected error")
    void shouldThrowExceptionOnUnexpectedError() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(exception);

        // When/Then
        assertThatThrownBy(() -> webhookClientAdapter.deliver(WEBHOOK_URL, PAYLOAD))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("Failed to deliver notification");
    }

    @Test
    @DisplayName("Should set correct headers in request")
    void shouldSetCorrectHeaders() {
        // Given
        ResponseEntity<String> response = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(response);

        ArgumentCaptor<org.springframework.http.HttpEntity<String>> entityCaptor = 
                ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);

        // When
        webhookClientAdapter.deliver(WEBHOOK_URL, PAYLOAD);

        // Then
        verify(restTemplate).exchange(
                eq(WEBHOOK_URL),
                eq(org.springframework.http.HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        );

        org.springframework.http.HttpEntity<String> capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getHeaders().getContentType())
                .isEqualTo(org.springframework.http.MediaType.APPLICATION_JSON);
        assertThat(capturedEntity.getBody()).isEqualTo(PAYLOAD);
    }
}

