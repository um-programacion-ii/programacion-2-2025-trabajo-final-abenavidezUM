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

## 📝 Próximas Funcionalidades

- [ ] Endpoints de consulta de eventos (ISSUE-027)
- [ ] Endpoints de bloqueo de asientos (ISSUE-028)
- [ ] Endpoints de ventas (ISSUE-029)
- [ ] Manejo de errores y reintentos (ISSUE-030)

## 🧪 Testing

```bash
mvn test
```

## 📖 Estado Actual

✅ Proyecto inicializado (ISSUE-026)
- Estructura básica creada
- RestTemplate configurado con autenticación
- Health checks implementados
- Logging configurado
