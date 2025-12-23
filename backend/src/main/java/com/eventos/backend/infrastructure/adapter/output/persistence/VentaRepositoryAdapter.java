package com.eventos.backend.infrastructure.adapter.output.persistence;

import com.eventos.backend.domain.model.Venta;
import com.eventos.backend.domain.ports.output.VentaRepositoryPort;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia para ventas
 * Implementa el puerto de salida usando Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class VentaRepositoryAdapter implements VentaRepositoryPort {

    private final VentaRepository ventaRepository;

    @Override
    public Venta save(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    public Optional<Venta> findById(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<Venta> findPendientesConfirmacion() {
        return ventaRepository.findPendientesConfirmacion();
    }
}

