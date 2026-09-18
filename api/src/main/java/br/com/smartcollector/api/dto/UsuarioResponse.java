package br.com.smartcollector.api.dto;

import br.com.smartcollector.api.model.Usuario;
import br.com.smartcollector.api.model.UsuarioRole;

/**
 * Resposta publica de usuario. A senha nunca sai da aplicacao — e por isso que
 * os controllers devolvem este record, e nao a entidade.
 */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        UsuarioRole funcao
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(),
                                   usuario.getEmail(), usuario.getFuncao());
    }
}
