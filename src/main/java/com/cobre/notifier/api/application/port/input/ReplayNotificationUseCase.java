package com.cobre.notifier.api.application.port.input;

import com.cobre.notifier.api.domain.NotificationEvent;

import java.util.UUID;

/**
 * Puerto de entrada (Input Port) para replay de notificaciones.
 * Define el caso de uso para reintentar manualmente una notificación.
 */
public interface ReplayNotificationUseCase {

    /**
     * Reintenta manualmente una notificación.
     * Resetea el estado de la notificación y la procesa nuevamente.
     * 
     * @param notificationId ID de la notificación a reintentar
     * @return Notificación procesada con estado actualizado
     * @throws com.cobre.notifier.api.domain.exception.NotificationNotFoundException si no se encuentra
     */
    NotificationEvent replay(UUID notificationId);
}

