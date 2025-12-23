package com.eventos.backend.infrastructure.adapter.output.persistence.repository;

import com.eventos.backend.domain.model.Evento;
import com.eventos.backend.domain.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Buscar evento por ID externo (ID del servicio de cátedra)
     */
    Optional<Evento> findByIdExterno(Long idExterno);

    /**
     * Buscar todos los eventos activos
     */
    @Query("SELECT e FROM Evento e WHERE e.activo = true ORDER BY e.fecha ASC")
    List<Evento> findAllActive();

    /**
     * Buscar eventos activos con sus integrantes (evita N+1)
     */
    @Query("SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.integrantes WHERE e.activo = true ORDER BY e.fecha ASC")
    List<Evento> findAllActiveWithIntegrantes();

    /**
     * Buscar eventos por tipo
     */
    List<Evento> findByTipoEventoAndActivoTrue(TipoEvento tipoEvento);

    /**
     * Buscar eventos futuros activos
     */
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND e.fecha > :fecha ORDER BY e.fecha ASC")
    List<Evento> findFutureEvents(@Param("fecha") LocalDateTime fecha);

    /**
     * Buscar eventos pasados activos
     */
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND e.fecha < :fecha ORDER BY e.fecha DESC")
    List<Evento> findPastEvents(@Param("fecha") LocalDateTime fecha);

    /**
     * Buscar eventos en un rango de fechas
     */
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND e.fecha BETWEEN :inicio AND :fin ORDER BY e.fecha ASC")
    List<Evento> findEventsBetweenDates(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    /**
     * Buscar eventos por título (búsqueda parcial, case-insensitive)
     */
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND LOWER(e.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY e.fecha ASC")
    List<Evento> findByTituloContaining(@Param("titulo") String titulo);

    /**
     * Buscar eventos por tipo y rango de fechas
     */
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND e.tipoEvento = :tipo AND e.fecha BETWEEN :inicio AND :fin ORDER BY e.fecha ASC")
    List<Evento> findByTipoAndDateRange(
            @Param("tipo") TipoEvento tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    /**
     * Contar eventos activos
     */
    @Query("SELECT COUNT(e) FROM Evento e WHERE e.activo = true")
    Long countActiveEvents();

    /**
     * Verificar si existe evento por ID externo
     */
    Boolean existsByIdExterno(Long idExterno);

    /**
     * Buscar eventos que necesitan sincronización (modificados recientemente)
     */
    @Query("SELECT e FROM Evento e WHERE e.activo = true AND (e.ultimaSincronizacion IS NULL OR e.updatedAt > e.ultimaSincronizacion)")
    List<Evento> findEventsNeedingSync();

    /**
     * Obtener el evento con la última sincronización más reciente
     */
    Optional<Evento> findFirstByOrderByUltimaSincronizacionDesc();

    /**
     * Buscar todos los eventos activos (sin ordenar)
     */
    List<Evento> findByActivoTrue();
}

