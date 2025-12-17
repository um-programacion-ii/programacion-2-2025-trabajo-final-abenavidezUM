# Servicio Proxy

Servicio intermediario entre el backend y la infraestructura de cátedra (Redis y Kafka).

## 🎯 Responsabilidades

El proxy es la **capa de abstracción** que conecta nuestro backend con la infraestructura del servidor de cátedra:

- **Consultar Redis de cátedra**: Accede al estado de asientos en tiempo real
- **Consumir Kafka de cátedra**: Recibe notificaciones de cambios en eventos/asientos
- **Notificar al backend**: Reenvía las notificaciones de cátedra al backend
- **Exponer API REST**: Permite al backend consultar estado de asientos sin conectarse directamente a Redis
- **Logging y monitoreo**: Registro completo de mensajes Kafka y consultas Redis
- **Health checks**: Verificación de conectividad con Redis de cátedra y backend

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Web**: Para controllers REST
- **Spring Data Redis**: Cliente de Redis para cátedra
- **Spring Kafka**: Consumer de Kafka para cátedra
- **Spring WebFlux**: WebClient para notificar al backend
- **Spring Actuator**: Health checks y métricas
- **Lombok**: Reducción de boilerplate
- **Jackson**: Serialización JSON de mensajes Kafka
- **Maven**: Gestión de dependencias

## 📋 Configuración

### Variables de Entorno

```bash
# Redis de cátedra
CATEDRA_REDIS_HOST=192.168.194.250
CATEDRA_REDIS_PORT=6379
CATEDRA_REDIS_PASSWORD=
CATEDRA_REDIS_DATABASE=0

# Kafka de cátedra
CATEDRA_KAFKA_BOOTSTRAP=192.168.194.250:9092
CATEDRA_KAFKA_GROUP_ID=proxy-service-group
CATEDRA_KAFKA_TOPIC=eventos-cambios

# Backend (para notificaciones)
BACKEND_URL=http://localhost:8080
BACKEND_NOTIFICATION_ENDPOINT=/api/admin/notificaciones/eventos
```

### Perfiles

- **dev**: Desarrollo local, logging DEBUG
- **prod**: Producción, logging WARN

## 🚀 Ejecución

### Desarrollo
```bash
mvn spring-boot:run
```

### Producción
```bash
mvn clean package
java -jar target/proxy-1.0.0.jar --spring.profiles.active=prod
```

## 🔍 Endpoints

### Health Check
```
GET /proxy/api/health
```

Verifica el estado del servicio y sus conexiones.

**Response**:
```json
{
  "service": "proxy-service",
  "status": "UP",
  "catedra_redis": "UP",
  "backend": "UP"
}
```

### Consulta de Estado de Asientos

#### Obtener Estado de un Asiento Específico
```
GET /proxy/api/asientos/estado/{eventoId}/{fila}/{columna}
```

**Response**:
```json
{
  "eventoId": 1,
  "fila": 2,
  "columna": 3,
  "estado": "LIBRE",
  "timestamp": "2025-12-17T15:30:00"
}
```

**Estados posibles**: `LIBRE`, `BLOQUEADO`, `VENDIDO`, `OCUPADO`

---

#### Obtener Mapa Completo de Asientos
```
GET /proxy/api/asientos/mapa/{eventoId}
```

**Response**:
```json
{
  "eventoId": 1,
  "asientos": {
    "1:1": "VENDIDO",
    "1:2": "VENDIDO",
    "2:1": "BLOQUEADO",
    "2:2": "LIBRE"
  },
  "resumen": {
    "LIBRE": 50,
    "BLOQUEADO": 10,
    "VENDIDO": 30,
    "OCUPADO": 5
  },
  "totalAsientos": 95,
  "timestamp": "2025-12-17T15:30:00"
}
```

---

#### Obtener Resumen de Asientos por Estado
```
GET /proxy/api/asientos/resumen/{eventoId}
```

**Response**:
```json
{
  "eventoId": 1,
  "resumen": {
    "LIBRE": 50,
    "BLOQUEADO": 10,
    "VENDIDO": 30
  },
  "timestamp": "2025-12-17T15:30:00"
}
```

## 📊 Arquitectura

```
┌─────────────────────────────────────────────────────┐
│            SERVIDOR CÁTEDRA                         │
│                                                      │
│  ┌──────────┐    ┌────────────┐                    │
│  │  Redis   │    │   Kafka    │                    │
│  │ (estado) │    │ (eventos)  │                    │
│  └──────────┘    └────────────┘                    │
│       │                │                            │
└───────┼────────────────┼────────────────────────────┘
        │                │
        │ Consultas      │ Notificaciones
        │                │
        ▼                ▼
┌─────────────────────────────────────────────────────┐
│             PROXY SERVICE (Puerto 8082)             │
│                                                      │
│  • CatedraRedisService: Consulta Redis             │
│  • CatedraKafkaListener: Consume Kafka             │
│  • BackendNotificationService: Notifica cambios    │
│  • AsientoEstadoController: API REST               │
│                                                      │
└─────────────────────────────────────────────────────┘
        │                │
        │ API REST       │ Webhooks
        │                │
        ▼                ▼
┌─────────────────────────────────────────────────────┐
│             BACKEND (Puerto 8080)                   │
│                                                      │
│  • Consulta estado de asientos vía proxy           │
│  • Recibe notificaciones de cambios                │
│  • Procesa lógica de negocio                       │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### Flujo de Datos

**1. Consulta de Estado de Asientos**:
```
Backend → GET /proxy/api/asientos/mapa/{id} → Proxy
                                               ↓
                                         Redis Cátedra
                                               ↓
Backend ← JSON con estado de asientos ← Proxy
```

**2. Notificaciones de Cambios**:
```
Kafka Cátedra → Mensaje de cambio → Proxy (Consumer)
                                      ↓
                              (Procesa mensaje)
                                      ↓
                         POST /api/admin/notificaciones/eventos
                                      ↓
                                   Backend
```

## 🔄 Componentes Principales

### 1. CatedraRedisService
Cliente de Redis que consulta el estado de asientos en tiempo real.

**Métodos principales**:
- `getEstadoAsiento(eventoId, fila, columna)`: Estado de un asiento específico
- `getEstadoAsientosEvento(eventoId)`: Mapa completo de asientos
- `contarAsientosPorEstado(eventoId)`: Resumen por estado
- `isRedisAvailable()`: Health check

### 2. CatedraKafkaListener
Consumer de Kafka que escucha notificaciones de cátedra.

**Mensajes que procesa**:
- `NUEVO_EVENTO`: Se creó un evento
- `EVENTO_ACTUALIZADO`: Se modificó un evento
- `EVENTO_CANCELADO`: Se canceló un evento
- `ASIENTO_BLOQUEADO`: Se bloqueó un asiento
- `ASIENTO_VENDIDO`: Se vendió un asiento
- `ASIENTO_LIBERADO`: Se liberó un asiento

### 3. BackendNotificationService
Servicio que reenvía notificaciones al backend usando WebClient.

**Endpoints del backend que llama**:
- `POST /api/admin/notificaciones/eventos/evento`
- `POST /api/admin/notificaciones/eventos/asiento`

### 4. AsientoEstadoController
API REST para que el backend consulte estado de asientos.

## 📝 Funcionalidades Implementadas

- ✅ Cliente Redis de cátedra (ISSUE-027)
- ✅ Kafka consumer de cátedra (ISSUE-028)
- ✅ Sistema de notificaciones al backend (ISSUE-029)
- ✅ API REST para consultas de estado (ISSUE-030)

## 🧪 Testing

```bash
mvn test
```

## 📖 Estado Actual

✅ **Proyecto completo - Fase 7 finalizada (CORREGIDO)**

- ✅ Proyecto inicializado (ISSUE-026)
  - Estructura básica creada
  - Dependencias de Redis y Kafka agregadas
  - Health checks implementados
  - Logging configurado

- ✅ Cliente Redis de cátedra (ISSUE-027)
  - Consulta estado de asiento específico
  - Obtiene mapa completo de asientos
  - Cuenta asientos por estado
  - Health check de Redis

- ✅ Kafka consumer de cátedra (ISSUE-028)
  - Escucha notificaciones de eventos
  - Escucha notificaciones de asientos
  - Procesamiento con ACK manual
  - Manejo de errores y reintentos

- ✅ Sistema de notificaciones (ISSUE-029)
  - WebClient para HTTP asíncrono
  - Notifica cambios de eventos al backend
  - Notifica cambios de asientos al backend
  - Health check del backend

- ✅ API REST para consultas (ISSUE-030)
  - Endpoint para estado de asiento individual
  - Endpoint para mapa de asientos
  - Endpoint para resumen por estado
  - Health check completo
