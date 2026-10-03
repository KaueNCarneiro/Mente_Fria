# Relatório Individual de Contribuição — Sprint 1 — Lucas Gabriel Valadares Bassi (RA 2840482423008)

**Papel nesta sprint:** Product Owner / Responsável por Backend

## 1. O que fiz
| Item | PR/commit | Status |
|---|---|---|
| Estrutura do backend no repositório (Spring Boot + Maven, pasta `/backend`) | Sem registro: o repositório foi excluído por completo e recriado na Sprint 2 | Feito, depois refeito na Sprint 2 |
| Conexão do backend com o PostgreSQL | Sem registro: repositório excluído | Não concluído na Sprint 1; resolvido na Sprint 2 com a estrutura recriada |
| Decisão como PO da dúvida D-A (cadastro de Administrador aberto só até existir o primeiro) | Documentada no Contrato de Comunicação (item B5) e no Plano de Testes (CT01B) | Feito |
| Participação nas decisões de arquitetura de 25/09/2026 | Documentada em `Arquitetura.md`, seção 2.1 | Feito |

## 2. Rituais que participei
- [x] weeklies 1 de 1
- [ ] Sprint Review
- [ ] Retrospectiva

## 3. PRs de colegas que revisei
| PR | Autor | Comentário resumido |
|---|---|---|
| Sem registro: o repositório original foi excluído e o histórico de PRs foi perdido | — | — |

## 4. Dificuldades e o que aprendi
Na Sprint 1 criei a estrutura do backend, mas não consegui conectá-la ao banco PostgreSQL. Os conflitos no repositório ficaram grandes demais para resolver, e a equipe decidiu excluir o repositório inteiro e recomeçar. Por isso os commits e PRs da Sprint 1 não existem mais e não constam neste relatório. Na Sprint 2 recriei a estrutura do backend, já com a conexão ao banco funcionando.

Aprendi a testar cada camada isoladamente antes de ligar a próxima, a combinar com a equipe quem altera a estrutura base para evitar conflitos, e a manter credenciais fora do código. Se fosse refazer, validaria a conexão com o banco logo no início da sprint, pediria ajuda mais cedo e revisaria toda a estrutura para checar se não há repositórios duplicados ou até mesmo em falta.
