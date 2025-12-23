package com.cobre.notifier.api.infrastructure.web.dto;

import com.cobre.notifier.api.domain.DeliveryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationEventFilterRequest Tests")
class NotificationEventFilterRequestTest {

    @Test
    @DisplayName("Should create NotificationEventFilterRequest with no-args constructor")
    void shouldCreateNotificationEventFilterRequestWithNoArgsConstructor() {
        // When
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getClientId()).isNull();
        assertThat(request.getStatus()).isNull();
        assertThat(request.getFromDate()).isNull();
        assertThat(request.getToDate()).isNull();
    }

    @Test
    @DisplayName("Should set and get clientId")
    void shouldSetAndGetClientId() {
        // Given
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();
        String clientId = "client-123";

        // When
        request.setClientId(clientId);

        // Then
        assertThat(request.getClientId()).isEqualTo(clientId);
    }

    @Test
    @DisplayName("Should set and get status")
    void shouldSetAndGetStatus() {
        // Given
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();
        DeliveryStatus status = DeliveryStatus.SENT;

        // When
        request.setStatus(status);

        // Then
        assertThat(request.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("Should set and get fromDate")
    void shouldSetAndGetFromDate() {
        // Given
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);

        // When
        request.setFromDate(fromDate);

        // Then
        assertThat(request.getFromDate()).isEqualTo(fromDate);
    }

    @Test
    @DisplayName("Should set and get toDate")
    void shouldSetAndGetToDate() {
        // Given
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();
        LocalDateTime toDate = LocalDateTime.now();

        // When
        request.setToDate(toDate);

        // Then
        assertThat(request.getToDate()).isEqualTo(toDate);
    }

    @Test
    @DisplayName("Should set all fields")
    void shouldSetAllFields() {
        // Given
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();
        String clientId = "client-456";
        DeliveryStatus status = DeliveryStatus.FAILED;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(7);
        LocalDateTime toDate = LocalDateTime.now();

        // When
        request.setClientId(clientId);
        request.setStatus(status);
        request.setFromDate(fromDate);
        request.setToDate(toDate);

        // Then
        assertThat(request.getClientId()).isEqualTo(clientId);
        assertThat(request.getStatus()).isEqualTo(status);
        assertThat(request.getFromDate()).isEqualTo(fromDate);
        assertThat(request.getToDate()).isEqualTo(toDate);
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // Given
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();
        request.setClientId("client-123");
        request.setStatus(DeliveryStatus.SENT);

        // When
        request.setClientId(null);
        request.setStatus(null);
        request.setFromDate(null);
        request.setToDate(null);

        // Then
        assertThat(request.getClientId()).isNull();
        assertThat(request.getStatus()).isNull();
        assertThat(request.getFromDate()).isNull();
        assertThat(request.getToDate()).isNull();
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        NotificationEventFilterRequest request1 = new NotificationEventFilterRequest();
        request1.setClientId("client-123");
        request1.setStatus(DeliveryStatus.SENT);

        NotificationEventFilterRequest request2 = new NotificationEventFilterRequest();
        request2.setClientId("client-123");
        request2.setStatus(DeliveryStatus.SENT);

        NotificationEventFilterRequest request3 = new NotificationEventFilterRequest();
        request3.setClientId("client-456");
        request3.setStatus(DeliveryStatus.SENT);

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        NotificationEventFilterRequest request = new NotificationEventFilterRequest();
        request.setClientId("client-123");
        request.setStatus(DeliveryStatus.SENT);

        // When
        String toString = request.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("client-123");
        assertThat(toString).contains("SENT");
    }
}

