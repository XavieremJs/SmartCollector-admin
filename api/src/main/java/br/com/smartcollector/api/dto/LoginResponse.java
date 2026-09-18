package br.com.smartcollector.api.dto;

import br.com.smartcollector.api.model.UsuarioRole;

import java.time.Instant;

public record LoginResponse(
        String token,
        String nome,
        UsuarioRole funcao,
        Instant expiraEm
) {
}
