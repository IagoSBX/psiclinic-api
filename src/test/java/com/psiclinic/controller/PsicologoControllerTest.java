package com.psiclinic.controller;

import com.psiclinic.dto.PsicologoRequestDTO;
import com.psiclinic.dto.PsicologoResponseDTO;
import com.psiclinic.exception.DuplicateResourceException;
import com.psiclinic.exception.ResourceNotFoundException;
import com.psiclinic.service.PsicologoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
 * Testes de camada web (controller) do PsicologoController, com o
 * PsicologoService mockado.
 */
@WebMvcTest(PsicologoController.class)
class PsicologoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PsicologoService psicologoService;

    private static final String JSON_VALIDO = """
            {
              "nome": "Dra. Maria Souza",
              "crp": "06/123456",
              "especialidade": "Terapia Cognitivo-Comportamental",
              "email": "maria.souza@email.com",
              "telefone": "11988888888"
            }
            """;

    @Test
    void deveCriarPsicologoERetornar201ComLocationHeader() throws Exception {
        PsicologoResponseDTO resposta = new PsicologoResponseDTO(1L, "Dra. Maria Souza", "06/123456",
                "Terapia Cognitivo-Comportamental", "maria.souza@email.com", "11988888888");

        when(psicologoService.criar(any(PsicologoRequestDTO.class))).thenReturn(resposta);

        mockMvc.perform(post("/psicologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/psicologos/1"))
                .andExpect(jsonPath("$.crp").value("06/123456"));
    }

    @Test
    void deveRetornar400QuandoNomeEmBranco() throws Exception {
        String jsonInvalido = """
                {
                  "nome": "",
                  "crp": "06/123456",
                  "especialidade": "TCC",
                  "email": "maria.souza@email.com"
                }
                """;

        mockMvc.perform(post("/psicologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409QuandoCrpDuplicado() throws Exception {
        when(psicologoService.criar(any(PsicologoRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Já existe um psicólogo cadastrado com o CRP 06/123456"));

        mockMvc.perform(post("/psicologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void deveListarTodosOsPsicologos() throws Exception {
        PsicologoResponseDTO p1 = new PsicologoResponseDTO(1L, "Dra. Maria Souza", "06/123456",
                "TCC", "maria.souza@email.com", "11988888888");

        when(psicologoService.listarTodos()).thenReturn(List.of(p1));

        mockMvc.perform(get("/psicologos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveRetornar404QuandoPsicologoNaoEncontrado() throws Exception {
        when(psicologoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Psicólogo não encontrado com id 99"));

        mockMvc.perform(get("/psicologos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarPsicologo() throws Exception {
        PsicologoResponseDTO atualizado = new PsicologoResponseDTO(1L, "Dra. Maria Souza Atualizado", "06/123456",
                "TCC", "maria.souza@email.com", "11988888888");

        when(psicologoService.atualizar(eq(1L), any(PsicologoRequestDTO.class))).thenReturn(atualizado);

        mockMvc.perform(put("/psicologos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Dra. Maria Souza Atualizado"));
    }

    @Test
    void deveDeletarPsicologoERetornar204() throws Exception {
        mockMvc.perform(delete("/psicologos/1"))
                .andExpect(status().isNoContent());

        verify(psicologoService).deletar(1L);
    }
}
