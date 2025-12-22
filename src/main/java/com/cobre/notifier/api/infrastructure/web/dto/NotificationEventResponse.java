package com.cobre.notifier.api.infrastructure.web.dto;

import com.cobre.notifier.api.domain.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para NotificationEvent.
 * Representa la información de una notificación que se expone a través de la API REST.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un evento de notificación")
public class NotificationEventResponse {

    @Schema(description = "ID único de la notificación", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "ID del cliente", example = "client-123")
    private String clientId;

    @Schema(description = "Tipo de evento", example = "payment.completed")
    private String eventType;

    @Schema(description = "Payload del evento en formato JSON", example = "{\"amount\":1000.0}")
    private String payload;

    @Schema(description = "URL del webhook donde se envió la notificación", example = "https://example.com/webhook")
    private String webhookUrl;

    @Schema(description = "Estado de entrega de la notificación", example = "SENT")
    private DeliveryStatus status;

    @Schema(description = "Número de reintentos realizados", example = "0")
    private Integer retryCount;

    @Schema(description = "Fecha y hora de creación")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha y hora de última actualización")
    private LocalDateTime updatedAt;

    @Schema(description = "Fecha y hora de envío exitoso (null si no se ha enviado)")
    private LocalDateTime sentAt;

    @Schema(description = "Fecha y hora de fallo (null si no ha fallado)")
    private LocalDateTime failedAt;

    @Schema(description = "Mensaje de error (null si no hay error)")
    private String errorMessage;

    @Schema(description = "Código de respuesta HTTP del webhook", example = "200")
    private String responseCode;

    @Schema(description = "Cuerpo de la respuesta del webhook")
    private String responseBody;
}

