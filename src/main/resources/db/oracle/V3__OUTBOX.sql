-- =====================================================================
-- Outbox transacional
-- O evento e gravado na MESMA transacao da operacao de negocio.
-- Se a coleta e finalizada, o evento existe. Se a transacao falha,
-- nenhum dos dois existe. Nao ha janela de inconsistencia.
-- =====================================================================

CREATE TABLE tb_outbox_evento (
    id             NUMBER(19)     NOT NULL,
    tipo_evento    VARCHAR2(60)   NOT NULL,
    agregado_id    NUMBER(19)     NOT NULL,
    payload        CLOB           NOT NULL,
    status         VARCHAR2(20)   DEFAULT 'PENDENTE' NOT NULL,
    tentativas     NUMBER(3)      DEFAULT 0 NOT NULL,
    ultimo_erro    VARCHAR2(500),
    criado_em      TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    processado_em  TIMESTAMP,
    CONSTRAINT pk_outbox PRIMARY KEY (id),
    CONSTRAINT ck_outbox_status CHECK (status IN ('PENDENTE', 'PROCESSADO', 'FALHA'))
);

CREATE SEQUENCE outbox_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- O relay busca por status + ordem de criacao; este indice cobre a consulta.
CREATE INDEX idx_outbox_pendente ON tb_outbox_evento (status, criado_em);

-- ---------------------------------------------------------------------
-- Publicacao de eventos a partir do pacote de negocio
-- ---------------------------------------------------------------------
CREATE OR REPLACE PACKAGE PKG_OUTBOX AS

    PROCEDURE prc_publicar(p_tipo        IN VARCHAR2,
                           p_agregado_id IN NUMBER,
                           p_payload     IN CLOB);

END PKG_OUTBOX;
/

CREATE OR REPLACE PACKAGE BODY PKG_OUTBOX AS

    PROCEDURE prc_publicar(p_tipo        IN VARCHAR2,
                           p_agregado_id IN NUMBER,
                           p_payload     IN CLOB) IS
    BEGIN
        INSERT INTO tb_outbox_evento (id, tipo_evento, agregado_id, payload)
        VALUES (outbox_seq.NEXTVAL, p_tipo, p_agregado_id, p_payload);
        -- Sem COMMIT: participa da transacao de quem chamou.
    END prc_publicar;

END PKG_OUTBOX;
/

-- ---------------------------------------------------------------------
-- A procedure de entrega passa a publicar o evento antes de encerrar.
-- ---------------------------------------------------------------------
CREATE OR REPLACE PACKAGE BODY PKG_RELATORIO AS

    FUNCTION fn_capacidade_disponivel(p_id_catador IN NUMBER) RETURN NUMBER IS
        v_capacidade  tb_catador.capacidade_volume_total%TYPE;
        v_em_posse    NUMBER := 0;
    BEGIN
        SELECT capacidade_volume_total INTO v_capacidade
          FROM tb_catador WHERE id = p_id_catador;

        SELECT NVL(SUM(i.volume), 0) INTO v_em_posse
          FROM tb_catador_item ci
          JOIN tb_itens i ON i.id = ci.id_item
         WHERE ci.id_catador = p_id_catador AND ci.foi_entregue = 0;

        RETURN GREATEST(v_capacidade - v_em_posse, 0);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN RETURN 0;
    END fn_capacidade_disponivel;

    FUNCTION fn_volume_da_coleta(p_id_coleta IN NUMBER) RETURN NUMBER IS
        v_total NUMBER := 0;
    BEGIN
        SELECT NVL(SUM(i.volume), 0) INTO v_total
          FROM tb_coleta_itens ci
          JOIN tb_itens i ON i.id = ci.id_item
         WHERE ci.id_coleta = p_id_coleta;
        RETURN v_total;
    END fn_volume_da_coleta;

    PROCEDURE prc_registrar_entrega(p_id_coleta IN  NUMBER,
                                    p_id_centro IN  NUMBER,
                                    p_resultado OUT VARCHAR2) IS
        v_volume      NUMBER;
        v_espaco      NUMBER;
        v_finalizada  tb_coletas.foi_finalizada%TYPE;
        v_id_catador  tb_coletas.id_catador%TYPE;
        v_qtd_itens   PLS_INTEGER := 0;
        v_endereco    tb_centros_coleta.endereco%TYPE;
        v_payload     CLOB;

        CURSOR c_itens_da_coleta IS
            SELECT id_item FROM tb_coleta_itens WHERE id_coleta = p_id_coleta;
    BEGIN
        SELECT foi_finalizada, id_catador INTO v_finalizada, v_id_catador
          FROM tb_coletas WHERE id_coleta = p_id_coleta FOR UPDATE;

        IF v_finalizada = 1 THEN
            RAISE_APPLICATION_ERROR(-20002,
                'A coleta ' || p_id_coleta || ' ja foi finalizada.');
        END IF;

        v_volume := fn_volume_da_coleta(p_id_coleta);

        SELECT volume_itens_total - volume_itens_atual, endereco
          INTO v_espaco, v_endereco
          FROM tb_centros_coleta WHERE id = p_id_centro FOR UPDATE;

        IF v_volume > v_espaco THEN
            RAISE_APPLICATION_ERROR(-20001,
                'Espaco insuficiente no centro. Necessario: ' || v_volume ||
                ', disponivel: ' || v_espaco || '.');
        END IF;

        UPDATE tb_centros_coleta
           SET volume_itens_atual = volume_itens_atual + v_volume
         WHERE id = p_id_centro;

        FOR r IN c_itens_da_coleta LOOP
            UPDATE tb_catador_item
               SET foi_entregue = 1
             WHERE id_catador = v_id_catador
               AND id_item = r.id_item
               AND foi_entregue = 0;
            v_qtd_itens := v_qtd_itens + SQL%ROWCOUNT;
        END LOOP;

        UPDATE tb_coletas
           SET foi_finalizada = 1, id_centro = p_id_centro
         WHERE id_coleta = p_id_coleta;

        -- Evento publicado na mesma transacao das alteracoes acima.
        v_payload := '{'
            || '"idColeta":'     || p_id_coleta                        || ','
            || '"idCatador":'    || v_id_catador                       || ','
            || '"idCentro":'     || p_id_centro                        || ','
            || '"enderecoCentro":"' || REPLACE(v_endereco, '"', '\"')  || '",'
            || '"volume":'       || TO_CHAR(v_volume, 'FM99999990.00', 'NLS_NUMERIC_CHARACTERS=''.,''') || ','
            || '"qtdItens":'     || v_qtd_itens                        || ','
            || '"dataEntrega":"' || TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD"T"HH24:MI:SS') || '"'
            || '}';

        PKG_OUTBOX.prc_publicar('COLETA_ENTREGUE', p_id_coleta, v_payload);

        p_resultado := 'Entrega registrada: ' || v_qtd_itens ||
                       ' item(ns), volume ' || v_volume || '.';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20003,
                'Coleta ou centro de coleta nao encontrado.');
    END prc_registrar_entrega;

END PKG_RELATORIO;
/
