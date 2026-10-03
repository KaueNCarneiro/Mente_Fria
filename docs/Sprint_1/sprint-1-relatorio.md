# Relatório de Entrega — Sprint 1 — Mente Fria

* Período: 11/09/2026 a 18/09/2026 (E5; Sprint 1 comprimida em 1 semana)
* Sprint Review: Não foi realizada a sprint review.

> **Meta real da sprint:** a equipe planejou entregar primeiro a **infraestrutura** (comunicação banco ↔ backend, Python ↔ Java e estrutura do repositório no GitHub) para só então começar o CRUD (US1–US7). Por isso as histórias funcionais do backlog não eram o alvo desta sprint, apesar de estarem marcadas como "Sprint 1" no Backlog Priorizado.
>
> **Resultado:** a meta não foi cumprida. A estrutura do Git ficou desorganizada, o backend não chegou a se comunicar com o banco, e o repositório foi excluído e recriado na Sprint 2. Por isso os commits e PRs da Sprint 1 não existem mais; este relatório se apoia nos documentos preservados e no que a equipe relata.

## 1. Planejado vs. entregue
| Item / História (E2) | Planejada para esta sprint? | Entregue? | Observação |
|---|---|---|---|
| Infra: comunicação banco ↔ backend (Java ↔ PostgreSQL) | Sim | Não | O backend foi criado, mas nunca conectou ao banco. Concluída na Sprint 2 |
| Infra: comunicação Python ↔ Java | Sim | Não | O contrato JSON foi definido (Contrato de Comunicação §1) e o roteiro de Hello World escrito; sem execução integrada. Módulo Python entregue na Sprint 2 |
| Infra: estrutura do repositório no GitHub | Sim | Não | Estrutura de pastas e conflitos inviabilizaram o uso; repositório excluído e recriado em 27/09/2026 |
| US8 — Repositório Git com README | Sim | Parcial | README e documentação escritos, mas o histórico de commits foi perdido na exclusão |
| Banco de dados (apoio às US4–US7) | Sim | Sim (documentos) | `Script_DDL.sql` com seed, `DER.md`, constraints nomeadas, `mapa_constraints.md` e SQL dos repositórios prontos |
| US1–US7 — CRUD de usuários, ingredientes, produtos, preço e validação | Não | Não | Adiadas por decisão da equipe até a infraestrutura funcionar; continuam pendentes |

## 2. Incremento funcional demonstrável
Não houve funcionalidade rodando de ponta a ponta. O que existe como base, preservado no repositório atual:

- Banco: `docs/Script_DDL.sql` (PostgreSQL 16, com seed) e `docs/DER.md`.
- Contratos: `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` e `docs/Apoio/mapa_constraints.md`.
- Roteiros e protótipo: `docs/Sprint_1/sprint1-roteiro.md` e `docs/Roteiro do Protótipo Navegável — Mente Fria.md`.

Para reproduzir o banco: README, seção "Banco de dados" (Docker `postgres:16` + `Script_DDL.sql`).

## 3. Backlog atualizado
`docs/Sprint_1/sprint-1-backlog.md`

Mudanças de status: nenhuma história foi concluída; a infraestrutura foi replanejada para a Sprint 2.

## 4. Evidências de teste
- Sem testes automatizados de backend e banco nem de integração nesta sprint.
- Detalhe: Não houve testes, apenas revisões para reorganização na sprint 2

## 5. Retrospectiva e contribuição individual
- Ata de retrospectiva: `docs/Sprint_1/sprint-1-retrospectiva.md`
- Relatórios individuais de contribuição:`docs/Sprint_1`

## 6. Riscos/impedimentos para a próxima sprint
- Infraestrutura ainda sem funcionar (banco ↔ backend, Python ↔ Java, repositório): prioridade total da Sprint 2.
- Perda do histórico de commits e PRs da Sprint 1, que afeta a rastreabilidade (US8) e a avaliação individual.
- Atraso acumulado do CRUD (US1–US7) e das histórias da Sprint 2.
- Versões divergentes entre a Arquitetura (JDK 21, Spring Boot 3.3.4) e o `pom.xml` (Java 25, Spring Boot 4.0.8).
