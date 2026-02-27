package com.eventos.backend.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas", indexes = {
    @Index(name = "idx_ventas_usuario", columnList = "usuario_id"),
    @Index(name = "idx_ventas_evento", columnList = "evento_id"),
    @Index(name = "idx_ventas_confirmada", columnList = "confirmada_catedra")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_externo")
    private Long idExterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Column(nullable = false)
    private Boolean resultado;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "confirmada_catedra")
    @Builder.Default
    private Boolean confirmadaCatedra = false;

    @Column(name = "intentos_sincronizacion")
    @Builder.Default
    private Integer intentosSincronizacion = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AsientoVenta> asientos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fechaVenta == null) {
            fechaVenta = LocalDateTime.now();
        }
    }

    /**
     * Método helper para agregar asientos manteniendo la bidireccionalidad
     */
    public void addAsiento(AsientoVenta asiento) {
        asientos.add(asiento);
        asiento.setVenta(this);
    }

    /**
     * Método helper para remover asientos manteniendo la bidireccionalidad
     */
    public void removeAsiento(AsientoVenta asiento) {
        asientos.remove(asiento);
        asiento.setVenta(null);
    }
}

