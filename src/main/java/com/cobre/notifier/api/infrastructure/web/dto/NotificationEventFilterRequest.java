package com.cobre.notifier.api.infrastructure.web.dto;

import com.cobre.notifier.api.domain.DeliveryStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de request para filtros de búsqueda de notificaciones.
 * Todos los parámetros son opcionales.
 */
@Data
public class NotificationEventFilterRequest {

    private String clientId;
    private DeliveryStatus status;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}

