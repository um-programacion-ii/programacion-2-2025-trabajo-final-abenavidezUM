package com.eventos.backend.mapper;

import com.eventos.backend.domain.Usuario;
import com.eventos.backend.dto.UsuarioDTO;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .firstName(usuario.getFirstName())
                .lastName(usuario.getLastName())
                .email(usuario.getEmail())
                .enabled(usuario.getEnabled())
                .createdAt(usuario.getCreatedAt())
                .build();
    }

    public Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) {
            return null;
        }

        return Usuario.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .enabled(dto.getEnabled())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}

