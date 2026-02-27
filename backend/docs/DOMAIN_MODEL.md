# Modelo de Dominio

## Diagrama de Entidades

```
TipoEvento
   ↓ (1:N)
Evento ←────┐
   ↓ (1:N)  │
Integrante  │
            │
Usuario     │ (N:M a través de Venta)
   ↓ (1:N)  │
Venta ──────┘
   ↓ (1:N)
AsientoVenta
```

## Entidades

### Usuario

**Tabla:** `usuarios`

**Descripción:** Representa los usuarios del sistema que pueden comprar entradas.

**Campos:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `username` (VARCHAR(50), UNIQUE, NOT NULL)
- `password` (VARCHAR(255), NOT NULL) - Hash BCrypt
- `first_name` (VARCHAR(100), NOT NULL)
- `last_name` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `enabled` (BOOLEAN, DEFAULT true)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Relaciones:**
- `@OneToMany` con `Venta` (un usuario puede tener muchas ventas)

**Notas:**
- Password se almacena hasheado con BCrypt
- Timestamps se actualizan automáticamente con @PrePersist y @PreUpdate

### TipoEvento

**Tabla:** `tipos_evento`

**Descripción:** Catálogo de tipos de eventos (Conferencia, Obra de teatro, Curso, etc.)

**Campos:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `nombre` (VARCHAR(100), NOT NULL)
- `descripcion` (TEXT)

**Relaciones:**
- `@OneToMany` con `Evento` (un tipo puede tener muchos eventos)

**Notas:**
- Datos maestros, precargados en la migración inicial
- Ejemplos: Conferencia, Obra de teatro, Curso, Concierto, Otro

### Evento

**Tabla:** `eventos`

**Descripción:** Representa los eventos disponibles para compra de entradas.

**Campos:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `id_externo` (BIGINT, UNIQUE) - ID del servicio de cátedra
- `titulo` (VARCHAR(255), NOT NULL)
- `resumen` (TEXT)
- `descripcion` (TEXT)
- `fecha` (TIMESTAMP, NOT NULL)
- `direccion` (VARCHAR(500))
- `imagen` (VARCHAR(1000)) - URL de la imagen
- `fila_asientos` (INTEGER, NOT NULL)
- `columna_asientos` (INTEGER, NOT NULL)
- `precio_entrada` (DECIMAL(10,2), NOT NULL)
- `tipo_evento_id` (BIGINT, FK)
- `ultima_sincronizacion` (TIMESTAMP)
- `activo` (BOOLEAN, DEFAULT true)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Relaciones:**
- `@ManyToOne` con `TipoEvento` (muchos eventos de un tipo)
- `@OneToMany` con `Integrante` (un evento tiene muchos integrantes)
- `@OneToMany` con `Venta` (un evento puede tener muchas ventas)

**Índices:**
- `idx_eventos_fecha` en `fecha` - búsquedas por fecha
- `idx_eventos_activo` en `activo` - filtrar eventos activos
- `idx_eventos_tipo` en `tipo_evento_id` - filtrar por tipo

**Notas:**
- `id_externo` se usa para sincronización con servicio de cátedra
- `activo` permite soft delete
- `fila_asientos` y `columna_asientos` definen la disposición de asientos
- `ultima_sincronizacion` rastrea sincronización con cátedra

### Integrante

**Tabla:** `integrantes`

**Descripción:** Presentadores o integrantes de un evento.

**Campos:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `evento_id` (BIGINT, FK, NOT NULL)
- `nombre` (VARCHAR(100), NOT NULL)
- `apellido` (VARCHAR(100), NOT NULL)
- `identificacion` (VARCHAR(50))

**Relaciones:**
- `@ManyToOne` con `Evento` (muchos integrantes de un evento)

**Índices:**
- `idx_integrantes_evento` en `evento_id`

**Cascade:**
- `CascadeType.ALL` - al eliminar evento se eliminan integrantes
- `orphanRemoval = true` - integrantes huérfanos se eliminan

**Notas:**
- Se eliminan automáticamente al eliminar el evento (ON DELETE CASCADE)

### Venta

**Tabla:** `ventas`

**Descripción:** Registro de ventas de entradas realizadas por usuarios.

**Campos:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `id_externo` (BIGINT) - ID de la venta en servicio de cátedra
- `usuario_id` (BIGINT, FK)
- `evento_id` (BIGINT, FK)
- `fecha_venta` (TIMESTAMP, NOT NULL)
- `precio_total` (DECIMAL(10,2), NOT NULL)
- `resultado` (BOOLEAN, NOT NULL) - éxito/fallo de la venta
- `descripcion` (TEXT)
- `confirmada_catedra` (BOOLEAN, DEFAULT false)
- `intentos_sincronizacion` (INTEGER, DEFAULT 0)
- `created_at` (TIMESTAMP)

**Relaciones:**
- `@ManyToOne` con `Usuario` (muchas ventas de un usuario)
- `@ManyToOne` con `Evento` (muchas ventas de un evento)
- `@OneToMany` con `AsientoVenta` (una venta tiene muchos asientos)

**Índices:**
- `idx_ventas_usuario` en `usuario_id` - ventas por usuario
- `idx_ventas_evento` en `evento_id` - ventas por evento
- `idx_ventas_confirmada` en `confirmada_catedra` - ventas pendientes de confirmación

**Notas:**
- `resultado` indica si la venta fue exitosa
- `confirmada_catedra` indica si fue sincronizada con el servicio externo
- `intentos_sincronizacion` rastrea reintentos de sincronización

### AsientoVenta

**Tabla:** `asientos_venta`

**Descripción:** Detalle de los asientos vendidos en cada venta.

**Campos:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `venta_id` (BIGINT, FK, NOT NULL)
- `fila` (INTEGER, NOT NULL)
- `columna` (INTEGER, NOT NULL)
- `nombre_persona` (VARCHAR(200), NOT NULL)
- `estado` (VARCHAR(50))

**Relaciones:**
- `@ManyToOne` con `Venta` (muchos asientos de una venta)

**Índices:**
- `idx_asientos_venta` en `venta_id`

**Cascade:**
- `CascadeType.ALL` - al eliminar venta se eliminan asientos
- `orphanRemoval = true` - asientos huérfanos se eliminan

**Notas:**
- `fila` y `columna` identifican la posición del asiento
- `nombre_persona` es quien ocupará el asiento
- Se eliminan automáticamente al eliminar la venta (ON DELETE CASCADE)

## Relaciones

### One-to-Many (1:N)

**TipoEvento → Evento:**
```java
// En TipoEvento
@OneToMany(mappedBy = "tipoEvento")
private List<Evento> eventos;

// En Evento
@ManyToOne
@JoinColumn(name = "tipo_evento_id")
private TipoEvento tipoEvento;
```

**Evento → Integrante:**
```java
// En Evento
@OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Integrante> integrantes;

// En Integrante
@ManyToOne
@JoinColumn(name = "evento_id")
private Evento evento;
```

**Usuario → Venta:**
```java
// En Usuario
@OneToMany(mappedBy = "usuario")
private List<Venta> ventas;

// En Venta
@ManyToOne
@JoinColumn(name = "usuario_id")
private Usuario usuario;
```

**Evento → Venta:**
```java
// En Evento
@OneToMany(mappedBy = "evento")
private List<Venta> ventas;

// En Venta
@ManyToOne
@JoinColumn(name = "evento_id")
private Evento evento;
```

**Venta → AsientoVenta:**
```java
// En Venta
@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
private List<AsientoVenta> asientos;

// En AsientoVenta
@ManyToOne
@JoinColumn(name = "venta_id")
private Venta venta;
```

## Estrategias de Fetch

### FetchType.LAZY

Usado en todas las relaciones `@ManyToOne` y `@OneToMany` para evitar el problema N+1.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "evento_id")
private Evento evento;
```

**Ventajas:**
- Carga datos solo cuando se acceden
- Mejor performance inicial
- Reduce queries innecesarias

**Consideración:**
- Usar `@EntityGraph` o `JOIN FETCH` en queries cuando se necesiten las relaciones

## Cascade Types

### CascadeType.ALL

Usado en relaciones padre-hijo donde el hijo no tiene sentido sin el padre:

```java
@OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Integrante> integrantes;
```

**Operaciones que se propagan:**
- PERSIST: guardar integrantes al guardar evento
- MERGE: actualizar integrantes al actualizar evento
- REMOVE: eliminar integrantes al eliminar evento
- REFRESH: refrescar integrantes al refrescar evento
- DETACH: desconectar integrantes al desconectar evento

### orphanRemoval = true

Elimina entidades huérfanas cuando se remueven de la colección:

```java
evento.getIntegrantes().remove(integrante);
// integrante se elimina de la BD automáticamente
```

## Métodos Helper

Para mantener la bidireccionalidad de las relaciones:

```java
public class Evento {
    public void addIntegrante(Integrante integrante) {
        integrantes.add(integrante);
        integrante.setEvento(this);
    }

    public void removeIntegrante(Integrante integrante) {
        integrantes.remove(integrante);
        integrante.setEvento(null);
    }
}
```

**Uso:**
```java
Evento evento = new Evento();
Integrante integrante = new Integrante();
evento.addIntegrante(integrante); // Mantiene ambos lados sincronizados
```

## Anotaciones JPA Importantes

### @Entity
Marca la clase como entidad JPA.

### @Table
Especifica el nombre de la tabla y configuración adicional (índices, constraints).

### @Id
Marca el campo como clave primaria.

### @GeneratedValue
Estrategia de generación de IDs (IDENTITY para auto-increment en PostgreSQL).

### @Column
Configura detalles de la columna (nullable, unique, length, precision).

### @ManyToOne / @OneToMany
Define relaciones entre entidades.

### @JoinColumn
Especifica la columna de foreign key.

### @Index
Define índices para mejorar performance de queries.

### @PrePersist / @PreUpdate
Callbacks que se ejecutan antes de INSERT/UPDATE.

### @Builder.Default
Define valores por defecto en el patrón Builder de Lombok.

## Normalización

El modelo está en **Tercera Forma Normal (3FN)**:

1. **1FN**: Todos los campos son atómicos
2. **2FN**: No hay dependencias parciales (todas las columnas dependen de la clave completa)
3. **3FN**: No hay dependencias transitivas

## Soft Delete

La entidad `Evento` usa soft delete con el campo `activo`:

```java
@Column(nullable = false)
private Boolean activo = true;
```

**Ventajas:**
- No se pierden datos históricos
- Se pueden reactivar eventos
- Las ventas mantienen referencia al evento

**Query para eventos activos:**
```java
@Query("SELECT e FROM Evento e WHERE e.activo = true")
List<Evento> findAllActive();
```

## Timestamps Automáticos

Todas las entidades principales tienen timestamps:

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

## Precisión Decimal

Para precios se usa `DECIMAL(10,2)`:

```java
@Column(precision = 10, scale = 2)
private BigDecimal precio;
```

- **precision = 10**: total de dígitos
- **scale = 2**: dígitos después del punto decimal
- Rango: -99,999,999.99 a 99,999,999.99

## Mejores Prácticas

1. **Usar FetchType.LAZY** para todas las relaciones
2. **Definir índices** en columnas de búsqueda frecuente
3. **Usar cascade apropiado** según la relación padre-hijo
4. **Mantener bidireccionalidad** con métodos helper
5. **Usar soft delete** cuando sea apropiado
6. **Timestamps automáticos** para auditoría
7. **Usar @Builder** de Lombok para crear instancias
8. **Validar constraints** en nivel de BD y aplicación

## Testing

### Ejemplo de Test

```java
@DataJpaTest
class EventoRepositoryTest {
    
    @Autowired
    private EventoRepository eventoRepository;
    
    @Test
    void testSaveEvento() {
        // Given
        TipoEvento tipo = new TipoEvento();
        tipo.setNombre("Conferencia");
        
        Evento evento = Evento.builder()
            .titulo("Java Summit 2025")
            .fecha(LocalDateTime.now().plusDays(30))
            .filaAsientos(10)
            .columnaAsientos(10)
            .precioEntrada(new BigDecimal("100.00"))
            .tipoEvento(tipo)
            .build();
        
        // When
        Evento saved = eventoRepository.save(evento);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("Java Summit 2025", saved.getTitulo());
    }
}
```

