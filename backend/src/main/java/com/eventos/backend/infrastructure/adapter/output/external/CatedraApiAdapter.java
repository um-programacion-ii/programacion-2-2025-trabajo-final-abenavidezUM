package com.eventos.backend.infrastructure.adapter.output.external;

import com.eventos.backend.domain.ports.output.CatedraApiPort;
import com.eventos.backend.dto.catedra.*;
import com.eventos.backend.infrastructure.adapter.output.external.service.CatedraApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador para comunicación con API de Cátedra
 * Implementa el puerto de salida usando CatedraApiClient
 */
@Component
@RequiredArgsConstructor
public class CatedraApiAdapter implements CatedraApiPort {

    private final CatedraApiClient catedraApiClient;

    @Override
    public List<CatedraEventoResumenDTO> obtenerEventosResumidos() {
        return catedraApiClient.obtenerEventosResumidos();
    }

    @Override
    public List<CatedraEventoCompletoDTO> obtenerEventosCompletos() {
        return catedraApiClient.obtenerEventosCompletos();
    }

    @Override
    public CatedraEventoCompletoDTO obtenerEventoPorId(Long id) {
        return catedraApiClient.obtenerEventoPorId(id);
    }

    @Override
    public CatedraBloquearAsientosResponseDTO bloquearAsientos(CatedraBloquearAsientosRequestDTO request) {
        return catedraApiClient.bloquearAsientos(request);
    }

    @Override
    public CatedraRealizarVentaResponseDTO realizarVenta(CatedraRealizarVentaRequestDTO request) {
        return catedraApiClient.realizarVenta(request);
    }

    @Override
    public List<CatedraVentaResumenDTO> obtenerVentas() {
        return catedraApiClient.obtenerVentas();
    }

    @Override
    public CatedraVentaDetalleDTO obtenerVentaPorId(Long id) {
        return catedraApiClient.obtenerVentaPorId(id);
    }
}

