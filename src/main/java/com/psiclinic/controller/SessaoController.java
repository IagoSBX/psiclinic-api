package com.psiclinic.controller;

import com.psiclinic.dto.SessaoRequestDTO;
import com.psiclinic.dto.SessaoResponseDTO;
import com.psiclinic.dto.SessaoStatusUpdateDTO;
import com.psiclinic.service.SessaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/sessoes")
@RequiredArgsConstructor
@Tag(name = "Sessões", description = "Agendamento e gerenciamento de sessões de psicoterapia")
public class SessaoController {

    private final SessaoService sessaoService;

    @PostMapping
    @Operation(summary = "Agendar uma nova sessão")
    public ResponseEntity<SessaoResponseDTO> agendar(@Valid @RequestBody SessaoRequestDTO dto) {
        SessaoResponseDTO criada = sessaoService.agendar(dto);
        return ResponseEntity.created(URI.create("/sessoes/" + criada.id())).body(criada);
    }

    @GetMapping
    @Operation(summary = "Listar sessões com paginação, ou filtrar por período (dataInicio/dataFim)")
    public ResponseEntity<Page<SessaoResponseDTO>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            Pageable pageable) {

        if (dataInicio != null && dataFim != null) {
            return ResponseEntity.ok(sessaoService.listarPorPeriodo(dataInicio, dataFim, pageable));
        }
        return ResponseEntity.ok(sessaoService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sessão por id")
    public ResponseEntity<SessaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sessaoService.buscarPorId(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar o status de uma sessão")
    public ResponseEntity<SessaoResponseDTO> atualizarStatus(@PathVariable Long id,
                                                               @Valid @RequestBody SessaoStatusUpdateDTO dto) {
        return ResponseEntity.ok(sessaoService.atualizarStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover uma sessão")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        sessaoService.deletar(id);
    }

    @GetMapping("/psicologo/{id}")
    @Operation(summary = "Listar sessões de um psicólogo")
    public ResponseEntity<Page<SessaoResponseDTO>> listarPorPsicologo(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(sessaoService.listarPorPsicologo(id, pageable));
    }

    @GetMapping("/paciente/{id}")
    @Operation(summary = "Listar sessões de um paciente")
    public ResponseEntity<Page<SessaoResponseDTO>> listarPorPaciente(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(sessaoService.listarPorPaciente(id, pageable));
    }
}
