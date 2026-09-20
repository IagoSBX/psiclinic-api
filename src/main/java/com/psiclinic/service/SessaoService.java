package com.psiclinic.service;

import com.psiclinic.dto.SessaoRequestDTO;
import com.psiclinic.dto.SessaoResponseDTO;
import com.psiclinic.dto.SessaoStatusUpdateDTO;
import com.psiclinic.exception.ResourceNotFoundException;
import com.psiclinic.exception.SessaoConflitanteException;
import com.psiclinic.mapper.SessaoMapper;
import com.psiclinic.model.Paciente;
import com.psiclinic.model.Psicologo;
import com.psiclinic.model.Sessao;
import com.psiclinic.model.StatusSessao;
import com.psiclinic.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SessaoService {

    private final SessaoRepository sessaoRepository;
    private final SessaoMapper sessaoMapper;
    private final PacienteService pacienteService;
    private final PsicologoService psicologoService;

    public SessaoResponseDTO agendar(SessaoRequestDTO dto) {
        Paciente paciente = pacienteService.buscarEntidadePorId(dto.pacienteId());
        Psicologo psicologo = psicologoService.buscarEntidadePorId(dto.psicologoId());

        validarConflitoDeHorario(dto.psicologoId(), dto.dataHora(), dto.duracaoMinutos(), null);

        Sessao sessao = sessaoMapper.toEntity(paciente, psicologo, dto.dataHora(), dto.duracaoMinutos(),
                dto.valor(), dto.observacoes());

        Sessao salva = sessaoRepository.save(sessao);
        return sessaoMapper.toResponseDTO(salva);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponseDTO> listar(Pageable pageable) {
        return sessaoRepository.findAll(pageable).map(sessaoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public SessaoResponseDTO buscarPorId(Long id) {
        return sessaoMapper.toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Sessao buscarEntidadePorId(Long id) {
        return sessaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada com id " + id));
    }

    public SessaoResponseDTO atualizarStatus(Long id, SessaoStatusUpdateDTO dto) {
        Sessao sessao = buscarEntidadePorId(id);
        sessao.setStatus(dto.status());
        return sessaoMapper.toResponseDTO(sessaoRepository.save(sessao));
    }

    public void deletar(Long id) {
        Sessao sessao = buscarEntidadePorId(id);
        sessaoRepository.delete(sessao);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponseDTO> listarPorPsicologo(Long psicologoId, Pageable pageable) {
        psicologoService.buscarEntidadePorId(psicologoId);
        return sessaoRepository.findByPsicologoId(psicologoId, pageable).map(sessaoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponseDTO> listarPorPaciente(Long pacienteId, Pageable pageable) {
        pacienteService.buscarEntidadePorId(pacienteId);
        return sessaoRepository.findByPacienteId(pacienteId, pageable).map(sessaoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<SessaoResponseDTO> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
        return sessaoRepository.findByDataHoraBetween(dataInicio, dataFim, pageable).map(sessaoMapper::toResponseDTO);
    }

    private void validarConflitoDeHorario(Long psicologoId, LocalDateTime dataHora, Integer duracaoMinutos,
                                           Long sessaoIdIgnorar) {
        LocalDateTime inicioNovo = dataHora;
        LocalDateTime fimNovo = dataHora.plusMinutes(duracaoMinutos);

        LocalDateTime inicioDia = dataHora.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);

        List<Sessao> sessoesDoDia = sessaoRepository.findByPsicologoIdAndDataHoraBetween(psicologoId, inicioDia, fimDia);

        boolean conflito = sessoesDoDia.stream()
                .filter(s -> sessaoIdIgnorar == null || !s.getId().equals(sessaoIdIgnorar))
                .filter(s -> s.getStatus() != StatusSessao.CANCELADA)
                .anyMatch(s -> {
                    LocalDateTime inicioExistente = s.getDataHora();
                    LocalDateTime fimExistente = inicioExistente.plusMinutes(s.getDuracaoMinutos());
                    return inicioNovo.isBefore(fimExistente) && inicioExistente.isBefore(fimNovo);
                });

        if (conflito) {
            throw new SessaoConflitanteException(
                    "O psicólogo já possui uma sessão agendada que conflita com o horário informado");
        }
    }
}
