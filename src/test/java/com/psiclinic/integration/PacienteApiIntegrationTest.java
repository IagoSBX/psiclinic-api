package com.psiclinic.integration;

import com.psiclinic.repository.PacienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração ponta a ponta: sobe o contexto Spring completo
 * (controller -> service -> mapper -> repositório JPA) contra um banco
 * H2 em memória (veja src/test/resources/application.yml), sem mockar
 * nenhuma camada. Cobre o fluxo real de cadastro de paciente, incluindo
 * a regra de negócio de CPF único validada via banco de dados.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PacienteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @AfterEach
    void limparBanco() {
        pacienteRepository.deleteAll();
    }

    private static String jsonPaciente(String cpf) {
        return """
                {
                  "nome": "João da Silva",
                  "cpf": "%s",
                  "dataNascimento": "1990-05-14",
                  "telefone": "11999999999",
                  "email": "joao.silva@email.com",
                  "endereco": "Rua das Flores, 123 - São Paulo/SP"
                }
                """.formatted(cpf);
    }

    @Test
    void deveCadastrarEEntaoBuscarPacientePersistido() throws Exception {
        String location = mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPaciente("12345678900")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.nome").value("João da Silva"))
                .andExpect(jsonPath("$.dataCadastro").exists());

        org.assertj.core.api.Assertions.assertThat(pacienteRepository.existsByCpf("12345678900")).isTrue();
    }

    @Test
    void deveRetornar409AoCadastrarCpfJaExistenteNoBanco() throws Exception {
        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPaciente("98765432100")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPaciente("98765432100")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("98765432100")));

        org.assertj.core.api.Assertions.assertThat(pacienteRepository.findAll()).hasSize(1);
    }

    @Test
    void deveRetornar404AoBuscarPacienteInexistente() throws Exception {
        mockMvc.perform(get("/pacientes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
