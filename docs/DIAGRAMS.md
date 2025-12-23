# Diagramas del Sistema

Esta sección contiene diagramas visuales que explican el funcionamiento, arquitectura y flujos del sistema Cobre Notifier API.

## Flujo de Funcionamiento Alto Nivel

Este diagrama muestra el flujo principal del sistema desde que recibe un evento de Kafka hasta la entrega del webhook.

```mermaid
flowchart TD
    Kafka[Kafka Event<br/>platform.events] --> Consumer[Kafka Consumer]
    Consumer --> Deserialize[Deserializar Mensaje]
    Deserialize --> ValidateSub[Validar Suscripción]
    ValidateSub -->|No Existe| Error1[Error: Suscripción<br/>No Encontrada]
    ValidateSub -->|No Activa| Error2[Error: Suscripción<br/>Inactiva]
    ValidateSub -->|No Suscrita| Error3[Error: No Suscrita<br/>al Event Type]
    ValidateSub -->|Válida| Create[Crear NotificationEvent<br/>Estado: PENDING]
    Create --> Process[NotificationService<br/>Procesar]
    Process --> ValidateSub2[Validar Suscripción<br/>Actualizada]
    ValidateSub2 --> Webhook[WebhookDeliveryService<br/>Enviar Webhook HTTPS]
    Webhook -->|Éxito 2xx| Success[Estado: SENT<br/>Guardar en BD]
    Webhook -->|Error 4xx/5xx| Failed[Estado: FAILED<br/>Guardar Error]
    Webhook -->|Timeout| Timeout[Estado: FAILED<br/>Timeout Error]
    Failed --> RetryCheck{¿Puede<br/>Reintentar?}
    Timeout --> RetryCheck
    RetryCheck -->|Sí<br/>retryCount < maxRetries| RetryService[RetryService<br/>Scheduler]
    RetryCheck -->|No<br/>Max Retries| FinalFailed[Estado: FAILED<br/>Final]
    RetryService -->|Exponential Backoff| Process
    Success --> End1[Fin]
    FinalFailed --> End2[Fin]
    Error1 --> End3[Fin]
    Error2 --> End3
    Error3 --> End3
```

**Código fuente del diagrama:**

```mermaid
flowchart TD
    Kafka[Kafka Event<br/>platform.events] --> Consumer[Kafka Consumer]
    Consumer --> Deserialize[Deserializar Mensaje]
    Deserialize --> ValidateSub[Validar Suscripción]
    ValidateSub -->|No Existe| Error1[Error: Suscripción<br/>No Encontrada]
    ValidateSub -->|No Activa| Error2[Error: Suscripción<br/>Inactiva]
    ValidateSub -->|No Suscrita| Error3[Error: No Suscrita<br/>al Event Type]
    ValidateSub -->|Válida| Create[Crear NotificationEvent<br/>Estado: PENDING]
    Create --> Process[NotificationService<br/>Procesar]
    Process --> ValidateSub2[Validar Suscripción<br/>Actualizada]
    ValidateSub2 --> Webhook[WebhookDeliveryService<br/>Enviar Webhook HTTPS]
    Webhook -->|Éxito 2xx| Success[Estado: SENT<br/>Guardar en BD]
    Webhook -->|Error 4xx/5xx| Failed[Estado: FAILED<br/>Guardar Error]
    Webhook -->|Timeout| Timeout[Estado: FAILED<br/>Timeout Error]
    Failed --> RetryCheck{¿Puede<br/>Reintentar?}
    Timeout --> RetryCheck
    RetryCheck -->|Sí<br/>retryCount < maxRetries| RetryService[RetryService<br/>Scheduler]
    RetryCheck -->|No<br/>Max Retries| FinalFailed[Estado: FAILED<br/>Final]
    RetryService -->|Exponential Backoff| Process
    Success --> End1[Fin]
    FinalFailed --> End2[Fin]
    Error1 --> End3[Fin]
    Error2 --> End3
    Error3 --> End3
```

## Arquitectura Detallada

Diagrama que muestra la arquitectura hexagonal con todos sus componentes y sus relaciones.

```mermaid
graph TB
    subgraph Infrastructure["Infrastructure Layer"]
        subgraph KafkaInfra["Kafka"]
            KafkaConsumer[KafkaEventConsumer]
            KafkaConfig[KafkaConfig]
            KafkaDTO[KafkaEventMessage]
        end
        subgraph PersistenceInfra["Persistence"]
            JPAEntities[NotificationEventEntity<br/>SubscriptionEntity]
            JPARepos[NotificationEventJpaRepository<br/>SubscriptionJpaRepository]
            Adapters[NotificationEventRepositoryAdapter<br/>SubscriptionRepositoryAdapter]
            Mappers[NotificationEventMapper<br/>SubscriptionMapper]
        end
        subgraph WebInfra["Web REST API"]
            Controller[NotificationEventController]
            WebDTOs[NotificationEventResponse<br/>NotificationEventFilterRequest]
            WebMapper[NotificationEventWebMapper]
            ExceptionHandler[GlobalExceptionHandler]
        end
        subgraph WebhookInfra["Webhook Client"]
            WebhookAdapter[WebhookClientAdapter]
            RestTemplate[RestTemplate]
            Metrics[Prometheus Metrics]
        end
    end
    
    subgraph Application["Application Layer"]
        subgraph InputPorts["Input Ports"]
            ProcessUseCase[ProcessNotificationUseCase]
            RetryUseCase[RetryFailedNotificationsUseCase]
            GetUseCase[GetNotificationEventsUseCase]
            ReplayUseCase[ReplayNotificationUseCase]
        end
        subgraph OutputPorts["Output Ports"]
            NotificationRepo[NotificationEventRepository]
            SubscriptionRepo[SubscriptionRepository]
        end
        subgraph Services["Services"]
            NotificationService[NotificationService]
            RetryService[RetryService]
            WebhookDeliveryService[WebhookDeliveryService]
        end
    end
    
    subgraph Domain["Domain Layer"]
        NotificationEvent[NotificationEvent]
        Subscription[Subscription]
        DeliveryStatus[DeliveryStatus Enum]
        DomainExceptions[Domain Exceptions]
    end
    
    KafkaConsumer --> ProcessUseCase
    ProcessUseCase --> NotificationService
    NotificationService --> NotificationRepo
    NotificationService --> SubscriptionRepo
    NotificationService --> WebhookDeliveryService
    WebhookDeliveryService --> WebhookAdapter
    RetryService --> RetryUseCase
    RetryUseCase --> NotificationService
    Controller --> GetUseCase
    Controller --> ReplayUseCase
    GetUseCase --> NotificationService
    ReplayUseCase --> NotificationService
    
    NotificationService --> NotificationEvent
    NotificationService --> Subscription
    RetryService --> NotificationEvent
    
    NotificationRepo --> Adapters
    SubscriptionRepo --> Adapters
    Adapters --> JPARepos
    Adapters --> Mappers
    Mappers --> JPAEntities
    Mappers --> NotificationEvent
    Mappers --> Subscription
    
    WebhookAdapter --> RestTemplate
    WebhookAdapter --> Metrics
    Controller --> WebMapper
    WebMapper --> NotificationEvent
```

**Código fuente del diagrama:**

```mermaid
graph TB
    subgraph Infrastructure["Infrastructure Layer"]
        subgraph KafkaInfra["Kafka"]
            KafkaConsumer[KafkaEventConsumer]
            KafkaConfig[KafkaConfig]
            KafkaDTO[KafkaEventMessage]
        end
        subgraph PersistenceInfra["Persistence"]
            JPAEntities[NotificationEventEntity<br/>SubscriptionEntity]
            JPARepos[NotificationEventJpaRepository<br/>SubscriptionJpaRepository]
            Adapters[NotificationEventRepositoryAdapter<br/>SubscriptionRepositoryAdapter]
            Mappers[NotificationEventMapper<br/>SubscriptionMapper]
        end
        subgraph WebInfra["Web REST API"]
            Controller[NotificationEventController]
            WebDTOs[NotificationEventResponse<br/>NotificationEventFilterRequest]
            WebMapper[NotificationEventWebMapper]
            ExceptionHandler[GlobalExceptionHandler]
        end
        subgraph WebhookInfra["Webhook Client"]
            WebhookAdapter[WebhookClientAdapter]
            RestTemplate[RestTemplate]
            Metrics[Prometheus Metrics]
        end
    end
    
    subgraph Application["Application Layer"]
        subgraph InputPorts["Input Ports"]
            ProcessUseCase[ProcessNotificationUseCase]
            RetryUseCase[RetryFailedNotificationsUseCase]
            GetUseCase[GetNotificationEventsUseCase]
            ReplayUseCase[ReplayNotificationUseCase]
        end
        subgraph OutputPorts["Output Ports"]
            NotificationRepo[NotificationEventRepository]
            SubscriptionRepo[SubscriptionRepository]
        end
        subgraph Services["Services"]
            NotificationService[NotificationService]
            RetryService[RetryService]
            WebhookDeliveryService[WebhookDeliveryService]
        end
    end
    
    subgraph Domain["Domain Layer"]
        NotificationEvent[NotificationEvent]
        Subscription[Subscription]
        DeliveryStatus[DeliveryStatus Enum]
        DomainExceptions[Domain Exceptions]
    end
    
    KafkaConsumer --> ProcessUseCase
    ProcessUseCase --> NotificationService
    NotificationService --> NotificationRepo
    NotificationService --> SubscriptionRepo
    NotificationService --> WebhookDeliveryService
    WebhookDeliveryService --> WebhookAdapter
    RetryService --> RetryUseCase
    RetryUseCase --> NotificationService
    Controller --> GetUseCase
    Controller --> ReplayUseCase
    GetUseCase --> NotificationService
    ReplayUseCase --> NotificationService
    
    NotificationService --> NotificationEvent
    NotificationService --> Subscription
    RetryService --> NotificationEvent
    
    NotificationRepo --> Adapters
    SubscriptionRepo --> Adapters
    Adapters --> JPARepos
    Adapters --> Mappers
    Mappers --> JPAEntities
    Mappers --> NotificationEvent
    Mappers --> Subscription
    
    WebhookAdapter --> RestTemplate
    WebhookAdapter --> Metrics
    Controller --> WebMapper
    WebMapper --> NotificationEvent
```

## Diagrama de Secuencia - Procesamiento de Notificación

Este diagrama muestra la secuencia completa de interacciones entre componentes cuando se procesa una notificación.

```mermaid
sequenceDiagram
    participant Kafka as Kafka Topic
    participant Consumer as KafkaEventConsumer
    participant SubRepo as SubscriptionRepository
    participant Domain as NotificationEvent Domain
    participant ProcessUseCase as ProcessNotificationUseCase
    participant NotifService as NotificationService
    participant WebhookService as WebhookDeliveryService
    participant WebhookAdapter as WebhookClientAdapter
    participant RestTemplate as RestTemplate
    participant ClientWebhook as Cliente Webhook URL
    participant NotifRepo as NotificationEventRepository
    participant DB as PostgreSQL

    Kafka->>Consumer: Event Message
    Consumer->>Consumer: Deserializar KafkaEventMessage
    Consumer->>SubRepo: findActiveByClientId(clientId)
    SubRepo->>DB: Query Subscription
    DB-->>SubRepo: Subscription Entity
    SubRepo-->>Consumer: Subscription Domain
    
    alt Suscripción No Encontrada o Inactiva
        Consumer-->>Kafka: Error - No Acknowledge
    else Suscripción Válida
        Consumer->>SubRepo: isSubscribedTo(eventType)
        SubRepo-->>Consumer: true/false
        
        alt No Suscrita al Event Type
            Consumer-->>Kafka: Error - No Acknowledge
        else Suscrita al Event Type
            Consumer->>Domain: NotificationEvent.create(...)
            Domain-->>Consumer: NotificationEvent (PENDING)
            Consumer->>ProcessUseCase: process(notification)
            ProcessUseCase->>NotifService: process(notification)
            
            NotifService->>SubRepo: findActiveByClientIdAndEventType(...)
            SubRepo->>DB: Query Subscription
            DB-->>SubRepo: Subscription
            SubRepo-->>NotifService: Subscription
            
            NotifService->>WebhookService: deliver(webhookUrl, payload)
            WebhookService->>WebhookAdapter: deliver(webhookUrl, payload)
            WebhookAdapter->>RestTemplate: POST webhookUrl
            RestTemplate->>ClientWebhook: HTTPS POST Request
            ClientWebhook-->>RestTemplate: HTTP Response
            
            alt Respuesta Exitosa (2xx)
                RestTemplate-->>WebhookAdapter: Response 200
                WebhookAdapter-->>WebhookService: Status Code
                WebhookService-->>NotifService: Success
                NotifService->>Domain: markAsSent(responseCode, body)
                Domain->>Domain: status = SENT
            else Error Cliente (4xx)
                RestTemplate-->>WebhookAdapter: HttpClientErrorException
                WebhookAdapter-->>WebhookService: NotificationDeliveryException
                WebhookService-->>NotifService: Exception
                NotifService->>Domain: markAsFailed(errorMessage)
                Domain->>Domain: status = FAILED
            else Error Servidor (5xx) o Timeout
                RestTemplate-->>WebhookAdapter: HttpServerErrorException/Timeout
                WebhookAdapter-->>WebhookService: NotificationDeliveryException
                WebhookService-->>NotifService: Exception
                NotifService->>Domain: markAsFailed(errorMessage)
                Domain->>Domain: status = FAILED
            end
            
            NotifService->>NotifRepo: save(notification)
            NotifRepo->>DB: INSERT/UPDATE
            DB-->>NotifRepo: Saved Entity
            NotifRepo-->>NotifService: NotificationEvent
            NotifService-->>ProcessUseCase: NotificationEvent
            ProcessUseCase-->>Consumer: NotificationEvent
            Consumer->>Kafka: Acknowledge Message
        end
    end
```

**Código fuente del diagrama:**

```mermaid
sequenceDiagram
    participant Kafka as Kafka Topic
    participant Consumer as KafkaEventConsumer
    participant SubRepo as SubscriptionRepository
    participant Domain as NotificationEvent Domain
    participant ProcessUseCase as ProcessNotificationUseCase
    participant NotifService as NotificationService
    participant WebhookService as WebhookDeliveryService
    participant WebhookAdapter as WebhookClientAdapter
    participant RestTemplate as RestTemplate
    participant ClientWebhook as Cliente Webhook URL
    participant NotifRepo as NotificationEventRepository
    participant DB as PostgreSQL

    Kafka->>Consumer: Event Message
    Consumer->>Consumer: Deserializar KafkaEventMessage
    Consumer->>SubRepo: findActiveByClientId(clientId)
    SubRepo->>DB: Query Subscription
    DB-->>SubRepo: Subscription Entity
    SubRepo-->>Consumer: Subscription Domain
    
    alt Suscripción No Encontrada o Inactiva
        Consumer-->>Kafka: Error - No Acknowledge
    else Suscripción Válida
        Consumer->>SubRepo: isSubscribedTo(eventType)
        SubRepo-->>Consumer: true/false
        
        alt No Suscrita al Event Type
            Consumer-->>Kafka: Error - No Acknowledge
        else Suscrita al Event Type
            Consumer->>Domain: NotificationEvent.create(...)
            Domain-->>Consumer: NotificationEvent (PENDING)
            Consumer->>ProcessUseCase: process(notification)
            ProcessUseCase->>NotifService: process(notification)
            
            NotifService->>SubRepo: findActiveByClientIdAndEventType(...)
            SubRepo->>DB: Query Subscription
            DB-->>SubRepo: Subscription
            SubRepo-->>NotifService: Subscription
            
            NotifService->>WebhookService: deliver(webhookUrl, payload)
            WebhookService->>WebhookAdapter: deliver(webhookUrl, payload)
            WebhookAdapter->>RestTemplate: POST webhookUrl
            RestTemplate->>ClientWebhook: HTTPS POST Request
            ClientWebhook-->>RestTemplate: HTTP Response
            
            alt Respuesta Exitosa (2xx)
                RestTemplate-->>WebhookAdapter: Response 200
                WebhookAdapter-->>WebhookService: Status Code
                WebhookService-->>NotifService: Success
                NotifService->>Domain: markAsSent(responseCode, body)
                Domain->>Domain: status = SENT
            else Error Cliente (4xx)
                RestTemplate-->>WebhookAdapter: HttpClientErrorException
                WebhookAdapter-->>WebhookService: NotificationDeliveryException
                WebhookService-->>NotifService: Exception
                NotifService->>Domain: markAsFailed(errorMessage)
                Domain->>Domain: status = FAILED
            else Error Servidor (5xx) o Timeout
                RestTemplate-->>WebhookAdapter: HttpServerErrorException/Timeout
                WebhookAdapter-->>WebhookService: NotificationDeliveryException
                WebhookService-->>NotifService: Exception
                NotifService->>Domain: markAsFailed(errorMessage)
                Domain->>Domain: status = FAILED
            end
            
            NotifService->>NotifRepo: save(notification)
            NotifRepo->>DB: INSERT/UPDATE
            DB-->>NotifRepo: Saved Entity
            NotifRepo-->>NotifService: NotificationEvent
            NotifService-->>ProcessUseCase: NotificationEvent
            ProcessUseCase-->>Consumer: NotificationEvent
            Consumer->>Kafka: Acknowledge Message
        end
    end
```

## Diagrama de Secuencia - Retry de Notificaciones Fallidas

Este diagrama muestra cómo funciona el proceso de retry con exponential backoff.

```mermaid
sequenceDiagram
    participant Scheduler as Spring Scheduler
    participant RetryService as RetryService
    participant NotifRepo as NotificationEventRepository
    participant DB as PostgreSQL
    participant Domain as NotificationEvent Domain
    participant NotifService as NotificationService
    participant WebhookService as WebhookDeliveryService
    participant ClientWebhook as Cliente Webhook URL

    Scheduler->>RetryService: retryFailedNotifications()<br/>(cada 60s)
    RetryService->>NotifRepo: findPendingRetries(maxRetries, limit)
    NotifRepo->>DB: SELECT WHERE status IN (PENDING, RETRYING, FAILED)<br/>AND retryCount < maxRetries
    DB-->>NotifRepo: List<NotificationEventEntity>
    NotifRepo-->>RetryService: List<NotificationEvent>
    
    loop Para cada notificación pendiente
        RetryService->>Domain: canRetry(maxRetries)
        Domain-->>RetryService: true
        
        RetryService->>Domain: incrementRetry(maxRetries)
        Domain->>Domain: retryCount++
        Domain->>Domain: status = RETRYING
        
        RetryService->>RetryService: calculateExponentialBackoff(retryCount)
        Note over RetryService: delay = initialDelay * multiplier^retryCount<br/>Ejemplo: 1000ms * 2^1 = 2000ms
        
        RetryService->>NotifRepo: save(notification)
        NotifRepo->>DB: UPDATE notification_events
        DB-->>NotifRepo: Updated Entity
        
        RetryService->>NotifService: process(notification)
        NotifService->>WebhookService: deliver(webhookUrl, payload)
        WebhookService->>ClientWebhook: HTTPS POST Request
        
        alt Éxito en Retry
            ClientWebhook-->>WebhookService: 200 OK
            WebhookService-->>NotifService: Success
            NotifService->>Domain: markAsSent(...)
            Domain->>Domain: status = SENT
            NotifService->>NotifRepo: save(notification)
            NotifRepo->>DB: UPDATE status = SENT
        else Falla Nuevamente
            ClientWebhook-->>WebhookService: Error
            WebhookService-->>NotifService: Exception
            NotifService->>Domain: markAsFailed(...)
            Domain->>Domain: status = FAILED
            
            RetryService->>Domain: canRetry(maxRetries)
            Domain-->>RetryService: retryCount >= maxRetries ? false : true
            
            alt Max Retries Alcanzado
                Domain->>Domain: status = FAILED (final)
                RetryService->>NotifRepo: save(notification)
                NotifRepo->>DB: UPDATE status = FAILED
            else Puede Reintentar
                Note over RetryService: Esperar próximo ciclo de scheduler<br/>con exponential backoff
            end
        end
    end
    
    RetryService-->>Scheduler: Retry process completed
```

**Código fuente del diagrama:**

```mermaid
sequenceDiagram
    participant Scheduler as Spring Scheduler
    participant RetryService as RetryService
    participant NotifRepo as NotificationEventRepository
    participant DB as PostgreSQL
    participant Domain as NotificationEvent Domain
    participant NotifService as NotificationService
    participant WebhookService as WebhookDeliveryService
    participant ClientWebhook as Cliente Webhook URL

    Scheduler->>RetryService: retryFailedNotifications()<br/>(cada 60s)
    RetryService->>NotifRepo: findPendingRetries(maxRetries, limit)
    NotifRepo->>DB: SELECT WHERE status IN (PENDING, RETRYING, FAILED)<br/>AND retryCount < maxRetries
    DB-->>NotifRepo: List<NotificationEventEntity>
    NotifRepo-->>RetryService: List<NotificationEvent>
    
    loop Para cada notificación pendiente
        RetryService->>Domain: canRetry(maxRetries)
        Domain-->>RetryService: true
        
        RetryService->>Domain: incrementRetry(maxRetries)
        Domain->>Domain: retryCount++
        Domain->>Domain: status = RETRYING
        
        RetryService->>RetryService: calculateExponentialBackoff(retryCount)
        Note over RetryService: delay = initialDelay * multiplier^retryCount<br/>Ejemplo: 1000ms * 2^1 = 2000ms
        
        RetryService->>NotifRepo: save(notification)
        NotifRepo->>DB: UPDATE notification_events
        DB-->>NotifRepo: Updated Entity
        
        RetryService->>NotifService: process(notification)
        NotifService->>WebhookService: deliver(webhookUrl, payload)
        WebhookService->>ClientWebhook: HTTPS POST Request
        
        alt Éxito en Retry
            ClientWebhook-->>WebhookService: 200 OK
            WebhookService-->>NotifService: Success
            NotifService->>Domain: markAsSent(...)
            Domain->>Domain: status = SENT
            NotifService->>NotifRepo: save(notification)
            NotifRepo->>DB: UPDATE status = SENT
        else Falla Nuevamente
            ClientWebhook-->>WebhookService: Error
            WebhookService-->>NotifService: Exception
            NotifService->>Domain: markAsFailed(...)
            Domain->>Domain: status = FAILED
            
            RetryService->>Domain: canRetry(maxRetries)
            Domain-->>RetryService: retryCount >= maxRetries ? false : true
            
            alt Max Retries Alcanzado
                Domain->>Domain: status = FAILED (final)
                RetryService->>NotifRepo: save(notification)
                NotifRepo->>DB: UPDATE status = FAILED
            else Puede Reintentar
                Note over RetryService: Esperar próximo ciclo de scheduler<br/>con exponential backoff
            end
        end
    end
    
    RetryService-->>Scheduler: Retry process completed
```

## Ciclo de Vida de una Notificación

Este diagrama muestra todos los estados posibles de una notificación y las transiciones entre ellos.

```mermaid
stateDiagram-v2
    [*] --> PENDING: NotificationEvent.create()
    
    PENDING --> SENT: Webhook exitoso<br/>markAsSent()
    PENDING --> FAILED: Webhook falla<br/>markAsFailed()
    PENDING --> RETRYING: RetryService procesa<br/>incrementRetry()
    
    RETRYING --> SENT: Retry exitoso<br/>markAsSent()
    RETRYING --> FAILED: Retry falla<br/>markAsFailed()
    RETRYING --> RETRYING: Retry falla pero<br/>retryCount < maxRetries<br/>incrementRetry()
    
    FAILED --> RETRYING: RetryService procesa<br/>canRetry() = true<br/>incrementRetry()
    FAILED --> FAILED: Max retries alcanzado<br/>retryCount >= maxRetries
    
    SENT --> [*]: Estado final
    FAILED --> [*]: Estado final<br/>(max retries)
    
    note right of PENDING
        Estado inicial cuando se crea
        la notificación desde Kafka
    end note
    
    note right of RETRYING
        Estado transitorio durante
        reintentos con exponential backoff
    end note
    
    note right of SENT
        Estado final exitoso
        Webhook entregado correctamente
    end note
    
    note right of FAILED
        Estado final de error
        Puede ser temporal (retry) o
        permanente (max retries)
    end note
```

**Código fuente del diagrama:**

```mermaid
stateDiagram-v2
    [*] --> PENDING: NotificationEvent.create()
    
    PENDING --> SENT: Webhook exitoso<br/>markAsSent()
    PENDING --> FAILED: Webhook falla<br/>markAsFailed()
    PENDING --> RETRYING: RetryService procesa<br/>incrementRetry()
    
    RETRYING --> SENT: Retry exitoso<br/>markAsSent()
    RETRYING --> FAILED: Retry falla<br/>markAsFailed()
    RETRYING --> RETRYING: Retry falla pero<br/>retryCount < maxRetries<br/>incrementRetry()
    
    FAILED --> RETRYING: RetryService procesa<br/>canRetry() = true<br/>incrementRetry()
    FAILED --> FAILED: Max retries alcanzado<br/>retryCount >= maxRetries
    
    SENT --> [*]: Estado final
    FAILED --> [*]: Estado final<br/>(max retries)
    
    note right of PENDING
        Estado inicial cuando se crea
        la notificación desde Kafka
    end note
    
    note right of RETRYING
        Estado transitorio durante
        reintentos con exponential backoff
    end note
    
    note right of SENT
        Estado final exitoso
        Webhook entregado correctamente
    end note
    
    note right of FAILED
        Estado final de error
        Puede ser temporal (retry) o
        permanente (max retries)
    end note
```

## Flujo de Replay Manual

Este diagrama muestra cómo funciona el endpoint de replay manual desde la API REST.

```mermaid
sequenceDiagram
    participant Client as Cliente REST
    participant Controller as NotificationEventController
    participant ReplayUseCase as ReplayNotificationUseCase
    participant NotifService as NotificationService
    participant Domain as NotificationEvent Domain
    participant NotifRepo as NotificationEventRepository
    participant DB as PostgreSQL
    participant WebhookService as WebhookDeliveryService
    participant ClientWebhook as Cliente Webhook URL

    Client->>Controller: POST /notification_events/{notification_event_id}/replay
    Controller->>ReplayUseCase: replay(notificationId)
    ReplayUseCase->>NotifService: replay(notificationId)
    NotifService->>NotifRepo: findById(id)
    NotifRepo->>DB: SELECT BY id
    DB-->>NotifRepo: NotificationEventEntity
    NotifRepo-->>NotifService: NotificationEvent
    
    alt Notificación No Encontrada
        NotifService-->>ReplayUseCase: NotificationNotFoundException
        ReplayUseCase-->>Controller: 404 Not Found
        Controller-->>Client: 404 Error Response
    else Notificación Encontrada
        NotifService->>Domain: Resetear estado para replay
        Domain->>Domain: status = PENDING<br/>retryCount = 0<br/>errorMessage = null
        
        NotifService->>NotifRepo: save(notification)
        NotifRepo->>DB: UPDATE notification_events
        DB-->>NotifRepo: Updated Entity
        
        NotifService->>NotifService: process(notification)
        NotifService->>WebhookService: deliver(webhookUrl, payload)
        WebhookService->>ClientWebhook: HTTPS POST Request
        
        alt Éxito
            ClientWebhook-->>WebhookService: 200 OK
            WebhookService-->>NotifService: Success
            NotifService->>Domain: markAsSent(...)
            Domain->>Domain: status = SENT
        else Falla
            ClientWebhook-->>WebhookService: Error
            WebhookService-->>NotifService: Exception
            NotifService->>Domain: markAsFailed(...)
            Domain->>Domain: status = FAILED
        end
        
        NotifService->>NotifRepo: save(notification)
        NotifRepo->>DB: UPDATE notification_events
        DB-->>NotifRepo: NotificationEvent
        NotifService-->>ReplayUseCase: NotificationEvent
        ReplayUseCase-->>Controller: NotificationEvent
        Controller->>Controller: Convertir a DTO
        Controller-->>Client: 202 Accepted<br/>NotificationEventResponse
    end
```

**Código fuente del diagrama:**

```mermaid
sequenceDiagram
    participant Client as Cliente REST
    participant Controller as NotificationEventController
    participant ReplayUseCase as ReplayNotificationUseCase
    participant NotifService as NotificationService
    participant Domain as NotificationEvent Domain
    participant NotifRepo as NotificationEventRepository
    participant DB as PostgreSQL
    participant WebhookService as WebhookDeliveryService
    participant ClientWebhook as Cliente Webhook URL

    Client->>Controller: POST /notification_events/{notification_event_id}/replay
    Controller->>ReplayUseCase: replay(notificationId)
    ReplayUseCase->>NotifService: replay(notificationId)
    NotifService->>NotifRepo: findById(id)
    NotifRepo->>DB: SELECT BY id
    DB-->>NotifRepo: NotificationEventEntity
    NotifRepo-->>NotifService: NotificationEvent
    
    alt Notificación No Encontrada
        NotifService-->>ReplayUseCase: NotificationNotFoundException
        ReplayUseCase-->>Controller: 404 Not Found
        Controller-->>Client: 404 Error Response
    else Notificación Encontrada
        NotifService->>Domain: Resetear estado para replay
        Domain->>Domain: status = PENDING<br/>retryCount = 0<br/>errorMessage = null
        
        NotifService->>NotifRepo: save(notification)
        NotifRepo->>DB: UPDATE notification_events
        DB-->>NotifRepo: Updated Entity
        
        NotifService->>NotifService: process(notification)
        NotifService->>WebhookService: deliver(webhookUrl, payload)
        WebhookService->>ClientWebhook: HTTPS POST Request
        
        alt Éxito
            ClientWebhook-->>WebhookService: 200 OK
            WebhookService-->>NotifService: Success
            NotifService->>Domain: markAsSent(...)
            Domain->>Domain: status = SENT
        else Falla
            ClientWebhook-->>WebhookService: Error
            WebhookService-->>NotifService: Exception
            NotifService->>Domain: markAsFailed(...)
            Domain->>Domain: status = FAILED
        end
        
        NotifService->>NotifRepo: save(notification)
        NotifRepo->>DB: UPDATE notification_events
        DB-->>NotifRepo: NotificationEvent
        NotifService-->>ReplayUseCase: NotificationEvent
        ReplayUseCase-->>Controller: NotificationEvent
        Controller->>Controller: Convertir a DTO
        Controller-->>Client: 202 Accepted<br/>NotificationEventResponse
    end
```

