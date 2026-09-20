package com.psiclinic.service;

import com.psiclinic.dto.PsicologoRequestDTO;
import com.psiclinic.dto.PsicologoResponseDTO;
import com.psiclinic.exception.DuplicateResourceException;
import com.psiclinic.exception.ResourceNotFoundException;
import com.psiclinic.mapper.PsicologoMapper;
import com.psiclinic.model.Psicologo;
import com.psiclinic.repository.PsicologoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PsicologoService {

    private final PsicologoRepository psicologoRepository;
    private final PsicologoMapper psicologoMapper;

    public PsicologoResponseDTO criar(PsicologoRequestDTO dto) {
        if (psicologoRepository.existsByCrp(dto.crp())) {
            throw new DuplicateResourceException("Já existe um psicólogo cadastrado com o CRP " + dto.crp());
        }
        Psicologo psicologo = psicologoMapper.toEntity(dto);
        Psicologo salvo = psicologoRepository.save(psicologo);
        return psicologoMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<PsicologoResponseDTO> listarTodos() {
        return psicologoRepository.findAll().stream()
                .map(psicologoMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PsicologoResponseDTO buscarPorId(Long id) {
        return psicologoMapper.toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Psicologo buscarEntidadePorId(Long id) {
        return psicologoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo não encontrado com id " + id));
    }

    public PsicologoResponseDTO atualizar(Long id, PsicologoRequestDTO dto) {
        Psicologo psicologo = buscarEntidadePorId(id);

        if (psicologoRepository.existsByCrpAndIdNot(dto.crp(), id)) {
            throw new DuplicateResourceException("Já existe um psicólogo cadastrado com o CRP " + dto.crp());
        }

        psicologoMapper.updateEntityFromDto(dto, psicologo);
        Psicologo atualizado = psicologoRepository.save(psicologo);
        return psicologoMapper.toResponseDTO(atualizado);
    }

    public void deletar(Long id) {
        Psicologo psicologo = buscarEntidadePorId(id);
        psicologoRepository.delete(psicologo);
    }
}
