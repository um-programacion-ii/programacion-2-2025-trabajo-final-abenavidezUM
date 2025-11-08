#!/bin/bash

# Script para limpiar el entorno de desarrollo
# Detiene servicios y elimina volúmenes (datos)

set -e

echo "========================================="
echo "  Limpieza completa del entorno"
echo "========================================="
echo ""
echo "ADVERTENCIA: Esto eliminará todos los datos de:"
echo "  - Base de datos PostgreSQL"
echo "  - Datos de Redis"
echo ""
read -p "¿Estás seguro? (s/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "Operación cancelada"
    exit 0
fi

# Verificar que docker-compose.yml exista
if [ ! -f "docker-compose.yml" ]; then
    echo "Error: docker-compose.yml no encontrado"
    echo "Asegúrate de estar en el directorio raíz del proyecto"
    exit 1
fi

echo ""
echo "Deteniendo servicios y eliminando volúmenes..."
docker-compose down -v

echo ""
echo "Eliminando imágenes huérfanas..."
docker image prune -f

echo ""
echo "========================================="
echo "  Limpieza completada"
echo "========================================="
echo ""
echo "Todos los datos han sido eliminados."
echo "Para iniciar desde cero: ./scripts/start-dev.sh"
echo ""

