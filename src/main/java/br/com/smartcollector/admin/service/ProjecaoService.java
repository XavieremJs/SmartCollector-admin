package br.com.smartcollector.admin.service;

import br.com.smartcollector.admin.readmodel.model.EventoAplicado;
import br.com.smartcollector.admin.readmodel.model.HistoricoColeta;
import br.com.smartcollector.admin.readmodel.model.RelatorioMensal;
import br.com.smartcollector.admin.readmodel.repository.EventoAplicadoRepository;
import br.com.smartcollector.admin.readmodel.repository.HistoricoColetaRepository;
import br.com.smartcollector.admin.readmodel.repository.RelatorioMensalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Aplica os eventos do outbox no read model (MySQL).
 *
 * Roda numa transacao do mysqlTransactionManager, separada da transacao
 * do Oracle. A idempotencia e garantida pela tabela evento_aplicado:
 * se o mesmo evento chegar duas vezes, a segunda e ignorada.
 */
@Service
public class ProjecaoService {

    private static final Logger log = LoggerFactory.getLogger(ProjecaoService.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final HistoricoColetaRepository historicoRepository;
    private final RelatorioMensalRepository relatorioRepository;
    private final EventoAplicadoRepository eventoAplicadoRepository;
    private final ObjectMapper objectMapper;

    public ProjecaoService(HistoricoColetaRepository historicoRepository,
                           RelatorioMensalRepository relatorioRepository,
                           EventoAplicadoRepository eventoAplicadoRepository,
                           ObjectMapper objectMapper) {
        this.historicoRepository = historicoRepository;
        this.relatorioRepository = relatorioRepository;
        this.eventoAplicadoRepository = eventoAplicadoRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(transactionManager = "mysqlTransactionManager",
                   propagation = Propagation.REQUIRES_NEW)
    public void aplicar(Long idEvento, String tipoEvento, String payload) throws Exception {

        if (eventoAplicadoRepository.existsById(idEvento)) {
            log.debug("Evento {} ja aplicado; ignorando.", idEvento);
            return;
        }

        if (!"COLETA_ENTREGUE".equals(tipoEvento)) {
            log.warn("Tipo de evento desconhecido: {}. Marcando como aplicado.", tipoEvento);
            eventoAplicadoRepository.save(new EventoAplicado(idEvento, tipoEvento));
            return;
        }

        JsonNode no = objectMapper.readTree(payload);

        Long idColeta        = no.get("idColeta").asLong();
        Long idCatador       = no.get("idCatador").asLong();
        Long idCentro        = no.get("idCentro").asLong();
        String endereco      = no.get("enderecoCentro").asText();
        BigDecimal volume    = new BigDecimal(no.get("volume").asText());
        int qtdItens         = no.get("qtdItens").asInt();
        LocalDateTime data   = LocalDateTime.parse(no.get("dataEntrega").asText(), ISO);
        String anoMes        = data.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        HistoricoColeta historico = new HistoricoColeta();
        historico.setIdColeta(idColeta);
        historico.setIdCatador(idCatador);
        historico.setIdCentro(idCentro);
        historico.setEnderecoCentro(endereco);
        historico.setVolume(volume);
        historico.setQtdItens(qtdItens);
        historico.setDataEntrega(data);
        historico.setAnoMes(anoMes);
        historicoRepository.save(historico);

        RelatorioMensal mensal = relatorioRepository
                .findByAnoMesAndIdCatador(anoMes, idCatador)
                .orElseGet(() -> {
                    RelatorioMensal novo = new RelatorioMensal();
                    novo.setAnoMes(anoMes);
                    novo.setIdCatador(idCatador);
                    return novo;
                });
        mensal.acumular(volume, qtdItens);
        relatorioRepository.save(mensal);

        eventoAplicadoRepository.save(new EventoAplicado(idEvento, tipoEvento));

        log.info("Evento {} aplicado: coleta {} do catador {}.", idEvento, idColeta, idCatador);
    }
}
