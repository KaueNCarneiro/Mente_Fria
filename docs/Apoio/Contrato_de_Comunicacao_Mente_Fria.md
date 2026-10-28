# Contrato de Comunicação — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

**Revisão:** **27/10/2026 — versão 2 (Sprint 3)**, aprovada pela equipe e autorizada pelo Kauê (histórico na seção 3). Versões anteriores: 20/09/2026 (revisão de consistência, aprovada pelo Kauê) e 08/10/2026 (itens B3, B5, B6 e D3 confirmados).

> **Fonte de verdade.** Este documento prevalece sobre a `docs/Apoio/Arquitetura.md` e sobre os roteiros nos **nomes de campo, nos valores de `status` e no formato de erro**.
>
> **Origem da v2:** proposta da auditoria da Sprint 3 (`docs/Sprint_3/sprint-3-contrato-v2-proposta.md`). Todos os formatos desta versão são **exatamente** os que o backend e os módulos Python de `docs/Sprint_3/referencia/` produzem, conferidos por teste (95 testes no backend, 47 no Python).

---

## 0. Mapa dos contratos

A arquitetura definida em `docs/Apoio/Arquitetura.md` tem três "costuras" entre partes diferentes do sistema:

| Costura | Tecnologias | Contrato | Donos |
|---|---|---|---|
| Banco ↔ Java | PostgreSQL ↔ Spring JDBC (`NamedParameterJdbcTemplate`) | **Sim, documentado fora deste arquivo:** `docs/Apoio/sql_repositorios.md` (SQL por método), `docs/Apoio/mapa_constraints.md` (constraint → campo → mensagem → HTTP) e o schema em `docs/DER.md` + `bd/Script_DDL.sql` | Diogo (SQL, schema e repositórios) ↔ Lucas (serviços e controllers) |
| Java ↔ Python | Subprocesso, JSON via stdin/stdout | **Sim** — §1 | Kauê ↔ Lucas |
| Frontend ↔ Java | HTTP/REST, JSON, JWT | **Sim** — §2 | Lucas ↔ Leonardo |

Um contrato existe para alinhar duas partes que não compartilham código nem enxergam a implementação uma da outra. Em Banco↔Java, os dados são do Diogo (Termo de Aceite): por isso os documentos de dados acima fazem esse papel, e o `bd/Script_DDL.sql` + `docs/DER.md` são a fonte única da verdade do schema. Java↔Python e Frontend↔Java são as outras fronteiras reais entre pessoas e processos diferentes.

---

## 1. Contrato Backend (Java) ↔ Módulos Python — UC10/UC11 e UC14

**Protocolo:** o Java inicia o processo Python via `ProcessBuilder`, escreve **um único JSON** na `stdin` do processo e **fecha a `stdin`**; o processo escreve **um único JSON** de resposta na `stdout` e encerra. São dois módulos com o mesmo protocolo: `optimization/app/otimizador.py` (produção, §1.1–§1.4) e `optimization/app/reposicao.py` (reposição, §1.7).

- **Codificação UTF-8** nas duas pontas. O Java envia **sem BOM**; os módulos também aceitam entrada **com** BOM, porque o pipe do PowerShell envia um (decisão A8 da Sprint 3).
- **Código de saída `0`** = há um JSON válido na `stdout`, **inclusive quando `status` é `"erro"`**. Qualquer outro código = falha técnica (exceção não tratada, processo morto); o `stderr` vai para o log do backend.
- **Timeout: 10 segundos** por execução. Medido na Sprint 3: 188 ms numa máquina comum e 3,1 s no container com 0,1 de CPU (limite do Render free). Se estourar, o Java encerra o processo e devolve um erro tratado, **sem *stack trace* para o usuário**.
- **Uma execução por vez:** o backend enfileira as chamadas ao Python, porque as licenças acadêmica e de avaliação limitam sessões simultâneas (A27).

### 1.1 Requisição de produção (Java → Python, `stdin`)

Nomes de campo iguais aos do DER (snake_case), para não exigir tradução nas duas pontas. **Na v2, cada ingrediente ganha `custo_unitario`**, porque o modelo passou a maximizar o lucro (§1.2).

```json
{
  "ingredientes": [
    { "id": 1, "quantidade_estoque": 40.000, "custo_unitario": 18.50 },
    { "id": 2, "quantidade_estoque": 2.000, "custo_unitario": 12.00 },
    { "id": 3, "quantidade_estoque": 0.000, "custo_unitario": 22.00 },
    { "id": 4, "quantidade_estoque": 12.000, "custo_unitario": 5.50 },
    { "id": 7, "quantidade_estoque": 300.000, "custo_unitario": 0.35 }
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
- `ingredientes` traz **todos os ingredientes usados por algum produto enviado, inclusive os com estoque 0**. **O Java não remove ingrediente por falta de estoque.** Motivo: um produto que usa um ingrediente em falta precisa ser limitado a 0, e se o ingrediente sumisse da lista o Python não saberia o limite dele. No exemplo acima, a granola (`id` 3) está com estoque 0: se fosse retirada da lista, o Python recomendaria copos do produto 2 sem granola em estoque; com ela na lista, o resultado correto é 66 do produto 1 e 0 do produto 2.
- **`quantidade_estoque` é o saldo válido:** a soma do que resta nas linhas de entrada **não vencidas** (consulta 3 do `consultas_sprint3.sql`). As linhas vencidas não entram (decisão de 25/09/2026, agora por linha de estoque).
- O Python responde `status: "erro"` se uma composição citar um `ingrediente_id` que **não está** em `ingredientes`, se um produto vier **sem composição** ou se a lista de `produtos` vier **vazia**.
- `composicao` já vem achatada a partir de `produto_ingrediente`.
- **Unidades:** `quantidade_estoque` e `quantidade_utilizada` estão na **mesma unidade do ingrediente** (kg, l, un), como no banco. O Python não converte nada.
- **Números:** JSON `number`. `custo_unitario` ≥ 0 e `preco_venda` > 0, com até 2 casas; quantidades com até 3, como no banco. Números fora do domínio resultam em `status: "erro"` com código de saída 0.

### 1.2 Resposta de produção — sucesso (Python → Java, `stdout`)

```json
{
  "status": "sucesso",
  "receita_estimada": 851.40,
  "margem_estimada": 438.24,
  "recomendacao": [
    { "produto_id": 1, "quantidade": 66 },
    { "produto_id": 2, "quantidade": 0 }
  ],
  "consumo": [
    { "ingrediente_id": 1, "quantidade": 19.800 },
    { "ingrediente_id": 2, "quantidade": 1.980 },
    { "ingrediente_id": 7, "quantidade": 66.000 }
  ]
}
```

(É o exemplo guardado em `optimization/exemplos/entrada_contrato.json` e `saida_contrato.json`, conferido por teste.)

- **O modelo maximiza o lucro** (`margem_estimada` = receita − custo dos ingredientes), não a receita. Mudança da Sprint 3, comunicada ao professor (F9).
- `recomendacao` traz **um item por produto recebido**; `quantidade` é um **inteiro ≥ 0** (número de copos).
- `consumo` traz **só** os ingredientes com consumo maior que zero. Com estoque zerado, vem `[]`.
- **"Estoque insuficiente para produzir qualquer produto" é `sucesso`**, com todas as quantidades 0 e os valores 0; o frontend mostra uma mensagem amigável. O `erro` fica para problemas de dados (§1.3).
- `receita_estimada` e `margem_estimada` são **informativas**: o Java recalcula as duas com `BigDecimal` (§1.4).

### 1.3 Resposta de produção — erro (código de saída 0)

```json
{
  "status": "erro",
  "mensagem": "Nenhum produto ativo para otimização"
}
```

| Situação | `mensagem` |
|---|---|
| Lista de produtos vazia ou ausente | `Nenhum produto ativo para otimização` |
| Lista de ingredientes ausente | `Lista de ingredientes ausente ou inválida` |
| Estoque, custo ou id inválido | `Números inválidos em ingredientes` / `Ingrediente sem custo unitário válido` |
| Produto sem composição | `Produto <id> sem composição` |
| Composição cita ingrediente fora da lista | `Composição cita ingrediente ausente da lista` |
| Quantidade, preço ou casas inválidos | `Números inválidos em composição` / `Números inválidos em produtos` |
| JSON ilegível | `JSON de entrada inválido` |

Como o Java trata cada caso (UC11):
- `status: "erro"` → **falha de negócio ou de dados**: `422 {"mensagem": "<mensagem do Python>"}`.
- Código de saída ≠ 0, timeout ou JSON ilegível na saída → **falha técnica**: `500 {"mensagem": "Não foi possível concluir a operação."}`, com o detalhe só no log.

### 1.4 Decisões que eram pendências

- **Precisão decimal:** o Java **recalcula `receita_estimada` e `margem_estimada` com `BigDecimal`** a partir das quantidades inteiras de `recomendacao`, dos preços de venda e dos custos; os valores devolvidos pelo Python servem só como conferência. Testado na Sprint 3 contra um cálculo manual.
- **Timeout:** 10 segundos, medido (§1).

### 1.5 Ponte com o roteiro de Hello World (modo eco)

O mesmo envelope é usado com um payload trivial, só para validar que o Java escreve, o Python lê/processa/responde e o Java lê de volta, sem acoplar o teste de infraestrutura à lógica do Gurobi:

```json
// stdin (Java → Python)
{ "echo": "Açaí (polpa)" }
// stdout (Python → Java)
{ "status": "sucesso", "echo": "AÇAÍ (POLPA)" }
```

Se `echo` não for texto, a resposta é `{"status": "erro", "mensagem": "Campo 'echo' ausente ou inválido"}`.

O endpoint temporário `GET /api/teste/python` (o Java lê o nome do primeiro ingrediente no banco, envia ao Python e devolve a resposta) existe **só no perfil `dev`** do Spring (decisão F4). Fora dele a rota não existe e responde `401`; por isso ele não precisa ser apagado antes do deploy.

### 1.6 Licença do Gurobi e execução

- **Licenças disponíveis** (A34, 27/10/2026): uma **WLS acadêmica** do Kauê, válida até 25/09/2027, de uso **pessoal** ("uma única pessoa específica"), para trabalhos de disciplina; e a **licença restrita** que vem com o `gurobipy` 13.0.3 instalado pelo `pip` (decisão P2), usada no desenvolvimento e nos testes. Não há Gurobi Compute Server: ele não é necessário, porque a WLS roda dentro do próprio processo Python.
- **No sistema publicado (Render), o solver só roda com autorização expressa da Gurobi** (orientação do professor em 27/10/2026; decisão A30), com qualquer das duas licenças. Sem ela, vale o **plano B1** (A31, decidido em 27/10): no Render, `OTIMIZADOR_HABILITADO=false`, e as rotas de produção e de reposição respondem `503 {"mensagem":"A recomendação está disponível só na demonstração local da equipe."}` **sem chamar o Python**. A otimização é demonstrada pelo Kauê, no computador dele, com a WLS dele. Localmente, a variável fica ligada (padrão `true`).
- O Java repassa ao processo filho as variáveis `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID` **se existirem**. Com a autorização da Gurobi, o titular (Kauê) cria essas variáveis no Render: **o contrato JSON não muda**. Os valores nunca vão para o Git.
- *Histórico:* as notas de 28/09, 02/10 e 08/10/2026 sobre Compute Server local e "Alternativa C" (WLS do Lucas no container) foram revistas: não há Compute Server; a WLS acadêmica que existe é a do Kauê (A34), e o uso dela no Render depende da Gurobi (A30); ver a seção 3.

### 1.7 Reposição (UC14/US14) — `optimization/app/reposicao.py`

Mesmas regras gerais do §1 (um JSON de ida, um de volta, código de saída 0 com `status`, UTF-8, aceita BOM, uma execução por vez).

**Requisição (Java → Python).** O Java já escolheu os ingredientes (os 8 mais consumidos para períodos até 30 dias; os 15 mais consumidos acima disso) e envia o **saldo válido**:

```json
{ "orcamento": 100.00,
  "ingredientes": [ { "id": 5, "consumo_periodo": 0.720, "quantidade_estoque": 1.980,
                      "quantidade_minima": 2.000, "custo_unitario": 15.00 } ] }
```

Casas: `orcamento` e `custo_unitario` com até 2; quantidades com até 3. `id` não pode repetir.

**Resposta de sucesso** (exemplo guardado em `optimization/exemplos/saida_reposicao_demo.json`):

```json
{ "status": "sucesso", "orcamento_suficiente": true, "custo_total": 11.10, "valor_faltante": 0.0,
  "cobertura_minima": 1.0,
  "itens": [ { "ingrediente_id": 5, "quantidade_sugerida": 0.74, "quantidade_minima_compra": 0.02,
               "quantidade_necessaria": 0.74, "custo_estimado": 11.10 } ] }
```

- `itens` traz **um item por ingrediente recebido**, do maior para o menor `custo_estimado` (empate: menor id).
- `quantidade_sugerida` tem até **2 casas** e fica entre `quantidade_minima_compra` e `quantidade_necessaria`.
- **Orçamento que não cobre os mínimos:** `orcamento_suficiente: false`, `quantidade_sugerida` = o mínimo de cada um, `valor_faltante` = quanto falta, `cobertura_minima: null`.
- `cobertura_minima` (0 a 1) é informativa e **não aparece para o dono** (termo técnico).

**Erros** (`status: "erro"`): `Orçamento inválido`, `Nenhum ingrediente consumido no período`, `Números inválidos em ingredientes`, `Ingrediente sem custo unitário válido`, `JSON de entrada inválido`.

---

## 2. Contrato Frontend ↔ Backend (REST API)

**Base URL:** `http://localhost:8080` (local). As rotas abaixo **já começam com `/api`**. Produção: a URL do serviço no Render, definida no deploy (o banco gratuito é criado em 31/10/2026, à noite, e expira 30 dias depois; o sistema fica disponível até 30/11/2026, data-limite da avaliação, com backup testado; a data fica no README). O frontend guarda a base em um único lugar, `frontend/js/config.js`: no `github.io` ele usa a URL do Render com tempo limite de 90 s (o serviço gratuito "dorme" após 15 min sem uso); em qualquer outro endereço, `http://localhost:8080` com 15 s.

**Autenticação:** JWT no header `Authorization: Bearer <token>`, obtido em `POST /api/auth/login`. Todas as rotas exigem token válido, **exceto** `POST /api/auth/login`, `POST /api/auth/registro` (só enquanto não existir Administrador) e `GET /api/saude`. O token vale **2 horas**; token expirado = `401`. O `subject` do token é o id do usuário: **quem fez a ação** (pedido, movimentação) vem do token, **nunca** do corpo.

**CORS:** o backend libera apenas as origens de `CORS_ALLOWED_ORIGIN`. No desenvolvimento: `http://127.0.0.1:5500,http://localhost:5500` (Live Server). Em produção: a origem do GitHub Pages (ex.: `https://kauencarneiro.github.io`, sem barra e sem caminho). Métodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS` (o `PATCH` entra na v2, para a US26); headers `Authorization` e `Content-Type`. Uma origem fora da lista recebe `403` no *preflight*.

**Convenções do JSON:**

- Nomes de campo em **snake_case**, iguais ao DER (`unidade_medida`, `custo_unitario`), aplicados por **configuração global** do Spring (`spring.jackson.property-naming-strategy=SNAKE_CASE`, decisão A23). *(item B6 — confirmado pela equipe)*
- Valores monetários com 2 casas decimais e quantidades com até 3; datas em ISO-8601 (`2026-09-20T14:30:00`) no fuso **`America/Sao_Paulo`** (o Java define a hora, e não o banco, decisão A9); codificação UTF-8.

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

`campos` é **opcional**: aparece nos erros de validação ou de conflito de campo, e pode trazer **vários campos** de uma vez (US7). Erros que não pertencem a um campo (ex.: estoque insuficiente) trazem só `mensagem`. **As mensagens por campo vêm do `docs/Apoio/mapa_constraints.md`**, para serem iguais em todo o sistema. Todas as respostas de erro usam `Content-Type: application/json`, **inclusive `401` e `403`**.

**Códigos HTTP:**

| Código | Quando |
|---|---|
| `200` / `201` | Sucesso |
| `400` | Validação de campo ou de regra de cadastro (inclui tudo que o banco rejeita por `NOT NULL`, `CHECK` e `FK`, o item repetido na composição e o produto sem açaí); JSON malformado; valor de rota ou parâmetro com tipo errado |
| `401` | Sem token, token inválido/expirado, ou credenciais inválidas no login |
| `403` | Perfil sem permissão (ex.: Funcionário tentando alterar preço) |
| `404` | Recurso inexistente |
| `405` | Método não suportado na rota |
| `409` | Conflito de unicidade: e-mail já cadastrado e cadastro de Administrador quando já existe um |
| `422` | Regra de negócio de uma operação com dados válidos (ex.: estoque insuficiente) ou `status: "erro"` vindo do Python |
| `500` | Falha técnica (sem *stack trace* na resposta) |
| `503` | Otimização desligada no servidor (`OTIMIZADOR_HABILITADO=false`, plano B1 da licença, A31) |

**Mensagens padrão** (v2):

| Situação | HTTP | Corpo |
|---|---|---|
| Sem token, token inválido ou expirado (qualquer rota protegida, **inclusive rota inexistente**) | 401 | `{"mensagem":"Sua sessão expirou ou você não está autenticado."}` |
| Login com e-mail inexistente, senha errada ou usuário inativo | 401 | `{"mensagem":"E-mail ou senha inválidos."}` |
| Perfil sem permissão | 403 | `{"mensagem":"Você não tem permissão para esta ação."}` |
| JSON malformado, valor de rota com tipo errado (`/api/produtos/abc/preco`), parâmetro obrigatório ausente | 400 | `{"mensagem":"Requisição inválida."}` |
| Rota inexistente (com token válido) | 404 | `{"mensagem":"Recurso não encontrado."}` |
| Recurso pelo id inexistente (`PUT /api/ingredientes/{id}`, `PUT /api/produtos/{id}/preco`, `GET /api/estoque/{id}/linhas`) | 404 | `{"mensagem":"Ingrediente não encontrado."}` / `{"mensagem":"Produto não encontrado."}` |
| Método não suportado | 405 | `{"mensagem":"Método não permitido."}` |
| Produção ou reposição com a otimização desligada no servidor (A31) | 503 | `{"mensagem":"A recomendação está disponível só na demonstração local da equipe."}` |
| Erro inesperado | 500 | `{"mensagem":"Não foi possível concluir a operação."}`, sem *stack trace* nem detalhe interno |

O frontend (`frontend/js/api.js`) trata assim: no `401` com sessão, chama `logout` e mostra "Sua sessão expirou. Entre novamente."; no `403`, preserva a sessão.

### 2.1 Exemplos detalhados (padrão a repetir nos demais endpoints)

**`POST /api/auth/login`** — UC3, público

```json
// request
{ "email": "dono@acai.com", "senha": "senha1234" }
// response 200
{ "token": "eyJhbGciOi...", "perfil": "ADMINISTRADOR", "nome": "Dono da Açaiteria" }
```
O **`nome` é obrigatório** na resposta, porque o menu o mostra. Erro (US3): `401 { "mensagem": "E-mail ou senha inválidos." }`. Um usuário inativo (`ativo = false`) recebe a **mesma** resposta, para não revelar quais e-mails existem.

**`POST /api/auth/registro`** — UC1, público (**só enquanto não existir Administrador**)

```json
// request
{ "nome": "Dono da Açaiteria", "email": "dono@acai.com", "senha": "senha1234" }
// response 201
{ "id": 1, "nome": "Dono da Açaiteria", "email": "dono@acai.com", "perfil": "ADMINISTRADOR" }
```
Erros: campos inválidos → `400` com todos de uma vez: `campos.nome` "Informe o nome."; `campos.email` "Informe o e-mail." / "Informe um e-mail válido."; `campos.senha` "Informe a senha." / "A senha deve ter no mínimo 8 caracteres." (CT02). E-mail duplicado (CT03) → `409 { "mensagem": "Dados inválidos", "campos": { "email": "Este e-mail já está cadastrado." } }`; **Administrador já existe** → `409 { "mensagem": "O cadastro do Administrador já foi realizado." }`. *(item B5 — confirmado por Lucas em 20/09/2026)*

**`GET /api/usuarios`** e **`POST /api/usuarios`** — UC2, Admin

`GET` responde `[{id, nome, email, ativo}]`. `POST` recebe `{nome, email, senha}`, com as mesmas mensagens de campo do registro, e responde `201 {id, nome, email, perfil: "FUNCIONARIO"}`. O `senha_hash` nunca sai.

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
As unidades chegam ao banco pelo seed. Cadastrar uma unidade (`POST`) e alterar a porção padrão (`PATCH`) é a US26/UC20, fora da Sprint 3.

**`GET /api/ingredientes`** — Admin/Funcionário: o **registro completo**, `[{id, nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque, quantidade_minima}]`, ordenado por `nome`. (O `IngredienteResumo` de 3 campos da Sprint 1 deixa de existir: com ele, a tela de edição recusava editar.)

**`POST /api/ingredientes`** — UC4, Admin (o `PUT /api/ingredientes/{id}` usa o mesmo corpo)

```json
// request
{ "nome": "Açaí (polpa)", "unidade_medida": "kg", "custo_unitario": 18.50, "porcao_padrao": 0.150, "quantidade_minima": 10.000 }
// response 201
{ "id": 1, "nome": "Açaí (polpa)", "unidade_medida": "kg", "custo_unitario": 18.50, "porcao_padrao": 0.150, "quantidade_estoque": 0.000, "quantidade_minima": 10.000 }
```
Erro (unidade não cadastrada — FK `fk_ingrediente_unidade_medida`): `400 { "mensagem": "Dados inválidos", "campos": { "unidade_medida": "Selecione uma unidade de medida cadastrada." } }`. O estoque **não** muda por aqui: só por movimentação (§2.4) e pedido (§2.5).

**`GET /api/produtos`** — Admin/Funcionário: `[{id, nome, preco_venda, ativo, composicao: [{ingrediente_id, ingrediente_nome, quantidade_utilizada, unidade_medida}]}]`.

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
`quantidade_utilizada` está **na unidade do ingrediente** (kg, l, un). Produto e composição são gravados em **uma única transação**. *(quem converte porções em quantidade — item B1, confirmado em 25/09/2026: o frontend)*

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
Erros: Funcionário (CT05) → `403 { "mensagem": "Você não tem permissão para esta ação." }`; preço zero (CT17) → `400` com `"preco_venda": "O preço deve ser maior que zero."`; produto inexistente → `404 { "mensagem": "Produto não encontrado." }`.

**`GET /api/saude`** — público (usado no Roteiro de Hello World e como *health check* no deploy do Render)

```json
// response 200
{ "status": "ok", "banco": "ok" }
```
Se o banco estiver fora, a resposta é `500`.

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
| Produtos | GET | `/api/produtos` | Admin/Funcionário | UC5 | 1 |
| Produtos | POST | `/api/produtos` | Admin | UC5 | 1 |
| Produtos | PUT | `/api/produtos/{id}/preco` | Admin | UC6 | 1 |
| Estoque | GET | `/api/estoque` | Admin/Funcionário | UC9 | 3 |
| Estoque | GET | `/api/estoque/{ingrediente_id}/linhas` | Admin/Funcionário | UC9 | 3 |
| Estoque | POST | `/api/estoque/movimentacoes` | Admin/Funcionário | UC9 | 3 |
| Estoque | POST | `/api/estoque/linhas/{linha_id}/descarte` | Admin/Funcionário | UC9 (opcional, A12) | 3 |
| Pedidos | POST | `/api/pedidos` | Admin/Funcionário | UC12 | 3 |
| Pedidos | GET | `/api/pedidos` | Admin/Funcionário | — | 3 |
| Produção | POST | `/api/producao/recomendacao` | Admin | UC10/UC11 | 3 |
| Reposição | **POST** | `/api/reposicao/recomendacao` | Admin | UC14 | 3 |
| Dashboard | GET | `/api/dashboard` | Admin | UC17 | 3 |
| Marketing | GET | `/api/marketing/sugestoes` | Admin | UC18 | 3 |
| Teste (eco) | GET | `/api/teste/python` | **Só no perfil `dev`** | — | 1 (Hello World) |

**Fora da Sprint 3** (decisão A2b; os formatos propostos estão no roteiro v4.6, em `docs/Sprint_3/historico/`):

| Recurso | Método | Rota | UC/US |
|---|---|---|---|
| Clientes | GET, POST | `/api/clientes` | UC13 |
| Unidades de medida (cadastrar; alterar a porção padrão) | POST, PATCH | `/api/unidades-medida` e `/api/unidades-medida/{unidade_medida}` | UC20/US26 |
| Estoque | GET | `/api/estoque/alertas-reposicao` | UC15 |
| Estoque | GET | `/api/estoque/alertas-validade` | UC16 |
| Dashboard | GET | `/api/dashboard/export-csv` | UC19 |
| Pedidos (cancelar; **não devolve estoque**) | — | a definir | US27 |

**Rotas opcionais — telas "Editar" do protótipo** (o SQL já existe em `docs/Apoio/sql_repositorios.md`, mas as histórias US1–US7 não as exigem). Fora — decisão confirmada pela equipe (item B3, registro em 08/10/2026); só entram em uma sprint posterior se a equipe decidir:

| Método | Rota | Tela |
|---|---|---|
| GET | `/api/ingredientes/{id}` | 07 — Ingredientes: Editar |
| GET | `/api/produtos/{id}` | 09 — Produtos: Editar |
| PUT | `/api/produtos/{id}` | 09 — Produtos: Editar (nome, situação e composição) |
| PUT | `/api/usuarios/{id}` | 05 — Funcionários: Editar |

### 2.3 Sem paginação no MVP

Paginação em `GET /api/pedidos` e `GET /api/ingredientes` fica **fora do MVP** (o volume da açaiteria não a exige).

### 2.4 Estoque (UC9/US9) — por linhas de entrada

O estoque é controlado **por linha**: cada `ENTRADA` é uma linha com `quantidade_restante` e `data_validade` opcional. A baixa consome as linhas **válidas** da menor validade para a maior (linhas sem validade por último); **linha vencida nunca é vendida**. O saldo do ingrediente é sempre a soma dos restantes.

**`GET /api/estoque`** (Admin e Funcionário): um item por ingrediente, ordenado por `nome`.

```json
[ { "ingrediente_id": 3, "nome": "Granola", "unidade_medida": "kg", "quantidade_estoque": 11.160,
    "quantidade_minima": 2.000, "quantidade_valida": 5.160, "quantidade_vencida": 6.000,
    "validade_mais_proxima": "2026-12-10" } ]
```

- `quantidade_estoque` é o saldo **total** (inclui o vencido até o descarte). `quantidade_valida` é o que pode ser usado. `validade_mais_proxima` considera só as linhas válidas com restante maior que 0 (`null` se não houver).
- A situação "abaixo do mínimo" (`quantidade_valida ≤ quantidade_minima`) é **calculada pelo frontend** a partir desses números (proposta adotada na aprovação de 27/10/2026). O alerta de reposição oficial é a US15, fora da Sprint 3.

**`GET /api/estoque/{ingrediente_id}/linhas`** (Admin e Funcionário): uma linha por `ENTRADA`.

```json
[ { "id": 12, "data_movimentacao": "2026-10-20T09:15:00", "quantidade": 6.000, "quantidade_restante": 6.000,
    "data_validade": "2026-10-24", "situacao": "VENCIDA" } ]
```

`situacao` assume `VENCIDA` (validade < hoje), `PROXIMA` (até 7 dias), `OK` ou `SEM_VALIDADE`. Uma linha que vence hoje ainda vale. Ingrediente inexistente → `404 {"mensagem":"Ingrediente não encontrado."}`.

**`POST /api/estoque/movimentacoes`** (Admin e Funcionário). O `usuario_id` vem **do token**, nunca do corpo.

```json
// entrada
{ "ingrediente_id": 1, "tipo": "ENTRADA", "quantidade": 10.000, "data_validade": "2026-11-30", "observacao": "Lote 3" }
// saída
{ "ingrediente_id": 1, "tipo": "SAIDA", "motivo": "CONVENCIONAL", "quantidade": 0.500, "observacao": "Quebra" }
// resposta 201 (na entrada, "motivo" vem null e "data_validade" vem preenchida; na saída, o contrário)
{ "id": 25, "ingrediente_id": 1, "tipo": "SAIDA", "motivo": "CONVENCIONAL", "quantidade": 0.500,
  "data_validade": null, "data_movimentacao": "2026-10-28T14:02:00", "quantidade_estoque": 45.250 }
```

Mensagens por campo (todas `400`, `{"mensagem":"Dados inválidos","campos":{…}}`): `ingrediente_id` "Ingrediente não encontrado."; `tipo` "Tipo de movimentação inválido."; `quantidade` "A quantidade deve ser maior que zero."; `motivo` "Informe o motivo da saída." / "Motivo de saída inválido." / "O motivo só pode ser informado em saídas."; `data_validade` "A validade só pode ser informada em entradas.".

Regras:
- `motivo` é obrigatório na `SAIDA`, com os valores `CONVENCIONAL` ou `DESCARTE_VENCIDO`. O motivo `PEDIDO` só o backend grava; vindo do cliente, é `400`.
- `motivo` na `ENTRADA` → `400`. `data_validade` na `SAIDA` → `400` (constraint `chk_movimentacao_data_validade`). `quantidade ≤ 0` → `400`.
- A saída convencional consome só linhas **válidas**, da menor validade para a maior. O descarte (`DESCARTE_VENCIDO`) consome só linhas **vencidas**.
- Quantidade maior que o disponível aplicável → `422 {"mensagem":"Estoque insuficiente para o ingrediente <nome>"}`, e nada é gravado.
- Uma `ENTRADA` com validade já vencida **é aceita**: a linha nasce vencida e aparece para descarte (proposta adotada na aprovação de 27/10/2026).

**`POST /api/estoque/linhas/{linha_id}/descarte`** (Admin e Funcionário; **opcional**, decisão A12 de 27/10/2026). Sem corpo. Descarta **tudo o que resta** de **uma** linha vencida: grava a mesma `SAIDA` com motivo `DESCARTE_VENCIDO` que a movimentação gravaria, com o usuário do token e a observação "Descarte da linha {id}".

```json
// resposta 201 (mesmo formato da movimentação)
{ "id": 31, "ingrediente_id": 1, "tipo": "SAIDA", "motivo": "DESCARTE_VENCIDO", "quantidade": 20.000,
  "data_validade": null, "data_movimentacao": "2026-10-28T15:00:00", "quantidade_estoque": 20.000 }
```

- Linha inexistente, ou id de uma `SAIDA` → `404 {"mensagem":"Linha de estoque não encontrada."}`.
- Linha não vencida (ou sem validade) → `422 {"mensagem":"Só é possível descartar linhas vencidas."}`.
- Linha já zerada → `422 {"mensagem":"Esta linha já não tem saldo."}`.
- Nos casos recusados, nada é gravado.

### 2.5 Pedidos (UC12/US12)

**`POST /api/pedidos`** — Admin/Funcionário

```json
// request
{
  "cliente_id": 1,
  "itens": [ { "produto_id": 1, "quantidade": 2 }, { "produto_id": 2, "quantidade": 1 } ]
}
// response 201
{ "id": 1, "valor_total": 44.70, "data_pedido": "2026-09-20T14:30:00" }
```

- `cliente_id` é opcional. `itens` **não pode repetir `produto_id`** (a chave primária de `pedido_produto` é `(pedido_id, produto_id)`).
- O `preco_unitario_registrado` e o `valor_total` vêm do **preço do banco**, nunca da requisição. `data_pedido` usa o relógio do Java em `America/Sao_Paulo` (A9).
- O pedido dá **baixa automática** dos ingredientes, pela composição, só nas linhas **válidas** (§2.4), tudo numa única transação.
- Erros `400` (`{"mensagem":"Dados inválidos","campos":{…}}`): produto inativo ou inexistente → `campos.itens` "Produto não encontrado."; lista vazia → "Adicione ao menos um produto."; produto repetido → "Este produto já foi adicionado ao pedido."; quantidade ≤ 0 → "A quantidade deve ser maior que zero."; cliente inexistente → `campos.cliente_id` "Cliente não encontrado.".
- Estoque válido insuficiente em qualquer item (UC12/CT25) → `422 { "mensagem": "Estoque insuficiente para o produto Açaí Especial 500ml" }`: pedido inteiro rejeitado, nenhum item gravado e nenhum estoque baixado.

**`GET /api/pedidos`** — Admin/Funcionário, do mais recente para o mais antigo.

```json
[ { "id": 6, "data_pedido": "2026-10-28T15:00:00", "cliente_id": null, "usuario_nome": "Maria",
    "valor_total": 44.70, "status": "ATIVO",
    "itens": [ { "produto_id": 1, "nome": "Açaí Tradicional 300ml", "quantidade": 2, "preco_unitario_registrado": 12.90 } ] } ]
```

O `status` (`ATIVO` ou `CANCELADO`) já existe no banco para a US27 (cancelar pedido, fora da Sprint 3), que **não devolve estoque**.

### 2.6 Recomendação de produção (UC10/UC11)

**`POST /api/producao/recomendacao`** (só Administrador; a requisição não tem corpo). *(item D3: `POST`, e não `GET`, porque é uma ação disparada pelo proprietário e inicia um processo pesado)*

```json
{ "calculado_em": "2026-10-30T20:30:00", "receita_estimada": 1966.80, "margem_estimada": 1011.71,
  "recomendacao": [ { "produto_id": 1, "nome": "Açaí Tradicional 300ml", "quantidade": 151, "lucro_por_copo": 6.64 },
                    { "produto_id": 2, "nome": "Açaí Especial 500ml",    "quantidade": 1,   "lucro_por_copo": 9.07 },
                    { "produto_id": 3, "nome": "Açaí Premium 700ml",     "quantidade": 0,   "lucro_por_copo": 10.63 } ] }
```

- Os valores do exemplo são os do estoque do `seed_demo.sql` (validados com o Gurobi real). Com o seed oficial (estoque 0), tudo vem 0: é o esperado.
- O Java calcula `lucro_por_copo` e recalcula receita e margem com `BigDecimal`.
- O frontend mostra "Sem lucro" quando `lucro_por_copo ≤ 0` e "Falta ingrediente" quando `lucro_por_copo > 0` e a quantidade é 0. A tela **não** usa termos técnicos.
- Erros: `status: "erro"` do Python → `422`; falha técnica ou timeout → `500`; Funcionário → `403`; otimização desligada no servidor → `503` (A31).
- A última recomendação gerada fica guardada na memória do backend para o marketing (§2.9) e o dashboard (§2.8). Ela some se o backend reiniciar ou "dormir" (plano free).

### 2.7 Reposição (UC14/US14) — **`POST`** (antes `GET`; decisão de 14/10/2026)

**`POST /api/reposicao/recomendacao`** (só Administrador). Requisição: `{ "periodo_dias": 30, "orcamento": 100.00 }`.

```json
{ "calculado_em": "2026-10-28T15:00:00", "periodo_dias": 30, "orcamento": 100.00,
  "orcamento_suficiente": true, "custo_total": 63.22, "valor_faltante": 0.00, "cobertura_minima": 1.0,
  "mensagem": null,
  "itens": [ { "ingrediente_id": 3, "nome": "Granola", "unidade_medida": "kg", "consumo_periodo": 0.080,
               "quantidade_estoque": 0.920, "quantidade_minima": 2.000, "custo_unitario": 22.00,
               "quantidade_sugerida": 1.16, "custo_estimado": 25.52 } ] }
```

- Período = **os últimos N dias, contando hoje** (São Paulo). Até 30 dias entram os **8** ingredientes mais consumidos; acima, os **15**. Só entram ingredientes **com consumo**.
- Consumo = pedidos × composição, **inclusive pedidos cancelados** (A18). `quantidade_estoque` = saldo **válido**.
- `itens` vem do maior para o menor `custo_estimado`.
- **Sem pedidos no período:** `200`, `itens: []` e `mensagem: "Não houve pedidos no período escolhido, então não há consumo para repor."` (o Python **não** é chamado).
- **Orçamento insuficiente:** `orcamento_suficiente: false`, `valor_faltante > 0`, `mensagem: "O orçamento não cobre nem os mínimos. A tabela mostra os mínimos e quanto falta."`.
- Erros: `400` com `campos.periodo_dias` ("Informe um período entre 1 e 365 dias.") e/ou `campos.orcamento` ("Informe o orçamento." / "O orçamento não pode ser negativo." / "Use no máximo 2 casas decimais no orçamento."); `422` com a mensagem do Python; `500` em falha técnica; Funcionário → `403`; otimização desligada no servidor → `503` (A31), depois da validação dos campos e só quando há consumo (sem pedidos no período, a resposta continua `200`).

### 2.8 Dashboard (UC17/US17) — Tela 03

**`GET /api/dashboard`** (só Administrador). Parâmetros, todos opcionais: `mes` (`AAAA-MM`; padrão: o mês atual em São Paulo), `usuario_id`, `produto_id` (pode repetir: `produto_id=1&produto_id=3`), `valor_min`, `valor_max`.

```json
{ "mes": "2026-10",
  "faturamento": { "valor_total": 326.40, "quantidade_pedidos": 3 },
  "mais_vendidos": [ { "produto_id": 3, "nome": "Açaí Premium 700ml", "quantidade": 7, "valor_total": 174.30 } ],
  "ingredientes_mais_utilizados": [ { "ingrediente_id": 1, "nome": "Açaí (polpa)", "unidade_medida": "kg",
                                      "quantidade": 7.800, "custo_estimado": 144.30 } ],
  "ranking": [ { "rank": 1, "usuario_id": 2, "usuario": "Func", "quantidade_pedidos": 2, "valor_total": 213.00,
                 "tipos_de_produto": "Açaí Premium 700ml, Açaí Tradicional 300ml" } ],
  "ultimo_plano": { "calculado_em": "2026-10-28T15:00:00", "receita_estimada": 1966.80, "margem_estimada": 1011.71,
                    "produtos_priorizados": [ "Açaí Tradicional 300ml", "Açaí Especial 500ml" ] },
  "estoque": { "ingredientes_abaixo_do_minimo": 1, "ingredientes_com_vencido": 1 },
  "otimizador_habilitado": true }
```

- Só pedidos `ATIVO` (cancelado não entra em faturamento, mais vendidos, ingredientes nem ranking).
- `ingredientes_mais_utilizados` vem ordenado pelo **custo** do consumo (A17). O copo descartável aparece na lista, porque no banco ele é um ingrediente.
- `ranking`: posição pelo valor total (desempate: quantidade de pedidos). O filtro de produto restringe também a quantidade e o valor; o filtro de valor vale **depois** de somar.
- `ultimo_plano` traz a **receita máxima estimada** e os **produtos prioritários** que o Termo de Aceite exige, lidos do último cálculo de produção (A16). É `null` se ninguém gerou a recomendação desde que o backend subiu. `estoque` usa o saldo **válido**. `otimizador_habilitado` é `false` quando a otimização está desligada no servidor (A31): a Tela 03 explica que o plano só existe na demonstração local.
- Erros: `400` com `campos.mes` ("Informe o mês no formato AAAA-MM.") ou `campos.valor_min` ("O valor mínimo não pode ser maior que o máximo."); Funcionário → `403`.

### 2.9 Marketing (UC18/US18) — Tela 14

**`GET /api/marketing/sugestoes`** (só Administrador; sem parâmetros).

```json
{ "calculado_em": "2026-10-28T15:00:00", "mensagem": null,
  "sugestoes": [ { "produto_id": 1, "produto": "Açaí Tradicional 300ml", "quantidade_recomendada": 151,
                   "lucro_por_copo": 6.64, "selo": "Produto priorizado", "ideia": "Foto bonita",
                   "como_fazer": "Monte o Açaí Tradicional 300ml em camadas coloridas e tire uma foto para postar. Copo bonito faz o cliente postar também." } ] }
```

- Priorizados: quantidade recomendada > 0, maior primeiro (empate: menor id), **até 3**. As ideias vêm de um catálogo de 18, em rodízio pelo dia do cálculo, sem repetir no mesmo resultado (A20).
- Sem cálculo: `calculado_em: null`, `sugestoes: []`, `mensagem: "Gere a recomendação na tela Produção para ver as sugestões."`. Com a otimização desligada no servidor (A31): `mensagem: "As sugestões dependem da recomendação de produção, que neste endereço está disponível só na demonstração local da equipe."`.
- Cálculo sem nenhum produto a produzir: `sugestoes: []` e `mensagem: "A última recomendação não indicou nenhum produto para produzir. Confira o estoque e gere a recomendação de novo."`.
- Nada é publicado automaticamente (critério da US18).

### 2.10 Itens adotados — situação

| Item | O que foi adotado | Situação |
|---|---|---|
| B1 | O frontend converte porções em quantidade | **Confirmado** pelo grupo em 25/09/2026 |
| B3 | Rotas das telas "Editar" fora | **Confirmado pela equipe** (registro em 08/10/2026) |
| B5 | Cadastro de Administrador só no primeiro acesso; depois, `409` (é a opção 2 do D-A) | **Confirmado por Lucas** em 20/09/2026 |
| B6 | JSON em snake_case. Na v2, por configuração global do Spring (A23), em vez de `@JsonProperty` campo a campo | **Confirmado por Lucas e Leonardo** (registro em 08/10/2026); forma atualizada em 27/10/2026 |
| D3 | `POST` (e não `GET`) em `/api/producao/recomendacao` | **Confirmado por Lucas** (registro em 08/10/2026) |
| v2 | Situação "abaixo do mínimo" calculada pelo frontend; `ENTRADA` com validade vencida aceita (§2.4) | Adotados na aprovação da v2 (27/10/2026) |

---

## 3. Histórico de revisões

| Data | Alteração | Itens |
|---|---|---|
| 27/10/2026 (noite, 2) | **Plano B1 da licença (A31):** `OTIMIZADOR_HABILITADO`; código `503` nas rotas de produção e reposição; `otimizador_habilitado` no dashboard; mensagem própria no marketing | A31 |
| 27/10/2026 (noite) | **§1.6:** solver no Render só com autorização expressa da Gurobi (resposta do professor; A30, A31); licenças corrigidas: existe a WLS acadêmica do Kauê, válida até 25/09/2027, de uso pessoal (A34). **§2:** disponibilidade até 30/11/2026, com backup (A32) | A30–A32, A34 |
| 27/10/2026 | **Versão 2 (Sprint 3)**, aprovada pela equipe; aplicada a pedido do grupo, com autorização do Kauê. **§1:** modelo por **lucro** (`custo_unitario` na entrada; `margem_estimada` e `consumo` na saída); saldo **válido** por linha; tabela de mensagens de erro; BOM aceito; uma execução por vez; eco só no perfil `dev`; **§1.6** licença de avaliação do Gurobi 13.0.3 (sem WLS nem Compute Server: as notas de 28/09, 02/10 e 08/10 ficam sem efeito); **§1.7** módulo de reposição. **§2:** validade do token (2 h); `PATCH` no CORS; snake_case global; fuso de São Paulo; códigos `405` e `422` do Python; mensagens padrão de `400`, `401`, `404`, `405` e `500`; formatos completos de ingredientes, produtos, usuários e login; rotas da Sprint 3 e lista do que fica fora; **§2.4** estoque por linhas e descarte por linha (A12); **§2.5** pedidos com baixa e `GET`; **§2.6** recomendação; **§2.7** reposição passa a `POST`; **§2.8** dashboard; **§2.9** marketing. **§0:** caminhos atualizados (`docs/Apoio/`, `bd/`) | A8, A9, A12, A16–A23, A27, D3, F4, F9, P2 |
| 08/10/2026 | **Itens B3, B6 e D3 confirmados pela equipe** (B5 e B1 já estavam confirmados); removidas as marcações "(confirmar com …)". **§1:** nota do Compute Server atualizada — licença de produção será a do Lucas; produção decidida pelo grupo: **Alternativa C** (licença WLS direta no container do backend, sem Compute Server; Opção A descartada); o contrato JSON não muda. Proposta para o Kauê aplicar no arquivo oficial. | B3, B6, D3, §1 |
| 28/09/2026 | §1: nota sobre o Gurobi Compute Server (dependência de rede, timeout ainda mais provisório, limite de 1 job por vez). Proposta minha para o Kauê revisar e, se concordar, aplicar no arquivo oficial. | §1 |
| 25/09/2026 | Item **B1** (§2.4) confirmado pelo grupo: o frontend converte porções em quantidade (B1). **§1.1:** ingredientes com lote vencido deixam de contar como estoque disponível enviado ao Python (decisão do grupo). | B1 |
| 20/09/2026 | Item **B5** (§2.4) confirmado pelo Lucas: cadastro de Administrador só até existir o primeiro (D-A). Caso de teste correspondente registrado como CT01B no Plano de Testes. | B5 |
| 20/09/2026 | Revisão de consistência aprovada pelo Kauê. **§0:** donos e documentos do contrato Banco↔Java. **§1:** ingredientes com estoque 0 passam a ser enviados (removido o filtro "estoque > 0"); regras de erro e de "sem estoque" (`sucesso` com quantidades 0); UTF-8, timeout de 10 s e recálculo da receita em `BigDecimal`; exemplo de Hello World com o banco real. **§2:** Base URL sem `/api`; CORS; convenções do JSON (snake_case); formato de erro com `campos`; tabela de códigos HTTP; exemplos sem dados do seed antigo; novos exemplos (`unidades-medida`, `produtos`, preço, registro, `saude`); coluna Sprint; rotas "Editar" separadas; `POST` em produção; regras de `itens` únicos e de `data_validade`. | A1–A9, B1–B6, C1–C2, D1–D4 |
