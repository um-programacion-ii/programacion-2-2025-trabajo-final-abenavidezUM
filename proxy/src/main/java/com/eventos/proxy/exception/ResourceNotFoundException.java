package com.eventos.proxy.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s con ID %d no encontrado", resourceType, id));
    }
}

