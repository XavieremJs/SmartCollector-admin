package br.com.smartcollector.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CentroColetaRequest(

        @NotBlank(message = "O endereco e obrigatorio")
        @Size(max = 200, message = "O endereco deve ter no maximo 200 caracteres")
        String endereco,

        @NotNull(message = "O volume total e obrigatorio")
        @DecimalMin(value = "0.0", message = "O volume total nao pode ser negativo")
        BigDecimal volumeTotal
) {
}
