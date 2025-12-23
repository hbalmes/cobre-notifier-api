# Métricas de Reintentos y Webhooks

Este documento describe todas las métricas disponibles relacionadas con reintentos de notificaciones y entrega de webhooks.

## Métricas de Reintentos

### Contadores Básicos

#### `notification_retry_attempts_total`
- **Tipo**: Counter
- **Descripción**: Total de reintentos realizados
- **Uso**: Monitorear el volumen total de reintentos
- **Query Prometheus**:
  ```promql
  sum(rate(notification_retry_attempts_total[5m]))
  ```

#### `notification_retry_attempts_success`
- **Tipo**: Counter
- **Descripción**: Número de reintentos exitosos
- **Uso**: Medir cuántos reintentos resultan en entrega exitosa
- **Query Prometheus**:
  ```promql
  sum(rate(notification_retry_attempts_success[5m]))
  ```

#### `notification_retry_attempts_failed`
- **Tipo**: Counter
- **Descripción**: Número de reintentos que continúan fallando
- **Uso**: Identificar problemas persistentes con webhooks
- **Query Prometheus**:
  ```promql
  sum(rate(notification_retry_attempts_failed[5m]))
  ```

#### `notification_retry_attempts_max_reached`
- **Tipo**: Counter
- **Descripción**: Número de notificaciones que alcanzaron el máximo de reintentos
- **Uso**: Alertar cuando muchas notificaciones fallan definitivamente
- **Query Prometheus**:
  ```promql
  sum(rate(notification_retry_attempts_max_reached[5m]))
  ```

### Métricas de Tiempo

#### `notification_retry_delay`
- **Tipo**: Timer
- **Descripción**: Tiempo de delay entre reintentos (exponential backoff)
- **Uso**: Monitorear los delays aplicados entre reintentos
- **Queries Prometheus**:
  ```promql
  # Promedio de delay
  rate(notification_retry_delay_sum[5m]) / rate(notification_retry_delay_count[5m])
  
  # P95 de delay
  histogram_quantile(0.95, sum(rate(notification_retry_delay_bucket[5m])) by (le))
  ```

### Métricas de Estado

#### `notification_retry_count`
- **Tipo**: Gauge
- **Descripción**: Número actual de notificaciones pendientes de reintento
- **Uso**: Monitorear la cola de notificaciones pendientes
- **Query Prometheus**:
  ```promql
  notification_retry_count
  ```

#### `notification_retry_success_rate`
- **Tipo**: Gauge
- **Descripción**: Tasa de éxito después de reintentos (porcentaje)
- **Uso**: Medir la efectividad de los reintentos
- **Query Prometheus**:
  ```promql
  notification_retry_success_rate
  ```

### Métricas Detalladas (con Tags)

#### `notification_retry_by_status`
- **Tipo**: Counter
- **Tags**: `status` (PENDING, RETRYING, FAILED, SENT)
- **Descripción**: Reintentos agrupados por estado de notificación
- **Uso**: Analizar qué estados requieren más reintentos
- **Query Prometheus**:
  ```promql
  sum(rate(notification_retry_by_status_total[5m])) by (status)
  ```

#### `notification_retry_by_client`
- **Tipo**: Counter
- **Tags**: `client_id`
- **Descripción**: Reintentos agrupados por cliente
- **Uso**: Identificar clientes con problemas de webhook
- **Query Prometheus**:
  ```promql
  topk(10, sum(rate(notification_retry_by_client_total[5m])) by (client_id))
  ```

#### `notification_retry_distribution`
- **Tipo**: DistributionSummary
- **Descripción**: Distribución del número de reintentos por notificación
- **Uso**: Analizar cuántos reintentos típicamente se necesitan
- **Queries Prometheus**:
  ```promql
  # Mediana
  histogram_quantile(0.5, sum(rate(notification_retry_distribution_bucket[5m])) by (le))
  
  # P95
  histogram_quantile(0.95, sum(rate(notification_retry_distribution_bucket[5m])) by (le))
  
  # P99
  histogram_quantile(0.99, sum(rate(notification_retry_distribution_bucket[5m])) by (le))
  ```

## Métricas de Entrega de Webhooks

Estas métricas están implementadas en `WebhookClientAdapter`:

### `webhook_delivery_success`
- **Tipo**: Counter
- **Descripción**: Total de entregas exitosas de webhooks

### `webhook_delivery_failure`
- **Tipo**: Counter
- **Descripción**: Total de fallos en entrega de webhooks

### `webhook_delivery_client_error`
- **Tipo**: Counter
- **Descripción**: Errores 4xx del cliente

### `webhook_delivery_server_error`
- **Tipo**: Counter
- **Descripción**: Errores 5xx del servidor

### `webhook_delivery_timeout`
- **Tipo**: Counter
- **Descripción**: Timeouts en entrega

### `webhook_delivery_duration`
- **Tipo**: Timer
- **Descripción**: Duración de la entrega del webhook

## Queries Útiles de Prometheus

### Tasa de Éxito de Reintentos

```promql
# Tasa de éxito después de reintentos
notification_retry_success_rate

# O calcular manualmente
sum(rate(notification_retry_attempts_success[5m])) / 
sum(rate(notification_retry_attempts_total[5m])) * 100
```

### Clientes con Más Reintentos

```promql
topk(10, sum(rate(notification_retry_by_client_total[5m])) by (client_id))
```

### Notificaciones que Alcanzaron Máximo de Reintentos

```promql
sum(rate(notification_retry_attempts_max_reached[5m]))
```

### Distribución de Reintentos

```promql
# Histograma completo
histogram_quantile(0.5, sum(rate(notification_retry_distribution_bucket[5m])) by (le))
histogram_quantile(0.95, sum(rate(notification_retry_distribution_bucket[5m])) by (le))
histogram_quantile(0.99, sum(rate(notification_retry_distribution_bucket[5m])) by (le))
```

### Reintentos por Estado

```promql
sum(rate(notification_retry_by_status_total[5m])) by (status)
```

### Tiempo Promedio entre Reintentos

```promql
rate(notification_retry_delay_sum[5m]) / rate(notification_retry_delay_count[5m])
```

## Alertas Recomendadas

### Alerta: Muchas Notificaciones Alcanzaron Máximo de Reintentos

```yaml
alert: HighMaxRetriesReached
expr: sum(rate(notification_retry_attempts_max_reached[5m])) > 10
for: 5m
labels:
  severity: warning
annotations:
  summary: "Más de 10 notificaciones alcanzaron el máximo de reintentos en los últimos 5 minutos"
  description: "{{ $value }} notificaciones alcanzaron el máximo de reintentos"
```

### Alerta: Tasa de Éxito Baja

```yaml
alert: LowRetrySuccessRate
expr: notification_retry_success_rate < 50
for: 10m
labels:
  severity: critical
annotations:
  summary: "Tasa de éxito de reintentos por debajo del 50%"
  description: "La tasa de éxito actual es {{ $value }}%"
```

### Alerta: Muchas Notificaciones Pendientes

```yaml
alert: HighPendingRetries
expr: notification_retry_count > 100
for: 5m
labels:
  severity: warning
annotations:
  summary: "Más de 100 notificaciones pendientes de reintento"
  description: "Hay {{ $value }} notificaciones esperando reintento"
```

## Dashboard de Grafana

El dashboard `Webhook Retries Dashboard` incluye los siguientes paneles:

1. **Resumen de Reintentos**: Total, exitosos, fallidos, max alcanzados
2. **Tasa de Éxito**: Gauge con porcentaje de éxito
3. **Distribución de Reintentos**: Histograma con percentiles
4. **Reintentos por Cliente**: Top 10 clientes con más reintentos
5. **Tiempo entre Reintentos**: Promedio y P95 de delays
6. **Estado Actual**: Notificaciones pendientes, en reintento, fallidas
7. **Alertas y Tendencias**: Tendencias de max reintentos y tasa de éxito

### Variables del Dashboard

- `client_id`: Filtro por cliente (dropdown)
- `time_range`: Selector de rango de tiempo

## Acceso a las Métricas

### Endpoint de Prometheus

Las métricas están disponibles en:
```
http://localhost:8080/actuator/prometheus
```

### Prometheus UI

Ver métricas en Prometheus:
```
http://localhost:9090
```

### Grafana Dashboard

Acceder al dashboard:
```
http://localhost:3000
```

Usuario: `admin` / Contraseña: `admin` (por defecto)

## Referencias

- [RetryService](../src/main/java/com/cobre/notifier/api/application/service/RetryService.java)
- [NotificationService](../src/main/java/com/cobre/notifier/api/application/service/NotificationService.java)
- [WebhookClientAdapter](../src/main/java/com/cobre/notifier/api/infrastructure/webhook/WebhookClientAdapter.java)
- [Dashboard de Grafana](../grafana/dashboards/webhook-retries-dashboard.json)

