package com.eventos.proxy.service;

import com.eventos.proxy.dto.kafka.CatedraAsientoNotificacionDTO;
import com.eventos.proxy.dto.kafka.CatedraEventoNotificacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Servicio para enviar notificaciones al backend cuando hay cambios
 * detectados en el servidor de cátedra (vía Kafka o Redis)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackendNotificationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${backend.url}")
    private String backendUrl;

    @Value("${backend.notification-endpoint}")
    private String notificationEndpoint;

    /**
     * Notifica al backend sobre un cambio en un evento
     * 
     * @param notificacion Datos del evento que cambió
     */
    public void notificarCambioEvento(CatedraEventoNotificacionDTO notificacion) {
        log.info("Enviando notificación de cambio de evento {} al backend: {}", 
                notificacion.getEventoId(), notificacion.getTipo());
        
        String url = backendUrl + notificationEndpoint + "/evento";
        
        webClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(notificacion)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> 
                        log.info("Notificación de evento {} enviada exitosamente", 
                                notificacion.getEventoId()))
                .doOnError(error -> 
                        log.error("Error al enviar notificación de evento {}: {}", 
                                notificacion.getEventoId(), error.getMessage()))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    /**
     * Notifica al backend sobre un cambio en el estado de un asiento
     * 
     * @param notificacion Datos del asiento que cambió
     */
    public void notificarCambioAsiento(CatedraAsientoNotificacionDTO notificacion) {
        log.info("Enviando notificación de cambio de asiento {}:{} evento {} al backend: {}", 
                notificacion.getFila(), notificacion.getColumna(), 
                notificacion.getEventoId(), notificacion.getTipo());
        
        String url = backendUrl + notificationEndpoint + "/asiento";
        
        webClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(notificacion)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> 
                        log.info("Notificación de asiento {}:{} evento {} enviada exitosamente", 
                                notificacion.getFila(), notificacion.getColumna(), 
                                notificacion.getEventoId()))
                .doOnError(error -> 
                        log.error("Error al enviar notificación de asiento {}:{} evento {}: {}", 
                                notificacion.getFila(), notificacion.getColumna(), 
                                notificacion.getEventoId(), error.getMessage()))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    /**
     * Verifica si el backend está disponible
     * 
     * @return true si está disponible, false si no
     */
    public boolean isBackendAvailable() {
        try {
            String healthUrl = backendUrl + "/actuator/health";
            
            String response = webClientBuilder.build()
                    .get()
                    .uri(healthUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            
            log.info("Backend está disponible");
            return response != null;
        } catch (Exception e) {
            log.warn("Backend NO está disponible: {}", e.getMessage());
            return false;
        }
    }
}

