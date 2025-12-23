# Cobre Notifier API

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)
![License](https://img.shields.io/badge/License-Proprietary-red.svg)

Sistema de notificaciones event-driven para Cobre. Consume eventos de Kafka, valida suscripciones de clientes y entrega notificaciones vía webhooks HTTPS con estrategia de retry con exponential backoff.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Requisitos](#-requisitos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Uso](#-uso)
- [API REST](#-api-rest)
- [Seguridad](#-seguridad)
- [Docker](#-docker)
- [Testing](#-testing)
- [Documentación](#-documentación)
- [Despliegue](#-despliegue)

## ✨ Características

- ✅ Consumo de eventos desde Kafka (`platform.events`)
- ✅ Validación de suscripciones de clientes
- ✅ Entrega de notificaciones vía webhooks HTTPS
- ✅ Estrategia de retry con exponential backoff (3 intentos: 1s, 2s, 4s)
- ✅ Scheduler automático para reintentos cada 10 segundos
- ✅ Idempotencia basada en `kafka_event_id`
- ✅ API REST para consultas y replay manual
- ✅ Observabilidad con Prometheus y Grafana
- ✅ Documentación OpenAPI/Swagger
- ✅ Arquitectura Hexagonal (Ports & Adapters)
- ✅ Cobertura de código >85%

## 🛠️ Stack Tecnológico

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.2.0** - Framework de aplicación
- **Maven** - Gestión de dependencias
- **PostgreSQL 15** - Base de datos relacional
- **Apache Kafka** - Event streaming platform
- **Flyway** - Migraciones de base de datos
- **Docker & Docker Compose** - Containerización
- **Prometheus & Grafana** - Observabilidad y métricas
- **Springdoc OpenAPI** - Documentación API
- **Micrometer** - Métricas para Prometheus
- **JUnit 5, Mockito, AssertJ** - Testing
- **JaCoCo** - Code coverage

## 🏗️ Arquitectura

El proyecto sigue una **Arquitectura Hexagonal (Ports & Adapters)** con separación clara de capas:

```
src/main/java/com/cobre/notifier/api/
├── domain/              # Capa de dominio (entidades y lógica de negocio)
├── application/         # Capa de aplicación (casos de uso y servicios)
│   ├── port/
│   │   ├── input/       # Puertos de entrada (use cases)
│   │   └── output/      # Puertos de salida (repositories)
│   └── service/         # Implementación de servicios
└── infrastructure/      # Capa de infraestructura (adaptadores)
    ├── persistence/     # Adaptadores de persistencia (JPA)
    ├── kafka/           # Adaptadores de Kafka
    ├── scheduler/       # Scheduler para reintentos automáticos
    ├── webhook/         # Adaptadores de webhook
    └── web/             # Adaptadores web (REST API)
```

Para más detalles, ver [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

## 📦 Requisitos

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (para servicios externos)
- PostgreSQL 15 (o usar Docker)
- Apache Kafka (o usar Docker)

## 🚀 Instalación

### Opción 1: Desarrollo Local (sin Docker)

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd cobre-notifier-api
   ```

2. **Configurar base de datos PostgreSQL**
   ```bash
   # Crear base de datos
   createdb cobre_notifier
   
   # O usar Docker solo para PostgreSQL
   docker run -d --name postgres \
     -e POSTGRES_DB=cobre_notifier \
     -e POSTGRES_USER=cobre_user \
     -e POSTGRES_PASSWORD=cobre_password \
     -p 5432:5432 \
     postgres:15-alpine
   ```

3. **Configurar variables de entorno**
   ```bash
   cp .env.example .env
   # Editar .env con tus configuraciones
   ```

4. **Compilar y ejecutar**
   ```bash
   make build
   make run
   ```

### Opción 2: Docker Compose (Recomendado)

1. **Iniciar todos los servicios**
   ```bash
   make docker-up
   ```

   Esto iniciará:
   - PostgreSQL 15
   - Kafka + Zookeeper
   - Prometheus
   - Grafana
   - WireMock (para testing de webhooks)
   - Aplicación Spring Boot

2. **Verificar que todo está corriendo**
   ```bash
   docker-compose ps
   ```

## ⚙️ Configuración

### Variables de Entorno

Copia `.env.example` a `.env` y configura las siguientes variables:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cobre_notifier
DB_USER=cobre_user
DB_PASSWORD=cobre_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC=platform.events
KAFKA_GROUP_ID=cobre-notifier-group

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
SWAGGER_ENABLED=true

# Notifications
NOTIFICATION_MAX_RETRIES=3
NOTIFICATION_INITIAL_DELAY_MS=1000
NOTIFICATION_RETRY_MULTIPLIER=2.0
NOTIFICATION_SCHEDULER_ENABLED=true
NOTIFICATION_SCHEDULER_DELAY_MS=10000

# Webhooks
WEBHOOK_TIMEOUT_MS=5000
WEBHOOK_CONNECT_TIMEOUT_MS=3000
```

### Perfiles de Spring

- **dev**: Desarrollo (SQL logging habilitado, Swagger habilitado)
- **prod**: Producción (SQL logging deshabilitado, Swagger deshabilitado)
- **test**: Testing (H2 in-memory, logging reducido)

## 📖 Uso

### Comandos Make Disponibles

```bash
make help              # Ver todos los comandos disponibles
make build             # Compilar la aplicación
make test              # Ejecutar tests
make test-coverage     # Ejecutar tests con reporte de cobertura
make run               # Ejecutar aplicación localmente
make docker-up         # Iniciar servicios Docker
make docker-down       # Detener servicios Docker
make docker-logs       # Ver logs de servicios Docker
make health            # Verificar salud de la aplicación
make swagger           # Abrir Swagger UI en el navegador
```

### Endpoints Principales

#### Health Check

La aplicación expone health checks en `/actuator/health` siguiendo el estándar de **Spring Boot Actuator**.

**Usando curl:**
```bash
curl http://localhost:8080/actuator/health
```

**Usando Make:**
```bash
make health
```

**Respuesta esperada:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

#### Swagger UI
```bash
open http://localhost:8080/swagger-ui/index.html
# o
make swagger
```

#### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

## 🔌 API REST

### GET /notification_events

Obtiene todas las notificaciones con filtros opcionales.

**Query Parameters:**
- `client_id` o `clientId` (opcional): ID del cliente (ej: CLIENT001)
- `delivery_status` o `deliveryStatus` (opcional): Estado de entrega
  - `completed` → Notificaciones enviadas exitosamente
  - `failed` → Notificaciones fallidas después de todos los reintentos
  - `pending` → Notificaciones pendientes de entrega o reintento
- `from_date` o `fromDate` (opcional): Fecha desde (formato: `yyyy-MM-ddTHH:mm:ss`)
- `to_date` o `toDate` (opcional): Fecha hasta (formato: `yyyy-MM-ddTHH:mm:ss`)

**Nota:** La API acepta tanto `snake_case` (`client_id`, `delivery_status`) como `camelCase` (`clientId`, `deliveryStatus`) para mayor flexibilidad.

**Ejemplo con snake_case:**
```bash
curl "http://localhost:8080/notification_events?client_id=CLIENT001&delivery_status=completed"
```

**Ejemplo con camelCase:**
```bash
curl "http://localhost:8080/notification_events?clientId=CLIENT001&deliveryStatus=completed"
```

**Ejemplo con filtros de fecha:**
```bash
curl "http://localhost:8080/notification_events?from_date=2024-01-01T00:00:00&to_date=2024-12-31T23:59:59"
```

**Respuesta:**
```json
[
  {
    "event_id": "123e4567-e89b-12d3-a456-426614174000",
    "event_type": "credit_card_payment",
    "content": "{\"amount\": 150.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-001\"}",
    "delivery_date": "2024-03-15T09:30:22Z",
    "delivery_status": "completed",
    "client_id": "CLIENT001"
  }
]
```

### GET /notification_events/{notification_event_id}

Obtiene una notificación por su ID.

**Ejemplo:**
```bash
curl http://localhost:8080/notification_events/123e4567-e89b-12d3-a456-426614174000
```

**Respuesta:**
```json
{
  "event_id": "123e4567-e89b-12d3-a456-426614174000",
  "event_type": "debit_transfer",
  "content": "Money transfer sent to Account #8901 for $500.00",
  "delivery_date": "2024-03-15T14:30:55Z",
  "delivery_status": "completed",
  "client_id": "CLIENT003"
}
```

### POST /notification_events/{notification_event_id}/replay

Reintenta manualmente el envío de una notificación. Solo funciona para notificaciones que no han sido enviadas exitosamente (estados `pending` o `failed`).

**Ejemplo:**
```bash
curl -X POST http://localhost:8080/notification_events/123e4567-e89b-12d3-a456-426614174000/replay
```

**Respuesta (202 Accepted):**
```json
{
  "event_id": "123e4567-e89b-12d3-a456-426614174000",
  "event_type": "credit_card_payment",
  "content": "{\"amount\": 150.00, \"currency\": \"USD\"}",
  "delivery_date": "2024-03-15T15:45:10Z",
  "delivery_status": "completed",
  "client_id": "CLIENT001"
}
```

**Errores posibles:**
- `404 Not Found`: Notificación no encontrada
- `400 Bad Request`: No se puede hacer replay de una notificación ya enviada exitosamente

### POST /api/v1/events (Solo Desarrollo)

Publica un evento directamente al topic de Kafka. **Solo disponible en perfil `dev`**.

**Ejemplo:**
```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "CLIENT001",
    "event_type": "credit_card_payment",
    "content": "{\"amount\": 150.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-001\"}"
  }'
```

**Respuesta:**
```json
{
  "event_id": "c51a2951-e39e-4539-bf8a-52980714e4eb",
  "event_type": "credit_card_payment",
  "content": "{\"amount\": 150.00, \"currency\": \"USD\", \"transaction_id\": \"TXN-001\"}",
  "published_at": "2024-03-15T10:30:00Z",
  "status": "published"
}
```

**Tipos de eventos soportados:**
- `credit_card_payment` - Pago con tarjeta de crédito
- `debit_card_withdrawal` - Retiro con tarjeta de débito
- `credit_transfer` - Transferencia recibida
- `debit_automatic_payment` - Pago automático
- `credit_refund` - Reembolso
- `debit_transfer` - Transferencia enviada
- `credit_deposit` - Depósito
- `debit_purchase` - Compra
- `credit_cashback` - Cashback
- `debit_subscription` - Suscripción

Para documentación completa, ver Swagger UI: http://localhost:8080/swagger-ui/index.html

## 🔒 Seguridad

### Vulnerabilidades OWASP Top 10 Identificadas

#### 1. A01:2021 – Broken Access Control

**Riesgo:** La API no implementa autenticación/autorización, permitiendo que cualquier usuario acceda a todas las notificaciones.

**Medidas Mitigadas:**
- ✅ Validación de suscripciones activas antes de procesar eventos
- ✅ Validación de que el cliente está suscrito al tipo de evento específico
- ✅ Filtrado por `client_id` en consultas (aunque no previene acceso a otros clientes)

**Medidas Propuestas:**
- 🔄 Implementar autenticación con JWT o API Keys
- 🔄 Implementar autorización basada en roles (RBAC)
- 🔄 Validar que el usuario solo pueda consultar notificaciones de sus propios clientes
- 🔄 Rate limiting por cliente/IP para prevenir abuso

#### 2. A03:2021 – Injection

**Riesgo:** Posibles inyecciones SQL o de comandos a través de parámetros de entrada.

**Medidas Mitigadas:**
- ✅ Uso de JPA Criteria API para queries dinámicas (previene SQL injection)
- ✅ Uso de Prepared Statements a través de JPA/Hibernate
- ✅ Validación de tipos de datos en parámetros de fecha
- ✅ Sanitización de inputs en el mapeo de `delivery_status`

**Medidas Propuestas:**
- 🔄 Validación más estricta de inputs con Bean Validation (`@Valid`, `@NotNull`, `@Pattern`)
- 🔄 Sanitización de contenido JSON antes de almacenar
- 🔄 Implementar whitelist de caracteres permitidos en `client_id` y `event_type`
- 🔄 Logging de intentos de injection para detección temprana

#### 3. A05:2021 – Security Misconfiguration

**Riesgo:** Configuración insegura de la aplicación, exposición de información sensible, y endpoints de desarrollo expuestos en producción.

**Medidas Mitigadas:**
- ✅ Swagger deshabilitado en producción (`SWAGGER_ENABLED=false`)
- ✅ Endpoint `/api/v1/events` solo disponible en perfil `dev`
- ✅ Health checks con detalles limitados (`show-details: when-authorized`)
- ✅ Variables de entorno para configuración sensible

**Medidas Propuestas:**
- 🔄 Implementar HTTPS obligatorio en producción
- 🔄 Configurar CORS restrictivo para APIs públicas
- 🔄 Ocultar información de versión y stack en headers HTTP
- 🔄 Implementar security headers (HSTS, X-Frame-Options, CSP)
- 🔄 Rotación de credenciales de base de datos y Kafka
- 🔄 Uso de secretos gestionados (AWS Secrets Manager, HashiCorp Vault)

### Resumen de Seguridad

| Vulnerabilidad | Estado | Prioridad |
|---------------|--------|-----------|
| Broken Access Control | ⚠️ Parcialmente mitigado | Alta |
| Injection | ✅ Mitigado | Media |
| Security Misconfiguration | ⚠️ Parcialmente mitigado | Alta |

**Recomendaciones Inmediatas:**
1. Implementar autenticación/autorización antes de producción
2. Configurar HTTPS y security headers
3. Implementar rate limiting
4. Auditar y rotar credenciales regularmente

## 🐳 Docker

### Construir Imagen

```bash
docker build -t cobre-notifier-api .
```

### Ejecutar con Docker Compose

```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Detener servicios
docker-compose down
```

### Servicios Disponibles

- **Aplicación**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **WireMock**: http://localhost:8089 (para testing de webhooks)

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
make test

# Tests con cobertura
make test-coverage

# Ver reporte de cobertura
make coverage-report
```

### Cobertura Actual

- **Instructions**: 85%
- **Branches**: 52%
- **Lines**: ~93%
- **Methods**: ~89%

### Estructura de Tests

```
src/test/java/com/cobre/notifier/api/
├── domain/              # Tests de entidades de dominio
├── application/         # Tests de servicios de aplicación
└── infrastructure/      # Tests de adaptadores
    ├── persistence/     # Tests de persistencia (Testcontainers)
    ├── kafka/           # Tests de Kafka
    ├── webhook/         # Tests de webhook
    └── web/             # Tests de REST API
```

## 📚 Documentación

### Documentación con Docsify

La documentación está disponible con Docsify en Docker:

```bash
# Iniciar servidor de documentación
make docs

# O manualmente
docker-compose up -d docs
```

**URLs de Documentación:**
- **Docsify**: http://localhost:3001
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs JSON**: http://localhost:8080/api-docs

### Estructura de Documentación

- **docs/**: Documentación Markdown (README, Arquitectura, Seguridad)
- **specs/**: Especificaciones OpenAPI/Swagger (openapi.json)

### Actualizar Especificaciones

Para actualizar las especificaciones OpenAPI desde la aplicación en ejecución:

```bash
make docs-update-specs
```

## 🚢 Despliegue

### Requisitos de Producción

1. **Base de datos PostgreSQL** con migraciones aplicadas
2. **Kafka** configurado y accesible
3. **Variables de entorno** configuradas
4. **Perfil `prod`** activado

### Variables de Entorno Críticas

```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<postgres-host>
DB_PASSWORD=<secure-password>
KAFKA_BOOTSTRAP_SERVERS=<kafka-servers>
SWAGGER_ENABLED=false  # Deshabilitar en producción
```

### Health Checks

La aplicación expone health checks en `/actuator/health` que pueden ser usados por orquestadores como Kubernetes. Usa el comando `make health` para verificar el estado de la aplicación.

## 📊 Observabilidad

### Métricas Prometheus

La aplicación expone métricas en `/actuator/prometheus`:

- `kafka_publish_success_total`: Contador de publicaciones exitosas a Kafka
- `kafka_consume_success_total`: Contador de consumos exitosos de Kafka
- `webhook_delivery_success_total`: Contador de entregas exitosas de webhook
- `webhook_delivery_duration_seconds`: Duración de entregas de webhook
- `notification_retry_attempts_total`: Total de intentos de reintento
- `notification_replay_manual_total`: Total de replays manuales

### Dashboards Grafana

Los dashboards de Grafana están configurados para conectarse automáticamente a Prometheus y mostrar:
- Métricas de Kafka (publicaciones y consumos)
- Métricas de webhooks (éxitos, fallos, tiempos)
- Métricas de reintentos y replays
- Top clientes por volumen de notificaciones

## 🤝 Contribución

1. Crear branch desde `develop`: `git checkout -b feature/nueva-funcionalidad`
2. Realizar cambios y tests
3. Verificar cobertura: `make test-coverage`
4. Crear Pull Request

## 📝 Licencia

Este proyecto es propiedad de Cobre.

## 👥 Contacto

- **Email**: herbal828@gmail.com
