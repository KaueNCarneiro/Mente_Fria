-- =========================================================
-- constraints_sprint3_teste.sql — Mente Fria
-- Testes de banco da Sprint 3: cliente, pedido (status/cancelamento), pedido_produto,
-- movimentacao_estoque (linhas, quantidade_restante, motivo), baixa e unidade de medida.
-- Continua a numeração de bd/constraints_teste.sql (T01–T32): aqui T33 em diante.
-- Roda em uma transação DESFEITA no final; rode em banco descartável (os IDs avançam).
--   psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f constraints_sprint3_teste.sql
-- Pré-requisito: Script_DDL.sql (versão da Sprint 3) carregado.
-- =========================================================
\set ON_ERROR_STOP on
BEGIN;
CREATE TEMP TABLE resultado (ordem SERIAL, teste TEXT, cenario TEXT, ok BOOLEAN, detalhe TEXT);

CREATE FUNCTION pg_temp.rejeita(p_teste TEXT, p_cenario TEXT, p_comando TEXT, p_esperada TEXT)
RETURNS VOID AS $f$
DECLARE v_constraint TEXT; v_coluna TEXT; v_encontrada TEXT;
BEGIN
    BEGIN
        EXECUTE p_comando;
        INSERT INTO resultado (teste, cenario, ok, detalhe) VALUES (p_teste, p_cenario, FALSE, 'foi ACEITO, mas deveria ser rejeitado');
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_constraint = CONSTRAINT_NAME, v_coluna = COLUMN_NAME;
        v_encontrada := COALESCE(NULLIF(v_constraint, ''), 'NOT NULL:' || NULLIF(v_coluna, ''), SQLERRM);
        INSERT INTO resultado (teste, cenario, ok, detalhe) VALUES (p_teste, p_cenario, v_encontrada = p_esperada, 'rejeitado por: ' || v_encontrada);
    END;
END $f$ LANGUAGE plpgsql;

CREATE FUNCTION pg_temp.aceita(p_teste TEXT, p_cenario TEXT, p_comando TEXT)
RETURNS VOID AS $f$
BEGIN
    BEGIN
        EXECUTE p_comando;
        INSERT INTO resultado (teste, cenario, ok, detalhe) VALUES (p_teste, p_cenario, TRUE, 'aceito');
    EXCEPTION WHEN OTHERS THEN
        INSERT INTO resultado (teste, cenario, ok, detalhe) VALUES (p_teste, p_cenario, FALSE, 'foi REJEITADO: ' || SQLERRM);
    END;
END $f$ LANGUAGE plpgsql;

INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES ('Apoio S3', 'apoio.s3@exemplo.com', 'hash_exemplo', 'ADMINISTRADOR');

DO $testes$
DECLARE u TEXT := '(SELECT id FROM usuario WHERE email = ''apoio.s3@exemplo.com'')';
BEGIN
    -- ---------- cliente (US13) ----------
    PERFORM pg_temp.aceita('T33', 'Cliente válido é aceito',
        $q$INSERT INTO cliente (nome, contato, endereco) VALUES ('Cliente Teste', '(16) 90000-0000', 'Rua Teste, 1')$q$);
    PERFORM pg_temp.rejeita('T34', 'Cliente sem nome', $q$INSERT INTO cliente (nome, contato, endereco) VALUES (NULL, 'x', 'y')$q$, 'NOT NULL:nome');
    PERFORM pg_temp.rejeita('T35', 'Cliente sem contato', $q$INSERT INTO cliente (nome, contato, endereco) VALUES ('x', NULL, 'y')$q$, 'NOT NULL:contato');
    PERFORM pg_temp.rejeita('T36', 'Cliente sem endereço', $q$INSERT INTO cliente (nome, contato, endereco) VALUES ('x', 'y', NULL)$q$, 'NOT NULL:endereco');

    -- ---------- pedido (US12 / US27) ----------
    PERFORM pg_temp.aceita('T37', 'Pedido sem cliente é aceito (status nasce ATIVO)',
        format('INSERT INTO pedido (cliente_id, usuario_id, valor_total) VALUES (NULL, %s, 0)', u));
    PERFORM pg_temp.rejeita('T38', 'Pedido sem usuário', $q$INSERT INTO pedido (usuario_id) VALUES (NULL)$q$, 'NOT NULL:usuario_id');
    PERFORM pg_temp.rejeita('T39', 'Pedido com usuário inexistente', $q$INSERT INTO pedido (usuario_id) VALUES (999999)$q$, 'fk_pedido_usuario');
    PERFORM pg_temp.rejeita('T40', 'Pedido com cliente inexistente', format('INSERT INTO pedido (cliente_id, usuario_id) VALUES (999999, %s)', u), 'fk_pedido_cliente');
    PERFORM pg_temp.rejeita('T41', 'Valor total negativo', format('INSERT INTO pedido (usuario_id, valor_total) VALUES (%s, -1)', u), 'chk_pedido_valor_total');
    PERFORM pg_temp.rejeita('T42', 'Status de pedido inválido', format('INSERT INTO pedido (usuario_id, status) VALUES (%s, ''PAGO'')', u), 'chk_pedido_status');
    PERFORM pg_temp.rejeita('T43', 'CANCELADO sem data e sem usuário do cancelamento', format('INSERT INTO pedido (usuario_id, status) VALUES (%s, ''CANCELADO'')', u), 'chk_pedido_cancelamento');
    PERFORM pg_temp.rejeita('T44', 'ATIVO com data de cancelamento preenchida', format('INSERT INTO pedido (usuario_id, data_cancelamento) VALUES (%s, now())', u), 'chk_pedido_cancelamento');
    PERFORM pg_temp.aceita('T45', 'CANCELADO completo é aceito',
        format('INSERT INTO pedido (usuario_id, status, data_cancelamento, usuario_cancelamento_id) VALUES (%s, ''CANCELADO'', now(), %s)', u, u));
    PERFORM pg_temp.rejeita('T46', 'Usuário do cancelamento inexistente',
        format('INSERT INTO pedido (usuario_id, status, data_cancelamento, usuario_cancelamento_id) VALUES (%s, ''CANCELADO'', now(), 999999)', u), 'fk_pedido_usuario_cancelamento');

    -- ---------- pedido_produto (US12, N:N) ----------
    PERFORM pg_temp.aceita('T47', 'Item de pedido válido é aceito',
        $q$INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES ((SELECT MIN(id) FROM pedido), 1, 2, 12.90)$q$);
    PERFORM pg_temp.rejeita('T48', 'Quantidade do item igual a zero',
        $q$INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES ((SELECT MIN(id) FROM pedido), 2, 0, 18.90)$q$, 'chk_pedido_produto_quantidade');
    PERFORM pg_temp.rejeita('T49', 'Preço registrado igual a zero',
        $q$INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES ((SELECT MIN(id) FROM pedido), 2, 1, 0)$q$, 'chk_pedido_produto_preco');
    PERFORM pg_temp.rejeita('T50', 'Mesmo produto duas vezes no mesmo pedido',
        $q$INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES ((SELECT MIN(id) FROM pedido), 1, 1, 12.90)$q$, 'pedido_produto_pkey');
    PERFORM pg_temp.rejeita('T51', 'Item com produto inexistente',
        $q$INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES ((SELECT MIN(id) FROM pedido), 999999, 1, 10)$q$, 'fk_pedido_produto_produto');
    PERFORM pg_temp.rejeita('T52', 'Item com pedido inexistente',
        $q$INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES (999999, 1, 1, 10)$q$, 'fk_pedido_produto_pedido');

    -- ---------- movimentacao_estoque: ENTRADA = linha de estoque (US9) ----------
    PERFORM pg_temp.aceita('T53', 'ENTRADA com validade e restante = quantidade é aceita',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, data_validade, quantidade_restante) VALUES (1, %s, ''ENTRADA'', 5, CURRENT_DATE + 30, 5)', u));
    PERFORM pg_temp.aceita('T54', 'ENTRADA sem validade é aceita (nunca vence)',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante) VALUES (1, %s, ''ENTRADA'', 5, 5)', u));
    PERFORM pg_temp.rejeita('T55', 'ENTRADA sem quantidade_restante',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade) VALUES (1, %s, ''ENTRADA'', 5)', u), 'chk_movimentacao_restante');
    PERFORM pg_temp.rejeita('T56', 'quantidade_restante maior que a quantidade da entrada',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante) VALUES (1, %s, ''ENTRADA'', 5, 6)', u), 'chk_movimentacao_restante');
    PERFORM pg_temp.rejeita('T57', 'quantidade_restante negativa',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante) VALUES (1, %s, ''ENTRADA'', 5, -1)', u), 'chk_movimentacao_restante');
    PERFORM pg_temp.rejeita('T58', 'ENTRADA com motivo preenchido',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante, motivo) VALUES (1, %s, ''ENTRADA'', 5, 5, ''PEDIDO'')', u), 'chk_movimentacao_motivo');

    -- ---------- movimentacao_estoque: SAIDA ----------
    PERFORM pg_temp.aceita('T59', 'SAIDA por pedido é aceita',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo) VALUES (1, %s, ''SAIDA'', 1, ''PEDIDO'')', u));
    PERFORM pg_temp.aceita('T60', 'SAIDA convencional é aceita',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo) VALUES (1, %s, ''SAIDA'', 1, ''CONVENCIONAL'')', u));
    PERFORM pg_temp.aceita('T61', 'SAIDA por descarte de vencido é aceita',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo) VALUES (1, %s, ''SAIDA'', 1, ''DESCARTE_VENCIDO'')', u));
    PERFORM pg_temp.rejeita('T62', 'SAIDA sem motivo',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade) VALUES (1, %s, ''SAIDA'', 1)', u), 'chk_movimentacao_motivo');
    PERFORM pg_temp.rejeita('T63', 'SAIDA com motivo inválido',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo) VALUES (1, %s, ''SAIDA'', 1, ''PERDA'')', u), 'chk_movimentacao_motivo');
    PERFORM pg_temp.rejeita('T64', 'SAIDA com quantidade_restante preenchida',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo, quantidade_restante) VALUES (1, %s, ''SAIDA'', 1, ''PEDIDO'', 1)', u), 'chk_movimentacao_restante');
    PERFORM pg_temp.rejeita('T65', 'SAIDA com validade preenchida',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo, data_validade) VALUES (1, %s, ''SAIDA'', 1, ''PEDIDO'', CURRENT_DATE)', u), 'chk_movimentacao_data_validade');
    PERFORM pg_temp.rejeita('T66', 'Tipo de movimentação inválido',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade) VALUES (1, %s, ''AJUSTE'', 1)', u), 'chk_movimentacao_tipo');
    PERFORM pg_temp.rejeita('T67', 'Quantidade da movimentação igual a zero (CT26)',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante) VALUES (1, %s, ''ENTRADA'', 0, 0)', u), 'chk_movimentacao_quantidade');
    PERFORM pg_temp.rejeita('T68', 'Movimentação com ingrediente inexistente',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo) VALUES (999999, %s, ''SAIDA'', 1, ''PEDIDO'')', u), 'fk_movimentacao_ingrediente');
    PERFORM pg_temp.rejeita('T69', 'Movimentação com usuário inexistente',
        $q$INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo) VALUES (1, 999999, 'SAIDA', 1, 'PEDIDO')$q$, 'fk_movimentacao_usuario');
    PERFORM pg_temp.rejeita('T70', 'Movimentação sem tipo',
        format('INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade) VALUES (1, %s, NULL, 1)', u), 'NOT NULL:tipo');

    -- ---------- baixa de estoque (rede de segurança do CT21) e unidade (US26) ----------
    PERFORM pg_temp.rejeita('T71', 'Baixa maior que o saldo deixaria o estoque negativo',
        $q$UPDATE ingrediente SET quantidade_estoque = quantidade_estoque - 1000000 WHERE id = 1$q$, 'chk_ingrediente_quantidade_estoque');
    PERFORM pg_temp.aceita('T72', 'Nova unidade de medida é aceita',
        $q$INSERT INTO unidade_medida_porcao_padrao (unidade_medida, porcao_padrao) VALUES ('g', 40)$q$);
    PERFORM pg_temp.rejeita('T73', 'Alterar a porção padrão da unidade para zero',
        $q$UPDATE unidade_medida_porcao_padrao SET porcao_padrao = 0 WHERE unidade_medida = 'kg'$q$, 'chk_unidade_porcao_padrao');
END
$testes$;

SELECT teste, CASE WHEN ok THEN 'OK' ELSE 'FALHOU' END AS resultado, cenario, detalhe FROM resultado ORDER BY ordem;
SELECT COUNT(*) FILTER (WHERE ok) AS passaram, COUNT(*) FILTER (WHERE NOT ok) AS falharam FROM resultado;
DO $verifica$ BEGIN
    IF EXISTS (SELECT 1 FROM resultado WHERE NOT ok) THEN RAISE EXCEPTION 'Há testes de banco falhando (veja a tabela acima).'; END IF;
END $verifica$;
ROLLBACK;
