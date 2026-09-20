package com.psiclinic.controller;

import com.psiclinic.dto.PsicologoRequestDTO;
import com.psiclinic.dto.PsicologoResponseDTO;
import com.psiclinic.service.PsicologoService;
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
@RequestMapping("/psicologos")
@RequiredArgsConstructor
@Tag(name = "Psicólogos", description = "Gerenciamento de psicólogos da clínica")
public class PsicologoController {

    private final PsicologoService psicologoService;

    @PostMapping
    @Operation(summary = "Cadastrar um novo psicólogo")
    public ResponseEntity<PsicologoResponseDTO> criar(@Valid @RequestBody PsicologoRequestDTO dto) {
        PsicologoResponseDTO criado = psicologoService.criar(dto);
        return ResponseEntity.created(URI.create("/psicologos/" + criado.id())).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar todos os psicólogos")
    public ResponseEntity<List<PsicologoResponseDTO>> listar() {
        return ResponseEntity.ok(psicologoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar psicólogo por id")
    public ResponseEntity<PsicologoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(psicologoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de um psicólogo")
    public ResponseEntity<PsicologoResponseDTO> atualizar(@PathVariable Long id,
                                                            @Valid @RequestBody PsicologoRequestDTO dto) {
        return ResponseEntity.ok(psicologoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um psicólogo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        psicologoService.deletar(id);
    }
}
