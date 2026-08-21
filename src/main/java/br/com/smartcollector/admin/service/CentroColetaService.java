package br.com.smartcollector.admin.service;

import br.com.smartcollector.admin.model.CentroColeta;
import br.com.smartcollector.admin.repository.CentroColetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CentroColetaService {

    private final CentroColetaRepository centroRepository;

    public CentroColetaService(CentroColetaRepository centroRepository) {
        this.centroRepository = centroRepository;
    }

    @Transactional(readOnly = true)
    public List<CentroColeta> listarTodos() {
        return centroRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CentroColeta> buscarPorEndereco(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return centroRepository.findByEnderecoContainingIgnoreCaseOrderByEndereco(termo.trim());
    }

    @Transactional(readOnly = true)
    public List<CentroColeta> proximosDaCapacidade() {
        return centroRepository.buscarProximosDaCapacidade();
    }

    @Transactional
    public void salvar(CentroColeta centro) {
        if (centro.getVolumeAtual().compareTo(centro.getVolumeTotal()) > 0) {
            throw new IllegalArgumentException(
                "O volume atual nao pode ser maior que a capacidade total do centro");
        }
        centroRepository.save(centro);
    }

    @Transactional
    public void excluir(CentroColeta centro) {
        centroRepository.delete(centro);
    }
}
