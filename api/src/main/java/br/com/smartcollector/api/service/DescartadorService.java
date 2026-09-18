package br.com.smartcollector.api.service;

import br.com.smartcollector.api.dto.DescartadorRequest;
import br.com.smartcollector.api.exception.RecursoNaoEncontradoException;
import br.com.smartcollector.api.exception.RegraNegocioException;
import br.com.smartcollector.api.model.Descartador;
import br.com.smartcollector.api.model.Usuario;
import br.com.smartcollector.api.repository.DescartadorRepository;
import br.com.smartcollector.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DescartadorService {

    private final DescartadorRepository descartadorRepository;
    private final UsuarioRepository usuarioRepository;

    public DescartadorService(DescartadorRepository descartadorRepository,
                              UsuarioRepository usuarioRepository) {
        this.descartadorRepository = descartadorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Descartador> listar() {
        return descartadorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Descartador buscar(Long id) {
        return descartadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Descartador", id));
    }

    @Transactional
    public Descartador criar(DescartadorRequest requisicao) {
        if (descartadorRepository.existsById(requisicao.idUsuario())) {
            throw new RegraNegocioException(
                    "O usuario " + requisicao.idUsuario() + " ja e um descartador.");
        }

        Usuario usuario = usuarioRepository.findById(requisicao.idUsuario())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", requisicao.idUsuario()));

        Descartador descartador = new Descartador();
        descartador.setUsuario(usuario);
        descartador.setEndereco(requisicao.endereco());

        return descartadorRepository.save(descartador);
    }

    @Transactional
    public Descartador atualizar(Long id, DescartadorRequest requisicao) {
        Descartador descartador = buscar(id);
        descartador.setEndereco(requisicao.endereco());

        return descartadorRepository.save(descartador);
    }

    @Transactional
    public void remover(Long id) {
        descartadorRepository.delete(buscar(id));
    }
}
