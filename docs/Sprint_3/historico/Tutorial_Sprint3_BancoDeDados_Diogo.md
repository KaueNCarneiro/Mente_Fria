# Tutorial — Sprint 3 (Banco de Dados e Documentos) do zero ao PR

**Para:** Diogo Pereira Miranda · **Papel:** dados (DBA / engenharia de dados) e qualidade da documentação
**Base:** `sprint-3-roteiro.md` v4.6 (seções 5 e 10.1) + `Analise_Roteiro_Sprint3_v4.6.md` + `Registro_de_Decisoes_e_Pendencias_Sprint3.md` · **Atualizado em:** 14/10/2026

> **O que foi verificado e o que não foi.** Tudo o que é SQL foi **executado no PostgreSQL 16.15** sobre o **`bd/Script_DDL.sql` real da `main`** (commit `1ddb5ae`): o patch aplica com `git apply --check`; o script antigo `bd/constraints_teste.sql` continua passando (**32 de 32**) com o schema novo; os testes novos passam (**41 de 41**); o seed de demonstração roda e a regra de estoque fecha; a baixa por linhas foi testada em 6 cenários e, **em concorrência**, em 30 rodadas (um dos dois pedidos passa e o saldo fica certo, sem deadlock). **Não foi verificado:** Docker/Windows (os comandos seguem o seu tutorial da Sprint 1) e o código Java (não compilado). Itens que dependem de decisão do PO estão marcados **[PROVISÓRIO]**.

## 0. O que você entrega e em que ordem

| Prioridade | Entrega | Prazo | Libera quem |
|---|---|---|---|
| **1** | **Aplicar o patch do DDL** (linhas de estoque, `motivo`, status do pedido, seed com estoque 0) e os **41 testes novos** | **Hoje, 14/10** | Lucas (US9/US12), Kauê (dados) |
| **2** | **Assinaturas dos repositórios Java** acordadas com o Lucas (tabela da Parte 7) | Hoje | Lucas |
| 3 | **SQLs da Sprint 3** documentados (`sql_repositorios_sprint3.md`) | 15/10 | Lucas |
| 4 | **`seed_demo.sql`** | 15/10 | Kauê, Leonardo, demonstração |
| 5 | **Documentos que faltam** (Parte 8; **avisar o professor antes**) | 15 a 16/10 | Toda a equipe |
| 6 | PR do banco e dos documentos | 16/10 | — |

**Se faltar tempo:** faça 1 e 2 hoje. Sem elas, o Lucas e o Kauê ficam parados.

**Pronto quando…** (1) os 32 testes antigos e os 41 novos passam; (2) o seed oficial tem estoque 0 e a regra de estoque fecha; (3) o seed de demonstração roda; (4) as assinaturas dos repositórios estão combinadas por escrito; (5) a lista da Parte 8 foi feita; (6) o PR está aprovado e a equipe foi avisada de **"recriar o banco"**.

## 1. Por que estas mudanças (a base de tudo)

**A regra nova do estoque.** Hoje `ingrediente.quantidade_estoque` é só um número. Para saber **quanto está vencido** e **qual lote consumir primeiro**, cada `ENTRADA` passa a ser uma **linha** de estoque, com `quantidade_restante` (o que ainda não foi consumido). A regra que o banco passa a respeitar, sempre:

> `quantidade_estoque` = soma de `quantidade_restante` de todas as `ENTRADA` do ingrediente.

| Decisão | Por quê |
|---|---|
| **Seed oficial com estoque 0 (D11/F2)** | O seed antigo tem 40 kg de açaí e **nenhuma** entrada: violaria a regra desde o primeiro dia. Uma entrada exige `usuario_id`, e o seed oficial não tem usuários (a conta nasce pela tela, US1). O estoque de demonstração vai para o `seed_demo.sql` |
| **`CHECK`s estritos (F1)** | Em SQL, `NULL IN ('A','B')` não é "falso": é "desconhecido", e o `CHECK` **deixa passar**. Um `CHECK` escrito ao pé da letra do roteiro aceitaria uma saída **sem motivo**. Veja a prova na Parte 3 |
| **Linhas por `quantidade_restante` e baixa pela menor validade** | A simulação "distribuir as saídas sobre as entradas" **erra** quando entra uma linha com validade menor depois (Parte 5 mostra o caso). Guardar o restante por linha é a única forma exata |
| **Assinaturas combinadas antes (F7)** | Dois integrantes no mesmo pacote Java em 2 dias é o que gerou os conflitos da Sprint 1. Combinar antes elimina o problema |
| **`hoje` como parâmetro (F3)** | O servidor roda em UTC; o Brasil está 3 horas atrás. Todo SQL que compara validade recebe `:hoje` do Java (fuso `America/Sao_Paulo`), nunca `CURRENT_DATE` |

## 2. Preparar o ambiente

```powershell
cd C:\Users\diogo\Mente_Fria
docker start mente-fria-db
git checkout main
git pull
git checkout -b feature/banco-sprint3
dir bd
```
**Você deve ver:** `Script_DDL.sql` e `constraints_teste.sql` em `bd/`.

**Regressão primeiro** (prove que o que existe passa **antes** de mudar):
```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste;" -c "CREATE DATABASE mente_fria_teste;"
docker cp bd/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -f /tmp/Script_DDL.sql
docker cp bd/constraints_teste.sql mente-fria-db:/tmp/constraints_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql
```
**Você deve ver:** `32 | 0`.

## 3. Aplicar o patch do DDL e entender cada mudança

Copie `referencia_bd/Script_DDL_sprint3.patch` para a raiz do repositório e rode:
```powershell
git apply --check Script_DDL_sprint3.patch
git apply Script_DDL_sprint3.patch
git diff --stat
```
**Você deve ver:** `bd/Script_DDL.sql | 50 ++++...` e `1 file changed, 38 insertions(+), 12 deletions(-)` (entre as trocas, os 7 valores de estoque do seed viram `0.000`). Se `--check` falhar, alguém mudou o `Script_DDL.sql` depois do commit `1ddb5ae`: use `referencia_bd/bd/Script_DDL.sql` como modelo e aplique à mão. **Apague o `.patch` da raiz antes de comitar.**

| Mudança | Tabela | Por quê |
|---|---|---|
| `quantidade_restante DECIMAL(10,3) NULL` | `movimentacao_estoque` | O que resta de cada `ENTRADA` |
| `motivo VARCHAR(20) NULL` | `movimentacao_estoque` | Por que saiu: `PEDIDO`, `CONVENCIONAL` ou `DESCARTE_VENCIDO` (só `SAIDA`) |
| `chk_movimentacao_restante` | `movimentacao_estoque` | `ENTRADA`: restante obrigatório e entre 0 e a quantidade. Outros tipos: restante vazio |
| `chk_movimentacao_motivo` | `movimentacao_estoque` | `SAIDA`: motivo obrigatório e válido. Outros tipos: motivo vazio |
| `ix_movimentacao_linhas` | `movimentacao_estoque` | Índice parcial para a baixa por linhas (só linhas com saldo) |
| `status`, `data_cancelamento`, `usuario_cancelamento_id` | `pedido` | US27 (cancelar sem apagar). Já entra no banco para não recriar tudo na Sprint 4 |
| `chk_pedido_status`, `chk_pedido_cancelamento`, `fk_pedido_usuario_cancelamento` | `pedido` | Status válido; cancelado exige data e usuário; ativo não pode ter os dois |
| Seed: `quantidade_estoque = 0.000` | `ingrediente` | D11 |

### 3.1 A prova do F1 (faça, são 3 minutos)
```powershell
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "SELECT (NULL::text IN ('PEDIDO','CONVENCIONAL')) IS NULL AS resultado_e_desconhecido;"
```
**Você deve ver:** `t`. Ou seja, `NULL IN (...)` não é falso: é desconhecido, e um `CHECK` aceita o que é desconhecido. Por isso o DDL traz `motivo IS NOT NULL AND motivo IN (...)` e usa `tipo <> 'SAIDA'` (em vez de `tipo = 'ENTRADA'`), de modo que **cada constraint cuida de uma coisa só**: um tipo inválido (`AJUSTE`) é barrado **somente** por `chk_movimentacao_tipo`, e a mensagem para o usuário sai certa.

## 4. Os 41 testes novos (T33 a T73)

```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste;" -c "CREATE DATABASE mente_fria_teste;"
docker cp bd/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -f /tmp/Script_DDL.sql
docker cp referencia_bd/bd/constraints_sprint3_teste.sql mente-fria-db:/tmp/constraints_sprint3_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql
```
**Você deve ver:** 41 linhas `T33` a `T73`, todas `OK`, e `41 | 0`. Copie o arquivo para `bd/constraints_sprint3_teste.sql`. Rode também o antigo (`bd/constraints_teste.sql`) sobre o schema novo: **`32 | 0`** (a Parte 2 já fez o baseline; repita para provar que o patch não quebrou nada).

| Grupo | Testes | O que prova |
|---|---|---|
| Cliente | T33–T36 | `nome`, `contato` e `endereco` obrigatórios |
| Pedido | T37–T46 | Cliente opcional; usuário e cliente existem; total ≥ 0; **status** válido; **cancelamento** completo ou nenhum |
| Itens do pedido | T47–T52 | Quantidade e preço > 0; produto repetido; produto e pedido existem |
| Entrada (linha) | T53–T58 | Com ou sem validade; **`restante` obrigatório**, ≤ quantidade, ≥ 0; entrada **sem** motivo |
| Saída | T59–T65 | Os 3 motivos; **sem motivo** e motivo inválido barrados; restante e validade proibidos |
| Gerais | T66–T73 | Tipo inválido; quantidade 0 (CT26); FKs; baixa além do saldo (CT21); unidade de medida |

### 4.1 Quebrar de propósito (obrigatório: é a evidência do critério nº 4 do Plano de Testes)
**Experimento A (o F1 em ação):** troque as duas constraints pelas versões **literais do roteiro** e rode os testes novos:
```sql
ALTER TABLE movimentacao_estoque DROP CONSTRAINT chk_movimentacao_restante, DROP CONSTRAINT chk_movimentacao_motivo;
ALTER TABLE movimentacao_estoque ADD CONSTRAINT chk_movimentacao_restante CHECK (quantidade_restante IS NULL OR quantidade_restante BETWEEN 0 AND quantidade);
ALTER TABLE movimentacao_estoque ADD CONSTRAINT chk_movimentacao_motivo CHECK ((tipo = 'ENTRADA' AND motivo IS NULL) OR (tipo = 'SAIDA' AND motivo IN ('PEDIDO','CONVENCIONAL','DESCARTE_VENCIDO')));
```
**Você deve ver 4 falhas:** `T55` (entrada sem restante **aceita**), `T62` (**saída sem motivo aceita**), `T64` (saída com restante **aceita**) e `T66` (tipo inválido barrado pela constraint **errada**, `chk_movimentacao_motivo`). Resultado `37 | 4`.
**Experimento B:** `ALTER TABLE movimentacao_estoque DROP CONSTRAINT chk_movimentacao_motivo;` → falham `T58`, `T62`, `T63`. Depois de cada experimento, **recrie** o banco de teste. Guarde os prints.

## 5. Estoque por linhas: o algoritmo e a prova

**Regras (confirmadas no roteiro):**
1. Entrada → nova linha com `quantidade_restante = quantidade`; soma ao saldo.
2. Saída por **pedido** ou **convencional** → consome só linhas **válidas** (sem validade, ou validade ≥ `hoje`), da **menor validade** para a maior; linhas **sem validade por último**; empate pelo `id`.
3. **Descarte de vencidos** → consome só linhas **vencidas** (validade < `hoje`), da mais antiga para a mais nova.
4. Saldo total = soma de **todas** as linhas (inclui o vencido, até alguém descartar). **Disponível** = soma das linhas **válidas**.

**Por que não "simular" com somas acumuladas:** com a linha A (vence 20/10) já consumida em 10, entra a linha C (vence 15/10). A realidade é A=10 e C=20; a simulação por acumulado "consumiria" 10 da C. A próxima saída de 5 deve sair da **C** (menor validade). Só o `quantidade_restante` real acerta.

Rodar a referência (precisa do `psycopg2`; o banco de teste fica no Docker):
```powershell
pip install psycopg2-binary
$env:MF_DSN = "host=localhost port=5432 dbname=mente_fria_teste user=postgres password=SUA_SENHA_LOCAL"
python referencia_bd\fefo_referencia.py
```
**Você deve ver:**

| Cenário | Resultado esperado |
|---|---|
| S1 (exemplo 10.1 do roteiro): linha vencida de 20 e linha válida de 20; pedido de 5 | total **35**, válido **15**, vencido **20**; depois do descarte de 20: total **15** |
| S2 (contraexemplo): A=20 (vence 20/10) sai 10; entra C=20 (vence 15/10); sai 5 | antes da saída: **C=20, A=10**; depois: **C=15, A=10** |
| S3: linhas 3 (14/10), 4 (20/10) e 10 (sem validade); saída de 6 e depois de 5 | após 6: `0 / 1 / 10`; após 5: `0 / 0 / 6` (**sem validade por último**) |
| S4/S5: só há linha vencida (8) | pedido de 1 → **recusado** (nada muda); descarte de 9 → recusado; descarte de 8 → ok |
| Final | **violações da regra = `[]`** e saídas por motivo contadas |

**Concorrência** (dois pedidos ao mesmo tempo):
```powershell
python referencia_bd\concorrencia_referencia.py
```
**Você deve ver** `30 x (('estoque insuficiente (422)', 'ok'), '20.000', False)` (um passa, o outro é recusado, o saldo fica 20, a regra não é violada) e `30 x (('ok', 'ok'), ('80.000', '80.000'), False)` (travar os ingredientes **em ordem de `id`** evita o deadlock quando um pedido pede 6 e 7 e o outro pede 7 e 6).

> **Por quê o `FOR UPDATE`:** sem ele, dois pedidos leem o mesmo saldo e **os dois passam**, vendendo estoque que não existe. Com o bloqueio, o segundo **espera** o primeiro terminar e lê o saldo já descontado.

## 6. Os SQLs da Sprint 3 (`docs/Apoio/sql_repositorios_sprint3.md`)

Crie o documento no formato do `sql_repositorios.md` (convenções; tabela rota ↔ método; cada método com SQL, retorno e falhas). O conteúdo está em `referencia_bd/consultas_sprint3.sql` (verificado). Todos recebem **`:hoje`** (F3).

| # | Consulta | Usada por |
|---|---|---|
| 1 | `GET /api/estoque`: por ingrediente, saldo total, **válido**, **vencido**, mínimo e validade mais próxima | Telas 10/16 |
| 2 | Linhas de um ingrediente, com a situação (`SEM_VALIDADE`, `VENCIDA`, `PROXIMA`, `OK`) | Detalhe do estoque |
| 3 | **Entrada do Python:** ingredientes dos produtos **ativos** (inclusive com 0), **saldo válido** e **custo** | Recomendação (Fase 4A) |
| 4 e 5 | Linhas para baixar (válidas) e para descartar (vencidas), com `FOR UPDATE` | Pedido, saída, descarte |
| 6 | `UPDATE` da linha, do saldo e `INSERT` da `SAIDA` com o motivo, **na mesma transação** | Idem |
| 7 | `INSERT` da `ENTRADA` com `quantidade_restante = quantidade` e soma ao saldo | Entrada de estoque |
| 8 | Conferência da regra de estoque (**0 linhas** = tudo certo) | Testes |

**Você deve ver, no banco de demonstração (Parte 7), para o `GET /api/estoque`:** Granola com total 11,160, **válido 5,160** e **vencido 6,000**; Açaí com válido 45,750; Morango 1,980 (abaixo do mínimo 2,000). A consulta 3 devolve a Granola com **5,160** (o vencido **não** conta, CT29).

> **Quando o Python recebe zero:** com o seed **oficial** (estoque 0), a consulta 3 devolve tudo 0 e a recomendação vem **toda zerada**. É o esperado, não um erro.

## 7. Assinaturas dos repositórios Java (F7: combinar com o Lucas hoje)

**Acordo:** você cria os `*Repository` e os `record`s de linha; o Lucas cria controllers e serviços. Convençam-se pela **tabela abaixo antes** de codar (mudou? atualize aqui e avise o outro). Pacote base: `com.mentefria.backend`. `NamedParameterJdbcTemplate`, `BigDecimal`, **sem regra de negócio**.

| Repositório | Método | Devolve |
|---|---|---|
| `IngredienteRepository` | `listar()`, `buscarPorId(id)`, `inserir(...)`, `atualizar(...)` | `Ingrediente` completo (snake_case no JSON) |
| | `buscarNomesPorIds(List<Integer>)` | `Map<Integer,String>` |
| | `travar(List<Integer> idsOrdenados)` | saldos, com `FOR UPDATE` **em ordem de id** |
| | `somarSaldo(id, qtd)` / `baixarSaldo(id, qtd)` | `int` linhas afetadas |
| | `listarEstoque(LocalDate hoje)` | linhas da consulta 1 |
| `MovimentacaoRepository` | `inserirEntrada(ingId, usuarioId, qtd, validade, obs)` | `int` id (grava `quantidade_restante = qtd`) |
| | `linhasValidas(ingId, hoje)` / `linhasVencidas(ingId, hoje)` | `List<Linha(id, restante)>` com `FOR UPDATE` |
| | `consumirLinha(linhaId, tira)` | `int` |
| | `inserirSaida(ingId, usuarioId, qtd, motivo, obs)` | `int` id |
| | `linhasDoIngrediente(ingId, hoje)` | linhas da consulta 2 |
| `ClienteRepository` | `listar()`, `inserir(nome, contato, endereco)` | `Cliente` / `int` |
| `PedidoRepository` | `inserir(clienteId, usuarioId, total)` | `(id, dataPedido)` |
| | `inserirItens(pedidoId, itens)` | `batchUpdate` |
| | `listar()` | pedidos com itens |
| `ProdutoRepository` | `listarComComposicao()`, `buscarPorIds(ids)`, `composicoes(ids)`, `inserir(...)`, `inserirComposicao(...)`, `atualizarPreco(id, preco)` | conforme `sql_repositorios.md` |
| `UsuarioRepository` | `existeAdministrador()`, `inserir(...)`, `buscarPorEmail(email)`, `listarFuncionarios()` | conforme `sql_repositorios.md` |

Exemplo do padrão (**não compilado**):
```java
@Repository
public class MovimentacaoRepository {
    public record Linha(int id, BigDecimal restante) {}
    private final NamedParameterJdbcTemplate jdbc;
    public MovimentacaoRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Linha> linhasValidas(int ingredienteId, LocalDate hoje) {
        return jdbc.query("""
            SELECT id, quantidade_restante FROM movimentacao_estoque
            WHERE ingrediente_id = :ing AND tipo = 'ENTRADA' AND quantidade_restante > 0
              AND (data_validade IS NULL OR data_validade >= :hoje)
            ORDER BY data_validade NULLS LAST, id FOR UPDATE""",
            new MapSqlParameterSource().addValue("ing", ingredienteId).addValue("hoje", hoje),
            (rs, n) -> new Linha(rs.getInt("id"), rs.getBigDecimal("quantidade_restante")));
    }
}
```

## 8. Seed de demonstração (`bd/seed_demo.sql`)

**Por quê:** com o seed oficial em estoque 0, não há como demonstrar alertas, vencimento, pedidos nem a recomendação. O `seed_demo.sql` monta tudo com o mesmo algoritmo da Parte 5.
**Como (banco de estudo, **nunca** o de desenvolvimento do Lucas):**
```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_demo;" -c "CREATE DATABASE mente_fria_demo;"
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -q -f /tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -c "INSERT INTO usuario (nome,email,senha_hash,perfil) VALUES ('Dono','dono@exemplo.com','hash_falso','ADMINISTRADOR'),('Func','func@exemplo.com','hash_falso','FUNCIONARIO');"
docker cp referencia_bd/bd/seed_demo.sql mente-fria-db:/tmp/seed_demo.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -v ON_ERROR_STOP=1 -f /tmp/seed_demo.sql
```
(Em um ambiente real, o Administrador e o Funcionário são criados **pela tela**; o `hash_falso` serve só para estudo e **não permite login**.) **Você deve ver:** `BEGIN`, `DO`, a conferência com `(0 rows)` e `COMMIT`. O script **recusa** rodar sem Administrador ou sem Funcionário, ou em banco que já tem movimentações.

**Valores esperados** (as validades são relativas ao dia em que o seed rodou):

| Ingrediente | Saldo total | Válido | Vencido |
|---|---|---|---|
| Açaí (polpa) | 45,750 | 45,750 | 0 |
| Leite condensado | 14,220 | 14,220 | 0 |
| **Granola** | 11,160 | **5,160** | **6,000** (a linha vencida **não** é consumida por pedidos) |
| Banana | 11,050 | 11,050 | 0 (vence em 3 dias) |
| **Morango** | **1,980** | 1,980 | 0 (**abaixo do mínimo 2,000**) |
| Leite em pó | 4,760 | 4,760 | 0 |
| Copo descartável | 269,000 | 269,000 | 0 (sem validade) |

Totais dos pedidos (conferidos à mão): mês anterior **271,50**; mês atual **326,40**; ranking: Dono **346,20** (3 pedidos), Func **251,70** (3 pedidos). **A recomendação para este estoque é 151 / 1 / 0** (receita 1.966,80; lucro 1.011,71): entregue esses números ao Kauê e ao Lucas.

## 9. Os documentos que de fato faltam (a auditoria da `main`)

> **Antes de qualquer edição no Termo ou na Visão: avisar o professor (F9, P3).** O Termo foi aprovado em 28/08. Registre por escrito (e-mail ou mensagem) o que muda e por quê. **Só depois** edite.
>
> **Já está feito na `main` (não refaça):** `.env` e `.env.example` (PR #24); "Programação Linear Inteira pura" em Visão, Termo, Backlog, Plano de Testes, Arquitetura, README e UML; entidades da Visão; itens 2 e 10 do Termo; US19 → item 17; US26 no Backlog; Alternativa C e Gurobi 13.0.1 na Arquitetura; Contrato com B3, B6 e D3 confirmados.

| # | Documento | O que fazer (e por quê) |
|---|---|---|
| 1 | **Backlog** (US10, US11, US17, US6) e **Visão** (L29, L37, L70, L72) e **Termo** (L19, L25) | Trocar o que o otimizador maximiza de **receita para lucro**. US10: "para **maximizar o lucro** com o estoque disponível"; critério: "considerando estoque **válido**, composição, preço de venda **e custo dos ingredientes**". US11: exibir a quantidade por produto, o **lucro potencial** e a receita estimada. US6 (L16): "cálculos de receita" → "cálculos". **US17 (L38):** "receita máxima estimada" → **"lucro potencial estimado"** (confirmar com o PO na Fase 6). *Por quê:* receita ignora o custo; a dor (a) do cliente é "lucratividade" |
| 2 | **Backlog** (US9) | "saída de produtos" → "**baixa de ingredientes**" (F10). *Por quê:* o banco só registra movimentação de ingrediente |
| 3 | **Backlog** (US14, US27, Sprint alvo) | US14: reposição com **orçamento** (a regra do roteiro); acrescentar **US27 — cancelar pedido** (Could, Sprint 4); coluna **Sprint alvo**: US13 a US19, US26 e US27 → **Sprint 4** |
| 4 | **`docs/DER.md`** | Dicionário: `movimentacao_estoque.quantidade_restante` e `motivo`; `pedido.status`, `data_cancelamento`, `usuario_cancelamento_id`. Diagrama (Mermaid): os mesmos campos. Reescrever a nota do **FEFO**: passa a ser **por linha** (`quantidade_restante`) e a nota do saldo: "`quantidade_estoque` = soma dos restantes das entradas". Anotar o **seed com estoque 0** e o `seed_demo.sql` |
| 5 | **`docs/Apoio/mapa_constraints.md`** | Acrescentar as tabelas `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque` (a seção "Fora deste mapa" diz que entram quando as US forem detalhadas: **agora**). Linhas novas abaixo |
| 6 | **`docs/Plano_de_Testes_Mente_Fria.md`** | CT23/CT24 passam a falar de **lucro** (acrescentar o caso "maior receita ≠ maior lucro"); **CT29 por linha** (a linha vencida não conta; a validade vale até o dia, **hoje ainda vale**); novos casos abaixo |
| 7 | **`docs/Apoio/Arquitetura.md`** | D5: a reposição com orçamento passa a usar o solver (não é só Java); estoque por linhas e a regra de estoque; `@Profile("dev")` no endpoint de teste; `hoje` em `America/Sao_Paulo`. **Contrato v2** com o Kauê (Java ↔ Python) e o **Contrato REST** de estoque, pedidos e recomendação com o Lucas e o Leonardo |
| 8 | **Referências antigas** em documentos **vivos** | `sql_repositorios_sprint1.md` → `docs/Apoio/sql_repositorios.md`; `constraints_sprint1_teste.sql` → `bd/constraints_teste.sql`; `docs/Script_DDL.sql` → `bd/Script_DDL.sql`; `ARQUITETURA.md` → `docs/Apoio/Arquitetura.md`. Onde: Plano de Testes (5), `mapa_constraints.md` (2), Contrato (7), `sprint-1-roteiro.md` (12). **Não reescreva** relatórios de sprint (são histórico) |
| 9 | **`docs/Diagramas_UML.md`** | Comentário de `calcularQuantidadeOtimaProducao()`: maximiza **lucro**; reposição com orçamento em `calcularQuantidadeRecomendadaCompra` |
| 10 | **README** (linha 3) | "maximizar a receita" → "maximizar o lucro"; acrescentar o seed de demonstração e os testes novos de banco (comandos das Partes 4 e 8) |

**Linhas novas do `mapa_constraints.md`** (mensagens sugeridas; `NOT NULL` por **tabela + coluna**):

| Tabela | Constraint | Campo na API | Mensagem | HTTP |
|---|---|---|---|---|
| `cliente` | NOT NULL `nome`, `contato`, `endereco` | idem | Informe o nome / o contato / o endereço. | 400 |
| `pedido` | `fk_pedido_cliente` | `cliente_id` | Cliente não encontrado. | 400 |
| `pedido` | `chk_pedido_valor_total`, `chk_pedido_status`, `chk_pedido_cancelamento`, `fk_pedido_usuario*` | — | (erro interno: o backend preenche) | 500 |
| `pedido_produto` | `chk_pedido_produto_quantidade` / `pedido_produto_pkey` / `fk_pedido_produto_produto` | `itens` | A quantidade deve ser maior que zero. / Este produto já foi adicionado ao pedido. / Produto não encontrado. | 400 |
| `movimentacao_estoque` | `chk_movimentacao_tipo` | `tipo` | Tipo de movimentação inválido. | 400 |
| | `chk_movimentacao_quantidade` | `quantidade` | A quantidade deve ser maior que zero. | 400 |
| | `chk_movimentacao_data_validade` | `data_validade` | A validade só pode ser informada em entradas. | 400 |
| | `chk_movimentacao_motivo` | `motivo` | Informe o motivo da saída. | 400 |
| | `chk_movimentacao_restante`, `fk_movimentacao_usuario` | — | (erro interno) | 500 |
| | `fk_movimentacao_ingrediente` | `ingrediente_id` | Ingrediente não encontrado. | 400 |
| `ingrediente` | `chk_ingrediente_quantidade_estoque` (em baixa) | — | Estoque insuficiente. | **422** |

**Casos novos do Plano de Testes** (ajuste a numeração à do roteiro, se já houver):

| ID | Cenário | Resultado esperado |
|---|---|---|
| CT30 | Entrada cria linha | `quantidade_restante = quantidade`; saldo soma |
| CT31 | Baixa pela menor validade | Consome a linha de menor validade; sem validade por último |
| CT32 | Linha nova com validade **menor** que uma já consumida | A próxima saída sai da **nova** (S2 da Parte 5) |
| CT33 | Pedido ou saída com só linha vencida | Recusado; nada muda |
| CT34 | Descarte de vencidos | Só consome vencidas; recusa quantidade maior que o vencido |
| CT35 | Saída sem motivo | Rejeitada pelo banco (`chk_movimentacao_motivo`) |
| CT36 | Regra de estoque após cada operação | Violações = 0 |
| CT37 | Dois pedidos simultâneos pelo mesmo estoque | Só um passa; saldo certo; sem deadlock |

## 10. PR e fechamento

```powershell
git add bd/Script_DDL.sql
git commit -m "feat: linhas de estoque, motivo e status do pedido; seed oficial com estoque 0"
git add bd/constraints_sprint3_teste.sql
git commit -m "test: constraints de cliente, pedido e movimentacao por linhas (T33-T73)"
git add bd/seed_demo.sql docs README.md
git commit -m "docs: seed de demonstracao, DER, mapa de constraints, SQL da Sprint 3 e plano de testes"
git push -u origin feature/banco-sprint3
```
No PR: **"ATENÇÃO — recriar o banco"** (o schema mudou: todos precisam rodar o "Recomeçar do zero" do README), a tabela de decisões (D11, F1) e os resultados `32 | 0` e `41 | 0`. Peça revisão ao **Lucas** (consome o schema e os SQLs) e ao **Kauê**. **Avise a equipe no grupo** antes do merge.

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `git apply` falha | `Script_DDL.sql` mudou depois do commit `1ddb5ae` | Usar `referencia_bd/bd/Script_DDL.sql` como modelo |
| `relation "usuario" already exists` | Script rodado duas vezes | Recriar o banco |
| `violates check constraint "chk_movimentacao_restante"` ao inserir entrada | Faltou `quantidade_restante` | Toda `ENTRADA` grava `quantidade_restante = quantidade` |
| `violates check constraint "chk_movimentacao_motivo"` | Saída sem motivo | Informar `PEDIDO`, `CONVENCIONAL` ou `DESCARTE_VENCIDO` |
| Seed de demonstração recusa | Falta Administrador/Funcionário ou o banco não é novo | Parte 8 |
| `psycopg2` não conecta | `MF_DSN` errado | Conferir host, porta, banco e senha |
| Datas de validade "diferentes" no dia seguinte | São relativas ao dia do seed | Os "dias" (−2, +3) é que valem no dia do seed |

## Checklist final
- [ ] Baseline `32 | 0` **antes** do patch e **depois**
- [ ] Patch aplicado; seed oficial com estoque 0
- [ ] `41 | 0` nos testes novos; experimentos A e B registrados
- [ ] Assinaturas dos repositórios combinadas por escrito com o Lucas
- [ ] `sql_repositorios_sprint3.md` criado
- [ ] `seed_demo.sql` roda; valores da Parte 8 conferidos
- [ ] Professor avisado **antes** de editar Termo e Visão
- [ ] Documentos da Parte 9 feitos (só os que faltam)
- [ ] PR com "recriar o banco"

## Depois (Sprint 4, só sinalização)
Cancelamento de pedido (US27), alertas (US15/US16), dashboard (US17) e reposição (US14). Pendência **F12** (definir "consumo" de forma única) antes da Fase 6.
