package br.com.smartcollector.admin.repository;

import br.com.smartcollector.admin.model.CentroColeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CentroColetaRepository extends JpaRepository<CentroColeta, Long> {

    List<CentroColeta> findByEnderecoContainingIgnoreCaseOrderByEndereco(String endereco);

    @Query("SELECT c FROM CentroColeta c WHERE c.volumeTotal > 0 "
         + "AND (c.volumeAtual / c.volumeTotal) >= 0.9 ORDER BY c.volumeAtual DESC")
    List<CentroColeta> buscarProximosDaCapacidade();
}
