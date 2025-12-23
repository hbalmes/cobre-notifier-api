# cURLs para Postman - WireMock Testing

## Collection de Postman

Importa el archivo `postman_collection_wiremock.json` en Postman para tener los 4 requests listos.

## cURLs Individuales

### 1. CLIENT001 - Éxito (200 OK)

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "CLIENT001",
    "event_type": "credit_card_payment",
    "content": "{\"amount\": 150.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-001\"}"
  }'
```

**Variaciones para múltiples ejecuciones:**

```bash
# Con diferentes event_types
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT001", "event_type": "debit_card_withdrawal", "content": "{\"amount\": 50.00}"}'

curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT001", "event_type": "credit_transfer", "content": "{\"amount\": 100.00}"}'
```

### 2. CLIENT002 - Error 500

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "CLIENT002",
    "event_type": "credit_card_payment",
    "content": "{\"amount\": 200.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-002\"}"
  }'
```

**Variaciones:**

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT002", "event_type": "credit_transfer", "content": "{\"amount\": 250.00}"}'

curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT002", "event_type": "credit_refund", "content": "{\"amount\": 30.00}"}'
```

### 3. CLIENT003 - Timeout

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "CLIENT003",
    "event_type": "debit_card_withdrawal",
    "content": "{\"amount\": 75.50, \"currency\": \"USD\", \"transaction_id\": \"TXN-003\"}"
  }'
```

**Variaciones:**

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT003", "event_type": "debit_automatic_payment", "content": "{\"amount\": 45.00}"}'

curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT003", "event_type": "debit_transfer", "content": "{\"amount\": 90.00}"}'
```

### 4. CLIENT004 - Error 400

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "CLIENT004",
    "event_type": "credit_card_payment",
    "content": "{\"amount\": 300.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-004\"}"
  }'
```

**Variaciones:**

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT004", "event_type": "debit_card_withdrawal", "content": "{\"amount\": 120.00}"}'

curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{"client_id": "CLIENT004", "event_type": "credit_transfer", "content": "{\"amount\": 500.00}"}'
```

## Script para Ejecutar Múltiples Veces

Ejecuta el script `postman_curls_wiremock.sh` para enviar múltiples eventos y generar métricas:

```bash
chmod +x scripts/postman_curls_wiremock.sh
./scripts/postman_curls_wiremock.sh
```

O ejecuta directamente:

```bash
bash scripts/postman_curls_wiremock.sh
```

## Formato del Body

Todos los requests usan el mismo formato:

```json
{
  "client_id": "CLIENTXXX",
  "event_type": "tipo_de_evento",
  "content": "{\"campo1\": \"valor1\", \"campo2\": \"valor2\"}"
}
```

**Nota importante**: El campo `content` debe ser un JSON **stringificado** (con comillas escapadas).

## Event Types Disponibles por Cliente

### CLIENT001
- `credit_card_payment`
- `debit_card_withdrawal`
- `credit_transfer`
- `debit_automatic_payment`

### CLIENT002
- `credit_card_payment`
- `credit_transfer`
- `credit_refund`

### CLIENT003
- `debit_card_withdrawal`
- `debit_automatic_payment`
- `debit_transfer`

### CLIENT004
- Todos los tipos de eventos

## Respuesta Esperada

```json
{
  "event_id": "uuid-generado",
  "client_id": "CLIENT001",
  "event_type": "credit_card_payment",
  "content": "{\"amount\": 150.00, ...}",
  "published_at": "2024-12-22T23:14:09Z",
  "status": "published"
}
```

## Verificar Resultados

Después de enviar eventos, verifica:

1. **Notificaciones creadas:**
   ```bash
   curl "http://localhost:8080/notification_events?client_id=CLIENT001"
   ```

2. **Métricas en Prometheus:**
   ```bash
   curl http://localhost:8080/actuator/prometheus | grep notification_retry
   ```

3. **Dashboard de Grafana:**
   ```
   http://localhost:3000
   ```

4. **Requests recibidos en WireMock:**
   ```
   http://localhost:8089/__admin/requests
   ```

