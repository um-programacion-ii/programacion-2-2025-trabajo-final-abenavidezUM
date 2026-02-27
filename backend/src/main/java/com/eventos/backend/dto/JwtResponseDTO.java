package com.eventos.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponseDTO {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;

    public JwtResponseDTO(String token, String username, String email) {
        this.token = token;
        this.username = username;
        this.email = email;
    }
}

