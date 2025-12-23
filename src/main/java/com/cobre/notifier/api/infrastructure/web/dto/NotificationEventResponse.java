package com.cobre.notifier.api.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para NotificationEvent.
 * Representa la información de un evento de notificación según la estructura del challenge.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un evento de notificación")
public class NotificationEventResponse {

    @JsonProperty("event_id")
    @Schema(description = "ID único del evento", example = "123e4567-e89b-12d3-a456-426614174000")
    private String eventId;

    @JsonProperty("event_type")
    @Schema(description = "Tipo de evento", example = "credit_card_payment", allowableValues = {
            "credit_card_payment", "debit_card_withdrawal", "credit_transfer", "debit_automatic_payment",
            "credit_refund", "debit_transfer", "credit_deposit", "debit_purchase", "credit_cashback", "debit_subscription"
    })
    private String eventType;

    @JsonProperty("content")
    @Schema(description = "Contenido del evento", example = "Credit card payment received for $150.00")
    private String content;

    @JsonProperty("delivery_date")
    @Schema(description = "Fecha de entrega (ISO-8601)", example = "2024-03-15T14:30:55Z")
    private String deliveryDate;

    @JsonProperty("delivery_status")
    @Schema(description = "Estado de entrega", example = "completed", allowableValues = {"completed", "failed", "pending"})
    private String deliveryStatus;

    @JsonProperty("client_id")
    @Schema(description = "ID del cliente", example = "CLIENT001")
    private String clientId;
}

