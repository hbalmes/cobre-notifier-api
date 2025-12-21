package com.cobre.notifier.api.domain.exception;

/**
 * Excepción lanzada cuando una suscripción es inválida o no puede ser procesada.
 */
public class InvalidSubscriptionException extends DomainException {

    public InvalidSubscriptionException(String message) {
        super(message);
    }

    public InvalidSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidSubscriptionException inactiveSubscription(String clientId) {
        return new InvalidSubscriptionException(
                String.format("Subscription for client %s is not active", clientId));
    }

    public static InvalidSubscriptionException missingWebhookUrl(String clientId) {
        return new InvalidSubscriptionException(
                String.format("Subscription for client %s does not have a webhook URL configured", clientId));
    }

    public static InvalidSubscriptionException noEventTypes(String clientId) {
        return new InvalidSubscriptionException(
                String.format("Subscription for client %s does not have any event types configured", clientId));
    }
}

