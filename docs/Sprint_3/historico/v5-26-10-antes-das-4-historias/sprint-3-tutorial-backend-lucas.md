# Tutorial — Sprint 3 (Backend) — Lucas

**Para:** Lucas Gabriel Valadares Bassi · **Papel:** PO e backend · **Revisão:** 26/10/2026 (auditoria) · **Substitui** `historico/Tutorial_Sprint3_Backend_Lucas.md`.
**Base:** `sprint-3-roteiro.md` (v5), `sprint-3-contrato-v2-proposta.md`, `sprint-3-decisoes-e-pendencias.md`.

> **Leia primeiro: o que foi verificado e o que não foi.**
>
> - **Fase 1 (segurança, CORS, erros): VERIFICADA em execução em 26/10.** O código em `docs/Sprint_3/referencia/backend/` compila no Boot 4.0.8 / JDK 25 e passa **18 de 18** testes MockMvc.
> - **O tutorial de 14/10 tinha 3 defeitos que impediam o uso**, todos já corrigidos aqui:
>   1. não compilava (A4);
>   2. a aplicação não subia por causa do CORS (A5);
>   3. alguns erros `400` viravam `500` (A7).
> - **Fases 2, 3 e 4A: ponto de partida NÃO compilado.** O SQL e as regras foram executados em PostgreSQL 16; o Java dessas fases ainda não. Compile, rode os testes e corrija o que o compilador apontar.
> - **Persistência:** Spring JDBC com `NamedParameterJdbcTemplate` (decisão D4). **Não use JPA, Hibernate, `@Entity` nem `JpaRepository`.**
> - **Sem meta de cobertura** (Plano de Testes, §2). O critério é: cada regra do requisito mínimo tem teste do caminho principal.
>
> Se algo não bater com a sua tela, **pare** e mande a mensagem exata no grupo. Não tente adivinhar.

## 0. Onde você está e a ordem do seu trabalho

| Item | Estado em 26/10 (verificado) |
|---|---|
| `.env` | **Feito** (seu PR #24): `application.properties` lê `./.env`; rode sempre **de dentro de `backend/`** |
| Backend | Só `GET /api/saude` e `GET /api/ingredientes` (3 campos) |
| `pom.xml` | `jdbc`, `webmvc`, `postgresql` (**`scope runtime`, que precisa sair: A4**) e os starters de teste |
| Banco novo | O Diogo aplica o patch em 26–27/10. Depois disso, **recrie o seu banco** |

| Fase | Entrega | Quando | Depende de | Libera |
|---|---|---|---|---|
| 1 | Segurança, JWT, CORS, erros (18 testes) | **Seg 26 – Ter 27** | Nada | Todos |
| — | **Decisão A3** (PO, com o professor) | **Ter 27** | — | O plano de testes |
| 2 | Auth, usuários, unidades, ingredientes, produtos, preço | Qua 28 – Qui 29 | Fase 1; repositórios do Diogo | Gate do Leonardo |
| 3 | Estoque por linhas, pedido | Qui 29 – Sex 30 | Fase 2; Contrato v2 aprovado (A10); banco novo | Telas do Leonardo |
| 4A | Ponte com o Python e recomendação (em dupla com o Kauê) | Sex 30 | Fase P do Kauê; Fase 3 (saldo válido) | Tela 12 |

**Um PR por fase, no dia.** Desde 14/10 nada foi para o GitHub. A revisão por um colega é obrigatória (README, "Convenções").

## 1. Por que algumas coisas são assim

| O que | Por quê |
|---|---|
| `hoje` e `agora` vêm de um `Clock` em `America/Sao_Paulo` e vão como parâmetro nos SQLs (F3, A9) | Pela documentação da equipe, o Render roda em UTC. Das 21h à meia-noite, a validade e a data do pedido cairiam no dia (e no mês) seguinte |
| CORS **dentro** do filtro de segurança, com `PATCH` | A segurança roda antes do MVC; sem isso, o *preflight* (`OPTIONS`) leva `401`. O `PATCH` é da US26 |
| Bean de CORS do tipo **`UrlBasedCorsConfigurationSource`** (A5) | O Spring MVC já registra outro `CorsConfigurationSource`. Pedir o tipo genérico gera `NoUniqueBeanDefinitionException` e **a aplicação não sobe** (verificado) |
| `401`/`403` escritos no `SecurityConfig`, e os demais erros no `@RestControllerAdvice` | Erros de autenticação acontecem **antes** do controller, então o `@RestControllerAdvice` não os vê. Por isso há dois lugares, com o mesmo formato `{mensagem}` |
| Driver PostgreSQL sem `scope runtime` (A4) | O tradutor de erros usa a classe `PSQLException` do driver. Com `runtime`, o projeto **não compila** (verificado) |
| `/api/teste/python` só com `@Profile("dev")` (F4) | Sem controle de acesso, a rota não pode existir em produção |
| Validação **manual** (não Bean Validation) | Devolve **todos** os campos inválidos de uma vez, com as mensagens fixas do `mapa_constraints.md` (decisão de 14/10) |
| `NamedParameterJdbcTemplate` e não `JdbcClient` | Decisão D4. A skill de JDBC recomenda `JdbcClient`, mas trocar agora é retrabalho sem ganho |

## 2. Fase 1 — Fundação (segurança, JWT, CORS, erros)

**Objetivo:** toda rota, exceto as públicas, exige token; toda resposta de erro sai em JSON no formato do contrato.

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

> ⚠️ **Codificação do `.env`:** salve **sem BOM**. No VS Code, escolha "UTF-8" (não "UTF-8 with BOM"). No PowerShell 5.1, use `-Encoding ascii`.
>
> **Por quê:** o Spring lê o `.env` como ISO-8859-1. O BOM "gruda" na primeira variável e ela deixa de ser reconhecida (verificado no código do Spring Boot 4.0.8).

Acrescente ao `backend\.env.example`, **sem valores**:

```
JWT_SECRET=
CORS_ALLOWED_ORIGIN=http://127.0.0.1:5500,http://localhost:5500
PYTHON_CMD=python
OTIMIZADOR_SCRIPT=../optimization/app/otimizador.py
```

**Você deve ver:** `.\mvnw spring-boot:run` → `Started BackendApplication`, e `Invoke-RestMethod http://localhost:8080/api/saude` → `status ok, banco ok`. **Se falhar** com `Could not resolve placeholder`: você não está dentro de `backend/`, ou o `.env` tem BOM.

### 2.2 `pom.xml`

O diff **exato** e validado está em `docs/Sprint_3/referencia/backend/pom.xml.diff`. São duas mudanças:

1. **No driver PostgreSQL, apague a linha `<scope>runtime</scope>`** (A4).
2. Acrescente, dentro de `<dependencies>`:

```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.13.0</version></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.13.0</version><scope>runtime</scope></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.13.0</version><scope>runtime</scope></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security-test</artifactId><scope>test</scope></dependency>
```

(No Boot 4, o artefato de teste é `spring-boot-starter-security-test`, que já traz o `spring-security-test`.)

**Valide a árvore de dependências:**

```powershell
.\mvnw -q dependency:tree "-Dincludes=com.fasterxml.jackson.core,tools.jackson.core,io.jsonwebtoken"
```

**Você deve ver** (resultado de 26/10): `tools.jackson.core:jackson-databind:3.1.5` (do Boot) **e** `com.fasterxml.jackson.core:jackson-databind:2.21.5` (trazido pelo `jjwt-jackson`).

**Isso é esperado e não é conflito:** são pacotes diferentes, e o jjwt ainda não tem módulo para o Jackson 3.

### 2.3 `application.properties` (acrescente)

```properties
app.jwt.secret=${JWT_SECRET}
app.jwt.validade-horas=2
app.cors.allowed-origins=${CORS_ALLOWED_ORIGIN:http://127.0.0.1:5500,http://localhost:5500}
app.python.cmd=${PYTHON_CMD:python}
app.otimizador.script=${OTIMIZADOR_SCRIPT:../optimization/app/otimizador.py}
app.otimizador.timeout-segundos=10
```

Em `.properties`, a `\` é caractere de escape: caminho do Windows usa `/` ou `\\`.

### 2.4 As classes

**Copie** de `docs/Sprint_3/referencia/backend/src/main/java/com/mentefria/backend/` para o mesmo caminho em `backend/src/main/java/com/mentefria/backend/`:

| Arquivo | O que faz | Detalhe importante |
|---|---|---|
| `config/ApiException.java` | Erro de negócio com status e `campos` | — |
| `config/RelogioConfig.java` | `Clock` em `America/Sao_Paulo` | O trecho de 14/10 vinha **sem `package` e sem `import`** e não compilava. O arquivo de `referencia/` está completo |
| `auth/JwtService.java` | Gera e valida o token (HS256, `JWT_SECRET` com 32+ bytes) | Falha **ao subir** se o segredo for curto (falhar cedo é bom) |
| `auth/JwtAuthFilter.java` | Lê `Authorization: Bearer` e autentica | **Sem `@Component`**: é criado dentro do `SecurityConfig`, para não ser registrado duas vezes |
| `config/SecurityConfig.java` | Regras por perfil, CORS, `401`/`403` em JSON | Bean de CORS do tipo `UrlBasedCorsConfigurationSource` (A5) |
| `config/GlobalExceptionHandler.java` | Traduz erros em JSON `{mensagem, campos}` | Inclui os handlers de `400` e `403` que faltavam (A7) |

**Leia cada arquivo antes de copiar:** você vai defender esse código na Review. O que cada parte do `GlobalExceptionHandler` faz:

| Exceção | HTTP | Mensagem | Por quê |
|---|---|---|---|
| `ApiException` | o da exceção | a da exceção, mais os `campos` | Regras de negócio (ex.: `409` "O cadastro do Administrador já foi realizado.") |
| `DataIntegrityViolationException` | `400`/`409`/`422`, conforme o `mapa_constraints.md` | "Dados inválidos" e o campo | Rede de segurança: o banco recusou. A chave é o **nome da constraint**; no `NOT NULL`, é `NN:tabela.coluna`. **Verificado** que o PostgreSQL 16 informa tabela e coluna no erro 23502, constraint nos erros 23503, 23505 e 23514 |
| `HttpMessageNotReadableException` | `400` | "Requisição inválida." | JSON quebrado |
| `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException` | `400` | "Requisição inválida." | **A7:** sem isso, `/api/produtos/abc/preco` dava `500` (verificado) |
| `AccessDeniedException` | `403` | "Você não tem permissão para esta ação." | Se um dia usarem `@PreAuthorize`, a negação chega aqui e não no filtro |
| `NoResourceFoundException` | `404` | "Recurso não encontrado." | Sem esse handler, o genérico transformaria o `404` em `500`. Era o erro BE03 da Sprint 2 |
| `HttpRequestMethodNotSupportedException` | `405` | "Método não permitido." | — |
| `Exception` (qualquer outra) | `500` | "Não foi possível concluir a operação." | O *stack trace* vai **só para o log**, nunca para a resposta (verificado: o corpo não contém o texto da exceção) |

Quem permite o quê (Contrato, §2.2), já no `SecurityConfig`:

| Quem | Rotas |
|---|---|
| Público | `GET /api/saude`, `POST /api/auth/login`, `POST /api/auth/registro`; `/api/teste/**` **só no perfil `dev`** |
| Administrador | `/api/usuarios/**`, `/api/unidades-medida/**`, `/api/producao/**`, `/api/reposicao/**`, `/api/marketing/**`, `/api/dashboard/**`; `POST`, `PUT` e `PATCH` em `/api/ingredientes/**` e `/api/produtos/**` |
| Admin e Funcionário | O resto: `GET /api/ingredientes`, `GET /api/produtos`, `/api/estoque/**`, `/api/clientes/**`, `/api/pedidos/**` |

Se existir algum `CorsConfig` antigo (`WebMvcConfigurer`), **apague-o**: dois CORS dão resultado imprevisível.

### 2.5 Os testes da Fase 1 (escreva ou copie ANTES de considerar a fase pronta)

Copie `referencia/backend/src/test/java/com/mentefria/backend/config/` (`SegurancaEErrosTest.java` e `RotasDeTesteController.java`) para `backend/src/test/java/com/mentefria/backend/config/`.

- O `RotasDeTesteController` existe **só nos testes**: simula as rotas que você ainda vai criar, para testar segurança e erros sem banco.
- **Quando os controllers reais existirem (Fase 2),** troque as rotas simuladas pelos controllers reais no `@WebMvcTest(controllers = …)` e apague as rotas simuladas que conflitarem (ex.: `/api/usuarios`).

```powershell
.\mvnw test "-Dtest=SegurancaEErrosTest"
```

**Você deve ver:** `Tests run: 18, Failures: 0, Errors: 0` (o resultado de 26/10). Os 18 testes cobrem:

- `GET /api/saude` público (`200`);
- sem token, token inválido, token expirado e rota inexistente sem token → **`401` JSON**;
- rota inexistente com token → `404` JSON;
- `/api/teste/python` sem o perfil `dev` → `401`;
- Funcionário em `/api/usuarios` e em `PUT .../preco` → **`403` JSON** (CT05, CT08); Funcionário em `/api/estoque` → `200`;
- Administrador em `PUT .../preco` → `200`;
- JSON malformado, id não numérico e parâmetro ausente → **`400` JSON**;
- erro inesperado → `500` **sem detalhe interno**;
- *preflight* do `127.0.0.1:5500` com `PATCH` → `200` com `Access-Control-Allow-Origin`; origem desconhecida → `403`.

**Se falhar, compare com o resultado da auditoria:**

| Sintoma | Causa | O que fazer |
|---|---|---|
| `package org.postgresql.util does not exist` | Driver com `scope runtime` | §2.2, mudança 1 |
| `NoUniqueBeanDefinitionException … CorsConfigurationSource` (a aplicação não sobe) | Bean de CORS declarado com o tipo genérico | Use o `SecurityConfig` de `referencia/` |
| `expected:<400> but was:<500>` em `idNaoNumericoNaRotaDa400` | Faltam os handlers da A7 | Use o `GlobalExceptionHandler` de `referencia/` |
| `cannot find symbol WebMvcTest` | Pacote do Boot 4 | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` |
| `@MockBean` não existe | Foi removido no Boot 4 | `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`) |
| O `BackendApplicationTests.contextLoads` falha | Precisa do banco no ar e do `JWT_SECRET` | Banco ligado e `.env` correto; ou rode só a classe de segurança |

### 2.6 Validação manual (com o backend rodando)

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

**Evidência da Fase 1:** a saída do `mvnw test` e o print dessa chamada, em `docs/Sprint_3/evidencias/`. Commit: `feat: seguranca JWT, CORS e tratamento padrao de erros`.

> A partir da Fase 1, o frontend não consegue mais listar ingredientes sem login, e o login é da Fase 2. **Faça o merge da Fase 1 junto com a Fase 2, ou logo antes dela**, e avise o Leonardo.

## 3. Fase 2 — Autenticação e CRUD (US1–US7)

**Pré-requisito:** as **assinaturas dos repositórios** combinadas com o Diogo (tutorial dele, §7). Ele cria os `*Repository`; você, os serviços e controllers. Branch: `feature/backend-auth-crud`.

### 3.1 Regras de validação

Na aplicação, antes do banco. Devolva **todos** os campos inválidos de uma vez.

| Endpoint | Regras |
|---|---|
| `POST /api/auth/registro` | Se já existe Administrador → `409` (**checar primeiro**, CT01B). Nome obrigatório. E-mail obrigatório, válido e **normalizado** (`trim` + minúsculas). Senha obrigatória com 8+ caracteres (CT02). O perfil é **sempre** `ADMINISTRADOR` e nunca vem do corpo |
| `POST /api/auth/login` | E-mail inexistente, senha errada ou usuário inativo → **o mesmo** `401 "E-mail ou senha inválidos."` (CT07). A resposta tem **`token`, `perfil` e `nome`** |
| `POST /api/usuarios` | Igual ao registro, com perfil `FUNCIONARIO`. E-mail duplicado → `409` (constraint `uq_usuario_email`). Resposta `201 {id, nome, email, perfil}` |
| `GET /api/usuarios` | `[{id, nome, email, ativo}]`, nunca `senha_hash` |
| `GET /api/unidades-medida` | `[{unidade_medida, porcao_padrao}]` |
| `GET /api/ingredientes` | **Registro completo** (Contrato v2, §2.3), ordenado por `nome`. **Apague o `IngredienteResumo`** |
| `POST`/`PUT /api/ingredientes` | Nome; unidade cadastrada; `custo ≥ 0`; `porção > 0`; `mínima ≥ 0`. `quantidade_estoque` **nunca** vem do corpo. `PUT` com 0 linhas afetadas → `404` |
| `POST /api/produtos` | Nome; preço `> 0`; composição não vazia (CT13); **sem ingrediente repetido** (testar **antes** de "não encontrado"); ingredientes existentes; quantidades `> 0`; **tem açaí** (CT15). Produto e composição na **mesma transação** |
| `PUT /api/produtos/{id}/preco` | Preço `> 0`; 0 linhas afetadas → `404` |

### 3.2 `AuthController`

Ponto de partida, não compilado. Usa `UsuarioRepository` do Diogo, com `buscarPorEmail` devolvendo `Optional`.

```java
package com.mentefria.backend.auth;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.usuario.Usuario;
import com.mentefria.backend.usuario.UsuarioRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegistroRequest(String nome, String email, String senha) {}
    public record LoginRequest(String email, String senha) {}
    private final UsuarioRepository usuarios; private final JwtService jwt; private final PasswordEncoder encoder;
    public AuthController(UsuarioRepository u, JwtService j, PasswordEncoder e) { usuarios = u; jwt = j; encoder = e; }

    static String normalizar(String email) { return email == null ? "" : email.trim().toLowerCase(); }

    @PostMapping("/registro") @Transactional
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody RegistroRequest req) {
        if (usuarios.existeAdministrador()) throw new ApiException(409, "O cadastro do Administrador já foi realizado.", null);
        String email = normalizar(req.email());
        Map<String, String> erros = new LinkedHashMap<>();
        if (req.nome() == null || req.nome().isBlank()) erros.put("nome", "Informe o nome.");
        if (email.isEmpty()) erros.put("email", "Informe o e-mail.");
        else if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) erros.put("email", "Informe um e-mail válido.");
        if (req.senha() == null || req.senha().isEmpty()) erros.put("senha", "Informe a senha.");
        else if (req.senha().length() < 8) erros.put("senha", "A senha deve ter no mínimo 8 caracteres.");
        if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
        int id = usuarios.inserir(req.nome().trim(), email, encoder.encode(req.senha()), "ADMINISTRADOR");
        return ResponseEntity.status(201).body(Map.of("id", id, "nome", req.nome().trim(), "email", email, "perfil", "ADMINISTRADOR"));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        var invalido = new ApiException(401, "E-mail ou senha inválidos.", null);
        if (req.email() == null || req.senha() == null) throw invalido;
        Usuario u = usuarios.buscarPorEmail(normalizar(req.email())).orElseThrow(() -> invalido);
        if (!u.ativo() || !encoder.matches(req.senha(), u.senhaHash())) throw invalido;
        return Map.of("token", jwt.gerarToken(u.id(), u.perfil()), "perfil", u.perfil(), "nome", u.nome());
    }
}
```

**Dois cuidados que a revisão de JDBC apontou:**
- `buscarPorEmail` **não** pode usar `queryForObject`, porque lança `EmptyResultDataAccessException` quando não acha. Use `jdbc.query(...).stream().findFirst()`.
- Para saber **quem** fez a ação (movimentação, pedido), use `Integer.parseInt(authentication.getName())`. O `subject` do token é o id do usuário.

### 3.3 Usuários, unidades, ingredientes

- Controller fino → validação → repositório.
- `BigDecimal` (nunca `double`) em custo, porção e quantidades; `@JsonProperty("custo_unitario") BigDecimal custoUnitario` (o pacote `com.fasterxml.jackson.annotation` continua valendo no Jackson 3).
- `POST` → `201` com o objeto completo; `PUT` → `200`.
- Listagens com `@Transactional(readOnly = true)` no serviço.
- **O estoque agora é 0** no seed oficial: o esperado é a tela mostrar 0.

### 3.4 Produtos: composição, açaí e transação

```java
@Transactional
public int criar(String nome, BigDecimal preco, List<ItemComposicao> composicao) {
    Map<String, String> erros = new LinkedHashMap<>();
    if (nome == null || nome.isBlank()) erros.put("nome", "Informe o nome do produto.");
    if (preco == null || preco.signum() <= 0) erros.put("preco_venda", "O preço deve ser maior que zero.");
    if (composicao == null || composicao.isEmpty()) {
        erros.put("composicao", "Adicione ao menos um ingrediente.");                                 // CT13
    } else {
        List<Integer> ids = composicao.stream().map(ItemComposicao::ingredienteId).toList();
        if (ids.stream().distinct().count() < ids.size()) erros.put("composicao", "Este ingrediente já foi adicionado ao produto.");
        else if (composicao.stream().anyMatch(i -> i.quantidadeUtilizada() == null || i.quantidadeUtilizada().signum() <= 0))
            erros.put("composicao", "A quantidade utilizada deve ser maior que zero.");             // CT14
        else {
            Map<Integer, String> nomes = ingredientes.buscarNomesPorIds(ids);
            if (nomes.size() < ids.size()) erros.put("composicao", "Ingrediente não encontrado.");
            else if (!RegraAcai.composicaoTemAcai(List.copyOf(nomes.values())))
                erros.put("composicao", "O produto precisa ter o açaí entre os ingredientes.");      // CT15
        }
    }
    if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
    int produtoId = produtos.inserir(nome.trim(), preco);
    produtos.inserirComposicao(produtoId, composicao);   // mesma transação: se falhar, desfaz o produto também
    return produtoId;
}
```

**Correção em relação a 14/10:** a classe `RegraAcai` **não existe pronta**.
- O `docs/Apoio/sql_repositorios.md` (§4) tem os métodos estáticos `ehIngredienteAcai` e `composicaoTemAcai` dentro de uma `ClasseDoService` de exemplo.
- A tabela de casos lá tem **9 linhas e 12 nomes** (o texto diz "15 casos").

**Crie** `produto/RegraAcai.java` com esses dois métodos e um teste unitário parametrizado:
- aceitos: "Açaí (polpa)", "AÇAÍ ZERO", "polpa de acai";
- recusados: "Granola", "Leite condensado", `""` e `null`.

`GET /api/produtos` devolve `composicao[]` com `ingrediente_id`, `ingrediente_nome`, `quantidade_utilizada` e `unidade_medida`. Agrupe as linhas produto × ingrediente com um `ResultSetExtractor` num `LinkedHashMap`.

### 3.5 Validar a Fase 2 (gate)

| # | Chamada | Esperado | CT |
|---|---|---|---|
| 1 | `Chamar POST http://localhost:8080/api/auth/registro @{nome="Dono";email=" DONO@Acai.com ";senha="senha1234"}` | `201`; e-mail gravado como `dono@acai.com` | CT01 |
| 2 | O mesmo, de novo | `409` "O cadastro do Administrador já foi realizado." | CT01B |
| 3 | Login correto | `200` com `token`, `perfil` e `nome` | CT06 |
| 4 | Login com senha errada **e** com e-mail inexistente | `401`, **a mesma** mensagem nos dois | CT07 |
| 5 | `GET /api/ingredientes` com token | `200`; 7 itens; **todos os campos**; estoque 0 | CT09 |
| 6 | Criar Funcionário e entrar como ele; `POST /api/ingredientes` com o token dele | `201`; depois **`403`** | CT04, CT05 |
| 7 | `POST /api/ingredientes` com `custo_unitario: -1` e `porcao_padrao: 0` | `400` com **os dois** campos em `campos` | CT10, CT11, CT18 |
| 8 | `POST /api/produtos` sem açaí / vazio / com ingrediente repetido | `400`: CT15 / CT13 / "já foi adicionado" | CT13, CT15 |
| 9 | `POST /api/produtos` válido e depois `GET /api/produtos` | `201`; a lista traz a composição | CT12 |
| 10 | `PUT /api/produtos/1/preco` com `{preco_venda: 0}` e depois com `13.50`; o mesmo como Funcionário | `400` e `200`; Funcionário → `403` | CT16, CT17, CT05 |

**Gate:** com os 10 passando, **avise o Leonardo** (o aceite real é dele, tutorial §5). Só então a Fase 2 conta como concluída.

**Testes automáticos da Fase 2:**
- unitário: `RegraAcai` e normalização de e-mail;
- MockMvc: troque as rotas simuladas pelos controllers reais;
- integração (§6): CT10, CT11, CT14 e CT17 (constraints pelo backend).

## 4. Fase 3 — Estoque por linhas e pedido (US9, US12)

**Pré-requisitos:**
- Contrato v2 aprovado (A10);
- banco **recriado** com o schema novo do Diogo;
- repositórios `MovimentacaoRepository`, `PedidoRepository` e `IngredienteRepository.travar` do Diogo.

Branch: `feature/backend-estoque-pedidos`.

### 4.1 O que o código tem de reproduzir

Tudo já provado em SQL (PostgreSQL 16) pela auditoria:

| Cenário | Resultado esperado |
|---|---|
| S1: linha vencida de 20 + linha válida de 20; pedido de 5 | Total **35**, válido **15**, vencido **20**; após descartar 20: total **15** |
| S2: A=20 (vence 20/10), sai 10; entra C=20 (vence 15/10); sai 5 | C=15 e A=10 (a saída sai da **menor validade**) |
| S3: linhas 3 (14/10), 4 (20/10) e 10 (sem validade); saída de 6, depois de 5 | `0/1/10` e depois `0/0/6` (**sem validade por último**) |
| S4: só há linha vencida; pedido de 1 | **`422`**; nada muda |
| S5: descarte maior que o vencido | **`422`**; o descarte exato funciona |
| Concorrência: dois pedidos de 30 sobre 50 | **Um** passa, o outro recebe `422`; saldo 20 (30/30 rodadas); sem *deadlock* com a ordem invertida |
| Invariante | `quantidade_estoque` = soma dos restantes, após cada operação |

Esses cenários viram testes de integração (§6). O SQL de cada passo está em `docs/Sprint_3/referencia/bd/consultas_sprint3.sql` e, depois, no `sql_repositorios_sprint3.md` do Diogo.

### 4.2 Baixa por linhas

Usada pelo pedido, pela saída convencional e pelo descarte.

```java
@Component
public class BaixaPorLinhas {
    private final MovimentacaoRepository movs; private final IngredienteRepository ings;
    public BaixaPorLinhas(MovimentacaoRepository m, IngredienteRepository i) { movs = m; ings = i; }

    /** Consome 'qtd' das linhas já carregadas e travadas (FOR UPDATE), na ordem recebida. Chamar DENTRO de @Transactional. */
    public void consumir(int ingredienteId, List<MovimentacaoRepository.Linha> linhas, BigDecimal qtd, String motivo,
                         int usuarioId, LocalDateTime agora, String obs, Supplier<ApiException> semEstoque) {
        BigDecimal disponivel = linhas.stream().map(MovimentacaoRepository.Linha::restante).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (disponivel.compareTo(qtd) < 0) throw semEstoque.get();
        BigDecimal falta = qtd;
        for (var linha : linhas) {
            if (falta.signum() <= 0) break;
            BigDecimal tira = linha.restante().min(falta);
            movs.consumirLinha(linha.id(), tira);
            falta = falta.subtract(tira);
        }
        ings.baixarSaldo(ingredienteId, qtd);
        movs.inserirSaida(ingredienteId, usuarioId, qtd, motivo, agora, obs);      // A9: a hora vem do Java
    }
}
```

### 4.3 `POST /api/estoque/movimentacoes`

Tudo numa **única** transação, com `hoje = LocalDate.now(clock)` e `agora = LocalDateTime.now(clock)`. Formato no Contrato v2, §2.4.

1. `ingredientes.travar(List.of(id))` (`FOR UPDATE`).
2. **`ENTRADA`:** `inserirEntrada(...)` (a linha nasce com `restante = quantidade`) + `somarSaldo`. `motivo` na entrada → `400`. Entrada com validade já vencida: **aceitar** (proposta; decisão de PO, sua).
3. **`SAIDA`:** `motivo` obrigatório. `CONVENCIONAL` usa `linhasValidas(id, hoje)`; `DESCARTE_VENCIDO` usa `linhasVencidas(id, hoje)`; `PEDIDO` vindo do cliente → `400`. Quantidade acima do disponível → `422` "Estoque insuficiente para o ingrediente X" (CT21), **sem alterar nada**. `data_validade` na saída → `400`.
4. `quantidade ≤ 0` → `400` (CT26). Os dois perfis podem (CT27). `usuario_id` **do token**.

### 4.4 `GET /api/estoque` e `GET /api/estoque/{id}/linhas`

- `ingredientes.listarEstoque(hoje)` → consulta 1. **Você deve ver**, no banco de demonstração do Diogo: Granola `11,160 / válido 5,160 / vencido 6,000`; Morango `1,980` (abaixo do mínimo de 2,000).
- `movs.linhasDoIngrediente(id, hoje)` → consulta 2, com `situacao` (`VENCIDA`, `PROXIMA`, `OK`, `SEM_VALIDADE`).

### 4.5 `POST /api/pedidos` e `GET /api/pedidos`

```java
@Transactional
public PedidoCriado criar(Integer clienteId, List<ItemPedido> itens, int usuarioId) {
    LocalDate hoje = LocalDate.now(clock); LocalDateTime agora = LocalDateTime.now(clock);
    // 1. validar: itens não vazio, sem produto repetido, quantidade inteira > 0 (400, campo "itens")
    // 2. produtos = produtoRepo.buscarPorIds(ids) -> todos existem e estão ATIVOS (senão 400)
    // 3. necessario[ing] += quantidade_utilizada * quantidade (BigDecimal), pela composição
    // 4. ingredientes.travar(idsOrdenados)          -> FOR UPDATE em ORDEM DE ID (evita deadlock: provado)
    // 5. linhas[ing] = movs.linhasValidas(ing, hoje) -> só VÁLIDAS (CT29/CT33), já travadas
    // 6. se algum ingrediente não cobre 'necessario': throw new ApiException(422, "Estoque insuficiente para o produto " + nome, null)
    // 7. total = soma(preço DO BANCO * quantidade); (id, data) = pedidoRepo.inserir(clienteId, usuarioId, total, agora)  // A9
    // 8. pedidoRepo.inserirItens(...) com preco_unitario_registrado = preço do banco (nunca do cliente)
    // 9. para cada ingrediente: baixa.consumir(ing, linhas[ing], necessario[ing], "PEDIDO", usuarioId, agora, "Baixa automática do pedido " + id, ...)
}
```

`GET /api/pedidos`: formato no Contrato v2, §2.5, do mais recente para o mais antigo.

### 4.6 Validar a Fase 3

| # | Chamada | Esperado | CT |
|---|---|---|---|
| 1 | Entrada de 10 kg de açaí com validade futura | `201`; `GET /api/estoque` mostra +10 válido | CT22, CT30 |
| 2 | Saída convencional maior que o válido | `422`; saldo **igual** | CT21, CT33 |
| 3 | Saída com `data_validade` / sem `motivo` | `400` / `400` | CT35 |
| 4 | Descarte com vencido (banco de demonstração) | Só consome a linha vencida; `quantidade_vencida` zera | CT34 |
| 5 | `POST /api/pedidos` com 2× Tradicional | `201`; açaí −0,600, leite condensado −0,060, copo −2; **uma** `SAIDA` por ingrediente, com motivo `PEDIDO` | CT38 |
| 6 | Pedido com produto repetido | `400` | — |
| 7 | Pedido com um item sem estoque válido | `422` e **nada** gravado | CT25 |
| 8 | Funcionário cria pedido e movimentação | `201` | CT27, CT28 |
| 9 | `GET /api/pedidos` | Lista com `itens` e `status` | CT40 |

## 5. Fase 4A — Ponte com o Python e recomendação (em dupla com o Kauê)

**Pré-requisitos:**
- Fase P do Kauê: `otimizador.py` v2 na `main` e a pasta `optimization/exemplos/`;
- seed de demonstração;
- Fase 3 (consulta 3: saldo válido).

### 5.1 Hello World (eco), primeiro

```java
@Profile("dev")                                  // F4: não existe em produção
@RestController @RequestMapping("/api/teste")
public class TestePythonController {
    private final OtimizadorProducao otimizador;
    public TestePythonController(OtimizadorProducao o) { otimizador = o; }
    @GetMapping(value = "/python", produces = "application/json;charset=UTF-8")
    public String eco() { return otimizador.executar("{\"echo\":\"Açaí (polpa)\"}"); }
}
```

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw spring-boot:run
```

**Você deve ver:** `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}`, **com os acentos certos**. Sem o perfil `dev`, a rota → `401` (já testado na Fase 1).

### 5.2 `OtimizadorPython` (implementa `OtimizadorProducao { String executar(String json); }`)

Use `ProcessBuilder` com `app.python.cmd` e `app.otimizador.script`, e cinco cuidados:

1. **Escreva a entrada em UTF-8 sem BOM** e **feche** a `stdin`:
   ```java
   try (var w = new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8)) { w.write(json); }
   ```
2. **`waitFor(timeout)`**: se estourar → `destroyForcibly()` e `500`.
3. Leia a `stdout` em **UTF-8**. A `stderr` vai só para o log.
4. Passe as credenciais `GRB_*` **explicitamente** ao processo filho. O `.env` do Spring **não** chega ao Python (o Diogo comprovou isso na Sprint 2):
   ```java
   for (String nome : List.of("GRB_WLSACCESSID", "GRB_WLSSECRET", "GRB_LICENSEID")) {
       String v = environment.getProperty(nome);
       if (v != null && !v.isBlank()) pb.environment().put(nome, v);
   }
   ```
   Nunca escreva esses valores em log.
5. Dublê `OtimizadorFalso` (devolve o conteúdo de `optimization/exemplos/saida_demo.json`) para os testes, com `@MockitoBean`.

### 5.3 `POST /api/producao/recomendacao` (só Administrador)

1. **Entrada:** consulta 3 (ingredientes dos produtos ativos, **inclusive com 0**, saldo **válido**, custo) + composições. Monte com `Map`/`List` e o `JsonMapper` do Spring (Jackson 3: `tools.jackson.databind.json.JsonMapper`, **não** o `com.fasterxml...ObjectMapper`).
2. **Saída:** desserialize para um `record` com `@JsonProperty`. No Jackson 3, as exceções são **não verificadas** (`JacksonException extends RuntimeException`).
3. `status: "erro"` → `422` com a mensagem; código de saída diferente de 0, timeout ou JSON ilegível → `500` sem *stack trace* (CT44).
4. **Recalcule com `BigDecimal`** a receita, a margem e o `lucro_por_copo`. Se divergir do Python em mais de R$ 0,01, registre um aviso no log e **use o seu valor**.
5. **Resposta:** Contrato v2, §2.6.

### 5.4 Conferir com números verificados

| Estoque válido | Plano (produtos 1 / 2 / 3) | Receita | Lucro |
|---|---|---|---|
| Seed oficial (0) | 0 / 0 / 0 | 0,00 | 0,00 |
| `seed_demo.sql` (açaí 45,75; leite condensado 14,22; **granola 5,16**; banana 11,05; morango 1,98; leite em pó 4,76; copo 269) | **151 / 1 / 0** | **1.966,80** | **1.011,71** |

- Lucro por copo: 6,64 / 9,07 / 10,63.
- O plano 151/1/0 foi confirmado **também por força bruta, sem solver** (26/10).
- **Teste do vencido (CT29/CT42):** capture o JSON enviado ao dublê (`ArgumentCaptor`) e confira que a granola vai com **`5.160`**.
- **Manual:** Administrador → `200`; Funcionário → `403`; `PYTHON_CMD=nao_existe` → `500` sem detalhe; seed oficial → tudo 0.
- **Meça o tempo** e entregue ao Kauê (P6).

## 6. Testes de integração (Testcontainers)

Dependências; **sem `<version>`**, porque o Boot 4.0.8 já gerencia o Testcontainers 2.0.5:

```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-testcontainers</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.testcontainers</groupId><artifactId>testcontainers-postgresql</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.testcontainers</groupId><artifactId>testcontainers-junit-jupiter</artifactId><scope>test</scope></dependency>
```

Para usar **o mesmo** `bd/Script_DDL.sql` (uma só fonte do schema), coloque isto **dentro do `<build>` que já existe**, não num segundo `<build>`:

```xml
<testResources>
  <testResource><directory>src/test/resources</directory></testResource>
  <testResource><directory>${project.basedir}/../bd</directory><targetPath>schema</targetPath>
    <includes><include>Script_DDL.sql</include></includes></testResource>
</testResources>
```

```java
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;        // Testcontainers 2: classe não genérica

@SpringBootTest(properties = "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres!!")
@AutoConfigureMockMvc                                            // no Boot 4, @SpringBootTest sozinho NÃO traz MockMvc
@Testcontainers
abstract class IntegracaoBase {
    @Container @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16").withInitScript("schema/Script_DDL.sql");
}
```

`@AutoConfigureMockMvc` vem de `org.springframework.boot.webmvc.test.autoconfigure`.

**Testes mínimos:**
- CT10, CT11, CT14, CT17 (pendentes desde a Sprint 2);
- CT21, CT25, CT26;
- os cenários S1–S5 e a concorrência da §4.1;
- CT39 (`Clock` fixo).

> ⚠️ **Precisa do Docker Desktop aberto e funcionando.** Na auditoria de 26/10, o Docker Desktop desta máquina abriu, mas o motor não respondeu. É o mesmo sintoma do BD01 da Sprint 2. Se acontecer com você: abra a janela do Docker Desktop, veja se ele pede alguma ação, e confira no BIOS/UEFI se a virtualização está ligada. **Não pule estes testes:** eles provam o CT25 e a concorrência.

## 7. Entrega

1. Um PR por fase, com Conventional Commits, revisão de um colega e a tabela de validação preenchida.
2. **README:**
   - `JWT_SECRET`, `CORS_ALLOWED_ORIGIN`, `PYTHON_CMD` e `OTIMIZADOR_SCRIPT` passam para "obrigatórias";
   - o perfil `dev`;
   - como rodar `mvnw test`;
   - o aviso do `.env` sem BOM;
   - apagar o "Estado atual (corte da Sprint 2)".
3. **Contrato v2:** depois de aprovado (A10), confira que o código segue os formatos.
4. **Evidências:** em `docs/Sprint_3/evidencias/`.
5. **Antes do deploy** (se a A3 o incluir): remover `/api/teste/python`, junto com o cartão da tela e o ramo de eco.

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` | Variável não definida, `.env` com BOM, ou backend iniciado fora de `backend/` | §2.1 |
| `IllegalStateException: JWT_SECRET precisa ter pelo menos 32 caracteres.` | Segredo curto | 32+ caracteres |
| `package org.postgresql.util does not exist` | Driver com `scope runtime` | §2.2 (A4) |
| `NoUniqueBeanDefinitionException … CorsConfigurationSource` | Bean de CORS com o tipo genérico | §2.4 (A5) |
| `blocked by CORS policy` (funciona no Postman) | `localhost` × `127.0.0.1`, ou CORS fora do filtro | `CORS_ALLOWED_ORIGIN`; §2.4 |
| `401` com token certo | Header sem `Bearer `, ou token expirado (2 h) | Refazer o login |
| `500` onde devia ser `400` | Faltam os handlers da A7 | `GlobalExceptionHandler` de `referencia/` |
| Python "sem licença" quando chamado pelo Java | `GRB_*` não passadas ao processo filho | §5.2, cuidado 4 |
| Acentos quebrados no eco | Entrada não escrita em UTF-8 | §5.2, cuidado 1 |
| `violates check constraint "chk_movimentacao_restante"` | `ENTRADA` sem `quantidade_restante` | O repositório grava `restante = quantidade` |
| Dois pedidos vendem além do estoque | Faltou o `FOR UPDATE` | `travar(idsOrdenados)` antes de ler as linhas |
| `EmptyResultDataAccessException` no login | `queryForObject` em `buscarPorEmail` | `query(...).stream().findFirst()` |

## Checklist final

- [ ] Fase 1: `pom.xml` (A4), classes de `referencia/`, **18/18 testes**, `dependency:tree` conferido
- [ ] Decisão A3 tomada e registrada (PO)
- [ ] Fase 2: os 10 itens; `RegraAcai` com teste; o Leonardo fez o aceite
- [ ] Assinaturas combinadas com o Diogo (por escrito)
- [ ] Fase 3: os 9 itens; S1–S5, concorrência e CT25 como testes de integração
- [ ] `hoje` e `agora` vindos do `Clock` (São Paulo) em todo SQL de data
- [ ] Fase 4A: eco com `@Profile("dev")`; recomendação = 151/1/0; `GRB_*` no subprocesso; tempo medido
- [ ] README, Contrato v2 conferido, evidências

## Fora da Sprint 3 (só sinalização; depende da A3)

Cancelamento de pedido (US27, **não devolve estoque**), alertas (US15/US16), `PATCH` de unidades (US26), dashboard (US17), reposição (US14), marketing (US18), clientes (US13) e deploy (US20). A pendência F12 precisa ser resolvida antes do dashboard.
