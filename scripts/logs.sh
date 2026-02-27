#!/bin/bash

# Script para ver logs de los servicios
# Muestra logs en tiempo real de Docker Compose

set -e

# Verificar que docker-compose.yml exista
if [ ! -f "docker-compose.yml" ]; then
    echo "Error: docker-compose.yml no encontrado"
    echo "Asegúrate de estar en el directorio raíz del proyecto"
    exit 1
fi

echo "========================================="
echo "  Logs de servicios"
echo "========================================="
echo ""
echo "Mostrando logs en tiempo real..."
echo "Presiona Ctrl+C para salir"
echo ""

# Si se pasa un argumento, mostrar logs de ese servicio específico
if [ -n "$1" ]; then
    echo "Servicio: $1"
    echo ""
    docker-compose logs -f "$1"
else
    echo "Todos los servicios"
    echo ""
    docker-compose logs -f
fi

