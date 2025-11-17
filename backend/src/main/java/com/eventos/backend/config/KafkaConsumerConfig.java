package com.eventos.backend.config;

import com.eventos.backend.dto.kafka.EventoKafkaMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    /**
     * Configuración del Consumer Factory para mensajes de eventos
     */
    @Bean
    public ConsumerFactory<String, EventoKafkaMessageDTO> eventoConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        // Configuración básica
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        
        // Deserializadores
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        
        // Configuración del JsonDeserializer
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.eventos.backend.dto.kafka,java.time,java.util");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EventoKafkaMessageDTO.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        // Configuraciones de consumo
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Commit manual
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Procesar de a 10 mensajes máximo
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 segundos
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 segundos
        
        log.info("Configurando Kafka Consumer - Bootstrap: {}, Group: {}", bootstrapServers, groupId);
        
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Factory para crear listeners de Kafka
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventoKafkaMessageDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventoKafkaMessageDTO> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(eventoConsumerFactory());
        
        // Configurar commit manual (para asegurar que procesamos correctamente antes de hacer commit)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // Número de threads concurrentes para procesar mensajes
        factory.setConcurrency(1); // Por ahora solo 1 thread
        
        // Configuración de reintento en caso de error
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());
        
        log.info("Kafka Listener Container Factory configurado");
        
        return factory;
    }
}

