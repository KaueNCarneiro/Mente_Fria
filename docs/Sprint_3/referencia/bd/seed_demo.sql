-- seed_demo.sql — Mente Fria — DADOS DE DEMONSTRAÇÃO (Sprint 3, proposta; NÃO é o seed oficial)
-- Pré-requisitos: Script_DDL.sql da Sprint 3 carregado, o Administrador criado pela tela de cadastro (US1)
-- e pelo menos um Funcionário criado pela tela (US2). O script NÃO cria usuários nem guarda hash de senha.
-- Rode UMA vez em banco recém-criado. Datas de validade são relativas a CURRENT_DATE.
-- O que ele monta: estoque como LINHAS (ENTRADA com validade), clientes, pedidos e as baixas FEFO.
-- Invariante conferida no final: quantidade_estoque = soma(quantidade_restante das ENTRADAs).
BEGIN;

CREATE FUNCTION pg_temp.baixar_fefo(p_ing INT, p_qtd NUMERIC, p_usuario INT, p_motivo TEXT, p_quando TIMESTAMP, p_obs TEXT)
RETURNS VOID AS $f$
DECLARE r RECORD; falta NUMERIC := p_qtd; tira NUMERIC;
BEGIN
    PERFORM 1 FROM ingrediente WHERE id = p_ing FOR UPDATE;
    FOR r IN SELECT id, quantidade_restante FROM movimentacao_estoque
             WHERE ingrediente_id = p_ing AND tipo = 'ENTRADA' AND quantidade_restante > 0
               AND (data_validade IS NULL OR data_validade >= CURRENT_DATE)
             ORDER BY data_validade NULLS LAST, id FOR UPDATE
    LOOP
        EXIT WHEN falta <= 0;
        tira := LEAST(r.quantidade_restante, falta);
        UPDATE movimentacao_estoque SET quantidade_restante = quantidade_restante - tira WHERE id = r.id;
        falta := falta - tira;
    END LOOP;
    IF falta > 0 THEN RAISE EXCEPTION 'Estoque válido insuficiente para o ingrediente % (faltam %)', p_ing, falta; END IF;
    UPDATE ingrediente SET quantidade_estoque = quantidade_estoque - p_qtd WHERE id = p_ing;
    INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, data_movimentacao, motivo, observacao)
    VALUES (p_ing, p_usuario, 'SAIDA', p_qtd, p_quando, p_motivo, p_obs);
END $f$ LANGUAGE plpgsql;

DO $demo$
DECLARE v_admin INT; v_func INT; v_ped RECORD; v_ing RECORD;
BEGIN
    SELECT id INTO v_admin FROM usuario WHERE perfil = 'ADMINISTRADOR' ORDER BY id LIMIT 1;
    IF v_admin IS NULL THEN RAISE EXCEPTION 'Crie o Administrador pela tela de cadastro antes de rodar o seed de demonstração.'; END IF;
    SELECT id INTO v_func FROM usuario WHERE perfil = 'FUNCIONARIO' ORDER BY id LIMIT 1;
    IF v_func IS NULL THEN RAISE EXCEPTION 'Crie ao menos um Funcionário pela tela antes de rodar o seed de demonstração (o ranking por usuário precisa de dois usuários).'; END IF;
    IF EXISTS (SELECT 1 FROM movimentacao_estoque) OR EXISTS (SELECT 1 FROM pedido) THEN
        RAISE EXCEPTION 'O banco já tem movimentações ou pedidos: use um banco recém-criado.';
    END IF;

    -- 1) O saldo de abertura do seed vira LINHAS de estoque (ENTRADA com validade)
    UPDATE ingrediente SET quantidade_estoque = 0;
    INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante, data_movimentacao, observacao, data_validade)
    SELECT i.id, v_admin, 'ENTRADA', e.qtd, e.qtd, now() - interval '40 days', e.obs, CURRENT_DATE + e.dias
    FROM (VALUES
        ('Açaí (polpa)',           30.000, 20,   'Lote 1 (vence antes)'),
        ('Açaí (polpa)',           30.000, 60,   'Lote 2'),
        ('Leite condensado',       15.000, 90,   NULL),
        ('Granola',                 6.000, -2,   'Lote VENCIDO de propósito (alerta de validade)'),
        ('Granola',                 6.000, 45,   NULL),
        ('Banana',                 12.000, 3,    'Vence em 3 dias (alerta de validade)'),
        ('Morango',                 2.700, 10,   'Ficará abaixo do mínimo (alerta de reposição)'),
        ('Leite em pó',             5.000, 120,  NULL),
        ('Copo descartável 500ml', 300.000, NULL, 'Sem validade: nunca vence')
    ) AS e(nome, qtd, dias, obs) JOIN ingrediente i ON i.nome = e.nome;
    UPDATE ingrediente i SET quantidade_estoque = x.total
    FROM (SELECT ingrediente_id, SUM(quantidade_restante) AS total FROM movimentacao_estoque WHERE tipo = 'ENTRADA' GROUP BY 1) x
    WHERE x.ingrediente_id = i.id;

    -- 2) Clientes
    INSERT INTO cliente (nome, contato, endereco) VALUES
        ('Maria Souza', '(16) 99999-0001', 'Rua das Flores, 100'),
        ('João Pereira', '(16) 99999-0002', 'Av. Brasil, 250'),
        ('Ana Lima', '(16) 99999-0003', 'Rua Sete, 33');

    -- 3) Pedidos: 3 no mês anterior e 3 neste mês (data_pedido é única e serve de chave de ligação)
    CREATE TEMP TABLE pedido_demo (ts TIMESTAMP, usuario INT, cliente_nome TEXT) ON COMMIT DROP;
    CREATE TEMP TABLE item_demo (ts TIMESTAMP, produto_id INT, qtd INT) ON COMMIT DROP;
    INSERT INTO pedido_demo VALUES
        (date_trunc('month', now()) - interval '25 days' + interval '10 hours', v_admin, 'Maria Souza'),
        (date_trunc('month', now()) - interval '20 days' + interval '11 hours', v_func,  NULL),
        (date_trunc('month', now()) - interval '10 days' + interval '12 hours', v_admin, 'João Pereira'),
        (date_trunc('month', now()) + interval '1 hour',  v_func,  'Ana Lima'),
        (date_trunc('month', now()) + interval '2 hours', v_admin, NULL),
        (date_trunc('month', now()) + interval '3 hours', v_func,  'Maria Souza');
    INSERT INTO item_demo SELECT p.ts, i.produto_id, i.qtd FROM (
        SELECT 1 AS n, 1 AS produto_id, 4 AS qtd UNION ALL SELECT 1, 2, 2 UNION ALL SELECT 2, 1, 3
        UNION ALL SELECT 3, 3, 5 UNION ALL SELECT 3, 2, 1 UNION ALL SELECT 4, 1, 2 UNION ALL SELECT 4, 3, 4
        UNION ALL SELECT 5, 2, 6 UNION ALL SELECT 6, 3, 3 UNION ALL SELECT 6, 1, 1
    ) i JOIN (SELECT ts, row_number() OVER (ORDER BY ts) AS n FROM pedido_demo) p ON p.n = i.n;
    INSERT INTO pedido (cliente_id, usuario_id, data_pedido, valor_total)
        SELECT c.id, d.usuario, d.ts, 0 FROM pedido_demo d LEFT JOIN cliente c ON c.nome = d.cliente_nome;
    INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado)
        SELECT p.id, it.produto_id, it.qtd, pr.preco_venda FROM item_demo it
        JOIN pedido p ON p.data_pedido = it.ts JOIN produto pr ON pr.id = it.produto_id;
    UPDATE pedido p SET valor_total = t.total
    FROM (SELECT pedido_id, SUM(quantidade * preco_unitario_registrado) AS total FROM pedido_produto GROUP BY 1) t WHERE t.pedido_id = p.id;

    -- 4) Baixa FEFO dos ingredientes de cada pedido (uma SAIDA por pedido x ingrediente, motivo PEDIDO)
    FOR v_ped IN SELECT id, usuario_id, data_pedido FROM pedido ORDER BY data_pedido LOOP
        FOR v_ing IN SELECT pi.ingrediente_id, SUM(pp.quantidade * pi.quantidade_utilizada) AS qtd
                 FROM pedido_produto pp JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
                 WHERE pp.pedido_id = v_ped.id GROUP BY pi.ingrediente_id ORDER BY pi.ingrediente_id LOOP
            PERFORM pg_temp.baixar_fefo(v_ing.ingrediente_id, v_ing.qtd, v_ped.usuario_id, 'PEDIDO', v_ped.data_pedido, 'Baixa automática do pedido ' || v_ped.id);
        END LOOP;
    END LOOP;

    -- 5) Uma saída convencional (quebra de banana), para o motivo CONVENCIONAL aparecer na demonstração
    PERFORM pg_temp.baixar_fefo((SELECT id FROM ingrediente WHERE nome = 'Banana'), 0.500, v_admin, 'CONVENCIONAL', now()::timestamp, 'Banana madura demais');
END
$demo$;

-- Conferência da invariante (deve devolver 0 linhas)
SELECT i.id, i.nome, i.quantidade_estoque AS saldo,
       COALESCE((SELECT SUM(quantidade_restante) FROM movimentacao_estoque m WHERE m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'), 0) AS soma_linhas
FROM ingrediente i
WHERE i.quantidade_estoque <> COALESCE((SELECT SUM(quantidade_restante) FROM movimentacao_estoque m WHERE m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'), 0);
COMMIT;
