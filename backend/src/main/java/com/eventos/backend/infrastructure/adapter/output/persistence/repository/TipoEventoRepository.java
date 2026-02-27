package com.eventos.backend.infrastructure.adapter.output.persistence.repository;

import com.eventos.backend.domain.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoEventoRepository extends JpaRepository<TipoEvento, Long> {

    Optional<TipoEvento> findByNombre(String nombre);

    Boolean existsByNombre(String nombre);
}

