package com.eventos.backend.service;

import com.eventos.backend.domain.Evento;
import com.eventos.backend.domain.Usuario;
import com.eventos.backend.dto.*;
import com.eventos.backend.exception.BadRequestException;
import com.eventos.backend.exception.ConflictException;
import com.eventos.backend.exception.ResourceNotFoundException;
import com.eventos.backend.repository.EventoRepository;
import com.eventos.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para gestionar sesiones de compra en Redis.
 * Maneja el estado del proceso de compra: selección de evento, asientos y personas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SesionCompraService {

    private final RedisService redisService;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final String SESION_PREFIX = "sesion:compra:";
    private static final int MAX_ASIENTOS = 4;

    @Value("${sesion.compra.ttl-minutos:30}")
    private int sesionTtlMinutos;

    // ==================== OPERACIONES PRINCIPALES ====================

    /**
     * Inicia una nueva sesión de compra para un evento
     */
    public SesionCompraDTO iniciarSesion(Long eventoId) {
        Usuario usuario = getUsuarioActual();
        log.info("Iniciando sesión de compra - Usuario: {}, Evento: {}", usuario.getUsername(), eventoId);

        // Verificar si ya existe una sesión activa
        SesionCompraDTO sesionExistente = obtenerSesionActual();
        if (sesionExistente != null && !sesionExistente.isExpirada()) {
            if (sesionExistente.getEventoId().equals(eventoId)) {
                log.info("Sesión existente encontrada para el mismo evento, renovando");
                return renovarSesion(sesionExistente);
            } else {
                log.info("Sesión existente para otro evento, limpiando");
                limpiarSesion();
            }
        }

        // Obtener y validar el evento
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

        if (!evento.getActivo()) {
            throw new BadRequestException("El evento no está disponible");
        }

        // Crear nueva sesión
        LocalDateTime ahora = LocalDateTime.now();
        SesionCompraDTO sesion = SesionCompraDTO.builder()
                .sesionId(UUID.randomUUID().toString())
                .usuarioId(usuario.getId())
                .eventoId(evento.getId())
                .eventoIdExterno(evento.getIdExterno())
                .eventoTitulo(evento.getTitulo())
                .precioUnitario(evento.getPrecioEntrada())
                .asientosSeleccionados(new ArrayList<>())
                .personas(new ArrayList<>())
                .createdAt(ahora)
                .updatedAt(ahora)
                .expiresAt(ahora.plusMinutes(sesionTtlMinutos))
                .asientosBloqueados(false)
                .build();

        guardarSesion(sesion);
        log.info("Sesión de compra iniciada: {}", sesion.getSesionId());

        return sesion;
    }

    /**
     * Obtiene la sesión de compra actual del usuario
     */
    public SesionCompraDTO obtenerSesionActual() {
        Usuario usuario = getUsuarioActual();
        String key = getSesionKey(usuario.getId());
        
        SesionCompraDTO sesion = redisService.get(key, SesionCompraDTO.class);
        
        if (sesion != null && sesion.isExpirada()) {
            log.info("Sesión expirada, limpiando");
            limpiarSesion();
            return null;
        }
        
        return sesion;
    }

    /**
     * Actualiza los asientos seleccionados en la sesión
     */
    public SesionCompraDTO actualizarAsientos(List<AsientoSeleccionadoDTO> asientos) {
        SesionCompraDTO sesion = obtenerSesionActualOError();
        log.info("Actualizando asientos - Sesión: {}, Cantidad: {}", sesion.getSesionId(), asientos.size());

        // Validar cantidad de asientos
        if (asientos.size() > MAX_ASIENTOS) {
            throw new BadRequestException("No puede seleccionar más de " + MAX_ASIENTOS + " asientos");
        }

        // Validar que no haya asientos duplicados
        long asientosUnicos = asientos.stream()
                .map(AsientoSeleccionadoDTO::getId)
                .distinct()
                .count();
        if (asientosUnicos != asientos.size()) {
            throw new BadRequestException("No puede seleccionar el mismo asiento dos veces");
        }

        // Obtener evento para validar dimensiones
        Evento evento = eventoRepository.findById(sesion.getEventoId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", sesion.getEventoId()));

        // Validar que los asientos estén dentro del rango válido
        for (AsientoSeleccionadoDTO asiento : asientos) {
            if (asiento.getFila() < 1 || asiento.getFila() > evento.getFilaAsientos()) {
                throw new BadRequestException("Fila " + asiento.getFila() + " no válida");
            }
            if (asiento.getColumna() < 1 || asiento.getColumna() > evento.getColumnaAsientos()) {
                throw new BadRequestException("Columna " + asiento.getColumna() + " no válida");
            }
        }

        // Si los asientos ya estaban bloqueados y cambian, resetear el bloqueo
        if (Boolean.TRUE.equals(sesion.getAsientosBloqueados())) {
            sesion.setAsientosBloqueados(false);
            log.info("Asientos modificados, resetendo estado de bloqueo");
        }

        sesion.setAsientosSeleccionados(new ArrayList<>(asientos));
        sesion.setUpdatedAt(LocalDateTime.now());
        
        // Limpiar personas si los asientos cambiaron
        sesion.setPersonas(new ArrayList<>());

        guardarSesion(sesion);
        log.info("Asientos actualizados: {}", asientos.size());

        return sesion;
    }

    /**
     * Actualiza los datos de las personas para cada asiento
     */
    public SesionCompraDTO actualizarPersonas(List<PersonaAsientoDTO> personas) {
        SesionCompraDTO sesion = obtenerSesionActualOError();
        log.info("Actualizando personas - Sesión: {}, Cantidad: {}", sesion.getSesionId(), personas.size());

        // Validar que haya asientos seleccionados
        if (sesion.getAsientosSeleccionados() == null || sesion.getAsientosSeleccionados().isEmpty()) {
            throw new BadRequestException("Debe seleccionar asientos antes de ingresar datos de personas");
        }

        // Validar que la cantidad de personas coincida con los asientos
        if (personas.size() != sesion.getAsientosSeleccionados().size()) {
            throw new BadRequestException("La cantidad de personas debe coincidir con los asientos seleccionados");
        }

        // Validar que cada persona corresponda a un asiento seleccionado
        for (PersonaAsientoDTO persona : personas) {
            boolean asientoValido = sesion.getAsientosSeleccionados().stream()
                    .anyMatch(a -> a.getFila().equals(persona.getFila()) 
                            && a.getColumna().equals(persona.getColumna()));
            if (!asientoValido) {
                throw new BadRequestException("Asiento " + persona.getFila() + "-" + persona.getColumna() 
                        + " no está en la selección");
            }
        }

        sesion.setPersonas(new ArrayList<>(personas));
        sesion.setUpdatedAt(LocalDateTime.now());

        guardarSesion(sesion);
        log.info("Personas actualizadas: {}", personas.size());

        return sesion;
    }

    /**
     * Marca los asientos como bloqueados (después de confirmar bloqueo con cátedra)
     */
    public SesionCompraDTO marcarAsientosBloqueados() {
        SesionCompraDTO sesion = obtenerSesionActualOError();
        
        if (sesion.getAsientosSeleccionados() == null || sesion.getAsientosSeleccionados().isEmpty()) {
            throw new BadRequestException("No hay asientos seleccionados para bloquear");
        }
        
        sesion.setAsientosBloqueados(true);
        sesion.setUpdatedAt(LocalDateTime.now());
        
        guardarSesion(sesion);
        log.info("Asientos marcados como bloqueados: {}", sesion.getSesionId());
        
        return sesion;
    }

    /**
     * Limpia la sesión de compra actual
     */
    public void limpiarSesion() {
        Usuario usuario = getUsuarioActual();
        String key = getSesionKey(usuario.getId());
        
        redisService.delete(key);
        log.info("Sesión de compra limpiada para usuario: {}", usuario.getUsername());
    }

    /**
     * Renueva la expiración de la sesión
     */
    public SesionCompraDTO renovarSesion(SesionCompraDTO sesion) {
        sesion.setExpiresAt(LocalDateTime.now().plusMinutes(sesionTtlMinutos));
        sesion.setUpdatedAt(LocalDateTime.now());
        guardarSesion(sesion);
        log.info("Sesión renovada: {}", sesion.getSesionId());
        return sesion;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private SesionCompraDTO obtenerSesionActualOError() {
        SesionCompraDTO sesion = obtenerSesionActual();
        if (sesion == null) {
            throw new ResourceNotFoundException("Sesión de compra no encontrada o expirada");
        }
        return sesion;
    }

    private void guardarSesion(SesionCompraDTO sesion) {
        Usuario usuario = getUsuarioActual();
        String key = getSesionKey(usuario.getId());
        redisService.save(key, sesion, sesionTtlMinutos, TimeUnit.MINUTES);
    }

    private String getSesionKey(Long usuarioId) {
        return SESION_PREFIX + usuarioId;
    }

    private Usuario getUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Usuario no autenticado");
        }
        
        String username = authentication.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
    }
}

