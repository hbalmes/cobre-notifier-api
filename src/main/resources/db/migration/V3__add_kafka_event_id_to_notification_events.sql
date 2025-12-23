-- Migration: Add kafka_event_id to notification_events table
-- Description: Adds optional kafka_event_id field to track the event_id from Kafka message
-- This allows linking the event_id returned by POST /api/v1/events with the notification_event_id

ALTER TABLE notification_events 
ADD COLUMN kafka_event_id VARCHAR(255);

-- Index for kafka_event_id lookups
CREATE INDEX idx_notification_kafka_event_id ON notification_events(kafka_event_id);

-- Add comment
COMMENT ON COLUMN notification_events.kafka_event_id IS 'Optional UUID from Kafka event message, used to link event_id from POST /api/v1/events with notification_event_id';

