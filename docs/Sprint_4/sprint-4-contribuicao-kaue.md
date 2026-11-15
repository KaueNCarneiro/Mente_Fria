# Relatório Individual de Contribuição — Sprint 4 — Kauê Nogueira Carneiro (RA 2840482423039)

**Papel nesta sprint:** Facilitador / Scrum Master e responsável pelo módulo Python, pela reposição (US14) e pela publicação no Render (US20); dono do repositório no GitHub

## 1. O que fiz

| Item | PR/commit | Status |
| --- | --- | --- |
| Fase P + Q: otimização por lucro em dois estágios e reposição com orçamento em Python (US10, US14), com 47 testes | commit `27a962a`, merge `e40f16d`; evidência `2026-11-10_kaue_fasePQ_pytest.txt` | Feito |
| Fase 5: recomendação de reposição no backend (serviço, rota e testes; US14) | commit `e02de11`, merge `7957b05`; evidências `2026-11-11_kaue_fase5_mvnw-test.txt` e `_fase5_python-real.txt` | Feito |
| Arquivos do deploy (`Dockerfile`, `.dockerignore` e `render.yaml`) e `.env.example` único do backend (US20) | PRs #83 e #82 | Feito |
| Teste da imagem do deploy na máquina, com os limites do Render (CT67) | evidência `2026-11-11_kaue_ct67.txt` | Feito |
| Testes no ambiente publicado: CT68 (fluxo completo, nos dois perfis), CT69 (CORS), CT70 (serviço "dormindo") e CT72 (chave de reserva) | PR #102; evidências `2026-11-14_kaue_ct68.txt`, `_ct69.txt`, `_ct70.txt` e `_ct72.txt` | Feito (14/11) |
| Documentos do modelo matemático da produção e da reposição e registro da perda do histórico do Git | PRs #84 e #90 | Feito |
| Encaminhamento ao professor do e-mail de autorização da Gurobi (decisão A38); o professor aprovou | — (sem commit; registrado em `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`) | Feito |
| Aprovação da seção 2.11 do Contrato de Comunicação (US26, unidades de medida e porção padrão), que cabia a mim pela decisão A43 | — (sem commit; registrada na decisão A46, no mesmo arquivo) | Feito (10/11) |
| Instalação do app do Render no repositório, que só o dono pode fazer. Com isso, o deploy automático foi ligado, e cada merge na `main` passou a publicar o backend sozinho | — (ação administrativa no GitHub, sem commit); evidência `2026-11-15_diogo_deploy-automatico-render.txt` | Feito (15/11) |
| Deploy no Render (conta, serviço, schema, seed e backups) | — | Não feito por mim: passou para o Diogo pela decisão A47 (11/11) |
| Facilitação do encerramento: relatório da Sprint 4, revisão do relatório da Sprint 3, retrospectiva das Sprints 2, 3 e 4 e CT20 | — | Não feito: adiado em 11/11, sem data nova |

Os commits, os PRs e as evidências da parte técnica foram publicados no GitHub e conferidos pelo Diogo, na máquina dele (o cabeçalho das evidências registra isso).

## 2. Rituais que participei

- [X] Dailies/weeklies
- [ ] Sprint Review (06/11)
- [ ] Retrospectiva (das Sprints 2, 3 e 4, no encerramento)

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
| --- | --- | --- |
| #100 — `docs/backup-14-11` | sixtxxn | Revisor pedido no PR (backup do fim da semana de bugs e a data em que o banco vence nos tutoriais). Dei o meu acordo por mensagem ao Diogo; não há revisão minha registrada no GitHub |

## 4. Dificuldades e o que aprendi

A maior dificuldade da Sprint 4 foi o tempo. Eu tinha no meu tutorial a otimização em Python, a reposição no backend, os arquivos do deploy, os testes no ambiente publicado e a facilitação, e não consegui fechar tudo sozinho no prazo. Contei com o Diogo para levar a parte técnica ao GitHub e conferir se tudo estava correto, na máquina dele, com as evidências registradas no meu nome. O deploy passou para ele (A47), e o encerramento, que depende de mim como facilitador, ficou adiado.

Aprendi que pedir ajuda cedo evita que uma parte do caminho crítico pare: a Fase 5 e a Fase P + Q precisavam estar prontas antes da Fase 6 e das telas. Também aprendi que algumas tarefas não podem ser delegadas, porque dependem de quem é o dono: só eu podia autorizar o Render no repositório, e enquanto isso não foi feito o deploy precisou ser publicado à mão a cada merge. A lição para os próximos projetos é dividir a carga quando ela é planejada, e não quando o prazo já chegou.
