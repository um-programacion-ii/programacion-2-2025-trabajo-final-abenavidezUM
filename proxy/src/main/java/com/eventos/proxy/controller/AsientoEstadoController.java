package com.eventos.proxy.controller;

import com.eventos.proxy.dto.EstadoAsientoResponseDTO;
import com.eventos.proxy.dto.MapaAsientosResponseDTO;
import com.eventos.proxy.service.CatedraRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller para consultar el estado de asientos en tiempo real
 * consultando el Redis de cátedra
 * 
 * Este es el propósito REAL del proxy: dar acceso al estado de asientos
 * sin que el backend tenga que conectarse directamente a Redis de cátedra
 */
@Slf4j
@RestController
@RequestMapping("/api/asientos")
@RequiredArgsConstructor
public class AsientoEstadoController {

    private final CatedraRedisService catedraRedisService;

    /**
     * GET /api/asientos/estado/{eventoId}/{fila}/{columna}
     * 
     * Consulta el estado actual de un asiento específico en Redis de cátedra
     * 
     * @param eventoId ID del evento
     * @param fila Fila del asiento
     * @param columna Columna del asiento
     * @return Estado actual del asiento
     */
    @GetMapping("/estado/{eventoId}/{fila}/{columna}")
    public ResponseEntity<EstadoAsientoResponseDTO> getEstadoAsiento(
            @PathVariable Long eventoId,
            @PathVariable Integer fila,
            @PathVariable Integer columna) {
        
        log.info("GET /api/asientos/estado/{}/{}/{} - Consultando estado de asiento", 
                eventoId, fila, columna);
        
        String estado = catedraRedisService.getEstadoAsiento(eventoId, fila, columna);
        
        if (estado == null) {
            log.warn("No se pudo obtener estado del asiento {}:{} evento {}", 
                    fila, columna, eventoId);
            return ResponseEntity.status(503).build(); // Service Unavailable
        }
        
        EstadoAsientoResponseDTO response = EstadoAsientoResponseDTO.builder()
                .eventoId(eventoId)
                .fila(fila)
                .columna(columna)
                .estado(estado)
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/asientos/mapa/{eventoId}
     * 
     * Obtiene el mapa completo de asientos de un evento con sus estados actuales
     * consultando Redis de cátedra
     * 
     * @param eventoId ID del evento
     * @return Mapa de asientos con sus estados
     */
    @GetMapping("/mapa/{eventoId}")
    public ResponseEntity<MapaAsientosResponseDTO> getMapaAsientos(
            @PathVariable Long eventoId) {
        
        log.info("GET /api/asientos/mapa/{} - Consultando mapa de asientos", eventoId);
        
        Map<Object, Object> asientosRaw = catedraRedisService.getEstadoAsientosEvento(eventoId);
        
        // Convertir a Map<String, String>
        Map<String, String> asientos = asientosRaw.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue().toString()
                ));
        
        // Obtener resumen por estado
        Map<String, Long> resumen = catedraRedisService.contarAsientosPorEstado(eventoId);
        
        MapaAsientosResponseDTO response = MapaAsientosResponseDTO.builder()
                .eventoId(eventoId)
                .asientos(asientos)
                .resumen(resumen)
                .totalAsientos(asientos.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/asientos/resumen/{eventoId}
     * 
     * Obtiene un resumen con el conteo de asientos por estado
     * 
     * @param eventoId ID del evento
     * @return Conteo de asientos por estado
     */
    @GetMapping("/resumen/{eventoId}")
    public ResponseEntity<Map<String, Object>> getResumenAsientos(
            @PathVariable Long eventoId) {
        
        log.info("GET /api/asientos/resumen/{} - Consultando resumen de asientos", eventoId);
        
        Map<String, Long> resumen = catedraRedisService.contarAsientosPorEstado(eventoId);
        
        Map<String, Object> response = Map.of(
                "eventoId", eventoId,
                "resumen", resumen,
                "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(response);
    }
}

