package com.cobre.notifier.api.infrastructure.persistence.repository;

import com.cobre.notifier.api.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA Spring Data para SubscriptionEntity.
 * Proporciona métodos de consulta automáticos y personalizados.
 */
@Repository
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, UUID> {

    /**
     * Busca una suscripción activa por ID de cliente.
     */
    Optional<SubscriptionEntity> findByClientIdAndIsActiveTrue(String clientId);

    /**
     * Busca todas las suscripciones de un cliente.
     */
    List<SubscriptionEntity> findByClientId(String clientId);

    /**
     * Busca todas las suscripciones activas.
     */
    List<SubscriptionEntity> findByIsActiveTrue();

    /**
     * Busca suscripciones activas que están suscritas a un tipo de evento específico.
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE " +
           "s.isActive = true AND " +
           ":eventType MEMBER OF s.eventTypes")
    List<SubscriptionEntity> findActiveByEventType(@Param("eventType") String eventType);

    /**
     * Verifica si existe una suscripción activa para un cliente.
     */
    boolean existsByClientIdAndIsActiveTrue(String clientId);

    /**
     * Verifica si existe una suscripción con el ID dado.
     */
    boolean existsById(UUID id);
}

