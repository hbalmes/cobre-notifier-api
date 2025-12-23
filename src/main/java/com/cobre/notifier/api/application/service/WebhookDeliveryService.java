package com.cobre.notifier.api.application.service;

import com.cobre.notifier.api.domain.exception.NotificationDeliveryException;
import com.cobre.notifier.api.infrastructure.webhook.WebhookClientAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio para entregar notificaciones a webhooks HTTP/HTTPS.
 * Delega la entrega al adapter de infraestructura que incluye métricas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {

    private final WebhookClientAdapter webhookClientAdapter;

    /**
     * Entrega una notificación a un webhook.
     * 
     * @param webhookUrl URL del webhook
     * @param payload Payload de la notificación (JSON)
     * @return Código de respuesta HTTP como String
     * @throws NotificationDeliveryException si la entrega falla
     */
    public String deliver(String webhookUrl, String payload) {
        return webhookClientAdapter.deliver(webhookUrl, payload);
    }
}

