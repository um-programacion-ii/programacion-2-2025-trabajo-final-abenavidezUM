package com.eventos.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

/**
 * Configuración de RestTemplate para comunicación con el servicio proxy
 * 
 * Este RestTemplate es diferente al que se usa para la API de cátedra:
 * - No necesita el token JWT de cátedra
 * - Timeouts diferentes (más cortos para consultas de Redis)
 * - Logging específico
 */
@Configuration
@Slf4j
public class ProxyRestTemplateConfig {

    /**
     * RestTemplate específico para consumir la API del proxy
     */
    @Bean(name = "proxyRestTemplate")
    public RestTemplate proxyRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))  // Más corto que cátedra
                .setReadTimeout(Duration.ofSeconds(10))     // Redis es rápido
                .additionalInterceptors(new ProxyLoggingInterceptor())
                .build();
    }

    /**
     * Interceptor para logging de requests al proxy
     */
    private static class ProxyLoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

            log.debug("Request to Proxy: {} {}", request.getMethod(), request.getURI());

            long startTime = System.currentTimeMillis();

            ClientHttpResponse response = execution.execute(request, body);

            long duration = System.currentTimeMillis() - startTime;

            log.debug("Response from Proxy: {} - {} ms", 
                    response.getStatusCode(), duration);

            return response;
        }
    }
}

