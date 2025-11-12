package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraLoginRequestDTO {
    
    private String username;
    private String password;
    private Boolean rememberMe;
}

