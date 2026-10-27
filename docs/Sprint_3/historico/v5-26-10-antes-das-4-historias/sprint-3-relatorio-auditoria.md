# Relatório de auditoria — materiais da Sprint 3 — Mente Fria

**Data:** 26/10/2026 · **Pedido:** auditar o roteiro da Sprint 3 (v4.6) e os cerca de 49 artefatos produzidos em 14/10, corrigi-los e entregar roteiro, tutoriais, matriz e lista priorizada.

**Regras seguidas:**
- nenhum commit, push ou operação Git que escreva;
- nada alterado fora de `docs/Sprint_3/`;
- Spring JDBC sem JPA;
- sem meta de cobertura.

---

## A. Resumo executivo

**Os materiais de 14/10 tinham conteúdo técnico majoritariamente correto, mas não estavam prontos para orientar a execução sem correções.**

A parte mais trabalhosa estava certa e foi **confirmada em execução**:
- a regra de estoque por linhas (FEFO, descarte, invariante, concorrência);
- os CHECKs do banco;
- o modelo de produção por lucro;
- a divisão do frontend.

Os problemas que impediam o uso foram:

1. **O tutorial do backend gerava um projeto que não compila e, corrigido isso, uma aplicação que não sobe** (A4, A5). Depois de corrigidos, 2 dos 18 testes de segurança ainda falhavam (`400` virando `500`, A7). Esses três defeitos estavam no código que o Lucas copiaria primeiro.
2. **Os quatro tutoriais apontavam para pastas inexistentes** (`referencia_bd/`, `referencia_python/`, `referencia_frontend/`) e para módulos sem a estrutura `screens/`.
3. **O comando de teste do módulo Python pelo PowerShell falhava** (BOM, A8).
4. **O calendário expirou** e **nada da Sprint 3 chegou ao GitHub desde 14/10**.
5. **Quatro histórias Must (US14, US17, US18, US20) ficaram sem sprint** com o novo prazo (A3). Além disso, a janela de testes foi desenhada para o sistema publicado. **É o maior risco do projeto hoje, e não é técnico:** precisa de decisão do PO com o professor.
6. Contradições pontuais: cancelar pedido "devolve o estoque" (A6); Compute Server no desenvolvimento × código (A11); a `RegraAcai` dada como "pronta" sem existir.

**Situação depois da auditoria:**
- os documentos revisados e o código de referência validado estão em `docs/Sprint_3/`;
- o que depende de execução pelos integrantes, ou de decisão, está listado nas seções F e G, com dono e data.

---

## B. Inventário e cobertura

### B.1 Fontes examinadas

| Grupo | Fontes | Como |
|---|---|---|
| Escopo acordado | Backlog, Documento de Visão, Termo de Aceite (`.docx`); `Diagramas_UML.md`; `DER.md`; Roteiro do Protótipo; roteiro v4.6 | Leitura completa. Os `.docx` dos artefatos têm **o mesmo hash MD5** dos da `main` |
| Estado real | Repositório local e remoto (`git ls-remote`, todos os branches); `backend/`, `frontend/`, `optimization/`, `bd/` em `origin/main` (`ac028cd`) | Leitura e **execução** (testes da `main`: Python 8/8, banco 32/32, Playwright) |
| Histórico | Relatórios, atas, evidências e contribuições das Sprints 1 e 2; `sprint-1-roteiro.md` | Leitura; classificação por evidência |
| Orientação (os artefatos) | **53 arquivos** em `Downloads\Artefatos` + o roteiro v4.6 + os 2 tutoriais da Sprint 1 em PDF | Leitura completa de todos; **execução** dos que são executáveis |
| Arquitetura e contratos | `Arquitetura.md`, `Contrato_de_Comunicacao`, `mapa_constraints.md`, `sql_repositorios.md`, `Pendencias_ARQUITETURA…` | Leitura e comparação com o código |

**Os 53 artefatos:**
- 4 tutoriais da Sprint 3, a análise v4.6, o registro de decisões, a validação do `.env`, as pendências de arquitetura;
- 6 cópias de documentos da `main` (byte a byte idênticas, sem as mudanças planejadas) e 3 `.docx` (idênticos);
- DDL com o patch, `.patch`, `constraints_sprint3_teste.sql`, `seed_demo.sql`, `consultas_sprint3.sql`;
- `otimizador.py` v2, `test_otimizador.py`, `oraculo.py`, 8 JSON de exemplo;
- 11 módulos do frontend, 3 ferramentas `.mjs`, `LEIA-ME`;
- `fefo_referencia.py`, `concorrencia_referencia.py`.

### B.2 Não acessível ou limitado

| Fonte | Limitação | Efeito |
|---|---|---|
| Protótipo no Figma | Sem acesso | Usado o texto do Roteiro do Protótipo |
| Evidências da Sprint 2 no Google Drive (prints) | Sem acesso | Execuções das Sprints 1 e 2 classificadas como "não verificáveis no repositório" |
| Comentários de PR e *issues* (o `gh` não está instalado) | Só o histórico de commits e os refs `refs/pull/*` | Não dá para afirmar quem revisou cada PR |
| Histórico do Git anterior a 27/09 | Perdido (repositório recriado) | — |
| Docker Desktop desta máquina | **O motor não subiu** (sintoma do BD01) | O banco foi validado num PostgreSQL 16.10 embutido (Zonky), equivalente para SQL. Os comandos `docker …` dos tutoriais e o Testcontainers ficaram **não validados** |
| Licença WLS (Lucas e Kauê) | Indisponível | Testes rodados com a licença limitada do `pip` |
| Tutoriais da Sprint 1 em PDF | Linhas longas de código **cortadas no próprio PDF** | Usados como modelo de didática, não como fonte de código |

---

## C. Problemas identificados

| # | Problema | Fonte | Evidência | Impacto | Gravidade | Correção |
|---|---|---|---|---|---|---|
| 1 | **O projeto não compila** com o `GlobalExceptionHandler` do tutorial | Tutorial do backend §2.4 + `backend/pom.xml` | `mvnw test` → `package org.postgresql.util does not exist` (driver com `scope runtime`) | O Lucas pararia na primeira fase | **Crítica** | A4: tirar o `scope runtime`. Diff validado em `referencia/backend/pom.xml.diff` |
| 2 | **A aplicação não sobe** com o `SecurityConfig` do tutorial | Tutorial do backend §2.4 | `NoUniqueBeanDefinitionException: CorsConfigurationSource … found 2` | Idem | **Crítica** | A5: `UrlBasedCorsConfigurationSource` |
| 3 | **Requisição inválida devolve `500`**, e não `400` | Tutorial do backend §2.4 | Testes `idNaoNumericoNaRotaDa400` e `parametroObrigatorioAusenteDa400`: esperado 400, obtido 500 | Contraria o Contrato §2 (500 só para falha técnica) e o critério de bloqueio | Alta | A7: handlers adicionados; 18/18 |
| 4 | **Quatro Must (US14, US17, US18, US20) sem sprint**; a janela de testes pressupõe o sistema publicado | Roteiro v4.6 §2.1 e §4.4 × prazo de 01/11 × Termo §2 | — | Risco de não cumprir o Termo de Aceite | **Crítica (decisão)** | A3: levar ao professor até 27/10 (opções nas decisões) |
| 5 | Caminhos inexistentes em todos os tutoriais (`referencia_*`); módulos do frontend sem `screens/` | 4 tutoriais; `LEIA-ME` | Busca em Downloads e em 19 zips: nada encontrado | Nenhum passo com "copie de…" funcionaria | Alta | Material organizado em `docs/Sprint_3/referencia/`; caminhos reescritos |
| 6 | Calendário expirado; nada no GitHub desde 14/10 | Roteiro v4.6 §4.3; `git ls-remote` | Último commit: `ac028cd` (14/10) | Planejamento inválido | Alta | Roteiro v5, calendário de 26/10 a 01/11 |
| 7 | O Contrato v2 (Java↔Python e REST) não existe; o CORS do contrato não tem `PATCH`; a mensagem do `401` sem token não está definida | Contrato (= `main`) | Diff byte a byte | Backend e frontend implementariam formatos diferentes | Alta | Proposta em `sprint-3-contrato-v2-proposta.md` (A10) |
| 8 | **O comando do PowerShell para testar o módulo Python falha** ("JSON de entrada inválido") | Tutorial do Python §6 | Os bytes recebidos começam com `EF BB BF` | O Kauê acharia que o módulo está quebrado | Alta | A8: `utf-8-sig` + teste novo; alternativa com `cmd /c` |
| 9 | "Cancelar pedido devolve o estoque" | Comentário do DDL; tutorial do backend ("Depois") | Contra a decisão de 14/10 (roteiro v4.6 §0, v4.2 item 4) | Implementação errada da US27 | Média | A6: textos corrigidos |
| 10 | Compute Server no desenvolvimento (Arquitetura e Plano de Testes) × `otimizador` v2 sem suporte | `Arquitetura.md` §2.1, §3.3; Plano de Testes §4.2 | O código só lê as variáveis `GRB_WLS*` | Ambiente de desenvolvimento divergente | Média | A11: decisão pendente; recomendação: WLS direta |
| 11 | Data e hora (`data_pedido`, `data_movimentacao`) via `DEFAULT CURRENT_TIMESTAMP`, em UTC no Render | DDL; consultas | Revisão com a skill `sql-code-review` | Pedido às 22h de 31/10 entra no mês 11 (US17) | Média | A9: `:agora` vindo do `Clock` |
| 12 | "`RegraAcai` pronta com 15 casos" | Tutorial do backend §3.4 | O `sql_repositorios.md` tem métodos estáticos numa classe de exemplo; 9 linhas e 12 nomes | O Lucas procuraria uma classe que não existe | Média | Tutorial instrui a criar a classe e o teste |
| 13 | `RelogioConfig` sem `package` e sem `import` | Tutorial do backend §2.4 | Não compila ao copiar | — | Baixa | Arquivo completo em `referencia/` |
| 14 | Pacotes e artefatos de teste do Boot 4 imprecisos | Tutorial do backend | Conferido no Maven Central e no guia de migração | Erros de compilação nos testes | Média | `spring-boot-starter-security-test`; `org.springframework.boot.webmvc.test.autoconfigure`; `@AutoConfigureMockMvc` obrigatório; Testcontainers sem versão |
| 15 | O conversor do `requirements.txt` pode esvaziar o arquivo; o motivo citado (pip × UTF-16) está errado | Tutorial do Python §2 | O `pip` decodifica UTF-16 com BOM (código do `pip`) | Perda do arquivo | Média | Comando com parênteses; motivo corrigido (diff do Git) |
| 16 | Licença WLS acadêmica: 2 sessões; rodar a suíte inteira pode estourar | Tutorial do Python §7 | Documentação do Gurobi | Falha intermitente de licença | Média | Aviso: rodar só `-k "contrato or didatico"` com WLS |
| 17 | Função PowerShell `Chamar` usa `$args` (variável automática) | Tutorial do backend §2.5 | Semântica do PowerShell | Comportamento imprevisível | Baixa | Renomeada para `$p` |
| 18 | Referências cruzadas erradas (F9/F10 → "Parte 8" do Diogo; "banco de demonstração (Parte 7)") e CT30 em diante com dois usos | Registro de 14/10; tutorial do banco | Leitura cruzada | Confusão | Baixa | Referências refeitas; CT30–CT50 unificados |
| 19 | Lacunas da Fase 3: `GET /api/pedidos`, `GET /api/estoque/{id}/linhas`, atalho de descarte | Tutorial do backend × roteiro v4.6 Fase 3 e Contrato §2.2 | — | Telas sem endpoint | Média | Incluídos (A12: o atalho fica opcional) |
| 20 | **Playwright instável** (verificações 11, 20 e 21) | `frontend/tests/browser.cjs` | Original: 4 de 8 rodadas ok; dividido: 3 de 8 | Janela de testes sem um teste confiável | Média | A13: diagnóstico atribuído ao Leonardo (não é efeito da divisão) |
| 21 | Branch `origin/sprint-3` com o `.env` antigo | Git | `backend/env.example`; `./backend/.env` | Um merge reverte o PR #24 | Média | A15 |
| 22 | Contradições históricas: retrospectivas marcadas × "não realizada"; `.env` "feito" na Sprint 2 × corrigido em 14/10; datas do repositório (27/09 × 30/09) | Relatórios e contribuições das Sprints 1 e 2 | Leitura cruzada com o Git | Credibilidade das evidências | Baixa | Registrar na retrospectiva; **não** reescrever o histórico |

---

## D. Correções realizadas (todas em `docs/Sprint_3/`, sem commit)

| Arquivo | Novo ou alterado | O que e por quê |
|---|---|---|
| `sprint-3-roteiro.md` | Novo (v5) | Calendário, escopo, estado real, pendências herdadas, fases com critérios verificáveis, caminho crítico, estratégia de testes, riscos e A3. Mantém as regras do v4.6 por referência |
| `sprint-3-tutorial-backend-lucas.md` | Novo (substitui o de 14/10) | Problemas 1–3, 9, 12–14, 17 e 19; seções de `401`/`403` em JSON e `@RestControllerAdvice`; Fase 1 com código validado |
| `sprint-3-tutorial-banco-diogo.md` | Novo (substitui) | Problemas 5, 9, 11 e 18; resultados reais; assinaturas alinhadas ao backend (`:agora`, `Optional`) |
| `sprint-3-tutorial-frontend-leonardo.md` | Novo (substitui) | Problemas 5, 7 e 20; gate dependente do contrato |
| `sprint-3-tutorial-python-kaue.md` | Novo (substitui) | Problemas 7, 8, 10, 15, 16 e 21; facilitação |
| `sprint-3-decisoes-e-pendencias.md` | Novo (substitui o registro de 14/10) | F1–F12 revalidadas; A1–A15; divergências entre skills e projeto |
| `sprint-3-contrato-v2-proposta.md` | Novo | Problema 7 (para aprovação; não substitui o contrato oficial) |
| `sprint-3-casos-de-teste-propostos.md` | Novo | CT30–CT50 no modelo da skill `test-scenarios`; ajustes em CT23, CT24 e CT29 |
| `sprint-3-matriz-rastreabilidade.md` | Novo | Rastreabilidade com estado comprovado e prioridade |
| `historico/` | Novo | Cópias intactas do v4.6, da análise, do registro, dos 4 tutoriais originais, da validação do `.env` e das pendências de arquitetura |
| `referencia/bd/Script_DDL.sql` e `Script_DDL_sprint3.patch` | Alterados (só o comentário) | Problema 9 (o patch continua aplicando: verificado) |
| `referencia/python/app/otimizador.py` | Alterado | A8 (`utf-8-sig`) |
| `referencia/python/tests/test_otimizador.py` | Alterado | Teste novo `test_entrada_com_bom_e_aceita` |
| `referencia/backend/` | Novo | Fase 1 validada (6 classes, 2 classes de teste, `pom.xml.diff`) |
| `referencia/frontend/` | Reorganizado | `js/screens/` como os imports exigem; `LEIA-ME` e o cabeçalho do `testar_guarda.mjs` corrigidos |

**Não alterado:** `bd/`, `backend/`, `frontend/`, `optimization/`, os documentos oficiais e os originais em Downloads.

---

## E. Validações executadas

Todas em cópias no diretório temporário da sessão, fora do repositório.

| # | Validação | Resultado | Classificação |
|---|---|---|---|
| 1 | Python da `main` (baseline) | `8 passed` (Python 3.14.0, `gurobipy` 13.0.1) | **Aprovado** |
| 2 | Python v2 | `25 passed` com `gurobipy` 13.0.1 **e** 13.0.3 | **Aprovado** |
| 3 | Quebrar de propósito (Python) | Lucro → receita: 5 falhas; sem eco: 5 falhas | **Aprovado** |
| 4 | Exemplos via subprocesso × `saida_*.json` | 4 de 4 iguais; erro tratado com código 0 | **Aprovado** |
| 5 | Plano 151/1/0 e 66/0 pelo **oráculo de força bruta** (sem solver) | Iguais (margens 1.011,71 e 438,24) | **Aprovado** |
| 6 | Pipe do PowerShell com o módulo de 14/10 | **Falha** (BOM) → corrigido (A8); o pipe e o `cmd /c` funcionam | **Aprovado com ressalva** (corrigido) |
| 7 | `git apply --check` do patch | Aplica; resultado = DDL de referência (ignorando o fim de linha) | **Aprovado** |
| 8 | Banco: 32 testes na `main`; 32 + 41 no schema novo | `32 \| 0`; `32 \| 0`; `41 \| 0` (PostgreSQL 16.10) | **Aprovado** |
| 9 | Experimentos A e B | `37 \| 4` (T55, T62, T64, T66) e `38 \| 3` (T58, T62, T63) | **Aprovado** |
| 10 | `seed_demo.sql` | Recusa sem usuários e numa segunda execução; valores, totais e ranking conferidos; invariante = 0 | **Aprovado** |
| 11 | FEFO (S1–S5) e concorrência | Conferem; 30/30 e 30/30 | **Aprovado** |
| 12 | Metadados de erro do PostgreSQL (base do tradutor de erros Java) | 23502 com tabela e coluna; 23503, 23505 e 23514 com a constraint | **Aprovado** |
| 13 | Backend Fase 1 (código do tutorial de 14/10, sem correção) | Não compila → não sobe → 16 de 18 | **Reprovado** (problemas 1–3) |
| 14 | Backend Fase 1 corrigido | `Tests run: 18, Failures: 0` | **Aprovado** |
| 15 | `dependency:tree` (Jackson × jjwt) | Jackson 3.1.5 e Jackson 2.21.5 coexistindo | **Aprovado** |
| 16 | Frontend: retratos antes e depois da divisão | 25 de 25 `IGUAIS` | **Aprovado** |
| 17 | Guarda antigo e guarda novo | Tabela do tutorial reproduzida; telas iguais | **Aprovado** |
| 18 | Playwright (22) | Passa de forma intermitente, **original e dividido** | **Aprovado com ressalva** (A13) |
| 19 | Backend Fases 2, 3 e 4A (Java) | Não compilado | **Não validado** |
| 20 | Testcontainers e comandos `docker` | Motor do Docker indisponível | **Bloqueado** (BD01) |
| 21 | Licença WLS; Python 3.14.7; tempo do solver | Indisponíveis | **Bloqueado** |
| 22 | Integração ponta a ponta e telas novas | Endpoints inexistentes | **Não validado** (depende da execução da sprint) |

**Skills aplicadas:**

| Skill | Como foi usada |
|---|---|
| `springboot-security` | Checklist sobre a Fase 1; divergências registradas |
| `springboot-tdd` | 18 testes escritos e o ciclo vermelho → verde; **sem** meta de 80% e **sem** JPA |
| `311-frameworks-spring-jdbc` | `Optional` × `queryForObject`; `@Transactional`; `JdbcClient` × D4 registrado |
| `sql-code-review` | Achado A9; índices; FOR UPDATE |
| `test-scenarios` | CT30–CT50 |
| `diagnosing-bugs` | Ciclo diferencial que provou que a A13 não vem da divisão |
| `intended-vs-implemented` | **Não** aplicada como auditoria formal de código: não há código da Sprint 3 no repositório para comparar. **Recomendada** para a janela de testes (documentação × backend implementado) |
| `user-stories` | **Não** usada: mudar o texto das histórias depende da decisão A3 e do aviso ao professor |
| `render-deploy` e `render-docker` | **Não** usadas: deploy fora do escopo atual (A3) |

---

## F. Pendências e decisões necessárias

| # | Pendência | Responsável | Próxima ação | Prazo |
|---|---|---|---|---|
| A3 | Must fora da sprint e deploy | Lucas (PO) + Diogo | Mensagem ao professor com as opções | **Ter 27/10** |
| A10 | Contrato v2 | Kauê + Lucas + Leonardo | Reunião de 30 min; aplicar ao oficial | **Ter 27/10** |
| A11, P2 | Compute Server × WLS; versão do Gurobi | Kauê (+ Lucas, Diogo) | Decidir e registrar na Arquitetura | Ter 27/10 |
| BD01 | Docker sem motor nesta máquina | Diogo | Abrir o Docker Desktop, verificar a virtualização, registrar a solução | Seg 26/10 |
| A13 | Playwright instável | Leonardo | Diagnosticar (§7.1 do tutorial dele) | Sáb 31/10 |
| A15 | Branch `sprint-3` | Kauê | Fechar ou atualizar | Seg 26/10 |
| P5, P6 | WLS, tempo do solver | Kauê + Lucas | Quando a licença chegar; Fase 4A | Sex 30/10 |
| — | Executar os tutoriais e registrar evidências | Todos | Um PR por fase, no dia | Até Dom 01/11 |

---

## G. Lista priorizada para retomar o desenvolvimento

Ordem: primeiro o que **destrava os outros**.

**Lucas (caminho crítico)**
1. Copiar a Fase 1 de `referencia/backend/` e rodar os 18 testes. PR até terça.
2. **Decisão A3** com o professor (com o Diogo), terça.
3. Combinar as assinaturas dos repositórios com o Diogo, terça.
4. Fase 2 (quarta e quinta) → avisar o Leonardo.
5. Fase 3 (quinta e sexta) → Fase 4A com o Kauê (sexta).

**Diogo**
1. Destravar o Docker (BD01).
2. Patch do DDL + 41 testes + experimentos. PR **segunda/terça**.
3. Mensagem ao professor (A3, F9, P3) com o Lucas, terça.
4. Assinaturas, depois os repositórios Java (quarta e quinta).
5. `seed_demo.sql` e `sql_repositorios_sprint3.md` (quarta).
6. Documentos e CTs de banco no Plano de Testes (até sábado).

**Kauê**
1. Contrato v2 aprovado e aplicado; A11 e P2 decididas (terça).
2. Branch `sprint-3` (segunda).
3. Python v2 + 25 testes + 2ª máquina. PR quarta.
4. CTs de Python no Plano de Testes (quinta).
5. Fase 4A com o Lucas (sexta).
6. CT20, registro do Git, Review, Retro e relatório (sábado e domingo).

**Leonardo**
1. Baseline e divisão do `app.js` (segunda e terça).
2. Guarda por lista (terça).
3. Telas de Estoque e Pedido com API simulada no formato do Contrato v2 (quarta).
4. **Gate da Fase 2** (quinta).
5. Telas com o backend real (sexta); Tela 12 (sábado).
6. A13 (até sábado).

**Bloqueios que precisam ser resolvidos para os quatro avançarem juntos:**
1. **A3** (escopo e deploy);
2. **A10** (Contrato v2);
3. **Fase 1 do Lucas** na `main`: sem ela, ninguém integra;
4. **Patch do banco** na `main`: sem ele, não há Fase 3;
5. **Docker funcionando**: sem ele, não há banco local nem Testcontainers.
