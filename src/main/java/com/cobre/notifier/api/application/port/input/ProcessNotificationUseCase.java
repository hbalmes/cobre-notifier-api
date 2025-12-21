package com.cobre.notifier.api.application.port.input;

import com.cobre.notifier.api.domain.NotificationEvent;

/**
 * Puerto de entrada (Input Port) para procesar notificaciones.
 * Define el caso de uso para procesar y entregar notificaciones.
 */
public interface ProcessNotificationUseCase {

    /**
     * Procesa una notificación: valida la suscripción y entrega al webhook.
     * 
     * @param notificationEvent Notificación a procesar
     * @return Notificación procesada con estado actualizado
     */
    NotificationEvent process(NotificationEvent notificationEvent);
}

