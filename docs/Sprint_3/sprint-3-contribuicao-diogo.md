# Relatório Individual de Contribuição — Sprint 3 — Diogo Pereira Miranda (RA 2840482423006)

**Papel nesta sprint:** Responsável por dados (Banco de Dados, qualidade/consistência da documentação)

> A Sprint 3 oficial foi de planejamento e preparação (roteiro, Contrato v2, código de referência testado, Adendo nº 1). O código dela entrou na `main` na Sprint 4 (A39). Este relatório é entregue junto com o da Sprint 4 (A43).

## 1. O que fiz

| Item | PR/commit | Status |
|---|---|---|
| Organização, no repositório recriado, dos documentos das Sprints 1 e 2 (contribuições e documentos de apoio) e do Termo de Aceite e do Documento de Visão em `docs/`, nas branches de cada sprint | PRs #8 a #21 (05/10 a 12/10) | Feito |
| Modelo das variáveis de ambiente do backend (`backend/.env.example`), sem nenhum valor real | commit `c00a556`, PR #22 (revisado e integrado pelo Lucas) | Feito |
| Publicação do pacote de planejamento da Sprint 3 em `docs/Sprint_3/`: roteiro, relatório de auditoria, proposta do Contrato v2, casos de teste propostos, matriz de rastreabilidade, tutoriais de cada integrante e o código de referência testado em `docs/Sprint_3/referencia/` (backend, banco, frontend, Python e deploy) | commit `50c0a31`, PR #26 | Feito (27/10) |
| Aplicação da v2 ao Contrato de Comunicação oficial, a pedido do Kauê (decisão A10) | commit `12ccfde`, PR #28; na `main` desde o PR #30 | Feito (27/10) |
| Registro das respostas da equipe de 27/10 e 28/10 e das decisões A1 a A38 em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`, com os documentos de apoio atualizados (Adendo nº 1, licença do Gurobi, mensagens ao professor) | commits `7237017`, `4775f86`, `d352b5d`, `b1dbf25` e `775bbe5`; PRs #29, #31, #32, #34 e #36 | Feito (27/10 e 28/10) |
| Atualização dos documentos depois da resposta da Gurobi (A38: licença restrita do `pip` no Render, WLS nunca numa URL pública), incluindo a Arquitetura, que cabia a mim (A11 e P2) | commit `73ee022`, PR #38 | Feito (28/10) |
| Consolidação da documentação com o Adendo aprovado (A37): Backlog e Visão (`.docx`), DER, UML, Plano de Testes (CT30–CT72), mapa de constraints, SQL dos repositórios e README. Conferi no Word a formatação dos `.docx`, que tinham sido editados sem o Word | commit `f12432b`, PR #40 | Feito (27/10 e 28/10) |
| Integração da `sprint-3` na `main` | PRs #23, #25, #27, #30, #33, #35, #37, #39 e #41 | Feito |
| Aprovação do Adendo nº 1 ao Termo de Aceite, como integrante da equipe (27/10) | `docs/Sprint_3/sprint-3-adendo-termo-de-aceite.md` | Feito |
| Registro do incidente A26: o Docker Desktop da minha máquina travava ao iniciar (arquivos de *socket* inacessíveis), com o contorno verificado e o aviso de nunca usar "Reset to factory defaults", que apagaria o banco local | commits `d604607` e `6d079f8`, PR #50; `docs/Sprint_3/evidencias/2026-10-26_diogo_A26_incidente-docker.md` | Feito (incidente em 26/10, registro em 04/11) |
| Código do banco da Sprint 3 (patch do DDL, testes T33 a T73, seed de demonstração e consultas) | — | Sem PR ou commit em `bd/` nesta sprint: foi planejado e testado em `docs/Sprint_3/referencia/bd/` e entrou no `bd/` na Sprint 4 (PRs #44 e #48) |

## 2. Rituais que participei

- [X] Dailies/weeklies
- [ ] Sprint Review (a Sprint 3 não teve Review própria: foi entregue junto com a Sprint 4, A43)
- [ ] Retrospectiva (feita junto com a das Sprints 2 e 4, no encerramento)

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
|---|---|---|
| — | — | Nenhum nesta sprint |

## 4. Dificuldades e o que aprendi

A Sprint 3 foi de planejamento, e a minha parte do banco só virou código na Sprint 4. A maior dificuldade foi manter dezenas de documentos coerentes entre si enquanto as decisões mudavam: a licença do Gurobi, o calendário e o escopo do Adendo nº 1 mudaram em poucos dias, e cada mudança precisava chegar ao roteiro, à Arquitetura, ao Plano de Testes e aos tutoriais. Outra dificuldade foi o Docker Desktop, que parou de iniciar na minha máquina em 26/10 (A26) e me impediu de testar o banco até o contorno ser encontrado.

Aprendi que um único arquivo de decisões, com data, motivo e o que cada decisão substitui, é o que mantém a documentação confiável quando o plano muda. Também aprendi a registrar um incidente com a causa, o contorno verificado e o que nunca fazer: o "Reset to factory defaults" teria resolvido o Docker, mas apagaria o banco local. Por fim, ficou claro que um `.docx` editado fora do Word precisa ser conferido no Word antes da entrega.
