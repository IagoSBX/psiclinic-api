package com.psiclinic.repository;

import com.psiclinic.model.Sessao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    Page<Sessao> findByPsicologoId(Long psicologoId, Pageable pageable);

    Page<Sessao> findByPacienteId(Long pacienteId, Pageable pageable);

    Page<Sessao> findByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable);

    List<Sessao> findByPsicologoIdAndDataHoraBetween(Long psicologoId, LocalDateTime inicio, LocalDateTime fim);
}
