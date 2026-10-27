# Matriz de rastreabilidade — Sprint 3 — Mente Fria

**Revisão:** 26/10/2026 (auditoria).

**Identificadores:**
- Oficiais do projeto: US (Backlog), UC (`Diagramas_UML.md`), CT (Plano de Testes), D, F e P (roteiro v4.6 e registro de 14/10).
- **A1–A15:** criados na auditoria (`sprint-3-decisoes-e-pendencias.md`).
- **CT30–CT50:** **propostos** (`sprint-3-casos-de-teste-propostos.md`); viram oficiais quando entrarem no Plano de Testes.

**Estado comprovado em 26/10:**
- **Feito** = existe na `main` e funciona (testado);
- **Pronto fora** = feito e testado em `docs/Sprint_3/referencia/`, ainda não no repositório;
- **Parcial**;
- **Pendente**;
- **Não verificado**.

**Prioridade:**
- **P0** = bloqueia outras pessoas ou a entrega (fazer primeiro);
- **P1** = Must do núcleo;
- **P2** = Must da extensão ou qualidade obrigatória;
- **P3** = complementar.

## 1. Histórias de usuário

| Req. | Origem | Estado comprovado (26/10) | Tarefa | Resp. | Onde está a orientação | Dependências | Critério / evidência | Prior. |
|---|---|---|---|---|---|---|---|---|
| **US1** / UC1 — conta do Administrador | Backlog, Sprint 1 | Frontend pronto (API simulada); backend **pendente** | `POST /api/auth/registro` (409 se já existe; e-mail normalizado; senha ≥ 8) | Lucas | Lucas §3.1–§3.2 | Fase 1; `UsuarioRepository` (Diogo §7) | CT01, CT01B, CT02, CT03; validação da Fase 2, itens 1–2 | P1 |
| **US2** / UC2 — funcionários | Backlog, Sprint 1 | Idem | `POST`/`GET /api/usuarios` (Admin) | Lucas | Lucas §3.1 | Idem | CT04, CT05; itens 6 e 10 | P1 |
| **US3** / UC3 — login por perfil | Backlog, Sprint 1 | Idem; segurança **pronta fora** (18/18) | Login JWT; `401`/`403` em JSON; guarda por lista no frontend | Lucas, Leonardo | Lucas §2–§3; Leonardo §4 | — | CT06, CT07, CT08, CT46; aceite real, itens 2, 4, 14, 15 | **P0** (Fase 1 libera todos) |
| **US4** / UC4 — ingredientes | Backlog, Sprint 1 | `GET` com só 3 campos; escrita **pendente** | `GET` completo, `POST`, `PUT`; `GET /api/unidades-medida` | Lucas, Diogo | Lucas §3.3; Diogo §7 | Fase 1 | CT09, CT10, CT11; itens 5, 7; aceite, itens 7–9 | P1 |
| **US5** / UC5 — produtos e composição | Backlog, Sprint 1 | Backend **pendente**; `RegraAcai` **não existe pronta** | `POST`/`GET /api/produtos` em transação; `RegraAcai` com teste | Lucas, Diogo | Lucas §3.4 | Fase 1; `ProdutoRepository` | CT12, CT13, CT14, CT15; item 8–9 | P1 |
| **US6** / UC6 — preço | Backlog, Sprint 1 | Backend **pendente** | `PUT /api/produtos/{id}/preco` (Admin) | Lucas | Lucas §3.1 | Fase 1 | CT16, CT17, CT05; item 10 | P1 |
| **US7** / UC7 — validação transversal | Backlog, Sprint 1 | Banco e frontend **feitos**; erros do backend **prontos fora** | `@RestControllerAdvice` (A7), validação manual, `mapa_constraints` ampliado | Lucas, Diogo | Lucas §2.4; Diogo §9.2, item 5 | — | CT18, CT19, CT47, CT48 | **P0** |
| **US8** — repositório e README | Backlog, Sprint 1 | README existe; **CT20 nunca executado** | Atualizar o README; executar o CT20 | Kauê (CT20), Lucas (README do backend), Leonardo (README do frontend) | Kauê §9, item 4; Lucas §7 | Fases concluídas | CT20 | P2 |
| **US9** / UC9 — movimentação de estoque | Backlog, Sprint 2 | Schema e regras **prontos fora** (41/41, S1–S5, concorrência 30/30); backend e telas **pendentes**. **Texto do Backlog desatualizado** (F10) | Patch do DDL; `POST /api/estoque/movimentacoes`; `GET /api/estoque`; `GET .../linhas` (A12); Telas 10/16 | Diogo, Lucas, Leonardo | Diogo §3–§5; Lucas §4.1–§4.4; Leonardo §6.1 | Contrato v2 (A10); Fase 2 | CT21, CT22, CT26, CT27, CT30–CT36; Fase 3, itens 1–4 | P2 |
| **US10** / UC10 — produção ótima | Backlog, Sprint 2 | Python v1 **feito** (8/8); v2 **pronto fora** (25/25; oráculo); Java **pendente**. Termo, Visão e Backlog dizem "receita" (F9) | Python v2; Contrato v2; `POST /api/producao/recomendacao`; saldo válido | Kauê, Lucas | Kauê §2–§8; Lucas §5 | Fase P; Fase 3 (consulta 3); A10 | CT23, CT24, CT29, CT41, CT42, CT44, CT45, CT50; 151/1/0 | P2 |
| **US11** / UC11 — visualizar recomendação | Backlog, Sprint 2 | **Pendente** | Tela 12 sem termos técnicos; motivo do zero | Leonardo | Leonardo §6.3 | US10 | CT43 | P2 |
| **US12** / UC12 — pedido | Backlog, Sprint 2 | Regras SQL **prontas fora**; backend e telas **pendentes** | `POST`/`GET /api/pedidos` atômico, FEFO só em linhas válidas, preço do banco, `:agora` | Lucas, Diogo, Leonardo | Lucas §4.5; Diogo §6–§7; Leonardo §6.2 | Fase 2; Contrato v2 | CT25, CT28, CT33, CT37, CT38, CT39, CT40 | P2 |
| **US13** / UC13 — clientes (Could) | Backlog | Constraints **prontas fora** (T33–T36) | Fora da Sprint 3 | — | — | **A3** | — | — |
| **US14** / UC14 — reposição (**Must**) | Backlog | Modelo desenhado (v4.6, Fase Q); nada implementado | **Fora da Sprint 3: bloqueio A3** | PO + professor | Decisões A3 | A3 | — | **P0 (decisão)** |
| **US15/US16** — alertas (Should) | Backlog | Consultas de base prontas fora (consultas 1 e 2) | Fora da Sprint 3 | — | — | A3 | — | — |
| **US17** / UC17 — dashboard (**Must**) | Backlog | Dados de demonstração prontos fora (totais conferidos) | **Fora: A3**; depende da F12 | PO | — | A3, F12 | — | **P0 (decisão)** |
| **US18** / UC18 — marketing (**Must**) | Backlog | Desenho no v4.6 (Apêndice B) | **Fora: A3** | PO | — | A3 | — | **P0 (decisão)** |
| **US19** (Could), **US26** (Could), **US27** (Could) | Backlog / roteiro v4.6 | US27: schema **pronto fora** (status; **sem** devolver estoque, A6) | Fora | — | — | A3 | — | — |
| **US20** — deploy (**Must**) | Backlog, Sprint 4 | **Pendente**; a janela de testes pressupõe o sistema publicado | **Fora, salvo decisão A3** (recomendação: incluir em 31/10–01/11) | Lucas, Kauê | Decisões A3; roteiro v5 §4 | A3; A14 | URL pública (R6) | **P0 (decisão)** |

## 2. Pendências herdadas e decisões técnicas

| Item | Origem | Estado comprovado | Tarefa | Resp. | Orientação | Dependências | Critério / evidência | Prior. |
|---|---|---|---|---|---|---|---|---|
| Hello World Java → Python | Sprint 1 (roteiro, §4) | **Pendente**; o Python da `main` não tem eco | Eco no v2 (pronto fora) + `/api/teste/python` com `@Profile("dev")` | Kauê, Lucas | Kauê §6, §8; Lucas §5.1 | Fase P | CT50 | P2 |
| WLS só no PC do Kauê | Sprint 2 (relatório, §6) | **Pendente** | Rodar numa 2ª máquina | Kauê | Kauê §7, item 5 | Fase P | `25 passed` em 2 máquinas | P1 |
| Contrato v2 (A10) | Roteiro v4.6, Fase 0 | **Proposta pronta**; oficial não atualizado | Aprovar e aplicar | Kauê | Kauê §2 | — | Commit do contrato oficial | **P0** |
| A3 — Must fora da sprint | Auditoria | **Aberto** | Decisão com o professor | Lucas (PO), Diogo | Diogo §9.1; decisões | — | Decisão registrada | **P0** |
| A4 — o projeto não compilava | Auditoria (tutorial de 14/10) | **Corrigido fora** (verificado) | Tirar o `scope runtime` do driver | Lucas | Lucas §2.2 | — | `mvnw test` compila | **P0** |
| A5 — a aplicação não subia (CORS) | Auditoria | **Corrigido fora** (verificado) | `UrlBasedCorsConfigurationSource` | Lucas | Lucas §2.4 | — | Contexto sobe; 18/18 | **P0** |
| A7 — 400 virava 500 | Auditoria | **Corrigido fora** (verificado) | Handlers no advice | Lucas | Lucas §2.4 | — | CT47 | P1 |
| A8 — entrada com BOM recusada | Auditoria | **Corrigido fora** (verificado) | `utf-8-sig` | Kauê | Kauê §5–§6 | — | `test_entrada_com_bom_e_aceita` | P1 |
| A9 — data e hora em UTC | Auditoria (revisão de SQL) | **Pendente** | `:agora` vindo do `Clock` | Lucas, Diogo | Lucas §4; Diogo §6 | Fase 3 | CT39 | P2 |
| A11 — Compute Server no desenvolvimento | Arquitetura × código | **Aberto** | Decidir; registrar na Arquitetura | Kauê, Lucas | Kauê §2, item 4 | — | Arquitetura atualizada | P2 |
| P2 — versão do Gurobi | Registro de 14/10 | **Aberto**; as duas versões passam | Escolher e alinhar 3 arquivos | Kauê, Diogo | Kauê §2–§3 | — | `requirements.txt` UTF-8; Arquitetura e README iguais | P2 |
| A13 — Playwright instável | Auditoria | **Aberto** (original e dividido) | Diagnosticar e corrigir o teste | Leonardo | Leonardo §7.1 | — | 10 de 10 rodadas verdes | P2 |
| A15 — branch `sprint-3` divergente | Auditoria | Risco | Fechar ou atualizar | Kauê | Kauê §9, item 1 | — | Branch tratado | P1 |
| Documentos: receita → lucro, US9, US14, US27, DER, mapa, Plano de Testes, Arquitetura, UML, referências antigas | Roteiro v4.6, §10.1; análise de 14/10, §4 | **Pendente** (os `.docx` são idênticos aos da `main`) | Editar, depois de avisar o professor | Diogo | Diogo §9 | A3, P3 | PRs de documentação | P2 |
| Plano de Testes (CT23, CT24, CT29; CT30–CT50) | Roteiro v4.6, §4.4 | **Proposta pronta** | Incorporar **até 31/10** | Diogo, Kauê | Casos propostos; Diogo §9.2, item 6 | — | Plano atualizado | P2 |
| `[PREENCHER]` / `[CONFIRMAR]` da Sprint 2 | Evidências da Sprint 2 | **Pendente** (13 linhas) | Completar com fatos ou marcar "sem evidência" | Kauê (facilitação) | Kauê §9 | — | Arquivo sem marcadores abertos | P3 |
| BE03 (500 com *Whitelabel*) | Evidências da Sprint 2 | Causa não registrada | Registrar a hipótese (o banco ainda subia); a Fase 1 elimina o HTML de erro | Lucas | Lucas §2.4 | Fase 1 | CT48 | P3 |
| BD01 (virtualização do Docker) | Evidências da Sprint 2 | **Reproduzido em 26/10** nesta máquina (o motor não sobe) | Registrar a solução quando resolvida | Diogo | Diogo §2 | — | Docker funcionando | P1 (o Testcontainers depende dele) |
| Retrospectiva da Sprint 2, Reviews, perda do Git | Relatórios das Sprints 1 e 2 | **Pendente** | Ver o tutorial do Kauê | Kauê | Kauê §9, itens 5–7 | — | Atas | P3 |
| CI (GitHub Actions) | Plano de Testes, §1 | Inexistente | **Opcional** (não entra no prazo) | — | — | — | — | — |
