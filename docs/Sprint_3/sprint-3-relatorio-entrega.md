# Relatório de Entrega — Sprint 3 — Mente Fria

> **RASCUNHO de 28/10/2026** (decisão A39). Feito só com fatos verificáveis no repositório e nos documentos desta pasta. **Falta a revisão do Kauê (facilitação) e a aprovação do grupo.** A retrospectiva e as contribuições individuais são escritas pelos integrantes (§5).

> **Base desta revisão:** `main` em **28/10/2026**, commit [`c551b4e`](https://github.com/KaueNCarneiro/Mente_Fria/tree/c551b4e03c522567c2b9029d8bfad71cf139c6b9) (PR #39, que levou à `main` o PR #38, documentação da licença do Gurobi). A reorganização do calendário feita na noite de 28/10 (A39–A42) não entra nesta base.

- **Período:** depois do corte da Sprint 2 (02/10/2026) até o fim da Sprint 3 no cronograma do professor (**as datas oficiais de início e fim precisam ser conferidas pelo grupo antes do envio**). Os trabalhos registrados no GitHub vão de 08/10 (primeiros documentos da Sprint 3) a 28/10.
- **Sprint Review:** não realizada.
- **Retrospectiva:** a fazer, junto com a da Sprint 4, na qui 05/11 (roteiro da Sprint 4, §6).
- **Entrega:** junto com a da Sprint 4, na **sex 06/11/2026** (decisão A43).
- **Contexto:** no cronograma do professor, a Sprint 3 já terminou e o período atual é a **Sprint 4**, com entrega em **06/11/2026**. Os documentos desta pasta foram escritos na Sprint 3 e chamam de "Sprint 3" o pacote de trabalho que é **executado na Sprint 4** (A39, em `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`). O Adendo nº 1, que por equívoco de redação dizia "Sprint 3", foi corrigido para "Sprint 4" (A43).

**Resumo:**
- A Sprint 3 foi uma sprint de **planejamento e preparação**.
- Ela entregou na `main`:
  - o plano da sprint e as decisões;
  - o contrato de comunicação v2;
  - o Adendo nº 1 aprovado pelo professor;
  - os casos de teste novos;
  - a documentação atualizada;
  - o **código de todas as histórias do escopo, escrito e testado numa pasta de referência** (`docs/Sprint_3/referencia/`).
- **Nenhuma funcionalidade nova chegou ao código real** (`backend/`, `bd/`, `optimization/`, `frontend/`). Ele continua no estado da Sprint 2. Levar esse código às pastas reais, por PR, é o trabalho da Sprint 4.

## 1. Planejado vs. entregue

### 1.1 Histórias

O escopo foi fechado em 26/10 (decisão A2b). "Pronto na referência" quer dizer escrito e testado em `docs/Sprint_3/referencia/`, **não** no código real.

| História | Situação no código real (`main`) | Situação na referência | Observação |
|---|---|---|---|
| US1–US7 (conta, funcionários, login, ingredientes, produtos, preço, validação) | Como na Sprint 2: só `GET /api/saude` e `GET /api/ingredientes`; telas com respostas simuladas | Pronto: Fases 1 e 2 (18 + 15 + 13 testes) | Entra na Sprint 4 (Fases 1 e 2) |
| US9 (estoque por linhas) e US12 (pedido com baixa) | Não implementadas | Pronto: Fase 3 (15 testes) e banco (41 testes novos) | Sprint 4 |
| US10 e US11 (recomendação por lucro e visualização) | Otimizador v1, por receita (8 testes) | Pronto: Python v2 (25 testes) e Fase 4A (7 testes) | O lucro foi aprovado no Adendo nº 1 |
| US14 (reposição com orçamento) | Não implementada | Pronto: Python (22 testes) e Java (8 testes) | Sprint 4 |
| US17 e US18 (dashboard e marketing) | Não implementadas | Pronto: Fase 6 (5 + 4 + 3 testes) | Sprint 4 |
| US20 (publicação) | Não publicada | Pronto: `Dockerfile`, `render.yaml` e *workflow* do Pages; imagem testada com os limites do plano gratuito | Deploy definitivo na Sprint 4 (A41) |
| US15 e US16 (alertas) | Não implementadas | Cobertura parcial no backend (estoque válido, situação por linha, contagens do dashboard) | Adendo, item 4, opção (a) |
| US13, US19, US27 | Não implementadas | Não implementadas (só o banco tem as tabelas e colunas) | Fora do escopo (A2b); fora do semestre (A40) |
| US26 (porção padrão) | Critérios 1 e 2 já funcionam na tela de ingredientes do frontend da `main` (`frontend/js/app.js`: a porção é sugerida pela unidade e pode ser alterada), conferidos com a API simulada: o `GET /api/unidades-medida` real entra na Fase 2 | Banco pronto (T72, T73); `POST`/`PATCH` e Tela 19 não implementados | Fora do escopo (A2b); condicional na Sprint 4 (A40) |

### 1.2 Entregas de planejamento e documentação (todas na `main`)

| Entrega | Onde | Quando |
|---|---|---|
| Roteiro v4.6 e registro de decisões | `docs/Sprint_3/historico/` | Escritos até 14/10; publicados no GitHub só em 27/10 (PRs #26 e #27) |
| Correção do carregamento do `.env` no backend | PR #24 | 14/10 |
| Auditoria do plano, roteiro v5–v5.3, tutoriais por integrante, matriz de rastreabilidade | `docs/Sprint_3/` | 26–28/10 |
| Decisões A1–A38 | `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md` | 26–28/10 |
| **Contrato de comunicação v2** | `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` | 27/10: PR #28 (para o `sprint-3`) e PR #30 (para a `main`); ajustes nos PRs #34 e #35 |
| **Adendo nº 1 ao Termo de Aceite**, aprovado e assinado pelo professor | `docs/Sprint_3/sprint-3-adendo-termo-de-aceite.md` | 27/10 |
| Casos de teste CT30–CT72 no Plano de Testes | `docs/Plano_de_Testes_Mente_Fria.md` | 27/10 (A37) |
| Backlog, Visão, Arquitetura, DER, UML, mapas e README atualizados pelo adendo | `docs/` | 27/10 (A37) |
| Autorização escrita da Gurobi para a licença do `pip` no Render | `docs/Sprint_3/sprint-3-licenca-gurobi.md`, §0 | 28/10 (A38; PRs #38 e #39) |
| Código de referência completo, com testes | `docs/Sprint_3/referencia/` | 26–28/10 |

## 2. Incremento demonstrável

**No código real:** nada novo em relação à Sprint 2.

**Na referência** (demonstrável rodando os testes):
- backend com **95 testes**: 18 de segurança, 13 da regra do açaí, 57 de integração com PostgreSQL 16 real (Testcontainers) e 7 com o Python real;
- Python com **47 testes** (25 da produção e 22 da reposição), conferidos por oráculos de força bruta;
- banco com **32 + 41** testes de restrições;
- funções de apoio das telas novas com **15/15**;
- imagem de deploy testada com 512 MB e 0,1 CPU.

**Ainda não demonstrável:** nada roda de ponta a ponta no código real; nada está publicado.

## 3. Backlog

O Backlog foi atualizado em 27/10, pelo adendo (A37), e em 28/10, pelo calendário oficial (A39, A40). O escopo obrigatório entra na Sprint 4, que vai até 06/11.

## 4. Evidências de teste

| Evidência | O que está confirmado | Limite |
|---|---|---|
| Testes da referência (backend, Python, banco, frontend) | Executados durante a auditoria (26–28/10); números no §2 e em `docs/Sprint_3/sprint-3-roteiro.md`, §2 | Rodaram sobre a referência, **não** sobre o código real |
| `docs/Sprint_3/evidencias/` | Pasta criada | **Sem arquivos de evidência** até 28/10 |

## 5. Retrospectiva e contribuição individual

- **Retrospectiva da Sprint 3:** a fazer pelo grupo na qui 05/11, junto com a da Sprint 4 e a da Sprint 2, que nunca foi realizada (roteiro da Sprint 4, §6).
- **Contribuição individual:** uma por integrante, no padrão de `docs/Sprint_2/sprint-2-contribuicao-*.md`, a escrever por cada um até qui 05/11.

## 6. Riscos e impedimentos para a Sprint 4

- **Integração:** todo o código ainda precisa chegar à `main`, fase a fase, até 06/11. Os PRs começam na qui 29/10 (roteiro da Sprint 4, §4).
- **Ambiente:** o Docker não foi conferido nas máquinas do Lucas, do Kauê e do Leonardo.
- **Publicação:** o primeiro contato com o Render será no deploy definitivo, na seg 02/11 (A41).
- **Do PR #24 (14/10) até 26/10, nada foi para o GitHub;** o roteiro v4.6, escrito até 14/10, só foi publicado em 27/10. O trabalho só local foi o maior atraso da sprint.
