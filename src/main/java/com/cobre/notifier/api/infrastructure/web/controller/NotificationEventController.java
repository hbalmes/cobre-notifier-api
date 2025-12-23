package com.cobre.notifier.api.infrastructure.web.controller;

import com.cobre.notifier.api.application.port.input.GetNotificationEventsUseCase;
import com.cobre.notifier.api.application.port.input.ReplayNotificationUseCase;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventFilterRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * GET /notification_events?client_id=xxx&status=SENT&from_date=2024-01-01T00:00:00&to_date=2024-12-31T23:59:59
     * 
     * @param filters Filtros opcionales (clientId, status, fromDate, toDate)
     * @return Lista de notificaciones que cumplen los criterios
     */
    @Operation(
            summary = "Obtener todas las notificaciones",
            description = "Retorna una lista de notificaciones con filtros opcionales. Todos los parámetros de filtro son opcionales."
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
            @Parameter(description = "Filtros opcionales para buscar notificaciones")
            @ModelAttribute NotificationEventFilterRequest filters) {
        log.debug("Getting all notification events with filters: {}", filters);
        
        List<NotificationEvent> notifications = getNotificationEventsUseCase.getAll(
                filters.getClientId(),
                filters.getStatus(),
                filters.getFromDate(),
                filters.getToDate()
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

