# WireMock Configuration

WireMock es un servidor mock HTTP que permite simular diferentes respuestas de webhooks para testing.

## Configuración

WireMock está configurado en `docker-compose.yml` y escucha en el puerto `8089`.

## Mappings por Cliente

Los mappings están configurados para responder según el `client_id` en el body del request:

- **CLIENT001**: Siempre éxito (200 OK)
- **CLIENT002**: Siempre error 500 (Internal Server Error)
- **CLIENT003**: Timeout (delay de 10 segundos, mayor que el timeout de la app)
- **CLIENT004**: Error 400 (Bad Request)
- **Default**: Éxito (200 OK) para cualquier otro cliente

## Uso

### Configurar suscripción para usar WireMock

```sql
-- Ejemplo: CLIENT002 apunta a WireMock para simular errores
UPDATE subscriptions 
SET webhook_url = 'http://wiremock:8089/webhook/client002'
WHERE client_id = 'CLIENT002';
```

### Acceder a la UI de WireMock

La UI de WireMock está disponible en: `http://localhost:8089/__admin`

### Ver requests recibidos

Los requests recibidos se pueden ver en: `http://localhost:8089/__admin/requests`

### Crear nuevos mappings

1. Agregar archivo JSON en `wiremock/mappings/`
2. Reiniciar el contenedor: `docker-compose restart wiremock`
3. O usar la API REST de WireMock para crear mappings dinámicos

## API REST de WireMock

### Crear mapping dinámico

```bash
curl -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "POST",
      "urlPathPattern": "/webhook/test"
    },
    "response": {
      "status": 200,
      "body": "OK"
    }
  }'
```

### Ver todos los mappings

```bash
curl http://localhost:8089/__admin/mappings
```

### Resetear mappings

```bash
curl -X POST http://localhost:8089/__admin/reset
```

## Referencias

- [WireMock Documentation](http://wiremock.org/docs/)
- [WireMock Docker Image](https://hub.docker.com/r/wiremock/wiremock)

