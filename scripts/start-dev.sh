#!/bin/bash

# Script para iniciar el entorno de desarrollo
# Levanta todos los servicios necesarios con Docker Compose

set -e

echo "========================================="
echo "  Iniciando entorno de desarrollo"
echo "========================================="
echo ""

# Verificar que Docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker no está corriendo"
    echo "Por favor inicia Docker Desktop y vuelve a intentar"
    exit 1
fi

# Verificar que docker-compose.yml exista
if [ ! -f "docker-compose.yml" ]; then
    echo "Error: docker-compose.yml no encontrado"
    echo "Asegúrate de estar en el directorio raíz del proyecto"
    exit 1
fi

echo "Levantando servicios..."
docker-compose up -d

echo ""
echo "Esperando a que los servicios estén listos..."
sleep 5

echo ""
echo "Estado de los servicios:"
docker-compose ps

echo ""
echo "========================================="
echo "  Entorno de desarrollo iniciado"
echo "========================================="
echo ""
echo "Servicios disponibles:"
echo "  - PostgreSQL:          localhost:5432"
echo "  - Redis Local:         localhost:6379"
echo "  - Redis Cátedra Mock:  localhost:6380"
echo "  - Kafka:               localhost:9092"
echo "  - Zookeeper:           localhost:2181"
echo ""
echo "Para ver los logs: ./scripts/logs.sh"
echo "Para detener:      ./scripts/stop-dev.sh"
echo ""

