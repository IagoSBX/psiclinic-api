package com.psiclinic.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponseDTO(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        String email,
        String endereco,
        LocalDateTime dataCadastro
) {
}
