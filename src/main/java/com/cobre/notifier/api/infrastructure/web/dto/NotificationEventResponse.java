package com.cobre.notifier.api.infrastructure.web.dto;

import com.cobre.notifier.api.domain.DeliveryStatus;
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
public class NotificationEventResponse {

    private UUID id;
    private String clientId;
    private String eventType;
    private String payload;
    private String webhookUrl;
    private DeliveryStatus status;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;
    private String errorMessage;
    private String responseCode;
    private String responseBody;
}

