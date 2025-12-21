package com.cobre.notifier.api.domain.exception;

/**
 * Excepción lanzada cuando falla la entrega de una notificación.
 * Esta excepción se usa para encapsular errores de comunicación con webhooks.
 */
public class NotificationDeliveryException extends DomainException {

    private final String webhookUrl;
    private final Integer statusCode;

    public NotificationDeliveryException(String webhookUrl, String message) {
        super(String.format("Failed to deliver notification to %s: %s", webhookUrl, message));
        this.webhookUrl = webhookUrl;
        this.statusCode = null;
    }

    public NotificationDeliveryException(String webhookUrl, Integer statusCode, String message) {
        super(String.format("Failed to deliver notification to %s (HTTP %d): %s", 
                webhookUrl, statusCode, message));
        this.webhookUrl = webhookUrl;
        this.statusCode = statusCode;
    }

    public NotificationDeliveryException(String webhookUrl, Throwable cause) {
        super(String.format("Failed to deliver notification to %s", webhookUrl), cause);
        this.webhookUrl = webhookUrl;
        this.statusCode = null;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}

