package com.psiclinic.controller;

import com.psiclinic.dto.PacienteRequestDTO;
import com.psiclinic.dto.PacienteResponseDTO;
import com.psiclinic.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Gerenciamento de pacientes da clínica")
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    @Operation(summary = "Cadastrar um novo paciente")
    public ResponseEntity<PacienteResponseDTO> criar(@Valid @RequestBody PacienteRequestDTO dto) {
        PacienteResponseDTO criado = pacienteService.criar(dto);
        return ResponseEntity.created(URI.create("/pacientes/" + criado.id())).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar todos os pacientes")
    public ResponseEntity<List<PacienteResponseDTO>> listar() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por id")
    public ResponseEntity<PacienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de um paciente")
    public ResponseEntity<PacienteResponseDTO> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody PacienteRequestDTO dto) {
        return ResponseEntity.ok(pacienteService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um paciente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        pacienteService.deletar(id);
    }
}
