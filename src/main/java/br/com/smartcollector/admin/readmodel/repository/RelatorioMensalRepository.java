package br.com.smartcollector.admin.readmodel.repository;

import br.com.smartcollector.admin.readmodel.model.RelatorioMensal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelatorioMensalRepository
        extends JpaRepository<RelatorioMensal, RelatorioMensal.Chave> {

    Optional<RelatorioMensal> findByAnoMesAndIdCatador(String anoMes, Long idCatador);

    List<RelatorioMensal> findByAnoMesOrderByVolumeTotalDesc(String anoMes);
}
