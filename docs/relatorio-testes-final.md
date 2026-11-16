# Relatório de Testes Executados — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039)
**Data da bateria final:** 14 e 15/11/2026 (ambiente publicado), com as suítes automatizadas executadas de 11 a 15/11/2026
**Escopo testado:** funcionalidades do Termo de Aceite do Projeto (E2b), com as mudanças do Adendo nº 1 (27/10/2026) e a US26 (entregue em 12/11)

## 1. Resumo executivo
| Métrica | Valor |
|---|---|
| Total de casos de teste | 78 (CT01–CT78 do Plano de Testes) |
| Passaram | 78 |
| Falharam | 0 (os 4 bugs desta bateria foram corrigidos e retestados; seção 3) |
| Cobertura automatizada (linhas/branches) | Não medida (sem ferramenta de cobertura). Verificações automatizadas: backend 101, Python 47, banco 32 + 41, navegador 44 |

## 2. Casos de teste — visão consolidada
| ID | Área | Tipo | Resultado | Observação |
|---|---|---|---|---|
| CT01 | US1 | Integração | Passou | Cadastro de Administrador com dados válidos. Evidência: Fase2CrudTest |
| CT02 | US1 | Integração + Banco | Passou | Senha abaixo do mínimo. Evidência: Fase2CrudTest; constraints_teste.sql |
| CT03 | US1 | Integração + Banco | Passou | E-mail já cadastrado. Evidência: Fase2CrudTest; constraints_teste.sql |
| CT04 | US2 | Integração | Passou | Cadastro de funcionário válido pelo Administrador. Evidência: Fase2CrudTest |
| CT05 | US2 | Integração | Passou | Funcionário tenta cadastrar/alterar preço de produto. Evidência: Fase2CrudTest; SegurancaEErrosTest |
| CT06 | US3 | Integração + E2E | Passou | Login válido por perfil. Evidência: Fase2CrudTest; CT68 no ambiente publicado (14/11) |
| CT07 | US3 | Integração | Passou | Login com senha incorreta. Evidência: Fase2CrudTest |
| CT08 | US3 | Integração + E2E | Passou | Funcionário tenta acessar rota administrativa direto pela URL. Evidência: Fase2CrudTest; SegurancaEErrosTest; guarda de rotas no CT68 e no aceite (15/11) |
| CT09 | US4 | Integração | Passou | Cadastro de ingrediente com dados válidos. Evidência: Fase2CrudTest |
| CT10 | US4 | Integração + Banco | Passou | Custo unitário negativo. Evidência: Fase2CrudTest; constraints_teste.sql |
| CT11 | US4 | Integração + Banco | Passou | Porção padrão igual a zero. Evidência: Fase2CrudTest; constraints_teste.sql |
| CT12 | US5 | Integração | Passou | Cadastro de produto com composição válida, incluindo Açaí. Evidência: Fase2CrudTest; não executado no ambiente publicado |
| CT13 | US5 | Integração | Passou | Cadastro de produto sem nenhum ingrediente vinculado. Evidência: Fase2CrudTest |
| CT14 | US5 | Banco | Passou | Quantidade utilizada zerada/negativa na composição. Evidência: constraints_teste.sql |
| CT15 | US5 | Integração + Banco | Passou | Produto cadastrado com outros ingredientes, mas sem Açaí. Evidência: Fase2CrudTest; constraints_teste.sql |
| CT16 | US6 | Integração | Passou | Atualização de preço por Administrador. Evidência: Fase2CrudTest |
| CT17 | US6 | Integração + Banco | Passou | Preço igual a zero ou negativo. Evidência: Fase2CrudTest; constraints_teste.sql |
| CT18 | US7 | Integração | Passou | Requisição direta à API, contornando a interface, sem campo obrigatório. Evidência: Fase2CrudTest |
| CT19 | US7 | E2E (navegador) | Passou | Formulário de cadastro com campo obrigatório vazio. Evidência: frontend/tests/browser.cjs, verificações de campos obrigatórios |
| CT20 | US8 | Manual | Passou | Setup do zero a partir do README. Evidência: Em 15/11, só com o README, numa máquina com as ferramentas já instaladas (`2026-11-15_diogo_ct20_setup-do-zero.txt`) |
| CT21 | US9 | Integração + Banco | Passou | Saída de estoque maior que o disponível. Evidência: Fase3EstoquePedidoTest; constraints_sprint3_teste.sql |
| CT22 | US9 | Integração + E2E | Passou | Entrada válida de ingrediente. Evidência: Fase3EstoquePedidoTest; entrada pela tela no CT68 e no aceite |
| CT23 | US10 | Python | Passou | Um ingrediente escasso limita a produção de um único produto. Evidência: Coberto pelos testes do otimizador (`test_consumo_nunca_passa_do_estoque…` e oráculo em 200 instâncias); não há teste com o ID |
| CT24 | US10 | Python | Passou | Dois produtos disputam o mesmo ingrediente escasso. Evidência: Coberto por `test_maior_receita_nao_e_maior_lucro` e pelo oráculo; não há teste com o ID |
| CT25 | US12 | Integração | Passou | Pedido com múltiplos itens, um deles sem estoque suficiente. Evidência: Fase3EstoquePedidoTest |
| CT26 | US9 | Integração + Banco | Passou | Registrar movimentação com quantidade zero ou negativa. Evidência: Fase3EstoquePedidoTest; constraints_sprint3_teste.sql |
| CT27 | US9 | Integração + E2E | Passou | Os dois perfis conseguem registrar movimentação de estoque. Evidência: Fase3EstoquePedidoTest; os dois perfis no CT68 e no aceite |
| CT28 | US12 | Integração + E2E | Passou | Os dois perfis conseguem registrar pedido. Evidência: Fase3EstoquePedidoTest; pedidos dos dois perfis no CT68 |
| CT29 | US10 | Integração | Passou | Ingrediente com linha de entrada vencida não conta como estoque disponível. Evidência: Fase3EstoquePedidoTest; Fase4ProducaoTest |
| CT30 | US9 Estoque | Integração, E2E | Passou | Entrada cria uma linha. Evidência: SQL e `Fase3EstoquePedidoTest.entradaSomaAoSaldoEApareceComoValida` |
| CT31 | US9 Estoque | Integração | Passou | A baixa consome a menor validade primeiro; linha sem validade por último. Evidência: SQL S3 e `…s3BaixaAtravessaLinhas…` |
| CT32 | US9 Estoque | Integração | Passou | Linha nova com validade menor que a de uma linha já consumida. Evidência: SQL S2 e `…s2ASaidaSaiDaMenorValidade…` |
| CT33 | US9 Estoque | Integração, E2E | Passou | Só há linha vencida: saída convencional e pedido são recusados. Evidência: SQL S4 e `…s4SoHaVencidoEntaoPedidoDa422…` |
| CT34 | US9 Estoque | Integração, E2E | Passou | Descarte consome só linhas vencidas. Evidência: SQL S5 e `…s5DescarteMaiorQueOVencido…`, `…s1VencidoNaoEntraNaVenda…` |
| CT35 | US9 Estoque | Integração, Banco | Passou | Saída sem motivo é rejeitada pela API e pelo banco. Evidência: `…regrasDeValidacaoDaMovimentacao`; T62 |
| CT36 | US9 Estoque | Integração, Banco | Passou | Invariante `quantidade_estoque = soma dos restantes`. Evidência: SQL e nos testes de integração |
| CT37 | US9 Estoque | Integração | Passou | Dois pedidos simultâneos pelo mesmo estoque. Evidência: SQL 30/30; Java 5/5 em `…doisPedidosSimultaneos…` |
| CT38 | US12 Pedido | Integração, E2E | Passou | O pedido grava o preço do banco e baixa os ingredientes pela composição. Evidência: Com 4× (51,60; açaí atravessando duas linhas): `…pedidoBaixaPorLinhasComUmaSaidaPorIngrediente…` |
| CT39 | US12 Pedido | Integração | Passou | A data do pedido fica no fuso de São Paulo (A9). Evidência: `DashboardTest.mesAtualSoComPedidosAtivos` |
| CT40 | US12 Pedido | Integração, E2E | Passou | `GET /api/pedidos` lista os itens e o status. Evidência: `…pedidoBaixaPorLinhas…` |
| CT41 | US10/US11 Produção | Python | Passou | O modelo maximiza o lucro, não a receita. Evidência: `test_maior_receita_nao_e_maior_lucro` |
| CT42 | US10/US11 Produção | Integração, E2E | Passou | Recomendação com o estoque de demonstração. Evidência: Fase4ProducaoTest (dublê), PythonRealTest (Python real) e imagem de deploy (151/1/0 com o seed). No banco publicado deu 150/0/0, porque o estoque já não era o do seed (CT68) |
| CT43 | US10/US11 Produção | E2E (Playwright) | Passou | A Tela 12 não usa termos técnicos e explica o zero. Evidência: frontend/tests/browser.cjs (sem termos técnicos e motivos do zero); aceite na URL publicada (15/11) |
| CT44 | US10/US11 Produção | Integração | Passou | Erros do Python viram mensagens tratadas. Evidência: `Fase4ProducaoTest`, `ProcessoPythonTest` |
| CT45 | US10/US11 Produção | Integração | Passou | Funcionário não gera recomendação. Evidência:  |
| CT46 | Segurança (US3, US7) | MockMvc | Passou | `401` em JSON sem token, com token inválido, expirado e em rota inexistente. Evidência: `SegurancaEErrosTest` |
| CT47 | Segurança (US3, US7) | MockMvc | Passou | `400` em JSON para requisição malformada. Evidência: Depois da correção A7 |
| CT48 | Segurança (US3, US7) | MockMvc | Passou | `500` sem detalhe interno. Evidência:  |
| CT49 | Segurança (US3, US7) | MockMvc | Passou | CORS para o frontend, inclusive `PATCH`. Evidência:  |
| CT50 | Segurança (US3, US7) | Integração + MockMvc | Passou | Modo eco da ponte Java ↔ Python, com acentos. Evidência: `PythonRealTest.ecoComAcentosPeloPerfilDev` |
| CT51 | US14 Reposição | Integração, Python, E2E | Passou | Orçamento folgado compra toda a necessidade. Evidência: Python real: `PythonRealTest.reposicaoContaAMao` |
| CT52 | US14 Reposição | Integração, Python, E2E | Passou | Orçamento que não cobre os mínimos. Evidência: Idem |
| CT53 | US14 Reposição | Python | Passou | Orçamento parcial reparte de forma equilibrada. Evidência: `test_exemplo_didatico_via_subprocesso`; 150 instâncias contra o oráculo |
| CT54 | US14 Reposição | Integração | Passou | Período e quantidade de ingredientes. Evidência: `ReposicaoTest` |
| CT55 | US14 Reposição | Integração | Passou | Sem pedidos no período. Evidência:  |
| CT56 | US14 Reposição | Integração | Passou | Validação e permissão. Evidência:  |
| CT57 | US14 Reposição | E2E | Passou | Tela 13. Evidência: frontend/tests/browser.cjs (reposição); aceite na URL publicada (15/11) |
| CT58 | US17 Dashboard | Integração | Passou | Números do mês só com pedidos ativos. Evidência: `DashboardTest.mesAtualSoComPedidosAtivos` |
| CT59 | US17 Dashboard | Integração | Passou | Ranking e filtros. Evidência: `…filtrosDoRanking` |
| CT60 | US17 Dashboard | Integração | Passou | Outro mês e erros. Evidência: `…outroMesEErrosDeFiltro` |
| CT61 | US17 Dashboard | Integração | Passou | "Receita máxima estimada" e situação do estoque (Termo; A16). Evidência: `…situacaoDoEstoqueEUltimoPlano` |
| CT62 | US17 Dashboard | Banco, E2E | Passou | Dashboard com o seed de demonstração. Evidência: SQL no banco de demonstração; tela no aceite na URL publicada (15/11) |
| CT63 | US17 Dashboard | E2E | Passou | Tela 03. Evidência: frontend/tests/browser.cjs (dashboard); aceite na URL publicada (15/11) |
| CT64 | US18 Marketing | Integração | Passou | Sugestões a partir da última recomendação. Evidência: `MarketingTest` |
| CT65 | US18 Marketing | Integração | Passou | Sem cálculo, sem produto a produzir, permissão. Evidência:  |
| CT66 | US18 Marketing | E2E | Passou | Tela 14. Evidência: frontend/tests/browser.cjs (marketing); aceite na URL publicada (15/11) |
| CT67 | US20 Publicação | Deploy (local) | Passou | A imagem sobe dentro dos limites do plano free. Evidência: Imagem local com 512 MB e 0,1 CPU, 11/11 (`2026-11-11_kaue_ct67.txt`) |
| CT68 | US20 Publicação | Deploy, E2E | Passou | Fluxo do Termo no ambiente publicado. Evidência: Ambiente publicado, dois perfis, 14/11 (`2026-11-14_kaue_ct68.txt`) |
| CT69 | US20 Publicação | Deploy | Passou | CORS do GitHub Pages. Evidência: Render, 11/11 e 14/11 (`2026-11-14_kaue_ct69.txt`) |
| CT70 | US20 Publicação | Deploy, E2E | Passou | Primeira chamada depois de o serviço "dormir". Evidência: Ambiente publicado, 14/11: esperou 51,4 s e funcionou (`2026-11-14_kaue_ct70.txt`) |
| CT71 | US9 Estoque | Integração, E2E | Passou | Botão "Descartar" zera só a linha vencida escolhida (A12, opcional). Evidência: `Fase3EstoquePedidoTest.descarteDeUmaLinhaVencidaZeraSoEssaLinha`; com o defeito de propósito "aceitar linha não vencida", o teste falha |
| CT72 | US20 Publicação | Integração, Deploy, E2E | Passou | Otimização desligada no servidor (plano B1 da licença, A31; reserva desde 28/10, A38). Evidência: OtimizadorDesligadoTest (3 testes) e ambiente publicado, 14/11 (`2026-11-14_kaue_ct72.txt`) |
| CT73 | US26 Porção padrão | Integração, E2E | Passou | Cadastrar uma unidade nova, com a unidade normalizada. Evidência: Na API (`Us26PorcaoPadraoTest.cadastraUnidadeNovaNormalizada`; no banco, o T72); tela (Tela 19): Sim, com a API simulada (`npm test`, 12/11) |
| CT74 | US26 Porção padrão | Integração | Passou | Unidade repetida é recusada. Evidência: `Us26PorcaoPadraoTest.unidadeRepetidaDa409InclusiveComMaiuscula`; no banco, o SQL à mão de 10/11 |
| CT75 | US26 Porção padrão | Integração | Passou | Dados inválidos são recusados no `POST` e no `PATCH`, nos limites. Evidência: `Us26PorcaoPadraoTest.dadosInvalidosNosLimitesDao400`; no banco, a porção 0 é o T73 |
| CT76 | US26 Porção padrão | Integração, Banco | Passou | Alterar a porção padrão não muda os ingredientes (critério 3). Evidência: `Us26PorcaoPadraoTest.alterarPorcaoPadraoNaoMudaOsIngredientes`; no banco, o SQL à mão de 10/11 |
| CT77 | US26 Porção padrão | Integração | Passou | `PATCH` de unidade inexistente. Evidência: `Us26PorcaoPadraoTest.alterarUnidadeInexistenteDa404`; no banco, o SQL à mão de 10/11 |
| CT78 | US26 Porção padrão | Integração, E2E | Passou | Só o Administrador cadastra e altera. Evidência: Na API (`Us26PorcaoPadraoTest.soOAdministradorCadastraEAltera`); tela (Tela 19): Sim, com a API simulada (`npm test`, 12/11: o Funcionário volta ao Início e não vê o link) |

## 3. Bugs encontrados nesta bateria
| # | Descrição | Severidade | Status |
|---|---|---|---|
| 1 | A palavra “null” aparecia nas telas de Produção e de Reposição quando não havia aviso geral ou itens | Média | Corrigido (PR #103) e conferido na URL publicada em 15/11 |
| 2 | Ingrediente “Granola” duplicado, em `mg`, no banco publicado (resto de teste) | Baixa | Corrigido em 15/11: ingrediente apagado e telas conferidas |
| 3 | Com quantidade 0, a Produção mostrava “Falta ingrediente” mesmo quando o motivo era o lucro | Média | Corrigido (PR #107): “O estoque não dá ou rende mais em outro copo”; conferido na URL publicada |
| 4 | O item Clientes (fora do semestre) aparecia no menu com o selo “S2” | Baixa | Corrigido (PR #113): selo “Futuro” |

## 4. Testes de aceitação com cliente/PO
- **14/11/2026, CT68, na URL publicada:**
  - **Participantes:** Diogo, a pedido do Kauê (facilitador).
  - **Roteiro seguido:** o fluxo do Termo nos dois perfis: login, entrada de estoque, recomendação de produção, pedido com baixa automática, reposição, dashboard e marketing.
  - **Resultado:** passou (`docs/Sprint_4/evidencias/2026-11-14_kaue_ct68.txt`).
- **15/11/2026, aceite na URL publicada:**
  - **Participante:** Leonardo.
  - **Roteiro seguido:** as telas nos dois perfis, incluindo a Tela 19; primeiro o que só lê, depois o que grava (entrada de estoque e pedido); 17 prints.
  - **Resultado:** passou (`docs/Sprint_4/evidencias/2026-11-15_leonardo_aceite-pages.txt`).
- **Aceite do cliente:** registrado no Termo de Aceite do Cliente (`docs/termo-aceite-cliente.md`).
