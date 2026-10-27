# Análise do Roteiro da Sprint 3 (v4.6) — Mente Fria

**Data:** 14/10/2026 · **Documento:** `sprint-3-roteiro.md` (revisão v4.6 de 14/10/2026, Kauê e Diogo) · **Base citada pelo roteiro:** `main`, commit `a872561` (11/10/2026)
**Revisão de 14/10 (tarde):** incorpora as respostas da equipe e a auditoria da `main` atual (`HEAD` `ac028cd`, merge do PR #24 de 14/10; o schema, o código e os documentos auditados são os do commit `1ddb5ae`, e o `ac028cd` só acrescentou dois arquivos de Backlog das Sprints 1 e 2).
**Comparado com:** Backlog Priorizado (Word), Documento de Visão, Termo de Aceite, `Contrato_de_Comunicacao`, `Arquitetura`, `Plano_de_Testes`, `Script_DDL.sql` e seed, `pom.xml`, `application.properties`, `otimizador.py` e testes, `frontend/` (README, `app.js`, `api.js`, testes Playwright), relatórios e retrospectivas das Sprints 1 e 2.

---

## 1. Veredito

O roteiro v4.6 é **sólido** e **está condizente, completo e atualizado o bastante para virar tutorial**. Ele corrigiu o que faltava na versão anterior: estado real do código, calendário, escopo por prioridade, contratos, regras de estoque e riscos. As ressalvas abaixo **não impedem** ninguém de começar hoje.

| Pergunta | Resposta |
|---|---|
| Está condizente com as pendências das Sprints 1 e 2? | **Sim.** 2 itens menores faltam (seção 2) |
| Abrange tudo o que precisa ser feito na Sprint 3? | **Sim, com uma ressalva de escopo:** ele cobre todas as histórias, mas **move para a Sprint 4** a maior parte do que o Backlog marcava como Sprint 3 (US14 a US19, US26, US27). É uma decisão da equipe, registrada e justificada no próprio roteiro (seção 3) |
| Está atualizado com o repositório? | **Sim.** Auditei a `main` atual (clonada do GitHub): o roteiro só não viu o PR #24 (Fase 0, já feita) e os documentos que você atualizou depois de 11/10. A lista final do que **de fato falta** está na seção 4 |

**O que encontrei de novo (verificado em execução):** 4 pontos do desenho do banco e do backend que, se seguidos ao pé da letra, geram erro ou retrabalho. O mais grave é o `CHECK` do `motivo`, que **aceita `SAIDA` sem motivo** (seção 5, F1). Todos têm correção pronta nos tutoriais.

---

## 2. As pendências das Sprints 1 e 2 estão cobertas?

| Pendência herdada | Onde está no roteiro | Situação |
|---|---|---|
| Backend sem login/JWT, CORS e escrita (US1–US7) | Fases 1 e 2 | Coberta |
| Frontend só verificado com respostas simuladas | Fase 2 (regra de "gate": tela integrada ou não conta) | Coberta |
| Java → Python nunca executado | Fase 4A (Hello World com `echo`, depois a recomendação) | Coberta |
| Licença WLS só reproduzida no PC do Kauê | Fase P, item 9 (segunda máquina) | Coberta |
| `requirements.txt` fora do padrão | Fase P / decisão de versão do Gurobi | Coberta (13.0.3 no arquivo x 13.0.1 nos documentos) |
| Retrospectiva da Sprint 2 e Sprint Review nunca realizadas | Seção 10.1, item 7 | Coberta |
| Decisões D-A, D-B, B3, B6, D3 do Contrato | Fases 1 e 2; Contrato | Coberta |
| `.env` (`./backend/.env` não resolve de dentro de `backend/`) e `.env.example` | Fase 0 | Coberta |
| Constraints do banco só testadas para as tabelas da Sprint 1 | Seção 10.1 (testes de banco) | Coberta |
| Evidências com `[PREENCHER]` e `[CONFIRMAR]` das Sprints 1 e 2 | Fase 8 (DoD) | Coberta |
| Roteiro e tutoriais da Sprint 1 desatualizados (JDK 21, Spring Boot 3.3.4) | Fase 8 | Coberta |
| **CT20** (outra pessoa sobe o projeto do zero seguindo só o README) | — | **Faltando.** Incluí no tutorial do Diogo |
| **Documentar a perda do histórico do Git** (pedido do relatório da Sprint 2) | — | **Faltando.** Incluí no tutorial do Kauê (relatório) |

---

## 3. Cobertura da Sprint 3 contra o Backlog

Conferi tamanho e prioridade do roteiro com o Backlog (Word): **batem em todas as histórias**. O roteiro divide assim, com o calendário informado: a **Sprint 3 termina em 16/10 (até 18/10 com extensão)** e a **Sprint 4 em 30/10**, seguida da janela de testes (31/10 a 06/11).

| Entra na Sprint 3 (núcleo, até 16/10) | Extensão (até 18/10, se couber) | Sprint 4 |
|---|---|---|
| Fase 1 (segurança, erros, CORS com `PATCH`), Fase 2 (US1–US7 reais), mudanças do banco, Fase P (Python), frontend integrado e `app.js` dividido | US9 (estoque por linhas), US12 (pedido com FEFO), Fase 4 (recomendação de produção, US10/US11) | US13, US14, US15, US16, US17, US18, US19, US26, US27, US20 (deploy), retrospectiva |

**Aviso de risco (já registrado no roteiro, vale repetir):** a Sprint 4 passa a carregar **4 histórias `Must`** (US14, US17, US18, US20) mais o que sobrar da extensão, em cerca de 12 dias e sem folga para correção de erros. Se a extensão da Sprint 3 não fechar US9, US12 e a Fase 4, o corte pelo MoSCoW do Backlog é: primeiro US26 e US19 (Could), depois US13 (Could), depois US15 e US16 (Should); os `Must` não se cortam.

**Observação:** o roteiro adiciona três itens que **não estão no Backlog atual** e precisam entrar nele: **US27** (cancelar pedido), a reposição com orçamento (US14, que no Backlog é só "consumo de um período X e preço atual") e a mudança de receita para lucro (US10/US11). Está na lista de documentos do Diogo (seção 10.1).

---

## 4. Está atualizado com o repositório?

**Conferi contra os arquivos e bate:** `otimizador.py` usa `float` e não lê credenciais; são 8 testes Python; o backend só tem `GET /api/saude` e `GET /api/ingredientes`; o `pom.xml` não tem Security, JWT, validação nem Testcontainers; `application.properties` lê `./backend/.env`; o frontend tem as telas de cadastro/login, funcionários, ingredientes, produtos e preço, os itens "Dashboard, Porção Padrão, Estoque, Produção, Pedidos, Clientes, Reposição e Marketing" como indisponíveis e 22 verificações Playwright com API simulada; a Termo diz "maximizar a receita"; os tamanhos e prioridades do Backlog.

**Auditoria da `main` (clonei o repositório e conferi arquivo por arquivo):** a `main` está **dois commits à frente** do que o roteiro viu: o PR #24 (Lucas, 14/10, `1ddb5ae`) e os documentos que você comitou.

**Já está na `main` — não precisa refazer (nem entra na lista do Diogo):**

| Item | Onde |
|---|---|
| **Fase 0 inteira:** `.env` em `backend/`, `application.properties` lendo `./.env` e `backend/.env.example` criado | PR #24 |
| Terminologia **"Programação Linear Inteira pura"** | Visão, Termo, Backlog (US10), Plano de Testes (R3 e §4.1), Arquitetura, README, UML |
| Visão: lista das entidades ("6+ entidades") corrigida | Visão |
| Termo: item 2 (sem "validade" no ingrediente) e item 10 (histórico de preços) corrigidos | Termo |
| Backlog: US19 aponta o item 17; US26 incluída | Backlog, `Diagramas_UML.md` |
| Arquitetura: Alternativa C, Gurobi 13.0.1, licença do Lucas, uso acadêmico | `Arquitetura.md`, README |
| Contrato: B3, B6 e D3 confirmados; nota do Compute Server; §1.5 (Hello World `echo`) mantida | Contrato |

**Ainda falta de fato — lista final do Diogo (documentos):**

| # | O que falta | Onde (linhas segundo `pandoc -t plain`) |
|---|---|---|
| 1 | **Receita → lucro** no que o otimizador maximiza (decisão confirmada; o Termo é aprovado: **avisar o professor antes**) | Visão **L29, L37, L70, L72**; Termo **L19, L25**; Backlog **US6 (L16), US10 (L24), US11 (L26), US17 (L38)**; README **linha 3** |
| 2 | Backlog: **US9** ("saída de produtos" → baixa de **ingredientes**); **US14** (reposição com orçamento); **US27** (nova); coluna **Sprint alvo** (US13 a US19, US26 e US27 passam à Sprint 4) | Backlog |
| 3 | **DER** e **`bd/Script_DDL.sql`**: linhas de estoque (`quantidade_restante`), `motivo`, status e cancelamento do pedido, seed com estoque 0 | `docs/DER.md`, `bd/Script_DDL.sql` (patch pronto) |
| 4 | **`mapa_constraints.md`** das tabelas `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque` (o arquivo diz que "entram na Sprint 2") | `docs/Apoio/mapa_constraints.md` |
| 5 | **Plano de Testes:** CT23/CT24 passam a falar de lucro; CT29 passa a ser por **linha**; novos CTs (CT30 em diante) para US9, US12 e Fase 4 | `docs/Plano_de_Testes_Mente_Fria.md` |
| 6 | **Arquitetura:** D5 (reposição deixa de ser só Java); estoque por linhas; `@Profile("dev")`; fuso horário; Contrato v2 (`custo_unitario`, `margem_estimada`, `consumo`) e Contrato REST de estoque/pedidos/recomendação | `Arquitetura.md`, Contrato |
| 7 | **Referências antigas** (`sql_repositorios_sprint1.md`, `constraints_sprint1_teste.sql`, `docs/Script_DDL.sql`, `ARQUITETURA.md`) em documentos **vivos**: Plano de Testes (5), `mapa_constraints.md` (2), Contrato (7), `sprint-1-roteiro.md` (12). Relatórios de sprint **não** se reescrevem (são histórico) | vários |
| 8 | **Diagramas_UML.md:** `calcularQuantidadeOtimaProducao` (lucro) e a reposição com orçamento | UML |
| 9 | **Documentos da Sprint 3:** `docs/Sprint_3/` não existe; retrospectiva da Sprint 2 (nunca feita); CT20; registro da perda de histórico do Git | `docs/Sprint_3/` |
| 10 | **`optimization/requirements.txt`:** está em **UTF-16** e fixa `gurobipy==13.0.3`; a Arquitetura decidiu **13.0.1** | Kauê (com o Diogo) |

---

## 5. Pontos de atenção (o que o roteiro não percebeu)

**Status (14/10): a equipe confirmou F1 a F11. A F12 foi adiada para decisão posterior e está registrada em `Registro_de_Decisoes_e_Pendencias_Sprint3.md`.** Cada tutorial explica o **porquê** de cada correção ao lado do passo em que ela aparece.

| # | Ponto | Gravidade | Quem | Correção |
|---|---|---|---|---|
| **F1** | **`CHECK` do banco.** O roteiro pede "CHECK amarrando tipo e motivo" e `quantidade_restante IS NULL OR BETWEEN 0 AND quantidade`. Em SQL, `motivo IN (...)` com `motivo NULL` dá `NULL`, e o `CHECK` **aceita**: uma `SAIDA` **sem motivo** passa. E `IS NULL OR ...` deixa passar `ENTRADA` sem `quantidade_restante` (quebra a baixa e o invariante). Além disso, um `CHECK` que reprova também o tipo inválido **esconde** a constraint certa (`chk_movimentacao_tipo`). **Verificado:** meus testes reprovaram exatamente esses 3 casos até o DDL ser corrigido | **Alta** | Diogo | DDL corrigido (`IS NOT NULL` explícito, `<>` em vez do domínio) e **41 testes** passando (tutorial do Diogo) |
| **F2** | **Saldo de abertura (D11) ainda é decisão do Diogo**, e o seed oficial hoje **viola** o invariante `quantidade_estoque = soma(quantidade_restante)` (tem 40 kg de açaí e nenhuma linha). Sem decidir, o teste do invariante "sempre" falha. Contradiz o "Não há dúvidas abertas" da seção 11 | **Alta** | Diogo | Recomendo a proposta (a) do roteiro: **seed oficial com estoque 0**; estoque de demonstração só no `seed_demo.sql` |
| **F3** | **Fuso horário.** "Vencida = `data_validade` < data do servidor". O Render roda em UTC e o Brasil está em UTC−3: das 21h à meia-noite, uma linha que vence "hoje" já seria tratada como vencida | Média | Lucas | O Java calcula `hoje` com `ZoneId.of("America/Sao_Paulo")` e **passa como parâmetro** nos SQLs; nunca `CURRENT_DATE` do banco |
| **F4** | **`/api/teste/python` sob Spring Security.** Sem configuração, passa a exigir token, e a tela de comunicação chama sem token; liberar em produção seria falha de segurança | Média | Lucas | Controller com `@Profile("dev")` (só existe no perfil de desenvolvimento), com `permitAll` somente nesse perfil |
| **F5** | **jjwt e Boot 4.** A decisão do JWT (25/09) é anterior à troca para o Spring Boot 4.0.8 (27/09). O `jjwt` funciona, mas o `jjwt-jackson` traz o Jackson 2 junto do Jackson 3 do Boot. Os módulos do Testcontainers 2 também mudaram de nome | Média | Lucas | `jjwt` 0.13.x, conferir com `dependency:tree`; plano B `jjwt-gson`. Coordenadas do Testcontainers no tutorial |
| **F6** | **Exatidão do solver.** O `MIPGap` padrão do Gurobi é 1e-4 (aceita plano até 0,01% pior que o ótimo) | Média | Kauê | `MIPGap = 0`, `MIPGapAbs = 0` mais a verificação inteira (código verificado) |
| **F7** | **Coordenação Diogo (repositórios Java) x Lucas (controllers).** Dois integrantes no mesmo pacote em 2 dias, com o histórico de conflitos da Sprint 1 | Média | Lucas e Diogo | Combinar **antes** as assinaturas dos repositórios e quem cria cada arquivo (passo explícito nos dois tutoriais) |
| **F8** | **Dados para a Fase 4.** Com estoque 0 no seed oficial, a recomendação real dá tudo zero; só o `seed_demo.sql` produz um plano de verdade | Baixa | Diogo e Kauê | Números esperados para o seed de demonstração nos tutoriais (plano 151/1/0, lucro 1011,71) |
| **F9** | **Termo de Aceite assinado.** Trocar receita por lucro e mudar o escopo da reposição altera um documento aprovado pelo professor (28/08) | Média | Diogo (docs) | Registrar a mudança e **avisar o professor** antes de editar |
| **F10** | **US9 no Backlog** diz "saída de produtos"; a equipe confirmou que é baixa de **ingredientes** | Baixa | Diogo | Item na lista de documentos |
| **F11** | **`consumo` do Contrato v2**: o roteiro mostra só os ingredientes consumidos. Interpretei assim (lista só os de consumo maior que zero) | Baixa | Kauê | Confirmar; implementado e testado |
| **F12** | **Sprint 4 (para não esquecer):** "ingredientes mais utilizados" do dashboard x consumo da reposição (esta inclui pedidos cancelados, aquele não) precisam da mesma definição | Baixa | PO | Definir antes da Fase 6 |

---

---

## 6. O que foi verificado em execução (base dos tutoriais)

| Item | Resultado |
|---|---|
| Schema novo do roteiro no PostgreSQL 16 | Aplicado; 41 testes de constraints (T33–T73) passando, **depois** de corrigir F1 |
| Baixa FEFO por linhas e descarte | Cenários do roteiro reproduzidos (saldo total 35 e válido 15; contraexemplo da linha nova com validade menor: 15 e 10; baixa atravessando linhas; só vencido recusado) |
| Concorrência | 30 de 30 rodadas: com dois pedidos de 30 sobre 50, **um** passa e o saldo fica 20; com ordem de bloqueio invertida, **sem deadlock** |
| `seed_demo.sql` | Roda, o invariante fecha, totais conferidos à mão (271,50; 326,40; 346,20; 251,70) |
| Módulo Python v2 (lucro, 2 estágios, `consumo`, eco) | **24 testes** passando (incluindo 200 instâncias aleatórias contra um oráculo de força bruta); o experimento de quebrar de propósito derruba os testes certos |
| Lógica de JavaScript do frontend | Verificada em Node (permissões, saída convencional ou descarte, motivo do zero, total do pedido) |
| **Não verificado** | Compilação do Java (sem acesso ao Maven Central), Docker/Windows, navegador e Playwright, licença WLS, Python 3.14.7 |

---

## 7. Ordem de execução (hoje, 14/10, até o fim do núcleo em 16/10)

| Quando | Kauê | Diogo | Lucas | Leonardo |
|---|---|---|---|---|
| **Hoje (14/10)** | Contrato v2; Fase P (docs, testes, código) | **Decidir D11**; DDL corrigido; testes T33–T73 | Fase 0 (`.env`); Fase 1; combinar assinaturas com o Diogo | Baseline de testes; dividir o `app.js` |
| **15/10** | Ambiente Gurobi (2ª máquina); PR da Fase P | Seed de demonstração; SQLs; documentos | Fase 2 (auth e CRUD) | Aceite real da Fase 2, com o backend do Lucas |
| **16/10 (fim do núcleo)** | Facilitar a revisão; relatório da Sprint 3 | PR do banco; documentos | PRs revisados; aviso ao Leonardo | Evidências; PRs |
| **17 a 18/10 (extensão, se couber)** | Fase 4A em dupla com o Lucas | SQLs de US9/US12 (já prontos) | US9, US12 | Telas 10/16, 11/17 |

**Caminho crítico:** Lucas (Fase 1 e 2) libera o Leonardo (aceite real); Diogo (D11 e DDL) libera o Lucas (US9/US12) e o Kauê (dados para a Fase 4).

---

## 8. O que os tutoriais assumem

1. **O roteiro v4.6 é a fonte de verdade** (decisões D1–D22, calendário, contratos propostos). Onde ele ainda marca "proposta", os tutoriais seguem a proposta **e dizem que ela é proposta**.
2. **Itens do roteiro que não se alteraram:** divisão de funções (Diogo escreve os repositórios Java, Kauê em dupla na Fase 4A, Leonardo divide o `app.js`), `PATCH` com CORS, Hello World com `echo` até a Fase 4 validar.
3. **Desvios que eu fiz, com motivo:** `CHECK`s mais estritos (F1); `hoje` como parâmetro (F3); `seed_demo.sql` sem criar usuários (usa o Administrador e o Funcionário criados pela tela, assim não há hash de senha no repositório e o `POST /api/auth/registro` não fica bloqueado); validação manual explícita no backend (mensagens por campo determinísticas) em vez de Bean Validation.
4. **Sprint 4 fica fora dos tutoriais**, apenas sinalizada ao final de cada um.
