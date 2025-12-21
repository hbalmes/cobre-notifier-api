package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.domain.exception.NotificationDeliveryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para entregar notificaciones a webhooks HTTP/HTTPS.
 * Maneja la comunicación HTTP con los endpoints de webhook.
 */
@Slf4j
@Service
public class WebhookDeliveryService {

    private final RestTemplate restTemplate;
    
    @Value("${app.webhook.timeout-ms:5000}")
    private int timeoutMs;
    
    @Value("${app.webhook.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    public WebhookDeliveryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Entrega una notificación a un webhook.
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
            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            Integer statusCode = response.getStatusCode().value();
            log.debug("Webhook response: {} - {}", statusCode, response.getBody());
            
            return String.valueOf(statusCode);

        } catch (HttpClientErrorException e) {
            // Errores 4xx - cliente
            log.error("Client error delivering to {}: {}", webhookUrl, e.getMessage());
            throw new NotificationDeliveryException(webhookUrl, e.getStatusCode().value(), 
                    "Client error: " + e.getMessage());

        } catch (HttpServerErrorException e) {
            // Errores 5xx - servidor
            log.error("Server error delivering to {}: {}", webhookUrl, e.getMessage());
            throw new NotificationDeliveryException(webhookUrl, e.getStatusCode().value(), 
                    "Server error: " + e.getMessage());

        } catch (ResourceAccessException e) {
            // Timeout o problemas de conexión
            log.error("Connection error delivering to {}: {}", webhookUrl, e.getMessage());
            throw new NotificationDeliveryException(webhookUrl, e);

        } catch (Exception e) {
            // Otros errores
            log.error("Unexpected error delivering to {}: {}", webhookUrl, e.getMessage(), e);
            throw new NotificationDeliveryException(webhookUrl, e);
        }
    }
}

