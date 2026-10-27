# Roteiro — Sprint 3 — Mente Fria (v4)

> **Status:** rascunho para discussão da equipe, revisado em 14/10/2026 (v4.6), incorporando as respostas D1–D22 (13/10) e as respostas às dúvidas (14/10, seis rodadas).
> **Base:** repositório `KaueNCarneiro/Mente_Fria`, branch `main`, commit `a872561` (11/10/2026; conferi que não há commit novo), mais o texto "Modelo matemático final" enviado na conversa (ele **não** está no repositório). Itens marcados como **proposta** ainda não foram decididos; as dúvidas que sobraram estão na seção 11 (agora poucas).

## O que mudou na v4.6 (14/10, noite)

1. **A Sprint 4 passa a ser de desenvolvimento**, não mais de bugs e deploy (o deploy, US20, continua nela). **A correção de bugs fica para a janela de testes de 31/10 a 06/11**, que o professor reservou para isso: nela a equipe **localiza e corrige** os defeitos e registra tudo no Relatório de Testes Executados.
2. **Efeito:** a Sprint 4 fica **mais folgada** (sai a reserva para bugs), mas a janela de testes passa a ser a **única** etapa de correção, então a qualidade da bateria de testes decide o resultado (seção 4.4).
3. **Não há dúvidas abertas** (seção 11).

## O que mudou na v4.5 (14/10, noite)

1. **A entrega final não é 30/10.** Depois da Sprint 4 (30/10) há uma **janela de testes de 31/10 a 06/11** para as baterias de testes (**Relatório de Testes Executados**) e a validação do software. A seção 4.3 ganhou essa fase.
2. **Efeito:** o planejamento da Sprint 4 fica **menos apertado no fim**, mas o **Plano de Testes precisa estar pronto antes de 30/10** (hoje ele só cobre US1 a US10 e US12; ver Fase 8) e **o software precisa estar publicado e estável em 30/10**, porque os testes rodam nele.
3. *(Pergunta sobre corrigir ou só registrar defeitos na janela: respondida na v4.6, corrigir.)*

## O que mudou na v4.4 (14/10, início da noite)

1. **Datas:** Sprint 3 até **16/10** (extensão até **18/10**); **Sprint 4 até 30/10**. *(Corrigido na v4.5: a entrega final é depois, com testes até 06/11.)*
2. **Bugs:** a equipe confirma que **não há outros bugs pendentes além dos já mapeados** (BE03 e BD01); se surgirem, serão documentados. A pendência de consolidar a lista saiu.
3. **Sprint 4 tem cerca de 12 dias corridos** (19/10 a 30/10). Incluí um **calendário proposto** (seção 4.3) com deploy na primeira semana e congelamento de código antes dos dois últimos dias, reservados a testes finais, Review, Retrospectiva e documentação.

## O que mudou na v4.3 (14/10, fim da tarde)

1. **Corte decidido:** o que não couber até 16/10 (ou 18/10) **vai para a Sprint 4**. **Não há renegociação com o professor.** *(Na v4.6: a correção de bugs não fica na Sprint 4, e sim na janela de testes.)* A seção 2.1 mostra a divisão; a 4.2 mostra como fica a Sprint 4.
2. **Fechadas:** seletor de mês para todo o dashboard; pedido cancelado **conta** no consumo da reposição e **não** conta no faturamento nem no ranking; alertas e reposição usam o **saldo válido**; **botões de saída nas Telas 10 e 16**; devolução à linha de origem (US27b) fica para **uma futura evolução**, fora deste projeto.
3. **Continua faltando:** as datas da **Sprint 4** e da **entrega final** (seção 11). Sem elas não consigo dizer se a Sprint 4 comporta o que ela passou a carregar.

## O que mudou na v4.2 (14/10, tarde)

1. **Prazo (D1):** a Sprint 3 termina em **16/10** (pode ir até **18/10**). Hoje é 14/10. **O escopo completo não cabe**; a seção 4.1 traz uma proposta de fatiamento e a decisão que a equipe precisa tomar.
2. **Saída de estoque:** pedidos **nunca** consomem linha vencida. Na Tela 16 (e na 10), o botão "Saída" abre duas opções: **"Saída convencional"** e **"Descarte de vencidos"**. O botão "Descartar" por linha **também fica** (Fase 3).
3. **`quantidade_restante` entra** (você pediu para incluir se fosse necessário; é). Explicação simples na seção 10.1, item 1.
4. **Cancelar pedido:** **nada volta ao estoque** (decidido). Devolver à linha de origem é possível, mas exige uma tabela a mais; avaliação na Fase 3 (US27b, opcional).
5. **Dashboard:** "tipo de produto" = **nome do produto**; período = **mês**.
6. **Atenção, nova ligação entre decisões:** se o cancelamento não devolve estoque, o ingrediente foi perdido. Então o consumo da reposição deveria contar o pedido cancelado? (seção 11, item 3).

## O que mudou na v4.1 (14/10, manhã)

1. **Respostas fechadas:** consumo da reposição = pedidos × composição; mínimo = `quantidade_minima`; objetivo do solver = maximizar a menor cobertura; quantidade fracionada com **2 casas**; tabela ordenada por custo estimado de compra; Marketing com **até 3** produtos; cancelar pedido para **Administrador e Funcionário** (Could); excluir unidade fora; reposição por `POST`.
2. **Dashboard:** o ranking de vendas por usuário agora tem **colunas e filtros definidos** (Fase 6). Apareceu uma dúvida sobre "tipo de produto"; **resolvida na v4.2** (= nome do produto).
3. **Linhas de estoque: mudei minha recomendação.** Sua regra ("a saída sempre consome a linha de menor validade") é o FEFO. A "simulação" que eu havia proposto **falha** em um caso simples (linha nova com validade menor que a de uma já consumida). Agora recomendo uma **coluna `quantidade_restante`** em cada `ENTRADA` (Fase 3). É uma mudança pequena de schema para o Diogo.
4. **Pergunta nova que a regra levanta:** o pedido pode consumir linha **vencida**? **Resolvido na v4.2:** não.
5. **Datas:** respondidas na v4.2.

## O que mudou na v4 (13/10)

1. **US14 (reposição) deixou de ser uma conta em Java e passou a ser um problema de otimização com orçamento (OPEX).** Isso contradiz a decisão D5 da Arquitetura, cria um segundo módulo Python e uma tarefa nova de modelagem (Fase Q).
2. **"Lotes" não existem; o estoque é por "linhas"** (cada `ENTRADA` em `movimentacao_estoque`). Isso muda alertas, FEFO e o desconto de vencidos (seções 5, Fase 3, e 11).
3. **Dashboard (US17) = Tela 03**, sem "receita máxima estimada": o dashboard **não** chama mais o Python.
4. **Marketing (US18):** o resultado da Tela 12 (Produção) fica guardado no backend e a Tela 14 (Marketing) o lê. Recomendo a ferramenta na Fase 6; o banco de ideias de campanha está no Apêndice B.
5. **Novos itens:** `PATCH` de unidade de medida (US26), `GET /api/estoque`, `seed_demo.sql`, **cancelar pedido (US27, proposta)**.
6. **Responsabilidades:** as pendências foram separadas por pessoa, com explicação simples (seção 10).
7. **Datas (D1):** continuam desconhecidas. O que os documentos permitem inferir está na seção 4 e **é preocupante**.

## 0. O que consegui e não consegui acessar

**Li no repositório:** os três backlogs (`docs/Backlog_Priorizado_Mente_Fria.docx` e as versões de fim da Sprint 1 e do corte da Sprint 2), `README.md`, `Arquitetura.md`, `Contrato_de_Comunicacao_Mente_Fria.md`, `Plano_de_Testes_Mente_Fria.md`, `DER.md`, `Diagramas_UML.md`, `Roteiro do Protótipo Navegável`, `mapa_constraints.md`, Documento de Visão e Termo de Aceite (`.docx`), o relatório de entrega, a retrospectiva e as evidências da Sprint 2, o relatório e a ata da Sprint 1, as quatro contribuições individuais da Sprint 2, `bd/Script_DDL.sql`, `frontend/README.md`, e o código de `backend/`, `optimization/` e dos trechos de `frontend/js/app.js` ligados a menu, rotas e tela de comunicação.

**Li só em parte:** `sql_repositorios.md` (cabeçalhos e seções 5 e 6), `sprint-1-roteiro.md` (cabeçalhos e final) e `frontend/tests/browser.cjs` (só as linhas sobre a tela de comunicação).

**Não li:** as quatro contribuições individuais da Sprint 1, `bd/constraints_teste.sql`, `frontend/js/api.js` e `frontend/js/validation.js`.

**Não consegui acessar:**
- as imagens de evidência (links do Google Drive no relatório da Sprint 2) e o **protótipo no Figma** (usei o texto do Roteiro do Protótipo). Por isso **não vi** a tabela de reposição que você disse já existir no frontend: no repositório ela **não existe** (só o item "Reposição" do menu, marcado como indisponível), então presumo que esteja no Figma;
- comentários de PR e issues do GitHub (só o histórico de commits);
- o histórico anterior a 27/09, que a equipe informa ter sido perdido;
- documentos citados mas ausentes do repositório: `Pendencias_ARQUITETURA_para_o_grupo.md`, `Roteiro_Sprint1_Detalhado_PostgreSQL_v3.md`, `sprint-1-backlog.md`, `sprint-2-backlog.md` e `sprint-1-retrospectiva.md`;
- **o "arquivo do backlog priorizado" que você disse ter enviado nesta rodada:** só chegou o `.txt` com as respostas. Usei o `docs/Backlog_Priorizado_Mente_Fria.docx` do repositório (e as duas versões das Sprints 1 e 2). Nenhum dos três traz datas da Sprint 3 (os dois backlogs de sprint só datam as Sprints 1 e 2).

**Não executei:** o backend Java (sem JDK 25 e sem PostgreSQL aqui), os testes de constraints, o Playwright, nem nada com a licença WLS (só a licença restrita do `pip`). Para esses, confio no que os relatórios da equipe afirmam.

**Conferências próprias** (em pasta temporária, fora do repositório):
1. Contas do modelo final por força bruta com `Decimal` (seção 3).
2. Os 8 testes atuais do `optimization/` rodaram numa cópia, **sem licença WLS**: `8 passed` (Python 3.13, `gurobipy` 13.0.3, não 3.14/13.0.1).
3. Protótipo descartável do modelo em dois estágios com inteiros escalados contra força bruta: 2 cenários + 300 instâncias pequenas (3 produtos, 4 ingredientes), **0 divergências**. Limite: instâncias pequenas, não o cardápio real.
4. **Novo:** testei em SQLite (não em PostgreSQL) a ideia de calcular "quanto resta em cada linha" por soma acumulada de saídas: bate em 5 cenários simples, **mas falha** quando uma linha com validade menor entra depois de saídas já feitas (contraexemplo na Fase 3). Por isso a v4.1 recomenda uma coluna no banco.

---

## 1. Estado real do projeto (commit `a872561`)

| Camada | O que existe | O que falta para a Sprint 3 |
|---|---|---|
| Banco | `bd/Script_DDL.sql` com schema e seed (5 unidades, 7 ingredientes, 3 produtos, 13 composições, **0 usuários**, sem estoque em movimentações); 32 testes de constraints (relatados como aprovados) | SQL de `cliente`, `pedido`, `movimentacao_estoque`, dashboard, reposição e validade (`sql_repositorios.md` §6 diz que não existe); mapa de constraints para essas tabelas; **status do pedido** (US27) |
| Backend | Java 25, Spring Boot 4.0.8, só `GET /api/saude` e `GET /api/ingredientes` (3 campos); `pom.xml` com `jdbc`, `webmvc`, `postgresql` e testes. **`/api/teste/python` não existe no backend** (o frontend e o Contrato falam dele) | **Tudo mais:** Spring Security/JWT (nem está no `pom.xml`), CORS, tratamento de erros, endpoints de escrita, Testcontainers, chamada ao Python |
| Python | `otimizador.py` + 8 testes; maximiza **receita** (o modelo final maximiza **lucro**), usa `float`, não recebe custo, não lê credenciais WLS de variáveis de ambiente, **sem modo eco** | Reescrever para o modelo final (seção 3); ambiente Gurobi (Alternativa C); **novo módulo de reposição** (Fase Q) |
| Frontend | Telas de cadastro, login, funcionários, ingredientes, produtos/composição e preço, com sessão JWT e testes de navegador **com API simulada**; menu lista Dashboard, Porção Padrão, Estoque, Produção, Pedidos, Clientes, Reposição e Marketing como indisponíveis | 9 telas novas (seção 5) e integração com a API real; campos novos na Tela 13 (período e orçamento) |
| Documentação | Arquitetura, Contrato, DER, UML, Plano de Testes, relatórios das Sprints 1 e 2 | Contrato v2; atualizações pedidas ao Diogo (seção 10) |
| CI | Não existe (`.github/` ausente) | Opcional; ver Fase 8 |

---

## 2. Escopo

O Termo de Aceite (28/08) aceita **Must + Should**. Os itens **Could** (US13, US19, US26 e a nova US27) estão fora do Termo e podem ser cortados sem descumpri-lo. Tamanhos (P/M/G) vêm do backlog.

| US | Prioridade | Tam. | Alvo original | Estado no corte de 02/10 | O que a Sprint 3 entrega |
|---|---|---|---|---|---|
| 1, 2, 3 | Must | P, P, M | Sprint 1 | Parcial — só frontend | Backend de auth, usuários e login (Fases 1–2) |
| 4 | Must | M | Sprint 1 | Parcial | `GET` completo, `POST`, `PUT` de ingredientes |
| 5 | Must | G | Sprint 1 | Parcial | Produtos e composição no backend (regra do açaí no servidor) |
| 6, 7 | Must | P, M | Sprint 1 | Parcial | Preço (só Admin) e validação padronizada |
| 9 | Must | M | Sprint 2 | Não iniciado | Movimentações de estoque e `GET /api/estoque` (Fase 3) |
| 12 | Must | M | Sprint 2 | Não iniciado | Pedido com baixa automática (Fase 3) |
| 13 | **Could** | P | Sprint 2 | Não iniciado | Clientes (Fase 3) |
| **27 (nova)** | **Could** (confirmado) | **P/M** | — | — | **Cancelar pedido** (Administrador e Funcionário), **sem devolver estoque** (Fase 3). Devolução à linha de origem = US27b, opcional |
| 10 | Must | G | Sprint 2 | Parcial — Python | Modelo final + chamada Java + endpoint (Fases P e 4) |
| 11 | Must | M | Sprint 2 | Não iniciado | Tela 12 (Fase 4) |
| 14 | Must | G | Sprint 2 | Não iniciado | **Reposição com orçamento (OPEX), via solver** (Fases Q e 5). **Cresceu** em relação ao backlog |
| 15 | Should | G | Sprint 3 | Não iniciado | Alerta de reposição, `saldo <= mínimo` (Fase 5) |
| 16 | Should | M | Sprint 3 | Não iniciado | Alerta de validade por linha, 7 dias (Fase 5) |
| 17 | Must | G | Sprint 3 | Não iniciado | Dashboard = Tela 03 (Fase 6) |
| 18 | Must | G | Sprint 3 | Não iniciado | Sugestões de marketing a partir da Tela 12 (Fase 6) |
| 19 | **Could** | P | Sprint 3 | Não iniciado | CSV com `;` e UTF-8 com BOM (Fase 6) |
| 26 | **Could** | P | Sprint 3 | Não iniciado | Tela 19, `POST` e `PATCH /api/unidades-medida` (Fase 7) |

**Fora do escopo:** US8 (concluída), US20 (deploy, Sprint 4 — mantida como já definida), US21–US25 (`Won't`), rotas opcionais de edição de funcionário e de produto completo (Contrato §2.2, item B3), excluir unidade de medida (presumido; a FK já impede se estiver em uso — **confirmado em 14/10**).

**Peso:** 6 histórias G, 8 M e 6 P pendentes, quase todas Must, **mais** a US14 maior, o módulo Python novo e a US27. Corte por prioridade quando faltar tempo: Could (US27, US19, US26, US13), depois Should (US16, US15). **Os Must não são cortáveis** — se não couberem, a conversa é de replanejamento (D2: vocês avisam se houver decisão).

### 2.1 Divisão decidida em 14/10: Sprint 3 × Sprint 4

| Fica na **Sprint 3** (termina em **16/10**; extensão até **18/10**) | Vai para a **Sprint 4** (desenvolvimento e deploy; os bugs ficam para a janela de testes de 31/10 a 06/11) |
|---|---|
| Fase 0 (Contrato v2 de produção e estoque, `.env.example`) | Fase Q e **US14** (reposição com orçamento) |
| Fase 1 (segurança, erros, CORS com `PATCH`) | **US15** (alerta de reposição) e **US16** (alerta de validade) |
| Fase 2 (**US1–US7** reais, gate de aceite) | **US17** (dashboard) e **US19** (CSV) |
| Mudanças do banco (`quantidade_restante`, `motivo`, `status` do pedido), `seed_demo.sql`, repositórios | **US18** (marketing) |
| Fase P (Python de produção) | **US26** (porção padrão, com `PATCH`), **US13** (clientes), **US27** (cancelar pedido) |
| Até 18/10, se estender: **US9** (movimentações com linhas e `GET /api/estoque`), **US12** (pedido) e **US10/US11** (Fase 4, Tela 12) | **US20** (deploy), remoção de `/api/teste/python` (antes do deploy), Sprint Review e Retrospectiva |

**Fora do projeto (evolução futura):** devolver ao estoque os ingredientes de um pedido cancelado, na linha de origem (US27b).

**Ordem de corte dentro da Sprint 3, se faltar tempo:** primeiro sai o que está na linha "até 18/10"; depois, as partes de Fase 2 menos críticas (US6/US7 podem ficar com a validação básica). Os Must da coluna da esquerda não saem.

---

## 3. O modelo de produção e o que ele muda

**Modelo (texto completo na conversa; proposta: salvar em `docs/Apoio/Modelo_Matematico_Producao.md`):** `c_p = Σ k_i·q_ip`, `m_p = r_p − c_p`; estágio 1 maximiza `Σ m_p·x_p` com `Σ q_ip·x_p ≤ S_i` e `x_p` inteiro ≥ 0 (teto `u_p`; `x_p = 0` se `m_p ≤ 0`); estágio 2 é só desempate técnico (mais copos do menor id, mantendo `Σ m_p·x_p ≥ L*`). Saída: `margem_estimada = L*`, `receita_estimada = Σ r_p·x_p`, `consumo_i`.

**Conferência por força bruta** (custos do seed: 18,50 / 12,00 / 22,00 / 5,50 / 0,35):

| Cenário | Resultado |
|---|---|
| Exemplo do Contrato (granola 0) | custo 6,26 e 9,83; lucro 6,64 e 9,07; plano `(66, 0)`; `L* = 438,24`; receita 851,40 |
| Exemplo didático (granola **assumida em 12**; o texto do modelo não informa) | plano `(64, 46)`; `L* = 842,18`; receita 1.695,00; relaxação linear ≈ 845,78; `(66, 44)` dá 837,32, R$ 4,86 pior |

**O que precisa mudar** (verificado nos arquivos):

| Onde | Hoje | Passa a ser |
|---|---|---|
| `optimization/app/otimizador.py` | `maximizar Σ preco·x` em `float`; ingredientes só com `id` e `quantidade_estoque` | Lucro, dois estágios, inteiros escalados, `custo_unitario` obrigatório, `margem_estimada` e `consumo` na saída; ramo de eco (D6) |
| `optimization/tests/test_otimizador.py` | 8 testes sem custo; `test_exemplo_do_contrato` compara o dicionário **inteiro** | Todos de sucesso ganham `custo_unitario`; o do contrato passa a esperar `margem_estimada: 438.24` |
| Contrato §1.1, §1.2, §1.5 | Sem custo na entrada e sem margem na saída | Contrato v2 (Fase 0; explicação em 10.2) |
| `Arquitetura.md` §3.3 e **D5** | "O modelo… será detalhado no início da Sprint 2"; **D5: reposição em Java** | Apontar para o documento do modelo; **D5 passa a valer só para o que não usa solver** (reposição agora usa) |
| `README.md` linha 3; Visão (objetivo do MVP, R3); Termo (item 5 do escopo e critério de pronto); Backlog (US10, US14, US17) | "maximizar a receita"; US14 "consumo de um período X e preço atual"; US17 "receita máxima estimada" | Diogo atualiza (D4, seção 10.1) |
| `Plano_de_Testes` CT23 e CT24 | CT24 espera produto 1 = 10, produto 2 = 0 | **Continua válido** se o custo do ingrediente for menor que R$ 5,00 por unidade; acrescentar casos novos |
| `Plano_de_Testes` CT29 | "lote vencido não conta como estoque" | Reescrever para "linha vencida" (seção 5, Fase 3) |
| `Roteiro do Protótipo` Tela 12 | "receita estimada" | Acrescentar lucro potencial |

---

## 4. Premissas, datas e ordem de execução

1. **Método walking skeleton:** Sprint 3 = lógica de negócio, mas ela **absorve todo o CRUD** que as Sprints 1 e 2 não entregaram (decisão D2 confirmada; na v4.6, a Sprint 4 é de **desenvolvimento e deploy**, e os bugs ficam para a janela de testes de 31/10 a 06/11).
2. **A recomendação é uma sugestão:** a interface diz que o lucro é **potencial**.
3. **Trilha Python paralela:** o módulo é isolado (JSON por `stdin`/`stdout`, sem banco, sem Java) e anda desde o primeiro dia.
4. **Regras de processo (ata da Sprint 1 e README):** branches `feature/...`, PR revisado por outro integrante, ninguém altera a estrutura base sem avisar, problema mostrado ao grupo no mesmo dia, commits pequenos (Conventional Commits).
5. **Datas (D1) — respondida em 14/10:** a Sprint 3 **termina em 16/10 (sexta)**, com possibilidade de estender até **18/10 (domingo)**. **Continuam desconhecidas** as datas da Sprint 4 e da entrega final. Pelo que está no repositório, esse prazo não comporta o escopo completo: ver 4.1.

```
Trilha Python (paralela, desde a Fase 0)
  Fase P  Módulo de produção com o modelo final
  Fase Q  Módulo de reposição (modelo a formalizar ANTES do código)

Trilha backend / frontend
  Fase 0  Preparação (Contrato v2, documentos, .env.example)
  Fase 1  Fundação do backend (segurança, erros, CORS com PATCH)
  Fase 2  CRUD real US1–US7 ──► gate de aceite com frontend real
  Fase 3  Estoque por linhas, pedidos, cancelamento, clientes (US9, US12, US13, US27)
  Fase 7  Porção padrão (US26)            [em paralelo, depende só da Fase 1]

Convergência
  Fase 4  Produção: Java + Tela 12 (US10, US11)                 [Fase P + Fase 2; linhas vencidas precisam da Fase 3]
  Fase 5  Reposição e alertas (US14, US15, US16)                [Fases Q e 3]
  Fase 6  Dashboard, marketing e CSV (US17, US18, US19)         [Fases 3 e 4]
  Fase 8  Qualidade e encerramento (contínua)
```

**Regra de gate:** uma fase só está pronta quando o frontend real conversa com o backend real daquela fase (nada de aceite com respostas simuladas), com evidência registrada na hora.

### 4.1 Cabe no prazo? (resolvido em 14/10: o excedente vai para a Sprint 4, ver 2.1 e 4.2)

**Minha avaliação: o escopo da seção 2 não cabe em 2 a 4 dias.** Não consigo medir a capacidade da equipe; baseio-me no que está no repositório:
- no `main` há só `GET /api/saude` e `GET /api/ingredientes` (3 campos); **não há** Spring Security, JWT, CORS nem endpoint de escrita;
- a Sprint 2 (duas semanas) entregou o banco, a ligação Java↔banco, o módulo Python e o frontend contra API simulada;
- pelo backlog, a Sprint 3 herda **todo o CRUD** (US1–US7), mais estoque, pedidos, produção, reposição com solver novo, alertas, dashboard, marketing e ~9 telas.

**Proposta de fatiamento por dependência** (as quatro pessoas em paralelo; ordem do que destrava o resto):

| Faixa | O que entra | Quem puxa |
|---|---|---|
| **Até 16/10** (núcleo) | Fase 1 (segurança, erros, CORS com `PATCH`); Fase 2 (US1–US7 reais); **mudanças do banco** (`quantidade_restante`, `motivo`, `status` do pedido); Fase P completa (Python) e Contrato v2 de **produção e estoque**; frontend integrado à Fase 2 e `app.js` dividido | Lucas (1, 2), Diogo (banco + repositórios), Kauê (P + contrato), Leonardo (integração) |
| **Até 18/10** (se estender) | Fase 3 mínima: `POST /api/estoque/movimentacoes` com linhas, `GET /api/estoque` e `POST /api/pedidos` (US9, US12); Fase 4 (US10, US11) com o dublê ou o Python real | Lucas + Diogo; Kauê + Lucas (4A); Leonardo (Telas 10, 11, 12, 16) |
| **Não cabe (proposta: mover)** | Fase Q e **US14**; US15, US16; **US17**; **US18**; US19, US26, US13, US27 | — |

**Decisão da equipe (14/10):** as histórias que não cabem **vão para a Sprint 4**; não há renegociação. Entre elas há **Must** (US14, US17, US18) e **Should** (US15, US16), todas dentro do Termo de Aceite. A consequência é a seção 4.2.

**Custo relativo das que ficariam de fora** (para escolher o que puxar primeiro, se sobrar tempo): US18 e US15 são baratas depois da Fase 4 e da Fase 3 (um componente em memória e um catálogo; uma consulta); US16 depende das linhas; US17 é SQL puro mas com tela grande; **US14 é a mais cara** (modelo novo, Fase Q). Se a US14 ficar apertada, uma **versão provisória em Java sem solver** (distribuir o orçamento proporcionalmente à necessidade) cumpriria a tela e o contrato enquanto o solver não chega; é uma opção, não uma recomendação, pois vocês decidiram pelo solver.

### 4.2 Como fica a Sprint 4 (desenvolvimento e deploy; bugs ficam para a janela de testes)

A Sprint 4 passa a carregar: **US14, US15, US16, US17, US18** (Must e Should) + **US19, US26, US13, US27** (Could) + **US20** (deploy, Must) + remoção do `/api/teste/python` + Sprint Review e Retrospectiva. **A Sprint 4 termina em 30/10** (informado em 14/10), o que dá cerca de 12 dias corridos a partir de 19/10. É apertado, mas **plausível se o núcleo da Sprint 3 estiver pronto** e se o deploy acontecer cedo; não consigo medir a capacidade da equipe, só apontar onde o plano é frágil (seção 7).

**Recomendações para a Sprint 4 (para a equipe avaliar):**
1. **Fazer um primeiro deploy cedo** (no início da Sprint 4, ou já no fim da Sprint 3 se sobrar tempo), e não no fim. Os riscos de RAM (512 MB com Java + Python + `gurobipy`), tempo de resposta do solver e licença WLS só aparecem no Render. Deploy no último dia, com Must pendente, é o pior cenário.
2. **Ordem sugerida das histórias** (menor custo e menos dependência primeiro): US18 (um componente em memória e um catálogo) → US15 (uma consulta) → US16 (depende das linhas, já prontas) → US17 (SQL e tela grande) → US14 (a mais cara). As Could por último, na ordem US26, US19, US13, US27.
3. **A trilha Python não depende do Java.** Depois da Fase P, o Kauê pode **modelar a Fase Q no papel ainda na Sprint 3** e até implementar o `reposicao.py` com testes, sem tocar no resto; assim a US14 chega à Sprint 4 com o módulo pronto e só falta a ligação Java e a tela. É opcional e só se sobrar tempo na Sprint 3.
4. **Bugs:** a equipe confirmou em 14/10 que não há outros além dos já documentados, e que a correção fica para a **janela de testes**. Dos dois conhecidos, BE03 (resposta `500` com *Whitelabel Error Page* em `/api/ingredientes`) é tratado pela Fase 1 ainda na Sprint 3, e BD01 (Docker sem virtualização) é problema de ambiente. A Sprint 4 **não reserva tempo para bugs**; só os que bloquearem uma história em desenvolvimento.
5. **Teto de escopo:** se a Sprint 4 apertar, cortar nesta ordem: US27, US13, US19, US26, depois US16 e US15. Os Must (US14, US17, US18, US20) não saem.

### 4.3 Calendário proposto (hoje é quarta, 14/10)

| Período | Foco | Observação |
|---|---|---|
| **14/10 a 16/10** (extensão até **18/10**) | **Sprint 3:** Fases 0, 1, 2, P e mudanças do banco; com extensão, US9, US12 e Fase 4 | Ver 2.1. Decidir na **quinta à noite** se a extensão será usada |
| **19/10 a 20/10** | Absorver o que escorregou da Sprint 3; **primeiro deploy** (Render + GitHub Pages + banco) com o que existir; medir RAM, tempo do Python e licença WLS; US18 e US15 | O deploy cedo revela o que só aparece no Render |
| **21/10 a 23/10** | US16, US17 (backend e Tela 03); Fase Q (modelo e `reposicao.py`, se não adiantados); Telas 13 e 14 | Leonardo e Lucas em paralelo |
| **26/10 a 28/10** | US14 (ligação Java e Tela 13); Could na sobra (US26, US19, US13, US27); testes e evidências a cada história | **Congelar código na noite de 28/10** |
| **29/10 a 30/10** | Estabilizar o deploy; teste ponta a ponta no ambiente publicado; **Sprint Review e Retrospectiva**; relatório de entrega, evidências, ata, relatórios individuais e backlog final da Sprint 4 | A Sprint 2 terminou com vários `[PREENCHER]`/`[CONFIRMAR]`; os documentos pedem tempo. **O sistema precisa estar publicado e estável em 30/10** |
| **31/10 a 06/11** | **Janela de testes e correção de bugs:** baterias de testes sobre o sistema publicado, **localizar e corrigir** os defeitos, **Relatório de Testes Executados** e validação contra o Termo de Aceite | Ver 4.4 |

Se a Sprint 3 consumir a extensão até 18/10, as duas primeiras linhas da Sprint 4 se comprimem; o que sobra a cortar está em 4.2, item 5.

### 4.4 Janela de testes (31/10 a 06/11)

A equipe informou que depois da Sprint 4 há uma semana para executar as baterias de testes (**Relatório de Testes Executados**) e validar se o software está de acordo. O que isso exige **antes** de 30/10:
1. **Plano de Testes completo.** O plano atual cobre US1 a US10 e US12 (CT01–CT29). Faltam os casos de **US11, US13 a US19, US20 (deploy), US26 e US27**, os do modelo novo (lucro, desempate, reposição com orçamento), os das **linhas** (restante, vencida, descarte, pedido sem vencido) e o ajuste de CT23, CT24 e CT29. Proposta: Diogo escreve os casos da parte de banco e estoque; Kauê, os do Python e da otimização; Lucas e Leonardo, os de API e telas; cada história concluída já entrega seus casos.
2. **Sistema publicado e estável** em 30/10 (US20), com um **conjunto de dados de teste conhecido** (o `seed_demo.sql`), para que os resultados esperados possam ser calculados à mão e o relatório seja reproduzível.
3. **Os testes que já são automáticos** (Python, `MockMvc`, Testcontainers, Playwright) devem rodar numa máquina que não é a de quem os escreveu, e o relatório deve citar a saída deles, não só prints.
4. **Critérios do Termo de Aceite** como checklist de validação: login por perfil; cadastro → estoque → recomendação/propaganda → pedido → baixa automática; otimização funcionando; dashboard com dados reais; alertas e recomendações; URL pública; README que permita rodar do zero.
5. **Defeitos achados (decidido em 14/10): localizar e corrigir nesta janela.** Fluxo proposto para cada defeito: registrar (onde, passos, esperado × obtido) → escrever o caso de teste que o reproduz → corrigir em branch `fix/...` com PR revisado → repetir o teste → registrar no relatório o estado **antes e depois**. **Regra de bolso:** só entra correção; funcionalidade nova ou história inacabada não entra na janela (se algo da Sprint 4 ficar incompleto, o relatório o declara como pendência conhecida). Como é a única etapa de correção, convém **dividir a bateria por pessoa e por perfil** (Administrador e Funcionário) e **priorizar os caminhos do Termo de Aceite**, para os defeitos graves aparecerem nos primeiros dias, e não no último.

---

## 5. Detalhamento por fase

### Fase 0 — Preparação

- Sprint Planning: confirmar escopo, corte e divisão (seções 2 e 10).
- **Contrato v2 (Kauê, com Lucas e Leonardo), antes de qualquer código.** Hoje só há exemplos de request/response para login, registro, unidades (GET), ingredientes (POST), produtos (POST), preço, saúde e pedido (POST). **Faltam** as respostas de produção, reposição (agora com `periodo_dias` e `orcamento`), alertas, dashboard, marketing, `GET /api/estoque`, movimentações (com `motivo` nas saídas) e descarte por linha, clientes, `GET /api/pedidos`, cancelamento, `POST`/`PATCH /api/unidades-medida`, e as mudanças da seção 3. Pontos de decisão do Kauê: seção 10.2.
- **Atualizar os documentos** da seção 3 e da seção 10.1 (Diogo).
- Criar o `.env.example` (o README manda copiá-lo, mas ele **não existe**) com `DB_*`, `JWT_SECRET`, `CORS_ALLOWED_ORIGIN`, `PYTHON_CMD`, `OTIMIZADOR_SCRIPT`, `REPOSICAO_SCRIPT` (se houver) e as variáveis do Gurobi.
- Conferir o carregamento do `.env`: `application.properties` usa `spring.config.import=optional:file:./backend/.env[.properties]` (relativo a **onde o Java é iniciado**), enquanto o README manda rodar dentro de `/backend`; nesse caso resolveria para `backend/backend/.env`. Não executei o backend: **verificar**.
- **Proposta:** fazer a retrospectiva da Sprint 2 (não houve segundo o relatório; ver 10.1, item sobre D21).
- Atualizar o board: US1–US7 "parcial", US10 "parcial (módulo com modelo antigo)", US14 "redesenhada", US27 "nova".

### Fase P — Módulo Python de produção (trilha paralela)

Responsável proposto: Kauê. Arquivos: `optimization/app/otimizador.py`, `optimization/tests/test_otimizador.py`.

1. **Registrar o modelo:** `docs/Apoio/Modelo_Matematico_Producao.md`, com os dois cenários conferidos da seção 3.
2. **Oráculo de teste independente** (força bruta com `Decimal`, só nos testes): o resultado esperado não pode ser calculado pelo próprio otimizador (princípio do Plano de Testes §1).
3. **Entrada:** ler o JSON com `Decimal` (`json.load(..., parse_float=Decimal)`). Validar o domínio (`S_i ≥ 0`, `k_i ≥ 0`, `r_p > 0`, `q_ip > 0`; até 3 casas em quantidades e 2 em preços e custos; todo produto com ao menos um ingrediente; todo ingrediente com custo). Fora do domínio → `status: "erro"`. Manter as validações atuais.
4. **Pré-processamento:** `c_p`, `m_p`, teto `u_p`, `x_p = 0` quando `m_p ≤ 0`, escala para inteiros.
5. **Dois estágios:** estágio 1 obtém `L*`; depois, para cada produto em ordem de id, maximizar `x_p` com `Σ m_p·x_p ≥ L*` e os produtos já decididos fixados. É a lógica do protótipo.
6. **Verificação pós-solve em aritmética inteira** (plano respeita todos os estoques; lucro recalculado = `L*`). Divergência → falha técnica (código de saída ≠ 0), nunca um plano silenciosamente errado (D8, explicação em 10.2).
7. **Saída:** `status`, `receita_estimada`, `margem_estimada`, `recomendacao` (um item por produto recebido, inclusive 0) e `consumo` (recomendado; ver 10.2, D5b).
8. **Ramo de eco (D6):** se a entrada tiver a chave `echo` (e não tiver `produtos`), devolver `{"status":"sucesso","echo": <texto em maiúsculas>}` como no Contrato §1.5, antes das demais validações. São poucas linhas e permite validar a ponte Java↔Python agora, sem o modelo. **Remover quando o Hello World estiver validado** (ver Fase 4A, item 0).
9. **Ambiente Gurobi (Alternativa C):** ler `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID` do ambiente e entregá-las ao Gurobi ao criar o `Env` (o caminho exato de parâmetros deve ser conferido na documentação da versão instalada — não testei com WLS), mantendo `OutputFlag 0` (o banner de licença já quebrou o contrato na Sprint 2) e `stdout` só com o JSON. **Outra pessoa precisa rodar os testes** (hoje só o PC do Kauê reproduz com WLS).
10. **`requirements.txt`:** UTF-16 (o `pip` lê normalmente) e `gurobipy==13.0.3`, contra 13.0.1 na Arquitetura e no README (Diogo, 10.1).
11. **Testes:** atualizar os 8 existentes e acrescentar: exemplo do Contrato com `margem_estimada = 438,24`; exemplo didático `(64, 46)`, `L* = 842,18` (e **não** `(66, 44)`); maior receita ≠ maior lucro; `m_p ≤ 0` (inclusive 0) recebe 0; desempate determinístico; estoque zerado → `sucesso` com zeros; `custo_unitario` ausente/negativo, preço ≤ 0, quantidade ≤ 0 e casas a mais → `erro`; propriedade com instâncias aleatórias contra o oráculo (semente fixa); `stdout` só JSON em UTF-8; eco.
- **Pronto quando:** `python -m pytest optimization/tests` passa em pelo menos duas máquinas e Contrato v2 e documento do modelo estão no repositório.

### Fase Q — Módulo Python de reposição (US14) — **Sprint 4** (modelagem e `reposicao.py` podem começar antes, ver 4.2, item 3) — **começa pela modelagem**

Responsável proposto: Kauê. **Antes de escrever código, formalizar o modelo no papel** (como foi feito com o de produção) (as decisões de 14/10 já estão incorporadas abaixo). Você descreveu assim (resumo fiel):

- O usuário informa **o período em dias** e **o orçamento máximo (OPEX)** na Tela 13.
- Consumo do período = **pedidos × composição, só de pedidos ativos** (decisão de 14/10); para período **≤ 30 dias** entram os **8** ingredientes mais consumidos; **> 30 dias**, os **15** mais consumidos.
- O solver calcula a quantidade a comprar de cada um, considerando consumo estimado, estoque atual, custo unitário e quantidades mínimas, **sem ultrapassar o orçamento**.
- A tabela mostra, por ingrediente, **quantidade sugerida e custo estimado**, mais o **custo total**; se o orçamento **não cobrir os mínimos de todos**, o sistema **sinaliza**.

**O que isso significa tecnicamente (decisões de 14/10 marcadas):**

- É um **segundo modelo**, independente do de produção; recomendo um script separado (`optimization/app/reposicao.py`) com contrato próprio (§1.6 do Contrato v2) em vez de misturar com o `otimizador.py`, para não reabrir o contrato que já está pronto.
- **Esqueleto proposto** (para o Kauê validar): para cada ingrediente selecionado `i`, `c_i` = consumo no período, `S_i` = saldo atual, `qmin_i` = `quantidade_minima`, `k_i` = custo unitário.
  - **mínimo a comprar:** `min_i = max(0, qmin_i − S_i)` (levar o estoque pelo menos ao mínimo);
  - **necessidade:** `nec_i = max(0, c_i + qmin_i − S_i)` (repetir o consumo do período e manter o mínimo);
  - **restrições:** `min_i ≤ x_i ≤ nec_i` e `Σ k_i·x_i ≤ OPEX`;
  - **orçamento insuficiente** quando `Σ k_i·min_i > OPEX` → o módulo devolve `orcamento_suficiente: false` e, para essa situação, a lista com os mínimos (proposta) para o usuário ver quanto falta;
  - **objetivo (decidido em 14/10):** quando sobra orçamento além dos mínimos, maximizar a **menor cobertura** `x_i / nec_i` entre os ingredientes (reparte a sobra de forma justa); desempate técnico pelo ingrediente mais consumido (a formalizar, como o estágio 2 da produção);
  - **quantidades (decidido):** fracionadas, **até 2 casas decimais**. Modelar com variáveis inteiras em centésimos (`x_i` em múltiplos de 0,01), como o modelo de produção faz com milésimos e centavos, para conferir tudo em aritmética inteira. Atenção: as quantidades do banco têm 3 casas; definir o arredondamento de `min_i` e `nec_i` para 2 casas (proposta: **para cima**, para nunca ficar abaixo do mínimo) e conferir que o arredondamento não estoura o orçamento sem sinalizar;
- **Ordem da tabela (decidido em 14/10):** do maior para o menor **custo estimado de compra** (`k_i·x_i`).
- **Entrada do Java (proposta):** `orcamento`, e por ingrediente selecionado: `id`, `nome`, `consumo_periodo`, `quantidade_estoque`, `quantidade_minima`, `custo_unitario`. O estoque enviado é o **saldo válido** (sem o vencido; mesma regra da produção; decidido em 14/10). **Saída:** por ingrediente `quantidade_sugerida` e `custo_estimado`; `custo_total`; `orcamento_suficiente`.
- Depende do mesmo ambiente Gurobi (licença acadêmica: **1 job por vez**; duas recomendações simultâneas se enfileiram ou falham — documentar).
- **Testes:** oráculo por força bruta em instâncias pequenas (como em P); orçamento exatamente igual à soma dos mínimos; orçamento 0; um ingrediente sem consumo; período 30 vs 31 dias (8 vs 15); empate de consumo.
- **Pronto quando:** modelo documentado e conferido por força bruta, testes passando em duas máquinas.

### Fase 1 — Fundação do backend

Responsável proposto: Lucas. Estrutura atual: pacotes por domínio (`ingrediente/`, `saude/`), com `Controller` e `Repository`; seguir o padrão.

- `pom.xml`: Spring Security, biblioteca de JWT (decisão de 25/09; ex.: `jjwt`), validação e, para os testes, Testcontainers (PostgreSQL 16) e `spring-security-test`.
- `BCrypt` para `senha_hash`; `JWT_SECRET` por variável de ambiente; token de 1 a 2 horas.
- `CorsConfig` lendo `CORS_ALLOWED_ORIGIN`; métodos `GET/POST/PUT/DELETE/OPTIONS` **e `PATCH`** (novo, por causa do D16: sem isso o navegador bloqueia a chamada no preflight); headers `Authorization` e `Content-Type`. Atualizar o Contrato §2 e o `frontend/README.md`.
- `@RestControllerAdvice` no formato `{ "mensagem", "campos" }`, traduzindo `DataIntegrityViolationException` pela constraint (SQLSTATE 23502, 23503, 23505, 23514) conforme `mapa_constraints.md`. **Nenhuma resposta em HTML** (a captura BE03 da Sprint 2 mostrou a *Whitelabel Error Page*) e sem *stack trace*. `401` e `403` também em JSON.
- **Pronto quando:** `GET /api/saude` público; qualquer outra rota sem token → `401` JSON; Funcionário em rota de Administrador → `403` JSON; preflight CORS correto (inclusive `PATCH`).
- **Testes:** `MockMvc` para CT05, CT08 e CT18.

### Fase 2 — CRUD real (US1–US7)

Responsáveis propostos: Lucas (endpoints), Diogo (SQL e constraints), Leonardo (integração e aceite). **O SQL dos 18 métodos de repositório da Sprint 1 já está em `docs/Apoio/sql_repositorios.md`** (com a regra do açaí e 15 casos de teste); falta transformá-lo em código Java (Diogo, conforme D3).

| Rota | Perfil | CTs |
|---|---|---|
| `POST /api/auth/registro` (409 se já existe Administrador) | Público | CT01, CT01B, CT02, CT03 |
| `POST /api/auth/login` (inativo = mesma resposta de credencial inválida) | Público | CT06, CT07 |
| `POST /api/usuarios`, `GET /api/usuarios` | Admin | CT04 |
| `GET /api/unidades-medida` | Admin | — |
| `GET /api/ingredientes` **completo** (`id`, `nome`, `unidade_medida`, `custo_unitario`, `porcao_padrao`, `quantidade_estoque`, `quantidade_minima`), `POST`, `PUT /{id}` | Leitura: Admin/Funcionário; escrita: Admin | CT09–CT11 |
| `GET /api/produtos` com `composicao`, `POST` (transação produto + composição; regra do açaí), `PUT /{id}/preco` | idem | CT12–CT17, CT05 |

Pontos de atenção: e-mail normalizado; validação da aplicação antes do banco (senha ≥ 8, açaí na composição, composição vazia, item repetido; a revisão do Diogo registra que a contagem usada para "ingrediente repetido" no tutorial do backend devolveria a mensagem errada); `perfil` e `ativo` nunca vêm do corpo; `senha_hash` nunca sai; formato exatamente como o Contrato.

**Gate de aceite:** a lista "Para aceite real" de `frontend/README.md`.

### Fase 3 — Estoque por linhas, pedidos, cancelamento e clientes (US9, US12, US13, US27) — **US9 e US12 na Sprint 3 (até 18/10); US13 e US27 na Sprint 4**

Responsáveis propostos: Lucas (endpoints), Diogo (SQL, constraints, seed), Leonardo (Telas 10, 11, 16, 17 e, se entrarem, 18 e 18B).

**Regra do estoque (decisão D10, 13/10): não há lotes; o estoque é por linhas.** Cada `ENTRADA` em `movimentacao_estoque` é uma linha ligada ao ingrediente (`ingrediente_id`), com quantidade e `data_validade`. O saldo do ingrediente continua único em `ingrediente.quantidade_estoque`.

**Regras definidas em 14/10 (todas decididas):**
- toda `SAIDA` consome **sempre a linha com a menor `data_validade`** (FEFO, regra de negócio da equipe);
- **pedido e "Saída convencional" nunca consomem linha vencida** (o produto será consumido por pessoas); só o **descarte** mexe em linha vencida;
- o saldo vencido é **descontado da recomendação de produção** até o dono fazer o descarte (CT29, mantido, agora por linha);
- "vencida" = `data_validade < data do servidor`; **vence hoje ainda vale** (proposta, ver alertas).

**Por que a coluna `quantidade_restante` (incluída):** o banco só sabe quanto **entrou** em cada linha, não quanto **sobrou**. Com a regra acima o sistema precisa, a cada saída, saber de qual linha tirar, e para os alertas e o desconto de vencidos precisa saber quanto resta em cada uma. A alternativa de calcular o restante por soma acumulada de saídas (a "simulação" que propus antes) **erra** em caso simples: entra A (vence 20/10, 20 l), saem 10 l da A, depois entra C (vence 15/10, 20 l): a realidade é A = 10 e C = 20; a simulação diz o contrário. Explicação com exemplo na seção 10.1, item 1.

**Mudanças no banco (Diogo):**
- `movimentacao_estoque.quantidade_restante DECIMAL(10,3) NULL`: preenchida só nas `ENTRADA` (começa igual a `quantidade`), com `CHECK (quantidade_restante IS NULL OR quantidade_restante BETWEEN 0 AND quantidade)`.
- `movimentacao_estoque.motivo VARCHAR(20) NULL`: nas `SAIDA` vale `PEDIDO`, `CONVENCIONAL` ou `DESCARTE_VENCIDO`; nas `ENTRADA` fica `NULL` (`CHECK` amarrando `tipo` e `motivo`).
- **Invariante a testar sempre:** `ingrediente.quantidade_estoque` = soma de `quantidade_restante` das linhas do ingrediente. Linhas sem `data_validade` contam como válidas e entram por último na ordem.
- **Disponível para pedido / produção / alertas** = soma de `quantidade_restante` das linhas **não vencidas** (válidas). O saldo total (`quantidade_estoque`) inclui o vencido até o descarte.

**Como a baixa funciona (mesma transação, `FOR UPDATE` nas linhas):**
- *Pedido* (`motivo = PEDIDO`) e *saída convencional*: percorre as linhas **válidas** com `quantidade_restante > 0`, por `data_validade NULLS LAST, id`, descontando até cobrir a quantidade; se o total válido não bastar → `422` (no pedido, CT25, o pedido inteiro é rejeitado).
- *Descarte de vencidos* (`motivo = DESCARTE_VENCIDO`): percorre só as linhas **vencidas**, da mais antiga para a mais nova; quantidade maior que o saldo vencido → `422`.

**Entregas:**
- **Diogo antes do código:** estender `mapa_constraints.md` e `bd/constraints_teste.sql` para `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque`; SQL dos repositórios novos; **`bd/seed_demo.sql`** (D19, ver 10.1).
- `POST /api/estoque/movimentacoes` (os dois perfis): `ENTRADA` com `data_validade` opcional (grava `quantidade_restante = quantidade`; `motivo` não é aceito); `SAIDA` sem validade (enviada → `400`) **com `motivo` obrigatório** — `CONVENCIONAL` ou `DESCARTE_VENCIDO` (o `PEDIDO` só o backend grava) — e as regras da baixa acima; `quantidade_estoque` atualizado **na mesma transação**; saída maior que o saldo aplicável rejeitada sem alterar nada (CT21).
- **Descarte de linha vencida, dois caminhos (decidido em 14/10):** (1) **manual**, pela Tela 16 (e 10): ao clicar em "Saída", aparecem os botões **"Saída convencional"** e **"Descarte de vencidos"**, e o backend recebe o `motivo`; (2) **atalho no alerta da linha**: `POST /api/estoque/linhas/{id}/descarte` (os dois perfis), que zera o `quantidade_restante` daquela linha, grava uma `SAIDA` com `motivo = DESCARTE_VENCIDO` e abate o saldo. Só aceita linha vencida (`409` caso contrário).
- **`GET /api/estoque` (D18, novo; Admin/Funcionário):** por ingrediente, `ingrediente_id`, `nome`, `unidade_medida`, `quantidade_estoque`, `quantidade_minima`, `quantidade_valida`, `quantidade_vencida`, `validade_mais_proxima` (entre as linhas válidas com `quantidade_restante > 0`) e `situacao`. O frontend usa `quantidade_vencida` para mostrar ou esconder "Descarte de vencidos". Para o "consultar outras datas" da Tela 10, **proposta:** `GET /api/estoque/{ingrediente_id}/linhas` devolvendo cada `ENTRADA` com `data_movimentacao`, `quantidade`, `quantidade_restante`, `data_validade` e `situacao` (`VENCIDA`, `PROXIMA`, `OK`, `SEM_VALIDADE`). Formato a fechar no Contrato v2.
- `POST /api/pedidos` e `GET /api/pedidos`: uma transação para `pedido`, `pedido_produto`, baixa pela composição, linha a linha por menor validade **só entre as válidas**, e movimentações `SAIDA` com `motivo = PEDIDO` (CT25: um item inviável rejeita o pedido inteiro); `itens` sem `produto_id` repetido; só produtos ativos; `preco_unitario_registrado` e `valor_total` no backend (`BigDecimal`); `422` com "Estoque insuficiente para o produto …"; **concorrência** (proposta): `SELECT … FOR UPDATE` nas linhas de `ingrediente`.
- `GET/POST /api/clientes` (US13, Could).
- **Testes:** CT21, CT22, CT25–CT28; Testcontainers para a atomicidade do pedido; casos novos das linhas: pedido que só teria vencido → `422`; descarte maior que o vencido → `422`; invariante (`quantidade_estoque` = soma dos restantes), baixa que atravessa duas linhas, o contraexemplo da linha nova com validade menor, linha vencida e próxima, descarte.

**US27 — Cancelar pedido (Could; decisões de 14/10: Administrador e Funcionário; nada volta ao estoque)**

*Avaliação da versão decidida:* **pequena (P/M).** Sem devolver estoque, cancelar é só mudar o status do pedido, com cuidado nas consultas.

- **Banco (Diogo):** `pedido.status VARCHAR(10) NOT NULL DEFAULT 'ATIVO'` com `CHECK (status IN ('ATIVO','CANCELADO'))`, `data_cancelamento TIMESTAMP NULL` e `usuario_cancelamento_id INT NULL` (FK), com `CHECK` de que `CANCELADO` exige `data_cancelamento` e usuário. Entra no `Script_DDL.sql` e no DER.
- **Rota:** `POST /api/pedidos/{id}/cancelamento` (Administrador e Funcionário) → `200` com o pedido; `404` inexistente; `409` já cancelado.
- **Efeitos:** `GET /api/pedidos` traz `status`; **dashboard (faturamento, mais vendidos, ranking por usuário) só conta pedidos `ATIVO`**; a lista de pedidos ganha o botão "Cancelar" com confirmação e a marca "Cancelado". O estoque **não muda**: as saídas do pedido continuam registradas (o ingrediente foi perdido).
- **Ligação com a reposição (decidido em 14/10):** como o ingrediente foi gasto mesmo com o pedido cancelado, o consumo usado na reposição deveria contá-lo; o faturamento, não. Decidido em 14/10: pedidos cancelados **contam no consumo** da reposição e **não contam** no faturamento nem no ranking.
- **Testes:** cancelar muda o status e grava quem cancelou; cancelar duas vezes → `409`; cancelado sai do faturamento e do ranking; saldo e linhas **inalterados**; os dois perfis conseguem.
- **Documentos (Diogo):** DER, Contrato, Plano de Testes, UML (caso de uso novo), Backlog (US27, **fora do Termo**: Could).

**US27b (fora do projeto, evolução futura, decidido em 14/10) — devolver os ingredientes à linha de origem. Dá para fazer; é a parte mais trabalhosa do cancelamento, mas é limitada.** O problema: uma `SAIDA` hoje não diz de quais linhas tirou. Para devolver é preciso **guardar isso na hora da baixa**:
- **tabela nova** `movimentacao_consumo (saida_id, entrada_id, quantidade)`: cada baixa (de pedido ou manual) grava uma linha por entrada que consumiu; é a mesma transação da baixa, sobre código que já será escrito;
- **cancelar** passa a: ler as linhas de consumo das `SAIDA` do pedido, somar de volta em `quantidade_restante` de cada linha de origem e em `ingrediente.quantidade_estoque`, e registrar a devolução (proposta: novo `tipo = 'ESTORNO'`, que obriga a alterar o `CHECK` de tipo);
- **casos de borda a decidir:** (i) a linha de origem **já venceu**: o ingrediente devolvido cai numa linha vencida e aparece para descarte (coerente com a regra); (ii) a linha de origem **já foi descartada**: o descarte zerou o restante, então a devolução o reabre — ou se bloqueia a devolução dessa parte;
- **custo:** +1 tabela, +1 tipo de movimento, mudança em toda baixa, testes das bordas. Eu estimo **M a G** no total para o cancelamento com devolução, contra P/M sem devolução. **Decisão:** versão sem devolução; a US27b fica para uma futura evolução. **Lembrete:** se um dia for feita, a tabela `movimentacao_consumo` deveria ter nascido junto com a baixa FEFO, porque acrescentá-la depois obriga a refazer a baixa; a equipe aceita esse custo.

**Pronto quando:** fluxo completo pela interface com os dois perfis: entrada de estoque → pedido → saldo reduzido e movimentações `SAIDA` → (se US27) cancelar → pedido marcado como cancelado, saldo **inalterado**; descartar linha vencida → saldo e alerta atualizados.

### Fase 4 — Recomendação de produção: Java + Tela 12 (US10, US11) — **Sprint 3 (até 18/10)**

Pré-requisitos: Fase P e Fase 2. Responsáveis propostos: Lucas e Kauê em dupla (Java), Leonardo (tela).

**4A. Backend**
0. **Hello World (pendência da Sprint 1, D6):** implementar `GET /api/teste/python` (hoje **não existe** no backend) chamando o módulo Python no modo eco; validar com a tela `#/comunicacao`. **Fica por enquanto**, como vocês decidiram, e **é removido, junto com o cartão da tela e o ramo de eco do Python, depois que a Fase 4 estiver validada e antes do deploy da Sprint 4** (o Contrato §1.5 lembra que ele não tem controle de acesso: não pode ir para produção).
1. Interface `OtimizadorProducao` com um **dublê** que devolve respostas no formato do Contrato v2 (destrava endpoint, tela e testes sem depender da licença).
2. Montagem do JSON: só produtos `ativo = true`; **todos** os ingredientes usados por eles, **inclusive com estoque 0**; `custo_unitario` em cada um; `quantidade_estoque` = **saldo válido** (soma de `quantidade_restante` das linhas não vencidas; CT29).
3. Chamada real com `ProcessBuilder`: UTF-8, fechar o `stdin`, timeout configurável (padrão 10 s), `stderr` para o log; `status: "erro"` = falha de negócio/dados (`422`); código de saída ≠ 0 ou timeout = falha técnica (`500` sem *stack trace*).
4. `PYTHON_CMD` e `OTIMIZADOR_SCRIPT` passam a ser lidas. **As credenciais Gurobi precisam ser passadas explicitamente ao processo filho** (`ProcessBuilder.environment()`): variável carregada só pelo mecanismo do Spring não chega ao Python (o Diogo confirmou por teste). Explicação em 10.2 (D7c).
5. Recalcular com `BigDecimal` a receita **e a margem** a partir das quantidades inteiras; o valor do Python serve de conferência.
6. `POST /api/producao/recomendacao` (só Administrador), em linguagem de negócio, sem termos de otimização. **Guardar o último resultado em memória do backend** para a Tela 14 (Fase 6).

**4B. Frontend (Tela 12):** habilitar "Produção"; botão "Gerar recomendação"; carregamento, erro e nova tentativa; quantidade por produto, receita estimada e **lucro potencial** (com aviso de que é sugestão); ingredientes de cada produto (a composição já vem de `GET /api/produtos`); mensagens para quantidade 0 (D9, 10.2).

**Testes:** `MockMvc` com o dublê (Funcionário → `403`); montador de JSON (produto inativo fora, ingrediente com estoque 0 dentro, linha vencida descontada = CT29); teste de contrato com resposta real do Python guardada em arquivo; ponta a ponta manual com evidência.
**Pronto quando:** o Administrador gera a recomendação pela Tela 12 com banco e Python reais, o resultado bate com o oráculo, e o Funcionário não acessa a rota.

### Fase 5 — Reposição e alertas (US14, US15, US16) — **Sprint 4**

- **US14 (Must, G) — redesenhada.** Tela 13 ganha dois campos: **período (dias)** e **orçamento máximo (OPEX, R$)**. `POST /api/reposicao/recomendacao` (Admin) — **muda de `GET` para `POST`** pelo mesmo motivo da produção (ação disparada pelo usuário que roda o solver); corpo `{ "periodo_dias": 30, "orcamento": 1500.00 }`.
  - Java: soma o consumo por ingrediente no período, escolhe os 8 (≤ 30 dias) ou 15 (> 30 dias) mais consumidos, monta o JSON, chama o módulo da Fase Q (mesma estratégia de `ProcessBuilder`, mesmo dublê) e devolve a tabela: ingrediente, `quantidade_sugerida`, `custo_estimado`, `custo_total`, `orcamento_suficiente`.
  - **De onde sai o consumo (decidido em 14/10):** `pedido_produto × composição` dentro do período. Descartes não entram. **Pedidos cancelados contam** (decidido em 14/10), porque o ingrediente foi perdido; faturamento e ranking não os contam.
  - O frontend ainda não tem a Tela 13 no repositório (seção 0): contar com a construção, não só com a ligação.
- **US15 (Should, G)** — `GET /api/estoque/alertas-reposicao`: alerta quando **`saldo <= quantidade_minima`** (D13). "Saldo" = o **saldo válido**, sem o vencido (decidido em 14/10). Alerta em nível de interface (faixa nas Telas 10 e 16); sem pedido automático.
- **US16 (Should, M)** — `GET /api/estoque/alertas-validade`: **por linha**; só `ENTRADA` com `data_validade`; **"próxima do vencimento" = faltam até 7 dias** (D13); linhas já vencidas destacadas à parte; só linhas com restante > 0; usa a data do servidor. O 7 fica como constante com parâmetro opcional `dias` (sem tabela de configuração); "vence hoje" conta como **próxima**, não vencida (proposta).
- **Testes** (a partir de CT30): alerta no limite exato do mínimo (`=` alerta); linha sem validade não alerta; linha vencida e linha a 7 e a 8 dias; linha totalmente consumida não alerta; reposição com consumo conhecido (à mão), orçamento suficiente e insuficiente.

### Fase 6 — Dashboard, marketing e CSV (US17, US18, US19) — **Sprint 4**

- **US17 (Must, G)** — `GET /api/dashboard` (Admin), **conteúdo = Tela 03 (D14)**: faturamento mensal, produtos mais vendidos, ingredientes mais utilizados e ranking de vendas por usuário (detalhado abaixo). **"Receita máxima estimada" sai** (o backlog a cita; a Tela 03 não) e o dashboard **não precisa mais do Python** (é só SQL com `GROUP BY`/`JOIN`, requisito R4). Só pedidos `ATIVO`.
  - **Ranking de vendas por usuário (decidido em 14/10):** tabela com as colunas **Rank, Usuário, Quantidade de pedidos vendidos, Valor total vendido e Tipos de produto** (só os que o usuário vendeu). **Filtros:** Usuário, Tipo de produto e Valor total vendido.
  - **Detalhes do ranking (aprovados em 14/10):** *Rank* = posição por **valor total vendido**, do maior para o menor (desempate por quantidade de pedidos); *Tipos de produto* = lista dos produtos vendidos naquele usuário (em SQL, `STRING_AGG(DISTINCT …)`); filtro de *valor total* = faixa mínimo–máximo aplicada **depois** de somar por usuário (`HAVING`); filtro de *tipo de produto* restringe também a quantidade e o valor somados àquele(s) produto(s).
  - **"Tipo de produto" = nome do produto** (decidido em 14/10), então não há categoria nova no banco. O filtro lista os nomes dos produtos.
  - **Período = 1 mês** (decidido em 14/10): um seletor de **mês/ano**, com o mês atual como padrão. Vale para **todos** os blocos, inclusive o ranking (decidido em 14/10).
- **US18 (Must, G)** — `GET /api/marketing/sugestoes` (Admin), Tela 14. **Como o resultado da Tela 12 chega à Tela 14 (D15):**
  - **Ferramenta recomendada: guardar o último resultado na memória do backend** (um componente Spring com um `AtomicReference` com o resultado e o horário). Está no stack (Java/Spring), **não cria tabela** e bate com o DER, que já diz que o resultado da otimização "é mantido em memória (RAM) do processo, não em banco de dados". Mantém `GET /api/marketing/sugestoes` sem parâmetros, como no Contrato, e a regra de "priorizado" fica em um só lugar.
  - **Fluxo:** o Administrador gera a recomendação na Tela 12 → `POST /api/producao/recomendacao` guarda o resultado → ao abrir a Tela 14, `GET /api/marketing/sugestoes` monta as sugestões a partir dele. Se ainda não houve cálculo, a tela mostra "Gere a recomendação na tela Produção para ver as sugestões" com link para ela. Cada resposta traz `calculado_em` para o usuário saber de quando é.
  - **Cuidados:** a memória some se o backend reiniciar, e no plano gratuito do Render ele dorme após 15 min sem uso (o resultado se perde; a mensagem acima cobre). Só há um processo, então não há problema de duas instâncias. Como o estoque muda, a tela deve mostrar a hora do cálculo.
  - **Alternativas que descartei:** (1) guardar no `sessionStorage` do navegador — funciona, mas é por aba e a regra de priorização ficaria no frontend; (2) tabela no banco — contraria a decisão do DER de não persistir recomendações.
  - **Produto priorizado (D15):** o de **maior quantidade recomendada**, só com quantidade > 0, em ordem decrescente, desempate pelo menor id. **Quantos entram (decidido em 14/10): até 3**; se menos de 3 produtos tiverem quantidade > 0, aparecem menos.
  - **Sugestão de campanha = produto + selo "Produto priorizado" + uma ideia em linguagem simples**, como nos seus exemplos. Implementação: um **catálogo fixo** de ideias (Apêndice B), com o nome do produto no texto, distribuído por posição (a 1ª ideia para o 1º produto etc., sem repetir no mesmo resultado). Formato proposto da resposta:

```json
{
  "calculado_em": "2026-10-13T20:30:00",
  "sugestoes": [
    { "produto_id": 2, "produto": "Açaí Especial 500ml", "quantidade_recomendada": 46,
      "selo": "Produto priorizado",
      "ideia": "Destaque o tamanho",
      "como_fazer": "Poste uma foto do copo ao lado de uma colher e escreva o tamanho no texto." }
  ]
}
```
  - **Não publica nada automaticamente** (critério do backlog). Como já temos o lucro por copo, **proposta opcional:** nas ideias de desconto, mostrar o lucro por copo para o dono saber quanto de desconto cabe.
- **US19 (Could, P)** — `GET /api/dashboard/export-csv`: **separador `;` e UTF-8 com BOM** (D17), mesmas colunas e filtros da tela. O JWT vai no header `Authorization`, então o download precisa de `fetch` + `Blob`, não de um link.
- **Dados de demonstração:** `bd/seed_demo.sql` (10.1).
- **Testes:** consulta agregada contra dados de resultado conhecido (à mão), com Testcontainers; autorização (Funcionário → `403`); CSV conferido com a tela; sugestões com resultado vazio, com um e com três produtos; empate de quantidade.

### Fase 7 — Porção padrão (US26, Could) — **Sprint 4**

Tela 19, `POST /api/unidades-medida` e **`PATCH /api/unidades-medida/{unidade_medida}`** (Admin) para alterar a porção padrão de uma unidade existente. Dos três critérios do backlog, dois já existem no frontend (sugerir a porção; poder sobrescrevê-la) assim que `GET /api/unidades-medida` existir; o terceiro (alterar o padrão **não** recalcula ingredientes existentes) é regra de backend, e o DER já o descreve.

**Sobre o PATCH (sua pergunta): é boa ideia.** `PATCH` é o verbo certo para alterar **só um campo** de um recurso existente (aqui, `porcao_padrao`), e a chave da unidade (`kg`, `l`…) já é a chave natural da tabela. Requisitos: (1) **liberar `PATCH` no CORS** (Fase 1; é o erro clássico de "funciona no Postman, falha no navegador"); (2) registrar a rota no Contrato (corpo `{ "porcao_padrao": 0.250 }`, `404` unidade inexistente, `400` porção ≤ 0); (3) o frontend usa `fetch` com `method: 'PATCH'`, que já funciona. `PUT` também serviria, mas exigiria enviar o objeto inteiro à toa. **Excluir unidade fica de fora** (a FK já impede quando em uso).
Testes novos: porção zero rejeitada, unidade duplicada no `POST`, `PATCH` não altera ingredientes existentes, ingrediente com porção diferente do padrão da unidade (Plano de Testes §4.2).

### Fase 8 — Qualidade e encerramento (contínua)

- **Definition of Done (toda história):** PR revisado por outro integrante; teste automatizado do caminho principal; sem credencial no código; rota restrita por perfil com verificação no servidor; frontend integrado ao endpoint real; evidência registrada **durante** a sprint (a Sprint 2 terminou com vários `[PREENCHER]`/`[CONFIRMAR]`); contrato atualizado se o formato mudar.
- **Critérios de pronto do MVP (Termo):** login por perfil; fluxo cadastro → estoque → recomendação/propaganda → pedido → baixa automática; otimização funcionando; dashboard com dados reais; alertas e recomendações; URL pública (Sprint 4); README que permita rodar do zero.
- **Plano de Testes:** casos de US11, US13–US19, US26 e US27, os do modelo novo, os das linhas, e reescrever CT23/CT24/CT29. **Precisa estar completo antes de 30/10**, porque a janela de testes de 31/10 a 06/11 o executa e gera o **Relatório de Testes Executados** (seção 4.4).
- **Remover antes do deploy:** `/api/teste/python`, o cartão da tela de comunicação e o ramo de eco (Fase 4A, item 0).
- **CI (opcional):** não há `.github/`. O `pytest` roda sem WLS com a licença restrita do pacote (seção 0), o que viabiliza um job de Python; para o Java, `mvn test` com Testcontainers e o dublê.
- **README:** mover `JWT_SECRET`, `CORS_ALLOWED_ORIGIN`, `PYTHON_CMD` e `OTIMIZADOR_SCRIPT` de "planejadas" para "obrigatórias"; remover o aviso "Estado atual (corte da Sprint 2)".
- **Rituais:** dailies/weeklies; **Sprint Review com demonstração** (não houve nas Sprints 1 e 2) e **Retrospectiva**.
- **Entregáveis finais**, no padrão de `docs/Sprint_2/`: relatório de entrega, evidências de teste, ata de retrospectiva, relatórios individuais e backlog atualizado (`.docx`), com números de PR.

---

## 6. Divisão de trabalho (aceita pela equipe em 13/10)

| Integrante | Foco |
|---|---|
| Lucas (PO + backend) | Fases 1 e 2; endpoints das Fases 3, 5 e 6; Fase 4A em dupla; decisões de PO |
| Diogo (dados) | SQL, constraints e seed das Fases 3 a 6; consultas do dashboard; **repositórios Java do SQL que ele define**; **atualização dos documentos** (10.1); `seed_demo.sql`; relatório da Sprint 2 |
| Leonardo (frontend) | Integração real da Fase 2; **telas novas** (10, 11, 12, 13 com período/orçamento, 14, 16, 17, 03, 19 e, se entrarem, 18/18B e o cancelamento); **dividir o `app.js` em módulos antes de começar** |
| Kauê (facilitador + Python) | **Fases P e Q**; Contrato v2; ambiente Gurobi; Fase 4A em dupla; rituais; CI, se entrar |

Risco principal: **o Lucas concentra o backend inteiro e o papel de PO**, e agora há mais backend (reposição, cancelamento, `GET /api/estoque`).

---

## 7. Riscos

| Risco | Mitigação proposta |
|---|---|
| **Prazo:** Sprint 3 termina em 16/10 (até 18/10); **a Sprint 4 (até 30/10, ~12 dias corridos) passa a carregar US14–US19, US26, US13, US27 e o deploy** (os bugs ficam para a janela de testes) | Deploy cedo; ordem por custo (4.2); congelar funcionalidades em 28/10; teto de escopo definido (4.2, item 5) |
| Gargalo no backend (um desenvolvedor, quase todas as histórias G) | Dividir endpoints (seção 6); começar pela Fase 1 |
| **US14 cresceu: segundo modelo** (as regras de mínimo, objetivo, consumo, casas e ordem já estão fechadas) | Modelar no papel antes de codificar (Fase Q) |
| **Regra das "linhas":** a baixa FEFO por linha é nova e transacional | Coluna `quantidade_restante` + `motivo`; testar o invariante (`quantidade_estoque` = soma dos restantes) e a concorrência |
| Repetir a integração tardia da Sprint 2 | Gate por fase com frontend real |
| **Sprint 4: o backend continua concentrado no Lucas** (US14 a US19, US26, US13, US27 e a configuração do deploy) | Diogo escreve os repositórios Java (aceito em D3); dividir endpoints por pessoa já no planejamento da Sprint 4; Kauê assume a ligação Java do módulo de reposição em dupla |
| **A janela de testes vira prorrogação da Sprint 4** (história inacabada entrando como "correção") | Regra da 4.4, item 5: só correções; o resto vira pendência conhecida no relatório |
| **Plano de Testes incompleto na hora da janela de testes** (cobre US1–US10 e US12) | Cada história entrega seus casos; Plano completo antes de 30/10 (4.4) |
| Estrutura do repositório quebrar de novo | PRs pequenos e revisados; ninguém recria o repositório sem preservar cópia |
| **Contrato v2 quebra o que já existe** (8 testes, README, Contrato, Java) | Fechar na Fase 0 e atualizar tudo no mesmo PR |
| **Exatidão numérica** (Gurobi resolve em ponto flutuante) | Escalar, conferir em aritmética inteira, testar contra oráculo |
| **Tempo de resposta** (1 + \|P\| otimizações; validação de licença WLS pela rede; timeout de 10 s ainda não medido) | Medir com o cardápio real **e no Render**; timeout configurável |
| **Licença Gurobi acadêmica: 1 job por vez** (produção e reposição simultâneas) | Documentar; serializar chamadas no backend |
| Licença do Lucas: validade de 90 dias, renovação exige a rede da Fatec | Conferir a validade contra a data da demonstração final |
| RAM do container no Render (512 MB com Java + Python + `gurobipy`); resultado de marketing em memória some quando o serviço dorme | Medir no deploy (Sprint 4); mensagem de "gere novamente" na Tela 14 |
| Endpoint `/api/teste/python` sem autenticação chegar à produção | Remover antes do deploy (Fase 8) |
| Ambiente Windows e variáveis de ambiente | Documentar CMD e PowerShell; `.env.example` completo |

---

## 8. Resumo do que é entregue

**Ao fim da Sprint 3 (16/10; até 18/10):** segurança, erros e CORS; login e cadastros (US1–US7) com banco real e os dois perfis; mudanças do banco (`quantidade_restante`, `motivo`, `status` do pedido); módulo Python de produção com o modelo final (lucro), testado em mais de uma máquina; Contrato v2 de produção e estoque. **Se a extensão até 18/10 for usada:** movimentações de estoque por linhas, `GET /api/estoque`, pedido com baixa automática e a **recomendação de produção** (Python → Java → Tela 12).

**Na Sprint 4 (até 30/10), seguida pela janela de testes (31/10 a 06/11):** reposição com orçamento (US14), alertas (US15, US16), dashboard (US17), marketing (US18), CSV (US19), porção padrão (US26), clientes (US13), cancelar pedido (US27), deploy (US20), remoção de `/api/teste/python`, Sprint Review e Retrospectiva. **Na janela de testes:** baterias de testes, localização e correção dos bugs, Relatório de Testes Executados.

---

## 9. Registro das respostas de 13/10 (D1–D22)

| D | Decisão da equipe | Onde entrou |
|---|---|---|
| D1 | Analisar o backlog para responder as datas | Os backlogs não trazem datas; em 14/10 a equipe informou: **Sprint 3 termina em 16/10 (até 18/10)**; o excedente **vai para a Sprint 4** (seções 2.1 e 4.2). **Sprint 4 até 30/10**; **testes de 31/10 a 06/11** (informado em 14/10), seções 4.3 e 4.4 |
| D2 | Manter tudo da Sprint 1 e 2 na Sprint 3; Sprint 4 como definida | Seções 2 e 4 |
| D3 | Aceitam: Diogo escreve repositórios, Kauê em dupla na 4A, Leonardo divide o `app.js` | Seção 6 |
| D4 | Diogo atualiza e commita | 10.1 |
| D5 | **"Não entendi"** | Explicação completa em 10.2, com exemplos |
| D6 | Manter `/api/teste/python` por enquanto; tirar depois de validado | Fase P (item 8) e Fase 4A (item 0), Fase 8 |
| D7 | (a) Diogo; (b), (c) Kauê | 10.1 e 10.2 |
| D8, D9 | Kauê | 10.2 |
| D10 | Sem lotes; estoque por linhas; alertar linha vencida | Fase 3 (14/10: saída consome a menor validade **válida**; pedido nunca consome vencido; vencido é descontado da produção até o descarte; `quantidade_restante` incluída) |
| D11 | Diogo | 10.1 |
| D12 | Solver com OPEX, 8/15 ingredientes, tabela com custo e total, sinalizar orçamento insuficiente | Fases Q e 5 (14/10: todas as dúvidas de modelagem respondidas) |
| D13 | `saldo <= mínimo`; "próximo do vencimento" = 1 semana | Fase 5 |
| D14 | Lista final = Tela 03 | Fase 6 |
| D15 | Priorizado = maior quantidade recomendada; resultado da Tela 12 aparece na Tela 14; eu recomendo a ferramenta e as ideias | Fase 6 e Apêndice B |
| D16 | `PATCH`? Pedem minha opinião | Fase 7 (**sim, com CORS**) |
| D17 | `;` e UTF-8 com BOM | Fase 6 |
| D18 | `GET /api/estoque` | Fase 3 |
| D19 | `seed_demo.sql` permitido | 10.1 |
| D20 | Cancelar pedido, se não for muito difícil | **US27** (Fase 3): M, não difícil |
| D21, D22 | Diogo corrige relatório da Sprint 2 e os caminhos antigos | 10.1 |

---

## 10. Pendências por pessoa (com explicação)

### 10.1 Diogo

1. **`quantidade_restante` e `motivo` em `movimentacao_estoque` (explicação simples).**
   *O problema:* cada `ENTRADA` é uma linha ("entraram 20 l de açaí, vence em 12/10"). Essa linha guarda **quanto entrou**, mas não **quanto ainda sobra** dela. Só que as regras decididas pedem exatamente isso: a saída tira da linha de menor validade; pedido nunca tira de linha vencida; o alerta é por linha; o vencido é descontado da produção até o descarte. Para qualquer uma delas, o sistema precisa saber "quanto ainda tem nesta linha". `quantidade_restante` é esse número, guardado na própria linha de entrada (começa igual a `quantidade` e vai diminuindo).
   *Exemplo (hoje é 13/10):*

   | Linha | Entrou | Vence | `quantidade_restante` |
   |---|---|---|---|
   | 1 | 20 l | 12/10 (vencida) | 20 |
   | 2 | 20 l | 14/10 (vence em 1 dia) | 20 |

   Um pedido usa 5 l de açaí: a linha 1 está vencida, então o sistema **pula** e tira da linha 2, que fica com 15. Saldo total do açaí: 35 l; saldo válido: 15 l. O alerta mostra "linha 1 vencida, 20 l"; a produção enxerga só os 15 l válidos. O dono clica em "Descartar" (ou faz a saída "Descarte de vencidos"): a linha 1 vai a 0 e o saldo total passa a 15 l.
   *Sem a coluna:* teríamos de adivinhar quanto de cada linha foi gasto somando todas as saídas, e isso erra quando chega uma linha nova com validade menor que a de linhas já usadas.
   *O que fazer:* (a) `quantidade_restante DECIMAL(10,3) NULL`, preenchida só nas `ENTRADA`, com `CHECK` entre 0 e `quantidade`; (b) `motivo VARCHAR(20) NULL`, só nas `SAIDA`, com `PEDIDO`, `CONVENCIONAL` ou `DESCARTE_VENCIDO` (e `NULL` nas `ENTRADA`), amarrado a `tipo` por `CHECK`; (c) teste fixo: `ingrediente.quantidade_estoque` = soma dos `quantidade_restante`; (d) `Script_DDL.sql`, DER e `mapa_constraints.md` atualizados.
2. **Atualizar os documentos que dizem "maximizar a receita" (D4).** Lista: `README.md` (linha 3), Documento de Visão (objetivo do MVP e requisito R3), Termo de Aceite (item 5 do escopo e critério de pronto), Backlog (US10), mais as novas mudanças: **US14** (agora com orçamento e solver), **US16** ("X dias" = 7), **US17** (lista da Tela 03, sem "receita máxima estimada"), **US26** (`PATCH`), **US27** (nova), `Arquitetura.md` (§3.3 e **D5**, que dizia "reposição em Java"), DER (regra de linhas e status do pedido), Plano de Testes (CT23, CT24, CT29 e casos novos). *Sugestão:* como o Termo foi **assinado em 28/08**, registrar a mudança como um **adendo datado** (13/10/2026) em vez de reescrever o texto assinado, e avisar o professor.
3. **Versão do Gurobi (D7a).** *O que é:* três lugares dizem coisas diferentes: Arquitetura e README falam em Gurobi **13.0.1** e Python **3.14**; o `optimization/requirements.txt` fixa **13.0.3**. *O que fazer:* conferir qual versão de fato roda (`pip show gurobipy`) e qual será instalada no container do Render, escolher **uma**, e deixar os três arquivos iguais. Aproveitar para regravar o `requirements.txt` em UTF-8 (hoje é UTF-16; o `pip` lê, mas editores e diffs podem estranhar).
4. **Saldo de abertura (D11).** *O problema, com exemplo:* o seed do `Script_DDL.sql` grava o estoque direto em `ingrediente.quantidade_estoque` (por exemplo, açaí com 40), **mas não existe nenhuma linha em `movimentacao_estoque`**. Pela regra de linhas decidida em 13/10, o sistema não sabe de onde vêm esses 40 nem quando vencem: não há linha para alertar e a coluna `quantidade_restante` não teria de onde sair. O próprio comentário do script diz que na Sprint 2 esses saldos seriam registrados como `ENTRADA` com `data_validade`. *O que decidir:* (a) **proposta:** o banco de produção/entrega começa com **estoque 0** e o dono lança as entradas pela tela; (b) os saldos de exemplo viram `ENTRADA` no **`seed_demo.sql`**, cada uma com `quantidade_restante` igual à quantidade; (c) a dificuldade: `usuario_id` é `NOT NULL` e o seed não tem usuários, então o `seed_demo.sql` precisa criar um usuário demo **antes**. Cuidado: se ele criar um Administrador, `POST /api/auth/registro` passa a responder `409`; use só em ambiente de demonstração. (d) Validades de exemplo: uma linha já vencida, uma vencendo em ≤ 7 dias e as demais folgadas, para demonstrar os alertas. **Não colocar senha real nem hash de senha real no repositório.**
5. **`bd/seed_demo.sql` (D19).** Usuários de demonstração (mais de um, para o "ranking por usuário" da Tela 03), pedidos de vários dias e entradas com validades variadas. Sem tocar no `Script_DDL.sql`.
6. **Banco para as Fases 3 a 6:** `mapa_constraints.md` e `constraints_teste.sql` para `cliente`, `pedido`, `pedido_produto`, `movimentacao_estoque`; `status`/`data_cancelamento`/`usuario_cancelamento_id` em `pedido` (US27; **sem devolução de estoque**); *se a US27b for aprovada:* tabela `movimentacao_consumo` (Fase 3); as **colunas `quantidade_restante` e `motivo`** (item 1), a baixa FEFO só sobre linhas válidas por linha (Fase 3), do consumo por período (pedidos × composição), do dashboard e dos alertas.
7. **Relatório da Sprint 2 (D21).** O relatório diz que **não houve** retrospectiva, mas as quatro contribuições individuais marcam "[X] Retrospectiva". Alinhar os documentos com o que realmente aconteceu e, se não houve, **fazer** a retrospectiva (`sprint-2-retrospectiva.md` §4 já lista propostas). A Sprint Review das Sprints 1 e 2 também nunca ocorreu; alinhar com o professor se é exigência.
8. **Caminhos antigos (D22).** `docs/Script_DDL.sql` → `bd/Script_DDL.sql`; `constraints_sprint1_teste.sql` → `bd/constraints_teste.sql`; `sprint1-roteiro.md`/`sprint-1-roteiro.md`, `sprint-1-backlog.md`, `sprint-2-backlog.md`, `Pendencias_ARQUITETURA_para_o_grupo.md` e `Roteiro_Sprint1_Detalhado_PostgreSQL_v3.md` (os backlogs hoje são `.docx`); o roteiro da Sprint 1 ainda diz JDK 21 e Spring Boot 3.3.4 (hoje 25 e 4.0.8); o Contrato cita `docs/sql_repositorios_sprint1.md`, que agora está em `docs/Apoio/`.
9. **Repositórios Java** do SQL que ele mesmo escreveu (D3).

### 10.2 Kauê

**D5 — Contrato v2, explicado.** O Contrato define **dois** "idiomas": o que o Java fala com o Python (§1) e o que o frontend fala com o Java (§2). O modelo novo exige mudar o primeiro e criar uma parte do segundo. Os quatro itens:

**(a) Mais um campo na ida e na volta.** O modelo novo calcula **lucro** (preço − custo dos ingredientes), então o Python precisa saber o **custo** de cada ingrediente, e devolve o **lucro total**. Hoje, na ida, cada ingrediente só tem `id` e `quantidade_estoque`:

```json
// HOJE (ida, trecho)
{ "id": 1, "quantidade_estoque": 40.000 }
// v2 (ida, trecho)
{ "id": 1, "quantidade_estoque": 40.000, "custo_unitario": 18.50 }

// HOJE (volta)
{ "status": "sucesso", "receita_estimada": 851.40,
  "recomendacao": [ { "produto_id": 1, "quantidade": 66 }, { "produto_id": 2, "quantidade": 0 } ] }
// v2 (volta): acrescenta margem_estimada
{ "status": "sucesso", "receita_estimada": 851.40, "margem_estimada": 438.24,
  "recomendacao": [ ... ] }
```
Decisão: **confirmar** que é só isso. (Esses números são os do exemplo do Contrato com o custo do seed: receita 851,40 e lucro 438,24.)

**(b) `consumo` também?** O modelo define como saída "quanto o plano gasta de cada ingrediente". Exemplo para o mesmo plano (66 copos do produto 1): açaí 19,800 kg, leite condensado 1,980, copo 66,000.
```json
"consumo": [ { "ingrediente_id": 1, "quantidade": 19.800 },
             { "ingrediente_id": 2, "quantidade": 1.980 },
             { "ingrediente_id": 7, "quantidade": 66.000 } ]
```
Nenhuma tela pede isso. **Proposta:** o Python **devolve** (é barato e serve para conferir que o plano cabe no estoque), mas o Java **não repassa** ao frontend por enquanto. Decisão: sim ou não.

**(c) Que erro HTTP o usuário recebe.** Dois tipos de falha: (1) o Python **responde `status: "erro"`** (por exemplo, "Nenhum produto ativo para otimização"): é um problema de dados que o usuário pode corrigir; (2) o Python **quebra ou demora** (código de saída ≠ 0, timeout): é falha técnica. **Proposta:** (1) → `422` com `{ "mensagem": "…" }`; (2) → `500` com mensagem genérica, sem detalhes, e o detalhe no log. Decisão: concorda?

**(d) A resposta do Java para a Tela 12.** O Contrato **não tem** o formato da resposta de `POST /api/producao/recomendacao`. **Proposta:** usar os mesmos nomes do Python, mais o nome do produto e o lucro por copo (que o Java calcula):
```json
{ "calculado_em": "2026-10-13T20:30:00",
  "receita_estimada": 851.40, "margem_estimada": 438.24,
  "recomendacao": [
    { "produto_id": 1, "nome": "Açaí Tradicional 300ml", "quantidade": 66, "lucro_por_copo": 6.64 },
    { "produto_id": 2, "nome": "Açaí Especial 500ml", "quantidade": 0, "lucro_por_copo": 9.07 } ] }
```
Na tela, `margem_estimada` aparece como "lucro potencial". Decisão: aprovar ou ajustar.

**D7 (b) — Como o Python recebe a licença (Alternativa C).** *Em palavras simples:* a licença do Gurobi é uma "chave" de três valores (`WLSACCESSID`, `WLSSECRET`, `LICENSEID`). No seu computador ela fica em arquivo ou no Compute Server local; **no servidor do Render não há esse arquivo**, então a chave tem de chegar por **variáveis de ambiente** (como já é a senha do banco), nunca no código nem no Git. Cabe ao `otimizador.py` (e ao de reposição) **ler** essas três variáveis e **entregá-las ao Gurobi** ao criar o ambiente, antes de iniciá-lo. Conferir na documentação da versão instalada qual é o jeito de passar os parâmetros WLS.

**D7 (c) — Como o Java entrega a licença ao Python.** *O problema:* o Spring lê o arquivo `.env` para a **configuração dele**, mas isso **não** coloca as variáveis no ambiente do sistema; quando o Java abre o Python (`ProcessBuilder`), o Python só enxerga as variáveis do sistema. Por isso o Diogo viu a licença "sumir" no teste. *Solução:* o Java lê os três valores pela configuração do Spring e os **coloca explicitamente** em `processBuilder.environment()` antes de iniciar o processo (no Render, onde são variáveis reais, também funciona). Regras: nunca escrever esses valores em log nem em mensagem de erro; e a licença só pode ser usada pelo processo de otimização.

**D7 (d)** (sobrou): qual configuração local você usou nos testes de 30/09? O script não configura nada explicitamente; registre no README para os outros reproduzirem.

**D8 — Exatidão, falha e tempo.** *Contexto:* o Gurobi calcula com números decimais aproximados; pode devolver `65,9999999` em vez de `66`, ou, raramente, um plano que estoura um estoque por uma diferença minúscula. O Python arredonda e **reconfere tudo com contas exatas com inteiros**: o plano cabe em cada ingrediente? o lucro recalculado é igual ao `L*` do primeiro estágio? Três decisões: **(1)** você concorda com essa conferência depois de cada estágio? **(2)** Se a conferência falhar, o que fazer? *Proposta:* tratar como **falha técnica** (código de saída ≠ 0 → o Java devolve `500` genérico e registra o detalhe), nunca mostrar um plano que pode estar errado. **(3)** Timeout: manter **10 s configurável** e medir com o cardápio real; a conta tem 1 + (nº de produtos) otimizações, e no Render a validação da licença pela rede e a partida do Python podem somar segundos. Medir **no Render**, não só no seu PC.

**D9 — Tela 12, o que mostrar quando um produto vem 0.** *Dois motivos diferentes para o mesmo "0":* **(i) sem lucro:** o custo dos ingredientes é maior ou igual ao preço (exemplo: preço R$ 5,00 e custo R$ 6,26 → lucro negativo); o modelo **não produz** esse copo de propósito. **(ii) sem estoque:** o copo daria lucro, mas falta algum ingrediente (no exemplo do Contrato, o produto 2 vem 0 porque a granola está zerada, e o lucro dele seria R$ 9,07). A tela hoje só mostraria "0" nos dois casos. *Perguntas:* explicar o motivo em cada linha? E, quando **tudo** vem 0, usar mensagens diferentes ("Nenhum produto dá lucro hoje" × "Estoque insuficiente")? *Proposta sem mexer no Python:* o Java já calcula o `lucro_por_copo` (item d); a tela marca **"Sem lucro"** se for ≤ 0 e **"Falta ingrediente"** se for > 0 e a quantidade for 0.

**Fase Q (US14).** Formalizar o modelo (seção 5, Fase Q) e fechar as decisões 11.3 a 11.7. Eu posso redigir o rascunho do modelo e a conferência por força bruta, como fiz com o de produção.

**D6.** Implementar o ramo de eco no Python (Fase P, item 8).

### 10.3 Leonardo

Dividir o `app.js` em módulos; Tela 13 com **período e orçamento**; Telas 10/16 (alertas, linhas e `GET /api/estoque`; **botão "Saída" abre "Saída convencional" e "Descarte de vencidos"**, este só habilitado se `quantidade_vencida > 0`; botão "Descartar" no alerta da linha), 12 (motivos do 0), 14 (selo e ideia, até 3 produtos), 03 (dashboard com a tabela de ranking: Rank, Usuário, Quantidade de pedidos, Valor total, Tipos de produto; filtros Usuário, Tipo de produto e Valor total), 19; lista de pedidos com cancelamento para os dois perfis (se US27 entrar); Tela 13 com período (dias) e orçamento (R$) e tabela ordenada por custo estimado.

### 10.4 Lucas (PO + backend)

Puxar as Fases 1 e 2 e o calendário da seção 4.3.

---

## 11. Dúvidas que ainda estão abertas

**Nenhuma.** Todas as decisões de escopo, regras de negócio, modelagem, prazos e do fluxo de correção de bugs estão tomadas e registradas nas seções 2.1, 4.2 a 4.4 e 9. O que resta são pendências de execução, listadas por pessoa na seção 10.

---

## Apêndice A — Respostas rápidas às perguntas feitas em 13/10

- **PATCH é boa ideia?** Sim; condição: liberar `PATCH` no CORS (Fase 7).
- **Cancelar pedido é muito difícil?** Não: tamanho M; a parte chata é o entorno (dashboard e consumo só de pedidos ativos, documentação). Entra como US27, Could (Fase 3).
- **Ferramenta para levar o resultado da Produção ao Marketing?** Memória do backend (Fase 6).
- **O que é `quantidade_restante`?** O "quanto ainda sobra" de cada linha de entrada; explicação com exemplo na seção 10.1, item 1. É necessário para cumprir as regras de vencimento decididas.
- **Devolver o ingrediente de um pedido cancelado à sua linha é difícil?** É factível, mas é a parte mais trabalhosa do cancelamento (tabela `movimentacao_consumo`, tipo `ESTORNO`, casos de borda); M a G contra P/M sem devolução. Detalhes na Fase 3 (US27b).

## Apêndice B — Banco de ideias de campanha (linguagem simples)

**Como o sistema usa isto:** cada produto priorizado recebe **uma** ideia do catálogo (sem repetir no mesmo resultado). O texto deve caber em duas linhas, sem termos de marketing. O sistema **não publica nada**; só sugere.

| # | Ideia (título) | O que dizer ao dono, em palavras simples | Origem |
|---|---|---|---|
| 1 | Destaque o tamanho | "Mostre em destaque que o copo é de {tamanho}. Poste uma foto com uma colher ao lado." | Exemplo da equipe |
| 2 | Mostre o que vai dentro | "Poste uma foto ou um vídeo curto mostrando cada ingrediente do {produto}." | Exemplo da equipe; Saipos (bastidores) |
| 3 | Convide para uma pausa | "Convide os clientes para uma pausa com {produto} no meio da tarde." | Exemplo da equipe |
| 4 | Foto bonita | "Monte o {produto} em camadas coloridas e tire uma foto para postar. Copo bonito faz o cliente postar também." | Saipos (produtos "instagramáveis") |
| 5 | Combo para dividir | "Monte um combo do {produto} para duas ou mais pessoas, com preço melhor que comprar separado." | Saipos (combos compartilháveis) |
| 6 | Combo com extra | "Junte ao {produto} um suco ou uma sobremesa por um preço fechado." | Saipos (combos com extras) |
| 7 | Desconto na primeira compra | "Dê um desconto para quem pedir o {produto} pela primeira vez." | Saipos (cupom de primeira compra) |
| 8 | Cartão fidelidade | "A cada certo número de compras, a próxima sai grátis. Pode começar pelo {produto}." | Saipos (cartão de compras) |
| 9 | Edição limitada | "Anuncie o {produto} como 'só até acabar' ou 'especial do mês'." | Saipos (quantidade limitada; especial do mês) |
| 10 | Cupom na embalagem | "Coloque um cupom para o próximo pedido do {produto} junto da entrega ou no panfleto." | Saipos (panfleto e delivery) |
| 11 | Enquete ou sorteio | "Pergunte nos stories qual {produto} o cliente prefere, ou sorteie um para quem marcar um amigo." | Saipos (enquetes e sorteios) |
| 12 | Data ou clima | "Em dias frios ou datas especiais, ofereça o {produto} com cobertura quente ou em combo temático." | Saipos (açaí no inverno; datas comemorativas) |

**Observações de honestidade:** as ideias vêm de blogs da **Saipos** (empresa de sistema para restaurantes; conteúdo de divulgação, não pesquisa acadêmica), lidos em 13/10: `saipos.com/sistema/acaiteria/ideias-para-acaiteria`, `…/acai-no-inverno`, `…/propaganda-de-acai-delivery` e `…/panfleto-acaiteria`. Apareceram também PDFs do **Sebrae** na busca, mas **não os abri**; não os uso. As ideias são genéricas e nenhuma foi medida para o público de vocês. Os textos acima são redação minha, simplificada; antes de entrar no sistema, o Leonardo ou o Lucas devem relê-los com o professor ou com o cliente do projeto.
