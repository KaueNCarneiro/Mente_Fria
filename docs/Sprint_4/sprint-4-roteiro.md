# Roteiro — Sprint 4 — Mente Fria

**Revisão:** 28/10/2026, noite (v2: respostas do grupo, A43; v1: criação, A39–A42).
**Período:** a Sprint 4 oficial já está em curso; a **execução** planejada aqui vai de qui **29/10** a sex **06/11/2026**. A **entrega** e a **Review** com o professor são na **sex 06/11**.
**Depois:** uma **semana de mapeamento e correção de bugs, até 13/11** (A43). Ela não é uma sprint e não recebe histórias novas.

> **Este é o único documento com o calendário em vigor.** Os demais (tutoriais, matriz, decisões) apontam para os marcos daqui. Se um documento de `docs/Sprint_3/` trouxer uma data diferente, vale esta.

**Documentos irmãos:**
- `sprint-4-decisoes-e-pendencias.md`: decisões A39 em diante e as pendências abertas. As decisões A1–A38 continuam em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`.
- `sprint-4-mensagem-professor.md`: rascunho para o grupo enviar.
- `evidencias/`: evidências desta sprint.

---

## 0. Por que existe uma "Sprint 4" (A39)

No cronograma do professor, a **Sprint 3 já terminou** e o período atual é a **Sprint 4**. Os documentos de `docs/Sprint_3/` foram escritos durante a Sprint 3 oficial e descrevem, com o nome "Sprint 3", o trabalho que de fato é **executado agora**. A partir de 28/10/2026, os nomes ficam assim:

| Nome | O que é | Onde está |
|---|---|---|
| **Sprint 3** (oficial, encerrada) | Planejamento e preparação: roteiro, auditoria, Contrato v2 (na `main`), **código de referência completo e testado**, Adendo nº 1 aprovado, decisões A1–A38, documentos atualizados (A37, A38). **Nenhum código chegou à `main`** | `docs/Sprint_3/` (registro) |
| **Sprint 4** (oficial, até 06/11) | Levar o escopo aprovado ao **código real**, por PR fase a fase; publicar; entregar e fazer a Review em 06/11 | Este roteiro. **O como** continua nos tutoriais e em `docs/Sprint_3/referencia/` |
| **Semana de bugs** (depois de 06/11) | Executar a bateria de testes no ambiente publicado e **só corrigir** (era a "janela de testes" dos documentos da Sprint 3) | `docs/Sprint_3/sprint-3-casos-de-teste-propostos.md`, §3 |

**Sobre o Adendo nº 1 (A43):** o texto aprovado dizia "Sprint 3" por um equívoco de redação; o adendo foi validado pessoalmente com o professor **já como Sprint 4**. Em 28/10, o nome foi corrigido para "Sprint 4" no próprio adendo, com uma nota de correção. Nenhum conteúdo, critério ou data mudou. O nome do arquivo (`sprint-3-adendo-termo-de-aceite.md`) foi mantido, porque outros documentos apontam para ele.

**Os nomes antigos nos documentos da Sprint 3 são mantidos.** "Escopo da Sprint 3", "CT30–CT72 (Sprint 3)", `sprint-3-tutorial-*.md` e `referencia/` continuam com esses nomes. Leia "Sprint 3" nesses lugares como "o pacote planejado na Sprint 3 e entregue na Sprint 4".

---

## 1. Objetivo

**Em 06/11**, o sistema está **publicado numa URL HTTPS** e funciona de ponta a ponta, com banco, backend e frontend reais, nos dois perfis:

> cadastro e login → cadastros (ingrediente, produto, preço) → entrada de estoque com validade → recomendação de produção por lucro (Tela 12) e sugestões de campanha (Tela 14) → pedido com baixa automática por linhas → reposição com orçamento (Tela 13) → dashboard do mês (Tela 03).

É o fluxo do critério de pronto do Termo de Aceite, com a otimização **na URL publicada**, porque a licença do `pip` foi autorizada pela Gurobi (A38).

**Estado de partida, conferido em 28/10:**
- A `main` está no estado da Sprint 2: só `GET /api/saude` e `GET /api/ingredientes`, DDL da Sprint 1, otimizador v1 e as telas da Sprint 1.
- Nenhum PR de fase foi aberto.
- Todo o resto está pronto e testado em `docs/Sprint_3/referencia/`:
  - backend com 95 testes;
  - Python com 47;
  - banco com 32 + 41;
  - funções de apoio das telas com 15;
  - imagem de deploy.

## 2. Escopo

**Obrigatório:** US1–US7, US9–US12, US14, US17, US18 e US20 (decisão A2b), com as mudanças de banco e a cobertura dos alertas do adendo, item 4 (US15/US16 parciais).

**Opcional dentro do escopo:** o botão "Descartar" por linha (A12). É o primeiro corte.

**Condicional:** **US26** (porção padrão por unidade de medida). Só entra se passar no *gate* da seg 02/11 (§4; decisão A40).

**Fora do semestre** (A40):
- US13, US19 e US27;
- as versões completas de US15 e US16;
- US21–US25 (Won't).

Não há outra sprint de desenvolvimento depois de 06/11. Os motivos estão na A40.

## 3. Quem faz o quê

Os responsáveis e as fases são os mesmos já combinados. O passo a passo de cada fase está no tutorial de cada um.

| Entrega | Histórias | Responsável | Como fazer |
|---|---|---|---|
| Banco: patch do DDL, 41 testes, repositórios Java, `seed_demo.sql`, `sql_repositorios_sprint3.md` | Base de US9, US12, US14, US17 | Diogo | `docs/Sprint_4/tutoriais/tutorial-diogo-banco.md` (novo, A44) |
| Fases 1, 2, 3, 4A e 6 (segurança, cadastros, estoque e pedido, recomendação, dashboard e marketing) | US1–US7, US9–US12, US17, US18 | Lucas (+ Diogo nos repositórios, por commit na branch de cada fase do Lucas, A44; + Kauê na 4A) | `docs/Sprint_4/tutoriais/tutorial-lucas-backend.md` (novo, A44) |
| Fases P, Q e 5 (Python por lucro, reposição); deploy; facilitação | US10, US14, US20 | Kauê (+ Lucas no Render; + Diogo no `ReposicaoRepository`, por commit na branch da Fase 5, A44) | `docs/Sprint_4/tutoriais/tutorial-kaue-python-deploy-facilitacao.md` (novo, A44) |
| Frontend: módulos, guarda, aceite real, Telas 10/16, 11/17, 12, 03, 13, 14; GitHub Pages | US1–US7, US9, US11, US12, US14, US17, US18, US20 | Leonardo (+ Lucas na Tela 14, se preciso) | `docs/Sprint_4/tutoriais/tutorial-leonardo-frontend.md` (novo, A44) |
| **US26** (condicional, A40) | US26 | **Diogo** (Kauê aprova o contrato; Lucas revisa os PRs; A43) | Tutorial do Diogo (`docs/Sprint_4/tutoriais/tutorial-diogo-banco.md`), Parte 10 |
| Plano de Testes, evidências, relatórios das Sprints 3 e 4, retrospectiva, CT20 | Processo | Kauê (facilita) e todos | Tutorial do Kauê, Partes 9 e 10 |

## 4. Calendário e marcos

**Premissas:**
- O grupo trabalha no fim de semana.
- **Seg 02/11 é feriado (Finados), e o grupo trabalha** (A43). Sem aula, ele é o **dia de mais carga** da sprint.
- Cada fase só conta com o **PR revisado na `main`** e a evidência em `evidencias/`.

| Dia | Kauê | Diogo | Lucas | Leonardo | Marco |
|---|---|---|---|---|---|
| **Qui 29/10** | Fase P (copiar, testar, `requirements.txt` em UTF-8); **encaminhar ao professor o e-mail da Gurobi** (A38) | **PR do banco** (DDL + 41 testes); repositórios de usuário, unidade, ingrediente e produto; **enviar a mensagem da Sprint 4** ao professor (`sprint-4-mensagem-professor.md`), depois da revisão do Lucas | **PR da Fase 1** (18/18); revisar a mensagem ao professor | Baseline (22 OK) e retratos; divisão do `app.js` (PR); logo depois, a correção do teste instável (A13, PR pequeno; A45) | **M1 (noite):** PR do banco e Fase 1 na `main` |
| **Sex 30/10** | **PR da Fase P + Q** (`47 passed`); 2ª máquina; PR do `.env.example` (G8) | Repositórios de movimentação, pedido, reposição e dashboard prontos para entrar por commit na branch de cada fase, quando ela sair (A44); `seed_demo.sql`; `sql_repositorios_sprint3.md` | **PR da Fase 2** (15 + 13) → avisar o Leonardo | Guarda de rotas (PR); `config.js` e *workflow* do Pages (com o Kauê); Telas de Estoque e Pedido com a API simulada | **M2 (noite):** Fase 2 e Fase P + Q na `main` |
| **Sáb 31/10** | Fase 5 (reposição em Java): começar; **PR do deploy** (Parte 8.1 do tutorial: `Dockerfile`, `.dockerignore` e `render.yaml` na raiz) | Revisar PRs; A26 nas evidências | **PR da Fase 3** (15/15); **criar a conta do Render** (só a conta, sem banco nem serviço; A43) | **Gate da Fase 2** (16 evidências, de manhã); à tarde, ligar Estoque e Pedido ao backend real assim que a Fase 3 entrar | **Fase 3 na `main` (noite)** |
| **Dom 01/11** | **Fase 4A em dupla** com o Lucas | Revisar PRs; casos do banco no Plano de Testes (conferir) | **PR da Fase 4A** (7 + Python real), em dupla | Estoque e Pedido com o backend real | **M3 (noite):** Fases 3 e 4A na `main`. **Decisão do botão "Descartar"** (A42) |
| **Seg 02/11** (feriado, **dia mais carregado**: sem aula; A43) | **PR da Fase 5** (8/8), **cedo**: a Fase 6 do Lucas depende dela (A44); à tarde, **deploy definitivo com o Lucas**, guiando pela Parte 8 do tutorial dele (banco e serviço criados neste dia); seed e **backup**; aprovar a seção da US26 no contrato | **US26 (se passar no *gate* da manhã):** seção no contrato e casos de teste (CT73+); à tarde, seed de demonstração no banco publicado, com o Kauê | **PR da Fase 6** (5 + 4 + 3), de manhã, rodando os testes depois de trazer a Fase 5 para a branch (A44); à tarde, **deploy definitivo na conta dele**, com o Kauê; à noite, suíte completa (95) | Tela 12 (Produção); botão "Descartar", se aprovado no M3; à tarde, começar a Tela 13, assim que a Fase 5 entrar | **Conferência da manhã** (PO): *gate* da US26 e licença |
| **Ter 03/11** | CT67 (imagem local), CT69, CT70 e CT72 no ambiente publicado | **US26:** backend (`POST`/`PATCH /api/unidades-medida` e testes de integração), PR revisado pelo Lucas | Revisar PRs (inclusive o backend da US26); Tela 14, se o Leonardo precisar | Telas 03, 13 (concluir) e 14 | **M4 (noite):** todas as fases e telas obrigatórias na `main` |
| **Qua 04/11** | CT20 (subir do zero só com o README, com um colega); CT68 completo na URL, **depois das 18h** | **US26:** Tela 19 e evidências; **na `main` até as 18h, ou sai** (o Lucas revisa a Tela 19) | Revisar a Tela 19; correções; evidências | Prazo final da A13, se ainda não entrou (A45); **aceite na URL publicada** (dois perfis), **depois das 18h** | **Congelamento às 18h:** depois disso, só correções. O aceite e o CT68 rodam sobre o que está na `main` nessa hora |
| **Qui 05/11** | Relatório da Sprint 4 e revisão do relatório da Sprint 3; **Retrospectiva** (Sprints 2, 3 e 4); ensaio da apresentação; backup | Evidências; contribuições individuais das Sprints 3 e 4 | Evidências; contribuições individuais das Sprints 3 e 4 | Evidências; contribuições individuais das Sprints 3 e 4 | Tudo pronto para a entrega (o Kauê também escreve as suas contribuições) |
| **Sex 06/11** | **Entrega das Sprints 3 e 4 e Review** com demonstração, nos dois perfis (A43) | | | | **Fim da Sprint 4** |
| **Depois de 06/11** | **Semana de bugs, até 13/11** (A43): bateria de testes no ambiente publicado (`sprint-3-casos-de-teste-propostos.md`, §3); fluxo registrar → teste que reproduz → `fix/...` → PR → retestar. **Sem histórias novas** | | | | |

**Por que essas datas** (decisão A41):
- **Uma fase por dia no caminho crítico do Lucas:** Fase 1 → 2 → 3 → 4A → 6. Os PRs do banco e da Fase P + Q saem antes de quem depende deles.
- **Telas na ordem em que o backend fica pronto:**
  - Estoque e Pedido depois da Fase 3;
  - Tela 12 depois da 4A;
  - Telas 03 e 14 depois da Fase 6;
  - Tela 13 depois da Fase 5.
- **Deploy definitivo na seg 02/11, e não mais no sáb 31:**
  - o banco gratuito vence 30 dias depois de criado: em 02/12, depois de 30/11 e da semana de bugs;
  - o Render fica 4 dias em uso antes da entrega;
  - o ensaio numa conta pessoal deixa de ser necessário;
  - o que entrar na `main` depois é publicado sozinho.
- **Qua 04/11 e qui 05/11 são a folga** para corrigir, juntar evidências e preparar a Review.

**Backups do banco publicado** (A32, datas pela A41):
- logo depois do seed (02/11);
- no fim da semana de bugs;
- uma vez por semana;
- em 29/11.

## 5. Conferências e cortes

**Decisão do botão "Descartar" no M3 (dom 01/11, à noite)**, feita pelo PO (Lucas): as Telas 10/16 e 11/17 funcionam com o backend real? Se **não**, o botão sai (o backend dele fica). Se **sim**, o Leonardo o faz na seg 02/11, junto com a Tela 12.

**Conferência de seg 02/11, de manhã**, feita pelo PO, com o estado do M3 (dom à noite):
- ***Gate* da US26** (A40): só entra se, ao mesmo tempo:
  - as Fases 1, 2, 3, 4A e P + Q estão na `main`;
  - o *gate* da Fase 2 foi aprovado;
  - o Diogo, que faz a US26 (A43), está com as entregas dele em dia.

  **Quem faz o quê na US26 (A43):** o **Diogo** faz tudo (contrato, casos de teste, backend e Tela 19), porque é quem tem mais folga depois de sex 30/10 e já é dono dos repositórios. O **Kauê** aprova a seção do contrato (é o dono do contrato) e o **Lucas** revisa os dois PRs. O **Leonardo não participa**: as seis telas obrigatórias já o ocupam até qua 04/11.

  Ela tem de estar **na `main`, com os testes, até qua 04/11 às 18h**; se não estiver, sai sem afetar o resto.
- **Licença:** solver ligado no Render (`OTIMIZADOR_HABILITADO=true`, sem `GRB_*`; A38). A reserva B1 só se o solver falhar lá.

**Revisão no M4 (ter 03/11, noite):** se alguma tela obrigatória não estiver na `main`, corte nesta ordem:
1. botão "Descartar";
2. filtros do ranking na tela;
3. ranking por usuário na Tela 03;
4. acabamento visual.

**Não cortar:**
- o bloco "Plano de produção" do dashboard (é a "receita máxima estimada" do Termo);
- o detalhe por linha da Tela 10 (mostra "Vence em breve" e "Vencida", a cobertura do adendo, item 4).

**Nenhuma história Must sai.** Se mesmo assim não couber, o PO fala com o professor **antes** de 06/11. A semana de bugs não serve para desenvolver.

## 6. Entrega da Sprint 3 (no nome dela)

O professor ainda não recebeu **nenhuma entrega formal** da Sprint 3 (recebeu mensagens e assinou o adendo). Ela vai **junto com a entrega da Sprint 4, na sex 06/11** (A43), no padrão da Sprint 2:

| Item | Situação | Quem |
|---|---|---|
| Relatório de entrega (`docs/Sprint_3/sprint-3-relatorio-entrega.md`) | **Rascunho pronto** (28/10), só com fatos verificáveis | Kauê revisa até qui 05/11; o grupo aprova |
| Contribuição individual da Sprint 3 | A fazer, uma por integrante, no padrão de `docs/Sprint_2/sprint-2-contribuicao-*.md`, **até qui 05/11** (junto com a da Sprint 4) | Cada um |
| Retrospectiva da Sprint 3 | Feita **junto com a da Sprint 4, na qui 05/11** | Kauê facilita; todos |
| Artefatos já prontos | Roteiro, decisões, Contrato v2, adendo aprovado, código de referência com testes, CT30–CT72 no Plano de Testes, documentos atualizados | — |

A entrega das duas sprints é feita pelo Kauê (facilitação), em 06/11.

## 7. Critérios de pronto em 06/11

- [ ] **Sistema publicado**: frontend no GitHub Pages e backend no Render, por HTTPS, URL no README, com a data de expiração do banco (US20; CT67, CT69, CT70 e CT72).
- [ ] Login pelos dois perfis com o backend real; `401` e `403` em JSON.
- [ ] Cadastros de US4–US7 com erros por campo vindos do backend real.
- [ ] Estoque com validade; saída convencional e descarte; vencido nunca vendido; "Vence em breve" e "Vencida" no detalhe por linha. Botão "Descartar", se não foi cortado.
- [ ] Pedido com baixa por linhas, atômico.
- [ ] Tela 12 = 151/1/0 com o seed de demonstração, **na URL publicada**, sem termos técnicos.
- [ ] Reposição na Tela 13 (US14); dashboard do mês na Tela 03 (US17); sugestões na Tela 14 (US18).
- [ ] CT68 completo na URL publicada (qua 04/11).
- [ ] Testes:
  - banco `32 | 0` e `41 | 0`;
  - Python `47 passed` em 2 máquinas;
  - backend `mvnw test` verde, com Testcontainers;
  - funções de apoio 15/15;
  - Playwright estável ou com a instabilidade registrada (A13).
- [ ] Um PR revisado por fase, na `main`; evidências em `docs/Sprint_4/evidencias/`.
- [ ] Relatórios das Sprints 3 e 4, retrospectiva (Sprints 2, 3 e 4) e contribuições individuais das duas sprints, entregues juntos (A43).
- [ ] US26 na `main` com os testes **ou** registrada como fora do semestre (A40).

## 8. Riscos

| Risco | Prob. / Impacto | Mitigação |
|---|---|---|
| **Primeira integração real de todas as fases** acontece agora; nada chegou à `main` até 28/10 | Alta / Alto | Código de referência testado; um PR por dia no caminho crítico; folga na qua 04 e na qui 05 |
| Docker não conferido nas máquinas do Lucas, do Kauê e do Leonardo (sem ele, os testes com Testcontainers não rodam) | Média / Alto | Conferir na qui 29, antes do primeiro PR; contorno da A26 (nunca "Reset to factory defaults") |
| **Leonardo com seis telas**, concentradas na ter 03/11 | Alta / Alto | Funções de apoio prontas; Lucas assume a Tela 14 se preciso; ordem de corte do §5 |
| **Seg 02/11 sobrecarregada** (Fases 5 e 6, deploy, Tela 12, início da Tela 13 e da US26) | Média / Alto | O grupo confirmou que trabalha no feriado, sem aula (A43). Se algo do dia escorregar, a ordem é: Fases 5 e 6 e deploy primeiro; a US26 é a primeira a parar |
| Render free: o serviço dorme após 15 min; 0,1 CPU | Certa / Médio | `config.js` com 90 s; acordar o serviço antes de cada sessão |
| O solver não caber no Render free | Baixa / Médio | Imagem testada com 512 MB e 0,1 CPU; reserva B1 no painel; **nunca** a WLS no Render |
| Teste de navegador instável (A13) | Média / Médio | Causa achada em 30/10; correção de uma linha logo depois da divisão do `app.js` (A45), prazo final qua 04/11 |
| A US26 atrapalhar o obrigatório | Baixa / Médio | *Gate* da seg 02/11; feita só pelo Diogo, fora do caminho crítico; corte automático na qua 04/11 às 18h |
