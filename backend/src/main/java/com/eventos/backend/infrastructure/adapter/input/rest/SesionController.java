package com.eventos.backend.infrastructure.adapter.input.rest;

import com.eventos.backend.dto.*;
import com.eventos.backend.application.service.SesionCompraServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para gestionar sesiones de compra
 */
@RestController
@RequestMapping("/api/sesion")
@RequiredArgsConstructor
@Slf4j
public class SesionController {

    private final SesionCompraServiceImpl sesionCompraService;

    /**
     * POST /api/sesion?eventoId=X
     * Inicia una sesión de compra para un evento
     */
    @PostMapping
    public ResponseEntity<SesionCompraDTO> iniciarSesion(@RequestParam Long eventoId) {
        log.info("POST /api/sesion - eventoId: {}", eventoId);
        SesionCompraDTO sesion = sesionCompraService.iniciarSesion(eventoId);
        return ResponseEntity.ok(sesion);
    }

    /**
     * GET /api/sesion
     * Obtiene el estado actual de la sesión de compra
     */
    @GetMapping
    public ResponseEntity<SesionCompraDTO> obtenerSesionActual() {
        log.info("GET /api/sesion");
        SesionCompraDTO sesion = sesionCompraService.obtenerSesionActual();
        if (sesion == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sesion);
    }

    /**
     * PUT /api/sesion/asientos
     * Actualiza los asientos seleccionados
     */
    @PutMapping("/asientos")
    public ResponseEntity<SesionCompraDTO> actualizarAsientos(
            @Valid @RequestBody ActualizarAsientosRequestDTO request) {
        log.info("PUT /api/sesion/asientos - cantidad: {}", request.getAsientos().size());
        SesionCompraDTO sesion = sesionCompraService.actualizarAsientos(request.getAsientos());
        return ResponseEntity.ok(sesion);
    }

    /**
     * PUT /api/sesion/personas
     * Actualiza los datos de las personas
     */
    @PutMapping("/personas")
    public ResponseEntity<SesionCompraDTO> actualizarPersonas(
            @Valid @RequestBody ActualizarPersonasRequestDTO request) {
        log.info("PUT /api/sesion/personas - cantidad: {}", request.getPersonas().size());
        SesionCompraDTO sesion = sesionCompraService.actualizarPersonas(request.getPersonas());
        return ResponseEntity.ok(sesion);
    }

    /**
     * PUT /api/sesion/renovar
     * Renueva la expiración de la sesión
     */
    @PutMapping("/renovar")
    public ResponseEntity<SesionCompraDTO> renovarSesion() {
        log.info("PUT /api/sesion/renovar");
        SesionCompraDTO sesion = sesionCompraService.obtenerSesionActual();
        if (sesion == null) {
            return ResponseEntity.noContent().build();
        }
        SesionCompraDTO renovada = sesionCompraService.renovarSesion(sesion);
        return ResponseEntity.ok(renovada);
    }

    /**
     * DELETE /api/sesion
     * Limpia la sesión de compra actual
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> limpiarSesion() {
        log.info("DELETE /api/sesion");
        sesionCompraService.limpiarSesion();
        return ResponseEntity.ok(Map.of("mensaje", "Sesión limpiada exitosamente"));
    }
}

