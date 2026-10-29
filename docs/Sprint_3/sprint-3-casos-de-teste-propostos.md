# Casos de teste propostos — Sprint 3 — Mente Fria

**Status:** **incorporado ao Plano de Testes em 27/10** (A37); este arquivo fica como origem dos casos (auditoria de 26/10/2026; v5.2, 27/10: CT71 do descarte por linha, A12; v5.3, 27/10 noite: CT72 do plano B1 e CT68 ajustado ao B1; 28/10: CT68 e CT72 ajustados à A38, licença do `pip` autorizada no Render). **Destino:** `docs/Plano_de_Testes_Mente_Fria.md`, seção 3, nova subseção "Sprint 3". Incorporados em 27/10 (A37). **Execução:** na Sprint 4, os testes automatizados de cada fase; depois da entrega de 06/11, a bateria completa no ambiente publicado, na **semana de mapeamento e correção de bugs** (até 13/11; A39, A43). *Antes: janela de testes de 02 a 06/11 (A36).*
**Numeração:** continua a oficial (CT01–CT29).
- CT30–CT37 têm os mesmos números que o tutorial do banco de 14/10 já usava.
- **CT51–CT70 são novos** (US14, US17, US18 e US20, que entraram na Sprint 3 pela decisão A2b).
- **CT71 é novo** (27/10): descarte de uma linha vencida pelo botão (A12). Fica na seção da US9.
- **CT72 é novo** (27/10): otimização desligada no servidor (plano B1 da licença, A31). Desde 28/10 (A38), o B1 é só **reserva**: o CT72 confere que a chave funciona. Fica na seção da US20.
- Os alertas (US15/US16) continuam fora; quando voltarem, usam o próximo número livre.

**Sem meta de cobertura** (Plano de Testes, §2).

**Coluna "Validado em 26/10":** diz se o comportamento já foi executado na auditoria sobre o material de `referencia/` e **por qual teste**. Isso **não** dispensa a execução sobre o código real dos integrantes nem a do ambiente publicado. Nomes de teste: `Classe.metodo` em `referencia/backend/src/test/java/...` ou `referencia/python/tests/`.

---

## 1. Ajustes em casos existentes

| ID | Mudança | Motivo |
|---|---|---|
| CT23 | "Quantidade recomendada = 3" continua válida. Acrescentar à entrada o **`custo_unitario`** e ao resultado a **`margem_estimada`** | Contrato v2 (o módulo passa a maximizar lucro) |
| CT24 | Mantido. O resultado esperado continua válido enquanto o custo do ingrediente disputado for menor que o preço | Roteiro v4.6, §3 |
| CT29 | Reescrever "lote vencido" como "**linha** de entrada vencida (`data_validade < hoje`; **vence hoje ainda vale**)". Verificar a **entrada** enviada ao Python (saldo válido), e não só o plano | Estoque por linhas (D10). Validado: `Fase4ProducaoTest.oPythonRecebeOSaldoValidoSemOVencidoEComCusto` |

---

## 2. Casos novos

**Formato:** objetivo · pré-condições · perfil · passos → resultado esperado. A "camada" diz onde automatizar: **BD** = SQL; **INT** = Testcontainers; **MVC** = MockMvc; **PY** = pytest; **E2E** = aceite manual ou Playwright; **DEP** = ambiente publicado.

### US9 — Estoque por linhas

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT30 | Entrada cria uma linha | Banco novo; ingrediente 1 com saldo 0 | Admin ou Funcionário | `POST /api/estoque/movimentacoes` `ENTRADA` de 10 com validade futura → `201`; a linha tem `quantidade_restante = 10`; o saldo passa a 10 | INT, E2E | **Sim** (SQL e `Fase3EstoquePedidoTest.entradaSomaAoSaldoEApareceComoValida`) |
| CT31 | A baixa consome a **menor validade** primeiro; linha sem validade por último | Linhas 3 (vence em +1 dia), 4 (vence em +5) e 10 (sem validade) | Admin | Saída convencional de 6 → restantes `0/1/10`; saída de 5 → `0/0/6` | INT | **Sim** (SQL S3 e `…s3BaixaAtravessaLinhas…`) |
| CT32 | Linha nova com validade **menor** que a de uma linha já consumida | A=20 (vence em +20); sai 10; entra C=20 (vence em +15) | Admin | Saída de 5 → sai da **C** (C=15, A=10) | INT | **Sim** (SQL S2 e `…s2ASaidaSaiDaMenorValidade…`) |
| CT33 | Só há linha **vencida**: saída convencional e pedido são recusados | Ingrediente só com linha vencida | Admin | Saída convencional ou pedido → `422` "Estoque insuficiente…"; **nada** muda | INT, E2E | **Sim** (SQL S4 e `…s4SoHaVencidoEntaoPedidoDa422…`) |
| CT34 | Descarte consome **só** linhas vencidas | Linha vencida de 8 | Admin | Descarte de 9 → `422`; descarte de 8 → `quantidade_vencida = 0`; saldo total −8 | INT, E2E | **Sim** (SQL S5 e `…s5DescarteMaiorQueOVencido…`, `…s1VencidoNaoEntraNaVenda…`) |
| CT35 | Saída **sem motivo** é rejeitada pela API e pelo banco | — | Admin | API: `SAIDA` sem `motivo` → `400`. Banco: `INSERT` direto → `chk_movimentacao_motivo` (T62) | INT, BD | **Sim** (`…regrasDeValidacaoDaMovimentacao`; T62) |
| CT36 | Invariante `quantidade_estoque = soma dos restantes` | Após qualquer operação de CT30–CT34 e CT38 | — | Consulta 8 do `consultas_sprint3.sql` → **0 linhas** | INT, BD | **Sim** (SQL e nos testes de integração) |
| CT37 | Dois pedidos simultâneos pelo mesmo estoque | Saldo 50; dois pedidos de 30 ao mesmo tempo | Admin e Funcionário | Um `201` e um `422`; saldo final 20; sem *deadlock* | INT | **Sim** (SQL 30/30; Java 5/5 em `…doisPedidosSimultaneos…`) |
| CT71 | Botão "Descartar" zera **só** a linha vencida escolhida (A12, opcional) | Açaí com uma linha vencida de 20 e uma válida de 20 | Funcionário (e Admin) | `POST /api/estoque/linhas/{id da vencida}/descarte`, sem corpo → `201`, `SAIDA`/`DESCARTE_VENCIDO` de 20, saldo 20, a válida intacta, o usuário vem do token. De novo na mesma linha → `422` "Esta linha já não tem saldo."; na linha válida → `422` "Só é possível descartar linhas vencidas."; linha inexistente ou id de uma `SAIDA` → `404`; sem token → `401`. Nada muda nos casos recusados | INT, E2E | **Sim** (`Fase3EstoquePedidoTest.descarteDeUmaLinhaVencidaZeraSoEssaLinha`; com o defeito de propósito "aceitar linha não vencida", o teste falha) |

### US12 — Pedido

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT38 | O pedido grava o preço **do banco** e baixa os ingredientes pela composição | Estoque válido suficiente; produto 1 a R$ 12,90 | Funcionário | `POST /api/pedidos` com 2× produto 1 → `201`; `valor_total = 25,80`; `preco_unitario_registrado = 12,90`; **uma** `SAIDA` por ingrediente com `motivo = PEDIDO`; açaí −0,600; leite condensado −0,060; copo −2 | INT, E2E | **Sim**, com 4× (51,60; açaí atravessando duas linhas): `…pedidoBaixaPorLinhasComUmaSaidaPorIngrediente…` |
| CT39 | A data do pedido fica no fuso de São Paulo (A9) | Relógio em 31/10 às 23:30 (São Paulo) | Admin | Criar pedido → `data_pedido` = `2026-10-31T23:30`; o dashboard de outubro o conta | INT | **Sim** (`DashboardTest.mesAtualSoComPedidosAtivos`) |
| CT40 | `GET /api/pedidos` lista os itens e o status | Pedidos criados | Admin | `200`; cada pedido com `itens[]`, `usuario_nome` e `status = ATIVO`, do mais recente para o mais antigo | INT, E2E | **Sim** (`…pedidoBaixaPorLinhas…`) |

### US10 / US11 — Recomendação de produção

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT41 | O modelo maximiza o **lucro**, não a receita | Instância "maior receita ≠ maior lucro" | — | Plano `{1: 10, 2: 0}`, margem 90 | PY | **Sim** (`test_maior_receita_nao_e_maior_lucro`) |
| CT42 | Recomendação com o estoque de demonstração | Estoque do `seed_demo.sql` | Admin | `POST /api/producao/recomendacao` → **151/1/0**, margem **1.011,71**, receita **1.966,80**; a granola vai ao Python com **5,160** | INT, E2E | **Sim** (dublê: `Fase4ProducaoTest`; Python real: `PythonRealTest`; container de deploy) |
| CT43 | A Tela 12 não usa termos técnicos e explica o zero | Recomendação com algum produto em 0 | Admin | "Lucro potencial" e "Receita estimada"; "Sem lucro" × "Falta ingrediente"; nenhum termo técnico na página | E2E (Playwright) | Não (frontend) |
| CT44 | Erros do Python viram mensagens tratadas | Dublê com `status: "erro"`; depois falha técnica | Admin | `422` com a mensagem; falha → `500` "Não foi possível concluir a operação." sem detalhe | INT | **Sim** (`Fase4ProducaoTest`, `ProcessoPythonTest`) |
| CT45 | Funcionário não gera recomendação | — | Funcionário | `403` em JSON | INT | **Sim** |

### US14 — Reposição (nova na Sprint 3)

Cenário de referência (conta à mão): estoque válido açaí 20, leite condensado 5, copo 100, granola 1, banana 1; pedidos 10× Tradicional em 10/10 e 5× Tradicional + 2× Especial em 28/10; hoje = 28/10.

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT51 | Orçamento folgado compra toda a necessidade | Cenário de referência | Admin | `POST /api/reposicao/recomendacao` `{periodo_dias: 30, orcamento: 100}` → `200`; custo total **63,22**; Granola 1,16 kg (25,52) primeiro; açaí 0,80; banana 2,20; leite condensado 0,90; copo 0 | INT, PY, E2E | **Sim** (Python real: `PythonRealTest.reposicaoContaAMao`) |
| CT52 | Orçamento que não cobre os mínimos | Idem | Admin | `orcamento: 30` → `orcamento_suficiente = false`; falta **10,71** (mínimos 40,71); a tabela mostra os mínimos | INT, PY, E2E | **Sim** (idem) |
| CT53 | Orçamento parcial reparte de forma equilibrada | Exemplo didático: açaí (mín. 0,20; nec. 1,00), granola (0,10; 0,40), leite condensado (0; 0,20) | — | Orçamento 20,00 → 0,66 / 0,27 / 0,15; total **19,95**; menor cobertura 66% | PY | **Sim** (`test_exemplo_didatico_via_subprocesso`; 150 instâncias contra o oráculo) |
| CT54 | Período e quantidade de ingredientes | Cenário de referência; 12 ingredientes extras consumidos | Admin | 7 dias → só o pedido de 28/10 (copo 7); 30 dias → até **8** ingredientes; 31 dias → até **15**; pedido **cancelado** continua contando | INT | **Sim** (`ReposicaoTest`) |
| CT55 | Sem pedidos no período | Banco sem pedidos | Admin | `200`; `itens = []`; mensagem "Não houve pedidos no período…"; o Python **não** é chamado | INT | **Sim** |
| CT56 | Validação e permissão | — | Admin / Funcionário | `periodo_dias = 0` e `orcamento = −5` → `400` com os dois campos; orçamento com 3 casas → `400`; Funcionário → `403` | INT | **Sim** |
| CT57 | Tela 13 | Cenário de referência | Admin | Campos período (dias) e orçamento (R$); tabela do maior para o menor custo; frase de resumo; **nenhum termo técnico** ("cobertura", "otimização"…) | E2E | Não (frontend; funções de apoio: `testar_apoio_telas.mjs`, 15/15) |

### US17 — Dashboard (novo na Sprint 3)

Cenário de referência (conta à mão): 15/09 Dono 2× Tradicional; 05/10 Func 3× Tradicional; 20/10 Dono 1× Tradicional + 1× Especial; 28/10 Func 2× Especial (**cancelado**); 31/10 23h30 Dono 1× Tradicional.

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT58 | Números do mês só com pedidos ativos | Cenário de referência | Admin | `GET /api/dashboard` → faturamento **83,40** (3 pedidos); Tradicional 5 (64,50); Especial 1; ingredientes por custo: açaí 1,95 kg (36,08), copo 6 (2,10), leite condensado (1,80), granola (0,88), banana (0,28) | INT | **Sim** (`DashboardTest.mesAtualSoComPedidosAtivos`) |
| CT59 | Ranking e filtros | Idem | Admin | Sem filtro: 1º Dono 44,70 (2 pedidos), 2º Func 38,70; `produto_id=2` → só Dono 18,90; `usuario_id` do Func → só Func; `valor_min=40` → Dono; `valor_max=40` → Func | INT | **Sim** (`…filtrosDoRanking`) |
| CT60 | Outro mês e erros | Idem | Admin | `mes=2026-09` → 25,80; `mes=10/2026` → `400` "Informe o mês no formato AAAA-MM."; `valor_min > valor_max` → `400`; mês sem pedidos → zeros | INT | **Sim** (`…outroMesEErrosDeFiltro`) |
| CT61 | "Receita máxima estimada" e situação do estoque (Termo; A16) | Idem; morango vencido | Admin | Antes de calcular: `ultimo_plano = null`; 5 ingredientes no mínimo ou abaixo, 1 com vencido. Depois de `POST /api/producao/recomendacao`: `ultimo_plano.receita_estimada` e os produtos priorizados | INT | **Sim** (`…situacaoDoEstoqueEUltimoPlano`) |
| CT62 | Dashboard com o seed de demonstração | `seed_demo.sql` | Admin | Mês atual: **326,40** (3 pedidos); Premium 7, Especial 6, Tradicional 3; açaí 7,800 kg (144,30) primeiro; ranking Func 213,00 (2), Dono 113,40 (1). Mês anterior: **271,50** | BD, E2E | **Sim** em SQL (Docker); tela: não |
| CT63 | Tela 03 | Seed de demonstração | Admin | Seletor de mês (padrão: atual); filtros do ranking; bloco "Plano de produção" com a mensagem quando não houver cálculo; Funcionário não vê o menu | E2E | Não (frontend) |

### US18 — Marketing (novo na Sprint 3)

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT64 | Sugestões a partir da última recomendação | Recomendação 151/1/0 gerada | Admin | `GET /api/marketing/sugestoes` → 2 sugestões (o Premium veio 0): Tradicional e Especial, selo "Produto priorizado", ideias **diferentes**, texto com o nome do produto, lucro por copo | INT | **Sim** (`MarketingTest`) |
| CT65 | Sem cálculo, sem produto a produzir, permissão | — | Admin / Funcionário | Sem cálculo → mensagem "Gere a recomendação na tela Produção…"; plano todo 0 → mensagem própria; Funcionário → `403` | INT | **Sim** |
| CT66 | Tela 14 | Recomendação gerada | Admin | Até 3 cartões; hora do cálculo visível; nenhum botão de "publicar"; nenhum termo técnico | E2E | Não (frontend) |

### US20 — Publicação (nova na Sprint 3)

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT67 | A imagem sobe dentro dos limites do plano free | `docker build` da raiz; banco com o DDL | — | Container com 512 MB e 0,1 CPU → `GET /api/saude` `200` em até ~2 min; sem `OOMKilled`; `/api/teste/python` → `401` | DEP (local) | **Sim** (simulação: 83 s; pico 133 MiB) |
| CT68 | Fluxo do Termo no ambiente publicado | Deploy no Render + Pages | Admin e Funcionário | Pela URL HTTPS do Pages: cadastro → login → entrada de estoque → recomendação → pedido → baixa; reposição; dashboard; marketing. **Com a licença do `pip` autorizada (A38, 28/10):** recomendação (151/1/0 com o seed), reposição, marketing e o plano no dashboard **funcionam na URL**. Só se a reserva B1 (A31) for ligada eles mostram o aviso do `503` (CT72) e passam para a demonstração local | DEP, E2E | Não (depende do deploy real); **o mesmo fluxo passou no container local** |
| CT69 | CORS do GitHub Pages | Idem | — | `OPTIONS` com a origem do Pages → `200` com `Access-Control-Allow-Origin`; outra origem → `403` | DEP | Sim no container local; no Render, não |
| CT70 | Primeira chamada depois de o serviço "dormir" | Serviço sem uso por 15 min | Admin | A tela espera (até 90 s) e funciona; **não** aparece "O servidor demorou para responder" | DEP, E2E | Não (`config.js` conferido: 90 s no Pages, 15 s local) |
| CT72 | Otimização desligada no servidor (plano B1 da licença, A31; **reserva desde 28/10**, A38) | `OTIMIZADOR_HABILITADO=false` (a chave de reserva, se o solver der problema no Render) | Admin e Funcionário | Produção → `503` "A recomendação está disponível só na demonstração local da equipe.", sem chamar o Python (Funcionário continua `403`); reposição: campo inválido → `400`, sem pedidos → `200`, com pedidos → `503`; marketing → mensagem própria; dashboard → `200` com `otimizador_habilitado: false`; as Telas 03, 12, 13 e 14 mostram a mensagem como aviso | INT, DEP, E2E | **Sim** (`OtimizadorDesligadoTest`, 3 testes; tirar a chave de propósito derruba o teste). No Render e nas telas, não. **No Render, opcional** (A38): trocar a variável, conferir e voltar para `true` |

### Segurança e erros (US3, US7, infraestrutura)

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT46 | `401` em JSON sem token, com token inválido, expirado e em rota inexistente | — | Anônimo | Cada caso → `401`, `Content-Type: application/json`, `mensagem` não vazia | MVC | **Sim** (`SegurancaEErrosTest`) |
| CT47 | `400` em JSON para requisição malformada | — | Admin | JSON quebrado; `PUT /api/produtos/abc/preco`; parâmetro obrigatório ausente → `400` "Requisição inválida." | MVC | **Sim** (depois da correção A7) |
| CT48 | `500` sem detalhe interno | Endpoint que lança exceção | Admin | `500` "Não foi possível concluir a operação."; o corpo **não** contém o texto da exceção | MVC | **Sim** |
| CT49 | CORS para o frontend, inclusive `PATCH` | — | — | `OPTIONS` de `http://127.0.0.1:5500` com `PATCH` → `200`; origem desconhecida → `403` | MVC | **Sim** |
| CT50 | Modo eco da ponte Java ↔ Python, com acentos | Perfil `dev` | — | `GET /api/teste/python` → `{"status": "sucesso", "echo": "AÇAÍ (POLPA)"}`; **sem** o perfil `dev` → `401` | INT + MVC | **Sim** (`PythonRealTest.ecoComAcentosPeloPerfilDev`) |

---

## 3. Execução na semana de correção de bugs (depois de 06/11; até 13/11; A39)

*Antes: "janela de testes de 02 a 06/11" (A36). O sistema já está publicado desde 02/11 (A41).*

- Ordem sugerida: primeiro os caminhos do Termo de Aceite **no ambiente publicado** (CT68, CT06, CT12, CT30, CT38, CT58), depois os casos de erro e, por último, o CT72 (a reserva). **Com a A38 (28/10):** CT42, CT51 e CT64 rodam **no Render**. Só se o B1 for ligado eles passam para a demonstração local do Kauê.
- Divisão por perfil: dois integrantes testam como Administrador e dois como Funcionário.
- Cada defeito segue o fluxo: registrar → caso que reproduz → `fix/...` → PR → retestar (roteiro v4.6, §4.4, item 5).
- Para defeitos difíceis, usar a skill `diagnosing-bugs`.
- **Atenção ao banco free do Render** (A21): ele expira 30 dias depois de criado. Anote a data no relatório de testes.
