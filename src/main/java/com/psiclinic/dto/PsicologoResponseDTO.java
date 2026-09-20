package com.psiclinic.dto;

public record PsicologoResponseDTO(
        Long id,
        String nome,
        String crp,
        String especialidade,
        String email,
        String telefone
) {
}
