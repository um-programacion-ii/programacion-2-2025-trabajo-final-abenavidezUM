# Arquitectura Hexagonal Implementada

## 📐 Estructura del Proyecto

```
backend/src/main/java/com/eventos/backend/
├── domain/                           # CAPA DE DOMINIO (núcleo del negocio)
│   ├── model/                        # Entidades del dominio
│   │   ├── Usuario.java
│   │   ├── Evento.java
│   │   ├── Venta.java
│   │   ├── AsientoVenta.java
│   │   ├── TipoEvento.java
│   │   └── Integrante.java
│   ├── ports/                        # Interfaces que definen contratos
│   │   ├── input/                    # Casos de uso (expuestos por el dominio)
│   │   │   ├── AutenticarUsuarioUseCase.java
│   │   │   ├── GestionarEventosUseCase.java
│   │   │   ├── GestionarSesionCompraUseCase.java
│   │   │   ├── GestionarAsientosUseCase.java
│   │   │   └── RealizarVentaUseCase.java
│   │   └── output/                   # Dependencias externas (necesitadas por el dominio)
│   │       ├── EventoRepositoryPort.java
│   │       ├── UsuarioRepositoryPort.java
│   │       ├── VentaRepositoryPort.java
│   │       ├── CatedraApiPort.java
│   │       ├── ProxyApiPort.java
│   │       └── RedisCachePort.java
│   └── exception/                    # Excepciones del dominio
│       ├── BadRequestException.java
│       ├── ResourceNotFoundException.java
│       ├── ConflictException.java
│       ├── ForbiddenException.java
│       └── UnauthorizedException.java
│
├── application/                      # CAPA DE APLICACIÓN (orquestación)
│   └── service/                      # Implementación de casos de uso
│       ├── AuthServiceImpl.java      # Implementa AutenticarUsuarioUseCase
│       ├── EventoServiceImpl.java
│       ├── SesionCompraServiceImpl.java
│       ├── AsientoServiceImpl.java
│       └── VentaServiceImpl.java
│
└── infrastructure/                   # CAPA DE INFRAESTRUCTURA (detalles técnicos)
    ├── adapter/
    │   ├── input/                    # Adaptadores de entrada
    │   │   ├── rest/                 # Controllers REST
    │   │   │   ├── AuthController.java
    │   │   │   ├── EventoController.java
    │   │   │   ├── SesionController.java
    │   │   │   ├── AsientoController.java
    │   │   │   ├── VentaController.java
    │   │   │   ├── UsuarioController.java
    │   │   │   ├── AdminController.java
    │   │   │   ├── NotificacionController.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   └── kafka/                # Listeners de Kafka
    │   │       └── EventoKafkaListener.java
    │   └── output/                   # Adaptadores de salida
    │       ├── persistence/          # Implementaciones de persistencia
    │       │   ├── UsuarioRepositoryAdapter.java
    │       │   ├── EventoRepositoryAdapter.java
    │       │   ├── VentaRepositoryAdapter.java
    │       │   └── repository/       # Spring Data JPA Repositories
    │       │       ├── UsuarioRepository.java
    │       │       ├── EventoRepository.java
    │       │       ├── VentaRepository.java
    │       │       ├── AsientoVentaRepository.java
    │       │       ├── TipoEventoRepository.java
    │       │       └── IntegranteRepository.java
    │       └── external/             # Implementaciones de APIs externas
    │           ├── CatedraApiAdapter.java
    │           ├── ProxyApiAdapter.java
    │           ├── RedisCacheAdapter.java
    │           └── service/          # Clientes HTTP
    │               ├── CatedraApiClient.java
    │               ├── ProxyClient.java
    │               ├── RedisService.java
    │               ├── EventoSyncService.java
    │               └── NotificacionService.java
    ├── config/                       # Configuraciones de Spring
    │   ├── SecurityConfig.java
    │   ├── RedisConfig.java
    │   ├── RestTemplateConfig.java
    │   ├── ProxyRestTemplateConfig.java
    │   ├── KafkaConsumerConfig.java
    │   └── OpenApiConfig.java
    ├── security/                     # Componentes de seguridad
    │   ├── JwtTokenProvider.java
    │   ├── JwtAuthenticationFilter.java
    │   └── CustomUserDetailsService.java
    └── mapper/                       # Mappers DTO ↔ Domain
        ├── EventoMapper.java
        ├── VentaMapper.java
        ├── UsuarioMapper.java
        ├── CatedraEventoMapper.java
        ├── AsientoVentaMapper.java
        ├── IntegranteMapper.java
        └── TipoEventoMapper.java
```

## 🎯 Principios de Arquitectura Hexagonal

### 1. **Inversión de Dependencias**
- El dominio NO depende de la infraestructura
- El dominio define interfaces (puertos)
- La infraestructura implementa esos puertos (adaptadores)

### 2. **Separación de Capas**

#### **Capa de Dominio** (Núcleo del Negocio)
- **Responsabilidad**: Contiene la lógica de negocio pura
- **Características**:
  - No depende de frameworks
  - No depende de bases de datos
  - No depende de APIs externas
  - Solo contiene: entidades, interfaces (puertos), excepciones

#### **Capa de Aplicación** (Orquestación)
- **Responsabilidad**: Implementa los casos de uso
- **Características**:
  - Orquesta llamadas al dominio
  - Usa puertos del dominio
  - Coordina transacciones
  - Implementa lógica de aplicación (no de negocio)

#### **Capa de Infraestructura** (Detalles Técnicos)
- **Responsabilidad**: Implementa los detalles técnicos
- **Características**:
  - Adaptadores de entrada (REST, Kafka)
  - Adaptadores de salida (JPA, HTTP clients, Redis)
  - Configuraciones de frameworks
  - Mappers y utilidades

### 3. **Puertos y Adaptadores**

#### **Puertos de Entrada** (Input Ports)
Interfaces que expone el dominio para ser consumidas desde fuera:
- `AutenticarUsuarioUseCase`: Login y registro
- `GestionarEventosUseCase`: Consulta de eventos
- `GestionarSesionCompraUseCase`: Gestión de sesiones
- `GestionarAsientosUseCase`: Bloqueo de asientos
- `RealizarVentaUseCase`: Ventas

**Implementados por**: `application/service/*ServiceImpl.java`  
**Consumidos por**: `infrastructure/adapter/input/rest/*Controller.java`

#### **Puertos de Salida** (Output Ports)
Interfaces que el dominio necesita para acceder a infraestructura:
- `EventoRepositoryPort`: Persistencia de eventos
- `UsuarioRepositoryPort`: Persistencia de usuarios
- `VentaRepositoryPort`: Persistencia de ventas
- `CatedraApiPort`: Comunicación con API de Cátedra
- `ProxyApiPort`: Comunicación con Proxy
- `RedisCachePort`: Caché en Redis

**Implementados por**: `infrastructure/adapter/output/*Adapter.java`  
**Consumidos por**: `application/service/*ServiceImpl.java`

## 📊 Flujo de una Petición

```
1. HTTP Request
   ↓
2. AuthController (Infrastructure - Input Adapter)
   ↓
3. AuthServiceImpl (Application - Use Case Implementation)
   ↓
4. UsuarioRepositoryPort (Domain - Output Port Interface)
   ↓
5. UsuarioRepositoryAdapter (Infrastructure - Output Adapter)
   ↓
6. UsuarioRepository (Infrastructure - Spring Data JPA)
   ↓
7. Database
```

## 🎓 Beneficios de esta Arquitectura

1. **Testabilidad**: Fácil mockear puertos para tests unitarios
2. **Mantenibilidad**: Cambios en infraestructura no afectan dominio
3. **Flexibilidad**: Cambiar de JPA a MongoDB solo requiere cambiar adaptadores
4. **Claridad**: Cada capa tiene una responsabilidad clara
5. **Independencia**: El dominio es independiente de frameworks

## 🔄 Ejemplo Práctico: Caso de Uso de Login

### 1. Controller (Adaptador de Entrada)
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AutenticarUsuarioUseCase autenticarUseCase; // Puerto de entrada
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(autenticarUseCase.autenticar(request));
    }
}
```

### 2. Service (Implementación del Caso de Uso)
```java
@Service
public class AuthServiceImpl implements AutenticarUsuarioUseCase { // Implementa puerto
    private final UsuarioRepositoryPort usuarioRepository; // Usa puerto de salida
    private final RedisCachePort redisCache; // Usa puerto de salida
    
    @Override
    public JwtResponseDTO autenticar(LoginRequestDTO request) {
        // Lógica de autenticación usando puertos
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        // ...
    }
}
```

### 3. Adapter (Implementación del Puerto de Salida)
```java
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort { // Implementa puerto
    private final UsuarioRepository usuarioRepository; // Spring Data JPA
    
    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
}
```

## 📝 Notas para la Presentación

1. **El dominio es el núcleo**: Todas las reglas de negocio están en `domain/`
2. **Los puertos son contratos**: Interfaces que definen qué se puede hacer
3. **Los adaptadores son implementaciones**: Detalles técnicos de cómo se hace
4. **La aplicación orquesta**: Coordina el flujo entre dominio e infraestructura
5. **Inyección de dependencias de Spring**: Conecta todo automáticamente

## ✅ Estado Actual

- ✅ Estructura de carpetas hexagonal completa
- ✅ Puertos de entrada definidos (5 casos de uso)
- ✅ Puertos de salida definidos (6 interfaces)
- ✅ Adaptadores de persistencia creados (3 adaptadores)
- ✅ Adaptadores de APIs externas creados (3 adaptadores)
- ✅ Controllers movidos a infrastructure/adapter/input/rest
- ✅ Servicios refactorizados en application/service
- ⏳ Ajustes de compilación pendientes (imports, anotaciones)

## 🚀 Para Completar

1. Corregir errores de compilación menores
2. Actualizar controllers para usar puertos de entrada
3. Actualizar inyección de dependencias en configuración
4. Verificar que todos los imports sean correctos
5. Ejecutar tests de integración


