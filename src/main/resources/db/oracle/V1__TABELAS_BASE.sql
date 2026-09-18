-- =====================================================================
-- SmartCollector - criacao do schema inicial
-- Oracle Database 23 (compativel com 19c+)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuarios (base para catador e descartador)
-- ---------------------------------------------------------------------
CREATE TABLE tb_usuario (
    id      NUMBER(19)      NOT NULL,
    nome    VARCHAR2(100)   NOT NULL,
    email   VARCHAR2(120)   NOT NULL,
    senha   VARCHAR2(100)   NOT NULL,
    funcao  VARCHAR2(30)    NOT NULL,
    CONSTRAINT pk_usuario     PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_funcao CHECK (funcao IN ('ADMIN', 'USER'))
);

CREATE SEQUENCE usuario_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- Centros de coleta
-- ---------------------------------------------------------------------
CREATE TABLE tb_centros_coleta (
    id                  NUMBER(19)      NOT NULL,
    endereco            VARCHAR2(200)   NOT NULL,
    volume_itens_total  NUMBER(10,2)    DEFAULT 0 NOT NULL,
    volume_itens_atual  NUMBER(10,2)    DEFAULT 0 NOT NULL,
    CONSTRAINT pk_centro_coleta PRIMARY KEY (id),
    CONSTRAINT ck_centro_volume CHECK (volume_itens_atual <= volume_itens_total)
);

CREATE SEQUENCE centro_coleta_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- Catadores (especializacao de usuario)
-- ---------------------------------------------------------------------
CREATE TABLE tb_catador (
    id                       NUMBER(19)   NOT NULL,
    capacidade_volume_total  NUMBER(10,2) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_catador     PRIMARY KEY (id),
    CONSTRAINT fk_catador_usuario FOREIGN KEY (id) REFERENCES tb_usuario (id)
);

-- ---------------------------------------------------------------------
-- Descartadores (especializacao de usuario)
-- ---------------------------------------------------------------------
CREATE TABLE tb_descartador (
    id        NUMBER(19)    NOT NULL,
    endereco  VARCHAR2(200) NOT NULL,
    CONSTRAINT pk_descartador PRIMARY KEY (id),
    CONSTRAINT fk_descartador_usuario FOREIGN KEY (id) REFERENCES tb_usuario (id)
);

-- ---------------------------------------------------------------------
-- Itens reciclaveis
-- ---------------------------------------------------------------------
CREATE TABLE tb_itens (
    id      NUMBER(19)    NOT NULL,
    nome    VARCHAR2(100) NOT NULL,
    volume  NUMBER(10,2)  DEFAULT 0 NOT NULL,
    CONSTRAINT pk_item PRIMARY KEY (id)
);

CREATE SEQUENCE item_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- Coletas
-- ---------------------------------------------------------------------
CREATE TABLE tb_coletas (
    id_coleta       NUMBER(19)  NOT NULL,
    data_coleta     TIMESTAMP   DEFAULT SYSTIMESTAMP NOT NULL,
    id_catador      NUMBER(19),
    id_descartador  NUMBER(19),
    id_centro       NUMBER(19),
    foi_finalizada  NUMBER(1)   DEFAULT 0 NOT NULL,
    CONSTRAINT pk_coleta PRIMARY KEY (id_coleta),
    CONSTRAINT fk_coleta_catador     FOREIGN KEY (id_catador)     REFERENCES tb_catador (id),
    CONSTRAINT fk_coleta_descartador FOREIGN KEY (id_descartador) REFERENCES tb_descartador (id),
    CONSTRAINT fk_coleta_centro      FOREIGN KEY (id_centro)      REFERENCES tb_centros_coleta (id),
    CONSTRAINT ck_coleta_finalizada  CHECK (foi_finalizada IN (0, 1))
);

CREATE SEQUENCE coleta_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- Itens de cada coleta (N:N)
-- ---------------------------------------------------------------------
CREATE TABLE tb_coleta_itens (
    id_coleta  NUMBER(19) NOT NULL,
    id_item    NUMBER(19) NOT NULL,
    CONSTRAINT pk_coleta_itens      PRIMARY KEY (id_coleta, id_item),
    CONSTRAINT fk_coleta_itens_coleta FOREIGN KEY (id_coleta) REFERENCES tb_coletas (id_coleta),
    CONSTRAINT fk_coleta_itens_item   FOREIGN KEY (id_item)   REFERENCES tb_itens (id)
);

-- ---------------------------------------------------------------------
-- Itens em posse do catador (N:N)
-- ---------------------------------------------------------------------
CREATE TABLE tb_catador_item (
    id_catador    NUMBER(19) NOT NULL,
    id_item       NUMBER(19) NOT NULL,
    foi_entregue  NUMBER(1)  DEFAULT 0 NOT NULL,
    CONSTRAINT pk_catador_item      PRIMARY KEY (id_catador, id_item),
    CONSTRAINT fk_catador_item_catador FOREIGN KEY (id_catador) REFERENCES tb_catador (id),
    CONSTRAINT fk_catador_item_item    FOREIGN KEY (id_item)    REFERENCES tb_itens (id),
    CONSTRAINT ck_catador_item_entregue CHECK (foi_entregue IN (0, 1))
);

-- ---------------------------------------------------------------------
-- Indices de apoio as consultas mais frequentes
-- ---------------------------------------------------------------------
CREATE INDEX idx_coleta_catador     ON tb_coletas (id_catador);
CREATE INDEX idx_coleta_descartador ON tb_coletas (id_descartador);
CREATE INDEX idx_coleta_centro      ON tb_coletas (id_centro);
CREATE INDEX idx_coleta_data        ON tb_coletas (data_coleta);
