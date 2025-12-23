package com.cobre.notifier.api.application.port.output;

import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (Output Port) para operaciones de persistencia de NotificationEvent.
 * Define las operaciones que la capa de aplicación necesita para gestionar notificaciones.
 */
public interface NotificationEventRepository {

    /**
     * Guarda una notificación.
     * 
     * @param notificationEvent Notificación a guardar
     * @return Notificación guardada
     */
    NotificationEvent save(NotificationEvent notificationEvent);

    /**
     * Busca una notificación por su ID.
     * 
     * @param id ID de la notificación
     * @return Notificación encontrada o Optional vacío
     */
    Optional<NotificationEvent> findById(UUID id);

    /**
     * Busca una notificación por su kafka_event_id.
     * Usado para verificar idempotencia y prevenir duplicados.
     * 
     * @param kafkaEventId ID del evento de Kafka
     * @return Notificación encontrada o Optional vacío
     */
    Optional<NotificationEvent> findByKafkaEventId(String kafkaEventId);

    /**
     * Busca notificaciones por cliente con filtros opcionales.
     * 
     * @param clientId ID del cliente
     * @param status Estado de la notificación (opcional)
     * @param fromDate Fecha desde (opcional)
     * @param toDate Fecha hasta (opcional)
     * @return Lista de notificaciones que cumplen los criterios
     */
    List<NotificationEvent> findByClientId(String clientId, 
                                          DeliveryStatus status, 
                                          LocalDateTime fromDate, 
                                          LocalDateTime toDate);

    /**
     * Busca todas las notificaciones con filtros opcionales.
     * 
     * @param status Estado de la notificación (opcional)
     * @param fromDate Fecha desde (opcional)
     * @param toDate Fecha hasta (opcional)
     * @return Lista de notificaciones que cumplen los criterios
     */
    List<NotificationEvent> findAll(DeliveryStatus status, 
                                    LocalDateTime fromDate, 
                                    LocalDateTime toDate);

    /**
     * Busca notificaciones pendientes de reintento.
     * Notificaciones con estado PENDING o RETRYING que aún pueden ser reintentadas.
     * 
     * @param maxRetries Número máximo de reintentos permitidos
     * @param limit Límite de resultados a retornar
     * @return Lista de notificaciones pendientes de reintento
     */
    List<NotificationEvent> findPendingRetries(int maxRetries, int limit);

    /**
     * Verifica si existe una notificación con el ID dado.
     * 
     * @param id ID de la notificación
     * @return true si existe, false en caso contrario
     */
    boolean existsById(UUID id);

    /**
     * Cuenta notificaciones por cliente y estado.
     * 
     * @param clientId ID del cliente
     * @param status Estado de la notificación
     * @return Número de notificaciones que cumplen los criterios
     */
    long countByClientIdAndStatus(String clientId, DeliveryStatus status);
}

