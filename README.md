# Sistema de Gestión de Eventos

Sistema integral para la gestión de asistencia a eventos únicos tales como charlas, cursos, obras de teatro, etc.

**Trabajo Final - Programación II - 2025**

## Descripción

Sistema que permite a los usuarios:
- Consultar eventos disponibles
- Visualizar mapas de asientos en tiempo real
- Seleccionar y bloquear asientos temporalmente
- Registrar datos de los asistentes
- Realizar compras de entradas
- Consultar historial de compras

## Arquitectura

El proyecto está dividido en cuatro componentes principales:

1. **Backend** - Java Spring Boot: API REST principal con gestión de eventos, usuarios y ventas
2. **Proxy** - Servicio intermediario para integración con Kafka y Redis de cátedra
3. **Mobile** - Aplicación móvil multiplataforma para usuarios finales
4. **Servicio Cátedra** - Provisto por la cátedra (API REST, Kafka, Redis)

## Estructura del Proyecto

```
.
├── backend/              # Backend Spring Boot
├── proxy/                # Servicio Proxy
├── mobile/               # Cliente Móvil
├── docs/                 # Documentación técnica
├── scripts/              # Scripts de utilidades
├── docker-compose.yml    # Configuración de servicios
└── README.md
```

## Tecnologías

- **Backend**: Java 17, Spring Boot, PostgreSQL, Redis, Kafka
- **Proxy**: Java 17, Spring Boot
- **Mobile**: Por definir (React Native / Flutter / Kotlin Multiplatform)
- **Infraestructura**: Docker, Docker Compose

## Requisitos

- Java JDK 17 o superior
- Docker Desktop
- Git

## Estado del Proyecto

Proyecto en fase inicial de desarrollo.

## Instalación y Uso

### Iniciar el entorno de desarrollo

1. Asegúrate de tener Docker Desktop instalado y corriendo

2. Inicia todos los servicios:
```bash
./scripts/start-dev.sh
```

Esto levantará:
- PostgreSQL (puerto 5432)
- Redis Local (puerto 6379)
- Redis Cátedra Mock (puerto 6380)
- Kafka (puerto 9092)
- Zookeeper (puerto 2181)

### Ver logs

Para ver los logs de todos los servicios:
```bash
./scripts/logs.sh
```

Para ver logs de un servicio específico:
```bash
./scripts/logs.sh postgres
```

### Detener servicios

```bash
./scripts/stop-dev.sh
```

### Limpiar todo (eliminar datos)

```bash
./scripts/clean.sh
```

Para más información sobre los scripts, consulta [scripts/README.md](scripts/README.md)

## Autor

Nombre: Agustin Benavidez

Legajo: 62344

## Licencia

Proyecto académico para Programación II - 2025

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/IEOUmR9z)
