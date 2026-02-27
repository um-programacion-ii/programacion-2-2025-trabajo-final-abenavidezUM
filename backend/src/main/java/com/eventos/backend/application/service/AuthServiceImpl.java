package com.eventos.backend.application.service;

import com.eventos.backend.domain.model.Usuario;
import com.eventos.backend.domain.ports.input.AutenticarUsuarioUseCase;
import com.eventos.backend.domain.ports.output.RedisCachePort;
import com.eventos.backend.domain.ports.output.UsuarioRepositoryPort;
import com.eventos.backend.domain.exception.ConflictException;
import com.eventos.backend.domain.exception.ResourceNotFoundException;
import com.eventos.backend.domain.exception.UnauthorizedException;
import com.eventos.backend.dto.JwtResponseDTO;
import com.eventos.backend.dto.LoginRequestDTO;
import com.eventos.backend.dto.RegisterRequestDTO;
import com.eventos.backend.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Implementación del caso de uso de autenticación
 * Capa de aplicación - orquesta la lógica de negocio de autenticación
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AutenticarUsuarioUseCase {

    // Puertos de salida (interfaces del dominio)
    private final UsuarioRepositoryPort usuarioRepository;
    private final RedisCachePort redisCache;
    
    // Dependencias de infraestructura (inyectadas)
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional(readOnly = true)
    public JwtResponseDTO autenticar(LoginRequestDTO loginRequest) {
        return login(loginRequest);
    }

    @Override
    @Transactional
    public JwtResponseDTO registrarUsuario(RegisterRequestDTO registerRequest) {
        return register(registerRequest);
    }

    /**
     * Autenticar usuario y generar token JWT
     */
    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        // Autenticar credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar token JWT
        String jwt = tokenProvider.generateToken(authentication);

        // Obtener información del usuario
        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", loginRequest.getUsername()));

        // Guardar token en Redis con TTL de 1 hora
        String redisKey = "auth:token:" + usuario.getId();
        redisCache.save(redisKey, jwt, 1, TimeUnit.HOURS);

        log.info("Usuario autenticado exitosamente: {}", loginRequest.getUsername());

        return new JwtResponseDTO(jwt, usuario.getUsername(), usuario.getEmail());
    }

    /**
     * Registrar nuevo usuario
     */
    @Transactional
    public JwtResponseDTO register(RegisterRequestDTO registerRequest) {
        // Validar que el username no exista
        if (usuarioRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("El username ya está en uso");
        }

        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El email ya está en uso");
        }

        // Crear nuevo usuario (activo por defecto en la entidad Usuario)
        Usuario usuario = Usuario.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .build();
        
        // El campo activo se inicializa con true por defecto en la entidad

        // Guardar usuario
        usuario = usuarioRepository.save(usuario);

        log.info("Nuevo usuario registrado: {}", usuario.getUsername());

        // Autenticar automáticamente
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar token JWT
        String jwt = tokenProvider.generateToken(authentication);

        // Guardar token en Redis
        String redisKey = "auth:token:" + usuario.getId();
        redisCache.save(redisKey, jwt, 1, TimeUnit.HOURS);

        return new JwtResponseDTO(jwt, usuario.getUsername(), usuario.getEmail());
    }

    /**
     * Cerrar sesión (logout)
     */
    @Transactional
    public void logout(String token) {
        try {
            // Extraer username del token
            String username = tokenProvider.getUsernameFromToken(token);
            
            // Obtener usuario
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
            
            // Eliminar token de Redis
            String redisKey = "auth:token:" + usuario.getId();
            redisCache.delete(redisKey);
            
            // Limpiar contexto de seguridad
            SecurityContextHolder.clearContext();
            
            log.info("Usuario cerró sesión: {}", username);
        } catch (Exception e) {
            log.error("Error al cerrar sesión: {}", e.getMessage());
            throw new UnauthorizedException("Error al cerrar sesión");
        }
    }

    /**
     * Renovar token JWT
     */
    @Transactional(readOnly = true)
    public JwtResponseDTO refreshToken(String oldToken) {
        try {
            // Validar token anterior
            if (!tokenProvider.validateToken(oldToken)) {
                throw new UnauthorizedException("Token inválido o expirado");
            }
            
            // Extraer username
            String username = tokenProvider.getUsernameFromToken(oldToken);
            
            // Obtener usuario
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
            
            // Crear nueva autenticación
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, null
            );
            
            // Generar nuevo token
            String newToken = tokenProvider.generateToken(authentication);
            
            // Actualizar en Redis
            String redisKey = "auth:token:" + usuario.getId();
            redisCache.delete(redisKey);
            redisCache.save(redisKey, newToken, 1, TimeUnit.HOURS);
            
            log.info("Token renovado para usuario: {}", username);
            
            return new JwtResponseDTO(newToken, usuario.getUsername(), usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al renovar token: {}", e.getMessage());
            throw new UnauthorizedException("Error al renovar token");
        }
    }

    /**
     * Obtener usuario actual por token
     */
    @Transactional(readOnly = true)
    public Usuario getCurrentUser(String token) {
        try {
            String username = tokenProvider.getUsernameFromToken(token);
            return usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        } catch (Exception e) {
            log.error("Error al obtener usuario actual: {}", e.getMessage());
            throw new UnauthorizedException("Error al obtener usuario actual");
        }
    }
}
