# Proxy

Servicio proxy que actúa como intermediario entre el backend y los servicios de la cátedra (Kafka y Redis).

## Responsabilidades

- Consumir notificaciones de Kafka
- Consultar estado de asientos en Redis de cátedra
- Notificar cambios al backend

## Tecnologías

- Java 17
- Spring Boot
- Spring Kafka
- Redis Client

## Estado

En desarrollo.
