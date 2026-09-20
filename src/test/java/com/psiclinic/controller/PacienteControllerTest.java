package com.psiclinic.controller;

import com.psiclinic.dto.PacienteRequestDTO;
import com.psiclinic.dto.PacienteResponseDTO;
import com.psiclinic.exception.DuplicateResourceException;
import com.psiclinic.exception.ResourceNotFoundException;
import com.psiclinic.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de camada web (controller) do PacienteController.
 * O PacienteService é mockado: aqui validamos apenas roteamento HTTP,
 * (de)serialização JSON, validação de payload e mapeamento de exceções
 * para status HTTP pelo GlobalExceptionHandler.
 */
@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacienteService pacienteService;

    private static final String JSON_VALIDO = """
            {
              "nome": "João da Silva",
              "cpf": "12345678900",
              "dataNascimento": "1990-05-14",
              "telefone": "11999999999",
              "email": "joao.silva@email.com",
              "endereco": "Rua das Flores, 123"
            }
            """;

    @Test
    void deveCriarPacienteERetornar201ComLocationHeader() throws Exception {
        PacienteResponseDTO resposta = new PacienteResponseDTO(1L, "João da Silva", "12345678900",
                LocalDate.of(1990, 5, 14), "11999999999", "joao.silva@email.com",
                "Rua das Flores, 123", LocalDateTime.of(2026, 9, 20, 10, 30));

        when(pacienteService.criar(any(PacienteRequestDTO.class))).thenReturn(resposta);

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/pacientes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cpf").value("12345678900"));
    }

    @Test
    void deveRetornar400QuandoCpfInvalido() throws Exception {
        String jsonInvalido = """
                {
                  "nome": "João da Silva",
                  "cpf": "abc",
                  "dataNascimento": "1990-05-14"
                }
                """;

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(pacienteService, never()).criar(any());
    }

    @Test
    void deveRetornar409QuandoCpfDuplicado() throws Exception {
        when(pacienteService.criar(any(PacienteRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Já existe um paciente cadastrado com o CPF 12345678900"));

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void deveListarTodosOsPacientes() throws Exception {
        PacienteResponseDTO p1 = new PacienteResponseDTO(1L, "João", "11111111111",
                LocalDate.of(1990, 1, 1), null, null, null, LocalDateTime.now());

        when(pacienteService.listarTodos()).thenReturn(List.of(p1));

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deveBuscarPacientePorId() throws Exception {
        PacienteResponseDTO p1 = new PacienteResponseDTO(1L, "João", "11111111111",
                LocalDate.of(1990, 1, 1), null, null, null, LocalDateTime.now());

        when(pacienteService.buscarPorId(1L)).thenReturn(p1);

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    void deveRetornar404QuandoPacienteNaoEncontrado() throws Exception {
        when(pacienteService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Paciente não encontrado com id 99"));

        mockMvc.perform(get("/pacientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void deveAtualizarPaciente() throws Exception {
        PacienteResponseDTO atualizado = new PacienteResponseDTO(1L, "João Atualizado", "12345678900",
                LocalDate.of(1990, 5, 14), "11999999999", "joao.silva@email.com",
                "Rua das Flores, 123", LocalDateTime.now());

        when(pacienteService.atualizar(eq(1L), any(PacienteRequestDTO.class))).thenReturn(atualizado);

        mockMvc.perform(put("/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Atualizado"));
    }

    @Test
    void deveDeletarPacienteERetornar204() throws Exception {
        mockMvc.perform(delete("/pacientes/1"))
                .andExpect(status().isNoContent());

        verify(pacienteService).deletar(1L);
    }
}
