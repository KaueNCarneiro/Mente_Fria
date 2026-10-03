# Relatório Individual de Contribuição — Sprint 2 — Leonardo Fagundes Oliveira (RA 2840482421009)

**Papel nesta sprint:** Responsável por Frontend

## 1. O que fiz

| Item | PR/commit | Status |
|---|---|---|
| Organizei o frontend no repositório recriado, separando HTML, CSS, módulos JavaScript e recursos visuais. | PR #2 | Feito |
| Desenvolvi as telas de cadastro do administrador, login, funcionários, ingredientes, produtos com composição e alteração de preço, seguindo o Figma e os documentos do projeto. | PR #2 | Telas implementadas; integração depende das rotas do backend |
| Preparei a comunicação HTTP com o Java e a tela de teste de saúde da API, ingredientes e comunicação com Python. | PR #2 | Frontend preparado; integração ponta a ponta pendente |
| Implementei sessão com JWT, menus por perfil, tratamento de erros e validações de formulários, porções e composição dos produtos. | PR #2 | Verificado com API simulada |
| Preparei testes com Playwright, conferi telas desktop/mobile e documentei a execução local. | PR #2 | 22 verificações passaram com API simulada |
| Centralizei no `.gitignore` da raiz as regras de dependências e capturas dos testes. | 4cc0c7a | Feito |

## 2. Rituais que participei

- [X] Dailies/weeklies (2 de 2)
- [ ] Sprint Review
- [X] Retrospectiva

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
|---|---|---|
| [PR #1 — Comunicacao python](https://github.com/KaueNCarneiro/Mente_Fria/pull/1) | Kauê Nogueira Carneiro | Revisei o PR com a comunicação Python, configuração do Gurobi e ajustes no `.gitignore`, e realizei o merge em 01/10/2026 (commit `56b0e2e`). A revisão não foi registrada pela ferramenta de Review do GitHub. |

## 4. Dificuldades e o que aprendi

A principal dificuldade foi preparar os fluxos enquanto parte dos endpoints do backend ainda não estava disponível. Segui o contrato e usei respostas simuladas para verificar a interface. Aprendi mais sobre `fetch`, JSON, JWT, CORS e tratamento de erros. A integração com a API e o banco reais ainda precisa ser validada.

Também adaptei as telas do Figma às regras de porções e composição dos produtos. Aprofundei meus conhecimentos em validação de formulários, responsividade, testes com Playwright e organização do Git. Entendi que o bloqueio de ações na interface precisa ser acompanhado de autorização no backend.
