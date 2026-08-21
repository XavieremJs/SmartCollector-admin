-- =====================================================================
-- Pacote de rotinas de negocio e relatorios do SmartCollector
-- =====================================================================

CREATE OR REPLACE VIEW V_OCUPACAO_CENTROS AS
SELECT c.id,
       c.endereco,
       c.volume_itens_total,
       c.volume_itens_atual,
       CASE
           WHEN c.volume_itens_total = 0 THEN 0
           ELSE ROUND(c.volume_itens_atual * 100 / c.volume_itens_total, 1)
       END AS percentual_ocupacao
  FROM tb_centros_coleta c;
/

CREATE OR REPLACE PACKAGE PKG_RELATORIO AS

    -- Erros de negocio expostos a aplicacao
    e_centro_sem_espaco  EXCEPTION;
    e_coleta_finalizada  EXCEPTION;
    PRAGMA EXCEPTION_INIT(e_centro_sem_espaco, -20001);
    PRAGMA EXCEPTION_INIT(e_coleta_finalizada, -20002);

    FUNCTION fn_capacidade_disponivel(p_id_catador IN NUMBER) RETURN NUMBER;

    FUNCTION fn_volume_da_coleta(p_id_coleta IN NUMBER) RETURN NUMBER;

    PROCEDURE prc_registrar_entrega(p_id_coleta IN  NUMBER,
                                    p_id_centro IN  NUMBER,
                                    p_resultado OUT VARCHAR2);

END PKG_RELATORIO;
/

CREATE OR REPLACE PACKAGE BODY PKG_RELATORIO AS

    -- -----------------------------------------------------------------
    -- Capacidade ainda livre do catador: capacidade total menos a soma
    -- do volume dos itens que ele ja recolheu e ainda nao entregou.
    -- -----------------------------------------------------------------
    FUNCTION fn_capacidade_disponivel(p_id_catador IN NUMBER) RETURN NUMBER IS
        v_capacidade  tb_catador.capacidade_volume_total%TYPE;
        v_em_posse    NUMBER := 0;
    BEGIN
        SELECT capacidade_volume_total
          INTO v_capacidade
          FROM tb_catador
         WHERE id = p_id_catador;

        SELECT NVL(SUM(i.volume), 0)
          INTO v_em_posse
          FROM tb_catador_item ci
          JOIN tb_itens i ON i.id = ci.id_item
         WHERE ci.id_catador   = p_id_catador
           AND ci.foi_entregue = 0;

        RETURN GREATEST(v_capacidade - v_em_posse, 0);

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 0;
    END fn_capacidade_disponivel;

    -- -----------------------------------------------------------------
    -- Volume total dos itens que compoem uma coleta.
    -- -----------------------------------------------------------------
    FUNCTION fn_volume_da_coleta(p_id_coleta IN NUMBER) RETURN NUMBER IS
        v_total NUMBER := 0;
    BEGIN
        SELECT NVL(SUM(i.volume), 0)
          INTO v_total
          FROM tb_coleta_itens ci
          JOIN tb_itens i ON i.id = ci.id_item
         WHERE ci.id_coleta = p_id_coleta;

        RETURN v_total;
    END fn_volume_da_coleta;

    -- -----------------------------------------------------------------
    -- Registra a entrega de uma coleta num centro:
    --   1. valida que a coleta ainda esta aberta
    --   2. valida que o centro comporta o volume
    --   3. soma o volume ao centro
    --   4. marca os itens do catador como entregues
    --   5. finaliza a coleta
    -- Tudo numa unica transacao: qualquer falha desfaz o conjunto.
    -- -----------------------------------------------------------------
    PROCEDURE prc_registrar_entrega(p_id_coleta IN  NUMBER,
                                    p_id_centro IN  NUMBER,
                                    p_resultado OUT VARCHAR2) IS

        v_volume       NUMBER;
        v_espaco       NUMBER;
        v_finalizada   tb_coletas.foi_finalizada%TYPE;
        v_id_catador   tb_coletas.id_catador%TYPE;
        v_qtd_itens    PLS_INTEGER := 0;

        CURSOR c_itens_da_coleta IS
            SELECT id_item
              FROM tb_coleta_itens
             WHERE id_coleta = p_id_coleta;

    BEGIN
        SELECT foi_finalizada, id_catador
          INTO v_finalizada, v_id_catador
          FROM tb_coletas
         WHERE id_coleta = p_id_coleta
           FOR UPDATE;

        IF v_finalizada = 1 THEN
            RAISE_APPLICATION_ERROR(-20002,
                'A coleta ' || p_id_coleta || ' ja foi finalizada.');
        END IF;

        v_volume := fn_volume_da_coleta(p_id_coleta);

        SELECT volume_itens_total - volume_itens_atual
          INTO v_espaco
          FROM tb_centros_coleta
         WHERE id = p_id_centro
           FOR UPDATE;

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
               AND id_item    = r.id_item
               AND foi_entregue = 0;

            v_qtd_itens := v_qtd_itens + SQL%ROWCOUNT;
        END LOOP;

        UPDATE tb_coletas
           SET foi_finalizada = 1,
               id_centro      = p_id_centro
         WHERE id_coleta = p_id_coleta;

        p_resultado := 'Entrega registrada: ' || v_qtd_itens ||
                       ' item(ns), volume ' || v_volume || '.';

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20003,
                'Coleta ou centro de coleta nao encontrado.');
    END prc_registrar_entrega;

END PKG_RELATORIO;
/
