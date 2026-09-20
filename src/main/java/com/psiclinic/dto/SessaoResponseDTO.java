package com.psiclinic.dto;

import com.psiclinic.model.StatusSessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessaoResponseDTO(
        Long id,
        PacienteResumoDTO paciente,
        PsicologoResumoDTO psicologo,
        LocalDateTime dataHora,
        Integer duracaoMinutos,
        StatusSessao status,
        BigDecimal valor,
        String observacoes
) {
    public record PacienteResumoDTO(Long id, String nome) {
    }

    public record PsicologoResumoDTO(Long id, String nome) {
    }
}
