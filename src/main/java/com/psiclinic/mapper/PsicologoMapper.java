package com.psiclinic.mapper;

import com.psiclinic.dto.PsicologoRequestDTO;
import com.psiclinic.dto.PsicologoResponseDTO;
import com.psiclinic.model.Psicologo;
import org.springframework.stereotype.Component;

@Component
public class PsicologoMapper {

    public Psicologo toEntity(PsicologoRequestDTO dto) {
        return Psicologo.builder()
                .nome(dto.nome())
                .crp(dto.crp())
                .especialidade(dto.especialidade())
                .email(dto.email())
                .telefone(dto.telefone())
                .build();
    }

    public void updateEntityFromDto(PsicologoRequestDTO dto, Psicologo psicologo) {
        psicologo.setNome(dto.nome());
        psicologo.setCrp(dto.crp());
        psicologo.setEspecialidade(dto.especialidade());
        psicologo.setEmail(dto.email());
        psicologo.setTelefone(dto.telefone());
    }

    public PsicologoResponseDTO toResponseDTO(Psicologo psicologo) {
        return new PsicologoResponseDTO(
                psicologo.getId(),
                psicologo.getNome(),
                psicologo.getCrp(),
                psicologo.getEspecialidade(),
                psicologo.getEmail(),
                psicologo.getTelefone()
        );
    }
}
