package com.eventos.proxy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * Configuración de RestTemplate para consumir la API de cátedra
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    @Value("${catedra.api.token}")
    private String catedraToken;

    @Bean
    public RestTemplate catedraRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .additionalInterceptors(authInterceptor(), loggingInterceptor())
                .build();
    }

    /**
     * Interceptor para agregar el token JWT a todas las peticiones
     */
    private ClientHttpRequestInterceptor authInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().setBearerAuth(catedraToken);
            return execution.execute(request, body);
        };
    }

    /**
     * Interceptor para logging de requests y responses
     */
    private ClientHttpRequestInterceptor loggingInterceptor() {
        return new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                                ClientHttpRequestExecution execution) throws IOException {
                log.debug("Request: {} {}", request.getMethod(), request.getURI());
                
                ClientHttpResponse response = execution.execute(request, body);
                
                log.debug("Response: {} (Status: {})", 
                        request.getURI(), response.getStatusCode());
                
                return response;
            }
        };
    }
}

