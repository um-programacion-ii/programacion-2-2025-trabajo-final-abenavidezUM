package com.eventos.backend.repository;

import com.eventos.backend.domain.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoEventoRepository extends JpaRepository<TipoEvento, Long> {

    Optional<TipoEvento> findByNombre(String nombre);

    Boolean existsByNombre(String nombre);
}

