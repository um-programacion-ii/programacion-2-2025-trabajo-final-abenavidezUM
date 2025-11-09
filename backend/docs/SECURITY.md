# Seguridad y Autenticación JWT

## Arquitectura de Seguridad

La aplicación utiliza Spring Security con JWT (JSON Web Tokens) para autenticación stateless.

```
Cliente
   ↓
POST /api/auth/login (username, password)
   ↓
AuthController
   ↓
AuthService
   ↓
AuthenticationManager valida credenciales
   ↓
JwtTokenProvider genera token
   ↓
Token guardado en Redis con TTL
   ↓
Response: { token, username, email }
   ↓
Cliente guarda token (localStorage/sessionStorage)
   ↓
Requests subsiguientes incluyen: Authorization: Bearer {token}
   ↓
JwtAuthenticationFilter intercepta request
   ↓
Valida token y establece autenticación en SecurityContext
   ↓
Request procede a Controller
```

## Componentes

### 1. JwtTokenProvider

Genera y valida tokens JWT.

**Métodos principales:**
- `generateToken(Authentication)`: Genera token desde autenticación
- `generateTokenFromUsername(String)`: Genera token desde username
- `getUsernameFromToken(String)`: Extrae username del token
- `validateToken(String)`: Valida integridad y expiración
- `isTokenExpired(String)`: Verifica si expiró

**Configuración:**
```yaml
jwt:
  secret: secret-key-256-bits-minimum
  expiration: 3600000  # 1 hora en ms
```

**Estructura del Token:**
```json
{
  "sub": "username",
  "iat": 1234567890,
  "exp": 1234571490
}
```

### 2. JwtAuthenticationFilter

Filtro que intercepta requests HTTP y valida tokens.

**Flujo:**
1. Extrae token del header `Authorization: Bearer {token}`
2. Valida token usando JwtTokenProvider
3. Carga usuario desde UserDetailsService
4. Establece autenticación en SecurityContext
5. Request continúa al controller

**Ubicación en cadena de filtros:**
- Se ejecuta antes de `UsernamePasswordAuthenticationFilter`
- Si token es válido, establece autenticación
- Si no hay token o es inválido, request continúa sin autenticación

### 3. CustomUserDetailsService

Implementa `UserDetailsService` de Spring Security.

**Responsabilidades:**
- Cargar usuario desde base de datos
- Convertir entidad `Usuario` a `UserDetails`
- Proveer autoridades (roles)

**Métodos:**
- `loadUserByUsername(String)`: Carga por username
- `loadUserById(Long)`: Carga por ID

### 4. SecurityConfig

Configuración central de Spring Security.

**Rutas públicas (sin autenticación):**
- `/api/auth/**` - Login, registro
- `/api/eventos/public/**` - Listado público de eventos
- `/actuator/health` - Health check
- `/actuator/info` - Info de la app

**Rutas protegidas (requieren token):**
- Todas las demás rutas bajo `/api/**`

**Características:**
- CSRF deshabilitado (API stateless)
- CORS habilitado para desarrollo
- Sesiones deshabilitadas (stateless)
- PasswordEncoder: BCrypt

### 5. AuthService

Servicio de lógica de negocio para autenticación.

**Métodos:**

**login(LoginRequestDTO):**
- Valida credenciales con AuthenticationManager
- Genera token JWT
- Guarda token en Redis (1 hora TTL)
- Retorna token y datos de usuario

**register(RegisterRequestDTO):**
- Valida que username y email sean únicos
- Hashea password con BCrypt
- Crea usuario en BD
- Autentica automáticamente
- Retorna token

**logout(String username):**
- Elimina token de Redis
- Invalida sesión

**isTokenActive(Long userId, String token):**
- Verifica si token está activo en Redis
- Útil para invalidación forzada

### 6. AuthController

Endpoints REST para autenticación.

**POST /api/auth/login**
```json
Request:
{
  "username": "juan",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "juan",
  "email": "juan@example.com"
}
```

**POST /api/auth/register**
```json
Request:
{
  "username": "maria",
  "password": "password123",
  "firstName": "María",
  "lastName": "González",
  "email": "maria@example.com"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "maria",
  "email": "maria@example.com"
}
```

**POST /api/auth/logout**
```
Headers: Authorization: Bearer {token}
Response: 200 OK
```

**GET /api/auth/me**
```
Headers: Authorization: Bearer {token}
Response: "Usuario autenticado: juan"
```

## Uso del Cliente

### 1. Registro

```javascript
const response = await fetch('/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'usuario',
    password: 'password',
    firstName: 'Nombre',
    lastName: 'Apellido',
    email: 'email@example.com'
  })
});

const data = await response.json();
localStorage.setItem('token', data.token);
```

### 2. Login

```javascript
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'usuario',
    password: 'password'
  })
});

const data = await response.json();
localStorage.setItem('token', data.token);
```

### 3. Requests Autenticados

```javascript
const token = localStorage.getItem('token');

const response = await fetch('/api/eventos', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

### 4. Logout

```javascript
const token = localStorage.getItem('token');

await fetch('/api/auth/logout', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

localStorage.removeItem('token');
```

## Seguridad de Passwords

### BCryptPasswordEncoder

- Algoritmo de hash unidireccional
- Incluye salt automático (aleatorio por password)
- Configuración de strength: 10 rondas por defecto
- Hash resultante: 60 caracteres

**Ejemplo:**
```
Password: "password123"
Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

### Validación

Spring Security compara automáticamente:
```java
passwordEncoder.matches(rawPassword, hashedPassword)
```

## CORS Configuration

Permite requests desde:
- `http://localhost:3000` - Frontend React/Vue/Angular
- `http://localhost:8080` - Mismo origen
- `http://localhost:8081` - Mobile/otros

**Métodos permitidos:**
- GET, POST, PUT, DELETE, PATCH, OPTIONS

**Headers permitidos:**
- Authorization, Content-Type, Accept, X-Requested-With

## Token Storage

### Redis

Tokens se guardan en Redis como backup/invalidación:
- Key: `auth:token:{userId}`
- Value: token JWT
- TTL: 1 hora

**Ventajas:**
- Invalidación inmediata (logout)
- Verificación de tokens activos
- Renovación de tokens

### Cliente

**localStorage:**
- Persiste entre sesiones
- Vulnerable a XSS

**sessionStorage:**
- Solo durante sesión actual
- Más seguro

**Recomendación:** usar httpOnly cookies en producción.

## Manejo de Errores

### Tokens Inválidos

- Token malformado → 401 Unauthorized
- Token expirado → 401 Unauthorized
- Token con firma inválida → 401 Unauthorized

### Credenciales Incorrectas

- Username/password inválido → 401 Unauthorized
- Usuario deshabilitado → 401 Unauthorized

### Validaciones de Registro

- Username duplicado → 400 Bad Request
- Email duplicado → 400 Bad Request
- Validaciones de formato → 400 Bad Request

## Mejores Prácticas

1. **JWT Secret:**
   - Mínimo 256 bits (32 caracteres)
   - Usar variable de entorno en producción
   - Nunca commitear en código

2. **Token Expiration:**
   - Corto para APIs sensibles (15-30 min)
   - Implementar refresh tokens para UX

3. **HTTPS:**
   - Siempre usar HTTPS en producción
   - Tokens enviados en headers son vulnerables en HTTP

4. **Storage:**
   - Preferir httpOnly cookies
   - Si usas localStorage, sanitizar contra XSS

5. **Logout:**
   - Siempre eliminar token del cliente
   - Invalidar en servidor (Redis)

## Testing

### Obtener Token para Tests

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario","password":"password"}'

# Response incluye token
```

### Usar Token en Requests

```bash
curl -X GET http://localhost:8080/api/eventos \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## Troubleshooting

### Token no funciona

1. Verificar que empiece con "Bearer "
2. Verificar expiración
3. Verificar que JWT_SECRET sea el mismo
4. Revisar logs del JwtAuthenticationFilter

### CORS errors

1. Verificar origen en SecurityConfig
2. Agregar origen del cliente si es diferente
3. Verificar headers permitidos

### Usuario no se autentica

1. Verificar que usuario exista en BD
2. Verificar que `enabled = true`
3. Revisar password hasheado correctamente

