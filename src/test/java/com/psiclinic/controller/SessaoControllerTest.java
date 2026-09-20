package com.psiclinic.controller;

import com.psiclinic.dto.SessaoRequestDTO;
import com.psiclinic.dto.SessaoResponseDTO;
import com.psiclinic.exception.SessaoConflitanteException;
import com.psiclinic.model.StatusSessao;
import com.psiclinic.service.SessaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de camada web (controller) do SessaoController, com o
 * SessaoService mockado.
 */
@WebMvcTest(SessaoController.class)
class SessaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessaoService sessaoService;

    private static SessaoResponseDTO sessaoResposta(Long id, StatusSessao status) {
        return new SessaoResponseDTO(
                id,
                new SessaoResponseDTO.PacienteResumoDTO(1L, "João da Silva"),
                new SessaoResponseDTO.PsicologoResumoDTO(1L, "Dra. Maria Souza"),
                LocalDateTime.of(2026, 10, 1, 14, 0),
                50,
                status,
                new BigDecimal("180.00"),
                "Primeira sessão de avaliação"
        );
    }

    // data sempre no futuro em relação a "agora", para não esbarrar na validação @Future com o passar do tempo
    private static final String DATA_HORA_FUTURA = LocalDateTime.now().plusYears(1)
            .withHour(14).withMinute(0).withSecond(0).withNano(0)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

    private static final String JSON_VALIDO = """
            {
              "pacienteId": 1,
              "psicologoId": 1,
              "dataHora": "%s",
              "duracaoMinutos": 50,
              "valor": 180.00,
              "observacoes": "Primeira sessão de avaliação"
            }
            """.formatted(DATA_HORA_FUTURA);

    @Test
    void deveAgendarSessaoERetornar201() throws Exception {
        when(sessaoService.agendar(any(SessaoRequestDTO.class)))
                .thenReturn(sessaoResposta(1L, StatusSessao.AGENDADA));

        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/sessoes/1"))
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.paciente.nome").value("João da Silva"));
    }

    @Test
    void deveRetornar409QuandoHorarioConflitante() throws Exception {
        when(sessaoService.agendar(any(SessaoRequestDTO.class)))
                .thenThrow(new SessaoConflitanteException(
                        "O psicólogo já possui uma sessão agendada que conflita com o horário informado"));

        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void deveRetornar400QuandoDataHoraNoPassado() throws Exception {
        String jsonInvalido = """
                {
                  "pacienteId": 1,
                  "psicologoId": 1,
                  "dataHora": "2020-01-01T14:00:00",
                  "duracaoMinutos": 50,
                  "valor": 180.00
                }
                """;

        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarSessoesPaginadas() throws Exception {
        var pagina = new PageImpl<>(List.of(sessaoResposta(1L, StatusSessao.AGENDADA)), PageRequest.of(0, 20), 1);
        when(sessaoService.listar(any())).thenReturn(pagina);

        mockMvc.perform(get("/sessoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deveFiltrarSessoesPorPeriodo() throws Exception {
        var pagina = new PageImpl<>(List.of(sessaoResposta(1L, StatusSessao.AGENDADA)), PageRequest.of(0, 20), 1);
        when(sessaoService.listarPorPeriodo(any(), any(), any())).thenReturn(pagina);

        mockMvc.perform(get("/sessoes")
                        .param("dataInicio", "2026-10-01T00:00:00")
                        .param("dataFim", "2026-10-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(sessaoService).listarPorPeriodo(any(), any(), any());
    }

    @Test
    void deveAtualizarStatusDaSessao() throws Exception {
        when(sessaoService.atualizarStatus(eq(1L), any())).thenReturn(sessaoResposta(1L, StatusSessao.REALIZADA));

        mockMvc.perform(put("/sessoes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"REALIZADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REALIZADA"));
    }

    @Test
    void deveDeletarSessaoERetornar204() throws Exception {
        mockMvc.perform(delete("/sessoes/1"))
                .andExpect(status().isNoContent());

        verify(sessaoService).deletar(1L);
    }

    @Test
    void deveListarSessoesDeUmPsicologo() throws Exception {
        var pagina = new PageImpl<>(List.of(sessaoResposta(1L, StatusSessao.AGENDADA)), PageRequest.of(0, 20), 1);
        when(sessaoService.listarPorPsicologo(eq(1L), any())).thenReturn(pagina);

        mockMvc.perform(get("/sessoes/psicologo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void deveListarSessoesDeUmPaciente() throws Exception {
        var pagina = new PageImpl<>(List.of(sessaoResposta(1L, StatusSessao.AGENDADA)), PageRequest.of(0, 20), 1);
        when(sessaoService.listarPorPaciente(eq(1L), any())).thenReturn(pagina);

        mockMvc.perform(get("/sessoes/paciente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
