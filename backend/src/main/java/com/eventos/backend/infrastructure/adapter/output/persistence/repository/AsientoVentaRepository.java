package com.eventos.backend.infrastructure.adapter.output.persistence.repository;

import com.eventos.backend.domain.model.AsientoVenta;
import com.eventos.backend.domain.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsientoVentaRepository extends JpaRepository<AsientoVenta, Long> {

    /**
     * Buscar asientos por venta
     */
    List<AsientoVenta> findByVenta(Venta venta);

    /**
     * Buscar asientos por venta ID
     */
    List<AsientoVenta> findByVentaId(Long ventaId);

    /**
     * Buscar asiento por fila y columna en un evento específico (ventas exitosas)
     */
    @Query("SELECT a FROM AsientoVenta a WHERE a.venta.evento.id = :eventoId AND a.fila = :fila AND a.columna = :columna AND a.venta.resultado = true")
    List<AsientoVenta> findByEventoAndFilaAndColumna(
            @Param("eventoId") Long eventoId,
            @Param("fila") Integer fila,
            @Param("columna") Integer columna
    );

    /**
     * Verificar si un asiento está ocupado en un evento
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AsientoVenta a WHERE a.venta.evento.id = :eventoId AND a.fila = :fila AND a.columna = :columna AND a.venta.resultado = true")
    Boolean isAsientoOccupied(
            @Param("eventoId") Long eventoId,
            @Param("fila") Integer fila,
            @Param("columna") Integer columna
    );

    /**
     * Buscar todos los asientos vendidos de un evento (ventas exitosas)
     */
    @Query("SELECT a FROM AsientoVenta a WHERE a.venta.evento.id = :eventoId AND a.venta.resultado = true")
    List<AsientoVenta> findAsientosVendidosByEvento(@Param("eventoId") Long eventoId);

    /**
     * Buscar asientos por nombre de persona (búsqueda parcial)
     */
    @Query("SELECT a FROM AsientoVenta a WHERE LOWER(a.nombrePersona) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<AsientoVenta> findByNombrePersonaContaining(@Param("nombre") String nombre);

    /**
     * Contar asientos vendidos por evento
     */
    @Query("SELECT COUNT(a) FROM AsientoVenta a WHERE a.venta.evento.id = :eventoId AND a.venta.resultado = true")
    Long countAsientosVendidosByEvento(@Param("eventoId") Long eventoId);

    /**
     * Buscar asientos disponibles por fila en un evento
     */
    @Query("SELECT a.columna FROM AsientoVenta a WHERE a.venta.evento.id = :eventoId AND a.fila = :fila AND a.venta.resultado = true")
    List<Integer> findOccupiedColumnsByEventoAndFila(
            @Param("eventoId") Long eventoId,
            @Param("fila") Integer fila
    );

    /**
     * Obtener mapa de ocupación de un evento
     */
    @Query("SELECT NEW map(a.fila as fila, a.columna as columna, a.nombrePersona as persona) FROM AsientoVenta a WHERE a.venta.evento.id = :eventoId AND a.venta.resultado = true")
    List<Object> getOccupancyMap(@Param("eventoId") Long eventoId);

    /**
     * Eliminar asientos por venta
     */
    void deleteByVenta(Venta venta);
}

