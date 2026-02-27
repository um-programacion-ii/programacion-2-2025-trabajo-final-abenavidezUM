package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraRegistroRequestDTO {
    
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String nombreAlumno;
    private String descripcionProyecto;
}

