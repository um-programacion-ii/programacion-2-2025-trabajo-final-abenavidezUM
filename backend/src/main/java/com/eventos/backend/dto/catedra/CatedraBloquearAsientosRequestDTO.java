package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraBloquearAsientosRequestDTO {
    
    private Long eventoId;
    private List<CatedraAsientoDTO> asientos;
}

