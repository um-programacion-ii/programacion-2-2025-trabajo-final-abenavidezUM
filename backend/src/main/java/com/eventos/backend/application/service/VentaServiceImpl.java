package com.eventos.backend.application.service;

import com.eventos.backend.domain.model.AsientoVenta;
import com.eventos.backend.domain.model.Evento;
import com.eventos.backend.domain.model.Usuario;
import com.eventos.backend.domain.model.Venta;
import com.eventos.backend.dto.*;
import com.eventos.backend.dto.catedra.CatedraAsientoDTO;
import com.eventos.backend.dto.catedra.CatedraRealizarVentaRequestDTO;
import com.eventos.backend.dto.catedra.CatedraRealizarVentaResponseDTO;
import com.eventos.backend.domain.exception.BadRequestException;
import com.eventos.backend.domain.exception.ResourceNotFoundException;
import com.eventos.backend.infrastructure.mapper.VentaMapper;
import com.eventos.backend.infrastructure.adapter.output.external.service.CatedraApiClient;
import com.eventos.backend.infrastructure.adapter.output.external.service.ProxyClient;
import com.eventos.backend.application.service.SesionCompraServiceImpl;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.EventoRepository;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.UsuarioRepository;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaServiceImpl {

    private final VentaRepository ventaRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaMapper ventaMapper;
    private final CatedraApiClient catedraApiClient;
    private final SesionCompraServiceImpl sesionCompraService;
    private final ProxyClient proxyClient;

    private static final int MAX_REINTENTOS = 3;

    /**
     * Realiza una venta completa
     */
    @Transactional
    public VentaDTO realizarVenta() {
        Usuario usuario = getUsuarioActual();
        log.info("Realizando venta para usuario: {}", usuario.getUsername());

        // Obtener sesión de compra
        SesionCompraDTO sesion = sesionCompraService.obtenerSesionActual();
        if (sesion == null) {
            throw new BadRequestException("No hay sesión de compra activa");
        }

        // Validar sesión completa
        if (!sesion.isCompleta()) {
            throw new BadRequestException("La sesión de compra no está completa");
        }

        // Validar que los asientos estén bloqueados
        if (!Boolean.TRUE.equals(sesion.getAsientosBloqueados())) {
            throw new BadRequestException("Debe bloquear los asientos antes de realizar la venta");
        }

        // Obtener evento
        Evento evento = eventoRepository.findById(sesion.getEventoId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", sesion.getEventoId()));

        if (!evento.getActivo()) {
            throw new BadRequestException("El evento no está disponible");
        }

        // Verificar disponibilidad de asientos en tiempo real
        for (AsientoSeleccionadoDTO asiento : sesion.getAsientosSeleccionados()) {
            try {
                var estadoAsiento = proxyClient.obtenerEstadoAsiento(
                    evento.getIdExterno(), 
                    asiento.getFila(), 
                    asiento.getColumna()
                );
                
                if (estadoAsiento != null && !"BLOQUEADO".equals(estadoAsiento.getEstado())) {
                    throw new BadRequestException(
                        "El asiento " + asiento.getFila() + "-" + asiento.getColumna() + 
                        " ya no está disponible (estado: " + estadoAsiento.getEstado() + ")"
                    );
                }
            } catch (Exception e) {
                log.warn("No se pudo verificar estado del asiento {}-{}: {}", 
                    asiento.getFila(), asiento.getColumna(), e.getMessage());
                // Continuar con la venta aunque no se pueda verificar
            }
        }

        // Crear venta local
        Venta venta = Venta.builder()
                .usuario(usuario)
                .evento(evento)
                .fechaVenta(LocalDateTime.now())
                .precioTotal(sesion.getPrecioTotal())
                .resultado(false)
                .descripcion("Pendiente de confirmación")
                .confirmadaCatedra(false)
                .intentosSincronizacion(0)
                .build();

        // Agregar asientos
        for (PersonaAsientoDTO persona : sesion.getPersonas()) {
            AsientoVenta asiento = AsientoVenta.builder()
                    .fila(persona.getFila())
                    .columna(persona.getColumna())
                    .nombrePersona(persona.getNombre())
                    .build();
            venta.addAsiento(asiento);
        }

        // Guardar venta local primero
        venta = ventaRepository.save(venta);
        log.info("Venta local creada: {}", venta.getId());

        // Intentar confirmar con cátedra
        try {
            boolean confirmada = confirmarVentaCatedra(venta, sesion);
            
            if (confirmada) {
                venta.setResultado(true);
                venta.setConfirmadaCatedra(true);
                venta.setDescripcion("Venta confirmada exitosamente");
                ventaRepository.save(venta);
                
                // Limpiar sesión tras venta exitosa
                sesionCompraService.limpiarSesion();
                log.info("Venta confirmada con cátedra: {}", venta.getId());
            } else {
                venta.setResultado(false);
                venta.setDescripcion("Error al confirmar con cátedra");
                venta.setIntentosSincronizacion(1);
                ventaRepository.save(venta);
                log.warn("Venta no confirmada con cátedra: {}", venta.getId());
            }
        } catch (Exception e) {
            log.error("Error al confirmar venta con cátedra: {}", e.getMessage());
            venta.setResultado(false);
            venta.setDescripcion("Error: " + e.getMessage());
            venta.setIntentosSincronizacion(1);
            ventaRepository.save(venta);
        }

        return ventaMapper.toDTO(venta);
    }

    /**
     * Confirma la venta con el servicio de cátedra
     */
    private boolean confirmarVentaCatedra(Venta venta, SesionCompraDTO sesion) {
        if (venta.getEvento().getIdExterno() == null) {
            log.warn("Evento sin ID externo, no se puede confirmar con cátedra");
            return false;
        }

        List<CatedraAsientoDTO> asientosCatedra = sesion.getPersonas().stream()
                .map(p -> CatedraAsientoDTO.builder()
                        .fila(p.getFila())
                        .columna(p.getColumna())
                        .persona(p.getNombre())
                        .build())
                .collect(Collectors.toList());

        CatedraRealizarVentaRequestDTO request = CatedraRealizarVentaRequestDTO.builder()
                .eventoId(venta.getEvento().getIdExterno())
                .asientos(asientosCatedra)
                .build();

        CatedraRealizarVentaResponseDTO response = catedraApiClient.realizarVenta(request);

        if (response != null && Boolean.TRUE.equals(response.getResultado())) {
            venta.setIdExterno(response.getVentaId());
            return true;
        }

        return false;
    }

    /**
     * Obtiene el historial de ventas del usuario actual (sin paginación)
     */
    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerMisVentas() {
        Usuario usuario = getUsuarioActual();
        log.info("Obteniendo ventas para usuario: {}", usuario.getUsername());
        
        List<Venta> ventas = ventaRepository.findByUsuarioIdWithAsientos(usuario.getId());
        return ventaMapper.toDTOList(ventas);
    }

    /**
     * Obtiene el historial de ventas del usuario actual con paginación
     */
    @Transactional(readOnly = true)
    public Page<VentaDTO> obtenerMisVentasPaginado(Pageable pageable) {
        Usuario usuario = getUsuarioActual();
        log.info("Obteniendo ventas paginadas - Usuario: {}, Página: {}", 
                usuario.getUsername(), pageable.getPageNumber());
        
        List<Venta> ventas = ventaRepository.findByUsuarioIdWithAsientos(usuario.getId());
        List<VentaDTO> ventasDTO = ventaMapper.toDTOList(ventas);
        
        return paginateList(ventasDTO, pageable);
    }

    /**
     * Obtiene solo las ventas exitosas del usuario
     */
    @Transactional(readOnly = true)
    public Page<VentaDTO> obtenerVentasExitosas(Pageable pageable) {
        Usuario usuario = getUsuarioActual();
        log.info("Obteniendo ventas exitosas para usuario: {}", usuario.getUsername());
        
        List<Venta> ventas = ventaRepository.findSuccessfulVentasByUsuario(usuario.getId());
        List<VentaDTO> ventasDTO = ventaMapper.toDTOList(ventas);
        
        return paginateList(ventasDTO, pageable);
    }

    /**
     * Obtiene solo las ventas fallidas del usuario
     */
    @Transactional(readOnly = true)
    public Page<VentaDTO> obtenerVentasFallidas(Pageable pageable) {
        Usuario usuario = getUsuarioActual();
        log.info("Obteniendo ventas fallidas para usuario: {}", usuario.getUsername());
        
        List<Venta> ventas = ventaRepository.findFailedVentasByUsuario(usuario.getId());
        List<VentaDTO> ventasDTO = ventaMapper.toDTOList(ventas);
        
        return paginateList(ventasDTO, pageable);
    }

    /**
     * Obtiene el detalle de una venta
     */
    @Transactional(readOnly = true)
    public VentaDTO obtenerVenta(Long ventaId) {
        Usuario usuario = getUsuarioActual();
        
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", ventaId));

        // Verificar que la venta pertenece al usuario
        if (!venta.getUsuario().getId().equals(usuario.getId())) {
            throw new BadRequestException("No tiene acceso a esta venta");
        }

        return ventaMapper.toDTO(venta);
    }

    /**
     * Sincroniza ventas con el servicio de cátedra
     */
    @Scheduled(fixedDelayString = "${venta.sync.interval:600000}")
    @Transactional
    public void sincronizarVentasCatedra() {
        log.info("Sincronizando ventas con cátedra");
        
        try {
            var ventasCatedra = catedraApiClient.listarVentas();
            
            if (ventasCatedra == null || ventasCatedra.isEmpty()) {
                log.info("No hay ventas en cátedra para sincronizar");
                return;
            }

            int sincronizadas = 0;
            for (var ventaCatedra : ventasCatedra) {
                if (ventaCatedra.getVentaId() != null) {
                    var ventaLocal = ventaRepository.findByIdExterno(ventaCatedra.getVentaId());
                    if (ventaLocal.isPresent()) {
                        Venta venta = ventaLocal.get();
                        if (!venta.getConfirmadaCatedra()) {
                            venta.setConfirmadaCatedra(true);
                            venta.setResultado(true);
                            venta.setDescripcion("Sincronizado desde cátedra");
                            ventaRepository.save(venta);
                            sincronizadas++;
                        }
                    }
                }
            }
            
            log.info("Sincronización completada: {} ventas actualizadas", sincronizadas);
            
        } catch (Exception e) {
            log.error("Error al sincronizar ventas con cátedra: {}", e.getMessage());
        }
    }

    private <T> Page<T> paginateList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        
        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    /**
     * Reintenta confirmar ventas pendientes con cátedra
     */
    @Scheduled(fixedDelayString = "${venta.reintento.interval:300000}")
    @Transactional
    public void reintentarVentasPendientes() {
        List<Venta> ventasPendientes = ventaRepository.findVentasForRetry(MAX_REINTENTOS);
        
        if (ventasPendientes.isEmpty()) {
            return;
        }

        log.info("Reintentando {} ventas pendientes", ventasPendientes.size());

        for (Venta venta : ventasPendientes) {
            try {
                // Reconstruir datos de sesión para el reintento
                SesionCompraDTO sesionFake = SesionCompraDTO.builder()
                        .eventoId(venta.getEvento().getId())
                        .eventoIdExterno(venta.getEvento().getIdExterno())
                        .personas(venta.getAsientos().stream()
                                .map(a -> PersonaAsientoDTO.builder()
                                        .fila(a.getFila())
                                        .columna(a.getColumna())
                                        .nombre(a.getNombrePersona())
                                        .build())
                                .collect(Collectors.toList()))
                        .build();

                boolean confirmada = confirmarVentaCatedra(venta, sesionFake);
                
                venta.setIntentosSincronizacion(venta.getIntentosSincronizacion() + 1);
                
                if (confirmada) {
                    venta.setResultado(true);
                    venta.setConfirmadaCatedra(true);
                    venta.setDescripcion("Venta confirmada en reintento");
                    log.info("Venta {} confirmada en reintento", venta.getId());
                } else {
                    venta.setDescripcion("Reintento " + venta.getIntentosSincronizacion() + " fallido");
                }
                
                ventaRepository.save(venta);
                
            } catch (Exception e) {
                log.error("Error al reintentar venta {}: {}", venta.getId(), e.getMessage());
                venta.setIntentosSincronizacion(venta.getIntentosSincronizacion() + 1);
                venta.setDescripcion("Error en reintento: " + e.getMessage());
                ventaRepository.save(venta);
            }
        }
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

