package br.com.smartcollector.admin.readmodel.repository;

import br.com.smartcollector.admin.readmodel.model.HistoricoColeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoricoColetaRepository extends JpaRepository<HistoricoColeta, Long> {

    List<HistoricoColeta> findByIdCatadorOrderByDataEntregaDesc(Long idCatador);

    List<HistoricoColeta> findByIdCatadorAndAnoMesOrderByDataEntregaDesc(Long idCatador,
                                                                        String anoMes);

    @Query("SELECT DISTINCT h.anoMes FROM HistoricoColeta h ORDER BY h.anoMes DESC")
    List<String> listarMesesDisponiveis();
}
