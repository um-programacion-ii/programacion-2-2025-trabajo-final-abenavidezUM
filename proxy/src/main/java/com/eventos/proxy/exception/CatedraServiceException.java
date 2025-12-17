package com.eventos.proxy.exception;

/**
 * Excepción lanzada cuando hay un error al comunicarse con el servicio de cátedra
 */
public class CatedraServiceException extends RuntimeException {
    
    public CatedraServiceException(String message) {
        super(message);
    }
    
    public CatedraServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

