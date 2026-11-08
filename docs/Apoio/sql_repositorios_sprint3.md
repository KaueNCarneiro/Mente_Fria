# SQL dos repositórios — Sprint 3 (estoque por linhas, pedido, produção, reposição e dashboard)

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

**Última atualização:** 04/11/2026. Continua o `sql_repositorios.md` (Sprint 1, US1–US6). A **lógica** dos métodos da Sprint 1 (usuário, unidade, ingrediente e produto) segue valendo, mas o SQL exato de alguns deles mudou nos repositórios de referência: as diferenças estão na §8.

> **Status:** os SQLs abaixo são **cópia fiel** dos repositórios de referência (`docs/Sprint_3/referencia/backend/src/main/java/com/mentefria/backend/`), os mesmos que entram no backend nas fases do Lucas e do Kauê (tutorial do Diogo, Parte 5). As consultas 1 a 13 de `docs/Sprint_3/referencia/bd/consultas_sprint3.sql` foram executadas no PostgreSQL 16 sobre o `Script_DDL.sql` da Sprint 3 e o `seed_demo.sql` (26/10). Os repositórios foram exercitados pelos testes de integração da referência na validação do tutorial do Lucas (29 e 30/10, num clone temporário). **Este documento não foi executado à parte:** ele organiza e explica o SQL que já existe.

Este documento é o contrato entre quem cuida dos dados e quem escreve os `Repository` em Java. Para cada método, ele mostra o SQL pronto, o que o método devolve e com que erro o banco pode recusar. A numeração **(1) a (13)** é a do `consultas_sprint3.sql`; onde o SQL do `.java` difere dele, vale o do `.java` (diferenças na §7). Para achar uma consulta pelo número, use o índice da §3.

---

## 1. Convenções

As convenções da Sprint 1 (`sql_repositorios.md`, §1) continuam valendo: `NamedParameterJdbcTemplate`, SQL **sempre** com parâmetros, `BigDecimal` para quantidades e valores, regra de negócio no `Service` e nunca no `Repository`. A Sprint 3 acrescenta:

- **`:hoje` e `:agora` vêm do Java** (decisões F3 e A9: a F3 trouxe o `:hoje`, e a A9 ampliou para o `:agora`): `LocalDate.now(clock)` e `LocalDateTime.now(clock)`, com o relógio no fuso de São Paulo (`RelogioConfig`). O SQL **nunca** usa `CURRENT_DATE` nem `CURRENT_TIMESTAMP`: o Render roda em UTC, 3 horas à frente. O `DEFAULT CURRENT_TIMESTAMP` das tabelas fica só como rede de segurança.
- **Período `[inicio, fim)`:** as consultas de reposição e de dashboard recebem `:inicio` (incluído) e `:fim` (excluído), ambos `LocalDateTime` à meia-noite. Assim, um pedido às 23h59 do último dia entra, e um à 0h00 do dia seguinte não.
- **`FOR UPDATE` só dentro de uma transação** (`@Transactional` no `Service`). Fora dela, a trava é solta na hora e não protege nada.
- **Ordem das travas** (sem *deadlock*): primeiro os ingredientes, **em ordem de `id`**; depois as linhas de cada um. Pedido, saída, entrada e descarte seguem a mesma ordem (§4).
- **Falhas possíveis** listam a constraint e o SQLSTATE. A tradução para mensagem e código HTTP está em `mapa_constraints.md`.

---

## 2. Rastreabilidade com o Contrato de Comunicação

Rotas da Sprint 3 e os métodos que cada uma chama, na ordem em que o `Service` chama. **Perfil:** "Ambos" é Administrador e Funcionário (qualquer usuário logado).

| Rota | Perfil | História | Métodos do repositório |
|---|---|---|---|
| `GET /api/estoque` | Ambos | US9 | `IngredienteRepository.listarEstoque` (1) |
| `GET /api/estoque/{ingrediente_id}/linhas` | Ambos | US9 | `IngredienteRepository.buscarPorId` (Sprint 1; `404` se não existe) → `MovimentacaoRepository.linhasDoIngrediente` (2) |
| `POST /api/estoque/movimentacoes` (`ENTRADA`) | Ambos | US9 | `IngredienteRepository.travar` → `buscarPorId` → `MovimentacaoRepository.inserirEntrada` (7) → `IngredienteRepository.somarSaldo` (7) → `buscarPorId` (o saldo novo, para a resposta) |
| `POST /api/estoque/movimentacoes` (`SAIDA`) | Ambos | US9 | `IngredienteRepository.travar` → `buscarPorId` → `MovimentacaoRepository.linhasValidas` (4, motivo `CONVENCIONAL`) ou `linhasVencidas` (5, motivo `DESCARTE_VENCIDO`) → a baixa (6) → `buscarPorId` (o saldo novo, para a resposta) |
| `POST /api/estoque/linhas/{linha_id}/descarte` | Ambos | US9 (A12, opcional) | `MovimentacaoRepository.ingredienteDaEntrada` → `IngredienteRepository.travar` → `MovimentacaoRepository.travarLinha` → a baixa (6) → `IngredienteRepository.buscarPorId` (o saldo novo, para a resposta) |
| `GET /api/pedidos` | Ambos | US12 | `PedidoRepository.listar` |
| `POST /api/pedidos` | Ambos | US12 | `ProdutoRepository.buscarPorIds` → `composicoes` → `IngredienteRepository.travar` → `MovimentacaoRepository.linhasValidas` (4) → `PedidoRepository.inserir` → `inserirItens` → a baixa (6), uma por ingrediente |
| `POST /api/producao/recomendacao` | Administrador | US10 | `IngredienteRepository.entradaOtimizador` (3) → `ProdutoRepository.listarAtivos` → `composicoes` |
| `POST /api/reposicao/recomendacao` | Administrador | US14 | `ReposicaoRepository.maisConsumidos` (9) → `IngredienteRepository.saldosValidos` |
| `GET /api/dashboard` | Administrador | US17 | `DashboardRepository.ranking` (13) → `IngredienteRepository.listarEstoque` (1) → `DashboardRepository.faturamento` (10) → `maisVendidos` (11) → `ingredientesMaisUtilizados` (12) |

**A baixa (6)** é a classe `BaixaPorLinhas` (pacote `estoque`), usada pelo pedido, pela saída convencional e pelo descarte: para cada linha, `MovimentacaoRepository.consumirLinha`; no fim, `IngredienteRepository.baixarSaldo` e `MovimentacaoRepository.inserirSaida`.

---

## 3. Repositórios

### Índice: consulta → método

| # | Consulta | Método(s) | Onde |
|---|---|---|---|
| 1 | Estoque: total, válido, vencido, validade mais próxima | `IngredienteRepository.listarEstoque` | §3, IngredienteRepository |
| 2 | Linhas de um ingrediente, com a `situacao` | `MovimentacaoRepository.linhasDoIngrediente` | §3, MovimentacaoRepository |
| 3 | Entrada do Python: saldo **válido** e custo | `IngredienteRepository.entradaOtimizador` | §3, IngredienteRepository |
| 4 | Linhas válidas para baixar, `FOR UPDATE` | `MovimentacaoRepository.linhasValidas` | §3, MovimentacaoRepository |
| 5 | Linhas vencidas para descartar, `FOR UPDATE` | `MovimentacaoRepository.linhasVencidas` | §3, MovimentacaoRepository |
| 6 | Baixa: linha, saldo e `SAIDA` | `MovimentacaoRepository.consumirLinha` + `IngredienteRepository.baixarSaldo` + `MovimentacaoRepository.inserirSaida` | §3 (os três) e §4 (a baixa) |
| 7 | `ENTRADA` + soma ao saldo | `MovimentacaoRepository.inserirEntrada` + `IngredienteRepository.somarSaldo` | §3 (os dois) |
| 8 | Invariante (0 linhas = tudo certo) | Sem método: usada no seed e nos testes | §4 |
| novo | Pedidos com os itens (CT40) | `PedidoRepository.listar` | §3, PedidoRepository |
| 9 | Reposição: os mais consumidos no período | `ReposicaoRepository.maisConsumidos` | §3, ReposicaoRepository |
| 10 | Faturamento do mês | `DashboardRepository.faturamento` | §3, DashboardRepository |
| 11 | Produtos mais vendidos | `DashboardRepository.maisVendidos` | §3, DashboardRepository |
| 12 | Ingredientes mais utilizados, pelo custo | `DashboardRepository.ingredientesMaisUtilizados` | §3, DashboardRepository |
| 13 | Ranking de vendas por usuário, com filtros | `DashboardRepository.ranking` | §3, DashboardRepository |

Sem número no `consultas_sprint3.sql`, mas também novos da Sprint 3: `IngredienteRepository.travar`, `MovimentacaoRepository.ingredienteDaEntrada` e `travarLinha`, `ProdutoRepository.buscarPorIds`, `composicoes` e `listarAtivos`, `PedidoRepository.inserir` e `inserirItens`, e `UsuarioRepository.travarParaCadastroDeAdministrador` (§8).

### IngredienteRepository (métodos novos da Sprint 3)

Os métodos da Sprint 1 (`listar`, `buscarPorId`, `inserir`, `atualizar`, `buscarNomesPorIds`) estão no `sql_repositorios.md`; o que mudou no SQL deles está na §8.

**`listarEstoque`** · US9, US17 · consulta (1)

Uma linha por ingrediente, com o saldo total, o válido, o vencido e a validade mais próxima (Telas 10/16; também o resumo "abaixo do mínimo" e "com vencidos" do dashboard).

```sql
SELECT i.id, i.nome, i.unidade_medida, i.quantidade_estoque, i.quantidade_minima,
       COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade IS NULL OR m.data_validade >= :hoje), 0) AS valida,
       COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade < :hoje), 0) AS vencida,
       MIN(m.data_validade) FILTER (WHERE m.quantidade_restante > 0 AND m.data_validade >= :hoje) AS proxima
FROM ingrediente i
LEFT JOIN movimentacao_estoque m ON m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'
GROUP BY i.id, i.nome, i.unidade_medida, i.quantidade_estoque, i.quantidade_minima
ORDER BY i.nome, i.id
```

- **Retorno:** lista de `EstoqueItem` (uma linha por ingrediente, inclusive os sem entrada, com `0`)
- **Nota:** `quantidade_estoque` é o saldo **total** (válido + vencido). Uma linha que **vence hoje** ainda é válida (`>= :hoje`). O `FILTER` soma só as linhas que passam na condição, sem precisar de uma subconsulta para cada coluna. O `LEFT JOIN` com `m.tipo = 'ENTRADA'` **dentro do `ON`** mantém os ingredientes sem nenhuma entrada (no `WHERE`, eles sumiriam).

**`entradaOtimizador`** · US10 · consulta (3)

O que o módulo Python de produção recebe: os ingredientes usados por algum produto **ativo**, com o saldo **válido** e o custo.

```sql
SELECT i.id, i.custo_unitario,
       COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade IS NULL OR m.data_validade >= :hoje), 0) AS valida
FROM ingrediente i
LEFT JOIN movimentacao_estoque m ON m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'
WHERE i.id IN (SELECT pi.ingrediente_id FROM produto_ingrediente pi JOIN produto p ON p.id = pi.produto_id WHERE p.ativo)
GROUP BY i.id, i.custo_unitario ORDER BY i.id
```

- **Retorno:** lista de `IngredienteOtimizador` (`id`, `quantidadeEstoque`, `custoUnitario`)
- **Nota:** no JSON para o Python, o campo se chama `quantidade_estoque` (o nome segue o contrato com o Python), mas o valor é o saldo **válido** (§5). Um ingrediente com saldo `0` entra assim mesmo: é ele que limita a produção (CT29).

**`travar`** · US9, US12

Trava os ingredientes que a operação vai mexer, **em ordem de `id`**, até o fim da transação.

```sql
SELECT id FROM ingrediente WHERE id IN (:ids) ORDER BY id FOR UPDATE
```

- **Retorno:** nada (o resultado é descartado; o que importa é a trava)
- **Nota:** `:ids` é uma `List<Integer>` já ordenada; lista vazia não chega ao banco (o método volta antes). É o que garante o CT37 (dois pedidos ao mesmo tempo, sem *deadlock*). Por que em ordem: se o pedido A travasse açaí e depois banana, e o pedido B travasse banana e depois açaí, cada um esperaria o outro para sempre (*deadlock*). Com a mesma ordem para todos, o segundo só espera o primeiro terminar.

**`somarSaldo`** · US9 · consulta (7)

Soma a quantidade de uma `ENTRADA` ao saldo total do ingrediente, na mesma transação do `inserirEntrada`.

```sql
UPDATE ingrediente SET quantidade_estoque = quantidade_estoque + :q WHERE id = :id
```

- **Retorno:** linhas afetadas
- **Falhas possíveis:** nenhuma constraint esperada (o ingrediente já foi conferido e travado). Um saldo acima de 9.999.999,999 estouraria a coluna `DECIMAL(10,3)` (22003)

**`baixarSaldo`** · US9, US12 · consulta (6)

Tira do saldo total o que a baixa consumiu das linhas.

```sql
UPDATE ingrediente SET quantidade_estoque = quantidade_estoque - :q WHERE id = :id
```

- **Retorno:** linhas afetadas
- **Falhas possíveis:** `chk_ingrediente_quantidade_estoque` (23514) se o saldo ficasse negativo
- **Nota:** o `Service` confere antes se as linhas cobrem a quantidade e responde `422`. A constraint é a **rede de segurança** (T71): ela só dispara se o Java errar.

**`saldosValidos`** · US14

Saldo **válido** de todos os ingredientes, usado pela reposição. **Não tem SQL próprio:** chama o `listarEstoque` e guarda o `quantidadeValida` de cada ingrediente num `Map`.

- **Retorno:** `Map<Integer, BigDecimal>` (id do ingrediente → saldo válido)

### MovimentacaoRepository

**`linhasDoIngrediente`** · US9 · consulta (2)

As `ENTRADA`s de um ingrediente, com a situação de cada linha (detalhe do estoque, Telas 11/17).

```sql
SELECT m.id, m.data_movimentacao, m.quantidade, m.quantidade_restante, m.data_validade,
       CASE WHEN m.data_validade IS NULL THEN 'SEM_VALIDADE'
            WHEN m.data_validade < :hoje THEN 'VENCIDA'
            WHEN m.data_validade <= :hoje + 7 THEN 'PROXIMA' ELSE 'OK' END AS situacao
FROM movimentacao_estoque m WHERE m.ingrediente_id = :ing AND m.tipo = 'ENTRADA'
ORDER BY m.data_validade NULLS LAST, m.id
```

- **Retorno:** lista de `LinhaEstoque`, inclusive as linhas já zeradas (histórico)
- **Nota:** `PROXIMA` = vence entre hoje e daqui a 7 dias (`data_validade <= :hoje + 7`). Uma linha que vence hoje é `PROXIMA`, não `VENCIDA`. Somar um número a uma `DATE` no PostgreSQL soma dias. O `Service` confere antes se o ingrediente existe (`404`), porque um ingrediente sem entradas também devolve lista vazia.

**`linhasValidas`** · US9, US12 · consulta (4)

As linhas que **podem** ser consumidas por um pedido ou uma saída convencional, na ordem FEFO, já travadas.

```sql
SELECT id, quantidade_restante FROM movimentacao_estoque
WHERE ingrediente_id = :ing AND tipo = 'ENTRADA' AND quantidade_restante > 0
  AND (data_validade IS NULL OR data_validade >= :hoje)
ORDER BY data_validade NULLS LAST, id FOR UPDATE
```

- **Retorno:** lista de `Linha` (`id`, `restante`)
- **Nota:** **FEFO** (*First Expired, First Out*): a que vence primeiro sai primeiro. Linhas sem validade vão por último (`NULLS LAST`); no empate, a mais antiga (`id`). **Nunca** devolve linha vencida: um pedido não consome produto vencido (CT33). A ordem FEFO é a dos CT31 e CT32. O índice parcial `ix_movimentacao_linhas` (só `ENTRADA` com saldo) acelera esta busca.

**`linhasVencidas`** · US9 · consulta (5)

As linhas vencidas com saldo, para o descarte (saída com motivo `DESCARTE_VENCIDO`).

```sql
SELECT id, quantidade_restante FROM movimentacao_estoque
WHERE ingrediente_id = :ing AND tipo = 'ENTRADA' AND quantidade_restante > 0 AND data_validade < :hoje
ORDER BY data_validade, id FOR UPDATE
```

- **Retorno:** lista de `Linha` (`id`, `restante`)
- **Nota:** é o espelho da consulta (4): só o que **já venceu**, da mais antiga para a mais nova (CT34). Uma linha sem validade nunca vence, então nunca aparece aqui.

**`consumirLinha`** · US9, US12 · consulta (6)

Tira de uma linha a parte que a baixa consumiu. A `BaixaPorLinhas` chama uma vez por linha, até cobrir a quantidade.

```sql
UPDATE movimentacao_estoque SET quantidade_restante = quantidade_restante - :tira WHERE id = :id
```

- **Retorno:** linhas afetadas
- **Falhas possíveis:** `chk_movimentacao_restante` (23514) se o restante ficasse negativo
- **Nota:** `:tira` é o menor valor entre o restante da linha e o que ainda falta. A linha já está travada pela consulta (4) ou (5).

**`inserirSaida`** · US9, US12 · consulta (6)

Grava a `SAIDA`, com o motivo e a hora do Java. Vem depois de consumir as linhas e baixar o saldo.

```sql
INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo, data_movimentacao, observacao)
VALUES (:ing, :usuario, 'SAIDA', :q, :motivo, :agora, :obs) RETURNING id
```

- **Retorno:** `id` gerado (`int`)
- **Falhas possíveis:** `chk_movimentacao_motivo` (23514): motivo vazio ou fora de `PEDIDO`, `CONVENCIONAL`, `DESCARTE_VENCIDO` · `chk_movimentacao_quantidade` (23514) · `fk_movimentacao_ingrediente`, `fk_movimentacao_usuario` (23503) · NOT NULL em `quantidade` (23502)
- **Nota:** `quantidade_restante` e `data_validade` ficam `NULL` numa saída: as constraints `chk_movimentacao_restante` e `chk_movimentacao_data_validade` exigem isso. O motivo `PEDIDO` só o backend grava; o `Service` recusa `PEDIDO` vindo da tela. `data_movimentacao = :agora` mantém a hora no fuso de São Paulo (CT39).

**`inserirEntrada`** · US9 · consulta (7)

Grava uma `ENTRADA`, que já nasce como uma **linha** de estoque, com `restante = quantidade`. Vem antes do `somarSaldo`, na mesma transação.

```sql
INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, quantidade_restante, data_validade,
                                  data_movimentacao, observacao)
VALUES (:ing, :usuario, 'ENTRADA', :q, :q, :validade, :agora, :obs) RETURNING id
```

- **Retorno:** `id` gerado (`int`)
- **Falhas possíveis:** `chk_movimentacao_quantidade` (23514, CT26) · `chk_movimentacao_restante` (23514) · `fk_movimentacao_ingrediente`, `fk_movimentacao_usuario` (23503) · NOT NULL em `quantidade` (23502)
- **Nota:** o mesmo parâmetro `:q` preenche `quantidade` e `quantidade_restante`. `data_validade` é opcional (`NULL` = sem validade). O `INSERT` não tem a coluna `motivo`, então ela fica `NULL`, como a `chk_movimentacao_motivo` exige numa entrada; um motivo enviado pela tela numa entrada é recusado antes, no `Service` (`400` "O motivo só pode ser informado em saídas."). Uma entrada com validade já vencida é aceita (**proposta; decisão do PO**, comentário no `EstoqueService`): a linha nasce vencida e aparece para descarte.

**`ingredienteDaEntrada`** · US9 (A12, opcional)

De qual ingrediente é uma `ENTRADA`, **sem travar**, para o descarte de uma linha travar o ingrediente **antes** da linha (a mesma ordem do pedido).

```sql
SELECT ingrediente_id FROM movimentacao_estoque WHERE id = :id AND tipo = 'ENTRADA'
```

- **Retorno:** 0 ou 1 linha (`Optional<Integer>`; vazio → `404` "Linha de estoque não encontrada.")

**`travarLinha`** · US9 (A12, opcional)

A `ENTRADA` que vai ser descartada, já travada, com o restante e a validade, para o `Service` conferir se está vencida e se ainda tem saldo.

```sql
SELECT id, quantidade_restante, data_validade FROM movimentacao_estoque
WHERE id = :id AND tipo = 'ENTRADA' FOR UPDATE
```

- **Retorno:** 0 ou 1 linha (`Optional<LinhaTravada>`)
- **Nota:** linha não vencida, ou sem saldo, → `422` no `Service`. O descarte de uma linha é o CT71.

### ProdutoRepository (métodos novos da Sprint 3)

Os métodos da Sprint 1 (`inserir`, `inserirComposicao`, `atualizarPreco`, `listarComComposicao`) estão no `sql_repositorios.md`; o que mudou no SQL deles está na §8.

**`buscarPorIds`** · US12

Os produtos de um pedido, com o preço **do banco** (nunca o que veio da tela) e se estão ativos.

```sql
SELECT id, nome, preco_venda, ativo FROM produto WHERE id IN (:ids) ORDER BY id
```

- **Retorno:** `Map<Integer, Produto.Basico>`; um id que não volta é um produto inexistente (`400` "Produto não encontrado.")
- **Nota:** lista vazia não chega ao banco (`IN ()` é inválido; o método volta antes).

**`composicoes`** · US10, US12

A composição de vários produtos de uma vez: quanto de cada ingrediente cada produto usa.

```sql
SELECT produto_id, ingrediente_id, quantidade_utilizada FROM produto_ingrediente WHERE produto_id IN (:ids) ORDER BY produto_id, ingrediente_id
```

- **Retorno:** `Map<Integer, List<Produto.ItemComposicao>>` (id do produto → itens)

**`listarAtivos`** · US10

Os produtos que entram na recomendação de produção.

```sql
SELECT id, nome, preco_venda, ativo FROM produto WHERE ativo ORDER BY id
```

- **Retorno:** lista de `Produto.Basico`

### PedidoRepository

> **Transação obrigatória:** `inserir` + `inserirItens` + a baixa de cada ingrediente formam **uma operação só** (CT25: tudo ou nada). Se faltar estoque de um ingrediente, nada é gravado: nem o pedido, nem os itens, nem as baixas dos outros ingredientes.

**`inserir`** · US12

Cria o pedido, com o total já calculado pelo `Service` com os preços do banco e a hora do Java.

```sql
INSERT INTO pedido (cliente_id, usuario_id, valor_total, data_pedido) VALUES (:cliente, :usuario, :total, :agora) RETURNING id
```

- **Retorno:** `id` gerado (`int`)
- **Falhas possíveis:** `fk_pedido_cliente` (23503, cliente inexistente → `400`) · `chk_pedido_valor_total` (23514) · `fk_pedido_usuario` (23503) · NOT NULL em `usuario_id`, `valor_total` (23502)
- **Nota:** `cliente_id` é opcional (`NULL` = venda sem cliente). `status` nasce `ATIVO` (default). `data_pedido = :agora` deixa o pedido no dia e no mês certos de São Paulo (CT39).

**`inserirItens`** · US12

Grava os itens do pedido, cada um com o preço **registrado** no momento da venda. Use `batchUpdate`, um item por linha.

```sql
INSERT INTO pedido_produto (pedido_id, produto_id, quantidade, preco_unitario_registrado) VALUES (:pedido, :produto, :q, :preco)
```

- **Retorno:** — (`batchUpdate`)
- **Falhas possíveis:** `pedido_produto_pkey` (23505, produto repetido) · `chk_pedido_produto_quantidade`, `chk_pedido_produto_preco` (23514) · `fk_pedido_produto_produto`, `fk_pedido_produto_pedido` (23503) · NOT NULL em `quantidade`, `preco_unitario_registrado` (23502)
- **Nota:** o preço fica gravado no item porque o preço do produto pode mudar depois (US6). O faturamento e o ranking somam o preço **registrado**, não o atual.

**`listar`** · US12 (CT40)

Os pedidos com os itens, do mais recente para o mais antigo.

```sql
SELECT p.id, p.data_pedido, p.cliente_id, u.nome AS usuario_nome, p.valor_total, p.status,
       pp.produto_id, pr.nome AS produto_nome, pp.quantidade, pp.preco_unitario_registrado
FROM pedido p
JOIN usuario u ON u.id = p.usuario_id
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto pr ON pr.id = pp.produto_id
ORDER BY p.data_pedido DESC, p.id DESC, pp.produto_id
```

- **Retorno:** **uma linha por item**; o método agrupa por `p.id` num `LinkedHashMap` (que mantém a ordem) e devolve uma lista de `PedidoResumo`, cada um com os seus itens
- **Nota:** não está no `consultas_sprint3.sql`: foi escrita junto com o `PedidoRepository`.

### ReposicaoRepository

**`maisConsumidos`** · US14 · consulta (9)

Os ingredientes mais consumidos no período, com o que a recomendação de reposição precisa.

```sql
SELECT i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima,
       SUM(pp.quantidade * pi.quantidade_utilizada) AS consumo
FROM pedido p
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
JOIN ingrediente i ON i.id = pi.ingrediente_id
WHERE p.data_pedido >= :inicio AND p.data_pedido < :fim
GROUP BY i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima
ORDER BY consumo DESC, i.id
LIMIT :limite
```

- **Retorno:** lista de `Consumo`; lista vazia = não houve pedidos no período (o `Service` responde sem chamar o Python)
- **Nota:** **consumo = pedidos × composição** (F12, resolvida pela A18): cada item vendido vezes o quanto o produto usa de cada ingrediente. A composição usada é a **atual** (`produto_ingrediente`), e não uma gravada na venda: isso vale porque não existe rota para editar a composição (A18; se um dia existir, revisar). **Pedidos cancelados contam** (A18: o ingrediente foi gasto). Descartes e saídas convencionais não contam. `:limite` = 8 para até 30 dias e 15 acima; no empate, o menor `id`.

### DashboardRepository

As quatro consultas recebem o mesmo período: do dia 1 do mês escolhido (padrão: o atual) ao dia 1 do mês seguinte. **Só contam pedidos `ATIVO`.**

**`faturamento`** · US17 · consulta (10)

Total vendido e número de pedidos no mês.

```sql
SELECT COALESCE(SUM(valor_total), 0) AS valor, COUNT(*) AS pedidos FROM pedido
WHERE status = 'ATIVO' AND data_pedido >= :inicio AND data_pedido < :fim
```

- **Retorno:** `Faturamento` (`valorTotal`, `quantidadePedidos`), sempre **1 linha** (`queryForObject`); mês sem pedidos → `0` e `0`, graças ao `COALESCE`

**`maisVendidos`** · US17 · consulta (11)

Os produtos mais vendidos no mês, em quantidade e em valor.

```sql
SELECT pr.id, pr.nome, SUM(pp.quantidade) AS quantidade, SUM(pp.quantidade * pp.preco_unitario_registrado) AS valor
FROM pedido p
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto pr ON pr.id = pp.produto_id
WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
GROUP BY pr.id, pr.nome
ORDER BY quantidade DESC, valor DESC, pr.id
```

- **Retorno:** lista de `ProdutoVendido`

**`ingredientesMaisUtilizados`** · US17 · consulta (12)

Os ingredientes usados no mês, ordenados pelo **custo** do que foi usado.

```sql
SELECT i.id, i.nome, i.unidade_medida, SUM(pp.quantidade * pi.quantidade_utilizada) AS quantidade,
       ROUND(SUM(pp.quantidade * pi.quantidade_utilizada) * i.custo_unitario, 2) AS custo
FROM pedido p
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
JOIN ingrediente i ON i.id = pi.ingrediente_id
WHERE p.status = 'ATIVO' AND p.data_pedido >= :inicio AND p.data_pedido < :fim
GROUP BY i.id, i.nome, i.unidade_medida, i.custo_unitario
ORDER BY custo DESC, i.id
```

- **Retorno:** lista de `IngredienteUtilizado`
- **Nota:** ordena pelo custo, e não pela quantidade, porque kg e unidades não se comparam (A17): 2 kg de açaí e 50 copos não dizem qual "pesou" mais. O consumo é a mesma fórmula da consulta (9) (§5).

**`ranking`** · US17 · consulta (13)

Vendas por usuário no mês, com quatro filtros **opcionais**: usuário, produtos, valor mínimo e valor máximo.

```sql
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
ORDER BY valor DESC, pedidos DESC, u.id
```

- **Retorno:** lista de `LinhaRanking`; o `Service` numera as posições (1, 2, 3…)
- **Nota — filtros opcionais:** cada filtro tem a forma "`parâmetro IS NULL OR condição`": sem o filtro, a primeira metade é verdadeira e a linha passa. O `CAST` é obrigatório porque o PostgreSQL precisa saber o **tipo** de um `NULL`; no Java, `addValue("usuario", null, Types.INTEGER)`.
- **Nota — filtro de produto:** `IN ()` vazio é inválido, então o Java manda `:filtrar_produto = false` e uma lista "de mentira" (`[-1]`) quando não há filtro. Com o filtro, a quantidade e o valor somam **só** os produtos escolhidos.
- **Nota — `WHERE` × `HAVING`:** o filtro de valor vai no `HAVING` porque compara o valor **já somado** por usuário. O `WHERE` filtra as linhas antes de somar.

---

## 4. Transações e ordem das travas

Todo fluxo que mexe em estoque roda num único `@Transactional` e trava na mesma ordem: **ingredientes por `id` → linhas de cada um**.

| Fluxo | Passos, em ordem |
|---|---|
| **Pedido** (`PedidoService.criar`) | validar itens → `buscarPorIds` → `composicoes` → somar o necessário por ingrediente → `travar` (todos, por `id`) → `linhasValidas` de cada um → se algum não cobre: `422` e nada é gravado → `inserir` → `inserirItens` → a baixa de cada ingrediente (motivo `PEDIDO`) |
| **Entrada** | `travar` → `buscarPorId` → `inserirEntrada` → `somarSaldo` → `buscarPorId` (saldo para a resposta) |
| **Saída convencional / descarte** | `travar` → `buscarPorId` → `linhasValidas` (convencional) ou `linhasVencidas` (descarte) → a baixa → `buscarPorId` (saldo para a resposta) |
| **Descarte de uma linha** (A12) | `ingredienteDaEntrada` → `travar` → `travarLinha` → conferir (vencida e com saldo) → a baixa → `buscarPorId` (saldo para a resposta) |
| **A baixa** (`BaixaPorLinhas.consumir`) | se as linhas não cobrem: erro, sem gravar → `consumirLinha` em cada linha, na ordem recebida, até cobrir → `baixarSaldo` → `inserirSaida` |

**A invariante** (consulta 8): em qualquer momento, o saldo total de cada ingrediente é a soma dos restantes das suas `ENTRADA`s. Ela não tem método no backend: é conferida no fim do `seed_demo.sql` e nos testes (CT36). **Deve devolver 0 linhas.**

```sql
SELECT i.id FROM ingrediente i WHERE i.quantidade_estoque <>
  COALESCE((SELECT SUM(quantidade_restante) FROM movimentacao_estoque m WHERE m.ingrediente_id = i.id AND m.tipo = 'ENTRADA'), 0);
```

---

## 5. As regras que mais confundem

1. **`:hoje` e `:agora` vêm do Java (F3 e A9).** O banco no Render está em UTC. Se o SQL usasse `CURRENT_DATE`, das 21h à meia-noite (horário de São Paulo) o banco já estaria no dia seguinte: uma linha que vence hoje apareceria vencida, e um pedido das 22h cairia no dia (ou no mês) errado.
2. **`quantidade_estoque` tem dois sentidos.** Na tabela `ingrediente` (e no `listarEstoque`), é o saldo **total**, válido + vencido. No JSON da produção (consulta 3), o campo com o mesmo nome leva o saldo **válido**, porque o produto vencido não pode ser usado. O nome segue o contrato com o Python.
3. **Uma só fórmula de consumo (F12, resolvida pela A18), dois filtros.** A reposição (9) e o dashboard (12) calculam o consumo do mesmo jeito: `quantidade vendida × quantidade_utilizada`. Muda só quais pedidos entram:

   | Consulta | Pedidos que contam | Por quê |
   |---|---|---|
   | (9) Reposição | **Todos**, inclusive cancelados (A18) | O ingrediente foi gasto e precisa ser reposto |
   | (10) a (13) Dashboard | Só `ATIVO` | Pedido cancelado não é venda |

4. **O período de cada rota.** O `Service` monta `:inicio` e `:fim`; o SQL só compara.

   | Rota | `:inicio` | `:fim` |
   |---|---|---|
   | Reposição (N dias) | hoje − (N − 1), à meia-noite | amanhã, à meia-noite |
   | Dashboard (mês) | dia 1 do mês, à meia-noite | dia 1 do mês seguinte, à meia-noite |

   Exemplo: reposição de 7 dias em 04/11 = de 29/10 00h00 até 05/11 00h00 (os 7 dias, contando hoje).

---

## 6. Fora deste documento

- **US13 (clientes):** as constraints de `cliente` existem e têm testes (T33–T36), mas o SQL dos repositórios não entra no semestre (A40).
- **US15/US16 completas (alertas):** o Adendo nº 1 (item 4) trata os alertas pela tela de estoque (consultas 1 e 2) e pelo resumo do dashboard, sem consultas próprias.
- **US27 (cancelar pedido):** o schema já tem `status`, `data_cancelamento` e `usuario_cancelamento_id`, e as consultas já filtram por `status`, mas o `UPDATE` de cancelamento fica fora do semestre (A40).
- **US26 (porção padrão por unidade):** condicional (A40). Se entrar, os métodos `inserir` e `atualizarPorcao` do `UnidadeMedidaRepository` vão para o `sql_repositorios.md` (tutorial do Diogo, Parte 10.5).

---

## 7. Diferenças em relação ao `consultas_sprint3.sql`

O `.sql` foi o rascunho executado em 26/10; os repositórios são a versão que vai para o backend. Onde diferem, vale o repositório (o SQL deste documento):

As consultas (8) a (13) são **iguais** nos dois. As diferenças:

| Consulta | No `consultas_sprint3.sql` | No repositório (vale) |
|---|---|---|
| (1) | Envolvida num `SELECT * FROM (…) x`; apelidos `i.id AS ingrediente_id`, `quantidade_valida`, `quantidade_vencida`, `validade_mais_proxima`; `ORDER BY nome` | Sem a subconsulta; `i.id` sem apelido; apelidos `valida`, `vencida`, `proxima`; `ORDER BY i.nome, i.id` (desempate estável) |
| (2), (4), (5) | Parâmetro `:ingrediente_id` | `:ing` (só o nome muda) |
| (3) | Apelido `quantidade_estoque` | Apelido `valida` (o nome `quantidade_estoque` fica só no JSON para o Python) |
| (6) | Um único bloco com os três comandos; parâmetros `:linha_id`, `:ingrediente_id`, `:usuario_id`, `:quantidade`, `:observacao`; o `INSERT` da `SAIDA` sem `RETURNING` e sem `data_movimentacao` | Três métodos (`consumirLinha`, `IngredienteRepository.baixarSaldo` e `inserirSaida`), chamados pela `BaixaPorLinhas`; parâmetros `:id`, `:ing`, `:usuario`, `:q`, `:obs` (no `baixarSaldo`, o ingrediente é `:id`); o `INSERT` tem `RETURNING id` e grava `data_movimentacao = :agora` (A9) |
| (7) | Um bloco com o `INSERT` e o `UPDATE`; parâmetros `:ingrediente_id`, `:usuario_id`, `:quantidade`, `:data_validade`, `:observacao`; sem `data_movimentacao` (usaria o `DEFAULT CURRENT_TIMESTAMP`, em UTC) | Dois métodos (`inserirEntrada` e `IngredienteRepository.somarSaldo`); parâmetros `:ing`, `:usuario`, `:q`, `:validade`, `:obs` (no `somarSaldo`, o ingrediente é `:id`); grava `data_movimentacao = :agora` (A9) |

---

## 8. Métodos da Sprint 1 que mudaram nos repositórios de referência

A lógica do `sql_repositorios.md` continua valendo para estes métodos, mas o SQL exato que vai para o backend é o do `.java` de referência:

| Método | No `sql_repositorios.md` (Sprint 1) | No repositório de referência (vale) |
|---|---|---|
| `IngredienteRepository.listar` | `ORDER BY nome` | `ORDER BY nome, id` |
| `IngredienteRepository.inserir` | `RETURNING id`; parâmetros `:unidade_medida`, `:custo_unitario`, `:porcao_padrao`, `:quantidade_minima` | `RETURNING` com todas as colunas (devolve o `Ingrediente` inteiro); parâmetros `:unidade`, `:custo`, `:porcao`, `:minima` |
| `IngredienteRepository.atualizar` | `UPDATE` → linhas afetadas (`0` = `404`) | `UPDATE … RETURNING` todas as colunas → `Optional<Ingrediente>` (vazio = `404`); mesmos parâmetros curtos do `inserir` |
| `IngredienteRepository.buscarNomesPorIds` | Sem ordem | `ORDER BY id`; devolve `Map<Integer, String>` |
| `ProdutoRepository.listarComComposicao` | Apelidos `produto_id`, `produto_nome`; `ORDER BY p.nome, i.nome` | `p.id`, `p.nome` sem apelido; `ORDER BY p.nome, p.id, i.nome`; o método já agrupa num `LinkedHashMap` |
| `UsuarioRepository.inserir` | Parâmetro `:senha_hash` | Parâmetro `:hash` |
| `UsuarioRepository.listarFuncionarios` | Seleciona `perfil`; `ORDER BY nome` | Sem `perfil`; `ORDER BY nome, id` |

**Método novo da Sprint 3 no `UsuarioRepository`:**

**`travarParaCadastroDeAdministrador`** · US1

Trava a tabela `usuario` antes de conferir se já existe Administrador, para que dois cadastros ao mesmo tempo no primeiro acesso não criem dois administradores (o risco que a D-A do `sql_repositorios.md` tinha aceitado).

```sql
LOCK TABLE usuario IN SHARE ROW EXCLUSIVE MODE
```

- **Retorno:** nada
- **Nota:** roda dentro da transação do cadastro (`UsuarioService`), antes do `existeAdministrador`. Esse modo de trava deixa os outros **lerem** a tabela, mas faz um segundo cadastro **esperar** o primeiro terminar. Aí ele já enxerga o Administrador criado e responde `409`.
