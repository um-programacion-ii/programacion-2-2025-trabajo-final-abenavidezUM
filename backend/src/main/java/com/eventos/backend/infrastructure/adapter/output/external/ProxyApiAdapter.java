package com.eventos.backend.infrastructure.adapter.output.external;

import com.eventos.backend.domain.ports.output.ProxyApiPort;
import com.eventos.backend.dto.proxy.ProxyEstadoAsientoResponseDTO;
import com.eventos.backend.dto.proxy.ProxyMapaAsientosResponseDTO;
import com.eventos.backend.infrastructure.adapter.output.external.service.ProxyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador para comunicación con servicio Proxy
 * Implementa el puerto de salida usando ProxyClient
 */
@Component
@RequiredArgsConstructor
public class ProxyApiAdapter implements ProxyApiPort {

    private final ProxyClient proxyClient;

    @Override
    public ProxyEstadoAsientoResponseDTO obtenerEstadoAsiento(Long eventoId, Integer fila, Integer columna) {
        return proxyClient.obtenerEstadoAsiento(eventoId, fila, columna);
    }

    @Override
    public ProxyMapaAsientosResponseDTO obtenerMapaAsientos(Long eventoId) {
        return proxyClient.obtenerMapaAsientos(eventoId);
    }

    @Override
    public boolean isProxyAvailable() {
        return proxyClient.isProxyAvailable();
    }
}

