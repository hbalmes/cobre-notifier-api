package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.domain.exception.NotificationDeliveryException;
import com.cobre.notifier.api.infrastructure.webhook.WebhookClientAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookDeliveryService Tests")
class WebhookDeliveryServiceTest {

    @Mock
    private WebhookClientAdapter webhookClientAdapter;

    @InjectMocks
    private WebhookDeliveryService webhookDeliveryService;

    @Test
    @DisplayName("Should deliver notification successfully")
    void shouldDeliverNotificationSuccessfully() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        String payload = "{\"event\":\"payment.completed\"}";
        String expectedResponseCode = "200";

        when(webhookClientAdapter.deliver(webhookUrl, payload))
                .thenReturn(expectedResponseCode);

        // When
        String responseCode = webhookDeliveryService.deliver(webhookUrl, payload);

        // Then
        assertThat(responseCode).isEqualTo(expectedResponseCode);
        verify(webhookClientAdapter, times(1)).deliver(webhookUrl, payload);
    }

    @Test
    @DisplayName("Should throw NotificationDeliveryException when delivery fails")
    void shouldThrowNotificationDeliveryExceptionWhenDeliveryFails() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        String payload = "{\"event\":\"payment.completed\"}";

        when(webhookClientAdapter.deliver(webhookUrl, payload))
                .thenThrow(new NotificationDeliveryException(webhookUrl, "Connection timeout"));

        // When/Then
        assertThatThrownBy(() -> webhookDeliveryService.deliver(webhookUrl, payload))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("Failed to deliver notification");

        verify(webhookClientAdapter, times(1)).deliver(webhookUrl, payload);
    }

    @Test
    @DisplayName("Should handle different response codes")
    void shouldHandleDifferentResponseCodes() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        String payload = "{\"event\":\"payment.completed\"}";
        String[] responseCodes = {"200", "201", "202", "204"};

        for (String responseCode : responseCodes) {
            when(webhookClientAdapter.deliver(webhookUrl, payload))
                    .thenReturn(responseCode);

            // When
            String result = webhookDeliveryService.deliver(webhookUrl, payload);

            // Then
            assertThat(result).isEqualTo(responseCode);
        }

        verify(webhookClientAdapter, times(responseCodes.length)).deliver(webhookUrl, payload);
    }

    @Test
    @DisplayName("Should pass payload correctly to adapter")
    void shouldPassPayloadCorrectlyToAdapter() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        String payload = "{\"event\":\"payment.completed\",\"amount\":1000.0}";

        when(webhookClientAdapter.deliver(webhookUrl, payload))
                .thenReturn("200");

        // When
        webhookDeliveryService.deliver(webhookUrl, payload);

        // Then
        verify(webhookClientAdapter).deliver(eq(webhookUrl), eq(payload));
    }

    @Test
    @DisplayName("Should handle empty payload")
    void shouldHandleEmptyPayload() {
        // Given
        String webhookUrl = "https://example.com/webhook";
        String payload = "";

        when(webhookClientAdapter.deliver(webhookUrl, payload))
                .thenReturn("200");

        // When
        String responseCode = webhookDeliveryService.deliver(webhookUrl, payload);

        // Then
        assertThat(responseCode).isEqualTo("200");
        verify(webhookClientAdapter).deliver(webhookUrl, payload);
    }

    @Test
    @DisplayName("Should handle different webhook URLs")
    void shouldHandleDifferentWebhookUrls() {
        // Given
        String[] webhookUrls = {
                "https://example.com/webhook",
                "https://api.example.com/notifications",
                "http://localhost:8080/webhook"
        };
        String payload = "{\"event\":\"test\"}";

        for (String webhookUrl : webhookUrls) {
            when(webhookClientAdapter.deliver(webhookUrl, payload))
                    .thenReturn("200");

            // When
            String result = webhookDeliveryService.deliver(webhookUrl, payload);

            // Then
            assertThat(result).isEqualTo("200");
        }

        verify(webhookClientAdapter, times(webhookUrls.length)).deliver(anyString(), eq(payload));
    }
}

