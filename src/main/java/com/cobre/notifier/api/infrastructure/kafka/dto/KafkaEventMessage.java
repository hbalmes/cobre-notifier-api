package com.cobre.notifier.api.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un mensaje de evento recibido desde Kafka.
 * Mapea el formato del evento desde el topic platform.events.
 * El cliente solo debe enviar: client_id, event_type y content.
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

    @JsonProperty("content")
    private String content;
}

