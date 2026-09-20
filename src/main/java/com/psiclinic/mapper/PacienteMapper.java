package com.psiclinic.mapper;

import com.psiclinic.dto.PacienteRequestDTO;
import com.psiclinic.dto.PacienteResponseDTO;
import com.psiclinic.model.Paciente;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {

    public Paciente toEntity(PacienteRequestDTO dto) {
        return Paciente.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .dataNascimento(dto.dataNascimento())
                .telefone(dto.telefone())
                .email(dto.email())
                .endereco(dto.endereco())
                .build();
    }

    public void updateEntityFromDto(PacienteRequestDTO dto, Paciente paciente) {
        paciente.setNome(dto.nome());
        paciente.setCpf(dto.cpf());
        paciente.setDataNascimento(dto.dataNascimento());
        paciente.setTelefone(dto.telefone());
        paciente.setEmail(dto.email());
        paciente.setEndereco(dto.endereco());
    }

    public PacienteResponseDTO toResponseDTO(Paciente paciente) {
        return new PacienteResponseDTO(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getDataNascimento(),
                paciente.getTelefone(),
                paciente.getEmail(),
                paciente.getEndereco(),
                paciente.getDataCadastro()
        );
    }
}
