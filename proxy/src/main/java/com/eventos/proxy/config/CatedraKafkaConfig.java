package com.eventos.proxy.config;

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

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Kafka para consumir notificaciones del servidor de cátedra
 */
@Slf4j
@EnableKafka
@Configuration
public class CatedraKafkaConfig {

    @Value("${catedra.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${catedra.kafka.group-id}")
    private String groupId;

    /**
     * Factory de consumers de Kafka para mensajes de cátedra
     */
    @Bean
    public ConsumerFactory<String, String> catedraConsumerFactory() {
        log.info("Configurando Kafka consumer para cátedra: {} - Grupo: {}", 
                bootstrapServers, groupId);
        
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Container factory para listeners de Kafka
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> catedraKafkaListenerContainerFactory(
            ConsumerFactory<String, String> catedraConsumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(catedraConsumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        log.info("Kafka listener container factory configurado");
        return factory;
    }
}

