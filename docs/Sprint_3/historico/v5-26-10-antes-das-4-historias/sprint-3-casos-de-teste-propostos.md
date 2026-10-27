# Casos de teste propostos — Sprint 3 — Mente Fria

**Status:** PROPOSTA (auditoria de 26/10/2026). **Destino:** `docs/Plano_de_Testes_Mente_Fria.md`, seção 3, nova subseção "Sprint 3". O Diogo e o Kauê incorporam **até 31/10** (a janela de testes de 01–06/11 executa o Plano).
**Numeração:** continua a oficial (CT01–CT29).
- CT30–CT37 têm os mesmos números que o tutorial do banco de 14/10 já usava.
- Os alertas (US15/US16), que o v4.6 numerava "a partir de CT30", saíram da Sprint 3 e, quando voltarem, usam o próximo número livre.

**Sem meta de cobertura** (Plano de Testes, §2).

**Coluna "Validado em 26/10":** indica se o comportamento já foi executado na auditoria, sobre o material de `referencia/`. Isso **não** dispensa a execução sobre o código real dos integrantes.

---

## 1. Ajustes em casos existentes

| ID | Mudança | Motivo |
|---|---|---|
| CT23 | "Quantidade recomendada = 3" continua válida. Acrescentar à entrada o **`custo_unitario`** e ao resultado a **`margem_estimada`** | Contrato v2 (o módulo passa a maximizar lucro) |
| CT24 | Mantido. O resultado esperado continua válido enquanto o custo do ingrediente disputado for menor que o preço | Roteiro v4.6, §3 |
| CT29 | Reescrever "lote vencido" como "**linha** de entrada vencida (`data_validade < hoje`; **vence hoje ainda vale**)". Verificar a **entrada** enviada ao Python (saldo válido), e não só o plano | Estoque por linhas (D10). No seed de demonstração, o desconto do vencido não muda o plano |

---

## 2. Casos novos

**Formato:** objetivo · pré-condições · perfil · passos → resultado esperado. A "camada" diz onde automatizar: **BD** = SQL; **INT** = Testcontainers; **MVC** = MockMvc; **PY** = pytest; **E2E** = aceite manual ou Playwright.

### US9 — Estoque por linhas

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT30 | Entrada cria uma linha | Banco novo; ingrediente 1 com saldo 0 | Admin ou Funcionário | `POST /api/estoque/movimentacoes` `ENTRADA` de 10 com validade futura → `201`; a linha tem `quantidade_restante = 10`; o saldo passa a 10 | INT, E2E | SQL: sim (S1–S3) |
| CT31 | A baixa consome a **menor validade** primeiro; linha sem validade por último | Linhas 3 (vence em +1 dia), 4 (vence em +7) e 10 (sem validade) | Admin | Saída convencional de 6 → restantes `0/1/10`; saída de 5 → `0/0/6` | INT | SQL: sim (S3) |
| CT32 | Linha nova com validade **menor** que a de uma linha já consumida | A=20 (vence em 20/10); sai 10; entra C=20 (vence em 15/10) | Admin | Saída de 5 → sai da **C** (C=15, A=10) | INT | SQL: sim (S2) |
| CT33 | Só há linha **vencida**: saída convencional e pedido são recusados | Ingrediente só com linha vencida de 8 | Admin | Saída convencional de 1 → `422` "Estoque insuficiente…"; **nada** muda | INT, E2E | SQL: sim (S4) |
| CT34 | Descarte consome **só** linhas vencidas | Linha vencida de 8 | Admin | Descarte de 9 → `422`; descarte de 8 → `quantidade_vencida = 0`; saldo total −8 | INT, E2E | SQL: sim (S5) |
| CT35 | Saída **sem motivo** é rejeitada pela API e pelo banco | — | Admin | API: `SAIDA` sem `motivo` → `400`. Banco: `INSERT` direto → `chk_movimentacao_motivo` (T62) | MVC, BD | BD: sim (T62) |
| CT36 | Invariante `quantidade_estoque = soma dos restantes` | Após qualquer operação de CT30–CT34 e CT38 | — | Consulta 8 do `consultas_sprint3.sql` → **0 linhas** | INT, BD | SQL: sim |
| CT37 | Dois pedidos simultâneos pelo mesmo estoque | Saldo 50; dois pedidos de 30 ao mesmo tempo | Admin e Funcionário | Um `201` e um `422`; saldo final 20; sem *deadlock* (também com a ordem de bloqueio invertida) | INT | SQL: sim (30/30) |

### US12 — Pedido

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT38 | O pedido grava o preço **do banco** e baixa os ingredientes pela composição | Estoque válido suficiente; produto 1 a R$ 12,90 | Funcionário | `POST /api/pedidos` com 2× produto 1 (enviando um preço falso no corpo, que deve ser ignorado) → `201`; `valor_total = 25,80`; `preco_unitario_registrado = 12,90`; **uma** `SAIDA` por ingrediente com `motivo = PEDIDO`; açaí −0,600; leite condensado −0,060; copo −2 | INT, E2E | Não (backend inexistente) |
| CT39 | A data do pedido fica no fuso de São Paulo (A9) | `Clock` fixo em 31/10 às 22:30 (São Paulo) | Admin | Criar pedido → `data_pedido` = `2026-10-31T22:30`, e **não** 01/11 | INT | Não |
| CT40 | `GET /api/pedidos` lista os itens e o status | Pedidos do seed de demonstração | Admin | `200`; 6 pedidos; cada um com `itens[]` e `status = ATIVO` | INT, E2E | Não |

### US10 / US11 — Recomendação de produção

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT41 | O modelo maximiza o **lucro**, não a receita | Instância "maior receita ≠ maior lucro" | — | Plano `{1: 10, 2: 0}`, margem 90 (a receita máxima daria `{1: 0, 2: 10}`) | PY | **Sim** (`test_maior_receita_nao_e_maior_lucro`) |
| CT42 | Recomendação com o seed de demonstração | `seed_demo.sql` carregado | Admin | `POST /api/producao/recomendacao` → plano **151/1/0**, margem **1.011,71**, receita **1.966,80**; a entrada enviada ao Python leva a granola com **5,160** (saldo válido), e não 11,160 | INT (com dublê) + E2E | Módulo e oráculo: **sim**. Java: não |
| CT43 | A Tela 12 não usa termos técnicos e explica o zero | Recomendação com algum produto em 0 | Admin | A tela mostra "Lucro potencial" e "Receita estimada"; produto com `lucro_por_copo ≤ 0` → "Sem lucro"; com `lucro_por_copo > 0` e quantidade 0 → "Falta ingrediente"; o texto da página **não** contém "otimização", "solver", "programação linear", "variável" nem "restrição" | E2E (Playwright) | Não |
| CT44 | Erros do Python viram mensagens tratadas | Dublê devolvendo `status: "erro"`; depois, `PYTHON_CMD` inválido | Admin | `422` com mensagem amigável; com `PYTHON_CMD` inválido → `500` "Não foi possível concluir a operação." sem *stack trace* | MVC/INT | Não |
| CT45 | Funcionário não gera recomendação | — | Funcionário | `POST /api/producao/recomendacao` → `403` em JSON | MVC | Regra de rota: **sim** (o padrão `/api/producao/**` está no `SecurityConfig` validado) |

### Segurança e erros (US3, US7, infraestrutura)

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT46 | `401` em JSON sem token, com token inválido, expirado e em rota inexistente | — | Anônimo | Cada caso → `401`, `Content-Type: application/json`, `mensagem` não vazia | MVC | **Sim** (`SegurancaEErrosTest`) |
| CT47 | `400` em JSON para requisição malformada | — | Admin | JSON quebrado; `PUT /api/produtos/abc/preco`; parâmetro obrigatório ausente → `400` "Requisição inválida." | MVC | **Sim** (depois da correção A7) |
| CT48 | `500` sem detalhe interno | Endpoint que lança exceção | Admin | `500` "Não foi possível concluir a operação."; o corpo **não** contém o texto da exceção nem "Exception" | MVC | **Sim** |
| CT49 | CORS para o frontend, inclusive `PATCH` | — | — | `OPTIONS` vindo de `http://127.0.0.1:5500` com `PATCH` → `200` e `Access-Control-Allow-Origin`; origem desconhecida → `403` | MVC | **Sim** |
| CT50 | Modo eco da ponte Java ↔ Python, com acentos | Perfil `dev` | — | `GET /api/teste/python` → `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}`; **sem** o perfil `dev` → `401` | E2E + MVC | Python e regra de rota: **sim**. Java: não |

---

## 3. Execução na janela de testes (01–06/11)

- Ordem sugerida: primeiro os caminhos do Termo de Aceite (CT06, CT12, CT30, CT38 e CT42), depois os casos de erro.
- Divisão por perfil: dois integrantes testam como Administrador e dois como Funcionário.
- Cada defeito segue o fluxo: registrar → caso que reproduz → `fix/...` → PR → retestar (roteiro v4.6, §4.4, item 5).
- Para defeitos difíceis, usar a skill `diagnosing-bugs`.
