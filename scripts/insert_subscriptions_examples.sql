-- ============================================
-- Script de Ejemplos para Insertar Suscripciones
-- ============================================
-- Este script contiene ejemplos listos para usar en TablePlus o cualquier cliente SQL
-- Simplemente copia y pega el ejemplo que necesites

-- ============================================
-- EJEMPLO 1: CLIENT001 - Múltiples tipos de eventos
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    current_timestamp BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    current_timestamp := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    -- Insertar suscripción
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT001',
        'https://webhook.site/unique-id-123',
        true,
        current_timestamp,
        current_timestamp
    );
    
    -- Insertar tipos de eventos
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'credit_card_payment'),
        (subscription_uuid, 'debit_card_withdrawal'),
        (subscription_uuid, 'credit_transfer'),
        (subscription_uuid, 'debit_automatic_payment');
    
    RAISE NOTICE 'Suscripción creada para CLIENT001 con ID: %', subscription_uuid;
END $$;

-- ============================================
-- EJEMPLO 2: CLIENT002 - Solo eventos de crédito
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    current_timestamp BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    current_timestamp := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT002',
        'https://webhook.site/unique-id-456',
        true,
        current_timestamp,
        current_timestamp
    );
    
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'credit_card_payment'),
        (subscription_uuid, 'credit_transfer'),
        (subscription_uuid, 'credit_refund'),
        (subscription_uuid, 'credit_deposit'),
        (subscription_uuid, 'credit_cashback');
    
    RAISE NOTICE 'Suscripción creada para CLIENT002 con ID: %', subscription_uuid;
END $$;

-- ============================================
-- EJEMPLO 3: CLIENT003 - Solo eventos de débito
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    current_timestamp BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    current_timestamp := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT003',
        'https://webhook.site/unique-id-789',
        true,
        current_timestamp,
        current_timestamp
    );
    
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'debit_card_withdrawal'),
        (subscription_uuid, 'debit_automatic_payment'),
        (subscription_uuid, 'debit_transfer'),
        (subscription_uuid, 'debit_purchase'),
        (subscription_uuid, 'debit_subscription');
    
    RAISE NOTICE 'Suscripción creada para CLIENT003 con ID: %', subscription_uuid;
END $$;

-- ============================================
-- EJEMPLO 4: CLIENT004 - Todos los eventos
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    current_timestamp BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    current_timestamp := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT004',
        'https://webhook.site/unique-id-101112',
        true,
        current_timestamp,
        current_timestamp
    );
    
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'credit_card_payment'),
        (subscription_uuid, 'debit_card_withdrawal'),
        (subscription_uuid, 'credit_transfer'),
        (subscription_uuid, 'debit_automatic_payment'),
        (subscription_uuid, 'credit_refund'),
        (subscription_uuid, 'debit_transfer'),
        (subscription_uuid, 'credit_deposit'),
        (subscription_uuid, 'debit_purchase'),
        (subscription_uuid, 'credit_cashback'),
        (subscription_uuid, 'debit_subscription');
    
    RAISE NOTICE 'Suscripción creada para CLIENT004 con ID: %', subscription_uuid;
END $$;

-- ============================================
-- VERIFICAR SUSCRIPCIONES CREADAS
-- ============================================
SELECT 
    s.id,
    s.client_id,
    s.webhook_url,
    s.is_active,
    s.created_at,
    array_agg(set.event_type ORDER BY set.event_type) as event_types
FROM subscriptions s
LEFT JOIN subscription_event_types set ON s.id = set.subscription_id
GROUP BY s.id, s.client_id, s.webhook_url, s.is_active, s.created_at
ORDER BY s.created_at DESC;

-- ============================================
-- LIMPIAR SUSCRIPCIONES (si necesitas empezar de nuevo)
-- ============================================
-- CUIDADO: Esto eliminará todas las suscripciones y sus tipos de eventos
-- DELETE FROM subscription_event_types;
-- DELETE FROM subscriptions;

