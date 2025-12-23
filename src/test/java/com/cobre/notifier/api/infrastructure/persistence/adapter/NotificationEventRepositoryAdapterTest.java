package com.cobre.notifier.api.infrastructure.persistence.adapter;

import com.cobre.notifier.api.domain.DeliveryStatus;
import com.cobre.notifier.api.domain.NotificationEvent;
import com.cobre.notifier.api.infrastructure.persistence.entity.NotificationEventEntity;
import com.cobre.notifier.api.infrastructure.persistence.mapper.NotificationEventMapper;
import com.cobre.notifier.api.infrastructure.persistence.repository.NotificationEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para Fase 4: Infrastructure - Persistencia - NotificationEventRepositoryAdapter
 * Valida la integración entre el adapter y el repositorio JPA.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fase 4: Infrastructure - Persistencia - NotificationEventRepositoryAdapter Tests")
class NotificationEventRepositoryAdapterTest {

    @Mock
    private NotificationEventJpaRepository jpaRepository;

    @Mock
    private NotificationEventMapper mapper;

    @InjectMocks
    private NotificationEventRepositoryAdapter adapter;

    private NotificationEvent notification;
    private NotificationEventEntity entity;

    @BeforeEach
    void setUp() {
        notification = NotificationEvent.create(
                "client-123", "payment.completed", "{\"amount\":100.0}", 
                "https://example.com/webhook");
        entity = NotificationEventEntity.builder()
                .id(notification.getId())
                .clientId(notification.getClientId())
                .eventType(notification.getEventType())
                .payload(notification.getPayload())
                .webhookUrl(notification.getWebhookUrl())
                .status(notification.getStatus())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Should save notification event")
    void shouldSaveNotificationEvent() {
        // Given
        when(mapper.toEntity(notification)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(notification);

        // When
        NotificationEvent result = adapter.save(notification);

        // Then
        assertThat(result).isEqualTo(notification);
        verify(mapper, times(1)).toEntity(notification);
        verify(jpaRepository, times(1)).save(entity);
        verify(mapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Should find notification event by id")
    void shouldFindNotificationEventById() {
        // Given
        UUID id = notification.getId();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(notification);

        // When
        Optional<NotificationEvent> result = adapter.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(notification);
        verify(jpaRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should return empty when notification not found")
    void shouldReturnEmptyWhenNotificationNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<NotificationEvent> result = adapter.findById(id);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find notifications by client id with filters")
    void shouldFindNotificationsByClientIdWithFilters() {
        // Given
        String clientId = "client-123";
        DeliveryStatus status = DeliveryStatus.PENDING;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        List<NotificationEventEntity> entities = List.of(entity);
        
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(notification);

        // When
        List<NotificationEvent> result = adapter.findByClientId(clientId, status, fromDate, toDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(notification);
        verify(jpaRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should find all notifications with filters")
    void shouldFindAllNotificationsWithFilters() {
        // Given
        DeliveryStatus status = DeliveryStatus.SENT;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        List<NotificationEventEntity> entities = List.of(entity);
        
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(notification);

        // When
        List<NotificationEvent> result = adapter.findAll(status, fromDate, toDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(notification);
        verify(jpaRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should find pending retries")
    void shouldFindPendingRetries() {
        // Given
        int maxRetries = 3;
        int limit = 100;
        List<NotificationEventEntity> entities = List.of(entity);
        
        when(jpaRepository.findPendingRetries(maxRetries)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(notification);

        // When
        List<NotificationEvent> result = adapter.findPendingRetries(maxRetries, limit);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(notification);
        verify(jpaRepository, times(1)).findPendingRetries(maxRetries);
    }

    @Test
    @DisplayName("Should apply limit to pending retries")
    void shouldApplyLimitToPendingRetries() {
        // Given
        int maxRetries = 3;
        int limit = 1;
        NotificationEventEntity entity2 = NotificationEventEntity.builder()
                .id(UUID.randomUUID())
                .clientId("client-456")
                .eventType("payment.failed")
                .payload("{}")
                .webhookUrl("https://example.com/webhook")
                .status(DeliveryStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        List<NotificationEventEntity> entities = List.of(entity, entity2);
        
        when(jpaRepository.findPendingRetries(maxRetries)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(notification);

        // When
        List<NotificationEvent> result = adapter.findPendingRetries(maxRetries, limit);

        // Then
        assertThat(result).hasSize(limit);
    }

    @Test
    @DisplayName("Should check if notification exists by id")
    void shouldCheckIfNotificationExistsById() {
        // Given
        UUID id = notification.getId();
        when(jpaRepository.existsById(id)).thenReturn(true);

        // When
        boolean exists = adapter.existsById(id);

        // Then
        assertThat(exists).isTrue();
        verify(jpaRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Should count notifications by client id and status")
    void shouldCountNotificationsByClientIdAndStatus() {
        // Given
        String clientId = "client-123";
        DeliveryStatus status = DeliveryStatus.SENT;
        when(jpaRepository.countByClientIdAndStatus(clientId, status)).thenReturn(5L);

        // When
        long count = adapter.countByClientIdAndStatus(clientId, status);

        // Then
        assertThat(count).isEqualTo(5L);
        verify(jpaRepository, times(1)).countByClientIdAndStatus(clientId, status);
    }
}

