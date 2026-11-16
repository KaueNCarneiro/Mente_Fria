# Relatório de Testes Executados — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039)
**Trilha:** B
**Data da bateria final:** 14 e 15/11/2026, no ambiente publicado, com as suítes automatizadas executadas de 11 a 15/11/2026 (detalhe na seção 2)
**Escopo testado:** funcionalidades do Termo de Aceite do Projeto (E2b, 28/08/2026) com as mudanças do Adendo nº 1 (27/10/2026, `docs/Sprint_3/sprint-3-adendo-termo-de-aceite.md`) e a US26, entregue em 12/11 (decisão A46)
**Sistema publicado:** frontend https://kauencarneiro.github.io/Mente_Fria/ · backend https://mente-fria-backend.onrender.com

> Este relatório consolida os casos do Plano de Testes (`docs/Plano_de_Testes_Mente_Fria.md`, CT01–CT78) e as evidências de `docs/Sprint_4/evidencias/`. Ele não é uma execução nova: cada caso aponta para o teste automatizado ou para a evidência onde o resultado está. O detalhe da semana de testes no ambiente publicado está em `docs/Sprint_4/sprint-4-semana-de-bugs.md`.

## 1. Resumo executivo

| Métrica | Valor |
|---|---|
| Total de casos de teste (Plano de Testes) | 78 (CT01–CT78) |
| Passaram | 78 |
| Falharam na bateria final | 0 (os 4 defeitos achados foram corrigidos e retestados antes do fim; seção 3) |
| Verificações automatizadas | 265: backend 101 testes · Python 47 · banco 32 + 41 · navegador 44 |
| Cobertura automatizada (linhas/branches) | Não medida. O Plano de Testes não fixou meta de cobertura e o projeto não usa ferramenta de cobertura |

**Como ler a coluna “Tipo”:** Integração = Java com PostgreSQL 16 real (Testcontainers); MockMvc = camada web do Spring; Banco = scripts de constraints em SQL; Python = testes do otimizador (pytest); E2E = navegador (Playwright, com a API simulada) ou o sistema publicado; Deploy = imagem Docker e Render; Manual = executado por uma pessoa.

## 2. Execuções que formam a bateria

| Data | Execução | Resultado | Evidência |
|---|---|---|---|
| 29/10 | Constraints do banco | 32 \| 0 e 41 \| 0; os testes detectam constraints erradas de propósito | `2026-10-29_diogo_banco_quebrar-de-proposito.txt` |
| 10/11 | Otimizador em Python (produção e reposição) | 47 passed | `2026-11-10_kaue_fasePQ_pytest.txt` |
| 11/11 | Backend completo, com a US26 | 101 testes, 0 falhas | `2026-11-11_diogo_us26-backend_mvnw-test.txt` |
| 11/11 | Python real chamado pelo Java (licença do `pip`) | 3 + 8 testes, 0 falhas | `2026-11-11_kaue_fase5_python-real.txt`, `2026-11-11_kaue_fase5_mvnw-test.txt` |
| 11/11 | Imagem de deploy com os limites do plano gratuito (CT67) | Passou | `2026-11-11_kaue_ct67.txt` |
| 12/11 | Navegador (Playwright), com a Tela 19 | 43 verificações, 10 de 10 rodadas | `2026-11-12_diogo_us26-tela19_npm-test.txt` |
| 14/11 | Ambiente publicado: CT68, CT69, CT70 e CT72 | Os quatro passaram; o CT68 achou 3 defeitos | `2026-11-14_kaue_ct68.txt` a `_ct72.txt` |
| 15/11 | Correções dos defeitos, com revisão | 43–44 verificações, 3 de 3 rodadas em cada | evidências `2026-11-15_*_npm-test.txt` |
| 15/11 | Conferência das correções na URL publicada | Passou | `2026-11-15_diogo_semana-bugs_conferencia-url-publicada.txt` |
| 15/11 | Aceite na URL publicada, dois perfis, 17 prints | Passou | `2026-11-15_leonardo_aceite-pages.txt` |
| 15/11 | CT20: setup do zero só com o README | Passou | `2026-11-15_diogo_ct20_setup-do-zero.txt` |

## 3. Casos de teste — visão consolidada

| ID | Área | Caso de teste | Tipo | Resultado | Observação / evidência |
|---|---|---|---|---|---|
| CT01 | US1 | Cadastro de Administrador com dados válidos | Integração | Passou | Fase2CrudTest |
| CT02 | US1 | Senha abaixo do mínimo | Integração + Banco | Passou | Fase2CrudTest; constraints_teste.sql |
| CT03 | US1 | E-mail já cadastrado | Integração + Banco | Passou | Fase2CrudTest; constraints_teste.sql |
| CT04 | US2 | Cadastro de funcionário válido pelo Administrador | Integração | Passou | Fase2CrudTest |
| CT05 | US2 | Funcionário tenta cadastrar/alterar preço de produto | Integração | Passou | Fase2CrudTest; SegurancaEErrosTest |
| CT06 | US3 | Login válido por perfil | Integração + E2E | Passou | Fase2CrudTest; CT68 no ambiente publicado (14/11) |
| CT07 | US3 | Login com senha incorreta | Integração | Passou | Fase2CrudTest |
| CT08 | US3 | Funcionário tenta acessar rota administrativa direto pela URL | Integração + E2E | Passou | Fase2CrudTest; SegurancaEErrosTest; guarda de rotas no CT68 e no aceite (15/11) |
| CT09 | US4 | Cadastro de ingrediente com dados válidos | Integração | Passou | Fase2CrudTest |
| CT10 | US4 | Custo unitário negativo | Integração + Banco | Passou | Fase2CrudTest; constraints_teste.sql |
| CT11 | US4 | Porção padrão igual a zero | Integração + Banco | Passou | Fase2CrudTest; constraints_teste.sql |
| CT12 | US5 | Cadastro de produto com composição válida, incluindo Açaí | Integração | Passou | Fase2CrudTest (não executado no ambiente publicado) |
| CT13 | US5 | Cadastro de produto sem nenhum ingrediente vinculado | Integração | Passou | Fase2CrudTest |
| CT14 | US5 | Quantidade utilizada zerada/negativa na composição | Banco | Passou | constraints_teste.sql |
| CT15 | US5 | Produto cadastrado com outros ingredientes, mas sem Açaí | Integração + Banco | Passou | Fase2CrudTest; constraints_teste.sql |
| CT16 | US6 | Atualização de preço por Administrador | Integração | Passou | Fase2CrudTest |
| CT17 | US6 | Preço igual a zero ou negativo | Integração + Banco | Passou | Fase2CrudTest; constraints_teste.sql |
| CT18 | US7 | Requisição direta à API, contornando a interface, sem campo obrigatório | Integração | Passou | Fase2CrudTest |
| CT19 | US7 | Formulário de cadastro com campo obrigatório vazio | E2E (navegador) | Passou | frontend/tests/browser.cjs: verificações de campos obrigatórios nos formulários |
| CT20 | US8 | Setup do zero a partir do README | Manual | Passou | Setup do zero só com o README, em 15/11 (`2026-11-15_diogo_ct20_setup-do-zero.txt`; numa máquina com as ferramentas já instaladas) |
| CT21 | US9 | Saída de estoque maior que o disponível | Integração + Banco | Passou | Fase3EstoquePedidoTest; constraints_sprint3_teste.sql |
| CT22 | US9 | Entrada válida de ingrediente | Integração + E2E | Passou | Fase3EstoquePedidoTest; entrada pela tela no CT68 e no aceite |
| CT23 | US10 | Um ingrediente escasso limita a produção de um único produto | Python | Passou | Coberto pelos testes do otimizador (`test_consumo_nunca_passa_do_estoque…`, oráculo em 200 instâncias), sem teste com o ID |
| CT24 | US10 | Dois produtos disputam o mesmo ingrediente escasso | Python | Passou | Coberto por `test_maior_receita_nao_e_maior_lucro` (dois produtos disputam o mesmo ingrediente) e pelo oráculo, sem teste com o ID |
| CT25 | US12 | Pedido com múltiplos itens, um deles sem estoque suficiente | Integração | Passou | Fase3EstoquePedidoTest |
| CT26 | US9 | Registrar movimentação com quantidade zero ou negativa | Integração + Banco | Passou | Fase3EstoquePedidoTest; constraints_sprint3_teste.sql |
| CT27 | US9 | Os dois perfis conseguem registrar movimentação de estoque | Integração + E2E | Passou | Fase3EstoquePedidoTest; os dois perfis no CT68 e no aceite |
| CT28 | US12 | Os dois perfis conseguem registrar pedido | Integração + E2E | Passou | Fase3EstoquePedidoTest; pedidos dos dois perfis no CT68 |
| CT29 | US10 | Ingrediente com linha de entrada vencida não conta como estoque disponível | Integração | Passou | Fase3EstoquePedidoTest; Fase4ProducaoTest |
| CT30 | US9 Estoque | Entrada cria uma linha | Integração, E2E | Passou | SQL e `Fase3EstoquePedidoTest.entradaSomaAoSaldoEApareceComoValida` |
| CT31 | US9 Estoque | A baixa consome a menor validade primeiro; linha sem validade por último | Integração | Passou | SQL S3 e `…s3BaixaAtravessaLinhas…` |
| CT32 | US9 Estoque | Linha nova com validade menor que a de uma linha já consumida | Integração | Passou | SQL S2 e `…s2ASaidaSaiDaMenorValidade…` |
| CT33 | US9 Estoque | Só há linha vencida: saída convencional e pedido são recusados | Integração, E2E | Passou | SQL S4 e `…s4SoHaVencidoEntaoPedidoDa422…` |
| CT34 | US9 Estoque | Descarte consome só linhas vencidas | Integração, E2E | Passou | SQL S5 e `…s5DescarteMaiorQueOVencido…`, `…s1VencidoNaoEntraNaVenda…` |
| CT35 | US9 Estoque | Saída sem motivo é rejeitada pela API e pelo banco | Integração, Banco | Passou | `…regrasDeValidacaoDaMovimentacao`; T62 |
| CT36 | US9 Estoque | Invariante `quantidade_estoque = soma dos restantes` | Integração, Banco | Passou | SQL e nos testes de integração |
| CT37 | US9 Estoque | Dois pedidos simultâneos pelo mesmo estoque | Integração | Passou | SQL 30/30; Java 5/5 em `…doisPedidosSimultaneos…` |
| CT38 | US12 Pedido | O pedido grava o preço do banco e baixa os ingredientes pela composição | Integração, E2E | Passou | Com 4× (51,60; açaí atravessando duas linhas): `…pedidoBaixaPorLinhasComUmaSaidaPorIngrediente…` |
| CT39 | US12 Pedido | A data do pedido fica no fuso de São Paulo (A9) | Integração | Passou | `DashboardTest.mesAtualSoComPedidosAtivos` |
| CT40 | US12 Pedido | `GET /api/pedidos` lista os itens e o status | Integração, E2E | Passou | `…pedidoBaixaPorLinhas…` |
| CT41 | US10/US11 Produção | O modelo maximiza o lucro, não a receita | Python | Passou | `test_maior_receita_nao_e_maior_lucro` |
| CT42 | US10/US11 Produção | Recomendação com o estoque de demonstração | Integração, E2E | Passou | Dublê: `Fase4ProducaoTest`; Python real: `PythonRealTest`; container de deploy |
| CT43 | US10/US11 Produção | A Tela 12 não usa termos técnicos e explica o zero | E2E (Playwright) | Passou | frontend/tests/browser.cjs (produção sem termos técnicos e motivos do zero); aceite na URL publicada (15/11) |
| CT44 | US10/US11 Produção | Erros do Python viram mensagens tratadas | Integração | Passou | `Fase4ProducaoTest`, `ProcessoPythonTest` |
| CT45 | US10/US11 Produção | Funcionário não gera recomendação | Integração | Passou |  |
| CT46 | Segurança (US3, US7) | `401` em JSON sem token, com token inválido, expirado e em rota inexistente | MockMvc | Passou | `SegurancaEErrosTest` |
| CT47 | Segurança (US3, US7) | `400` em JSON para requisição malformada | MockMvc | Passou | Depois da correção A7 |
| CT48 | Segurança (US3, US7) | `500` sem detalhe interno | MockMvc | Passou |  |
| CT49 | Segurança (US3, US7) | CORS para o frontend, inclusive `PATCH` | MockMvc | Passou |  |
| CT50 | Segurança (US3, US7) | Modo eco da ponte Java ↔ Python, com acentos | Integração + MockMvc | Passou | `PythonRealTest.ecoComAcentosPeloPerfilDev` |
| CT51 | US14 Reposição | Orçamento folgado compra toda a necessidade | Integração, Python, E2E | Passou | Python real: `PythonRealTest.reposicaoContaAMao` |
| CT52 | US14 Reposição | Orçamento que não cobre os mínimos | Integração, Python, E2E | Passou | Idem |
| CT53 | US14 Reposição | Orçamento parcial reparte de forma equilibrada | Python | Passou | `test_exemplo_didatico_via_subprocesso`; 150 instâncias contra o oráculo |
| CT54 | US14 Reposição | Período e quantidade de ingredientes | Integração | Passou | `ReposicaoTest` |
| CT55 | US14 Reposição | Sem pedidos no período | Integração | Passou |  |
| CT56 | US14 Reposição | Validação e permissão | Integração | Passou |  |
| CT57 | US14 Reposição | Tela 13 | E2E | Passou | frontend/tests/browser.cjs (reposição); aceite na URL publicada (15/11) |
| CT58 | US17 Dashboard | Números do mês só com pedidos ativos | Integração | Passou | `DashboardTest.mesAtualSoComPedidosAtivos` |
| CT59 | US17 Dashboard | Ranking e filtros | Integração | Passou | `…filtrosDoRanking` |
| CT60 | US17 Dashboard | Outro mês e erros | Integração | Passou | `…outroMesEErrosDeFiltro` |
| CT61 | US17 Dashboard | "Receita máxima estimada" e situação do estoque (Termo; A16) | Integração | Passou | `…situacaoDoEstoqueEUltimoPlano` |
| CT62 | US17 Dashboard | Dashboard com o seed de demonstração | Banco, E2E | Passou | SQL no banco de demonstração; tela no aceite na URL publicada (15/11) |
| CT63 | US17 Dashboard | Tela 03 | E2E | Passou | frontend/tests/browser.cjs (dashboard); aceite na URL publicada (15/11) |
| CT64 | US18 Marketing | Sugestões a partir da última recomendação | Integração | Passou | `MarketingTest` |
| CT65 | US18 Marketing | Sem cálculo, sem produto a produzir, permissão | Integração | Passou |  |
| CT66 | US18 Marketing | Tela 14 | E2E | Passou | frontend/tests/browser.cjs (marketing); aceite na URL publicada (15/11) |
| CT67 | US20 Publicação | A imagem sobe dentro dos limites do plano free | Deploy (local) | Passou | Imagem local com 512 MB e 0,1 CPU (`2026-11-11_kaue_ct67.txt`) |
| CT68 | US20 Publicação | Fluxo do Termo no ambiente publicado | Deploy, E2E | Passou | Ambiente publicado, dois perfis, 14/11 (`2026-11-14_kaue_ct68.txt`); 150/0/0 com o estoque publicado |
| CT69 | US20 Publicação | CORS do GitHub Pages | Deploy | Passou | Render, 11/11 e 14/11 (`2026-11-14_kaue_ct69.txt`) |
| CT70 | US20 Publicação | Primeira chamada depois de o serviço "dormir" | Deploy, E2E | Passou | Ambiente publicado, 14/11: esperou 51,4 s e funcionou (`2026-11-14_kaue_ct70.txt`) |
| CT71 | US9 Estoque | Botão "Descartar" zera só a linha vencida escolhida (A12, opcional) | Integração, E2E | Passou | `Fase3EstoquePedidoTest.descarteDeUmaLinhaVencidaZeraSoEssaLinha`; com o defeito de propósito "aceitar linha não vencida", o teste falha |
| CT72 | US20 Publicação | Otimização desligada no servidor (plano B1 da licença, A31; reserva desde 28/10, A38) | Integração, Deploy, E2E | Passou | OtimizadorDesligadoTest (3 testes) e ambiente publicado, 14/11 (`2026-11-14_kaue_ct72.txt`) |
| CT73 | US26 Porção padrão | Cadastrar uma unidade nova, com a unidade normalizada | Integração, E2E | Passou | Na API (`Us26PorcaoPadraoTest.cadastraUnidadeNovaNormalizada`; no banco, o T72); tela (Tela 19): Sim, com a API simulada (`npm test`, 12/11) |
| CT74 | US26 Porção padrão | Unidade repetida é recusada | Integração | Passou | `Us26PorcaoPadraoTest.unidadeRepetidaDa409InclusiveComMaiuscula`; no banco, o SQL à mão de 10/11 |
| CT75 | US26 Porção padrão | Dados inválidos são recusados no `POST` e no `PATCH`, nos limites | Integração | Passou | `Us26PorcaoPadraoTest.dadosInvalidosNosLimitesDao400`; no banco, a porção 0 é o T73 |
| CT76 | US26 Porção padrão | Alterar a porção padrão não muda os ingredientes (critério 3) | Integração, Banco | Passou | `Us26PorcaoPadraoTest.alterarPorcaoPadraoNaoMudaOsIngredientes`; no banco, o SQL à mão de 10/11 |
| CT77 | US26 Porção padrão | `PATCH` de unidade inexistente | Integração | Passou | `Us26PorcaoPadraoTest.alterarUnidadeInexistenteDa404`; no banco, o SQL à mão de 10/11 |
| CT78 | US26 Porção padrão | Só o Administrador cadastra e altera | Integração, E2E | Passou | Na API (`Us26PorcaoPadraoTest.soOAdministradorCadastraEAltera`); tela (Tela 19): Sim, com a API simulada (`npm test`, 12/11: o Funcionário volta ao Início e não vê o link) |

## 4. Bugs encontrados nesta bateria

| # | Descrição | Severidade | Status |
|---|---|---|---|
| 1 | A palavra “null” aparecia nas telas de Produção e de Reposição quando não havia aviso geral ou itens | Média (visível ao usuário) | Corrigido (PR #103), revisado pelo Leonardo e conferido na URL publicada em 15/11 |
| 2 | Ingrediente “Granola” duplicado, em `mg`, no banco publicado (resto de teste) | Baixa (dado) | Corrigido em 15/11: o ingrediente foi apagado; telas conferidas |
| 3 | Com quantidade 0, a Produção mostrava “Falta ingrediente” mesmo quando o motivo era o lucro | Média (informação errada ao usuário) | Corrigido (PR #107): o texto passou a “O estoque não dá ou rende mais em outro copo”; conferido na URL publicada |
| 4 | O item Clientes (fora do semestre) aparecia no menu com o selo “S2”, como se fosse entrega atrasada | Baixa | Corrigido (PR #113): selo “Futuro”, com a dica “Fora deste semestre” |

O fluxo seguido foi: registrar → teste que reproduz → branch `fix/...` → PR com revisão → retestar. O detalhe está em `docs/Sprint_4/sprint-4-semana-de-bugs.md`, seção 2.

## 5. Testes de aceitação com cliente/PO

- **14/11/2026, CT68:** o fluxo do Termo de Aceite foi executado na URL publicada, nos dois perfis: login, entrada de estoque, recomendação de produção, pedido com baixa automática, reposição, dashboard e marketing. Execução do Diogo, a pedido do Kauê (facilitador). Passou.
- **15/11/2026, aceite na URL publicada:** o Leonardo percorreu as telas nos dois perfis, incluindo a Tela 19, com 17 prints (DevTools fechado): primeiro o que só lê, depois o que grava (entrada de estoque e pedido). Passou.
- **Aceite formal do cliente:** é o Termo de Aceite do Cliente (E9b), que compara o escopo do Termo de Aceite do Projeto com o que foi entregue.

## 6. Limites desta bateria

- **Cobertura:** não foi medida (seção 1).
- **Ambiente publicado:** o CT12 (cadastro de produto) e os casos de erro foram executados nos testes automatizados, e não no ambiente publicado; lá, só a guarda de rotas do Funcionário foi executada.
- **Valores de referência:** o CT42, o CT51, o CT58 e o CT64 conferem os valores do cenário de referência nos testes automatizados. No banco publicado, os números são outros (150/0/0 em vez de 151/1/0), porque o estoque já não era o do seed.
- **Testes indiretos:** o CT23 e o CT24 estão cobertos pelos testes do otimizador e pelo oráculo de força bruta, mas não há um teste com esses IDs.
- **CT20:** foi executado numa máquina que já tinha as ferramentas instaladas, e não por um colega numa máquina limpa.
- **Datas:** os testes no ambiente publicado foram em 14 e 15/11, depois do fim previsto da semana de bugs (13/11), porque o deploy só aconteceu em 11/11 (decisão A47).
