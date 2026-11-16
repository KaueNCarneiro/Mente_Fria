# Relatório Individual de Contribuição — Sprint 4 — Leonardo Fagundes Oliveira (RA 2840482421009)

**Papel nesta sprint:** Responsável por Frontend

## 1. O que fiz

| Item | PR/commit | Status |
|---|---|---|
| Divisão do `app.js` (235 linhas) em módulos (`state`, `nav`, `ui`, `router` e um arquivo por tela em `js/screens/`), sem mudar o comportamento: o retrato das 25 telas ficou igual antes e depois | PR #64, commit `4c46a70`; evidências `2026-11-06_leonardo_baseline_npm-test.txt` e `2026-11-06_leonardo_modulos_iguais.txt` | Feito |
| Correção do teste de navegador instável (A13, antecipada pela A45): a verificação 21 contava os campos antes de o `<dialog>` terminar de fechar. De 0 em 10 rodadas para 10 em 10 | PR #65, commit `8bc2ff6`; evidência `2026-11-06_leonardo_a13_depois.txt` | Feito |
| Guarda de rotas por lista de permissões do Funcionário (`rotaPermitida`, em `js/nav.js`), no lugar da regra antiga, que bloqueava `#/pedidos/novo` e liberava `#/dashboard` e `#/producao`. Depois, retirada das rotas de clientes da lista (US13 fora do semestre) | PR #66, commits `327865b` e `6c81b36`; evidências `2026-11-06_leonardo_guarda-antes.txt` e `_guarda-depois.txt` | Feito |
| Configuração de produção e publicação (US20, parte do frontend): `config.js` escolhe o backend pelo endereço da página (Render com tempo limite de 90 s no `*.github.io`; local com 15 s) e o *workflow* `frontend-pages.yml` publica a pasta `frontend/` no GitHub Pages | PR #67, commit `57bef10` | Feito; publicado em `https://kauencarneiro.github.io/Mente_Fria/` desde 11/11 |
| Telas 10/16: estoque, linhas de entrada com a situação da validade, movimentação e o botão "Descartar" (A12) (US9) | PR #68, commit `022ddd3`; evidência `2026-11-06_leonardo_estoque_npm-test.txt` | Feito |
| Telas 11/17: lista e registro de pedidos, com total somado em centavos e o `422` de estoque insuficiente como mensagem geral (US12) | PR #69, commit `4f6c0b0`; evidência `2026-11-06_leonardo_pedido_npm-test.txt` | Feito |
| Tela 12: recomendação de produção, sem termos técnicos e com o `503` da reserva B1 como aviso (US11) | PR #70, commit `5f0f8c0`; evidência `2026-11-06_leonardo_producao_npm-test.txt` | Feito |
| Tela 03: dashboard com filtros (mês, usuário, tipos de produto e valor) e o bloco do plano de produção (US17) | PR #71, commit `f39fae5`; evidência `2026-11-06_leonardo_dashboard_npm-test.txt` | Feito |
| Tela 13: sugestão de compra de ingredientes, com o resumo nos quatro casos do orçamento (US14) | PR #72, commit `e899b69`; evidência `2026-11-06_leonardo_reposicao_npm-test.txt` | Feito |
| Tela 14: sugestões de marketing, até 3 cartões e nenhum botão de publicar (US18) | PR #73, commit `86104ff`; evidência `2026-11-06_leonardo_marketing_npm-test.txt` | Feito |
| `frontend/README.md` reescrito (estava na Sprint 1): como rodar, telas, rotas e perfis, regras comuns, estrutura de módulos, testes e publicação. Atualizado no encerramento com a URL publicada e a situação de 15/11 | PR #74, commit `c0ac169`; atualização no PR deste relatório | Feito |
| Conferência do frontend publicado, sem login (15/11): o Pages responde, o `config.js` publicado aponta para o Render com 90 s, o backend acordou em 46 s e o CORS libera a origem do Pages | Evidência `2026-11-15_leonardo_conferencia-ambiente-publicado.txt` | Feito |
| Aceite na URL publicada, nos dois perfis, com prints (tutorial, Parte 16), em 15/11: as Telas 12, 14, 13, 03, 10/16 e 19 como Administrador; estoque, pedidos e o bloqueio de uma rota do Administrador como Funcionário; uma entrada de estoque e um pedido gravados | Evidência `2026-11-15_leonardo_aceite-pages.txt` e 17 prints `2026-11-15_leonardo_aceite-pages_*.png` | Feito, depois da entrega de 06/11 e da semana de bugs |

Os commits são de 06/11, com a suíte em 39 verificações e a API simulada. Os PRs #64 a #74 entraram na `sprint-4` em 11/11 e chegaram à `main` no mesmo dia (PR #85).

## 2. Rituais que participei

- [X] Dailies/weeklies
- [ ] Sprint Review (06/11)
- [ ] Retrospectiva (das Sprints 2, 3 e 4, no encerramento)

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
|---|---|---|
| #103 — `fix/frontend-producao-null` | sixtxxn | Revisor pedido no PR (as telas são minhas); aprovado em 15/11. Conferi a correção do "null" nas telas de Produção e de Reposição: o `replaceChildren` escreve `null` como texto, e a lista passou a não receber o item quando não há aviso. Rodei o `npm test` na branch: 43 verificações, em 3 rodadas (evidência `2026-11-15_leonardo_revisao-prs-103-107_npm-test.txt`) |
| #107 — `fix/producao-texto-quantidade-zero` | sixtxxn | Acordo pedido no PR (muda a regra D9 do contrato); aprovado em 15/11. Concordei com a troca do texto "Falta ingrediente" na Tela 12 e conferi se a tela, o teste, o contrato, o CT43 e o tutorial ficaram com o mesmo texto. Rodei o `npm test` na branch: 43 verificações, em 3 rodadas (mesma evidência) |
| #113 — `fix/menu-clientes-selo` | sixtxxn | Revisor pedido no PR; aprovado em 15/11. Conferi a troca do selo "S2" por "Futuro" no item Clientes do menu (US13, fora do semestre): o item continua apagado e sem link, e o menu do Funcionário não muda. Rodei o `npm test` na branch: 44 verificações, em 3 rodadas (evidência `2026-11-15_leonardo_revisao-pr-113_npm-test.txt`) |

Não há outra revisão minha registrada no GitHub nas Sprints 3 e 4.

## 4. Dificuldades e o que aprendi

A maior dificuldade foi ligar o frontend ao backend pelas APIs. As Fases 2 a 6 do backend só entraram na `main` em 11/11, depois das minhas telas, então escrevi as seis telas contra a API simulada dos testes, que intercepta as chamadas a `http://localhost:8080` e responde no formato do Contrato de Comunicação. Isso exigiu seguir o contrato campo a campo: nomes em `snake_case`, o `produto_id` repetido na URL do filtro do dashboard, o `usuario_id` fora do corpo (ele vem do token) e o total do pedido só como exibição, porque o backend recalcula com o preço do banco.

Aprendi que a tela precisa tratar cada resposta da API de um jeito: o `400` vira erro por campo, o `422` vira mensagem geral sem limpar o formulário, o `401` encerra a sessão, o `403` é a segurança de verdade (a guarda de rotas é só experiência de uso) e o `503` da recomendação desligada é aviso, não erro. Na publicação, aprendi que o CORS libera uma origem exata (`https://kauencarneiro.github.io`, sem caminho) e que o plano gratuito do Render "dorme": por isso o tempo limite em produção é de 90 s.

Também aprendi o limite da API simulada. O "null" da Tela 12 passou pelas 39 verificações e só apareceu no ambiente publicado (CT68), porque nenhuma verificação olhava o texto inteiro do resultado. Faltou o aceite com o backend real antes da entrega, que o roteiro previa e que dependia das fases do backend: ele só foi feito em 15/11, na URL publicada. No Git, abri os primeiros PRs com a base na `main` (#53 a #63) e precisei fechá-los e reabrir com a base na `sprint-4`. A lição para os próximos projetos é integrar com o backend real cedo, mesmo com poucas rotas prontas, e conferir a base do PR antes de abrir.
