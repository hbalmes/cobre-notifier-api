package com.cobre.notifier.api.infrastructure.webhook;

import com.cobre.notifier.api.domain.exception.NotificationDeliveryException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter para entregar notificaciones a webhooks HTTP/HTTPS.
 * Incluye métricas Prometheus para observabilidad.
 */
@Slf4j
@Component
public class WebhookClientAdapter {

    private final RestTemplate restTemplate;
    private final MeterRegistry meterRegistry;
    
    private final Counter webhookDeliverySuccessCounter;
    private final Counter webhookDeliveryFailureCounter;
    private final Counter webhookDeliveryClientErrorCounter;
    private final Counter webhookDeliveryServerErrorCounter;
    private final Counter webhookDeliveryTimeoutCounter;
    private final Timer webhookDeliveryTimer;

    @Value("${app.webhook.timeout-ms:5000}")
    private int timeoutMs;
    
    @Value("${app.webhook.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    public WebhookClientAdapter(RestTemplate restTemplate, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.meterRegistry = meterRegistry;
        
        // Inicializar métricas
        this.webhookDeliverySuccessCounter = Counter.builder("webhook.delivery.success")
                .description("Total number of successful webhook deliveries")
                .register(meterRegistry);
        
        this.webhookDeliveryFailureCounter = Counter.builder("webhook.delivery.failure")
                .description("Total number of failed webhook deliveries")
                .register(meterRegistry);
        
        this.webhookDeliveryClientErrorCounter = Counter.builder("webhook.delivery.client_error")
                .description("Total number of webhook deliveries failed due to client errors (4xx)")
                .register(meterRegistry);
        
        this.webhookDeliveryServerErrorCounter = Counter.builder("webhook.delivery.server_error")
                .description("Total number of webhook deliveries failed due to server errors (5xx)")
                .register(meterRegistry);
        
        this.webhookDeliveryTimeoutCounter = Counter.builder("webhook.delivery.timeout")
                .description("Total number of webhook deliveries failed due to timeout")
                .register(meterRegistry);
        
        this.webhookDeliveryTimer = Timer.builder("webhook.delivery.duration")
                .description("Webhook delivery duration in milliseconds")
                .register(meterRegistry);
    }

    /**
     * Entrega una notificación a un webhook con métricas.
     * 
     * @param webhookUrl URL del webhook
     * @param payload Payload de la notificación (JSON)
     * @return Código de respuesta HTTP como String
     * @throws NotificationDeliveryException si la entrega falla
     */
    public String deliver(String webhookUrl, String payload) {
        log.debug("Delivering notification to webhook: {}", webhookUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            return webhookDeliveryTimer.recordCallable(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.exchange(
                            webhookUrl,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

                    Integer statusCode = response.getStatusCode().value();
                    log.debug("Webhook response: {} - {}", statusCode, response.getBody());
                    
                    // Registrar éxito
                    webhookDeliverySuccessCounter.increment();
                    
                    return String.valueOf(statusCode);

                } catch (HttpClientErrorException e) {
                    // Errores 4xx - cliente
                    log.error("Client error delivering to {}: {}", webhookUrl, e.getMessage());
                    webhookDeliveryFailureCounter.increment();
                    webhookDeliveryClientErrorCounter.increment();
                    throw new NotificationDeliveryException(webhookUrl, e.getStatusCode().value(), 
                            "Client error: " + e.getMessage());

                } catch (HttpServerErrorException e) {
                    // Errores 5xx - servidor
                    log.error("Server error delivering to {}: {}", webhookUrl, e.getMessage());
                    webhookDeliveryFailureCounter.increment();
                    webhookDeliveryServerErrorCounter.increment();
                    throw new NotificationDeliveryException(webhookUrl, e.getStatusCode().value(), 
                            "Server error: " + e.getMessage());

                } catch (ResourceAccessException e) {
                    // Timeout o problemas de conexión
                    log.error("Connection error delivering to {}: {}", webhookUrl, e.getMessage());
                    webhookDeliveryFailureCounter.increment();
                    webhookDeliveryTimeoutCounter.increment();
                    throw new NotificationDeliveryException(webhookUrl, e);

                } catch (Exception e) {
                    // Otros errores
                    log.error("Unexpected error delivering to {}: {}", webhookUrl, e.getMessage(), e);
                    webhookDeliveryFailureCounter.increment();
                    throw new NotificationDeliveryException(webhookUrl, e);
                }
            });
        } catch (NotificationDeliveryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in webhook delivery timer: {}", e.getMessage(), e);
            webhookDeliveryFailureCounter.increment();
            throw new NotificationDeliveryException(webhookUrl, e);
        }
    }
}

