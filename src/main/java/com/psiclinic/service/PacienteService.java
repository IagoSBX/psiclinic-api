package com.psiclinic.service;

import com.psiclinic.dto.PacienteRequestDTO;
import com.psiclinic.dto.PacienteResponseDTO;
import com.psiclinic.exception.DuplicateResourceException;
import com.psiclinic.exception.ResourceNotFoundException;
import com.psiclinic.mapper.PacienteMapper;
import com.psiclinic.model.Paciente;
import com.psiclinic.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;

    public PacienteResponseDTO criar(PacienteRequestDTO dto) {
        if (pacienteRepository.existsByCpf(dto.cpf())) {
            throw new DuplicateResourceException("Já existe um paciente cadastrado com o CPF " + dto.cpf());
        }
        Paciente paciente = pacienteMapper.toEntity(dto);
        Paciente salvo = pacienteRepository.save(paciente);
        return pacienteMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.findAll().stream()
                .map(pacienteMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO buscarPorId(Long id) {
        return pacienteMapper.toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Paciente buscarEntidadePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado com id " + id));
    }

    public PacienteResponseDTO atualizar(Long id, PacienteRequestDTO dto) {
        Paciente paciente = buscarEntidadePorId(id);

        if (pacienteRepository.existsByCpfAndIdNot(dto.cpf(), id)) {
            throw new DuplicateResourceException("Já existe um paciente cadastrado com o CPF " + dto.cpf());
        }

        pacienteMapper.updateEntityFromDto(dto, paciente);
        Paciente atualizado = pacienteRepository.save(paciente);
        return pacienteMapper.toResponseDTO(atualizado);
    }

    public void deletar(Long id) {
        Paciente paciente = buscarEntidadePorId(id);
        pacienteRepository.delete(paciente);
    }
}
