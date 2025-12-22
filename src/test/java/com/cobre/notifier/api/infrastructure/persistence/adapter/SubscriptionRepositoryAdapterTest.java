package com.cobre.notifier.api.infrastructure.persistence.adapter;

import com.cobre.notifier.api.domain.Subscription;
import com.cobre.notifier.api.infrastructure.persistence.entity.SubscriptionEntity;
import com.cobre.notifier.api.infrastructure.persistence.mapper.SubscriptionMapper;
import com.cobre.notifier.api.infrastructure.persistence.repository.SubscriptionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests para Fase 4: Infrastructure - Persistencia - SubscriptionRepositoryAdapter
 * Valida la integración entre el adapter y el repositorio JPA.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fase 4: Infrastructure - Persistencia - SubscriptionRepositoryAdapter Tests")
class SubscriptionRepositoryAdapterTest {

    @Mock
    private SubscriptionJpaRepository jpaRepository;

    @Mock
    private SubscriptionMapper mapper;

    @InjectMocks
    private SubscriptionRepositoryAdapter adapter;

    private Subscription subscription;
    private SubscriptionEntity entity;

    @BeforeEach
    void setUp() {
        subscription = Subscription.create(
                "client-123", 
                List.of("payment.completed", "payment.failed"), 
                "https://example.com/webhook");
        entity = SubscriptionEntity.builder()
                .id(subscription.getId())
                .clientId(subscription.getClientId())
                .eventTypes(subscription.getEventTypes())
                .webhookUrl(subscription.getWebhookUrl())
                .isActive(subscription.getIsActive())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Should save subscription")
    void shouldSaveSubscription() {
        // Given
        when(mapper.toEntity(subscription)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // When
        Subscription result = adapter.save(subscription);

        // Then
        assertThat(result).isEqualTo(subscription);
        verify(mapper, times(1)).toEntity(subscription);
        verify(jpaRepository, times(1)).save(entity);
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Should find subscription by id")
    void shouldFindSubscriptionById() {
        // Given
        UUID id = subscription.getId();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // When
        Optional<Subscription> result = adapter.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(subscription);
        verify(jpaRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should return empty when subscription not found")
    void shouldReturnEmptyWhenSubscriptionNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Subscription> result = adapter.findById(id);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find active subscription by client id")
    void shouldFindActiveSubscriptionByClientId() {
        // Given
        String clientId = "client-123";
        when(jpaRepository.findByClientIdAndIsActiveTrue(clientId))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // When
        Optional<Subscription> result = adapter.findActiveByClientId(clientId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(subscription);
        verify(jpaRepository, times(1)).findByClientIdAndIsActiveTrue(clientId);
    }

    @Test
    @DisplayName("Should find subscriptions by client id")
    void shouldFindSubscriptionsByClientId() {
        // Given
        String clientId = "client-123";
        List<SubscriptionEntity> entities = List.of(entity);
        when(jpaRepository.findByClientId(clientId)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // When
        List<Subscription> result = adapter.findByClientId(clientId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(subscription);
        verify(jpaRepository, times(1)).findByClientId(clientId);
    }

    @Test
    @DisplayName("Should find all active subscriptions")
    void shouldFindAllActiveSubscriptions() {
        // Given
        List<SubscriptionEntity> entities = List.of(entity);
        when(jpaRepository.findByIsActiveTrue()).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // When
        List<Subscription> result = adapter.findAllActive();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(subscription);
        verify(jpaRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should find active subscriptions by event type")
    void shouldFindActiveSubscriptionsByEventType() {
        // Given
        String eventType = "payment.completed";
        List<SubscriptionEntity> entities = List.of(entity);
        when(jpaRepository.findActiveByEventType(eventType)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(subscription);

        // When
        List<Subscription> result = adapter.findActiveByEventType(eventType);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(subscription);
        verify(jpaRepository, times(1)).findActiveByEventType(eventType);
    }

    @Test
    @DisplayName("Should check if active subscription exists by client id")
    void shouldCheckIfActiveSubscriptionExistsByClientId() {
        // Given
        String clientId = "client-123";
        when(jpaRepository.existsByClientIdAndIsActiveTrue(clientId)).thenReturn(true);

        // When
        boolean exists = adapter.existsActiveByClientId(clientId);

        // Then
        assertThat(exists).isTrue();
        verify(jpaRepository, times(1)).existsByClientIdAndIsActiveTrue(clientId);
    }

    @Test
    @DisplayName("Should check if subscription exists by id")
    void shouldCheckIfSubscriptionExistsById() {
        // Given
        UUID id = subscription.getId();
        when(jpaRepository.existsById(id)).thenReturn(true);

        // When
        boolean exists = adapter.existsById(id);

        // Then
        assertThat(exists).isTrue();
        verify(jpaRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Should delete subscription by id")
    void shouldDeleteSubscriptionById() {
        // Given
        UUID id = subscription.getId();
        doNothing().when(jpaRepository).deleteById(id);

        // When
        adapter.deleteById(id);

        // Then
        verify(jpaRepository, times(1)).deleteById(id);
    }
}

