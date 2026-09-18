package br.com.smartcollector.api.dto;

import br.com.smartcollector.api.model.UsuarioRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
        String nome,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Email invalido")
        @Size(max = 120, message = "O email deve ter no maximo 120 caracteres")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 8, message = "A senha deve ter no minimo 8 caracteres")
        String senha,

        @NotNull(message = "A funcao e obrigatoria")
        UsuarioRole funcao
) {
}
