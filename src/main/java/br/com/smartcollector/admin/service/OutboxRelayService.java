package br.com.smartcollector.admin.service;

import br.com.smartcollector.admin.model.OutboxEvento;
import br.com.smartcollector.admin.repository.OutboxEventoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Relay do outbox: le os eventos pendentes no Oracle e delega a projecao
 * no MySQL.
 *
 * Ponto importante: as duas gravacoes NAO estao na mesma transacao — nao ha
 * transacao distribuida aqui. A ordem e deliberada:
 *
 *   1. projeta no MySQL (idempotente)
 *   2. marca como PROCESSADO no Oracle
 *
 * Se o processo cair entre 1 e 2, o evento sera reentregue e a tabela
 * evento_aplicado descarta a repeticao. A garantia e "pelo menos uma vez"
 * com efeito exatamente uma vez — nunca perda silenciosa.
 */
@Service
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

    private final OutboxEventoRepository outboxRepository;
    private final ProjecaoService projecaoService;

    @Value("${app.outbox.lote:50}")
    private int tamanhoDoLote;

    public OutboxRelayService(OutboxEventoRepository outboxRepository,
                              ProjecaoService projecaoService) {
        this.outboxRepository = outboxRepository;
        this.projecaoService = projecaoService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.intervalo-ms:10000}")
    @Transactional(transactionManager = "oracleTransactionManager")
    public void processarPendentes() {

        List<OutboxEvento> pendentes =
                outboxRepository.buscarPendentesComTrava(Limit.of(tamanhoDoLote));

        if (pendentes.isEmpty()) {
            return;
        }

        log.debug("Relay: {} evento(s) pendente(s).", pendentes.size());

        for (OutboxEvento evento : pendentes) {
            try {
                projecaoService.aplicar(evento.getId(),
                                        evento.getTipoEvento(),
                                        evento.getPayload());
                evento.marcarProcessado();

            } catch (Exception e) {
                evento.registrarFalha(e.getMessage());
                log.error("Falha ao projetar o evento {} (tentativa {}/{}): {}",
                          evento.getId(), evento.getTentativas(),
                          OutboxEvento.MAX_TENTATIVAS, e.getMessage());
            }
        }
    }

    public long pendentes() {
        return outboxRepository.countByStatus(OutboxEvento.Status.PENDENTE);
    }

    public long emFalha() {
        return outboxRepository.countByStatus(OutboxEvento.Status.FALHA);
    }
}
