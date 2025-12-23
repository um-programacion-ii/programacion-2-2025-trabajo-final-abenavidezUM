package com.eventos.backend.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asientos_venta", indexes = {
    @Index(name = "idx_asientos_venta", columnList = "venta_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsientoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Column(nullable = false)
    private Integer fila;

    @Column(nullable = false)
    private Integer columna;

    @Column(name = "nombre_persona", nullable = false, length = 200)
    private String nombrePersona;

    @Column(length = 50)
    private String estado;
}

