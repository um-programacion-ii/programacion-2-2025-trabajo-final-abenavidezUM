package com.eventos.proxy.controller;

import com.eventos.proxy.client.CatedraApiClient;
import com.eventos.proxy.dto.BloquearAsientosRequestDTO;
import com.eventos.proxy.dto.BloquearAsientosResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para endpoints de bloqueo de asientos
 */
@Slf4j
@RestController
@RequestMapping("/api/asientos")
@RequiredArgsConstructor
public class BloqueoController {

    private final CatedraApiClient catedraApiClient;

    /**
     * POST /api/asientos/bloquear
     * 
     * Bloquea asientos para un evento específico.
     * Los asientos deben estar en estado "Libre" para poder ser bloqueados.
     * 
     * Request body:
     * {
     *   "eventoId": 1,
     *   "asientos": [
     *     {"fila": 2, "columna": 1},
     *     {"fila": 2, "columna": 2}
     *   ]
     * }
     * 
     * Response:
     * {
     *   "resultado": true/false,
     *   "descripcion": "Mensaje descriptivo",
     *   "eventoId": 1,
     *   "asientos": [
     *     {"fila": 2, "columna": 1, "estado": "Bloqueo exitoso" o "Ocupado" o "Bloqueado"}
     *   ]
     * }
     * 
     * @param request Petición con eventoId y asientos a bloquear
     * @return Respuesta con resultado del bloqueo
     */
    @PostMapping("/bloquear")
    public ResponseEntity<BloquearAsientosResponseDTO> bloquearAsientos(
            @RequestBody BloquearAsientosRequestDTO request) {
        
        log.info("POST /api/asientos/bloquear - Evento: {}, Asientos: {}", 
                request.getEventoId(), 
                request.getAsientos().size());
        
        try {
            BloquearAsientosResponseDTO response = catedraApiClient.bloquearAsientos(request);
            
            if (response == null) {
                log.error("Respuesta nula del servicio de cátedra");
                return ResponseEntity.status(503).build(); // Service Unavailable
            }
            
            // Devolver 200 OK siempre, el campo "resultado" indica si el bloqueo fue exitoso
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al bloquear asientos para evento {}: {}", 
                    request.getEventoId(), e.getMessage());
            return ResponseEntity.status(503).build(); // Service Unavailable
        }
    }
}

