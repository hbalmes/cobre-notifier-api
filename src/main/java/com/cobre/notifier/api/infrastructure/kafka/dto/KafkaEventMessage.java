package com.cobre.notifier.api.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un mensaje de evento recibido desde Kafka.
 * Mapea el formato del evento desde el topic platform.events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEventMessage {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("payload")
    private String payload;

    @JsonProperty("timestamp")
    private Long timestamp;
}

