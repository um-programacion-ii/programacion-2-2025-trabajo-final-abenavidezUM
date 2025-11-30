package com.eventos.backend.controller;

import com.eventos.backend.dto.BloquearAsientosRequestDTO;
import com.eventos.backend.dto.BloquearAsientosResponseDTO;
import com.eventos.backend.dto.MapaAsientosDTO;
import com.eventos.backend.service.AsientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para gestionar asientos de eventos
 */
@RestController
@RequestMapping("/api/asientos")
@RequiredArgsConstructor
@Slf4j
public class AsientoController {

    private final AsientoService asientoService;

    /**
     * GET /api/asientos/evento/{eventoId}
     * Obtiene el mapa de asientos de un evento
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<MapaAsientosDTO> obtenerMapaAsientos(@PathVariable Long eventoId) {
        log.info("GET /api/asientos/evento/{}", eventoId);
        MapaAsientosDTO mapa = asientoService.obtenerMapaAsientos(eventoId);
        return ResponseEntity.ok(mapa);
    }

    /**
     * POST /api/asientos/bloquear
     * Bloquea asientos seleccionados
     */
    @PostMapping("/bloquear")
    public ResponseEntity<BloquearAsientosResponseDTO> bloquearAsientos(
            @Valid @RequestBody BloquearAsientosRequestDTO request) {
        log.info("POST /api/asientos/bloquear - evento: {}, cantidad: {}", 
                request.getEventoId(), request.getAsientos().size());
        BloquearAsientosResponseDTO response = asientoService.bloquearAsientos(
                request.getEventoId(), request.getAsientos());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/asientos/liberar
     * Libera los asientos bloqueados del usuario actual
     */
    @PostMapping("/liberar")
    public ResponseEntity<Map<String, String>> liberarAsientos() {
        log.info("POST /api/asientos/liberar");
        asientoService.liberarAsientos();
        return ResponseEntity.ok(Map.of("mensaje", "Asientos liberados"));
    }
}

