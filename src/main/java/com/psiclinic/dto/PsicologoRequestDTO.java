package com.psiclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PsicologoRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "CRP é obrigatório")
        String crp,

        @NotBlank(message = "Especialidade é obrigatória")
        String especialidade,

        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        String email,

        String telefone
) {
}
