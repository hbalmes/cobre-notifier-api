package com.cobre.notifier.api.infrastructure.persistence.repository;

import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.infrastructure.persistence.entity.NotificationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA Spring Data para NotificationEventEntity.
 * Proporciona métodos de consulta automáticos y personalizados.
 */
@Repository
public interface NotificationEventJpaRepository extends JpaRepository<NotificationEventEntity, UUID>, JpaSpecificationExecutor<NotificationEventEntity> {

    /**
     * Busca notificaciones pendientes de reintento.
     * Notificaciones con estado PENDING o RETRYING que aún pueden ser reintentadas.
     */
    @Query("SELECT n FROM NotificationEventEntity n WHERE " +
           "(n.status = 'PENDING' OR n.status = 'RETRYING') AND " +
           "n.retryCount < :maxRetries " +
           "ORDER BY n.updatedAt ASC")
    List<NotificationEventEntity> findPendingRetries(
            @Param("maxRetries") int maxRetries);

    /**
     * Cuenta notificaciones por cliente y estado.
     */
    long countByClientIdAndStatus(String clientId, DeliveryStatus status);

    /**
     * Verifica si existe una notificación con el ID dado.
     */
    boolean existsById(UUID id);
}

