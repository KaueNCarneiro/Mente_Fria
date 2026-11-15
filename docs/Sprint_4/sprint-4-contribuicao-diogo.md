# Relatório Individual de Contribuição — Sprint 4 — Diogo Pereira Miranda (RA 2840482423006)

**Papel nesta sprint:** Responsável por dados (Banco de Dados e repositórios), pela US26 e, desde a decisão A47, pela publicação no Render

## 1. O que fiz

| Item | PR/commit | Status |
|---|---|---|
| Banco da Sprint 4: o patch do DDL da Sprint 3 aplicado ao `bd/Script_DDL.sql` e os testes de constraint T33 a T73 (`bd/constraints_sprint3_teste.sql`), com a prova de que cada teste falha quando a constraint é removida | commit `6c07b19`, PR #44; evidência `2026-10-29_diogo_banco_quebrar-de-proposito.txt` | Feito |
| Correção dos cabeçalhos do `constraints_teste.sql` e da evidência de 29/10 | commits `02e03f7` e `0fec294`, PR #47 | Feito |
| Seed de demonstração para a Review (`bd/seed_demo.sql`), com datas relativas ao dia e sem criar usuários | commit `c18e471`, PR #48; evidência `2026-11-03_diogo_seed-demo.txt` | Feito |
| SQL dos repositórios da Sprint 3 documentado (`docs/Apoio/sql_repositorios_sprint3.md`) | commit `fb51b66`, PR #49 | Feito |
| Repositórios das Fases 2 a 6 (`NamedParameterJdbcTemplate`): sem eles, o backend da `main` não compilava. Suíte com 95 testes, 0 falhas | commit `f218157`, PRs #86 e #87; evidência `2026-11-11_diogo_repositorios_mvnw-test.txt` | Feito (11/11) |
| US26, contrato e casos de teste: seção 2.11 do Contrato de Comunicação, CT73 a CT78 e o registro da decisão A46 | commit `6b13a06`, PRs #76 e #77 (aprovação do Kauê) | Feito (10/11) |
| US26, SQL à mão num PostgreSQL temporário | commit `918e4f2`, PR #79; evidência `2026-11-10_diogo_us26-sql-a-mao.txt` | Feito (10/11) |
| US26, backend: `POST` e `PATCH /api/unidades-medida`, só para o Administrador, com a unidade normalizada e testes de integração. Suíte com 101 testes, 0 falhas | commits `731ea13` e `20e6300`, PRs #88 e #93; evidência `2026-11-11_diogo_us26-backend_mvnw-test.txt` | Feito (12/11) |
| US26, Tela 19 (Porção Padrão): lista, cadastro e alteração da porção padrão, com 4 verificações novas no teste do navegador (43 no total) | commit `4c8d875`, PRs #96 e #97; evidência `2026-11-12_diogo_us26-tela19_npm-test.txt` | Feito (12/11) |
| Deploy no Render, na minha conta (A47): serviço e banco pelo `render.yaml`, schema, Administrador e Funcionário, seed, backup e CORS conferido (CT69) | commits `63e698e`, `7d25bd6` e `726f1c5`, PR #92; evidências `2026-11-11_diogo_deploy-render.txt` e `2026-11-11_diogo_seed-render.txt` | Feito (11/11) |
| Segurança do banco publicado: acesso externo restrito ao IP da minha rede | commit `ffdcbf6`, PR #94 | Feito (12/11) |
| Deploy automático: liguei o *Auto-Deploy* no Render depois de o Kauê instalar o app no repositório, e conferi que os merges na `main` passaram a publicar sozinhos | commits `ea558ef` e `fb64b08`, PRs #105 e #110; evidência `2026-11-15_diogo_deploy-automatico-render.txt` | Feito (15/11) |
| Backups do banco publicado, com a restauração testada num banco descartável: o do fim da semana de bugs e o primeiro semanal | commits `9139235` e `d25f9fa`, PRs #100 e #117; evidências `2026-11-14_diogo_backup-render.txt` e `2026-11-15_diogo_backup-render.txt` | Feito (14/11 e 15/11) |
| Correções da semana de bugs: o "null" nas telas de Produção e de Reposição, o texto "Falta ingrediente" com quantidade 0 (regra D9 do contrato) e o selo "S2" no item Clientes do menu; cada uma com uma verificação nova no teste do navegador, que falhava antes da correção | commits `34ab9e7`, `05c5583` e `5488ff1`, PRs #103, #107 e #113; evidências `2026-11-15_diogo_fix-null-producao-reposicao_npm-test.txt`, `_texto-quantidade-zero-producao_npm-test.txt` e `_menu-clientes-selo_npm-test.txt` | Feito (15/11) |
| Exclusão, no banco publicado, do ingrediente "Granola" em `mg` que sobrou de um teste, e conferência das correções na URL publicada | commit `71e14ce`, PR #112; evidência `2026-11-15_diogo_semana-bugs_conferencia-url-publicada.txt` | Feito (15/11) |
| Documentação da Sprint 4: roteiro e decisões (A39 em diante), tutoriais reescritos como material de apoio (A44, a meu pedido), aviso da A47 no tutorial do Lucas e a tabela de pendências | PRs #42, #45, #98 e #101 | Feito |
| Publicação no GitHub e conferência, na minha máquina, da parte técnica do Kauê (Fase P + Q, arquivos do deploy, `.env.example` único, modelo matemático, registro do histórico do Git e evidências dos CT68, CT69, CT70 e CT72) | PRs #81, #82, #83, #84, #90 e #102 | Feito |
| Integração da `sprint-4` na `main` | PRs #43, #46, #75, #78, #80, #87, #89, #91, #93, #95, #97, #99, #104, #106, #111 e #116 | Feito |
| Backups seguintes (semanal e 29/11) e contribuições no encerramento | — | Em andamento: o banco gratuito vence em 11/12/2026 |

## 2. Rituais que participei

- [X] Dailies/weeklies
- [ ] Sprint Review (06/11)
- [ ] Retrospectiva (das Sprints 2, 3 e 4, no encerramento)

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
|---|---|---|
| #52 — `feature/sprint-4-seguranca-jwt` | Lucas-Bassi-0 | Aprovado em 05/11: Fase 1 do backend (Spring Security, JWT, CORS e erros em JSON) |
| #64 a #74 — frontend da Sprint 4 | Leoxd1 | Revisão da Parte 8.1 do meu tutorial, aprovada em 11/11: conferi que cada PR não tinha conflito com a `sprint-4` e que a evidência batia. Deixei duas observações sem bloquear: no #66, a guarda liberava `clientes` para o Funcionário, mas a US13 está fora do semestre (atendida pelo Leonardo no commit `6c81b36`); no #67, a URL do Render no `config.js` ainda era provisória antes de a `sprint-4` ir para a `main` |

## 4. Dificuldades e o que aprendi

A maior dificuldade da Sprint 4 foi a dependência entre as partes. Os meus repositórios dependiam das fases do backend, e o código do Lucas entrou na `main` sem eles: em 11/11, o backend da `main` não compilava até o PR #86. A US26, que era minha, não passou no *gate* de 02/11 porque as fases que ela usa não estavam prontas, e só continuou porque o PO a manteve em aberto, com prazo (A46). O deploy, que era do Lucas, passou para mim em 11/11 (A47), depois da entrega de 06/11.

No deploy, aprendi que uma senha nunca entra num comando: ela entra pelo `Read-Host`, e o histórico do terminal precisa ser limpo se isso falhar. Para proteger o banco publicado, restringi o acesso externo ao IP da minha rede, e aprendi que, com isso, o IP da minha rede precisa ser conferido antes de cada backup (em 14/11, a primeira tentativa foi recusada porque ele tinha mudado). Também aprendi que um backup só vale com a restauração testada.

Nos testes, aprendi a escrever a verificação antes da correção e a vê-la falhar primeiro: assim ficou provado que o "null", o texto da quantidade 0 e o selo do menu estavam cobertos. Aprendi também que a API simulada não pega tudo: o "null" só apareceu no ambiente publicado. A lição para os próximos projetos é publicar e testar no ambiente real cedo, e combinar a ordem das entregas para uma parte não esperar a outra no fim.
