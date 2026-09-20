package com.psiclinic.service;

import com.psiclinic.dto.PsicologoRequestDTO;
import com.psiclinic.dto.PsicologoResponseDTO;
import com.psiclinic.exception.DuplicateResourceException;
import com.psiclinic.mapper.PsicologoMapper;
import com.psiclinic.model.Psicologo;
import com.psiclinic.repository.PsicologoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsicologoServiceTest {

    @Mock
    private PsicologoRepository psicologoRepository;

    private PsicologoService psicologoService;

    @BeforeEach
    void setUp() {
        psicologoService = new PsicologoService(psicologoRepository, new PsicologoMapper());
    }

    @Test
    void deveCriarPsicologoQuandoCrpNaoExiste() {
        PsicologoRequestDTO dto = new PsicologoRequestDTO("Dra. Maria", "06/12345", "Terapia Cognitivo-Comportamental",
                "maria@email.com", "11988888888");

        when(psicologoRepository.existsByCrp("06/12345")).thenReturn(false);
        when(psicologoRepository.save(any(Psicologo.class))).thenAnswer(invocation -> {
            Psicologo p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PsicologoResponseDTO resultado = psicologoService.criar(dto);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.crp()).isEqualTo("06/12345");
    }

    @Test
    void deveLancarExcecaoQuandoCrpJaExiste() {
        PsicologoRequestDTO dto = new PsicologoRequestDTO("Dra. Maria", "06/12345", "Terapia Cognitivo-Comportamental",
                "maria@email.com", "11988888888");

        when(psicologoRepository.existsByCrp("06/12345")).thenReturn(true);

        assertThatThrownBy(() -> psicologoService.criar(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("06/12345");

        verify(psicologoRepository, never()).save(any());
    }
}
