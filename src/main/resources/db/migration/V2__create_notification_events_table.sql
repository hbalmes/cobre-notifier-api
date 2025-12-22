-- Migration: Create notification_events table
-- Description: Creates the notification_events table to store notification delivery events

CREATE TABLE notification_events (
    id UUID PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    webhook_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    response_code VARCHAR(10),
    response_body TEXT
);

-- Index for client_id lookups
CREATE INDEX idx_notification_client_id ON notification_events(client_id);

-- Index for status filtering
CREATE INDEX idx_notification_status ON notification_events(status);

-- Index for date range queries
CREATE INDEX idx_notification_created_at ON notification_events(created_at);

-- Composite index for client and status queries
CREATE INDEX idx_notification_client_status ON notification_events(client_id, status);

-- Index for retry queries (status + retry_count)
CREATE INDEX idx_notification_retry ON notification_events(status, retry_count, updated_at);

-- Add check constraint for status values
ALTER TABLE notification_events 
ADD CONSTRAINT chk_notification_status 
CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRYING'));

