# API Specifications

Esta carpeta contiene las especificaciones de la API en formato OpenAPI/Swagger.

## Archivos

- `openapi.json` - Especificación OpenAPI 3.0 completa de la API

## Uso

### Ver especificación en Swagger UI

La especificación puede visualizarse en:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs JSON**: http://localhost:8080/api-docs

### Generar especificación

Para actualizar la especificación desde la aplicación en ejecución:

```bash
curl http://localhost:8080/api-docs > specs/openapi.json
```

### Validar especificación

Puedes validar el archivo OpenAPI usando herramientas como:

- [Swagger Editor](https://editor.swagger.io/)
- [OpenAPI Validator](https://www.npmjs.com/package/swagger-cli)

```bash
# Con swagger-cli
npm install -g swagger-cli
swagger-cli validate specs/openapi.json
```

## Endpoints Documentados

- `GET /api/v1/notification_events` - Obtener todas las notificaciones
- `GET /api/v1/notification_events/{id}` - Obtener notificación por ID
- `POST /api/v1/notification_events/{id}/replay` - Reintentar notificación

