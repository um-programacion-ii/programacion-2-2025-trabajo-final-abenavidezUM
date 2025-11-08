# Scripts de Utilidades

Este directorio contiene scripts para facilitar el desarrollo y gestión del proyecto.

## Requisitos

- Bash (disponible en Linux, macOS y WSL en Windows)
- Docker Desktop instalado y corriendo

## Scripts Disponibles

### start-dev.sh

Inicia el entorno de desarrollo levantando todos los servicios necesarios con Docker Compose.

**Uso:**
```bash
./scripts/start-dev.sh
```

**Qué hace:**
- Verifica que Docker esté corriendo
- Levanta todos los contenedores definidos en docker-compose.yml
- Muestra el estado de los servicios
- Lista los puertos disponibles

### stop-dev.sh

Detiene todos los servicios del entorno de desarrollo.

**Uso:**
```bash
./scripts/stop-dev.sh
```

**Qué hace:**
- Detiene todos los contenedores
- Mantiene los volúmenes (los datos persisten)

### clean.sh

Limpieza completa del entorno, eliminando contenedores y volúmenes.

**Uso:**
```bash
./scripts/clean.sh
```

**Qué hace:**
- Solicita confirmación antes de proceder
- Detiene todos los contenedores
- Elimina volúmenes (base de datos y redis)
- Limpia imágenes huérfanas

**Advertencia:** Esta operación eliminará todos los datos. Úsala solo cuando quieras empezar desde cero.

### logs.sh

Muestra los logs de los servicios en tiempo real.

**Uso:**
```bash
# Ver logs de todos los servicios
./scripts/logs.sh

# Ver logs de un servicio específico
./scripts/logs.sh postgres
./scripts/logs.sh kafka
./scripts/logs.sh redis_local
```

**Qué hace:**
- Muestra logs en tiempo real (con seguimiento)
- Presiona Ctrl+C para salir

## Permisos de Ejecución

Los scripts deben tener permisos de ejecución. Si al ejecutarlos obtienes un error de permisos, ejecuta:

```bash
chmod +x scripts/*.sh
```

## Flujo de Trabajo Típico

1. **Iniciar el entorno:**
   ```bash
   ./scripts/start-dev.sh
   ```

2. **Desarrollar tu código...**

3. **Ver logs si es necesario:**
   ```bash
   ./scripts/logs.sh
   ```

4. **Detener al terminar:**
   ```bash
   ./scripts/stop-dev.sh
   ```

5. **Limpiar si necesitas empezar de cero:**
   ```bash
   ./scripts/clean.sh
   ```

## Servicios y Puertos

Después de ejecutar `start-dev.sh`, tendrás disponibles:

| Servicio | Puerto | Propósito |
|----------|--------|-----------|
| PostgreSQL | 5432 | Base de datos principal |
| Redis Local | 6379 | Cache y sesiones del backend |
| Redis Cátedra Mock | 6380 | Simulación del Redis de cátedra |
| Kafka | 9092 | Mensajería de eventos |
| Zookeeper | 2181 | Coordinación de Kafka |

## Solución de Problemas

### Error: "Docker no está corriendo"
- Inicia Docker Desktop y espera a que esté completamente activo

### Error: "docker-compose.yml no encontrado"
- Asegúrate de estar en el directorio raíz del proyecto
- Ejecuta `pwd` para verificar tu ubicación

### Los servicios no inician correctamente
- Verifica los logs: `./scripts/logs.sh`
- Intenta limpiar y reiniciar: `./scripts/clean.sh` y luego `./scripts/start-dev.sh`

### Puerto ya en uso
- Verifica que no tengas otros servicios corriendo en los mismos puertos
- En Mac/Linux: `lsof -i :5432` (reemplaza 5432 con el puerto en cuestión)
- Detén el servicio conflictivo o cambia el puerto en docker-compose.yml

