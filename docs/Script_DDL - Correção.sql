-- DDL.sql
-- schema.sql — Mente Fria
-- Equipe: Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006),
--         Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)
-- Trilha: B (Origem do problema: Cliente Real)
--
-- Gera o schema completo em um banco MySQL 8 vazio.
-- Reflete exatamente o dicionário de dados de docs/der.md (mesmos nomes de tabela e campo).
-- Ordem de criação respeita as dependências de chave estrangeira (tabelas-pai antes das tabelas-filhas).
 
-- =========================================================
-- 1. usuario
-- =========================================================
CREATE TABLE usuario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    senha_hash  VARCHAR(255) NOT NULL,
    perfil      ENUM('ADMINISTRADOR', 'FUNCIONARIO') NOT NULL,
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- =========================================================
-- 2. cliente
-- =========================================================
CREATE TABLE cliente (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(150) NOT NULL,
    contato   VARCHAR(50) NOT NULL,
    endereco  VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- =========================================================
-- 3. ingrediente
-- =========================================================
CREATE TABLE ingrediente (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    nome                 VARCHAR(100) NOT NULL,
    unidade_medida       VARCHAR(20) NOT NULL,
    custo_unitario       DECIMAL(10,2) NOT NULL,
    porcao_padrao        DECIMAL(10,3) NOT NULL,
    quantidade_estoque   DECIMAL(10,3) NOT NULL DEFAULT 0,
    quantidade_minima    DECIMAL(10,3) NOT NULL DEFAULT 0,
    CONSTRAINT chk_ingrediente_custo_unitario CHECK (custo_unitario >= 0),
    CONSTRAINT chk_ingrediente_porcao_padrao CHECK (porcao_padrao > 0),
    CONSTRAINT chk_ingrediente_quantidade_estoque CHECK (quantidade_estoque >= 0),
    CONSTRAINT chk_ingrediente_quantidade_minima CHECK (quantidade_minima >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- =========================================================
-- 4. produto
-- =========================================================
CREATE TABLE produto (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    preco_venda  DECIMAL(10,2) NOT NULL,
    ativo        BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_produto_preco_venda CHECK (preco_venda > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- =========================================================
-- 5. produto_ingrediente (N:N entre produto e ingrediente)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- =========================================================
-- 6. pedido
-- =========================================================
CREATE TABLE pedido (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id   INT NULL,
    usuario_id   INT NOT NULL,
    data_pedido  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total  DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_pedido_valor_total CHECK (valor_total >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- =========================================================
-- 7. pedido_produto (N:N entre pedido e produto)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- =========================================================
-- 8. movimentacao_estoque
-- =========================================================
CREATE TABLE movimentacao_estoque (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    ingrediente_id      INT NOT NULL,
    usuario_id          INT NOT NULL,
    tipo                ENUM('ENTRADA', 'SAIDA') NOT NULL,
    quantidade          DECIMAL(10,3) NOT NULL,
    data_movimentacao   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao          VARCHAR(255) NULL,
    data_validade       DATE,
    CONSTRAINT fk_movimentacao_ingrediente
        FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id),
    CONSTRAINT fk_movimentacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_movimentacao_quantidade CHECK (quantidade > 0),
    CONSTRAINT chk_movimentacao_data_validade CHECK (tipo = 'ENTRADA' OR data_validade IS NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Inserts
-- Dados de exemplo (seed) para o schema Mente Fria
-- Pré-requisito: rodar Script_DDL.sql antes (cria as tabelas em um banco MySQL).
-- Ordem de inserção respeita as dependências de FK (tabelas-pai antes das tabelas-filhas).
-- IDs não são informados explicitamente: o AUTO_INCREMENT define 1, 2, 3... na ordem de inserção,
-- o que é usado como referência nos comentários abaixo.

-- =========================================================
-- 1. usuario (id 1 = Administrador, id 2 = Funcionário)
-- =========================================================
INSERT INTO usuario (nome, email, senha_hash, perfil, ativo) VALUES
  ('Carlos Eduardo Souza', 'carlos.souza@mentefria.com', '$2b$10$exemploHashAdmin01', 'ADMINISTRADOR', TRUE),
  ('Mariana Alves Pereira', 'mariana.alves@mentefria.com', '$2b$10$exemploHashFunc01',  'FUNCIONARIO',   TRUE);

-- =========================================================
-- 2. cliente (id 1, 2, 3)
-- =========================================================
INSERT INTO cliente (nome, contato, endereco) VALUES
  ('Fernanda Lima',   '(11) 98765-4321', 'Rua das Flores, 123 - São Paulo/SP'),
  ('Rafael Santos',   '(11) 91234-5678', 'Av. Paulista, 900 - São Paulo/SP'),
  ('Juliana Costa',   '(11) 99988-7766', 'Rua Augusta, 500 - São Paulo/SP');

-- =========================================================
-- 3. ingrediente (id 1 a 7)
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
-- 4. produto (id 1, 2, 3)
-- =========================================================
INSERT INTO produto (nome, preco_venda, ativo) VALUES
  ('Açaí Tradicional 300ml', 12.90, TRUE), -- id 1
  ('Açaí Especial 500ml',    18.90, TRUE), -- id 2 (granola + banana)
  ('Açaí Premium 700ml',     24.90, TRUE); -- id 3 (morango + leite condensado + granola + leite em pó)

-- =========================================================
-- 5. produto_ingrediente (composição N:N produto x ingrediente)
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

-- =========================================================
-- 6. pedido (id 1, 2, 3)
-- =========================================================
INSERT INTO pedido (cliente_id, usuario_id, data_pedido, valor_total) VALUES
  (1,    2, '2026-09-01 10:15:00', 44.70), -- id 1: Fernanda, atendida pela Mariana (funcionária)
  (NULL, 2, '2026-09-02 14:30:00', 37.80), -- id 2: venda de balcão, sem cliente cadastrado
  (2,    1, '2026-09-03 09:00:00', 56.70); -- id 3: Rafael, atendido pelo Carlos (administrador)

-- =========================================================
-- 7. pedido_produto (itens de cada pedido)
-- =========================================================
INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES
  -- Pedido 1: 2x Tradicional (25.80) + 1x Especial (18.90) = 44.70
  (1, 1, 2, 12.90),
  (1, 2, 1, 18.90),
  -- Pedido 2: 1x Premium (24.90) + 1x Tradicional (12.90) = 37.80
  (2, 3, 1, 24.90),
  (2, 1, 1, 12.90),
  -- Pedido 3: 3x Especial (56.70)
  (3, 2, 3, 18.90);

-- =========================================================
-- 8. movimentacao_estoque
-- =========================================================
INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, data_movimentacao, observacao, data_validade) VALUES
  (1, 1, 'ENTRADA', 50.000, '2026-08-25 08:00:00', 'Compra inicial de açaí - fornecedor Polpa Norte', '2026-10-15'),
  (4, 1, 'ENTRADA', 15.000, '2026-08-25 08:10:00', 'Compra inicial de banana', '2027-03-01'),
  (5, 1, 'ENTRADA',  8.000, '2026-08-26 08:00:00', 'Compra inicial de morango', '2026-12-20'),
  (1, 2, 'SAIDA',    1.050, '2026-09-01 10:16:00', 'Baixa de estoque referente ao Pedido #1', NULL),
  (4, 2, 'SAIDA',    0.050, '2026-09-01 10:16:00', 'Baixa de estoque referente ao Pedido #1', NULL),
  (1, 2, 'SAIDA',    1.050, '2026-09-02 14:31:00', 'Baixa de estoque referente ao Pedido #2', NULL),
  (5, 2, 'SAIDA',    0.060, '2026-09-02 14:31:00', 'Baixa de estoque referente ao Pedido #2', NULL),
  (1, 1, 'SAIDA',    1.350, '2026-09-03 09:01:00', 'Baixa de estoque referente ao Pedido #3', NULL),
  (4, 1, 'SAIDA',    0.150, '2026-09-03 09:01:00', 'Baixa de estoque referente ao Pedido #3', NULL);