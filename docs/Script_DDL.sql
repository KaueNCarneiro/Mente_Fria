-- DDL.sql
-- schema.sql — Mente Fria
-- Equipe: Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006),
--         Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)
-- Trilha: B (Origem do problema: Cliente Real)
--
-- Gera o schema completo em um banco PostgreSQL vazio (testado para PostgreSQL 16+;
-- confirme a versão exata oferecida pelo Render no momento da criação do banco — 17 e 18
-- disponíveis em 2026 — e mantenha a mesma versão localmente e no Testcontainers).
--
-- Migrado de MySQL para PostgreSQL em 16/09/2026 (motivo: Render não oferece MySQL como
-- banco gerenciado gratuito — ver nota de decisão técnica completa em docs/der.md).
--
-- Reflete exatamente o dicionário de dados de docs/der.md (mesmos nomes de tabela e campo).
-- Ordem de criação respeita as dependências de chave estrangeira (tabelas-pai antes das tabelas-filhas).

-- =========================================================
-- 1. usuario
-- =========================================================
CREATE TABLE usuario (
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    senha_hash  VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20) NOT NULL,
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT chk_usuario_perfil CHECK (perfil IN ('ADMINISTRADOR', 'FUNCIONARIO'))
);

-- =========================================================
-- 2. cliente
-- =========================================================
CREATE TABLE cliente (
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome      VARCHAR(150) NOT NULL,
    contato   VARCHAR(50) NOT NULL,
    endereco  VARCHAR(255) NOT NULL
);

-- =========================================================
-- 3. unidade_medida_porcao_padrao (US26/UC20)
-- Precisa existir antes de "ingrediente", que passa a referenciá-la por FK.
-- =========================================================
CREATE TABLE unidade_medida_porcao_padrao (
    unidade_medida  VARCHAR(20) PRIMARY KEY,
    porcao_padrao   DECIMAL(10,3) NOT NULL,
    CONSTRAINT chk_unidade_porcao_padrao CHECK (porcao_padrao > 0)
);

-- =========================================================
-- 4. ingrediente
-- =========================================================
CREATE TABLE ingrediente (
    id                   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome                 VARCHAR(100) NOT NULL,
    unidade_medida       VARCHAR(20) NOT NULL,
    custo_unitario       DECIMAL(10,2) NOT NULL,
    porcao_padrao        DECIMAL(10,3) NOT NULL,
    quantidade_estoque   DECIMAL(10,3) NOT NULL DEFAULT 0,
    quantidade_minima    DECIMAL(10,3) NOT NULL DEFAULT 0,
    CONSTRAINT fk_ingrediente_unidade_medida
        FOREIGN KEY (unidade_medida) REFERENCES unidade_medida_porcao_padrao(unidade_medida),
    CONSTRAINT chk_ingrediente_custo_unitario CHECK (custo_unitario >= 0),
    CONSTRAINT chk_ingrediente_porcao_padrao CHECK (porcao_padrao > 0),
    CONSTRAINT chk_ingrediente_quantidade_estoque CHECK (quantidade_estoque >= 0),
    CONSTRAINT chk_ingrediente_quantidade_minima CHECK (quantidade_minima >= 0)
);

-- =========================================================
-- 5. produto
-- =========================================================
CREATE TABLE produto (
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    preco_venda  DECIMAL(10,2) NOT NULL,
    ativo        BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_produto_preco_venda CHECK (preco_venda > 0)
);

-- =========================================================
-- 6. produto_ingrediente (N:N entre produto e ingrediente)
-- =========================================================
CREATE TABLE produto_ingrediente (
    produto_id             INT NOT NULL,
    ingrediente_id         INT NOT NULL,
    quantidade_utilizada   DECIMAL(10,3) NOT NULL,
    PRIMARY KEY (produto_id, ingrediente_id),
    CONSTRAINT fk_produto_ingrediente_produto
        FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT fk_produto_ingrediente_ingrediente
        FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id),
    CONSTRAINT chk_produto_ingrediente_quantidade CHECK (quantidade_utilizada > 0)
);

-- =========================================================
-- 7. pedido
-- =========================================================
CREATE TABLE pedido (
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id   INT NULL,
    usuario_id   INT NOT NULL,
    data_pedido  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total  DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_pedido_valor_total CHECK (valor_total >= 0)
);

-- =========================================================
-- 8. pedido_produto (N:N entre pedido e produto)
-- =========================================================
CREATE TABLE pedido_produto (
    pedido_id                   INT NOT NULL,
    produto_id                  INT NOT NULL,
    quantidade                  INT NOT NULL,
    preco_unitario_registrado   DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (pedido_id, produto_id),
    CONSTRAINT fk_pedido_produto_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_pedido_produto_produto
        FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT chk_pedido_produto_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_pedido_produto_preco CHECK (preco_unitario_registrado > 0)
);

-- =========================================================
-- 9. movimentacao_estoque
-- =========================================================
CREATE TABLE movimentacao_estoque (
    id                  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ingrediente_id      INT NOT NULL,
    usuario_id          INT NOT NULL,
    tipo                VARCHAR(10) NOT NULL,
    quantidade          DECIMAL(10,3) NOT NULL,
    data_movimentacao   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao          VARCHAR(255) NULL,
    data_validade       DATE,
    CONSTRAINT fk_movimentacao_ingrediente
        FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id),
    CONSTRAINT fk_movimentacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_movimentacao_tipo CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT chk_movimentacao_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_movimentacao_data_validade CHECK (tipo = 'ENTRADA' OR data_validade IS NULL)
);

-- =========================================================
-- SEED DA SPRINT 1 — dados de exemplo
-- =========================================================
-- Revisão 19/09/2026: o seed contém apenas o que a Sprint 1 (US1–US8) utiliza.
--   * Sem usuários: a conta do Administrador nasce pela US1 e os funcionários pela US2.
--   * Sem clientes, pedidos e movimentações de estoque: pertencem à Sprint 2 (US9, US12, US13)
--     e dependem de um usuário já existente (usuario_id é NOT NULL). O conteúdo anterior tinha
--     saldos que não batiam com as movimentações e permanece no histórico do Git.
--   * As cinco unidades da Tela 19 (kg, mg, l, ml, un) são cadastradas aqui, porque a tela de
--     gestão de unidades (US26) só chega na Sprint 3. Em minúsculo: a FK diferencia maiúsculas.
-- Pré-requisito: o schema acima, rodado em um banco PostgreSQL vazio.
-- IDs não são informados: GENERATED ALWAYS AS IDENTITY gera 1, 2, 3... na ordem de inserção,
-- o que é usado como referência nos comentários abaixo.

-- =========================================================
-- 1. unidade_medida_porcao_padrao (US26/UC20)
-- Precisa ser inserida antes de "ingrediente" por causa da FK ingrediente.unidade_medida.
-- Valores de mg e ml derivados de kg e l (0,040 kg = 40.000 mg; 0,030 l = 30 ml).
-- São apenas sugestões de pré-preenchimento e podem ser ajustadas.
-- =========================================================
INSERT INTO unidade_medida_porcao_padrao (unidade_medida, porcao_padrao) VALUES
  ('kg', 0.040),
  ('mg', 40000.000),
  ('l',  0.030),
  ('ml', 30.000),
  ('un', 1.000);

-- =========================================================
-- 2. ingrediente (id 1 a 7)
-- quantidade_estoque = saldo de abertura. Na Sprint 2 esses saldos serão registrados como
-- movimentações ENTRADA (com data_validade), mantendo a regra do DER:
-- quantidade_estoque = soma das ENTRADAS - soma das SAÍDAS.
-- =========================================================
INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque, quantidade_minima) VALUES
  ('Açaí (polpa)',           'kg', 18.50, 0.150,  40.000, 10.000), -- id 1
  ('Leite condensado',       'l',  12.00, 0.030,  15.000,  5.000), -- id 2
  ('Granola',                'kg', 22.00, 0.040,   8.000,  2.000), -- id 3
  ('Banana',                 'kg',  5.50, 0.050,  12.000,  3.000), -- id 4
  ('Morango',                'kg', 15.00, 0.040,   6.000,  2.000), -- id 5
  ('Leite em pó',            'kg', 28.00, 0.020,   5.000,  1.500), -- id 6
  ('Copo descartável 500ml', 'un',  0.35, 1.000, 300.000, 50.000); -- id 7

-- =========================================================
-- 3. produto (id 1, 2, 3)
-- =========================================================
INSERT INTO produto (nome, preco_venda, ativo) VALUES
  ('Açaí Tradicional 300ml', 12.90, TRUE), -- id 1
  ('Açaí Especial 500ml',    18.90, TRUE), -- id 2 (granola + banana)
  ('Açaí Premium 700ml',     24.90, TRUE); -- id 3 (morango + leite condensado + granola + leite em pó)

-- =========================================================
-- 4. produto_ingrediente (composição N:N produto x ingrediente)
-- Todo produto inclui o açaí (ingrediente 1), como exige a US5.
-- =========================================================
INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES
  -- Açaí Tradicional 300ml
  (1, 1, 0.300), -- açaí
  (1, 2, 0.030), -- leite condensado
  (1, 7, 1.000), -- copo
  -- Açaí Especial 500ml
  (2, 1, 0.450), -- açaí
  (2, 3, 0.040), -- granola
  (2, 4, 0.050), -- banana
  (2, 7, 1.000), -- copo
  -- Açaí Premium 700ml
  (3, 1, 0.600), -- açaí
  (3, 5, 0.060), -- morango
  (3, 2, 0.040), -- leite condensado
  (3, 3, 0.040), -- granola
  (3, 6, 0.020), -- leite em pó
  (3, 7, 1.000); -- copo
