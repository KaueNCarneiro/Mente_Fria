# Plano de Testes — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

**Etapa:** E4 — Semana 4 (entrega 11/09/2026)

**Status:** este documento é a **estratégia** de testes. Ainda não há funcionalidade pronta; casos de teste detalhados, execução e evidências passam a viver em `E5-E8_evidencias_de_teste.md`, a partir da E5. A tabela da seção 3 é o ponto de partida e cresce a cada sprint.

---

> **Referência — requisitos mínimos do §3 do Manual**, já mapeados pela equipe no Documento de Visão (seção 6) e usados na coluna "O que cobre" da seção 1:
>
> | # | Requisito mínimo | Onde aparece no projeto |
> |---|---|---|
> | R1 | Autenticação com 2+ perfis | US1, US2, US3 |
> | R2 | 6+ entidades com relacionamento N:N | Produto↔Ingrediente e Pedido↔Produto (US5, US12) |
> | R3 | Regra de negócio não trivial | Programação Linear (US10, US11), reposição (US14), marketing (US18) |
> | R4 | Consulta agregada (relatório/dashboard) | US17, US19 |
> | R5 | Validações em interface e banco | US4, US6, US7 e transversal (US7) |
> | R6 | Deploy público por URL | US20 |
> | R7 | Repositório Git com README | US8 |

---

## 1. Estratégia

| Tipo de teste | O que cobre | Ferramenta | Quando roda |
|---|---|---|---|
| **Unitário** | Regra de negócio isolada, sem banco/rede: validações de campo antes de persistir (senha ≥ 8 caracteres, preço > 0, quantidade > 0…) e métodos de domínio (`Ingrediente.precisaReposicao()`, `Produto.calcularCustoProducao()`, `Produto.calcularQuantidadeOtimaProducao()`). Cobre principalmente **R3** e a metade "aplicação" de **R5**. | JUnit 5 + Mockito (backend Java); pytest (módulo Python/Gurobi — cenários de estoque/composição fixos, com resultado esperado calculado fora do próprio otimizador, para não validar o solver contra ele mesmo) | A cada build local (`mvn test` / `pytest`) e em todo push/PR no CI |
| **Integração** | Fluxo real entre camadas — Controller → Service → Repository → PostgreSQL; autenticação/autorização por perfil ponta a ponta na API; constraints do banco (`NOT NULL`, `CHECK`, `UNIQUE`, `FK` do DER/DDL); consultas agregadas do dashboard (`GROUP BY`/`JOIN`); chamada do backend ao módulo Python/Gurobi. Cobre **R1**, **R2**, **R4** e a metade "banco" de **R5**. | JUnit 5 + Spring Boot Test (`@SpringBootTest`, `MockMvc`) + **Testcontainers (PostgreSQL 16)** — atualizado de MySQL para PostgreSQL em 16/09/2026 (ver DER.md), todos com Docker disponível | No pipeline de CI, a cada Pull Request (obrigatório antes do merge) |
| **Manual/aceitação** | Fluxo principal ponta a ponta pela interface (Termo de Aceite, seção 2): cadastro de ingredientes/produtos → estoque → recomendação de produção/vendas e propaganda → registro de pedido → baixa automática; critérios de aceite de cada história (E2); e os itens do §3 que não são testáveis por código: **R6** (deploy) e **R7** (repositório/README) | Roteiro de teste manual por história, executado por um integrante diferente de quem implementou + navegador/API client (Postman ou Insomnia) para checagem pontual de endpoints | Ao final de cada sprint (Sprint Review) e obrigatoriamente antes de qualquer tag de entrega (E5–E8) |

Duas observações que não mudam a tabela acima, mas valem registrar:
- **Frontend (JS puro, sem build tool ainda — README):** por enquanto, validado via roteiro manual/aceitação. Se surgir lógica pura em JS que justifique isolamento (formatação, validação client-side antes de enviar ao backend), avaliar introduzir Jest ou Vitest *naquele* momento — não antes, para não criar dependência de build sem necessidade real.
- **CI:** assumindo GitHub Actions, já que o repositório é GitHub. Isso não muda a estratégia acima, só o arquivo de pipeline — ajustem se preferirem outra ferramenta.

---

## 2. Critério de bloqueio de merge

**Bloqueia o merge — corrigir antes de aceitar o PR:**
1. O build não compila/não roda (`mvn test` ou `pytest` falha antes mesmo de avaliar o resultado dos testes).
2. Algum teste unitário ou de integração já existente passa a falhar (regressão).
3. O PR introduz uma regra de negócio ligada a um requisito mínimo do §3 (autenticação, N:N, Programação Linear, validação, dashboard) sem nenhum teste automatizado cobrindo o caminho principal.
4. O PR remove, enfraquece ou contorna uma constraint documentada no DER (`NOT NULL`, `CHECK`, `UNIQUE`, `FK`) sem combinar antes com a equipe.
5. Um endpoint que deveria ser restrito por perfil (ex.: alterar preço, restrito ao Administrador — Termo de Aceite, item 3) fica acessível sem verificação de autorização.
6. Há credencial, senha, chave de licença Gurobi ou string de conexão escrita diretamente no código.

**Não bloqueia, mas exige comentário no PR e item no board:**
- Falta de teste para funcionalidade `Should`/`Could` ainda não crítica para o MVP.
- Falta de roteiro manual para um fluxo que o automatizado já cobre tecnicamente.

Não fixamos uma meta numérica de cobertura (ex.: "≥ 70% de linhas") agora. No início do projeto, o risco maior é lógica sem teste nenhum — não uma métrica abaixo do ideal. Vale revisitar isso a partir da E5, já com uma base de código real para medir.

---

## 3. Casos de teste planejados (cresce a cada sprint)

**Prioridade** aqui é a prioridade de *execução do teste* — não é o MoSCoW da história, embora normalmente andem juntos: **Alta** = bloqueia funcionalidade crítica ou envolve segurança/integridade de dados; **Média** = comportamento esperado, impacto limitado se atrasar; **Baixa** = caso auxiliar.

Incluímos uma coluna extra, **Caso de uso (E3)**, além do modelo, para fechar a rastreabilidade Backlog (E2) ↔ Caso de uso (E3) ↔ Teste, usando o mapeamento já definido em `Diagramas_UML.md`, seção 3.

### Sprint 1 — cadastro, autenticação, validação transversal

| ID | História (E2) | Caso de uso (E3) | Cenário | Entrada | Resultado esperado | Prioridade |
|---|---|---|---|---|---|---|
| CT01 | US1 | UC1 | Cadastro de Administrador com dados válidos | Nome, e-mail novo, senha com 8+ caracteres | Conta criada; perfil `ADMINISTRADOR` atribuído automaticamente | Alta |
| CT01B | US1 | UC1 | Cadastro de Administrador quando já existe um | Requisição de registro com um Administrador já cadastrado no sistema | Rejeitado com `409`; mensagem "O cadastro do Administrador já foi realizado." | Alta |
| CT02 | US1 | UC1 | Senha abaixo do mínimo | Senha com 7 caracteres | Cadastro rejeitado; erro específico no campo senha (validação de aplicação — não dá para checar isso depois de já estar em hash) | Alta |
| CT03 | US1 | UC1 | E-mail já cadastrado | E-mail idêntico a um usuário existente | Rejeitado pela aplicação e, se essa checagem for contornada, pela constraint `uq_usuario_email` | Alta |
| CT04 | US2 | UC2 | Cadastro de funcionário válido pelo Administrador | Nome, e-mail, senha válidos | Conta criada com perfil `FUNCIONARIO` | Alta |
| CT05 | US2 | UC2 | Funcionário tenta cadastrar/alterar preço de produto | Usuário autenticado com perfil `FUNCIONARIO` | Acesso negado (403) | Alta |
| CT06 | US3 | UC3 | Login válido por perfil | Credenciais corretas de Administrador e de Funcionário (2 execuções) | Redirecionamento para a área correspondente a cada perfil | Alta |
| CT07 | US3 | UC3 | Login com senha incorreta | E-mail válido + senha errada | Mensagem de erro clara; login não efetivado | Alta |
| CT08 | US3 | UC3 | Funcionário tenta acessar rota administrativa direto pela URL | Usuário autenticado com perfil `FUNCIONARIO` | Bloqueio/redirecionamento; ação não é executada | Alta |
| CT09 | US4 | UC4 | Cadastro de ingrediente com dados válidos | Nome, unidade, custo ≥ 0, porção > 0 | Ingrediente persistido com os valores informados | Alta |
| CT10 | US4 | UC4 | Custo unitário negativo | `custo_unitario = -1` | Rejeitado na interface e pela constraint `chk_ingrediente_custo_unitario` | Alta |
| CT11 | US4 | UC4 | Porção padrão igual a zero | `porcao_padrao = 0` | Rejeitado pela constraint `chk_ingrediente_porcao_padrao` (diferente do custo: porção não aceita nem zero) | Média |
| CT12 | US5 | UC5 | Cadastro de produto com composição válida, incluindo Açaí | Produto + ingrediente "Açaí" + 1 outro ingrediente, ambos com `quantidade_utilizada > 0` | Produto e vínculos persistidos em `produto_ingrediente` | Alta |
| CT13 | US5 | UC5 | Cadastro de produto sem nenhum ingrediente vinculado | Produto sem composição | Rejeitado — composição vazia | Alta |
| CT14 | US5 | UC5 | Quantidade utilizada zerada/negativa na composição | `quantidade_utilizada ≤ 0` | Rejeitado pela constraint `chk_produto_ingrediente_quantidade` | Média |
| CT15 | US5 | UC5 | Produto cadastrado com outros ingredientes, mas sem Açaí | Produto + ingredientes válidos que não incluem "Açaí" | Rejeitado — sistema exige o ingrediente "Açaí" especificamente entre os vinculados | Alta |
| CT16 | US6 | UC6 | Atualização de preço por Administrador | `preco_venda > 0` | Preço atualizado | Alta |
| CT17 | US6 | UC6 | Preço igual a zero ou negativo | `preco_venda ≤ 0` | Rejeitado pela constraint `chk_produto_preco_venda` | Alta |
| CT18 | US7 | UC7 | Requisição direta à API, contornando a interface, sem campo obrigatório | Requisição HTTP sem `nome` | Backend rejeita (400/422) mesmo sem passar pela validação de tela | Alta |
| CT19 | US7 | UC7 | Formulário de cadastro com campo obrigatório vazio | Campo em branco | Interface exibe erro específico antes de enviar | Média |
| CT20 | US8 | — | Setup do zero a partir do README | `git clone` + passos do README, ambiente limpo | Projeto sobe localmente sem etapa não documentada | Média |

> **Decisão de implementação (CT01B) — 20/09/2026:** decidido junto com a dúvida D-A (`sql_repositorios_sprint1.md`, seção 5) — o cadastro de Administrador (US1) fica aberto **só enquanto não existir nenhum Administrador** no banco (opção 2), usando `UsuarioRepository.existeAdministrador`. Depois do primeiro cadastro, uma nova tentativa é rejeitada com `409`. Confirmado pelo Lucas e já assumido pelo *Contrato de Comunicação* (item B5).

> **Decisão de implementação (CT15) — 19/09/2026:** a regra exige o ingrediente "Açaí" especificamente, não apenas "composição não vazia". Decidido: um ingrediente conta como açaí quando o seu **nome, sem acentos e sem diferenciar maiúsculas de minúsculas, contém `acai`** (ex.: "Açaí (polpa)", "AÇAÍ ZERO", "Polpa de açaí"). A verificação é feita na aplicação (Java), isolada em um único método, sem alteração no DER nem no DDL. Consequências: (1) se o dono renomear o ingrediente para um nome sem "açaí", o sistema recusa novos produtos até a correção do nome (falha segura); (2) mais de um ingrediente de açaí é permitido; (3) alternativas descartadas: ID fixo (o ID é gerado pelo banco) e coluna `eh_ingrediente_base` (mais robusta, mas exigiria alterar DER, UML, DDL e Tela 07 — pode ser adotada depois trocando apenas esse método). O teste unitário do método deve cobrir: aceitos — "Açaí (polpa)", "AÇAÍ ZERO", "polpa de acai"; recusados — "Granola", "Leite condensado", texto vazio e `null`.

### Sprint 2 (prévia) — estoque, pedidos e otimização

| ID | História (E2) | Caso de uso (E3) | Cenário | Entrada | Resultado esperado | Prioridade |
|---|---|---|---|---|---|---|
| CT21 | US9 | UC9 | Saída de estoque maior que o disponível | Saída = estoque atual + 1 unidade | Rejeitado; `quantidade_estoque` não é alterada (também protegido por `chk_ingrediente_quantidade_estoque`) | Alta |
| CT22 | US9 | UC9 | Entrada válida de ingrediente | Entrada de quantidade positiva | `quantidade_estoque` incrementada; registro criado em `movimentacao_estoque` com `tipo = ENTRADA` | Média |
| CT23 | US10 | UC10 | Um ingrediente escasso limita a produção de um único produto | Produto X usa 400g de polpa/unidade; estoque = 1.200g; nenhum outro ingrediente limita | Quantidade recomendada = 3 (1.200 ÷ 400) — não mais que isso | Alta |
| CT24 | US10 | UC10 | Dois produtos disputam o mesmo ingrediente escasso | Produtos A e B usam o mesmo ingrediente; estoque insuficiente para produzir o máximo de ambos ao mesmo tempo | A soma do consumo desse ingrediente pela recomendação final não ultrapassa o estoque disponível | Alta |
| CT25 | US12 | UC12 | Pedido com múltiplos itens, um deles sem estoque suficiente | Pedido com 2 produtos, um inviável por estoque | Pedido inteiro é rejeitado; nenhum item é gravado; estoque de nenhum ingrediente é alterado (atomicidade da transação) | Alta |
| CT26 | US9 | UC9 | Registrar movimentação com quantidade zero ou negativa | `quantidade ≤ 0` | Rejeitado pela constraint `chk_movimentacao_quantidade` | Média |
| CT27 | US9 | UC9 | Os dois perfis conseguem registrar movimentação de estoque | Execução com perfil `ADMINISTRADOR` e, separadamente, com perfil `FUNCIONARIO` | Operação permitida para os dois perfis — UC9 não é exclusivo de nenhum dos dois | Média |
| CT28 | US12 | UC12 | Os dois perfis conseguem registrar pedido | Execução com perfil `ADMINISTRADOR` e, separadamente, com perfil `FUNCIONARIO` | Operação permitida para os dois perfis — UC12 não é exclusivo de nenhum dos dois | Média |

*US11, US13 e US14 entram na próxima atualização junto com o restante da Sprint 2. US15–US19 (Sprint 3) e US20 (Sprint 4) seguem o mesmo padrão conforme a sprint se aproxima. US21–US25 são `Won't` no backlog e não geram caso de teste.*

---

## 4. Decisões e notas

### 4.1 Já validadas com a equipe
- **D-A (cadastro de Administrador) e D-B (Tela 07 sem o campo porção):** confirmadas em 20/09/2026. Lucas aprovou a opção 2 do D-A (cadastro aberto só até existir o primeiro Administrador; depois, `409` — CT01B); Leonardo aprovou incluir o campo *porção* na Tela 07, pré-preenchido pela unidade escolhida (ver `sql_repositorios_sprint1.md`, seção 5).
- **Testes de integração:** Testcontainers com PostgreSQL 16 real (atualizado de MySQL em 16/09/2026, ver DER.md) — Docker confirmado disponível para todo mundo.
- **US5 (composição do produto):** a regra exige o ingrediente "Açaí" especificamente, não apenas "composição não vazia" (como localizar esse ingrediente: decidido em 19/09/2026 — o nome, sem acentos e sem diferenciar maiúsculas, contém "acai"; ver a decisão registrada na seção 3, junto ao CT15).
- **Backlog — atores de US9, US12, US13, US15 e US16:** confirmado que os dois perfis (Administrador e Funcionário) têm acesso a essas 5 funcionalidades, conforme o `.docx` — que também é o que bate com `Diagramas_UML.md`. O `.pdf` do backlog está desatualizado nesses 5 pontos (mostra só 1 ator em cada um); vale atualizar essa exportação para não confundir quem abrir só o `.pdf`. CT27 e CT28 (seção 3) já cobrem essa confirmação para US9 e US12; US13, US15 e US16 recebem o mesmo tratamento quando forem detalhadas.

### 4.2 Em aberto (não bloqueiam este plano)
- **US26/UC20 (porção padrão por unidade de medida — adicionada nesta revisão):** ainda sem caso de teste dedicado; entra na atualização da sprint que implementar a Tela 19. Pontos a cobrir quando for detalhado: (1) cadastrar ingrediente com unidade ainda não registrada em `unidade_medida_porcao_padrao` deve ser rejeitado pela FK `fk_ingrediente_unidade_medida`; (2) alterar o padrão de uma unidade não deve recalcular `porcao_padrao` de ingredientes já cadastrados; (3) um ingrediente pode gravar `porcao_padrao` diferente do padrão sugerido para sua unidade (caso do Açaí em `kg`).
- **Licença Gurobi (node-locked vs. WLS — pendência já registrada no README):** o `gurobipy` instalado via pip já vem com uma licença gratuita "size-limited" (até 2000 variáveis/2000 restrições), suficiente para os testes unitários no volume de uma açaiteria. A escolha entre node-locked e WLS não bloqueia os testes agora; só passa a importar se o modelo de produção crescer muito ou para remover o aviso de "licença restrita" em produção. Se decidirem rodar o solver real também nos testes de integração/CI no futuro, WLS tende a se encaixar melhor por não depender de uma máquina específica — um node-locked trava por hardware, o que é problemático em runners de CI, que trocam de máquina a cada execução.
- **Meta de cobertura de teste:** não fixada agora (seção 2). Revisitar a partir da E5, com base de código real para medir.
- **Ferramenta de teste de frontend:** não decidida agora, propositalmente (seção 1).
