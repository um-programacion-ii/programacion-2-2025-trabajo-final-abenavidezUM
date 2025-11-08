#!/bin/bash

# Script para detener el entorno de desarrollo
# Detiene todos los servicios de Docker Compose

set -e

echo "========================================="
echo "  Deteniendo entorno de desarrollo"
echo "========================================="
echo ""

# Verificar que docker-compose.yml exista
if [ ! -f "docker-compose.yml" ]; then
    echo "Error: docker-compose.yml no encontrado"
    echo "Asegúrate de estar en el directorio raíz del proyecto"
    exit 1
fi

echo "Deteniendo servicios..."
docker-compose down

echo ""
echo "========================================="
echo "  Servicios detenidos correctamente"
echo "========================================="
echo ""
echo "Los datos en volúmenes persisten."
echo "Para eliminar los volúmenes: ./scripts/clean.sh"
echo ""

