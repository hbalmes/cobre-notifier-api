package com.cobre.notifier.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio que representa una suscripción de un cliente.
 * Define qué tipos de eventos un cliente desea recibir y a qué webhook enviarlos.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    private UUID id;
    private String clientId;
    private List<String> eventTypes;
    private String webhookUrl;
    private Boolean isActive;
    private Long createdAt;
    private Long updatedAt;

    /**
     * Crea una nueva suscripción activa.
     * 
     * @param clientId ID del cliente
     * @param eventTypes Lista de tipos de eventos a los que se suscribe
     * @param webhookUrl URL del webhook donde recibir las notificaciones
     * @return Nueva instancia de Subscription
     */
    public static Subscription create(String clientId, List<String> eventTypes, String webhookUrl) {
        long now = System.currentTimeMillis();
        return Subscription.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .eventTypes(eventTypes != null ? List.copyOf(eventTypes) : List.of())
                .webhookUrl(webhookUrl)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Verifica si la suscripción está activa.
     * 
     * @return true si está activa, false en caso contrario
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * Activa la suscripción.
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Desactiva la suscripción.
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Verifica si la suscripción está suscrita a un tipo de evento específico.
     * 
     * @param eventType Tipo de evento a verificar
     * @return true si está suscrita al evento, false en caso contrario
     */
    public boolean isSubscribedTo(String eventType) {
        if (eventType == null || this.eventTypes == null) {
            return false;
        }
        return this.eventTypes.contains(eventType);
    }

    /**
     * Verifica si la suscripción pertenece a un cliente específico.
     * 
     * @param clientId ID del cliente
     * @return true si pertenece al cliente, false en caso contrario
     */
    public boolean belongsToClient(String clientId) {
        return this.clientId != null && this.clientId.equals(clientId);
    }

    /**
     * Actualiza la URL del webhook.
     * 
     * @param webhookUrl Nueva URL del webhook
     */
    public void updateWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Actualiza los tipos de eventos a los que está suscrito.
     * 
     * @param eventTypes Nueva lista de tipos de eventos
     */
    public void updateEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes != null ? List.copyOf(eventTypes) : List.of();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Verifica si la suscripción puede recibir notificaciones.
     * Una suscripción puede recibir notificaciones si está activa y tiene una URL de webhook válida.
     * 
     * @return true si puede recibir notificaciones, false en caso contrario
     */
    public boolean canReceiveNotifications() {
        return isActive() && webhookUrl != null && !webhookUrl.isBlank();
    }

    /**
     * Verifica si la suscripción tiene tipos de eventos configurados.
     * 
     * @return true si tiene tipos de eventos, false en caso contrario
     */
    public boolean hasEventTypes() {
        return eventTypes != null && !eventTypes.isEmpty();
    }
}

