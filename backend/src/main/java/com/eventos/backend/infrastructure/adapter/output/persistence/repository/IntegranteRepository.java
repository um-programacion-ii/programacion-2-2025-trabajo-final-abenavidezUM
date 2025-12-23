package com.eventos.backend.infrastructure.adapter.output.persistence.repository;

import com.eventos.backend.domain.model.Evento;
import com.eventos.backend.domain.model.Integrante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegranteRepository extends JpaRepository<Integrante, Long> {

    /**
     * Buscar integrantes por evento
     */
    List<Integrante> findByEvento(Evento evento);

    /**
     * Buscar integrantes por evento ID
     */
    List<Integrante> findByEventoId(Long eventoId);

    /**
     * Buscar integrantes por identificación
     */
    List<Integrante> findByIdentificacion(String identificacion);

    /**
     * Buscar integrantes por nombre y apellido (búsqueda parcial)
     */
    @Query("SELECT i FROM Integrante i WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND LOWER(i.apellido) LIKE LOWER(CONCAT('%', :apellido, '%'))")
    List<Integrante> findByNombreAndApellidoContaining(
            @Param("nombre") String nombre,
            @Param("apellido") String apellido
    );

    /**
     * Contar integrantes por evento
     */
    Long countByEvento(Evento evento);

    /**
     * Eliminar todos los integrantes de un evento
     */
    void deleteByEvento(Evento evento);
}

