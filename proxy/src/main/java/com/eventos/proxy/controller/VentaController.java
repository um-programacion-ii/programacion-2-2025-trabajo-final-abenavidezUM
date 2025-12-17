package com.eventos.proxy.controller;

import com.eventos.proxy.client.CatedraApiClient;
import com.eventos.proxy.dto.RealizarVentaRequestDTO;
import com.eventos.proxy.dto.RealizarVentaResponseDTO;
import com.eventos.proxy.dto.VentaDetalleDTO;
import com.eventos.proxy.dto.VentaResumenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para endpoints de ventas
 */
@Slf4j
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final CatedraApiClient catedraApiClient;

    /**
     * POST /api/ventas/realizar
     * 
     * Realiza una venta de asientos para un evento.
     * Los asientos DEBEN estar previamente bloqueados, de lo contrario la venta fallará.
     * 
     * Request body:
     * {
     *   "eventoId": 1,
     *   "fecha": "2025-08-17T20:00:00.000Z",
     *   "precioVenta": 1400.10,
     *   "asientos": [
     *     {
     *       "fila": 2,
     *       "columna": 3,
     *       "persona": "Fernando Galvez"
     *     }
     *   ]
     * }
     * 
     * Response:
     * {
     *   "eventoId": 1,
     *   "ventaId": 1506,
     *   "fechaVenta": "2025-08-24T23:18:41.974720Z",
     *   "resultado": true/false,
     *   "descripcion": "Venta realizada con exito" o "Venta rechazada...",
     *   "precioVenta": 1400.0,
     *   "asientos": [...]
     * }
     * 
     * @param request Petición con datos de la venta
     * @return Respuesta con resultado de la venta
     */
    @PostMapping("/realizar")
    public ResponseEntity<RealizarVentaResponseDTO> realizarVenta(
            @RequestBody RealizarVentaRequestDTO request) {
        
        log.info("POST /api/ventas/realizar - Evento: {}, Precio: {}, Asientos: {}", 
                request.getEventoId(), 
                request.getPrecioVenta(),
                request.getAsientos().size());
        
        RealizarVentaResponseDTO response = catedraApiClient.realizarVenta(request);
        // Devolver 200 OK siempre, el campo "resultado" indica si la venta fue exitosa
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/ventas
     * 
     * Obtiene la lista de todas las ventas del alumno (exitosas y fallidas).
     * 
     * Response:
     * [
     *   {
     *     "eventoId": 1,
     *     "ventaId": 1503,
     *     "fechaVenta": "2025-08-23T22:51:02.574851Z",
     *     "resultado": false,
     *     "descripcion": "Venta rechazada...",
     *     "precioVenta": 1200.1,
     *     "cantidadAsientos": 0
     *   }
     * ]
     * 
     * @return Lista de ventas resumidas
     */
    @GetMapping
    public ResponseEntity<List<VentaResumenDTO>> getVentas() {
        log.info("GET /api/ventas - Consultando lista de ventas");
        List<VentaResumenDTO> ventas = catedraApiClient.getVentas();
        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas/{id}
     * 
     * Obtiene el detalle completo de una venta específica.
     * 
     * Response (venta exitosa):
     * {
     *   "eventoId": 1,
     *   "ventaId": 1504,
     *   "fechaVenta": "2025-08-23T22:51:15.101553Z",
     *   "resultado": true,
     *   "descripcion": "Venta realizada con exito",
     *   "precioVenta": 1200.1,
     *   "asientos": [
     *     {
     *       "fila": 2,
     *       "columna": 1,
     *       "persona": "Fernando Villarreal",
     *       "estado": "Ocupado"
     *     }
     *   ]
     * }
     * 
     * Response (venta fallida):
     * {
     *   "eventoId": 1,
     *   "ventaId": 1503,
     *   "fechaVenta": "2025-08-23T22:51:02.574851Z",
     *   "resultado": false,
     *   "descripcion": "Venta rechazada...",
     *   "precioVenta": 1200.1,
     *   "asientos": []
     * }
     * 
     * @param id ID de la venta
     * @return Detalle completo de la venta
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaDetalleDTO> getVentaById(@PathVariable Long id) {
        log.info("GET /api/ventas/{} - Consultando detalle de venta", id);
        VentaDetalleDTO venta = catedraApiClient.getVentaById(id);
        return ResponseEntity.ok(venta);
    }
}

