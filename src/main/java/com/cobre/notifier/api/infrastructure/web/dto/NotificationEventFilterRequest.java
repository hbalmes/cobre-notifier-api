package com.cobre.notifier.api.infrastructure.web.dto;

import com.cobre.notifier.api.domain.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de request para filtros de búsqueda de notificaciones.
 * Todos los parámetros son opcionales.
 */
@Data
@Schema(description = "Filtros opcionales para buscar notificaciones")
public class NotificationEventFilterRequest {

    @Schema(description = "ID del cliente para filtrar", example = "client-123")
    private String clientId;

    @Schema(description = "Estado de entrega para filtrar", example = "SENT")
    private DeliveryStatus status;

    @Schema(description = "Fecha desde para filtrar (formato: yyyy-MM-ddTHH:mm:ss)", example = "2024-01-01T00:00:00")
    private LocalDateTime fromDate;

    @Schema(description = "Fecha hasta para filtrar (formato: yyyy-MM-ddTHH:mm:ss)", example = "2024-12-31T23:59:59")
    private LocalDateTime toDate;
}

