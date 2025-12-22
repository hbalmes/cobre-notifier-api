# Seguridad - Cobre Notifier API

## 🔒 Consideraciones de Seguridad

Este documento describe las consideraciones de seguridad implementadas y recomendaciones para el despliegue en producción.

## 🛡️ Medidas de Seguridad Implementadas

### 1. Validación de Entrada

- **Validación de Suscripciones**: Se valida que las suscripciones existan y estén activas antes de procesar notificaciones
- **Validación de Clientes**: Se verifica que las notificaciones pertenezcan al cliente correcto
- **Validación de Estados**: Se valida que las operaciones se realicen en estados válidos

### 2. Manejo de Errores

- **Excepciones de Dominio**: Errores específicos sin exponer detalles internos
- **Global Exception Handler**: Manejo centralizado de excepciones
- **Logs Seguros**: No se registran datos sensibles en logs

### 3. Configuración Segura

- **Variables de Entorno**: Credenciales y configuraciones sensibles via variables de entorno
- **Perfiles de Spring**: Separación entre desarrollo y producción
- **Swagger Deshabilitado en Producción**: Documentación API deshabilitada por defecto en `prod`

### 4. Base de Datos

- **Migraciones Flyway**: Control de versiones de esquema
- **Validación de Esquema**: `ddl-auto: validate` previene cambios automáticos
- **Índices Optimizados**: Para prevenir problemas de rendimiento

### 5. Docker

- **Usuario No-Root**: La aplicación corre como usuario `spring:spring` (no root)
- **Imagen Alpine**: Imagen base ligera con menor superficie de ataque
- **Healthchecks**: Monitoreo de salud de contenedores

## ⚠️ Recomendaciones para Producción

### 1. Autenticación y Autorización

**⚠️ IMPORTANTE**: La API actualmente **NO tiene autenticación**. Para producción, se recomienda:

- Implementar **OAuth2** o **JWT** para autenticación
- Agregar **autorización basada en roles** (RBAC)
- Validar tokens en cada request
- Implementar rate limiting

**Ejemplo de implementación sugerida**:
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/api/v1/notification_events")
public ResponseEntity<List<NotificationEventResponse>> getAll(...) {
    // ...
}
```

### 2. HTTPS/TLS

- **Siempre usar HTTPS** en producción
- Configurar certificados SSL/TLS válidos
- Forzar redirección HTTP → HTTPS
- Usar TLS 1.2+ (deshabilitar versiones antiguas)

### 3. Variables de Entorno Sensibles

**Nunca commitear**:
- Contraseñas de base de datos
- API keys
- Tokens de autenticación
- Certificados privados

**Usar**:
- Secret managers (AWS Secrets Manager, HashiCorp Vault, etc.)
- Variables de entorno en orquestadores (Kubernetes Secrets)
- `.env` files (no versionados)

### 4. Logs y Monitoreo

- **No registrar datos sensibles** en logs:
  - Contraseñas
  - Tokens
  - Payloads completos de eventos
  - URLs de webhooks con credenciales

- **Implementar log rotation**
- **Centralizar logs** (ELK, Splunk, etc.)
- **Alertas de seguridad** (intentos de acceso no autorizados)

### 5. Webhooks

- **Validar firmas de webhooks** si el proveedor las soporta
- **Timeouts configurados** para prevenir DoS
- **Rate limiting** en webhooks salientes
- **Validar URLs de webhook** antes de guardar suscripciones
- **Whitelist de dominios** permitidos para webhooks

### 6. Base de Datos

- **Conexiones encriptadas**: Usar SSL/TLS para conexiones a PostgreSQL
- **Credenciales rotadas**: Cambiar contraseñas regularmente
- **Backups regulares**: Implementar estrategia de backups
- **Acceso restringido**: Solo la aplicación debe tener acceso a la BD
- **Principio de menor privilegio**: Usuario de BD con permisos mínimos necesarios

### 7. Kafka

- **Autenticación SASL**: Configurar autenticación en Kafka
- **Encriptación TLS**: Usar TLS para comunicación con Kafka
- **ACLs (Access Control Lists)**: Restringir acceso a topics
- **Validación de mensajes**: Validar formato y contenido de mensajes

### 8. API REST

- **Rate Limiting**: Implementar límites de requests por IP/cliente
- **CORS configurado**: Restringir orígenes permitidos
- **Validación de entrada**: Validar todos los parámetros de entrada
- **Sanitización**: Sanitizar inputs para prevenir inyecciones

### 9. Contenedores Docker

- **Imágenes escaneadas**: Escanear imágenes para vulnerabilidades
- **Actualizaciones**: Mantener imágenes base actualizadas
- **Secrets**: No pasar secrets como variables de entorno en Dockerfiles
- **Network policies**: Restringir comunicación entre contenedores

### 10. Observabilidad

- **Métricas sensibles**: No exponer métricas con datos sensibles
- **Dashboards protegidos**: Autenticar acceso a Grafana
- **Alertas**: Configurar alertas para comportamientos sospechosos

## 🔐 Checklist de Seguridad Pre-Producción

- [ ] Autenticación y autorización implementadas
- [ ] HTTPS/TLS configurado
- [ ] Variables de entorno sensibles en secret manager
- [ ] Swagger deshabilitado en producción
- [ ] Logs no contienen datos sensibles
- [ ] Conexiones a BD encriptadas
- [ ] Kafka con autenticación y TLS
- [ ] Rate limiting implementado
- [ ] CORS configurado correctamente
- [ ] Health checks configurados
- [ ] Backups de BD configurados
- [ ] Monitoreo y alertas configurados
- [ ] Imágenes Docker escaneadas
- [ ] Documentación de seguridad actualizada

## 🚨 Incidentes de Seguridad

En caso de detectar una vulnerabilidad:

1. **No crear issues públicos** con detalles de seguridad
2. **Contactar al equipo de seguridad** directamente
3. **Documentar el incidente** internamente
4. **Aplicar parches** lo antes posible
5. **Comunicar** a usuarios afectados si es necesario

## 📚 Referencias

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security](https://spring.io/projects/spring-security)
- [Docker Security](https://docs.docker.com/engine/security/)
- [PostgreSQL Security](https://www.postgresql.org/docs/current/security.html)

