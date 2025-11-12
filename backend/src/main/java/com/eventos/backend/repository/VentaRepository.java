package com.eventos.backend.repository;

import com.eventos.backend.domain.Evento;
import com.eventos.backend.domain.Usuario;
import com.eventos.backend.domain.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Buscar venta por ID externo (ID del servicio de cátedra)
     */
    Optional<Venta> findByIdExterno(Long idExterno);

    /**
     * Buscar ventas por usuario
     */
    List<Venta> findByUsuario(Usuario usuario);

    /**
     * Buscar ventas por usuario ID ordenadas por fecha descendente
     */
    @Query("SELECT v FROM Venta v WHERE v.usuario.id = :usuarioId ORDER BY v.fechaVenta DESC")
    List<Venta> findByUsuarioIdOrderByFechaVentaDesc(@Param("usuarioId") Long usuarioId);

    /**
     * Buscar ventas por evento
     */
    List<Venta> findByEvento(Evento evento);

    /**
     * Buscar ventas por evento ID
     */
    List<Venta> findByEventoId(Long eventoId);

    /**
     * Buscar ventas exitosas por usuario
     */
    @Query("SELECT v FROM Venta v WHERE v.usuario.id = :usuarioId AND v.resultado = true ORDER BY v.fechaVenta DESC")
    List<Venta> findSuccessfulVentasByUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Buscar ventas fallidas por usuario
     */
    @Query("SELECT v FROM Venta v WHERE v.usuario.id = :usuarioId AND v.resultado = false ORDER BY v.fechaVenta DESC")
    List<Venta> findFailedVentasByUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Buscar ventas pendientes de confirmación con cátedra
     */
    @Query("SELECT v FROM Venta v WHERE v.resultado = true AND v.confirmadaCatedra = false ORDER BY v.fechaVenta ASC")
    List<Venta> findPendingConfirmation();

    /**
     * Buscar ventas no confirmadas con reintentos limitados
     */
    @Query("SELECT v FROM Venta v WHERE v.resultado = true AND v.confirmadaCatedra = false AND v.intentosSincronizacion < :maxIntentos ORDER BY v.fechaVenta ASC")
    List<Venta> findVentasForRetry(@Param("maxIntentos") Integer maxIntentos);

    /**
     * Buscar ventas confirmadas con cátedra
     */
    @Query("SELECT v FROM Venta v WHERE v.confirmadaCatedra = true ORDER BY v.fechaVenta DESC")
    List<Venta> findConfirmedVentas();

    /**
     * Buscar ventas en un rango de fechas
     */
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin ORDER BY v.fechaVenta DESC")
    List<Venta> findVentasBetweenDates(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    /**
     * Buscar ventas por usuario y evento
     */
    @Query("SELECT v FROM Venta v WHERE v.usuario = :usuario AND v.evento = :evento ORDER BY v.fechaVenta DESC")
    List<Venta> findByUsuarioAndEvento(
            @Param("usuario") Usuario usuario,
            @Param("evento") Evento evento
    );

    /**
     * Buscar ventas exitosas por evento
     */
    @Query("SELECT v FROM Venta v WHERE v.evento = :evento AND v.resultado = true ORDER BY v.fechaVenta DESC")
    List<Venta> findSuccessfulVentasByEvento(@Param("evento") Evento evento);

    /**
     * Contar ventas exitosas por evento
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.evento.id = :eventoId AND v.resultado = true")
    Long countSuccessfulVentasByEvento(@Param("eventoId") Long eventoId);

    /**
     * Contar asientos vendidos por evento
     */
    @Query("SELECT SUM(SIZE(v.asientos)) FROM Venta v WHERE v.evento.id = :eventoId AND v.resultado = true")
    Long countAsientosVendidosByEvento(@Param("eventoId") Long eventoId);

    /**
     * Buscar ventas con asientos (eager fetch para evitar N+1)
     */
    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.asientos WHERE v.usuario.id = :usuarioId ORDER BY v.fechaVenta DESC")
    List<Venta> findByUsuarioIdWithAsientos(@Param("usuarioId") Long usuarioId);

    /**
     * Verificar si existe venta por ID externo
     */
    Boolean existsByIdExterno(Long idExterno);

    /**
     * Calcular ingresos totales por evento
     */
    @Query("SELECT SUM(v.precioTotal) FROM Venta v WHERE v.evento.id = :eventoId AND v.resultado = true")
    Double calculateTotalRevenueByEvento(@Param("eventoId") Long eventoId);
}

