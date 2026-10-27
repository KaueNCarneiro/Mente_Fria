-- consultas_sprint3.sql — SQL da Sprint 3 (executado no PostgreSQL 16 sobre Script_DDL_sprint3.sql + seed_demo.sql)
-- :hoje é SEMPRE enviado pela aplicação (LocalDate em America/Sao_Paulo), nunca CURRENT_DATE do banco (o Render roda em UTC).

-- (1) GET /api/estoque — uma linha por ingrediente
SELECT * FROM (
  SELECT i.id AS ingrediente_id, i.nome, i.unidade_medida, i.quantidade_estoque, i.quantidade_minima,
         COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade IS NULL OR m.data_validade >= :hoje), 0) AS quantidade_valida,
         COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade < :hoje), 0)                          AS quantidade_vencida,
         MIN(m.data_validade) FILTER (WHERE m.quantidade_restante > 0 AND m.data_validade >= :hoje)             AS validade_mais_proxima
  FROM ingrediente i
  LEFT JOIN movimentacao_estoque m ON m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'
  GROUP BY i.id, i.nome, i.unidade_medida, i.quantidade_estoque, i.quantidade_minima) x
ORDER BY nome;

-- (2) GET /api/estoque/{ingrediente_id}/linhas — as ENTRADAs do ingrediente
SELECT m.id, m.data_movimentacao, m.quantidade, m.quantidade_restante, m.data_validade,
       CASE WHEN m.data_validade IS NULL THEN 'SEM_VALIDADE'
            WHEN m.data_validade < :hoje THEN 'VENCIDA'
            WHEN m.data_validade <= :hoje + 7 THEN 'PROXIMA' ELSE 'OK' END AS situacao
FROM movimentacao_estoque m WHERE m.ingrediente_id = :ingrediente_id AND m.tipo = 'ENTRADA'
ORDER BY m.data_validade NULLS LAST, m.id;

-- (3) Entrada do módulo Python: ingredientes dos produtos ATIVOS (inclusive com 0) com o saldo VÁLIDO e o custo
SELECT i.id, i.custo_unitario,
       COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade IS NULL OR m.data_validade >= :hoje), 0) AS quantidade_estoque
FROM ingrediente i
LEFT JOIN movimentacao_estoque m ON m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'
WHERE i.id IN (SELECT pi.ingrediente_id FROM produto_ingrediente pi JOIN produto p ON p.id = pi.produto_id WHERE p.ativo)
GROUP BY i.id, i.custo_unitario ORDER BY i.id;

-- (4) Baixa por linhas (pedido ou saída convencional): linhas VÁLIDAS, menor validade primeiro, sem validade por último
SELECT id, quantidade_restante FROM movimentacao_estoque
WHERE ingrediente_id = :ingrediente_id AND tipo = 'ENTRADA' AND quantidade_restante > 0
  AND (data_validade IS NULL OR data_validade >= :hoje)
ORDER BY data_validade NULLS LAST, id FOR UPDATE;
-- (5) Descarte de vencidos: só linhas VENCIDAS, da mais antiga para a mais nova
SELECT id, quantidade_restante FROM movimentacao_estoque
WHERE ingrediente_id = :ingrediente_id AND tipo = 'ENTRADA' AND quantidade_restante > 0 AND data_validade < :hoje
ORDER BY data_validade, id FOR UPDATE;
-- (6) Depois de decidir as linhas: UPDATE por linha, saldo e a SAIDA com motivo (tudo na MESMA transação)
UPDATE movimentacao_estoque SET quantidade_restante = quantidade_restante - :tira WHERE id = :linha_id;
UPDATE ingrediente SET quantidade_estoque = quantidade_estoque - :quantidade WHERE id = :ingrediente_id;
INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo, observacao) VALUES (:ingrediente_id, :usuario_id, 'SAIDA', :quantidade, :motivo, :observacao);
-- (7) ENTRADA: a linha nasce com restante = quantidade; soma ao saldo na mesma transação
INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante, data_validade, observacao)
VALUES (:ingrediente_id, :usuario_id, 'ENTRADA', :quantidade, :quantidade, :data_validade, :observacao) RETURNING id;
UPDATE ingrediente SET quantidade_estoque = quantidade_estoque + :quantidade WHERE id = :ingrediente_id;
-- (8) Invariante (deve devolver 0 linhas)
SELECT i.id FROM ingrediente i WHERE i.quantidade_estoque <>
  COALESCE((SELECT SUM(quantidade_restante) FROM movimentacao_estoque m WHERE m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'), 0);

-- ============================================================================================================
-- Acréscimo de 26/10/2026 (US14, US17): executadas no PostgreSQL 16.15 (Docker) sobre Script_DDL.sql + seed_demo.sql
-- e pelos testes de integração do backend (ReposicaoTest, DashboardTest). Período = [:inicio, :fim), vindos do Java.
-- Definição única de consumo (F12): pedidos x composição. Reposição conta pedidos CANCELADOS; dashboard, só ATIVO.
-- ============================================================================================================

-- (9) US14: os :limite ingredientes mais consumidos no período (8 até 30 dias; 15 acima), com o que a reposição precisa
SELECT i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima,
       SUM(pp.quantidade * pi.quantidade_utilizada) AS consumo
FROM pedido p
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
JOIN ingrediente i ON i.id = pi.ingrediente_id
WHERE p.data_pedido >= :inicio AND p.data_pedido < :fim
GROUP BY i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima
ORDER BY consumo DESC, i.id
LIMIT :limite;

-- (10) US17: faturamento do mês (só pedidos ATIVO)
SELECT COALESCE(SUM(valor_total), 0) AS valor, COUNT(*) AS pedidos FROM pedido
WHERE status = 'ATIVO' AND data_pedido >= :inicio AND data_pedido < :fim;

-- (11) US17: produtos mais vendidos no mês
SELECT pr.id, pr.nome, SUM(pp.quantidade) AS quantidade, SUM(pp.quantidade * pp.preco_unitario_registrado) AS valor
FROM pedido p
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto pr ON pr.id = pp.produto_id
WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
GROUP BY pr.id, pr.nome
ORDER BY quantidade DESC, valor DESC, pr.id;

-- (12) US17: ingredientes mais utilizados no mês, ordenados pelo CUSTO do que foi usado (kg e unidades não se comparam)
SELECT i.id, i.nome, i.unidade_medida, SUM(pp.quantidade * pi.quantidade_utilizada) AS quantidade,
       ROUND(SUM(pp.quantidade * pi.quantidade_utilizada) * i.custo_unitario, 2) AS custo
FROM pedido p
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
JOIN ingrediente i ON i.id = pi.ingrediente_id
WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
GROUP BY i.id, i.nome, i.unidade_medida, i.custo_unitario
ORDER BY custo DESC, i.id;

-- (13) US17: ranking de vendas por usuário, com os filtros de 14/10 (todos opcionais).
-- Filtro de produto restringe também a quantidade e o valor; filtro de valor vale DEPOIS de somar (HAVING).
-- Os nulos precisam de tipo (CAST): no Java, MapSqlParameterSource.addValue(nome, null, Types.INTEGER/NUMERIC).
SELECT u.id, u.nome, COUNT(DISTINCT p.id) AS pedidos,
       SUM(pp.quantidade * pp.preco_unitario_registrado) AS valor,
       STRING_AGG(DISTINCT pr.nome, ', ' ORDER BY pr.nome) AS tipos
FROM pedido p
JOIN usuario u ON u.id = p.usuario_id
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto pr ON pr.id = pp.produto_id
WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
  AND (CAST(:usuario AS INTEGER) IS NULL OR p.usuario_id = :usuario)
  AND (NOT :filtrar_produto OR pp.produto_id IN (:produtos))
GROUP BY u.id, u.nome
HAVING (CAST(:valor_min AS NUMERIC) IS NULL OR SUM(pp.quantidade * pp.preco_unitario_registrado) >= :valor_min)
   AND (CAST(:valor_max AS NUMERIC) IS NULL OR SUM(pp.quantidade * pp.preco_unitario_registrado) <= :valor_max)
ORDER BY valor DESC, pedidos DESC, u.id;
