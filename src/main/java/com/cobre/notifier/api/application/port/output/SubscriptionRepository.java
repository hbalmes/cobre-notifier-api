package com.cobre.notifier.api.application.port.output;

import com.cobre.notifier.api.domain.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (Output Port) para operaciones de persistencia de Subscription.
 * Define las operaciones que la capa de aplicación necesita para gestionar suscripciones.
 */
public interface SubscriptionRepository {

    /**
     * Guarda una suscripción.
     * 
     * @param subscription Suscripción a guardar
     * @return Suscripción guardada
     */
    Subscription save(Subscription subscription);

    /**
     * Busca una suscripción por su ID.
     * 
     * @param id ID de la suscripción
     * @return Suscripción encontrada o Optional vacío
     */
    Optional<Subscription> findById(UUID id);

    /**
     * Busca una suscripción activa por ID de cliente.
     * 
     * @param clientId ID del cliente
     * @return Suscripción activa encontrada o Optional vacío
     */
    Optional<Subscription> findActiveByClientId(String clientId);

    /**
     * Busca todas las suscripciones de un cliente.
     * 
     * @param clientId ID del cliente
     * @return Lista de suscripciones del cliente
     */
    List<Subscription> findByClientId(String clientId);

    /**
     * Busca todas las suscripciones activas.
     * 
     * @return Lista de suscripciones activas
     */
    List<Subscription> findAllActive();

    /**
     * Busca suscripciones activas que están suscritas a un tipo de evento específico.
     * 
     * @param eventType Tipo de evento
     * @return Lista de suscripciones activas suscritas al evento
     */
    List<Subscription> findActiveByEventType(String eventType);

    /**
     * Verifica si existe una suscripción activa para un cliente.
     * 
     * @param clientId ID del cliente
     * @return true si existe una suscripción activa, false en caso contrario
     */
    boolean existsActiveByClientId(String clientId);

    /**
     * Verifica si existe una suscripción con el ID dado.
     * 
     * @param id ID de la suscripción
     * @return true si existe, false en caso contrario
     */
    boolean existsById(UUID id);

    /**
     * Elimina una suscripción.
     * 
     * @param id ID de la suscripción a eliminar
     */
    void deleteById(UUID id);
}

