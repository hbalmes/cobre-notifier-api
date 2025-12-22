package com.cobre.notifier.api.infrastructure.web.exception;

import com.cobre.notifier.api.domain.exception.DomainException;
import com.cobre.notifier.api.domain.exception.InvalidNotificationStateException;
import com.cobre.notifier.api.domain.exception.InvalidSubscriptionException;
import com.cobre.notifier.api.domain.exception.NotificationDeliveryException;
import com.cobre.notifier.api.domain.exception.NotificationNotFoundException;
import com.cobre.notifier.api.domain.exception.SubscriptionNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Should handle NotificationNotFoundException")
    void shouldHandleNotificationNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        NotificationNotFoundException ex = new NotificationNotFoundException(id);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotificationNotFoundException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains(id.toString());
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle SubscriptionNotFoundException")
    void shouldHandleSubscriptionNotFoundException() {
        // Given
        String clientId = "client-123";
        SubscriptionNotFoundException ex = new SubscriptionNotFoundException(clientId);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleSubscriptionNotFoundException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains(clientId);
    }

    @Test
    @DisplayName("Should handle InvalidNotificationStateException")
    void shouldHandleInvalidNotificationStateException() {
        // Given
        InvalidNotificationStateException ex = new InvalidNotificationStateException("Invalid state");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidNotificationStateException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid state");
    }

    @Test
    @DisplayName("Should handle InvalidSubscriptionException")
    void shouldHandleInvalidSubscriptionException() {
        // Given
        InvalidSubscriptionException ex = new InvalidSubscriptionException("Invalid subscription");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidSubscriptionException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid subscription");
    }

    @Test
    @DisplayName("Should handle NotificationDeliveryException")
    void shouldHandleNotificationDeliveryException() {
        // Given
        NotificationDeliveryException ex = new NotificationDeliveryException(
                "https://example.com/webhook", 
                "Delivery failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotificationDeliveryException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).contains("Delivery failed");
    }

    @Test
    @DisplayName("Should handle generic DomainException")
    void shouldHandleGenericDomainException() {
        // Given
        DomainException ex = new DomainException("Generic domain error") {
            // Anonymous subclass for testing
        };

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDomainException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Generic domain error");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "clientId", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getDetails()).isNotNull();
        assertThat(response.getBody().getDetails()).containsKey("clientId");
        assertThat(response.getBody().getDetails().get("clientId")).isEqualTo("must not be blank");
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with required type")
    void shouldHandleMethodArgumentTypeMismatchExceptionWithRequiredType() {
        // Given
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("invalid-uuid");
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenAnswer(invocation -> UUID.class);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatchException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("invalid-uuid");
        assertThat(response.getBody().getMessage()).contains("id");
        assertThat(response.getBody().getMessage()).contains("UUID");
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException without required type")
    void shouldHandleMethodArgumentTypeMismatchExceptionWithoutRequiredType() {
        // Given
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("invalid-value");
        when(ex.getName()).thenReturn("param");
        when(ex.getRequiredType()).thenReturn(null);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatchException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("invalid-value");
        assertThat(response.getBody().getMessage()).contains("param");
        assertThat(response.getBody().getMessage()).contains("unknown");
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with multiple field errors")
    void shouldHandleMethodArgumentNotValidExceptionWithMultipleFieldErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("request", "clientId", "must not be blank");
        FieldError fieldError2 = new FieldError("request", "status", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isNotNull();
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails()).containsKey("clientId");
        assertThat(response.getBody().getDetails()).containsKey("status");
    }
}

