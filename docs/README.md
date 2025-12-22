# Cobre Notifier API

<div class="badges">
  <a href="https://www.oracle.com/java/" class="badge badge-java" target="_blank">Java 21</a>
  <a href="https://spring.io/projects/spring-boot" class="badge badge-spring" target="_blank">Spring Boot 3.2.0</a>
  <a href="https://maven.apache.org/" class="badge badge-npm" target="_blank">Maven</a>
  <a href="https://www.postgresql.org/" class="badge badge-coverage" target="_blank">PostgreSQL 15</a>
  <a href="https://kafka.apache.org/" class="badge badge-kafka" target="_blank">Apache Kafka</a>
  <a href="https://opensource.org/licenses/MIT" class="badge badge-license" target="_blank">License MIT</a>
</div>

Sistema de notificaciones event-driven para Cobre. Consume eventos de Kafka, valida suscripciones de clientes y entrega notificaciones vía webhooks HTTPS con estrategia de retry con exponential backoff.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Uso](#uso)
- [API REST](#api-rest)
- [Docker](#docker)
- [Testing](#testing)
- [Documentación](#documentación)
- [Despliegue](#despliegue)

## ✨ Características

- ✅ Consumo de eventos desde Kafka (`platform.events`)
- ✅ Validación de suscripciones de clientes
- ✅ Entrega de notificaciones vía webhooks HTTPS
- ✅ Estrategia de retry con exponential backoff
- ✅ API REST para consultas y replay manual
- ✅ Observabilidad con Prometheus y Grafana
- ✅ Documentación OpenAPI/Swagger
- ✅ Arquitectura Hexagonal (Ports & Adapters)
- ✅ Cobertura de código >85%

## 🛠️ Stack Tecnológico

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.2.0** - Framework de aplicación
- **Maven** - Gestión de dependencias
- **PostgreSQL 15** - Base de datos
- **Apache Kafka** - Event streaming
- **Flyway** - Migraciones de base de datos
- **Docker & Docker Compose** - Containerización
- **Prometheus & Grafana** - Observabilidad
- **Springdoc OpenAPI** - Documentación API
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
    ├── webhook/         # Adaptadores de webhook
    └── web/             # Adaptadores web (REST API)
```

Para más detalles, ver [Arquitectura Detallada](ARCHITECTURE.md)

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
make swagger           # Abrir Swagger UI en el navegador
```

### Endpoints Principales

#### Health Check
```bash
curl http://localhost:8080/actuator/health
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

### GET /api/v1/notification_events

Obtiene todas las notificaciones con filtros opcionales.

**Query Parameters:**
- `client_id` (opcional): ID del cliente
- `status` (opcional): Estado de entrega (PENDING, SENT, FAILED, RETRYING)
- `from_date` (opcional): Fecha desde (formato: yyyy-MM-ddTHH:mm:ss)
- `to_date` (opcional): Fecha hasta (formato: yyyy-MM-ddTHH:mm:ss)

**Ejemplo:**
```bash
curl "http://localhost:8080/api/v1/notification_events?client_id=client-123&status=SENT"
```

### GET /api/v1/notification_events/{id}

Obtiene una notificación por su ID.

**Ejemplo:**
```bash
curl http://localhost:8080/api/v1/notification_events/123e4567-e89b-12d3-a456-426614174000
```

### POST /api/v1/notification_events/{id}/replay

Reintenta manualmente el envío de una notificación.

**Ejemplo:**
```bash
curl -X POST http://localhost:8080/api/v1/notification_events/123e4567-e89b-12d3-a456-426614174000/replay
```

Para documentación completa, ver Swagger UI: http://localhost:8080/swagger-ui/index.html

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

- [Arquitectura](ARCHITECTURE.md) - Detalles de la arquitectura hexagonal
- [Seguridad](docs/SECURITY.md) - Consideraciones de seguridad
- [Swagger UI](http://localhost:8080/swagger-ui/index.html) - Documentación interactiva de la API

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

La aplicación expone health checks en `/actuator/health` que pueden ser usados por orquestadores como Kubernetes.

## 📊 Observabilidad

### Métricas Prometheus

La aplicación expone métricas en `/actuator/prometheus`:

- `webhook_delivery_total`: Contador de entregas de webhook
- `webhook_delivery_duration_seconds`: Duración de entregas
- `webhook_delivery_errors_total`: Errores de entrega

### Dashboards Grafana

Los dashboards de Grafana están configurados para conectarse automáticamente a Prometheus.

## 🤝 Contribución

1. Crear branch desde `develop`: `git checkout -b feature/nueva-funcionalidad`
2. Realizar cambios y tests
3. Verificar cobertura: `make test-coverage`
4. Crear Pull Request

## 📝 Licencia

Este proyecto es propiedad de Cobre.

## 👥 Contacto

- **Email**: herbal828@gmail.com
