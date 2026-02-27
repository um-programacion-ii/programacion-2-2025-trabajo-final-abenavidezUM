package com.eventos.backend.infrastructure.adapter.output.persistence;

import com.eventos.backend.domain.model.Evento;
import com.eventos.backend.domain.ports.output.EventoRepositoryPort;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia para eventos
 * Implementa el puerto de salida usando Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class EventoRepositoryAdapter implements EventoRepositoryPort {

    private final EventoRepository eventoRepository;

    @Override
    public Evento save(Evento evento) {
        return eventoRepository.save(evento);
    }

    @Override
    public Optional<Evento> findById(Long id) {
        return eventoRepository.findById(id);
    }

    @Override
    public Optional<Evento> findByIdExterno(Long idExterno) {
        return eventoRepository.findByIdExterno(idExterno);
    }

    @Override
    public List<Evento> findAllActivos() {
        return eventoRepository.findAllActive();
    }

    @Override
    public void deleteById(Long id) {
        eventoRepository.deleteById(id);
    }
}

