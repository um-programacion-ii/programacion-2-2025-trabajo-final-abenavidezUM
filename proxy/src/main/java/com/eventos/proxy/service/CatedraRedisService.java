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
     * @param eventoId ID del evento
     * @param fila Fila del asiento
     * @param columna Columna del asiento
     * @return Estado del asiento ("LIBRE", "BLOQUEADO", "OCUPADO", "VENDIDO") o null si no existe
     */
    public String getEstadoAsiento(Long eventoId, int fila, int columna) {
        try {
            // Intentar con estructura de hash (más eficiente)
            String hashKey = String.format("evento:%d:asientos", eventoId);
            String field = String.format("%d:%d", fila, columna);
            
            Object estado = redisTemplate.opsForHash().get(hashKey, field);
            
            if (estado != null) {
                log.debug("Estado asiento {}:{} evento {}: {}", fila, columna, eventoId, estado);
                return estado.toString();
            }
            
            // Si no existe en hash, intentar con key individual
            String key = String.format("evento:%d:asiento:%d:%d", eventoId, fila, columna);
            Object estadoIndividual = redisTemplate.opsForValue().get(key);
            
            if (estadoIndividual != null) {
                log.debug("Estado asiento {}:{} evento {}: {}", fila, columna, eventoId, estadoIndividual);
                return estadoIndividual.toString();
            }
            
            log.debug("Asiento {}:{} evento {} no encontrado en Redis", fila, columna, eventoId);
            return "LIBRE"; // Por defecto si no existe
            
        } catch (Exception e) {
            log.error("Error al consultar estado de asiento {}:{} evento {}: {}", 
                    fila, columna, eventoId, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el estado de todos los asientos de un evento
     * 
     * @param eventoId ID del evento
     * @return Mapa con posición del asiento (fila:columna) y su estado
     */
    public Map<Object, Object> getEstadoAsientosEvento(Long eventoId) {
        try {
            String hashKey = String.format("evento:%d:asientos", eventoId);
            Map<Object, Object> asientos = redisTemplate.opsForHash().entries(hashKey);
            
            log.debug("Obtenidos {} asientos para evento {}", asientos.size(), eventoId);
            return asientos;
            
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
            Set<String> keys = redisTemplate.keys("evento:*:asientos");
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

