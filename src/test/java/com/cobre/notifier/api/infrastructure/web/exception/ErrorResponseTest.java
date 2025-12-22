package com.cobre.notifier.api.infrastructure.web.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    @Test
    @DisplayName("Should create ErrorResponse with no-args constructor")
    void shouldCreateErrorResponseWithNoArgsConstructor() {
        // When
        ErrorResponse errorResponse = new ErrorResponse();

        // Then
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNull();
        assertThat(errorResponse.getStatus()).isNull();
        assertThat(errorResponse.getError()).isNull();
        assertThat(errorResponse.getMessage()).isNull();
        assertThat(errorResponse.getDetails()).isNull();
    }

    @Test
    @DisplayName("Should create ErrorResponse with all-args constructor")
    void shouldCreateErrorResponseWithAllArgsConstructor() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Integer status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        Map<String, String> details = new HashMap<>();
        details.put("field", "value");

        // When
        ErrorResponse errorResponse = new ErrorResponse(
                timestamp, status, error, message, details
        );

        // Then
        assertThat(errorResponse.getTimestamp()).isEqualTo(timestamp);
        assertThat(errorResponse.getStatus()).isEqualTo(status);
        assertThat(errorResponse.getError()).isEqualTo(error);
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("Should create ErrorResponse with builder")
    void shouldCreateErrorResponseWithBuilder() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = new HashMap<>();
        details.put("clientId", "must not be blank");

        // When
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .details(details)
                .build();

        // Then
        assertThat(errorResponse.getTimestamp()).isEqualTo(timestamp);
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("Bad Request");
        assertThat(errorResponse.getMessage()).isEqualTo("Validation failed");
        assertThat(errorResponse.getDetails()).isEqualTo(details);
        assertThat(errorResponse.getDetails()).containsKey("clientId");
        assertThat(errorResponse.getDetails().get("clientId")).isEqualTo("must not be blank");
    }

    @Test
    @DisplayName("Should create ErrorResponse with builder without details")
    void shouldCreateErrorResponseWithBuilderWithoutDetails() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(500)
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .details(null)
                .build();

        // Then
        assertThat(errorResponse.getTimestamp()).isEqualTo(timestamp);
        assertThat(errorResponse.getStatus()).isEqualTo(500);
        assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(errorResponse.getDetails()).isNull();
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = new HashMap<>();
        details.put("field1", "error1");
        details.put("field2", "error2");

        // When
        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(404);
        errorResponse.setError("Not Found");
        errorResponse.setMessage("Resource not found");
        errorResponse.setDetails(details);

        // Then
        assertThat(errorResponse.getTimestamp()).isEqualTo(timestamp);
        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getError()).isEqualTo("Not Found");
        assertThat(errorResponse.getMessage()).isEqualTo("Resource not found");
        assertThat(errorResponse.getDetails()).isEqualTo(details);
        assertThat(errorResponse.getDetails()).hasSize(2);
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Bad Request")
                .message("Error")
                .details(new HashMap<>())
                .build();

        // When
        errorResponse.setTimestamp(null);
        errorResponse.setStatus(null);
        errorResponse.setError(null);
        errorResponse.setMessage(null);
        errorResponse.setDetails(null);

        // Then
        assertThat(errorResponse.getTimestamp()).isNull();
        assertThat(errorResponse.getStatus()).isNull();
        assertThat(errorResponse.getError()).isNull();
        assertThat(errorResponse.getMessage()).isNull();
        assertThat(errorResponse.getDetails()).isNull();
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = new HashMap<>();
        details.put("field", "value");

        ErrorResponse errorResponse1 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .details(details)
                .build();

        ErrorResponse errorResponse2 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .details(details)
                .build();

        ErrorResponse errorResponse3 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(500)
                .error("Internal Server Error")
                .message("Error")
                .details(null)
                .build();

        // Then
        assertThat(errorResponse1).isEqualTo(errorResponse2);
        assertThat(errorResponse1.hashCode()).isEqualTo(errorResponse2.hashCode());
        assertThat(errorResponse1).isNotEqualTo(errorResponse3);
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .build();

        // When
        String toString = errorResponse.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("400");
        assertThat(toString).contains("Bad Request");
        assertThat(toString).contains("Validation failed");
    }

    @Test
    @DisplayName("Should handle empty details map")
    void shouldHandleEmptyDetailsMap() {
        // Given
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .details(new HashMap<>())
                .build();

        // Then
        assertThat(errorResponse.getDetails()).isNotNull();
        assertThat(errorResponse.getDetails()).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple details")
    void shouldHandleMultipleDetails() {
        // Given
        Map<String, String> details = new HashMap<>();
        details.put("clientId", "must not be blank");
        details.put("status", "must not be null");
        details.put("fromDate", "must be a valid date");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .details(details)
                .build();

        // Then
        assertThat(errorResponse.getDetails()).hasSize(3);
        assertThat(errorResponse.getDetails()).containsKey("clientId");
        assertThat(errorResponse.getDetails()).containsKey("status");
        assertThat(errorResponse.getDetails()).containsKey("fromDate");
    }
}

