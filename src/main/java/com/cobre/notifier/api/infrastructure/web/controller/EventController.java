package com.cobre.notifier.api.infrastructure.web.controller;

import com.cobre.notifier.api.infrastructure.kafka.dto.KafkaEventMessage;
import com.cobre.notifier.api.infrastructure.kafka.producer.KafkaEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para publicar eventos a Kafka.
 * SOLO PARA DESARROLLO/TESTING - No usar en producción.
 * 
 * Este endpoint permite publicar eventos directamente al topic platform.events
 * para facilitar las pruebas del sistema de notificaciones.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "API para publicar eventos a Kafka (solo desarrollo/testing)")
public class EventController {

    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Publica un evento al topic de Kafka.
     * 
     * POST /api/v1/events
     * 
     * @param event Evento a publicar
     * @return Respuesta con el estado de la publicación
     */
    @Operation(
            summary = "Publicar evento a Kafka",
            description = "Publica un evento al topic platform.events. Solo disponible en perfil dev."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento publicado exitosamente",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Endpoint no disponible en producción"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al publicar el evento"
            )
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> publishEvent(@RequestBody KafkaEventMessage event) {
        // Solo permitir en desarrollo
        if (!activeProfile.contains("dev")) {
            log.warn("Attempt to use test endpoint in non-dev profile: {}", activeProfile);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "This endpoint is only available in dev profile",
                            "activeProfile", activeProfile
                    ));
        }

        try {
            log.info("Publishing test event: clientId={}, eventType={}", 
                    event.getClientId(), event.getEventType());
            
            // Generar event_id único
            String eventId = UUID.randomUUID().toString();
            
            // Fecha de publicación en formato ISO-8601 UTC
            String publishedAt = Instant.now()
                    .atZone(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_INSTANT);
            
            String messageJson = objectMapper.writeValueAsString(event);
            kafkaEventProducer.publishEvent(messageJson);
            
            return ResponseEntity.ok(Map.of(
                    "event_id", eventId,
                    "event_type", event.getEventType() != null ? event.getEventType() : "",
                    "content", event.getContent() != null ? event.getContent() : "",
                    "published_at", publishedAt,
                    "status", "published"
            ));
        } catch (Exception e) {
            log.error("Error publishing test event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to publish event",
                            "message", e.getMessage()
                    ));
        }
    }
}

