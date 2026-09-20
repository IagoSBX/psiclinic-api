package com.psiclinic.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessaoRequestDTO(

        @NotNull(message = "Paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "Psicólogo é obrigatório")
        Long psicologoId,

        @NotNull(message = "Data e hora são obrigatórias")
        @Future(message = "Data e hora da sessão devem ser no futuro")
        LocalDateTime dataHora,

        @NotNull(message = "Duração é obrigatória")
        @Min(value = 15, message = "Duração mínima é de 15 minutos")
        Integer duracaoMinutos,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        String observacoes
) {
}
