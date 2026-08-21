package br.com.smartcollector.admin.repository;

import br.com.smartcollector.admin.model.OutboxEvento;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface OutboxEventoRepository extends JpaRepository<OutboxEvento, Long> {

    /**
     * Busca eventos pendentes travando as linhas com SKIP LOCKED, para que
     * varias instancias da aplicacao possam rodar o relay sem processar
     * o mesmo evento duas vezes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("SELECT e FROM OutboxEvento e WHERE e.status = 'PENDENTE' ORDER BY e.criadoEm ASC")
    List<OutboxEvento> buscarPendentesComTrava(Limit limite);

    long countByStatus(OutboxEvento.Status status);
}
