package com.cobre.notifier.api.infrastructure.web.controller;

import com.cobre.notifier.api.application.port.input.GetNotificationEventsUseCase;
import com.cobre.notifier.api.application.port.input.ReplayNotificationUseCase;
import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventResponse;
import com.cobre.notifier.api.infrastructure.web.mapper.NotificationEventWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestionar eventos de notificación.
 * Expone endpoints para consultar y hacer replay de notificaciones.
 */
@Slf4j
@RestController
@RequestMapping("/notification_events")
@RequiredArgsConstructor
@Tag(name = "Notification Events", description = "API para gestionar eventos de notificación")
public class NotificationEventController {

    private final GetNotificationEventsUseCase getNotificationEventsUseCase;
    private final ReplayNotificationUseCase replayNotificationUseCase;
    private final NotificationEventWebMapper mapper;

    /**
     * Obtiene todas las notificaciones con filtros opcionales.
     * 
     * GET /notification_events?client_id=xxx&delivery_status=sent&from_date=2024-01-01T00:00:00&to_date=2024-12-31T23:59:59
     * 
     * Soporta tanto snake_case (client_id, delivery_status) como camelCase (clientId, deliveryStatus)
     * 
     * @param clientId ID del cliente para filtrar (acepta client_id o clientId)
     * @param deliveryStatus Estado de entrega como string (acepta delivery_status o deliveryStatus): "completed", "failed", "pending"
     * @param fromDate Fecha desde para filtrar
     * @param toDate Fecha hasta para filtrar
     * @return Lista de notificaciones que cumplen los criterios
     */
    @Operation(
            summary = "Obtener todas las notificaciones",
            description = "Retorna una lista de notificaciones con filtros opcionales. Todos los parámetros de filtro son opcionales. " +
                    "Acepta parámetros en snake_case (client_id, delivery_status) o camelCase (clientId, deliveryStatus)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de notificaciones obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = NotificationEventResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<NotificationEventResponse>> getAll(
            @Parameter(description = "ID del cliente para filtrar", example = "CLIENT001")
            @RequestParam(value = "client_id", required = false) String clientIdSnake,
            @RequestParam(value = "clientId", required = false) String clientIdCamel,
            @Parameter(description = "Estado de entrega (completed, failed, pending)", example = "completed")
            @RequestParam(value = "delivery_status", required = false) String deliveryStatusSnake,
            @RequestParam(value = "deliveryStatus", required = false) String deliveryStatusCamel,
            @Parameter(description = "Fecha desde (formato: yyyy-MM-ddTHH:mm:ss)", example = "2024-01-01T00:00:00")
            @RequestParam(value = "from_date", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateSnake,
            @RequestParam(value = "fromDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateCamel,
            @Parameter(description = "Fecha hasta (formato: yyyy-MM-ddTHH:mm:ss)", example = "2024-12-31T23:59:59")
            @RequestParam(value = "to_date", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateSnake,
            @RequestParam(value = "toDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateCamel) {
        
        // Resolver parámetros: preferir snake_case si está presente, sino usar camelCase
        String clientId = clientIdSnake != null ? clientIdSnake : clientIdCamel;
        String deliveryStatusStr = deliveryStatusSnake != null ? deliveryStatusSnake : deliveryStatusCamel;
        LocalDateTime fromDate = fromDateSnake != null ? fromDateSnake : fromDateCamel;
        LocalDateTime toDate = toDateSnake != null ? toDateSnake : toDateCamel;
        
        // Convertir delivery_status string a enum DeliveryStatus
        DeliveryStatus status = null;
        if (deliveryStatusStr != null && !deliveryStatusStr.isEmpty()) {
            try {
                // Mapear valores de API (completed, failed, pending) a enum (SENT, FAILED, PENDING/RETRYING)
                status = switch (deliveryStatusStr.toLowerCase()) {
                    case "completed" -> DeliveryStatus.SENT;
                    case "failed" -> DeliveryStatus.FAILED;
                    case "pending" -> DeliveryStatus.PENDING;
                    default -> {
                        // Intentar parsear directamente como enum
                        try {
                            yield DeliveryStatus.valueOf(deliveryStatusStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid delivery_status value: {}, ignoring filter", deliveryStatusStr);
                            yield null;
                        }
                    }
                };
            } catch (Exception e) {
                log.warn("Error parsing delivery_status: {}, ignoring filter", deliveryStatusStr, e);
            }
        }
        
        log.debug("Getting all notification events with filters: clientId={}, status={}, fromDate={}, toDate={}", 
                clientId, status, fromDate, toDate);
        
        List<NotificationEvent> notifications = getNotificationEventsUseCase.getAll(
                clientId,
                status,
                fromDate,
                toDate
        );

        List<NotificationEventResponse> responses = notifications.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene una notificación por su ID.
     * 
     * GET /notification_events/{notification_event_id}
     * 
     * @param notification_event_id ID de la notificación
     * @return Notificación encontrada
     */
    @Operation(
            summary = "Obtener notificación por ID",
            description = "Retorna una notificación específica por su identificador único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notificación encontrada",
                    content = @Content(schema = @Schema(implementation = NotificationEventResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notificación no encontrada"
            )
    })
    @GetMapping("/{notification_event_id}")
    public ResponseEntity<NotificationEventResponse> getById(
            @Parameter(description = "ID único de la notificación", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("notification_event_id") UUID notification_event_id) {
        log.debug("Getting notification event by id: {}", notification_event_id);
        
        NotificationEvent notification = getNotificationEventsUseCase.getById(notification_event_id);
        NotificationEventResponse response = mapper.toResponse(notification);

        return ResponseEntity.ok(response);
    }

    /**
     * Reintenta manualmente una notificación.
     * 
     * POST /notification_events/{notification_event_id}/replay
     * 
     * @param notification_event_id ID de la notificación a reintentar
     * @return Notificación procesada con estado actualizado
     */
    @Operation(
            summary = "Reintentar notificación",
            description = "Reintenta manualmente el envío de una notificación. Resetea el estado y procesa nuevamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Notificación aceptada para replay",
                    content = @Content(schema = @Schema(implementation = NotificationEventResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notificación no encontrada"
            )
    })
    @PostMapping("/{notification_event_id}/replay")
    public ResponseEntity<NotificationEventResponse> replay(
            @Parameter(description = "ID único de la notificación a reintentar", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("notification_event_id") UUID notification_event_id) {
        log.info("Replaying notification event: {}", notification_event_id);
        
        NotificationEvent notification = replayNotificationUseCase.replay(notification_event_id);
        NotificationEventResponse response = mapper.toResponse(notification);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

