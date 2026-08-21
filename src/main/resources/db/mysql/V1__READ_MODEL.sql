-- =====================================================================
-- Read model: tabelas desnormalizadas para consulta.
-- Alimentadas pelo relay do outbox. Nunca recebem escrita direta
-- de regra de negocio.
-- =====================================================================

CREATE TABLE historico_coleta (
    id_coleta        BIGINT        NOT NULL,
    id_catador       BIGINT        NOT NULL,
    id_centro        BIGINT        NOT NULL,
    endereco_centro  VARCHAR(200)  NOT NULL,
    volume           DECIMAL(10,2) NOT NULL,
    qtd_itens        INT           NOT NULL,
    data_entrega     DATETIME      NOT NULL,
    ano_mes          CHAR(7)       NOT NULL,
    PRIMARY KEY (id_coleta),
    KEY idx_hist_catador      (id_catador, data_entrega DESC),
    KEY idx_hist_catador_mes  (id_catador, ano_mes),
    KEY idx_hist_centro       (id_centro, data_entrega DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Agregado mensal por catador, recalculado a cada evento aplicado.
CREATE TABLE relatorio_mensal (
    ano_mes          CHAR(7)       NOT NULL,
    id_catador       BIGINT        NOT NULL,
    total_coletas    INT           NOT NULL DEFAULT 0,
    volume_total     DECIMAL(12,2) NOT NULL DEFAULT 0,
    itens_total      INT           NOT NULL DEFAULT 0,
    atualizado_em    DATETIME      NOT NULL,
    PRIMARY KEY (ano_mes, id_catador),
    KEY idx_rel_mes (ano_mes, volume_total DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Garante idempotencia: um evento reentregue nao e aplicado duas vezes.
CREATE TABLE evento_aplicado (
    id_evento    BIGINT      NOT NULL,
    tipo_evento  VARCHAR(60) NOT NULL,
    aplicado_em  DATETIME    NOT NULL,
    PRIMARY KEY (id_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
