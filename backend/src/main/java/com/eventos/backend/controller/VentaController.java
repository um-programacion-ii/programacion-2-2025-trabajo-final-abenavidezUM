package com.eventos.backend.controller;

import com.eventos.backend.dto.VentaDTO;
import com.eventos.backend.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestionar ventas
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

    private final VentaService ventaService;

    /**
     * POST /api/ventas
     * Realiza una venta con la sesión de compra actual
     */
    @PostMapping
    public ResponseEntity<VentaDTO> realizarVenta() {
        log.info("POST /api/ventas");
        VentaDTO venta = ventaService.realizarVenta();
        return ResponseEntity.ok(venta);
    }

    /**
     * GET /api/ventas
     * Obtiene el historial de ventas del usuario
     */
    @GetMapping
    public ResponseEntity<List<VentaDTO>> obtenerMisVentas() {
        log.info("GET /api/ventas");
        List<VentaDTO> ventas = ventaService.obtenerMisVentas();
        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas/{id}
     * Obtiene el detalle de una venta
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerVenta(@PathVariable Long id) {
        log.info("GET /api/ventas/{}", id);
        VentaDTO venta = ventaService.obtenerVenta(id);
        return ResponseEntity.ok(venta);
    }
}

