package com.cobre.notifier.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio que representa un evento de notificación.
 * Contiene la lógica de negocio relacionada con el ciclo de vida de una notificación.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

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

    /**
     * Crea una nueva instancia de NotificationEvent con estado inicial.
     * 
     * @param clientId ID del cliente
     * @param eventType Tipo de evento
     * @param payload Payload del evento (JSON)
     * @param webhookUrl URL del webhook donde enviar la notificación
     * @return Nueva instancia de NotificationEvent
     */
    public static NotificationEvent create(String clientId, String eventType, String payload, String webhookUrl) {
        return NotificationEvent.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .eventType(eventType)
                .payload(payload)
                .webhookUrl(webhookUrl)
                .status(DeliveryStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Marca la notificación como enviada exitosamente.
     * 
     * @param responseCode Código de respuesta HTTP
     * @param responseBody Cuerpo de la respuesta
     */
    public void markAsSent(String responseCode, String responseBody) {
        this.status = DeliveryStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.errorMessage = null;
    }

    /**
     * Marca la notificación como fallida.
     * 
     * @param errorMessage Mensaje de error
     */
    public void markAsFailed(String errorMessage) {
        this.status = DeliveryStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * Incrementa el contador de reintentos y marca como RETRYING si no se ha alcanzado el máximo.
     * 
     * @param maxRetries Número máximo de reintentos permitidos
     * @return true si se puede reintentar, false si se alcanzó el máximo
     */
    public boolean incrementRetry(int maxRetries) {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
        
        if (this.retryCount >= maxRetries) {
            this.status = DeliveryStatus.FAILED;
            this.failedAt = LocalDateTime.now();
            if (this.errorMessage == null) {
                this.errorMessage = String.format("Maximum retry attempts (%d) reached", maxRetries);
            }
            return false;
        }
        
        this.status = DeliveryStatus.RETRYING;
        return true;
    }

    /**
     * Verifica si la notificación puede ser reintentada.
     * 
     * @param maxRetries Número máximo de reintentos permitidos
     * @return true si puede ser reintentada, false en caso contrario
     */
    public boolean canRetry(int maxRetries) {
        return this.status == DeliveryStatus.PENDING 
                || this.status == DeliveryStatus.RETRYING 
                || (this.status == DeliveryStatus.FAILED && this.retryCount < maxRetries);
    }

    /**
     * Verifica si la notificación pertenece a un cliente específico.
     * 
     * @param clientId ID del cliente
     * @return true si pertenece al cliente, false en caso contrario
     */
    public boolean belongsToClient(String clientId) {
        return this.clientId != null && this.clientId.equals(clientId);
    }

    /**
     * Verifica si la notificación está en un estado final (SENT o FAILED).
     * 
     * @return true si está en estado final, false en caso contrario
     */
    public boolean isInFinalState() {
        return this.status == DeliveryStatus.SENT || this.status == DeliveryStatus.FAILED;
    }

    /**
     * Verifica si la notificación está pendiente de procesamiento.
     * 
     * @return true si está pendiente, false en caso contrario
     */
    public boolean isPending() {
        return this.status == DeliveryStatus.PENDING || this.status == DeliveryStatus.RETRYING;
    }

    /**
     * Resetea el estado de la notificación para un replay manual.
     */
    public void resetForReplay() {
        this.status = DeliveryStatus.PENDING;
        this.retryCount = 0;
        this.updatedAt = LocalDateTime.now();
        this.sentAt = null;
        this.failedAt = null;
        this.errorMessage = null;
        this.responseCode = null;
        this.responseBody = null;
    }

    /**
     * Actualiza el mensaje de error sin cambiar el estado.
     * Útil para registrar errores durante reintentos.
     * 
     * @param errorMessage Mensaje de error
     */
    public void updateErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
}

