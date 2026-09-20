package com.psiclinic.service;

import com.psiclinic.dto.SessaoRequestDTO;
import com.psiclinic.dto.SessaoResponseDTO;
import com.psiclinic.dto.SessaoStatusUpdateDTO;
import com.psiclinic.exception.SessaoConflitanteException;
import com.psiclinic.mapper.SessaoMapper;
import com.psiclinic.model.Paciente;
import com.psiclinic.model.Psicologo;
import com.psiclinic.model.Sessao;
import com.psiclinic.model.StatusSessao;
import com.psiclinic.repository.SessaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PacienteService pacienteService;

    @Mock
    private PsicologoService psicologoService;

    private SessaoMapper sessaoMapper;

    private SessaoService sessaoService;

    private Paciente paciente;
    private Psicologo psicologo;
    private LocalDateTime dataHoraBase;

    @BeforeEach
    void setUp() {
        sessaoMapper = new SessaoMapper();
        sessaoService = new SessaoService(sessaoRepository, sessaoMapper, pacienteService, psicologoService);

        paciente = Paciente.builder().id(1L).nome("João Silva").build();
        psicologo = Psicologo.builder().id(1L).nome("Dra. Maria").build();
        dataHoraBase = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void deveAgendarSessaoQuandoNaoHaConflito() {
        SessaoRequestDTO dto = new SessaoRequestDTO(1L, 1L, dataHoraBase, 50, BigDecimal.valueOf(150), "Primeira sessão");

        when(pacienteService.buscarEntidadePorId(1L)).thenReturn(paciente);
        when(psicologoService.buscarEntidadePorId(1L)).thenReturn(psicologo);
        when(sessaoRepository.findByPsicologoIdAndDataHoraBetween(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> {
            Sessao s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        SessaoResponseDTO resultado = sessaoService.agendar(dto);

        assertThat(resultado.id()).isEqualTo(100L);
        assertThat(resultado.status()).isEqualTo(StatusSessao.AGENDADA);
        verify(sessaoRepository).save(any(Sessao.class));
    }

    @Test
    void deveLancarExcecaoQuandoHorarioExatoJaEstaOcupado() {
        Sessao sessaoExistente = Sessao.builder()
                .id(1L)
                .psicologo(psicologo)
                .paciente(paciente)
                .dataHora(dataHoraBase)
                .duracaoMinutos(50)
                .status(StatusSessao.AGENDADA)
                .build();

        SessaoRequestDTO dto = new SessaoRequestDTO(1L, 1L, dataHoraBase, 50, BigDecimal.valueOf(150), null);

        when(pacienteService.buscarEntidadePorId(1L)).thenReturn(paciente);
        when(psicologoService.buscarEntidadePorId(1L)).thenReturn(psicologo);
        when(sessaoRepository.findByPsicologoIdAndDataHoraBetween(anyLong(), any(), any()))
                .thenReturn(List.of(sessaoExistente));

        assertThatThrownBy(() -> sessaoService.agendar(dto))
                .isInstanceOf(SessaoConflitanteException.class)
                .hasMessageContaining("conflita");

        verify(sessaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoHorariosSeSobrepoemParcialmente() {
        Sessao sessaoExistente = Sessao.builder()
                .id(1L)
                .psicologo(psicologo)
                .paciente(paciente)
                .dataHora(dataHoraBase)
                .duracaoMinutos(50)
                .status(StatusSessao.AGENDADA)
                .build();

        LocalDateTime novoHorario = dataHoraBase.plusMinutes(30);
        SessaoRequestDTO dto = new SessaoRequestDTO(1L, 1L, novoHorario, 50, BigDecimal.valueOf(150), null);

        when(pacienteService.buscarEntidadePorId(1L)).thenReturn(paciente);
        when(psicologoService.buscarEntidadePorId(1L)).thenReturn(psicologo);
        when(sessaoRepository.findByPsicologoIdAndDataHoraBetween(anyLong(), any(), any()))
                .thenReturn(List.of(sessaoExistente));

        assertThatThrownBy(() -> sessaoService.agendar(dto))
                .isInstanceOf(SessaoConflitanteException.class);

        verify(sessaoRepository, never()).save(any());
    }

    @Test
    void devePermitirAgendarQuandoNaoHaSobreposicaoDeHorarios() {
        Sessao sessaoExistente = Sessao.builder()
                .id(1L)
                .psicologo(psicologo)
                .paciente(paciente)
                .dataHora(dataHoraBase)
                .duracaoMinutos(50)
                .status(StatusSessao.AGENDADA)
                .build();

        LocalDateTime novoHorario = dataHoraBase.plusMinutes(50);
        SessaoRequestDTO dto = new SessaoRequestDTO(1L, 1L, novoHorario, 50, BigDecimal.valueOf(150), null);

        when(pacienteService.buscarEntidadePorId(1L)).thenReturn(paciente);
        when(psicologoService.buscarEntidadePorId(1L)).thenReturn(psicologo);
        when(sessaoRepository.findByPsicologoIdAndDataHoraBetween(anyLong(), any(), any()))
                .thenReturn(List.of(sessaoExistente));
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> {
            Sessao s = invocation.getArgument(0);
            s.setId(200L);
            return s;
        });

        SessaoResponseDTO resultado = sessaoService.agendar(dto);

        assertThat(resultado.id()).isEqualTo(200L);
        verify(sessaoRepository).save(any(Sessao.class));
    }

    @Test
    void naoDeveConsiderarSessaoCanceladaComoConflito() {
        Sessao sessaoCancelada = Sessao.builder()
                .id(1L)
                .psicologo(psicologo)
                .paciente(paciente)
                .dataHora(dataHoraBase)
                .duracaoMinutos(50)
                .status(StatusSessao.CANCELADA)
                .build();

        SessaoRequestDTO dto = new SessaoRequestDTO(1L, 1L, dataHoraBase, 50, BigDecimal.valueOf(150), null);

        when(pacienteService.buscarEntidadePorId(1L)).thenReturn(paciente);
        when(psicologoService.buscarEntidadePorId(1L)).thenReturn(psicologo);
        when(sessaoRepository.findByPsicologoIdAndDataHoraBetween(anyLong(), any(), any()))
                .thenReturn(List.of(sessaoCancelada));
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> {
            Sessao s = invocation.getArgument(0);
            s.setId(300L);
            return s;
        });

        SessaoResponseDTO resultado = sessaoService.agendar(dto);

        assertThat(resultado.id()).isEqualTo(300L);
    }

    @Test
    void deveAtualizarStatusDaSessao() {
        Sessao sessao = Sessao.builder()
                .id(1L)
                .paciente(paciente)
                .psicologo(psicologo)
                .dataHora(dataHoraBase)
                .duracaoMinutos(50)
                .status(StatusSessao.AGENDADA)
                .valor(BigDecimal.TEN)
                .build();

        when(sessaoRepository.findById(1L)).thenReturn(java.util.Optional.of(sessao));
        when(sessaoRepository.save(any(Sessao.class))).thenReturn(sessao);

        SessaoResponseDTO resultado = sessaoService.atualizarStatus(1L, new SessaoStatusUpdateDTO(StatusSessao.REALIZADA));

        assertThat(resultado.status()).isEqualTo(StatusSessao.REALIZADA);
    }
}
