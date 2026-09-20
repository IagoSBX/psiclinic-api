package com.psiclinic.integration;

import com.psiclinic.model.Paciente;
import com.psiclinic.model.Psicologo;
import com.psiclinic.repository.PacienteRepository;
import com.psiclinic.repository.PsicologoRepository;
import com.psiclinic.repository.SessaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração ponta a ponta do fluxo de agendamento de sessões,
 * cobrindo a regra de negócio mais importante do domínio: um psicólogo
 * não pode ter duas sessões que se sobrepõem no tempo. Diferente dos
 * testes unitários de SessaoServiceTest (que mockam o repositório), aqui
 * a checagem de conflito roda contra um banco H2 real.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SessaoApiIntegrationTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private SessaoRepository sessaoRepository;

    private Long pacienteId;
    private Long psicologoId;
    private LocalDateTime horarioBase;

    @BeforeEach
    void criarPacienteEPsicologo() {
        Paciente paciente = pacienteRepository.save(Paciente.builder()
                .nome("João da Silva")
                .cpf("12345678900")
                .dataNascimento(LocalDate.of(1990, 5, 14))
                .build());

        Psicologo psicologo = psicologoRepository.save(Psicologo.builder()
                .nome("Dra. Maria Souza")
                .crp("06/123456")
                .especialidade("TCC")
                .email("maria.souza@email.com")
                .build());

        pacienteId = paciente.getId();
        psicologoId = psicologo.getId();
        // usa uma data bem no futuro para não esbarrar na validação @Future com o passar do tempo
        horarioBase = LocalDateTime.now().plusYears(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
    }

    @AfterEach
    void limparBanco() {
        sessaoRepository.deleteAll();
        psicologoRepository.deleteAll();
        pacienteRepository.deleteAll();
    }

    private String jsonSessao(LocalDateTime dataHora, int duracaoMinutos) {
        return """
                {
                  "pacienteId": %d,
                  "psicologoId": %d,
                  "dataHora": "%s",
                  "duracaoMinutos": %d,
                  "valor": 180.00,
                  "observacoes": "Sessão de teste"
                }
                """.formatted(pacienteId, psicologoId, dataHora.format(ISO), duracaoMinutos);
    }

    @Test
    void deveAgendarSessaoComSucesso() throws Exception {
        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSessao(horarioBase, 50)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.paciente.id").value(pacienteId))
                .andExpect(jsonPath("$.psicologo.id").value(psicologoId));

        assertThat(sessaoRepository.findAll()).hasSize(1);
    }

    @Test
    void deveRecusarSegundaSessaoQueSobrepoeHorarioDoMesmoPsicologo() throws Exception {
        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSessao(horarioBase, 50)))
                .andExpect(status().isCreated());

        // começa 20 minutos depois do início da primeira sessão (que dura 50 min) -> sobrepõe
        LocalDateTime horarioConflitante = horarioBase.plusMinutes(20);

        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSessao(horarioConflitante, 30)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));

        assertThat(sessaoRepository.findAll()).hasSize(1);
    }

    @Test
    void devePermitirSegundaSessaoLogoAposOFimDaPrimeira() throws Exception {
        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSessao(horarioBase, 50)))
                .andExpect(status().isCreated());

        // começa exatamente quando a primeira termina (50 min depois) -> não sobrepõe
        LocalDateTime horarioSeguinte = horarioBase.plusMinutes(50);

        mockMvc.perform(post("/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSessao(horarioSeguinte, 30)))
                .andExpect(status().isCreated());

        assertThat(sessaoRepository.findAll()).hasSize(2);
    }
}
