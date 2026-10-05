# Relatório Individual de Contribuição — Sprint 1 — Diogo Pereira Miranda (RA 2840482423006)


**Papel nesta sprint:** Responsável por dados (Banco de Dados) — nesta sprint, também conduzi boa parte da definição de arquitetura do sistema junto com o apoio da equipe.

**Período coberto:** 11/09/2026 a 18/09/2026. Esta sprint foi majoritariamente de **planejamento e decisão de arquitetura**.

---

## 1. O que fiz

| Item | Evidência (arquivo / documento) | Status |
|---|---|---|
| Conduzida a definição de 5 decisões de arquitetura do sistema: banco de dados PostgreSQL; comunicação Java↔Python por subprocesso (em vez de um servidor HTTP à parte); autenticação por JWT; acesso a dados via Spring `JdbcTemplate` (em vez de JPA/Hibernate, por falta de experiência da equipe com a ferramenta); escopo do módulo Python limitado à recomendação de produção | `docs/ARQUITETURA.md` (criação) | Criado — merge não se aplica (repositório ainda não existia neste formato) |
| Identificadas e propostas 3 correções de inconsistência entre o Termo de Aceite, o Backlog e os demais documentos do projeto: um item fora do escopo aceito que continuava no Termo; uma referência cruzada errada na história US19 do Backlog; o local onde a validade do ingrediente deveria ser registrada | Correções entregues em formato de texto | Propostas — a aplicação nos documentos oficiais foi **conferida só na Sprint 2** (ver relatório daquela sprint) |
| Adaptado o roteiro de implementação da Sprint 1 ("Hello World" entre banco, backend e Python), convertendo a versão original do grupo (MySQL + FastAPI + JPA/Hibernate) para a arquitetura decidida (PostgreSQL + subprocesso Python + `JdbcTemplate`) | `Roteiro_Sprint1_Detalhado_PostgreSQL_v2.md` | Criado |

**Observação sobre as evidências:** como já registrado no relatório da Sprint 2, o histórico de commits/PRs do repositório anterior não foi preservado na recriação feita pelo Kauê. Os itens acima não têm número de PR ou branch rastreável por esse motivo.

---

## 2. Rituais que participei

- [x] weeklies 1 de 1
- [ ] Sprint Review
- [ ] Retrospectiva

---

## 3. PRs de colegas que revisei

*(Nesta fase inicial, o trabalho foi principalmente de definição de arquitetura e documentos, não de revisão de código de colegas.)*

| PR | Autor | Comentário resumido |
|---|---|---|
| Sem registro: o repositório original foi excluído e o histórico de PRs foi perdido | — | — |

---

## 4. Dificuldades e o que aprendi


A maior dificuldade desta sprint não foi técnica, foi de decisão: várias escolhas de arquitetura (JPA ou JDBC puro, como o Java chamaria o Python, qual seria o escopo real do módulo de otimização) não tinham uma resposta "certa" óbvia, e exigiram pesar experiência da equipe contra o que seria tecnicamente mais robusto. Aprendi que vale mais a pena fechar essas decisões cedo e por escrito (documentadas com o motivo da escolha, não só a escolha em si) do que deixá-las implícitas — isso evitou retrabalho logo na Sprint 2, quando a implementação começou de verdade.
