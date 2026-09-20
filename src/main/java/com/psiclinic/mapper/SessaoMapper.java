package com.psiclinic.mapper;

import com.psiclinic.dto.SessaoResponseDTO;
import com.psiclinic.model.Paciente;
import com.psiclinic.model.Psicologo;
import com.psiclinic.model.Sessao;
import com.psiclinic.model.StatusSessao;
import org.springframework.stereotype.Component;

@Component
public class SessaoMapper {

    public Sessao toEntity(Paciente paciente, Psicologo psicologo, java.time.LocalDateTime dataHora,
                            Integer duracaoMinutos, java.math.BigDecimal valor, String observacoes) {
        return Sessao.builder()
                .paciente(paciente)
                .psicologo(psicologo)
                .dataHora(dataHora)
                .duracaoMinutos(duracaoMinutos)
                .valor(valor)
                .observacoes(observacoes)
                .status(StatusSessao.AGENDADA)
                .build();
    }

    public SessaoResponseDTO toResponseDTO(Sessao sessao) {
        return new SessaoResponseDTO(
                sessao.getId(),
                new SessaoResponseDTO.PacienteResumoDTO(sessao.getPaciente().getId(), sessao.getPaciente().getNome()),
                new SessaoResponseDTO.PsicologoResumoDTO(sessao.getPsicologo().getId(), sessao.getPsicologo().getNome()),
                sessao.getDataHora(),
                sessao.getDuracaoMinutos(),
                sessao.getStatus(),
                sessao.getValor(),
                sessao.getObservacoes()
        );
    }
}
