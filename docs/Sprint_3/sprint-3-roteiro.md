# Roteiro — Sprint 3 — Mente Fria (v5.2)

**Revisão:** 27/10/2026 (respostas da equipe). **Bases anteriores:** v5.1 (noite de 26/10, em `historico/v5.1-27-10-antes-das-respostas/`), v5 (tarde de 26/10, em `historico/v5-26-10-antes-das-4-historias/`) e v4.6, de Kauê e Diogo (`historico/sprint-3-roteiro-v4.6.md`).
**Período:** **26/10 (seg) a 01/11/2026 (dom)**. Janela de testes e correções: **01/11 a 06/11/2026** (decisão A1).
**Documentos irmãos (nesta pasta):**
- `sprint-3-decisoes-e-pendencias.md`: decisões e bloqueios, com IDs F, D, P e A.
- `sprint-3-contrato-v2-proposta.md`: contrato **aprovado em 27/10**, para o Kauê aplicar ao oficial (igual ao código validado).
- `sprint-3-mensagem-professor.md`: mensagem pronta para o professor (P3).
- `sprint-3-matriz-rastreabilidade.md`, `sprint-3-casos-de-teste-propostos.md`, `sprint-3-relatorio-auditoria.md`.
- Os 4 tutoriais: `sprint-3-tutorial-*.md`.
- `referencia/`: código, SQL e arquivos de deploy **já executados**, prontos para copiar.

---

## 0. O que mudou

### 0.00 Resposta do professor (27/10, noite) — v5.3

Texto completo em `resposta-professor.md`.
1. **Adendo ao Termo aceito:** rascunho em `sprint-3-adendo-termo-de-aceite.md` (A33), com o cálculo do lucro explícito. Os demais documentos só mudam **depois** da aprovação do adendo.
2. **Licença do Gurobi no Render NÃO autorizada** até a Gurobi confirmar por escrito (A30). Pedido pronto em `sprint-3-licenca-gurobi.md`; plano B a decidir (A31).
3. **Sistema publicado até 30/11**, com backup e restauração testados (A32). O banco definitivo passa a ser criado **no sábado 31 à noite**.
4. **Obrigatórios primeiro:** a lista de cortes mudou (§7). A sprint extra só acontece com o obrigatório testado e estável (A28).
5. **Lacuna encontrada:** o Termo aceitou "Must + Should" e cita alertas de vencimento e de reposição no critério de pronto, mas US15/US16 estão fora. O adendo, item 4, propõe como tratar (decisão da equipe).
6. **Plano B da licença decidido: B1** (A31). No Render, `OTIMIZADOR_HABILITADO=false`: produção e reposição respondem `503` com mensagem, e o resto funciona. A otimização é demonstrada pelo Kauê, com a WLS dele. Implementado e testado: a suíte do backend vai a **95** e as funções de apoio a **15**.

### 0.0 Da v5.1 para a v5.2 (27/10, respostas da equipe)

1. **Contrato v2 aprovado** (A10) e aplicado ao oficial (item 9).
2. **Mensagem ao professor pronta** em `sprint-3-mensagem-professor.md` (P3): mudanças de conteúdo, licença, data da avaliação e sprint extra.
3. **A16, A17 e A20 aprovadas.** O catálogo de marketing passou de 12 para **18 ideias** (A20). Na A17, o copo continua na lista de ingredientes, porque no banco ele é um.
4. **Descarte por linha (A12) entrou**, como opcional: backend pronto e testado (CT71; a suíte vai a **92**). Se apertar, o botão é o primeiro corte (A28).
5. **Render (A21): deploy definitivo no sábado 31**, para o banco gratuito durar até 30/11. Ensaio opcional na qua/qui numa conta pessoal, apagado no mesmo dia.
6. **P2: `gurobipy` 13.0.3.**
7. **A15 corrigida:** um merge do branch `sprint-3` **não** desfaz o PR #24 (simulado e, depois, conferido no merge real, PR #25: A29). Os documentos chegaram à `main` pelo PR #27. Falta juntar os dois `.env.example`.
8. **A28: gatilhos na sexta 30 à noite**: cortar o botão da A12 se houver atraso; Sprint 4 relâmpago no sábado se tudo estiver pronto.
9. **Contrato oficial atualizado para a v2** (A10), com autorização do Kauê; falta o commit.

### 0.1 Da v5 para a v5.1 (noite de 26/10)

1. **Escopo (A2b): entram US14, US17, US18 e US20.** Com elas, **todos os Must do Termo de Aceite estão na Sprint 3**. O bloqueio A3 foi encerrado.
2. **O backend inteiro está escrito e testado em `referencia/backend/`** (decisão do usuário: "código completo validado"), incluindo os repositórios do Diogo.
   - **91 testes** passam com PostgreSQL 16 real (Testcontainers) e com o Python real.
   - Experimentos "quebrar de propósito" derrubaram os testes certos.
   - O trabalho de cada um passa a ser **copiar, entender, rodar os testes, abrir o PR e fazer o aceite**, e não escrever do zero.
3. **Reposição (US14):** o modelo da Fase Q foi formalizado e implementado em `referencia/python/app/reposicao.py`. São 22 testes e um oráculo de força bruta em 150 instâncias.
4. **Deploy (US20):** `Dockerfile`, `render.yaml`, *workflow* do GitHub Pages e `config.js` estão em `referencia/deploy/`.
   - A imagem foi montada e rodada com os limites do plano free (512 MB, 0,1 CPU): sobe em 83 s, pico de 133 MiB, fluxo completo sem falhas.
5. **Docker da máquina do Diogo (BD01): causa encontrada** (A26). O banco foi revalidado no Docker real (PostgreSQL 16.15), com os mesmos resultados.
6. Novas decisões A16–A27; três delas são **propostas para o PO confirmar** (A16, A17, A20).

### 0.2 Da v4.6 para a v5 (tarde de 26/10)

1. Calendário refeito (nada foi entregue no GitHub depois de 14/10; último commit `ac028cd`, PR #24).
2. Contrato v2 vira pré-requisito explícito (A10).
3. Correções validadas em execução: A4 (não compilava), A5 (não subia), A7 (`400` virava `500`), A8 (BOM no Python), A9 (fuso de São Paulo).
4. Caminhos dos tutoriais corrigidos (`docs/Sprint_3/referencia/`).
5. Cancelar pedido **não** devolve estoque (A6).

---

## 1. Objetivo e entregas da Sprint 3

**Objetivo:** em 01/11, o sistema está **publicado numa URL HTTPS** e funciona de ponta a ponta, com banco, backend e frontend reais, nos dois perfis:

> cadastro e login → cadastros (ingrediente, produto, preço) → entrada de estoque com validade → recomendação de produção por lucro (Tela 12) e sugestões de campanha (Tela 14) → pedido com baixa automática por linhas → reposição com orçamento (Tela 13) → dashboard do mês (Tela 03).

É o fluxo do critério de pronto do Termo de Aceite.

| Entrega | Histórias | Responsável principal |
|---|---|---|
| Segurança (JWT), CORS e erros padronizados | Base de US1–US7 | Lucas |
| Autenticação, usuários, unidades, ingredientes, produtos e preço | US1–US7 | Lucas (+ Diogo nos repositórios) |
| Schema novo, 41 testes, seed de demonstração, SQL da Sprint 3 (consultas 1–13) | Base de US9, US12, US14, US17 | Diogo |
| Módulo Python v2 (lucro) e Contrato v2 | US10 | Kauê |
| Estoque por linhas e pedido com baixa | US9, US12 | Lucas + Diogo |
| Ponte Java → Python e recomendação | US10, US11 | Lucas + Kauê |
| **Reposição com orçamento** (Python e Java) | **US14** | **Kauê** (+ Diogo no SQL) |
| **Dashboard e marketing** | **US17, US18** | **Lucas** (+ Diogo no repositório do dashboard) |
| **Publicação** (Render + GitHub Pages) | **US20** | **Kauê e Lucas** (Render), **Leonardo** (Pages) |
| Frontend: módulos, guarda, aceite real e as Telas 10/16, 11/17, 12, **03, 13 e 14** | US1–US7, US9, US11, US12, US14, US17, US18 | Leonardo |
| Plano de Testes (CT23, CT24, CT29 e CT30–CT72), relatório, evidências, retrospectiva | Processo | Kauê (facilitação) + todos |

---

## 2. Estado real em 26/10/2026 (noite)

Verificado no repositório e por execução. Detalhes no relatório de auditoria, §B e §E.

| Camada | No repositório (`main`, `ac028cd`) | Pronto **fora** do repositório (em `referencia/`, já executado) |
|---|---|---|
| Banco | DDL da Sprint 1 e 32 testes (32/32) | Patch do DDL; **41/41** e **32/32** no Docker real; `seed_demo.sql`; consultas 1–13; FEFO e concorrência (30/30) |
| Backend | Só `GET /api/saude` e `GET /api/ingredientes` (3 campos) | **Tudo** de US1–US7, US9–US12, US14, US17, US18: **95 testes** (18 de segurança, 13 da regra do açaí, 54 de integração com PostgreSQL 16 e 7 com o Python real) |
| Python | `otimizador.py` v1 (receita): 8/8 | v2 (lucro) **25/25** e **reposição 22/22**, com `gurobipy` 13.0.1 e 13.0.3; oráculos de força bruta |
| Frontend | `app.js` com 235 linhas; 22 verificações do Playwright (instáveis: A13) | Divisão em módulos (25/25 telas idênticas); guarda por lista; funções de apoio das Telas 03/13/14 e `config.js` de produção (**14/14**) |
| Deploy | Nada | `Dockerfile`, `.dockerignore`, `render.yaml`, *workflow* do Pages: imagem testada com 512 MB e 0,1 CPU |
| Documentos | Visão, Termo e Backlog dizem "receita"; DER sem linhas; Contrato sem v2 | Contrato v2 (proposta igual ao código), decisões, casos de teste CT30–CT72 |

---

## 3. Pendências herdadas das Sprints 1 e 2

Classificação pela evidência, não pelo que os relatórios afirmam.

| Pendência | Estado comprovado | Onde é resolvida |
|---|---|---|
| Backend de US1–US7 | Pendente no repositório; **pronto fora** | Fases 1 e 2 (Lucas) |
| Hello World Java → Python | Pendente no repositório; **pronto fora** (eco real no perfil `dev`) | Fase P + Fase 4A |
| CORS | **Pronto fora** (inclusive a origem do GitHub Pages) | Fase 1 |
| Python fora do PC do Kauê | **Pendente** | Fase P (2ª máquina), com a licença restrita do `pip`. A WLS acadêmica do Kauê é pessoal (A34) |
| Testes do backend, Testcontainers | **Prontos fora** (95) | Fases 1–6 |
| `.env` | Corrigido em 14/10 (PR #24) | Concluído; README a alinhar |
| CT20 (subir do zero só com o README) | Nunca executado | Fase 8 (Kauê) |
| `[PREENCHER]`/`[CONFIRMAR]` da Sprint 2 | Pendente | Fase 8. Só completar com fatos |
| BE03 (`500` com *Whitelabel*) | Causa não registrada | A Fase 1 elimina o HTML de erro; registrar a hipótese |
| **BD01** (Docker) | **Causa encontrada em 26/10** (A26): *sockets* travados nas pastas do Docker; contorno verificado | Diogo registra |
| Retrospectiva da Sprint 2, Reviews, perda do Git | Pendentes | Fase 8 (Kauê) |
| `requirements.txt` em UTF-16; versão do Gurobi | Pendente (P2) | Fase P |
| Branch `origin/sprint-3` | **Merges feitos e conferidos** (PRs #25, #26 e #27; A29) | Kauê (juntar os `.env.example`; apagar o `sprint-3`) |

---

## 4. Escopo (decisão A2b)

**Dentro:** US1–US7, US9, US10, US11, US12, **US14, US17, US18, US20** (todos Must), as mudanças de banco que viabilizam US9/US12/US27 (status do pedido já no DDL) e a Fase 8 (qualidade e encerramento).

**Dentro, como opcional:** o botão "Descartar" por linha (A12, entrou em 27/10). É o primeiro corte se houver atraso (A28).

**Fora da Sprint 3:** US13 (Could), US15 e US16 (Should), US19, US26 e US27 (Could). **Sprint 4 relâmpago:** se, na sexta 30 à noite, tudo da Sprint 3 estiver na `main` e as seis telas prontas, o sábado 31 pode receber 1 ou 2 histórias pequenas (sugestão: US27 e US26), sem atrasar o deploy definitivo (A28). Se o professor estender a janela de testes, a Sprint 4 relâmpago também pode usar o início dela (P7, mantida em 27/10). Caso contrário, as histórias ficam para depois da janela de testes, se o professor permitir.

**Mudanças de conteúdo a comunicar ao professor (P3, terça 27; mensagem pronta em `sprint-3-mensagem-professor.md`):** receita → lucro (F9); reposição com orçamento; dashboard com a receita máxima estimada do último cálculo (A16, A17); licença de avaliação do Gurobi no deploy (A22); banco criado em 31/10, válido até 30/11 (A21); sprint extra (A28).

---

## 5. Fases, responsáveis e critérios de conclusão

As regras de negócio detalhadas da v4.6 **continuam valendo**: estoque por linhas e FEFO (§5 Fase 3, §10.1), modelo de produção (§3, Fase P), reposição (Fase Q), dashboard e marketing (Fase 6). As decisões que faltavam estão nas decisões A16–A20.

**Como cada fase é feita agora:** o código está em `referencia/`. Cada um **copia o pedaço da sua fase, lê, roda os testes daquela fase, abre o PR e faz o aceite**. "Pronto" exige o PR revisado e a evidência; não basta copiar.

| Fase | O que entrega | Quem | Depende de | Pronto quando… (verificável) | Tutorial |
|---|---|---|---|---|---|
| **0 — Contrato v2 e aviso** | Contrato v2 **aplicado ao oficial em 27/10** (falta o commit; o Kauê revisa); mensagem ao professor (P3) | Kauê (revisão), Diogo e Lucas (aviso) | — | Commit do contrato; mensagem enviada e resposta guardada | Kauê §2; `sprint-3-mensagem-professor.md` |
| **1 — Fundação do backend** | Segurança, JWT, CORS, erros | Lucas | — | `SegurancaEErrosTest`: **18/18** | Lucas §2 |
| **Banco** | Patch do DDL, 41 testes, seed de demonstração, `sql_repositorios_sprint3.md`, repositórios Java | Diogo | — | `32 \| 0` e `41 \| 0`; experimentos A e B; repositórios no PR com os testes das fases | Diogo §2–§8 |
| **P — Python v2** | `otimizador.py` v2, exemplos, `requirements.txt` UTF-8, 2ª máquina | Kauê | Contrato §1 | `25 passed` (com a Fase Q, `47 passed`) em **duas** máquinas | Kauê §3–§7 |
| **2 — CRUD real** | US1–US7 | Lucas + Diogo | Fase 1, Banco | `Fase2CrudTest`: **15/15** e `RegraAcaiTest` 13/13; aceite do Leonardo | Lucas §3 |
| **Front 1** | `app.js` dividido; guarda por lista | Leonardo | — | `IGUAIS`; guarda conforme a tabela | Leonardo §2–§4 |
| **Gate da Fase 2** | Aceite real | Leonardo | Fase 2 | 16 evidências | Leonardo §5 |
| **3 — Estoque e pedido** | Movimentações, estoque, linhas, pedidos e descarte por linha (A12) | Lucas + Diogo | Fase 2, Banco, A10 | `Fase3EstoquePedidoTest`: **15/15** | Lucas §4 |
| **4A — Python no Java** | Ponte, eco (`dev`), recomendação | Lucas + Kauê | Fases P e 3 | `Fase4ProducaoTest` **7/7**; `PythonRealTest` e `ProcessoPythonTest` com `MF_PYTHON` | Lucas §5; Kauê §8 |
| **Q + 5 — Reposição (US14)** | `reposicao.py` e `POST /api/reposicao/recomendacao` | **Kauê** (+ Diogo no SQL 9) | Fases P e 3 | `pytest`: **47 passed**; `ReposicaoTest` **8/8**; `PythonRealTest.reposicaoContaAMao` | Kauê §11 |
| **6 — Dashboard e marketing (US17, US18)** | `GET /api/dashboard` e `GET /api/marketing/sugestoes` | **Lucas** (+ Diogo no repositório) | Fases 3 e 4A | `DashboardTest` **5/5**; `MarketingTest` **4/4** | Lucas §6; Diogo §7 |
| **7 — Publicação (US20)** | Render (backend + Postgres) e GitHub Pages | **Kauê e Lucas** (Render), **Leonardo** (Pages) | Ensaio opcional (qua/qui, conta pessoal); **definitivo no sáb 31** com tudo na `main` (A21) | URL HTTPS no README; data de expiração do banco; CT67–CT70 | Kauê §12; Leonardo §9; Lucas §8 |
| **Front 2** | Telas 10/16, 11/17 e 12 | Leonardo | Fases 3 e 4A | Testes de navegador; **sem termo técnico** | Leonardo §6.1–§6.3 |
| **Front 3** | Telas **03, 13 e 14** | Leonardo (+ Lucas na 14, se preciso) | Fases 5 e 6 | Testes de navegador; funções de apoio 15/15 | Leonardo §6.4–§6.6 |
| **8 — Qualidade e encerramento** | Plano de Testes (até CT72), README, evidências, relatório, retrospectiva, CT20 | Todos (Kauê facilita) | — | Plano completo **até 31/10** | Kauê §9; Diogo §9 |

---

## 6. Dependências e caminho crítico

```
Contrato v2 (Kauê, 27/10) ─┬─> Fase 3 ──> Fase 4A ──┬─> Fase 6 (marketing usa o último plano) ──> Telas 03 e 14
                           │                        └─> Tela 12
                           └─> Telas 10/11 (Leo)
Fase 1 ──> Fase 2 ──> Gate (Leo) ──> Fase 3 ──> Fase 5 (reposição usa pedidos) ──> Tela 13
Banco (Diogo, 27/10) ──> Fases 2, 3, 5 e 6
Fase P + Q (Kauê, 27–28/10) ──> Fases 4A e 5
Fases 1–2 na main (28/10) ──> ENSAIO opcional (conta pessoal, apagado no mesmo dia) ··· tudo na main ──> DEPLOY DEFINITIVO (sáb 31)
```

**Caminho crítico:** Fase 1 → 2 → 3 → 4A → 6 → Telas 03 e 14. Ele passa pelo Lucas e, no fim, pelo Leonardo, que tem **seis telas**. Para aliviar:
- todo o Java está pronto em `referencia/`: o Lucas copia e revisa, não escreve;
- a reposição inteira (Python e Java) é do Kauê;
- os repositórios são do Diogo;
- o Lucas pode assumir a Tela 14 (a mais simples) no sábado;
- as funções de apoio das Telas 03, 13 e 14 já estão testadas.

---

## 7. Calendário (26/10 a 01/11 e janela de testes)

| Dia | Kauê | Diogo | Lucas | Leonardo |
|---|---|---|---|---|
| **Seg 26** (noite) | Ler a proposta do Contrato v2 | Ler o tutorial; Docker funcionando (A26) | Ler o tutorial | Baseline (22 OK) e retratos |
| **Ter 27** | Contrato aprovado e aplicado; Fase P (copiar e testar) | **PR do banco** (DDL + 41 testes); **mensagem ao professor** (com o Lucas); repositórios de usuário, unidade, ingrediente e produto | **PR da Fase 1** (18/18); começar a Fase 2 | Dividir o `app.js`; guarda; PR |
| **Qua 28** | **PR da Fase P + Q** (47 testes); ensaio de deploy (opcional, conta pessoal) | Repositórios de movimentação, pedido, reposição e dashboard; `seed_demo.sql`; `sql_repositorios_sprint3.md` | **PR da Fase 2** (15/15) → avisar o Leo; ensaio de deploy com o Kauê (opcional) | `config.js` + *workflow* do Pages (com o Kauê); Telas de Estoque e Pedido com API simulada |
| **Qui 29** | **Fase 4A em dupla**; Java da reposição; ensaio, se não foi na quarta | Plano de Testes: casos do banco; DER e mapa de constraints | **PR da Fase 3** (15/15); Fase 4A em dupla; **decidir quem cria a conta definitiva do Render** | **Gate da Fase 2** (manhã); Estoque e Pedido com o backend real |
| **Sex 30** | **PR da Fase 5** (reposição); casos do Python e da US14 no Plano | Documentos (Backlog, Termo como adendo, Arquitetura, UML) | **PR da Fase 4A** e **PR da Fase 6** (dashboard e marketing); **à noite, conferir os gatilhos da A28** | Tela 12; Tela 03; botão "Descartar" (A12) |
| **Sáb 31** | **Deploy definitivo, à noite** (conta, banco e serviço criados hoje; o banco vale até 30/11 no mesmo horário; A32) e **backup** logo depois do seed; **CT67–CT70** no ambiente publicado; README; CT20 com um colega | Revisar PRs; Plano de Testes completo; evidências | PRs revisados; Tela 14 (se o Leo precisar) | Telas 13 e 14; testes de navegador |
| **Dom 01/11** | **Review e Retrospectiva**; evidências | Evidências do banco | Congelamento do código | Evidências do frontend; aceite **na URL publicada** |
| **01–06/11** | Janela de testes **no ambiente publicado**: só correções (v4.6, §4.4, item 5); fluxo registrar → teste que reproduz → `fix/...` → PR → retestar | | | |

**Se algo escorregar, corte nesta ordem** (do primeiro a sair ao último; revista em 27/10 pela orientação do professor: os **adicionais** saem antes):
1. Botão "Descartar" por linha (A12). O backend fica; só o botão sai.
2. Filtros do ranking **na tela** (o backend já os tem).
3. Ranking por usuário na Tela 03 (adicional do dashboard; adendo, item 3).
4. Acabamento visual das telas.

**Não cortar:** o bloco "Plano de produção" do dashboard (é a "receita máxima estimada" e os "produtos prioritários" do Termo) e o detalhe por linha da Tela 10 (é o que mostra "Vence em breve" e "Vencida", a cobertura atual do alerta de vencimento; adendo, item 4).

**Conferência da sexta 30, à noite (A28)**, feita pelo PO:
- **Corte:** as Telas 10/16, 11/17 e 12 funcionam com o backend real? Se **não**, o botão da A12 sai.
- **Sprint 4 relâmpago:** todos os PRs das Fases 1–6 estão na `main`, as seis telas prontas **e testadas, sem defeito aberto** nas obrigatórias? Se **sim**, o sábado pode receber US27 e/ou US26, sem atrasar o deploy definitivo nem o CT67–CT70. Na dúvida, não (orientação do professor, 27/10).
- **Licença (A30):** chegou autorização escrita da Gurobi? Se **não**, o deploy de sábado vai com o plano B (A31).

**Nenhuma história Must sai.** Se mesmo assim não couber, o PO conversa com o professor **antes** de 01/11. Não use a janela de testes para desenvolver.

---

## 8. Estratégia de integração e testes

1. **Contrato antes do código** (A10). A proposta do contrato é igual ao código: aprovar é rápido.
2. **Deploy: ensaio cedo, definitivo no sábado (A21, 27/10).** O banco gratuito expira 30 dias depois de criado; para ele durar até 30/11, o definitivo é criado no sábado 31. Para os problemas do Render (memória, tempo, CORS, licença) não aparecerem só no sábado, faça um **ensaio** na quarta ou na quinta numa conta pessoal, apagando tudo no mesmo dia. A simulação local (512 MB, 0,1 CPU) já passou.
3. **Gates.** Uma fase só conta quando o lado de consumo real a usa:
   - Fase 2 → aceite do Leonardo;
   - Fase 3 → telas de Estoque e Pedido;
   - Fase 4A → Tela 12 com 151/1/0;
   - Fases 5 e 6 → Telas 13, 03 e 14;
   - US20 → CT68 na URL publicada.
4. **Pirâmide** (números de `referencia/`):

   | Camada | Ferramenta | Testes prontos |
   |---|---|---|
   | Banco | SQL com `constraints_*.sql` | 32 + 41 |
   | Python | `pytest` + oráculos | 25 (produção) + 22 (reposição) |
   | Backend sem banco | MockMvc | 18 + 13 |
   | Backend com banco | Testcontainers (PostgreSQL 16) | 57 |
   | Ponte Java → Python real | `MF_PYTHON` + `gurobipy` | 7 |
   | Frontend | Playwright com API simulada; funções de apoio com Node | 22 (instáveis, A13) + 14 |
   | Imagem de deploy | Docker com 512 MB e 0,1 CPU | Fluxo completo (14 verificações) |
   | Ponta a ponta | Aceite manual na URL publicada | CT68 |

5. **Sem meta de cobertura** (Plano de Testes, §2).
6. **Casos novos:** CT30–CT72, para o Diogo e o Kauê incorporarem ao Plano de Testes até 31/10.
7. **Evidências** registradas **durante** a sprint em `docs/Sprint_3/evidencias/`.

---

## 9. Riscos

| Risco | Prob./Impacto | Mitigação |
|---|---|---|
| **Leonardo com seis telas** (10/16, 11/17, 12, 03, 13, 14) | Alta / Alto | Funções de apoio prontas e testadas; Lucas assume a Tela 14 se preciso; corte do §7 |
| Gargalo no Lucas (caminho crítico) | Média / Alto | Todo o Java pronto; Kauê com a reposição; Diogo com os repositórios |
| **Render free:** banco expira em 30 dias; serviço dorme após 15 min; 0,1 CPU (sobe em ~83 s; primeiro login ~7 s) | Certa / Médio | Banco criado no sábado 31 (vale até 30/11); data da avaliação perguntada ao professor; `config.js` com 90 s; "acordar" o serviço antes de cada sessão de teste (A21) |
| **Primeiro contato com o Render só no sábado** (se o ensaio não for feito) | Média / Alto | Ensaio na qua/qui numa conta pessoal; simulação local já feita |
| **Licença do Gurobi no servidor não autorizada** (professor, 27/10) | **Alta / Alto** | O Kauê pergunta à Gurobi se a WLS acadêmica dele (válida até 2027, uso pessoal) pode rodar no Render; plano B pronto até sexta (A30, A31, A34) |
| **Banco expira no dia 30/11, data-limite da avaliação** | Média / Alto | Criar no sábado à noite; backups com restauração testada (A32) |
| **Alertas de vencimento e de reposição (Termo) com US15/US16 fora** | Média / Médio | Adendo, item 4: registrar a cobertura atual ou tornar o prazo parametrizável (A33) |
| Contrato v2 não aprovado a tempo | Baixa / Alto | Proposta igual ao código; 30 min de reunião na terça |
| Trabalho só local, sem push (nada no GitHub desde 14/10) | Média / Alto | **Um PR por fase, todos os dias** |
| Docker de outra máquina com o mesmo BD01 | Média / Médio | Contorno da A26 (renomear as pastas; nunca "Reset to factory defaults") |
| Teste de navegador instável (A13) | Média / Médio | Investigar antes da janela de testes |
| Alguém começar trabalho a partir do branch `origin/sprint-3` (caminho antigo do `.env`; A15) | Baixa / Baixo | Branches novos sempre a partir da `main`; apagar o `sprint-3` depois de levar os comentários do `env.example` |
| Resultado do marketing e do dashboard some quando o backend reinicia | Certa / Baixo | Mensagem "Gere a recomendação na tela Produção" (A16, D15) |

---

## 10. Critérios para considerar a Sprint 3 pronta (01/11)

- [ ] **Sistema publicado**: frontend no GitHub Pages e backend no Render, **por HTTPS**, URL no README (US20, CT67–CT70).
- [ ] Login pelos dois perfis com backend real; `401`/`403` em JSON.
- [ ] Cadastros de US4–US7 com erros por campo vindos do backend real.
- [ ] Estoque com validade; saída convencional e descarte; vencido nunca vendido. Botão "Descartar" por linha, se não foi cortado (A12, A28).
- [ ] Pedido com baixa por linhas, atômico.
- [ ] Recomendação de produção na Tela 12 = 151/1/0 com o seed de demonstração, sem termos técnicos.
- [ ] **Reposição** na Tela 13 com período e orçamento (US14).
- [ ] **Dashboard** do mês na Tela 03 com ranking e filtros (US17).
- [ ] **Sugestões de campanha** na Tela 14 (US18).
- [ ] Testes: banco `32|0` e `41|0`; Python `47 passed` em 2 máquinas; backend `mvnw test` verde (inclusive Testcontainers); Playwright estável.
- [ ] Contrato v2 oficial; Plano de Testes até CT72; README sem "planejadas" para variáveis já em uso.
- [ ] Um PR revisado por fase, na `main`; evidências em `docs/Sprint_3/evidencias/`; retrospectiva registrada.
