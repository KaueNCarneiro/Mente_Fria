# Relatório de Entrega — Sprint 4 — Mente Fria

> **RASCUNHO de 15/11/2026.** Feito só com fatos verificáveis no repositório, no GitHub e nas evidências de `docs/Sprint_4/evidencias/`. **Falta a revisão do Kauê (facilitação) e a aprovação do grupo.** A retrospectiva é um documento à parte: `docs/Sprint_4/sprint-4-retrospectiva.md` (§6).

> **Base desta revisão:** `main` em **15/11/2026**, commit [`3acf6af`](https://github.com/KaueNCarneiro/Mente_Fria/tree/3acf6af) (PR #123). Horários em Brasília.

- **Período:**
  - execução planejada: qui 29/10 a sex 06/11/2026 (roteiro da Sprint 4);
  - semana de correção de bugs: até 13/11 (A43);
  - trabalhos registrados no GitHub: de 29/10 (PRs #40 a #43) a 15/11 (PR #123).
- **Sprint Review:** **não realizada** (informado pelo Diogo em 15/11). A Review com demonstração estava marcada para sex 06/11. Nessa data, o sistema não estava na `main` nem publicado (§1.3).
- **Retrospectiva:** das Sprints 2, 3 e 4, **sem reunião**, como registro de análise (informado pelo Diogo em 15/11; o mesmo formato do registro da Sprint 2). Na Sprint 2 houve uma ligação de retrospectiva em 02/10, com os quatro integrantes, sem ata.
- **Entrega:** junto com a Sprint 3 (A43). O relatório da Sprint 3 é `docs/Sprint_3/sprint-3-relatorio-entrega.md`. O plano era sex 06/11/2026; **o envio ao professor é em 15/11/2026** (informado pelo Diogo em 15/11).

**Resumo:**
- Todo o escopo obrigatório e a US26 estão na `main` e publicados:
  - frontend em `https://kauencarneiro.github.io/Mente_Fria/`, pelo GitHub Pages;
  - backend em `https://mente-fria-backend.onrender.com`, pelo Render.
- O fluxo do Termo de Aceite passou na URL publicada, nos dois perfis (CT68, 14/11).
- Os quatro defeitos achados na semana de bugs foram corrigidos e publicados.
- **A entrega atrasou em relação ao roteiro:**
  - o código obrigatório chegou à `main` em 11/11, e não até 04/11;
  - a publicação foi em 11/11, e não em 02/11;
  - não houve Review em 06/11;
  - os testes no ambiente publicado foram em 14 e 15/11, depois do fim previsto da semana de bugs.

## 1. Planejado vs. entregue

### 1.1 Histórias

O escopo vem do roteiro da Sprint 4, §2:
- **obrigatório:** US1–US7, US9–US12, US14, US17, US18 e US20 (A2b);
- **opcional:** o botão "Descartar" (A12);
- **condicional:** a US26 (A40, A46).

As datas da tabela são as da chegada à `main`.

| História | Planejado | Entregue na `main` | Publicado | Evidência principal |
|---|---|---|---|---|
| US1–US7 (conta, funcionários, login, ingredientes, produtos, preço, validação) | Fases 1 (qui 29/10) e 2 (sex 30/10), do Lucas, com os repositórios do Diogo | **Fase 1:** 05/11 (PR #52). **Fase 2:** 11/11, entrou na `sprint-4` sem PR e chegou à `main` pelo #85. **Repositórios:** #86/#87, 11/11 | Sim, desde 11/11 | `2026-10-29_lucas_fase1_mvnw-test.txt` (18 testes); `2026-11-11_diogo_repositorios_mvnw-test.txt` (95 testes); `2026-11-15_lucas_fases-2-3-4a-6_testes-e-contrato.txt` |
| US9 (estoque por linhas) e US12 (pedido com baixa) | Fase 3 (sáb 31/10) e Telas 10/16 e 11/17 | **Fase 3:** 11/11, sem PR (#85). **Telas:** PRs #68 e #69, na `sprint-4` em 11/11 | Sim | Fase 3: 15 testes. Telas: `2026-11-06_leonardo_estoque_npm-test.txt` e `_pedido_npm-test.txt`. CT68 (baixa conferida) |
| Botão "Descartar" (A12, opcional) | Decidido no M3 (dom 01/11) | **Entrou** com a tela de Estoque (PR #68) | Sim | `2026-11-06_leonardo_estoque_npm-test.txt` |
| US10 e US11 (recomendação por lucro e Tela 12) | Fase P (Python, Kauê), Fase 4A (Lucas) e Tela 12 | **Fase P + Q:** PR #81, na `sprint-4` em 11/11. **Fase 4A:** 11/11, sem PR. **Tela 12:** PR #70 | Sim | `2026-11-10_kaue_fasePQ_pytest.txt` (47 testes); Fase 4A: 7 testes. CT68: 150/0/0 com o estoque publicado. CT67: 151/1/0 com o seed |
| US14 (reposição com orçamento e Tela 13) | Fase Q (Python), Fase 5 (Java, Kauê) e Tela 13 | **Fase 5:** 11/11 (na `sprint-4`, `7957b05`). **Tela 13:** PR #72 | Sim | `2026-11-11_kaue_fase5_mvnw-test.txt` (8 testes) e `_python-real.txt` (3 testes). CT68 |
| US17 e US18 (dashboard e marketing) | Fase 6 (Lucas) e Telas 03 e 14 | **Fase 6:** 11/11, sem PR. **Telas:** PRs #71 e #73 | Sim | Fase 6: 5 + 4 testes. Telas: `_dashboard_npm-test.txt` e `_marketing_npm-test.txt`. CT68 |
| US20 (publicação) | Deploy na seg 02/11 (A41), na conta do Lucas | **Arquivos do deploy:** PRs #83/#89 (11/11). **Publicação do Pages:** PR #67. **Deploy:** 11/11, na conta do Diogo (A47). **Deploy automático:** desde 15/11 | Sim, desde 11/11 | `2026-11-11_diogo_deploy-render.txt`; CT67, CT69, CT70 e CT72; `2026-11-15_diogo_deploy-automatico-render.txt` |
| US15 e US16 (alertas), parciais | Cobertura do adendo, item 4, opção (a) | Situação "Abaixo do mínimo" e "Vencida" por linha e contagens no dashboard | Sim | CT68 (o dashboard mostrou "2 ingredientes abaixo do mínimo" e "1 com quantidade vencida") |
| **US26** (porção padrão por unidade), condicional | *Gate* na seg 02/11; corte na qua 04/11, 18h (A40) | Mantida em aberto pelo PO (A46, prazo 14/11). **Backend:** PRs #88/#93. **Tela 19:** PRs #96/#97. Tudo em 12/11 | Sim, desde 12/11 | `2026-11-11_diogo_us26-backend_mvnw-test.txt` (101 testes); `2026-11-12_diogo_us26-tela19_npm-test.txt` (43 verificações, 10 de 10 rodadas). O professor foi avisado pelo Lucas e está de acordo |
| US13, US19, US27; US15 e US16 completas; US21–US25 | Fora do semestre (A40) | Não implementadas | — | O menu mostra Clientes (US13) com o selo "Futuro" (PR #113) |

**Como o código chegou à `main`:**
- Os PRs #53 a #63 do Leonardo foram abertos com base na `main` e fechados sem merge. Foram refeitos com base na `sprint-4` (#64 a #74).
- As Fases 2, 3, 4A e 6 entraram direto na `sprint-4`, um commit por fase, sem PR (registrado na evidência do Lucas de 15/11).
- O PR #85 levou as fases à `main` às 17h01 de 11/11. O backend da `main` só voltou a compilar às 19h41, com os repositórios (PR #87).

### 1.2 Critérios de pronto do roteiro (§7) × situação em 15/11

| Critério (roteiro, §7) | Situação em 15/11 |
|---|---|
| Sistema publicado (Pages e Render), URL no README, com a data de expiração do banco | **Feito**, mas em 11/11, e não em 06/11. O README foi atualizado em 15/11 (PR #108): o banco vence em **11/12/2026, às 21h59** |
| Login pelos dois perfis com o backend real; `401` e `403` em JSON | **Feito:** CT68 (14/11) e `SegurancaEErrosTest` (18 testes) |
| Cadastros de US4–US7 com erros por campo vindos do backend real | **Feito nos testes automatizados** (`Fase2CrudTest`, 15 testes). **Não executado no ambiente publicado** (CT12; o CT68 não refez cadastros) |
| Estoque com validade, saída, descarte, vencido nunca vendido, situação por linha; "Descartar" | **Feito:** Fase 3 (15 testes), Tela de Estoque (#68) e CT68 (entrada pela tela) |
| Pedido com baixa por linhas, atômico | **Feito:** Fase 3 e CT68 (pedidos #9 e #10, baixa conferida com a composição) |
| Tela 12 = 151/1/0 com o seed, **na URL publicada**, sem termos técnicos | **Em parte:** 151/1/0 conferido com o seed na imagem local (CT67, 11/11). Na URL publicada deu **150/0/0**, porque o banco já não tinha o estoque do seed (CT68) |
| Reposição (Tela 13), dashboard (Tela 03) e sugestões (Tela 14) | **Feito:** CT68 |
| CT68 completo na URL publicada (qua 04/11) | **Feito em 14/11** |
| Testes: banco `32 \| 0` e `41 \| 0` | **Feito:** `2026-10-29_diogo_banco_quebrar-de-proposito.txt` (controle com o DDL correto: 41 \| 0 e 32 \| 0) |
| Testes: Python `47 passed` em 2 máquinas | **Feito:** 47 testes passaram (`2026-11-10_kaue_fasePQ_pytest.txt`). A segunda máquina foi dada por atendida pelo Diogo em 11/11: o Python rodou no Windows da máquina dele e no container do CT67 (tutorial do Kauê, 4.7) |
| Testes: backend `mvnw test` verde, com Testcontainers | **Feito:** 95 testes em 11/11 e 101 com a US26 (11/11) |
| Testes: funções de apoio 15/15 | **Sem evidência própria na Sprint 4** |
| Testes: Playwright estável ou com a instabilidade registrada (A13) | **Feito:** A13 corrigida (PR #65). A instabilidade do `python -m http.server` está registrada no `frontend/README.md`: com um servidor em Node, 10 de 10 rodadas |
| Um PR revisado por fase, na `main`; evidências em `docs/Sprint_4/evidencias/` | **Em parte:** as Fases 2, 3, 4A e 6 entraram sem PR (§1.1). Há 41 arquivos de evidência |
| Relatórios das Sprints 3 e 4, retrospectiva e contribuições individuais | **Contribuições:** os quatro entregaram em 15/11 (PRs #108, #109, #115 e #118). **Relatórios e retrospectiva:** rascunhos em 15/11 (este relatório, a revisão do relatório da Sprint 3 e `docs/Sprint_4/sprint-4-retrospectiva.md`), esperando a revisão do Kauê |
| US26 na `main` com os testes, ou fora do semestre | **Feito:** na `main` em 12/11, com os testes (A46) |

### 1.3 Calendário: planejado × real

| Marco (roteiro, §4) | Planejado | Real |
|---|---|---|
| M1: banco e Fase 1 | qui 29/10 | Banco: PR #44, na `sprint-4` em 29/10. Fase 1: 05/11 (#52) |
| M2: Fase 2 e Fase P + Q | sex 30/10 | 11/11 |
| Fase 3 | sáb 31/10 | 11/11 |
| M3: Fase 4A e decisão do "Descartar" | dom 01/11 | 11/11; o "Descartar" entrou |
| Fases 5 e 6 e deploy | seg 02/11 | 11/11 |
| Telas do frontend | até ter 03/11 | PRs abertos em 06/11, à noite; merge na `sprint-4` em 11/11 |
| Congelamento | qua 04/11, 18h | Não houve |
| Entrega e Review | sex 06/11 | **Sem Review.** Na `main` e publicado em 11/11 |
| Semana de bugs | até 13/11 | Testes no ambiente publicado em 14 e 15/11 (`sprint-4-semana-de-bugs.md`) |

## 2. Incremento demonstrável

- **Sistema publicado:** frontend `https://kauencarneiro.github.io/Mente_Fria/` e backend `https://mente-fria-backend.onrender.com` (`GET /api/saude`).
- **Fluxo do Termo de Aceite, nos dois perfis:** login → entrada de estoque com validade → recomendação de produção por lucro (Tela 12) → pedido com baixa automática → reposição com orçamento (Tela 13) → dashboard do mês (Tela 03) → sugestões de campanha (Tela 14). Conferido na URL publicada em 14/11 (CT68).
- **US26:** cadastro de unidade e porção padrão (Tela 19), só do Administrador.
- **Otimização na URL publicada:** com a licença do `pip`, autorizada por escrito pela Gurobi (A38). Há uma chave de reserva que desliga o solver sem derrubar o resto do sistema (CT72).
- **Deploy automático desde 15/11:** cada merge na `main` publica o backend. O Pages publica o frontend.
- **Setup local:** o projeto sobe do zero só com o README (CT20, 15/11).

## 3. Backlog

- O Backlog foi conferido no Word e atualizado em 15/11 (PR #120): a US26 aparece como entregue na Sprint 4 (A46).
- As histórias fora do semestre seguem a A40.
- Não há outra sprint de desenvolvimento no semestre.

## 4. Evidências de teste

Todas estão em `docs/Sprint_4/evidencias/`.

| Grupo | Evidências | Resultado |
|---|---|---|
| Banco | `2026-10-29_diogo_banco_quebrar-de-proposito.txt` | Os testes detectam constraints erradas. Com o DDL correto: 41 \| 0 e 32 \| 0 |
| Backend | `2026-10-29_lucas_fase1_mvnw-test.txt`, `2026-11-11_diogo_repositorios_mvnw-test.txt`, `2026-11-11_kaue_fase5_*.txt`, `2026-11-11_diogo_us26-backend_mvnw-test.txt` | 18 (Fase 1); 95 (suíte completa); 8 + 3 (Fase 5); 101 (com a US26). 0 falhas |
| Python | `2026-11-10_kaue_fasePQ_pytest.txt` | 47 testes passaram |
| Frontend | `2026-11-06_leonardo_*` (uma por tela), `2026-11-12_diogo_us26-tela19_npm-test.txt` e as de 15/11 | Até 39 verificações em 06/11 (10 de 10 rodadas); 43 com a Tela 19; 44 com o selo de Clientes |
| Deploy e ambiente publicado | `2026-11-10_kaue_deploy_imagem-local-parcial.txt`, `2026-11-11_kaue_ct67.txt`, `2026-11-11_diogo_deploy-render.txt`, `2026-11-11_diogo_seed-render.txt`, `2026-11-14_kaue_ct68.txt` a `_ct72.txt` | CT67, CT68, CT69, CT70 e CT72 passaram |
| Semana de bugs | `docs/Sprint_4/sprint-4-semana-de-bugs.md` e as evidências de 15/11 | 4 defeitos, os 4 corrigidos e publicados |
| Backups | `2026-11-14_diogo_backup-render.txt` e `2026-11-15_diogo_backup-render.txt` | Restauração testada num banco descartável |
| Setup do zero | `2026-11-15_diogo_ct20_setup-do-zero.txt` | CT20 passou (com os limites registrados no arquivo) |

**Limites:**
- **Execução:** a maior parte dos testes do backend, do Python, do deploy e do ambiente publicado foi executada na máquina do Diogo, a pedido dos donos de cada parte, como dizem as próprias evidências.
- **Aceite do Leonardo:** feito em 15/11, na URL publicada, nos dois perfis e com a Tela 19, com 17 prints (`2026-11-15_leonardo_aceite-pages.txt`, PR #127). Não cobriu cadastros, "Descartar" e saída de estoque.
- **_Gate_ da Fase 2:** as 16 evidências não foram feitas; o CT68 cobriu o fluxo.

## 5. Decisões da Sprint 4

Registradas em `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`:

| Decisão | Resumo |
|---|---|
| **A39** | A Sprint 3 oficial terminou; o trabalho é executado na Sprint 4, com entrega e Review em 06/11 |
| **A40** | Histórias fora do escopo obrigatório ficam fora do semestre; a US26 é condicional |
| **A41** | Deploy definitivo na seg 02/11 |
| **A42** | Conferências de corte pelo PO (M3 e *gate* da US26) |
| **A43** | Respostas do grupo: trabalho no feriado de 02/11, conta do Render do Lucas, entrega das Sprints 3 e 4 juntas, semana de bugs até 13/11 |
| **A44** | Tutoriais reescritos como material de apoio |
| **A45** | Correção do teste instável (A13) antecipada |
| **A46** | A US26 continua em aberto até 14/11, decisão do PO. Foi entregue em 12/11, e o professor foi avisado |
| **A47** | O deploy no Render passa para o Diogo, na conta dele (11/11) |

## 6. Retrospectiva e contribuição individual

- **Retrospectiva das Sprints 2, 3 e 4:** registro de análise sem reunião (informado pelo Diogo em 15/11), em `docs/Sprint_4/sprint-4-retrospectiva.md`. Ele registra o esclarecimento sobre a Sprint 2: houve uma ligação de retrospectiva em 02/10, com os quatro integrantes, sem ata (informado pelo Diogo em 15/11), ao contrário do que dizem o relatório e o registro de análise daquela sprint.
- **Contribuições individuais das Sprints 3 e 4:** entregues pelos quatro em 15/11. Estão em `docs/Sprint_3/sprint-3-contribuicao-*.md` e `docs/Sprint_4/sprint-4-contribuicao-*.md`.

## 7. Riscos até o fim do semestre

| Risco | O que fazer |
|---|---|
| O banco gratuito do Render vence em **11/12/2026, às 21h59** (a data-limite do professor é 30/11) | Backups com restauração testada: o semanal por volta de 22/11 e o de 29/11 (Diogo). Depois de 11/12, recriar a partir do backup |
| O banco só aceita conexões externas da rede do Diogo (restrição de IP, 12/11); se o IP mudar, o backup falha | Antes de cada backup, conferir o IP e atualizar a regra no Render |
| O deploy automático reinicia o backend a cada merge na `main`, inclusive merges só de documentação | Evitar merges na `main` durante uma demonstração |
| O serviço gratuito "dorme" depois de 15 minutos sem uso | Abrir a URL uns 2 minutos antes de demonstrar (o frontend espera até 90 s) |
| O CT12, os cadastros e os casos de erro não foram executados no ambiente publicado (o aceite de 15/11 também não os cobriu) | Estão cobertos pelos testes automatizados; se houver tempo, rodar no ambiente publicado antes de 30/11 |
| A licença do Gurobi | Nunca usar a WLS pessoal no Render: vale só a licença do `pip` (A38) |
