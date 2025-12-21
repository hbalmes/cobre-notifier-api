package com.cobre.notifier.api.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra una suscripción.
 */
public class SubscriptionNotFoundException extends DomainException {

    public SubscriptionNotFoundException(UUID subscriptionId) {
        super(String.format("Subscription with id %s not found", subscriptionId));
    }

    public SubscriptionNotFoundException(String clientId) {
        super(String.format("Subscription for client %s not found", clientId));
    }

    public SubscriptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

