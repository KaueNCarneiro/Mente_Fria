# Relatório Individual de Contribuição — Sprint 3 — Lucas Gabriel Valadares Bassi (RA 2840482423008)

**Papel nesta sprint:** Product Owner / Responsável por Backend

> A Sprint 3 oficial foi de planejamento e preparação (roteiro, Contrato v2, código de referência testado, Adendo nº 1). O código dela entrou na `main` na Sprint 4 (A39). Este relatório é entregue junto com o da Sprint 4 (A43).

## 1. O que fiz
| Item | PR/commit | Status |
|---|---|---|
| Correção do carregamento do `.env` no backend (o Spring passou a ler o arquivo pelo caminho definido no `application.properties`) | commit `1ddb5ae` (`fix: corrige carregamento do env no backend`), PR #24 | Feito |
| Integração do `.env.example` do backend na `sprint-3` | Merge do PR #22 (`chore/adicionar-env-example`) | Feito |

## 2. Rituais que participei
- [X] Dailies/weeklies
- [ ] Sprint Review (a Sprint 3 não teve Review própria: foi entregue junto com a Sprint 4, A43)
- [ ] Retrospectiva (feita junto com a das Sprints 2 e 4, no encerramento)

## 3. PRs de colegas que revisei
| PR | Autor | Comentário resumido |
|---|---|---|
| #22 — `chore/adicionar-env-example` | sixtxxn | Revisor indicado no PR; integrei o modelo de variáveis de ambiente do backend na `sprint-3` (merge `e3ea5b8`) |

## 4. Dificuldades e o que aprendi
A Sprint 3 foi de planejamento: o código de todas as histórias foi escrito e testado em `docs/Sprint_3/referencia/` e só chegou à `main` na Sprint 4. A minha principal dificuldade técnica foi de novo o arquivo `.env`: o backend não encontrava as variáveis do banco conforme a pasta de onde era iniciado, e o erro `Could not resolve placeholder` só foi resolvido quando o caminho do `.env` passou a ser definido no `application.properties` (PR #24).

Aprendi que configuração e segredos precisam de um modelo versionado (o `.env.example`) separado do arquivo real, que nunca vai para o Git, e que o caminho relativo depende da pasta de onde o Java é iniciado. Também acompanhei as decisões de escopo e de deploy registradas no planejamento (licença do Gurobi, Render, Adendo nº 1), que definiram o trabalho da Sprint 4.
