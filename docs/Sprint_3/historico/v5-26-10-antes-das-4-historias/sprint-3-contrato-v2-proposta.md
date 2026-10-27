# Contrato de Comunicação v2 — PROPOSTA para aprovação (Sprint 3)

**Status:** proposta da auditoria de 26/10/2026. **Não substitui** o contrato oficial (`docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`, mantido pelo Kauê).
**Para virar oficial:** Kauê, Lucas e Leonardo revisam (pendência A10, prazo **27/10**) e o Kauê aplica no contrato oficial, registrando na seção 3 dele ("Histórico de revisões").
**Origem de cada item:**
- roteiro v4.6, §5 e §10.2;
- os tutoriais de 14/10;
- código **executado** nesta auditoria: `referencia/python/` (25 testes) e `referencia/backend/` (18 testes MockMvc).

Os itens marcados **[decidir]** ainda precisam de escolha da equipe.

---

## 1. Java ↔ Python (substitui a §1.1 a §1.3 do contrato atual)

**O que não muda:**
- Um JSON na `stdin`, um JSON na `stdout`.
- Código de saída `0` significa "há JSON válido", **inclusive** quando o `status` é `"erro"`.
- Código diferente de `0` é falha técnica.
- Codificação UTF-8.
- Timeout provisório de 10 s.

### 1.1 Requisição (Java → Python)

Cada ingrediente ganha **`custo_unitario`**. Em `quantidade_estoque` vai o **saldo válido**, sem as linhas vencidas (consulta 3 do `consultas_sprint3.sql`).

```json
{ "ingredientes": [ { "id": 1, "quantidade_estoque": 40.000, "custo_unitario": 18.50 } ],
  "produtos": [ { "id": 1, "preco_venda": 12.90,
                  "composicao": [ { "ingrediente_id": 1, "quantidade_utilizada": 0.300 } ] } ] }
```

**Regras:**
- Entram só produtos ativos.
- Entram **todos** os ingredientes usados por eles, **inclusive os com estoque 0** (como no contrato atual).
- Casas decimais: `custo_unitario` ≥ 0, com até 2 casas; `preco_venda` > 0, com até 2; quantidades com até 3.
- Números fora do domínio resultam em `status: "erro"` com código de saída 0.
- O Java envia a entrada em UTF-8 **sem BOM**. O módulo também aceita entrada **com** BOM (decisão A8), porque o pipe do PowerShell envia um.

### 1.2 Resposta de sucesso

```json
{ "status": "sucesso", "receita_estimada": 851.40, "margem_estimada": 438.24,
  "recomendacao": [ { "produto_id": 1, "quantidade": 66 }, { "produto_id": 2, "quantidade": 0 } ],
  "consumo": [ { "ingrediente_id": 1, "quantidade": 19.800 }, { "ingrediente_id": 2, "quantidade": 1.980 },
               { "ingrediente_id": 7, "quantidade": 66.000 } ] }
```

- O modelo maximiza o **lucro** (`margem_estimada`), não a receita. Veja a decisão F9: avisar o professor antes de alterar o Termo.
- `recomendacao` traz um item por produto recebido. A quantidade é inteira e ≥ 0.
- `consumo` traz **só** os ingredientes com consumo maior que zero (F11). Com estoque zerado, vem `[]`.
- `receita_estimada` e `margem_estimada` são **informativas**: o Java recalcula as duas com `BigDecimal`.

### 1.3 Resposta de erro (código de saída 0)

`{ "status": "erro", "mensagem": "<texto>" }`. Textos que o módulo produz hoje (`referencia/python/app/otimizador.py`):

| Situação | `mensagem` |
|---|---|
| Lista de produtos vazia ou ausente | `Nenhum produto ativo para otimização` |
| Lista de ingredientes ausente | `Lista de ingredientes ausente ou inválida` |
| Estoque, custo ou id inválido | `Números inválidos em ingredientes` / `Ingrediente sem custo unitário válido` |
| Produto sem composição | `Produto <id> sem composição` |
| Composição cita ingrediente fora da lista | `Composição cita ingrediente ausente da lista` |
| Quantidade, preço ou casas inválidos | `Números inválidos em composição` / `Números inválidos em produtos` |
| JSON ilegível | `JSON de entrada inválido` |

**Como o Java trata cada caso:**
- `status: "erro"` → `422 {"mensagem": "..."}` (falha de negócio ou de dados).
- Código de saída diferente de 0, timeout ou JSON ilegível na saída → `500 {"mensagem": "Não foi possível concluir a operação."}`, com o detalhe só no log.

### 1.4 Modo eco (Hello World; mantido até a Fase 4A ser validada)

`{"echo": "Açaí (polpa)"}` → `{"status": "sucesso", "echo": "AÇAÍ (POLPA)"}`.

Se `echo` não for texto, a resposta é `{"status": "erro", "mensagem": "Campo 'echo' ausente ou inválido"}`. O endpoint `GET /api/teste/python` existe **só no perfil `dev`** (F4) e é removido antes do deploy.

---

## 2. Frontend ↔ Backend (acréscimos à §2 do contrato atual)

### 2.1 Mensagens padrão de erro

Completam a tabela de códigos HTTP do contrato. São as mensagens implementadas em `referencia/backend/`. As de 400, 401, 403, 404 e 500 foram validadas por teste; a de 405 não tem teste:

| Situação | HTTP | Corpo |
|---|---|---|
| Sem token, token inválido ou expirado (qualquer rota protegida, **inclusive rota inexistente**) | 401 | `{"mensagem":"Sua sessão expirou ou você não está autenticado."}` |
| Login com e-mail inexistente, senha errada ou usuário inativo | 401 | `{"mensagem":"E-mail ou senha inválidos."}` (já no contrato) |
| Perfil sem permissão | 403 | `{"mensagem":"Você não tem permissão para esta ação."}` (já no contrato) |
| JSON malformado, valor de rota com tipo errado (`/api/produtos/abc/preco`), parâmetro obrigatório ausente | 400 | `{"mensagem":"Requisição inválida."}` |
| Rota inexistente (com token válido) | 404 | `{"mensagem":"Recurso não encontrado."}` |
| Método não suportado | 405 | `{"mensagem":"Método não permitido."}` |
| Erro inesperado | 500 | `{"mensagem":"Não foi possível concluir a operação."}`, sem *stack trace* nem detalhe interno |

Todas as respostas de erro usam `Content-Type: application/json`. O frontend (`frontend/js/api.js`) já trata assim: no `401` com sessão, chama `logout` e mostra "Sua sessão expirou. Entre novamente."; no `403`, preserva a sessão.

### 2.2 CORS

Métodos: `GET, POST, PUT, **PATCH**, DELETE, OPTIONS` (o `PATCH` é novo; será usado na US26). Headers: `Authorization` e `Content-Type`.

Origens, em desenvolvimento: `http://127.0.0.1:5500,http://localhost:5500`. Uma origem fora da lista recebe `403` no *preflight* (validado).

### 2.3 Formatos que o frontend já consome (Sprint 1, agora obrigatórios para o backend)

- **`GET /api/ingredientes`** (Admin e Funcionário): registro completo, `[{id, nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque, quantidade_minima}]`, ordenado por `nome`. O `IngredienteResumo` atual (3 campos) **deixa de existir**: com ele, a tela de edição recusa editar.
- **`GET /api/produtos`** (Admin e Funcionário): `[{id, nome, preco_venda, ativo, composicao: [{ingrediente_id, ingrediente_nome, quantidade_utilizada, unidade_medida}]}]`.
- **`GET /api/usuarios`** (Admin): `[{id, nome, email, ativo}]`. **`POST /api/usuarios`** responde `201 {id, nome, email, perfil: "FUNCIONARIO"}`. O `senha_hash` nunca sai.
- **`POST /api/auth/login`**: `200 {token, perfil, nome}`. O **`nome` é obrigatório**, porque o menu o mostra.

### 2.4 Estoque (US9) — novo

**`GET /api/estoque`** (Admin e Funcionário): um item por ingrediente, ordenado por `nome`.

```json
[ { "ingrediente_id": 3, "nome": "Granola", "unidade_medida": "kg", "quantidade_estoque": 11.160,
    "quantidade_minima": 2.000, "quantidade_valida": 5.160, "quantidade_vencida": 6.000,
    "validade_mais_proxima": "2026-12-10" } ]
```

- `quantidade_estoque` é o saldo **total** (inclui o vencido até o descarte). `quantidade_valida` é o que pode ser usado. `validade_mais_proxima` considera só as linhas válidas com restante maior que 0 (`null` se não houver).
- **[decidir]** O campo `situacao` do roteiro (§5, Fase 3) é calculado pelo frontend a partir desses números, **ou** o backend o envia (`ABAIXO_DO_MINIMO` / `OK`). Proposta: o frontend calcula. O alerta de reposição oficial é a US15, fora da Sprint 3.

**`GET /api/estoque/{ingrediente_id}/linhas`** (Admin e Funcionário; decisão A12): uma linha por `ENTRADA`.

```json
[ { "id": 12, "data_movimentacao": "2026-10-20T09:15:00", "quantidade": 6.000, "quantidade_restante": 6.000,
    "data_validade": "2026-10-24", "situacao": "VENCIDA" } ]
```

`situacao` assume `VENCIDA` (validade < hoje), `PROXIMA` (até 7 dias), `OK` ou `SEM_VALIDADE`. "Hoje" vence hoje, mas ainda vale.

**`POST /api/estoque/movimentacoes`** (Admin e Funcionário). O `usuario_id` vem **do token**, nunca do corpo.

```json
// entrada
{ "ingrediente_id": 1, "tipo": "ENTRADA", "quantidade": 10.000, "data_validade": "2026-11-30", "observacao": "Lote 3" }
// saída
{ "ingrediente_id": 1, "tipo": "SAIDA", "motivo": "CONVENCIONAL", "quantidade": 0.500, "observacao": "Quebra" }
// resposta 201
{ "id": 25, "ingrediente_id": 1, "tipo": "SAIDA", "motivo": "CONVENCIONAL", "quantidade": 0.500,
  "data_movimentacao": "2026-10-28T14:02:00", "quantidade_estoque": 45.250 }
```

**Regras:**
- `motivo` é obrigatório na `SAIDA`, com os valores `CONVENCIONAL` ou `DESCARTE_VENCIDO`. O `PEDIDO` só o backend grava; vindo do cliente, é `400`.
- `motivo` na `ENTRADA` → `400`.
- `data_validade` na `SAIDA` → `400` `{"campos":{"data_validade":"A validade só pode ser informada em entradas."}}`.
- `quantidade ≤ 0` → `400`.
- A saída convencional consome só linhas **válidas**, da menor validade para a maior. O descarte consome só linhas **vencidas**.
- Quantidade maior que o disponível aplicável → `422 {"mensagem":"Estoque insuficiente para o ingrediente <nome>"}`, e nada é gravado.
- **[decidir — PO]** Aceitar `ENTRADA` com validade já vencida. Proposta do tutorial: aceitar (a linha nasce vencida e aparece para descarte).

### 2.5 Pedidos (US12) — complemento ao exemplo existente

**`POST /api/pedidos`** (Admin e Funcionário). A requisição é igual à do contrato atual: `cliente_id` opcional; `itens` sem `produto_id` repetido.

- A resposta **`201 {id, valor_total, data_pedido}`** usa `data_pedido` em `America/Sao_Paulo` (A9).
- Produto inativo ou inexistente → `400` em `campos.itens`.
- Estoque válido insuficiente em qualquer item → `422 {"mensagem":"Estoque insuficiente para o produto <nome>"}`, e nada é gravado (CT25).

**`GET /api/pedidos`** (Admin e Funcionário). Proposta:

```json
[ { "id": 6, "data_pedido": "2026-10-28T15:00:00", "cliente_id": null, "usuario_nome": "Maria",
    "valor_total": 44.70, "status": "ATIVO",
    "itens": [ { "produto_id": 1, "nome": "Açaí Tradicional 300ml", "quantidade": 2, "preco_unitario_registrado": 12.90 } ] } ]
```

Ordenado do mais recente para o mais antigo. O `status` já existe no banco novo (US27, que **não devolve estoque**).

### 2.6 Recomendação de produção (US10/US11) — novo formato de resposta

**`POST /api/producao/recomendacao`** (só Administrador; a requisição não tem corpo).

```json
{ "calculado_em": "2026-10-30T20:30:00", "receita_estimada": 1966.80, "margem_estimada": 1011.71,
  "recomendacao": [ { "produto_id": 1, "nome": "Açaí Tradicional 300ml", "quantidade": 151, "lucro_por_copo": 6.64 },
                    { "produto_id": 2, "nome": "Açaí Especial 500ml",    "quantidade": 1,   "lucro_por_copo": 9.07 },
                    { "produto_id": 3, "nome": "Açaí Premium 700ml",     "quantidade": 0,   "lucro_por_copo": 10.63 } ] }
```

- Valores validados para o estoque do `seed_demo.sql`.
- O Java calcula `lucro_por_copo` e recalcula receita e margem com `BigDecimal`.
- O frontend mostra "Sem lucro" quando `lucro_por_copo ≤ 0` e "Falta ingrediente" quando `lucro_por_copo > 0` e a quantidade é 0.
- Com o seed oficial (estoque 0), tudo vem 0: é o esperado.
- Erros: `status: "erro"` do Python → `422`; falha técnica ou timeout → `500`; Funcionário → `403`.

---

## 3. O que esta proposta **não** cobre (fora da Sprint 3)

Reposição com orçamento (US14), alertas (US15/US16), dashboard e CSV (US17/US19), marketing (US18), clientes (US13), porção padrão com `PATCH` (US26) e cancelamento de pedido (US27). Os formatos propostos para essas histórias estão no roteiro v4.6 (`historico/`) e voltam à pauta conforme a decisão A3.
