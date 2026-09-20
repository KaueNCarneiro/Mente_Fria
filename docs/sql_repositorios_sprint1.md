# SQL dos repositórios — Sprint 1 (US1–US6)

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

**Última atualização:** 19/09/2026

> **Status:** todos os SQLs abaixo foram executados no PostgreSQL 16, sobre o `Script_DDL.sql` entregue, com parâmetros reais (37 verificações, incluindo os erros esperados conferidos por SQLSTATE e nome de constraint). A regra do açaí foi testada em Java 21 (15 casos). **Os trechos de código Spring (seção "Padrões de código") ainda não foram executados** — a equipe valida ao implementar. Itens marcados como **proposta** dependem de confirmação.

Este documento é o contrato entre quem cuida dos dados e quem escreve os `Repository` em Java: cada método abaixo é um SQL pronto, com o que devolve e com o que pode falhar. Convenção de nomes: a mesma do `Roteiro_Sprint1_Detalhado_PostgreSQL_v2.md`.

---

## 1. Convenções

- **`NamedParameterJdbcTemplate`** (parte do Spring JDBC, decisão D4): os parâmetros aparecem como `:nome`.
- **SQL sempre com parâmetros.** Nunca concatenar texto vindo do usuário no SQL (SQL Injection).
- **Um `Repository` por agregado; regras de negócio no `Service`.** O `Repository` só executa SQL.
- **Transações no `Service`** (`@Transactional`): cadastrar produto + composição é uma operação só (seção 3).
- **`DECIMAL` do banco ↔ `BigDecimal` no Java.** Nunca `double`/`float` para preço, custo ou quantidade.
- **`senha_hash`** só é lida em `buscarPorEmail` (para conferir o login) e **nunca** vai para uma resposta da API.
- **E-mail normalizado** (sem espaços nas pontas, em minúsculas) antes de gravar e antes de consultar: o `UNIQUE` do PostgreSQL diferencia maiúsculas (verificado).
- **Quem acessa:** as histórias US4, US5 e US6 são do proprietário — em princípio, endpoints de **Administrador**; alterar preço é sempre só Administrador (CT05). O controle de acesso é do Controller/segurança, não do SQL.
- **Falhas possíveis** listam o SQLSTATE e a constraint; a tradução para mensagem por campo está em `mapa_constraints.md`.

---

## 2. Padrões de código Java (não executados)

```java
// 1) INSERT ... RETURNING id  → devolve o id gerado
Integer id = jdbc.queryForObject(SQL_INSERIR,
        new MapSqlParameterSource()
                .addValue("nome", nome)
                .addValue("email", email)
                .addValue("senha_hash", senhaHash)
                .addValue("perfil", perfil),
        Integer.class);

// 2) IN (:ids)  → passe uma List<Integer> (não vazia)
List<IngredienteNome> nomes = jdbc.query(SQL_NOMES_POR_IDS, Map.of("ids", idsDaComposicao), mapeador);

// 3) Várias linhas de uma vez (composição do produto)
jdbc.batchUpdate(SQL_INSERIR_COMPOSICAO, SqlParameterSourceUtils.createBatch(linhas)); // linhas: List<Map<String,Object>>

// 4) UPDATE/DELETE → número de linhas afetadas (0 = não encontrado → 404)
int afetadas = jdbc.update(SQL_ATUALIZAR_PRECO, Map.of("id", id, "preco_venda", preco));
```

---

## 3. Repositórios

### UsuarioRepository

**`existeAdministrador`** · US1

Diz se já existe alguma conta de Administrador (decide se o cadastro do dono ainda pode ser aberto — ver dúvida D-A).

```sql
SELECT EXISTS (SELECT 1 FROM usuario WHERE perfil = 'ADMINISTRADOR')
```

- **Retorno:** `boolean` (`queryForObject(sql, Boolean.class)`)

**`inserir`** · US1, US2

Cria um usuário. O `perfil` é definido pelo Service (`ADMINISTRADOR` na US1, `FUNCIONARIO` na US2), nunca vem do corpo da requisição.

```sql
INSERT INTO usuario (nome, email, senha_hash, perfil)
VALUES (:nome, :email, :senha_hash, :perfil)
RETURNING id
```

- **Retorno:** `id` gerado (`int`)
- **Falhas possíveis:** `uq_usuario_email` (23505) · `chk_usuario_perfil` (23514) · NOT NULL em `nome`, `email`, `senha_hash` (23502)
- **Nota:** `ativo` nasce `TRUE` (default). O e-mail deve chegar já normalizado (sem espaços nas pontas, em minúsculas).

**`buscarPorEmail`** · US3

Busca o usuário para o login. Devolve 0 ou 1 linha.

```sql
SELECT id, nome, email, senha_hash, perfil, ativo
FROM usuario
WHERE email = :email
```

- **Retorno:** 0 ou 1 linha
- **Nota:** Único lugar que lê `senha_hash`, para conferir o hash no login. Esse campo **nunca** vai para uma resposta da API. O Service recusa `ativo = FALSE` (proposta).

**`listarFuncionarios`** · US2

Lista os funcionários (Tela 04). Não seleciona `senha_hash`.

```sql
SELECT id, nome, email, perfil, ativo
FROM usuario
WHERE perfil = 'FUNCIONARIO'
ORDER BY nome
```

- **Retorno:** lista de linhas

**`atualizarFuncionario`** · US2 (opcional)

Edita nome, e-mail e situação de um funcionário (Tela 05 tem "Editar", mas a US2 só pede o cadastro). O `AND perfil = 'FUNCIONARIO'` impede editar o Administrador por este caminho.

```sql
UPDATE usuario
SET nome = :nome, email = :email, ativo = :ativo
WHERE id = :id AND perfil = 'FUNCIONARIO'
```

- **Retorno:** linhas afetadas (`0` = não encontrado → 404)
- **Falhas possíveis:** `uq_usuario_email` (23505) · NOT NULL (23502)

### UnidadeMedidaRepository

**`listar`** · US4

Alimenta a lista de seleção "unidade de medida" do cadastro de ingredientes. Necessária porque `ingrediente.unidade_medida` é FK e a tela de unidades só chega na Sprint 3. Devolve também a porção sugerida (pré-preenchimento).

```sql
SELECT unidade_medida, porcao_padrao
FROM unidade_medida_porcao_padrao
ORDER BY unidade_medida
```

- **Retorno:** lista de linhas (5 no seed: `kg`, `l`, `mg`, `ml`, `un`)

### IngredienteRepository

**`listar`** · US4

Lista os ingredientes (Tela 06).

```sql
SELECT id, nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque, quantidade_minima
FROM ingrediente
ORDER BY nome
```

- **Retorno:** lista de linhas
- **Nota:** Nomes que **começam** com letra acentuada (ex.: "Água de coco") dependem do *collation* do banco: em `C`/`C.UTF-8` vão para o **fim** da lista (verificado); em collations de idioma (como `en_US.utf8`, comum no `postgres:16` do Docker — confirmem com `\l`) ficam junto das demais. Se precisarem de ordem igual em todas as máquinas, ordenem no Service.

**`buscarPorId`** · US4

Busca um ingrediente (Tela 07 — Editar). Devolve 0 ou 1 linha.

```sql
SELECT id, nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque, quantidade_minima
FROM ingrediente
WHERE id = :id
```

- **Retorno:** 0 ou 1 linha

**`buscarNomesPorIds`** · US5

Traz `id` e `nome` dos ingredientes de uma composição. Serve a duas checagens do Service: (1) algum id não existe (menos linhas que ids pedidos) e (2) a regra do açaí (ver seção própria).

```sql
SELECT id, nome
FROM ingrediente
WHERE id IN (:ids)
```

- **Retorno:** lista de linhas
- **Nota:** `:ids` é uma `List<Integer>`. Não pode ser vazia (`IN ()` é inválido): o Service rejeita composição vazia antes (CT13).

**`inserir`** · US4

Cadastra um ingrediente. `quantidade_estoque` não é informada: nasce `0` e só muda por movimentação (Sprint 2).

```sql
INSERT INTO ingrediente (nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_minima)
VALUES (:nome, :unidade_medida, :custo_unitario, :porcao_padrao, :quantidade_minima)
RETURNING id
```

- **Retorno:** `id` gerado (`int`)
- **Falhas possíveis:** `fk_ingrediente_unidade_medida` (23503) · `chk_ingrediente_custo_unitario`, `chk_ingrediente_porcao_padrao`, `chk_ingrediente_quantidade_minima` (23514) · NOT NULL em `nome`, `unidade_medida`, `custo_unitario`, `porcao_padrao` (23502)
- **Nota:** `quantidade_minima` tem default `0` no banco; se o formulário não a enviar, passe `0`.

**`atualizar`** · US4

Edita os dados cadastrais. **Não** altera `quantidade_estoque` (só as movimentações da Sprint 2 fazem isso).

```sql
UPDATE ingrediente
SET nome = :nome, unidade_medida = :unidade_medida, custo_unitario = :custo_unitario,
    porcao_padrao = :porcao_padrao, quantidade_minima = :quantidade_minima
WHERE id = :id
```

- **Retorno:** linhas afetadas (`0` = não encontrado → 404)
- **Falhas possíveis:** as mesmas do `inserir`

### ProdutoRepository

> **Transação obrigatória:** `inserir` + `inserirComposicao` (e, na edição, `removerComposicao` + `inserirComposicao`) formam **uma operação só**. Se a composição falhar, o produto não pode ficar gravado sem ingredientes. Verificado: com `@Transactional`/`ROLLBACK`, uma composição com ingrediente inexistente desfaz também o `INSERT` do produto.

**`inserir`** · US5, US6

Cria o produto. **Sempre na mesma transação** das linhas de composição (`inserirComposicao`), com `@Transactional` no Service.

```sql
INSERT INTO produto (nome, preco_venda)
VALUES (:nome, :preco_venda)
RETURNING id
```

- **Retorno:** `id` gerado (`int`)
- **Falhas possíveis:** `chk_produto_preco_venda` (23514) · NOT NULL em `nome`, `preco_venda` (23502)
- **Nota:** `ativo` nasce `TRUE` (default).

**`inserirComposicao`** · US5

Grava uma linha da composição (N:N). Use `batchUpdate` com uma linha por ingrediente.

```sql
INSERT INTO produto_ingrediente (produto_id, ingrediente_id, quantidade_utilizada)
VALUES (:produto_id, :ingrediente_id, :quantidade_utilizada)
```

- **Retorno:** — (`batchUpdate`)
- **Falhas possíveis:** `chk_produto_ingrediente_quantidade` (23514) · `produto_ingrediente_pkey` (23505, ingrediente repetido) · `fk_produto_ingrediente_ingrediente`, `fk_produto_ingrediente_produto` (23503)
- **Nota:** `quantidade_utilizada` está na **unidade do ingrediente** (kg, l, un). Se a tela trabalhar em porções (Tela 09), a conversão `porções × porcao_padrao` é feita no Service.

**`removerComposicao`** · US5 (opcional)

Apaga a composição de um produto para regravá-la na edição (Tela 09 — Editar). Sempre na mesma transação do `inserirComposicao`.

```sql
DELETE FROM produto_ingrediente WHERE produto_id = :produto_id
```

- **Retorno:** linhas afetadas

**`atualizarDados`** · US5 (opcional)

Edita nome e situação do produto (Tela 09 — Editar). O preço tem método próprio (US6).

```sql
UPDATE produto SET nome = :nome, ativo = :ativo WHERE id = :id
```

- **Retorno:** linhas afetadas (`0` = não encontrado → 404)
- **Falhas possíveis:** NOT NULL em `nome` (23502)

**`atualizarPreco`** · US6

Atualiza o preço de venda. **Só Administrador** (CT05); o histórico de preços não entra no MVP (US21 é Won't).

```sql
UPDATE produto SET preco_venda = :preco_venda WHERE id = :id
```

- **Retorno:** linhas afetadas (`0` = não encontrado → 404)
- **Falhas possíveis:** `chk_produto_preco_venda` (23514) · NOT NULL (23502)

**`listarComComposicao`** · US5, US6

Lista produtos com seus ingredientes (Tela 08, com a composição expansível).

```sql
SELECT p.id AS produto_id, p.nome AS produto_nome, p.preco_venda, p.ativo,
       i.id AS ingrediente_id, i.nome AS ingrediente_nome, i.unidade_medida,
       pi.quantidade_utilizada
FROM produto p
LEFT JOIN produto_ingrediente pi ON pi.produto_id = p.id
LEFT JOIN ingrediente i ON i.id = pi.ingrediente_id
ORDER BY p.nome, i.nome
```

- **Retorno:** **uma linha por par produto × ingrediente** (o Service agrupa por `produto_id`, por exemplo com um `ResultSetExtractor`)
- **Nota:** O `LEFT JOIN` faz um produto sem composição aparecer com colunas de ingrediente `NULL` — situação que o app não deveria gerar, mas que assim fica visível em vez de sumir.

**`buscarComComposicao`** · US5

Um produto com a composição (Tela 09 — Editar). Mesmo formato do `listarComComposicao`.

```sql
SELECT p.id AS produto_id, p.nome AS produto_nome, p.preco_venda, p.ativo,
       i.id AS ingrediente_id, i.nome AS ingrediente_nome, i.unidade_medida,
       pi.quantidade_utilizada
FROM produto p
LEFT JOIN produto_ingrediente pi ON pi.produto_id = p.id
LEFT JOIN ingrediente i ON i.id = pi.ingrediente_id
WHERE p.id = :id
ORDER BY i.nome
```

- **Retorno:** linhas do produto (0 linhas = não encontrado → 404)

---

## 4. Regra do açaí (US5 / CT15) — decidida em 19/09/2026

**Decisão (Q2, opção "nome contém"):** um ingrediente conta como açaí quando o seu **nome, sem acentos e sem diferenciar maiúsculas de minúsculas, contém `acai`**. A verificação é feita no `Service` (Java), sem alteração no DER nem no DDL, e isolada em **um único método** — se um dia a equipe adotar a coluna `eh_base`, só ele muda.

```java
static boolean ehIngredienteAcai(String nome) {
    if (nome == null) {
        return false;
    }
    String semAcentos = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return semAcentos.toLowerCase(Locale.ROOT).contains("acai");
}

static boolean composicaoTemAcai(List<String> nomesDosIngredientes) {
    return nomesDosIngredientes.stream().anyMatch(ClasseDoService::ehIngredienteAcai);
}
```

**Fluxo no cadastro do produto (Service), antes de gravar nada:**

1. Composição vazia → `400`, campo `composicao`: *"Adicione ao menos um ingrediente."* (CT13)
2. `IngredienteRepository.buscarNomesPorIds(ids)` devolve menos linhas que os ids distintos pedidos → `400`: *"Ingrediente não encontrado."*
3. `composicaoTemAcai(nomes)` é falso → `400`, campo `composicao`: *"O produto precisa ter o açaí entre os ingredientes."* (CT15)
4. Só então, dentro da transação: `ProdutoRepository.inserir` + `inserirComposicao`.

**Casos para o teste unitário do método** (todos passaram em Java 21):

| Nome | Resultado esperado |
|---|---|
| `Açaí (polpa)` (nome do seed) | é açaí |
| `AÇAÍ ZERO` | é açaí |
| `polpa de acai` | é açaí |
| `Polpa de açaí` | é açaí |
| `  Açaí  ` (espaços nas pontas) | é açaí |
| `Ac\u0327ai\u0301` (acentos decompostos, como alguns teclados/colagens geram) | é açaí |
| `Granola`, `Leite condensado`, `Copo descartável 500ml` | não é açaí |
| texto vazio, `null` | não é açaí |
| `Assaí` (grafia alternativa) | **não é açaí** — limitação conhecida |

Consequências aceitas: (1) se o dono renomear o ingrediente para um nome sem "açaí", o sistema recusa novos produtos até o nome ser corrigido (falha segura); (2) mais de um ingrediente de açaí é permitido; (3) o banco **não** garante essa regra — só a aplicação.

---

## 5. Dúvidas e pontos a alinhar

### D-A — O cadastro de Administrador (US1) fica aberto para qualquer pessoa?

**Impacto:** o sistema atende um único comércio. Se o cadastro de Administrador ficar sempre aberto, qualquer pessoa que descubra a URL cria uma conta de Administrador e passa a ver e alterar preços, estoque e usuários — o que anula a separação de perfis (US2/US3).

**Opções:**
1. Sempre aberto (é o que a Tela 01 → Tela 02 do protótipo sugere hoje).
2. Aberto **somente enquanto não existir nenhum Administrador** (primeiro acesso), usando `UsuarioRepository.existeAdministrador`. Depois, criar novos administradores fica para quem já é Administrador (fora da Sprint 1).
3. Conta do dono criada fora do sistema (seed ou variável de ambiente) — descartada, pois a Q4 definiu que o seed não tem usuários.

**Recomendação:** opção 2. O custo é baixo: a consulta já está pronta e o `Service` ganha uma condição (se já existe Administrador, o cadastro responde `409`, e o frontend esconde o link "Criar conta" da tela de login). O CT01 continua válido e falta um caso de teste novo: *cadastro de Administrador quando já existe um → recusado*. Duas requisições simultâneas no primeiro acesso poderiam criar dois administradores; para o MVP o risco é desprezível.

**Decisão necessária:** equipe (Lucas, como Product Owner/Backend).

### D-B — A Tela 07 (protótipo) não tem o campo "porção", mas o banco exige

`ingrediente.porcao_padrao` é `NOT NULL` sem valor padrão, e a US4 lista "porção" entre os campos obrigatórios. Mas a Tela 07 do `Roteiro do Protótipo` mostra nome, unidade de medida, custo e quantidade mínima — sem porção. E a Tela 09 monta a composição "por quantidade de porções", que depende da porção de cada ingrediente. **Recomendação:** a Tela 07 ganha o campo *porção*, pré-preenchido com a porção sugerida da unidade escolhida (`UnidadeMedidaRepository.listar` já devolve esse valor). Não muda o banco; é um ajuste de tela (Leonardo).

### Proposta (sem decisão pendente) — usuário inativo

`usuario.ativo` existe, mas nenhuma história da Sprint 1 desativa usuários. O login recusar `ativo = FALSE` é a escolha segura e não custa nada: o `Service` confere o campo já devolvido por `buscarPorEmail`.

---

## 6. Fora deste documento (Sprint 2 em diante)

SQL de `cliente` (US13), `pedido`/`pedido_produto` (US12), `movimentacao_estoque` e ajuste de `quantidade_estoque` (US9), consultas do dashboard (US17) e as consultas de reposição e validade (US14–US16). Índices adicionais ficam para a Sprint 3, com o dashboard e dados reais.
