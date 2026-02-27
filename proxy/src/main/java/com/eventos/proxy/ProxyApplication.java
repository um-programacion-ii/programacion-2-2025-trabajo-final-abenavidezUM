package com.eventos.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del servicio Proxy
 * 
 * Este servicio actúa como intermediario entre el backend y la API de cátedra,
 * facilitando la comunicación con los servicios externos.
 */
@SpringBootApplication
public class ProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }
}

