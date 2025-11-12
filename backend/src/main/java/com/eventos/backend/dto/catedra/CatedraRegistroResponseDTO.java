package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraRegistroResponseDTO {
    
    private Boolean creado;
    private String resultado;
    private String token;
}

