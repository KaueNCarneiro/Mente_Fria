# Relatório Individual de Contribuição — Sprint 4 — Lucas Gabriel Valadares Bassi (RA 2840482423008)

**Papel nesta sprint:** Product Owner / Responsável por Backend

## 1. O que fiz
| Item | PR/commit | Status |
|---|---|---|
| Fase 1 do backend: Spring Security, token JWT, CORS e erros sempre em JSON (US1, base de todas as rotas); 18/18 testes | commit `41dbb7f`, PR #52; evidência `2026-10-29_lucas_fase1_mvnw-test.txt` | Feito |
| Fase 2: cadastro do Administrador, login, Funcionários, unidades, ingredientes, produtos e preço (US1–US7) | commit `e490355` (branch `sprint-4`) | Feito |
| Fase 3: estoque por linhas com validade (FEFO, nunca vende vencido, descarte) e pedido com baixa automática e atômica (US9, US12) | commit `298f1d2` | Feito |
| Fase 4A: ponte Java → Python e recomendação de produção por lucro (US10, US11) | commit `0ed86eb` | Feito |
| Fase 6: dashboard do mês e sugestões de campanha (US17, US18) | commit `7eaec94` | Feito |
| Integração na `sprint-4` da Fase P+Q (Python) e da Fase 5 (reposição) do Kauê | merges `e40f16d` e `7957b05` | Feito |
| Integração da `sprint-4` na `main` | Merge do PR #85 | Feito |
| Testes das fases (95 testes, 0 falhas, com os repositórios do Diogo; 101 com a US26) e conferência das 23 rotas com o Contrato | evidência `2026-11-15_lucas_fases-2-3-4a-6_testes-e-contrato.txt` | Feito |
| README atualizado com o sistema publicado, as variáveis e os testes (tutorial, Parte 10) | commit `092f39b` | Feito |
| Decisões de PO: US26 mantida em aberto com condições e prazo (A46, 04/11 e 10/11); deploy no Render passado ao Diogo (A47, 11/11) | `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md` | Feito |

## 2. Rituais que participei
- [X] Dailies/weeklies
- [ ] Sprint Review (06/11)
- [ ] Retrospectiva (das Sprints 2, 3 e 4, no encerramento)

## 3. PRs de colegas que revisei
| PR | Autor | Comentário resumido |
|---|---|---|
| #47 — `fix/cabecalho-constraints-teste` | sixtxxn | Revisor indicado no PR: correção do cabeçalho dos testes de constraints e da evidência do banco |
| #48 — `feature/seed-demo` | sixtxxn | Revisor indicado no PR; integrei o seed de demonstração para a Review (merge `8facce8`) |
| #49 — `docs/sql-repositorios-sprint3` | sixtxxn | Revisor indicado no PR; integrei o SQL dos repositórios da Sprint 3 (merge `4909d23`) |
| #88 — `feature/us26-backend` | sixtxxn | Revisor indicado no PR (A43: o PO revisa os PRs da US26): `POST` e `PATCH /api/unidades-medida` |
| #92 — decisão A47 | sixtxxn | Revisor indicado no PR: registro da passagem do deploy no Render para o Diogo |

## 4. Dificuldades e o que aprendi
A maior dificuldade da Sprint 4 foi o tempo. A Fase 1 (segurança) ficou pronta em 29/10, mas só entrou na `main` em 05/11, e as Fases 2, 3, 4A e 6 só entraram em 11/11, depois da entrega de 06/11. Como as fases dependiam umas das outras e do trabalho dos colegas (os repositórios do Diogo e a Fase 5 do Kauê), o atraso no meu caminho crítico segurou também as telas e o deploy. Sem tempo naquela semana, pedi que o deploy no Render passasse para o Diogo (A47), e o sistema foi publicado em 11/11.

Na parte técnica, aprendi como o Spring Security protege as rotas com um token JWT, como o CORS libera só o frontend, e como um tratamento global de erros faz toda resposta de erro sair em JSON, com os campos que o frontend precisa. Também aprendi a rodar testes de integração com um PostgreSQL temporário (Testcontainers) e a chamar o módulo Python como subprocesso a partir do Java.

Como PO, aprendi que uma exceção de escopo (manter a US26 em aberto, A46) precisa de condições e prazo escritos, para não tirar tempo do obrigatório. A lição para os próximos projetos é publicar a branch cedo, para os colegas poderem entrar nela, e não deixar o caminho crítico para o fim da sprint.
