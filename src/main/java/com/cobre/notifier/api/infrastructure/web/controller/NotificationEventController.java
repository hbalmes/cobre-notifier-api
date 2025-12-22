package com.cobre.notifier.api.infrastructure.web.controller;

import com.cobre.notifier.api.application.port.input.GetNotificationEventsUseCase;
import com.cobre.notifier.api.application.port.input.ReplayNotificationUseCase;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventFilterRequest;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventResponse;
import com.cobre.notifier.api.infrastructure.web.mapper.NotificationEventWebMapper;
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
@RequestMapping("/api/v1/notification_events")
@RequiredArgsConstructor
public class NotificationEventController {

    private final GetNotificationEventsUseCase getNotificationEventsUseCase;
    private final ReplayNotificationUseCase replayNotificationUseCase;
    private final NotificationEventWebMapper mapper;

    /**
     * Obtiene todas las notificaciones con filtros opcionales.
     * 
     * GET /api/v1/notification_events?client_id=xxx&status=SENT&from_date=2024-01-01T00:00:00&to_date=2024-12-31T23:59:59
     * 
     * @param filters Filtros opcionales (clientId, status, fromDate, toDate)
     * @return Lista de notificaciones que cumplen los criterios
     */
    @GetMapping
    public ResponseEntity<List<NotificationEventResponse>> getAll(
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
     * GET /api/v1/notification_events/{id}
     * 
     * @param id ID de la notificación
     * @return Notificación encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationEventResponse> getById(@PathVariable UUID id) {
        log.debug("Getting notification event by id: {}", id);
        
        NotificationEvent notification = getNotificationEventsUseCase.getById(id);
        NotificationEventResponse response = mapper.toResponse(notification);

        return ResponseEntity.ok(response);
    }

    /**
     * Reintenta manualmente una notificación.
     * 
     * POST /api/v1/notification_events/{id}/replay
     * 
     * @param id ID de la notificación a reintentar
     * @return Notificación procesada con estado actualizado
     */
    @PostMapping("/{id}/replay")
    public ResponseEntity<NotificationEventResponse> replay(@PathVariable UUID id) {
        log.info("Replaying notification event: {}", id);
        
        NotificationEvent notification = replayNotificationUseCase.replay(id);
        NotificationEventResponse response = mapper.toResponse(notification);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

