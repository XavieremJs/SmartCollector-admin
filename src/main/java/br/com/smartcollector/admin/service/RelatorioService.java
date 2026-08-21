package br.com.smartcollector.admin.service;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Servico que consome as rotinas PL/SQL do pacote PKG_RELATORIO.
 * A regra de negocio de volume fica no banco para garantir atomicidade
 * mesmo quando a coleta e registrada por outra aplicacao.
 */
@Service
public class RelatorioService {

    private final DataSource dataSource;
    private final JdbcClient jdbcClient;

    public RelatorioService(DataSource dataSource, JdbcClient jdbcClient) {
        this.dataSource = dataSource;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Chama a function PKG_RELATORIO.FN_CAPACIDADE_DISPONIVEL.
     */
    @Transactional(readOnly = true)
    public BigDecimal capacidadeDisponivel(Long idCatador) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                 "{ ? = call PKG_RELATORIO.FN_CAPACIDADE_DISPONIVEL(?) }")) {

            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setLong(2, idCatador);
            cs.execute();
            return cs.getBigDecimal(1);

        } catch (SQLException e) {
            throw new IllegalStateException(
                "Falha ao consultar a capacidade disponivel do catador " + idCatador, e);
        }
    }

    /**
     * Chama a procedure PKG_RELATORIO.PRC_REGISTRAR_ENTREGA, que atualiza o
     * volume do centro e marca os itens como entregues numa unica transacao.
     */
    @Transactional
    public String registrarEntrega(Long idColeta, Long idCentro) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                 "{ call PKG_RELATORIO.PRC_REGISTRAR_ENTREGA(?, ?, ?) }")) {

            cs.setLong(1, idColeta);
            cs.setLong(2, idCentro);
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            return cs.getString(3);

        } catch (SQLException e) {
            throw new IllegalStateException(
                "Falha ao registrar a entrega da coleta " + idColeta, e);
        }
    }

    /**
     * Le a view V_OCUPACAO_CENTROS, alimentada pelas rotinas do pacote.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> ocupacaoPorCentro() {
        return jdbcClient.sql("""
                SELECT endereco,
                       volume_itens_total  AS total,
                       volume_itens_atual  AS atual,
                       percentual_ocupacao AS percentual
                  FROM V_OCUPACAO_CENTROS
                 ORDER BY percentual_ocupacao DESC
                """)
                .query()
                .listOfRows();
    }
}
