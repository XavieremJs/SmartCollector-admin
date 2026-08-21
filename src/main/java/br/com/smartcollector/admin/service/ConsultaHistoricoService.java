package br.com.smartcollector.admin.service;

import br.com.smartcollector.admin.readmodel.model.HistoricoColeta;
import br.com.smartcollector.admin.readmodel.model.RelatorioMensal;
import br.com.smartcollector.admin.readmodel.repository.HistoricoColetaRepository;
import br.com.smartcollector.admin.readmodel.repository.RelatorioMensalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Consultas do painel. Todas leem o MySQL — nenhuma toca o Oracle.
 */
@Service
public class ConsultaHistoricoService {

    private final HistoricoColetaRepository historicoRepository;
    private final RelatorioMensalRepository relatorioRepository;

    public ConsultaHistoricoService(HistoricoColetaRepository historicoRepository,
                                    RelatorioMensalRepository relatorioRepository) {
        this.historicoRepository = historicoRepository;
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional(transactionManager = "mysqlTransactionManager", readOnly = true)
    public List<HistoricoColeta> historicoDoCatador(Long idCatador, String anoMes) {
        if (anoMes == null || anoMes.isBlank()) {
            return historicoRepository.findByIdCatadorOrderByDataEntregaDesc(idCatador);
        }
        return historicoRepository
                .findByIdCatadorAndAnoMesOrderByDataEntregaDesc(idCatador, anoMes);
    }

    @Transactional(transactionManager = "mysqlTransactionManager", readOnly = true)
    public List<String> mesesDisponiveis() {
        return historicoRepository.listarMesesDisponiveis();
    }

    @Transactional(transactionManager = "mysqlTransactionManager", readOnly = true)
    public List<RelatorioMensal> rankingDoMes(String anoMes) {
        return relatorioRepository.findByAnoMesOrderByVolumeTotalDesc(anoMes);
    }

    @Transactional(transactionManager = "mysqlTransactionManager", readOnly = true)
    public Optional<RelatorioMensal> resumoDoCatador(String anoMes, Long idCatador) {
        return relatorioRepository.findByAnoMesAndIdCatador(anoMes, idCatador);
    }
}
