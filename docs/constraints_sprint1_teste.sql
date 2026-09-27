-- =========================================================
-- constraints_sprint1_teste.sql — Mente Fria
-- Testes de banco da Sprint 1 (US1–US7 / requisito R5, metade "banco")
-- =========================================================
-- Cada caso tenta gravar um dado inválido e confere se o PostgreSQL o rejeita EXATAMENTE
-- pela constraint esperada (ou aceita, nos casos válidos). Tudo roda dentro de uma transação
-- que é DESFEITA no final: nenhuma linha fica no banco.
--
-- ATENÇÃO: os contadores de ID (GENERATED ... AS IDENTITY) avançam mesmo com o ROLLBACK.
-- Rode em um banco descartável (ex.: mente_fria_teste), não no banco de desenvolvimento.
--
-- Uso:
--   createdb / CREATE DATABASE mente_fria_teste;
--   psql -U postgres -d mente_fria_teste -f Script_DDL.sql
--   psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f constraints_sprint1_teste.sql
--
-- Pré-requisito: Script_DDL.sql carregado (usa os ids do seed: ingredientes 1–7, produtos 1–3).
-- Código de saída diferente de 0 = algum teste falhou (serve para o CI no futuro).
--
-- Fora deste script (não é regra de banco): "todo produto precisa ter o açaí" (US5/CT15) e
-- "senha com no mínimo 8 caracteres" (US1/CT02) são validadas na aplicação.

\set ON_ERROR_STOP on
BEGIN;

CREATE TEMP TABLE resultado (
    ordem    SERIAL,
    teste    TEXT,
    cenario  TEXT,
    ok       BOOLEAN,
    detalhe  TEXT
);

-- Espera que o comando seja REJEITADO pela constraint p_esperada.
-- Para NOT NULL, o valor esperado é 'NOT NULL:<coluna>'.
CREATE FUNCTION pg_temp.rejeita(p_teste TEXT, p_cenario TEXT, p_comando TEXT, p_esperada TEXT)
RETURNS VOID AS $f$
DECLARE
    v_constraint TEXT;
    v_coluna     TEXT;
    v_encontrada TEXT;
BEGIN
    BEGIN
        EXECUTE p_comando;
        INSERT INTO resultado (teste, cenario, ok, detalhe)
        VALUES (p_teste, p_cenario, FALSE, 'foi ACEITO, mas deveria ser rejeitado');
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_constraint = CONSTRAINT_NAME, v_coluna = COLUMN_NAME;
        -- O PostgreSQL devolve texto vazio (e não NULL) quando o erro não tem constraint nomeada (caso do NOT NULL)
        v_encontrada := COALESCE(NULLIF(v_constraint, ''), 'NOT NULL:' || NULLIF(v_coluna, ''), SQLERRM);
        INSERT INTO resultado (teste, cenario, ok, detalhe)
        VALUES (p_teste, p_cenario, v_encontrada = p_esperada, 'rejeitado por: ' || v_encontrada);
    END;
END
$f$ LANGUAGE plpgsql;

-- Espera que o comando seja ACEITO.
CREATE FUNCTION pg_temp.aceita(p_teste TEXT, p_cenario TEXT, p_comando TEXT)
RETURNS VOID AS $f$
BEGIN
    BEGIN
        EXECUTE p_comando;
        INSERT INTO resultado (teste, cenario, ok, detalhe)
        VALUES (p_teste, p_cenario, TRUE, 'aceito');
    EXCEPTION WHEN OTHERS THEN
        INSERT INTO resultado (teste, cenario, ok, detalhe)
        VALUES (p_teste, p_cenario, FALSE, 'foi REJEITADO: ' || SQLERRM);
    END;
END
$f$ LANGUAGE plpgsql;

DO $testes$
BEGIN
    -- ---------- usuario (US1, US2, US3, US7) ----------
    PERFORM pg_temp.aceita('T01', 'Usuário válido é aceito',
        $q$INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES ('Teste Admin', 'admin.teste@exemplo.com', 'hash_exemplo', 'ADMINISTRADOR')$q$);
    PERFORM pg_temp.rejeita('T02', 'E-mail duplicado (CT03)',
        $q$INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES ('Outro', 'admin.teste@exemplo.com', 'hash_exemplo', 'FUNCIONARIO')$q$,
        'uq_usuario_email');
    PERFORM pg_temp.rejeita('T03', 'Perfil inválido',
        $q$INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES ('Fulano', 'fulano@exemplo.com', 'hash_exemplo', 'GERENTE')$q$,
        'chk_usuario_perfil');
    PERFORM pg_temp.rejeita('T04', 'Usuário sem nome',
        $q$INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES (NULL, 'a@exemplo.com', 'hash_exemplo', 'FUNCIONARIO')$q$,
        'NOT NULL:nome');
    PERFORM pg_temp.rejeita('T05', 'Usuário sem e-mail',
        $q$INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES ('Fulano', NULL, 'hash_exemplo', 'FUNCIONARIO')$q$,
        'NOT NULL:email');
    PERFORM pg_temp.rejeita('T06', 'Usuário sem senha (hash)',
        $q$INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES ('Fulano', 'b@exemplo.com', NULL, 'FUNCIONARIO')$q$,
        'NOT NULL:senha_hash');

    -- ---------- ingrediente (US4, US7) ----------
    PERFORM pg_temp.aceita('T07', 'Ingrediente válido é aceito',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Leite de coco', 'l', 9.90, 0.030)$q$);
    PERFORM pg_temp.aceita('T08', 'Custo zero é aceito (a regra é custo >= 0)',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Brinde', 'un', 0, 1)$q$);
    PERFORM pg_temp.rejeita('T09', 'Custo negativo (CT10)',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Teste', 'kg', -1, 0.040)$q$,
        'chk_ingrediente_custo_unitario');
    PERFORM pg_temp.rejeita('T10', 'Porção padrão zero (CT11)',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Teste', 'kg', 1, 0)$q$,
        'chk_ingrediente_porcao_padrao');
    PERFORM pg_temp.rejeita('T11', 'Quantidade mínima negativa',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_minima) VALUES ('Teste', 'kg', 1, 0.040, -1)$q$,
        'chk_ingrediente_quantidade_minima');
    PERFORM pg_temp.rejeita('T12', 'Estoque negativo',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque) VALUES ('Teste', 'kg', 1, 0.040, -1)$q$,
        'chk_ingrediente_quantidade_estoque');
    PERFORM pg_temp.rejeita('T13', 'Unidade de medida inexistente',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Teste', 'xx', 1, 0.040)$q$,
        'fk_ingrediente_unidade_medida');
    PERFORM pg_temp.rejeita('T14', 'Unidade em maiúsculo ("KG" ≠ "kg": a FK diferencia maiúsculas)',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Teste', 'KG', 1, 0.040)$q$,
        'fk_ingrediente_unidade_medida');
    PERFORM pg_temp.rejeita('T15', 'Ingrediente sem nome',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES (NULL, 'kg', 1, 0.040)$q$,
        'NOT NULL:nome');
    PERFORM pg_temp.rejeita('T16', 'Ingrediente sem unidade de medida',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Teste', NULL, 1, 0.040)$q$,
        'NOT NULL:unidade_medida');
    PERFORM pg_temp.rejeita('T17', 'Ingrediente sem custo',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Teste', 'kg', NULL, 0.040)$q$,
        'NOT NULL:custo_unitario');
    PERFORM pg_temp.rejeita('T18', 'Ingrediente sem porção padrão',
        $q$INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao) VALUES ('Teste', 'kg', 1, NULL)$q$,
        'NOT NULL:porcao_padrao');

    -- ---------- produto (US5, US6, US7) ----------
    PERFORM pg_temp.aceita('T19', 'Produto válido é aceito',
        $q$INSERT INTO produto (nome, preco_venda) VALUES ('Açaí Teste 400ml', 15.90)$q$);
    PERFORM pg_temp.rejeita('T20', 'Preço zero (CT17)',
        $q$INSERT INTO produto (nome, preco_venda) VALUES ('Teste', 0)$q$,
        'chk_produto_preco_venda');
    PERFORM pg_temp.rejeita('T21', 'Preço negativo (CT17)',
        $q$INSERT INTO produto (nome, preco_venda) VALUES ('Teste', -5)$q$,
        'chk_produto_preco_venda');
    PERFORM pg_temp.rejeita('T22', 'Produto sem preço',
        $q$INSERT INTO produto (nome, preco_venda) VALUES ('Teste', NULL)$q$,
        'NOT NULL:preco_venda');
    PERFORM pg_temp.rejeita('T23', 'Produto sem nome',
        $q$INSERT INTO produto (nome, preco_venda) VALUES (NULL, 10)$q$,
        'NOT NULL:nome');
    PERFORM pg_temp.rejeita('T24', 'Atualizar preço para zero (US6)',
        $q$UPDATE produto SET preco_venda = 0 WHERE id = 1$q$,
        'chk_produto_preco_venda');

    -- ---------- produto_ingrediente (US5, N:N) ----------
    PERFORM pg_temp.aceita('T25', 'Composição válida é aceita (banana no Tradicional)',
        $q$INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES (1, 4, 0.020)$q$);
    PERFORM pg_temp.rejeita('T26', 'Quantidade utilizada zero (CT14)',
        $q$INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES (1, 5, 0)$q$,
        'chk_produto_ingrediente_quantidade');
    PERFORM pg_temp.rejeita('T27', 'Quantidade utilizada negativa (CT14)',
        $q$INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES (1, 5, -0.1)$q$,
        'chk_produto_ingrediente_quantidade');
    PERFORM pg_temp.rejeita('T28', 'Mesmo ingrediente duas vezes no mesmo produto',
        $q$INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES (1, 1, 0.100)$q$,
        'produto_ingrediente_pkey');
    PERFORM pg_temp.rejeita('T29', 'Composição com ingrediente inexistente',
        $q$INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES (1, 999999, 0.100)$q$,
        'fk_produto_ingrediente_ingrediente');
    PERFORM pg_temp.rejeita('T30', 'Composição com produto inexistente',
        $q$INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada) VALUES (999999, 1, 0.100)$q$,
        'fk_produto_ingrediente_produto');

    -- ---------- unidade_medida_porcao_padrao (US26 — só seed na Sprint 1) ----------
    PERFORM pg_temp.rejeita('T31', 'Porção padrão da unidade igual a zero',
        $q$INSERT INTO unidade_medida_porcao_padrao (unidade_medida, porcao_padrao) VALUES ('g', 0)$q$,
        'chk_unidade_porcao_padrao');
    PERFORM pg_temp.rejeita('T32', 'Unidade de medida duplicada',
        $q$INSERT INTO unidade_medida_porcao_padrao (unidade_medida, porcao_padrao) VALUES ('kg', 0.5)$q$,
        'unidade_medida_porcao_padrao_pkey');
END
$testes$;

SELECT teste,
       CASE WHEN ok THEN 'OK' ELSE 'FALHOU' END AS resultado,
       cenario,
       detalhe
FROM resultado
ORDER BY ordem;

SELECT COUNT(*) FILTER (WHERE ok)     AS passaram,
       COUNT(*) FILTER (WHERE NOT ok) AS falharam
FROM resultado;

-- Faz o psql terminar com erro (código de saída != 0) se algum teste falhou.
DO $verifica$
BEGIN
    IF EXISTS (SELECT 1 FROM resultado WHERE NOT ok) THEN
        RAISE EXCEPTION 'Há testes de banco falhando (veja a tabela acima).';
    END IF;
END
$verifica$;

ROLLBACK;
