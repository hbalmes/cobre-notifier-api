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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

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
            
            // Generar event_id basado en hash del contenido para idempotencia
            // Si el usuario envía el mismo contenido, se generará el mismo event_id
            String eventId = generateIdempotencyKey(event);
            event.setEventId(eventId);
            
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

    /**
     * Genera una clave de idempotencia basada en el contenido del mensaje.
     * Usa SHA-256 hash del contenido para generar un ID único y determinístico.
     * Mismo contenido = mismo event_id.
     * 
     * @param event Mensaje del evento
     * @return String con el hash del contenido
     */
    private String generateIdempotencyKey(KafkaEventMessage event) {
        // Crear string único basado en contenido del mensaje
        String content = String.format("%s:%s:%s", 
                event.getClientId() != null ? event.getClientId() : "", 
                event.getEventType() != null ? event.getEventType() : "", 
                event.getContent() != null ? event.getContent() : "");
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a base64 y tomar primeros 32 caracteres
            String base64Hash = Base64.getEncoder().encodeToString(hash);
            return base64Hash.substring(0, Math.min(32, base64Hash.length()));
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating idempotency key, using fallback", e);
            // Fallback: usar hash simple del contenido
            return String.valueOf(content.hashCode());
        }
    }
}

