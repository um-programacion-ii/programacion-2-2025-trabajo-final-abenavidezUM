package com.eventos.backend.domain;

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
@Table(name = "eventos", indexes = {
    @Index(name = "idx_eventos_fecha", columnList = "fecha"),
    @Index(name = "idx_eventos_activo", columnList = "activo"),
    @Index(name = "idx_eventos_tipo", columnList = "tipo_evento_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_externo", unique = true)
    private Long idExterno;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String resumen;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 500)
    private String direccion;

    @Column(length = 1000)
    private String imagen;

    @Column(name = "fila_asientos", nullable = false)
    private Integer filaAsientos;

    @Column(name = "columna_asientos", nullable = false)
    private Integer columnaAsientos;

    @Column(name = "precio_entrada", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioEntrada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_evento_id")
    private TipoEvento tipoEvento;

    @Column(name = "ultima_sincronizacion")
    private LocalDateTime ultimaSincronizacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Integrante> integrantes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Método helper para agregar integrantes manteniendo la bidireccionalidad
     */
    public void addIntegrante(Integrante integrante) {
        integrantes.add(integrante);
        integrante.setEvento(this);
    }

    /**
     * Método helper para remover integrantes manteniendo la bidireccionalidad
     */
    public void removeIntegrante(Integrante integrante) {
        integrantes.remove(integrante);
        integrante.setEvento(null);
    }
}

