-- ============================================
-- Script para Insertar Suscripciones con WireMock
-- ============================================
-- Este script configura suscripciones que apuntan a WireMock
-- para testing de diferentes escenarios de webhook

-- ============================================
-- CLIENT001 - WireMock: Siempre éxito (200 OK)
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    ts BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    ts := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    -- Insertar suscripción apuntando a WireMock
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT001',
        'http://wiremock:8080/webhook/client001',  -- WireMock responderá con 200 OK (puerto 8080 desde dentro de Docker)
        true,
        ts,
        ts
    )
    ON CONFLICT (client_id) DO UPDATE
    SET webhook_url = EXCLUDED.webhook_url,
        updated_at = EXCLUDED.updated_at;
    
    -- Obtener el UUID actualizado si ya existía
    SELECT id INTO subscription_uuid FROM subscriptions WHERE client_id = 'CLIENT001';
    
    -- Eliminar event_types existentes para este cliente
    DELETE FROM subscription_event_types WHERE subscription_id = subscription_uuid;
    
    -- Insertar tipos de eventos
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'credit_card_payment'),
        (subscription_uuid, 'debit_card_withdrawal'),
        (subscription_uuid, 'credit_transfer'),
        (subscription_uuid, 'debit_automatic_payment');
    
    RAISE NOTICE 'Suscripción CLIENT001 configurada con WireMock (éxito) - ID: %', subscription_uuid;
END $$;

-- ============================================
-- CLIENT002 - WireMock: Siempre error 500
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    ts BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    ts := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT002',
        'http://wiremock:8080/webhook/client002',  -- WireMock responderá con 500 (puerto 8080 desde dentro de Docker)
        true,
        ts,
        ts
    )
    ON CONFLICT (client_id) DO UPDATE
    SET webhook_url = EXCLUDED.webhook_url,
        updated_at = EXCLUDED.updated_at;
    
    SELECT id INTO subscription_uuid FROM subscriptions WHERE client_id = 'CLIENT002';
    
    DELETE FROM subscription_event_types WHERE subscription_id = subscription_uuid;
    
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'credit_card_payment'),
        (subscription_uuid, 'credit_transfer'),
        (subscription_uuid, 'credit_refund');
    
    RAISE NOTICE 'Suscripción CLIENT002 configurada con WireMock (error 500) - ID: %', subscription_uuid;
END $$;

-- ============================================
-- CLIENT003 - WireMock: Timeout (delay 10s)
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    ts BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    ts := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT003',
        'http://wiremock:8080/webhook/client003',  -- WireMock responderá con timeout (10s delay) (puerto 8080 desde dentro de Docker)
        true,
        ts,
        ts
    )
    ON CONFLICT (client_id) DO UPDATE
    SET webhook_url = EXCLUDED.webhook_url,
        updated_at = EXCLUDED.updated_at;
    
    SELECT id INTO subscription_uuid FROM subscriptions WHERE client_id = 'CLIENT003';
    
    DELETE FROM subscription_event_types WHERE subscription_id = subscription_uuid;
    
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'debit_card_withdrawal'),
        (subscription_uuid, 'debit_automatic_payment'),
        (subscription_uuid, 'debit_transfer');
    
    RAISE NOTICE 'Suscripción CLIENT003 configurada con WireMock (timeout) - ID: %', subscription_uuid;
END $$;

-- ============================================
-- CLIENT004 - WireMock: Error 400 (Bad Request)
-- ============================================
DO $$
DECLARE
    subscription_uuid UUID;
    ts BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    ts := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT004',
        'http://wiremock:8080/webhook/client004',  -- WireMock responderá con 400 (puerto 8080 desde dentro de Docker)
        true,
        ts,
        ts
    )
    ON CONFLICT (client_id) DO UPDATE
    SET webhook_url = EXCLUDED.webhook_url,
        updated_at = EXCLUDED.updated_at;
    
    SELECT id INTO subscription_uuid FROM subscriptions WHERE client_id = 'CLIENT004';
    
    DELETE FROM subscription_event_types WHERE subscription_id = subscription_uuid;
    
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'credit_card_payment'),
        (subscription_uuid, 'debit_card_withdrawal'),
        (subscription_uuid, 'credit_transfer'),
        (subscription_uuid, 'debit_automatic_payment'),
        (subscription_uuid, 'credit_refund'),
        (subscription_uuid, 'debit_transfer');
    
    RAISE NOTICE 'Suscripción CLIENT004 configurada con WireMock (error 400) - ID: %', subscription_uuid;
END $$;

-- ============================================
-- Verificar suscripciones creadas
-- ============================================
SELECT 
    s.client_id,
    s.webhook_url,
    s.is_active,
    array_agg(set.event_type ORDER BY set.event_type) as event_types,
    COUNT(set.event_type) as total_event_types
FROM subscriptions s
LEFT JOIN subscription_event_types set ON s.id = set.subscription_id
WHERE s.client_id IN ('CLIENT001', 'CLIENT002', 'CLIENT003', 'CLIENT004')
GROUP BY s.id, s.client_id, s.webhook_url, s.is_active
ORDER BY s.client_id;

-- ============================================
-- Notas de uso:
-- ============================================
-- 1. WireMock debe estar corriendo en el puerto 8080 (interno) / 8089 (externo)
-- 2. Los mappings están en wiremock/mappings/
-- 3. Para usar desde fuera del contenedor Docker, usar: http://localhost:8089/webhook/clientXXX
-- 4. Para usar desde dentro de la red Docker, usar: http://wiremock:8080/webhook/clientXXX (puerto interno)
-- 5. Los event_types deben coincidir con los que se envían en los mensajes de Kafka

