# Preparando o ambiente — Sprint 1 (Comunicação Hello World)

**Versão 3 — alinhada à `ARQUITETURA.md` e ao Contrato de Comunicação: PostgreSQL + Spring `JdbcTemplate` + Python chamado como subprocesso** (substitui a v2)

> **Status (25/09/2026):** o módulo Python (seção 3) foi executado e testado com `pytest`. O código Java, o SQL e o JavaScript ainda **não foram executados** — a equipe valida na Sprint 1 e corrige este documento no que divergir. Itens marcados como **proposta** ainda não foram decididos pela equipe (lista na seção "Pendências", no fim). Este roteiro segue o **Contrato de Comunicação** (`docs/Contrato_de_Comunicacao_Mente_Fria.md`): valores de `status` do Python (`sucesso`/`erro`), campo `echo` do Hello World e JSON em snake_case.

**Contexto:** vocês nunca fizeram comunicação entre programas/APIs, então a ideia central do roteiro continua a mesma: **isolar cada camada e testá-la sozinha antes de ligar a próxima**. Abaixo está o que precisa existir/estar instalado em cada pasta do repositório antes de codar a lógica.

Esta versão substitui a anterior porque as decisões D1–D5 da `ARQUITETURA.md` mudaram duas peças importantes: o backend acessa o banco com **`JdbcTemplate`** (não mais JPA/Hibernate) e o Python **não é mais um servidor FastAPI** — o Java executa o script Python como **subprocesso** e troca JSON por entrada/saída padrão.

```
/docs           — documentação e Script_DDL.sql (local do script: proposta)
/backend        — Java + Spring Boot (JdbcTemplate)
/frontend       — HTML, CSS, JavaScript
/optimization   — Python (script chamado pelo Java; Gurobi entra na Sprint 2)
```

---

## O que mudou da v2 para a v3 (alinhamento ao Contrato de Comunicação)

Depois que o Kauê aprovou o contrato (20/09/2026), este roteiro passou a segui-lo:

| Assunto | v2 | v3 | Contrato |
|---|---|---|---|
| `status` do Python | `OK` / `ERRO` | `sucesso` / `erro` | §1.2 e §1.3 |
| Campo do Hello World | `texto` | `echo` | §1.5 |
| Nomes no JSON da API | camelCase (`unidadeMedida`) | snake_case (`unidade_medida`), com `@JsonProperty` | §2 (item B6, a confirmar com Lucas e Leonardo) |

O restante (banco, `JdbcTemplate`, subprocesso, CORS, `config.js`) não mudou.

---

## O que mudou em relação à versão anterior deste roteiro (v1 → v2)

| Assunto | Versão anterior | Agora | Origem |
|---|---|---|---|
| Acesso a dados no Java | Spring Data JPA + Hibernate (`@Entity`, `JpaRepository`) | Spring JDBC: `JdbcTemplate` + SQL escrito por nós | D4 |
| Java ↔ Python | FastAPI + uvicorn na porta 8000, chamado por `RestTemplate` (HTTP) | Java executa `python otimizador.py` (`ProcessBuilder`); JSON via stdin/stdout. **Não existe servidor Python** | D2 |
| Teste do Python isolado | Swagger (`/docs`) | `pytest` (e execução direta no terminal) | ARQUITETURA §7, passo 5 |
| Banco do Hello World | `mente_fria_scratch` + tabela `teste` | Banco `mente_fria` criado a partir do `Script_DDL.sql` (schema real + seed) | ARQUITETURA §7, passo 1 |
| Senha do banco | Escrita no `application.properties` | Variáveis de ambiente (`DB_*`), nunca no código | ARQUITETURA §3.2 e §5 |
| CORS | Origem fixa no código; só `GET`/`POST` | Origem vem de `CORS_ALLOWED_ORIGIN`; header `Authorization` já liberado (login JWT vem depois) | ARQUITETURA §3.1 |
| URL da API no front | Escrita dentro do `fetch` | `frontend/js/config.js`; sem `innerHTML` com dados vindos da API | ARQUITETURA §3.1 e §4 |
| Versão do PostgreSQL | 15+ | **16** (README, Plano de Testes, DER); versão final ainda a fixar | README / DER |
| Endpoints | `/api/teste` | `/api/saude`, `/api/ingredientes`, `/api/teste/python` (temporário) | ARQUITETURA §7, passos 2 e 6 |

**Lembretes de PostgreSQL** (continuam valendo do roteiro anterior):

- Não existe `CREATE DATABASE IF NOT EXISTS` — o comando dá erro se o banco já existir.
- `\c nome_do_banco` é meta-comando do `psql` (equivale ao `USE` do MySQL); não funciona em outros clientes.
- Driver e porta: `org.postgresql` / `jdbc:postgresql://...:5432/...`.
- Identificadores não citados viram minúsculo automaticamente — usar nomes de tabela em minúsculo.
- Colunas autoincrementáveis: o schema real usa `GENERATED ALWAYS AS IDENTITY` (e não `SERIAL`) — ver `DER.md`.

---

## Pré-requisitos gerais

- Git
- **JDK 21** (decidido pelo grupo em 25/09/2026)
- **Docker** — para rodar o PostgreSQL 16 em container (recomendado; o README já exige Docker para os testes de integração). Alternativa: PostgreSQL 16 instalado localmente
- **Python 3.14** (decidido pelo grupo em 25/09/2026) com `venv`. Nenhum framework web: só `pytest` por enquanto (`gurobipy` entra na Sprint 2). **Atenção:** confirmar com `pip install gurobipy` que a versão do Gurobi disponível já suporta o Python 3.14 antes de codar o modelo real — ver `ARQUITETURA.md`, seção 2.1.
- Cliente de API: **Postman** ou **Insomnia**. Para `GET`, o próprio navegador também serve (basta abrir a URL)
- **VS Code** com a extensão **Live Server** para servir o `/frontend` estático sem build
- IDE para Java (**IntelliJ Community** é o mais direto para Spring Boot)
- **pgAdmin** (opcional) — interface gráfica para visualizar tabelas

A ordem de setup abaixo segue a ordem do roteiro (1.1 → 1.11): **banco → Java↔banco → Python isolado → Java↔Python → CORS → front**.

Antes do primeiro commit, criem o `.gitignore` do repositório (senhas e ambientes virtuais **nunca** vão para o Git):

```
.env
optimization/venv/
backend/target/
__pycache__/
.pytest_cache/
```

---

## 1. Banco de dados (passos 1.1–1.2)

O `Script_DDL.sql` do projeto já cria todas as tabelas **e** insere os dados de exemplo (seed), mas **não** cria o banco em si. Por isso o banco é criado antes, e o script roda dentro dele.

**Opção A — Docker (recomendada).** O container `postgres:16` já vem com codificação UTF-8, o que evita diferenças de acentuação entre as máquinas da equipe.

```bash
docker run --name mente-fria-db -e POSTGRES_PASSWORD=SUA_SENHA_LOCAL -e POSTGRES_DB=mente_fria -p 5432:5432 -d postgres:16
docker cp docs/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
```

Nos dias seguintes, basta `docker start mente-fria-db` (o `docker run` só se faz uma vez).

**Opção B — PostgreSQL local:**

```bash
psql -U postgres -c "CREATE DATABASE mente_fria;"
psql -U postgres -d mente_fria -f docs/Script_DDL.sql
```

Neste caminho, confirmem que o banco é UTF-8: `psql -U postgres -d mente_fria -c "SHOW server_encoding;"` deve devolver `UTF8`.

**Conferir** (deve listar 7 ingredientes, do "Açaí (polpa)" ao "Copo descartável 500ml"):

```bash
docker exec mente-fria-db psql -U postgres -d mente_fria -c "SELECT id, nome, unidade_medida FROM ingrediente ORDER BY id;"
```

**Recomeçar do zero** (substitui a ideia do banco "scratch": como o banco é recriado em segundos a partir do script, ele é tão seguro quanto era o de teste). Pare o Spring antes, senão o `DROP` falha por conexão aberta:

```bash
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE mente_fria;" -c "CREATE DATABASE mente_fria;"
docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
```

**Observação:** rodar o script duas vezes no mesmo banco dá erro (as tabelas já existem) — para repetir, use o "recomeçar do zero" acima. Se já houver um PostgreSQL local usando a porta 5432, o container não sobe; pare o local ou mapeie outra porta (`-p 5433:5432`) e ajuste `DB_PORT`.

---

## 2. Backend Java — conexão isolada com o banco (passos 1.3–1.5)

### 2.1 Gerar o projeto

Em start.spring.io: **Maven**, **Java**, e as dependências **Spring Web**, **JDBC API** e **PostgreSQL Driver**. **Não** marque *Spring Data JPA* (decisão D4). Extraia o projeto dentro de `/backend`. (Maven e Spring Boot 3.3.4 foram decididos pelo grupo em 25/09/2026 — ver `ARQUITETURA.md`, seção 2.1; os exemplos já usam Maven, como no README.)

Nos exemplos abaixo, `com.mentefria` é só um pacote de exemplo — ajustem ao pacote gerado.

### 2.2 `application.properties`

Arquivo: `backend/src/main/resources/application.properties` — configura a conexão **sem nenhuma senha escrita no arquivo**.

```properties
spring.config.import=optional:file:../.env[.properties]

spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:mente_fria}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

server.port=8080

app.cors.allowed-origins=${CORS_ALLOWED_ORIGIN:http://127.0.0.1:5500,http://localhost:5500}
app.python.cmd=${PYTHON_CMD:python}
app.otimizador.script=${OTIMIZADOR_SCRIPT:../optimization/otimizador.py}
```

Aqui não existe `ddl-auto` nem `spring.jpa.*`: sem JPA, **nada** cria ou altera tabelas — o `Script_DDL.sql` é a única fonte do schema.

A primeira linha (`spring.config.import`) faz o Spring ler o arquivo `.env` da raiz do repositório (o caminho `../.env` vale quando o Spring roda a partir da pasta `/backend`). É o que faz o `.env` do README funcionar — o Spring **não** lê `.env` sozinho.

### 2.3 Variáveis de ambiente

Arquivo: `.env.example` (raiz do repositório, **este** vai para o Git, sem valores secretos). Cada integrante copia para `.env` e preenche:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mente_fria
DB_USER=postgres
DB_PASSWORD=
CORS_ALLOWED_ORIGIN=http://127.0.0.1:5500,http://localhost:5500
PYTHON_CMD=python
OTIMIZADOR_SCRIPT=../optimization/otimizador.py
```

Alternativa ao `.env`: definir as variáveis na hora de rodar — no terminal (`export DB_USER=postgres DB_PASSWORD=...` no Linux/macOS; `$env:DB_USER="postgres"; $env:DB_PASSWORD="..."` no PowerShell) ou em *Run > Edit Configurations > Environment variables* do IntelliJ. Se aparecer `Could not resolve placeholder 'DB_USER'`, as variáveis não foram carregadas (caminho do `.env` errado ou variável não definida).

### 2.4 Código

**Arquivo:** `backend/src/main/java/com/mentefria/ingrediente/IngredienteResumo.java` — objeto simples que carrega os campos que o Hello World devolve (um `record` do Java). O contrato usa **snake_case** no JSON (`unidade_medida`), e a anotação `@JsonProperty` faz essa conversão.

```java
package com.mentefria.ingrediente;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IngredienteResumo(int id, String nome, @JsonProperty("unidade_medida") String unidadeMedida) {}
```

> `@JsonProperty` está no pacote `com.fasterxml.jackson.annotation`, que **continua o mesmo** no Spring Boot 3.5 e no 4 (a biblioteca de anotações é compartilhada entre o Jackson 2 e o 3), então esse import não depende da versão. Se o Lucas preferir uma configuração global do Jackson em vez da anotação, só este arquivo muda.

**Arquivo:** `.../ingrediente/IngredienteRepository.java` — único lugar que conhece o SQL de ingredientes.

```java
package com.mentefria.ingrediente;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IngredienteRepository {

    private final JdbcTemplate jdbc;

    public IngredienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<IngredienteResumo> listar() {
        return jdbc.query(
            "SELECT id, nome, unidade_medida FROM ingrediente ORDER BY id",
            (rs, linha) -> new IngredienteResumo(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("unidade_medida")));
    }
}
```

> **Regra do projeto:** todo SQL com valores vindos do usuário usa parâmetros (`?` ou `NamedParameterJdbcTemplate`). **Nunca** concatenar texto no SQL (SQL Injection). Este `SELECT` não recebe parâmetros; os próximos, sim.

**Arquivo:** `.../ingrediente/IngredienteController.java` — expõe `GET /api/ingredientes`.

```java
package com.mentefria.ingrediente;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IngredienteController {

    private final IngredienteRepository repositorio;

    public IngredienteController(IngredienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping("/ingredientes")
    public List<IngredienteResumo> listar() {
        return repositorio.listar();
    }
}
```

**Arquivo:** `.../saude/SaudeController.java` — `GET /api/saude` confirma que o Java consegue falar com o banco (`SELECT 1`).

```java
package com.mentefria.saude;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SaudeController {

    private final JdbcTemplate jdbc;

    // Exceção didática: no projeto real o Controller não fala com o banco direto (Controller → Service → Repository).
    public SaudeController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/saude")
    public Map<String, String> saude() {
        jdbc.queryForObject("SELECT 1", Integer.class); // se o banco estiver fora, a chamada devolve erro 500
        return Map.of("status", "ok", "banco", "ok");
    }
}
```

### 2.5 Rodar e testar (passo 1.5)

Rodar com `./mvnw spring-boot:run` (no Windows, `.\mvnw spring-boot:run`), dentro de `/backend`.

- `GET http://localhost:8080/api/saude` → `{"status":"ok","banco":"ok"}`
- `GET http://localhost:8080/api/ingredientes` → lista com 7 itens; o primeiro é `{"id":1,"nome":"Açaí (polpa)","unidade_medida":"kg"}`

Se as duas respostas vierem certas, o Java já conversa com o banco.

**Observação:** o `IngredienteResumo` é só um "molde" para a resposta. Sem JPA, o mapeamento linha → objeto é escrito por nós (a expressão `(rs, linha) -> ...`) — um pouco mais de código por tabela, mas cada SQL fica visível e revisável.

---

## 3. Python — módulo isolado (passos 1.6–1.7)

**Não existe servidor Python.** O Java executa o script quando precisa, entrega o JSON pela entrada padrão (stdin) e lê a resposta na saída padrão (stdout). Por isso o Python pode ser testado sozinho, sem Java e sem banco — e o mesmo contrato servirá para a otimização de verdade na Sprint 2.

Dentro de `/optimization`:

```bash
python -m venv venv
source venv/bin/activate      # Windows: venv\Scripts\activate
pip install pytest
pip freeze > requirements.txt
```

**Arquivo:** `optimization/otimizador.py` — recebe JSON, devolve JSON. No Hello World, recebe `{"echo": "..."}` e devolve o mesmo texto em maiúsculas, com os `status` do contrato (`sucesso`/`erro`), para provar que o Python processou algo.

```python
"""Módulo de otimização do Mente Fria — versão Hello World.

Segue o Contrato de Comunicação (docs/Contrato_de_Comunicacao_Mente_Fria.md, §1):
lê um JSON na entrada padrão (stdin) e escreve um JSON na saída padrão (stdout).
Não acessa banco nem rede.
Código de saída 0 = há um JSON válido no stdout (inclusive com status "erro").
Código diferente de 0 = falha inesperada (o detalhe vai para o stderr).
"""
import json
import sys


def processar(entrada: dict) -> dict:
    texto = entrada.get("echo")
    if not isinstance(texto, str):
        return {"status": "erro", "mensagem": "Campo 'echo' ausente ou inválido"}
    return {"status": "sucesso", "echo": texto.upper()}


def main() -> int:
    # Garante UTF-8 na entrada e na saída, independentemente do sistema (Windows usa outro padrão)
    sys.stdin.reconfigure(encoding="utf-8")
    sys.stdout.reconfigure(encoding="utf-8")
    try:
        entrada = json.load(sys.stdin)
    except json.JSONDecodeError:
        print(json.dumps({"status": "erro", "mensagem": "JSON de entrada inválido"}, ensure_ascii=False))
        return 0
    print(json.dumps(processar(entrada), ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

**Arquivo:** `optimization/test_otimizador.py` — roda o script exatamente como o Java fará.

```python
import json
import subprocess
import sys
from pathlib import Path

SCRIPT = Path(__file__).parent / "otimizador.py"


def executar(texto_entrada: str):
    """Roda o script como o Java fará: JSON no stdin, JSON no stdout."""
    return subprocess.run(
        [sys.executable, str(SCRIPT)],
        input=texto_entrada,
        capture_output=True,
        text=True,
        encoding="utf-8",
        timeout=10,
    )


def test_eco_com_acentos():
    resultado = executar(json.dumps({"echo": "Açaí (polpa)"}, ensure_ascii=False))
    assert resultado.returncode == 0, resultado.stderr
    assert json.loads(resultado.stdout) == {"status": "sucesso", "echo": "AÇAÍ (POLPA)"}


def test_campo_ausente_retorna_erro_tratado():
    resultado = executar(json.dumps({"outro": 1}))
    assert resultado.returncode == 0
    assert json.loads(resultado.stdout)["status"] == "erro"


def test_json_invalido_retorna_erro_tratado():
    resultado = executar("isto não é json")
    assert resultado.returncode == 0
    assert json.loads(resultado.stdout)["status"] == "erro"
```

Rodar `pytest` dentro de `/optimization` — devem passar os 3 testes. Também dá para testar "à mão" (Linux/macOS/Git Bash):

```bash
echo '{"echo":"Açaí (polpa)"}' | python otimizador.py
# {"status": "sucesso", "echo": "AÇAÍ (POLPA)"}
```

Isso confirma que o Python responde **antes** de ligar ao Java. Sobre os acentos: sem as duas linhas `reconfigure(...)`, no Windows o "Í" e o "Ç" podem chegar quebrados — é um erro clássico dessa integração, e o seed do projeto já tem "Açaí".

---

## 4. Java chamando Python (passos 1.8–1.9)

Aqui não há bean de cliente HTTP nem `RestTemplate`: o Java só precisa **iniciar um processo**.

**Arquivo:** `backend/src/main/java/com/mentefria/otimizacao/OtimizadorPython.java` — executa o script e devolve o JSON de saída. No projeto real, ele ficará atrás de uma interface (ex.: `OtimizadorProducao`), como diz a `ARQUITETURA.md`.

```java
package com.mentefria.otimizacao;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OtimizadorPython {

    private static final int TIMEOUT_SEGUNDOS = 10; // valor provisório

    private final String comandoPython;
    private final String script;

    public OtimizadorPython(@Value("${app.python.cmd}") String comandoPython,
                            @Value("${app.otimizador.script}") String script) {
        this.comandoPython = comandoPython;
        this.script = script;
    }

    public String executar(String jsonEntrada) {
        Process processo = null;
        try {
            processo = new ProcessBuilder(comandoPython, script)
                    .redirectError(ProcessBuilder.Redirect.INHERIT) // erros do Python aparecem no console do Spring
                    .start();

            try (OutputStream stdin = processo.getOutputStream()) {
                stdin.write(jsonEntrada.getBytes(StandardCharsets.UTF_8));
            } // fechar o stdin é obrigatório: o Python só segue quando a entrada termina

            if (!processo.waitFor(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
                processo.destroyForcibly();
                throw new IllegalStateException("O módulo de otimização demorou demais para responder.");
            }
            if (processo.exitValue() != 0) {
                throw new IllegalStateException(
                        "O módulo de otimização falhou (código " + processo.exitValue() + ").");
            }
            // Ler depois do waitFor é seguro para saídas pequenas (Hello World e recomendação de produção).
            return new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível executar o módulo de otimização. Confira PYTHON_CMD e OTIMIZADOR_SCRIPT.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (processo != null) {
                processo.destroyForcibly();
            }
            throw new IllegalStateException("Execução do módulo de otimização interrompida.", e);
        }
    }
}
```

**Arquivo:** `.../teste/TestePythonController.java` — endpoint **temporário** que percorre a cadeia inteira: pega o nome do primeiro ingrediente no banco, manda ao Python e devolve o que o Python respondeu.

```java
package com.mentefria.teste;

import com.mentefria.ingrediente.IngredienteRepository;
import com.mentefria.otimizacao.OtimizadorPython;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper; // Spring Boot 3.3.4 (Jackson 2), decidido pelo grupo em 25/09/2026

@RestController
@RequestMapping("/api/teste")
public class TestePythonController {

    private final IngredienteRepository repositorio;
    private final OtimizadorPython otimizador;
    private final ObjectMapper mapper;

    public TestePythonController(IngredienteRepository repositorio,
                                 OtimizadorPython otimizador,
                                 ObjectMapper mapper) {
        this.repositorio = repositorio;
        this.otimizador = otimizador;
        this.mapper = mapper;
    }

    @GetMapping(value = "/python", produces = "application/json;charset=UTF-8")
    public String testarPython() throws com.fasterxml.jackson.core.JsonProcessingException {
        String nome = repositorio.listar().get(0).nome(); // exige o seed carregado (seção 1)
        String entrada = mapper.writeValueAsString(Map.of("echo", nome));
        return otimizador.executar(entrada);
    }
}
```

> **Nota — versão do Spring Boot (decidida):** o grupo escolheu o Spring Boot 3.3.4 em 25/09/2026, que usa o Jackson 2 — por isso o código acima já usa `com.fasterxml.jackson.databind.ObjectMapper` direto, sem a alternativa do Jackson 3 (`tools.jackson`, usada só a partir do Spring Boot 4.x) que aparecia aqui antes.

Testar de novo no Postman (ou no navegador): `GET http://localhost:8080/api/teste/python` → `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}`. Se voltar certo, o texto já passou por **banco → Java → Python → Java**.

> **Remover** o `TestePythonController` assim que o Hello World for validado: ele é só para teste, não tem controle de acesso e não deve chegar à produção.

**Observação:** `python` e o caminho do script vêm de `PYTHON_CMD` e `OTIMIZADOR_SCRIPT`. O caminho é relativo à pasta de onde o Spring é iniciado (`/backend` quando se usa `./mvnw`); se aparecer "No such file", use o caminho absoluto. Em Linux/macOS o comando costuma ser `python3`, e no Windows às vezes `py`.

---

## 5. CORS (o ponto que mais costuma travar quem nunca fez isso)

Por padrão, o navegador bloqueia uma página servida numa origem (ex.: `http://127.0.0.1:5500`, o Live Server) de chamar uma API noutra origem (`http://localhost:8080`), mesmo sendo tudo local. É sempre preciso liberar isso no backend antes de o front funcionar. Atenção: o **Postman não aplica essa regra** — por isso "funciona no Postman e falha no navegador" é o sintoma clássico.

**Arquivo:** `backend/src/main/java/com/mentefria/config/CorsConfig.java`

```java
package com.mentefria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origensPermitidas;

    // A lista vem de CORS_ALLOWED_ORIGIN, separada por vírgula e sem espaços.
    public CorsConfig(@Value("${app.cors.allowed-origins}") String[] origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type");
    }
}
```

Diferenças para o roteiro anterior: a origem não fica fixa no código, e o header `Authorization` já está liberado, porque o login por JWT (próxima etapa da `ARQUITETURA.md`) vai mandar o token nesse header. Em produção, `CORS_ALLOWED_ORIGIN` será a URL do GitHub Pages do projeto.

**Cuidado:** `http://127.0.0.1:5500` e `http://localhost:5500` são origens **diferentes** para o navegador. Liberem a que o Live Server realmente abrir (o `.env.example` já traz as duas) e **reiniciem o Spring** depois de mudar a variável.

---

## 6. Frontend — por último (passos 1.10–1.11)

Em `/frontend`:

**Arquivo:** `frontend/js/config.js` — site estático não tem variáveis de ambiente, então a URL da API fica num único lugar (trocada para a URL do Render no deploy).

```js
const CONFIG = {
  API_BASE_URL: "http://localhost:8080",
};
```

**Arquivo:** `frontend/index.html`

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <title>Mente Fria — Hello World</title>
</head>
<body>
  <h1>Mente Fria</h1>
  <p id="status">Carregando...</p>
  <h2>Ingredientes (banco → Java → tela)</h2>
  <ul id="lista"></ul>
  <h2>Texto processado pelo Python</h2>
  <p id="python"></p>

  <script src="js/config.js"></script>
  <script src="js/script.js"></script>
</body>
</html>
```

**Arquivo:** `frontend/js/script.js` — usa `textContent` (nunca `innerHTML`) para exibir dados vindos da API, e mostra mensagens claras de carregando/erro.

```js
async function buscar(caminho) {
  const resposta = await fetch(`${CONFIG.API_BASE_URL}${caminho}`);
  if (!resposta.ok) {
    throw new Error(`O servidor respondeu com erro ${resposta.status}`);
  }
  return resposta.json();
}

async function iniciar() {
  const status = document.getElementById("status");
  try {
    const ingredientes = await buscar("/api/ingredientes");
    const lista = document.getElementById("lista");
    ingredientes.forEach((ingrediente) => {
      const item = document.createElement("li");
      item.textContent = `${ingrediente.nome} (${ingrediente.unidade_medida})`;
      lista.appendChild(item);
    });

    const python = await buscar("/api/teste/python");
    document.getElementById("python").textContent = python.echo;

    status.textContent = "Conectado ao backend.";
  } catch (erro) {
    status.textContent = "Não foi possível falar com o servidor. Confira se o backend está rodando.";
    console.error(erro);
  }
}

iniciar();
```

Servir a pasta com o Live Server (*Go Live*). Se o CORS do passo 5 estiver certo, a lista de ingredientes e o texto "AÇAÍ (POLPA)" aparecem na tela.

---

## Checklist de execução (uma coisa de cada vez)

- [ ] Subir o PostgreSQL 16, carregar o `Script_DDL.sql` → conferir os 7 ingredientes direto no `psql`/pgAdmin
- [ ] Subir o Spring Boot → testar `/api/saude` e `/api/ingredientes` no Postman
- [ ] Rodar `pytest` em `/optimization` → 3 testes passando (sem Java, sem banco)
- [ ] Ligar Java → Python → testar `/api/teste/python` no Postman (acentos corretos)
- [ ] Configurar CORS → abrir o front no Live Server → ver a lista e o texto do Python na tela
- [ ] Antes de commitar: `.env` no `.gitignore`, nenhuma senha no código, `.env.example` versionado
- [ ] Ao validar tudo: **apagar** o `TestePythonController`

Não pulem etapa: se algo quebrar, vocês saberão exatamente em qual camada está o problema, porque cada uma já foi validada isolada antes.

---

## Erros comuns

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| `password authentication failed for user "postgres"` | Senha do `.env` diferente da senha do container | Ajustar o `.env` ou recriar o container com a senha desejada |
| `Connection refused` ao subir o Spring | PostgreSQL parado, ou porta 5432 ocupada por outro Postgres | `docker start mente-fria-db`; conferir `DB_PORT` |
| `Could not resolve placeholder 'DB_USER'` | Variáveis não carregadas | Conferir o `.env` e o caminho `../.env` do `spring.config.import`; ou definir as variáveis no terminal/IntelliJ |
| `relation "ingrediente" does not exist` | Script não foi carregado (ou banco errado) | Rodar de novo a seção 1 |
| `Cannot run program "python"` | Comando diferente na máquina | Ajustar `PYTHON_CMD` (`python3`, `py`) |
| `/api/teste/python` demora e dá erro de tempo | O Python não recebeu o fim da entrada, ou travou | Conferir se o `stdin` é fechado no Java; ver o erro do Python no console do Spring |
| Acentos quebrados na resposta do Python | Codificação do sistema (comum no Windows) | Manter as duas linhas `reconfigure(encoding="utf-8")` no `otimizador.py` |
| No navegador: "blocked by CORS policy" (e no Postman funciona) | Origem do Live Server diferente da liberada | Ajustar `CORS_ALLOWED_ORIGIN` (`127.0.0.1` ≠ `localhost`, e a porta) e reiniciar o Spring |

---

## Como este roteiro se liga à `ARQUITETURA.md` (§7)

| Passo da `ARQUITETURA.md` | Neste roteiro |
|---|---|
| 1. Banco local + `Script_DDL.sql` | Seção 1 |
| 2. Backend ↔ Banco (`/api/saude`, `/api/ingredientes`) | Seção 2 |
| 3. Frontend ↔ Backend (com CORS) | Seções 5 e 6 |
| 4. Login por perfil com JWT (US1–US3) | **Fora deste roteiro** — próxima etapa |
| 5. Python isolado + `pytest` | Seção 3 (versão Hello World; os cenários CT23/CT24 entram na Sprint 2) |
| 6. Java chama Python | Seção 4 (versão Hello World; o endpoint real é `POST /api/producao/recomendacao`) |
| 7. Deploy do esqueleto (Pages + Render) | **Fora deste roteiro** — recomendado para o fim da Sprint 1 |

---

## Pendências surgidas ao adaptar o roteiro

- **Versões decididas pelo grupo em 25/09/2026** (`ARQUITETURA.md`, seção 2.1): JDK 21, Spring Boot 3.3.4 (já refletido na seção 4), Maven, Python 3.14 (conferir compatibilidade com o Gurobi antes de seguir), PostgreSQL 16 (disponível no Render).
- **Onde fica o `Script_DDL.sql`** no repositório: os comandos assumem `docs/Script_DDL.sql` (proposta).
- **Variáveis `PYTHON_CMD` e `OTIMIZADOR_SCRIPT`**: propostas provisórias. O **timeout de 10 s** já consta no contrato (§1), também como valor provisório até medir com o Gurobi.
- **JSON em snake_case** (item B6 do contrato): adotado com `@JsonProperty`; falta a confirmação do Lucas (que pode preferir a configuração global do Jackson) e do Leonardo (que consome os campos). Os demais itens a confirmar estão na seção 2.4 do contrato.
- **Carregar o `.env`** com `spring.config.import`: proposta; se a equipe preferir, dá para usar só variáveis de terminal/IntelliJ.
- **README:** atualizar a tabela de variáveis (`CORS_ALLOWED_ORIGIN`, `PYTHON_CMD`, `OTIMIZADOR_SCRIPT`) e o passo "Como rodar localmente" conforme este roteiro.
