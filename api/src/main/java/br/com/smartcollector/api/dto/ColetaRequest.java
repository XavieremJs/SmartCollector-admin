package br.com.smartcollector.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ColetaRequest(

        @NotNull(message = "O id do descartador e obrigatorio")
        Long idDescartador,

        @NotEmpty(message = "A coleta precisa de ao menos um item")
        List<Long> idsItens
) {
}
