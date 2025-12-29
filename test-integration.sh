#!/bin/bash

# Script de Verificación de Integración
# Sistema de Gestión de Eventos

echo "🧪 INICIANDO PRUEBAS DE INTEGRACIÓN"
echo "===================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Contadores
PASSED=0
FAILED=0

# Función para imprimir resultado
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC} - $2"
        ((PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC} - $2"
        ((FAILED++))
    fi
}

# Función para imprimir warning
print_warning() {
    echo -e "${YELLOW}⚠️  WARN${NC} - $1"
}

echo "1️⃣  VERIFICANDO SERVICIOS BASE"
echo "================================"

# PostgreSQL
echo -n "Verificando PostgreSQL... "
if nc -z localhost 5432 2>/dev/null; then
    print_result 0 "PostgreSQL activo en puerto 5432"
else
    print_result 1 "PostgreSQL no responde en puerto 5432"
fi

# Redis
echo -n "Verificando Redis... "
if nc -z localhost 6379 2>/dev/null; then
    print_result 0 "Redis activo en puerto 6379"
else
    print_result 1 "Redis no responde en puerto 6379"
fi

echo ""
echo "2️⃣  VERIFICANDO BACKEND"
echo "======================="

# Backend Health Check
echo -n "Verificando Backend Health... "
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/actuator/health 2>/dev/null)
if [ "$RESPONSE" = "200" ]; then
    print_result 0 "Backend health check OK"
else
    print_result 1 "Backend no responde (HTTP $RESPONSE)"
fi

# Eventos Públicos
echo -n "Verificando endpoint de eventos públicos... "
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/eventos/public 2>/dev/null)
if [ "$RESPONSE" = "200" ]; then
    print_result 0 "Endpoint /api/eventos/public OK"
else
    print_result 1 "Endpoint /api/eventos/public falló (HTTP $RESPONSE)"
fi

# Notificaciones Health
echo -n "Verificando endpoint de notificaciones... "
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/admin/notificaciones/health 2>/dev/null)
if [ "$RESPONSE" = "200" ]; then
    print_result 0 "Endpoint de notificaciones OK"
else
    print_result 1 "Endpoint de notificaciones falló (HTTP $RESPONSE)"
fi

echo ""
echo "3️⃣  VERIFICANDO PROXY"
echo "====================="

# Proxy Health Check
echo -n "Verificando Proxy Health... "
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/proxy/api/health 2>/dev/null)
if [ "$RESPONSE" = "200" ]; then
    print_result 0 "Proxy health check OK"
    
    # Si el proxy responde, verificar estado
    HEALTH_DATA=$(curl -s http://localhost:8082/proxy/api/health 2>/dev/null)
    
    if echo "$HEALTH_DATA" | grep -q '"status":"UP"'; then
        echo -e "  ${GREEN}➜${NC} Proxy completamente funcional"
    elif echo "$HEALTH_DATA" | grep -q '"status":"DEGRADED"'; then
        print_warning "Proxy en modo DEGRADED (Redis/Kafka de cátedra no disponible)"
    fi
    
else
    print_result 1 "Proxy no responde (HTTP $RESPONSE)"
fi

echo ""
echo "4️⃣  VERIFICANDO INTEGRACIÓN BACKEND-PROXY"
echo "=========================================="

# Test de notificación simulada
echo -n "Probando notificación de evento... "
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/admin/notificaciones/eventos/evento \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "NUEVO_EVENTO",
    "eventoId": 999,
    "nombre": "Evento Test",
    "fecha": "2025-12-31T20:00:00",
    "descripcion": "Test",
    "timestamp": "2025-12-17T20:00:00"
  }' 2>/dev/null)

if [ "$RESPONSE" = "200" ]; then
    print_result 0 "Backend recibe notificaciones correctamente"
else
    print_result 1 "Backend no procesa notificaciones (HTTP $RESPONSE)"
fi

# Test de notificación de asiento
echo -n "Probando notificación de asiento... "
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/admin/notificaciones/eventos/asiento \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "ASIENTO_VENDIDO",
    "eventoId": 1,
    "fila": 5,
    "columna": 5,
    "nuevoEstado": "VENDIDO",
    "timestamp": "2025-12-17T20:00:00"
  }' 2>/dev/null)

if [ "$RESPONSE" = "200" ]; then
    print_result 0 "Notificaciones de asiento funcionan"
else
    print_result 1 "Notificaciones de asiento fallan (HTTP $RESPONSE)"
fi

echo ""
echo "5️⃣  VERIFICANDO CONEXIÓN A CÁTEDRA (OPCIONAL)"
echo "=============================================="

# Verificar ZeroTier
if command -v zerotier-cli &> /dev/null; then
    echo -n "Verificando ZeroTier... "
    ZT_STATUS=$(zerotier-cli status 2>/dev/null | grep -c "ONLINE")
    if [ "$ZT_STATUS" -eq 1 ]; then
        print_result 0 "ZeroTier ONLINE"
    else
        print_warning "ZeroTier no está ONLINE"
    fi
else
    print_warning "ZeroTier CLI no instalado"
fi

# Ping a servidor de cátedra
echo -n "Verificando acceso a servidor de cátedra... "
if ping -c 1 -W 2 192.168.194.250 &>/dev/null; then
    print_result 0 "Servidor de cátedra accesible"
else
    print_warning "Servidor de cátedra no responde a ping (puede ser normal)"
fi

# HTTP a cátedra
echo -n "Verificando API de cátedra... "
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 http://192.168.194.250:8080/actuator/health 2>/dev/null)
if [ "$RESPONSE" = "200" ] || [ "$RESPONSE" = "401" ]; then
    print_result 0 "API de cátedra responde (HTTP $RESPONSE)"
else
    print_warning "API de cátedra no disponible (aplicación funcionará en modo local)"
fi

echo ""
echo "📊 RESUMEN DE PRUEBAS"
echo "====================="
echo -e "${GREEN}Pasadas: $PASSED${NC}"
echo -e "${RED}Fallidas: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 ¡TODAS LAS PRUEBAS PASARON!${NC}"
    echo "La aplicación está lista para usar."
    exit 0
elif [ $FAILED -le 3 ]; then
    echo -e "${YELLOW}⚠️  ALGUNAS PRUEBAS FALLARON${NC}"
    echo "La aplicación puede funcionar con funcionalidad limitada."
    exit 1
else
    echo -e "${RED}❌ MÚLTIPLES PRUEBAS FALLARON${NC}"
    echo "Verifica que los servicios estén corriendo correctamente."
    exit 2
fi


