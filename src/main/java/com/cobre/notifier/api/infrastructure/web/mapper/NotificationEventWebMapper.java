package com.cobre.notifier.api.infrastructure.web.mapper;

import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.web.dto.NotificationEventResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Mapper para convertir entre entidades de dominio NotificationEvent y DTOs de respuesta.
 */
@Component
public class NotificationEventWebMapper {

    /**
     * Convierte una entidad de dominio NotificationEvent a un DTO de respuesta.
     * 
     * @param notificationEvent Entidad de dominio
     * @return DTO de respuesta
     */
    public NotificationEventResponse toResponse(NotificationEvent notificationEvent) {
        if (notificationEvent == null) {
            return null;
        }

        return NotificationEventResponse.builder()
                .eventId(notificationEvent.getId() != null ? notificationEvent.getId().toString() : null)
                .clientId(notificationEvent.getClientId())
                .eventType(notificationEvent.getEventType())
                .content(notificationEvent.getPayload())
                .deliveryDate(formatDeliveryDate(notificationEvent.getSentAt() != null ? 
                        notificationEvent.getSentAt() : notificationEvent.getCreatedAt()))
                .deliveryStatus(mapDeliveryStatus(notificationEvent.getStatus()))
                .build();
    }

    /**
     * Convierte DeliveryStatus a String según el formato del challenge.
     * 
     * @param status Estado de entrega
     * @return String representando el estado
     */
    private String mapDeliveryStatus(DeliveryStatus status) {
        if (status == null) {
            return "pending";
        }
        return switch (status) {
            case SENT -> "completed";
            case FAILED -> "failed";
            case PENDING, RETRYING -> "pending";
        };
    }

    /**
     * Formatea LocalDateTime a ISO-8601 String con Z (UTC).
     * 
     * @param dateTime Fecha y hora a formatear
     * @return String en formato ISO-8601 o null si dateTime es null
     */
    private String formatDeliveryDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_INSTANT);
    }
}

