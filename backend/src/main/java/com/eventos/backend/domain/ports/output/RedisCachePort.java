package com.eventos.backend.domain.ports.output;

import java.util.concurrent.TimeUnit;

/**
 * Puerto de salida para caché en Redis
 * Define las operaciones de caché que el dominio necesita
 */
public interface RedisCachePort {
    
    /**
     * Guarda un valor en Redis
     * @param key clave
     * @param value valor a guardar
     * @param ttl tiempo de vida
     * @param timeUnit unidad de tiempo
     */
    void save(String key, Object value, long ttl, TimeUnit timeUnit);
    
    /**
     * Obtiene un valor de Redis
     * @param key clave
     * @param clazz clase del objeto a deserializar
     * @return valor o null si no existe
     */
    <T> T get(String key, Class<T> clazz);
    
    /**
     * Elimina un valor de Redis
     * @param key clave
     */
    void delete(String key);
    
    /**
     * Verifica si existe una clave en Redis
     * @param key clave
     * @return true si existe
     */
    boolean exists(String key);
}

