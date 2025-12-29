#!/usr/bin/env python3
"""
Script de diagnóstico para Redis de cátedra
Verifica qué keys y datos hay almacenados
"""

import redis
import json
import sys
from typing import Dict, Any, Set

def connect_redis(host='192.168.194.250', port=6379, db=0) -> redis.Redis:
    """Conecta a Redis de cátedra"""
    print(f"🔌 Conectando a Redis: {host}:{port} DB:{db}")
    try:
        r = redis.Redis(
            host=host,
            port=port,
            db=db,
            decode_responses=True,
            socket_timeout=5,
            socket_connect_timeout=5
        )
        # Test connection
        r.ping()
        print("✅ Conexión exitosa\n")
        return r
    except Exception as e:
        print(f"❌ Error de conexión: {e}")
        sys.exit(1)

def scan_all_keys(r: redis.Redis, pattern='*') -> Set[str]:
    """Obtiene todas las keys que coincidan con el patrón"""
    keys = set()
    cursor = 0
    while True:
        cursor, partial_keys = r.scan(cursor, match=pattern, count=100)
        keys.update(partial_keys)
        if cursor == 0:
            break
    return keys

def inspect_evento(r: redis.Redis, evento_id: int) -> Dict[str, Any]:
    """Inspecciona todas las estructuras de datos de un evento"""
    print(f"\n🔍 Inspeccionando Evento ID: {evento_id}")
    print("=" * 60)
    
    result = {
        'eventoId': evento_id,
        'hashData': {},
        'individualKeys': [],
        'otherKeys': []
    }
    
    # 1. Hash: evento:{id}:asientos
    hash_key = f"evento:{evento_id}:asientos"
    print(f"\n📦 1. Hash Key: {hash_key}")
    if r.exists(hash_key):
        hash_data = r.hgetall(hash_key)
        result['hashData'] = hash_data
        print(f"   ✅ Existe - {len(hash_data)} asientos")
        if hash_data:
            print(f"   📊 Primeros 5 asientos:")
            for i, (field, value) in enumerate(list(hash_data.items())[:5]):
                print(f"      {field} -> {value}")
            if len(hash_data) > 5:
                print(f"      ... y {len(hash_data) - 5} más")
    else:
        print(f"   ❌ No existe")
    
    # 2. Keys individuales: evento:{id}:asiento:*
    pattern_individual = f"evento:{evento_id}:asiento:*"
    print(f"\n🔑 2. Keys individuales: {pattern_individual}")
    individual_keys = scan_all_keys(r, pattern_individual)
    result['individualKeys'] = list(individual_keys)
    print(f"   {'✅' if individual_keys else '❌'} {len(individual_keys)} keys encontradas")
    if individual_keys:
        print(f"   📊 Primeras 5 keys:")
        for key in list(individual_keys)[:5]:
            value = r.get(key)
            print(f"      {key} -> {value}")
        if len(individual_keys) > 5:
            print(f"      ... y {len(individual_keys) - 5} más")
    
    # 3. Cualquier otra key relacionada
    pattern_other = f"*evento*{evento_id}*"
    print(f"\n🔍 3. Cualquier key relacionada: {pattern_other}")
    other_keys = scan_all_keys(r, pattern_other)
    # Excluir las que ya encontramos
    other_keys = other_keys - {hash_key} - individual_keys
    result['otherKeys'] = list(other_keys)
    print(f"   {'✅' if other_keys else '❌'} {len(other_keys)} keys adicionales")
    if other_keys:
        for key in list(other_keys)[:5]:
            print(f"      {key}")
    
    # 4. Resumen
    total_data = len(result['hashData']) + len(result['individualKeys'])
    print(f"\n📊 Resumen:")
    print(f"   Hash: {len(result['hashData'])} asientos")
    print(f"   Keys individuales: {len(result['individualKeys'])}")
    print(f"   Total datos: {total_data}")
    
    return result

def find_all_eventos(r: redis.Redis) -> Set[int]:
    """Encuentra todos los IDs de eventos en Redis"""
    print("\n🔍 Buscando todos los eventos en Redis...")
    print("=" * 60)
    
    # Buscar todas las keys con "evento"
    all_keys = scan_all_keys(r, "*evento*")
    print(f"Total keys con 'evento': {len(all_keys)}")
    
    # Extraer IDs
    evento_ids = set()
    for key in all_keys:
        parts = key.split(":")
        for part in parts:
            try:
                evento_id = int(part)
                evento_ids.add(evento_id)
            except ValueError:
                continue
    
    print(f"\n✅ Eventos encontrados: {sorted(evento_ids)}")
    return evento_ids

def main():
    """Función principal"""
    print("\n" + "="*60)
    print("🔬 DIAGNÓSTICO DE REDIS DE CÁTEDRA")
    print("="*60)
    
    # Conectar
    r = connect_redis()
    
    # 1. Verificar conexión
    print("\n📡 1. Test de Conectividad")
    print("-" * 60)
    info = r.info('server')
    print(f"   Redis Version: {info.get('redis_version', 'Unknown')}")
    print(f"   OS: {info.get('os', 'Unknown')}")
    
    # 2. Buscar todos los eventos
    print("\n📚 2. Búsqueda Global de Eventos")
    print("-" * 60)
    evento_ids = find_all_eventos(r)
    
    # 3. Inspeccionar evento 1 (el que estamos probando)
    print("\n🎯 3. Inspección Detallada - Evento 1")
    print("-" * 60)
    inspect_evento(r, 1)
    
    # 4. Si hay otros eventos, mostrar resumen
    if len(evento_ids) > 1:
        print("\n📋 4. Resumen de Otros Eventos")
        print("-" * 60)
        for evento_id in sorted(evento_ids):
            if evento_id == 1:
                continue
            hash_key = f"evento:{evento_id}:asientos"
            if r.exists(hash_key):
                count = r.hlen(hash_key)
                print(f"   Evento {evento_id}: {count} asientos en hash")
    
    # 5. Keys generales
    print("\n🗝️  5. Análisis General de Keys")
    print("-" * 60)
    all_keys = scan_all_keys(r, "*")
    print(f"   Total keys en DB: {len(all_keys)}")
    
    # Agrupar por patrón
    patterns = {}
    for key in all_keys:
        pattern_parts = key.split(":")[0] if ":" in key else key
        patterns[pattern_parts] = patterns.get(pattern_parts, 0) + 1
    
    print(f"\n   Patrones encontrados:")
    for pattern, count in sorted(patterns.items(), key=lambda x: -x[1]):
        print(f"      {pattern}*: {count} keys")
    
    print("\n" + "="*60)
    print("✅ Diagnóstico completado")
    print("="*60 + "\n")

if __name__ == "__main__":
    main()

