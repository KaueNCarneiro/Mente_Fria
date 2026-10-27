# Tutorial — Sprint 3 (Banco de Dados e Documentos) — Diogo

**Para:** Diogo Pereira Miranda · **Papel:** dados e qualidade da documentação · **Revisão:** 26/10/2026 (auditoria) · **Substitui** `historico/Tutorial_Sprint3_BancoDeDados_Diogo.md`.
**Base:** `sprint-3-roteiro.md` (v5), `sprint-3-decisoes-e-pendencias.md`, `sprint-3-casos-de-teste-propostos.md`.

> **O que foi verificado e o que não foi (26/10).**
> - Executado num **PostgreSQL 16.10** temporário (Zonky, sem Docker, porque o Docker Desktop da máquina não respondeu).
> - O patch aplica (`git apply --check` ok) sobre o `bd/Script_DDL.sql` da `main`.
> - Os 32 testes antigos passam antes **e** depois do patch: `32 | 0`. Os 41 novos: `41 | 0`.
> - Experimento A: `37 | 4`. Experimento B: `38 | 3`.
> - O seed de demonstração roda e confere com todos os valores da §8. FEFO S1–S5 conferem. Concorrência 30/30 nas duas situações.
> - **Não verificado:** os comandos `docker …` desta máquina (o motor não subiu) e o Java dos repositórios (não compilado).
> - **Mudança de caminhos:** o tutorial de 14/10 citava `referencia_bd/…`, uma pasta que nunca existiu. Todo o material está em **`docs/Sprint_3/referencia/`**.

## 0. O que você entrega e em que ordem

| Prioridade | Entrega | Quando | Libera quem |
|---|---|---|---|
| **1** | Patch do DDL e os 41 testes novos | **Seg 26** | Lucas (Fase 3), Kauê (dados) |
| **2** | Assinaturas dos repositórios Java combinadas com o Lucas (§7) | **Ter 27** | Lucas (Fase 2) |
| **3** | Mensagem ao professor: A3, F9 e P3 (§9.1) | **Ter 27** | Toda a equipe |
| 4 | Repositórios Java (usuário, ingrediente, produto; depois movimentação e pedido) | Qua 28 – Qui 29 | Lucas |
| 5 | `seed_demo.sql` no repositório e `sql_repositorios_sprint3.md` | Qua 28 | Kauê, Leonardo, Lucas |
| 6 | Documentos (§9.2) e casos de teste do banco no Plano de Testes | Qui 29 – Sáb 31 | Janela de testes |
| 7 | PR do banco | **Ter 27** (DDL e testes); os outros PRs conforme ficarem prontos | — |

**Pronto quando:**
1. `32 | 0` e `41 | 0`;
2. seed oficial com estoque 0;
3. o `seed_demo.sql` roda e confere;
4. assinaturas escritas;
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

> **Se o `docker start` falhar** com `failed to connect to the docker API … dockerDesktopLinuxEngine`, o Docker Desktop não está pronto.
> 1. Abra a janela dele e veja se pede alguma ação.
> 2. Espere aparecer "Engine running".
> 3. Se não aparecer, confira a virtualização no BIOS/UEFI. É o BD01 da Sprint 2: registre como foi resolvido, porque esse registro está pendente.

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

**Revisão de SQL (skill `sql-code-review`, 26/10):**
- Todos os comandos usam parâmetros nomeados; não há concatenação.
- O `FOR UPDATE` segue a ordem de bloqueio por `id` (sem *deadlock*, provado).
- O índice parcial cobre as consultas 4 e 5.
- As FKs `pedido_produto.produto_id` e `pedido.usuario_id` não têm índice. Para o volume de uma açaiteria é irrelevante; fica só registrado.

## 7. Assinaturas dos repositórios Java (F7, combinar com o Lucas na terça 27)

**Acordo:** você cria os `*Repository` e os `record`s de linha; o Lucas cria os controllers e serviços. Se mudar algo aqui, avise o Lucas e atualize os dois tutoriais.

Pacote base: `com.mentefria.backend`. Regras: `NamedParameterJdbcTemplate` (D4), `BigDecimal`, **sem regra de negócio**, **sem JPA**.

| Repositório | Método | Devolve |
|---|---|---|
| `UsuarioRepository` | `existeAdministrador()`, `inserir(nome, email, hash, perfil)`, `buscarPorEmail(email)`, `listarFuncionarios()` | `boolean` / `int` (id) / **`Optional<Usuario>`** / `List` |
| `UnidadeMedidaRepository` | `listar()` | `List<UnidadeMedida>` |
| `IngredienteRepository` | `listar()`, `buscarPorId(id)`, `inserir(...)`, `atualizar(...)` | `Ingrediente` completo / `Optional` / `int` |
| | `buscarNomesPorIds(List<Integer>)` | `Map<Integer, String>` |
| | `travar(List<Integer> idsOrdenados)` | `void` (`SELECT … FOR UPDATE` **em ordem de id**) |
| | `somarSaldo(id, qtd)` / `baixarSaldo(id, qtd)` | `int` (linhas afetadas) |
| | `listarEstoque(LocalDate hoje)` | Linhas da consulta 1 |
| | `entradaOtimizador(LocalDate hoje)` | Linhas da consulta 3 |
| `MovimentacaoRepository` | `inserirEntrada(ingId, usuarioId, qtd, validade, agora, obs)` | `int` (grava `quantidade_restante = qtd`) |
| | `linhasValidas(ingId, hoje)` / `linhasVencidas(ingId, hoje)` | `List<Linha(id, restante)>`, com `FOR UPDATE` |
| | `consumirLinha(linhaId, tira)` | `int` |
| | `inserirSaida(ingId, usuarioId, qtd, motivo, agora, obs)` | `int` |
| | `linhasDoIngrediente(ingId, hoje)` | Linhas da consulta 2 |
| `PedidoRepository` | `inserir(clienteId, usuarioId, total, agora)` | `(id, dataPedido)` |
| | `inserirItens(pedidoId, itens)` | `batchUpdate` |
| | `listar()` | Pedidos com itens |
| `ProdutoRepository` | `listarComComposicao()`, `buscarPorIds(ids)`, `composicoes(ids)`, `inserir(...)`, `inserirComposicao(...)`, `atualizarPreco(id, preco)` | Conforme `sql_repositorios.md` |

**Padrão** (não compilado; segue a revisão da skill de JDBC):

```java
// usuario/Usuario.java  (arquivo próprio: o AuthController do Lucas importa com.mentefria.backend.usuario.Usuario)
package com.mentefria.backend.usuario;
public record Usuario(int id, String nome, String email, String senhaHash, String perfil, boolean ativo) {}

// usuario/UsuarioRepository.java
package com.mentefria.backend.usuario;
@Repository
public class UsuarioRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Usuario> buscarPorEmail(String email) {          // nunca queryForObject: lança exceção se não achar
        return jdbc.query("SELECT id, nome, email, senha_hash, perfil, ativo FROM usuario WHERE email = :email",
                Map.of("email", email),
                (rs, n) -> new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                        rs.getString("senha_hash"), rs.getString("perfil"), rs.getBoolean("ativo")))
            .stream().findFirst();
    }

    public int inserir(String nome, String email, String hash, String perfil) {   // RETURNING id sempre devolve 1 linha
        return jdbc.queryForObject("INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES (:n, :e, :h, :p) RETURNING id",
                Map.of("n", nome, "e", email, "h", hash, "p", perfil), Integer.class);
    }
}
```

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

- Pedidos: mês anterior **271,50**; mês atual **326,40**. Ranking: Dono **346,20** (3 pedidos); Func **251,70** (3 pedidos). Invariante: 0 violações.
- **A recomendação para este estoque é 151/1/0** (receita 1.966,80; lucro 1.011,71), confirmada também por força bruta, sem solver.

## 9. Documentos

### 9.1 Antes de editar: avisar o professor (F9, P3, A3) — terça 27

O Termo foi aprovado em 28/08. Envie **uma** mensagem, com o Lucas (PO), cobrindo três pontos:

1. **receita → lucro:** o que o sistema maximiza (motivo: a dor (a) do Documento de Visão é "lucratividade");
2. **reposição com orçamento** (US14);
3. **escopo e prazo (A3):** US14, US17, US18 e US20 não cabem até 01/11; perguntar como registrar (adendo ao Termo) e se o deploy é exigido para a janela de testes.

Guarde a resposta em `docs/Sprint_3/`. **Só depois** edite o Termo, a Visão e o Backlog. No Termo assinado, registre a mudança como **adendo datado**, sem reescrever o texto aprovado.

### 9.2 O que fazer (verificado em 26/10: os `.docx` são idênticos aos da `main`, então nada foi feito ainda)

| # | Documento | O que fazer (e por quê) |
|---|---|---|
| 1 | **Backlog** (US6, US10, US11, US17), **Visão** e **Termo** | Receita → **lucro** (depois da §9.1). US10: "maximizar o **lucro** com o estoque **válido**", e o critério ganha "custo dos ingredientes". US11: exibir quantidade, **lucro potencial** e receita estimada. US6: "cálculos de receita" → "cálculos". US17: "receita máxima estimada" → **"lucro potencial estimado"** (confirmar com o PO) |
| 2 | **Backlog** (US9) | "Saída de produtos" → "**baixa de ingredientes**" (F10) |
| 3 | **Backlog** (US14, US27, Sprint alvo) | US14 com orçamento; nova **US27 — cancelar pedido** (Could; **não devolve estoque**); coluna "Sprint alvo" conforme a decisão A3 |
| 4 | **`docs/DER.md`** | Dicionário e diagrama: `movimentacao_estoque.quantidade_restante` e `motivo`; `pedido.status`, `data_cancelamento` e `usuario_cancelamento_id`. Reescrever as notas de FEFO (**por linha**) e de saldo ("= soma dos restantes"). Anotar o seed com estoque 0, o `seed_demo.sql` e o `:agora` (A9) |
| 5 | **`docs/Apoio/mapa_constraints.md`** | Acrescentar `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque` (tabela abaixo). Corrigir a referência `sql_repositorios_sprint1.md` → `sql_repositorios.md` |
| 6 | **`docs/Plano_de_Testes_Mente_Fria.md`** | Ajustar CT23, CT24 e CT29 e incorporar **CT30–CT50** de `sprint-3-casos-de-teste-propostos.md` (os de banco são seus; os de Python, do Kauê). **Sem** meta de cobertura |
| 7 | **`docs/Apoio/Arquitetura.md`** | D5 (a reposição com orçamento passa a usar o solver, quando entrar); estoque por linhas; `@Profile("dev")`; `America/Sao_Paulo` (F3, A9); a decisão A11 (Compute Server) depois que o Kauê decidir; versão do Gurobi (P2) |
| 8 | **Referências antigas em documentos vivos** | `sql_repositorios_sprint1.md` → `docs/Apoio/sql_repositorios.md`; `constraints_sprint1_teste.sql` → `bd/constraints_teste.sql`; `docs/Script_DDL.sql` → `bd/Script_DDL.sql`; `ARQUITETURA.md` → `docs/Apoio/Arquitetura.md`. Onde: Plano de Testes, `mapa_constraints.md`, Contrato (§0) e o cabeçalho de `bd/constraints_teste.sql`. **Não** reescreva os relatórios de sprint: são histórico |
| 9 | **`docs/Diagramas_UML.md`** | `calcularQuantidadeOtimaProducao()` passa a maximizar **lucro** |
| 10 | **README** | Linha 3 (receita → lucro, depois da §9.1); comandos dos testes novos (§4); seed de demonstração (§8) |

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
| `failed to connect to the docker API` | Docker Desktop sem o motor | §2 (BD01) |
| `git apply` falha | `Script_DDL.sql` mudou depois de 08/10 | Usar `referencia/bd/Script_DDL.sql` como modelo |
| `relation "usuario" already exists` | Script rodado duas vezes | Recriar o banco |
| `violates check constraint "chk_movimentacao_restante"` | Entrada sem `quantidade_restante` | Toda `ENTRADA` grava `restante = quantidade` |
| `violates check constraint "chk_movimentacao_motivo"` | Saída sem motivo | `PEDIDO`, `CONVENCIONAL` ou `DESCARTE_VENCIDO` |
| O seed de demonstração recusa | Falta Administrador ou Funcionário, ou o banco não é novo | §8 |
| `psycopg2` não conecta | `MF_DSN` errado | Conferir host, porta, banco e senha |
| Datas "diferentes" no dia seguinte | São relativas ao dia do seed | Conte os dias (−2, +3), não as datas |

## Checklist final

- [ ] Baseline `32 | 0` antes e depois do patch
- [ ] Patch aplicado; seed oficial com estoque 0; comentário da US27 correto
- [ ] `41 | 0`; experimentos A (`37 | 4`) e B (`38 | 3`) registrados
- [ ] Mensagem ao professor (A3, F9, P3) enviada e resposta guardada
- [ ] Assinaturas combinadas por escrito com o Lucas
- [ ] Repositórios Java (sem JPA, `Optional` no `buscarPorEmail`, `:agora`)
- [ ] `sql_repositorios_sprint3.md` criado; `seed_demo.sql` no `bd/`
- [ ] Documentos da §9.2; CT30 em diante no Plano de Testes até 31/10
- [ ] PRs com "recriar o banco"

## Fora da Sprint 3 (só sinalização; depende da A3)

Cancelamento (US27), alertas (US15/US16), dashboard (US17), reposição (US14). A pendência F12 precisa ser resolvida antes do dashboard.
