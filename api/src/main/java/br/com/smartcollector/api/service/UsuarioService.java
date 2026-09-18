package br.com.smartcollector.api.service;

import br.com.smartcollector.api.dto.UsuarioRequest;
import br.com.smartcollector.api.exception.RecursoNaoEncontradoException;
import br.com.smartcollector.api.exception.RegraNegocioException;
import br.com.smartcollector.api.model.Usuario;
import br.com.smartcollector.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }

    @Transactional
    public Usuario criar(UsuarioRequest requisicao) {
        if (usuarioRepository.existsByEmail(requisicao.email())) {
            throw new RegraNegocioException("Ja existe um usuario com o email " + requisicao.email());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.nome());
        usuario.setEmail(requisicao.email());
        usuario.setSenha(passwordEncoder.encode(requisicao.senha()));
        usuario.setFuncao(requisicao.funcao());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequest requisicao) {
        Usuario usuario = buscar(id);

        usuarioRepository.findByEmail(requisicao.email())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new RegraNegocioException(
                            "Ja existe um usuario com o email " + requisicao.email());
                });

        usuario.setNome(requisicao.nome());
        usuario.setEmail(requisicao.email());
        usuario.setSenha(passwordEncoder.encode(requisicao.senha()));
        usuario.setFuncao(requisicao.funcao());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void remover(Long id) {
        usuarioRepository.delete(buscar(id));
    }
}
