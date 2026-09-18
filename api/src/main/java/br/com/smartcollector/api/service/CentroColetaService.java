package br.com.smartcollector.api.service;

import br.com.smartcollector.api.dto.CentroColetaRequest;
import br.com.smartcollector.api.exception.RecursoNaoEncontradoException;
import br.com.smartcollector.api.exception.RegraNegocioException;
import br.com.smartcollector.api.model.CentroColeta;
import br.com.smartcollector.api.repository.CentroColetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CentroColetaService {

    private final CentroColetaRepository centroRepository;

    public CentroColetaService(CentroColetaRepository centroRepository) {
        this.centroRepository = centroRepository;
    }

    @Transactional(readOnly = true)
    public List<CentroColeta> listar() {
        return centroRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CentroColeta buscar(Long id) {
        return centroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Centro de coleta", id));
    }

    @Transactional
    public CentroColeta criar(CentroColetaRequest requisicao) {
        CentroColeta centro = new CentroColeta();
        centro.setEndereco(requisicao.endereco());
        centro.setVolumeTotal(requisicao.volumeTotal());
        centro.setVolumeAtual(BigDecimal.ZERO);

        return centroRepository.save(centro);
    }

    @Transactional
    public CentroColeta atualizar(Long id, CentroColetaRequest requisicao) {
        CentroColeta centro = buscar(id);

        // Reduzir a capacidade abaixo do que ja esta armazenado deixaria o
        // centro num estado que a propria constraint do banco rejeita.
        if (requisicao.volumeTotal().compareTo(centro.getVolumeAtual()) < 0) {
            throw new RegraNegocioException(
                    "O volume total nao pode ser menor que o volume ja ocupado ("
                    + centro.getVolumeAtual() + ").");
        }

        centro.setEndereco(requisicao.endereco());
        centro.setVolumeTotal(requisicao.volumeTotal());

        return centroRepository.save(centro);
    }

    @Transactional
    public void remover(Long id) {
        centroRepository.delete(buscar(id));
    }
}
