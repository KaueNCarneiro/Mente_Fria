# Tutorial — Sprint 3 (Backend) do zero ao PR

**Para:** Lucas Gabriel Valadares Bassi · **Papel:** PO e responsável por backend
**Base:** `sprint-3-roteiro.md` v4.6 (Fases 1, 2, 3 e 4A) + `Analise_Roteiro_Sprint3_v4.6.md` + `Registro_de_Decisoes_e_Pendencias_Sprint3.md` · **Atualizado em:** 14/10/2026

> **Leia primeiro.** Este tutorial **substitui** o `Tutorial_Backend_Sprint1` (era para JDK 21, Spring Boot 3.3.4 e o pacote `com.mentefria`). O projeto é **JDK 25 / Spring Boot 4.0.8** e o pacote real é **`com.mentefria.backend`**. Mudam o JSON (Jackson 3), o Spring Security 7 e os imports de teste.
>
> **O que foi verificado e o que não foi.** **Não foi possível compilar** o projeto Spring (sem acesso ao Maven Central). **Todo o código Java é um ponto de partida conservador, não executado:** compile, rode `./mvnw test` e corrija o que o compilador apontar (aceite as sugestões do IntelliJ para imports do Boot 4). Foram **verificados em execução**: os SQLs (PostgreSQL 16, sobre o schema real), a **lógica de baixa por linhas** (6 cenários e concorrência) e os **números esperados da recomendação** (módulo Python executado). O que você precisa reproduzir em Java está nas tabelas "Você deve ver".

## 0. Onde você está (auditoria da `main`, commit `1ddb5ae`)

| Item | Estado hoje |
|---|---|
| **Fase 0 (`.env`)** | **Já feita** (seu PR #24): `application.properties` lê `./.env`; `backend/.env.example` existe; rodar **de dentro de `backend/`** |
| Backend | Só `GET /api/saude` e `GET /api/ingredientes` (3 campos); pacotes `ingrediente` e `saude`; teste `contextLoads` |
| `pom.xml` | `jdbc`, `webmvc`, `postgresql` e starters de teste. **Faltam** Security, JWT e Testcontainers |
| Banco | O Diogo entrega hoje o schema novo (`quantidade_restante`, `motivo`, status do pedido) e o seed oficial com estoque 0 |

**Ordem do seu trabalho** (um PR por fase; **não misture**):

| Fase | O que | Quando |
|---|---|---|
| 1 | Segurança (JWT), CORS, erros padronizados | Hoje |
| 2 | Auth, usuários, unidades, ingredientes e produtos | 15/10 |
| 3 | Estoque por linhas (US9), pedido (US12), `GET /api/estoque` | 17 a 18/10 (extensão) |
| 4A | Ponte com o Python e recomendação, em dupla com o Kauê | 17 a 18/10 (se couber) |

**Combine hoje com o Diogo** a **tabela de assinaturas dos repositórios** (tutorial dele, Parte 7). Ele cria os `*Repository`; você, controllers e serviços (F7: evita os conflitos da Sprint 1).

## 1. Por que algumas coisas mudam em relação ao que você já conhece

| O que | Por quê |
|---|---|
| **`hoje` vem do Java** (`America/Sao_Paulo`) e é passado como parâmetro (F3) | O Render roda em UTC; o Brasil está 3 h atrás. Das 21h à meia-noite, uma linha que vence "hoje" seria tratada como vencida 3 horas antes |
| **`/api/teste/python` só existe com `@Profile("dev")`** (F4) | Com a segurança ligada ele exigiria token; liberá-lo "no geral" abre uma porta sem controle de acesso em produção. No perfil `dev` ele existe; em produção, **não existe** |
| **JWT com `jjwt` 0.13.x, conferindo a árvore** (F5) | A escolha do JWT é anterior ao Spring Boot 4 (que usa Jackson 3). O `jjwt-jackson` traz o Jackson 2 junto; funciona, mas precisa ser **verificado** com `dependency:tree` |
| **CORS dentro do filtro de segurança** (e com `PATCH`) | A segurança roda **antes** do Spring MVC. Sem CORS no próprio filtro, o *preflight* (`OPTIONS`) é barrado com `401`. `PATCH` será usado no cadastro de porção (Sprint 4) |
| **Estoque por linhas** | Cada `ENTRADA` é uma linha com `quantidade_restante`. Só assim se sabe **o que está vencido** e **de qual lote sai** a baixa |
| **Baixa só de linhas válidas, menor validade primeiro** | Evita vender ingrediente vencido e reduz perda: o que vence antes sai antes |
| **Validação manual explícita** (e não Bean Validation) | A API devolve **vários erros de campo de uma vez**, com mensagens fixas do `mapa_constraints.md`; manual dá resultado determinístico |

## 2. Fase 1 — Fundação: segurança, JWT, CORS, erros

### 2.1 Preparar
```powershell
cd C:\caminho\Mente_Fria
docker start mente-fria-db
git checkout main
git pull
git checkout -b feature/backend-fundacao-seguranca
cd backend
copy .env.example .env
```
Edite o `.env` (nunca comite): `DB_USER`, `DB_PASSWORD` e o novo `JWT_SECRET` (**32+ caracteres**, aleatório). Teste:
```powershell
.\mvnw spring-boot:run
```
**Você deve ver** `Started BackendApplication` e `Invoke-RestMethod http://localhost:8080/api/saude` → `status ok, banco ok`. Acrescente ao `backend/.env.example` as variáveis novas (`JWT_SECRET=`, `CORS_ALLOWED_ORIGIN=http://127.0.0.1:5500,http://localhost:5500`, `PYTHON_CMD=python`, `OTIMIZADOR_SCRIPT=../optimization/app/otimizador.py`), **sem valores**.

### 2.2 Dependências (`pom.xml`)
```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.13.0</version></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.13.0</version><scope>runtime</scope></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.13.0</version><scope>runtime</scope></dependency>
<dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
```
Recarregue o Maven e **valide**: `.\mvnw dependency:tree | Select-String "jackson|jjwt"`. Se houver conflito do Jackson, troque `jjwt-jackson` por `jjwt-gson` (e acrescente `com.google.code.gson:gson`). As anotações `@JsonProperty` continuam em `com.fasterxml.jackson.annotation`. Confirme no Maven Central a versão mais recente do `jjwt`.

### 2.3 `application.properties` (acrescente)
```properties
app.jwt.secret=${JWT_SECRET}
app.jwt.validade-horas=2
app.cors.allowed-origins=${CORS_ALLOWED_ORIGIN:http://127.0.0.1:5500,http://localhost:5500}
app.python.cmd=${PYTHON_CMD:python}
app.otimizador.script=${OTIMIZADOR_SCRIPT:../optimization/app/otimizador.py}
app.otimizador.timeout-segundos=10
```
`JWT_SECRET` é **obrigatória**: sem ela o Spring não sobe (falhar cedo é bom). **Nunca** comite o valor.

### 2.4 Classes (pacote `com.mentefria.backend`)

**`config/ApiException.java`**
```java
package com.mentefria.backend.config;

import java.util.Map;

public class ApiException extends RuntimeException {
    private final int status;
    private final Map<String, String> campos;
    public ApiException(int status, String mensagem, Map<String, String> campos) {
        super(mensagem); this.status = status; this.campos = campos;
    }
    public static ApiException campo(int status, String campo, String texto) {
        return new ApiException(status, "Dados inválidos", Map.of(campo, texto));
    }
    public int status() { return status; }
    public Map<String, String> campos() { return campos; }
}
```

**`config/RelogioConfig.java`** (F3: uma única fonte de "hoje")
```java
@Configuration
public class RelogioConfig {
    @Bean Clock clock() { return Clock.system(ZoneId.of("America/Sao_Paulo")); }
}
```
Use `LocalDate.now(clock)` em **todo** serviço que compare validade; passe o resultado como `:hoje` aos SQLs. Nos testes, injete um `Clock.fixed(...)`.

**`auth/JwtService.java`**
```java
package com.mentefria.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private final SecretKey chave;
    private final long validadeMs;

    public JwtService(@Value("${app.jwt.secret}") String segredo, @Value("${app.jwt.validade-horas:2}") long horas) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres.");
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.validadeMs = horas * 60 * 60 * 1000;
    }
    public String gerarToken(int usuarioId, String perfil) {
        Date agora = new Date();
        return Jwts.builder().subject(String.valueOf(usuarioId)).claim("perfil", perfil)
                .issuedAt(agora).expiration(new Date(agora.getTime() + validadeMs)).signWith(chave).compact();
    }
    public Claims validarEExtrair(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
    }
}
```

**`auth/JwtAuthFilter.java`** (**sem** `@Component`: criado no `SecurityConfig`, para não ser registrado duas vezes)
```java
package com.mentefria.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    public JwtAuthFilter(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            try {
                var claims = jwt.validarEExtrair(h.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("perfil", String.class))));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();     // token inválido/expirado: segue sem autenticar -> 401
            }
        }
        chain.doFilter(req, res);
    }
}
```

**`config/SecurityConfig.java`**: a tabela de permissões do Contrato (§2.2) vira código.

| Quem | Rotas |
|---|---|
| Público | `GET /api/saude`, `POST /api/auth/login`, `POST /api/auth/registro`; **`/api/teste/**` só no perfil `dev`** |
| Administrador | `/api/usuarios/**`, `/api/unidades-medida/**`, `POST`/`PUT`/`PATCH` em `/api/ingredientes/**` e `/api/produtos/**`, `/api/producao/**`, `/api/reposicao/**`, `/api/marketing/**`, `/api/dashboard/**` |
| Administrador e Funcionário | o resto (`GET /api/ingredientes`, `GET /api/produtos`, `/api/estoque/**`, `/api/clientes/**`, `/api/pedidos/**`) |

```java
package com.mentefria.backend.config;

import com.mentefria.backend.auth.JwtAuthFilter;
import com.mentefria.backend.auth.JwtService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private static final String ADMIN = "ADMINISTRADOR";
    private static final String JSON = "application/json;charset=UTF-8";

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> origens) {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(origens);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setMaxAge(3600L);
        var fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", cfg);
        return fonte;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt, CorsConfigurationSource cors, Environment env) throws Exception {
        boolean dev = env.acceptsProfiles(Profiles.of("dev"));
        http.cors(c -> c.configurationSource(cors))
            .csrf(csrf -> csrf.disable())                         // sem cookie/sessão: não há CSRF
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> { res.setStatus(401); res.setContentType(JSON);
                    res.getWriter().write("{\"mensagem\":\"Sua sessão expirou ou você não está autenticado.\"}"); })
                .accessDeniedHandler((req, res, ex) -> { res.setStatus(403); res.setContentType(JSON);
                    res.getWriter().write("{\"mensagem\":\"Você não tem permissão para esta ação.\"}"); }))
            .authorizeHttpRequests(a -> {
                a.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll();
                a.requestMatchers(HttpMethod.GET, "/api/saude").permitAll();
                a.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll();
                if (dev) a.requestMatchers("/api/teste/**").permitAll();       // F4: só no perfil dev
                a.requestMatchers("/api/usuarios/**", "/api/unidades-medida/**", "/api/producao/**",
                                  "/api/reposicao/**", "/api/marketing/**", "/api/dashboard/**").hasRole(ADMIN);
                a.requestMatchers(HttpMethod.POST, "/api/ingredientes/**", "/api/produtos/**").hasRole(ADMIN);
                a.requestMatchers(HttpMethod.PUT, "/api/ingredientes/**", "/api/produtos/**").hasRole(ADMIN);
                a.requestMatchers(HttpMethod.PATCH, "/api/ingredientes/**", "/api/produtos/**").hasRole(ADMIN);
                a.anyRequest().authenticated();
            })
            .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```
**Por que o CORS está aqui** (e não em `WebMvcConfigurer`): a segurança roda antes do MVC; sem um `CorsConfigurationSource` no filtro, o *preflight* leva `401`. **Se existir um `CorsConfig` antigo, apague-o.** Se o compilador recusar algo do Security 7, use a sugestão do IntelliJ (o DSL por lambda é o único aceito).

**`config/GlobalExceptionHandler.java`** — traduz as constraints do `mapa_constraints.md`. Chave: nome da constraint; no `NOT NULL`, `NN:tabela.coluna` (a coluna sozinha é ambígua: `nome` existe em várias tabelas).
```java
package com.mentefria.backend.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private record Msg(int status, String campo, String texto) {}
    private static Map.Entry<String, Msg> m(String k, int st, String campo, String texto) { return Map.entry(k, new Msg(st, campo, texto)); }

    private static final Map<String, Msg> MAPA = Map.ofEntries(
        m("uq_usuario_email", 409, "email", "Este e-mail já está cadastrado."),
        m("chk_usuario_perfil", 400, "perfil", "Perfil de acesso inválido."),
        m("NN:usuario.nome", 400, "nome", "Informe o nome."),
        m("NN:usuario.email", 400, "email", "Informe o e-mail."),
        m("NN:usuario.senha_hash", 400, "senha", "Informe a senha."),
        m("NN:ingrediente.nome", 400, "nome", "Informe o nome do ingrediente."),
        m("NN:ingrediente.unidade_medida", 400, "unidade_medida", "Selecione a unidade de medida."),
        m("NN:ingrediente.custo_unitario", 400, "custo_unitario", "Informe o custo."),
        m("NN:ingrediente.porcao_padrao", 400, "porcao_padrao", "Informe a porção."),
        m("chk_ingrediente_custo_unitario", 400, "custo_unitario", "O custo não pode ser negativo."),
        m("chk_ingrediente_porcao_padrao", 400, "porcao_padrao", "A porção deve ser maior que zero."),
        m("chk_ingrediente_quantidade_minima", 400, "quantidade_minima", "A quantidade mínima não pode ser negativa."),
        m("chk_ingrediente_quantidade_estoque", 422, "quantidade", "Estoque insuficiente."),
        m("fk_ingrediente_unidade_medida", 400, "unidade_medida", "Selecione uma unidade de medida cadastrada."),
        m("NN:produto.nome", 400, "nome", "Informe o nome do produto."),
        m("NN:produto.preco_venda", 400, "preco_venda", "Informe o preço de venda."),
        m("chk_produto_preco_venda", 400, "preco_venda", "O preço deve ser maior que zero."),
        m("chk_produto_ingrediente_quantidade", 400, "composicao", "A quantidade utilizada deve ser maior que zero."),
        m("produto_ingrediente_pkey", 400, "composicao", "Este ingrediente já foi adicionado ao produto."),
        m("fk_produto_ingrediente_ingrediente", 400, "composicao", "Ingrediente não encontrado."),
        m("fk_produto_ingrediente_produto", 400, "composicao", "Produto não encontrado."),
        m("NN:cliente.nome", 400, "nome", "Informe o nome."),
        m("NN:cliente.contato", 400, "contato", "Informe o contato."),
        m("NN:cliente.endereco", 400, "endereco", "Informe o endereço."),
        m("fk_pedido_cliente", 400, "cliente_id", "Cliente não encontrado."),
        m("chk_pedido_produto_quantidade", 400, "itens", "A quantidade deve ser maior que zero."),
        m("pedido_produto_pkey", 400, "itens", "Este produto já foi adicionado ao pedido."),
        m("fk_pedido_produto_produto", 400, "itens", "Produto não encontrado."),
        m("chk_movimentacao_tipo", 400, "tipo", "Tipo de movimentação inválido."),
        m("chk_movimentacao_quantidade", 400, "quantidade", "A quantidade deve ser maior que zero."),
        m("chk_movimentacao_data_validade", 400, "data_validade", "A validade só pode ser informada em entradas."),
        m("chk_movimentacao_motivo", 400, "motivo", "Informe o motivo da saída."),
        m("fk_movimentacao_ingrediente", 400, "ingrediente_id", "Ingrediente não encontrado."),
        m("chk_unidade_porcao_padrao", 400, "porcao_padrao", "A porção deve ser maior que zero."),
        m("unidade_medida_porcao_padrao_pkey", 409, "unidade_medida", "Esta unidade já está cadastrada.")
    );

    private static ResponseEntity<Map<String, Object>> resposta(int status, String mensagem, Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", mensagem);
        if (campos != null && !campos.isEmpty()) corpo.put("campos", campos);
        return ResponseEntity.status(status).body(corpo);
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> api(ApiException e) { return resposta(e.status(), e.getMessage(), e.campos()); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException e) {
        if (e.getMostSpecificCause() instanceof PSQLException p && p.getServerErrorMessage() != null) {
            var s = p.getServerErrorMessage();
            String chave = s.getConstraint() != null ? s.getConstraint() : "NN:" + s.getTable() + "." + s.getColumn();
            Msg msg = MAPA.get(chave);
            if (msg != null) return resposta(msg.status(), msg.status() == 422 ? msg.texto() : "Dados inválidos", Map.of(msg.campo(), msg.texto()));
            log.warn("Constraint sem mapeamento: {}", chave);
        }
        log.error("Violação de integridade", e);
        return resposta(500, "Não foi possível concluir a operação.", null);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) { return resposta(400, "Requisição inválida.", null); }
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, Object>> naoEncontrado(NoResourceFoundException e) { return resposta(404, "Recurso não encontrado.", null); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, Object>> metodo(HttpRequestMethodNotSupportedException e) { return resposta(405, "Método não permitido.", null); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Erro inesperado", e);                          // o stack trace vai para o LOG, nunca para a resposta
        return resposta(500, "Não foi possível concluir a operação.", null);
    }
}
```
(O handler genérico de `Exception` capturaria o 404 do Spring e o transformaria em 500: por isso `NoResourceFoundException` tem handler próprio. Foi o erro `BE03` da Sprint 2.)

### 2.5 Validar a Fase 1
Auxiliar de PowerShell que mostra o status mesmo nos erros:
```powershell
function Chamar($metodo, $url, $corpo = $null, $token = $null) {
  $h = @{}; if ($token) { $h["Authorization"] = "Bearer $token" }
  $args = @{ Method = $metodo; Uri = $url; Headers = $h }
  if ($corpo) { $args.ContentType = "application/json; charset=utf-8"; $args.Body = [Text.Encoding]::UTF8.GetBytes(($corpo | ConvertTo-Json -Depth 6)) }
  try { Invoke-RestMethod @args } catch { "HTTP " + [int]$_.Exception.Response.StatusCode; $_.ErrorDetails.Message }
}
```
| Chamada | Esperado |
|---|---|
| `Chamar GET http://localhost:8080/api/saude` | `status ok, banco ok` (público) |
| `Chamar GET http://localhost:8080/api/ingredientes` (sem token) | `HTTP 401` e a mensagem "Sua sessão expirou..." |
| `Chamar GET http://localhost:8080/api/nao-existe` (sem token) | `HTTP 401` (a segurança responde antes do 404) |
| Preflight: `Invoke-WebRequest -Method Options -Uri http://localhost:8080/api/ingredientes -Headers @{Origin="http://127.0.0.1:5500";"Access-Control-Request-Method"="GET"}` | `200` e `Access-Control-Allow-Origin: http://127.0.0.1:5500` |
| `dependency:tree` | sem erro de conflito do Jackson |

Se o preflight devolver `401`, o CORS não está no filtro de segurança. **Depois da Fase 1, as rotas passam a exigir token e o frontend ainda não consegue entrar** (o login é da Fase 2): é o esperado. Por isso, **faça o merge da Fase 1 junto com a Fase 2** (ou logo depois), e não deixe a `main` nesse estado por muito tempo. Commit e PR: `feat: seguranca JWT, CORS e tratamento padrao de erros`.

## 3. Fase 2 — Autenticação e CRUDs

Branch nova: `git checkout -b feature/backend-auth-crud`. SQL dos repositórios: `docs/Apoio/sql_repositorios.md` (use `NamedParameterJdbcTemplate`). **Peça ao Diogo** os repositórios, conforme a tabela de assinaturas.

### 3.1 Regras de validação por endpoint (na aplicação, antes do banco; devolva **todos** os campos inválidos de uma vez)

| Endpoint | Regras |
|---|---|
| `POST /api/auth/registro` | existe Administrador → `409` (**checar primeiro**); nome obrigatório; e-mail obrigatório e válido, **normalizado** (`trim` + minúsculas); senha obrigatória, ≥ 8 (CT02). Perfil **sempre** `ADMINISTRADOR`, nunca vem do corpo |
| `POST /api/auth/login` | qualquer falha (e-mail inexistente, senha errada, usuário inativo) → o **mesmo** `401 "E-mail ou senha inválidos."` |
| `POST /api/usuarios` | igual ao registro, perfil `FUNCIONARIO`; e-mail duplicado → `409` (constraint) |
| `POST`/`PUT /api/ingredientes` | nome obrigatório; unidade obrigatória; `custo ≥ 0`; `porção > 0`; `mínima ≥ 0`; `quantidade_estoque` **nunca** vem do corpo |
| `POST /api/produtos` | nome; preço `> 0`; composição não vazia (CT13); sem ingrediente repetido; ingredientes existem; **tem açaí** (CT15); quantidades `> 0` |
| `PUT /api/produtos/{id}/preco` | preço `> 0`; `0` linhas afetadas → `404` |

### 3.2 `AuthController`
```java
package com.mentefria.backend.auth;

import com.mentefria.backend.config.ApiException;
import com.mentefria.backend.usuario.Usuario;
import com.mentefria.backend.usuario.UsuarioRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegistroRequest(String nome, String email, String senha) {}
    public record LoginRequest(String email, String senha) {}
    private final UsuarioRepository usuarios; private final JwtService jwt; private final PasswordEncoder encoder;
    public AuthController(UsuarioRepository u, JwtService j, PasswordEncoder e) { this.usuarios = u; this.jwt = j; this.encoder = e; }

    static String normalizar(String email) { return email == null ? "" : email.trim().toLowerCase(); }

    @PostMapping("/registro")
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
**A resposta do login precisa ter `nome`** (o frontend a mostra no menu). `senha_hash` **nunca** sai em resposta alguma. `Usuario` = `record (int id, String nome, String email, String senhaHash, String perfil, boolean ativo)`.

### 3.3 Usuários, unidades, ingredientes
Mesmo padrão: controller fino → validação → repositório. Pontos que costumam falhar:
- **`GET /api/ingredientes` passa a devolver o registro completo** (`id, nome, unidade_medida, custo_unitario, porcao_padrao, quantidade_estoque, quantidade_minima`, em `snake_case`): **apague** o `IngredienteResumo`. O frontend **recusa editar** se custo, porção ou mínima vierem vazios.
- `BigDecimal` (nunca `double`) para custo, porção e quantidades; `@JsonProperty("custo_unitario") BigDecimal custoUnitario` faz a tradução.
- `POST` → `201` com o objeto completo; `PUT` → `200`; `0` linhas afetadas → `404`.
- `GET /api/ingredientes` ordena por `nome`; **o estoque agora é 0** no seed oficial (esperado: a tela mostra 0 até alguém registrar entradas).

### 3.4 Produtos: composição, açaí e transação
**Corrija o bug que o Diogo achou na Sprint 2:** com um ingrediente repetido na lista, comparar `encontrados.size() < ids.size()` devolvia a mensagem errada ("não encontrado"). Detecte a **repetição primeiro**:
```java
@Transactional
public int criar(String nome, BigDecimal preco, List<ItemComposicao> composicao) {
    Map<String, String> erros = new LinkedHashMap<>();
    if (nome == null || nome.isBlank()) erros.put("nome", "Informe o nome do produto.");
    if (preco == null || preco.signum() <= 0) erros.put("preco_venda", "O preço deve ser maior que zero.");
    if (composicao == null || composicao.isEmpty()) {
        erros.put("composicao", "Adicione ao menos um ingrediente.");                                  // CT13
    } else {
        List<Integer> ids = composicao.stream().map(ItemComposicao::ingredienteId).toList();
        if (ids.stream().distinct().count() < ids.size()) erros.put("composicao", "Este ingrediente já foi adicionado ao produto.");
        else if (composicao.stream().anyMatch(i -> i.quantidadeUtilizada() == null || i.quantidadeUtilizada().signum() <= 0))
            erros.put("composicao", "A quantidade utilizada deve ser maior que zero.");              // CT14
        else {
            Map<Integer, String> nomes = ingredientes.buscarNomesPorIds(ids);
            if (nomes.size() < ids.size()) erros.put("composicao", "Ingrediente não encontrado.");
            else if (!RegraAcai.composicaoTemAcai(List.copyOf(nomes.values())))
                erros.put("composicao", "O produto precisa ter o açaí entre os ingredientes.");       // CT15
        }
    }
    if (!erros.isEmpty()) throw new ApiException(400, "Dados inválidos", erros);
    int produtoId = produtos.inserir(nome.trim(), preco);
    produtos.inserirComposicao(produtoId, composicao);   // mesma transação: falhou, desfaz o produto também
    return produtoId;
}
```
`RegraAcai` está pronta em `docs/Apoio/sql_repositorios.md` (seção 4) com os 15 casos de teste. **Crie o teste unitário dela agora.** `GET /api/produtos` devolve `composicao: [{ingrediente_id, ingrediente_nome, quantidade_utilizada, unidade_medida}]` (a consulta devolve uma linha por par produto × ingrediente; agrupe com `ResultSetExtractor` em um `LinkedHashMap`; trate produto sem composição sem exceção).

### 3.5 Validar a Fase 2

| # | Chamada | Esperado |
|---|---|---|
| 1 | `POST /api/auth/registro` `{nome:"Dono",email:" DONO@Acai.com ",senha:"senha1234"}` | `201`; e-mail gravado como `dono@acai.com` |
| 2 | O mesmo de novo | `409` "O cadastro do Administrador já foi realizado." |
| 3 | Login correto | `200` com `token`, `perfil`, `nome` |
| 4 | Login com senha errada **e** com e-mail inexistente | `401`, **a mesma** mensagem |
| 5 | `GET /api/ingredientes` com o token | `200`, 7 itens, **todos os campos**, estoque 0 |
| 6 | `POST /api/usuarios` (Funcionário) e login como ele; depois `POST /api/ingredientes` com o token dele | `201`; depois **`403`** |
| 7 | `POST /api/ingredientes` com `custo_unitario:-1` e `porcao_padrao:0` | `400` com **os dois** campos em `campos` |
| 8 | `POST /api/produtos` sem açaí / vazio / ingrediente repetido | `400`: CT15 / CT13 / "já foi adicionado" |
| 9 | `POST /api/produtos` válido e `GET /api/produtos` | `201`; a lista traz **a composição** |
| 10 | `PUT /api/produtos/1/preco` `{preco_venda:0}` e depois `13.50` | `400` e `200`; Funcionário → `403` |

**Gate:** com os 10 itens passando, **avise o Leonardo** (ele faz o aceite real do frontend, no tutorial dele). Só depois a Fase 2 conta como concluída. **Testes:** unitário (`RegraAcai`, normalização de e-mail), segurança (MockMvc: CT05, CT08, sem token → `401`, CT01B, login com a mesma mensagem) e integração com banco real (Parte 6). No Boot 4 os pacotes de `@AutoConfigureMockMvc` e `@WebMvcTest` mudaram: aceite a sugestão do IntelliJ. Para o usuário nos testes, gere um token real com o `JwtService`.

## 4. Fase 3 — Estoque por linhas, pedido e `GET /api/estoque` (extensão até 18/10)

Branch: `feature/backend-estoque-pedidos`. **Pré-requisito:** o schema novo do Diogo no seu banco (**recrie o banco**: `Recomeçar do zero` do README).

### 4.1 O que o código tem que reproduzir (já provado em SQL)

| Cenário | Resultado esperado |
|---|---|
| **S1:** linha vencida de 20 e linha válida de 20; pedido de 5 | total **35**, válido **15**, vencido **20**; após descartar 20: total **15** |
| **S2:** A=20 (vence 20/10) sai 10; entra C=20 (vence 15/10); sai 5 | C=15 e A=10 (a saída sai da **menor validade**, a C) |
| **S3:** linhas 3 (14/10), 4 (20/10), 10 (sem validade); saída de 6, depois 5 | `0/1/10` e depois `0/0/6` (**sem validade por último**) |
| **S4:** só há linha vencida; pedido de 1 | **`422`**, nada muda |
| **S5:** descarte maior que o vencido | **`422`**; descarte exato funciona |
| **Concorrência:** dois pedidos de 30 sobre 50 | **um** passa, o outro recebe `422`; saldo final 20 |
| **Regra de estoque** (saldo = soma dos restantes) | vale **após cada operação** |

Escreva estes cenários como testes de integração (Testcontainers). Se o seu Java reproduzir a tabela, as regras estão certas.

### 4.2 Baixa por linhas (usada pelo pedido, pela saída convencional e pelo descarte)
```java
@Component
public class BaixaPorLinhas {
    private final MovimentacaoRepository movs; private final IngredienteRepository ings;
    public BaixaPorLinhas(MovimentacaoRepository m, IngredienteRepository i) { this.movs = m; this.ings = i; }

    /** Consome 'qtd' das linhas já carregadas (travadas com FOR UPDATE), na ordem recebida. */
    public void consumir(int ingredienteId, List<MovimentacaoRepository.Linha> linhas, BigDecimal qtd,
                         String motivo, int usuarioId, String obs, Supplier<ApiException> semEstoque) {
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
        movs.inserirSaida(ingredienteId, usuarioId, qtd, motivo, obs);
    }
}
```
### 4.3 Movimentação (`POST /api/estoque/movimentacoes`)
Tudo em **uma** transação, com `hoje = LocalDate.now(clock)`:
- **`ENTRADA`:** valida; `inserirEntrada` (a linha nasce com `restante = quantidade`) e `somarSaldo`. Validade opcional. **Dúvida para você, como PO:** aceitar uma entrada cuja validade já passou? O roteiro não define; implemente **aceitando** (a linha nasce vencida e aparece no alerta) e registre a decisão.
- **`SAIDA`:** `motivo` obrigatório: `CONVENCIONAL` usa `linhasValidas`; `DESCARTE_VENCIDO` usa `linhasVencidas`. Quantidade maior que a disponível → **`422`** "Estoque insuficiente para o ingrediente X" (CT21), **nada alterado**. `data_validade` em saída → `400`.
- `quantidade ≤ 0` → `400` (CT26). Os dois perfis acessam (CT27). O `usuario_id` vem **do token** (`Authentication.getName()`), nunca do corpo.
- Antes de tudo: `ingredientes.travar(List.of(id))` (`FOR UPDATE`).

### 4.4 Pedido (`POST /api/pedidos`) — o mais delicado
Tudo em **um** `@Transactional`:
```java
@Transactional
public PedidoCriado criar(Integer clienteId, List<ItemPedido> itens, int usuarioId) {
    LocalDate hoje = LocalDate.now(clock);
    // 1. validar: itens não vazio, sem produto repetido, quantidade inteira > 0 (400, campo "itens")
    // 2. produtos = produtoRepo.buscarPorIds(ids)  -> todos existem e estão ATIVOS (senão 400)
    // 3. composicao = produtoRepo.composicoes(ids) -> necessário[ing] += qtd_utilizada * quantidade (BigDecimal)
    // 4. ingredientes.travar(idsOrdenados)         -> FOR UPDATE em ORDEM DE ID (evita deadlock)
    // 5. para cada ingrediente (ordem de id): linhas[ing] = movs.linhasValidas(ing, hoje)  (já travadas)
    // 6. para cada item NA ORDEM: se algum ingrediente da composição não tem 'necessário' <= soma(linhas): 
    //        throw new ApiException(422, "Estoque insuficiente para o produto " + nome, null);     // CT25: nada gravado
    // 7. total = soma(preço_atual * quantidade); (id, data) = pedidoRepo.inserir(clienteId, usuarioId, total)
    // 8. pedidoRepo.inserirItens(...)  com preco_unitario_registrado = preço ATUAL do produto (vem do banco, não do cliente)
    // 9. para cada ingrediente: baixa.consumir(ing, linhas[ing], necessario[ing], "PEDIDO", usuarioId,
    //        "Baixa automática do pedido " + id, () -> new ApiException(422, "Estoque insuficiente...", null))
}
```
**Por quê cada ponto:** `FOR UPDATE` + ordem de `id`: dois pedidos simultâneos esperam um ao outro **sem deadlock** (provado: 30 rodadas). A baixa usa **só linhas válidas** (CT29: vencido não conta). O preço vem **do banco**. `PedidoCriado` = `{id, valor_total, data_pedido}`. **Teste obrigatório (CT25):** pedido com 2 produtos, o segundo sem estoque válido → `422`, **nenhuma** linha em `pedido`, `pedido_produto` ou `movimentacao_estoque` e saldos **intactos**.

### 4.5 `GET /api/estoque` [formato a fechar no Contrato REST v2: proposta]
`ingredientes.listarEstoque(hoje)` devolve por ingrediente `ingrediente_id, nome, unidade_medida, quantidade_estoque (total), quantidade_valida, quantidade_vencida, quantidade_minima, validade_mais_proxima`. **Você deve ver** (banco de demonstração do Diogo): Granola `11,160 / 5,160 / 6,000`; Morango `1,980` (abaixo do mínimo 2,000).

### 4.6 Validar a Fase 3
| # | Chamada | Esperado |
|---|---|---|
| 1 | Entrada de 10 kg de açaí, validade futura | `201`; `GET /api/estoque` mostra +10 válido |
| 2 | Saída convencional maior que o válido | `422`; saldo **igual** |
| 3 | Saída com `data_validade` | `400` |
| 4 | Descarte com vencido (use o seed de demonstração) | só consome a linha vencida; `quantidade_vencida` zera |
| 5 | `POST /api/pedidos` 2×Tradicional (com estoque suficiente) | `201`; açaí −0,600, leite cond. −0,060, copo −2; **uma** `SAIDA` por ingrediente, motivo `PEDIDO` |
| 6 | Pedido com produto repetido | `400` |
| 7 | Pedido com um item sem estoque válido | `422` e **nada** gravado |
| 8 | Funcionário cria pedido e movimentação | `201` (CT27, CT28) |

## 5. Fase 4A — Ponte com o Python e recomendação (em dupla com o Kauê)

**Pré-requisitos:** Contrato v2 aprovado; `optimization/exemplos/` do Kauê; o seed de demonstração.

### 5.1 Hello World com `echo` (primeiro, antes da recomendação)
```java
@Profile("dev")                                  // F4: não existe em produção
@RestController @RequestMapping("/api/teste")
public class TestePythonController {
    private final OtimizadorProducao otimizador;
    public TestePythonController(OtimizadorProducao o) { this.otimizador = o; }
    @GetMapping(value = "/python", produces = "application/json;charset=UTF-8")
    public String eco() { return otimizador.executar("{\"echo\":\"Açaí (polpa)\"}"); }
}
```
Rode com o perfil: `$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw spring-boot:run`. **Você deve ver** `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}` **com acentos corretos** (é o teste de codificação no Windows). Sem o perfil, `GET /api/teste/python` → `401`/`404`.

### 5.2 `OtimizadorPython`
Interface `OtimizadorProducao { String executar(String jsonEntrada); }` e a implementação com `ProcessBuilder` (`stdin` fechado, `waitFor` com timeout configurável `app.otimizador.timeout-segundos`, `exitValue`). Três cuidados:
1. **Variáveis do Gurobi no subprocesso:** o `.env` do Spring **não** chega ao Python; passe explicitamente:
   ```java
   for (String nome : List.of("GRB_WLSACCESSID", "GRB_WLSSECRET", "GRB_LICENSEID")) {
       String v = environment.getProperty(nome);                     // org.springframework.core.env.Environment
       if (v != null && !v.isBlank()) pb.environment().put(nome, v);
   }
   ```
2. **Ler a saída depois do `waitFor`** é seguro para saídas pequenas (um JSON de poucas linhas).
3. Dublê `OtimizadorFalso` (devolve o conteúdo de `saida_demo.json`) para testes; use `@MockitoBean` (o `@MockBean` foi removido no Boot 4).

### 5.3 `POST /api/producao/recomendacao` (só Administrador)
1. **Entrada:** consulta 3 do Diogo (ingredientes dos produtos **ativos**, **inclusive os com 0**, **saldo válido** e **custo**) + composições.
2. **JSON** com `Map`/`List` e o **`JsonMapper` do Spring** (Jackson 3: `tools.jackson.databind.json.JsonMapper`, **não** o `com.fasterxml...ObjectMapper` do tutorial antigo).
3. **Saída:** desserialize para um `record` com `@JsonProperty` (`status`, `mensagem`, `receita_estimada`, `margem_estimada`, `recomendacao`, `consumo`). Em Jackson 3 as exceções são **não verificadas**: capture `RuntimeException`.
4. `status: "erro"` → `422` com mensagem amigável; saída ≠ 0, timeout ou JSON ilegível → `500` sem *stack trace*.
5. **Recalcule em `BigDecimal`** (`receita = Σ preço × qtd`; `lucro = Σ (preço − custo da receita) × qtd`); se diferir do Python em mais de R$ 0,01, registre um aviso no log e **use o seu valor**.
6. **Resposta** [formato a fechar no Contrato REST v2: proposta]: `calculado_em`, `receita_estimada`, `margem_estimada`, `recomendacao: [{produto_id, nome, quantidade, lucro_por_copo}]`. Quando tudo for 0, deixe o frontend explicar o porquê (ele sabe pelo `lucro_por_copo`).

### 5.4 Conferir com números verificados

| Estoque (válido) | Plano (prod. 1 / 2 / 3) | Receita | Lucro | `consumo` |
|---|---|---|---|---|
| **Seed oficial** (estoque 0) | 0 / 0 / 0 | 0,00 | 0,00 | vazio |
| **Seed de demonstração** (açaí 45,75; leite cond. 14,22; granola **5,16**; banana 11,05; morango 1,98; leite em pó 4,76; copo 269) | **151 / 1 / 0** | **1.966,80** | **1.011,71** | açaí 45,75; etc. |

Lucro por copo: produto 1 = 6,64; produto 2 = 9,07; produto 3 = 10,63. **Teste do vencido (CT29):** neste estoque o desconto **não muda** o plano; por isso teste **a entrada**: capture o JSON enviado ao dublê (`ArgumentCaptor`) e confira que a **granola vai com `5.160`** (e não `11.160`). **Validação manual:** administrador → `200` com a tabela; funcionário → `403`; `PYTHON_CMD` errado → `500` sem *stack trace*; estoque 0 → `200` com tudo 0. **Meça o tempo** e entregue ao Kauê.

## 6. Testes de integração (Testcontainers)

```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-testcontainers</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.testcontainers</groupId><artifactId>testcontainers-postgresql</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.testcontainers</groupId><artifactId>testcontainers-junit-jupiter</artifactId><scope>test</scope></dependency>
```
(Os módulos do Testcontainers 2 mudaram de nome: **confirme as versões** no Maven Central.) Para carregar o **mesmo** `bd/Script_DDL.sql` (uma só fonte da verdade, sem copiar o arquivo):
```xml
<build><testResources>
  <testResource><directory>src/test/resources</directory></testResource>
  <testResource><directory>${project.basedir}/../bd</directory><targetPath>schema</targetPath>
    <includes><include>Script_DDL.sql</include></includes></testResource>
</testResources></build>
```
```java
@SpringBootTest(properties = "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres!!")
@Testcontainers
abstract class IntegracaoBase {
    @Container @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16").withInitScript("schema/Script_DDL.sql");
}
```
(`PostgreSQLContainer` vem de `org.testcontainers.postgresql`; `@ServiceConnection` de `org.springframework.boot.testcontainers.service.connection`: confirme os imports.) O Docker Desktop precisa estar aberto. **Testes mínimos:** CT10, CT11, CT14, CT17, CT21, CT25, CT26 (os pendentes da evidência da Sprint 2), os cenários da Parte 4.1 e os de segurança.

## 7. Entrega

1. **Um PR por fase**, com Conventional Commits, revisão de um colega e a tabela de validação preenchida.
2. **README:** `JWT_SECRET`, `CORS_ALLOWED_ORIGIN`, `PYTHON_CMD` e `OTIMIZADOR_SCRIPT` deixam de ser "planejadas"; estado dos endpoints; como rodar os testes de integração; o perfil `dev`.
3. **Contrato REST v2:** transforme as propostas (estoque, pedidos, recomendação) em seção do Contrato, com o Leonardo e o Kauê.
4. **Evidências:** prints das validações e a saída de `./mvnw test`.

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` | Variável não definida | `backend/.env` (rodando de dentro de `backend/`) |
| `The specified key byte array is ... not secure enough` | `JWT_SECRET` curta | 32+ caracteres |
| `blocked by CORS policy` (e funciona no Postman) | Origem diferente (`127.0.0.1` ≠ `localhost`) ou CORS fora do filtro | `CORS_ALLOWED_ORIGIN` e a Parte 2.5 |
| `401` com token certo | Header sem `Bearer ` ou token expirado (2 h) | Refazer o login |
| `500` em vez de `404` | Faltou o handler de `NoResourceFoundException` | Parte 2.4 |
| Python sem licença quando chamado pelo Java | `GRB_*` não passadas ao subprocesso | Parte 5.2, item 1 |
| `violates check constraint "chk_movimentacao_restante"` | `ENTRADA` sem `quantidade_restante` | O repositório grava `restante = quantidade` |
| Pedidos simultâneos vendem além do estoque | Faltou `FOR UPDATE` | `travar(idsOrdenados)` antes de ler as linhas |
| `cannot find symbol` em imports de teste | Pacotes de teste do Boot 4 | Sugestão do IntelliJ |

## Checklist final
- [ ] Fase 1: `401`/`403`/CORS/erros; `dependency:tree` sem conflito
- [ ] Fase 2: os 10 itens; o Leonardo aceitou
- [ ] `RegraAcai` com testes; bug da composição repetida corrigido
- [ ] Assinaturas combinadas com o Diogo (por escrito)
- [ ] Fase 3: os 8 itens e os cenários S1 a S5 e concorrência como testes
- [ ] `hoje` do `Clock` (São Paulo) em todo SQL de validade
- [ ] Hello World `echo` com `@Profile("dev")`; recomendação conferida com 151/1/0
- [ ] `GRB_*` no subprocesso
- [ ] Testcontainers carregando `bd/Script_DDL.sql`
- [ ] README, Contrato REST v2 e evidências

## Depois (Sprint 4, só sinalização)
Cancelamento de pedido (US27, devolve as quantidades às linhas), alertas (US15/US16), `PATCH` de unidades (US26), dashboard (US17), reposição (US14), marketing (US18) e o **deploy** (US20). Pendência **F12** antes da Fase 6.
