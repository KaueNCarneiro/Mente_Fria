# Tutorial — Sprint 3 (Banco de Dados e Documentos) — Diogo

**Para:** Diogo Pereira Miranda · **Papel:** dados e qualidade da documentação · **Revisão:** 27/10/2026 (v5.2: mensagem ao professor pronta; P2 = 13.0.3; A11 com você) · **Substitui** a v5.1 (`historico/v5.1-27-10-antes-das-respostas/`), a versão da tarde (`historico/v5-26-10-antes-das-4-historias/`) e `historico/Tutorial_Sprint3_BancoDeDados_Diogo.md`.
**Base:** `sprint-3-roteiro.md` (v5.1), `sprint-3-decisoes-e-pendencias.md`, `sprint-3-casos-de-teste-propostos.md`.

> **O que foi verificado (26/10).**
> - Tudo executado **duas vezes**: num PostgreSQL 16.10 embutido (tarde) e, à noite, no **Docker real (postgres:16, versão 16.15)**, com **os mesmos comandos `docker` deste tutorial** num container temporário.
>   - O patch aplica (`git apply --check` ok) e o resultado é idêntico ao `referencia/bd/Script_DDL.sql`.
>   - Os 32 testes antigos passam antes **e** depois do patch (`32 | 0`); os 41 novos, `41 | 0`.
>   - Experimento A: `37 | 4` (T55, T62, T64, T66). Experimento B: `38 | 3` (T58, T62, T63).
>   - O seed de demonstração confere com todos os valores da §8 e recusa a segunda execução. FEFO S1–S5 conferem. Concorrência 30/30 nas duas situações.
> - **Os repositórios Java estão escritos, compilados e testados** em `docs/Sprint_3/referencia/backend/` (§7). Eles passam nos testes de integração do backend com PostgreSQL 16 real (Testcontainers).
> - **Entraram US14 e US17** (decisão A2b). As consultas 9–13 (reposição e dashboard) foram executadas no Docker sobre o seed de demonstração (§6 e §8).
> - **O Docker Desktop desta máquina (BD01) tem causa encontrada e contorno verificado** (§2, decisão A26).

## 0. O que você entrega e em que ordem

| Prioridade | Entrega | Quando | Libera quem |
|---|---|---|---|
| **1** | **PR do banco:** patch do DDL e os 41 testes novos | **Ter 27** | Lucas (Fase 2 depende do seed com estoque 0), Kauê |
| **2** | Mensagem ao professor: P3 (§9.1), com o Lucas | **Ter 27** | Toda a equipe |
| **3** | Repositórios de usuário, unidade, ingrediente e produto (§7) | **Ter 27 – Qua 28** | Lucas (Fase 2) |
| 4 | Repositórios de movimentação, pedido, **reposição** e **dashboard** (§7) | **Qua 28** | Lucas (Fases 3 e 6), Kauê (Fase 5) |
| 5 | `seed_demo.sql` no repositório e `sql_repositorios_sprint3.md` (consultas 1–13) | Qua 28 | Todos (demonstração) |
| 6 | Documentos (§9.2) e casos de teste do banco no Plano de Testes | Qui 29 – Sáb 31 | Janela de testes |
| 7 | Registrar o BD01 (causa e contorno, A26) nas evidências | Até Sex 30 | — |

**Pronto quando:**
1. `32 | 0` e `41 | 0`;
2. seed oficial com estoque 0;
3. o `seed_demo.sql` roda e confere (§8, inclusive os números do dashboard);
4. repositórios no PR, com os testes de integração das fases verdes;
5. professor avisado;
6. documentos da §9.2 feitos;
7. PRs aprovados, com o aviso "recriar o banco".

## 1. Por que estas mudanças

**A regra nova do estoque:** cada `ENTRADA` é uma **linha** com `quantidade_restante`. A regra, sempre:

> `ingrediente.quantidade_estoque` = soma de `quantidade_restante` das `ENTRADA` do ingrediente.

| Decisão | Por quê |
|---|---|
| Seed oficial com **estoque 0** (D11/F2) | O seed antigo tinha 40 kg de açaí e **nenhuma** entrada: violaria a regra. Uma entrada exige `usuario_id`, e o seed oficial não tem usuários |
| **CHECKs estritos** (F1) | `NULL IN ('A','B')` não é falso, é desconhecido, e o `CHECK` **deixa passar**. Comprovado: com os CHECKs literais do roteiro, uma saída **sem motivo** entra (T62) |
| `quantidade_restante` por linha | A "simulação" por somas acumuladas erra quando chega uma linha com validade menor (S2, §5) |
| `:hoje` e `:agora` vindos do Java (F3, A9) | Pela documentação da equipe, o Render roda em UTC; o Brasil está 3 h atrás. Afeta a validade **e** a data do pedido (mês do dashboard) |
| Assinaturas combinadas antes (F7) | Evita os conflitos da Sprint 1 no mesmo pacote |

## 2. Preparar o ambiente e o baseline

```powershell
cd C:\Users\diogo\Mente_Fria
docker start mente-fria-db
git checkout main
git pull
git checkout -b feature/banco-sprint3
```

> **Se o `docker start` falhar** com `failed to connect to the docker API … dockerDesktopLinuxEngine`, o Docker Desktop não está pronto. **Nesta máquina, a causa foi encontrada em 26/10 (BD01, decisão A26):**
> - **Sintoma:** a janela do Docker Desktop mostra *"An unexpected error occurred … starting services: … rename …\Docker\run\sailor-ingest.sock … The file cannot be accessed by the system"* (ou o mesmo com `docker-secrets-engine\engine.sock`).
> - **Causa:** arquivos de *socket* que ficaram inacessíveis nas pastas do Docker. Não dá para apagá-los nem renomeá-los; dá para renomear **a pasta**.
> - **Contorno (verificado):**
>   1. **Nunca** clique em "Reset to factory defaults": apaga o `mente-fria-db`. Clique em **Quit**.
>   2. `wsl --shutdown`
>   3. `Rename-Item "$env:LOCALAPPDATA\Docker\run" "run.antiga-$(Get-Date -Format yyyyMMdd-HHmm)"`
>   4. `Rename-Item "$env:LOCALAPPDATA\docker-secrets-engine" "docker-secrets-engine.antiga-$(Get-Date -Format yyyyMMdd-HHmm)"`
>   5. Abra o Docker Desktop. Em 26/10, o motor respondeu em 5 s.
> - **Pode voltar** a cada reinício do Docker. Hipótese, não comprovada: o Windows desta máquina é uma versão Insider (build 26300). Registre isso como a solução do BD01 nas evidências.

**Baseline, antes de mudar qualquer coisa:**

```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste;" -c "CREATE DATABASE mente_fria_teste;"
docker cp bd/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -f /tmp/Script_DDL.sql
docker cp bd/constraints_teste.sql mente-fria-db:/tmp/constraints_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql
```

**Você deve ver:** `32 | 0`, o mesmo resultado da auditoria.

## 3. Aplicar o patch do DDL

```powershell
git apply --check docs/Sprint_3/referencia/bd/Script_DDL_sprint3.patch
git apply docs/Sprint_3/referencia/bd/Script_DDL_sprint3.patch
git diff --stat
```

**Você deve ver:** nenhuma saída no `--check`, e depois `bd/Script_DDL.sql | 50 ++++…`, `1 file changed, 38 insertions(+), 12 deletions(-)`.

- **Se o `--check` falhar:** o `Script_DDL.sql` mudou depois de 08/10. Use `docs/Sprint_3/referencia/bd/Script_DDL.sql` como modelo e aplique à mão. O resultado deve ser idêntico a esse arquivo, ignorando o fim de linha (conferido na auditoria).
- **O que mudou em relação a 14/10:** o comentário da US27 agora diz que cancelar pedido **não** devolve estoque (A6). O texto antigo contradizia a decisão de 14/10.

| Mudança | Tabela | Por quê |
|---|---|---|
| `quantidade_restante DECIMAL(10,3) NULL` + `chk_movimentacao_restante` | `movimentacao_estoque` | `ENTRADA`: obrigatório e entre 0 e `quantidade`. Outros tipos: vazio |
| `motivo VARCHAR(20) NULL` + `chk_movimentacao_motivo` | `movimentacao_estoque` | `SAIDA`: obrigatório (`PEDIDO`, `CONVENCIONAL`, `DESCARTE_VENCIDO`). Outros tipos: vazio |
| `ix_movimentacao_linhas` (índice parcial) | `movimentacao_estoque` | Acelera as consultas 4 e 5 (linhas com saldo) |
| `status`, `data_cancelamento`, `usuario_cancelamento_id` + `chk_pedido_status`, `chk_pedido_cancelamento`, `fk_pedido_usuario_cancelamento` | `pedido` | US27 (fora da Sprint 3), já no schema para não recriar o banco depois |
| Seed: `quantidade_estoque = 0.000` | `ingrediente` | D11 |

### 3.1 A prova do F1 (3 minutos)

```powershell
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "SELECT (NULL::text IN ('PEDIDO','CONVENCIONAL')) IS NULL AS resultado_e_desconhecido;"
```

**Você deve ver:** `t`. Por isso o DDL usa `motivo IS NOT NULL AND motivo IN (...)` e `tipo <> 'SAIDA'`. Assim, cada constraint cuida de uma coisa só, e um tipo inválido é barrado **só** pelo `chk_movimentacao_tipo`.

## 4. Os 41 testes novos (T33–T73)

```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste;" -c "CREATE DATABASE mente_fria_teste;"
docker cp bd/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -f /tmp/Script_DDL.sql
copy docs\Sprint_3\referencia\bd\constraints_sprint3_teste.sql bd\constraints_sprint3_teste.sql
docker cp bd/constraints_sprint3_teste.sql mente-fria-db:/tmp/constraints_sprint3_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql
```

**Você deve ver:** `41 | 0` e, para os antigos sobre o schema novo, `32 | 0`.

| Grupo | Testes | O que prova |
|---|---|---|
| Cliente | T33–T36 | Nome, contato e endereço obrigatórios |
| Pedido | T37–T46 | Cliente opcional; FKs; total ≥ 0; status válido; cancelamento completo ou nenhum |
| Itens | T47–T52 | Quantidade e preço > 0; produto repetido; FKs |
| Entrada (linha) | T53–T58 | Com ou sem validade; `restante` obrigatório, ≤ quantidade, ≥ 0; sem motivo |
| Saída | T59–T65 | Os 3 motivos; sem motivo e motivo inválido barrados; restante e validade proibidos |
| Gerais | T66–T73 | Tipo inválido; quantidade 0 (CT26); FKs; baixa além do saldo (CT21); unidade |

### 4.1 Quebrar de propósito (evidência do critério nº 4 do Plano de Testes)

**Experimento A** (os CHECKs literais do roteiro):

```sql
ALTER TABLE movimentacao_estoque DROP CONSTRAINT chk_movimentacao_restante, DROP CONSTRAINT chk_movimentacao_motivo;
ALTER TABLE movimentacao_estoque ADD CONSTRAINT chk_movimentacao_restante CHECK (quantidade_restante IS NULL OR quantidade_restante BETWEEN 0 AND quantidade);
ALTER TABLE movimentacao_estoque ADD CONSTRAINT chk_movimentacao_motivo CHECK ((tipo = 'ENTRADA' AND motivo IS NULL) OR (tipo = 'SAIDA' AND motivo IN ('PEDIDO','CONVENCIONAL','DESCARTE_VENCIDO')));
```

**Você deve ver** `37 | 4` (resultado de 26/10):
- T55: entrada sem restante, **aceita**;
- T62: **saída sem motivo, aceita**;
- T64: saída com restante, aceita;
- T66: tipo inválido barrado pela constraint errada.

**Experimento B:** `ALTER TABLE movimentacao_estoque DROP CONSTRAINT chk_movimentacao_motivo;` → `38 | 3` (falham T58, T62 e T63).

Depois de cada experimento, **recrie** o banco de teste. Guarde os prints em `docs/Sprint_3/evidencias/`.

## 5. Estoque por linhas: o algoritmo e a prova

**Regras (roteiro v4.6, §5 Fase 3, e decisão D10):**

1. **Entrada:** nova linha com `quantidade_restante = quantidade`, que soma ao saldo.
2. **Pedido ou saída convencional:** consome só linhas **válidas** (sem validade, ou validade ≥ `hoje`), da menor validade para a maior. Linhas sem validade vão por último; empate desempata por `id`.
3. **Descarte de vencidos:** consome só linhas **vencidas**, da mais antiga para a mais nova.
4. **Saldos:** o total é a soma de **todas** as linhas; o disponível é a soma das **válidas**.

Para rodar a referência (Python e `psycopg2`, contra o banco de teste):

```powershell
pip install psycopg2-binary
$env:MF_DSN = "host=localhost port=5432 dbname=mente_fria_teste user=postgres password=SUA_SENHA_LOCAL"
python docs\Sprint_3\referencia\apoio\fefo_referencia.py
python docs\Sprint_3\referencia\apoio\concorrencia_referencia.py
```

**Você deve ver** (resultado de 26/10):

| Cenário | Resultado |
|---|---|
| S1 | Total **35**, válido **15**, vencido **20**; após o descarte de 20: total **15** |
| S2 (contraexemplo) | Antes: **C=20, A=10**; depois da saída de 5: **C=15, A=10** |
| S3 | Após 6: `0 / 1 / 10`; após 5: `0 / 0 / 6` |
| S4/S5 | Pedido de 1 → recusado; descarte de 9 → recusado; descarte de 8 → ok |
| Final | `violações = []`; saídas por motivo: `CONVENCIONAL 1`, `DESCARTE_VENCIDO 2`, `PEDIDO 4` |
| Concorrência | `30 x (('estoque insuficiente (422)', 'ok'), '20.000', False)` e `30 x (('ok', 'ok'), ('80.000', '80.000'), False)` |

> **Atenção:** os dois scripts fazem `TRUNCATE` das tabelas. Rode **só** no `mente_fria_teste`, nunca no `mente_fria`.

## 6. Os SQLs da Sprint 3 (`docs/Apoio/sql_repositorios_sprint3.md`)

- Crie no formato do `sql_repositorios.md`: convenções; tabela rota ↔ método; cada método com SQL, retorno e falhas.
- A fonte é `docs/Sprint_3/referencia/bd/consultas_sprint3.sql` (executado em 26/10).
- **Duas mudanças em relação a 14/10:**
  - **A9:** os `INSERT` de `movimentacao_estoque` (6 e 7) e o de `pedido` recebem **`:agora`** do Java na coluna de data (`data_movimentacao`, `data_pedido`). O `DEFAULT CURRENT_TIMESTAMP` fica só como rede de segurança.
  - Na consulta 3, o campo `quantidade_estoque` devolvido é o **saldo válido** (o nome segue o Contrato Java↔Python). Escreva isso no documento, porque na tabela `ingrediente` o mesmo nome significa saldo **total**.

| # | Consulta | Usada por |
|---|---|---|
| 1 | `GET /api/estoque`: total, válido, vencido, mínimo, validade mais próxima | Telas 10/16 |
| 2 | Linhas de um ingrediente com `situacao` | `GET /api/estoque/{id}/linhas` (A12) |
| 3 | Entrada do Python: ingredientes dos produtos ativos, saldo **válido** e custo | Fase 4A |
| 4 e 5 | Linhas para baixar (válidas) e para descartar (vencidas), com `FOR UPDATE` | Pedido, saída, descarte |
| 6 | `UPDATE` da linha e do saldo + `INSERT` da `SAIDA` (com `:agora`), na mesma transação | Idem |
| 7 | `INSERT` da `ENTRADA` (com `:agora`) + soma ao saldo | Entrada |
| 8 | Invariante (**0 linhas** = tudo certo) | Testes |
| novo | `GET /api/pedidos`: pedidos com itens, do mais recente para o mais antigo | CT40 |
| **9** | US14: os 8 (até 30 dias) ou 15 (acima) ingredientes mais consumidos no período, com custo e mínimo. **Pedidos cancelados contam** (A18) | `POST /api/reposicao/recomendacao` |
| **10** | US17: faturamento do mês (só `ATIVO`) | `GET /api/dashboard` |
| **11** | US17: produtos mais vendidos no mês | idem |
| **12** | US17: ingredientes mais utilizados, ordenados pelo **custo** (A17) | idem |
| **13** | US17: ranking por usuário com os filtros opcionais (usuário, produto, valor mín./máx. no `HAVING`) | idem |

**Consumo (F12, resolvida pela A18):** a mesma fórmula nas consultas 9 e 12 (pedidos × composição). A diferença é só o filtro de status: a reposição conta todos; o dashboard, só `ATIVO`. Escreva isso no `sql_repositorios_sprint3.md`.

**Período:** o Java manda `:inicio` e `:fim` (intervalo `[inicio, fim)`), já no fuso de São Paulo (A9). Reposição: os últimos N dias contando hoje. Dashboard: do dia 1 do mês ao dia 1 do mês seguinte.

**Parâmetros opcionais da consulta 13:** o PostgreSQL precisa saber o tipo de um `NULL`. Por isso o SQL tem `CAST(:usuario AS INTEGER) IS NULL`, e o Java manda `addValue("usuario", null, Types.INTEGER)`. Sem o tipo, o PostgreSQL pode recusar o comando por não saber o tipo do parâmetro (não testado sem o `CAST`; com ele, testado).

**Revisão de SQL (skill `sql-code-review`, 26/10):**
- Todos os comandos usam parâmetros nomeados; não há concatenação.
- O `FOR UPDATE` segue a ordem de bloqueio por `id` (sem *deadlock*, provado).
- O índice parcial cobre as consultas 4 e 5.
- As FKs `pedido_produto.produto_id` e `pedido.usuario_id` não têm índice. Para o volume de uma açaiteria é irrelevante; fica só registrado.

## 7. Repositórios Java (F7) — prontos e testados em `referencia/`

**Acordo (F7):** você é o dono dos `*Repository` e dos `record`s de linha; o Lucas, dos serviços e controllers. Os repositórios estão **escritos, compilados e testados** em `docs/Sprint_3/referencia/backend/src/main/java/com/mentefria/backend/`. Seu trabalho é **copiar, ler, conferir o SQL com o `consultas_sprint3.sql` e abrir o PR** (ou entregar ao Lucas para irem no PR da fase, combinando na terça).

Regras que o código segue (revisão com a skill de JDBC): `NamedParameterJdbcTemplate` (D4), `BigDecimal`, parâmetros nomeados (nada concatenado com dado do usuário), **sem regra de negócio**, **sem JPA**; `Optional` + `query(...).stream().findFirst()` quando pode não haver linha; `RETURNING` com `queryForObject` quando sempre há; `:hoje` e `:agora` vindos do Java.

| Repositório (arquivo) | Métodos | Consultas | Fase | Teste que prova |
|---|---|---|---|---|
| `usuario/UsuarioRepository` | `travarParaCadastroDeAdministrador`, `existeAdministrador`, `inserir`, `buscarPorEmail` (`Optional`), `listarFuncionarios` | `sql_repositorios.md` | 2 | `Fase2CrudTest` |
| `unidade/UnidadeMedidaRepository` | `listar` | idem | 2 | idem |
| `ingrediente/IngredienteRepository` | `listar`, `buscarPorId`, `inserir` e `atualizar` (com `RETURNING`), `buscarNomesPorIds`, `travar` (ordem de id), `somarSaldo`, `baixarSaldo`, `listarEstoque` (1), `entradaOtimizador` (3), `saldosValidos` | 1, 3 | 2, 3, 4A, 5 | `Fase2CrudTest`, `Fase3…`, `Fase4…` |
| `produto/ProdutoRepository` | `listarComComposicao`, `inserir`, `inserirComposicao` (lote), `atualizarPreco`, `buscarPorIds`, `composicoes`, `listarAtivos` | `sql_repositorios.md` | 2 | `Fase2CrudTest` |
| `estoque/MovimentacaoRepository` | `inserirEntrada` (7), `linhasValidas` (4), `linhasVencidas` (5), `consumirLinha` e `inserirSaida` (6), `linhasDoIngrediente` (2) | 2, 4–7 | 3 | `Fase3EstoquePedidoTest` |
| `pedido/PedidoRepository` | `inserir` (com `:agora`), `inserirItens` (lote), `listar` | novo | 3 | idem |
| **`reposicao/ReposicaoRepository`** | `maisConsumidos(inicio, fim, limite)` | **9** | 5 (Kauê) | `ReposicaoTest` |
| **`dashboard/DashboardRepository`** | `faturamento`, `maisVendidos`, `ingredientesMaisUtilizados`, `ranking` (filtros opcionais com nulos tipados) | **10–13** | 6 (Lucas) | `DashboardTest` |

**Mudanças em relação às assinaturas de 14/10 (já combinadas no código):**
- `IngredienteRepository.inserir` e `atualizar` devolvem o `Ingrediente` completo (`RETURNING`), porque o `POST` responde com o registro inteiro;
- `PedidoRepository.inserir` devolve só o `id`: a data é o próprio `:agora` que o serviço já tem;
- `UsuarioRepository` ganhou `travarParaCadastroDeAdministrador` (dois cadastros de Administrador ao mesmo tempo não passam juntos pela checagem).

**Para rodar os testes dos seus repositórios:** com o Docker ligado, dentro de `backend/`, `.\mvnw test "-Dtest=Fase2CrudTest"` (e as outras classes conforme as fases chegam). Os testes recriam o schema a partir do **`bd/Script_DDL.sql`**: o seu PR do DDL é o que eles usam.

## 8. Seed de demonstração (`bd/seed_demo.sql`)

Copie `docs\Sprint_3\referencia\bd\seed_demo.sql` para `bd\seed_demo.sql`. Para **estudar** num banco à parte (nunca no de desenvolvimento do Lucas):

```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_demo;" -c "CREATE DATABASE mente_fria_demo;"
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -q -f /tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -c "INSERT INTO usuario (nome,email,senha_hash,perfil) VALUES ('Dono','dono@exemplo.com','hash_falso','ADMINISTRADOR'),('Func','func@exemplo.com','hash_falso','FUNCIONARIO');"
docker cp bd/seed_demo.sql mente-fria-db:/tmp/seed_demo.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -v ON_ERROR_STOP=1 -f /tmp/seed_demo.sql
```

- O `hash_falso` serve só para estudo e **não permite login**. No uso real, os usuários nascem pelas telas (US1/US2).
- O script **recusa** rodar sem Administrador, sem Funcionário, ou em banco que já tem pedidos ou movimentações (conferido).
- As validades são relativas ao dia em que o seed roda. O script usa `CURRENT_DATE` do banco: aceitável para demonstração local.

**Você deve ver** (conferido em 26/10):

| Ingrediente | Total | Válido | Vencido |
|---|---|---|---|
| Açaí (polpa) | 45,750 | 45,750 | 0 |
| Leite condensado | 14,220 | 14,220 | 0 |
| **Granola** | 11,160 | **5,160** | **6,000** |
| Banana | 11,050 | 11,050 | 0 (vence em 3 dias) |
| **Morango** | **1,980** | 1,980 | 0 (abaixo do mínimo de 2,000) |
| Leite em pó | 4,760 | 4,760 | 0 |
| Copo descartável | 269,000 | 269,000 | 0 (sem validade) |

- Pedidos: mês anterior **271,50**; mês atual **326,40**. Somando os dois meses: Dono **346,20** (3 pedidos); Func **251,70** (3 pedidos). Invariante: 0 violações.
- **A recomendação para este estoque é 151/1/0** (receita 1.966,80; lucro 1.011,71), confirmada por força bruta, pelo Java com o Python real e dentro do container de deploy.

**Dashboard (consultas 10–13) no mês atual, com o seed** (executado no Docker em 26/10; o dashboard filtra por **um** mês):

| Bloco | Você deve ver |
|---|---|
| Faturamento | **326,40** em 3 pedidos (mês anterior: 271,50 em 3) |
| Mais vendidos | Premium 7 (174,30); Especial 6 (113,40); Tradicional 3 (38,70) |
| Ingredientes (pelo custo) | Açaí 7,800 kg (144,30); granola 0,520 (11,44); morango 0,420 (6,30); copo 16 (5,60); leite condensado 0,370 (4,44); leite em pó 0,140 (3,92); banana 0,300 (1,65) |
| Ranking | 1º Func 213,00 (2 pedidos: Premium e Tradicional); 2º Dono 113,40 (1 pedido: Especial) |
| Situação do estoque | 1 no mínimo ou abaixo (morango); 1 com vencido (granola) |

**Reposição (consulta 9) com 60 dias:** copo 31; açaí 14,250; granola 0,840; leite condensado 0,780; morango 0,720; banana 0,450; leite em pó 0,240. Com orçamento de R$ 100, só o **morango** precisa de compra: **0,74 kg (R$ 11,10)**. É o exemplo `optimization/exemplos/entrada_reposicao_demo.json` do Kauê, conferido nos dois sentidos (SQL e módulo).

> **Cuidado com a data:** o seed põe os pedidos do mês atual nas primeiras horas do dia 1, pelo relógio **do banco**. Se você rodar o seed num dia 1 à noite (ou no último dia do mês depois das 21h), a conta do mês pode mudar. Para demonstração, rode em outro horário.

## 9. Documentos

### 9.1 Antes de editar: avisar o professor (F9, P3) — terça 27

**A mensagem está pronta para copiar e colar em `sprint-3-mensagem-professor.md`** (27/10). Ela cobre os pontos abaixo, faz três perguntas (adendo ao Termo, licença de avaliação no servidor, data da avaliação por causa da expiração do banco) e uma extra (sprint curta se sobrar tempo, A28). Revise com o Lucas e envie.

O Termo foi aprovado em 28/08. **Todos os Must do Termo estão na Sprint 3** (A2b), então não há pedido de corte de escopo. Os pontos da mensagem:

1. **Receita → lucro:** o sistema maximiza o lucro (preço − custo dos ingredientes), porque a dor (a) do Documento de Visão é "lucratividade". A receita estimada continua aparecendo.
2. **Reposição com orçamento (US14):** o dono informa o período e o orçamento; o sistema sugere quanto comprar de cada ingrediente sem passar do orçamento.
3. **Dashboard (US17):** mostra faturamento do mês, mais vendidos, ingredientes mais usados, ranking por usuário e, como o Termo pede, a **receita máxima estimada** e os produtos prioritários do último cálculo (A16).
4. **Publicação (US20):** Render (plano gratuito) e GitHub Pages. Duas perguntas:
   - o banco gratuito expira 30 dias depois de criado (A21): vamos criá-lo em 31/10 (vale até 30/11). Até quando o professor vai precisar do sistema publicado?
   - o Gurobi roda com a licença de avaliação que vem com o `pip`, porque não temos a WLS (A22): o uso é aceitável? **Resposta: só com confirmação expressa da Gurobi (A30).**

**Resposta recebida em 27/10** (`resposta-professor.md`). O professor aceitou o adendo datado e pediu as alterações, as justificativas e os critérios de aceitação; o rascunho está em **`sprint-3-adendo-termo-de-aceite.md`** (A33). Revise com o Lucas, envie e **só depois da aprovação** edite a Visão e o Backlog (§9.2). O Termo original não muda: o adendo é anexado a ele.

### 9.2 O que fazer (verificado em 26/10: os `.docx` são idênticos aos da `main`, então nada foi feito ainda)

| # | Documento | O que fazer (e por quê) |
|---|---|---|
| 1 | **Backlog** (US6, US10, US11, US17), **Visão** e **Termo** | Receita → **lucro** (depois da §9.1). US10: "maximizar o **lucro** com o estoque **válido**", e o critério ganha "custo dos ingredientes". US11: exibir quantidade, **lucro potencial** e receita estimada. US6: "cálculos de receita" → "cálculos". US17: **manter** "receita máxima estimada" (A16) e acrescentar o ranking e os filtros decididos em 14/10 |
| 2 | **Backlog** (US9) | "Saída de produtos" → "**baixa de ingredientes**" (F10) |
| 3 | **Backlog** (US14, US27, Sprint alvo) | US14 com período e orçamento; nova **US27 — cancelar pedido** (Could; **não devolve estoque**); coluna "Sprint alvo": **US14, US17, US18 e US20 = Sprint 3** (A2b); US13, US15, US16, US19, US26 e US27 = fora |
| 4 | **`docs/DER.md`** | Dicionário e diagrama: `movimentacao_estoque.quantidade_restante` e `motivo`; `pedido.status`, `data_cancelamento` e `usuario_cancelamento_id`. Reescrever as notas de FEFO (**por linha**) e de saldo ("= soma dos restantes"). Anotar o seed com estoque 0, o `seed_demo.sql` e o `:agora` (A9) |
| 5 | **`docs/Apoio/mapa_constraints.md`** | Acrescentar `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque` (tabela abaixo). Corrigir a referência `sql_repositorios_sprint1.md` → `sql_repositorios.md` |
| 6 | **`docs/Plano_de_Testes_Mente_Fria.md`** | Ajustar CT23, CT24 e CT29 e incorporar **CT30–CT71** de `sprint-3-casos-de-teste-propostos.md` (banco, estoque e dashboard são seus; Python, reposição e deploy, do Kauê). **Sem** meta de cobertura |
| 7 | **`docs/Apoio/Arquitetura.md`** | D5 (a reposição com orçamento usa o solver, módulo `reposicao.py`); estoque por linhas; `@Profile("dev")`; `America/Sao_Paulo` (F3, A9); **sem Compute Server; licenças: WLS acadêmica do Kauê (pessoal, até 25/09/2027) e a restrita do `pip` no desenvolvimento; no Render, só com autorização da Gurobi** (A11, A30, A34); trocar "licença do Lucas" e "90 dias" por "licença do Kauê" e "um ano"; versão do Gurobi 13.0.3 (P2, decidida em 27/10); **deploy** como está em `referencia/deploy/` (Docker no Render, Pages por Actions, limites da A21) |
| 8 | **Referências antigas em documentos vivos** | `sql_repositorios_sprint1.md` → `docs/Apoio/sql_repositorios.md`; `constraints_sprint1_teste.sql` → `bd/constraints_teste.sql`; `docs/Script_DDL.sql` → `bd/Script_DDL.sql`; `ARQUITETURA.md` → `docs/Apoio/Arquitetura.md`. Onde: Plano de Testes, `mapa_constraints.md` e o cabeçalho de `bd/constraints_teste.sql` (o Contrato já foi corrigido na v2, em 27/10). **Não** reescreva os relatórios de sprint: são histórico |
| 9 | **`docs/Diagramas_UML.md`** | `calcularQuantidadeOtimaProducao()` passa a maximizar **lucro** |
| 10 | **README** | Linha 3 (receita → lucro, depois da §9.1); comandos dos testes novos (§4); seed de demonstração (§8); **linha 5: a URL publicada** (o Kauê informa) |
| 11 | **Evidências da Sprint 2 (BD01)** | Registrar a causa e o contorno da A26 (§2). É fato novo, não reescrita do histórico |

**Linhas novas do `mapa_constraints.md`** (`NOT NULL` por **tabela + coluna**, porque o PostgreSQL informa as duas: verificado):

| Tabela | Constraint | Campo na API | Mensagem | HTTP |
|---|---|---|---|---|
| `cliente` | NOT NULL `nome`, `contato`, `endereco` | idem | Informe o nome / o contato / o endereço. | 400 |
| `pedido` | `fk_pedido_cliente` | `cliente_id` | Cliente não encontrado. | 400 |
| `pedido` | `chk_pedido_valor_total`, `chk_pedido_status`, `chk_pedido_cancelamento`, `fk_pedido_usuario*` | — | (erro interno: quem preenche é o backend) | 500 |
| `pedido_produto` | `chk_pedido_produto_quantidade` / `pedido_produto_pkey` / `fk_pedido_produto_produto` | `itens` | A quantidade deve ser maior que zero. / Este produto já foi adicionado ao pedido. / Produto não encontrado. | 400 |
| `movimentacao_estoque` | `chk_movimentacao_tipo` | `tipo` | Tipo de movimentação inválido. | 400 |
| | `chk_movimentacao_quantidade` | `quantidade` | A quantidade deve ser maior que zero. | 400 |
| | `chk_movimentacao_data_validade` | `data_validade` | A validade só pode ser informada em entradas. | 400 |
| | `chk_movimentacao_motivo` | `motivo` | Informe o motivo da saída. | 400 |
| | `chk_movimentacao_restante`, `fk_movimentacao_usuario` | — | (erro interno) | 500 |
| | `fk_movimentacao_ingrediente` | `ingrediente_id` | Ingrediente não encontrado. | 400 |
| `ingrediente` | `chk_ingrediente_quantidade_estoque` (na baixa) | — | Estoque insuficiente. | **422** |

## 10. PRs e fechamento

```powershell
git add bd/Script_DDL.sql bd/constraints_sprint3_teste.sql
git commit -m "feat: linhas de estoque, motivo e status do pedido; seed oficial com estoque 0"
git push -u origin feature/banco-sprint3
```

No PR, inclua:
- **"ATENÇÃO — recriar o banco"** (todos rodam o "Recomeçar do zero" do README);
- os resultados `32 | 0` e `41 | 0`;
- os experimentos A e B;
- a tabela de decisões (D11, F1, A6).

Peça revisão ao Lucas e ao Kauê. **Antes**, apague qualquer cópia do `.patch` da raiz. Os outros PRs (`seed_demo`, SQL da Sprint 3, documentos) saem separados: um assunto por PR.

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `failed to connect to the docker API` | Docker Desktop sem o motor | §2 (BD01, A26: renomear as duas pastas; **nunca** "Reset to factory defaults") |
| `git apply` falha | `Script_DDL.sql` mudou depois de 08/10 | Usar `referencia/bd/Script_DDL.sql` como modelo |
| `relation "usuario" already exists` | Script rodado duas vezes | Recriar o banco |
| `violates check constraint "chk_movimentacao_restante"` | Entrada sem `quantidade_restante` | Toda `ENTRADA` grava `restante = quantidade` |
| `violates check constraint "chk_movimentacao_motivo"` | Saída sem motivo | `PEDIDO`, `CONVENCIONAL` ou `DESCARTE_VENCIDO` |
| O seed de demonstração recusa | Falta Administrador ou Funcionário, ou o banco não é novo | §8 |
| `psycopg2` não conecta | `MF_DSN` errado | Conferir host, porta, banco e senha |
| Datas "diferentes" no dia seguinte | São relativas ao dia do seed | Conte os dias (−2, +3), não as datas |

## Checklist final

- [ ] Docker funcionando (A26, se preciso) e BD01 registrado nas evidências
- [ ] Baseline `32 | 0` antes e depois do patch
- [ ] Patch aplicado; seed oficial com estoque 0; comentário da US27 correto
- [ ] `41 | 0`; experimentos A (`37 | 4`) e B (`38 | 3`) registrados
- [ ] Mensagem ao professor (F9, P3: lucro, reposição, dashboard, deploy e licença) enviada e resposta guardada
- [ ] Repositórios de `referencia/` copiados e lidos; testes de integração das fases verdes
- [ ] `sql_repositorios_sprint3.md` com as consultas 1–13; `seed_demo.sql` no `bd/`; números do dashboard conferidos (§8)
- [ ] Documentos da §9.2; CT30–CT71 no Plano de Testes até 31/10
- [ ] PRs com "recriar o banco"

## Fora da Sprint 3 (decisão A2b)

Cancelamento (US27, já no schema), clientes (US13, constraints prontas), alertas (US15/US16; as consultas 1 e 2 servem de base), CSV (US19) e porção padrão (US26).
