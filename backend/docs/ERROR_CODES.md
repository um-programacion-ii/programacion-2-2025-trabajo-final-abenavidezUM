# Códigos de Error y Manejo de Excepciones

## Formato de Respuesta de Error

Todas las respuestas de error siguen un formato JSON consistente:

```json
{
  "timestamp": "2025-11-10 15:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado con username: 'juan'",
  "path": "/api/usuarios/juan"
}
```

Para errores de validación de campos:

```json
{
  "timestamp": "2025-11-10 15:30:45",
  "status": 400,
  "error": "Validation Error",
  "message": "Errores de validación en los campos",
  "path": "/api/auth/register",
  "fieldErrors": [
    {
      "field": "username",
      "message": "El username es obligatorio"
    },
    {
      "field": "email",
      "message": "El email debe ser válido"
    }
  ]
}
```

## Códigos HTTP

### 400 - Bad Request

**Cuándo se usa:**
- Solicitud malformada
- Parámetros inválidos
- Errores de validación
- Argumentos ilegales

**Excepciones:**
- `BadRequestException`
- `MethodArgumentNotValidException`
- `IllegalArgumentException`

**Ejemplos:**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El formato de fecha es inválido"
}
```

```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Errores de validación en los campos",
  "fieldErrors": [
    {"field": "email", "message": "El email debe ser válido"},
    {"field": "password", "message": "El password debe tener al menos 6 caracteres"}
  ]
}
```

### 401 - Unauthorized

**Cuándo se usa:**
- Credenciales inválidas
- Token JWT expirado
- Token JWT inválido
- Falta de autenticación

**Excepciones:**
- `UnauthorizedException`
- `AuthenticationException`
- `BadCredentialsException`

**Ejemplos:**

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales inválidas"
}
```

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT expirado"
}
```

### 403 - Forbidden

**Cuándo se usa:**
- Usuario autenticado pero sin permisos
- Intento de acceder a recurso prohibido
- Roles insuficientes

**Excepciones:**
- `ForbiddenException`
- `AccessDeniedException`

**Ejemplos:**

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "No tiene permisos para acceder a este recurso"
}
```

### 404 - Not Found

**Cuándo se usa:**
- Recurso no existe
- Entidad no encontrada en base de datos
- Endpoint no existe

**Excepciones:**
- `ResourceNotFoundException`

**Ejemplos:**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Evento no encontrado con id: '123'"
}
```

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado con username: 'juan'"
}
```

### 409 - Conflict

**Cuándo se usa:**
- Duplicación de recursos únicos
- Conflicto de estado
- Violación de constraints

**Excepciones:**
- `ConflictException`

**Ejemplos:**

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "El username ya está en uso"
}
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "El email ya está en uso"
}
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Los asientos seleccionados ya no están disponibles"
}
```

### 500 - Internal Server Error

**Cuándo se usa:**
- Error inesperado del servidor
- Excepciones no capturadas
- Errores de conexión a BD/Redis
- Bugs en el código

**Excepciones:**
- `Exception` (catch-all)

**Ejemplos:**

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "Ha ocurrido un error interno en el servidor"
}
```

**Nota:** Los detalles técnicos NO se exponen al cliente por seguridad.

## Excepciones Personalizadas

### ResourceNotFoundException

**Uso:**
```java
throw new ResourceNotFoundException("Evento", "id", eventoId);
// Resultado: "Evento no encontrado con id: '123'"

throw new ResourceNotFoundException("Usuario no encontrado");
// Resultado: "Usuario no encontrado"
```

**Casos de uso:**
- Entidad no existe en BD
- Recurso solicitado no encontrado

### BadRequestException

**Uso:**
```java
throw new BadRequestException("El formato de fecha es inválido");
```

**Casos de uso:**
- Parámetros inválidos
- Formato incorrecto
- Datos malformados

### UnauthorizedException

**Uso:**
```java
throw new UnauthorizedException("Token expirado");
```

**Casos de uso:**
- Token inválido
- Sesión expirada
- Falta de credenciales

### ForbiddenException

**Uso:**
```java
throw new ForbiddenException("No puede modificar este evento");
```

**Casos de uso:**
- Falta de permisos
- Intento de acceder a recurso prohibido
- Roles insuficientes

### ConflictException

**Uso:**
```java
throw new ConflictException("El email ya está registrado");
```

**Casos de uso:**
- Duplicación de recursos únicos
- Conflictos de estado
- Violación de constraints

## Validaciones con Bean Validation

### Anotaciones Comunes

**@NotBlank:**
```java
@NotBlank(message = "El username es obligatorio")
private String username;
```

**@NotNull:**
```java
@NotNull(message = "La fecha no puede ser null")
private LocalDateTime fecha;
```

**@Email:**
```java
@Email(message = "El email debe ser válido")
private String email;
```

**@Size:**
```java
@Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
private String username;

@Size(min = 6, message = "El password debe tener al menos 6 caracteres")
private String password;
```

**@Min / @Max:**
```java
@Min(value = 0, message = "El precio no puede ser negativo")
private BigDecimal precio;

@Max(value = 100, message = "La fila no puede ser mayor a 100")
private Integer fila;
```

**@Pattern:**
```java
@Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos")
private String dni;
```

**@Future / @Past:**
```java
@Future(message = "La fecha debe ser futura")
private LocalDateTime fecha;
```

### Response de Validación

Cuando falla una validación con `@Valid`:

```json
{
  "timestamp": "2025-11-10 15:30:45",
  "status": 400,
  "error": "Validation Error",
  "message": "Errores de validación en los campos",
  "path": "/api/auth/register",
  "fieldErrors": [
    {
      "field": "username",
      "message": "El username debe tener entre 3 y 50 caracteres"
    },
    {
      "field": "email",
      "message": "El email debe ser válido"
    },
    {
      "field": "password",
      "message": "El password debe tener al menos 6 caracteres"
    }
  ]
}
```

## Logging de Errores

### Niveles de Log

**WARN (log.warn):**
- Errores esperados/manejados
- 400, 401, 403, 404, 409
- No requieren intervención inmediata

```java
log.warn("Resource not found: Evento no encontrado con id: 123");
log.warn("Validation failed: 3 errors");
log.warn("Unauthorized access: Token expirado");
```

**ERROR (log.error):**
- Errores inesperados
- 500 Internal Server Error
- Requieren investigación

```java
log.error("Internal server error: ", ex);
```

### Formato de Logs

```
2025-11-10 15:30:45 - WARN  - Resource not found: Usuario no encontrado con username: 'juan'
2025-11-10 15:31:12 - WARN  - Validation failed: 2 errors
2025-11-10 15:32:05 - ERROR - Internal server error: 
java.lang.NullPointerException: ...
```

## Manejo en el Cliente

### JavaScript/TypeScript

```javascript
try {
  const response = await fetch('/api/eventos/123', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const error = await response.json();
    
    switch (error.status) {
      case 400:
        // Mostrar errores de validación
        if (error.fieldErrors) {
          error.fieldErrors.forEach(fe => {
            console.error(`${fe.field}: ${fe.message}`);
          });
        } else {
          console.error(error.message);
        }
        break;
      
      case 401:
        // Redirigir a login
        window.location.href = '/login';
        break;
      
      case 403:
        // Mostrar mensaje de permisos
        alert('No tiene permisos para esta acción');
        break;
      
      case 404:
        // Mostrar "no encontrado"
        alert(error.message);
        break;
      
      case 409:
        // Mostrar conflicto
        alert(error.message);
        break;
      
      case 500:
        // Error del servidor
        alert('Error del servidor. Por favor intente más tarde.');
        break;
    }
  }
  
  const data = await response.json();
  // Procesar respuesta exitosa
  
} catch (error) {
  // Error de red
  console.error('Network error:', error);
}
```

## Mejores Prácticas

1. **Usar excepciones específicas:**
   ```java
   // ❌ Malo
   throw new RuntimeException("Usuario no encontrado");
   
   // ✅ Bueno
   throw new ResourceNotFoundException("Usuario", "id", userId);
   ```

2. **Mensajes descriptivos:**
   ```java
   // ❌ Malo
   throw new BadRequestException("Error");
   
   // ✅ Bueno
   throw new BadRequestException("El formato de fecha debe ser yyyy-MM-dd");
   ```

3. **No exponer detalles técnicos:**
   ```java
   // ❌ Malo (en producción)
   throw new BadRequestException("SQLException: " + ex.getMessage());
   
   // ✅ Bueno
   log.error("Database error", ex);
   throw new BadRequestException("Error al procesar la solicitud");
   ```

4. **Validar en el lugar apropiado:**
   - Controller: validaciones de formato con `@Valid`
   - Service: validaciones de negocio con excepciones personalizadas

5. **Log apropiado:**
   - `log.warn()` para errores esperados (404, 409)
   - `log.error()` para errores inesperados (500)

## Testing

### Ejemplo de Test

```java
@Test
void testResourceNotFoundException() {
    // Given
    Long eventoId = 999L;
    when(eventoRepository.findById(eventoId))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> {
        eventoService.getEvento(eventoId);
    });
}
```

### Verificar Respuesta de Error

```java
@Test
void testValidationError() throws Exception {
    RegisterRequestDTO request = new RegisterRequestDTO();
    request.setUsername(""); // inválido
    
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Validation Error"))
        .andExpect(jsonPath("$.fieldErrors").isArray());
}
```

