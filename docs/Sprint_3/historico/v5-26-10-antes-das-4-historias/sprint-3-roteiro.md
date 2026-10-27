# Roteiro — Sprint 3 — Mente Fria (v5)

**Revisão:** 26/10/2026, a partir da auditoria dos materiais de 14/10. **Base anterior:** v4.6, de Kauê e Diogo (`historico/sprint-3-roteiro-v4.6.md`).
**Período:** **26/10 (seg) a 01/11/2026 (dom)**. Janela de testes e correções: **01/11 a 06/11/2026** (decisão A1).
**Documentos irmãos (nesta pasta):**
- `sprint-3-decisoes-e-pendencias.md`: decisões e bloqueios, com IDs F, D, P e A.
- `sprint-3-contrato-v2-proposta.md`: contrato para aprovar até 27/10.
- `sprint-3-matriz-rastreabilidade.md`
- `sprint-3-casos-de-teste-propostos.md`
- `sprint-3-relatorio-auditoria.md`
- Os 4 tutoriais: `sprint-3-tutorial-*.md`.
- `referencia/`: código e SQL **já executados** na auditoria, prontos para copiar.

---

## 0. O que mudou da v4.6 para a v5

1. **Calendário refeito.** As datas da v4.6 passaram. Nada foi entregue no GitHub depois de 14/10 (último commit: `ac028cd`, PR #24). A Sprint 3 agora tem **7 dias corridos**.
2. **Escopo:** núcleo + extensão (US1–US7, US9, US10, US11, US12, mudanças do banco). A "Sprint 4" da v4.6 **não existe mais**.
3. **Bloqueio novo e crítico (A3).** US14, US17, US18 e US20 (todas Must no Termo) ficaram sem sprint, e a janela de testes pressupõe o sistema publicado. **Decisão do PO com o professor até 27/10.**
4. **Contrato v2 vira pré-requisito explícito (A10).** Backend e frontend não implementam estoque, pedido nem recomendação sem ele.
5. **Correções técnicas validadas em execução:**
   - A4: o projeto não compilava com o `GlobalExceptionHandler`.
   - A5: a aplicação não subia com o CORS do tutorial.
   - A7: erros 400 viravam 500.
   - A8: o módulo Python recusava a entrada vinda do PowerShell.
   - A9: datas e horas no fuso de São Paulo.
6. **Os caminhos dos tutoriais foram corrigidos.** As pastas `referencia_bd/`, `referencia_python/` e `referencia_frontend/` nunca existiram; o material está agora em `docs/Sprint_3/referencia/`.
7. **Contradição removida:** cancelar pedido **não** devolve estoque (A6).

---

## 1. Objetivo e entregas da Sprint 3

**Objetivo:** ao final de 01/11, o sistema roda localmente de ponta a ponta com banco real, backend real e frontend real, nos dois perfis:

> cadastro e login → cadastros (ingrediente, produto, preço) → entrada de estoque com validade → pedido com baixa automática por linhas (FEFO, sem vencido) → recomendação de produção por lucro (Python real) na Tela 12.

| Entrega | Histórias | Responsável principal |
|---|---|---|
| Segurança (JWT), CORS e erros padronizados | Base de US1–US7 | Lucas |
| Autenticação, usuários, unidades, ingredientes, produtos e preço reais | US1–US7 | Lucas (+ Diogo nos repositórios) |
| Schema novo (linhas, motivo, status), 41 testes, seed de demonstração, SQL da Sprint 3 | Base de US9, US12, US10 | Diogo |
| Módulo Python v2 (lucro, 2 estágios, eco) e Contrato v2 | US10 | Kauê |
| Estoque por linhas e pedido com baixa | US9, US12 | Lucas + Diogo |
| Ponte Java → Python e recomendação | US10, US11 | Lucas + Kauê |
| Frontend em módulos, guarda de rotas, aceite real, telas de Estoque, Pedido e Produção | US1–US7, US9, US11, US12 | Leonardo |
| Plano de Testes atualizado (CT23, CT24, CT29 e CT30 em diante), relatório e evidências da Sprint 3, retrospectiva | Processo | Kauê (facilitação) + todos |

---

## 2. Estado real em 26/10/2026

Verificado no repositório e por execução. Detalhes no relatório de auditoria, §B.

| Camada | No repositório (`main`, `ac028cd`) | Pronto **fora** do repositório (em `referencia/`, já executado) |
|---|---|---|
| Banco | DDL da Sprint 1 e 32 testes (**32/32** reexecutados) | Patch do DDL (`git apply --check` ok), **41/41** testes novos, `seed_demo.sql`, `consultas_sprint3.sql`, FEFO e concorrência (30/30) |
| Backend | Só `GET /api/saude` e `GET /api/ingredientes` (3 campos); sem Security, JWT ou Testcontainers | Fase 1 com correções A4, A5 e A7: **18/18 testes MockMvc** (401/403 em JSON, CORS com `PATCH`, 400/404/500) |
| Python | `otimizador.py` v1 (receita, `float`): **8/8** testes | v2 (lucro, 2 estágios, eco, BOM): **25/25** com `gurobipy` 13.0.1 e 13.0.3, conferido pelo oráculo de força bruta |
| Frontend | `app.js` com 235 linhas; 22 verificações do Playwright com API simulada | Divisão em módulos: **25/25 telas idênticas**; guarda por lista validado |
| Documentos | Visão, Termo e Backlog ainda dizem "receita"; DER sem linhas; Contrato sem v2 | Contrato v2 (proposta), decisões, casos de teste propostos |

---

## 3. Pendências herdadas das Sprints 1 e 2

Classificação pela evidência, não pelo que os relatórios afirmam.

| Pendência | Estado comprovado | Onde é resolvida |
|---|---|---|
| Backend de US1–US7 (auth, usuários, CRUD, preço, validação) | **Pendente** (só existe o frontend com API simulada) | Fases 1 e 2 (Lucas) |
| Hello World Java → Python | **Pendente.** O desenho da Sprint 1 ficou obsoleto: o Python da `main` não tem modo eco | Fase P (eco) + Fase 4A |
| CORS | **Pendente** | Fase 1 |
| Reprodução da licença WLS fora do PC do Kauê | **Pendente** | Fase P (2ª máquina) |
| Testes do backend, Testcontainers e CI | **Pendentes** (só existe o `contextLoads`) | Fases 1–3 (CI é opcional) |
| `.env` | **Corrigido em 14/10** (PR #24); o relatório da Sprint 2 dizia "feito" antes disso | Concluído; README a alinhar (Fase 8) |
| CT20 (subir o projeto do zero só com o README) | **Nunca executado** | Fase 8 (Kauê) |
| Evidências com `[PREENCHER]` e `[CONFIRMAR]` da Sprint 2 (13 linhas) | **Pendente** | Fase 8. Só completar com fatos; **não** reescrever o histórico |
| BE03 (erro 500 com *Whitelabel*) | **Causa não registrada.** Hipótese pelos horários: o banco ainda estava subindo | Fase 1 elimina o HTML de erro; registrar a hipótese |
| Retrospectiva da Sprint 2 e Review das Sprints 1 e 2 | **Não realizadas.** As contribuições individuais marcam "Retrospectiva" como feita: há contradição | Fase 8 (Kauê) |
| Perda do histórico do Git em 27/09 | Fato comprovado (primeiro commit `9e02498`, de 27/09); documentação **pendente** | Fase 8 (Kauê) |
| `requirements.txt` em UTF-16, `gurobipy` 13.0.3 × 13.0.1 | **Pendente** (P2) | Fase P |
| Referências antigas em documentos vivos (`docs/Script_DDL.sql`, `sql_repositorios_sprint1.md`, `ARQUITETURA.md`…) | **Pendente** | Diogo (documentos) |
| Branch `origin/sprint-3` divergente | Risco (A15) | Kauê |

---

## 4. Escopo

**Dentro:** US1–US7 (Must), US9 (Must), US10 (Must), US11 (Must), US12 (Must), as mudanças de banco que viabilizam US9/US12/US27 (status do pedido já no DDL) e a Fase 8 (qualidade e encerramento).

**Fora da Sprint 3 (sinalizadas):** US13 (Could), US14 (Must), US15 e US16 (Should), US17 e US18 (Must), US19 (Could), US20 (Must), US26 (Could), US27 (Could) e o atalho de descarte por linha (A12).

> ⚠️ **A3 — decidir até 27/10.** O Termo de Aceite aceita Must + Should. Fora da Sprint 3 ficam **quatro Must** (US14, US17, US18, US20) e **dois Should** (US15, US16). O que levar ao professor está em `sprint-3-decisoes-e-pendencias.md`, item A3. **Recomendação técnica:** incluir **pelo menos a US20 (deploy)** em 31/10–01/11, porque a janela de testes de 01–06/11 testa o sistema publicado (v4.6, §4.4). Se a decisão incluir mais histórias, este roteiro precisa ser revisto no mesmo dia.

---

## 5. Fases, responsáveis e critérios de conclusão

As regras de negócio detalhadas da v4.6 **continuam valendo** e não foram repetidas aqui:
- estoque por linhas, FEFO, descarte, `quantidade_restante` (v4.6, §5 Fase 3 e §10.1);
- modelo de produção por lucro (v4.6, §3 e §5 Fase P);
- detalhes da Fase 4A (v4.6, §5).

Cada fase tem um tutorial com os passos, as validações e o diagnóstico.

| Fase | O que entrega | Quem | Depende de | Pronto quando… (verificável) | Tutorial |
|---|---|---|---|---|---|
| **0 — Contrato v2 e decisões** | Contrato v2 aprovado e aplicado ao oficial; decisões A3, A11 e P2 tomadas; professor avisado (F9/P3) | Kauê (contrato), Lucas (PO), Diogo (aviso) | — | Commit do contrato oficial com a entrada no histórico de revisões; mensagem ao professor registrada | Kauê §2; Diogo §9 |
| **1 — Fundação do backend** | Security + JWT, CORS com `PATCH`, `@RestControllerAdvice`, `Clock` de São Paulo | Lucas | — | `mvnw test` com os **18 testes de segurança e erros** verdes; *preflight* com `200` | Lucas §2 |
| **Banco** | Patch do DDL, 41 testes, `seed_demo.sql`, `sql_repositorios_sprint3.md`, assinaturas dos repositórios | Diogo | — | `32 \| 0` e `41 \| 0`; experimentos A (`37 \| 4`) e B (`38 \| 3`); valores do seed conferidos; assinaturas registradas | Diogo §2–§8 |
| **P — Python v2** | `otimizador.py` v2, 25 testes, exemplos, `requirements.txt` em UTF-8, 2ª máquina | Kauê | Contrato v2 (§1) | `25 passed` em **duas** máquinas; `stdout` só com JSON | Kauê §3–§7 |
| **2 — CRUD real** | US1–US7 com banco real; repositórios do Diogo | Lucas + Diogo | Fase 1, Banco | Os 10 itens da validação da Fase 2 (tutorial do Lucas) | Lucas §3 |
| **Front 1** | `app.js` dividido; guarda por lista | Leonardo | — | `IGUAIS` (25 telas); guarda conforme a tabela; Playwright estável (A13) | Leonardo §2–§4 |
| **Gate da Fase 2** | Aceite real com o backend real | Leonardo | Fase 2 | 16 evidências em `docs/Sprint_3/evidencias/` | Leonardo §5 |
| **3 — Estoque e pedido** | `POST /api/estoque/movimentacoes`, `GET /api/estoque`, `GET /api/estoque/{id}/linhas`, `POST` e `GET /api/pedidos` | Lucas + Diogo | Fase 2, Banco, Contrato v2 | Cenários S1–S5, concorrência e CT25 como testes de integração; validação manual da Fase 3 | Lucas §4; Diogo §5–§7 |
| **4A — Python no Java** | Eco com `@Profile("dev")`; `POST /api/producao/recomendacao` | Lucas + Kauê | Fase P, Fase 2, Fase 3 (saldo válido) | Demonstração = **151/1/0** (lucro 1.011,71); Funcionário recebe `403`; `PYTHON_CMD` errado dá `500` sem detalhe; tempo medido | Lucas §5; Kauê §8 |
| **Front 2** | Telas de Estoque (10/16), Pedido (11/17) e Produção (12) | Leonardo | Fases 3 e 4A, Contrato v2 | Testes de navegador das telas; **nenhum termo técnico** na Tela 12 | Leonardo §6–§7 |
| **8 — Qualidade e encerramento** | Plano de Testes (CT23, CT24, CT29 e CT30 em diante), README, evidências, relatório, retrospectiva, CT20 | Todos (Kauê facilita) | — | Plano completo **até 31/10** (a janela de testes o executa) | Kauê §9; Diogo §9 |

---

## 6. Dependências e caminho crítico

```
Contrato v2 (Kauê, 27/10) ─┬─> Fase 3 (Lucas+Diogo) ──> Fase 4A (Lucas+Kauê) ──> Tela 12 (Leo)
                           └─> Telas 10/11 (Leo)
Fase 1 (Lucas) ──> Fase 2 (Lucas, usa repositórios do Diogo) ──> Gate de aceite (Leo) ──> Fase 3
Banco (Diogo: DDL + testes + assinaturas, 26–27/10) ──> Fase 2 e Fase 3
Fase P (Kauê) ──> Fase 4A
Decisão A3 (PO + professor, 27/10) ──> se incluir US20: deploy em 31/10–01/11 (Lucas + Kauê)
```

**Caminho crítico:** Fase 1 → Fase 2 → Fase 3 → Fase 4A → Tela 12. **Todo o caminho passa pelo Lucas.** Para aliviar:
- o Diogo escreve os repositórios Java (F7);
- o Kauê faz a Fase 4A em dupla;
- o código validado da Fase 1 está pronto em `referencia/backend/`.

---

## 7. Calendário (26/10 a 01/11 e janela de testes)

| Dia | Kauê | Diogo | Lucas | Leonardo |
|---|---|---|---|---|
| **Seg 26** | Contrato v2: revisar a proposta; decisões A11 e P2 | Patch do DDL e 41 testes; experimentos A e B | Fase 1 (copiar de `referencia/backend/`) e os 18 testes | Baseline (22 OK) e retratos; iniciar a divisão do `app.js` |
| **Ter 27** | Contrato aprovado e aplicado; Fase P (copiar, testar, 2ª máquina) | Assinaturas com o Lucas; mensagem ao professor (A3, F9, P3) | **PO: decisão A3**; PR da Fase 1; começar a Fase 2 | Terminar a divisão; guarda; PR |
| **Qua 28** | PR da Fase P; `exemplos/` ao Lucas | Repositórios Java (usuário, ingrediente, produto); `seed_demo.sql`; `sql_repositorios_sprint3.md` | Fase 2 (auth, CRUD) | Preparar telas de Estoque e Pedido contra o Contrato v2 (API simulada) |
| **Qui 29** | Plano de Testes: CT23/CT24/CT29 e CTs do Python | Repositórios de movimentação e pedido; documentos (DER, mapa de constraints) | Validação da Fase 2 → avisar o Leo; começar a Fase 3 | **Gate da Fase 2** (16 evidências) |
| **Sex 30** | **Fase 4A em dupla**; medir o tempo | Plano de Testes: CT30 em diante (banco); apoio à Fase 3 | Fase 3 (estoque, pedido) e Fase 4A | Telas de Estoque e Pedido com o backend real |
| **Sáb 31** | README; CT20 com um colega; relatório da Sprint 3 | Revisar os PRs; documentos restantes | PRs revisados; (se a A3 incluir) deploy | Tela 12; testes de navegador |
| **Dom 01/11** | **Review e Retrospectiva**; evidências | Evidências do banco | Congelamento do código | Evidências do frontend |
| **01–06/11** | Janela de testes: só correções (v4.6, §4.4, item 5); fluxo registrar → teste que reproduz → `fix/...` → PR → retestar | | | |

Se algo escorregar, **corte nesta ordem** (do primeiro a sair ao último): telas de Estoque e Pedido além do mínimo → `GET /api/estoque/{id}/linhas` → Tela 12 completa (fica só a tabela). **Os Must da Sprint 3 (US1–US7, US9–US12) não saem.**

---

## 8. Estratégia de integração e testes

1. **Contrato antes do código** (A10). Cada lado testa contra o Contrato v2:
   - o Leonardo, com respostas simuladas no `browser.cjs`;
   - o Lucas, com MockMvc;
   - o Kauê, com os `exemplos/`.
2. **Gates.** Uma fase só conta quando o lado de consumo real a usa:
   - Fase 2 → aceite do Leonardo;
   - Fase 3 → telas de Estoque e Pedido;
   - Fase 4A → Tela 12 com 151/1/0.
3. **Pirâmide:**

   | Camada | Ferramenta |
   |---|---|
   | Banco | SQL com `constraints_*.sql` |
   | Python | `pytest` + oráculo |
   | Backend (sem banco) | MockMvc |
   | Backend (com banco) | Testcontainers: S1–S5, concorrência, CT25 |
   | Frontend | Playwright com API simulada |
   | Ponta a ponta | Aceite manual com evidência |

4. **Sem meta de cobertura** (Plano de Testes, §2). Vale a regra de bloqueio de merge do Plano de Testes, §2.
5. **Casos novos:** `sprint-3-casos-de-teste-propostos.md` (CT30 em diante), para o Diogo e o Kauê incorporarem ao `docs/Plano_de_Testes_Mente_Fria.md` até 31/10.
6. **Evidências** registradas **durante** a sprint em `docs/Sprint_3/evidencias/`: saída dos testes, prints, PRs. Não deixar `[PREENCHER]`.

---

## 9. Riscos

| Risco | Prob./Impacto | Mitigação |
|---|---|---|
| **A3 sem decisão:** quatro Must e o deploy fora; janela de testes sem sistema publicado | Alta / Alto | Decisão em 27/10; recomendação mínima: incluir a US20 |
| Gargalo no Lucas (caminho crítico inteiro) | Alta / Alto | Código validado em `referencia/backend/`; Diogo nos repositórios; Kauê em dupla na 4A |
| Contrato v2 não aprovado a tempo | Média / Alto | Proposta pronta; reunião de 30 min na segunda 26 ou na terça 27 |
| Trabalho só local, sem push (nada no GitHub desde 14/10) | Média / Alto | **Um PR por fase, todos os dias**; a revisão de PR é obrigatória (README) |
| Teste de navegador instável (A13) | Média / Médio | Investigar antes da janela de testes |
| Licença WLS: 2 sessões; validade de 90 dias; só o titular usa | Média / Médio | Não rodar a suíte inteira com WLS; conferir a validade; A14 |
| Merge do branch `origin/sprint-3` revertendo o `.env` (A15) | Baixa / Médio | Fechar ou atualizar o branch |
| Fuso horário (validade e data do pedido) | Média / Médio | F3 + A9 |
| RAM de 512 MB no Render (Java + Python + `gurobipy`) | — | Só se a A3 incluir o deploy; medir no primeiro deploy |

---

## 10. Critérios para considerar a Sprint 3 pronta (01/11)

- [ ] Login pelos dois perfis com backend real; `401`/`403` em JSON (CT05–CT08).
- [ ] Cadastros de US4–US7 com erros por campo vindos do backend real (CT09–CT19).
- [ ] Entrada de estoque com validade; saída convencional e descarte; vencido nunca vendido (CT21, CT22, CT26, CT27 e CT30 em diante).
- [ ] Pedido com baixa por linhas, atômico (CT25, CT28).
- [ ] Recomendação de produção real na Tela 12 com o seed de demonstração = 151/1/0 e linguagem sem termos técnicos (US11).
- [ ] Testes: banco `32|0` e `41|0`; Python `25 passed` em 2 máquinas; backend `mvnw test` verde (inclusive Testcontainers); Playwright estável.
- [ ] Contrato v2 oficial; Plano de Testes atualizado; README sem "planejadas" para variáveis já em uso.
- [ ] Um PR revisado por fase, na `main`; evidências em `docs/Sprint_3/evidencias/`; retrospectiva registrada.
- [ ] Decisão A3 registrada (e, se incluída, a US20 publicada).
