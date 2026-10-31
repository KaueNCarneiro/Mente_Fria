# Tutorial — Sprint 4 — Backend (Java) do zero ao deploy — Lucas

**Para:** Lucas Gabriel Valadares Bassi · **Papel:** Product Owner (PO) e backend · **Data:** 30/10/2026 (decisão A44)
**Substitui:** `docs/Sprint_3/sprint-3-tutorial-backend-lucas.md`, que fica como registro.
**Calendário em vigor:** `docs/Sprint_4/sprint-4-roteiro.md`, §4. Se uma data daqui divergir, vale o roteiro.

---

## O que foi verificado e o que não foi

Todos os comandos deste tutorial foram **executados de verdade** em 29 e 30/10/2026, num **clone temporário** do repositório. Partimos da `main` de 28/10, commit `af05b26`. O ambiente:
- Windows PowerShell 5.1;
- JDK 25.0.2;
- Docker 29.8.0;
- um banco PostgreSQL **temporário**. O `mente-fria-db` não foi tocado.

| O quê | Resultado real |
|---|---|
| Fase 1: cópia dos arquivos, `pom.xml`, testes | **18 testes, 0 falhas** (9 s) |
| Fase 2: cópia, fluxo a dois com o Diogo, testes | **46 testes, 0 falhas** (17 s) |
| Fase 3: cópia, testes | **15 testes, 0 falhas** (18 s) |
| Fase 4A: cópia, testes com dublê e com o Python real | **7** com dublê; **4 + 2** com o Python real |
| Fase 6: cópia, testes (depois da Fase 5 do Kauê) | **5 + 4 + 3 testes, 0 falhas** |
| Todas as fases juntas, com o Python real | **95 testes, 0 falhas** (38 s) |
| Conferências manuais | Todas as respostas "Você deve ver" foram copiadas da execução real |
| Fluxo a dois (o Diogo entra na sua branch) | Simulado com um "GitHub de mentira" (um repositório na própria máquina): os comandos são os mesmos |

**Não foi possível verificar aqui:**
- o push e o PR no GitHub de verdade (dependem da sua conta e das permissões no repositório);
- o Render.

**O que muda na sua tela:**
- datas, horários e números de `id` serão diferentes dos mostrados;
- o resto deve bater.

> **Se alguma saída não bater:** pare, copie a mensagem **exata** e mande no grupo. Não tente adivinhar.

---

## Como usar este tutorial

### Qual terminal usar

Todos os comandos são para o **Windows PowerShell**. Para abrir:

- **Fora da IDE:** Menu Iniciar → digite `PowerShell` → **Windows PowerShell**.
- **No IntelliJ:** *View* → *Tool Windows* → *Terminal*.

**Como saber se você está no PowerShell:** a linha começa com **`PS`**, por exemplo `PS C:\Users\lucas>`.

| Se a linha parece… | Você está no… | O que fazer |
|---|---|---|
| `PS C:\Users\...>` | PowerShell | Pode seguir |
| `C:\Users\...>` (sem `PS`) | CMD (Prompt de Comando) | Feche e abra o PowerShell |
| `usuario@PC MINGW64 ~ $` | Git Bash | Feche e abra o PowerShell. Vários comandos daqui não funcionam no Git Bash |

### Entrar na pasta do projeto

Quase todo comando assume que você está na **pasta raiz do repositório**, a que tem `backend`, `bd`, `docs`, `frontend` e `optimization`.

1. No Explorador de Arquivos, abra a pasta do projeto.
2. Clique com o botão direito no nome da pasta → **Copiar como caminho**.
3. No PowerShell, digite `cd ` (com espaço), cole o caminho e tecle Enter. Exemplo:

```powershell
cd "C:\Users\lucas\Documents\Mente_Fria"
dir
```

**Você deve ver** algo assim. Datas e tamanhos mudam:

```
Mode   LastWriteTime   Length Name
----   -------------   ------ ----
d----- ...                    backend
d----- ...                    bd
d----- ...                    docs
d----- ...                    frontend
d----- ...                    optimization
-a---- ...                    .gitignore
-a---- ...                    README.md
```

Neste tutorial, **`<PASTA_DO_PROJETO>`** significa esse caminho, o seu.

### Dois terminais

Em várias partes você vai usar **dois PowerShells abertos ao mesmo tempo**:
- **Terminal A:** para comandos (Git, testes, chamadas à API).
- **Terminal B:** só para deixar o backend rodando.

Cada passo diz qual usar.

### Formato de cada passo

- **Objetivo:** o que você vai conseguir no fim.
- **Onde:** qual terminal, qual pasta, qual branch.
- **Faça:** os comandos. Copie **bloco por bloco**. Linhas que começam com `#` são comentários: o PowerShell ignora.
- **O que cada parte faz:** a explicação.
- **Você deve ver:** a saída real. Só avance quando bater.
- **Se der errado:** os erros mais prováveis.
- **Antes de avançar:** uma lista para conferir.

### Marcadores para trocar

| Marcador | Troque por |
|---|---|
| `<PASTA_DO_PROJETO>` | O caminho da pasta do repositório na sua máquina |
| `<SENHA_DO_BANCO>` | A senha do PostgreSQL do container `mente-fria-db`: a mesma que você pôs em `POSTGRES_PASSWORD` quando criou o container na Sprint 1. Se o seu `backend\.env` já existe, é o valor de `DB_PASSWORD` dele |

---

## Plano de tempo e "pronto quando"

| Dia | O que você faz | Parte |
|---|---|---|
| **Qui 29/10** (atrasada: faça primeiro) | Fase 1: segurança, token, CORS, erros. PR **na `main` até a noite** (marco M1) | 3 |
| **Sex 30/10** | Fase 2: cadastros e login, com os repositórios do Diogo. PR na `main` à noite (M2) | 4 |
| **Sáb 31/10** | Fase 3: estoque e pedido, com o Diogo. **Criar a conta do Render** (só a conta) | 5 e 8 |
| **Dom 01/11** | Fase 4A: ponte Java → Python, **em dupla com o Kauê**. À noite, como PO: decidir o botão "Descartar" (M3) | 6 e 9 |
| **Seg 02/11** (feriado, dia de mais carga) | Manhã: Fase 6 (dashboard e marketing) e, como PO, o *gate* da US26. Tarde: **deploy definitivo** na sua conta, com o Kauê. Noite: todos os testes (95) | 7, 8 e 9 |
| **Ter 03 e qua 04/11** | Revisar PRs (inclusive os da US26 do Diogo); Tela 14, se o Leonardo precisar. **Qua, 18h: congelamento** | 9 |
| **Qui 05/11** | Evidências e contribuições individuais das Sprints 3 e 4 | 10 |
| **Sex 06/11** | Entrega e Review | — |

Se a Fase 1 atrasar mais um dia, faça a Fase 1 e a Fase 2 na sexta, nessa ordem, e **avise o grupo no mesmo dia**: o Leonardo depende da Fase 2 no sábado de manhã.

**Pronto quando:**
- [ ] cinco PRs na `main`, um por fase (1, 2, 3, 4A e 6), cada um com a saída dos testes daquela fase;
- [ ] `.\mvnw test` com **95 testes, 0 falhas**, usando o Python real;
- [ ] as 10 conferências da Fase 2 batendo, e o Leonardo avisado;
- [ ] o sistema publicado na sua conta do Render, com a URL no README;
- [ ] você sabe responder as "Perguntas para a Review" do fim de cada parte.

---

## Parte 0 — Conceitos em 5 minutos

### 0.1 Glossário

| Termo | O que é (sem jargão) | No nosso projeto |
|---|---|---|
| **Spring Boot** | Um "kit" que já traz servidor web, conexão com banco e segurança prontos. Você escreve só as regras | O `backend/` inteiro |
| **Bean** | Um objeto que o Spring cria e guarda para você | Toda classe com `@Service`, `@Repository`, `@RestController`, `@Component` ou `@Bean` |
| **Injeção de dependência** | Você declara no construtor do que precisa, e o Spring entrega pronto. Você não usa `new` | `UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder)` |
| **Controller** (`@RestController`) | A "porta de entrada": recebe a requisição HTTP e devolve JSON. Quase sem regra | `UsuarioController`, `PedidoController`… |
| **Service** (`@Service`) | Onde ficam as **regras de negócio** | "Só um Administrador", "pedido tudo ou nada" |
| **Repository** (`@Repository`) | Onde ficam os **SQLs**. Só fala com o banco | `UsuarioRepository`, do Diogo |
| **`NamedParameterJdbcTemplate`** | A ferramenta do Spring para rodar SQL com **parâmetros com nome** (`:email`), sem montar texto à mão | Todos os repositórios (decisão D4: sem JPA) |
| **`record`** | Uma classe enxuta só para carregar dados | `Usuario`, `Ingrediente`, os corpos dos JSONs |
| **`@Transactional`** | "Tudo ou nada": se algo falhar no meio, o banco desfaz tudo | Criar pedido e dar baixa no estoque |
| **`FOR UPDATE`** (trava) | "Esta linha é minha até eu terminar": outra operação espera | Dois pedidos ao mesmo tempo não vendem o mesmo açaí |
| **JWT / token** | Um "crachá" assinado que o backend entrega no login. O navegador manda o crachá em toda chamada | `Authorization: Bearer eyJ...`, válido por 2 horas |
| **Hash de senha (BCrypt)** | A senha embaralhada de um jeito que não dá para desfazer | `usuario.senha_hash`. A senha nunca é guardada |
| **401 × 403** | **401:** "não sei quem é você" (sem token ou token ruim). **403:** "sei quem é, mas você não pode" | O Funcionário tentando mudar preço recebe `403` |
| **CORS** | A regra do navegador que só deixa uma página falar com uma API se a API permitir aquela origem | Liberamos `http://127.0.0.1:5500`, `http://localhost:5500` e, no Render, a origem do GitHub Pages |
| **Preflight** | A "pergunta" `OPTIONS` que o navegador faz antes de um `PUT`/`PATCH`/`POST` com JSON | Precisa responder `200` sem pedir token |
| **`snake_case`** | Nomes com `_` (`preco_venda`). Em Java usamos `precoVenda` | Uma linha no `application.properties` traduz os dois sozinha |
| **Maven / `mvnw`** | O "gerenciador" do Java: baixa bibliotecas, compila, roda testes. O `mvnw` é o Maven que vem junto do projeto | `.\mvnw test`, `.\mvnw spring-boot:run` |
| **`pom.xml`** | A lista de bibliotecas do projeto (parecido com o `requirements.txt` do Python) | Ganha Spring Security, JWT e Testcontainers na Fase 1 |
| **Teste com MockMvc** | Um teste que "finge" requisições HTTP sem abrir o navegador nem a porta 8080 | `SegurancaEErrosTest` |
| **Testcontainers** | Uma biblioteca que **sobe um PostgreSQL de verdade no Docker** só para os testes e apaga no fim | Fases 2 a 6. Precisa do Docker aberto |
| **Dublê** (*mock*) | Um "ator" que faz o papel de outra peça nos testes | Os testes trocam o Python por dublês: rodam sem licença e sem Python |
| **Perfil** (*profile*) do Spring | Um "modo" que liga ou desliga partes da aplicação | `dev` liga a rota de teste do Python; `teste-seguranca` só existe nos testes |
| **`.env`** | Um arquivo com senhas e configurações da **sua** máquina. **Nunca** vai para o Git | `backend\.env` |
| **Branch / PR / merge** | **Branch:** uma "cópia de trabalho" do código. **PR:** o pedido para juntar a sua branch na `main`. **Merge:** a junção | Um PR por fase |

### 0.2 Como as peças se encaixam

```
 Navegador (frontend do Leonardo)
        |  HTTP + JSON + "Authorization: Bearer <token>"
        v
 +---------------------------------------------------------------+
 | backend (Spring Boot, porta 8080)                             |
 |                                                               |
 |  JwtAuthFilter --> SecurityConfig  (quem é você? pode entrar?)|
 |        |  sem token: 401   sem permissão: 403                 |
 |        v                                                      |
 |  Controller  ->  Service (regras, @Transactional)  ->  Repository (SQL)
 |                       |                                   |   |
 |                       v                                   v   |
 |               ProcessoPython ----> python otimizador.py  PostgreSQL
 |                                    (Gurobi)              (mente-fria-db)
 |  GlobalExceptionHandler: qualquer erro vira JSON {mensagem, campos}
 +---------------------------------------------------------------+
```

**A ideia que você vai defender na Review:** cada camada tem um papel só.
- O controller não tem regra.
- O service não tem SQL.
- O repository não tem regra de negócio.
- Assim, cada coisa é testada no lugar certo.

### 0.3 Os códigos que aparecem no projeto

| Código | O que significa | Onde está |
|---|---|---|
| **US***n* (ex.: US12) | História de usuário do Backlog | `docs/Backlog_Priorizado_Mente_Fria.docx` |
| **CT***n* (ex.: CT25) | Caso de teste do Plano de Testes | `docs/Plano_de_Testes_Mente_Fria.md` |
| **A***n*, **D***n*, **F***n* | Decisões registradas da equipe (A = auditoria, D = respostas de 13/10, F = análise de 14/10) | A1–A38 (e a legenda de D e F) em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`; A39 em diante em `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`; as respostas D estão no roteiro v4.6, §9, em `docs/Sprint_3/historico/` |
| **M1, M2, M3, M4** | Marcos do calendário (o que precisa estar na `main` em cada noite) | Roteiro da Sprint 4, §4 |
| ***Gate* da Fase 2** | O "portão": o Leonardo só liga as telas no backend real depois que as 10 conferências da Fase 2 passam | Parte 4.8 |
| **Fase P / Fase Q / Fase 5** | Trabalho do Kauê: módulo Python de produção (P), módulo de reposição (Q) e o Java da reposição (5) | Tutorial do Kauê |
| **Plano B1** | A "chave de reserva": `OTIMIZADOR_HABILITADO=false` faz produção e reposição responderem `503` sem chamar o Python | Parte 6 |
| **A26** | O travamento do Docker Desktop com contorno conhecido. **Nunca** "Reset to factory defaults" | Parte 1.2 |
| **G***n* (ex.: G8) | Pendências do grupo (decisões ainda abertas ou já tomadas) | `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`, §2 |
| **Tela** *n* (ex.: Tela 12) | O número de cada tela no protótipo navegável. Duas telas com o mesmo número, como **10/16** ou **11/17**, são a mesma tela vista pelo Administrador e pelo Funcionário | `docs/Roteiro do Protótipo Navegável — Mente Fria.md` |
| **S1–S5** | Os cinco cenários de estoque por linhas que os testes da Fase 3 reproduzem | Parte 5.4 |
| **WLS** | A licença pessoal do Gurobi do Kauê. **Não** é usada por você nem no Render (A34, A38) | — |

---

## Parte 1 — Preparar o computador (uma vez)

### 1.1 Conferir as ferramentas

**Objetivo:** ter certeza de que Java, Git e Docker estão instalados e no PATH.
**Onde:** Terminal A, em qualquer pasta.

```powershell
java -version
git --version
docker --version
```

**O que cada parte faz:** cada comando pergunta a versão de uma ferramenta. Se a ferramenta não estiver instalada, o PowerShell avisa "não é reconhecido como nome de cmdlet".

**Você deve ver** (os números podem ser um pouco diferentes):

```
java version "25.0.2" 2026-01-20 LTS
...
git version 2.53.0.windows.1
Docker version 29.8.0, build 88096ef
```

| Se der errado | O que fazer |
|---|---|
| `java version "17..."` ou `"21..."` | O projeto usa **Java 25**. Instale o JDK 25 e confira de novo, num PowerShell **novo** |
| `'java' não é reconhecido…` | O JDK não está no PATH. No IntelliJ, *File* → *Project Structure* → *SDK* mostra onde ele está. Peça ajuda no grupo para ajustar o PATH |

### 1.2 Docker e o banco de desenvolvimento

**Objetivo:** o Docker Desktop aberto e o container do banco, `mente-fria-db`, rodando.
**Por quê:**
- O backend usa esse banco quando você o roda na sua máquina.
- Os testes das Fases 2 a 6 sobem **outro** PostgreSQL, também no Docker.
- Sem o Docker aberto, nada disso funciona.

1. Abra o **Docker Desktop**, pelo Menu Iniciar. Espere aparecer **"Engine running"** no canto inferior esquerdo.
2. No Terminal A:

```powershell
docker ps -a --filter name=mente-fria-db
```

**O que faz:** lista os containers, inclusive os parados (`-a`), filtrando pelo nome.

**Você deve ver** uma linha com `mente-fria-db`. Se a coluna STATUS disser `Exited`, ligue o container:

```powershell
docker start mente-fria-db
docker ps --filter name=mente-fria-db
```

**Você deve ver** `Up ...` e `0.0.0.0:5432->5432/tcp`. Isso quer dizer que o banco atende na porta 5432 da sua máquina.

| Se der errado | O que fazer |
|---|---|
| `error during connect` / `The system cannot find the file specified` | O Docker Desktop não está aberto ou ainda está iniciando. Espere "Engine running" |
| O Docker Desktop trava ao abrir com *"starting services: … rename … .sock … The file cannot be accessed by the system"* (A26) | 1. **Nunca** clique em **"Reset to factory defaults"**: isso apaga o `mente-fria-db`. Clique em **Quit**. 2. No PowerShell: `wsl --shutdown`. 3. No Explorador, renomeie as pastas `%LOCALAPPDATA%\Docker\run` e `%LOCALAPPDATA%\docker-secrets-engine` (por exemplo, para `run.antiga`). Apagar não funciona; renomear, sim. 4. Abra o Docker Desktop de novo |
| A lista vem **vazia** (não existe `mente-fria-db`) | O container da Sprint 1 não existe nesta máquina. Pare e peça ajuda ao Diogo: ele cria o container com você |

> ⚠️ **Regra da equipe:** nunca apague, recrie ou resete o **container** `mente-fria-db`. Nesta sprint, você vai **recriar só o banco `mente_fria` dentro dele** (Parte 4.7), que é outra coisa.

### 1.3 O arquivo `backend\.env`

**Objetivo:** o backend saber a senha do banco e ter a chave que assina os tokens.

**Por quê:** o `application.properties` lê os valores com `${DB_PASSWORD}`, `${JWT_SECRET}` etc. Esses valores vêm do `backend\.env`. O `.env` tem senha, por isso **nunca** vai para o Git: o `.gitignore` já o esconde.

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
Test-Path backend\.env
```

**O que faz:** responde `True` se o arquivo existe, `False` se não.

**Se respondeu `False`,** crie a partir do exemplo completo:

```powershell
Copy-Item backend\env.example backend\.env
```

> O `backend\env.example` (sem ponto) tem todas as variáveis, com comentários. O `.env.example` (com ponto) é mais curto. O Kauê vai juntar os dois num PR (pendência G8). **Não edite nenhum dos dois.** Depois desse PR, só existe o `backend\.env.example`, já completo: se o `env.example` não existir mais, copie o `.env.example`.

**Gere a chave do token** (o `JWT_SECRET`): ela precisa ter **32 ou mais caracteres aleatórios**.

```powershell
$bytes = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

**O que cada linha faz:**
1. Cria um espaço para 48 bytes.
2. Enche esse espaço com bytes aleatórios seguros, do próprio Windows.
3. Transforma os bytes em texto (Base64).

**Você deve ver** uma linha de **64 caracteres**, por exemplo `KG4XIDdpV3…`. Copie essa linha.

**Edite o `.env`:**

```powershell
notepad backend\.env
```

No Bloco de Notas, deixe estas linhas assim (as outras podem ficar como estão):

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mente_fria
DB_USER=postgres
DB_PASSWORD=<SENHA_DO_BANCO>
JWT_SECRET=<cole aqui a linha de 64 caracteres>
```

Salve com **Ctrl+S** e feche.

> ⚠️ **O `.env` não pode ter BOM** (uma marca invisível no começo do arquivo). O Bloco de Notas do Windows 11 salva em "UTF-8" **sem** BOM: confira na barra de baixo que está escrito **UTF-8**, e não "UTF-8 com BOM". Com BOM, o Spring lê errado a **primeira linha** do arquivo. Se ela for uma variável sem valor padrão (`DB_USER`, `DB_PASSWORD` ou `JWT_SECRET`), o backend não sobe e mostra `Could not resolve placeholder`.

**Conferir que o `.env` está fora do Git:**

```powershell
git status --short
```

**Você deve ver:** **nenhuma** linha com `.env`. Se aparecer `?? backend/.env`, pare e avise: o `.gitignore` foi alterado.

**Antes de avançar:**
- [ ] `java -version` mostra 25;
- [ ] `docker ps` mostra `mente-fria-db` com `Up`;
- [ ] `backend\.env` tem `DB_PASSWORD` e um `JWT_SECRET` de 64 caracteres;
- [ ] `git status` não mostra o `.env`.

---

## Parte 2 — Git e PR: o caminho de toda fase

Você vai repetir este caminho **cinco vezes**, uma por fase. Leia uma vez com calma. Depois, as partes de cada fase só dizem "siga a Parte 2".

### 2.1 Começar uma fase: branch nova a partir da `main` atualizada

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
git status
git checkout main
git pull
git checkout -b feature/backend-fase1-seguranca
```

**O que cada parte faz:**

| Comando | O que faz |
|---|---|
| `git status` | Mostra se há arquivos alterados sem commit. Antes de trocar de branch, o certo é **nada** |
| `git checkout main` | Vai para a branch `main` (o código oficial) |
| `git pull` | Traz do GitHub o que os colegas já juntaram na `main` |
| `git checkout -b <nome>` | Cria uma branch nova **a partir de onde você está** (a `main` atualizada) e já entra nela |

**Você deve ver** (validado):

```
On branch main
Your branch is up to date with 'origin/main'.

nothing to commit, working tree clean
...
Switched to a new branch 'feature/backend-fase1-seguranca'
```

| Se der errado | O que fazer |
|---|---|
| `git status` lista arquivos `modified` ou `??` | Você tem trabalho sem commit. Se for de outra fase, faça o commit nela (Parte 2.3) antes de trocar. Se não sabe o que é, pare e pergunte no grupo |
| `error: Your local changes … would be overwritten by checkout` | Mesmo caso acima |
| `fatal: a branch named '…' already exists` | A branch já existe: use `git checkout <nome>`, sem o `-b` |

**Confira em qual branch você está, a qualquer momento:**

```powershell
git branch --show-current
```

### 2.2 Commit: guardar o trabalho da fase

```powershell
git status --short
git add backend
git status --short
git commit -m "feat: seguranca JWT, CORS e tratamento padrao de erros"
```

| Comando | O que faz |
|---|---|
| `git status --short` | Lista o que mudou: `M` = modificado, `D` = apagado, `??` = arquivo novo que o Git ainda não conhece |
| `git add backend` | Marca **só a pasta `backend`** para entrar no commit. Assim nada de outras pastas entra por engano |
| `git commit -m "…"` | Cria o commit com a mensagem. Padrão da equipe: *Conventional Commits* (`feat:`, `fix:`, `test:`, `docs:`), **sem acentos** na mensagem |

**Nunca adicione:** o `backend\.env` (ele já é ignorado), a pasta `backend\target` (criada pelo Maven, também ignorada) nem arquivos de outra pessoa.

### 2.3 Enviar para o GitHub

```powershell
git push -u origin feature/backend-fase1-seguranca
```

**O que faz:** envia a sua branch para o GitHub (`origin`). O `-u` "liga" a branch local à remota. Depois disso, `git push` e `git pull` sozinhos já sabem para onde ir.

**Você deve ver** no fim:

```
 * [new branch]      feature/backend-fase1-seguranca -> feature/backend-fase1-seguranca
branch 'feature/backend-fase1-seguranca' set up to track 'origin/feature/backend-fase1-seguranca'.
```

| Se der errado | O que fazer |
|---|---|
| `Permission denied` / `403` | A sua conta do GitHub não tem permissão de escrita no repositório. Peça ao Kauê, dono do repositório, para conferir em *Settings* → *Collaborators* |
| O Git abre uma janela pedindo login | É o gerenciador de credenciais do Windows: entre com a sua conta do GitHub |

> **Mensagens em vermelho do Git:** no PowerShell, o Git escreve o progresso numa saída que às vezes aparece em vermelho. Se a última linha for a esperada, deu certo.

### 2.4 Abrir o PR (no navegador)

**Antes de abrir o PR,** rode todos os testes que já existem na branch, e não só os da fase: dentro de `backend`, `.\mvnw test`. Os testes com o Python real se pulam sozinhos se o `MF_PYTHON` não estiver definido. Assim você confere que a fase nova não quebrou as anteriores.

1. Abra `https://github.com/KaueNCarneiro/Mente_Fria`.
2. Deve aparecer uma faixa amarela com o nome da sua branch e o botão **"Compare & pull request"**. Clique nele.
   - Se a faixa não aparecer: aba **Pull requests** → **New pull request** → em **compare**, escolha a sua branch.
3. Confira no topo: **base: `main`** ← **compare: `feature/...`**.
4. **Título:** igual à mensagem do commit.
5. **Descrição:** use o modelo abaixo.
6. À direita, em **Reviewers**, escolha um colega (regra da equipe: revisão obrigatória).
7. Clique em **Create pull request**.

**Modelo de descrição do PR:**

```
## O que entra
Fase 1 do backend: segurança (token JWT), CORS e erros em JSON.

## Como testar
cd backend
.\mvnw test "-Dtest=SegurancaEErrosTest"

## Resultado na minha máquina
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

## Avisos
- A partir desta fase, quase toda rota exige login (token).
- Evidência em docs/Sprint_4/evidencias/ (se houver).
```

### 2.5 Depois da aprovação: merge e limpeza

No GitHub, no PR aprovado:
1. **Merge pull request** → **Confirm merge**.
2. **Delete branch**.

Depois, no Terminal A:

```powershell
git checkout main
git pull
git branch -d feature/backend-fase1-seguranca
```

**O que faz:** volta para a `main`, traz o merge e apaga a branch local, que já foi juntada.

**Avise no grupo** (modelo):

```
Fase 1 do backend na main (PR #__). 18 testes passando.
A partir de agora quase toda rota pede token. Leonardo: o login entra na Fase 2 (sex).
```

### 2.6 Trazer a `main` nova para dentro da sua branch

Às vezes um colega junta algo na `main` de que a **sua** branch precisa. Exemplo: a Fase 5 do Kauê, na Fase 6. Sem sair da sua branch:

```powershell
git fetch origin
git merge --no-edit origin/main
```

| Comando | O que faz |
|---|---|
| `git fetch origin` | Baixa as novidades do GitHub **sem mexer** nos seus arquivos |
| `git merge --no-edit origin/main` | Junta a `main` do GitHub na branch em que você está. Quando os dois lados têm commits novos, o Git cria um "commit de junção"; o `--no-edit` aceita a mensagem padrão dele, sem abrir um editor de texto (no Git do Windows, o editor padrão é o Vim, difícil de sair para quem não conhece) |

**Você deve ver** a lista dos arquivos que chegaram, por exemplo `create mode 100644 backend/.../ReposicaoService.java`.

**Se aparecer `CONFLICT`:** os dois lados mexeram na mesma linha. Não force nada: pare e chame o colega que fez a outra mudança.

### 2.7 Fluxo a dois: o Diogo entra na sua branch

**Por quê:** os repositórios (as classes de SQL) são do Diogo, mas só compilam junto com o seu código da mesma fase. Por isso os dois trabalham **na mesma branch**, e cada um faz commit dos **seus** arquivos (decisão A44).

| Passo | Quem | Comandos |
|---|---|---|
| 1. Criar a branch, copiar a sua parte, commit e push | **Você** | Parte 2.1, cópia da fase, `git add backend`, `git commit`, `git push -u origin <branch>` |
| 2. Avisar | **Você** | "Diogo, a branch `<branch>` está no GitHub. Pode pôr os repositórios" |
| 3. Pegar a sua branch, copiar os repositórios, commit e push | **Diogo** | `git fetch`, `git checkout <branch>`, cópia, `git add backend`, `git commit`, `git push` |
| 4. Trazer o commit do Diogo | **Você** | `git pull` (dentro da sua branch) |
| 5. Rodar os testes e abrir o PR | **Você** | O PR leva os commits dos dois |

**Como não dar conflito:** cada um copia **só os seus arquivos** (as tabelas de cada fase dizem quais são). E ninguém mexe na branch enquanto o outro está com commit pendente.

**Você deve ver** no passo 4 (validado):

```
 create mode 100644 backend/src/main/java/com/mentefria/backend/produto/ProdutoRepository.java
 create mode 100644 backend/src/main/java/com/mentefria/backend/unidade/UnidadeMedidaRepository.java
 create mode 100644 backend/src/main/java/com/mentefria/backend/usuario/UsuarioRepository.java
```

### 2.8 Guardar a evidência dos testes de uma fase

A entrega exige, para cada fase, a saída dos testes guardada em `docs/Sprint_4/evidencias/` (roteiro da Sprint 4, §7). O padrão, de dentro de `backend`:

```powershell
$saida = .\mvnw test "-Dtest=<CLASSES_DA_FASE>"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\<AAAA-MM-DD>_lucas_<fase>_mvnw-test.txt
$saida | Select-String "Tests run:|BUILD"
```

| Linha | O que faz |
|---|---|
| `$saida = .\mvnw test …` | Roda os testes e guarda a saída na variável `$saida`, em vez de mostrar tudo na tela |
| `$saida \| Out-File -Encoding utf8 …` | Grava a saída num arquivo de texto em UTF-8 (o Git consegue mostrar as diferenças dele). Troque `<AAAA-MM-DD>` pela data do dia |
| `$saida \| Select-String "Tests run:\|BUILD"` | Mostra na tela só as linhas do resumo, para você conferir na hora |

**Você deve ver** (validado) só o resumo, por exemplo:

```
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 9.004 s -- in
com.mentefria.backend.config.SegurancaEErrosTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Antes do resumo podem aparecer avisos `WARNING: A Java agent has been loaded dynamically…`: são normais. Cada parte de fase traz o comando já pronto, com as classes certas e a **data planejada** no nome do arquivo. Se fizer a fase em outro dia, troque a data no nome.

> Não use `Tee-Object` para isso: no Windows PowerShell 5.1 ele grava o arquivo em UTF-16, e o Git o trata como binário.

---

## Parte 3 — Fase 1: segurança, token, CORS e erros em JSON

**Objetivo:**
- toda rota exige login, menos as públicas (saúde, login e cadastro do Administrador);
- todo erro sai em JSON `{ "mensagem": ..., "campos": ... }`, nunca numa página HTML.

**Quando:** qui 29/10 (atrasada: faça primeiro).
**Branch:** `feature/backend-fase1-seguranca`.

### 3.1 Criar a branch

Siga a Parte 2.1 com o nome `feature/backend-fase1-seguranca`.

### 3.2 Bibliotecas novas no `pom.xml`

**Por quê:** a Fase 1 precisa de três grupos de bibliotecas:
- **Spring Security**: para as regras de quem pode o quê;
- **jjwt**: para criar e conferir o token;
- **Testcontainers**: para os testes com banco de verdade a partir da Fase 2.

Em vez de editar o `pom.xml` à mão, aplicamos um *diff*: um arquivo que diz exatamente quais linhas entram e quais saem.

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>` (raiz, não `backend`).

```powershell
git apply --check docs\Sprint_3\referencia\backend\pom.xml.diff
git apply docs\Sprint_3\referencia\backend\pom.xml.diff
git diff --stat
```

| Comando | O que faz |
|---|---|
| `git apply --check <diff>` | **Ensaio:** confere se o diff encaixa no seu `pom.xml` sem mudar nada. Silêncio = encaixa |
| `git apply <diff>` | Aplica de verdade |
| `git diff --stat` | Resume o que mudou em relação ao último commit |

**Você deve ver** (validado): o `--check` e o `apply` **sem nenhuma saída**, e depois:

```
 backend/pom.xml | 50 +++++++++++++++++++++++++++++++++++++++++++++++++-
 1 file changed, 49 insertions(+), 1 deletion(-)
```

**O que mudou no `pom.xml`:**
1. **Saiu** `<scope>runtime</scope>` do driver do PostgreSQL. O tratamento de erros usa uma classe do driver (`PSQLException`) e, com `runtime`, o código **não compila** (decisão A4).
2. **Entraram:**
   - `spring-boot-starter-security`;
   - `jjwt-api`, `jjwt-impl` e `jjwt-jackson` 0.13.0;
   - `spring-boot-starter-security-test`;
   - três artefatos do Testcontainers, **sem versão**, porque o Spring Boot 4.0.8 já escolhe a certa.
3. **Entrou** um `<testResources>`: ele faz os testes usarem **o mesmo** `bd/Script_DDL.sql` da aplicação. Quando o Diogo muda o banco, os testes mudam junto.

**Conferir as bibliotecas** (opcional, mas ensina muito):

```powershell
cd backend
.\mvnw dependency:tree "-Dincludes=com.fasterxml.jackson.core,tools.jackson.core,io.jsonwebtoken"
```

**O que faz:** mostra a "árvore" de bibliotecas, filtrada pelas do Jackson (que transforma Java em JSON) e as do JWT. As aspas em volta de `-D…` são obrigatórias no PowerShell.

**Você deve ver** (validado), entre outras linhas:

```
[INFO] |        \- tools.jackson.core:jackson-databind:jar:3.1.5:compile
[INFO] +- io.jsonwebtoken:jjwt-api:jar:0.13.0:compile
[INFO] +- io.jsonwebtoken:jjwt-impl:jar:0.13.0:runtime
[INFO] \- io.jsonwebtoken:jjwt-jackson:jar:0.13.0:runtime
[INFO]    \- com.fasterxml.jackson.core:jackson-databind:jar:2.21.5:runtime
[INFO] BUILD SUCCESS
```

**Duas versões do Jackson não é conflito:**
- `tools.jackson` (3.x) é a do Spring Boot 4;
- `com.fasterxml.jackson` (2.x) vem só para o jjwt.

São pacotes diferentes e convivem.

Volte para a raiz:

```powershell
cd ..
```

### 3.3 O `application.properties` novo

**O que é:** o arquivo de configuração do Spring. Ele diz onde está o banco, qual porta usar etc. Os valores com `${…}` vêm do `.env`.

```powershell
Copy-Item docs\Sprint_3\referencia\backend\src\main\resources\application.properties backend\src\main\resources\application.properties
```

**O que faz:** copia o arquivo da referência **por cima** do atual. O `Copy-Item` substitui sem perguntar, e é isso que queremos aqui.

O que o arquivo novo acrescenta:

| Linha | Para quê |
|---|---|
| `server.port=${PORT:8080}` | No Render, a porta vem da variável `PORT`. Na sua máquina, 8080 |
| `spring.jackson.property-naming-strategy=SNAKE_CASE` | Faz `precoVenda` (Java) virar `preco_venda` (JSON) em todo lugar. Uma linha no lugar de `@JsonProperty` em cada campo (A23) |
| `app.jwt.secret=${JWT_SECRET}` e `app.jwt.validade-horas=2` | A chave do token e quanto tempo ele vale |
| `app.cors.allowed-origins=…` | Quais páginas podem chamar a API (o Live Server do Leonardo; no Render, o GitHub Pages) |
| `app.python.cmd`, `app.otimizador.script`, `app.reposicao.script`, `app.otimizador.timeout-segundos` | Como chamar o Python (Fase 4A) |
| `app.otimizador.habilitado=${OTIMIZADOR_HABILITADO:true}` | A chave de reserva do plano B1 (Parte 6) |

> Em arquivo `.properties`, a barra invertida `\` é um caractere especial. Caminhos do Windows usam `/` (por exemplo, `../optimization/app/otimizador.py`).

### 3.4 Copiar as seis classes da Fase 1

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`. Copie o bloco inteiro.

```powershell
$ref = "docs\Sprint_3\referencia\backend\src\main\java\com\mentefria\backend"
$dst = "backend\src\main\java\com\mentefria\backend"
New-Item -ItemType Directory -Force "$dst\config", "$dst\auth" | Out-Null
Copy-Item "$ref\config\ApiException.java", "$ref\config\GlobalExceptionHandler.java", "$ref\config\RelogioConfig.java", "$ref\config\SecurityConfig.java" "$dst\config\"
Copy-Item "$ref\auth\JwtService.java", "$ref\auth\JwtAuthFilter.java" "$dst\auth\"
```

**O que cada parte faz:**
- `$ref = "…"` e `$dst = "…"`: guardam os dois caminhos em variáveis, para não repetir o texto longo.
- `New-Item -ItemType Directory -Force`: cria as pastas `config` e `auth`. O `-Force` não reclama se elas já existirem. O `| Out-Null` só esconde a listagem.
- `Copy-Item origem1, origem2 destino`: copia os arquivos listados para a pasta de destino.

> ⚠️ Da pasta `auth` da referência, copie **só** `JwtService` e `JwtAuthFilter`. O `AuthController` que está lá é da **Fase 2**: ele usa classes que ainda não existem, e o projeto deixaria de compilar.

| Arquivo | Para quê |
|---|---|
| `config/ApiException.java` | O "erro de negócio" do projeto: carrega o status HTTP, a mensagem e os campos com problema |
| `config/GlobalExceptionHandler.java` | Transforma **qualquer** erro em JSON |
| `config/RelogioConfig.java` | O relógio do sistema, sempre no fuso de São Paulo |
| `config/SecurityConfig.java` | As regras de quem pode o quê, o CORS e as respostas `401` e `403` |
| `auth/JwtService.java` | Cria e confere o token |
| `auth/JwtAuthFilter.java` | Lê o token de cada requisição |

### 3.5 Entendendo o código da Fase 1

#### `JwtService`: o "carimbo" do crachá

```java
public JwtService(@Value("${app.jwt.secret}") String segredo, @Value("${app.jwt.validade-horas:2}") long horas) {
    byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres.");
    this.chave = Keys.hmacShaKeyFor(bytes);
    ...
}
public String gerarToken(int usuarioId, String perfil) {
    Date agora = new Date();
    return Jwts.builder().subject(String.valueOf(usuarioId)).claim("perfil", perfil)
            .issuedAt(agora).expiration(new Date(agora.getTime() + validadeMs)).signWith(chave).compact();
}
```

- **`@Value("${app.jwt.secret}")`** pede ao Spring o valor daquela configuração (que veio do `.env`). É injeção de dependência de um valor, e não de um objeto.
- **A conferência dos 32 caracteres** faz a aplicação **não subir** com chave fraca. É melhor falhar no começo que funcionar inseguro.
- **`gerarToken`** monta o crachá com três informações:
  - o **id do usuário** (`subject`);
  - o **perfil** (`ADMINISTRADOR` ou `FUNCIONARIO`);
  - a data de **validade**.

  O `signWith(chave)` é a "assinatura": se alguém mudar uma letra do token, a assinatura não bate mais.

#### `JwtAuthFilter`: quem lê o crachá

```java
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
```

- Um **filtro** roda **antes** do controller, em toda requisição.
- Se veio `Authorization: Bearer <token>` válido, ele "registra" o usuário no `SecurityContextHolder`. Ele anota o id e o perfil como `ROLE_ADMINISTRADOR` ou `ROLE_FUNCIONARIO`.
- Se o token é inválido ou expirou, ele **não** derruba a requisição. Só deixa de registrar o usuário. Quem decide o `401` é o `SecurityConfig`.
- **Por que não tem `@Component`:** ele é criado à mão dentro do `SecurityConfig` (`new JwtAuthFilter(jwt)`). Se fosse um bean, o Spring o registraria **duas vezes**.

#### `SecurityConfig`: as regras

```java
http.cors(c -> c.configurationSource(cors))
    .csrf(csrf -> csrf.disable())                         // sem cookie/sessão: não há CSRF
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .exceptionHandling(e -> e
        .authenticationEntryPoint((req, res, ex) -> { res.setStatus(401); ... "Sua sessão expirou ou você não está autenticado." })
        .accessDeniedHandler((req, res, ex) -> { res.setStatus(403); ... "Você não tem permissão para esta ação." }))
    .authorizeHttpRequests(a -> {
        a.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll();
        a.requestMatchers(HttpMethod.GET, "/api/saude").permitAll();
        a.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll();
        if (dev) a.requestMatchers("/api/teste/**").permitAll();       // F4: só no perfil dev
        a.requestMatchers("/api/usuarios/**", "/api/unidades-medida/**", "/api/producao/**",
                          "/api/reposicao/**", "/api/marketing/**", "/api/dashboard/**").hasRole(ADMIN);
        a.requestMatchers(HttpMethod.POST, "/api/ingredientes/**", "/api/produtos/**").hasRole(ADMIN);
        ...
        a.anyRequest().authenticated();
    })
    .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class);
```

Leia de cima para baixo: **a primeira regra que combina vence.**

| Quem | Rotas |
|---|---|
| Todo mundo, sem token | `OPTIONS` (o *preflight* do CORS), `GET /api/saude`, `POST /api/auth/login`, `POST /api/auth/registro`. `/api/teste/**` **só** no perfil `dev` |
| Só Administrador | Usuários, unidades, produção, reposição, marketing, dashboard; `POST`, `PUT` e `PATCH` de ingredientes e produtos |
| Administrador e Funcionário | Todo o resto: listar ingredientes e produtos, estoque, pedidos |

**Detalhes que caem na Review:**
- **`STATELESS`:** o backend não guarda "sessão". Cada requisição se identifica sozinha pelo token.
- **CSRF desligado:** é uma proteção para sites que usam cookie de sessão. Como usamos token no cabeçalho, não se aplica.
- **`401` e `403` em JSON:** sem essas duas funções, o Spring responderia no formato padrão dele, sem a nossa mensagem em português. O frontend não teria o que mostrar.
- **CORS dentro da segurança:** o `.cors(...)` fica **na corrente de segurança**, que roda antes de tudo. Assim, o *preflight* é respondido logo, e uma origem desconhecida é barrada com `403`. Além disso, as respostas `401` e `403` também levam o cabeçalho `Access-Control-Allow-Origin`: sem ele, o navegador esconderia do frontend a mensagem de erro em JSON.
- **O bean de CORS é do tipo `UrlBasedCorsConfigurationSource`** (decisão A5). Com o tipo genérico, o Spring encontra dois candidatos e a aplicação **não sobe**.

#### `GlobalExceptionHandler`: todo erro vira JSON

`@RestControllerAdvice` é uma classe que "escuta" os erros de **todos** os controllers. Cada método com `@ExceptionHandler(TipoDoErro.class)` trata um tipo:

| Erro | HTTP | Mensagem | Quando acontece |
|---|---|---|---|
| `ApiException` | o que vier nela | a dela + `campos` | Regras de negócio ("O cadastro do Administrador já foi realizado.") |
| `DataIntegrityViolationException` | `400`, `409` ou `422` | "Dados inválidos" + o campo (no `422`, a própria mensagem do mapa) | O **banco** recusou (uma *constraint*). O nome da *constraint* é procurado num **mapa** (`MAPA`) que diz o status, o campo e a mensagem. Uma *constraint* que não está no mapa vira `500` e um aviso no log |
| `HttpMessageNotReadableException` | `400` | "Requisição inválida." | JSON quebrado |
| `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException` | `400` | "Requisição inválida." | `/api/produtos/abc/preco` (texto onde devia ser número) ou parâmetro faltando (A7) |
| `AccessDeniedException` | `403` | "Você não tem permissão para esta ação." | Reserva, se um dia usarem `@PreAuthorize` nos métodos (A7) |
| `NoResourceFoundException` | `404` | "Recurso não encontrado." | Rota que não existe (com token) |
| `HttpRequestMethodNotSupportedException` | `405` | "Método não permitido." | `DELETE` numa rota que só aceita `GET` |
| `Exception` (qualquer outro) | `500` | "Não foi possível concluir a operação." | Um defeito. O detalhe técnico vai **só para o log**, nunca para a resposta |

Um trecho do **mapa** (o resto segue o mesmo padrão):

```java
m("uq_usuario_email", 409, "email", "Este e-mail já está cadastrado."),
m("chk_ingrediente_custo_unitario", 400, "custo_unitario", "O custo não pode ser negativo."),
m("NN:ingrediente.nome", 400, "nome", "Informe o nome do ingrediente."),
```

O primeiro texto é o **nome da *constraint* do banco** (definida pelo Diogo no `Script_DDL.sql`). `NN:` quer dizer "NOT NULL" daquela coluna. Assim, quando o banco recusa algo, a mensagem que chega à tela é em português e no campo certo.

#### `ApiException` e `RelogioConfig`

```java
public static ApiException campo(int status, String campo, String texto) {
    return new ApiException(status, "Dados inválidos", Map.of(campo, texto));
}
```

```java
@Bean Clock clock() { return Clock.system(ZoneId.of("America/Sao_Paulo")); }
```

- `ApiException.campo(...)` é um atalho para o erro mais comum: um campo inválido.
- **O relógio em São Paulo** (decisão F3/A9): o Render roda em UTC (3 horas à frente). Sem isso, um pedido às 22h de 31/10 cairia em **novembro** no dashboard. Os serviços pedem a data com `LocalDate.now(clock)` e a mandam como parâmetro para o SQL.

### 3.6 Os testes da Fase 1

**O que são:**
- **`SegurancaEErrosTest`:** 18 testes que "fingem" requisições (MockMvc), **sem banco**.
- **`RotasDeTesteController`:** um controller **que só existe nos testes**. Ele simula rotas de cadastro e preço para testar a segurança antes das rotas reais existirem.

**Por que o `RotasDeTesteController` tem `@Profile("teste-seguranca")`** (A24):
- Quando as rotas reais chegarem (Fase 2), um teste de integração carregaria as **duas** versões da mesma rota.
- Com isso, o Spring recusaria subir ("mapeamento ambíguo").
- O perfil faz o controller simulado existir **só** no `SegurancaEErrosTest`.

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
$refT = "docs\Sprint_3\referencia\backend\src\test\java\com\mentefria\backend"
$dstT = "backend\src\test\java\com\mentefria\backend"
New-Item -ItemType Directory -Force "$dstT\config" | Out-Null
Copy-Item "$refT\config\SegurancaEErrosTest.java", "$refT\config\RotasDeTesteController.java" "$dstT\config\"
git rm backend\src\test\java\com\mentefria\backend\BackendApplicationTests.java
```

**Por que apagar o `BackendApplicationTests`:**
- Ele só confere se a aplicação "sobe" (`contextLoads`), e para isso precisa do banco ligado.
- A partir da Fase 2, a `IntegracaoBase` faz isso melhor, com banco próprio no Docker.
- O `git rm` apaga o arquivo **e** já avisa o Git da remoção.

**Você deve ver:** `rm 'backend/src/test/java/com/mentefria/backend/BackendApplicationTests.java'`.

**Confira o que mudou até aqui:**

```powershell
git status --short
```

**Você deve ver** (validado):

```
 M backend/pom.xml
 M backend/src/main/resources/application.properties
D  backend/src/test/java/com/mentefria/backend/BackendApplicationTests.java
?? backend/src/main/java/com/mentefria/backend/auth/
?? backend/src/main/java/com/mentefria/backend/config/
?? backend/src/test/
```

**Rode os testes:**

```powershell
cd backend
.\mvnw test "-Dtest=SegurancaEErrosTest"
```

| Parte | O que faz |
|---|---|
| `cd backend` | O Maven roda **de dentro** da pasta que tem o `pom.xml` |
| `.\mvnw` | O Maven que vem com o projeto. O `.\` quer dizer "o arquivo desta pasta" |
| `test` | Compila e roda os testes |
| `"-Dtest=SegurancaEErrosTest"` | Roda só esta classe. As aspas são obrigatórias no PowerShell |

**Você deve ver** (validado, cerca de 10 segundos), entre muitas linhas:

```
[INFO] Running com.mentefria.backend.config.SegurancaEErrosTest
...
... ERROR ... c.m.b.config.GlobalExceptionHandler      : Erro inesperado
...
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> **A linha `ERROR … Erro inesperado` é esperada.** Um dos 18 testes provoca um erro de propósito, para conferir que a resposta é `500` **sem** detalhes técnicos. O log mostra esse erro. O que importa é **`Failures: 0, Errors: 0`** e **`BUILD SUCCESS`**.
> O aviso `Mockito is currently self-attaching…` também é normal.

O que os 18 testes conferem:
- `GET /api/saude` é público;
- sem token, token inválido, token expirado e rota inexistente sem token → `401` em JSON;
- rota inexistente com token → `404` em JSON;
- `/api/teste/python` fora do perfil `dev` → `401`;
- Funcionário em `/api/usuarios` e em `PUT …/preco` → `403` em JSON (CT05, CT08);
- JSON malformado, id que não é número, parâmetro faltando → `400`;
- erro inesperado → `500` sem detalhe;
- *preflight* com `PATCH` → `200`; origem desconhecida → `403`.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `package org.postgresql.util does not exist` | O `pom.xml` ainda tem `scope runtime` no driver | Refaça a Parte 3.2 |
| `cannot find symbol … UsuarioService` (ou outra classe da Fase 2) | Você copiou o `AuthController` | Apague `backend\src\main\java\com\mentefria\backend\auth\AuthController.java` |
| `NoUniqueBeanDefinitionException … CorsConfigurationSource` | `SecurityConfig` diferente do da referência | Copie de novo (Parte 3.4) |
| `expected:<400> but was:<500>` | `GlobalExceptionHandler` diferente do da referência | Copie de novo |
| `'.\mvnw' não é reconhecido` | Você não está dentro de `backend` | `cd backend` |
| `JAVA_HOME is not defined correctly` | O Maven não achou o JDK | Feche e abra o PowerShell; se continuar, peça ajuda no grupo |

### 3.7 Conferência manual com o backend rodando

**Objetivo:** ver com os próprios olhos o `401` em JSON e o CORS.

**Terminal B** (abra um segundo PowerShell):

```powershell
cd "<PASTA_DO_PROJETO>\backend"
.\mvnw spring-boot:run
```

**O que faz:** compila e **sobe** o backend na porta 8080, usando o `.env` e o banco `mente-fria-db`. O terminal fica "preso" mostrando o log. É assim mesmo.

**Você deve ver** (validado), no fim:

```
Using generated security password: 620efc75-...
Tomcat started on port 8080 (http) with context path '/'
Started BackendApplication in 1.543 seconds (process running for 1.782)
```

> `Using generated security password` é um aviso padrão do Spring Security sobre um usuário de teste que **não usamos** (o login é pelo token). Pode ignorar.

**Terminal A:** cole a função abaixo **uma vez** em cada PowerShell novo. Ela fica valendo até você fechar aquela janela.

```powershell
function Chamar($metodo, $url, $corpo = $null, $token = $null) {
  $h = @{}
  if ($token) { $h["Authorization"] = "Bearer $token" }
  $p = @{ Method = $metodo; Uri = $url; Headers = $h; UseBasicParsing = $true }
  if ($corpo) { $p.ContentType = "application/json; charset=utf-8"; $p.Body = [Text.Encoding]::UTF8.GetBytes(($corpo | ConvertTo-Json -Depth 6)) }
  try {
    $r = Invoke-WebRequest @p
    $status = [int]$r.StatusCode
    $texto = [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
  } catch {
    $resp = $_.Exception.Response
    if (-not $resp) { throw }
    $status = [int]$resp.StatusCode
    $ms = New-Object IO.MemoryStream
    $resp.GetResponseStream().CopyTo($ms)
    $texto = [Text.Encoding]::UTF8.GetString($ms.ToArray())
    if (-not $texto -and $_.ErrorDetails) { $texto = $_.ErrorDetails.Message }
  }
  $json = $null
  if ($texto) { $json = $texto | ConvertFrom-Json }
  [pscustomobject]@{ status = $status; corpo = $json }
}
$api = "http://localhost:8080/api"
```

**O que a função faz:**
1. Monta a requisição: o método (`GET`, `POST`…), o endereço, o token (se houver) e o corpo, convertido para JSON em UTF-8.
2. Envia com `Invoke-WebRequest`.
3. Se a resposta for de erro (`4xx`/`5xx`), o PowerShell lança uma exceção. O `catch` pega a resposta mesmo assim, para você ver o status e a mensagem.
4. Lê o corpo **como UTF-8**. Sem isso, o PowerShell 5.1 mostra `AÃ§aÃ­` no lugar de `Açaí`.
5. Devolve um objeto com `status` e `corpo`.

`$api` guarda o começo do endereço, para os comandos ficarem curtos.

**Agora as chamadas:**

```powershell
Chamar GET "$api/saude" | ConvertTo-Json -Depth 6 -Compress
Chamar GET "$api/ingredientes" | ConvertTo-Json -Depth 6 -Compress
```

`| ConvertTo-Json -Depth 6 -Compress` mostra a resposta inteira numa linha. A **ordem** dos campos de `/api/saude` pode variar (`"banco"` antes de `"status"`): o que importa são os valores.

**Você deve ver** (validado):

```
{"status":200,"corpo":{"status":"ok","banco":"ok"}}
{"status":401,"corpo":{"mensagem":"Sua sessão expirou ou você não está autenticado."}}
```

**O CORS:** a "pergunta" que o navegador faz antes de mudar um preço.

```powershell
$h = @{ "Origin"="http://127.0.0.1:5500"; "Access-Control-Request-Method"="PATCH"; "Access-Control-Request-Headers"="authorization,content-type" }
$r = Invoke-WebRequest -Method Options -Uri "$api/produtos/1/preco" -Headers $h -UseBasicParsing
"status: " + $r.StatusCode
"allow-origin: " + $r.Headers["Access-Control-Allow-Origin"]
"allow-methods: " + $r.Headers["Access-Control-Allow-Methods"]
```

**Você deve ver** (validado):

```
status: 200
allow-origin: http://127.0.0.1:5500
allow-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
```

**Pare o backend:** no **Terminal B**, tecle **Ctrl+C**. O Windows pergunta `Deseja finalizar o arquivo em lotes (S/N)?`: responda **S** e Enter.

**Antes de avançar:**
- [ ] 18 testes, 0 falhas;
- [ ] `/api/saude` → `200`; `/api/ingredientes` sem token → `401` com a mensagem em JSON;
- [ ] o *preflight* → `200` com `allow-origin`.

### 3.8 Commit, PR e merge (Parte 2)

**Terminal A**, na `<PASTA_DO_PROJETO>` (se estiver em `backend`, volte com `cd ..`). Primeiro a evidência (Parte 2.8):

```powershell
cd backend
$saida = .\mvnw test "-Dtest=SegurancaEErrosTest"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-29_lucas_fase1_mvnw-test.txt
$saida | Select-String "Tests run:|BUILD"
cd ..
```

Depois, o commit e o push:

```powershell
git add backend docs\Sprint_4\evidencias
git status --short
git commit -m "feat: seguranca JWT, CORS e tratamento padrao de erros"
git push -u origin feature/backend-fase1-seguranca
```

**Você deve ver** no `git status --short` (validado): as linhas com `A ` (adicionado), `M ` e `D `. **Nenhuma** linha com `.env`.

Abra o PR (Parte 2.4), peça revisão e, aprovado, faça o merge **ainda hoje** (Parte 2.5). Avise o grupo.

### 3.9 Perguntas para a Review (a sua defesa)

1. Qual a diferença entre `401` e `403`? Dê um exemplo real do projeto de cada um.
2. Por que o token tem validade de 2 horas, e o que acontece quando ela acaba?
3. Por que o CORS fica dentro da corrente de segurança? (Pense no *preflight* e na mensagem de um `401` chegando ao frontend.)
4. Onde está o tratamento de "e-mail já cadastrado", se nenhum código Java procura e-mail repetido? (Dica: `MAPA` + *constraint* do banco.)
5. Por que o backend usa o relógio de São Paulo, e não o do servidor?

---

## Parte 4 — Fase 2: cadastros e login (US1–US7), com o Diogo

**Objetivo:** as rotas reais de cadastro do Administrador, login, Funcionários, unidades, ingredientes, produtos e preço.
**Quando:** sex 30/10 (marco M2 à noite).
**Branch:** `feature/backend-fase2-cadastros`.

### 4.1 Pré-requisitos: confira antes de começar

1. **A Fase 1 está na `main`** (Parte 3.8).
2. **O PR do banco do Diogo está na `main`.** Ele traz o `bd/Script_DDL.sql` novo, em que o estoque começa em 0. Confira:

```powershell
git checkout main
git pull
Select-String -Path bd\Script_DDL.sql -Pattern "quantidade_restante" | Select-Object -First 1
```

`Select-String` procura um texto dentro de um arquivo, como o "Localizar" de um editor.

**Você deve ver** pelo menos uma linha com `quantidade_restante`. **Se não aparecer nada,** o PR do banco ainda não entrou: avise o Diogo e espere. Sem ele, um dos testes falha (`listaIngredientesCompletaComEstoqueZero`).

### 4.2 Criar a branch e publicá-la cedo

```powershell
git checkout -b feature/backend-fase2-cadastros
```

Você vai fazer o push **logo depois de copiar a sua parte** (Parte 4.4), para o Diogo poder entrar na branch.

### 4.3 Copiar a sua parte

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`. Copie o bloco inteiro.

```powershell
$r = "docs\Sprint_3\referencia\backend\src\main\java\com\mentefria\backend"
$d = "backend\src\main\java\com\mentefria\backend"
New-Item -ItemType Directory -Force "$d\usuario", "$d\unidade", "$d\produto", "$d\python", "$d\producao" | Out-Null
Copy-Item "$r\usuario\Usuario.java", "$r\usuario\UsuarioDtos.java", "$r\usuario\UsuarioService.java", "$r\usuario\UsuarioController.java" "$d\usuario\"
Copy-Item "$r\unidade\UnidadeMedida.java", "$r\unidade\UnidadeMedidaController.java" "$d\unidade\"
Copy-Item "$r\ingrediente\Ingrediente.java", "$r\ingrediente\IngredienteService.java", "$r\ingrediente\IngredienteController.java" "$d\ingrediente\"
Remove-Item "$d\ingrediente\IngredienteResumo.java" -ErrorAction SilentlyContinue
Copy-Item "$r\produto\Produto.java", "$r\produto\ProdutoService.java", "$r\produto\ProdutoController.java", "$r\produto\RegraAcai.java" "$d\produto\"
Copy-Item "$r\auth\AuthController.java" "$d\auth\"
Copy-Item "$r\python\ModulosPython.java", "$r\python\ProcessoPython.java" "$d\python\"
Copy-Item "$r\producao\Recomendacao.java", "$r\producao\UltimaRecomendacao.java" "$d\producao\"
$rt = "docs\Sprint_3\referencia\backend\src\test\java\com\mentefria\backend"
$dt = "backend\src\test\java\com\mentefria\backend"
New-Item -ItemType Directory -Force "$dt\produto" | Out-Null
Copy-Item "$rt\IntegracaoBase.java", "$rt\Fase2CrudTest.java" "$dt\"
Copy-Item "$rt\produto\RegraAcaiTest.java" "$dt\produto\"
```

**O que você copiou e para quê:**

| Pacote | Arquivo | Tipo | Para quê |
|---|---|---|---|
| `usuario` | `Usuario` | record | Uma linha da tabela `usuario`. **Nunca** sai na resposta (tem o hash da senha) |
| | `UsuarioDtos` | records | Os formatos de entrada e de saída do JSON (`NovoUsuario`, `UsuarioCriado`, `UsuarioResumo`) |
| | `UsuarioService` | service | Regras: um Administrador só, e-mail normalizado, todos os erros de uma vez |
| | `UsuarioController` | controller | `POST` e `GET /api/usuarios` (Funcionários) |
| `unidade` | `UnidadeMedida`, `UnidadeMedidaController` | record, controller | `GET /api/unidades-medida` (só leitura nesta sprint) |
| `ingrediente` | `Ingrediente`, `IngredienteService`, `IngredienteController` | record, service, controller | **Substituem** os da `main`. O `IngredienteResumo` da Sprint 2 sai: o `Ingrediente` novo tem todos os campos |
| `produto` | `Produto`, `ProdutoService`, `ProdutoController`, `RegraAcai` | records, service, controller, regra | Produto com composição; a regra "precisa ter açaí"; o preço |
| `auth` | `AuthController` | controller | `POST /api/auth/registro` e `POST /api/auth/login` |
| `python`, `producao` | `ModulosPython`, `ProcessoPython`, `Recomendacao`, `UltimaRecomendacao` | infraestrutura | **Ainda sem rotas.** Entram já porque a `IntegracaoBase` dos testes troca os módulos Python por dublês e limpa o último resultado. Sem eles, os testes não compilam |
| testes | `IntegracaoBase`, `Fase2CrudTest`, `produto/RegraAcaiTest` | testes | A base de todos os testes com banco; os 15 testes da Fase 2; os 13 da regra do açaí |

O `-ErrorAction SilentlyContinue` evita um erro em vermelho se você rodar o bloco de novo (o arquivo já terá sido apagado).

**Não copie os `*Repository.java`:** eles são do Diogo e entram pelo commit dele (Parte 4.5). Até lá, o projeto **não compila**. É esperado.

### 4.4 Commit e push da sua parte

```powershell
git add backend
git commit -m "feat: cadastros, login e infraestrutura da Fase 2 (parte do Lucas)"
git push -u origin feature/backend-fase2-cadastros
```

Avise o Diogo:

```
Diogo, a branch feature/backend-fase2-cadastros está no GitHub com a minha parte.
Pode pôr os quatro repositórios da Fase 2 (usuario, unidade, ingrediente, produto). Me avisa quando der push.
```

### 4.5 O Diogo põe os repositórios; você traz o commit dele

O Diogo segue a parte dele (Parte 2.7 deste tutorial, no papel dele, ou o tutorial dele). Ele copia **só**:
- `usuario/UsuarioRepository.java`;
- `unidade/UnidadeMedidaRepository.java`;
- `ingrediente/IngredienteRepository.java`;
- `produto/ProdutoRepository.java`.

Quando ele avisar:

```powershell
git pull
```

**Você deve ver** (validado) a linha `Fast-forward` e os arquivos dele, entre eles:

```
 create mode 100644 backend/src/main/java/com/mentefria/backend/produto/ProdutoRepository.java
 create mode 100644 backend/src/main/java/com/mentefria/backend/unidade/UnidadeMedidaRepository.java
 create mode 100644 backend/src/main/java/com/mentefria/backend/usuario/UsuarioRepository.java
```

### 4.6 Entendendo o código da Fase 2

#### Um repositório por dentro (`UsuarioRepository`, do Diogo)

```java
@Repository
public class UsuarioRepository {
    private static final RowMapper<Usuario> USUARIO = (rs, n) -> new Usuario(rs.getInt("id"), rs.getString("nome"),
            rs.getString("email"), rs.getString("senha_hash"), rs.getString("perfil"), rs.getBoolean("ativo"));
    private final NamedParameterJdbcTemplate jdbc;

    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    ...
    public Optional<Usuario> buscarPorEmail(String email) {          // nunca queryForObject: lança exceção se não achar
        return jdbc.query("SELECT id, nome, email, senha_hash, perfil, ativo FROM usuario WHERE email = :email",
                Map.of("email", email), USUARIO).stream().findFirst();
    }
    ...
}
```

(Os `...` marcam os métodos que ficaram de fora do trecho: `existeAdministrador`, `inserir`, `listarFuncionarios` e a trava do cadastro.)

- **`:email` é um parâmetro com nome.** O valor vai separado do SQL (`Map.of("email", email)`). Isso impede **SQL injection**: alguém digitar `' OR 1=1 --` no e-mail não muda o comando.
- **`RowMapper`:** a "receita" para transformar cada linha do resultado num `Usuario`.
- **`Optional` + `findFirst()`:** "pode não existir". O `queryForObject` lançaria exceção quando não acha, e o login viraria `500` em vez de "E-mail ou senha inválidos.".

#### Um service por dentro (`UsuarioService`)

```java
@Transactional
public UsuarioCriado registrarAdministrador(NovoUsuario req) {
    usuarios.travarParaCadastroDeAdministrador();
    if (usuarios.existeAdministrador())                                          // CT01B: checar ANTES de validar
        throw new ApiException(409, "O cadastro do Administrador já foi realizado.", null);
    return criar(req, ADMINISTRADOR);                                            // o perfil nunca vem do corpo
}
```

- **`@Transactional`:** a trava, a conferência e o `INSERT` acontecem juntos.
- **A trava** (`LOCK TABLE usuario`) impede que **dois** cadastros de Administrador feitos no mesmo segundo passem juntos pela conferência.
- **"Checar antes de validar":** se já existe Administrador, a resposta é `409`, mesmo que o formulário esteja inválido. É isso que o caso de teste CT01B pede.
- **O perfil nunca vem do JSON:** quem chama o registro vira Administrador; quem é criado em `/api/usuarios` vira Funcionário. Ninguém consegue se "promover" mandando `"perfil": "ADMINISTRADOR"`.

```java
static Map<String, String> validar(NovoUsuario req) {
    Map<String, String> erros = new LinkedHashMap<>();
    ...
    if (req.nome() == null || req.nome().isBlank()) erros.put("nome", "Informe o nome.");
    if (email.isEmpty()) erros.put("email", "Informe o e-mail.");
    ...
    return erros;
}
```

**Todos os erros de uma vez (US7):** a validação junta **todos** os campos com problema antes de responder. A tela mostra cada mensagem no seu campo, sem o usuário corrigir um por vez.

#### O login (`AuthController`)

```java
var invalido = new ApiException(401, "E-mail ou senha inválidos.", null);
if (req.email() == null || req.senha() == null) throw invalido;
Usuario u = usuarios.buscarPorEmail(UsuarioService.normalizarEmail(req.email())).orElseThrow(() -> invalido);
if (!u.ativo() || !encoder.matches(req.senha(), u.senhaHash())) throw invalido;
return new LoginResposta(jwt.gerarToken(u.id(), u.perfil()), u.perfil(), u.nome());
```

- E-mail inexistente, senha errada ou usuário inativo dão **a mesma** resposta (CT07). Assim, ninguém descobre quais e-mails existem.
- `encoder.matches` compara a senha digitada com o **hash** guardado. A senha nunca é guardada nem comparada em texto.

#### As outras regras da Fase 2

| Regra | Onde | Teste que prova |
|---|---|---|
| E-mail com espaço e maiúscula vira minúsculo, sem espaço (`" DONO@Acai.com "` → `dono@acai.com`) | `UsuarioService.normalizarEmail` | `registroNormalizaEmailESegundoRegistroDa409` |
| `quantidade_estoque` **nunca** vem do corpo do ingrediente (o estoque só muda por movimentação) | `Ingrediente.Dados` não tem o campo | `funcionarioNaoCadastraIngredienteEAdministradorSim` |
| Produto: ingrediente repetido é conferido **antes** de "não encontrado"; **açaí obrigatório**; produto e composição na **mesma transação** | `ProdutoService.criar` | `produtoSemAcai…`, `ingredienteInexistenteNaComposicaoNaoGravaOProduto` |
| "Tem açaí?" funciona com acento, sem acento, em maiúsculas e com espaços sobrando. **"Assaí" não conta**: é uma limitação conhecida, e o teste a registra | `RegraAcai` | `RegraAcaiTest` (13 casos) |

#### A `IntegracaoBase`: como os testes com banco funcionam

```java
@SpringBootTest(...)
@AutoConfigureMockMvc
@Import(IntegracaoBase.RelogioDeTeste.class)
public abstract class IntegracaoBase {
    ...
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 28, 15, 0);
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");
    static { POSTGRES.start(); }
    ...
```

- **`@SpringBootTest`:** sobe a aplicação inteira, como no `spring-boot:run`, mas dentro do teste.
- **`PostgreSQLContainer("postgres:16")` + `@ServiceConnection`:** o Testcontainers liga um PostgreSQL 16 no Docker. O Spring passa a usá-lo no lugar do `mente-fria-db`. Os testes **nunca** mexem no seu banco.
- **Antes de cada teste, o schema é recriado** a partir do `bd/Script_DDL.sql` (A25). Cada teste começa do zero e não depende do outro.
- **Relógio ajustável:** "agora" é sempre 28/10/2026, 15h em São Paulo. Os testes podem "viajar no tempo" com `relogio.em(...)`.
- **Os módulos Python viram dublês** (`@MockitoBean`): os testes rodam sem Python e sem licença do Gurobi.
- **O nome termina em `Test`, e não em `IT`:** o Maven, no padrão, ignora classes que terminam em `IT`.

### 4.7 Rodar os testes da Fase 2

**Antes:** Docker Desktop aberto (Parte 1.2).

```powershell
cd backend
.\mvnw test "-Dtest=SegurancaEErrosTest,RegraAcaiTest,Fase2CrudTest"
```

**O que faz:** roda as três classes. A vírgula separa as classes.

**Você deve ver** (validado, cerca de 17 segundos):

```
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.config.SegurancaEErrosTest
[INFO] Running com.mentefria.backend.Fase2CrudTest
... tc.testcontainers/ryuk:0.14.0 : Container testcontainers/ryuk:0.14.0 started ...
... tc.postgres:16                : Container postgres:16 started ...
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.Fase2CrudTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.produto.RegraAcaiTest
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**As duas linhas `Container … started`** são o Testcontainers trabalhando:
- `ryuk` é um "faxineiro" que apaga o banco de teste quando os testes acabam;
- `postgres:16` é o banco dos testes.

Na **primeira** vez, o Docker baixa essas imagens e demora mais.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `Could not find a valid Docker environment` | Docker fechado ou travado | Parte 1.2 |
| `cannot find symbol … UsuarioRepository` | O commit do Diogo ainda não chegou | Parte 4.5 (`git pull`) |
| `cannot find symbol … OtimizadorProducao` na `IntegracaoBase` | Faltam os 4 arquivos de infraestrutura | Copie de novo o bloco da Parte 4.3 |
| `listaIngredientesCompletaComEstoqueZero` falha com 40 | O `bd/Script_DDL.sql` ainda é o antigo | O PR do banco do Diogo não está na `main` (Parte 4.1) |
| `Ambiguous mapping` | O `RotasDeTesteController` perdeu o `@Profile` | Copie de novo da referência |

Volte para a raiz: `cd ..`.

### 4.8 Recriar o banco de desenvolvimento com o schema novo

**Por quê:**
- O seu banco `mente_fria`, dentro do container `mente-fria-db`, ainda tem as tabelas da Sprint 1.
- O backend da Fase 2 espera as tabelas novas (por exemplo, `quantidade_restante`).
- **Recriar só o banco** apaga os dados de teste que você tinha nele. O container continua o mesmo.

**Antes:** feche qualquer conexão com o banco `mente_fria`: a aba *Database* do IntelliJ, o pgAdmin, um `psql` aberto. Pare o backend, se estiver rodando.

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria WITH (FORCE);"
docker exec mente-fria-db psql -U postgres -c "CREATE DATABASE mente_fria;"
docker cp bd\Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql
"exit: $LASTEXITCODE"
```

| Parte | O que faz |
|---|---|
| `docker exec mente-fria-db psql -U postgres` | Roda o `psql` (o terminal do PostgreSQL) **dentro** do container, como o usuário `postgres` |
| `-c "…"` | Executa um comando SQL e sai |
| *(sem `-d`)* | Conecta no banco de manutenção `postgres`. Você não pode apagar o banco em que está conectado |
| `DROP DATABASE IF EXISTS mente_fria WITH (FORCE)` | Apaga **só o banco** `mente_fria`. O `WITH (FORCE)` derruba conexões esquecidas abertas |
| `docker cp arquivo container:/tmp/…` | Copia o `Script_DDL.sql` novo da sua máquina para **dentro** do container (o container é "outra máquina") |
| `-d mente_fria` | Agora, sim, conecta no banco novo |
| `-q` | Modo silencioso: sem mensagens = deu certo |
| `-v ON_ERROR_STOP=1` | Para no primeiro erro, em vez de seguir e esconder o problema |
| `-f /tmp/Script_DDL.sql` | Executa o arquivo |
| `$LASTEXITCODE` | O "código de saída" do último programa: **0 = sucesso** |

**Você deve ver** (validado):

```
DROP DATABASE
CREATE DATABASE
exit: 0
```

**Confira o banco novo:**

```powershell
docker exec mente-fria-db psql -U postgres -d mente_fria -c "SELECT nome, quantidade_estoque FROM ingrediente ORDER BY id;"
```

**Você deve ver** (validado):

```
          nome          | quantidade_estoque
------------------------+--------------------
 Açaí (polpa)           |              0.000
 Leite condensado       |              0.000
 Granola                |              0.000
 Banana                 |              0.000
 Morango                |              0.000
 Leite em pó            |              0.000
 Copo descartável 500ml |              0.000
(7 rows)
```

O estoque começa em **0** (decisão D11): ele só muda por movimentação.

> Se os acentos aparecerem trocados (`AÃ§aÃ­`), é só a **exibição** do terminal. O dado no banco está certo.

### 4.9 As 10 conferências manuais (o *gate* da Fase 2)

**Objetivo:** provar, pela API de verdade, o que o Leonardo vai usar nas telas.

1. **Terminal B:** `cd "<PASTA_DO_PROJETO>\backend"` e `.\mvnw spring-boot:run`. Espere `Started BackendApplication`.
2. **Terminal A:** se for um PowerShell novo, cole a função `Chamar` e o `$api` de novo (Parte 3.7).
3. Rode **um bloco por vez** e compare.

**1. Cadastrar o Administrador (CT01)**, com e-mail "sujo" de propósito:

```powershell
Chamar POST "$api/auth/registro" @{ nome = "Dono"; email = " DONO@Acai.com "; senha = "senha1234" } | ConvertTo-Json -Depth 6 -Compress
```

`@{ chave = valor; … }` é como o PowerShell escreve um objeto. A função o transforma em JSON.

**Você deve ver:** `{"status":201,"corpo":{"id":1,"nome":"Dono","email":"dono@acai.com","perfil":"ADMINISTRADOR"}}`. Repare no e-mail **normalizado**.

**2. Cadastrar de novo (CT01B):**

```powershell
Chamar POST "$api/auth/registro" @{ nome = "Dono"; email = "outro@acai.com"; senha = "senha1234" } | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:** `{"status":409,"corpo":{"mensagem":"O cadastro do Administrador já foi realizado."}}`

**3. Login (CT06), guardando o token numa variável:**

```powershell
$login = Chamar POST "$api/auth/login" @{ email = "dono@acai.com"; senha = "senha1234" }
$login.corpo | Select-Object perfil, nome | ConvertTo-Json -Compress
$tokAdmin = $login.corpo.token
"o token tem " + $tokAdmin.Length + " caracteres"
```

**Você deve ver:** `{"perfil":"ADMINISTRADOR","nome":"Dono"}` e `o token tem 202 caracteres`. O tamanho pode variar um pouco. `$tokAdmin` guarda o crachá para as próximas chamadas.

**4. Senha errada e e-mail inexistente (CT07): a mesma resposta.**

```powershell
Chamar POST "$api/auth/login" @{ email = "dono@acai.com"; senha = "errada123" } | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/auth/login" @{ email = "ninguem@acai.com"; senha = "senha1234" } | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver**, nas duas linhas: `{"status":401,"corpo":{"mensagem":"E-mail ou senha inválidos."}}`

**5. Listar ingredientes com token (CT09):**

```powershell
$ing = (Chamar GET "$api/ingredientes" $null $tokAdmin).corpo
"itens: " + @($ing).Count
$ing[0] | ConvertTo-Json -Compress
```

O `$null` ocupa o lugar do corpo (`GET` não tem corpo). O token vai no quarto lugar.

**Você deve ver:**

```
itens: 7
{"id":1,"nome":"Açaí (polpa)","unidade_medida":"kg","custo_unitario":18.50,"porcao_padrao":0.150,"quantidade_estoque":0.000,"quantidade_minima":10.000}
```

São sete itens, com **todos** os campos e estoque 0.

**6. Criar um Funcionário; ele não pode cadastrar ingrediente (CT04, CT05):**

```powershell
Chamar POST "$api/usuarios" @{ nome = "Func"; email = "func@acai.com"; senha = "senha1234" } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
$tokFunc = (Chamar POST "$api/auth/login" @{ email = "func@acai.com"; senha = "senha1234" }).corpo.token
Chamar POST "$api/ingredientes" @{ nome = "Paçoca"; unidade_medida = "kg"; custo_unitario = 20; porcao_padrao = 0.05; quantidade_minima = 1 } $tokFunc | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/ingredientes" @{ nome = "Paçoca"; unidade_medida = "kg"; custo_unitario = 20; porcao_padrao = 0.05; quantidade_minima = 1 } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:**

```
{"status":201,"corpo":{"id":2,"nome":"Func","email":"func@acai.com","perfil":"FUNCIONARIO"}}
{"status":403,"corpo":{"mensagem":"Você não tem permissão para esta ação."}}
{"status":201,"corpo":{"id":8,"nome":"Paçoca","unidade_medida":"kg","custo_unitario":20.00,"porcao_padrao":0.050,"quantidade_estoque":0.000,"quantidade_minima":1.000}}
```

**7. Dois campos inválidos de uma vez (CT10, CT11, CT18):**

```powershell
Chamar POST "$api/ingredientes" @{ nome = "Mel"; unidade_medida = "kg"; custo_unitario = -1; porcao_padrao = 0; quantidade_minima = 1 } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:** `{"status":400,"corpo":{"mensagem":"Dados inválidos","campos":{"custo_unitario":"O custo não pode ser negativo.","porcao_padrao":"A porção deve ser maior que zero."}}}`

**8. Produtos inválidos (CT13, CT15):** sem açaí, vazio e com ingrediente repetido.

```powershell
Chamar POST "$api/produtos" @{ nome = "So banana"; preco_venda = 10; composicao = @(@{ ingrediente_id = 4; quantidade_utilizada = 0.1 }) } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/produtos" @{ nome = "Vazio"; preco_venda = 10; composicao = @() } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/produtos" @{ nome = "Rep"; preco_venda = 10; composicao = @(@{ ingrediente_id = 1; quantidade_utilizada = 0.3 }, @{ ingrediente_id = 1; quantidade_utilizada = 0.1 }) } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

`@( … )` é uma **lista** no PowerShell; aqui, a lista da composição.

**Você deve ver:**

```
{"status":400,"corpo":{"mensagem":"Dados inválidos","campos":{"composicao":"O produto precisa ter o açaí entre os ingredientes."}}}
{"status":400,"corpo":{"mensagem":"Dados inválidos","campos":{"composicao":"Adicione ao menos um ingrediente."}}}
{"status":400,"corpo":{"mensagem":"Dados inválidos","campos":{"composicao":"Este ingrediente já foi adicionado ao produto."}}}
```

**9. Um produto válido e a lista com a composição (CT12):**

```powershell
Chamar POST "$api/produtos" @{ nome = "Açaí Teste"; preco_venda = 15.90; composicao = @(@{ ingrediente_id = 1; quantidade_utilizada = 0.3 }, @{ ingrediente_id = 7; quantidade_utilizada = 1 }) } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
$prods = (Chamar GET "$api/produtos" $null $tokAdmin).corpo
"produtos: " + @($prods).Count
$prods[0] | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:**

```
{"status":201,"corpo":{"id":4,"nome":"Açaí Teste","preco_venda":15.9,"ativo":true}}
produtos: 4
{"id":2,"nome":"Açaí Especial 500ml","preco_venda":18.90,"ativo":true,"composicao":[{"ingrediente_id":1,"ingrediente_nome":"Açaí (polpa)","quantidade_utilizada":0.450,"unidade_medida":"kg"},...]}
```

A lista vem em ordem de nome e traz a **composição** de cada produto.

**10. Preço: zero não pode; 13,50 pode; o Funcionário não pode (CT16, CT17, CT05):**

```powershell
Chamar PUT "$api/produtos/1/preco" @{ preco_venda = 0 } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar PUT "$api/produtos/1/preco" @{ preco_venda = 13.50 } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar PUT "$api/produtos/1/preco" @{ preco_venda = 14 } $tokFunc | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:**

```
{"status":400,"corpo":{"mensagem":"Dados inválidos","campos":{"preco_venda":"O preço deve ser maior que zero."}}}
{"status":200,"corpo":{"id":1,"preco_venda":13.5}}
{"status":403,"corpo":{"mensagem":"Você não tem permissão para esta ação."}}
```

**Pare o backend** (Terminal B: Ctrl+C, S).

**Com as 10 batendo,** o *gate* está aberto: **avise o Leonardo** (ele faz o aceite das telas no sábado de manhã).

### 4.10 Evidência, commit e PR

**Guardar a saída dos testes como evidência** (Parte 2.8), na `<PASTA_DO_PROJETO>`:

```powershell
cd backend
$saida = .\mvnw test "-Dtest=SegurancaEErrosTest,RegraAcaiTest,Fase2CrudTest"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-30_lucas_fase2_mvnw-test.txt
$saida | Select-String "Tests run:|BUILD"
cd ..
```

Depois, siga a Parte 2:

```powershell
git add backend docs\Sprint_4\evidencias
git commit -m "test: evidencia dos testes da Fase 2"
git push
```

**Antes do merge,** confira que o PR mostra **os dois autores** (você e o Diogo) na aba *Commits*. Faça o merge na sexta à noite (M2).

### 4.11 Perguntas para a Review

1. Por que `:email` no SQL, e não `"… WHERE email = '" + email + "'"`?
2. O que acontece se dois navegadores tentarem cadastrar o Administrador no mesmo segundo?
3. Por que o login responde a mesma coisa para "e-mail não existe" e "senha errada"?
4. O que o `@Transactional` garante no cadastro de produto com composição?
5. Os testes mexem no seu banco `mente_fria`? Por quê?

---

## Parte 5 — Fase 3: estoque por linhas e pedido (US9, US12), com o Diogo

**Objetivo:**
- entradas e saídas de estoque **por linha** (cada entrada com a sua validade);
- pedidos com **baixa automática**, sempre da validade mais próxima, e **nunca** de estoque vencido;
- tudo ou nada.

**Quando:** sáb 31/10.
**Branch:** `feature/backend-fase3-estoque-pedido`.
**Pré-requisito:** Fase 2 na `main`.

### 5.1 O conceito: estoque por linhas (FEFO)

Cada **entrada** de um ingrediente vira uma **linha** com três dados: a quantidade, a validade e quanto ainda **resta** dela (`quantidade_restante`).

```
Açaí (polpa)
  linha 4: entrou 2,000 kg, validade 27/10 (VENCIDA) -> resta 0,000 (descartada)
  linha 1: entrou 10,000 kg, validade 28/11 (OK)     -> resta 9,400
  saldo total = soma do que resta = 9,400
```

- **FEFO** (*first expired, first out*): a venda tira primeiro da linha que **vence antes**.
- **Estoque vencido nunca é vendido.** Ele só sai por **descarte** (motivo `DESCARTE_VENCIDO`).
- **Invariante:** o saldo do ingrediente é sempre igual à soma do que resta nas linhas.

### 5.2 Criar a branch, copiar a sua parte, commit e push

```powershell
git checkout main
git pull
git checkout -b feature/backend-fase3-estoque-pedido
$r = "docs\Sprint_3\referencia\backend\src\main\java\com\mentefria\backend"
$d = "backend\src\main\java\com\mentefria\backend"
New-Item -ItemType Directory -Force "$d\estoque", "$d\pedido" | Out-Null
Copy-Item "$r\estoque\BaixaPorLinhas.java", "$r\estoque\EstoqueService.java", "$r\estoque\EstoqueController.java" "$d\estoque\"
Copy-Item "$r\pedido\PedidoService.java", "$r\pedido\PedidoController.java" "$d\pedido\"
Copy-Item "docs\Sprint_3\referencia\backend\src\test\java\com\mentefria\backend\Fase3EstoquePedidoTest.java" "backend\src\test\java\com\mentefria\backend\"
git add backend
git commit -m "feat: estoque por linhas e pedido com baixa automatica (parte do Lucas)"
git push -u origin feature/backend-fase3-estoque-pedido
```

Avise o Diogo. Ele põe:
- `estoque/MovimentacaoRepository.java`;
- `pedido/PedidoRepository.java`.

Quando ele avisar, rode `git pull`.

| Arquivo | Para quê |
|---|---|
| `BaixaPorLinhas` | A "máquina" que consome as linhas na ordem certa. É usada pelo pedido, pela saída convencional e pelo descarte |
| `EstoqueService`, `EstoqueController` | `GET /api/estoque`, `GET /api/estoque/{id}/linhas`, `POST /api/estoque/movimentacoes`, `POST /api/estoque/linhas/{linha}/descarte` |
| `PedidoService`, `PedidoController` | `GET` e `POST /api/pedidos` |
| `MovimentacaoRepository`, `PedidoRepository` (Diogo) | Os SQLs das linhas (com trava) e dos pedidos |

### 5.3 Entendendo o código da Fase 3

#### A trava (`FOR UPDATE`) e por que a ordem importa

```java
// MovimentacaoRepository (Diogo)
SELECT id, quantidade_restante FROM movimentacao_estoque
WHERE ingrediente_id = :ing AND tipo = 'ENTRADA' AND quantidade_restante > 0
  AND (data_validade IS NULL OR data_validade >= :hoje)
ORDER BY data_validade NULLS LAST, id FOR UPDATE
```

- **`data_validade >= :hoje`:** só linhas válidas. A que vence **hoje** ainda vale.
- **`ORDER BY data_validade NULLS LAST, id`:** é o FEFO. Linhas sem validade (como o copo) ficam por último.
- **`FOR UPDATE`:** trava essas linhas até a transação acabar. Antes delas, o pedido já travou as **linhas dos ingredientes** (`ingredientes.travar`, também com `FOR UPDATE`, em ordem de id). Se dois pedidos chegam juntos, o segundo **espera** já nessa primeira trava e só segue quando o primeiro termina, enxergando o saldo reduzido. Sem isso, os dois venderiam o mesmo açaí. (Uma leitura simples, sem `FOR UPDATE`, como a de `GET /api/estoque`, não espera.)
- **`:hoje` vem do Java,** pelo relógio de São Paulo, nunca do `CURRENT_DATE` do banco (decisão F3).

#### O pedido: tudo ou nada, em 9 passos

```java
@Transactional
public PedidoCriado criar(NovoPedido req, int usuarioId) {
    // 1. Validar os itens (400, campo "itens")
    // 2. Produtos existem e estão ativos
    // 3. Quanto cada ingrediente precisa (BigDecimal)
    // 4. Travar em ORDEM DE ID (sem deadlock) e 5. carregar só as linhas válidas
    ingredientes.travar(new ArrayList<>(necessario.keySet()));
    ...
        // 6. Algum ingrediente não cobre: 422 e nada é gravado
        if (BaixaPorLinhas.disponivel(validas).compareTo(e.getValue()) < 0)
            throw new ApiException(422, "Estoque insuficiente para o produto " + produtoQuePede.get(e.getKey()), null);
    // 7. Total com o preço DO BANCO e 8. itens com o preço registrado
    // 9. Baixa por linhas: uma SAIDA (motivo PEDIDO) por ingrediente
}
```

- **Tudo ou nada (CT25):** se **um** ingrediente faltar, sai `422` e nada fica gravado. O `@Transactional` desfaz tudo.
- **Travar em ordem de id:** se o pedido A trava açaí e depois banana, e o B trava banana e depois açaí, os dois podem esperar um pelo outro para sempre. Isso é um *deadlock*. Travando todos sempre na mesma ordem, isso não acontece.
- **`BigDecimal`:** dinheiro e quantidades usam `BigDecimal`, e não `double`. Com `double`, `0.1 + 0.2` dá `0.30000000000000004`.
- **O preço vem do banco,** nunca do JSON: ninguém consegue comprar com preço inventado.
- **Quem fez o pedido** vem do **token** (`authentication.getName()` é o id do usuário), nunca do corpo.

#### `BaixaPorLinhas.consumir`

```java
if (disponivel(linhas).compareTo(quantidade) < 0) throw semEstoque.get();
BigDecimal falta = quantidade;
for (var linha : linhas) {
    if (falta.signum() <= 0) break;
    BigDecimal tira = linha.restante().min(falta);
    movimentacoes.consumirLinha(linha.id(), tira);
    falta = falta.subtract(tira);
}
ingredientes.baixarSaldo(ingredienteId, quantidade);
```

Primeiro confere se o total dá. Se não der, lança o erro **sem mexer em nada**. Depois tira de cada linha o que ela tem, até cobrir a quantidade. No fim baixa o saldo do ingrediente e grava **uma** `SAIDA`.

#### O descarte de uma linha (botão "Descartar", A12)

`EstoqueService.descartarLinha` trava **primeiro o ingrediente e depois a linha**: é a mesma ordem do pedido, para não haver *deadlock*. Ele só aceita linha **vencida** com saldo. O botão é opcional e é o primeiro corte (Parte 9). O endpoint fica de qualquer jeito, porque está testado.

### 5.4 Rodar os testes da Fase 3

```powershell
cd backend
.\mvnw test "-Dtest=Fase3EstoquePedidoTest"
cd ..
```

**Você deve ver** (validado, cerca de 18 segundos):

```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.Fase3EstoquePedidoTest
[INFO] BUILD SUCCESS
```

| Cenário que os testes provam | Resultado |
|---|---|
| S1: linha vencida de 20 e válida de 20; saída de 5; descarte de 20 | A saída tira da válida; o descarte só da vencida |
| S2: a saída sai da validade mais próxima | FEFO |
| S3: uma baixa que atravessa várias linhas | Consome em ordem |
| S4: só existe vencido; pedido de 1 | `422`, nada gravado |
| S5: descarte maior que o vencido | `422`; o valor exato funciona |
| Dois pedidos de 30 sobre 50, ao mesmo tempo, 5 rodadas | Um passa, o outro `422`; saldo final 20 |
| Botão "Descartar" (CT71) | `201`; repetir → `422`; linha válida → `422`; inexistente → `404` |

### 5.5 Conferências manuais da Fase 3

**Use o mesmo banco da Parte 4.9: não recrie.** Os números abaixo (por exemplo, o preço de 13,50) dependem do que você fez lá.

Suba o backend da branch da Fase 3 (Terminal B: `cd "<PASTA_DO_PROJETO>\backend"` e `.\mvnw spring-boot:run`). No Terminal A, cole a `Chamar` e o `$api` (Parte 3.7), se for um PowerShell novo, e entre de novo como Administrador e Funcionário:

```powershell
$tokAdmin = (Chamar POST "$api/auth/login" @{ email = "dono@acai.com"; senha = "senha1234" }).corpo.token
$tokFunc = (Chamar POST "$api/auth/login" @{ email = "func@acai.com"; senha = "senha1234" }).corpo.token
$validade = (Get-Date).AddDays(30).ToString("yyyy-MM-dd")
$vencida = (Get-Date).AddDays(-2).ToString("yyyy-MM-dd")
```

`$validade` é a data de daqui a 30 dias e `$vencida` a de 2 dias atrás, no formato `2026-11-28`.

**1. Entrada de 10 kg de açaí (CT22, CT30)**, mais 10 de leite condensado e 10 copos, para o pedido do item 5:

```powershell
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 1; tipo = "ENTRADA"; quantidade = 10; data_validade = $validade } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 2; tipo = "ENTRADA"; quantidade = 10; data_validade = $validade } $tokAdmin | Out-Null
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 7; tipo = "ENTRADA"; quantidade = 10; data_validade = $validade } $tokAdmin | Out-Null
((Chamar GET "$api/estoque" $null $tokAdmin).corpo | Where-Object { $_.ingrediente_id -eq 1 }) | ConvertTo-Json -Compress
```

`Where-Object { … }` filtra a lista: aqui, só o ingrediente 1.

**Você deve ver:**

```
{"status":201,"corpo":{"id":1,"ingrediente_id":1,"tipo":"ENTRADA","motivo":null,"quantidade":10,"data_validade":"2026-11-28","data_movimentacao":"2026-10-29T23:55:40","quantidade_estoque":10.000}}
{"ingrediente_id":1,"nome":"Açaí (polpa)","unidade_medida":"kg","quantidade_estoque":10.000,"quantidade_minima":10.000,"quantidade_valida":10.000,"quantidade_vencida":0.000,"validade_mais_proxima":"2026-11-28"}
```

**2. Saída maior que o válido (CT21, CT33):**

```powershell
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 1; tipo = "SAIDA"; motivo = "CONVENCIONAL"; quantidade = 50 } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:** `{"status":422,"corpo":{"mensagem":"Estoque insuficiente para o ingrediente Açaí (polpa)"}}`

**3. Saída com validade, e saída sem motivo (CT35):**

```powershell
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 1; tipo = "SAIDA"; motivo = "CONVENCIONAL"; quantidade = 1; data_validade = $validade } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 1; tipo = "SAIDA"; quantidade = 1 } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:**

```
{"status":400,"corpo":{"mensagem":"Dados inválidos","campos":{"data_validade":"A validade só pode ser informada em entradas."}}}
{"status":400,"corpo":{"mensagem":"Dados inválidos","campos":{"motivo":"Informe o motivo da saída."}}}
```

**4. Entrada já vencida e descarte (CT34):**

```powershell
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 1; tipo = "ENTRADA"; quantidade = 2; data_validade = $vencida } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 1; tipo = "SAIDA"; motivo = "DESCARTE_VENCIDO"; quantidade = 2 } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver** o saldo subir para 12 e voltar para 10:

```
{"status":201,"corpo":{"id":4,...,"tipo":"ENTRADA",...,"data_validade":"2026-10-27",...,"quantidade_estoque":12.000}}
{"status":201,"corpo":{"id":5,...,"tipo":"SAIDA","motivo":"DESCARTE_VENCIDO","quantidade":2,...,"quantidade_estoque":10.000}}
```

A entrada vencida é **aceita**: alguém pode registrar uma compra que já chegou vencida. Mas esse saldo nunca é vendido.

**5. Pedido de 2 × Açaí Tradicional, feito pelo Funcionário (CT38, CT27):**

```powershell
Chamar POST "$api/pedidos" @{ itens = @(@{ produto_id = 1; quantidade = 2 }) } $tokFunc | ConvertTo-Json -Depth 6 -Compress
((Chamar GET "$api/estoque" $null $tokAdmin).corpo | Where-Object { $_.ingrediente_id -in 1,2,7 }) | Select-Object ingrediente_id, nome, quantidade_estoque | ConvertTo-Json -Compress
```

**Você deve ver:**

```
{"status":201,"corpo":{"id":1,"valor_total":27.00,"data_pedido":"2026-10-29T23:55:41"}}
[{"ingrediente_id":1,"nome":"Açaí (polpa)","quantidade_estoque":9.400},{"ingrediente_id":7,"nome":"Copo descartável 500ml","quantidade_estoque":8.000},{"ingrediente_id":2,"nome":"Leite condensado","quantidade_estoque":9.940}]
```

- **Total:** 2 × 13,50 = 27,00 (o preço que você mudou no item 10 da Fase 2).
- **Baixas:** açaí −0,600, leite condensado −0,060 e copo −2.

**6. Pedido com um item sem estoque (CT25): nada é gravado.**

```powershell
Chamar POST "$api/pedidos" @{ itens = @(@{ produto_id = 1; quantidade = 1 }, @{ produto_id = 2; quantidade = 1 }) } $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:** `{"status":422,"corpo":{"mensagem":"Estoque insuficiente para o produto Açaí Especial 500ml"}}`. O Especial usa banana e granola, que estão em 0. O Tradicional do mesmo pedido também **não** foi vendido.

**7–8. A lista de pedidos (CT40):**

```powershell
$ped = (Chamar GET "$api/pedidos" $null $tokAdmin).corpo
"pedidos: " + @($ped).Count
$ped[0] | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:**

```
pedidos: 1
{"id":1,"data_pedido":"2026-10-29T23:55:41","cliente_id":null,"usuario_nome":"Func","valor_total":27.00,"status":"ATIVO","itens":[{"produto_id":1,"nome":"Açaí Tradicional 300ml","quantidade":2,"preco_unitario_registrado":13.50}]}
```

É **um** pedido só: o do item 6 não foi gravado.

**9. As linhas do açaí e o botão "Descartar" (CT71):**

```powershell
(Chamar GET "$api/estoque/1/linhas" $null $tokAdmin).corpo | ConvertTo-Json -Compress
$e = (Chamar POST "$api/estoque/movimentacoes" @{ ingrediente_id = 3; tipo = "ENTRADA"; quantidade = 1.5; data_validade = (Get-Date).AddDays(-1).ToString("yyyy-MM-dd") } $tokAdmin).corpo
Chamar POST "$api/estoque/linhas/$($e.id)/descarte" $null $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/estoque/linhas/$($e.id)/descarte" $null $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/estoque/linhas/1/descarte" $null $tokAdmin | ConvertTo-Json -Depth 6 -Compress
Chamar POST "$api/estoque/linhas/999/descarte" $null $tokAdmin | ConvertTo-Json -Depth 6 -Compress
```

`$($e.id)` põe o `id` da linha recém-criada no meio do endereço.

**Você deve ver:**

```
[{"id":4,...,"quantidade_restante":0.000,"data_validade":"2026-10-27","situacao":"VENCIDA"},{"id":1,...,"quantidade_restante":9.400,"data_validade":"2026-11-28","situacao":"OK"}]
{"status":201,"corpo":{"id":10,"ingrediente_id":3,"tipo":"SAIDA","motivo":"DESCARTE_VENCIDO","quantidade":1.500,...,"quantidade_estoque":0.000}}
{"status":422,"corpo":{"mensagem":"Esta linha já não tem saldo."}}
{"status":422,"corpo":{"mensagem":"Só é possível descartar linhas vencidas."}}
{"status":404,"corpo":{"mensagem":"Linha de estoque não encontrada."}}
```

Pare o backend (Terminal B: Ctrl+C, S).

**Evidência, commit e PR** (Terminal A, na `<PASTA_DO_PROJETO>`):

```powershell
cd backend
$saida = .\mvnw test "-Dtest=Fase3EstoquePedidoTest"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-31_lucas_fase3_mvnw-test.txt
$saida | Select-String "Tests run:|BUILD"
cd ..
```

```powershell
git add backend docs\Sprint_4\evidencias
git commit -m "test: evidencia dos testes da Fase 3"
git push
```

Abra o PR (Parte 2.4) e faça o merge no sábado à noite.

### 5.6 Perguntas para a Review

1. Por que existe `quantidade_restante` em cada entrada, e não só o saldo do ingrediente?
2. O que o `FOR UPDATE` evita? Conte o caso dos dois pedidos ao mesmo tempo.
3. O que é um *deadlock*, e como a ordem por `id` o evita?
4. Por que o preço do pedido vem do banco, e não do JSON?
5. Se um pedido tem dois produtos e só um tem estoque, o que acontece?

---

## Parte 6 — Fase 4A: a ponte Java → Python e a recomendação (US10, US11), em dupla com o Kauê

**Objetivo:** `POST /api/producao/recomendacao` chama o módulo Python (Gurobi) e devolve quanto produzir de cada copo.
**Quando:** dom 01/11, **em dupla com o Kauê**.
**Branch:** `feature/backend-fase4a-producao`.

### 6.1 Pré-requisitos

1. **A Fase 3 na `main`.**
2. **A Fase P do Kauê na `main`:** o `otimizador.py` novo, que calcula por lucro, e a pasta `optimization/exemplos`. Confira:

```powershell
git checkout main
git pull
Select-String -Path optimization\app\otimizador.py -Pattern "custo_unitario" | Select-Object -First 1
Test-Path optimization\exemplos
```

**Você deve ver:** uma linha com `custo_unitario` e `True`. Se não, a Fase P ainda não entrou: combine com o Kauê.

### 6.2 O seu ambiente Python (uma vez)

**Por quê:**
- Os testes "com o Python de verdade" e a recomendação manual precisam de um Python com o `gurobipy`.
- Cada um tem o **seu** ambiente virtual (`venv`): uma pasta com um Python separado só para o projeto.
- A pasta `optimization\venv` **não vai para o Git** (o `.gitignore` já a esconde).

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
py --version
py -m venv optimization\venv
optimization\venv\Scripts\python.exe -m pip install -r optimization\requirements.txt
optimization\venv\Scripts\python.exe -c "import gurobipy; print(gurobipy.gurobi.version())"
```

| Comando | O que faz |
|---|---|
| `py --version` | Confere o Python instalado (o projeto usa 3.14). Se aparecer `'py' não é reconhecido` ou uma versão abaixo de 3.14, instale o Python 3.14 pelo site oficial (python.org), marcando "Add python.exe to PATH", e abra um PowerShell novo. Se tiver dúvida, peça ajuda ao Kauê |
| `py -m venv optimization\venv` | Cria o ambiente virtual na pasta `optimization\venv` |
| `…\python.exe -m pip install -r …\requirements.txt` | Instala as bibliotecas do projeto **dentro** do ambiente (inclusive o `gurobipy`). Chamamos o `python.exe` do ambiente diretamente, e assim não é preciso "ativar" nada |
| `…\python.exe -c "import gurobipy; …"` | Confere que o `gurobipy` está lá |

**Você deve ver** (validado, cerca de 20 segundos no `pip`):

```
Python 3.14.0
...
[notice] A new release of pip is available: 25.2 -> 26.2.2
...
(13, 0, 3)
```

> O aviso `[notice] A new release of pip…` (às vezes em vermelho) **não é erro**: é só a sugestão de atualizar o `pip`. O que importa é a última linha, `(13, 0, 3)`.

**A licença:**
- O `gurobipy` instalado pelo `pip` já vem com uma licença limitada, que basta para estes modelos pequenos.
- **Não** use a licença WLS do Kauê: ela é pessoal (decisões A34 e A38).

**Diga ao backend qual Python usar:** abra `notepad backend\.env` e deixe estas linhas assim:

```
PYTHON_CMD=../optimization/venv/Scripts/python.exe
OTIMIZADOR_SCRIPT=../optimization/app/otimizador.py
REPOSICAO_SCRIPT=../optimization/app/reposicao.py
```

**Por que esses caminhos:**
- `../` porque o backend roda **de dentro** de `backend`, e o caminho começa de lá.
- Barras `/`, e não `\`, porque o `.env` é lido como `.properties`, onde `\` é caractere especial.

### 6.3 Copiar a sua parte

Esta fase não tem repositório do Diogo, então não há fluxo a dois.

```powershell
git checkout -b feature/backend-fase4a-producao
$r = "docs\Sprint_3\referencia\backend\src"
$d = "backend\src"
Copy-Item "$r\main\java\com\mentefria\backend\python\ChaveOtimizador.java", "$r\main\java\com\mentefria\backend\python\TestePythonController.java" "$d\main\java\com\mentefria\backend\python\"
Copy-Item "$r\main\java\com\mentefria\backend\producao\ProducaoService.java", "$r\main\java\com\mentefria\backend\producao\ProducaoController.java" "$d\main\java\com\mentefria\backend\producao\"
New-Item -ItemType Directory -Force "$d\test\java\com\mentefria\backend\python" | Out-Null
Copy-Item "$r\test\java\com\mentefria\backend\Fase4ProducaoTest.java", "$r\test\java\com\mentefria\backend\PythonRealTest.java" "$d\test\java\com\mentefria\backend\"
Copy-Item "$r\test\java\com\mentefria\backend\python\ProcessoPythonTest.java" "$d\test\java\com\mentefria\backend\python\"
git add backend
git commit -m "feat: recomendacao de producao (ponte Java-Python)"
git push -u origin feature/backend-fase4a-producao
```

Como esta branch é nova e não passou pelo fluxo a dois, o `push -u` é aqui. Ele liga a branch ao GitHub; os próximos `git push` não precisam mais dele.

| Arquivo | Para quê |
|---|---|
| `ChaveOtimizador` | A **chave de reserva** do plano B1: com `OTIMIZADOR_HABILITADO=false`, a produção responde `503` sem chamar o Python |
| `TestePythonController` | `GET /api/teste/python`: um "eco" para provar que o Java fala com o Python. **Só** existe no perfil `dev` |
| `ProducaoService`, `ProducaoController` | A recomendação de produção |
| `Fase4ProducaoTest` | 7 testes com o Python **dublê** |
| `PythonRealTest`, `ProcessoPythonTest` | Testes com o Python **de verdade**. Só rodam se você disser onde ele está (`MF_PYTHON`) |

### 6.4 Entendendo o código da Fase 4A

#### Como o Java chama o Python (`ProcessoPython`, copiado na Fase 2)

O Java abre o Python como um **programa separado**:
1. escreve o JSON de entrada no "teclado" dele (`stdin`);
2. lê o JSON de resposta da "tela" dele (`stdout`).

Cinco cuidados, cada um já deu problema no projeto:

| Cuidado | Como está no código | Teste |
|---|---|---|
| Entrada em UTF-8 **sem BOM**, e o `stdin` **fechado** no fim (o Python só começa quando a entrada termina) | `OutputStreamWriter(…, UTF_8)` num `try` | `ProcessoPythonTest.idaEVoltaEmUtf8` |
| Tempo limite: se o Python travar, o Java mata o processo | `waitFor(10 s)` → `destroyForcibly()` → `500` | `timeoutMataOProcesso…` |
| `stdout` e `stderr` lidos ao mesmo tempo (senão um pode travar o outro); o `stderr` vai só para o log | Leitura em paralelo | — |
| As credenciais do Gurobi (`GRB_*`) passadas ao Python **só se existirem**, e nunca no log | `pb.environment().put(…)` | `credenciaisGrbChegamAoProcessoFilho` |
| **Uma** otimização por vez | `ReentrantLock` com tempo limite (A27) | — |

#### A recomendação (`ProducaoService.recomendar`)

```java
public Recomendacao recomendar() {
    chave.exigir();                                                              // plano B da licença (A31): 503 sem chamar o Python
    LocalDate hoje = LocalDate.now(clock);
    List<IngredienteOtimizador> ings = ingredientes.entradaOtimizador(hoje);     // saldo VÁLIDO (CT29), inclusive 0
    ...
    RespostaPy r = json.readValue(otimizador.executar(json.writeValueAsString(entrada)), RespostaPy.class);
    if ("erro".equals(r.status())) throw new ApiException(422, r.mensagem(), null);
    if (!"sucesso".equals(r.status()) || r.recomendacao() == null) throw new FalhaPython("Resposta fora do contrato", null);
    // Recalcular com BigDecimal (o Python é a fonte do PLANO; os valores em reais são do Java)
```

1. **`chave.exigir()`:** se a chave de reserva estiver desligada, para aqui com `503`.
2. **Saldo válido:** o Python recebe só o estoque **não vencido** (CT29). Assim, ele nunca planeja com açaí vencido.
3. **O contrato:** o formato do JSON de ida e de volta está em `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`, §1. Se o Python responder fora dele, é `500`. Nunca mostrar um plano errado.
4. **Recalcular em Java:** o Python decide **quanto** produzir. Os **reais** (receita, lucro, lucro por copo) são recalculados com `BigDecimal`. Se o plano não couber no estoque, é `500`.
5. **Guardar o resultado** na `UltimaRecomendacao`. O marketing e o dashboard (Fase 6) leem dali.

**Sem `@Transactional` de propósito:** a chamada ao Python pode levar segundos, e não deve "segurar" uma conexão do banco esse tempo todo.

### 6.5 Rodar os testes

**Com o dublê** (sem Python):

```powershell
cd backend
.\mvnw test "-Dtest=Fase4ProducaoTest"
```

**Você deve ver** (validado): `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`. Eles conferem:
- o plano **151/1/0**, a receita 1.966,80 e o lucro 1.011,71;
- o lucro por copo 6,64 / 9,07 / 10,63;
- **a granola vai ao Python com 5,160** (o saldo válido);
- `422` e `500`; o Funcionário recebe `403`.

**Com o Python de verdade:**

```powershell
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path
$env:MF_PYTHON
.\mvnw test "-Dtest=ProcessoPythonTest,PythonRealTest#ecoComAcentosPeloPerfilDev+producaoDaDemonstracaoDa151_1_0ComOGurobi"
```

| Parte | O que faz |
|---|---|
| `$env:MF_PYTHON = …` | Cria uma variável de ambiente **só nesta janela** do PowerShell. Fechou a janela, ela some |
| `Resolve-Path …` | Transforma o caminho relativo no caminho completo |
| `"-Dtest=Classe,Classe#metodo1+metodo2"` | Roda a classe inteira `ProcessoPythonTest` e só **dois** testes do `PythonRealTest`. O terceiro (reposição) depende da Fase 5 do Kauê |

**Você deve ver** (validado):

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.python.ProcessoPythonTest
[TEMPO] recomendação de produção ponta a ponta: 258 ms
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.PythonRealTest
[INFO] BUILD SUCCESS
```

> A linha `[TEMPO]` pode aparecer como `recomenda��o` na sua tela. É só a exibição dos acentos no Maven.

**Sem `MF_PYTHON`, esses testes são pulados, não falham.** Para ver:

```powershell
Remove-Item Env:MF_PYTHON
.\mvnw test "-Dtest=ProcessoPythonTest,PythonRealTest"
```

**Você deve ver:** `Tests run: 7, Failures: 0, Errors: 0, Skipped: 7`.

Volte para a raiz do projeto antes da Parte 6.6:

```powershell
cd ..
```

### 6.6 Conferências manuais da Fase 4A

**1. O eco pelo perfil `dev`.** No **Terminal B**:

```powershell
cd "<PASTA_DO_PROJETO>\backend"
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw spring-boot:run
```

**Você deve ver** no log: `The following 1 profile is active: "dev"`.

No **Terminal A**:

```powershell
Chamar GET "$api/teste/python" | ConvertTo-Json -Depth 6 -Compress
```

**Você deve ver:** `{"status":200,"corpo":{"status":"sucesso","echo":"AÇAÍ (POLPA)"}}`. O Java mandou o texto ao Python, e o Python o devolveu em maiúsculas, **com os acentos**.

Pare o backend. No Terminal B, rode `Remove-Item Env:SPRING_PROFILES_ACTIVE` e suba de novo **sem** o perfil. A mesma chamada deve dar `401`.

**2. A recomendação com os dados de demonstração** (o resultado que a Review vai mostrar).

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>` (se estiver em `backend`, rode `cd ..`). Se for um PowerShell novo, cole a `Chamar` e o `$api` de novo (Parte 3.7).

- **Pré-requisito:** o `bd/seed_demo.sql` do Diogo na `main`.
- O seed precisa de um banco **recém-criado**, com o Administrador e um Funcionário já cadastrados pela API.

1. Pare o backend (Terminal B: Ctrl+C, S). Recrie o banco com os comandos da Parte 4.8, no Terminal A, na `<PASTA_DO_PROJETO>`.
2. Suba o backend sem perfil (Terminal B).
3. No Terminal A:

```powershell
Chamar POST "$api/auth/registro" @{ nome = "Dono"; email = "dono@acai.com"; senha = "senha1234" } | Out-Null
$tokAdmin = (Chamar POST "$api/auth/login" @{ email = "dono@acai.com"; senha = "senha1234" }).corpo.token
Chamar POST "$api/usuarios" @{ nome = "Func"; email = "func@acai.com"; senha = "senha1234" } $tokAdmin | Out-Null
$tokFunc = (Chamar POST "$api/auth/login" @{ email = "func@acai.com"; senha = "senha1234" }).corpo.token
docker cp bd\seed_demo.sql mente-fria-db:/tmp/seed_demo.sql
docker exec mente-fria-db psql -U postgres -d mente_fria -v ON_ERROR_STOP=1 -f /tmp/seed_demo.sql
"exit: $LASTEXITCODE"
```

**Você deve ver** no fim: uma tabela vazia `(0 rows)` (a conferência do seed não achou nenhum erro), depois `COMMIT` e `exit: 0`.

```powershell
(Chamar POST "$api/producao/recomendacao" $null $tokFunc).status
(Chamar POST "$api/producao/recomendacao" $null $tokAdmin).corpo | ConvertTo-Json -Depth 6
```

**Você deve ver** (validado com o Python real):

```
403
{
    "calculado_em":  "2026-10-30T00:00:39",
    "receita_estimada":  1966.80,
    "margem_estimada":  1011.71,
    "recomendacao":  [
                         { "produto_id": 1, "nome": "Açaí Tradicional 300ml", "quantidade": 151, "lucro_por_copo": 6.64 },
                         { "produto_id": 2, "nome": "Açaí Especial 500ml", "quantidade": 1, "lucro_por_copo": 9.07 },
                         { "produto_id": 3, "nome": "Açaí Premium 700ml", "quantidade": 0, "lucro_por_copo": 10.63 }
                     ]
}
```

O Funcionário recebe `403`. O Administrador recebe **151/1/0**, a mesma resposta do teste com o Python real.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `500` na recomendação; no log, `Não foi possível iniciar o Python` | `PYTHON_CMD` errado no `.env` | Parte 6.2 |
| `500`; no log, `ModuleNotFoundError: No module named 'gurobipy'` | O `PYTHON_CMD` aponta para um Python sem o `gurobipy` | Use o `venv` (Parte 6.2) |
| `503` "A recomendação está disponível só na demonstração local da equipe." | `OTIMIZADOR_HABILITADO=false` no `.env` | Apague essa linha ou ponha `true` |

Pare o backend.

**Evidência, commit e PR** (Terminal A, na `<PASTA_DO_PROJETO>`). A evidência desta fase tem duas partes: os 7 testes com o dublê e os 4 + 2 com o Python real.

```powershell
cd backend
$saida = .\mvnw test "-Dtest=Fase4ProducaoTest"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-11-01_lucas_fase4a_mvnw-test.txt
$saida | Select-String "Tests run:|BUILD"
cd ..
```

```powershell
cd backend
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path
$saida = .\mvnw test "-Dtest=ProcessoPythonTest,PythonRealTest#ecoComAcentosPeloPerfilDev+producaoDaDemonstracaoDa151_1_0ComOGurobi"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-11-01_lucas_fase4a_python-real.txt
$saida | Select-String "Tests run:|BUILD"
cd ..
```

```powershell
git add backend docs\Sprint_4\evidencias
git commit -m "test: evidencia dos testes da Fase 4A"
git push
```

Abra o PR (Parte 2.4) e faça o merge no domingo à noite (M3).

### 6.7 Perguntas para a Review

1. Por que o Java chama o Python como um programa separado, e não "importa" o código?
2. Por que o Java recalcula os valores em reais, se o Python já calculou?
3. O que acontece se o Python travar?
4. Por que a recomendação não é `@Transactional`?
5. Para que serve `OTIMIZADOR_HABILITADO`, e por que ele fica `true` no Render (A38)?

---

## Parte 7 — Fase 6: dashboard e marketing (US17, US18), com o Diogo

**Objetivo:** `GET /api/dashboard` (faturamento do mês, mais vendidos, ingredientes, ranking, último plano) e `GET /api/marketing/sugestoes`.
**Quando:** seg 02/11, **de manhã**.
**Branch:** `feature/backend-fase6-dashboard-marketing`.

### 7.1 Pré-requisitos e ordem do dia

- Fases 3 e 4A na `main`.
- **A Fase 5 do Kauê** (o Java da reposição) precisa estar na `main` **antes** de você rodar o `OtimizadorDesligadoTest`. Esse teste chama a rota de reposição.
- **Validado:** sem a Fase 5, 1 dos 3 testes falha (`reposicaoValidaPrimeiroDepoisResponde503`, `expected: 400`).
- **Ordem da manhã de segunda:**
  1. O Kauê faz o merge da Fase 5 logo cedo.
  2. Você copia a sua parte.
  3. Você traz a `main` para a sua branch (Parte 2.6).
  4. Você roda os testes.

### 7.2 Copiar a sua parte

```powershell
git checkout main
git pull
git checkout -b feature/backend-fase6-dashboard-marketing
$r = "docs\Sprint_3\referencia\backend\src"
$d = "backend\src"
New-Item -ItemType Directory -Force "$d\main\java\com\mentefria\backend\dashboard", "$d\main\java\com\mentefria\backend\marketing" | Out-Null
Copy-Item "$r\main\java\com\mentefria\backend\dashboard\DashboardService.java", "$r\main\java\com\mentefria\backend\dashboard\DashboardController.java" "$d\main\java\com\mentefria\backend\dashboard\"
Copy-Item "$r\main\java\com\mentefria\backend\marketing\*.java" "$d\main\java\com\mentefria\backend\marketing\"
Copy-Item "$r\test\java\com\mentefria\backend\DashboardTest.java", "$r\test\java\com\mentefria\backend\MarketingTest.java", "$r\test\java\com\mentefria\backend\OtimizadorDesligadoTest.java" "$d\test\java\com\mentefria\backend\"
git add backend
git commit -m "feat: dashboard do mes e sugestoes de campanha (parte do Lucas)"
git push -u origin feature/backend-fase6-dashboard-marketing
```

`*.java` copia **todos** os arquivos `.java` da pasta `marketing` (os três são seus).

Avise o Diogo: ele põe o `dashboard/DashboardRepository.java`. Depois, rode `git pull`.

| Arquivo | Para quê |
|---|---|
| `DashboardService`, `DashboardController` | O painel do mês |
| `DashboardRepository` (Diogo) | As 4 consultas: faturamento, mais vendidos, ingredientes e ranking |
| `CatalogoIdeias`, `MarketingService`, `MarketingController` | As sugestões de campanha, a partir da última recomendação |
| `DashboardTest`, `MarketingTest`, `OtimizadorDesligadoTest` | 5 + 4 + 3 testes |

### 7.3 Entendendo o código da Fase 6

| Regra | Onde | Decisão |
|---|---|---|
| O período é um **mês** (`?mes=AAAA-MM`); o padrão é o mês atual em São Paulo | `DashboardService.painel` | 14/10 |
| Só pedidos `ATIVO` contam (o cancelado não entra no faturamento) | Todas as consultas do `DashboardRepository` | 14/10 |
| Ranking: a posição é pelo valor; o filtro de valor vale **depois** de somar (`HAVING`) | `DashboardRepository.ranking` | 14/10 |
| Parâmetros opcionais vão com tipo (`Types.INTEGER`), senão o PostgreSQL não sabe o tipo de um `NULL` | `ranking` | — |
| Ingredientes mais usados ordenados pelo **custo** do que foi usado | `ingredientesMaisUtilizados` | A17 |
| O painel mostra a "receita máxima estimada" e os "produtos prioritários" do **último** cálculo (exigência do Termo de Aceite) | `DashboardService` | A16 |
| Marketing: até **3** produtos priorizados (quantidade > 0, maior primeiro); uma ideia por produto, em rodízio pelo dia do ano | `MarketingService`, `CatalogoIdeias` (18 ideias) | A20 |

**Por que o resultado do marketing "some" quando o backend reinicia:** a última recomendação fica **na memória** (`UltimaRecomendacao`), e não no banco. Depois de reiniciar, o painel e o marketing pedem "Gere a recomendação na tela Produção" (A16). É o comportamento combinado.

### 7.4 Trazer a Fase 5 e rodar os testes

```powershell
git fetch origin
git merge --no-edit origin/main
cd backend
.\mvnw test "-Dtest=DashboardTest,MarketingTest,OtimizadorDesligadoTest"
cd ..
```

O `--no-edit` está explicado na Parte 2.6.

**Você deve ver** (validado):

```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.DashboardTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.MarketingTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in com.mentefria.backend.OtimizadorDesligadoTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**O que os testes provam:**
- **`DashboardTest`:** usa um cenário calculado **à mão**, comentado no topo da classe:
  - outubro: 83,40 em 3 pedidos;
  - ranking: Dono 44,70 e Func 38,70;
  - um pedido cancelado que não conta;
  - um pedido às 23h30 de 31/10 que conta em outubro (o relógio de São Paulo).
- **`OtimizadorDesligadoTest`:** liga a chave de reserva (`habilitado=false`) e prova duas coisas: produção e reposição respondem `503` **sem chamar o Python**, e o resto continua funcionando.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `reposicaoValidaPrimeiroDepoisResponde503 … expected: 400` | A Fase 5 do Kauê não está na sua branch | `git fetch origin` e `git merge --no-edit origin/main` (Parte 2.6) |
| `cannot find symbol … DashboardRepository` | O commit do Diogo ainda não chegou | `git pull` |

### 7.5 Conferência manual com os dados de demonstração

Use o banco com o seed da Parte 6.6 (não recrie). Prepare:
1. **Terminal B:** suba o backend **desta branch** (`cd "<PASTA_DO_PROJETO>\backend"` e `.\mvnw spring-boot:run`).
2. **Terminal A:** se for um PowerShell novo, cole a `Chamar` e o `$api` (Parte 3.7). Entre de novo como Administrador:

```powershell
$tokAdmin = (Chamar POST "$api/auth/login" @{ email = "dono@acai.com"; senha = "senha1234" }).corpo.token
```

O backend acabou de subir, então a primeira linha abaixo gera a recomendação de novo: o marketing depende dela.

```powershell
(Chamar POST "$api/producao/recomendacao" $null $tokAdmin).status
$dash = (Chamar GET "$api/dashboard" $null $tokAdmin).corpo
$dash.faturamento | ConvertTo-Json -Compress
$dash.ranking | Select-Object rank, usuario, valor_total | ConvertTo-Json -Compress
$dash.ultimo_plano | ConvertTo-Json -Compress
$m = (Chamar GET "$api/marketing/sugestoes" $null $tokAdmin).corpo
$m.sugestoes | Select-Object produto, quantidade_recomendada, ideia | ConvertTo-Json -Compress
```

**Você deve ver** (validado):

```
200
{"valor_total":326.40,"quantidade_pedidos":3}
[{"rank":1,"usuario":"Func","valor_total":213.00},{"rank":2,"usuario":"Dono","valor_total":113.40}]
{"calculado_em":"...","receita_estimada":1966.80,"margem_estimada":1011.71,"produtos_priorizados":["Açaí Tradicional 300ml","Açaí Especial 500ml"]}
[{"produto":"Açaí Tradicional 300ml","quantidade_recomendada":151,"ideia":"..."},{"produto":"Açaí Especial 500ml","quantidade_recomendada":1,"ideia":"..."}]
```

- **2 sugestões**, porque só dois produtos têm quantidade > 0.
- **As ideias mudam conforme o dia** (o rodízio). O texto não precisa bater.

> O seed põe 3 pedidos no mês **atual** do banco. Se você rodar perto da meia-noite do último dia do mês, os pedidos podem cair no mês seguinte. Nesse caso, use `"$api/dashboard?mes=AAAA-MM"` com o mês certo.

Pare o backend.

**Evidência, commit e PR** (Terminal A, na `<PASTA_DO_PROJETO>`):

```powershell
cd backend
$saida = .\mvnw test "-Dtest=DashboardTest,MarketingTest,OtimizadorDesligadoTest"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-11-02_lucas_fase6_mvnw-test.txt
$saida | Select-String "Tests run:|BUILD"
cd ..
```

```powershell
git add backend docs\Sprint_4\evidencias
git commit -m "test: evidencia dos testes da Fase 6"
git push
```

Abra o PR (Parte 2.4) e faça o merge **antes do deploy da tarde**.

### 7.6 Todos os testes juntos (seg 02/11, à noite)

```powershell
git checkout main
git pull
cd backend
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path
.\mvnw test
cd ..
```

**Você deve ver** (validado, cerca de 40 segundos):

```
[INFO] Tests run: 18 ... SegurancaEErrosTest
[INFO] Tests run: 5 ... DashboardTest
[INFO] Tests run: 15 ... Fase2CrudTest
[INFO] Tests run: 15 ... Fase3EstoquePedidoTest
[INFO] Tests run: 7 ... Fase4ProducaoTest
[INFO] Tests run: 4 ... MarketingTest
[INFO] Tests run: 3 ... OtimizadorDesligadoTest
[INFO] Tests run: 13 ... RegraAcaiTest
[INFO] Tests run: 4 ... ProcessoPythonTest
[INFO] Tests run: 3 ... PythonRealTest
[INFO] Tests run: 8 ... ReposicaoTest
[INFO] Tests run: 95, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Para guardar como evidência (de dentro de `backend`, com o `MF_PYTHON` ainda definido):

```powershell
$saida = .\mvnw test
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-11-02_lucas_suite-completa-95.txt
$saida | Select-String "Tests run: 95|BUILD"
cd ..
```

Você está na `main`, então **não** faça commit agora: esse arquivo entra no PR do README (Parte 10).

### 7.7 Perguntas para a Review

1. Por que o dashboard filtra `status = 'ATIVO'`?
2. O que o pedido das 23h30 de 31/10 prova?
3. Onde fica guardada a última recomendação, e o que acontece quando o backend reinicia?
4. O que o `OtimizadorDesligadoTest` garante?

---

## Parte 8 — Publicação no Render (US20): a sua parte

O passo a passo técnico do Render está no **tutorial do Kauê**, Parte 8 (`docs/Sprint_4/tutoriais/tutorial-kaue-python-deploy-facilitacao.md`). Leia com ele antes de segunda: lá estão os cliques do *blueprint* (8.3), a carga do schema (8.4), o seed (8.5), as conferências (8.6) e o backup (8.7).

**A conta é sua** (decisão A43):
- você faz os cliques no painel, e o Kauê guia;
- as credenciais são **só suas**: não as passe a ninguém.

### 8.1 Sáb 31/10: criar a conta (só a conta)

1. Acesse `https://render.com` → **Get Started**. Crie a conta com o seu e-mail ou com o GitHub.
2. **Não crie** banco nem serviço ainda: o banco gratuito **vence 30 dias depois de criado**. Criando na seg 02/11, ele vale até 02/12, depois da data-limite de 30/11 do professor (A41).
3. **Confira com o Kauê ainda no sábado** se o Render consegue enxergar o repositório `KaueNCarneiro/Mente_Fria`. O repositório é da conta do Kauê. Se o Render pedir autorização no GitHub para acessá-lo, quem autoriza é o dono, o Kauê. **Não verificado aqui:** descubra no sábado, e não na segunda.

### 8.2 Seg 02/11, à tarde: deploy definitivo, com o Kauê

**Pré-requisito:** as Fases 1 a 3 na `main` (o que entrar depois é publicado sozinho), e o PR do Kauê com os arquivos de deploy na raiz.

Durante o deploy:
- **Variáveis no painel:**
  - `DB_*` vêm do banco;
  - `JWT_SECRET` é gerado pelo Render;
  - `CORS_ALLOWED_ORIGIN` é a origem do GitHub Pages do Leonardo, **sem barra no fim**;
  - `OTIMIZADOR_HABILITADO=true`;
  - **nenhuma** variável `GRB_*` (A38).
- **Cada conta só pode ter um banco gratuito:** não crie outro.
- **Depois do deploy e depois de o Kauê carregar o schema no banco do Render** (tutorial dele, parte do deploy; antes disso o banco não tem tabelas), confira **só** o que não cria nada:

  ```powershell
  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
  $api = "https://<seu-servico>.onrender.com/api"
  Chamar GET "$api/saude" | ConvertTo-Json -Depth 6 -Compress
  Chamar GET "$api/ingredientes" | ConvertTo-Json -Depth 6 -Compress
  ```

  A primeira linha obriga o PowerShell 5.1 a usar TLS 1.2, que o Render exige em HTTPS (sem ela, algumas máquinas dão `Could not create SSL/TLS secure channel`). Esperado: `200` com `"banco":"ok"` e `401` em JSON. A primeira chamada pode levar até 1 minuto (o serviço "acorda").
- **O Administrador e o Funcionário do sistema publicado são criados logo depois da carga do schema**, pela API (tutorial do Kauê, Parte 8.5), com **senhas que não estão em nenhum arquivo do repositório**. É logo depois porque o primeiro cadastro em `/api/auth/registro` vira Administrador: não divulgue a URL antes disso. **Nunca** use no Render a senha `senha1234` deste tutorial: o repositório é público, e qualquer pessoa entraria como Administrador.

- **Anote no README:** a URL e a **data de expiração do banco** (02/12, no mesmo horário da criação).

**O que já foi simulado na máquina** (container com 512 MB e 0,1 CPU, como no plano gratuito): subida em 83 s; primeiro login em cerca de 7 s; recomendação em 3,1 s; pico de 133 MiB de memória. O Render de verdade ainda não foi testado. Há folga até qua 04/11 para corrigir.

**O plano gratuito "dorme" depois de 15 minutos sem uso.** Antes de uma demonstração, abra a URL uns 2 minutos antes.

---

## Parte 9 — Suas tarefas de PO

| Quando | O quê | Como decidir |
|---|---|---|
| **Qui 29/10** | Revisar a mensagem ao professor (`docs/Sprint_4/sprint-4-mensagem-professor.md`) antes de o Diogo enviar | Confira datas e fatos com o roteiro da Sprint 4 |
| **Dom 01/11, à noite (M3)** | Decidir o botão "Descartar" (A42) | As Telas 10/16 (estoque) e 11/17 (pedido) funcionam com o backend real? **Não** → o botão sai (o backend dele fica). **Sim** → o Leonardo o faz na segunda |
| **Seg 02/11, de manhã** | *Gate* da US26 (porção padrão, A40) | Entra **só se**, ao mesmo tempo: as Fases 1, 2, 3, 4A e P+Q estão na `main`; o *gate* da Fase 2 foi aprovado; o Diogo está em dia. Quem faz é o Diogo; você revisa os dois PRs dele (ter e qua). Se não estiver na `main` até qua 04/11 às 18h, sai |
| **Seg 02/11** | Confirmar que o deploy vai com o solver ligado (`OTIMIZADOR_HABILITADO=true`) | A38: licença do `pip` autorizada pela Gurobi |
| **Ter 03/11, à noite (M4)** | Se alguma tela obrigatória não estiver na `main`, cortar nesta ordem | 1. botão "Descartar"; 2. filtros do ranking na tela; 3. ranking por usuário; 4. acabamento visual. **Nunca:** o bloco "Plano de produção" do dashboard e o detalhe por linha da Tela 10 |
| **Ter 03 e qua 04/11** | Revisar PRs; assumir a Tela 14 se o Leonardo pedir | — |

**Nenhuma história obrigatória sai.** Se mesmo assim não couber, fale com o professor **antes** de 06/11.

---

## Parte 10 — Entrega (até qui 05/11)

1. **README** (um PR seu, depois da Fase 6):
   - na tabela de variáveis, tire o rótulo "Planejadas": agora todas são lidas pelo código. Das novas, só o `JWT_SECRET` é **obrigatório** (não tem valor padrão); `CORS_ALLOWED_ORIGIN`, `PYTHON_CMD`, `OTIMIZADOR_SCRIPT`, `REPOSICAO_SCRIPT`, `OTIMIZADOR_HABILITADO` (padrão `true`) e `OTIMIZADOR_TIMEOUT_SEGUNDOS` (padrão 10) têm padrão no `application.properties`;
   - inclua no PR a evidência da suíte completa (Parte 7.6);
   - o perfil `dev`;
   - como rodar `.\mvnw test` (Docker aberto; `MF_PYTHON` opcional);
   - o `.env` sem BOM;
   - **a URL publicada e a data de expiração do banco**;
   - apague o bloco "Estado atual (corte da Sprint 2)".
2. **Contrato:** confira que nada no código que você copiou difere de `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`.
3. **Evidências** em `docs/Sprint_4/evidencias/`: uma saída de teste por fase e a da suíte completa.
4. **Contribuições individuais** das Sprints 3 e 4, no padrão de `docs/Sprint_2/sprint-2-contribuicao-lucas.md`.

---

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` (ou `DB_USER`) | Variável faltando no `.env`, `.env` com BOM, ou backend rodado fora de `backend` | Parte 1.3; rode de dentro de `backend` |
| `JWT_SECRET precisa ter pelo menos 32 caracteres.` ao subir | A chave é curta | Gere outra (Parte 1.3) |
| `password authentication failed for user "postgres"` | `DB_PASSWORD` diferente da senha do container | Confira a senha |
| `Connection to localhost:5432 refused` | O `mente-fria-db` está parado | `docker start mente-fria-db` |
| `Port 8080 was already in use` | Ficou um backend rodando em outra janela | Feche-o com Ctrl+C naquela janela |
| `Could not find a valid Docker environment` | Docker fechado ou travado | Parte 1.2 |
| `package org.postgresql.util does not exist` | `pom.xml` sem o diff | Parte 3.2 |
| `Ambiguous mapping` ao subir os testes | `RotasDeTesteController` sem `@Profile` | Copie de novo |
| Testes de integração com "0 testes" | Classe terminando em `IT` | Os nomes da referência terminam em `Test` |
| `relation "movimentacao_estoque" does not exist` ao rodar o backend | O banco `mente_fria` ainda é o da Sprint 1 | Parte 4.8 |
| Acentos trocados (`AÃ§aÃ­`) nas suas chamadas | Você usou `Invoke-RestMethod` direto | Use a função `Chamar` |
| `blocked by CORS policy` no navegador (mas funciona aqui) | `localhost` × `127.0.0.1`, ou origem errada no Render | Confira `CORS_ALLOWED_ORIGIN` |
| `500` na recomendação | Python ou `gurobipy` errado | Parte 6.2 |
| `git pull` diz `Your local changes … would be overwritten` | Você tem alterações sem commit | Faça o commit (Parte 2.2) e repita |

---

## Colinha

```powershell
# começar uma fase
git checkout main; git pull; git checkout -b feature/backend-<fase>
# testes de uma classe (de dentro de backend)
.\mvnw test "-Dtest=NomeDaClasse"
# todos os testes, com o Python real
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path; .\mvnw test
# subir o backend (Terminal B, de dentro de backend); parar: Ctrl+C e S
.\mvnw spring-boot:run
# trazer a main nova para a sua branch
git fetch origin; git merge --no-edit origin/main
# guardar o trabalho e enviar
git add backend; git commit -m "feat: ..."; git push
```

---

## Checklist final

- [ ] Fase 1: `pom.xml` com o diff; 6 classes; **18/18**; PR na `main` (M1)
- [ ] Fase 2: com os repositórios do Diogo; **46** testes; as 10 conferências; o Leonardo avisado; PR na `main` (M2)
- [ ] Fase 3: com os repositórios do Diogo; **15/15**; as conferências do estoque e do pedido; PR na `main`
- [ ] Fase 4A: **7/7**; com o Python real, 4 + 2; eco `AÇAÍ (POLPA)`; **151/1/0** com o seed; PR na `main` (M3)
- [ ] Fase 6: depois da Fase 5 do Kauê, **5 + 4 + 3**; dashboard 326,40; 2 sugestões; PR na `main`
- [ ] Suíte completa: **95 testes, 0 falhas**, com o Python real
- [ ] Conta do Render criada no sábado; deploy definitivo na segunda; URL e expiração do banco no README
- [ ] Decisões de PO: botão "Descartar" (dom), *gate* da US26 (seg), cortes (ter, se preciso)
- [ ] Evidências e contribuições individuais

## Fora do escopo obrigatório

Conforme as decisões A2b e A40:
- **US26** (porção padrão por unidade) só entra se passar no *gate* de seg 02/11. Quem a faz é o **Diogo**; você revisa os PRs.
- **Ficam fora do semestre:**
  - cancelamento de pedido (US27; ele **não devolveria** estoque);
  - os alertas completos (US15 e US16);
  - o CSV do dashboard (US19);
  - o cadastro de clientes (US13).
