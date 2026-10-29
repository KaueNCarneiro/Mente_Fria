# Tutorial — Sprint 3 (Backend) — Lucas

**Para:** Lucas Gabriel Valadares Bassi · **Papel:** PO e backend · **Revisão:** 27/10/2026, noite (v5.3: **calendário deslocado um dia, A36**; antes, v5.2: respostas da equipe; descarte por linha, A12; deploy definitivo no sábado, A21) · **Substitui** a v5.1 (`historico/v5.1-27-10-antes-das-respostas/`), a versão da tarde (`historico/v5-26-10-antes-das-4-historias/`) e `historico/Tutorial_Sprint3_Backend_Lucas.md`.
**Base:** `sprint-3-roteiro.md` (v5.3), o **contrato oficial** `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` (v2; a proposta ficou congelada) e `sprint-3-decisoes-e-pendencias.md`.

> **Atenção (28/10/2026, decisão A39):** no cronograma do professor, este trabalho é a **Sprint 4**, com entrega e Review na **sex 06/11/2026**. Este tutorial foi escrito na Sprint 3 e continua sendo o **guia de execução**. As datas do §0 e do §8 foram atualizadas para o calendário da Sprint 4; o calendário completo, os marcos e as decisões A39–A42 estão em `docs/Sprint_4/`. Se algum dia citado no texto divergir, vale `docs/Sprint_4/sprint-4-roteiro.md`, §4.

> **Leia primeiro: o que foi verificado.**
>
> - **O backend inteiro está escrito e testado** em `docs/Sprint_3/referencia/backend/`: Fases 1, 2, 3 e 4A, reposição (US14), dashboard (US17) e marketing (US18).
> - **95 testes passam** (27/10, Boot 4.0.8, JDK 25):
>   - 18 de segurança e erros (MockMvc);
>   - 13 da regra do açaí;
>   - 57 de integração com **PostgreSQL 16 real** (Testcontainers), inclusive o descarte por linha (A12) e a otimização desligada (plano B1, A31);
>   - 7 com o **Python real** (`gurobipy` 13.0.3).
> - **Experimentos "quebrar de propósito"** (a prova de que os testes pegam defeitos):
>   - deixar a venda consumir linha vencida derrubou S1 e S4;
>   - mandar ao Python o saldo total derrubou o teste do CT29;
>   - contar pedido cancelado no dashboard derrubou o `DashboardTest`;
>   - não contar cancelado na reposição derrubou o `ReposicaoTest`;
>   - deixar o descarte por linha aceitar linha **não** vencida derrubou o teste do CT71.
> - **A mesma aplicação rodou num container com os limites do Render free** (512 MB, 0,1 CPU), com o fluxo completo sem falhas.
> - **Seu trabalho mudou:** não é mais escrever do zero. É **copiar o pedaço de cada fase, ler cada arquivo, rodar os testes daquela fase, abrir o PR e acompanhar o aceite.** Você vai defender esse código na Review: leia antes de copiar.
> - **Persistência:** Spring JDBC com `NamedParameterJdbcTemplate` (D4). **Sem JPA, Hibernate, `@Entity` ou `JpaRepository`.** Os repositórios são do Diogo (F7): ele copia e abre o PR deles; você copia serviços e controllers.
> - **Sem meta de cobertura** (Plano de Testes, §2).
>
> Se algo não bater com a sua tela, **pare** e mande a mensagem exata no grupo.

## 0. Onde você está e a ordem do seu trabalho

| Item | Estado em 26/10 (verificado) |
|---|---|
| `.env` | **Feito** (PR #24): rode sempre **de dentro de `backend/`** |
| Backend na `main` | Só `GET /api/saude` e `GET /api/ingredientes` (3 campos) |
| Backend em `referencia/` | **Completo e testado** (95 testes) |
| Banco novo | O Diogo abre o PR do DDL na **qui 29/10** (A39). A Fase 2 **depende dele**: os testes esperam o seed com estoque 0 |
| Docker | Necessário para os testes de integração. Se o Docker Desktop travar ao abrir, veja o **A26** (§7) |

| Fase | Entrega | Quando | Depende de | Libera |
|---|---|---|---|---|
| 1 | Segurança, JWT, CORS, erros (18 testes) | **Qui 29/10** | Nada | Todos |
| 2 | Auth, usuários, unidades, ingredientes, produtos, preço (15 + 13 testes) | **Sex 30/10** | Fase 1; PR do banco (Diogo) | Gate do Leonardo (sáb 31, de manhã) |
| 3 | Estoque por linhas e pedido (15 testes) | **Sáb 31/10** | Fase 2; Contrato v2 (A10) | Telas do Leonardo; reposição do Kauê |
| 4A | Ponte com o Python e recomendação (7 testes + Python real), **em dupla com o Kauê** | **Dom 01/11** | Fase P do Kauê; Fase 3 | Tela 12; marketing; dashboard |
| 6 | **Dashboard e marketing** (5 + 4 testes, mais 3 da otimização desligada) | **Seg 02/11**, de manhã (feriado com trabalho; dia de mais carga, A43) | Fases 3 e 4A; repositório do dashboard (Diogo) | Telas 03 e 14 |
| 8 | **Deploy definitivo** (A41) **na sua conta do Render** (A43), com o Kauê guiando; sem ensaio separado | **Seg 02/11**, à tarde | Fases 1–3 na `main`; o que entrar depois é publicado sozinho | Entrega de 06/11; semana de bugs |
| — | Suíte completa (**95**) com Testcontainers | **Seg 02/11**, à noite | Todas as fases | Congelamento (qua 04/11) |
| — | Revisar PRs (inclusive os dois da US26, do Diogo); Tela 14, se o Leonardo precisar | **Ter 03/11** (marco M4) e qua 04/11 | Todas as fases | Congelamento (qua 04/11) |
| — | **Criar a conta do Render** (só a conta, sem banco nem serviço; A43) | **Sáb 31/10** | — | Deploy de seg 02/11 |
| — | **Conferências de corte** (PO, A42): botão da A12 no **M3 (dom 01/11, à noite)**; *gate* da US26 (A40) e solver ligado no Render (A38) na **seg 02/11, de manhã** (A43) | Dom 01/11 e seg 02/11 | — | Seg 02 a qua 04/11 |
| — | Revisar a mensagem ao professor (`docs/Sprint_4/sprint-4-mensagem-professor.md`) antes de o Diogo enviar | **Qui 29/10** | — | Professor avisado (A43) |
| — | ~~Revisar o adendo e o pedido de prazo (02/11)~~ **Não é mais preciso:** o professor aprovou os dois pessoalmente em 27/10 | — | — | — |

A **Fase 5 (reposição)** é do Kauê (tutorial dele, §11). Você revisa o PR.

**Um PR por fase, no dia.** Desde 14/10 nada foi para o GitHub. Revisão de um colega obrigatória.

## 1. Por que algumas coisas são assim

| O que | Por quê |
|---|---|
| `hoje` e `agora` vêm de um `Clock` em `America/Sao_Paulo` e vão como parâmetro nos SQLs (F3, A9) | O Render roda em UTC. Das 21h à meia-noite, a validade e a data do pedido cairiam no dia (e no mês) seguinte. **Validado:** pedido às 23h30 de 31/10 conta em outubro |
| CORS **dentro** do filtro de segurança, com `PATCH` | A segurança roda antes do MVC; sem isso, o *preflight* leva `401` |
| Bean de CORS do tipo **`UrlBasedCorsConfigurationSource`** (A5) | Com o tipo genérico, **a aplicação não sobe** (verificado) |
| `401`/`403` no `SecurityConfig`; os demais erros no `@RestControllerAdvice` | Os erros de autenticação acontecem **antes** do controller |
| Driver PostgreSQL sem `scope runtime` (A4) | O tradutor de erros usa `PSQLException`. Com `runtime`, **não compila** |
| `/api/teste/python` só com `@Profile("dev")` (F4) | Sem controle de acesso, não pode existir em produção. **Validado no container:** `401` |
| Validação **manual** | Devolve **todos** os campos inválidos de uma vez, com as mensagens do `mapa_constraints.md` |
| `NamedParameterJdbcTemplate` e não `JdbcClient` | Decisão D4 |
| JSON em `snake_case` por **configuração global** (A23) | Uma linha no `application.properties` em vez de `@JsonProperty` em cada campo |
| Os serviços que chamam o Python **não** são `@Transactional` | A chamada pode levar segundos; não deve segurar uma conexão do banco |
| Uma otimização por vez (A27) | Licenças limitam sessões simultâneas |

## 2. Fase 1 — Fundação (segurança, JWT, CORS, erros)

**Objetivo:** toda rota, exceto as públicas, exige token; toda resposta de erro sai em JSON.

### 2.1 Preparar

```powershell
cd C:\caminho\Mente_Fria
docker start mente-fria-db
git checkout main
git pull
git checkout -b feature/backend-fundacao-seguranca
cd backend
copy .env.example .env      # se ainda não tiver o seu
```

Edite o `backend\.env` (**nunca** comite). Acrescente `JWT_SECRET=` com **32 ou mais caracteres aleatórios**.

> ⚠️ **`.env` sem BOM.** No VS Code, "UTF-8" (não "UTF-8 with BOM"). O Spring lê o `.env` como ISO-8859-1: o BOM "gruda" na primeira variável.

Acrescente ao `backend\.env.example`, **sem valores**:

```
JWT_SECRET=
CORS_ALLOWED_ORIGIN=http://127.0.0.1:5500,http://localhost:5500
PYTHON_CMD=python
OTIMIZADOR_SCRIPT=../optimization/app/otimizador.py
REPOSICAO_SCRIPT=../optimization/app/reposicao.py
```

### 2.2 `pom.xml`

O diff **exato** e validado está em `docs/Sprint_3/referencia/backend/pom.xml.diff` (49 linhas a mais, 1 a menos). Aplique **tudo de uma vez** já na Fase 1:

```powershell
cd ..
git apply --check docs/Sprint_3/referencia/backend/pom.xml.diff
git apply docs/Sprint_3/referencia/backend/pom.xml.diff
cd backend
```

**Você deve ver:** nenhuma saída no `--check` (conferido sobre a `main` em 26/10). O que muda:
1. Sai o `<scope>runtime</scope>` do driver PostgreSQL (A4).
2. Entram Spring Security, `jjwt` 0.13.0 (api, impl, jackson), `spring-boot-starter-security-test` e os três artefatos de Testcontainers (**sem `<version>`**: o Boot 4.0.8 gerencia o Testcontainers 2.0.5).
3. Entra o `<testResources>` dentro do `<build>` que já existe. Os testes de integração usam **o mesmo** `bd/Script_DDL.sql` da aplicação.

**Árvore de dependências:**

```powershell
.\mvnw -q dependency:tree "-Dincludes=com.fasterxml.jackson.core,tools.jackson.core,io.jsonwebtoken"
```

**Você deve ver** `tools.jackson.core:jackson-databind:3.1.5` (do Boot) **e** `com.fasterxml.jackson.core:jackson-databind:2.21.5` (do `jjwt-jackson`). **Não é conflito:** são pacotes diferentes.

### 2.3 `application.properties`

Troque o seu pelo de `docs/Sprint_3/referencia/backend/src/main/resources/application.properties`. Em relação ao da `main`, ele acrescenta:
- `server.port=${PORT:8080}` (no Render, a porta vem da variável `PORT`);
- `spring.jackson.property-naming-strategy=SNAKE_CASE` (A23);
- `app.jwt.*`, `app.cors.allowed-origins`, `app.python.cmd`, `app.otimizador.script`, `app.reposicao.script` e `app.otimizador.timeout-segundos` (padrão 10 s).

Em `.properties`, a `\` é escape: caminho do Windows usa `/`.

### 2.4 As classes

**Copie** de `docs/Sprint_3/referencia/backend/src/main/java/com/mentefria/backend/` para o mesmo caminho em `backend/`:

| Arquivo | O que faz | Detalhe importante |
|---|---|---|
| `config/ApiException.java` | Erro de negócio com status e `campos` | — |
| `config/RelogioConfig.java` | `Clock` em `America/Sao_Paulo` | — |
| `auth/JwtService.java` | Gera e valida o token (HS256, 32+ bytes) | Falha **ao subir** se o segredo for curto |
| `auth/JwtAuthFilter.java` | Lê `Authorization: Bearer` | **Sem `@Component`**: criado no `SecurityConfig` |
| `config/SecurityConfig.java` | Regras por perfil, CORS, `401`/`403` em JSON | Bean de CORS `UrlBasedCorsConfigurationSource` (A5) |
| `config/GlobalExceptionHandler.java` | Erros em JSON `{mensagem, campos}` | Handlers de `400` e `403` da A7 |

O que cada parte do `GlobalExceptionHandler` faz:

| Exceção | HTTP | Mensagem | Por quê |
|---|---|---|---|
| `ApiException` | o da exceção | a da exceção + `campos` | Regras de negócio |
| `DataIntegrityViolationException` | `400`/`409`/`422`, pelo `mapa_constraints.md` | "Dados inválidos" + o campo | O banco recusou. A chave é o nome da constraint; no `NOT NULL`, `NN:tabela.coluna` |
| `HttpMessageNotReadableException` | `400` | "Requisição inválida." | JSON quebrado |
| `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException` | `400` | "Requisição inválida." | A7 |
| `AccessDeniedException` | `403` | "Você não tem permissão para esta ação." | Se um dia usarem `@PreAuthorize` |
| `NoResourceFoundException` | `404` | "Recurso não encontrado." | Era o BE03 da Sprint 2 |
| `HttpRequestMethodNotSupportedException` | `405` | "Método não permitido." | — |
| `Exception` | `500` | "Não foi possível concluir a operação." | O *stack trace* vai só para o log. Inclui a `FalhaPython` (Fase 4A) |

Quem permite o quê (Contrato §2.2), já no `SecurityConfig`:

| Quem | Rotas |
|---|---|
| Público | `GET /api/saude`, `POST /api/auth/login`, `POST /api/auth/registro`; `/api/teste/**` **só no perfil `dev`** |
| Administrador | `/api/usuarios/**`, `/api/unidades-medida/**`, `/api/producao/**`, `/api/reposicao/**`, `/api/marketing/**`, `/api/dashboard/**`; `POST`, `PUT` e `PATCH` em `/api/ingredientes/**` e `/api/produtos/**` |
| Admin e Funcionário | O resto: `GET /api/ingredientes`, `GET /api/produtos`, `/api/estoque/**`, `/api/pedidos/**` |

### 2.5 Os testes da Fase 1

Copie `referencia/backend/src/test/java/com/mentefria/backend/config/` (`SegurancaEErrosTest.java` e `RotasDeTesteController.java`).

- O `RotasDeTesteController` existe **só nos testes** e só no perfil **`teste-seguranca`** (A24): simula rotas para testar segurança sem banco.
- **Por que o perfil:** quando os controllers reais chegarem (Fase 2), um `@SpringBootTest` carregaria as rotas simuladas **e** as reais, e não subiria ("mapeamento ambíguo"). Isso aconteceu na auditoria.
- **Apague** o `BackendApplicationTests.java` da `main` (o `contextLoads`): ele precisa do banco ligado e é substituído pela `IntegracaoBase` na Fase 2.

```powershell
.\mvnw test "-Dtest=SegurancaEErrosTest"
```

**Você deve ver:** `Tests run: 18, Failures: 0, Errors: 0`. Os 18 cobrem:
- `GET /api/saude` público;
- sem token, token inválido, expirado e rota inexistente sem token → **`401` JSON**;
- rota inexistente com token → `404` JSON;
- `/api/teste/python` sem `dev` → `401`;
- Funcionário em `/api/usuarios` e em `PUT .../preco` → **`403` JSON** (CT05, CT08);
- JSON malformado, id não numérico, parâmetro ausente → **`400`**;
- erro inesperado → `500` sem detalhe;
- *preflight* com `PATCH` → `200`; origem desconhecida → `403`.

| Sintoma | Causa | O que fazer |
|---|---|---|
| `package org.postgresql.util does not exist` | Driver com `scope runtime` | §2.2 |
| `NoUniqueBeanDefinitionException … CorsConfigurationSource` | Bean de CORS genérico | `SecurityConfig` de `referencia/` |
| `expected:<400> but was:<500>` | Faltam os handlers da A7 | `GlobalExceptionHandler` de `referencia/` |
| `cannot find symbol WebMvcTest` | Pacote do Boot 4 | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` |
| `@MockBean` não existe | Removido no Boot 4 | `@MockitoBean` |

### 2.6 Validação manual

```powershell
function Chamar($metodo, $url, $corpo = $null, $token = $null) {
  $h = @{}; if ($token) { $h["Authorization"] = "Bearer $token" }
  $p = @{ Method = $metodo; Uri = $url; Headers = $h }      # não use $args: é variável automática do PowerShell
  if ($corpo) { $p.ContentType = "application/json; charset=utf-8"; $p.Body = [Text.Encoding]::UTF8.GetBytes(($corpo | ConvertTo-Json -Depth 6)) }
  try { Invoke-RestMethod @p } catch { "HTTP " + [int]$_.Exception.Response.StatusCode; $_.ErrorDetails.Message }
}
Chamar GET http://localhost:8080/api/ingredientes
```

**Você deve ver:** `HTTP 401` e `{"mensagem":"Sua sessão expirou ou você não está autenticado."}`.

Commit: `feat: seguranca JWT, CORS e tratamento padrao de erros`. **Faça o merge da Fase 1 junto com a Fase 2, ou logo antes dela**, e avise o Leonardo: a partir dela, o frontend precisa de login.

## 3. Fase 2 — Autenticação e CRUD (US1–US7)

**Pré-requisito:** o PR do banco do Diogo na `main` (o seed precisa ter estoque 0) e os repositórios dele (ou copie-os junto, combinando com ele). Branch: `feature/backend-auth-crud`.

### 3.1 O que copiar

| Pacote | Arquivos | Quem é o dono (F7) |
|---|---|---|
| `usuario/` | `Usuario`, `UsuarioDtos`, `UsuarioRepository`, `UsuarioService`, `UsuarioController` | Diogo: `UsuarioRepository`; você: o resto |
| `unidade/` | `UnidadeMedida`, `UnidadeMedidaRepository`, `UnidadeMedidaController` | Diogo: o repositório |
| `ingrediente/` | `Ingrediente`, `IngredienteRepository`, `IngredienteService`, `IngredienteController` (**substituem** os da `main`; **apague** o `IngredienteResumo`) | Diogo: o repositório |
| `produto/` | `Produto`, `ProdutoRepository`, `ProdutoService`, `ProdutoController`, `RegraAcai` | Diogo: o repositório |
| `auth/` | `AuthController` | Você |
| **Infraestrutura** (sem rotas) | `python/ModulosPython`, `python/ProcessoPython`, `producao/Recomendacao`, `producao/UltimaRecomendacao` | Você. **Por quê já agora:** a `IntegracaoBase` dos testes troca os módulos Python por dublês e limpa o último resultado; sem esses quatro arquivos, os testes não compilam |
| Testes | `IntegracaoBase`, `Fase2CrudTest`, `produto/RegraAcaiTest` | Você |

### 3.2 O que ler com atenção (você vai defender na Review)

| Regra | Onde está | Teste que prova |
|---|---|---|
| Registro: **checar se já existe Administrador antes de validar** (CT01B); trava a tabela para dois registros simultâneos | `UsuarioService.registrarAdministrador` | `registroNormalizaEmailESegundoRegistroDa409` |
| E-mail normalizado (`trim` + minúsculas); perfil **nunca** vem do corpo | `UsuarioService` | idem |
| Todos os campos inválidos de uma vez | `UsuarioService.validar`, `IngredienteService.validar`, `ProdutoService.criar` | `registroComTodosOsCamposInvalidos…`, `ingredienteComVariosCamposInvalidos…` |
| Login: e-mail inexistente, senha errada e inativo dão **a mesma** resposta | `AuthController.login` | `senhaErradaEEmailInexistenteDaoAMesmaResposta` |
| `buscarPorEmail` nunca usa `queryForObject` | `UsuarioRepository` | — |
| `quantidade_estoque` nunca vem do corpo | `Ingrediente.Dados` não tem o campo | `funcionarioNaoCadastraIngredienteEAdministradorSim` |
| Produto: repetido testado **antes** de "não encontrado"; açaí obrigatório; produto e composição na mesma transação | `ProdutoService.criar` | `produtoSemAcai…`, `ingredienteInexistenteNaComposicaoNaoGravaOProduto` |
| A regra do açaí (casos do `sql_repositorios.md` §4) | `RegraAcai` | `RegraAcaiTest` (13 casos, inclusive acentos decompostos e "Assaí") |

### 3.3 Rodar

```powershell
.\mvnw test "-Dtest=SegurancaEErrosTest,RegraAcaiTest,Fase2CrudTest"
```

**Você deve ver:** 18 + 13 + **15** testes, sem falhas (26/10). O `Fase2CrudTest` sobe um PostgreSQL 16 no Docker e **recria o schema antes de cada teste** (A25).

### 3.4 Validar a Fase 2 (gate)

| # | Chamada | Esperado | CT |
|---|---|---|---|
| 1 | `Chamar POST http://localhost:8080/api/auth/registro @{nome="Dono";email=" DONO@Acai.com ";senha="senha1234"}` | `201`; e-mail `dono@acai.com` | CT01 |
| 2 | O mesmo, de novo | `409` "O cadastro do Administrador já foi realizado." | CT01B |
| 3 | Login correto | `200` com `token`, `perfil` e `nome` | CT06 |
| 4 | Senha errada **e** e-mail inexistente | `401`, **a mesma** mensagem | CT07 |
| 5 | `GET /api/ingredientes` com token | 7 itens; **todos os campos**; estoque 0 | CT09 |
| 6 | Criar Funcionário; `POST /api/ingredientes` com o token dele | `201`; depois **`403`** | CT04, CT05 |
| 7 | Ingrediente com `custo_unitario: -1` e `porcao_padrao: 0` | `400` com **os dois** campos | CT10, CT11, CT18 |
| 8 | Produto sem açaí / vazio / repetido | `400` CT15 / CT13 / "já foi adicionado" | CT13, CT15 |
| 9 | Produto válido e `GET /api/produtos` | `201`; a lista traz a composição | CT12 |
| 10 | `PUT /api/produtos/1/preco` com `0`, depois `13.50`; como Funcionário | `400`, `200`; `403` | CT16, CT17, CT05 |

**Gate:** com os 10 passando, **avise o Leonardo** (aceite real, tutorial dele §5).

## 4. Fase 3 — Estoque por linhas e pedido (US9, US12)

**Pré-requisitos:** Contrato v2 aprovado (A10); banco novo; repositórios `MovimentacaoRepository` e `PedidoRepository` do Diogo. Branch: `feature/backend-estoque-pedidos`.

**Copie:** `estoque/` (`MovimentacaoRepository`, `BaixaPorLinhas`, `EstoqueService`, `EstoqueController`), `pedido/` (`PedidoRepository`, `PedidoService`, `PedidoController`) e o teste `Fase3EstoquePedidoTest`.

### 4.1 O que o código reproduz (provado em SQL e agora em Java)

| Cenário | Resultado esperado | Teste |
|---|---|---|
| S1: linha vencida de 20 + válida de 20; saída de 5; descarte de 20 | Total 40, válido 20, vencido 20; a saída tira da válida; depois do descarte, total 15 | `s1VencidoNaoEntraNaVenda…` |
| S2: A vence em +20, sai 10; entra C vencendo em +15; sai 5 | C=15 e A=10 | `s2ASaidaSaiDaMenorValidade…` |
| S3: 10 sem validade, 3 (+1), 4 (+5); saídas de 6 e de 5 | `0/1/10`, depois `0/0/6` | `s3BaixaAtravessaLinhas…` |
| S4: só há vencido; pedido de 1 | `422`; nada gravado | `s4SoHaVencido…` |
| S5: descarte maior que o vencido | `422`; o exato funciona | `s5DescarteMaiorQueOVencido…` |
| Concorrência: dois pedidos de 30 sobre 50, 5 rodadas | Um passa, o outro `422`; saldo 20 | `doisPedidosSimultaneos…` |
| Invariante | Saldo = soma dos restantes, após cada operação | Em vários testes |
| **A12 (entrou em 27/10):** botão "Descartar" de uma linha vencida | `201` e só aquela linha zera; de novo → `422` "Esta linha já não tem saldo."; linha válida → `422` "Só é possível descartar linhas vencidas."; inexistente → `404` | `descarteDeUmaLinhaVencidaZeraSoEssaLinha` (CT71) |
| A9: hora do Java | `data_movimentacao` e `data_pedido` = relógio de São Paulo | `entradaSomaAoSaldo…`, `pedidoBaixaPorLinhas…` |

### 4.2 O que ler com atenção

- **`BaixaPorLinhas.consumir`**: consome as linhas **já travadas** na ordem recebida, baixa o saldo e grava **uma** `SAIDA`; devolve o id dela. Se não cobrir, lança o erro **sem alterar nada**.
- **`EstoqueService.movimentar`**: tudo numa transação; `travar` (`FOR UPDATE`); `CONVENCIONAL` usa `linhasValidas`, `DESCARTE_VENCIDO` usa `linhasVencidas`; `PEDIDO` vindo do cliente → `400`. **Entrada com validade já vencida é aceita** (proposta; decisão sua como PO).
- **`PedidoService.criar`** (9 passos comentados no código): validar → produtos ativos → necessidade por ingrediente (`BigDecimal`) → **travar em ordem de id** → só linhas válidas → `422` se faltar (nada gravado, CT25) → total com o **preço do banco** → itens → baixa.
- **`EstoqueService.descartarLinha` (A12):** trava **primeiro o ingrediente** e **depois a linha**, na mesma ordem do pedido e da movimentação. Assim duas operações ao mesmo tempo nunca ficam esperando uma pela outra (*deadlock*). Depois reaproveita `BaixaPorLinhas.consumir` com uma linha só. Se o PO cortar o botão (A28), o endpoint pode ficar: ele já está testado e não atrapalha nada.
- **Quem fez a ação:** `Integer.parseInt(authentication.getName())`. O `subject` do token é o id do usuário; **nunca** vem do corpo.

```powershell
.\mvnw test "-Dtest=Fase3EstoquePedidoTest"
```

**Você deve ver:** 15 testes, sem falhas.

### 4.3 Validar a Fase 3 (manual)

| # | Chamada | Esperado | CT |
|---|---|---|---|
| 1 | Entrada de 10 kg de açaí com validade futura | `201`; `GET /api/estoque` mostra +10 válido | CT22, CT30 |
| 2 | Saída convencional maior que o válido | `422`; saldo igual | CT21, CT33 |
| 3 | Saída com `data_validade` / sem `motivo` | `400` / `400` | CT35 |
| 4 | Descarte com vencido (banco de demonstração) | Só consome a linha vencida | CT34 |
| 5 | `POST /api/pedidos` com 2× Tradicional | `201`; açaí −0,600, leite condensado −0,060, copo −2 | CT38 |
| 6 | Pedido com um item sem estoque válido | `422` e **nada** gravado | CT25 |
| 7 | Funcionário cria pedido e movimentação | `201` | CT27, CT28 |
| 8 | `GET /api/pedidos` | Lista com `itens`, `usuario_nome` e `status` | CT40 |
| 9 | `GET /api/estoque/{id}/linhas` e, numa linha `VENCIDA`, `POST /api/estoque/linhas/{linha}/descarte` | `201`; a linha some do vencido; repetir → `422` | CT71 |

## 5. Fase 4A — Ponte com o Python e recomendação (em dupla com o Kauê)

**Pré-requisitos:** Fase P do Kauê (`otimizador.py` v2 e a pasta `optimization/exemplos/` na `main`); Fase 3.

**Copie:** `python/ChaveOtimizador` (plano B1 da licença, A31: o `ProducaoService` depende dela), `python/TestePythonController`, `producao/ProducaoService`, `producao/ProducaoController`; testes `Fase4ProducaoTest`, `PythonRealTest` e `python/ProcessoPythonTest`.

### 5.1 O que ler com atenção

**`ProcessoPython`** (copiado na Fase 2): os cinco cuidados que já deram problema no projeto.

| Cuidado | Como está no código | Teste |
|---|---|---|
| Entrada em UTF-8 **sem BOM**, `stdin` fechado | `OutputStreamWriter(…, UTF_8)` num `try` | `ProcessoPythonTest.idaEVoltaEmUtf8` |
| Timeout | `waitFor(10 s)` → `destroyForcibly()` → `FalhaPython` (`500`) | `timeoutMataOProcesso…` |
| `stdout` em UTF-8; `stderr` só no log | Lidos em paralelo (não travam) | — |
| Credenciais `GRB_*` repassadas ao filho (o `.env` do Spring **não** chega sozinho) | `pb.environment().put(…)`, só se existirem; nunca no log | `credenciaisGrbChegamAoProcessoFilho` |
| Uma execução por vez (A27) | `ReentrantLock` com tempo limite | — |

**`ProducaoService.recomendar`:**
1. Monta a entrada: consulta 3 (saldo **válido**, inclusive 0) + produtos ativos com composição.
2. Chama o módulo; `status: "erro"` → `422` com a mensagem; resposta fora do contrato → `500`.
3. **Recalcula com `BigDecimal`** a receita, a margem e o lucro por copo. Se o plano não couber no estoque, `500`: nunca mostrar plano errado.
4. Guarda o resultado na `UltimaRecomendacao` (o marketing e o dashboard leem dali).

### 5.2 Rodar

Com dublê (sem Python): `.\mvnw test "-Dtest=Fase4ProducaoTest"` → **7 testes**. Eles conferem:
- **151/1/0**, 1.966,80 e 1.011,71;
- lucro por copo 6,64 / 9,07 / 10,63;
- **a granola vai ao Python com 5,160** (CT29);
- a entrada tem o mesmo conteúdo de `optimization/exemplos/entrada_demo.json`;
- `422`/`500` e Funcionário `403`.

Com o **Python real**, use o **seu próprio** `venv` em `optimization`, com o `gurobipy` instalado por `pip install -r optimization/requirements.txt`. A licença restrita que vem com o `pip` basta para estes testes, porque eles são uso de desenvolvimento e os modelos são pequenos. Não peça licença à Gurobi e **não** use as credenciais da WLS do Kauê, que são pessoais (A34):

```powershell
$env:MF_PYTHON = "C:\caminho\Mente_Fria\optimization\.venv\Scripts\python.exe"
.\mvnw test "-Dtest=ProcessoPythonTest,PythonRealTest#ecoComAcentosPeloPerfilDev+producaoDaDemonstracaoDa151_1_0ComOGurobi"
```

**Você deve ver:** 4 + 2 testes, sem falhas. Na auditoria, a recomendação ponta a ponta levou **188 ms** (linha `[TEMPO]` na saída). O terceiro teste do `PythonRealTest` (reposição) só passa depois da Fase 5 do Kauê. Sem `MF_PYTHON`, essas classes são **puladas**, não falham.

**Manual:**
- `$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw spring-boot:run` → `GET /api/teste/python` → `{"status": "sucesso", "echo": "AÇAÍ (POLPA)"}`;
- sem o perfil, a rota → `401`;
- Administrador → `200`; Funcionário → `403`;
- `PYTHON_CMD=nao_existe` → `500` sem detalhe.

## 6. Fase 6 — Dashboard e marketing (US17, US18)

**Pré-requisitos:** Fases 3 e 4A; o `DashboardRepository` do Diogo (consultas 10–13). Branch: `feature/backend-dashboard-marketing`.

**Copie:** `dashboard/` (`DashboardRepository`, `DashboardService`, `DashboardController`), `marketing/` (`CatalogoIdeias`, `MarketingService`, `MarketingController`); testes `DashboardTest`, `OtimizadorDesligadoTest` (plano B1, A31) e `MarketingTest`. Os dois pacotes vão juntos: o dashboard usa o critério de "priorizado" do marketing.

### 6.1 O que ler com atenção

| Regra | Decisão | Onde |
|---|---|---|
| Período = um mês (`mes=AAAA-MM`); padrão: o mês atual em São Paulo | 14/10 | `DashboardService.painel` |
| Só pedidos `ATIVO` | 14/10 | Todas as consultas do `DashboardRepository` |
| Ranking: posição pelo valor; filtro de produto também restringe os valores; filtro de valor **depois** de somar (`HAVING`) | 14/10 | `DashboardRepository.ranking` |
| Nulos com tipo nos parâmetros opcionais (`Types.INTEGER`, `Types.NUMERIC`) | Sem isso o PostgreSQL não sabe o tipo de `NULL` | idem |
| Ingredientes ordenados pelo **custo** do consumo | **A17 (aprovada em 27/10)** | `ingredientesMaisUtilizados` |
| "Receita máxima estimada" e "produtos prioritários" do **último cálculo** + situação do estoque | **A16 (aprovada em 27/10)**, pelo Termo de Aceite | `DashboardService` |
| Marketing: priorizado = quantidade > 0, maior primeiro, empate menor id; **até 3**; ideias sem repetir, em rodízio pelo dia | 14/10 e **A20 (aprovada em 27/10; 18 ideias no catálogo)** | `MarketingService` |

**A16, A17 e A20 foram aprovadas pela equipe em 27/10.** O catálogo de marketing passou de 12 para 18 ideias (as 6 novas estão comentadas em `CatalogoIdeias`); releia-as com o cliente. Na A17, o copo continua entre os ingredientes, porque no banco ele é um: pelo custo, fica em 4º.

```powershell
.\mvnw test "-Dtest=DashboardTest,MarketingTest,OtimizadorDesligadoTest"
```

**Você deve ver:** 5 + 4 + 3 testes, sem falhas. O `OtimizadorDesligadoTest` liga o perfil do Render (`app.otimizador.habilitado=false`) e prova que produção e reposição respondem `503` **sem chamar o Python**, enquanto o resto funciona. O `DashboardTest` usa um cenário **calculado à mão**, comentado no topo da classe:
- outubro: 83,40 em 3 pedidos;
- ranking Dono 44,70 e Func 38,70;
- um pedido cancelado que não conta;
- um pedido às 23h30 de 31/10 que conta em outubro.

**Manual com o seed de demonstração** (números conferidos em SQL no tutorial do Diogo, §8):
- `GET /api/dashboard` → faturamento **326,40**, ranking Func 213,00 e Dono 113,40;
- depois de gerar a recomendação, `GET /api/marketing/sugestoes` → **2** sugestões (Tradicional e Especial).

## 7. Toda a suíte, Testcontainers e Docker

```powershell
$env:MF_PYTHON = "C:\caminho\Mente_Fria\optimization\.venv\Scripts\python.exe"     # opcional
.\mvnw test
```

**Você deve ver** (depois de todas as fases, inclusive a 5 do Kauê): `Tests run: 95, Failures: 0, Errors: 0` com `MF_PYTHON`. Sem a variável, as classes `PythonRealTest` e `ProcessoPythonTest` aparecem como puladas.

**Como a `IntegracaoBase` funciona (A25):**
- **Um** PostgreSQL 16 para todas as classes (iniciado uma vez);
- schema **recriado antes de cada teste** a partir do `bd/Script_DDL.sql`;
- um Administrador e um Funcionário criados **pela API**;
- **relógio ajustável** em São Paulo (`relogio.em(...)`);
- os dois módulos Python trocados por **dublês** (`@MockitoBean`).

Os testes terminam em `Test`, e não em `IT`, porque o Surefire padrão ignora `*IT`.

> ⚠️ **Precisa do Docker Desktop funcionando.** Se ele mostrar *"starting services: … rename … .sock … The file cannot be accessed by the system"* (o BD01; decisão **A26**):
> 1. **Nunca** clique em "Reset to factory defaults": apaga o `mente-fria-db`. Clique em **Quit**.
> 2. `wsl --shutdown`.
> 3. Renomeie as pastas `%LOCALAPPDATA%\Docker\run` e `%LOCALAPPDATA%\docker-secrets-engine` (ex.: `run.antiga`). Apagar não funciona; renomear a pasta, sim.
> 4. Abra o Docker Desktop de novo.

## 8. Deploy (US20) — sua parte, com o Kauê

O passo a passo do Render está no tutorial do **Kauê, §12**; o do GitHub Pages, no do **Leonardo, §9**. Você é o **dono da conta do Render** (A43) e o PO: toda ação no painel é sua, e o Kauê guia pelo §12 dele. As credenciais da conta são só suas. Sua parte:

**Por que o deploy definitivo é na seg 02/11 (A41; antes: sáb 31, A21):** o banco gratuito do Render expira **30 dias depois de criado**. Criado em 02/11, vale até **02/12**, depois de 30/11 (data-limite do professor) e da semana de bugs. O Render fica 4 dias em uso antes da entrega de 06/11, o que dispensa o ensaio numa conta pessoal.

1. **Primeira validação no Render (seg 02/11, logo depois do deploy definitivo):** *(antes: ensaio numa conta pessoal na qui 29 ou sex 30; dispensado pela A41)*.
   - Conferir as variáveis no painel do Render: `DB_*` vêm do banco; `JWT_SECRET` é gerado; `CORS_ALLOWED_ORIGIN` = a origem do Pages, **sem barra no fim**.
   - Repetir a validação da Fase 2 **na URL do Render** (troque `http://localhost:8080` pela URL).
   - Cada conta só pode ter um banco gratuito ativo: **não crie outro banco** na conta definitiva.
   - Os problemas mais prováveis (memória, tempo de subida, CORS) já foram simulados na máquina, mas o Render real não: há folga até qua 04/11 para corrigir.
2. **Sáb 31/10 (A43):** criar a conta do Render (só a conta; o banco e o serviço só na seg 02/11).
3. **Licença (A38, 28/10):** a Gurobi autorizou **por escrito** o uso da **licença restrita do `pip`** no Render; a WLS do Kauê **não** pode ir para uma URL pública. No Render: `OTIMIZADOR_HABILITADO=true` e **nenhuma** variável `GRB_*`. O plano B1 (A31) fica como **reserva**: se o solver der problema no Render (memória, tempo esgotado), troque a variável para `false` no painel. Como PO, na conferência de seg 02/11 (A42), confirme que o deploy vai com o solver ligado. *(Antes da A38: "não autorizado até a Gurobi confirmar"; resposta em `sprint-3-licenca-gurobi.md`, §0.)*
4. **Seg 02/11, à tarde: deploy definitivo** na sua conta, pelo *blueprint*, com as Fases 1–3 na `main` (A41); o que entrar depois é publicado sozinho. Anotar no README a **data de expiração do banco** (02/12, no mesmo horário da criação). Na ter 03/11, conferir CT69, CT70 e **CT72**; na qua 04/11, com todas as telas, o fluxo completo do CT68. Com a A38, a recomendação (151/1/0 com o seed), a reposição e o marketing **funcionam na URL**. O CT72 confere a reserva: com a variável em `false`, eles mostram o aviso do `503` (opcional no Render; depois, volte para `true`).

**O que a simulação local mostrou** (container com 512 MB e 0,1 CPU):

| Medida | Resultado |
|---|---|
| Subida | 83 s |
| Primeiro login | ~7 s |
| Recomendação (Gurobi dentro do container) | 3,1 s |
| Pico de memória | 133 MiB |
| `/api/teste/python` | `401` |
| CORS com origem do GitHub Pages | Funciona |

O plano free **dorme após 15 min**: antes de uma demonstração, abra a URL uns 2 minutos antes.

## 9. Entrega

1. Um PR por fase, com Conventional Commits, revisão de um colega e a saída do `mvnw test` daquela fase.
2. **README:**
   - `JWT_SECRET`, `CORS_ALLOWED_ORIGIN`, `PYTHON_CMD`, `OTIMIZADOR_SCRIPT` e `REPOSICAO_SCRIPT` passam para "obrigatórias"; acrescentar `OTIMIZADOR_HABILITADO` (padrão `true`; `true` também no Render desde a A38; `false` só para ligar a reserva B1, A31) e `OTIMIZADOR_TIMEOUT_SEGUNDOS` (padrão 10);
   - o perfil `dev`;
   - como rodar `mvnw test` (Docker ligado; `MF_PYTHON` opcional);
   - o `.env` sem BOM;
   - **a URL publicada e a data de expiração do banco** (A21);
   - apagar o "Estado atual (corte da Sprint 2)".
3. **Contrato v2:** já aprovado e na `main` (A10); confira que nada mudou em relação ao código que você copiou.
4. **Evidências** em `docs/Sprint_4/evidencias/`.

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` | Variável não definida, `.env` com BOM, ou backend fora de `backend/` | §2.1 |
| `package org.postgresql.util does not exist` | Driver com `scope runtime` | §2.2 (A4) |
| `NoUniqueBeanDefinitionException … CorsConfigurationSource` | Bean de CORS genérico | §2.4 (A5) |
| `Ambiguous mapping` ao subir os testes de integração | `RotasDeTesteController` sem o `@Profile("teste-seguranca")` | §2.5 (A24) |
| Testes de integração não rodam (0 testes) | Classe terminando em `IT` | Terminar em `Test` (A25) |
| `cannot find symbol … OtimizadorProducao` na `IntegracaoBase` | Faltam os 4 arquivos de infraestrutura | §3.1 |
| `listaIngredientesCompletaComEstoqueZero` falha com 40 | O `bd/Script_DDL.sql` ainda é o antigo | Esperar o PR do banco do Diogo |
| `Could not find a valid Docker environment` | Docker parado ou travado | §7 (A26) |
| `blocked by CORS policy` (funciona no Postman) | `localhost` × `127.0.0.1`, ou origem errada no Render | `CORS_ALLOWED_ORIGIN` |
| `500` onde devia ser `400` | Faltam os handlers da A7 | `GlobalExceptionHandler` de `referencia/` |
| Python "sem licença" quando chamado pelo Java | `gurobipy` não instalado no `PYTHON_CMD` usado | Apontar `PYTHON_CMD` para o Python do `venv` |
| Acentos quebrados no eco | Entrada não escrita em UTF-8 | `ProcessoPython` de `referencia/` |
| Dois pedidos vendem além do estoque | Faltou o `FOR UPDATE` | `travar(idsOrdenados)` antes de ler as linhas |
| `EmptyResultDataAccessException` no login | `queryForObject` em `buscarPorEmail` | `query(...).stream().findFirst()` |

## Checklist final

- [ ] Fase 1: `pom.xml.diff` aplicado; classes de `referencia/`; **18/18**
- [ ] Fase 2: **15/15** e 13/13; os 10 itens do gate; o Leonardo fez o aceite
- [ ] Fase 3: **15/15**; `hoje` e `agora` do `Clock` em todo SQL de data
- [ ] Fase 4A: **7/7**; com `MF_PYTHON`, eco e 151/1/0 com o Python real
- [ ] Fase 6: **5/5** e **4/4**; A16, A17 e A20 confirmadas
- [ ] Revisei o PR da Fase 5 do Kauê; a suíte completa dá **95**
- [ ] Deploy: validação da Fase 2 na URL do Render (seg 02/11); CT69, CT70 e CT72 (ter 03/11); CT68 completo (qua 04/11)
- [ ] README (URL e expiração do banco), Contrato v2 conferido, evidências

## Fora do escopo obrigatório

Conforme a decisão A2b e, desde 28/10, a A40: **US26** (`POST`/`PATCH` de unidades) só entra se passar no *gate* da seg 02/11, de manhã; quem a faz é o **Diogo** (A43), e você revisa os dois PRs dela (ter 03 e qua 04/11). Ficam **fora do semestre**: cancelamento de pedido (US27, **não devolve estoque**), alertas completos (US15/US16), CSV (US19) e clientes (US13).
