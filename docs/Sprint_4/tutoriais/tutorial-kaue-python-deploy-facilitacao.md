# Tutorial — Sprint 4 — Otimização (Python/Gurobi), reposição, deploy e facilitação — Kauê

**Para:** Kauê Nogueira Carneiro · **Papel:** Scrum Master; módulos de otimização (Python/Gurobi); reposição (US14); publicação no Render (US20); dono do repositório no GitHub · **Data:** 30/10/2026 (decisão A44)
**Substitui:** `docs/Sprint_3/sprint-3-tutorial-python-kaue.md`, que fica como registro.
**Calendário em vigor:** `docs/Sprint_4/sprint-4-roteiro.md`, §4. Se uma data daqui divergir, vale o roteiro.

---

## O que foi verificado e o que não foi

Os comandos deste tutorial foram **executados de verdade** em 30/10/2026, num **clone temporário** do repositório. Partimos da `main` de 28/10, commit `af05b26`. O ambiente:
- Windows PowerShell 5.1, inclusive numa **janela nova, com os padrões de fábrica do Windows** (sem perfil nem ajustes);
- Python 3.14.0, com `gurobipy` 13.0.3 e a licença limitada que vem com ele pelo `pip`;
- JDK 25 e Maven do projeto (`mvnw`);
- Docker 29.8.0, com containers **temporários**: um PostgreSQL que **só aceita SSL** fez o papel do banco do Render. O `mente-fria-db` não foi tocado;
- um "GitHub de mentira" (um repositório na própria máquina) para o push e o fluxo a dois.

Para a Fase 5 e o deploy, a `main` foi montada como ela deve estar depois do M3 (dom 01/11): o PR do banco, as Fases 1 a 4A do Lucas e a sua Fase P + Q.

| O quê | Resultado real |
|---|---|
| Ambiente Python do zero (`venv` + `pip install`) | `gurobipy` **(13, 0, 3)** e `pytest` 9.1.1, em cerca de 45 s |
| Testes da `main` antes de tudo (o "baseline") | **`8 passed`** |
| `requirements.txt` de UTF-16 para UTF-8 | Arquivo convertido; o `pip` aceita |
| Fase P: testes antes do código ("vermelho") | **17 failed, 8 passed** |
| Fase P: com o `otimizador.py` novo ("verde") | **`25 passed`** |
| Fase P: quebrar de propósito (2 experimentos) | **5 failed** e **5 failed** |
| Fase Q: testes antes do código | **Erro de coleta** (`ModuleNotFoundError: No module named 'reposicao'`), e não "falhas" |
| Fase Q: com o `reposicao.py` | **`47 passed`** |
| Fase Q: quebrar de propósito (3 experimentos) | **2**, **1** e **9 failed** |
| Rodar "como o Java" (5 exemplos) | As 4 saídas de produção **iguais** às guardadas; reposição 0,74 kg de morango (R$ 11,10); JSON inválido → erro tratado com código de saída 0 |
| Tempo de uma recomendação (5 vezes) | 168 a 224 ms (média 190 ms) |
| Fase 5: cópia, fluxo a dois com o Diogo, compilação | Compila com o repositório do Diogo; sem ele, `cannot find symbol` |
| Fase 5: testes | `ReposicaoTest` **8**; `PythonRealTest` **3**, com o Gurobi real; a suíte inteira da branch, **83**. Todos sem falhas |
| Imagem do deploy (`docker build`) | Montada em 78 s (com as imagens-base já baixadas); 584 MB |
| A imagem com os limites do Render (512 MB, 0,1 CPU) | Subiu em 55 s; `saude` ok; eco `401`; memória entre 115 e 127 MiB; sem `OOMKilled` |
| Ponta a ponta dentro da imagem | Recomendação **151/1/0** em 2,7 s; reposição com 0,74 kg de morango (R$ 11,10) em 2,4 s |
| CT69 (CORS) e CT72 (chave de reserva) na imagem | `200` para a origem do Pages e `403` para outra; com a chave desligada, `503` com a mensagem, e o resto `200` |
| Schema e seed pela URL, num banco que **exige SSL** | Sem SSL, recusado; com `?sslmode=require`, DDL e seed carregados, com os acentos |
| Backup e restauração | Cópia numa pasta fora do repositório (22 KB); restaurada num banco descartável e num banco novo de **outro dono**: 6 pedidos nos dois, e o próximo cadastro sem colisão de `id` |

**Achados novos (corrigem o tutorial antigo):**
- No PowerShell com os padrões de fábrica, o **pipe** (`Get-Content … | python …`) troca os acentos por `?`: o eco devolve `A?A? (POLPA)`. Por isso este tutorial manda os arquivos ao Python com **`cmd /c "… < arquivo"`**, que entrega os bytes exatos, como o Java faz (Parte 3.8).
- Na fase Q, antes de copiar o módulo, o `pytest` **não chega a rodar os testes**: ele para com um erro de coleta. O tutorial antigo dizia "falhas".
- `"$URL?sslmode=require"` não funciona no PowerShell: o `?` vira parte do nome da variável. Use `$URL + "?sslmode=require"` (Parte 8).
- Logo depois de criar um container do PostgreSQL, o banco leva alguns segundos para aceitar conexões; um comando imediato falha com `connection to server on socket … failed`. O tutorial espera 10 s.
- `docker rm -f` remove o container, mas deixa para trás um **volume** com os dados do PostgreSQL. Para containers temporários, use `docker rm -f -v` (Partes 8.2 e 8.7).

> Durante a validação, o Docker Desktop travou ao abrir (o travamento A26). Foi destravado com o contorno da Parte 1.2, **sem** "Reset to factory defaults", e o `mente-fria-db` continuou intacto.

**Não foi possível verificar aqui:**
- o push e o PR no GitHub de verdade;
- o Render de verdade (depende da conta do Lucas). O banco que exige SSL e a imagem com 512 MB e 0,1 CPU são simulações dele;
- o GitHub Pages e a autorização do Render no GitHub (dependem das contas do Leonardo, do Lucas e da sua);
- a "segunda máquina" (é outro integrante que roda).

**O que muda na sua tela:** caminhos, datas e tempos serão diferentes. O resto deve bater.

> **Se alguma saída não bater:** pare, copie a mensagem **exata** e mande no grupo. Não tente adivinhar.

---

## Como usar este tutorial

### Qual terminal usar

Todos os comandos são para o **Windows PowerShell**. Para abrir:
- **Fora da IDE:** Menu Iniciar → digite `PowerShell` → **Windows PowerShell**.
- **No VS Code:** *Terminal* → *New Terminal*. Confira, no canto direito do painel do terminal, que o tipo é **powershell**.
- **No IntelliJ:** *View* → *Tool Windows* → *Terminal*.

**Como saber se você está no PowerShell:** a linha começa com **`PS`**, por exemplo `PS C:\Users\kaue>`.

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
cd "C:\Users\kaue\Documents\Mente_Fria"
dir
```

**Você deve ver** as pastas `backend`, `bd`, `docs`, `frontend` e `optimization`, e os arquivos `.gitignore` e `README.md`.

Neste tutorial, **`<PASTA_DO_PROJETO>`** significa esse caminho, o seu.

### Acentos na tela: um comando por janela

Toda vez que abrir um PowerShell para este tutorial, rode **primeiro**:

```powershell
[Console]::OutputEncoding = [Text.Encoding]::UTF8
```

**O que faz:** diz ao PowerShell que o texto que os programas (Python, Docker, `psql`) escrevem está em UTF-8.
- **Por quê:** o padrão do Windows em português é outro (a "página de código 850"). Sem este comando, `AÇAÍ` aparece como `A├çA├ì` (verificado). O dado está certo; só a tela erra.
- Vale **só para aquela janela**. Fechou, precisa rodar de novo.

> ⚠️ **Não mude o `$OutputEncoding`** (outra variável, parecida). Verificado: com ele em UTF-8, o PowerShell manda **dois** BOMs ao Python, e o módulo responde "JSON de entrada inválido".

### O Python do projeto, sem "ativar" nada

O projeto tem um ambiente Python próprio na pasta `optimization\venv` (Parte 1.4). Em vez de "ativar" o ambiente, este tutorial chama **o `python.exe` de dentro dele** pelo caminho:

```powershell
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

Assim não importa se o Windows bloqueia scripts (o `Activate.ps1` às vezes é bloqueado pela política de execução) e não há dúvida sobre qual Python está rodando. Se você prefere ativar, pode, mas os comandos daqui funcionam sem isso.

### Dois terminais

Em algumas partes você vai usar **dois PowerShells abertos ao mesmo tempo**:
- **Terminal A:** para comandos (Git, testes, Docker, chamadas à API).
- **Terminal B:** só para deixar algo rodando (o backend, ou o log de um container).

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
| `<SENHA_DO_BANCO>` | A senha do PostgreSQL do container `mente-fria-db` (a do `POSTGRES_PASSWORD` da Sprint 1). Se o seu `backend\.env` já existe, é o `DB_PASSWORD` dele |
| `<URL_DO_SERVICO>` | O endereço do backend no Render, por exemplo `https://mente-fria-backend-xxxx.onrender.com` (aparece no painel depois do deploy) |
| `<ORIGEM_DO_PAGES>` | O endereço do GitHub Pages, **sem barra no fim e sem caminho**: `https://kauencarneiro.github.io` (confirme com o Leonardo) |

---

## Plano de tempo e "pronto quando"

| Dia | O que você faz | Parte |
|---|---|---|
| **Qui 29/10** (atrasado: faça primeiro, hoje) | Fase P: módulo de produção por lucro; `requirements.txt` em UTF-8. **Encaminhar ao professor o e-mail da Gurobi** (A38) | 3 e 5 |
| **Sex 30/10** | Fase Q (reposição) e **PR da Fase P + Q** (`47 passed`) à noite (marco M2); 2ª máquina; **PR do `.env.example`** (G8); ajudar o Leonardo no `config.js` e no *workflow* do Pages e **habilitar o Pages** (dono do repositório) | 4, 8 e 9 |
| **Sáb 31/10** | **PR do deploy** (`Dockerfile`, `.dockerignore`, `render.yaml`) e teste da imagem na sua máquina; **começar a Fase 5** (cópia, commit e push; o Diogo põe o repositório); registro da perda do histórico do Git; **testar com o Lucas se o Render enxerga o repositório** e combinar como os backups vão ser feitos | 7, 8 e 9 |
| **Dom 01/11** | **Fase 4A em dupla com o Lucas** (você cuida do contrato e da prova). À noite, depois do merge da 4A (M3): trazer a `main` para a branch da Fase 5 e rodar os testes | 6 e 7 |
| **Seg 02/11** (feriado, dia de mais carga) | **Cedo:** PR da Fase 5 (8/8), porque a Fase 6 do Lucas depende dela. **Tarde:** deploy definitivo **na conta do Lucas**, com você guiando; schema, seed e **backup**. Aprovar a seção da US26 no contrato | 7, 8 e 9 |
| **Ter 03/11** | CT67 (imagem local), CT69, CT70 e CT72 no ambiente publicado (M4 à noite) | 8 |
| **Qua 04/11** | CT20 (subir do zero só com o README, com um colega). **18h: congelamento.** Depois das 18h: CT68 completo na URL publicada | 8 e 9 |
| **Qui 05/11** | Relatório da Sprint 4 e revisão do da Sprint 3; **Retrospectiva** (Sprints 2, 3 e 4); ensaio da apresentação; backup | 9 e 10 |
| **Sex 06/11** | **Entrega das Sprints 3 e 4 e Review**, com demonstração nos dois perfis | 10 |
| **Até 13/11** | Semana de bugs no ambiente publicado (sem histórias novas) | 9 |

**Pronto quando:**
- [ ] o PR da Fase P + Q está na `main`, com `47 passed` na sua máquina **e** na de outro integrante;
- [ ] o PR do `.env.example` (G8) e o PR do deploy estão na `main`;
- [ ] o PR da Fase 5 está na `main`, com `ReposicaoTest` 8/8 e o `PythonRealTest` com o Python real;
- [ ] o sistema está publicado (conta do Lucas), com o schema e o seed carregados, o **backup feito e a restauração testada**, e a URL e as datas do banco no README;
- [ ] CT67, CT69, CT70, CT72, CT68 e CT20 registrados em `docs/Sprint_4/evidencias/`;
- [ ] Retrospectiva, Review e os dois relatórios registrados;
- [ ] você sabe responder as "Perguntas para a Review" do fim de cada parte.

---

## Parte 0 — Conceitos em 5 minutos

### 0.1 Glossário

| Termo | O que é (sem jargão) | No nosso projeto |
|---|---|---|
| **Otimização** | Escolher, entre todas as opções possíveis, a melhor segundo um critério | "Quantos copos de cada açaí produzir para ter o **maior lucro** com o estoque que existe" |
| **Programação Linear Inteira** | Um jeito de escrever o problema com contas simples (somas e multiplicações) e **respostas inteiras** | Não existe "66,4 copos": a resposta é 66 |
| **Solver / Gurobi** | O programa que resolve esse tipo de problema. O `gurobipy` é a biblioteca que deixa o Python falar com ele | `optimization/app/otimizador.py` e `reposicao.py` |
| **Licença do `pip`** | A licença limitada que vem junto do `gurobipy` instalado pelo `pip`. Resolve problemas pequenos (até 2.000 variáveis) | Os nossos têm 3 produtos e até 15 ingredientes. É a licença dos testes e do Render (A38) |
| **WLS** | A sua licença acadêmica **pessoal** do Gurobi (até 25/09/2027) | Só no seu computador. **Nunca** no Render nem no Git (A34, A38) |
| **Ambiente virtual (`venv`)** | Uma pasta com um Python separado só para o projeto, com as bibliotecas certas | `optimization\venv`. Fica fora do Git |
| **`pip` / `requirements.txt`** | O instalador de bibliotecas do Python e a lista do que instalar, com as versões | `gurobipy==13.0.3` (decisão P2), `pytest` |
| **`pytest`** | A ferramenta que roda os testes em Python | `optimization\tests` |
| **Vermelho → verde** | Primeiro os testes, que **falham** (vermelho) porque o código ainda não existe; depois o código, e eles passam (verde). Prova que os testes testam alguma coisa | Partes 3 e 4 |
| **Oráculo** | Um segundo jeito de calcular a resposta, **sem solver** (testando todas as combinações), para conferir o otimizador | `oraculo.py`, `oraculo_reposicao.py` |
| **`stdin` / `stdout`** | A "entrada" e a "saída" de texto de um programa. É como o Java conversa com o Python: escreve o JSON na entrada e lê o JSON da saída | Contrato oficial, §1 |
| **Código de saída** | Um número que o programa devolve ao terminar: `0` = deu certo | `0` mesmo com `status: "erro"` (erro de dado); diferente de `0` = falha técnica |
| **UTF-8, UTF-16, BOM** | Jeitos de gravar letras em bytes. O BOM é uma marca invisível no começo do arquivo | O pipe do PowerShell põe um BOM; o módulo aceita (A8) |
| **`Decimal` × `float`** | `float` erra contas com centavos (`0.1 + 0.2` dá `0.30000000000000004`). `Decimal` é exato | O JSON é lido direto em `Decimal`, e as contas são feitas em inteiros |
| **Modo eco** | Um "alô" entre o Java e o Python: o Python devolve o texto em maiúsculas | `{"echo": "Açaí (polpa)"}` → `"AÇAÍ (POLPA)"` |
| **Imagem / container (Docker)** | **Imagem:** um "pacote" pronto com sistema, Java, Python e o nosso código. **Container:** a imagem rodando | A imagem `mente-fria-backend`, que o Render roda |
| **`Dockerfile` / `.dockerignore`** | A "receita" da imagem e a lista do que pode entrar nela | Na raiz do repositório (Parte 8) |
| **Render** | O serviço que publica o backend e o banco na internet | Conta do Lucas (A43); plano gratuito |
| ***Blueprint* (`render.yaml`)** | Um arquivo que descreve tudo o que o Render deve criar, para não configurar à mão | Um serviço web (Docker) e um PostgreSQL 16 |
| **External Database URL** | O endereço completo do banco do Render, **com usuário e senha** | Só para carregar o schema e fazer backup. **Nunca** no Git |
| **`pg_dump` / `pg_restore`** | As ferramentas do PostgreSQL que copiam o banco para um arquivo e recriam o banco a partir dele | O backup que o professor pediu (A32) |
| **GitHub Pages** | O GitHub publicando arquivos estáticos (HTML, CSS, JS) | O frontend do Leonardo |
| **CORS** | A regra do navegador que só deixa uma página falar com uma API se a API permitir aquela origem | No Render, a origem permitida é a do Pages |
| ***Health check*** | Uma rota que o Render chama para saber se o serviço está vivo | `GET /api/saude` |
| **Branch / PR / merge** | **Branch:** uma "cópia de trabalho" do código. **PR:** o pedido para juntar a branch na `main`. **Merge:** a junção | Um PR por entrega |

### 0.2 Como as peças se encaixam

```
 Navegador ---> GitHub Pages (frontend do Leonardo)
     |
     |  HTTPS + JSON + token
     v
 +---------------------------- Render (conta do Lucas) -----------------------------+
 |  Container "mente-fria-backend" (imagem do Dockerfile)                            |
 |                                                                                   |
 |   Java (Spring Boot) --stdin: JSON--> python3 otimizador.py / reposicao.py        |
 |        ^             <--stdout: JSON--        (gurobipy + licença do pip)         |
 |        |                                                                          |
 |        +--- SQL ---> PostgreSQL 16 do Render ("mente-fria-db" no painel)          |
 +-----------------------------------------------------------------------------------+

 Na sua máquina: o mesmo Python, em optimization\venv, roda os testes (pytest)
 e é o que o Java chama quando você sobe o backend localmente.
```

**A ideia que você vai defender na Review:**
- O Python **só calcula**: não acessa banco nem rede. Recebe um JSON, devolve um JSON.
- O Java cuida do resto: busca os dados, valida, chama o Python, confere a resposta e grava.
- Cada lado é testado sozinho (com o oráculo e com dublês) e os dois juntos (`PythonRealTest`).

> O banco do Render se chama `mente-fria-db` **no painel do Render**, o mesmo nome do container da sua máquina. São coisas diferentes: um está na internet, o outro no seu Docker.

### 0.3 Os códigos que aparecem no projeto

| Código | O que significa | Onde está |
|---|---|---|
| **US***n* (ex.: US14) | História de usuário do Backlog | `docs/Backlog_Priorizado_Mente_Fria.docx` |
| **CT***n* (ex.: CT67) | Caso de teste do Plano de Testes | `docs/Plano_de_Testes_Mente_Fria.md` |
| **A***n*, **D***n*, **F***n* | Decisões registradas da equipe (A = auditoria, D = respostas de 13/10, F = análise de 14/10) | A1–A38 em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`; A39 em diante em `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md` |
| **G***n* (ex.: G8) | Pendências do grupo | `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`, §2 |
| **P2** | A decisão da versão do `gurobipy`: **13.0.3** (27/10) | `optimization/requirements.txt` |
| **M1, M2, M3, M4** | Marcos do calendário (o que precisa estar na `main` em cada noite) | Roteiro da Sprint 4, §4 |
| **Fase P / Fase Q** | Os seus módulos Python: produção por lucro (P) e reposição (Q) | Partes 3 e 4 |
| **Fase 4A / Fase 5** | O Java da produção (do Lucas, em dupla com você) e o Java da reposição (seu) | Partes 6 e 7 |
| **Plano B1** | A "chave de reserva": `OTIMIZADOR_HABILITADO=false` faz produção e reposição responderem `503` sem chamar o Python. **Reserva** desde a A38 | Parte 8 |
| **A26** | O travamento do Docker Desktop com contorno conhecido. **Nunca** "Reset to factory defaults" | Parte 1.2 |
| **Tela** *n* (ex.: Tela 13) | O número de cada tela no protótipo navegável | `docs/Roteiro do Protótipo Navegável — Mente Fria.md` |
| **Consulta 9** | O SQL do Diogo que soma o consumo de cada ingrediente nos pedidos do período | `ReposicaoRepository` (Parte 7) |

---

## Parte 1 — Preparar o computador (uma vez)

### 1.1 Conferir as ferramentas

**Objetivo:** ter certeza de que Git, Python, Java e Docker estão instalados e no PATH.
**Onde:** Terminal A, em qualquer pasta.

```powershell
git --version
py --version
java -version
docker --version
```

**O que cada parte faz:** cada comando pergunta a versão de uma ferramenta. Se a ferramenta não estiver instalada, o PowerShell avisa "não é reconhecido como nome de cmdlet".

**Você deve ver** (os números podem ser um pouco diferentes):

```
git version 2.53.0.windows.1
Python 3.14.0
java version "25.0.2" 2026-01-20 LTS
...
Docker version 29.8.0, build 88096ef
```

| Se der errado | O que fazer |
|---|---|
| `'py' não é reconhecido…` ou Python abaixo de 3.14 | Instale o Python 3.14 pelo site oficial (python.org), marcando "Add python.exe to PATH", e abra um PowerShell novo |
| `java version "17..."` ou `"21..."` | O backend usa **Java 25** (você precisa dele na Fase 5). Instale o JDK 25 e confira de novo, num PowerShell **novo** |

### 1.2 Docker e o banco de desenvolvimento

**Objetivo:** o Docker Desktop aberto e o container do banco, `mente-fria-db`, rodando.
**Por quê:** os testes da Fase 5 sobem um PostgreSQL próprio no Docker; o teste da imagem e o backup também usam o Docker.

1. Abra o **Docker Desktop**, pelo Menu Iniciar. Espere aparecer **"Engine running"** no canto inferior esquerdo.
2. No Terminal A:

```powershell
docker ps -a --filter name=mente-fria-db
```

**O que faz:** lista os containers, inclusive os parados (`-a`), filtrando pelo nome.

**Você deve ver** uma linha com `mente-fria-db`. Se a coluna STATUS disser `Exited`, ligue o container com `docker start mente-fria-db`.

| Se der errado | O que fazer |
|---|---|
| `error during connect` / `failed to connect to the docker API` | O Docker Desktop não está aberto ou ainda está iniciando. Espere "Engine running" |
| A lista vem **vazia** (não existe `mente-fria-db`) | O container da Sprint 1 não existe nesta máquina. Pare e peça ajuda ao Diogo: ele cria o container com você |
| O Docker Desktop abre com uma janela de erro *"starting services: … rename … .sock … The file cannot be accessed by the system"*, com os botões **Quit** e **Reset to factory defaults** (A26) | 1. **Nunca** clique em **"Reset to factory defaults"**: isso apaga o `mente-fria-db`. Clique em **Quit**. 2. No PowerShell: `wsl --shutdown`. 3. No Explorador, renomeie as pastas `%LOCALAPPDATA%\Docker\run` e `%LOCALAPPDATA%\docker-secrets-engine` (por exemplo, para `run.antiga`). Apagar não funciona; renomear, sim. 4. Abra o Docker Desktop de novo |

> ⚠️ **Regra da equipe:** nunca apague, recrie ou resete o **container** `mente-fria-db`. Os testes e as validações deste tutorial usam containers **temporários**, com outros nomes.

### 1.3 O terminal com acentos

Rode o comando da seção "Acentos na tela" (`[Console]::OutputEncoding = [Text.Encoding]::UTF8`) em **cada** PowerShell que abrir. Ele é o primeiro de todos os blocos deste tutorial.

### 1.4 O ambiente Python (`optimization\venv`)

**Objetivo:** um Python só do projeto, com o `gurobipy` 13.0.3 e o `pytest`.
**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

**Pasta `venv`, e não `.venv` (G7, A44):** é o nome que o `.gitignore` e o README já usam, e o que todos os tutoriais da Sprint 4 seguem.

**Primeiro, veja se você já tem um:**

```powershell
Test-Path optimization\venv
```

- **`False`:** crie (bloco abaixo).
- **`True`:** confira a versão do `gurobipy` com o último comando do bloco abaixo. Se aparecer `(13, 0, 3)`, pule a criação. Se aparecer outra versão ou erro, apague e crie de novo: `Remove-Item -Recurse -Force optimization\venv` e depois o bloco.

```powershell
py -m venv optimization\venv
optimization\venv\Scripts\python.exe -m pip install -r optimization\requirements.txt
optimization\venv\Scripts\python.exe -c "import gurobipy, pytest; print(gurobipy.gurobi.version(), pytest.__version__)"
```

| Comando | O que faz |
|---|---|
| `py -m venv optimization\venv` | Cria o ambiente virtual na pasta `optimization\venv` (cerca de 20 s) |
| `…\python.exe -m pip install -r …\requirements.txt` | Instala **dentro** do ambiente as bibliotecas da lista, nas versões fixadas |
| `…\python.exe -c "import …"` | Confere que as duas bibliotecas principais estão lá e mostra as versões |

**Você deve ver** (validado, cerca de 25 s no `pip`):

```
Successfully installed Pygments-2.21.0 colorama-0.4.6 gurobipy-13.0.3 iniconfig-2.3.0 packaging-26.3 pluggy-1.6.0 pytest-9.1.1

[notice] A new release of pip is available: 25.2 -> 26.2.2
...
(13, 0, 3) 9.1.1
```

> O aviso `[notice] A new release of pip…` (às vezes em vermelho) **não é erro**. O que importa é a última linha.

**Conferir que o `venv` está fora do Git:**

```powershell
git status --short
```

**Você deve ver:** nenhuma linha com `venv`. O `.gitignore` já esconde a pasta.

### 1.5 O `backend\.env` (para subir o backend na sua máquina)

Você só precisa dele para subir o backend (Fase 4A, na dupla com o Lucas) e para o CT20.
- Se o arquivo ainda não existe, siga a **Parte 1.3 do tutorial do Lucas** (`docs/Sprint_4/tutoriais/tutorial-lucas-backend.md`): ela explica o `DB_PASSWORD`, a geração do `JWT_SECRET` e o cuidado com o BOM.
- Depois do seu PR do G8 (Parte 9.2), o arquivo de exemplo é o `backend\.env.example`.

Para o backend chamar o **seu** Python, deixe estas linhas no `backend\.env` (`notepad backend\.env`):

```
PYTHON_CMD=../optimization/venv/Scripts/python.exe
OTIMIZADOR_SCRIPT=../optimization/app/otimizador.py
REPOSICAO_SCRIPT=../optimization/app/reposicao.py
```

**Por que esses caminhos:**
- `../` porque o backend roda **de dentro** de `backend`, e o caminho começa de lá.
- Barras `/`, e não `\`, porque o `.env` é lido como `.properties`, onde `\` é caractere especial.

**Antes de avançar:**
- [ ] `py --version` mostra 3.14 e `java -version` mostra 25;
- [ ] `docker ps` mostra `mente-fria-db` com `Up`;
- [ ] `(13, 0, 3) 9.1.1` no `venv`;
- [ ] `git status` não mostra o `venv` nem o `.env`.

---

## Parte 2 — Git e PR: o caminho de toda entrega

Você vai repetir este caminho em cada PR: Fase P + Q, `.env.example`, deploy e Fase 5. Leia uma vez com calma.

### 2.1 Começar: branch nova a partir da `main` atualizada

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
git status
git checkout main
git pull
git checkout -b feature/python-lucro-e-reposicao
```

| Comando | O que faz |
|---|---|
| `git status` | Mostra se há arquivos alterados sem commit. Antes de trocar de branch, o certo é **nada** |
| `git checkout main` | Vai para a branch `main` (o código oficial) |
| `git pull` | Traz do GitHub o que os colegas já juntaram na `main` |
| `git checkout -b <nome>` | Cria uma branch nova **a partir de onde você está** (a `main` atualizada) e já entra nela |

**Você deve ver** no fim: `Switched to a new branch 'feature/python-lucro-e-reposicao'`.

| Se der errado | O que fazer |
|---|---|
| `git status` lista arquivos `modified` ou `??` | Você tem trabalho sem commit. Se for de outra entrega, faça o commit nela antes de trocar. Se não sabe o que é, pare e pergunte no grupo |
| `fatal: a branch named '…' already exists` | A branch já existe: use `git checkout <nome>`, sem o `-b` |

**Os nomes das suas branches:**

| Entrega | Branch |
|---|---|
| Fase P + Q | `feature/python-lucro-e-reposicao` |
| `.env.example` (G8) | `chore/env-example-unico` |
| Arquivos do deploy | `chore/deploy-render` |
| Fase 5 | `feature/backend-reposicao` |

### 2.2 Commit

```powershell
git status --short
git add optimization
git status --short
git commit -m "feat: otimizacao por lucro em dois estagios, reposicao com orcamento e exemplos"
```

| Comando | O que faz |
|---|---|
| `git status --short` | Lista o que mudou: `M` = modificado, `A` = novo já marcado, `??` = novo que o Git ainda não conhece |
| `git add optimization` | Marca **só a pasta** da entrega para entrar no commit (em cada parte, o comando diz qual pasta). Assim nada de outras pastas entra por engano |
| `git commit -m "…"` | Cria o commit. Padrão da equipe: *Conventional Commits* (`feat:`, `fix:`, `test:`, `docs:`, `chore:`), **sem acentos** na mensagem |

**Nunca adicione:** o `optimization\venv`, o `backend\.env`, arquivos de backup do banco, nem a External Database URL em nenhum arquivo.

### 2.3 Enviar para o GitHub

```powershell
git push -u origin feature/python-lucro-e-reposicao
```

**O que faz:** envia a branch para o GitHub (`origin`). O `-u` "liga" a branch local à remota; depois disso, `git push` e `git pull` sozinhos já sabem para onde ir.

**Você deve ver** no fim (validado):

```
 * [new branch]      feature/python-lucro-e-reposicao -> feature/python-lucro-e-reposicao
branch 'feature/python-lucro-e-reposicao' set up to track 'origin/feature/python-lucro-e-reposicao'.
```

> **Mensagens em vermelho do Git:** no PowerShell, o Git escreve o progresso numa saída que às vezes aparece em vermelho. Se a última linha for a esperada, deu certo.

### 2.4 Abrir o PR (no navegador)

**Antes de abrir o PR,** rode **todos** os testes da parte que você mexeu (o `pytest` inteiro na Fase P + Q; o `.\mvnw test` inteiro na Fase 5), e não só os novos.

1. Abra `https://github.com/KaueNCarneiro/Mente_Fria`.
2. Clique em **"Compare & pull request"**, na faixa amarela com o nome da sua branch.
   - Se a faixa não aparecer: aba **Pull requests** → **New pull request** → em **compare**, escolha a sua branch.
3. Confira no topo: **base: `main`** ← **compare: `<sua branch>`**.
4. **Título:** igual à mensagem do commit.
5. **Descrição:** use o modelo de cada parte.
6. Em **Reviewers**, escolha quem cada parte indica (regra da equipe: revisão obrigatória).
7. Clique em **Create pull request**.

### 2.5 Depois da aprovação: merge e limpeza

No GitHub: **Merge pull request** → **Confirm merge** → **Delete branch**. Depois, no Terminal A:

```powershell
git checkout main
git pull
git branch -d feature/python-lucro-e-reposicao
```

**O que faz:** volta para a `main`, traz o merge e apaga a branch local, que já foi juntada.

### 2.6 Trazer a `main` nova para dentro da sua branch

Na Fase 5 você vai precisar disto: a sua branch nasce no sábado, mas só compila depois que a Fase 4A entra na `main`, no domingo. Sem sair da sua branch:

```powershell
git fetch origin
git merge --no-edit origin/main
```

| Comando | O que faz |
|---|---|
| `git fetch origin` | Baixa as novidades do GitHub **sem mexer** nos seus arquivos |
| `git merge --no-edit origin/main` | Junta a `main` do GitHub na branch em que você está. O `--no-edit` aceita a mensagem padrão do "commit de junção" sem abrir um editor de texto (no Git do Windows, o padrão é o Vim) |

**Se aparecer `CONFLICT`:** os dois lados mexeram na mesma linha. Não force nada: pare e chame o colega que fez a outra mudança.

### 2.7 Fluxo a dois: o Diogo entra na sua branch da Fase 5

**Por quê:** o `ReposicaoRepository` (o SQL da consulta 9) é do Diogo. Pela decisão A44, cada um faz commit dos **seus** arquivos, na **mesma branch** da fase. O PR leva os commits dos dois.

| Passo | Quem | Comandos |
|---|---|---|
| 1. Criar a branch, copiar a sua parte, commit e push | **Você** | Parte 7.2 |
| 2. Avisar | **Você** | "Diogo, a branch `feature/backend-reposicao` está no GitHub. Pode pôr o `ReposicaoRepository`" |
| 3. Pegar a branch, copiar o repositório, commit e push | **Diogo** | `git fetch`, `git checkout feature/backend-reposicao`, cópia, `git add backend`, `git commit`, `git push` |
| 4. Trazer o commit do Diogo | **Você** | `git pull` (dentro da sua branch) |
| 5. Rodar os testes e abrir o PR | **Você** | Partes 7.4 e 7.5 |

**Como não dar conflito:** cada um copia **só os seus arquivos**, e ninguém mexe na branch enquanto o outro está com commit pendente.

### 2.8 Guardar a evidência dos testes

A entrega exige a saída dos testes de cada fase em `docs/Sprint_4/evidencias/` (roteiro da Sprint 4, §7). O padrão para o Python, na `<PASTA_DO_PROJETO>`:

```powershell
$saida = optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
$saida | Out-File -Encoding utf8 docs\Sprint_4\evidencias\2026-10-30_kaue_fasePQ_pytest.txt
$saida | Select-Object -Last 1
```

| Linha | O que faz |
|---|---|
| `$saida = …` | Roda os testes e guarda a saída na variável `$saida`, em vez de mostrar na tela |
| `$saida \| Out-File -Encoding utf8 …` | Grava a saída num arquivo de texto em UTF-8 (o Git consegue mostrar as diferenças dele). Se fizer em outro dia, troque a data no nome |
| `$saida \| Select-Object -Last 1` | Mostra só a última linha, o resumo |

**Você deve ver** (validado): `47 passed in 7.69s` (o tempo muda).

Para o Java (Fase 5), o padrão é o mesmo do tutorial do Lucas, de dentro de `backend`: `$saida = .\mvnw test "-Dtest=<CLASSES>"`, depois o `Out-File` para `..\docs\Sprint_4\evidencias\…` e `$saida | Select-String "Tests run:|BUILD"`.

> Não use `Tee-Object` para isso: no Windows PowerShell 5.1 ele grava o arquivo em UTF-16, e o Git o trata como binário.

---

## Parte 3 — Fase P: a recomendação de produção por lucro (US10, US11)

**Objetivo:** trocar o `optimization/app/otimizador.py` da Sprint 1 (que maximiza **receita**) pelo novo, que maximiza **lucro**, com testes que provam o resultado.
**Quando:** qui 29/10 (atrasada: faça primeiro). O PR sai junto com a Fase Q, na sex 30/10 (M2).
**Branch:** `feature/python-lucro-e-reposicao` (Parte 2.1).

### 3.1 Por que o módulo muda

| O que muda | Por quê |
|---|---|
| Maximiza **lucro** `(preço − custo dos ingredientes) × copos`, não receita | Receita ignora o custo; a dor (a) da Visão é lucratividade. Isso altera o Termo: o Diogo e o Lucas avisaram o professor (F9) |
| **Dois estágios** | Vários planos podem ter o mesmo lucro máximo. O estágio 2 escolhe sempre o mesmo (favorecendo o menor `id`), para a tela não "mudar de ideia" com a mesma entrada |
| Contas em **inteiros** (milésimos e centavos) e leitura em `Decimal` | O `float` erra com centavos. Em inteiros, a conta é exata e pode ser conferida |
| `MIPGap = 0` | O padrão do Gurobi aceita um plano até 0,01% pior que o ótimo |
| `consumo` na resposta | O Java mostra quanto a recomendação gasta de cada ingrediente |
| Modo **eco** | O "alô" da ponte Java ↔ Python (Fase 4A) |
| Entrada lida com **`utf-8-sig`** | O pipe do PowerShell manda um BOM, e sem isso o módulo recusava a entrada (A8) |

### 3.2 O baseline: os testes da `main`, antes de mexer

**Objetivo:** provar que o ambiente funciona **antes** de mudar qualquer coisa. Se falhar aqui, o problema é de ambiente, não do código novo.
**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, na branch nova.

```powershell
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

| Parte | O que faz |
|---|---|
| `optimization\venv\Scripts\python.exe` | O Python do ambiente do projeto |
| `-m pytest` | Roda o `pytest` como módulo desse Python |
| `optimization\tests` | A pasta onde ele procura arquivos `test_*.py` |
| `-q` | Saída curta: um ponto por teste que passou |

**Você deve ver** (validado):

```
........                                                                 [100%]
8 passed in 1.10s
```

| Se der errado | O que fazer |
|---|---|
| `No module named pytest` | O `pip install` da Parte 1.4 não rodou nesse `venv` |
| Erro de licença do Gurobi | Confira se não há variáveis `GRB_*` definidas na janela (`dir env:GRB*`). Se houver, abra um PowerShell novo |

### 3.3 O `requirements.txt` em UTF-8

**Por quê:** o arquivo está gravado em **UTF-16**. O `pip` lê sem problema, mas o **Git** trata UTF-16 como arquivo binário e não mostra as diferenças linha a linha num PR.

```powershell
(Get-Content optimization\requirements.txt) | Set-Content -Encoding utf8 optimization\requirements.txt
Get-Content optimization\requirements.txt
git diff optimization\requirements.txt
```

| Parte | O que faz |
|---|---|
| `(Get-Content …)` | Lê o arquivo inteiro **antes** de gravar. Os **parênteses são obrigatórios**: sem eles, o PowerShell lê e grava o mesmo arquivo ao mesmo tempo e pode deixá-lo **vazio** |
| `Set-Content -Encoding utf8` | Grava de novo, agora em UTF-8 (o PowerShell 5.1 põe um BOM no começo; o `pip` aceita, verificado) |
| `git diff` | Mostra o que mudou |

**Você deve ver** (validado) as 7 linhas do arquivo, com `gurobipy==13.0.3` e `pytest==9.1.1`, e depois:

```
diff --git a/optimization/requirements.txt b/optimization/requirements.txt
...
Binary files a/optimization/requirements.txt and b/optimization/requirements.txt differ
```

**Por que ainda diz "Binary":** o lado antigo é UTF-16. Depois deste commit, as próximas mudanças no arquivo aparecem como texto.

| Se der errado | O que fazer |
|---|---|
| O arquivo ficou vazio | Faltaram os parênteses. Recupere com `git checkout -- optimization\requirements.txt` e rode de novo |

### 3.4 Entenda o modelo (10 minutos)

**Os dados:** cada produto tem um preço e uma composição (quanto leva de cada ingrediente); cada ingrediente tem um estoque e um custo por unidade.

**As contas:**
- **lucro por copo:** `m_p = preço_p − Σ (custo_i × quantidade do ingrediente i no copo p)`;
- **estágio 1:** escolher os copos `x_p` (inteiros, ≥ 0) que **maximizam** `Σ m_p × x_p`, sem passar do estoque de nenhum ingrediente: `Σ quantidade_ip × x_p ≤ estoque_i`;
- **estágio 2 (desempate):** com o lucro máximo `L*` travado, para cada produto em ordem crescente de `id`, maximizar `x_p` e fixar o valor;
- produto com `m_p ≤ 0` (não compensa) recebe 0.

**Exemplo do contrato, à mão** (granola com estoque 0):
- Tradicional: `12,90 − (0,300 × 18,50 + 0,030 × 12,00 + 1 × 0,35) = 12,90 − 6,26 = 6,64` por copo;
- Especial: `18,90 − 9,83 = 9,07` por copo, mas leva granola, que está zerada → 0 copos;
- o Tradicional para no leite condensado: `2,000 ÷ 0,030 = 66,6` → **66** copos;
- lucro `66 × 6,64 = 438,24`; receita `66 × 12,90 = 851,40`.

| Exemplo | Plano | Lucro (`margem_estimada`) | Receita |
|---|---|---|---|
| Contrato (granola 0) | 66 / 0 | 438,24 | 851,40 |
| Didático (granola 12) | **64 / 46** | 842,18 | 1.695,00 |
| Demonstração (`seed_demo.sql`) | **151 / 1 / 0** | 1.011,71 | 1.966,80 |
| Seed oficial (estoque 0) | 0 / 0 / 0 | 0 | 0 |

**O exemplo didático é a melhor defesa do solver na Review:** com granola, o "jeito óbvio" é encher primeiro o copo que rende mais por kg de açaí (o Tradicional, até o limite de 66) e usar o resto no Especial: 66/44, lucro 837,32. O ótimo inteiro é **64/46**, com **R$ 4,86 a mais**. Ninguém acha isso "de cabeça": o solver acha.

**Escala inteira:** o módulo multiplica as quantidades por 1.000 (milésimos de kg) e os preços e custos por 100 (centavos). O lucro por copo do Tradicional vira `1290 × 1000 − (1850 × 300 + 1200 × 30 + 35 × 1000) = 664.000`, em "centavos × milésimos" (1 = R$ 0,00001). Tudo é inteiro, então a conferência é exata.

### 3.5 Os testes primeiro (vermelho)

**Objetivo:** pôr os testes novos **antes** do código novo e vê-los falhar. É a prova de que eles testam alguma coisa.
**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
$ref = "docs\Sprint_3\referencia\python"
Copy-Item "$ref\tests\oraculo.py", "$ref\tests\test_otimizador.py" optimization\tests\
git status --short
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

| Parte | O que faz |
|---|---|
| `$ref = "…"` | Guarda o caminho da pasta de referência numa variável, para os comandos ficarem curtos |
| `Copy-Item a, b destino\` | Copia os dois arquivos para `optimization\tests`. O `test_otimizador.py` **substitui** o da Sprint 1 (o antigo continua no histórico do Git) |

**Você deve ver** (validado):

```
 M optimization/requirements.txt
 M optimization/tests/test_otimizador.py
?? optimization/tests/oraculo.py
...
FAILED optimization/tests/test_otimizador.py::test_exemplo_do_contrato_via_subprocesso
...
17 failed, 8 passed in 2.79s
```

**Isso é o esperado.** O código ainda é o antigo: maximiza receita, não tem eco nem `consumo`. Os 8 que passam testam coisas que o antigo já fazia: a saída só com JSON, o desempate, 5 dos 10 erros de entrada e o JSON inválido.

**O que cada teste prova:**

| Teste | O que prova |
|---|---|
| `test_exemplo_do_contrato_via_subprocesso` | 66/0, receita 851,40, lucro 438,24 e `consumo`, rodando o script **como o Java roda** (JSON na entrada, JSON na saída) |
| `test_stdout_contem_somente_o_json_em_utf8` | Nenhum aviso de licença "vaza" na saída (senão o Java não consegue ler o JSON) |
| `test_exemplo_didatico_nao_e_o_arredondamento_do_simplex` | O ótimo inteiro (64/46) não é o arredondamento |
| `test_maior_receita_nao_e_maior_lucro` | Preço maior pode render menos lucro (CT41) |
| `test_produto_com_lucro_zero_ou_negativo_recebe_zero` | `x = 0` quando o copo não compensa |
| `test_desempate_e_deterministico_e_favorece_menor_id` | Mesmo lucro: sempre o mesmo plano |
| `test_estoque_zerado_retorna_sucesso_com_zeros` | Sem estoque é **sucesso** com zeros, não erro |
| `test_entradas_invalidas_retornam_erro_tratado` (10 casos) | Dados ruins → `status: "erro"`, com código de saída 0 |
| `test_json_invalido_retorna_erro_tratado` | Texto que não é JSON → erro tratado |
| `test_entrada_com_bom_e_aceita` | Entrada com BOM (do PowerShell) é aceita (A8) |
| `test_eco_…` (4 casos) | O eco com acentos, e o eco inválido como erro tratado |
| `test_consumo_nunca_passa_do_estoque_…` | O consumo cabe no estoque e só lista o que é usado |
| `test_coincide_com_o_oraculo_em_200_instancias` | 200 problemas aleatórios (sempre os mesmos, pela semente fixa) dão o mesmo resultado que o oráculo |

**O oráculo** (`oraculo.py`) resolve o mesmo problema **sem solver**: testa todas as combinações de copos, com `Decimal`, e fica com a de maior lucro (no empate, a de maior `x` do menor `id`). É lento, então só serve para problemas pequenos, mas não depende do Gurobi. Se os dois concordam em 200 casos, é difícil que o modelo esteja errado (Plano de Testes, §1: o esperado não pode vir do próprio código testado).

### 3.6 O código (verde)

```powershell
Copy-Item "$ref\app\otimizador.py" optimization\app\
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

**Você deve ver** (validado):

```
.........................                                                [100%]
25 passed in 3.05s
```

Se a janela é nova e o `$ref` sumiu, rode de novo `$ref = "docs\Sprint_3\referencia\python"`.

**Leia o código antes de seguir: o módulo é seu, e você o defende na Review.** As partes:

| Parte | O que faz |
|---|---|
| `main()` | Lê a entrada com `utf-8-sig` (aceita o BOM) e com `parse_float=Decimal` (o JSON nunca passa por `float`). JSON inválido → `{"status": "erro", …}` e código 0 |
| `processar()` | Se a entrada tem `echo` e não tem `produtos`, é o modo eco. Senão, valida, resolve e monta a resposta |
| `_dec()` e `validar_e_converter()` | Aceitam só número (nunca texto nem `true`/`false`) com no máximo 3 casas (quantidades) ou 2 (dinheiro). Problema → `ErroDeDominio` → `status: "erro"` |
| `_criar_env()` | Abre o Gurobi com `OutputFlag 0` **antes** de `start()`: assim o aviso de licença não sai no `stdout` |
| `resolver()` | Monta o modelo, roda os dois estágios e confere tudo em inteiros |
| `_verificar()` | Refaz as contas sem o solver. Se algo não bater → `FalhaTecnica` (código ≠ 0). **Nunca** devolve um plano sem conferir |

**O começo de tudo (`main`):**

```python
sys.stdin.reconfigure(encoding="utf-8-sig")
sys.stdout.reconfigure(encoding="utf-8")
try:
    # parse_float=Decimal: o JSON nunca passa por float (evita 0.1 + 0.2 != 0.3)
    entrada = json.load(sys.stdin, parse_float=Decimal)
except json.JSONDecodeError:
    print(json.dumps({"status": "erro", "mensagem": "JSON de entrada inválido"}, ensure_ascii=False))
    return 0
```

- `utf-8-sig` lê UTF-8 e **descarta** um BOM no começo, se houver.
- `ensure_ascii=False` escreve `Açaí` com acentos, em vez de `A\u00e7a\u00ed`.
- `return 0`: um JSON inválido é um **erro de dado**, tratado. O código de saída diferente de 0 fica para falhas técnicas.

**O estágio 2 (`resolver`):**

```python
m.addConstr(lucro_expr >= lstar, name="manter_lucro")
for p in ativos:
    m.setObjective(x[p["id"]], GRB.MAXIMIZE)
    m.optimize()
    ...
    v = int(round(x[p["id"]].X))
    x[p["id"]].LB = v
    x[p["id"]].UB = v
```

1. Trava o lucro no máximo encontrado (`L*`).
2. Para cada produto, em ordem de `id`: pede o **maior** número de copos dele que ainda mantém o lucro.
3. Fixa esse número (limite de baixo = limite de cima = `v`) e passa para o próximo.

Resultado: entre os planos de lucro máximo, sempre o mesmo, favorecendo o menor `id`.

**A conferência (`_verificar`):** depois de cada estágio, refaz em inteiros o consumo de cada ingrediente e o lucro. Se o plano passar do estoque ou o lucro divergir, levanta `FalhaTecnica`. O Java transforma isso em `500`, sem detalhe para o usuário (CT44): é melhor não recomendar do que recomendar errado.

### 3.7 Quebrar de propósito (5 minutos)

**Objetivo:** ver os testes pegarem um erro de verdade. É uma ótima história para a Review.
**Como:** abra `optimization\app\otimizador.py` no editor (VS Code ou Bloco de Notas), faça **uma** troca, salve, rode o `pytest` e desfaça.

| Troca temporária | Onde | Resultado (validado) |
|---|---|---|
| `lucro = preco * 1000 - sum(custo[i] * q for i, q in itens.items())` → `lucro = preco * 1000` | `validar_e_converter`, perto da linha 98 | **5 failed, 20 passed**: o modelo volta a maximizar receita |
| `if "echo" in entrada and "produtos" not in entrada:` → `if False:` | `processar`, perto da linha 184 | **5 failed, 20 passed**: os 4 testes do eco e o do BOM (que usa o eco) |

```powershell
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

**Para desfazer** (volta o arquivo ao da referência):

```powershell
$ref = "docs\Sprint_3\referencia\python"
Copy-Item "$ref\app\otimizador.py" optimization\app\
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

**Você deve ver:** `25 passed` de novo. Copie as duas saídas com falhas para o PR (é a evidência do experimento).

### 3.8 Rodar "como o Java vai rodar"

**Objetivo:** mandar um JSON pela entrada do script e ler o JSON da saída, sem o Java. É a melhor forma de investigar um problema na Fase 4A.
**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, **depois** do `[Console]::OutputEncoding = [Text.Encoding]::UTF8`.

**Primeiro, os exemplos** (vão para o repositório; os da reposição também, para a Parte 4):

```powershell
$ref = "docs\Sprint_3\referencia\python"
New-Item -ItemType Directory -Force optimization\exemplos | Out-Null
Copy-Item "$ref\exemplos\*.json" optimization\exemplos\
(Get-ChildItem optimization\exemplos).Count
```

**Você deve ver:** `10`.

**Agora, o exemplo do contrato:**

```powershell
cmd /c "optimization\venv\Scripts\python.exe optimization\app\otimizador.py < optimization\exemplos\entrada_contrato.json"
```

| Parte | O que faz |
|---|---|
| `cmd /c "…"` | Pede ao Prompt de Comando para rodar a linha entre aspas e voltar |
| `< arquivo.json` | Manda o **conteúdo do arquivo** para a entrada (`stdin`) do Python, byte por byte, como o Java faz |

**Você deve ver** (validado, uma linha só):

```
{"status": "sucesso", "receita_estimada": 851.4, "margem_estimada": 438.24, "recomendacao": [{"produto_id": 1, "quantidade": 66}, {"produto_id": 2, "quantidade": 0}], "consumo": [{"ingrediente_id": 1, "quantidade": 19.8}, {"ingrediente_id": 2, "quantidade": 1.98}, {"ingrediente_id": 7, "quantidade": 66.0}]}
```

**Conferir automaticamente contra a saída guardada:**

```powershell
$s = cmd /c "optimization\venv\Scripts\python.exe optimization\app\otimizador.py < optimization\exemplos\entrada_contrato.json"
$s -eq (Get-Content -Raw -Encoding utf8 optimization\exemplos\saida_contrato.json).Trim()
```

**Você deve ver:** `True`. Troque `contrato` por `demo`, `eco` e `estoque_zero` nas duas linhas: os três também dão `True` (validado). O eco devolve `{"status": "sucesso", "echo": "AÇAÍ (POLPA)"}`, **com acentos**.

**Por que não `Get-Content … | python …`:** verificado numa janela com os padrões de fábrica:
- o pipe do PowerShell 5.1 troca os acentos por `?` antes de chegar ao Python: o eco devolve `A?A? (POLPA)`;
- além disso, ele põe um BOM no começo (o módulo aceita, por causa do `utf-8-sig`).

O Java não tem esse problema: ele escreve UTF-8 puro. Por isso o `cmd /c` com `<` é o jeito fiel de imitar o Java.

**Erro tratado:**

```powershell
Set-Content -Encoding ascii invalido.json "isto nao e json"
cmd /c "optimization\venv\Scripts\python.exe optimization\app\otimizador.py < invalido.json"
"codigo de saida: $LASTEXITCODE"
Remove-Item invalido.json
```

**Você deve ver** (validado):

```
{"status": "erro", "mensagem": "JSON de entrada inválido"}
codigo de saida: 0
```

`$LASTEXITCODE` é o código de saída do último programa. `0` com `status: "erro"` é o combinado do contrato: erro de **dado**, tratado. O último comando apaga o arquivo de teste, para ele não ir para o Git.

**Tempo de uma recomendação** (pendência P6; o Java espera até 10 s):

```powershell
$tempos = 1..5 | ForEach-Object { (Measure-Command { cmd /c "optimization\venv\Scripts\python.exe optimization\app\otimizador.py < optimization\exemplos\entrada_demo.json" }).TotalMilliseconds }
$tempos | Measure-Object -Minimum -Maximum -Average
```

| Parte | O que faz |
|---|---|
| `1..5 \| ForEach-Object { … }` | Repete 5 vezes |
| `Measure-Command { … }` | Mede quanto tempo o bloco leva (a saída do Python é descartada) |
| `Measure-Object -Minimum -Maximum -Average` | Calcula o menor, o maior e a média |

**Você deve ver** (validado nesta máquina; na sua, os números mudam):

```
Count    : 5
Average  : 190,11534
Maximum  : 224,2808
Minimum  : 168,1102
```

Anote os três números no PR. Para comparar: dentro do container com 0,1 de CPU (o limite do Render), a recomendação inteira, pelo Java, levou 2,7 s (Parte 8.2, validado em 30/10).

### 3.9 Perguntas para a Review

1. Por que maximizar lucro, e não receita? Mostre o exemplo didático (64/46 × 66/44).
2. Para que serve o estágio 2, se o estágio 1 já achou o lucro máximo?
3. Por que as contas são feitas em inteiros, e não com `float`?
4. O que é o oráculo, e por que ele não usa o Gurobi?
5. Qual a diferença entre `status: "erro"` (código 0) e uma falha técnica (código ≠ 0)? O que o usuário vê em cada caso?

---

## Parte 4 — Fase Q: reposição com orçamento (US14), no mesmo PR

**Objetivo:** o módulo `optimization/app/reposicao.py`, que responde "com este orçamento, quanto comprar de cada ingrediente".
**Quando:** sex 30/10, na mesma branch da Fase P. O PR da Fase P + Q sai à noite (M2).

### 4.1 O modelo

**Entrada (por ingrediente `i`, escolhido pelo Java: os 8 mais consumidos até 30 dias, os 15 acima disso):**
- `c_i` = consumo no período; `S_i` = saldo **válido** (sem o vencido); `qmin_i` = quantidade mínima; `k_i` = custo unitário;
- mais o orçamento `B`.

**Duas quantidades por ingrediente**, arredondadas **para cima** em centésimos (0,01), para nunca ficar abaixo do mínimo:
- **mínimo a comprar:** `min_i = max(0, qmin_i − S_i)`: o que falta para o estoque chegar ao mínimo;
- **necessidade:** `nec_i = max(0, c_i + qmin_i − S_i)`: repetir o consumo do período e ainda manter o mínimo.

**Se o custo dos mínimos passa do orçamento** (`Σ k_i × min_i > B`): "orçamento insuficiente". A resposta traz os mínimos e **quanto falta**, sem chamar o solver.

**Senão**, três estágios (as compras `x_i` são inteiras, em centésimos):

| Estágio | Objetivo | Restrições |
|---|---|---|
| 1 | maximizar `t`, a **menor cobertura** `x_i / nec_i` | `min_i ≤ x_i ≤ nec_i`; `x_i ≥ t × nec_i`; `Σ k_i × x_i ≤ B` |
| 2 | maximizar o **gasto** `Σ k_i × x_i` (usar a sobra) | as mesmas, com `x_i ≥ teto(t* × nec_i)` |
| 3 (desempate) | do mais consumido ao menos consumido (empate: menor `id`), maximizar `x_i` | mantendo o gasto do estágio 2 e fixando os já decididos |

**Por que três:**
- o estágio 1 reparte o orçamento "de forma justa" (decisão de 14/10): nenhum ingrediente fica muito pior que os outros;
- o 2 evita deixar dinheiro parado quando ainda dá para comprar;
- o 3 deixa o resultado **sempre igual** para a mesma entrada.

**Exemplo didático (conta à mão):**

| Ingrediente | Consumo | Saldo | Mínimo | Custo | `min` | `nec` |
|---|---|---|---|---|---|---|
| Açaí | 0,800 | 0,300 | 0,500 | 18,50 | 0,20 | 1,00 |
| Granola | 0,300 | 0 | 0,100 | 22,00 | 0,10 | 0,40 |
| Leite condensado | 0,200 | 0,100 | 0,100 | 12,00 | 0 | 0,20 |

- **Orçamento 50:** cobre toda a necessidade; custo 29,70.
- **Orçamento 5:** não cobre nem os mínimos (5,90); faltam 0,90.
- **Orçamento 20:**
  - com 66% de cada necessidade: açaí 0,66 (12,21), granola teto(26,4) = 0,27 (5,94), leite teto(13,2) = 0,14 (1,68); total 19,83;
  - com 67%: 20,015, passa de 20. Então **`t* = 0,66`**;
  - sobram 0,17: só cabe mais 0,01 de leite (0,12);
  - **resultado: 0,66 / 0,27 / 0,15 = 19,95.** É o que o `test_exemplo_didatico_via_subprocesso` confere.

### 4.2 Os testes primeiro

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, na mesma branch.

```powershell
$ref = "docs\Sprint_3\referencia\python"
Copy-Item "$ref\tests\oraculo_reposicao.py", "$ref\tests\test_reposicao.py" optimization\tests\
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

**Você deve ver** (validado):

```
=================================== ERRORS ====================================
____________ ERROR collecting optimization/tests/test_reposicao.py ____________
...
E   ModuleNotFoundError: No module named 'reposicao'
=========================== short test summary info ===========================
ERROR optimization/tests/test_reposicao.py
!!!!!!!!!!!!!!!!!!! Interrupted: 1 error during collection !!!!!!!!!!!!!!!!!!!!
1 error in 0.22s
```

**Isso é o "vermelho" desta fase, e é diferente da Fase P:**
- o `pytest` primeiro **coleta** os testes (abre cada `test_*.py`);
- o `test_reposicao.py` começa com `import reposicao`, e o módulo ainda não existe;
- então ele para **antes** de rodar qualquer teste, com um erro de coleta. Nenhum dos 25 da produção roda nesta vez.

### 4.3 O código

```powershell
Copy-Item "$ref\app\reposicao.py" optimization\app\
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

**Você deve ver** (validado):

```
...............................................                          [100%]
47 passed in 6.23s
```

São 25 da produção e 22 da reposição.

| Teste | O que prova |
|---|---|
| `test_exemplo_didatico_via_subprocesso` | O exemplo da 4.1 (19,95), rodando o script como o Java |
| `test_orcamento_que_cobre_toda_a_necessidade` / `…insuficiente…` / `…exatamente_igual_aos_minimos…` / `…zero…` | Os limites do orçamento |
| `test_quantidades_sao_arredondadas_para_cima_em_centesimos` | 0,001 vira 0,01 (nunca abaixo) |
| `test_desempate_favorece_o_ingrediente_mais_consumido` | O estágio 3 |
| `test_tabela_ordenada_por_custo_estimado_decrescente` | A ordem da tabela (decidida em 14/10) |
| `test_coincide_com_o_oraculo_em_150_instancias` | Força bruta com frações exatas, **sem solver** |
| `test_exemplo_demo_bate_com_a_saida_guardada` | O seed de demonstração (60 dias): só o morango, 0,74 kg (R$ 11,10). O Diogo conferiu o mesmo número pelo SQL |
| `test_entradas_invalidas_…` (10 casos), `…json_invalido_e_entrada_com_bom`, `…stdout_contem_somente_o_json` | Erros tratados, BOM, nada além do JSON |

**Como o código está organizado:**

| Parte | O que faz |
|---|---|
| `from otimizador import ErroDeDominio, FalhaTecnica, _criar_env, _dec, _inteiro_escalado` | **Reaproveita** do módulo de produção o jeito de abrir o Gurobi e de converter números. A licença fica num lugar só |
| `validar_e_converter()` | Valida e calcula `min` e `nec` em centésimos, arredondando para cima com `_teto_div` |
| `processar()` | Se o custo dos mínimos passa do orçamento, responde sem solver (`orcamento_suficiente: false` e `valor_faltante`). Senão, chama `resolver()` |
| `resolver()` | Os três estágios, com a mesma ideia da produção: resolver, conferir em inteiros, travar, seguir |
| `_cobertura()` | Calcula a cobertura com `Fraction` (frações exatas, sem arredondar) |
| `_montar()` | Monta a tabela, ordenada pelo custo de compra (maior primeiro) |

**O arredondamento para cima, em uma linha:**

```python
def _teto_div(a, b):
    return -(-a // b)
```

O `//` do Python arredonda **para baixo**. Trocando o sinal duas vezes, arredonda **para cima**: `_teto_div(1, 10)` dá 1, e não 0. É isso que transforma 0,001 kg em 0,01 kg.

> **Comentário desatualizado:** a primeira linha do `reposicao.py` cita "Contrato v2, §1.6". A seção da reposição no contrato oficial é a **§1.7** (a §1.6 é a da licença). Pode corrigir o comentário na sua cópia, no mesmo commit.

### 4.4 Quebrar de propósito

Rode só os testes da reposição, que são mais rápidos:

```powershell
optimization\venv\Scripts\python.exe -m pytest optimization\tests\test_reposicao.py -q
```

| Troca temporária em `reposicao.py` | Resultado (validado) |
|---|---|
| `"min": _teto_div(max(0, q_m - s_m), 10),` → `"min": max(0, q_m - s_m) // 10,` (arredonda para baixo) | **2 failed, 20 passed** |
| `for i in _ordem_desempate(variaveis):` → `for i in []:` (sem estágio 3) | **1 failed, 21 passed** |
| `m.setObjective(gasto_expr, GRB.MAXIMIZE)` → `m.setObjective(0, GRB.MAXIMIZE)` (sem estágio 2) | **9 failed, 13 passed** |

Desfaça com `Copy-Item "$ref\app\reposicao.py" optimization\app\` (numa janela nova, defina antes `$ref = "docs\Sprint_3\referencia\python"`) e confirme `47 passed` no `pytest` inteiro.

### 4.5 Rodar "como o Java vai rodar"

```powershell
cmd /c "optimization\venv\Scripts\python.exe optimization\app\reposicao.py < optimization\exemplos\entrada_reposicao_demo.json"
```

**Você deve ver** (validado; uma linha só, cortada aqui):

```
{"status": "sucesso", "orcamento_suficiente": true, "custo_total": 11.1, "valor_faltante": 0.0, "cobertura_minima": 1.0, "itens": [{"ingrediente_id": 5, "quantidade_sugerida": 0.74, "quantidade_minima_compra": 0.02, "quantidade_necessaria": 0.74, "custo_estimado": 11.1}, {"ingrediente_id": 1, "quantidade_sugerida": 0.0, ...
```

**Como ler:** com R$ 100 de orçamento, todo o necessário cabe (`cobertura_minima` 1,0). Só o ingrediente 5 (morango) precisa de compra: 0,74 kg, R$ 11,10. Os outros vêm com 0, porque o saldo já cobre o consumo de 60 dias e o mínimo.

### 4.6 Evidência, commit, push e PR da Fase P + Q

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, na branch `feature/python-lucro-e-reposicao`.

```powershell
$saida = optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
$saida | Out-File -Encoding utf8 docs\Sprint_4\evidencias\2026-10-30_kaue_fasePQ_pytest.txt
$saida | Select-Object -Last 1
git add optimization docs\Sprint_4\evidencias
git status --short
git commit -m "feat: otimizacao por lucro em dois estagios, reposicao com orcamento e exemplos"
git push -u origin feature/python-lucro-e-reposicao
```

**Você deve ver** no `git status --short` (validado, mais a linha da evidência):

```
M  optimization/app/otimizador.py
A  optimization/app/reposicao.py
A  optimization/exemplos/entrada_contrato.json
...
A  optimization/exemplos/saida_reposicao_demo.json
M  optimization/requirements.txt
A  optimization/tests/oraculo.py
A  optimization/tests/oraculo_reposicao.py
M  optimization/tests/test_otimizador.py
A  optimization/tests/test_reposicao.py
```

São 17 arquivos de `optimization` (10 deles em `exemplos`). **Não** pode aparecer nada de `venv` nem `__pycache__`.

Abra o PR (Parte 2.4). **Revisores:** o **Lucas** (consome o contrato na Fase 4A) e o **Diogo** (os números da demonstração vêm do SQL dele).

**Modelo de descrição do PR:**

```
## O que entra
Fase P: otimizador por lucro (dois estágios, contas em inteiros, eco, consumo, BOM).
Fase Q: módulo de reposição com orçamento (US14).
requirements.txt convertido para UTF-8 (gurobipy 13.0.3, P2). Exemplos em optimization/exemplos.

## Como testar
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q

## Resultado
Na minha máquina: 47 passed. Na 2ª máquina (<nome>): 47 passed.
Experimentos: produção 5 e 5 falhas; reposição 2, 1 e 9 falhas (saídas abaixo).
Tempo da recomendação (5 vezes): mín ___ ms, máx ___ ms, média ___ ms.

## Avisos
- Muda a recomendação de receita para lucro (F9).
- A Fase 4A do Lucas (dom) depende deste PR.
```

### 4.7 A segunda máquina

**Por quê:** fecha a pendência "só funciona no PC do Kauê". Outro integrante (o Diogo ou o Leonardo) roda os mesmos testes, do zero, na máquina dele.

**O colega faz**, na pasta do projeto dele:

```powershell
git fetch
git checkout feature/python-lucro-e-reposicao
py -m venv optimization\venv
optimization\venv\Scripts\python.exe -m pip install -r optimization\requirements.txt
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q
```

Se o colega já tem um `optimization\venv`, ele pula a linha do `py -m venv`. **Esperado:** `47 passed`. Ele comenta no PR o resultado e a versão do Python (`py --version`). Se falhar, ele cola a mensagem exata no PR.

**Antes de avançar:**
- [ ] `47 passed` na sua máquina e na de outro integrante;
- [ ] os 5 experimentos registrados no PR;
- [ ] `requirements.txt` em UTF-8;
- [ ] PR aprovado e na `main` na sex 30/10 à noite (M2). Avise o Lucas: a Fase 4A depende dele.

### 4.8 Perguntas para a Review

1. O que é a "cobertura", e por que maximizar a **menor** cobertura é "justo"?
2. Por que arredondar para **cima**?
3. Quando o solver **não** é chamado?
4. Por que o vermelho da Fase Q foi um erro de coleta, e não falhas?

---

## Parte 5 — A licença do Gurobi (A34, A38)

**Situação desde 28/10 (A38):**

| Onde | Licença | O que fazer |
|---|---|---|
| Testes e desenvolvimento (todos) | A licença limitada que vem com o `gurobipy` do `pip` | Nada. `pip install -r optimization\requirements.txt` e pronto |
| **Render** | A mesma licença do `pip`, **autorizada por escrito pela Gurobi** em 28/10 (resposta ao seu chamado) | `OTIMIZADOR_HABILITADO=true` e **nenhuma** variável `GRB_*` no painel |
| O seu computador, se quiser | A sua WLS acadêmica (pessoal, até 25/09/2027) | Só com as variáveis `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID` na **sua** janela. **Nunca** no Git, no `.env` de outra pessoa nem no Render |

- **Por que a licença do `pip` basta:** ela resolve problemas de até 2.000 variáveis e 2.000 restrições. Os nossos têm 3 produtos e até 15 ingredientes.
- **O aviso de licença** não aparece na saída porque o `_criar_env()` liga o `OutputFlag 0` antes de `start()`. O teste `test_stdout_contem_somente_o_json_em_utf8` é a guarda.
- **Com a WLS:** ela permite 2 sessões ao mesmo tempo, então não rode a suíte inteira com as variáveis `GRB_*` definidas; teste poucos casos (`-k "contrato or didatico"`).
- **O plano B1** (`OTIMIZADOR_HABILITADO=false`) é só **reserva**: se o solver der problema no Render, o Lucas troca a variável no painel e produção e reposição passam a responder `503` com mensagem. O resto do sistema continua.

**E-mail da Gurobi:** encaminhe ao professor a resposta da Gurobi de 28/10 no mesmo dia em que o Diogo enviar a mensagem da Sprint 4 (`docs/Sprint_4/sprint-4-mensagem-professor.md`). Se ainda não fez, faça hoje.

---

## Parte 6 — Fase 4A em dupla com o Lucas (dom 01/11)

**O seu papel:** o **contrato e a prova**. O Lucas copia e roda o Java (tutorial dele, Parte 6). Você confere que o que vai e volta entre o Java e o Python é o combinado, e investiga se algo divergir.

**Antes do domingo:** o seu PR da Fase P + Q na `main` (o Lucas confere isso na Parte 6.1 dele).

**Onde:** a dupla trabalha **na máquina do Lucas**, na branch dele (`feature/backend-fase4a-producao`), com o `venv` dele (tutorial do Lucas, Parte 6.2). Se preferirem rodar também na sua máquina: `git fetch`, `git checkout feature/backend-fase4a-producao`, e o `MF_PYTHON` apontando para o **seu** `optimization\venv`. Nesse caso, volte para a sua branch quando terminar.

| # | O que conferir | Como | Esperado |
|---|---|---|---|
| 1 | O eco pelo Java | Backend com o perfil `dev`; `GET /api/teste/python` (tutorial do Lucas, 6.6) | `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}`, **com acentos**. Quebrado → o Java não está escrevendo em UTF-8 |
| 2 | A recomendação real com o seed de demonstração | Tutorial do Lucas, 6.6, item 2 | **151/1/0**, igual ao `optimization\exemplos\saida_demo.json` |
| 3 | O Python real pelo Java | `ProcessoPythonTest` e `PythonRealTest` com o `MF_PYTHON` apontando para o `venv` da máquina em que vocês estão (tutorial do Lucas, 6.5) | 4 + 2 testes, 0 falhas (o terceiro do `PythonRealTest` é da sua Fase 5) |
| 4 | Os erros | Leia com o Lucas o `ProducaoService` e o `ProcessoPython` | `status: "erro"` → `422`; código de saída ≠ 0 → `500` sem detalhe (CT44) |
| 5 | O tempo | O seu número da Parte 3.8 | Bem abaixo dos 10 s do Java |

**Se a recomendação divergir de 151/1/0:** quase sempre o problema está **na entrada** (o saldo válido ou o custo de algum ingrediente). Compare o estoque de cada ingrediente no banco com o `optimization\exemplos\entrada_demo.json`. O teste `Fase4ProducaoTest` do Lucas confere, por exemplo, que a granola vai ao Python com **5,160** (o saldo válido, sem a linha vencida).

**Perguntas para a Review:** por que o Java chama o Python como um programa separado? O que acontece se o Python travar (o Java espera 10 s e mata o processo)?

---

## Parte 7 — Fase 5: o Java da reposição (US14)

**Objetivo:** `POST /api/reposicao/recomendacao` recebe um período e um orçamento, busca o consumo no banco, chama o seu `reposicao.py` e devolve a tabela de compra. É o que a Tela 13 do Leonardo mostra.
**Quando:** começa no **sáb 31/10**; os testes rodam no **dom 01/11 à noite**, depois do merge da Fase 4A; o PR entra **cedo na seg 02/11** (a Fase 6 do Lucas depende dele, A44).
**Branch:** `feature/backend-reposicao`.

### 7.1 Pré-requisitos e a ordem dos três dias

- **A sua Fase P + Q na `main`** (o `reposicao.py`).
- **A Fase 3 do Lucas na `main`** (pedidos): o `ReposicaoTest` cria pedidos para ter consumo.
- **A Fase 4A na `main`** (dom 01/11, M3): o `ReposicaoService` usa a `ChaveOtimizador`, que entra na 4A. **Antes disso, a branch não compila.** É esperado.
- **O `ReposicaoRepository` do Diogo** (consulta 9), por commit dele na sua branch (Parte 2.7).

| Quando | O que você faz |
|---|---|
| Sáb 31/10 | Branch, cópia da sua parte, commit e push; avisar o Diogo; ler o código (7.3) |
| Dom 01/11, à noite (depois do M3) | `git pull` (o commit do Diogo) e `git merge --no-edit origin/main` (a Fase 4A); rodar os testes |
| Seg 02/11, cedo | Evidência, PR, merge. **Avise o Lucas** assim que entrar na `main` |

### 7.2 Copiar a sua parte (sáb 31/10)

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
git checkout main
git pull
git checkout -b feature/backend-reposicao
$r = "docs\Sprint_3\referencia\backend\src"
$d = "backend\src"
New-Item -ItemType Directory -Force "$d\main\java\com\mentefria\backend\reposicao" | Out-Null
Copy-Item "$r\main\java\com\mentefria\backend\reposicao\ReposicaoService.java", "$r\main\java\com\mentefria\backend\reposicao\ReposicaoController.java" "$d\main\java\com\mentefria\backend\reposicao\"
Copy-Item "$r\test\java\com\mentefria\backend\ReposicaoTest.java" "$d\test\java\com\mentefria\backend\"
git add backend
git status --short
git commit -m "feat: recomendacao de reposicao com orcamento (Java)"
git push -u origin feature/backend-reposicao
```

| Linha | O que faz |
|---|---|
| `$r = …` e `$d = …` | Guardam a pasta de origem (referência) e a de destino (o backend de verdade) |
| `New-Item -ItemType Directory -Force …` | Cria a pasta `reposicao` (o `-Force` não reclama se ela já existe; o `\| Out-Null` esconde a resposta) |
| `Copy-Item …` | Copia o service, o controller e o teste. **Não** copie o `ReposicaoRepository`: ele é do Diogo |

**Você deve ver** (validado):

```
A  backend/src/main/java/com/mentefria/backend/reposicao/ReposicaoController.java
A  backend/src/main/java/com/mentefria/backend/reposicao/ReposicaoService.java
A  backend/src/test/java/com/mentefria/backend/ReposicaoTest.java
...
 * [new branch]      feature/backend-reposicao -> feature/backend-reposicao
branch 'feature/backend-reposicao' set up to track 'origin/feature/backend-reposicao'.
```

**Avise o Diogo** (modelo):

```
Diogo, a branch feature/backend-reposicao está no GitHub.
Pode pôr o reposicao/ReposicaoRepository.java (consulta 9). Me avisa quando der push.
```

O Diogo faz o passo 3 da Parte 2.7 (o tutorial dele tem os comandos). Quando ele avisar, traga o commit dele:

```powershell
git pull
```

**Você deve ver** (validado):

```
Fast-forward
 .../backend/reposicao/ReposicaoRepository.java     | 39 ++++++++++++++++++++++
 1 file changed, 39 insertions(+)
 create mode 100644 backend/src/main/java/com/mentefria/backend/reposicao/ReposicaoRepository.java
```

### 7.3 Entendendo o código da Fase 5

| Arquivo | Camada | Para quê |
|---|---|---|
| `ReposicaoController` | Porta de entrada | `POST /api/reposicao/recomendacao`. Só repassa ao service. **Só Administrador**: a regra já está no `SecurityConfig` da Fase 1 (`/api/reposicao/**`) |
| `ReposicaoService` | Regras | Valida, escolhe o período e os ingredientes, monta o JSON do contrato, chama o Python, confere a resposta e completa com nome, unidade, consumo, saldo e mínimo |
| `ReposicaoRepository` (Diogo) | SQL | A consulta 9: consumo de cada ingrediente nos pedidos do período |
| `ReposicaoTest` | Teste | 8 testes com banco real e o Python **dublê** |

**O SQL da consulta 9 (do Diogo):**

```sql
SELECT i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima,
       SUM(pp.quantidade * pi.quantidade_utilizada) AS consumo
FROM pedido p
JOIN pedido_produto pp ON pp.pedido_id = p.id
JOIN produto_ingrediente pi ON pi.produto_id = pp.produto_id
JOIN ingrediente i ON i.id = pi.ingrediente_id
WHERE p.data_pedido >= :inicio AND p.data_pedido < :fim
GROUP BY i.id, i.nome, i.unidade_medida, i.custo_unitario, i.quantidade_minima
ORDER BY consumo DESC, i.id
LIMIT :limite
```

- **Consumo** = copos vendidos × quanto cada copo leva do ingrediente, somado no período.
- **Pedidos cancelados contam** (decisão de 14/10, A18): o ingrediente foi gasto. Não há filtro de `status`.
- Descartes e saídas manuais **não** contam: a consulta só olha pedidos.
- `:inicio`, `:fim` e `:limite` são **parâmetros com nome**: o valor nunca é colado no texto do SQL.

**As regras no `ReposicaoService.recomendar`:**

```java
LocalDate hoje = LocalDate.now(clock);
int dias = req.periodoDias();
// Período = os últimos N dias, contando hoje: [hoje - (N-1) 00:00, amanhã 00:00)
List<Consumo> consumos = reposicao.maisConsumidos(hoje.minusDays(dias - 1L).atStartOfDay(), hoje.plusDays(1).atStartOfDay(),
        dias <= 30 ? LIMITE_ATE_30_DIAS : LIMITE_ACIMA_30_DIAS);
if (consumos.isEmpty())
    return new RecomendacaoReposicao(agora, dias, req.orcamento(), true, ..., "Não houve pedidos no período escolhido, então não há consumo para repor.", List.of());

chave.exigir();                                                              // só aqui o solver é necessário (A31)
Map<Integer, BigDecimal> saldo = ingredientes.saldosValidos(hoje);          // saldo VÁLIDO (decisão de 14/10)
```

1. **Validação:** período de 1 a 365 dias; orçamento obrigatório, não negativo, com no máximo 2 casas. Erro → `400` com o campo e a mensagem.
2. **Período:** os últimos N dias **contando hoje**, no fuso de São Paulo (o `clock` vem da Fase 1). Com 7 dias, de 6 dias atrás às 00:00 até amanhã às 00:00.
3. **Quantos ingredientes:** os **8** mais consumidos até 30 dias; os **15** mais consumidos acima disso (decisão de 14/10).
4. **Sem pedidos no período, o Python nem é chamado:** a resposta é uma tabela vazia com mensagem.
5. **`chave.exigir()`:** se o plano B1 estiver ligado, `503` aqui, sem chamar o Python.
6. **Saldo válido:** o que está vencido não conta como estoque.
7. **A resposta:** mantém a ordem do Python (maior custo primeiro) e acrescenta nome, unidade, consumo, saldo e mínimo. `status: "erro"` do Python → `422`; resposta fora do contrato → `500`.

### 7.4 Rodar os testes (dom 01/11, à noite)

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, na branch `feature/backend-reposicao`, com o Docker aberto. A Fase 4A já precisa estar na `main`.

```powershell
git pull
git fetch origin
git merge --no-edit origin/main
cd backend
.\mvnw test "-Dtest=ReposicaoTest"
```

**Você deve ver** (validado):

```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 9.168 s -- in com.mentefria.backend.ReposicaoTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Antes do resumo aparecem avisos `WARNING: A Java agent has been loaded dynamically…` e um sobre o Mockito, às vezes em vermelho: são normais.

| Teste | O que prova |
|---|---|
| `enviaOConsumoDoPeriodoOSaldoValidoEOMinimo` | O JSON que vai ao Python: o mais consumido primeiro (copo 17; açaí 5,4; …), com o saldo **válido** e o mínimo |
| `periodoCurtoSoContaOsPedidosDoPeriodo` | Com 7 dias, o pedido de 10/10 não entra |
| `pedidoCanceladoContaNoConsumoDaReposicao` | O cancelado continua contando (A18) |
| `ate30DiasEntramOs8MaisConsumidosEAcimaOs15` | O limite de 8 e de 15 |
| `semPedidosNoPeriodoNaoChamaOPython` | Tabela vazia, mensagem, e o dublê **nunca** é chamado |
| `validacaoDoPeriodoEDoOrcamento` | `400` com as mensagens de cada campo |
| `orcamentoInsuficienteEErroDoPython` | "Não cobre nem os mínimos" e falta 10,71; `status: "erro"` → `422` |
| `funcionarioNaoGeraReposicao` | O Funcionário recebe `403` |

**Com o Python de verdade** (o terceiro teste do `PythonRealTest`, que o Lucas deixou para você):

```powershell
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path
$env:MF_PYTHON
.\mvnw test "-Dtest=PythonRealTest"
```

| Parte | O que faz |
|---|---|
| `$env:MF_PYTHON = …` | Cria uma variável de ambiente **só nesta janela**, com o caminho completo do Python do `venv`. Sem ela, o `PythonRealTest` é pulado |
| `.\mvnw test "-Dtest=PythonRealTest"` | Roda os 3 testes com o Gurobi de verdade: o eco, a produção 151/1/0 e a **sua** reposição |

**Você deve ver** (validado):

```
[TEMPO] recomenda��o de produ��o ponta a ponta: 713 ms
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.531 s -- in com.mentefria.backend.PythonRealTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- A linha `[TEMPO]` pode aparecer com `��` no lugar dos acentos: é só a exibição do Maven.
- O `reposicaoContaAMao` é o **seu** teste. Com o Gurobi real, o cenário do `ReposicaoTest` dá: orçamento 100 → **63,22** (granola 25,52; açaí 14,80; banana 12,10; leite condensado 10,80); orçamento 30 → não cobre os mínimos (40,71), **faltam 10,71**. São as contas feitas à mão no tutorial antigo.

**Antes do PR, todos os testes do backend** (com o `MF_PYTHON` ainda definido):

```powershell
.\mvnw test
```

**Você deve ver** (validado, em 27 s; a `main` depois do M3 mais a Fase 5):

```
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.config.SegurancaEErrosTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.Fase2CrudTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.Fase3EstoquePedidoTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.Fase4ProducaoTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.produto.RegraAcaiTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.python.ProcessoPythonTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.PythonRealTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.mentefria.backend.ReposicaoTest
[INFO] Tests run: 83, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Se mais alguma coisa tiver entrado na `main` antes, o total muda. O que importa: `Failures: 0, Errors: 0` e `BUILD SUCCESS`.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `cannot find symbol … ReposicaoRepository` | O commit do Diogo ainda não chegou | `git pull` (Parte 7.2) |
| `cannot find symbol … ChaveOtimizador` | A Fase 4A ainda não está na sua branch | `git fetch origin` e `git merge --no-edit origin/main`; se a 4A ainda não entrou na `main`, espere o M3 |
| `Could not find a valid Docker environment` | O Docker Desktop está fechado | Parte 1.2 |
| `PythonRealTest` com `Skipped: 3` | O `MF_PYTHON` não está definido nesta janela | Rode a linha do `$env:MF_PYTHON` de novo |

Volte para a raiz: `cd ..`

### 7.5 Evidência, PR e merge (seg 02/11, cedo)

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, na branch `feature/backend-reposicao`, com o Docker aberto. Se a janela é nova, rode antes o `[Console]::OutputEncoding = [Text.Encoding]::UTF8`.

```powershell
cd backend
$saida = .\mvnw test "-Dtest=ReposicaoTest"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-11-02_kaue_fase5_mvnw-test.txt
$saida | Select-String "Tests run:|BUILD"
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path
$saida = .\mvnw test "-Dtest=PythonRealTest"
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-11-02_kaue_fase5_python-real.txt
$saida | Select-String "Tests run:|BUILD"
cd ..
git add docs\Sprint_4\evidencias
git commit -m "test: evidencia dos testes da Fase 5"
git push
```

Abra o PR (Parte 2.4). **Revisor:** o **Lucas** (a Fase 6 dele depende deste PR). O Diogo confere o SQL dele.

**Modelo de descrição do PR:**

```
## O que entra
Fase 5: POST /api/reposicao/recomendacao (US14). Service, controller e testes (Kauê);
ReposicaoRepository, consulta 9 (Diogo).

## Como testar
cd backend
.\mvnw test "-Dtest=ReposicaoTest"
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path
.\mvnw test "-Dtest=PythonRealTest"

## Resultado na minha máquina
ReposicaoTest: 8, 0 falhas. PythonRealTest: 3, 0 falhas (63,22 e falta de 10,71).
Suíte inteira (com MF_PYTHON): ___ testes, 0 falhas.

## Avisos
- Lucas: a Fase 6 pode trazer a main agora (Parte 2.6 do tutorial dele).
- Leonardo: a Tela 13 já pode usar a rota real.
```

Depois do merge (Parte 2.5), **avise no grupo:**

```
Fase 5 na main (PR #__). ReposicaoTest 8/8, PythonRealTest 3/3.
Lucas: pode trazer a main para a Fase 6. Leonardo: a Tela 13 já tem a rota real.
```

### 7.6 Perguntas para a Review

1. Por que um pedido cancelado conta no consumo da reposição, mas não no faturamento do dashboard?
2. Por que o saldo enviado ao Python é o **válido**?
3. Quando o Python não é chamado?
4. O que o `ReposicaoTest` testa com um dublê, e o que o `PythonRealTest` testa de verdade?

---

## Parte 8 — Publicação no Render (US20)

**Quem faz o quê (A43):**
- **o Lucas** cria a conta (até sáb 31/10) e, na seg 02/11, faz os cliques no painel. As credenciais da conta são só dele: não as peça nem as use;
- **você** prepara os arquivos, testa a imagem, autoriza o acesso ao repositório (é o dono), guia o Lucas, cria o Administrador, conduz a carga do schema, o seed e o backup, e roda os CTs do deploy;
- **o Diogo** confere o seed de demonstração no banco publicado (seg 02/11, à tarde).

**A URL do banco não sai da conta do Lucas.** A External Database URL tem a senha do banco, e só o Lucas vê o painel. Por isso:
- **na seg 02/11**, os comandos que usam a URL (Partes 8.4, o seed da 8.5 e a 8.7) rodam **no computador do Lucas**, na pasta do projeto dele, com você guiando e o Diogo conferindo o seed;
- **para os backups seguintes** (13/11, semanais e 29/11), combinem **no sábado** uma de duas formas e avisem no grupo: (a) o Lucas roda o `pg_dump` (Parte 8.7) e entrega o arquivo a você por um canal privado, para você testar a restauração; ou (b) o Lucas compartilha a URL com você por um gerenciador de senhas. **Nunca** pelo grupo, num PR ou num arquivo do repositório.

**Antes de segunda, o que só o dono do repositório faz (você):**
1. **GitHub Pages (sex 30 ou sáb 31, com o Leonardo):** no GitHub, *Settings* → *Pages* → *Build and deployment* → *Source*: **GitHub Actions** (tutorial do Leonardo, `docs/Sprint_4/tutoriais/tutorial-leonardo-frontend.md`, Parte 8.2). Sem isso, o *workflow* dele não publica o frontend.
2. **O Render enxergar o repositório (sáb 31, com o Lucas):** o Lucas conecta o GitHub dele ao Render. Como o repositório é **seu**, o GitHub pode pedir que **você** autorize o acesso do Render a `KaueNCarneiro/Mente_Fria`. Testem no sábado: o Lucas vai em **New** → **Blueprint** só até a tela de escolher o repositório, **sem confirmar nada**. Se o repositório não aparecer, é aí que você autoriza. **Não verificado aqui:** depende das contas.

**Decisão A21:** backend em Docker no Render (plano gratuito, região Virginia), PostgreSQL 16 gratuito no Render, frontend no GitHub Pages (tutorial do Leonardo).

**Limites do plano gratuito** (documentação do Render, conferida em 26 e 27/10):
- o **banco expira 30 dias depois de criado**, no mesmo horário; cada conta só pode ter **um** banco gratuito;
- o serviço "dorme" depois de 15 minutos sem uso e leva cerca de 1 minuto para voltar;
- 512 MB de memória e 0,1 de CPU, para o Java e o Python juntos.

**Por isso o deploy definitivo é na seg 02/11 (A41):** o banco vale até **02/12**, depois de 30/11 (a data-limite do professor) e da semana de bugs.

### 8.1 O PR dos arquivos de deploy (sáb 31/10)

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
git checkout main
git pull
git checkout -b chore/deploy-render
$dep = "docs\Sprint_3\referencia\deploy"
Copy-Item "$dep\Dockerfile", "$dep\.dockerignore", "$dep\render.yaml" .
git status --short
```

O `.` no fim do `Copy-Item` quer dizer "a pasta atual", a raiz do repositório.

**Você deve ver** (validado):

```
?? .dockerignore
?? Dockerfile
?? render.yaml
```

#### O que cada arquivo faz

**`Dockerfile`: a receita da imagem, em dois estágios.**

```dockerfile
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /src/backend
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY backend/src src
RUN ./mvnw -q -B -Dmaven.test.skip=true package && cp target/backend-*.jar /src/backend.jar
```

**Estágio 1 (`build`):** uma imagem com o JDK 25 compila o backend com o Maven do projeto.
- Copia primeiro só o `pom.xml` e baixa as bibliotecas. O Docker guarda essa etapa: se só o código mudar, ela não roda de novo, e o build fica mais rápido.
- `-Dmaven.test.skip=true`: os testes não rodam aqui (eles precisam do Docker, e já rodaram no PR).

```dockerfile
FROM python:3.14-slim-trixie
COPY --from=eclipse-temurin:25-jre-noble /opt/java/openjdk /opt/java/openjdk
ARG GUROBIPY_VERSAO=13.0.3
RUN pip install --no-cache-dir "gurobipy==${GUROBIPY_VERSAO}"
RUN useradd --create-home --uid 10001 app
WORKDIR /app
COPY optimization/app optimization/app
COPY --from=build /src/backend.jar backend.jar
USER app
ENV PYTHON_CMD=python3 ... JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
CMD ["java", "-jar", "/app/backend.jar"]
```

**Estágio 2 (a imagem final):** parte de uma imagem com Python 3.14 e copia para dentro só o Java de execução (o JRE).
- Instala **só** o `gurobipy` 13.0.3 (o `pytest` não vai para produção). A licença do `pip` vem junto.
- Cria um usuário sem privilégios (`app`) e roda com ele: se alguém explorar uma falha, não tem poder de administrador no container.
- Copia, para dentro de `/app` (o `WORKDIR`), só os módulos Python (`optimization/app`) e o `.jar` do estágio 1. O JDK e o código-fonte ficam para trás: a imagem fica menor.
- `MaxRAMPercentage=50`: o Java usa no máximo metade da memória, e sobra espaço para o processo do Gurobi.
- `CMD [...]` em forma de lista: o Java recebe direto o aviso de desligamento do Render e fecha com calma.
- **Nenhum segredo entra na imagem.** Banco e `JWT_SECRET` chegam por variáveis de ambiente do Render.

**`.dockerignore`: o que pode entrar no build.**

```
*
!backend/
!optimization/app/
backend/target/
backend/.env
...
```

A primeira linha (`*`) exclui **tudo**; as linhas com `!` liberam só o `backend` e o `optimization/app`. Assim o `.git`, o `docs`, o `frontend`, o `venv` e, principalmente, o `backend/.env` (com senha) **nunca** entram na imagem.

**`render.yaml`: o *blueprint*.**

| Trecho | O que diz ao Render |
|---|---|
| `type: web`, `runtime: docker`, `plan: free`, `region: virginia` | Um serviço web, construído pelo `Dockerfile`, no plano gratuito, na região mais próxima do Brasil entre as disponíveis |
| `healthCheckPath: /api/saude` | A rota que o Render chama para saber se o serviço está vivo |
| `DB_HOST` … `DB_PASSWORD` com `fromDatabase` | Os dados de conexão vêm do banco criado pelo próprio *blueprint* |
| `JWT_SECRET` com `generateValue: true` | O Render gera uma chave aleatória. Ninguém precisa digitar nem guardar |
| `CORS_ALLOWED_ORIGIN` com `sync: false` | O Render **pergunta** o valor na criação (a origem do Pages) |
| `OTIMIZADOR_HABILITADO: "true"` | O solver ligado, com a licença do `pip` (A38). **Não há `GRB_*`** |
| `databases:` `postgresMajorVersion: "16"` | O PostgreSQL 16, o mesmo do projeto e dos testes |

**Commit, push e PR:**

```powershell
git add Dockerfile .dockerignore render.yaml
git commit -m "chore: deploy no Render (Dockerfile, .dockerignore e render.yaml)"
git push -u origin chore/deploy-render
```

**Revisor:** o Lucas (é a conta dele). No PR, cole o resultado da Parte 8.2. Precisa estar na `main` **antes** do deploy de segunda.

### 8.2 Testar a imagem na sua máquina (CT67)

**Objetivo:** ver a imagem subir com os **mesmos limites do Render** (512 MB e 0,1 CPU), antes de segunda.
**Quando:**
- **sáb 31/10, com o PR do deploy:** só os passos 1, 2 e 4 (a imagem sobe com os limites e responde `saude`). A recomendação (Fase 4A) e a reposição (Fase 5) ainda não estão na `main`, então o passo 3 não funciona nesse dia;
- **ter 03/11 (CT67):** os quatro passos, com tudo na `main`.

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, com o Docker aberto. No sábado, na branch `chore/deploy-render`. Na terça, na `main` atualizada: `git checkout main`, `git pull` e `git status` (limpo). Os containers têm nomes próprios: **o `mente-fria-db` não é usado.**

**1. Montar a imagem:**

```powershell
docker build -t mente-fria-backend .
```

**O que faz:** monta a imagem a partir do `Dockerfile` da pasta atual (`.`) e dá a ela o nome `mente-fria-backend`.

**Você deve ver** no fim (validado):

```
#24 naming to docker.io/library/mente-fria-backend:latest done
...
#24 DONE 6.1s
```

Aqui levou 78 s, com as imagens-base já baixadas. Na primeira vez, o Docker baixa o Java, o Python e as bibliotecas do Maven, e pode levar vários minutos. O `docker images mente-fria-backend` mostra o tamanho: 584 MB.

**2. Um banco descartável com o DDL, e a imagem rodando com os limites do Render:**

```powershell
docker network create mf-imagem-rede
docker run -d --name mf-imagem-db --network mf-imagem-rede -e POSTGRES_PASSWORD=teste -e POSTGRES_DB=mente_fria postgres:16
Start-Sleep -Seconds 10
docker cp bd\Script_DDL.sql mf-imagem-db:/tmp/ddl.sql
docker exec mf-imagem-db psql -U postgres -d mente_fria -q -v ON_ERROR_STOP=1 -f /tmp/ddl.sql
"ddl exit: $LASTEXITCODE"
docker run -d --name mf-imagem-app --network mf-imagem-rede --memory=512m --cpus=0.1 -p 10000:10000 -e PORT=10000 -e DB_HOST=mf-imagem-db -e DB_PORT=5432 -e DB_NAME=mente_fria -e DB_USER=postgres -e DB_PASSWORD=teste -e JWT_SECRET=um-segredo-so-para-teste-local-com-mais-de-32-caracteres -e CORS_ALLOWED_ORIGIN=<ORIGEM_DO_PAGES> mente-fria-backend
```

| Comando | O que faz |
|---|---|
| `docker network create mf-imagem-rede` | Cria uma rede só para os dois containers se enxergarem pelo nome |
| `docker run -d --name mf-imagem-db … postgres:16` | Um PostgreSQL **descartável**, só para este teste. O `-d` roda em segundo plano e mostra um código longo (o id do container): é normal |
| `Start-Sleep -Seconds 10` | Espera o banco ficar pronto. Sem a espera, o próximo comando falha com `connection to server on socket … failed` (verificado) |
| `docker cp` + `docker exec … psql … -f` | Copia o DDL para dentro do banco descartável e o executa |
| `docker run … --memory=512m --cpus=0.1 …` | Roda a imagem com os limites do plano gratuito, na porta 10000, com as variáveis que o Render daria (inclusive a origem do Pages, para o CT69) |

**Você deve ver:** `ddl exit: 0`. O `JWT_SECRET` daqui é só para este teste local. No Render, ele é gerado sozinho.

**No Terminal B**, acompanhe o log: `docker logs -f mf-imagem-app`. Espere `Started BackendApplication` (Ctrl+C sai só do log; o container continua). Depois, no Terminal A:

```powershell
Invoke-RestMethod http://localhost:10000/api/saude
docker inspect mf-imagem-app --format "{{.State.OOMKilled}}"
docker stats --no-stream mf-imagem-app
```

**Você deve ver** (validado):
- no log, depois de 55 s: `Started BackendApplication in 43.602 seconds (process running for 51.449)`. É lento por causa do 0,1 de CPU;
- `saude` com `banco` `ok` e `status` `ok`;
- `false` no `OOMKilled` (o container **não** foi morto por falta de memória);
- no `docker stats`, algo como `115.1MiB / 512MiB`.

**3. Ponta a ponta dentro da imagem** (o mesmo que a Review vai mostrar). Precisa da função `Chamar`, a mesma do tutorial do Lucas (Parte 3.7). Cole uma vez por janela:

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
```

| Parte da `Chamar` | O que faz |
|---|---|
| `$metodo`, `$url`, `$corpo`, `$token` | O que chamar, onde, com qual corpo (opcional) e com qual token (opcional) |
| `[Text.Encoding]::UTF8.GetBytes(…)` | Manda o corpo em UTF-8, com os acentos certos |
| `try { … } catch { … }` | No PowerShell 5.1, uma resposta `4xx` ou `5xx` vira erro. O `catch` pega o código e a mensagem em vez de parar |
| `[pscustomobject]@{ status; corpo }` | Devolve sempre o código HTTP e o JSON da resposta |

```powershell
$api = "http://localhost:10000/api"
(Chamar GET "$api/teste/python").status
Chamar POST "$api/auth/registro" @{ nome = "Dono"; email = "dono@acai.com"; senha = "senha-local-1234" } | Out-Null
$tokAdmin = (Chamar POST "$api/auth/login" @{ email = "dono@acai.com"; senha = "senha-local-1234" }).corpo.token
(Chamar POST "$api/usuarios" @{ nome = "Func"; email = "func@acai.com"; senha = "senha-local-1234" } $tokAdmin).status
docker cp bd\seed_demo.sql mf-imagem-db:/tmp/seed_demo.sql
docker exec mf-imagem-db psql -U postgres -d mente_fria -q -v ON_ERROR_STOP=1 -f /tmp/seed_demo.sql | Out-Null
"seed exit: $LASTEXITCODE"
$rec = Chamar POST "$api/producao/recomendacao" $null $tokAdmin
(($rec.corpo.recomendacao | ForEach-Object { $_.quantidade }) -join "/") + "  lucro " + $rec.corpo.margem_estimada
$rep = Chamar POST "$api/reposicao/recomendacao" @{ periodo_dias = 60; orcamento = 100 } $tokAdmin
"$($rep.corpo.custo_total) $($rep.corpo.itens[0].nome) $($rep.corpo.itens[0].quantidade_sugerida)"
docker stats --no-stream mf-imagem-app
```

**Você deve ver** (validado):

```
401
201
seed exit: 0
151/1/0  lucro 1011.71
11.10 Morango 0.74
... mf-imagem-app   0.10%     126.5MiB / 512MiB ...
```

- `401`: o eco não existe fora do perfil `dev`, e a imagem roda sem perfil, como no Render.
- `201`: o Funcionário criado pelo Administrador.
- **151/1/0** (2,7 s) e o morango 0,74 kg, R$ 11,10 (2,4 s): o Gurobi rodando **dentro** do container, com a licença do `pip`.
- O primeiro login leva uns 10 s (é o 0,1 de CPU calculando o hash da senha).
- A senha `senha-local-1234` é só deste banco descartável. **Nunca** no Render.

**4. Limpar no fim** (só os containers deste teste):

```powershell
docker rm -f -v mf-imagem-app mf-imagem-db
docker network rm mf-imagem-rede
```

O `-v` apaga também o volume com os dados do banco descartável. Sem ele, cada teste deixa um volume esquecido no Docker (verificado). Para liberar os 584 MB da imagem depois do CT67: `docker rmi mente-fria-backend`.

| Se der errado (Parte 8.2) | O que fazer |
|---|---|
| `Conflict. The container name "/mf-imagem-db" is already in use` | Sobrou um container de um teste anterior: `docker rm -f -v mf-imagem-app mf-imagem-db` e comece o passo 2 de novo |
| `network with name mf-imagem-rede already exists` | `docker network rm mf-imagem-rede` e rode o passo 2 de novo |
| `port is already allocated` (porta 10000) | Outro programa usa a porta 10000. Troque `-p 10000:10000` por `-p 10001:10000` e use `http://localhost:10001` |
| O log não chega a `Started BackendApplication` em 3 minutos, ou o `OOMKilled` dá `true` | É o que aconteceria no Render. Guarde o log (`docker logs mf-imagem-app \| Out-File -Encoding utf8 "$HOME\log-imagem.txt"`) e avise o grupo antes de segunda |
| O build para no `./mvnw` (`not found` ou `bad interpreter`) | O `mvnw` ficou com fim de linha do Windows. O `backend/.gitattributes` força o formato certo; se você editou o arquivo, desfaça com `git checkout -- backend/mvnw` |

### 8.3 Seg 02/11, à tarde: criar tudo pelo *blueprint* (o Lucas clica, você guia)

**Onde:** no painel do Render, na conta do Lucas. Ele clica; você acompanha ao lado (ou em chamada, com a tela dele compartilhada).

**Pré-requisitos:**
- o PR da Parte 8.1 na `main`. Pelo roteiro, nessa hora as Fases 1 a 6 também já estão lá;
- o teste do sábado feito: o Render enxerga o repositório.

1. O Lucas entra no Render, na conta dele (criada até sáb 31/10).
2. No painel: **New** → **Blueprint** → escolher o repositório `KaueNCarneiro/Mente_Fria`. O Render encontra o `render.yaml` na raiz.
3. Quando ele perguntar o `CORS_ALLOWED_ORIGIN`, informar `<ORIGEM_DO_PAGES>` (**sem barra no fim e sem caminho**).
4. Revisar a lista do que será criado (um serviço web e um banco) e confirmar.
5. **Anotar a hora exata em que o banco foi criado.** Ele expira 30 dias depois, no mesmo horário (02/12).

Esses passos seguem a documentação do Render (*blueprint*, lida em 26/10). **Não foram executados aqui**, porque dependem da conta. A tela real pode ter nomes um pouco diferentes.

**Durante o primeiro build** (alguns minutos), confira com o Lucas, no painel do serviço:
- a aba **Environment** tem `DB_*`, `JWT_SECRET`, `CORS_ALLOWED_ORIGIN` e `OTIMIZADOR_HABILITADO=true`, e **nenhuma** `GRB_*`;
- a aba **Logs** termina com `Started BackendApplication`.

6. **Assim que o serviço estiver no ar, mande a `<URL_DO_SERVICO>` ao Leonardo.** Ele a põe na constante `PRODUCAO` do `config.js` (tutorial dele, Parte 8.3), num PR, e o Pages publica as telas apontando para o Render.

**Deploy automático:** por padrão, cada merge na `main` publica de novo. O que entrar depois (a US26, se passar no *gate*, e as correções da semana de bugs) aparece sozinho no Render.

**Agora, sem demora, as Partes 8.4 e 8.5** (schema e Administrador). Não divulgue a URL do serviço a ninguém fora do grupo antes disso.

### 8.4 Criar o schema no banco do Render (uma vez)

**Por quê:** o backend **não** cria tabelas. Sem o DDL, o banco do Render está vazio.
**Onde:** no **computador do Lucas** (a URL do banco não sai da conta dele), Terminal A, na pasta do projeto dele, com o Docker aberto. Você guia.

**Primeiro, a `main` atualizada:** o `bd\Script_DDL.sql` tem de ser o da Sprint 4 (o PR do banco do Diogo). Numa branch antiga, o DDL seria o da Sprint 1.

```powershell
[Console]::OutputEncoding = [Text.Encoding]::UTF8
git checkout main
git pull
git status
```

**Você deve ver** no fim: `nothing to commit, working tree clean`.

1. No painel do **banco** (não do serviço), página **Info**, o Lucas copia a **External Database URL**. Ela tem o usuário e a **senha**: não cole em nenhum arquivo, chat ou PR.
2. Na mesma janela:

> **Validado** com um PostgreSQL de teste configurado para **só aceitar SSL**, como o Render (na validação, a URL foi atribuída direto, sem o `Read-Host`). Contra o Render de verdade, não foi testado.

```powershell
$URL = Read-Host "Cole a External Database URL"
$URL = $URL + "?sslmode=require"
docker run --rm -v "${PWD}\bd:/bd" postgres:16 psql $URL -v ON_ERROR_STOP=1 -q -f /bd/Script_DDL.sql
docker run --rm postgres:16 psql $URL -tA -c "SELECT nome, quantidade_estoque FROM ingrediente WHERE id = 1;"
```

| Parte | O que faz |
|---|---|
| `Read-Host "…"` | Pede para colar a URL. O que se cola **não** fica no histórico de comandos do PowerShell, ao contrário de digitar a URL direto na linha |
| `$URL + "?sslmode=require"` | Acrescenta a exigência de conexão criptografada (o Render recusa conexão externa sem ela). **Não** escreva `"$URL?sslmode=require"`: o PowerShell entende `?sslmode` como parte do nome da variável e a URL some (verificado) |
| `docker run --rm … postgres:16 psql …` | Usa o `psql` de dentro de um container descartável (`--rm`): não é preciso instalar o PostgreSQL |
| `-v "${PWD}\bd:/bd"` | Deixa a pasta `bd` do projeto visível dentro do container, em `/bd`. O `psql` lê o **arquivo**, e os acentos chegam intactos (pelo pipe do PowerShell, eles se perderiam) |
| `-v ON_ERROR_STOP=1` | Para no primeiro erro, em vez de seguir com o banco pela metade |

**Você deve ver** (validado): o primeiro `docker run` não mostra nada (o `-q` deixa o `psql` quieto), e o segundo mostra:

```
Açaí (polpa)|0.000
```

É o ingrediente 1 do DDL da Sprint 4, com estoque zero, **com os acentos**. **Deixe esta janela aberta:** o seed (8.5) e o backup (8.7) usam a mesma `$URL`.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `Açaí (polpa)\|40.000` | O DDL carregado foi o da Sprint 1 (a pasta estava numa branch desatualizada) | **Pare antes do seed** e chame o Diogo: o banco do Render precisa ser limpo e o DDL certo carregado |
| `FATAL: no pg_hba.conf entry … no encryption` | A conexão foi sem SSL (verificado com o banco de teste) | Confira a linha `$URL = $URL + "?sslmode=require"` |
| `password authentication failed` | A URL foi copiada pela metade | Copie de novo, com o botão de copiar do painel |
| `could not translate host name` | Foi copiada a *Internal* Database URL, que só funciona dentro do Render | Use a **External** Database URL |
| `relation "…" already exists` | O DDL já tinha sido carregado | Não rode de novo: o schema já está lá |

### 8.5 O Administrador (imediatamente), o Funcionário e o seed

**Por que o Administrador primeiro e já:** a rota `POST /api/auth/registro` é pública e transforma **o primeiro** que se cadastrar em Administrador. Depois disso, ela responde `409` para todo mundo (`UsuarioService.registrarAdministrador`; caso de teste CT01B). Entre o fim da Parte 8.4 e o seu cadastro, qualquer pessoa que achasse a URL poderia virar Administrador. Por isso, cadastre-se **logo depois do schema**, antes de divulgar a URL.

**Onde:** Terminal A, na sua máquina ou na do Lucas (não precisa da URL do banco). Precisa da função `Chamar` (Parte 8.2, passo 3): cole-a antes, se a janela for nova.

**Senhas:** fortes, que **não estão em nenhum arquivo do repositório**. O repositório é público: **nunca** use no Render a `senha1234` nem a `senha-local-1234` dos tutoriais. Guarde as duas senhas num gerenciador de senhas e passe ao grupo só por mensagem privada.

```powershell
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$api = "<URL_DO_SERVICO>/api"
$senhaAdmin = Read-Host "Senha do Administrador"
(Chamar POST "$api/auth/registro" @{ nome = "<NOME_DO_ADMIN>"; email = "<EMAIL_DO_ADMIN>"; senha = $senhaAdmin }).status
(Chamar POST "$api/auth/registro" @{ nome = "Teste"; email = "teste@exemplo.com"; senha = "qualquer-coisa-1234" }).status
$tokAdmin = (Chamar POST "$api/auth/login" @{ email = "<EMAIL_DO_ADMIN>"; senha = $senhaAdmin }).corpo.token
$senhaFunc = Read-Host "Senha do Funcionário"
(Chamar POST "$api/usuarios" @{ nome = "<NOME_DO_FUNC>"; email = "<EMAIL_DO_FUNC>"; senha = $senhaFunc } $tokAdmin).status
```

| Linha | O que faz |
|---|---|
| `[Net.ServicePointManager]…Tls12` | Obriga o PowerShell 5.1 a usar TLS 1.2, que o Render exige em HTTPS |
| `$senhaAdmin = Read-Host …` | Pede a senha sem deixá-la no histórico de comandos. Ela aparece na tela enquanto você digita: confira que ninguém está olhando |
| O primeiro `registro` | Cria o Administrador |
| O segundo `registro` | A prova de que a porta fechou: ninguém mais vira Administrador |
| `login` e `usuarios` | O Administrador cria o Funcionário |

**Você deve ver:**

```
201
409
201
```

O `201` e o `409` do registro foram validados no tutorial do Lucas (Parte 4, CT01B); o `201` do Funcionário, na imagem local (Parte 8.2). Contra o Render, não foi testado. A primeira chamada pode levar ~1 minuto se o serviço estiver "dormindo".

| Se der errado | O que fazer |
|---|---|
| O **primeiro** registro dá `409` | Alguém se cadastrou como Administrador antes de você. **Pare**, avise o grupo e chame o Diogo: o banco do Render ainda não tem nada de valor, então ele o limpa e vocês repetem as Partes 8.4 e 8.5 |
| `400` | A senha ou o e-mail não passou na validação: veja a mensagem em `(Chamar …).corpo.campos` |
| `Could not create SSL/TLS secure channel` | Faltou a primeira linha |

**O seed de demonstração** (do Diogo; roda no **computador do Lucas**, na janela da Parte 8.4, com o Diogo conferindo a saída). Ele precisa do Administrador e do Funcionário já cadastrados:

```powershell
if (-not $URL) { $URL = (Read-Host "Cole a External Database URL") + "?sslmode=require" }
docker run --rm -v "${PWD}\bd:/bd" postgres:16 psql $URL -v ON_ERROR_STOP=1 -f /bd/seed_demo.sql
```

A primeira linha só pede a URL de novo se a janela for nova (`-not $URL` = "a variável está vazia").

**Você deve ver** (validado pela URL do banco de teste, com os dois usuários já cadastrados):

```
BEGIN
CREATE FUNCTION
DO
 id | nome | saldo | soma_linhas
----+------+-------+-------------
(0 rows)

COMMIT
```

A tabela vazia é a conferência do próprio seed: nenhum ingrediente com o saldo diferente da soma das linhas. Se o seed parar com erro antes do `COMMIT`, nada foi gravado: o Diogo investiga.

**Logo depois do seed, faça o backup** (Parte 8.7), na mesma janela.

### 8.6 Conferir o ambiente publicado (CT67–CT70 e CT72)

**Onde:** Terminal A, na sua máquina. **Antes de qualquer chamada ao Render**, no PowerShell 5.1:

```powershell
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
```

Sem essa linha, algumas máquinas dão `Could not create SSL/TLS secure channel`: o Render exige TLS 1.2.

| CT | Quando | O que | Esperado | Evidência |
|---|---|---|---|---|
| — | Seg 02/11 | `Invoke-RestMethod <URL_DO_SERVICO>/api/saude` | `status ok`, `banco ok` (a primeira chamada pode levar ~1 min) | — |
| — | Seg 02/11 | `<URL_DO_SERVICO>/api/teste/python` no navegador | `401`: o eco só existe no perfil `dev` | — |
| — | Seg 02/11 | O Lucas abre a aba **Logs** do serviço | Sem `ERROR`; nenhuma senha, URL de banco ou token impressos | — |
| CT67 | Ter 03/11 | A imagem local com todas as fases (Parte 8.2, os 4 passos) | Sobe com 512 MB e 0,1 CPU, sem `OOMKilled`; 151/1/0 e 0,74 | `2026-11-03_kaue_ct67.txt` |
| CT69 | Ter 03/11 | CORS: a pergunta `OPTIONS` com a origem do Pages e com outra origem (abaixo) | `200` com `Access-Control-Allow-Origin` para o Pages; `403` para a outra | `2026-11-03_kaue_ct69.txt` |
| CT70 | Ter 03/11 | Deixe o serviço 15 min sem uso; abra o Pages e faça login | A tela espera (até 90 s) e funciona, sem "O servidor demorou para responder" | Print com a URL e o horário |
| CT72 | Ter 03/11, em horário combinado com o grupo | O Lucas troca `OTIMIZADOR_HABILITADO` para `false` no painel e salva (o serviço reinicia). Você abre, **pelo Pages**, logado como Administrador, a Tela 12 (Produção) e a Tela 13 (Reposição) | As duas mostram "A recomendação está disponível só na demonstração local da equipe."; o resto funciona (validado na imagem local: `503` com essa mensagem e `GET /api/ingredientes` `200`). **Depois, de volta para `true`** | Prints das duas telas |
| CT68 | **Qua 04/11, depois das 18h** | O fluxo inteiro do Termo pela URL do Pages, nos dois perfis | Recomendação **151/1/0**; reposição; dashboard; marketing | Prints com a URL visível |

**Sobre o CT72:**
- é feito pelas **telas**, e não pelo PowerShell, para você não precisar digitar a senha do Administrador num comando;
- cada reinício deixa o serviço fora do ar por 1 a 2 minutos e faz ele "esquecer" a última recomendação (o dashboard fica sem plano até alguém gerar outro). Por isso, combine o horário com o grupo.

**O CT69 pelo PowerShell:**

```powershell
$h = @{ Origin = "<ORIGEM_DO_PAGES>"; "Access-Control-Request-Method" = "POST" }
$r = Invoke-WebRequest -Method Options -Uri "<URL_DO_SERVICO>/api/auth/login" -Headers $h -UseBasicParsing
"$($r.StatusCode) $($r.Headers['Access-Control-Allow-Origin'])"
$h = @{ Origin = "https://outro-site.example"; "Access-Control-Request-Method" = "POST" }
try { Invoke-WebRequest -Method Options -Uri "<URL_DO_SERVICO>/api/auth/login" -Headers $h -UseBasicParsing | Out-Null; "outra origem: passou (ERRADO)" } catch { "outra origem: $([int]$_.Exception.Response.StatusCode)" }
```

| Parte | O que faz |
|---|---|
| `-Method Options` com `Origin` e `Access-Control-Request-Method` | Imita a "pergunta" (*preflight*) que o navegador faz antes de um `POST` vindo de outro site |
| `try { … } catch { … }` | A resposta `403` vira erro no PowerShell 5.1; o `catch` mostra o código |

**Você deve ver** (validado contra a imagem local da Parte 8.2, com `http://localhost:10000` no lugar da `<URL_DO_SERVICO>`):

```
200 https://kauencarneiro.github.io
outra origem: 403
```

Guarde as saídas e os prints em `docs/Sprint_4/evidencias/`, com os nomes da tabela, e entregue por PR (Parte 9.5).

**Se a recomendação falhar no Render** (memória ou tempo esgotado):
1. guarde o log e avise o grupo;
2. o Lucas liga a reserva B1 (`false` no painel);
3. a otimização passa a ser mostrada **na sua máquina** (demonstração local): siga `docs/Sprint_3/sprint-3-licenca-gurobi.md`, §5, e ensaie uma vez antes da Review.

**Nunca** coloque a sua WLS no Render para "resolver" (A38).

### 8.7 Backup e restauração (pedido do professor, A32)

**Por quê:** o professor pediu um backup **com a restauração testada**. O banco gratuito expira em 02/12; o backup é o que permite recriar tudo se algo der errado.

**Quando (A41):**
- logo depois do seed (seg 02/11);
- ao fim da semana de bugs (13/11);
- uma vez por semana;
- e em **29/11**, véspera da data-limite.

**Onde:** na seg 02/11, no **computador do Lucas**, na janela da Parte 8.4. Nos backups seguintes, conforme o combinado do sábado (início da Parte 8): ou o Lucas faz o backup e entrega o arquivo a você para a restauração, ou você roda tudo com a URL que ele compartilhou.

**Onde guardar:** **fora** da pasta do projeto. O arquivo tem os dados e os hashes das senhas, e o `.gitignore` não tem uma regra para ele: dentro do repositório, um `git add .` descuidado o publicaria.

> **Validado** com o banco de teste que exige SSL (o mesmo da Parte 8.4, com o DDL e o seed): backup, restauração num banco descartável e restauração num banco novo de **outro dono**. Na validação, a pasta ficou na área temporária, e não em `$HOME`. **Contra o Render, não foi testado.**

**Fazer o backup:**

```powershell
if (-not $URL) { $URL = (Read-Host "Cole a External Database URL") + "?sslmode=require" }
$pasta = "$HOME\mente-fria-backup"
$arq = "mente_fria_$(Get-Date -Format yyyy-MM-dd).dump"
New-Item -ItemType Directory -Force $pasta | Out-Null
docker run --rm -v "${pasta}:/backup" postgres:16 pg_dump $URL -Fc --no-owner --no-privileges -f "/backup/$arq"
Get-ChildItem $pasta
```

| Parte | O que faz |
|---|---|
| `if (-not $URL) { … }` | Só pede a URL se a janela for nova |
| `$HOME\mente-fria-backup` | Uma pasta na pasta do usuário, fora do repositório |
| `$arq = "mente_fria_$(Get-Date -Format yyyy-MM-dd).dump"` | O nome do arquivo com a data do dia, por exemplo `mente_fria_2026-11-02.dump`. Cada backup ganha o seu |
| `"${pasta}:/backup"` | Monta essa pasta dentro do container. As chaves em `${pasta}` são obrigatórias: sem elas, o PowerShell confunde o `:` com outra coisa |
| `pg_dump … -Fc` | Faz a cópia no formato "custom" do PostgreSQL, compactado, próprio para o `pg_restore` |
| `--no-owner --no-privileges` | Não grava quem é o dono nem as permissões: um banco novo do Render terá outro usuário |

**Você deve ver** (validado): o `pg_dump` não mostra nada, e o `Get-ChildItem` lista o arquivo do dia, com cerca de 22 KB (com os dados de demonstração).

**Testar a restauração** num banco local descartável (**não** use o `mente-fria-db`), na mesma janela (usa `$pasta`, `$arq` e `$URL`):

```powershell
docker run -d --name mf-restore -e POSTGRES_PASSWORD=teste -e POSTGRES_DB=mente_fria postgres:16
Start-Sleep -Seconds 10
docker cp "$pasta\$arq" mf-restore:/tmp/b.dump
docker exec mf-restore pg_restore -U postgres -d mente_fria --no-owner --no-privileges --exit-on-error /tmp/b.dump
"pg_restore exit: $LASTEXITCODE"
docker exec mf-restore psql -U postgres -d mente_fria -tA -c "SELECT count(*) FROM pedido;"
docker run --rm postgres:16 psql $URL -tA -c "SELECT count(*) FROM pedido;"
docker exec mf-restore psql -U postgres -d mente_fria -tA -c "SELECT nome FROM ingrediente WHERE id = 1;"
docker rm -f -v mf-restore
```

Se o arquivo veio do Lucas (você não tem a URL), defina antes `$pasta` e `$arq` com a pasta e o nome do arquivo que ele mandou, pule a linha do `psql $URL` e peça a ele o número de pedidos do Render na mesma hora do backup.

**Você deve ver** (validado logo depois do seed):

```
pg_restore exit: 0
6
6
Açaí (polpa)
```

O `pg_restore` sem erro, o **mesmo número de pedidos** nas duas consultas (a restaurada e a do Render) e o acento preservado. Mais tarde, com pedidos da semana de bugs, o número será outro, mas os dois têm de ser iguais. Sem o `Start-Sleep`, o `pg_restore` falha com `connection to server on socket … failed` (verificado). Registre em `docs/Sprint_4/evidencias/` só os números e a data (**nunca** o arquivo `.dump`). É o critério do adendo, item 5.

| Se der errado | O que fazer |
|---|---|
| `Conflict. The container name "/mf-restore" is already in use` | Sobrou de um teste anterior: `docker rm -f -v mf-restore` e rode o bloco de novo |
| `connection to server on socket … failed` | O banco descartável ainda estava iniciando. Espere uns segundos e rode de novo só o `pg_restore` e o que vem depois |
| `Cannot find path` no `docker cp` | `$pasta` ou `$arq` vazios (janela nova): defina os dois de novo |

**Restaurar num banco novo do Render** (se o banco expirar ou for recriado): crie o banco, pegue a nova URL e rode o `pg_restore` com `-d` apontando para ela, em vez do DDL e do seed:

```powershell
$URL_NOVO = (Read-Host "Cole a External Database URL do banco novo") + "?sslmode=require"
$pasta = "$HOME\mente-fria-backup"
$arq = "mente_fria_2026-11-02.dump"
docker run --rm -v "${pasta}:/backup" postgres:16 pg_restore -d $URL_NOVO --no-owner --no-privileges --exit-on-error "/backup/$arq"
"pg_restore exit: $LASTEXITCODE"
```

Troque o nome em `$arq` pelo backup mais recente (`Get-ChildItem $pasta` lista todos).

**Validado** com um banco novo de outro dono, no banco de teste: `pg_restore exit: 0`, os 6 pedidos, e um cadastro novo depois da restauração recebeu o `id` seguinte, sem colisão.

### 8.8 Depois do deploy

- Passe ao Lucas a **URL do serviço** e a **hora de criação do banco**: ele põe no README, com a data de expiração (Parte 10 do tutorial dele). A URL do serviço pode ir para o README; a **URL do banco, nunca**.
- O serviço gratuito "dorme": antes de uma demonstração, abra a URL uns 2 minutos antes.

### 8.9 Perguntas para a Review

1. Por que a imagem tem dois estágios?
2. Por que o `.dockerignore` começa excluindo tudo?
3. Como o banco do Render ganhou as tabelas, se o backend não as cria?
4. Por que o deploy definitivo ficou para 02/11, e não para o sábado?
5. Como você provaria que o backup funciona?

---

## Parte 9 — Facilitação (Scrum Master)

### 9.1 A daily de 10 minutos (seg a sáb)

Cada um responde três perguntas: **o que fiz, o que vou fazer, o que me bloqueia**. Bloqueio → resolver **no mesmo dia** (regra da equipe). Registre curto, por exemplo num arquivo por semana em `docs/Sprint_4/`:

```
## 31/10 (sáb)
- Kauê: fiz P+Q (PR #__). Hoje: PR do deploy, começar a Fase 5. Bloqueio: nenhum.
- Lucas: ...
- Decidido: ...
```

### 9.2 Um só `backend/.env.example` (G8, sex 30/10)

**Por quê:** a `main` tem dois arquivos de exemplo. O `backend/.env.example` (com ponto) tem só 5 variáveis. O `backend/env.example` (sem ponto) tem comentários e as variáveis planejadas. Quem segue um tutorial não sabe qual copiar.

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
git checkout main
git pull
git checkout -b chore/env-example-unico
git mv -f backend\env.example backend\.env.example
git status --short
notepad backend\.env.example
```

| Comando | O que faz |
|---|---|
| `git mv -f origem destino` | Move o arquivo **e avisa o Git**. O `-f` deixa substituir o `.env.example` que já existe |

**Você deve ver** (validado):

```
M  backend/.env.example
D  backend/env.example
```

No Bloco de Notas, **apague tudo** e cole o texto abaixo. Ele junta os comentários úteis do `env.example` com todas as variáveis que o backend da Sprint 4 lê (os nomes e os padrões vêm do `application.properties` da referência). **Revise antes de salvar: o arquivo é seu.**

```
# Mente Fria — variáveis de ambiente do backend.
# Copie este arquivo para ".env" NA MESMA PASTA (backend) e preencha os valores.
# NUNCA comite o ".env" real (senhas e chaves). Só este ".env.example" vai para o Git.
# Salve o ".env" em UTF-8 SEM BOM. Nos caminhos, use "/" (no .env, "\" é caractere especial).

# --- Banco (obrigatórias) ---
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mente_fria
DB_USER=postgres
# Mesma senha usada em POSTGRES_PASSWORD ao criar o container do banco.
DB_PASSWORD=

# --- Login (obrigatória) ---
# Chave que assina os tokens (JWT): 32 ou mais caracteres aleatórios, diferente em cada ambiente.
JWT_SECRET=

# --- Opcionais: se faltarem, valem os padrões do application.properties ---
CORS_ALLOWED_ORIGIN=http://127.0.0.1:5500,http://localhost:5500
# Python do ambiente do projeto, a partir da pasta backend (sem esta linha, o padrão é "python").
PYTHON_CMD=../optimization/venv/Scripts/python.exe
OTIMIZADOR_SCRIPT=../optimization/app/otimizador.py
REPOSICAO_SCRIPT=../optimization/app/reposicao.py
OTIMIZADOR_TIMEOUT_SEGUNDOS=10
# Chave de reserva (plano B1): false faz produção e reposição responderem 503 sem chamar o Python.
OTIMIZADOR_HABILITADO=true
```

Salve (**Ctrl+S**; confira **UTF-8** na barra de baixo, e não "UTF-8 com BOM") e feche.

```powershell
git add backend
git commit -m "chore: um unico backend/.env.example com as variaveis da Sprint 4"
git push -u origin chore/env-example-unico
```

**Revisores:** o Lucas e o Diogo. **Avise no grupo:** quem for criar o `.env` daqui para frente copia o `backend\.env.example`. O `.env` de quem já tem continua valendo.

### 9.3 Registro da perda do histórico do Git (até sáb 31/10)

Meia página em `docs/Sprint_3/` (por exemplo, `sprint-3-registro-historico-git.md`) com a data, o motivo (conflitos de estrutura) e o que foi preservado. Para os fatos:

```powershell
git log --reverse --format="%h %ad %an %s" --date=short | Select-Object -First 3
git rev-list --count HEAD
```

**Você deve ver** (validado na `main` de 28/10):

```
9e02498 2026-09-27 Lucas Bassi first commit
e827e82 2026-09-30 KaueNCarneiro feat: Comunicacao do python, Configuração API Gurobi, Adição de elementos no gitignore
c703336 2026-09-30 KaueNCarneiro fix: corrige padrão do .gitignore para __pycache__ aninhado
```

A primeira linha é o commit mais antigo que restou. O segundo comando conta os commits da branch atual.

### 9.4 As outras tarefas da facilitação

| # | Tarefa | Quando | Evidência |
|---|---|---|---|
| 1 | **Aprovar a seção da US26 no contrato** (se a US26 passar no *gate* da manhã). A seção é escrita pelo Diogo; você aprova como **dono do contrato** (A43) | Seg 02/11 | Aprovação no PR do Diogo |
| 2 | **CT20:** um colega sobe o projeto **do zero, só com o README**, e você anota onde ele travou | Qua 04/11 | Relato e, se preciso, PR no README |
| 3 | **Retrospectiva** das Sprints 2 (nunca feita), 3 e 4, juntas. Registre a contradição: as contribuições individuais da Sprint 2 marcam "Retrospectiva" como feita, mas o relatório diz que não houve | Qui 05/11 | Ata no padrão de `docs/Sprint_1/sprint-1-ata-retrospectiva.md` |
| 4 | **Ensaio da Review** e a **Review** com demonstração (o fluxo do §1 do roteiro da Sprint 4, nos dois perfis). É a própria entrega ao professor | Ensaio qui 05/11; Review sex 06/11 | Ata e prints |
| 5 | **Relatório da Sprint 3:** o rascunho está em `docs/Sprint_3/sprint-3-relatorio-entrega.md`; revisar e entregar **junto com a Sprint 4** (A43). **Relatório da Sprint 4** em `docs/Sprint_4/`: planejado × entregue (use a matriz), evidências, riscos e decisões. Sem `[PREENCHER]` | Prontos qui 05/11; entrega sex 06/11 | Os dois arquivos |
| 6 | **Semana de bugs** (até 13/11, sem histórias novas): dividir a bateria de `docs/Sprint_3/sprint-3-casos-de-teste-propostos.md`, §3, por pessoa e perfil, priorizando os caminhos do Termo **no ambiente publicado**. Acordar o serviço antes de cada sessão | Preparar até qui 05/11 | Tabela de execução |
| 7 | **Backup** da Parte 8.7 | Qui 05/11 (e as outras datas da 8.7) | Números da restauração |

**Modelo da tabela da semana de bugs:**

```
| CT | Perfil | Quem | Data | Resultado (OK / bug #) | Observação |
|---|---|---|---|---|---|
| CT68 | Administrador | Kauê | 09/11 | OK | |
```

Fluxo de um bug: registrar → escrever um teste que o reproduz → branch `fix/...` → PR → testar de novo no ambiente publicado.

### 9.5 Como entregar os documentos (daily, registros, atas, relatórios, evidências)

Documentos também entram por PR. Junte vários num mesmo PR quando forem do mesmo dia. Na `<PASTA_DO_PROJETO>`:

```powershell
git checkout main
git pull
git checkout -b docs/kaue-sprint-4-facilitacao
# crie ou edite os arquivos em docs\ (no VS Code ou no Bloco de Notas)
git add docs
git status --short
git commit -m "docs: atas e registros da facilitacao"
git push -u origin docs/kaue-sprint-4-facilitacao
```

Abra o PR (Parte 2.4) com um colega como revisor. No `git status --short`, confira que só aparecem arquivos de `docs/`, e **nunca** um `.dump` ou algo com a URL do banco. Para uma entrega de outro dia, use outro nome de branch (por exemplo, `docs/kaue-evidencias-deploy`).

---

## Parte 10 — Entrega (até qui 05/11)

1. **Evidências** em `docs/Sprint_4/evidencias/`: Fase P + Q (`pytest`), Fase 5 (`ReposicaoTest` e `PythonRealTest`), CT67, CT69, CT70, CT72, CT68, CT20, backup e restauração.
2. **Contrato:** confira que o código copiado não difere de `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` (§1.1 a §1.7 e §2.7). Se discordar de algo, registre uma nova revisão na seção 3 dele.
3. **Modelo matemático** (do tutorial antigo, sem data no roteiro): **crie** `docs/Apoio/Modelo_Matematico_Producao.md` e `docs/Apoio/Modelo_Matematico_Reposicao.md`, com conjuntos, parâmetros, variáveis, os estágios e os exemplos das Partes 3.4 e 4.1. Eles entram por PR (Parte 9.5). Se não couberem até a entrega, registre como pendência no relatório.
4. **Contribuições individuais** das Sprints 3 e 4, no padrão de `docs/Sprint_2/sprint-2-contribuicao-kaue.md`.

---

## Problemas comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `A├çA├ì` no lugar de `AÇAÍ` na tela | A janela não está em UTF-8 | `[Console]::OutputEncoding = [Text.Encoding]::UTF8` (seção "Acentos na tela") |
| O eco devolve `A?A? (POLPA)` | O JSON foi mandado pelo pipe do PowerShell | Use `cmd /c "… < arquivo.json"` (Parte 3.8) |
| `JSON de entrada inválido` com um arquivo que parece certo | Pipe do PowerShell com o `$OutputEncoding` mudado para UTF-8 (dois BOMs) | Abra uma janela nova e use `cmd /c` |
| `17 failed, 8 passed` | O `otimizador.py` ainda é o antigo | Parte 3.6 |
| `ModuleNotFoundError: No module named 'reposicao'` | O `reposicao.py` não foi copiado | Parte 4.3 |
| `47 passed` vira `25 passed` | `test_reposicao.py` não copiado | Parte 4.2 |
| `requirements.txt` vazio depois da conversão | Faltaram os parênteses | `git checkout -- optimization\requirements.txt` e Parte 3.3 |
| `JSONDecodeError` nos testes | Aviso de licença no `stdout` | Confira que o `_criar_env` liga o `OutputFlag 0` antes de `start()` |
| `too many sessions` / erro de WLS | Mais de 2 sessões da sua WLS | Não rode a suíte inteira com `GRB_*` definidas (Parte 5) |
| `Model too large for size-limited license` | Problema grande demais para a licença do `pip` | Não deve acontecer com os dados do projeto. Guarde a entrada e avise |
| `FalhaTecnica` (código ≠ 0) | A conferência em inteiros não bateu | **Não ignore.** Guarde o JSON de entrada e investigue |
| Reposição sempre "não cobre nem os mínimos" | Orçamento menor que o custo dos mínimos (é o comportamento certo) | Confira o `valor_faltante` |
| `cannot find symbol … ChaveOtimizador` na Fase 5 | A Fase 4A ainda não está na branch | Parte 7.4 (`git merge --no-edit origin/main`) |
| O Docker Desktop abre com erro e o botão "Reset to factory defaults" | Travamento conhecido (A26) | **Nunca** clique no Reset. Parte 1.2 |
| `/api/saude` do Render demora ~1 min | O serviço gratuito estava dormindo | Normal. Abra a URL antes de demonstrar |
| Erro de SSL ao carregar o DDL ou no backup | Faltou o `?sslmode=require` | Parte 8.4 (`$URL + "?sslmode=require"`) |
| `Could not create SSL/TLS secure channel` no PowerShell | O PowerShell 5.1 sem TLS 1.2 | A linha do `SecurityProtocol` (Parte 8.6) |
| `Açaí (polpa)\|40.000` na conferência do schema | O DDL carregado foi o antigo: a pasta estava numa branch desatualizada | Pare e chame o Diogo antes do seed (Parte 8.4) |
| O primeiro registro no Render dá `409` | Alguém se cadastrou como Administrador antes | Parte 8.5, "Se der errado" |
| `connection to server on socket … failed` logo depois de um `docker run` do PostgreSQL | O banco ainda está iniciando | Espere 10 s e rode de novo |
| O Docker ocupa cada vez mais espaço | Containers temporários removidos sem `-v` deixam volumes | Use `docker rm -f -v`. Os volumes esquecidos aparecem em `docker volume ls -f dangling=true` |

---

## Colinha

```powershell
# Toda janela nova
[Console]::OutputEncoding = [Text.Encoding]::UTF8
cd "<PASTA_DO_PROJETO>"

# Testes Python
optimization\venv\Scripts\python.exe -m pytest optimization\tests -q

# Rodar como o Java
cmd /c "optimization\venv\Scripts\python.exe optimization\app\otimizador.py < optimization\exemplos\entrada_demo.json"
cmd /c "optimization\venv\Scripts\python.exe optimization\app\reposicao.py < optimization\exemplos\entrada_reposicao_demo.json"

# Testes Java da Fase 5 (dentro de backend)
.\mvnw test "-Dtest=ReposicaoTest"
$env:MF_PYTHON = (Resolve-Path ..\optimization\venv\Scripts\python.exe).Path
.\mvnw test "-Dtest=PythonRealTest"

# Trazer a main para a sua branch
git fetch origin
git merge --no-edit origin/main

# Render (URL nunca no Git)
$URL = (Read-Host "Cole a External Database URL") + "?sslmode=require"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
```

---

## Checklist final

- [ ] Ambiente: `venv` com `(13, 0, 3)`; janela em UTF-8
- [ ] Fase P + Q: `8 passed` → vermelho → `47 passed`; 5 experimentos; `requirements.txt` em UTF-8; exemplos no repositório; 2ª máquina; PR na `main` na sex 30/10 (M2)
- [ ] E-mail da Gurobi encaminhado ao professor
- [ ] PR do `.env.example` (G8)
- [ ] PR do deploy e imagem testada na sua máquina (sáb: passos 1, 2 e 4)
- [ ] Pages habilitado (Source: GitHub Actions); o Render enxerga o repositório (teste do sábado); forma dos backups combinada com o Lucas
- [ ] Registro da perda do histórico do Git
- [ ] Fase 4A em dupla: eco com acentos, 151/1/0, Python real
- [ ] Fase 5: `ReposicaoTest` 8/8; `PythonRealTest` 3/3; PR na `main` cedo na seg 02/11; Lucas avisado
- [ ] Render: deploy (conta do Lucas), URL ao Leonardo, schema, **Administrador criado logo depois do schema** (e o segundo registro com `409`), Funcionário, seed, backup e restauração testada, URL e hora do banco passadas ao Lucas
- [ ] CT67, CT69, CT70, CT72 (ter); CT20 e CT68 (qua)
- [ ] Retrospectiva, ensaio, Review; relatórios das Sprints 3 e 4; contribuições individuais; documentos entregues por PR (Parte 9.5)
- [ ] Modelo matemático: `Modelo_Matematico_Producao.md` e `Modelo_Matematico_Reposicao.md` (ou a pendência registrada)
- [ ] Tabela da semana de bugs

---

## Fora do escopo obrigatório (decisões A2b e A40)

**US26** (porção padrão) é do Diogo e só entra se passar no *gate* da seg 02/11 (A40, A43); a sua parte é aprovar a seção do contrato. Ficam **fora do semestre**: alertas completos de reposição e de validade (US15/US16; a cobertura parcial é a do adendo, item 4), clientes (US13), CSV (US19) e cancelamento de pedido (US27).
