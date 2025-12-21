package com.cobre.notifier.api.domain.exception;

/**
 * Excepción base para todas las excepciones de dominio.
 * Todas las excepciones de dominio deben extender de esta clase.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

