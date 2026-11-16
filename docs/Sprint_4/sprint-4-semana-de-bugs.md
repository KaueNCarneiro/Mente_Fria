# Semana de bugs — Sprint 4 — Mente Fria

> **Montada em 15/11/2026** a partir das evidências já registradas em `docs/Sprint_4/evidencias/`. **Não é uma execução nova:** cada linha aponta para o arquivo onde o resultado está. O que não tem registro aparece como **não executado**.

- **Período (A43):** depois da Review de 06/11 até 13/11. A bateria a executar é a do §3 de `docs/Sprint_3/sprint-3-casos-de-teste-propostos.md`, no ambiente publicado.
- **Atraso:** o sistema só foi publicado em 11/11 (A47). Por isso, os testes no ambiente publicado ficaram para 14 e 15/11, depois do fim previsto.
- **Fluxo de cada defeito:** registrar → caso que reproduz → `fix/...` → PR → retestar.
- **Ambiente publicado:** frontend em `https://kauencarneiro.github.io/Mente_Fria/` (GitHub Pages) e backend em `https://mente-fria-backend.onrender.com` (Render).
- **Banco gratuito do Render (A21):** criado em 11/11/2026, às 21h59. **Vence em 11/12/2026, às 21h59.**

## 1. Bateria do §3 × o que foi executado no ambiente publicado

A ordem do §3 é:
1. os caminhos do Termo de Aceite no ambiente publicado: CT68, CT06, CT12, CT30, CT38 e CT58;
2. os casos de erro;
3. por último, o CT72.

O CT42, o CT51 e o CT64 também rodam no Render (A38).

| Caso | O que pede | Executado? | Resultado | Evidência |
|---|---|---|---|---|
| **CT68** | Fluxo do Termo pela URL do Pages, nos dois perfis | **Sim, em 14/11** | **Passou** nos dois perfis: login, entrada de estoque, recomendação, pedido, baixa, reposição, dashboard e marketing. O cadastro não foi refeito, porque o Administrador e o Funcionário já existiam desde 11/11. Achou 3 defeitos (seção 2) | `2026-11-14_kaue_ct68.txt` |
| CT06 | Login válido por perfil | Sim, dentro do CT68 | **Passou:** cada perfil entrou na sua área. O Funcionário vê só Início, Ingredientes, Produtos, Estoque e Pedidos | `2026-11-14_kaue_ct68.txt` |
| CT12 | Cadastro de produto com composição | **Não** no ambiente publicado | — | — |
| CT30 | Entrada cria uma linha | Sim, pela tela, dentro do CT68 | **Passou pela tela:** entrada de 1 kg de morango, "Movimentação registrada.", estoque de 1,98 para 2,98 kg. O `201` e a linha no banco não foram conferidos | `2026-11-14_kaue_ct68.txt` |
| CT38 | O pedido grava o preço e baixa os ingredientes pela composição | Sim, dentro do CT68 | **Passou:** pedidos #9 (Administrador) e #10 (Funcionário), R$ 12,90 cada. A baixa confere com a composição do Tradicional: açaí −0,300 kg, leite condensado −0,030 L, 1 copo | `2026-11-14_kaue_ct68.txt` |
| CT58 | Números do mês só com pedidos ativos | Em parte | A tela abriu com números: R$ 358,20 em 5 pedidos. A regra "só pedidos ativos" e os valores do cenário de referência **não** foram conferidos no ambiente publicado | `2026-11-14_kaue_ct68.txt` |
| CT42 | Recomendação com o estoque de demonstração | Sim, com outro estoque | **150/0/0** em vez de 151/1/0, porque o banco publicado já não tinha o estoque do seed (45 kg de açaí válido). Conta conferida: 150 × 6,64 = 996,00 | `2026-11-14_kaue_ct68.txt` |
| CT51 | Orçamento folgado compra toda a necessidade | Sim, com outros parâmetros | Com 60 dias e R$ 100, deu "O orçamento cobre tudo o que falta: R$ 11,10" (só morango). Os valores do cenário de referência (30 dias, R$ 63,22) não se aplicam ao banco publicado | `2026-11-14_kaue_ct68.txt` |
| CT64 | Sugestões a partir da última recomendação | Sim, com outro estoque | 1 sugestão (Tradicional), coerente com a recomendação 150/0/0. O caso espera 2 sugestões porque parte de 151/1/0 | `2026-11-14_kaue_ct68.txt` |
| CT69 | CORS do GitHub Pages | **Sim, em 11/11 e em 14/11** | **Passou:** a origem do Pages recebe `200` com `Access-Control-Allow-Origin`, outra origem recebe `403` | `2026-11-11_diogo_deploy-render.txt`, `2026-11-14_kaue_ct69.txt` |
| CT70 | Primeira chamada depois de o serviço "dormir" | **Sim, em 14/11** | **Passou:** depois de 17 min sem acesso, a tela esperou 51,4 s (dentro dos 90 s) e funcionou | `2026-11-14_kaue_ct70.txt` |
| CT72 | Otimização desligada no servidor (reserva B1) | **Sim, em 14/11** | **Passou:** com a chave desligada, as Telas 03, 12, 13 e 14 mostram o aviso e o resto funciona; com a chave religada, a recomendação volta. O `503` em si não foi conferido pela tela | `2026-11-14_kaue_ct72.txt` |
| Casos de erro | O §3 manda rodá-los depois dos caminhos do Termo | Em parte | Só a guarda de rotas do Funcionário: as 6 rotas do Administrador voltam para o início com o aviso. Os outros casos de erro **não** foram executados no ambiente publicado; estão nos testes automatizados (seção 3) | `2026-11-14_kaue_ct68.txt` |
| CT67 | A imagem sobe dentro dos limites do plano gratuito (antes do deploy) | **Sim, em 11/11**, na máquina, com 512 MB e 0,1 CPU | **Passou:** build e subida ok, 401, 201, seed, 151/1/0 com lucro 1011,71 e reposição de 0,74 de morango. Nenhuma linha `ERROR` no log | `2026-11-11_kaue_ct67.txt` |

**Quem executou:** os casos publicados (CT68, CT69, CT70 e CT72) e o CT67 foram executados pelo Diogo, a pedido do Kauê, como registram as evidências. A divisão "dois testam como Administrador e dois como Funcionário" (§3) não aconteceu: os dois perfis foram testados na mesma sessão.

## 2. Defeitos encontrados e o que foi feito

| # | Defeito | Achado em | Correção | Reteste | Situação |
|---|---|---|---|---|---|
| 1 | A palavra "null" aparece nas telas de Produção e de Reposição quando não há aviso geral ou itens | CT68, 14/11 | PR #103 (`fix/frontend-producao-null`): o código deixa de passar `null` para a tela. O teste ganhou uma verificação para isso (`semNull`). 43 verificações, 3 de 3 rodadas | Revisão do Leonardo: 3 de 3 rodadas. Levado à `main` pelo #111 e conferido na URL publicada em 15/11 | **Resolvido** |
| 2 | Um segundo ingrediente "Granola", em `mg`, com estoque 0, "Abaixo do mínimo" (resto de teste no banco publicado) | CT68, 14/11 | Dado, não código. O Diogo apagou o ingrediente (id 8, sem produtos nem movimentações) em 15/11 | Telas conferidas: 7 ingredientes e nenhum abaixo do mínimo. O backup de 15/11 tem 7 ingredientes | **Resolvido** |
| 3 | Com quantidade 0, aparece "Falta ingrediente" mesmo quando o motivo é o lucro | CT68, 14/11 | PR #107: o texto passa a ser "O estoque não dá ou rende mais em outro copo". Contrato (regra D9), Plano de Testes e tutorial do Leonardo foram atualizados. Antes da correção, o teste falhou (exit 1); depois, 43 verificações, 3 de 3 rodadas | Revisão do Leonardo: 3 de 3 rodadas. Levado à `main` pelo #111 e conferido na URL publicada em 15/11 | **Resolvido** |
| 4 | O item Clientes (US13, fora do semestre) aparece no menu com o selo "S2" e a dica "Disponível na Sprint 2", o que parece uma entrega atrasada | 15/11 | PR #113: o selo passa a "Futuro", com a dica "Fora deste semestre". 44 verificações, 3 de 3 rodadas | Revisão do Leonardo: 3 de 3 rodadas. Levado à `main` pelo #116 (a conferência na URL publicada não tem evidência própria) | **Resolvido** |

Evidências:
- defeito 1: `2026-11-15_diogo_fix-null-producao-reposicao_npm-test.txt` e `2026-11-15_leonardo_revisao-prs-103-107_npm-test.txt`;
- defeito 2: `2026-11-15_diogo_backup-render.txt`;
- defeito 3: `2026-11-15_diogo_texto-quantidade-zero-producao_npm-test.txt`;
- conferência dos defeitos 1 e 3 na URL publicada: `2026-11-15_diogo_semana-bugs_conferencia-url-publicada.txt`;
- defeito 4: `2026-11-15_diogo_menu-clientes-selo_npm-test.txt` e `2026-11-15_leonardo_revisao-pr-113_npm-test.txt`.

**Achado do ambiente de teste (não é defeito do sistema):** na máquina do Diogo, o `python -m http.server` deixou o `npm test` instável. As falhas eram do tipo "a página não terminou de carregar" e aconteciam também na `main`, sem a Tela 19. Com um servidor estático em Node, 10 de 10 rodadas passaram. O `frontend/README.md` registra o aviso (`2026-11-12_diogo_us26-tela19_npm-test.txt`).

## 3. Testes automatizados rodados no período

| Data | O quê | Resultado | Evidência |
|---|---|---|---|
| 11/11 | Suíte completa do backend, com os repositórios das Fases 2 a 6 | 95 testes, 0 falhas | `2026-11-11_diogo_repositorios_mvnw-test.txt` |
| 11/11 | Fase 5 (reposição): `ReposicaoTest` com o Python dublê e `PythonRealTest` com o Python real (gurobipy 13.0.3) | 8 e 3 testes, 0 falhas | `2026-11-11_kaue_fase5_mvnw-test.txt`, `2026-11-11_kaue_fase5_python-real.txt` |
| 11/11 | Backend da US26 (CT73 a CT78): primeiro os testes falhando (4 de 6), depois a suíte completa | 101 testes, 0 falhas | `2026-11-11_diogo_us26-backend_mvnw-test.txt` |
| 12/11 | Tela 19 da US26 (`npm test`, API simulada) | 43 verificações, 10 de 10 rodadas com servidor em Node | `2026-11-12_diogo_us26-tela19_npm-test.txt` |
| 15/11 | Correções dos defeitos 1, 3 e 4 (`npm test`), na máquina do Diogo e na revisão do Leonardo | 43, 43 e 44 verificações; 3 de 3 rodadas em cada | ver a seção 2 |

## 4. Operação do ambiente publicado no período

| Data | O quê | Resultado | Evidência |
|---|---|---|---|
| 11/11 | Deploy no Render, na conta do Diogo (A47), e carga do seed | No ar; CT69 passou; seed carregado | `2026-11-11_diogo_deploy-render.txt`, `2026-11-11_diogo_seed-render.txt` |
| 14/11 | Backup do fim da semana de bugs | 23.574 bytes; restauração testada num banco descartável, 8 pedidos dos dois lados | `2026-11-14_diogo_backup-render.txt` |
| 15/11 | Deploy automático ligado e conferido em merges reais (#106 e #111) | Publicado sozinho, com o gatilho *Auto-Deploy*; `/api/saude` ok | `2026-11-15_diogo_deploy-automatico-render.txt` |
| 15/11 | Backup semanal | 23.689 bytes; restauração testada, 10 pedidos e 7 ingredientes | `2026-11-15_diogo_backup-render.txt` |
| 15/11 | Conferência sem login do frontend publicado (Leonardo) | Pages `200`; `config.js` aponta para o Render com 90 s; CORS liberado. **Não é o aceite** | `2026-11-15_leonardo_conferencia-ambiente-publicado.txt` |

## 5. O que não foi feito (anotar no relatório da Sprint 4)

- **CT12** (cadastro de produto com composição) não foi executado no ambiente publicado.
- **Casos de erro:** no ambiente publicado, só a guarda de rotas do Funcionário foi executada.
- **Valores do cenário de referência:** o CT42, o CT51, o CT58 e o CT64 não foram conferidos no banco publicado, porque ele já não tinha o estoque nem os pedidos do seed. Esses valores estão cobertos pelos testes automatizados com o banco de teste (seção 3).
- **Divisão por perfil entre os quatro integrantes:** não aconteceu.
- **Aceite do Leonardo na URL publicada, com prints** (tutorial dele, Parte 16): **feito em 15/11, depois desta tabela** (`2026-11-15_leonardo_aceite-pages.txt`, PR #127). Não cobriu cadastros, "Descartar" e saída de estoque.
- **Atraso:** os testes no ambiente publicado ocorreram em 14 e 15/11, depois do fim previsto da semana (13/11), porque o deploy só aconteceu em 11/11.
