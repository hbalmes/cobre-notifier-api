package com.cobre.notifier.api.domain.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra una notificación.
 */
public class NotificationNotFoundException extends DomainException {

    public NotificationNotFoundException(UUID notificationId) {
        super(String.format("Notification with id %s not found", notificationId));
    }

    public NotificationNotFoundException(String message) {
        super(message);
    }
}

