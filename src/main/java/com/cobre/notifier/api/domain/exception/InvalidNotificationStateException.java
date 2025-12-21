package com.cobre.notifier.api.domain.exception;

import com.cobre.notifier.api.domain.DeliveryStatus;

/**
 * Excepción lanzada cuando se intenta realizar una operación inválida
 * sobre una notificación basada en su estado actual.
 */
public class InvalidNotificationStateException extends DomainException {

    public InvalidNotificationStateException(DeliveryStatus currentStatus, String operation) {
        super(String.format("Cannot perform operation '%s' on notification with status '%s'", 
                operation, currentStatus));
    }

    public InvalidNotificationStateException(String message) {
        super(message);
    }

    public InvalidNotificationStateException(String message, Throwable cause) {
        super(message, cause);
    }
}

