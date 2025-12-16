package com.eventos.backend.controller;

import com.eventos.backend.dto.VentaDTO;
import com.eventos.backend.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestionar ventas e historial
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
     * Obtiene el historial de ventas del usuario (sin paginación)
     */
    @GetMapping
    public ResponseEntity<List<VentaDTO>> obtenerMisVentas() {
        log.info("GET /api/ventas");
        List<VentaDTO> ventas = ventaService.obtenerMisVentas();
        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas/paginado
     * Obtiene el historial de ventas con paginación
     */
    @GetMapping("/paginado")
    public ResponseEntity<Page<VentaDTO>> obtenerMisVentasPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/ventas/paginado - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<VentaDTO> ventas = ventaService.obtenerMisVentasPaginado(pageable);
        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas/exitosas
     * Obtiene solo las ventas exitosas
     */
    @GetMapping("/exitosas")
    public ResponseEntity<Page<VentaDTO>> obtenerVentasExitosas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/ventas/exitosas - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<VentaDTO> ventas = ventaService.obtenerVentasExitosas(pageable);
        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas/fallidas
     * Obtiene solo las ventas fallidas
     */
    @GetMapping("/fallidas")
    public ResponseEntity<Page<VentaDTO>> obtenerVentasFallidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/ventas/fallidas - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<VentaDTO> ventas = ventaService.obtenerVentasFallidas(pageable);
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
