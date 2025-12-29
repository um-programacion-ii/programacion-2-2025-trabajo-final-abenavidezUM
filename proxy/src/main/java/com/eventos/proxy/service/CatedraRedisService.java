package com.eventos.proxy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Servicio para consultar el estado de asientos en Redis de cátedra
 * 
 * El Redis de cátedra almacena el estado de los asientos en tiempo real:
 * - Key pattern: "evento:{eventoId}:asiento:{fila}:{columna}"
 * - Value: "LIBRE", "BLOQUEADO", "OCUPADO", "VENDIDO"
 * 
 * O puede usar una estructura de hash:
 * - Key: "evento:{eventoId}:asientos"
 * - Hash field: "{fila}:{columna}"
 * - Hash value: estado
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatedraRedisService {

    @Qualifier("catedraRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Obtiene el estado de un asiento específico
     * 
     * FORMATO REAL DE CÁTEDRA:
     * - Key: "evento_{id}" (con guión bajo)
     * - Value: JSON con asientos bloqueados/vendidos
     * 
     * @param eventoId ID del evento
     * @param fila Fila del asiento
     * @param columna Columna del asiento
     * @return Estado del asiento ("Libre", "Bloqueado", "Vendido") o "Libre" por defecto
     */
    public String getEstadoAsiento(Long eventoId, int fila, int columna) {
        try {
            // Obtener todos los asientos y buscar el específico
            Map<Object, Object> asientos = getEstadoAsientosEvento(eventoId);
            String key = fila + ":" + columna;
            
            if (asientos.containsKey(key)) {
                String estado = asientos.get(key).toString();
                log.debug("Estado asiento {}:{} evento {}: {}", fila, columna, eventoId, estado);
                return estado;
            }
            
            log.debug("Asiento {}:{} evento {} no encontrado en Redis (LIBRE por defecto)", fila, columna, eventoId);
            return "Libre"; // ✅ Con mayúscula inicial, como usa la cátedra
            
        } catch (Exception e) {
            log.error("Error al consultar estado de asiento {}:{} evento {}: {}", 
                    fila, columna, eventoId, e.getMessage());
            return "Libre"; // Fallback seguro
        }
    }

    /**
     * Obtiene el estado de todos los asientos de un evento
     * 
     * FORMATO REAL DE CÁTEDRA:
     * - Key: "evento_{id}" (con guión bajo)
     * - Value: JSON string con estructura:
     *   {"eventoId":1,"asientos":[{"fila":1,"columna":3,"estado":"Bloqueado",...}, ...]}
     * 
     * Solo se guardan asientos BLOQUEADOS o VENDIDOS, el resto se considera LIBRE.
     * 
     * @param eventoId ID del evento
     * @return Mapa con posición del asiento (fila:columna) y su estado
     */
    public Map<Object, Object> getEstadoAsientosEvento(Long eventoId) {
        try {
            // ✅ FORMATO CORRECTO: "evento_" + id (con guión bajo)
            String key = String.format("evento_%d", eventoId);
            String json = (String) redisTemplate.opsForValue().get(key);
            
            Map<Object, Object> resultado = new java.util.HashMap<>();
            
            if (json == null || json.isBlank()) {
                log.debug("No hay datos en Redis para key {} (ningún asiento bloqueado/vendido)", key);
                return resultado; // Vacío = todos libres
            }
            
            // Parsear JSON
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
                com.fasterxml.jackson.databind.JsonNode asientosNode = root.get("asientos");
                
                if (asientosNode != null && asientosNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode asientoNode : asientosNode) {
                        int fila = asientoNode.path("fila").asInt();
                        int columna = asientoNode.path("columna").asInt();
                        String estado = asientoNode.path("estado").asText();
                        
                        String mapKey = fila + ":" + columna;
                        resultado.put(mapKey, estado);
                    }
                }
                
                log.debug("Obtenidos {} asientos para evento {} desde key {}", resultado.size(), eventoId, key);
                
            } catch (Exception jsonEx) {
                log.error("Error al parsear JSON de Redis para evento {}: {}", eventoId, jsonEx.getMessage());
            }
            
            return resultado;
            
        } catch (Exception e) {
            log.error("Error al obtener asientos del evento {}: {}", eventoId, e.getMessage());
            return Map.of();
        }
    }

    /**
     * Obtiene la lista de eventos que tienen información en Redis
     * 
     * @return Set de IDs de eventos
     */
    public Set<String> getEventosConAsientos() {
        try {
            // ✅ FORMATO CORRECTO: "evento_*" (con guión bajo)
            Set<String> keys = redisTemplate.keys("evento_*");
            log.debug("Encontrados {} eventos con asientos en Redis", 
                    keys != null ? keys.size() : 0);
            return keys != null ? keys : Set.of();
        } catch (Exception e) {
            log.error("Error al obtener eventos con asientos: {}", e.getMessage());
            return Set.of();
        }
    }

    /**
     * Verifica si Redis de cátedra está disponible
     * 
     * @return true si está disponible, false si no
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("Redis de cátedra está disponible");
            return true;
        } catch (Exception e) {
            log.warn("Redis de cátedra NO está disponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cuenta los asientos en cada estado para un evento
     * 
     * @param eventoId ID del evento
     * @return Mapa con conteos por estado
     */
    public Map<String, Long> contarAsientosPorEstado(Long eventoId) {
        try {
            Map<Object, Object> asientos = getEstadoAsientosEvento(eventoId);
            
            return asientos.values().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.groupingBy(
                            estado -> estado,
                            java.util.stream.Collectors.counting()
                    ));
        } catch (Exception e) {
            log.error("Error al contar asientos por estado evento {}: {}", 
                    eventoId, e.getMessage());
            return Map.of();
        }
    }
}


