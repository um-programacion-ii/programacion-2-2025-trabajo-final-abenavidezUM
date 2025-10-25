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

## Instalación

Instrucciones de instalación y configuración serán agregadas a medida que avance el desarrollo.

## Autor

Nombre: Agustin Benavidez
Legajo: 62344

## Licencia

Proyecto académico para Programación II - 2025
