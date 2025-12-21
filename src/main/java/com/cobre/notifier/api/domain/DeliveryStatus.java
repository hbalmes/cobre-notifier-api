package com.cobre.notifier.api.domain;

/**
 * Enum que representa el estado de entrega de una notificación.
 */
public enum DeliveryStatus {
    /**
     * Notificación pendiente de ser enviada.
     */
    PENDING,
    
    /**
     * Notificación enviada exitosamente.
     */
    SENT,
    
    /**
     * Notificación fallida después de agotar los reintentos.
     */
    FAILED,
    
    /**
     * Notificación en proceso de reintento.
     */
    RETRYING
}

