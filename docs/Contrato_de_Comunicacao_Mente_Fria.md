# Contrato de Comunicação — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

**Revisão:** 20/09/2026 — revisão de consistência com `ARQUITETURA.md`, `Script_DDL.sql`, `mapa_constraints.md` e `sql_repositorios_sprint1.md`, **aprovada pelo Kauê** (histórico na seção 3).

> **Fonte de verdade.** Este documento prevalece sobre a `ARQUITETURA.md` e sobre os roteiros nos **nomes de campo, nos valores de `status` e no formato de erro**. Os itens marcados **(confirmar com …)** foram adotados conforme a recomendação da revisão e aguardam a confirmação da pessoa indicada (lista na seção 2.4).

---

## 0. Mapa dos contratos

A arquitetura definida em `ARQUITETURA.md` tem três "costuras" entre partes diferentes do sistema:

| Costura | Tecnologias | Contrato | Donos |
|---|---|---|---|
| Banco ↔ Java | PostgreSQL ↔ `JdbcTemplate` | **Sim, já documentado fora deste arquivo:** `docs/sql_repositorios_sprint1.md` (SQL por método), `docs/mapa_constraints.md` (constraint → campo → mensagem → HTTP) e o schema em `docs/DER.md` + `Script_DDL.sql` | Diogo (SQL e schema) ↔ Lucas (repositórios em Java) |
| Java ↔ Python | Subprocesso, JSON via stdin/stdout | **Sim** — §1 | Kauê ↔ Lucas |
| Frontend ↔ Java | HTTP/REST, JSON, JWT | **Sim** — §2 (um documento, mas cobre vários endpoints) | Lucas ↔ Leonardo |

Um contrato existe para alinhar duas partes que não compartilham código nem enxergam a implementação uma da outra. Em Banco↔Java, o Java é do Lucas e os dados são do Diogo (Termo de Aceite): por isso os dois documentos de dados acima fazem esse papel, e o `Script_DDL.sql` + `docs/DER.md` são a fonte única da verdade do schema. Java↔Python e Frontend↔Java são as outras fronteiras reais entre pessoas e processos diferentes.

---

## 1. Contrato Backend (Java) ↔ Módulo Python — UC10/UC11

**Protocolo:** o Java inicia o processo Python via `ProcessBuilder`, escreve **um único JSON** na `stdin` do processo e **fecha a `stdin`**; o processo escreve **um único JSON** de resposta na `stdout` e encerra.

- **Codificação UTF-8** nas duas pontas. No Python: `sys.stdin.reconfigure(encoding="utf-8")` e o mesmo para a `stdout`. Sem isso, no Windows, "Açaí" chega quebrado.
- **Código de saída `0`** = há um JSON válido na `stdout`, **inclusive quando `status` é `"erro"`**. Qualquer outro código = falha técnica (exceção não tratada, processo morto); o `stderr` vai para o log do backend.
- **Timeout: 10 segundos** (valor provisório, até medir com o Gurobi). Se estourar, o Java encerra o processo e devolve um erro tratado, **sem *stack trace* para o usuário**.

### 1.1 Requisição (Java → Python, `stdin`)

Nomes de campo iguais aos do DER (snake_case), para não exigir tradução nas duas pontas.

```json
{
  "ingredientes": [
    { "id": 1, "quantidade_estoque": 40.000 },
    { "id": 2, "quantidade_estoque": 2.000 },
    { "id": 3, "quantidade_estoque": 0.000 },
    { "id": 4, "quantidade_estoque": 12.000 },
    { "id": 7, "quantidade_estoque": 300.000 }
  ],
  "produtos": [
    {
      "id": 1,
      "preco_venda": 12.90,
      "composicao": [
        { "ingrediente_id": 1, "quantidade_utilizada": 0.300 },
        { "ingrediente_id": 2, "quantidade_utilizada": 0.030 },
        { "ingrediente_id": 7, "quantidade_utilizada": 1.000 }
      ]
    },
    {
      "id": 2,
      "preco_venda": 18.90,
      "composicao": [
        { "ingrediente_id": 1, "quantidade_utilizada": 0.450 },
        { "ingrediente_id": 3, "quantidade_utilizada": 0.040 },
        { "ingrediente_id": 4, "quantidade_utilizada": 0.050 },
        { "ingrediente_id": 7, "quantidade_utilizada": 1.000 }
      ]
    }
  ]
}
```

Regras:

- Só entram **produtos com `ativo = true`**. Esse filtro é feito pelo Java antes de montar o JSON (o Python não consulta o banco).
- `ingredientes` traz **todos os ingredientes usados por algum produto enviado, inclusive os com estoque 0**. **O Java não remove ingrediente sem estoque.** Motivo: um produto que usa um ingrediente em falta precisa ser limitado a 0, e se o ingrediente sumisse da lista o Python não saberia o limite dele. No exemplo acima, a granola (`id` 3) está com estoque 0 e o leite condensado (`id` 2) está escasso: se a granola fosse retirada da lista, o Python recomendaria 46 copos do produto 2 sem granola em estoque; com ela na lista, o resultado correto é 66 do produto 1 e 0 do produto 2.
- O Python responde `status: "erro"` se uma composição citar um `ingrediente_id` que **não está** em `ingredientes`, se um produto vier **sem composição** ou se a lista de `produtos` vier **vazia**.
- `composicao` já vem achatada a partir de `produto_ingrediente`.
- **Unidades:** `quantidade_estoque` e `quantidade_utilizada` estão na **mesma unidade do ingrediente** (kg, l, un), como no banco. O Python não converte nada.
- **Números:** JSON `number`; preços com 2 casas decimais e quantidades com até 3, como no banco.

### 1.2 Resposta — sucesso (Python → Java, `stdout`)

```json
{
  "status": "sucesso",
  "receita_estimada": 851.40,
  "recomendacao": [
    { "produto_id": 1, "quantidade": 66 },
    { "produto_id": 2, "quantidade": 0 }
  ]
}
```

- `recomendacao` traz **um item por produto recebido**; `quantidade` é um **inteiro ≥ 0** (número de copos).
- **"Estoque insuficiente para produzir qualquer produto" é `sucesso`**, com todas as quantidades 0 e `receita_estimada` 0; o frontend mostra uma mensagem amigável. O `erro` fica para problemas de dados (§1.3).
- `receita_estimada` é **informativa**: o Java recalcula o valor com `BigDecimal` (§1.4).

### 1.3 Resposta — erro (ex.: dados inconsistentes)

```json
{
  "status": "erro",
  "mensagem": "Nenhum produto ativo para otimização"
}
```

Casos de `erro`: lista de produtos vazia, produto sem composição, composição que cita ingrediente ausente da lista, números inválidos.

O Java trata `status: "erro"` como **falha de negócio ou de dados** (mostra mensagem amigável, UC11), diferente de um código de saída ≠ 0 ou de um timeout (**falha técnica**).

### 1.4 Decisões que eram pendências

- **Precisão decimal:** o Java **recalcula `receita_estimada` com `BigDecimal`** a partir das quantidades inteiras de `recomendacao` e dos preços de venda; o valor devolvido pelo Python serve só como conferência. Antes de confiar no valor exibido no dashboard, vale um teste explícito comparando o resultado com um cálculo manual.
- **Timeout:** 10 segundos (provisório), como no protocolo acima.

### 1.5 Ponte com o roteiro de Hello World (Sprint 1)

O Roteiro da Sprint 1 usa o **banco real** (`mente_fria`) e um endpoint **temporário**, `GET /api/teste/python`: o Java lê o nome do primeiro ingrediente no banco, envia ao Python e devolve a resposta. O mesmo envelope é usado com um payload trivial, só para validar que o Java escreve, o Python lê/processa/responde e o Java lê de volta, sem acoplar o teste de infraestrutura à lógica do Gurobi:

```json
// stdin (Java → Python)
{ "echo": "Açaí (polpa)" }
// stdout (Python → Java)
{ "status": "sucesso", "echo": "AÇAÍ (POLPA)" }
```

Esse endpoint é **removido** depois que o Hello World for validado (ele não tem controle de acesso).

---

## 2. Contrato Frontend ↔ Backend (REST API)

**Base URL:** `http://localhost:8080` (local). As rotas abaixo **já começam com `/api`**. Produção: a URL do serviço no Render, definida no deploy. O frontend guarda a base em um único lugar, `frontend/js/config.js` (`API_BASE_URL`).

**Autenticação:** JWT no header `Authorization: Bearer <token>`, obtido em `POST /api/auth/login`. Todas as rotas exigem token válido, **exceto** `POST /api/auth/login`, `POST /api/auth/registro` (só enquanto não existir Administrador) e `GET /api/saude`. O token tem validade curta (1 a 2 horas); token expirado = `401`.

**CORS:** o backend libera apenas a origem do frontend (`CORS_ALLOWED_ORIGIN`): no desenvolvimento, `http://127.0.0.1:5500` e `http://localhost:5500` (Live Server); em produção, a URL do GitHub Pages. Métodos `GET`, `POST`, `PUT`, `DELETE` e `OPTIONS`; headers `Authorization` e `Content-Type`.

**Convenções do JSON:**

- Nomes de campo em **snake_case**, iguais ao DER (`unidade_medida`, `custo_unitario`). *(confirmar com Lucas e Leonardo — item B6)*
- Valores monetários com 2 casas decimais e quantidades com até 3; datas em ISO-8601 (`2026-09-20T14:30:00`); codificação UTF-8.

**Formato de erro padrão:**

```json
{
  "mensagem": "Dados inválidos",
  "campos": {
    "custo_unitario": "O custo não pode ser negativo.",
    "porcao_padrao": "A porção deve ser maior que zero."
  }
}
```

`campos` é **opcional**: aparece nos erros de validação ou de conflito de campo, e pode trazer **vários campos** de uma vez (US7). Erros que não pertencem a um campo (ex.: estoque insuficiente) trazem só `mensagem`. **As mensagens por campo vêm do `docs/mapa_constraints.md`**, para serem iguais em todo o sistema.

**Códigos HTTP:**

| Código | Quando |
|---|---|
| `200` / `201` | Sucesso |
| `400` | Validação de campo ou de regra de cadastro (inclui tudo que o banco rejeita por `NOT NULL`, `CHECK` e `FK`, o item repetido na composição e o produto sem açaí) |
| `401` | Sem token, token inválido/expirado, ou credenciais inválidas no login |
| `403` | Perfil sem permissão (ex.: Funcionário tentando alterar preço) |
| `404` | Recurso inexistente |
| `409` | Conflito de unicidade: e-mail já cadastrado e cadastro de Administrador quando já existe um |
| `422` | Regra de negócio de uma operação com dados válidos (ex.: estoque insuficiente) |
| `500` | Falha técnica (sem *stack trace* na resposta) |

### 2.1 Exemplos detalhados (padrão a repetir nos demais endpoints)

**`POST /api/auth/login`** — UC3, público

```json
// request
{ "email": "dono@acai.com", "senha": "senha1234" }
// response 200
{ "token": "eyJhbGciOi...", "perfil": "ADMINISTRADOR", "nome": "Dono da Açaiteria" }
```
Erro (US3): `401 { "mensagem": "E-mail ou senha inválidos." }`. Um usuário inativo (`ativo = false`) recebe a **mesma** resposta, para não revelar quais e-mails existem.

**`POST /api/auth/registro`** — UC1, público (**só enquanto não existir Administrador**)

```json
// request
{ "nome": "Dono da Açaiteria", "email": "dono@acai.com", "senha": "senha1234" }
// response 201
{ "id": 1, "nome": "Dono da Açaiteria", "email": "dono@acai.com", "perfil": "ADMINISTRADOR" }
```
Erros: senha curta (CT02) → `400 { "mensagem": "Dados inválidos", "campos": { "senha": "A senha deve ter no mínimo 8 caracteres." } }`; e-mail duplicado (CT03) → `409 { "mensagem": "Dados inválidos", "campos": { "email": "Este e-mail já está cadastrado." } }`; **Administrador já existe** → `409 { "mensagem": "O cadastro do Administrador já foi realizado." }`. *(confirmar com Lucas — item B5)*

**`GET /api/unidades-medida`** — Admin (alimenta a lista de seleção do formulário de ingredientes; a `porcao_padrao` serve para pré-preencher a porção)

```json
// response 200
[
  { "unidade_medida": "kg", "porcao_padrao": 0.040 },
  { "unidade_medida": "l",  "porcao_padrao": 0.030 },
  { "unidade_medida": "mg", "porcao_padrao": 40000.000 },
  { "unidade_medida": "ml", "porcao_padrao": 30.000 },
  { "unidade_medida": "un", "porcao_padrao": 1.000 }
]
```
Na Sprint 1 as unidades chegam ao banco só pelo seed; o `POST` (cadastrar unidade) é a US26/UC20, **Sprint 3**.

**`POST /api/ingredientes`** — UC4, Admin

```json
// request
{ "nome": "Açaí (polpa)", "unidade_medida": "kg", "custo_unitario": 18.50, "porcao_padrao": 0.150, "quantidade_minima": 10.000 }
// response 201
{ "id": 1, "nome": "Açaí (polpa)", "unidade_medida": "kg", "custo_unitario": 18.50, "porcao_padrao": 0.150, "quantidade_estoque": 0.000, "quantidade_minima": 10.000 }
```
Erro (unidade não cadastrada — FK `fk_ingrediente_unidade_medida`): `400 { "mensagem": "Dados inválidos", "campos": { "unidade_medida": "Selecione uma unidade de medida cadastrada." } }`

**`POST /api/produtos`** — UC5, Admin

```json
// request
{
  "nome": "Açaí Tradicional 300ml",
  "preco_venda": 12.90,
  "composicao": [
    { "ingrediente_id": 1, "quantidade_utilizada": 0.300 },
    { "ingrediente_id": 2, "quantidade_utilizada": 0.030 },
    { "ingrediente_id": 7, "quantidade_utilizada": 1.000 }
  ]
}
// response 201
{ "id": 4, "nome": "Açaí Tradicional 300ml", "preco_venda": 12.90, "ativo": true }
```
`quantidade_utilizada` está **na unidade do ingrediente** (kg, l, un). Produto e composição são gravados em **uma única transação**. *(quem converte porções em quantidade — item B1, confirmar com Leonardo e Lucas)*

Erros (todos `400`, no formato `{ "mensagem": "Dados inválidos", "campos": { ... } }`):

| Situação | `campos` |
|---|---|
| Composição sem açaí (CT15) | `"composicao": "O produto precisa ter o açaí entre os ingredientes."` |
| Composição vazia (CT13) | `"composicao": "Adicione ao menos um ingrediente."` |
| Ingrediente inexistente | `"composicao": "Ingrediente não encontrado."` |
| Preço zero ou negativo (CT17) | `"preco_venda": "O preço deve ser maior que zero."` |

**`PUT /api/produtos/{id}/preco`** — UC6, **só Administrador**

```json
// request
{ "preco_venda": 13.50 }
// response 200
{ "id": 1, "preco_venda": 13.50 }
```
Erros: Funcionário (CT05) → `403 { "mensagem": "Você não tem permissão para esta ação." }`; preço zero (CT17) → `400` com `"preco_venda": "O preço deve ser maior que zero."`.

**`GET /api/saude`** — público (usado no Roteiro de Hello World e como *health check* no deploy)

```json
// response 200
{ "status": "ok", "banco": "ok" }
```
Se o banco estiver fora, a resposta é `500`.

**`POST /api/pedidos`** — UC12, Admin/Funcionário (**Sprint 2**)

```json
// request
{
  "cliente_id": 1,
  "itens": [ { "produto_id": 1, "quantidade": 2 }, { "produto_id": 2, "quantidade": 1 } ]
}
// response 201
{ "id": 1, "valor_total": 44.70, "data_pedido": "2026-09-20T14:30:00" }
```
Erro (estoque insuficiente em qualquer item — UC12/CT25): `422 { "mensagem": "Estoque insuficiente para o produto Açaí Especial 500ml" }` — pedido inteiro rejeitado, nenhum item gravado.

Regras: `itens` **não pode repetir `produto_id`** (a chave primária de `pedido_produto` é `(pedido_id, produto_id)`) → `400`. O `cliente_id` é opcional e só existe a partir do seed/cadastro da Sprint 2 (o seed da Sprint 1 não tem clientes). O `preco_unitario_registrado` é preenchido pelo backend, não vem na requisição.

### 2.2 Rotas

| Recurso | Método | Rota | Perfil | UC | Sprint |
|---|---|---|---|---|---|
| Saúde | GET | `/api/saude` | Público | — | 1 |
| Auth | POST | `/api/auth/login` | Público | UC3 | 1 |
| Auth | POST | `/api/auth/registro` | Público (só enquanto não existir Administrador) | UC1 | 1 |
| Funcionários | POST | `/api/usuarios` | Admin | UC2 | 1 |
| Funcionários | GET | `/api/usuarios` | Admin | — | 1 |
| Ingredientes | GET | `/api/ingredientes` | Admin/Funcionário | UC4 | 1 |
| Ingredientes | POST | `/api/ingredientes` | Admin | UC4 | 1 |
| Ingredientes | PUT | `/api/ingredientes/{id}` | Admin | UC4 | 1 |
| Unidades de medida | GET | `/api/unidades-medida` | Admin | UC4 (lista) | 1 |
| Unidades de medida (porção padrão) | POST | `/api/unidades-medida` | Admin | UC20 | 3 |
| Produtos | GET | `/api/produtos` | Admin/Funcionário | UC5 | 1 |
| Produtos | POST | `/api/produtos` | Admin | UC5 | 1 |
| Produtos | PUT | `/api/produtos/{id}/preco` | Admin | UC6 | 1 |
| Estoque | POST | `/api/estoque/movimentacoes` | Admin/Funcionário | UC9 | 2 |
| Clientes | GET, POST | `/api/clientes` | Admin/Funcionário | UC13 | 2 |
| Pedidos | POST | `/api/pedidos` | Admin/Funcionário | UC12 | 2 |
| Pedidos | GET | `/api/pedidos` | Admin/Funcionário | — | 2 |
| Produção | POST | `/api/producao/recomendacao` | Admin | UC10/UC11 | 2 |
| Reposição | GET | `/api/reposicao/recomendacao` | Admin | UC14 | 2 |
| Estoque | GET | `/api/estoque/alertas-reposicao` | Admin/Funcionário | UC15 | 3 |
| Estoque | GET | `/api/estoque/alertas-validade` | Admin/Funcionário | UC16 | 3 |
| Marketing | GET | `/api/marketing/sugestoes` | Admin | UC18 | 3 |
| Dashboard | GET | `/api/dashboard` | Admin | UC17 | 3 |
| Dashboard | GET | `/api/dashboard/export-csv` | Admin | UC19 | 3 |

**Rotas opcionais — telas "Editar" do protótipo** (o SQL já existe em `docs/sql_repositorios_sprint1.md`, mas as histórias US1–US7 não as exigem). **Fora da Sprint 1 até a equipe decidir:**

| Método | Rota | Tela |
|---|---|---|
| GET | `/api/ingredientes/{id}` | 07 — Ingredientes: Editar |
| GET | `/api/produtos/{id}` | 09 — Produtos: Editar |
| PUT | `/api/produtos/{id}` | 09 — Produtos: Editar (nome, situação e composição) |
| PUT | `/api/usuarios/{id}` | 05 — Funcionários: Editar |

**Regra da Sprint 2 (estoque):** em `POST /api/estoque/movimentacoes`, `data_validade` só é aceita quando `tipo = ENTRADA` (constraint `chk_movimentacao_data_validade`); em `SAIDA`, enviá-la é `400`.

### 2.3 Sem paginação no MVP

Paginação em `GET /api/pedidos` e `GET /api/ingredientes` fica **fora do MVP** (o volume da açaiteria não a exige).

### 2.4 Itens adotados que aguardam confirmação

| Item | O que foi adotado | Confirmar com |
|---|---|---|
| B1 | `POST /api/produtos` recebe `quantidade_utilizada` (mesmo campo do banco); o **frontend converte** porções em quantidade, usando a `porcao_padrao` de cada ingrediente (que já vem em `GET /api/ingredientes`) | Leonardo e Lucas |
| B3 | Rotas das telas "Editar" fora da Sprint 1 | Equipe |
| B5 | Cadastro de Administrador só no primeiro acesso; depois, `409` (é a opção 2 do D-A) | **Confirmado por Lucas em 20/09/2026** |
| B6 | JSON em snake_case; o Lucas configura a serialização no Spring (por exemplo, `@JsonProperty` ou configuração global do Jackson, que varia com a versão do Spring Boot) | Lucas e Leonardo |
| D3 | `POST` (e não `GET`) em `/api/producao/recomendacao`: é uma ação disparada pelo proprietário e inicia um processo pesado | Lucas |

---

## 3. Histórico de revisões

| Data | O que mudou | Itens da revisão |
|---|---|---|
| 20/09/2026 | Item **B5** (§2.4) confirmado pelo Lucas: cadastro de Administrador só até existir o primeiro (D-A). Caso de teste correspondente registrado como CT01B no Plano de Testes. | B5 |
| 20/09/2026 | Revisão de consistência aprovada pelo Kauê. **§0:** donos e documentos do contrato Banco↔Java. **§1:** ingredientes com estoque 0 passam a ser enviados (removido o filtro "estoque > 0"); regras de erro e de "sem estoque" (`sucesso` com quantidades 0); UTF-8, timeout de 10 s e recálculo da receita em `BigDecimal`; exemplo de Hello World com o banco real. **§2:** Base URL sem `/api`; CORS; convenções do JSON (snake_case); formato de erro com `campos`; tabela de códigos HTTP; exemplos sem dados do seed antigo; novos exemplos (`unidades-medida`, `produtos`, preço, registro, `saude`); coluna Sprint; rotas "Editar" separadas; `POST` em produção; regras de `itens` únicos e de `data_validade`. | A1–A9, B1–B6, C1–C2, D1–D4 |
