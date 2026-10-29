# Matriz de rastreabilidade — Sprint 3 — Mente Fria

> **Atenção (28/10/2026, A39):** o escopo desta matriz (planejado na Sprint 3) é **executado na Sprint 4 oficial**, com entrega em **06/11/2026**. Datas e marcos em vigor: `docs/Sprint_4/sprint-4-roteiro.md`, §4. A coluna de situação continua sendo atualizada aqui.

**Revisão:** 28/10/2026, noite (A39–A42: datas passam para o roteiro da Sprint 4; US26 condicional). Antes: 28/10/2026 (A38: licença do `pip` autorizada no Render; B1 vira reserva). Antes: 27/10/2026, noite (v5.3: resposta do professor, B1, CT72, A26 separada do BD01, A35). Antes: 27/10 (v5.2: respostas da equipe; descarte por linha A12 e CT71; deploy definitivo no sábado, A21). Antes: 26/10, noite (v5.1: entram US14, US17, US18 e US20, decisão A2b). Versão da tarde em `historico/v5-26-10-antes-das-4-historias/`.

**Identificadores:**
- Oficiais do projeto: US (Backlog), UC (`Diagramas_UML.md`), CT (Plano de Testes), D, F e P (roteiro v4.6 e registro de 14/10).
- **A1–A38:** criados na auditoria (`sprint-3-decisoes-e-pendencias.md`); **A39 em diante:** `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`.
- **CT30–CT72:** **oficiais desde 27/10** (incorporados ao Plano de Testes, A37); a origem é `sprint-3-casos-de-teste-propostos.md`.

**Estado comprovado em 26/10:**
- **Feito** = existe na `main` e funciona (testado);
- **Pronto fora** = escrito e testado em `docs/Sprint_3/referencia/`, ainda não no repositório (falta copiar, entender, abrir o PR e fazer o aceite);
- **Parcial**; **Pendente**; **Não verificado**.

**Prioridade:** **P0** = bloqueia outras pessoas ou a entrega; **P1** = Must do núcleo; **P2** = Must da extensão ou qualidade obrigatória; **P3** = complementar.

**Testes de `referencia/` citados:** backend 95 (PostgreSQL 16 real e Python real), Python 47, banco 32 + 41, frontend 15 (funções de apoio) e retratos `IGUAIS`.

## 1. Histórias de usuário

| Req. | Origem | Estado comprovado (26/10) | Tarefa | Resp. | Onde está a orientação | Dependências | Critério / evidência | Prior. |
|---|---|---|---|---|---|---|---|---|
| **US1** / UC1 — conta do Administrador | Backlog, Sprint 1 | Frontend feito (API simulada); backend **pronto fora** | `POST /api/auth/registro` | Lucas | Lucas §3 | Fase 1; `UsuarioRepository` | CT01, CT01B, CT02, CT03 (`Fase2CrudTest`) | P1 |
| **US2** / UC2 — funcionários | Backlog, Sprint 1 | Idem | `POST`/`GET /api/usuarios` | Lucas | Lucas §3 | Idem | CT04, CT05, CT08 | P1 |
| **US3** / UC3 — login por perfil | Backlog, Sprint 1 | Idem; segurança **pronta fora** (18/18) | JWT; `401`/`403` em JSON; guarda por lista | Lucas, Leonardo | Lucas §2–§3; Leonardo §4 | — | CT06, CT07, CT46; aceite real | **P0** |
| **US4** / UC4 — ingredientes | Backlog, Sprint 1 | `GET` com 3 campos na `main`; completo **pronto fora** | `GET`, `POST`, `PUT`; unidades | Lucas, Diogo | Lucas §3; Diogo §7 | Fase 1 | CT09, CT10, CT11, CT18 | P1 |
| **US5** / UC5 — produtos | Backlog, Sprint 1 | **Pronto fora** (com `RegraAcai` e 13 casos) | `POST`/`GET /api/produtos` em transação | Lucas, Diogo | Lucas §3 | Fase 1 | CT12–CT15 | P1 |
| **US6** / UC6 — preço | Backlog, Sprint 1 | **Pronto fora** | `PUT /api/produtos/{id}/preco` | Lucas | Lucas §3 | Fase 1 | CT16, CT17, CT05 | P1 |
| **US7** / UC7 — validação | Backlog, Sprint 1 | Banco e frontend feitos; backend **pronto fora** | Advice (A7), validação que devolve todos os campos | Lucas, Diogo | Lucas §2; Diogo §9.2 | — | CT18, CT19, CT47, CT48 | **P0** |
| **US8** — repositório e README | Backlog, Sprint 1 | README existe; **CT20 nunca executado** | README com deploy e testes; CT20 | Kauê, Lucas, Leonardo | Kauê §9; Lucas §9 | Fases concluídas | CT20 | P2 |
| **US9** / UC9 — estoque | Backlog, Sprint 2 | Schema **pronto fora** (41/41 no Docker); backend **pronto fora** (S1–S5, concorrência); telas pendentes. Texto do Backlog desatualizado (F10) | DDL; movimentações; `GET /api/estoque` e `…/linhas`; descarte por linha (A12, opcional); Telas 10/16 | Diogo, Lucas, Leonardo | Diogo §3–§5; Lucas §4; Leonardo §6.1 | A10; Fase 2 | CT21, CT22, CT26, CT27, CT30–CT36; CT71 (A12) | P1 |
| **US10** / UC10 — produção | Backlog, Sprint 2 | Python v2 **pronto fora** (25/25); Java **pronto fora** (dublê e Python real) | Contrato v2; recomendação com saldo válido | Kauê, Lucas | Kauê §2–§8; Lucas §5 | Fase P; Fase 3 | CT23, CT24, CT29, CT41, CT42, CT44, CT45, CT50 | P1 |
| **US11** / UC11 — ver recomendação | Backlog, Sprint 2 | Pendente (tela) | Tela 12 sem termos técnicos | Leonardo | Leonardo §6.3 | US10 | CT43 | P1 |
| **US12** / UC12 — pedido | Backlog, Sprint 2 | Backend **pronto fora**; telas pendentes | `POST`/`GET /api/pedidos`; Telas 11/17 | Lucas, Diogo, Leonardo | Lucas §4; Diogo §6–§7; Leonardo §6.2 | Fase 2; A10 | CT25, CT28, CT33, CT37–CT40 | P1 |
| **US14** / UC14 — reposição (**Must**) | Backlog; redesenhada em 14/10 | **Pronto fora:** `reposicao.py` (22 testes + oráculo) e Java (8 testes + Python real); Tela 13 pendente | Fase Q (Python); `POST /api/reposicao/recomendacao`; Tela 13 | **Kauê** (Python e Java), Diogo (SQL 9), Leonardo (tela) | Kauê §11; Diogo §6–§7; Leonardo §6.5 | Fase P; Fase 3 (pedidos); A10 | CT51–CT57 | P1 |
| **US17** / UC17 — dashboard (**Must**) | Backlog; Tela 03 | **Pronto fora:** SQL 10–13 (Docker e seed) e Java (5 testes); Tela 03 pendente. F12 resolvida (A18); conteúdo do Termo mantido (A16) | `GET /api/dashboard`; Tela 03 | **Diogo** (repositório), Lucas (serviço), Leonardo (tela) | Diogo §6–§7; Lucas §6; Leonardo §6.4 | Fase 3; Fase 4A (último plano) | CT58–CT63 | P1 |
| **US18** / UC18 — marketing (**Must**) | Backlog; Apêndice B do v4.6 | **Pronto fora** (4 testes); Tela 14 pendente | `GET /api/marketing/sugestoes`; Tela 14 | Lucas, Leonardo | Lucas §6; Leonardo §6.6 | Fase 4A | CT64–CT66 | P1 |
| **US20** — deploy (**Must**) | Backlog; Termo (critério de pronto) | **Pronto fora:** `Dockerfile` e `render.yaml` (imagem testada com 512 MB e 0,1 CPU, fluxo completo sem falhas), *workflow* do Pages e `config.js` (15/15). Deploy real **pendente**: **definitivo na seg 02/11** (A41; antes: sáb 31, A36), sem ensaio separado | Render (backend + Postgres) e GitHub Pages | **Kauê** e Lucas (Render), Leonardo (Pages) | Kauê §12; Leonardo §9; Lucas §8 | Fases 1–3 na `main` (A41); A21; A38 (licença do `pip` autorizada: a otimização roda na URL; B1 só como reserva, A31) | CT67–CT70, CT72 (reserva B1); URL pública (R6) | **P0** (a entrega de 06/11 e a semana de bugs dependem) |
| **US13** (Could), **US15/US16** (Should), **US19**, **US26**, **US27** (Could) | Backlog / v4.6; adendo, itens 4 e 6 | **Banco pronto fora** para US13, US26 e US27 (tabelas e colunas); **US15/US16:** backend da cobertura parcial pronto fora (estoque válido, situação por linha, contagens do dashboard; adendo, item 4); **US26:** critérios 1 e 2 já funcionam na tela de ingredientes da `main` (com a API simulada; o `GET` real de unidades entra na Fase 2); faltam `POST`/`PATCH` e a Tela 19; US19: não iniciada | **US26 condicional** (*gate* na seg 02/11, A40); **US13, US19, US27 e US15/US16 completas: fora do semestre** (A40) | Diogo (US26, A43); Lucas revisa | Roteiro da Sprint 4, §2 e §5 | Fases 1 e 2 (US26) | US26 na `main` com testes até qua 04/11, 18h, ou registrada como fora | P3 |

## 2. Pendências herdadas e decisões técnicas

| Item | Origem | Estado comprovado | Tarefa | Resp. | Orientação | Dependências | Critério / evidência | Prior. |
|---|---|---|---|---|---|---|---|---|
| Hello World Java → Python | Sprint 1 | **Pronto fora** (eco real no perfil `dev`) | Copiar | Kauê, Lucas | Kauê §6, §8; Lucas §5.1 | Fase P | CT50 | P2 |
| Python em outra máquina | Sprint 2 | **Pendente** | Rodar os 47 testes numa 2ª máquina | Kauê | Kauê §7 | Fase P | `47 passed` em 2 máquinas | P1 |
| Contrato v2 (A10) | v4.6, Fase 0 | **Feito:** aprovado, aplicado e na `main` (PRs #34 e #35); a proposta ficou congelada | — | Kauê | Contrato oficial | — | Contrato oficial na `main` | Feito |
| A3 — Must fora da sprint | Auditoria | **Encerrada** (A2b); P3 respondida em 27/10 (`resposta-professor.md`) | Feito: professor avisado pessoalmente em 27/10 (WLS e B1); adendo aprovado e assinado em 27/10; falta repassar a resposta da Gurobi (A38) | Diogo, Lucas | Adendo | — | Adendo enviado | P1 |
| A4, A5, A7 — compilar, subir, 400 | Auditoria | **Corrigidos fora** | Copiar a Fase 1 | Lucas | Lucas §2 | — | 18/18 | **P0** |
| A8 — BOM no Python | Auditoria | **Corrigido fora** (produção e reposição) | Copiar | Kauê | Kauê §5 | — | Testes de BOM | P1 |
| A9 — data e hora | Auditoria | **Pronto fora** (validado no Java) | Copiar | Lucas, Diogo | Lucas §4 | Fase 3 | CT39 | P1 |
| A16, A17, A20 — conteúdo do dashboard e do marketing | Auditoria | **Aprovadas em 27/10**, implementadas (A20 com 18 ideias) | Reler as ideias com o cliente | Lucas (PO) | Decisões | — | Registro da decisão | P3 |
| A12 — botão "Descartar" por linha | v4.6 Fase 3; equipe em 27/10 | **Entrou**; backend pronto fora (CT71) | Copiar com a Fase 3; botão na Tela 10 | Lucas, Leonardo | Lucas §4; Leonardo §6.1 | Fase 3 | CT71 | P3 (primeiro corte, A28) |
| A28 — gatilhos de corte (a "Sprint 4 relâmpago" deu lugar à A40) | Equipe em 27/10; A42 (28/10) | Definido | Botão no M3 (dom 01/11, à noite); *gate* da US26 na **seg 02/11, de manhã** (A42, A43); revisão no M4 (ter 03/11) | Lucas (PO) | Roteiro da Sprint 4, §5 | Todas as fases | Decisão registrada | P2 |
| A21 — limites do Render free | Documentação do Render | Simulado localmente. **Definitivo na seg 02/11** (A41; vale até 02/12) | O Lucas cria a conta (sáb 31, A43); banco e serviço na seg 02/11; anotar a expiração no README | Kauê, Lucas | Kauê §12 | — | URL no README | **P0** |
| A22/A30/A31/A34/A38 — licença do Gurobi no servidor | WLS do Kauê (A34) + PyPI + professor (27/10) + suporte da Gurobi (28/10) | **Autorizada por escrito em 28/10** (A38): licença do `pip` no Render; WLS **não** | `render.yaml` com `OTIMIZADOR_HABILITADO=true`, sem `GRB_*`; Kauê repassa o e-mail ao professor; B1 como reserva | Kauê, Lucas (PO) | `sprint-3-licenca-gurobi.md` (§0) | US10, US11, US14, US18 no Render | Resposta da Gurobi recebida (28/10); professor avisado; CT68 com a otimização na URL | **P0** |
| A32 — backup e restauração | Professor (27/10) | **Validado localmente** | Backup no Render e restauração testada | Kauê | Kauê §12.6 | US20 | Evidência da restauração | P1 |
| A33 — adendo ao Termo | Professor (27/10) | Rascunho pronto | Revisar, decidir o item 4 e enviar | Lucas, Diogo | `sprint-3-adendo-termo-de-aceite.md` | Edição dos documentos oficiais | Adendo aprovado | P1 |
| A26 — incidente novo de Docker (26/10), **não é o BD01** | Auditoria | **Contorno verificado**; o BD01 (virtualização, Sprint 2) segue sem causa registrada | Registrar a A26 como incidente novo nas evidências da Sprint 3 | Diogo | Diogo §2 | — | Evidência registrada | P3 |
| P2 — versão do Gurobi | Registro de 14/10 | **Decidida em 27/10: 13.0.3** (as duas passam nos 47 testes) | Fixar 13.0.3 nos 3 arquivos | Kauê, Diogo | Kauê §3 | — | `requirements.txt` UTF-8 | P2 |
| A13 — Playwright instável | Auditoria | **Aberto** (original e dividido) | Diagnosticar e corrigir o teste | Leonardo | Leonardo §7.1 | — | 10 de 10 rodadas verdes | P2 |
| A15 — branch `sprint-3` desatualizado | Auditoria (corrigida em 27/10: o merge não desfaz o PR #24) | Risco baixo | Levar os comentários do `env.example` para o `.env.example`; branches novos a partir da `main`; apagar o `sprint-3` | Kauê | Kauê §9 | — | PR do `.env.example` | P3 |
| Documentos (receita → lucro, US9, US14, US17, US27, DER, mapa, Plano de Testes, Arquitetura, UML, Backlog "Sprint alvo") | v4.6 §10.1; auditoria | **Feito em 27/10** (A37), depois da aprovação do adendo; US27 registrada como história **nova** no Backlog | `.docx` conferidos no Word em 27/10 | Diogo (revisão) | Diogo §9 | A33 | Documentos na `main` | P2 |
| Plano de Testes (CT23, CT24, CT29; CT30–CT72) | v4.6 §4.4 | **Incorporado em 27/10** (A37) | Executar na semana de bugs, de 06/11 a 13/11 (A43) | Diogo, Kauê | Casos propostos | — | Plano atualizado | P2 |
| `[PREENCHER]` da Sprint 2, BE03, retrospectivas, perda do Git | Sprints 1 e 2 | Pendentes | Ver tutorial do Kauê | Kauê, Lucas | Kauê §9 | — | Atas e evidências | P3 |
| CI (GitHub Actions para os testes) | Plano de Testes §1 | Inexistente | **Opcional** (o *workflow* do Pages não roda testes) | — | — | — | — | — |
