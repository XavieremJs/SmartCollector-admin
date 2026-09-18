package br.com.smartcollector.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DescartadorRequest(

        @NotNull(message = "O id do usuario e obrigatorio")
        Long idUsuario,

        @NotBlank(message = "O endereco e obrigatorio")
        @Size(max = 200, message = "O endereco deve ter no maximo 200 caracteres")
        String endereco
) {
}
