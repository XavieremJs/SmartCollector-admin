package br.com.smartcollector.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemRequest(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
        String nome,

        @NotNull(message = "O volume e obrigatorio")
        @DecimalMin(value = "0.01", message = "O volume deve ser maior que zero")
        BigDecimal volume
) {
}
