package com.cobre.notifier.api.application.port.input;

import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (Input Port) para consultar notificaciones.
 * Define el caso de uso para obtener notificaciones con filtros.
 */
public interface GetNotificationEventsUseCase {

    /**
     * Obtiene una notificación por su ID.
     * 
     * @param id ID de la notificación
     * @return Notificación encontrada
     * @throws com.cobre.notifier.api.domain.exception.NotificationNotFoundException si no se encuentra
     */
    NotificationEvent getById(UUID id);

    /**
     * Obtiene notificaciones con filtros opcionales.
     * 
     * @param clientId ID del cliente (opcional)
     * @param status Estado de la notificación (opcional)
     * @param fromDate Fecha desde (opcional)
     * @param toDate Fecha hasta (opcional)
     * @return Lista de notificaciones que cumplen los criterios
     */
    List<NotificationEvent> getAll(String clientId, 
                                   DeliveryStatus status, 
                                   LocalDateTime fromDate, 
                                   LocalDateTime toDate);
}

