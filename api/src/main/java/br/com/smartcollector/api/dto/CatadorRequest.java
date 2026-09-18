package br.com.smartcollector.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CatadorRequest(

        @NotNull(message = "O id do usuario e obrigatorio")
        Long idUsuario,

        @NotNull(message = "A capacidade total e obrigatoria")
        @DecimalMin(value = "0.01", message = "A capacidade deve ser maior que zero")
        BigDecimal capacidadeTotal
) {
}
