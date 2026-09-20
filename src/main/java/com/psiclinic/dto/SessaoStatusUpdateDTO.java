package com.psiclinic.dto;

import com.psiclinic.model.StatusSessao;
import jakarta.validation.constraints.NotNull;

public record SessaoStatusUpdateDTO(

        @NotNull(message = "Status é obrigatório")
        StatusSessao status
) {
}
