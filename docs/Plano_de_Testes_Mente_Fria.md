# Plano de Testes — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

**Etapa:** E4 — Semana 4 (entrega 11/09/2026). **Atualizado em 27/10/2026** para a Sprint 3 (Adendo nº 1 ao Termo de Aceite aprovado): casos CT30–CT72, ajustes de CT23, CT24 e CT29, licença do Gurobi e ferramentas de teste.

**Status:** este documento é a **estratégia** de testes e a lista de casos (seção 3), que cresce a cada sprint. As evidências de execução ficam nas pastas de cada sprint (`docs/Sprint_2/sprint-2-evidencias-teste.md`, `docs/Sprint_3/evidencias/`); o arquivo `E5-E8_evidencias_de_teste.md`, previsto antes, não foi criado.

---

> **Referência — requisitos mínimos do §3 do Manual**, já mapeados pela equipe no Documento de Visão (seção 6) e usados na coluna "O que cobre" da seção 1:
>
> | # | Requisito mínimo | Onde aparece no projeto |
> |---|---|---|
> | R1 | Autenticação com 2+ perfis | US1, US2, US3 |
> | R2 | 6+ entidades com relacionamento N:N | Produto↔Ingrediente e Pedido↔Produto (US5, US12) |
> | R3 | Regra de negócio não trivial | Programação Linear Inteira pura (US10, US11), reposição (US14), marketing (US18) |
> | R4 | Consulta agregada (relatório/dashboard) | US17, US19 |
> | R5 | Validações em interface e banco | US4, US6, US7 e transversal (US7) |
> | R6 | Deploy público por URL | US20 |
> | R7 | Repositório Git com README | US8 |

---

## 1. Estratégia

| Tipo de teste | O que cobre | Ferramenta | Quando roda |
|---|---|---|---|
| **Unitário** | Regra de negócio isolada, sem banco/rede: validações de campo antes de persistir (senha ≥ 8 caracteres, preço > 0, quantidade > 0…) e métodos de domínio (`Ingrediente.precisaReposicao()`, `Produto.calcularCustoProducao()`, `Produto.calcularQuantidadeOtimaProducao()`). Cobre principalmente **R3** e a metade "aplicação" de **R5**. | JUnit 5 + Mockito (backend Java); pytest (módulo Python/Gurobi — cenários de estoque/composição fixos, com resultado esperado calculado fora do próprio otimizador, para não validar o solver contra ele mesmo) | A cada build local (`mvn test` / `pytest`) e em todo push/PR no CI |
| **Integração** | Fluxo real entre camadas — Controller → Service → Repository → PostgreSQL; autenticação/autorização por perfil ponta a ponta na API; constraints do banco (`NOT NULL`, `CHECK`, `UNIQUE`, `FK` do DER/DDL); consultas agregadas do dashboard (`GROUP BY`/`JOIN`); chamada do backend ao módulo Python/Gurobi. Cobre **R1**, **R2**, **R4** e a metade "banco" de **R5**. | JUnit 5 + Spring Boot Test (`@SpringBootTest`, `MockMvc`) + **Testcontainers (PostgreSQL 16)** — atualizado de MySQL para PostgreSQL em 16/09/2026 (ver DER.md), todos com Docker disponível | Antes de cada merge, rodado localmente por quem abre o PR (`mvnw test`, com Docker). **Ainda não há CI configurado** (Sprint 3); se a equipe criar um, ele passa a rodar aqui |
| **Manual/aceitação** | Fluxo principal ponta a ponta pela interface (Termo de Aceite, seção 2): cadastro de ingredientes/produtos → estoque → recomendação de produção/vendas e propaganda → registro de pedido → baixa automática; critérios de aceite de cada história (E2); e os itens do §3 que não são testáveis por código: **R6** (deploy) e **R7** (repositório/README) | Roteiro de teste manual por história, executado por um integrante diferente de quem implementou + navegador/API client (Postman ou Insomnia) para checagem pontual de endpoints | Ao final de cada sprint (Sprint Review) e obrigatoriamente antes de qualquer tag de entrega (E5–E8) |

Duas observações que não mudam a tabela acima, mas valem registrar:
- **Frontend (JS puro, sem build tool — README):** validado por roteiro manual/aceitação, por **testes de navegador com Playwright** (`frontend/tests/browser.cjs`, com a API simulada) e, na Sprint 3, pelas **funções de apoio testadas com Node** (`testar_apoio_telas.mjs`). Jest ou Vitest continuam desnecessários.
- **CI:** até a Sprint 3, **não há** pipeline de testes. O único *workflow* previsto é o de publicação do frontend no GitHub Pages (US20). Se a equipe criar um CI, os testes que chamam o Gurobi de verdade continuam opcionais (`MF_PYTHON`), porque o runner não tem licença própria.

---

## 2. Critério de bloqueio de merge

**Bloqueia o merge — corrigir antes de aceitar o PR:**
1. O build não compila/não roda (`mvn test` ou `pytest` falha antes mesmo de avaliar o resultado dos testes).
2. Algum teste unitário ou de integração já existente passa a falhar (regressão).
3. O PR introduz uma regra de negócio ligada a um requisito mínimo do §3 (autenticação, N:N, Programação Linear Inteira, validação, dashboard) sem nenhum teste automatizado cobrindo o caminho principal.
4. O PR remove, enfraquece ou contorna uma constraint documentada no DER (`NOT NULL`, `CHECK`, `UNIQUE`, `FK`) sem combinar antes com a equipe.
5. Um endpoint que deveria ser restrito por perfil (ex.: alterar preço, restrito ao Administrador — Termo de Aceite, item 3) fica acessível sem verificação de autorização.
6. Há credencial, senha, chave de licença Gurobi ou string de conexão escrita diretamente no código.

**Não bloqueia, mas exige comentário no PR e item no board:**
- Falta de teste para funcionalidade `Should`/`Could` ainda não crítica para o MVP.
- Falta de roteiro manual para um fluxo que o automatizado já cobre tecnicamente.

Não fixamos uma meta numérica de cobertura (ex.: "≥ 70% de linhas") agora. No início do projeto, o risco maior é lógica sem teste nenhum — não uma métrica abaixo do ideal. Vale revisitar isso a partir da E5, já com uma base de código real para medir.

---

## 3. Casos de teste planejados (cresce a cada sprint)

**Prioridade** aqui é a prioridade de *execução do teste* — não é o MoSCoW da história, embora normalmente andem juntos: **Alta** = bloqueia funcionalidade crítica ou envolve segurança/integridade de dados; **Média** = comportamento esperado, impacto limitado se atrasar; **Baixa** = caso auxiliar.

Incluímos uma coluna extra, **Caso de uso (E3)**, além do modelo, para fechar a rastreabilidade Backlog (E2) ↔ Caso de uso (E3) ↔ Teste, usando o mapeamento já definido em `Diagramas_UML.md`, seção 3.

### Sprint 1 — cadastro, autenticação, validação transversal

| ID | História (E2) | Caso de uso (E3) | Cenário | Entrada | Resultado esperado | Prioridade |
|---|---|---|---|---|---|---|
| CT01 | US1 | UC1 | Cadastro de Administrador com dados válidos | Nome, e-mail novo, senha com 8+ caracteres | Conta criada; perfil `ADMINISTRADOR` atribuído automaticamente | Alta |
| CT01B | US1 | UC1 | Cadastro de Administrador quando já existe um | Requisição de registro com um Administrador já cadastrado no sistema | Rejeitado com `409`; mensagem "O cadastro do Administrador já foi realizado." | Alta |
| CT02 | US1 | UC1 | Senha abaixo do mínimo | Senha com 7 caracteres | Cadastro rejeitado; erro específico no campo senha (validação de aplicação — não dá para checar isso depois de já estar em hash) | Alta |
| CT03 | US1 | UC1 | E-mail já cadastrado | E-mail idêntico a um usuário existente | Rejeitado pela aplicação e, se essa checagem for contornada, pela constraint `uq_usuario_email` | Alta |
| CT04 | US2 | UC2 | Cadastro de funcionário válido pelo Administrador | Nome, e-mail, senha válidos | Conta criada com perfil `FUNCIONARIO` | Alta |
| CT05 | US2 | UC2 | Funcionário tenta cadastrar/alterar preço de produto | Usuário autenticado com perfil `FUNCIONARIO` | Acesso negado (403) | Alta |
| CT06 | US3 | UC3 | Login válido por perfil | Credenciais corretas de Administrador e de Funcionário (2 execuções) | Redirecionamento para a área correspondente a cada perfil | Alta |
| CT07 | US3 | UC3 | Login com senha incorreta | E-mail válido + senha errada | Mensagem de erro clara; login não efetivado | Alta |
| CT08 | US3 | UC3 | Funcionário tenta acessar rota administrativa direto pela URL | Usuário autenticado com perfil `FUNCIONARIO` | Bloqueio/redirecionamento; ação não é executada | Alta |
| CT09 | US4 | UC4 | Cadastro de ingrediente com dados válidos | Nome, unidade, custo ≥ 0, porção > 0 | Ingrediente persistido com os valores informados | Alta |
| CT10 | US4 | UC4 | Custo unitário negativo | `custo_unitario = -1` | Rejeitado na interface e pela constraint `chk_ingrediente_custo_unitario` | Alta |
| CT11 | US4 | UC4 | Porção padrão igual a zero | `porcao_padrao = 0` | Rejeitado pela constraint `chk_ingrediente_porcao_padrao` (diferente do custo: porção não aceita nem zero) | Média |
| CT12 | US5 | UC5 | Cadastro de produto com composição válida, incluindo Açaí | Produto + ingrediente "Açaí" + 1 outro ingrediente, ambos com `quantidade_utilizada > 0` | Produto e vínculos persistidos em `produto_ingrediente` | Alta |
| CT13 | US5 | UC5 | Cadastro de produto sem nenhum ingrediente vinculado | Produto sem composição | Rejeitado — composição vazia | Alta |
| CT14 | US5 | UC5 | Quantidade utilizada zerada/negativa na composição | `quantidade_utilizada ≤ 0` | Rejeitado pela constraint `chk_produto_ingrediente_quantidade` | Média |
| CT15 | US5 | UC5 | Produto cadastrado com outros ingredientes, mas sem Açaí | Produto + ingredientes válidos que não incluem "Açaí" | Rejeitado — sistema exige o ingrediente "Açaí" especificamente entre os vinculados | Alta |
| CT16 | US6 | UC6 | Atualização de preço por Administrador | `preco_venda > 0` | Preço atualizado | Alta |
| CT17 | US6 | UC6 | Preço igual a zero ou negativo | `preco_venda ≤ 0` | Rejeitado pela constraint `chk_produto_preco_venda` | Alta |
| CT18 | US7 | UC7 | Requisição direta à API, contornando a interface, sem campo obrigatório | Requisição HTTP sem `nome` | Backend rejeita (400/422) mesmo sem passar pela validação de tela | Alta |
| CT19 | US7 | UC7 | Formulário de cadastro com campo obrigatório vazio | Campo em branco | Interface exibe erro específico antes de enviar | Média |
| CT20 | US8 | — | Setup do zero a partir do README | `git clone` + passos do README, ambiente limpo | Projeto sobe localmente sem etapa não documentada | Média |

> **Decisão de implementação (CT01B) — 20/09/2026:** decidido junto com a dúvida D-A (`docs/Apoio/sql_repositorios.md`, seção 5) — o cadastro de Administrador (US1) fica aberto **só enquanto não existir nenhum Administrador** no banco (opção 2), usando `UsuarioRepository.existeAdministrador`. Depois do primeiro cadastro, uma nova tentativa é rejeitada com `409`. Confirmado pelo Lucas e já assumido pelo *Contrato de Comunicação* (item B5).

> **Decisão de implementação (CT15) — 19/09/2026:** a regra exige o ingrediente "Açaí" especificamente, não apenas "composição não vazia". Decidido: um ingrediente conta como açaí quando o seu **nome, sem acentos e sem diferenciar maiúsculas de minúsculas, contém `acai`** (ex.: "Açaí (polpa)", "AÇAÍ ZERO", "Polpa de açaí"). A verificação é feita na aplicação (Java), isolada em um único método, sem alteração no DER nem no DDL. Consequências: (1) se o dono renomear o ingrediente para um nome sem "açaí", o sistema recusa novos produtos até a correção do nome (falha segura); (2) mais de um ingrediente de açaí é permitido; (3) alternativas descartadas: ID fixo (o ID é gerado pelo banco) e coluna `eh_ingrediente_base` (mais robusta, mas exigiria alterar DER, UML, DDL e Tela 07 — pode ser adotada depois trocando apenas esse método). O teste unitário do método deve cobrir: aceitos — "Açaí (polpa)", "AÇAÍ ZERO", "polpa de acai"; recusados — "Granola", "Leite condensado", texto vazio e `null`.

### Sprint 2 (prévia) — estoque, pedidos e otimização

| ID | História (E2) | Caso de uso (E3) | Cenário | Entrada | Resultado esperado | Prioridade |
|---|---|---|---|---|---|---|
| CT21 | US9 | UC9 | Saída de estoque maior que o disponível | Saída = estoque atual + 1 unidade | Rejeitado; `quantidade_estoque` não é alterada (também protegido por `chk_ingrediente_quantidade_estoque`) | Alta |
| CT22 | US9 | UC9 | Entrada válida de ingrediente | Entrada de quantidade positiva | `quantidade_estoque` incrementada; registro criado em `movimentacao_estoque` com `tipo = ENTRADA` | Média |
| CT23 | US10 | UC10 | Um ingrediente escasso limita a produção de um único produto | Produto X usa 400g de polpa/unidade; estoque = 1.200g; nenhum outro ingrediente limita; **cada ingrediente com `custo_unitario`** (Sprint 3) | Quantidade recomendada = 3 (1.200 ÷ 400) — não mais que isso; a resposta traz a **`margem_estimada`** (lucro estimado) | Alta |
| CT24 | US10 | UC10 | Dois produtos disputam o mesmo ingrediente escasso | Produtos A e B usam o mesmo ingrediente; estoque insuficiente para produzir o máximo de ambos ao mesmo tempo | A soma do consumo desse ingrediente pela recomendação final não ultrapassa o estoque disponível | Alta |
| CT25 | US12 | UC12 | Pedido com múltiplos itens, um deles sem estoque suficiente | Pedido com 2 produtos, um inviável por estoque | Pedido inteiro é rejeitado; nenhum item é gravado; estoque de nenhum ingrediente é alterado (atomicidade da transação) | Alta |
| CT26 | US9 | UC9 | Registrar movimentação com quantidade zero ou negativa | `quantidade ≤ 0` | Rejeitado pela constraint `chk_movimentacao_quantidade` | Média |
| CT27 | US9 | UC9 | Os dois perfis conseguem registrar movimentação de estoque | Execução com perfil `ADMINISTRADOR` e, separadamente, com perfil `FUNCIONARIO` | Operação permitida para os dois perfis — UC9 não é exclusivo de nenhum dos dois | Média |
| CT28 | US12 | UC12 | Os dois perfis conseguem registrar pedido | Execução com perfil `ADMINISTRADOR` e, separadamente, com perfil `FUNCIONARIO` | Operação permitida para os dois perfis — UC12 não é exclusivo de nenhum dos dois | Média |
| CT29 | US10 | UC10 | Ingrediente com **linha de entrada vencida** não conta como estoque disponível | Ingrediente com saldo positivo, mas cuja linha de entrada já venceu (`data_validade < hoje`; **a que vence hoje ainda vale**) | A **entrada enviada ao Python** traz só o saldo válido; a recomendação é igual à de estoque zero para esse saldo vencido | Alta |

> **Decisão de implementação (CT29) — 25/09/2026:** o grupo decidiu que ingredientes vencidos não contam como estoque disponível na recomendação de produção (detalhes em `docs/Apoio/Arquitetura.md`, seção 2.1, e no Contrato de Comunicação, §1.1). O Java desconta o saldo vencido antes de montar o JSON enviado ao Python. **Sprint 3:** o estoque passou a ser por linha de entrada (D10), e o CT24 continua válido enquanto o custo do ingrediente disputado for menor que o preço.

*US21–US25 são `Won't` no backlog e não geram caso de teste. US13, US19, US26 e US27 estão fora da Sprint 3 (Adendo nº 1) e ganham casos quando entrarem.*

### Sprint 3 — estoque por linhas, pedido, recomendação por lucro, reposição, dashboard, marketing, publicação e segurança (CT30–CT72)

Incorporados em 27/10/2026 a partir de `docs/Sprint_3/sprint-3-casos-de-teste-propostos.md` (Adendo nº 1 aprovado). Formato: objetivo · pré-condições · perfil · passos → resultado esperado; **Camada:** BD = SQL; INT = integração (MockMvc + Testcontainers); PY = pytest; E2E = navegador ou manual; DEP = ambiente publicado. A coluna **Validado** diz se o comportamento já foi executado no código de referência (`docs/Sprint_3/referencia/`), e por qual teste; **não** dispensa a execução sobre o código real nem no ambiente publicado. A execução na janela de testes (02–06/11) segue a seção 3 do mesmo arquivo.

**Formato:** objetivo · pré-condições · perfil · passos → resultado esperado. A "camada" diz onde automatizar: **BD** = SQL; **INT** = Testcontainers; **MVC** = MockMvc; **PY** = pytest; **E2E** = aceite manual ou Playwright; **DEP** = ambiente publicado.

#### US9 — Estoque por linhas

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

#### US12 — Pedido

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT38 | O pedido grava o preço **do banco** e baixa os ingredientes pela composição | Estoque válido suficiente; produto 1 a R$ 12,90 | Funcionário | `POST /api/pedidos` com 2× produto 1 → `201`; `valor_total = 25,80`; `preco_unitario_registrado = 12,90`; **uma** `SAIDA` por ingrediente com `motivo = PEDIDO`; açaí −0,600; leite condensado −0,060; copo −2 | INT, E2E | **Sim**, com 4× (51,60; açaí atravessando duas linhas): `…pedidoBaixaPorLinhasComUmaSaidaPorIngrediente…` |
| CT39 | A data do pedido fica no fuso de São Paulo (A9) | Relógio em 31/10 às 23:30 (São Paulo) | Admin | Criar pedido → `data_pedido` = `2026-10-31T23:30`; o dashboard de outubro o conta | INT | **Sim** (`DashboardTest.mesAtualSoComPedidosAtivos`) |
| CT40 | `GET /api/pedidos` lista os itens e o status | Pedidos criados | Admin | `200`; cada pedido com `itens[]`, `usuario_nome` e `status = ATIVO`, do mais recente para o mais antigo | INT, E2E | **Sim** (`…pedidoBaixaPorLinhas…`) |

#### US10 / US11 — Recomendação de produção

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT41 | O modelo maximiza o **lucro**, não a receita | Instância "maior receita ≠ maior lucro" | — | Plano `{1: 10, 2: 0}`, margem 90 | PY | **Sim** (`test_maior_receita_nao_e_maior_lucro`) |
| CT42 | Recomendação com o estoque de demonstração | Estoque do `seed_demo.sql` | Admin | `POST /api/producao/recomendacao` → **151/1/0**, margem **1.011,71**, receita **1.966,80**; a granola vai ao Python com **5,160** | INT, E2E | **Sim** (dublê: `Fase4ProducaoTest`; Python real: `PythonRealTest`; container de deploy) |
| CT43 | A Tela 12 não usa termos técnicos e explica o zero | Recomendação com algum produto em 0 | Admin | "Lucro potencial" e "Receita estimada"; "Sem lucro" × "Falta ingrediente"; nenhum termo técnico na página | E2E (Playwright) | Não (frontend) |
| CT44 | Erros do Python viram mensagens tratadas | Dublê com `status: "erro"`; depois falha técnica | Admin | `422` com a mensagem; falha → `500` "Não foi possível concluir a operação." sem detalhe | INT | **Sim** (`Fase4ProducaoTest`, `ProcessoPythonTest`) |
| CT45 | Funcionário não gera recomendação | — | Funcionário | `403` em JSON | INT | **Sim** |

#### US14 — Reposição (nova na Sprint 3)

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

#### US17 — Dashboard (novo na Sprint 3)

Cenário de referência (conta à mão): 15/09 Dono 2× Tradicional; 05/10 Func 3× Tradicional; 20/10 Dono 1× Tradicional + 1× Especial; 28/10 Func 2× Especial (**cancelado**); 31/10 23h30 Dono 1× Tradicional.

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT58 | Números do mês só com pedidos ativos | Cenário de referência | Admin | `GET /api/dashboard` → faturamento **83,40** (3 pedidos); Tradicional 5 (64,50); Especial 1; ingredientes por custo: açaí 1,95 kg (36,08), copo 6 (2,10), leite condensado (1,80), granola (0,88), banana (0,28) | INT | **Sim** (`DashboardTest.mesAtualSoComPedidosAtivos`) |
| CT59 | Ranking e filtros | Idem | Admin | Sem filtro: 1º Dono 44,70 (2 pedidos), 2º Func 38,70; `produto_id=2` → só Dono 18,90; `usuario_id` do Func → só Func; `valor_min=40` → Dono; `valor_max=40` → Func | INT | **Sim** (`…filtrosDoRanking`) |
| CT60 | Outro mês e erros | Idem | Admin | `mes=2026-09` → 25,80; `mes=10/2026` → `400` "Informe o mês no formato AAAA-MM."; `valor_min > valor_max` → `400`; mês sem pedidos → zeros | INT | **Sim** (`…outroMesEErrosDeFiltro`) |
| CT61 | "Receita máxima estimada" e situação do estoque (Termo; A16) | Idem; morango vencido | Admin | Antes de calcular: `ultimo_plano = null`; 5 ingredientes no mínimo ou abaixo, 1 com vencido. Depois de `POST /api/producao/recomendacao`: `ultimo_plano.receita_estimada` e os produtos priorizados | INT | **Sim** (`…situacaoDoEstoqueEUltimoPlano`) |
| CT62 | Dashboard com o seed de demonstração | `seed_demo.sql` | Admin | Mês atual: **326,40** (3 pedidos); Premium 7, Especial 6, Tradicional 3; açaí 7,800 kg (144,30) primeiro; ranking Func 213,00 (2), Dono 113,40 (1). Mês anterior: **271,50** | BD, E2E | **Sim** em SQL (Docker); tela: não |
| CT63 | Tela 03 | Seed de demonstração | Admin | Seletor de mês (padrão: atual); filtros do ranking; bloco "Plano de produção" com a mensagem quando não houver cálculo; Funcionário não vê o menu | E2E | Não (frontend) |

#### US18 — Marketing (novo na Sprint 3)

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT64 | Sugestões a partir da última recomendação | Recomendação 151/1/0 gerada | Admin | `GET /api/marketing/sugestoes` → 2 sugestões (o Premium veio 0): Tradicional e Especial, selo "Produto priorizado", ideias **diferentes**, texto com o nome do produto, lucro por copo | INT | **Sim** (`MarketingTest`) |
| CT65 | Sem cálculo, sem produto a produzir, permissão | — | Admin / Funcionário | Sem cálculo → mensagem "Gere a recomendação na tela Produção…"; plano todo 0 → mensagem própria; Funcionário → `403` | INT | **Sim** |
| CT66 | Tela 14 | Recomendação gerada | Admin | Até 3 cartões; hora do cálculo visível; nenhum botão de "publicar"; nenhum termo técnico | E2E | Não (frontend) |

#### US20 — Publicação (nova na Sprint 3)

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT67 | A imagem sobe dentro dos limites do plano free | `docker build` da raiz; banco com o DDL | — | Container com 512 MB e 0,1 CPU → `GET /api/saude` `200` em até ~2 min; sem `OOMKilled`; `/api/teste/python` → `401` | DEP (local) | **Sim** (simulação: 83 s; pico 133 MiB) |
| CT68 | Fluxo do Termo no ambiente publicado | Deploy no Render + Pages | Admin e Funcionário | Pela URL HTTPS do Pages: cadastro → login → entrada de estoque → recomendação → pedido → baixa; reposição; dashboard; marketing. **Com o plano B1 (A31):** recomendação, reposição e marketing mostram o aviso do `503` (CT72) e são demonstrados localmente pelo Kauê; o resto do fluxo, na URL | DEP, E2E | Não (depende do deploy real); **o mesmo fluxo passou no container local** |
| CT69 | CORS do GitHub Pages | Idem | — | `OPTIONS` com a origem do Pages → `200` com `Access-Control-Allow-Origin`; outra origem → `403` | DEP | Sim no container local; no Render, não |
| CT70 | Primeira chamada depois de o serviço "dormir" | Serviço sem uso por 15 min | Admin | A tela espera (até 90 s) e funciona; **não** aparece "O servidor demorou para responder" | DEP, E2E | Não (`config.js` conferido: 90 s no Pages, 15 s local) |
| CT72 | Otimização desligada no servidor (plano B1 da licença, A31) | `OTIMIZADOR_HABILITADO=false` (como no Render sem autorização da Gurobi) | Admin e Funcionário | Produção → `503` "A recomendação está disponível só na demonstração local da equipe.", sem chamar o Python (Funcionário continua `403`); reposição: campo inválido → `400`, sem pedidos → `200`, com pedidos → `503`; marketing → mensagem própria; dashboard → `200` com `otimizador_habilitado: false`; as Telas 03, 12, 13 e 14 mostram a mensagem como aviso | INT, DEP, E2E | **Sim** (`OtimizadorDesligadoTest`, 3 testes; tirar a chave de propósito derruba o teste). No Render e nas telas, não |

#### Segurança e erros (US3, US7, infraestrutura)

| ID | Objetivo | Pré-condições | Perfil | Passos → resultado esperado | Camada | Validado em 26/10 |
|---|---|---|---|---|---|---|
| CT46 | `401` em JSON sem token, com token inválido, expirado e em rota inexistente | — | Anônimo | Cada caso → `401`, `Content-Type: application/json`, `mensagem` não vazia | MVC | **Sim** (`SegurancaEErrosTest`) |
| CT47 | `400` em JSON para requisição malformada | — | Admin | JSON quebrado; `PUT /api/produtos/abc/preco`; parâmetro obrigatório ausente → `400` "Requisição inválida." | MVC | **Sim** (depois da correção A7) |
| CT48 | `500` sem detalhe interno | Endpoint que lança exceção | Admin | `500` "Não foi possível concluir a operação."; o corpo **não** contém o texto da exceção | MVC | **Sim** |
| CT49 | CORS para o frontend, inclusive `PATCH` | — | — | `OPTIONS` de `http://127.0.0.1:5500` com `PATCH` → `200`; origem desconhecida → `403` | MVC | **Sim** |
| CT50 | Modo eco da ponte Java ↔ Python, com acentos | Perfil `dev` | — | `GET /api/teste/python` → `{"status": "sucesso", "echo": "AÇAÍ (POLPA)"}`; **sem** o perfil `dev` → `401` | INT + MVC | **Sim** (`PythonRealTest.ecoComAcentosPeloPerfilDev`) |


---

## 4. Decisões e notas

### 4.1 Já validadas com a equipe
- **D-A (cadastro de Administrador) e D-B (Tela 07 sem o campo porção):** confirmadas em 20/09/2026. Lucas aprovou a opção 2 do D-A (cadastro aberto só até existir o primeiro Administrador; depois, `409` — CT01B); Leonardo aprovou incluir o campo *porção* na Tela 07, pré-preenchido pela unidade escolhida (ver `docs/Apoio/sql_repositorios.md`, seção 5).
- **Testes de integração:** Testcontainers com PostgreSQL 16 real (atualizado de MySQL em 16/09/2026, ver DER.md) — Docker confirmado disponível para todo mundo.
- **US5 (composição do produto):** a regra exige o ingrediente "Açaí" especificamente, não apenas "composição não vazia" (como localizar esse ingrediente: decidido em 19/09/2026 — o nome, sem acentos e sem diferenciar maiúsculas, contém "acai"; ver a decisão registrada na seção 3, junto ao CT15).
- **Backlog — atores de US9, US12, US13, US15 e US16:** confirmado que os dois perfis (Administrador e Funcionário) têm acesso a essas 5 funcionalidades, conforme o `.docx` — que também é o que bate com `Diagramas_UML.md`. O `.pdf` do backlog está desatualizado nesses 5 pontos (mostra só 1 ator em cada um); vale atualizar essa exportação para não confundir quem abrir só o `.pdf`. CT27 e CT28 (seção 3) já cobrem essa confirmação para US9 e US12; US13, US15 e US16 recebem o mesmo tratamento quando forem detalhadas.
- **Modelo da recomendação de produção (08/10/2026):** o modelo matemático é de **Programação Linear Inteira pura** (variáveis de decisão inteiras, função objetivo e restrições lineares), resolvido com Gurobi 13.0.1 (Python 3.14). **Atualização de 27/10/2026:** o objetivo passou a ser o **lucro estimado** (Adendo nº 1) e a versão do Gurobi é a **13.0.3** (P2); a reposição com orçamento (US14) também usa o solver.

### 4.2 Em aberto (não bloqueiam este plano)
- **US26/UC20 (porção padrão por unidade de medida — adicionada nesta revisão):** ainda sem caso de teste dedicado; entra na atualização da sprint que implementar a Tela 19. Pontos a cobrir quando for detalhado: (1) cadastrar ingrediente com unidade ainda não registrada em `unidade_medida_porcao_padrao` deve ser rejeitado pela FK `fk_ingrediente_unidade_medida`; (2) alterar o padrão de uma unidade não deve recalcular `porcao_padrao` de ingredientes já cadastrados; (3) um ingrediente pode gravar `porcao_padrao` diferente do padrão sugerido para sua unidade (caso do Açaí em `kg`).
- **Licença Gurobi (atualizada em 27/10/2026):** os testes usam a **licença restrita do `pip`** (pesquisa, desenvolvimento e avaliação; até 2.000 variáveis e 2.000 restrições), que basta para todos os casos. Existe uma WLS acadêmica, **pessoal**, do Kauê (até 25/09/2027); ela não é usada por outros integrantes nem nos testes automatizados. No Render, o solver só roda com autorização escrita da Gurobi (plano B1 enquanto isso; CT72). Gurobi **13.0.3**. *Texto anterior (08/10):* WLS acadêmica, Gurobi 13.0.1. O `gurobipy` instalado via pip também vem com uma licença gratuita "size-limited" (até 2000 variáveis/2000 restrições), suficiente para os testes unitários no volume de uma açaiteria. Se decidirem rodar o solver real também nos testes de integração/CI no futuro, a WLS se encaixa melhor por não depender de uma máquina específica — um node-locked trava por hardware, o que é problemático em runners de CI, que trocam de máquina a cada execução.

> **Histórico — substituído em 27/10/2026 (não há Compute Server; os testes automatizados usam dublês do Python e, opcionalmente, o Python real com `MF_PYTHON`):** em 28/09/2026 (revisado em 02/10 e 08/10) o grupo tinha adotado o **Gurobi Compute Server** no desenvolvimento (`docs/Apoio/Arquitetura.md`, seção 3.3): cada desenvolvedor (Kauê e Lucas, por enquanto) roda o próprio servidor localmente, com a própria licença WLS acadêmica, o que evita o conflito do limite "1 job por vez, sem fila" entre pessoas diferentes testando ao mesmo tempo. Os testes **unitários** do `otimizador.py` (CT23, CT24) continuam podendo rodar sem Compute Server. Em **produção**, a licença será a do Lucas e o grupo decidiu pela **Alternativa C**: licença WLS usada diretamente no container do backend, sem Compute Server (a "Opção A" foi descartada — `docs/Apoio/Arquitetura.md`, seção 3.3). O que continua valendo: um **CI automatizado** (ex.: GitHub Actions) roda numa terceira máquina, sem licença Gurobi própria — qualquer teste de integração que chamasse o Gurobi de verdade precisaria de uma licença dedicada para o runner. Recomendação: nos testes automatizados, usar um **dublê** (mock) do `OtimizadorProducao` para não depender de licença Gurobi durante o CI, e deixar a chamada real para o teste manual/aceitação.
- **Meta de cobertura de teste:** não fixada agora (seção 2). Revisitar a partir da E5, com base de código real para medir.
- **Ferramenta de teste de frontend:** não decidida agora, propositalmente (seção 1).
