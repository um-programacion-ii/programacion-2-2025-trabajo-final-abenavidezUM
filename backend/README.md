# Backend

Backend del sistema desarrollado en Java Spring Boot.

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Spring Security (JWT)
- Spring Data JPA
- Spring Kafka
- PostgreSQL 15
- Redis 7
- Maven 3.9.5

## Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/eventos/backend/
│   │   │   ├── config/          # Configuraciones
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── domain/          # Entidades y enums
│   │   │   ├── dto/             # DTOs (request/response)
│   │   │   ├── exception/       # Manejo de excepciones
│   │   │   ├── kafka/           # Consumers de Kafka
│   │   │   ├── repository/      # Repositorios JPA
│   │   │   ├── security/        # Configuración de seguridad
│   │   │   ├── service/         # Lógica de negocio
│   │   │   └── BackendApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── logback-spring.xml
│   └── test/
├── .mvn/
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Requisitos

- Java JDK 17 o superior
- Docker Desktop (para servicios auxiliares)

## Compilar

```bash
./mvnw clean install
```

## Ejecutar

```bash
# Con perfil de desarrollo (por defecto)
./mvnw spring-boot:run

# O especificando el perfil
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación estará disponible en `http://localhost:8080/api`

## Ejecutar Tests

```bash
./mvnw test
```

## Perfiles

- **dev**: Desarrollo local con logs verbosos
- **prod**: Producción con configuración desde variables de entorno
- **test**: Tests con base de datos H2 en memoria

## Endpoints de Actuator

- Health: `http://localhost:8080/api/actuator/health`
- Info: `http://localhost:8080/api/actuator/info`
- Metrics: `http://localhost:8080/api/actuator/metrics`

## Estado

Proyecto inicializado correctamente.
