-- Migration: Add unique constraint on kafka_event_id
-- Description: Adds a unique constraint on kafka_event_id to prevent duplicate processing of the same Kafka event.
-- This constraint only applies to non-null values, allowing multiple notifications without kafka_event_id.

-- First, remove any duplicate kafka_event_id values (keep the oldest one)
-- This is a safety measure in case duplicates already exist
WITH duplicates AS (
    SELECT kafka_event_id, MIN(created_at) as min_created_at
    FROM notification_events
    WHERE kafka_event_id IS NOT NULL
    GROUP BY kafka_event_id
    HAVING COUNT(*) > 1
)
DELETE FROM notification_events n1
WHERE EXISTS (
    SELECT 1 FROM duplicates d
    WHERE n1.kafka_event_id = d.kafka_event_id
    AND n1.created_at > d.min_created_at
);

-- Create unique index on kafka_event_id (only for non-null values)
-- PostgreSQL unique indexes automatically ignore NULL values, so this works as a partial unique constraint
CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_kafka_event_id_unique 
ON notification_events(kafka_event_id) 
WHERE kafka_event_id IS NOT NULL;

-- Add comment
COMMENT ON INDEX idx_notification_kafka_event_id_unique IS 'Unique constraint on kafka_event_id to ensure idempotency. Only applies to non-null values.';

