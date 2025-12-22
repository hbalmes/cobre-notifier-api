package com.cobre.notifier.api.infrastructure.persistence.adapter;

import com.cobre.notifier.api.application.port.output.SubscriptionRepository;
import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.infrastructure.persistence.entity.SubscriptionEntity;
import com.cobre.notifier.api.infrastructure.persistence.mapper.SubscriptionMapper;
import com.cobre.notifier.api.infrastructure.persistence.repository.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter que implementa el puerto de salida SubscriptionRepository.
 * Conecta la capa de aplicación con la capa de persistencia JPA.
 */
@Component
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;
    private final SubscriptionMapper mapper;

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity entity = mapper.toEntity(subscription);
        SubscriptionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Subscription> findActiveByClientId(String clientId) {
        return jpaRepository.findByClientIdAndIsActiveTrue(clientId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findByClientId(String clientId) {
        return jpaRepository.findByClientId(clientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subscription> findAllActive() {
        return jpaRepository.findByIsActiveTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subscription> findActiveByEventType(String eventType) {
        return jpaRepository.findActiveByEventType(eventType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsActiveByClientId(String clientId) {
        return jpaRepository.existsByClientIdAndIsActiveTrue(clientId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

