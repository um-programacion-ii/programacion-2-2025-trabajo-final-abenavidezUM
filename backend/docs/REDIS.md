# Redis - Documentación de Uso

## Configuración

Redis está configurado para almacenar sesiones de usuario y datos temporales.

### Conexión

La conexión a Redis se configura en `application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # opcional
```

### Serialización

- **Keys**: StringRedisSerializer (texto plano)
- **Values**: GenericJackson2JsonRedisSerializer (JSON)

Esto permite almacenar objetos Java que se serializan automáticamente a JSON.

## RedisService

Servicio que proporciona operaciones básicas con Redis.

### Operaciones Básicas

#### Guardar con TTL por defecto (30 minutos)

```java
@Autowired
private RedisService redisService;

redisService.save("user:session:123", sessionData);
```

#### Guardar con TTL personalizado

```java
// Guardar por 5 minutos
redisService.save("temp:data", data, 5, TimeUnit.MINUTES);

// Guardar por 1 hora
redisService.save("cache:eventos", eventos, 1, TimeUnit.HOURS);
```

#### Obtener valor

```java
Object value = redisService.get("user:session:123");

// Con cast automático
SessionData session = redisService.get("user:session:123", SessionData.class);
```

#### Verificar existencia

```java
boolean exists = redisService.exists("user:session:123");
```

#### Eliminar

```java
redisService.delete("user:session:123");
```

### Operaciones Avanzadas

#### Guardar sin expiración

```java
redisService.savePermanent("config:app", configData);
```

#### Actualizar TTL

```java
// Renovar con TTL por defecto (30 min)
redisService.renewExpiration("user:session:123");

// Actualizar con TTL personalizado
redisService.updateExpiration("temp:data", 10, TimeUnit.MINUTES);
```

#### Obtener tiempo de expiración

```java
Long segundos = redisService.getExpiration("user:session:123");
// Retorna:
// > 0: segundos restantes
// -1: key existe pero sin expiración
// -2: key no existe
```

#### Contadores

```java
// Incrementar
Long count = redisService.increment("visits:counter");

// Decrementar
Long count = redisService.decrement("visits:counter");
```

## Patrones de Keys

Para mantener organizado Redis, usar prefijos descriptivos:

- `auth:token:{userId}` - Tokens JWT
- `session:user:{userId}` - Sesiones de usuario
- `session:compra:{userId}` - Sesión de compra
- `cache:evento:{eventoId}` - Cache de evento
- `cache:eventos:lista` - Cache de lista de eventos
- `lock:{resource}` - Locks distribuidos
- `counter:{name}` - Contadores

## TTL Recomendados

| Tipo de Dato | TTL | Razón |
|--------------|-----|-------|
| Token JWT | 1 hora | Balance entre seguridad y UX |
| Sesión de compra | 30 minutos | Tiempo razonable para completar compra |
| Cache de eventos | 5 minutos | Datos que cambian frecuentemente |
| Bloqueo de asientos | 5 minutos | Según requerimientos |
| Intentos de login | 15 minutos | Prevenir brute force |

## Ejemplo: Sesión de Compra

```java
@Service
public class SesionCompraService {
    
    @Autowired
    private RedisService redisService;
    
    private static final String SESSION_KEY_PREFIX = "session:compra:";
    private static final long SESSION_TTL_MINUTES = 30;
    
    public void crearSesion(Long userId, SesionCompraDTO sesion) {
        String key = SESSION_KEY_PREFIX + userId;
        redisService.save(key, sesion, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
    }
    
    public SesionCompraDTO obtenerSesion(Long userId) {
        String key = SESSION_KEY_PREFIX + userId;
        return redisService.get(key, SesionCompraDTO.class);
    }
    
    public void actualizarSesion(Long userId, SesionCompraDTO sesion) {
        String key = SESSION_KEY_PREFIX + userId;
        // Renueva automáticamente el TTL
        redisService.save(key, sesion, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
    }
    
    public void limpiarSesion(Long userId) {
        String key = SESSION_KEY_PREFIX + userId;
        redisService.delete(key);
    }
    
    public boolean existeSesion(Long userId) {
        String key = SESSION_KEY_PREFIX + userId;
        return redisService.exists(key);
    }
}
```

## Monitoreo

### Ver todas las keys

```bash
redis-cli
> KEYS *
```

### Ver keys con patrón

```bash
> KEYS session:*
> KEYS cache:*
```

### Ver valor de una key

```bash
> GET "session:user:123"
```

### Ver TTL de una key

```bash
> TTL "session:user:123"
```

### Eliminar una key

```bash
> DEL "session:user:123"
```

### Limpiar todo (SOLO DESARROLLO)

```bash
> FLUSHALL
```

## Mejores Prácticas

1. **Siempre usar TTL**: Evita acumulación de datos obsoletos
2. **Keys descriptivas**: Usar prefijos para organizar
3. **No almacenar datos sensibles sin encriptar**: Redis no encripta por defecto
4. **Manejo de errores**: RedisService ya captura excepciones
5. **Testing**: Usar embedded Redis o mockear en tests
6. **Monitoreo**: Vigilar uso de memoria en producción

## Troubleshooting

### Redis no conecta

```bash
# Verificar que Redis está corriendo
docker ps | grep redis

# Ver logs de Redis
docker logs eventos_redis_local
```

### Key no se encuentra

- Verificar que el TTL no expiró
- Verificar prefijo correcto de la key
- Usar `redis-cli KEYS *` para ver todas las keys

### Problemas de serialización

- Verificar que la clase sea serializable
- Asegurar que tenga constructor sin parámetros
- Revisar logs para ver stack trace

## Configuración en Producción

En producción, configurar:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      timeout: 60000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
```

Pool de conexiones optimizado para mayor concurrencia.

