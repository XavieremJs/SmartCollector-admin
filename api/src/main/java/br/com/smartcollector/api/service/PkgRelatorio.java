package br.com.smartcollector.api.service;

import br.com.smartcollector.api.exception.RecursoNaoEncontradoException;
import br.com.smartcollector.api.exception.RegraNegocioException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Ponte para o pacote PL/SQL PKG_RELATORIO, criado pelas migracoes do modulo
 * admin. As regras de entrega vivem no banco porque precisam travar linhas e
 * publicar o evento de outbox na mesma transacao — reescreve-las aqui em Java
 * criaria duas versoes da mesma regra.
 */
@Component
public class PkgRelatorio {

    private static final int ERRO_CENTRO_SEM_ESPACO = 20001;
    private static final int ERRO_COLETA_FINALIZADA = 20002;
    private static final int ERRO_NAO_ENCONTRADO = 20003;

    private final JdbcTemplate jdbcTemplate;

    public PkgRelatorio(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BigDecimal capacidadeDisponivel(Long idCatador) {
        return jdbcTemplate.queryForObject(
                "SELECT PKG_RELATORIO.fn_capacidade_disponivel(?) FROM dual",
                BigDecimal.class, idCatador);
    }

    public BigDecimal volumeDaColeta(Long idColeta) {
        return jdbcTemplate.queryForObject(
                "SELECT PKG_RELATORIO.fn_volume_da_coleta(?) FROM dual",
                BigDecimal.class, idColeta);
    }

    public String registrarEntrega(Long idColeta, Long idCentro) {
        try {
            return jdbcTemplate.execute(
                    (java.sql.Connection conexao) -> conexao.prepareCall(
                            "{call PKG_RELATORIO.prc_registrar_entrega(?, ?, ?)}"),
                    (CallableStatement chamada) -> {
                        chamada.setLong(1, idColeta);
                        chamada.setLong(2, idCentro);
                        chamada.registerOutParameter(3, Types.VARCHAR);
                        chamada.execute();
                        return chamada.getString(3);
                    });
        } catch (DataAccessException ex) {
            throw traduzir(ex);
        }
    }

    /**
     * Converte os RAISE_APPLICATION_ERROR do pacote em excecoes da aplicacao,
     * para que o handler devolva 404 ou 409 em vez de 500.
     */
    private RuntimeException traduzir(DataAccessException ex) {
        SQLException sql = procurarSqlException(ex);

        if (sql != null) {
            String mensagem = limpar(sql.getMessage());

            return switch (sql.getErrorCode()) {
                case ERRO_CENTRO_SEM_ESPACO, ERRO_COLETA_FINALIZADA ->
                        new RegraNegocioException(mensagem);
                case ERRO_NAO_ENCONTRADO ->
                        new RecursoNaoEncontradoException(mensagem);
                default -> ex;
            };
        }

        return ex;
    }

    private SQLException procurarSqlException(Throwable erro) {
        while (erro != null) {
            if (erro instanceof SQLException sql) {
                return sql;
            }
            erro = erro.getCause();
        }
        return null;
    }

    /**
     * O Oracle prefixa a mensagem com "ORA-20001: " e anexa a pilha PL/SQL.
     * O cliente da API so precisa da primeira linha, sem o codigo.
     */
    private String limpar(String mensagem) {
        if (mensagem == null) {
            return "Erro ao registrar a entrega.";
        }

        String primeiraLinha = mensagem.split("\\R", 2)[0];
        int separador = primeiraLinha.indexOf(": ");

        return separador >= 0 ? primeiraLinha.substring(separador + 2).trim()
                              : primeiraLinha.trim();
    }
}
