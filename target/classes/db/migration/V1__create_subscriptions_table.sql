-- Migration: Create subscriptions table
-- Description: Creates the subscriptions table and subscription_event_types collection table

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL UNIQUE,
    webhook_url VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

-- Index for client_id lookups
CREATE INDEX idx_subscription_client_id ON subscriptions(client_id);

-- Index for active subscriptions
CREATE INDEX idx_subscription_active ON subscriptions(is_active);

-- Composite index for active client subscriptions
CREATE INDEX idx_subscription_client_active ON subscriptions(client_id, is_active);

-- Table for subscription event types (collection)
CREATE TABLE subscription_event_types (
    subscription_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    PRIMARY KEY (subscription_id, event_type),
    CONSTRAINT fk_subscription_event_types_subscription 
        FOREIGN KEY (subscription_id) 
        REFERENCES subscriptions(id) 
        ON DELETE CASCADE
);

-- Index for event type lookups
CREATE INDEX idx_subscription_event_types_type ON subscription_event_types(event_type);

