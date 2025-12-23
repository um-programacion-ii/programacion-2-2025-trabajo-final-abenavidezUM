package com.eventos.backend.infrastructure.config;

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

@Configuration
@Slf4j
public class RestTemplateConfig {

    @Value("${catedra.api.token:}")
    private String catedraToken;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .additionalInterceptors(new CatedraTokenInterceptor(catedraToken))
                .additionalInterceptors(new LoggingInterceptor())
                .build();
    }

    /**
     * Interceptor para agregar el token JWT de cátedra en todas las peticiones
     */
    private static class CatedraTokenInterceptor implements ClientHttpRequestInterceptor {
        
        private final String token;

        public CatedraTokenInterceptor(String token) {
            this.token = token;
        }

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request, 
                byte[] body, 
                ClientHttpRequestExecution execution) throws IOException {
            
            if (token != null && !token.isEmpty()) {
                request.getHeaders().set("Authorization", "Bearer " + token);
            }
            
            return execution.execute(request, body);
        }
    }

    /**
     * Interceptor para logging de requests y responses
     */
    @Slf4j
    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request, 
                byte[] body, 
                ClientHttpRequestExecution execution) throws IOException {
            
            log.debug("Request: {} {}", request.getMethod(), request.getURI());
            
            long startTime = System.currentTimeMillis();
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("Response: {} {} - Status: {} - Duration: {}ms", 
                    request.getMethod(), 
                    request.getURI(), 
                    response.getStatusCode(), 
                    duration);
            
            return response;
        }
    }
}

