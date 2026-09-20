package com.psiclinic.service;

import com.psiclinic.dto.PacienteRequestDTO;
import com.psiclinic.dto.PacienteResponseDTO;
import com.psiclinic.exception.DuplicateResourceException;
import com.psiclinic.exception.ResourceNotFoundException;
import com.psiclinic.mapper.PacienteMapper;
import com.psiclinic.model.Paciente;
import com.psiclinic.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteService(pacienteRepository, new PacienteMapper());
    }

    @Test
    void deveCriarPacienteQuandoCpfNaoExiste() {
        PacienteRequestDTO dto = new PacienteRequestDTO("João Silva", "12345678900",
                LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com", "Rua A, 123");

        when(pacienteRepository.existsByCpf("12345678900")).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PacienteResponseDTO resultado = pacienteService.criar(dto);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.cpf()).isEqualTo("12345678900");
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaExiste() {
        PacienteRequestDTO dto = new PacienteRequestDTO("João Silva", "12345678900",
                LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com", "Rua A, 123");

        when(pacienteRepository.existsByCpf("12345678900")).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.criar(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("12345678900");

        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoPacienteNaoEncontrado() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComCpfDeOutroPaciente() {
        Paciente existente = Paciente.builder().id(1L).nome("João").cpf("11111111111").build();
        PacienteRequestDTO dto = new PacienteRequestDTO("João Silva", "22222222222",
                LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com", "Rua A, 123");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(pacienteRepository.existsByCpfAndIdNot("22222222222", 1L)).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.atualizar(1L, dto))
                .isInstanceOf(DuplicateResourceException.class);

        verify(pacienteRepository, never()).save(any());
    }
}
