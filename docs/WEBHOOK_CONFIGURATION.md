# Configuración de Webhook URL

## Descripción General

La URL del webhook se configura en la tabla `subscriptions` mediante el campo `webhook_url`. **Es importante entender que la configuración es por cliente, no por mensaje individual**.

Cada cliente tiene una suscripción activa que define:
- Los tipos de eventos a los que está suscrito (`event_types`)
- La URL del webhook donde recibir las notificaciones (`webhook_url`)
- El estado de la suscripción (`is_active`)

## Flujo de Procesamiento

Cuando se procesa un evento de Kafka, el sistema sigue este flujo:

```
1. Kafka Event llega al Consumer
   ↓
2. Consumer busca Subscription por client_id
   ↓
3. Valida que la suscripción esté activa y tenga el event_type
   ↓
4. Usa webhook_url de la suscripción para enviar la notificación
   ↓
5. Intenta entregar la notificación al webhook_url configurado
```

**Nota importante**: La URL del webhook **NO** viene en el mensaje de Kafka. Se obtiene de la suscripción del cliente almacenada en la base de datos.

## Configuración en Base de Datos

### Crear una Suscripción con Webhook URL

```sql
DO $$
DECLARE
    subscription_uuid UUID;
    current_timestamp BIGINT;
BEGIN
    subscription_uuid := gen_random_uuid();
    current_timestamp := EXTRACT(EPOCH FROM NOW()) * 1000;
    
    -- Insertar suscripción con webhook URL
    INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
    VALUES (
        subscription_uuid,
        'CLIENT001',
        'https://webhook.site/unique-id-123',  -- ← URL del webhook
        true,
        current_timestamp,
        current_timestamp
    );
    
    -- Insertar tipos de eventos
    INSERT INTO subscription_event_types (subscription_id, event_type)
    VALUES 
        (subscription_uuid, 'credit_card_payment'),
        (subscription_uuid, 'debit_card_withdrawal');
    
    RAISE NOTICE 'Suscripción creada para CLIENT001 con webhook: https://webhook.site/unique-id-123';
END $$;
```

### Actualizar Webhook URL de una Suscripción Existente

```sql
-- Actualizar webhook URL para un cliente específico
UPDATE subscriptions 
SET webhook_url = 'https://nueva-url-webhook.com/endpoint',
    updated_at = EXTRACT(EPOCH FROM NOW()) * 1000
WHERE client_id = 'CLIENT001';
```

### Verificar Configuración Actual

```sql
-- Ver suscripción y webhook URL de un cliente
SELECT 
    s.client_id,
    s.webhook_url,
    s.is_active,
    array_agg(set.event_type) as event_types
FROM subscriptions s
LEFT JOIN subscription_event_types set ON s.id = set.subscription_id
WHERE s.client_id = 'CLIENT001'
GROUP BY s.id, s.client_id, s.webhook_url, s.is_active;
```

## Ejemplos de Configuración

### Ejemplo 1: Cliente con Webhook de Prueba

```sql
-- CLIENT001 apunta a webhook.site para pruebas
INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'CLIENT001',
    'https://webhook.site/unique-id-123',
    true,
    EXTRACT(EPOCH FROM NOW()) * 1000,
    EXTRACT(EPOCH FROM NOW()) * 1000
);
```

### Ejemplo 2: Cliente con WireMock (Mock Server)

```sql
-- CLIENT002 apunta a WireMock para simular diferentes respuestas
INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'CLIENT002',
    'http://wiremock:8089/webhook/client002',
    true,
    EXTRACT(EPOCH FROM NOW()) * 1000,
    EXTRACT(EPOCH FROM NOW()) * 1000
);
```

### Ejemplo 3: Cliente con Webhook de Producción

```sql
-- CLIENT003 apunta a endpoint de producción del cliente
INSERT INTO subscriptions (id, client_id, webhook_url, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'CLIENT003',
    'https://api.cliente.com/webhooks/cobre',
    true,
    EXTRACT(EPOCH FROM NOW()) * 1000,
    EXTRACT(EPOCH FROM NOW()) * 1000
);
```

## Validaciones

El sistema valida automáticamente:

1. **Suscripción activa**: Solo se procesan eventos para clientes con `is_active = true`
2. **Webhook URL válida**: La URL no puede ser `NULL` o vacía
3. **Tipo de evento**: El cliente debe estar suscrito al tipo de evento recibido

Si alguna validación falla, el evento se rechaza y no se intenta entregar.

## Cambio de Webhook URL

Cuando se actualiza la `webhook_url` de una suscripción:

- Los **nuevos eventos** usarán la nueva URL inmediatamente
- Los eventos **ya procesados** mantienen la URL original en `notification_events.webhook_url`
- Los eventos **pendientes de reintento** pueden usar la nueva URL si se procesan después del cambio

## Recomendaciones

1. **URLs HTTPS**: Siempre usar HTTPS en producción para seguridad
2. **URLs de prueba**: Usar servicios como `webhook.site` o WireMock para desarrollo/testing
3. **Validación de URL**: Verificar que la URL sea accesible antes de activar la suscripción
4. **Monitoreo**: Configurar alertas para webhooks que fallan frecuentemente
5. **Documentación**: Mantener documentación de las URLs de webhook por cliente

## Troubleshooting

### Problema: Las notificaciones no se están entregando

**Verificar**:
1. ¿La suscripción está activa? (`is_active = true`)
2. ¿La URL del webhook es válida y accesible?
3. ¿El cliente está suscrito al tipo de evento?
4. ¿Hay errores en los logs de la aplicación?

### Problema: Las notificaciones llegan a la URL incorrecta

**Solución**: Verificar y actualizar el `webhook_url` en la tabla `subscriptions` para el cliente correspondiente.

### Problema: Necesito cambiar la URL temporalmente

**Solución**: Actualizar el `webhook_url` en la base de datos. Los nuevos eventos usarán la nueva URL inmediatamente.

## Referencias

- [Tabla subscriptions](../src/main/resources/db/migration/V1__create_subscriptions_table.sql)
- [KafkaEventConsumer](../src/main/java/com/cobre/notifier/api/infrastructure/kafka/consumer/KafkaEventConsumer.java)
- [NotificationService](../src/main/java/com/cobre/notifier/api/application/service/NotificationService.java)

