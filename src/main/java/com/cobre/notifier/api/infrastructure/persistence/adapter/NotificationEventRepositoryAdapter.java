package com.cobre.notifier.api.infrastructure.persistence.adapter;

import com.cobre.notifier.api.application.port.output.NotificationEventRepository;
import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.persistence.entity.NotificationEventEntity;
import com.cobre.notifier.api.infrastructure.persistence.mapper.NotificationEventMapper;
import com.cobre.notifier.api.infrastructure.persistence.repository.NotificationEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter que implementa el puerto de salida NotificationEventRepository.
 * Conecta la capa de aplicación con la capa de persistencia JPA.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventRepositoryAdapter implements NotificationEventRepository {

    private final NotificationEventJpaRepository jpaRepository;
    private final NotificationEventMapper mapper;

    @Override
    public NotificationEvent save(NotificationEvent notificationEvent) {
        NotificationEventEntity entity = mapper.toEntity(notificationEvent);
        NotificationEventEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<NotificationEvent> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<NotificationEvent> findByClientId(String clientId, 
                                                  DeliveryStatus status, 
                                                  LocalDateTime fromDate, 
                                                  LocalDateTime toDate) {
        List<NotificationEventEntity> entities = jpaRepository.findByClientIdWithFilters(
                clientId, status, fromDate, toDate);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationEvent> findAll(DeliveryStatus status, 
                                          LocalDateTime fromDate, 
                                          LocalDateTime toDate) {
        List<NotificationEventEntity> entities = jpaRepository.findAllWithFilters(
                status, fromDate, toDate);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationEvent> findPendingRetries(int maxRetries, int limit) {
        List<NotificationEventEntity> entities = jpaRepository.findPendingRetries(maxRetries);
        // Aplicar límite manualmente si es necesario
        if (limit > 0 && entities.size() > limit) {
            entities = entities.subList(0, limit);
        }
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long countByClientIdAndStatus(String clientId, DeliveryStatus status) {
        return jpaRepository.countByClientIdAndStatus(clientId, status);
    }
}

