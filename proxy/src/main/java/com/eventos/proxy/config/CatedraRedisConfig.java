package com.eventos.proxy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de Redis para conectarse al Redis de cátedra
 * Este Redis contiene el estado de los asientos en tiempo real
 */
@Slf4j
@Configuration
public class CatedraRedisConfig {

    @Value("${catedra.redis.host}")
    private String host;

    @Value("${catedra.redis.port}")
    private int port;

    @Value("${catedra.redis.password:}")
    private String password;

    @Value("${catedra.redis.database:0}")
    private int database;

    /**
     * Factory de conexión a Redis de cátedra
     */
    @Bean(name = "catedraRedisConnectionFactory")
    public RedisConnectionFactory catedraRedisConnectionFactory() {
        log.info("Configurando conexión a Redis de cátedra: {}:{} DB:{}", host, port, database);
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);
        
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        
        return factory;
    }

    /**
     * RedisTemplate para operaciones con Redis de cátedra
     */
    @Bean(name = "catedraRedisTemplate")
    public RedisTemplate<String, Object> catedraRedisTemplate(
            RedisConnectionFactory catedraRedisConnectionFactory) {
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(catedraRedisConnectionFactory);
        
        // Usar StringRedisSerializer para keys y values
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        
        log.info("RedisTemplate de cátedra configurado correctamente");
        return template;
    }
}

