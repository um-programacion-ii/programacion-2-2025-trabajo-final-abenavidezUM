# Servicio Proxy

Servicio intermediario entre el backend y la API del servidor de cátedra.

## 🎯 Responsabilidades

- **Consumir API de cátedra**: Consultar eventos, bloquear asientos, realizar ventas
- **Autenticación automática**: Manejo del token JWT con la API de cátedra
- **Logging y monitoreo**: Registro de todas las interacciones con servicios externos
- **Health checks**: Verificación de conectividad con API de cátedra

## 🛠️ Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Web**: Para RestTemplate y controllers
- **Spring Actuator**: Health checks y métricas
- **Lombok**: Reducción de boilerplate
- **Maven**: Gestión de dependencias

## 📋 Configuración

### Variables de Entorno

```bash
CATEDRA_API_URL=http://192.168.194.250:8080
CATEDRA_API_TOKEN=<jwt-token-de-catedra>
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

Verifica el estado del servicio y la conectividad con la API de cátedra.

**Response**:
```json
{
  "status": "UP",
  "service": "proxy-service",
  "catedra_api": "UP"
}
```

### Consulta de Eventos

#### Listar Eventos Resumidos
```
GET /proxy/api/eventos/resumidos
```

#### Listar Eventos Completos
```
GET /proxy/api/eventos
```

#### Obtener Evento por ID
```
GET /proxy/api/eventos/{id}
```

### Bloqueo de Asientos

#### Bloquear Asientos
```
POST /proxy/api/asientos/bloquear
Content-Type: application/json

{
  "eventoId": 1,
  "asientos": [
    {"fila": 2, "columna": 1},
    {"fila": 2, "columna": 2}
  ]
}
```

### Ventas

#### Realizar Venta
```
POST /proxy/api/ventas/realizar
Content-Type: application/json

{
  "eventoId": 1,
  "fecha": "2025-08-17T20:00:00.000Z",
  "precioVenta": 1400.10,
  "asientos": [
    {
      "fila": 2,
      "columna": 3,
      "persona": "Fernando Galvez"
    }
  ]
}
```

#### Listar Ventas
```
GET /proxy/api/ventas
```

#### Obtener Venta por ID
```
GET /proxy/api/ventas/{id}
```

## 📊 Arquitectura

```
Backend → Proxy Service → API Cátedra
                         (http://192.168.194.250:8080)
```

El proxy:
1. Recibe requests del backend
2. Agrega autenticación JWT automáticamente
3. Hace el request a la API de cátedra
4. Devuelve la respuesta al backend

## ⚠️ Manejo de Errores

El proxy implementa un sistema robusto de manejo de errores:

### Estructura de Respuesta de Error
```json
{
  "timestamp": "2025-12-17T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "No se pudo conectar con el servicio de cátedra",
  "path": "/proxy/api/eventos/1"
}
```

### Códigos de Error

| Código | Descripción |
|--------|-------------|
| **404** | Recurso no encontrado (evento/venta no existe) |
| **503** | Servicio de cátedra no disponible |
| **500** | Error interno del proxy |

### Excepciones Personalizadas

- **`CatedraServiceException`**: Error al comunicarse con la API de cátedra
- **`ResourceNotFoundException`**: Recurso no encontrado (404)

### GlobalExceptionHandler

El `@RestControllerAdvice` intercepta todas las excepciones y devuelve respuestas JSON consistentes:

- `HttpClientErrorException` → 4xx según código original
- `HttpServerErrorException` → 503 Service Unavailable
- `ResourceAccessException` → 503 Service Unavailable (timeout/conexión)
- `CatedraServiceException` → 503 Service Unavailable
- `ResourceNotFoundException` → 404 Not Found
- `Exception` → 500 Internal Server Error

## 📝 Funcionalidades Implementadas

- ✅ Endpoints de consulta de eventos (ISSUE-027)
- ✅ Endpoints de bloqueo de asientos (ISSUE-028)
- ✅ Endpoints de ventas (ISSUE-029)
- ✅ Manejo de errores global (ISSUE-030)

## 🧪 Testing

```bash
mvn test
```

## 📖 Estado Actual

✅ **Proyecto completo - Fase 7 finalizada**

- ✅ Proyecto inicializado (ISSUE-026)
  - Estructura básica creada
  - RestTemplate configurado con autenticación
  - Health checks implementados
  - Logging configurado

- ✅ Endpoints de eventos (ISSUE-027)
  - Listar eventos resumidos
  - Listar eventos completos
  - Obtener evento por ID

- ✅ Endpoints de asientos (ISSUE-028)
  - Bloquear asientos para un evento

- ✅ Endpoints de ventas (ISSUE-029)
  - Realizar venta
  - Listar ventas
  - Obtener venta por ID

- ✅ Manejo de errores (ISSUE-030)
  - GlobalExceptionHandler implementado
  - Excepciones personalizadas
  - Respuestas de error consistentes
  - Logging mejorado
