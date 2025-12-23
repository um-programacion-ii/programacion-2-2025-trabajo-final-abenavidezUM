package com.eventos.backend.domain.ports.output;

import com.eventos.backend.domain.model.Evento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de eventos
 * Define las operaciones de persistencia que el dominio necesita
 */
public interface EventoRepositoryPort {
    
    /**
     * Guarda o actualiza un evento
     * @param evento evento a guardar
     * @return evento guardado
     */
    Evento save(Evento evento);
    
    /**
     * Busca un evento por su ID
     * @param id identificador del evento
     * @return evento si existe
     */
    Optional<Evento> findById(Long id);
    
    /**
     * Busca un evento por su ID externo (de cátedra)
     * @param idExterno ID externo del evento
     * @return evento si existe
     */
    Optional<Evento> findByIdExterno(Long idExterno);
    
    /**
     * Obtiene todos los eventos activos
     * @return lista de eventos activos
     */
    List<Evento> findAllActivos();
    
    /**
     * Elimina un evento
     * @param id identificador del evento
     */
    void deleteById(Long id);
}

