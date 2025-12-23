#!/bin/bash
# Script para renovar el token JWT de Cátedra
# Uso: ./renovar-token.sh
# Lee automáticamente las credenciales del .env

echo "========================================="
echo "  RENOVAR TOKEN JWT DE CÁTEDRA"
echo "========================================="
echo ""

# Leer credenciales del .env
if [ ! -f ".env" ]; then
    echo "❌ ERROR: Archivo .env no encontrado"
    exit 1
fi

# Cargar variables del .env
export $(grep -v '^#' .env | grep -v '^$' | xargs)

if [ -z "$CATEDRA_USERNAME" ] || [ -z "$CATEDRA_PASSWORD" ]; then
    echo "❌ ERROR: CATEDRA_USERNAME o CATEDRA_PASSWORD no están en .env"
    exit 1
fi

USERNAME="$CATEDRA_USERNAME"
PASSWORD="$CATEDRA_PASSWORD"
SERVER_URL="http://192.168.194.250:8080"

echo "Usuario: $USERNAME"
echo "Servidor: $SERVER_URL"
echo ""

echo "Solicitando nuevo token..."
echo ""

# Llamar a /api/authenticate (endpoint más confiable)
RESPONSE=$(curl -s -X POST "${SERVER_URL}/api/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"${USERNAME}\", \"password\": \"${PASSWORD}\", \"rememberMe\": false}")

# Extraer token (puede venir como 'token' o 'id_token')
TOKEN=$(echo "$RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('token', data.get('id_token', '')))" 2>/dev/null)

if [ -z "$TOKEN" ] || [ "$TOKEN" == "" ]; then
    echo "❌ ERROR: No se pudo obtener el token"
    echo "Respuesta del servidor:"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    exit 1
fi

echo "✅ Token obtenido exitosamente!"
echo ""
echo "Token (primeros 50 caracteres): ${TOKEN:0:50}..."
echo ""

# Actualizar .env
ENV_FILE=".env"
if [ ! -f "$ENV_FILE" ]; then
    echo "⚠️  Archivo .env no encontrado, creando uno nuevo..."
    touch "$ENV_FILE"
fi

# Backup del .env
cp "$ENV_FILE" "${ENV_FILE}.backup"

# Actualizar o agregar CATEDRA_API_TOKEN
if grep -q "^CATEDRA_API_TOKEN=" "$ENV_FILE"; then
    # Reemplazar existente
    sed -i.bak "s|^CATEDRA_API_TOKEN=.*|CATEDRA_API_TOKEN=${TOKEN}|" "$ENV_FILE"
    echo "✅ Token actualizado en .env"
else
    # Agregar nuevo
    echo "CATEDRA_API_TOKEN=${TOKEN}" >> "$ENV_FILE"
    echo "✅ Token agregado a .env"
fi

# Actualizar USERNAME y PASSWORD para futuros usos
if grep -q "^CATEDRA_USERNAME=" "$ENV_FILE"; then
    sed -i.bak "s|^CATEDRA_USERNAME=.*|CATEDRA_USERNAME=${USERNAME}|" "$ENV_FILE"
else
    echo "CATEDRA_USERNAME=${USERNAME}" >> "$ENV_FILE"
fi

if grep -q "^CATEDRA_PASSWORD=" "$ENV_FILE"; then
    sed -i.bak "s|^CATEDRA_PASSWORD=.*|CATEDRA_PASSWORD=${PASSWORD}|" "$ENV_FILE"
else
    echo "CATEDRA_PASSWORD=${PASSWORD}" >> "$ENV_FILE"
fi

echo "✅ Credenciales guardadas en .env"
echo ""

# Verificar token
echo "Verificando token..."
VERIFY=$(curl -s -H "Authorization: Bearer ${TOKEN}" \
    "${SERVER_URL}/api/endpoints/v1/eventos" | head -c 50)

if [ ! -z "$VERIFY" ]; then
    echo "✅ Token verificado correctamente!"
else
    echo "⚠️  No se pudo verificar el token (el servidor podría estar caído)"
fi

echo ""
echo "========================================="
echo "  TOKEN RENOVADO EXITOSAMENTE"
echo "========================================="
echo ""
echo "Backup guardado en: ${ENV_FILE}.backup"
echo ""

# Limpiar archivos temporales de sed
rm -f "${ENV_FILE}.bak"

