package br.com.smartcollector.api.service;

import br.com.smartcollector.api.dto.CatadorRequest;
import br.com.smartcollector.api.exception.RecursoNaoEncontradoException;
import br.com.smartcollector.api.exception.RegraNegocioException;
import br.com.smartcollector.api.model.Catador;
import br.com.smartcollector.api.model.Usuario;
import br.com.smartcollector.api.repository.CatadorRepository;
import br.com.smartcollector.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatadorService {

    private final CatadorRepository catadorRepository;
    private final UsuarioRepository usuarioRepository;

    public CatadorService(CatadorRepository catadorRepository, UsuarioRepository usuarioRepository) {
        this.catadorRepository = catadorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Catador> listar() {
        return catadorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Catador buscar(Long id) {
        return catadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Catador", id));
    }

    @Transactional
    public Catador criar(CatadorRequest requisicao) {
        if (catadorRepository.existsById(requisicao.idUsuario())) {
            throw new RegraNegocioException(
                    "O usuario " + requisicao.idUsuario() + " ja e um catador.");
        }

        Usuario usuario = usuarioRepository.findById(requisicao.idUsuario())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", requisicao.idUsuario()));

        Catador catador = new Catador();
        catador.setUsuario(usuario);
        catador.setCapacidadeTotal(requisicao.capacidadeTotal());

        return catadorRepository.save(catador);
    }

    @Transactional
    public Catador atualizar(Long id, CatadorRequest requisicao) {
        Catador catador = buscar(id);
        catador.setCapacidadeTotal(requisicao.capacidadeTotal());

        return catadorRepository.save(catador);
    }

    @Transactional
    public void remover(Long id) {
        catadorRepository.delete(buscar(id));
    }
}
