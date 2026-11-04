# Tutorial — Sprint 4 — Frontend do baseline à URL publicada — Leonardo

**Para:** Leonardo Fagundes Oliveira · **Papel:** frontend (HTML, CSS, JavaScript) e publicação no GitHub Pages · **Data:** 30/10/2026 (decisão A44)
**Substitui:** `docs/Sprint_3/sprint-3-tutorial-frontend-leonardo.md`, que fica como registro.
**Calendário em vigor:** `docs/Sprint_4/sprint-4-roteiro.md`, §4. Se uma data daqui divergir, vale o roteiro.
**Contrato:** o oficial, `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` (v2). **Não** use a `sprint-3-contrato-v2-proposta.md`: ela ficou congelada.

---

## O que foi verificado e o que não foi

Os comandos deste tutorial foram **executados de verdade** em 30/10/2026, num **clone temporário** do repositório. Partimos da `main` de 28/10, commit `af05b26`. O ambiente:
- Windows PowerShell 5.1;
- Node 24.15.0 e npm 11.12.1;
- Google Chrome instalado (os testes de navegador usam o Chrome da máquina);
- Python 3.14.0, só para servir os arquivos (`py -m http.server`);
- o backend completo da referência rodando de verdade, com um banco PostgreSQL **temporário**. O `mente-fria-db` não foi tocado.

| O quê | Resultado real |
|---|---|
| `npm install` | 2 pacotes em 5 s; aparece o `package-lock.json` (não vai para o Git) |
| Baseline: os 22 testes de navegador da `main` | Passam, mas são **instáveis**: **2 de 10** rodadas completas. Todas as falhas na verificação 21 |
| Contar as rodadas numa janela com os padrões de fábrica (página de código 850) | Procurando o texto com acento, **0 de 5** (a janela estraga os acentos); procurando `\d+ verifica`, **5 de 5**. O tutorial usa a segunda forma |
| Retrato das telas (sem navegador) | `OK: 25 retratos` |
| Divisão do `app.js` em módulos | `node --check` ok em todos; **`IGUAIS`** nas 25 telas; os testes falham do mesmo jeito que na `main` (3 de 10, sempre na 21) |
| Guarda de rotas por lista | A tabela da Parte 6, exatamente; as 25 telas continuam `IGUAIS` |
| **Causa da instabilidade (A13)** | Achada: a verificação 21 conta os campos antes de a tela terminar de remover. Com uma espera de uma linha no teste, **34 de 35** rodadas passaram (Parte 7) |
| `config.js` de produção e funções de apoio das telas novas | **15 verificações passaram** |
| Tela-exemplo de Estoque (lista e detalhe por linha) | Com a API simulada: **23 verificações passaram**. Com o backend real: os dois perfis veem a lista e o detalhe, com "Vencida" e "Vence em breve" |
| *Gate* da Fase 2, os 16 passos pela tela, contra o backend real | Todas as respostas "Você deve ver" da Parte 10 foram copiadas dessa execução |

**Não foi possível verificar aqui:**
- o push, os PRs e a publicação no GitHub Pages de verdade (dependem do repositório e da conta do Kauê);
- o frontend publicado falando com o Render (o deploy é na seg 02/11);
- as telas que **você** vai escrever (Pedido, Produção, Dashboard, Reposição, Marketing e o resto do Estoque). Para elas, o tutorial dá o formato exato da API, as regras, as funções de apoio já testadas e o que conferir.

**O que muda na sua tela:** datas, horários, `id`s e tempos. O resto deve bater.

> **Se alguma saída não bater:** pare, copie a mensagem **exata** e mande no grupo. Não tente adivinhar.

---

## Como usar este tutorial

### Qual terminal usar

Todos os comandos são para o **Windows PowerShell**. Para abrir:
- **No VS Code:** *Terminal* → *New Terminal*. Confira, no canto direito do painel, que o tipo é **powershell**.
- **Fora da IDE:** Menu Iniciar → digite `PowerShell` → **Windows PowerShell**.

**Como saber se você está no PowerShell:** a linha começa com **`PS`**, por exemplo `PS C:\Users\leo>`.

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
cd "C:\Users\leo\Documents\Mente_Fria"
dir
```

**Você deve ver** as pastas `backend`, `bd`, `docs`, `frontend` e `optimization`.

Neste tutorial, **`<PASTA_DO_PROJETO>`** significa esse caminho, o seu. Muitos comandos do frontend rodam **dentro** de `frontend`: cada passo diz onde.

### Três terminais

| Terminal | Para quê | Fica… |
|---|---|---|
| **A** | Comandos (Git, `npm test`, ferramentas) | Livre |
| **B** | O servidor dos arquivos do frontend (`py -m http.server`) | Rodando o tempo todo |
| **C** | O backend real (só a partir da Parte 9) | Rodando enquanto você testa com o backend |

Cada passo diz qual usar. Para parar o que está rodando num terminal: **Ctrl+C**.

### Acentos na tela: um comando por janela

Toda vez que abrir um PowerShell para este tutorial, rode **primeiro**:

```powershell
[Console]::OutputEncoding = [Text.Encoding]::UTF8
```

**Por quê:** o padrão do Windows em português é a "página de código 850". Sem este comando, quando o PowerShell **guarda** a saída do `npm test` numa variável ou num arquivo, `verificações` vira `verificaÃ§Ãµes` (verificado). A tela erra, os arquivos de evidência saem com os acentos quebrados, e uma busca pelo texto acentuado não acha nada. Vale só para aquela janela.

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
| `<URL_DO_SERVICO>` | O endereço do backend no Render, que o Kauê manda na seg 02/11, por exemplo `https://mente-fria-backend-xxxx.onrender.com` |

---

## Plano de tempo e "pronto quando"

| Dia | O que você faz | Parte |
|---|---|---|
| **Qui 29/10** (atrasado: faça primeiro, hoje) | Baseline (testes e retratos); divisão do `app.js` (PR). Logo depois, a correção do teste instável (A13, decisão A45) | 3, 4, 5 e 7 |
| **Sex 30/10** | Guarda de rotas (PR); `config.js` e *workflow* do Pages (PR, com o Kauê); Telas de Estoque e Pedido com a API simulada | 6, 8, 11, 12 e 13 |
| **Sáb 31/10** | **De manhã: *gate* da Fase 2** (16 evidências), assim que o Lucas avisar. À tarde: Estoque e Pedido com o backend real, assim que a Fase 3 entrar | 9, 10, 11 a 13 |
| **Dom 01/11** | Estoque e Pedido com o backend real. À noite, o Lucas (PO) decide se o botão "Descartar" fica (M3) | 11 a 13 |
| **Seg 02/11** (feriado, dia de mais carga) | Tela 12 (Produção); botão "Descartar", se aprovado; à tarde, começar a Tela 13 (assim que a Fase 5 entrar); a URL do Render no `config.js` (o Kauê manda) | 12, 14, 15 e 8 |
| **Ter 03/11** | Telas 03 (Dashboard), 13 (Reposição, concluir) e 14 (Marketing). Se apertar, o Lucas assume a 14. **Noite: M4** | 15 |
| **Qua 04/11** | Prazo final da A13, se ainda não entrou (A45). **18h: congelamento.** Depois das 18h: aceite na URL publicada, nos dois perfis | 7 e 16 |
| **Qui 05/11** | Evidências; `frontend/README.md`; contribuições individuais das Sprints 3 e 4 | 17 |
| **Sex 06/11** | Entrega e Review | — |

**Pronto quando:**
- [ ] `app.js` dividido, com `IGUAIS` e os 22 testes passando;
- [ ] guarda por lista (a tabela da Parte 6);
- [ ] *gate* da Fase 2 com as 16 evidências;
- [ ] as telas 10/16, 11/17, 12, 03, 13 e 14 funcionando com o backend real, com testes de navegador e **sem termos técnicos** nas Telas 12, 13 e 14;
- [ ] o frontend publicado no GitHub Pages, falando com o Render;
- [ ] `frontend/README.md` com o estado real e a URL;
- [ ] você sabe responder as "Perguntas para a Review" do fim de cada parte.

---

## Parte 0 — Conceitos em 5 minutos

### 0.1 Glossário

| Termo | O que é (sem jargão) | No nosso projeto |
|---|---|---|
| **SPA** (aplicação de uma página só) | Um único `index.html`; o JavaScript troca o conteúdo da página sem recarregar | `frontend/index.html` carrega `js/app.js` |
| **Rota com `#`** | A parte do endereço depois do `#` diz qual tela mostrar. Mudar o `#` não recarrega a página | `#/ingredientes`, `#/produtos/preco/1` |
| **Roteador** (*router*) | O código que lê o `#` e decide qual tela desenhar | `render()` no `app.js` (depois, `js/router.js`) |
| **Módulo ES** (`import`/`export`) | Um arquivo `.js` que **exporta** o que os outros podem usar e **importa** o que precisa | `import {request} from './api.js'` |
| **DOM** | A árvore de elementos da página, que o JavaScript cria e altera | `node('table', …)` cria uma tabela |
| **`fetch` e API** | `fetch` é como o navegador chama o backend. A API é o conjunto de rotas `/api/...` do backend | `request('/api/estoque')` em `js/api.js` |
| **JSON** | O formato de texto dos dados que vão e voltam | `{"nome": "Granola", "quantidade_valida": 5.16}` |
| **`snake_case`** | Nomes com `_`. A API usa sempre assim | `preco_venda`, `quantidade_minima` |
| **Token / `sessionStorage`** | O "crachá" do login, guardado no navegador **só** enquanto a aba estiver aberta | `mente-fria.session` |
| **401, 403, 409, 422, 503** | **401:** sem login ou login vencido. **403:** logado, mas sem permissão. **409:** conflito (já existe). **422:** a regra não deixa (estoque insuficiente). **503:** serviço indisponível (a reserva B1) | Cada tela mostra a `mensagem` que o backend manda |
| **Guarda de rotas** | O roteador não abre, para o Funcionário, telas que são só do Administrador | Experiência de uso. A **segurança** de verdade é o `403` do backend |
| **Playwright** | Uma biblioteca que abre o **Chrome** sozinho, clica e confere a tela | `npm test` (`tests/browser.cjs`) |
| **API simulada** | Nos testes, o Playwright **intercepta** as chamadas a `localhost:8080` e responde com dados de mentira. Assim os testes rodam sem backend | `page.route('http://localhost:8080/**', …)` |
| **jsdom e retrato** | jsdom é um "navegador de mentira" em Node. O retrato guarda o HTML de 25 telas num `.json` | `ferramentas\retratar_telas.mjs` |
| **`IGUAIS`** | O resultado de comparar dois retratos: nenhuma tela mudou. É a prova de que uma refatoração não mudou o comportamento | Parte 5 |
| **`innerHTML` × texto** | Pôr dados da API como **texto** impede que um nome malicioso vire código (XSS). O `node()` do projeto sempre cria texto | Nunca use `innerHTML` com dados da API |
| **CORS** | A regra do navegador que só deixa uma página falar com uma API se a API permitir aquela origem | Local: `http://127.0.0.1:5500`. No Pages: `https://kauencarneiro.github.io` |
| **GitHub Pages e *workflow*** | O GitHub publicando arquivos estáticos. O *workflow* é a receita (no GitHub Actions) que publica a pasta `frontend` | `.github/workflows/frontend-pages.yml` |
| **Branch / PR / merge** | **Branch:** uma "cópia de trabalho" do código. **PR:** o pedido para juntar a branch na `main`. **Merge:** a junção | Um PR por etapa e por tela |

### 0.2 Como as peças se encaixam

```
 Chrome em http://127.0.0.1:5500/index.html#/estoque
    |
    |  index.html carrega js/app.js  ->  router.js lê o "#/estoque"
    |                                     |
    |                                     v
    |                        screens/estoque.js desenha a tela
    |                         (ui.js: node, table, heading, loading)
    |                                     |
    |                                     v
    |                        api.js: request('/api/estoque') com o token
    v
 backend (Lucas)  http://localhost:8080/api/...    <- no Pages: o Render (config.js)

 Nos testes (npm test), o Playwright responde no lugar do backend (API simulada).
```

**A ideia que você vai defender na Review:**
- cada tela é um **módulo** e um **PR**;
- toda tela tem os mesmos estados: carregando, vazio, erro com "Tentar novamente", e sucesso;
- a tela **valida antes** de enviar (experiência), mas quem **garante** é o backend (`400`, `403`, `422`);
- os testes provam a tela com a API simulada; o aceite prova com o backend real.

### 0.3 Os códigos que aparecem no projeto

| Código | O que significa | Onde está |
|---|---|---|
| **US***n* (ex.: US9) | História de usuário do Backlog | `docs/Backlog_Priorizado_Mente_Fria.docx` |
| **CT***n* (ex.: CT43) | Caso de teste do Plano de Testes | `docs/Plano_de_Testes_Mente_Fria.md` |
| **Tela** *n* (ex.: Tela 12) | O número de cada tela no protótipo. Duas com o mesmo número, como **10/16**, são a mesma tela vista pelo Administrador e pelo Funcionário | `docs/Roteiro do Protótipo Navegável — Mente Fria.md` |
| **A***n* (ex.: A12, A13) | Decisões e pendências registradas. A12 = botão "Descartar" (opcional); A13 = teste de navegador instável | A1–A38 em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`; A39 em diante em `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md` |
| **M1, M2, M3, M4** | Marcos do calendário (o que precisa estar na `main` em cada noite) | Roteiro da Sprint 4, §4 |
| ***Gate* da Fase 2** | O "portão": as telas só passam a usar o backend real depois que os 16 passos da Parte 10 passam | Parte 10 |
| **Fase 1 a 6** | As etapas do backend do Lucas (1 segurança, 2 cadastros, 3 estoque e pedido, 4A produção, 6 dashboard e marketing) e a Fase 5 do Kauê (reposição) | Tutoriais do Lucas e do Kauê |
| **Plano B1** | A "chave de reserva": se o solver der problema no Render, produção e reposição respondem `503`. As Telas 12 e 13 mostram um aviso, não um erro; o Marketing e o Dashboard recebem `200` com uma mensagem | Partes 14 e 15 |
| **A26** | O travamento do Docker Desktop com contorno conhecido. **Nunca** "Reset to factory defaults" | Parte 9 |

---

## Parte 1 — Preparar o computador (uma vez)

### 1.1 Conferir as ferramentas

**Onde:** Terminal A, em qualquer pasta.

```powershell
git --version
node --version
npm --version
py --version
java -version
docker ps --filter name=mente-fria-db
@("$env:ProgramFiles", "${env:ProgramFiles(x86)}", "$env:LOCALAPPDATA") | ForEach-Object { Test-Path "$_\Google\Chrome\Application\chrome.exe" }
```

**O que cada parte faz:**
- as quatro primeiras e o `java -version` perguntam a versão de cada ferramenta;
- o `docker ps` confere que o Docker está aberto e o banco `mente-fria-db` existe;
- a última procura o Google Chrome nos três lugares em que ele costuma ser instalado.

O Java e o Docker só são usados a partir do *gate* (Parte 9, sáb), mas **confira já**: o roteiro (§8) pede o Docker conferido antes do primeiro PR.

**Você deve ver** (os números podem ser um pouco diferentes):

```
git version 2.53.0.windows.1
v24.15.0
11.12.1
Python 3.14.0
java version "25.0.2" 2026-01-20 LTS
...
NAMES           STATUS        ...
mente-fria-db   Up 2 hours    ...
True
False
False
```

Das três últimas linhas, **uma** `True` basta.

| Se der errado | O que fazer |
|---|---|
| `'node' não é reconhecido…` | Instale o Node.js LTS pelo site oficial (nodejs.org) e abra um PowerShell novo |
| As três últimas linhas dão `False` | Os testes abrem o **Google Chrome** da máquina (`channel: 'chrome'` no `tests\browser.cjs`). Instale o Chrome pelo site oficial. Se ele estiver noutra pasta, confira em `chrome://version` (campo "Caminho executável") |
| `java version "17..."`, `"21..."` ou `'java' não é reconhecido…` | O backend precisa do **JDK 25** (tutorial do Lucas, Parte 1.1) |
| `docker ps` dá `error during connect`, ou a lista vem vazia | Docker fechado, ou o `mente-fria-db` não existe nesta máquina: tutorial do Lucas, Parte 1.2 (inclui o travamento A26: **nunca** "Reset to factory defaults"). Sem o container, peça ajuda ao Diogo |

### 1.2 Instalar as dependências do frontend

**Onde:** Terminal A, dentro de `frontend`.

```powershell
cd "<PASTA_DO_PROJETO>\frontend"
npm install
```

**O que faz:** lê o `package.json` e baixa o Playwright para a pasta `node_modules`.

**Você deve ver** (validado):

```
added 2 packages, and audited 3 packages in 5s

found 0 vulnerabilities
```

Depois disso aparece um arquivo novo, o `package-lock.json`. **Não o adicione aos seus commits** (Parte 2.2): ele não estava no repositório, e decidir isso não é desta sprint. A pasta `node_modules` já é ignorada pelo `.gitignore`.

### 1.3 O servidor dos arquivos (Terminal B)

**Objetivo:** abrir o frontend no navegador por `http://127.0.0.1:5500`. Módulos ES **não** funcionam abrindo o `index.html` com dois cliques (`file://`).

**Onde:** um **segundo** PowerShell (Terminal B), dentro de `frontend`.

```powershell
cd "<PASTA_DO_PROJETO>\frontend"
py -m http.server 5500 --bind 127.0.0.1
```

| Parte | O que faz |
|---|---|
| `py -m http.server` | Um servidor de arquivos simples, que já vem com o Python |
| `5500` | A porta. É a que os testes (`tests\browser.cjs`) e o CORS do backend esperam |
| `--bind 127.0.0.1` | Atende só na sua máquina, no endereço `127.0.0.1` |

**Você deve ver:** `Serving HTTP on 127.0.0.1 port 5500 (http://127.0.0.1:5500/) ...`. Deixe esse terminal aberto. Abra `http://127.0.0.1:5500` no Chrome: aparece a tela de login.

> Se você usa o **Live Server** do VS Code, ele também serve na porta 5500. Use **um** dos dois, nunca os dois ao mesmo tempo.

**Antes de avançar:**
- [ ] as cinco ferramentas respondem, e o Chrome existe;
- [ ] `npm install` sem erro;
- [ ] `http://127.0.0.1:5500` mostra o login.

---

## Parte 2 — Git e PR: o caminho de toda etapa

Você vai repetir este caminho em cada PR: divisão, guarda, Pages, A13 e cada tela.

### 2.1 Começar: branch nova a partir da `main` atualizada

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`.

```powershell
git status
git checkout main
git pull
git checkout -b feature/frontend-modulos
```

| Comando | O que faz |
|---|---|
| `git status` | Mostra se há arquivos alterados sem commit. Antes de trocar de branch, o certo é **nada** (fora os arquivos ignorados e os da 2.2) |
| `git checkout main` e `git pull` | Vão para a `main` e trazem o que os colegas já juntaram |
| `git checkout -b <nome>` | Cria a branch nova a partir da `main` atualizada e já entra nela |

**Os nomes das suas branches:**

| Etapa | Branch |
|---|---|
| Divisão do `app.js` | `feature/frontend-modulos` |
| Guarda por lista | `feature/frontend-guarda-rotas` |
| `config.js` e *workflow* do Pages | `feature/frontend-pages` |
| A URL do Render no `config.js` (seg 02/11) | `fix/frontend-url-render` |
| Correção do teste instável (A13) | `fix/frontend-teste-remocao` |
| Cada tela | `feature/frontend-estoque`, `feature/frontend-pedido`, `feature/frontend-producao`, `feature/frontend-dashboard`, `feature/frontend-reposicao`, `feature/frontend-marketing` |

### 2.2 Commit: só o que é da etapa

```powershell
git status --short
git add frontend\js frontend\tests\browser.cjs
git status --short
git commit -m "refactor(frontend): divide o app.js em modulos sem mudar comportamento"
```

| Comando | O que faz |
|---|---|
| `git status --short` | Lista o que mudou: `M` = modificado, `A` = novo já marcado, `??` = novo que o Git ainda não conhece |
| `git add <caminhos>` | Marca **só** os caminhos da etapa. Cada parte diz quais |
| `git commit -m "…"` | Cria o commit. Padrão da equipe: *Conventional Commits* (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`), **sem acentos** |

**Nunca adicione** (eles aparecem como `??` e devem continuar assim):

| Arquivo | Por quê |
|---|---|
| `frontend/package-lock.json` | Criado pelo `npm install` (Parte 1.2) |
| `frontend/tests/artifacts/` | As imagens que o `npm test` grava a cada rodada |
| `frontend/ferramentas/` | As ferramentas de retrato (Parte 3.3): são cópias da referência |
| `frontend/retrato_*.json` | Os retratos: arquivos temporários |

Por isso **nunca** use `git add .` nem `git add frontend` neste trabalho.

**A evidência vai no mesmo PR:** cada etapa só conta com o PR **e** a evidência em `docs/Sprint_4/evidencias/` (roteiro, §4). Acrescente o arquivo dela ao `git add` da etapa, por exemplo `git add frontend\js docs\Sprint_4\evidencias`.

### 2.3 Enviar, abrir o PR, merge

```powershell
git push -u origin feature/frontend-modulos
```

**Você deve ver** no fim: `branch 'feature/frontend-modulos' set up to track 'origin/feature/frontend-modulos'.`

**Antes de abrir o PR,** rode o `npm test` (Parte 3.2) e guarde a saída como evidência (Parte 2.5).

1. Abra `https://github.com/KaueNCarneiro/Mente_Fria` → **"Compare & pull request"** (ou aba **Pull requests** → **New pull request**).
2. Confira: **base: `main`** ← **compare: `<sua branch>`**.
3. Título igual à mensagem do commit; descrição no modelo abaixo; um colega em **Reviewers**.
4. **Create pull request**.

Depois da aprovação: **Merge pull request** → **Confirm merge** → **Delete branch**. No Terminal A:

```powershell
git checkout main
git pull
git branch -d feature/frontend-modulos
```

**Modelo de descrição do PR:**

```
## O que entra
<o que a etapa faz, em uma ou duas linhas>

## Como testar
cd frontend
npm test
(em outro terminal, dentro de frontend: py -m http.server 5500 --bind 127.0.0.1)

## Resultado na minha máquina
<as linhas finais do npm test e, na divisão, o IGUAIS>

## Avisos
<o que muda para os colegas>
```

> **Mensagens em vermelho do Git:** no PowerShell, o Git escreve o progresso numa saída que às vezes aparece em vermelho. Se a última linha for a esperada, deu certo.

### 2.4 Trazer a `main` nova para a sua branch

Quando um colega junta algo de que a sua branch precisa (por exemplo, a guarda, antes de criar uma tela):

```powershell
git fetch origin
git merge --no-edit origin/main
```

O `--no-edit` aceita a mensagem padrão do "commit de junção" sem abrir um editor de texto.

**Se aparecer `CONFLICT`:**
- **Entre duas telas suas** (o caso mais provável): toda tela nova acrescenta linhas nos mesmos lugares do `router.js`, do `shell.js`, do `nav.js` e do `tests\browser.cjs`. No VS Code, o arquivo em conflito mostra os dois blocos; clique em **Accept Both Changes**, confira que as duas rotas (ou as duas verificações) ficaram, salve, e rode `git add <arquivo>` e `git commit --no-edit`. Rode o `npm test` antes do push.
- **Com a mudança de um colega:** pare e chame quem fez a outra mudança.

**Para quase não ter conflito:** uma tela por vez. Faça o merge da tela anterior antes de criar a branch da próxima (Parte 2.1).

### 2.5 Guardar a evidência dos testes

**Onde:** Terminal A, dentro de `frontend`, com o Terminal B servindo os arquivos, **depois** do `[Console]::OutputEncoding = [Text.Encoding]::UTF8` (seção "Acentos na tela").

```powershell
$saida = npm test 2>&1 | ForEach-Object { "$_" }
$saida | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-29_leonardo_baseline_npm-test.txt
$saida | Select-Object -Last 2
```

| Parte | O que faz |
|---|---|
| `npm test 2>&1 \| ForEach-Object { "$_" }` | Roda os testes e junta a saída normal e a de erro como texto |
| `Out-File -Encoding utf8` | Grava em UTF-8 (o Git mostra as diferenças). Troque a data e a etapa no nome do arquivo |
| `Select-Object -Last 2` | Mostra só o fim, para conferir na hora |

Para guardar também a contagem das 10 rodadas (Parte 3.2), rode o laço e depois acrescente a linha final ao mesmo arquivo: `"passaram: $ok de 10" | Out-File -Encoding utf8 -Append ..\docs\Sprint_4\evidencias\2026-10-29_leonardo_baseline_npm-test.txt`.

> Não use `Tee-Object`: no Windows PowerShell 5.1 ele grava em UTF-16, e o Git trata o arquivo como binário.

---

## Parte 3 — O baseline (qui 29/10, primeiro de tudo)

**Objetivo:** saber como o frontend da `main` se comporta **antes** de mexer, para provar depois que a divisão não mudou nada.
**Onde:** Terminal A, dentro de `frontend`, na branch `feature/frontend-modulos` (Parte 2.1). Terminal B servindo os arquivos (Parte 1.3).

### 3.1 Os testes de navegador uma vez

```powershell
npm test
```

**O que faz:** roda `node tests/browser.cjs`. Ele abre o Chrome escondido, entra em `http://127.0.0.1:5500`, faz 22 verificações (login, cadastros, erros, perfis) com a **API simulada** e fecha.

**Você deve ver**, quando passa (validado):

```
OK login visual e campos obrigatórios
OK registro valida senha e confirmação
...
OK screenshots desktop e mobile sem overflow
22 verificações passaram. Integração HTTP simulada.
```

**Mas é comum ver isto** (validado: 8 de 10 rodadas na `main`):

```
...
OK API de resumo não permite editar com valores inventados
AssertionError [ERR_ASSERTION]: Expected values to be strictly equal:

1 !== 0
    at ...\frontend\tests\browser.cjs:58:906
```

**Isso é a pendência A13, e não um erro seu:** a verificação 21 ("porções zeradas e remoção com confirmação") falha **na maioria das rodadas** (8 de 10, validado) **no código original**. A causa e a correção estão na Parte 7. Nesta parte, você só **mede**.

### 3.2 Medir: 10 rodadas

```powershell
$ok = 0
1..10 | ForEach-Object { $s = npm test 2>&1 | Out-String; if ($s -match "\d+ verifica") { $ok++ } else { ($s -split "`n" | Select-String "^OK " | Select-Object -Last 1).Line } }
"passaram: $ok de 10"
```

| Parte | O que faz |
|---|---|
| `1..10 \| ForEach-Object { … }` | Repete 10 vezes (cerca de 4 s cada) |
| `npm test 2>&1 \| Out-String` | Guarda toda a saída de uma rodada como texto |
| `-match "\d+ verifica"` | Conta a rodada como boa se apareceu a linha final ("22 verificações passaram."). Procura só um número e o começo da palavra, **sem acento**: funciona em qualquer janela e com qualquer número de verificações (validado: com o texto acentuado, uma janela com os padrões de fábrica contou 0 de 5) |
| O `else` | Numa rodada ruim, mostra a **última verificação que passou**: a que falhou é a seguinte |

**Você deve ver** algo como (validado; o número varia):

```
OK API de resumo não permite editar com valores inventados
OK API de resumo não permite editar com valores inventados
...
passaram: 2 de 10
```

Anote o número: é a sua evidência de baseline. A linha repetida mostra que a falha é sempre logo depois da verificação 20, ou seja, na 21.

### 3.3 O retrato das telas (sem navegador)

**Por quê:** o retrato guarda o HTML, o título e a rota de 25 telas. Depois da divisão, um segundo retrato igual ao primeiro prova, sem depender do teste instável, que **nada mudou**.

```powershell
New-Item -ItemType Directory -Force ferramentas | Out-Null
Copy-Item ..\docs\Sprint_3\referencia\frontend\ferramentas\*.mjs ferramentas\
npm install jsdom --no-save
node ferramentas\retratar_telas.mjs . retrato_antes.json
```

| Comando | O que faz |
|---|---|
| `New-Item … ferramentas` e `Copy-Item …` | Copia as 4 ferramentas para `frontend\ferramentas` (o Node procura o `jsdom` a partir da pasta do script) |
| `npm install jsdom --no-save` | Instala o "navegador de mentira" **sem** mexer no `package.json` |
| `node ferramentas\retratar_telas.mjs . retrato_antes.json` | Abre as 25 telas no jsdom, com a API simulada, e grava o retrato |

**Você deve ver** (validado): `OK: 25 retratos -> retrato_antes.json`.

| Se der errado | O que fazer |
|---|---|
| `Cannot find package 'jsdom'` | Um `npm install` posterior removeu o `jsdom`: rode `npm install jsdom --no-save` de novo |
| `ERR_CONNECTION_REFUSED` no `npm test` | O Terminal B não está servindo os arquivos (Parte 1.3) |
| O `npm test` diz que não achou o Chrome (`chrome`) | O Chrome não está instalado ou não está na pasta padrão (Parte 1.1) |

**Antes de avançar:**
- [ ] a contagem das 10 rodadas anotada (e guardada com a Parte 2.5);
- [ ] `OK: 25 retratos -> retrato_antes.json`.

---

## Parte 4 — Entender o frontend atual (20 minutos, antes de dividir)

Você vai dividir e depois acrescentar telas a este código. Vale ler as quatro peças com calma.

### 4.1 `js/api.js`: toda chamada ao backend passa por aqui

```js
export async function request(path, {method = 'GET', body, public: isPublic = false} = {}) {
  const token = isPublic ? null : session()?.token;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    const response = await fetch(API_BASE_URL.replace(/\/$/, '') + path, {
      method, signal: controller.signal,
      headers: {Accept: 'application/json', ...(body ? {'Content-Type': 'application/json'} : {}), ...(token ? {Authorization: `Bearer ${token}`} : {})},
      ...(body ? {body: JSON.stringify(body)} : {})
    });
    ...
    if (!response.ok) {
      if (response.status === 401 && token && session()?.token === token) {
        logout(); window.dispatchEvent(new Event('session-expired'));
      }
      ...
      throw new ApiError(data?.mensagem || fallback[response.status] || …, response.status, data?.campos || {});
    }
```

1. **O token** vai sozinho no cabeçalho `Authorization`. Você nunca o monta à mão numa tela.
2. **O tempo limite** (`REQUEST_TIMEOUT_MS`, do `config.js`): 15 s na sua máquina; 90 s no Pages (Parte 8).
3. **Erro vira `ApiError`**, com a `mensagem` do backend e os `campos` (os erros por campo). As telas só mostram o que vem aqui.
4. **`401` com token:** o `api.js` encerra a sessão e avisa o roteador (`session-expired`), que volta ao login com "Sua sessão expirou. Entre novamente.".
5. **Sem conexão:** a mensagem "Não foi possível conectar à API…" é diferente de "senha inválida". O navegador não diferencia queda do backend de bloqueio de CORS.

`list(path)` é o `request` para rotas que devolvem **lista**: se vier outra coisa, ele avisa que a resposta não bate com o contrato.

### 4.2 `node()`: como a tela é desenhada

Não há HTML escrito à mão nas telas. Tudo é criado com uma função:

```js
const node = (tag, props = {}, ...children) => {
  const el = document.createElement(tag);
  for (const [key,value] of Object.entries(props)) {
    if (key.startsWith('on')) el.addEventListener(key.slice(2).toLowerCase(), value);
    else if (key === 'class') el.className = value;
    else if (value !== undefined && value !== null && value !== false) el.setAttribute(key, value === true ? '' : value);
  }
  for (const child of children.flat(Infinity)) if (child !== null && child !== undefined) el.append(child instanceof Node ? child : document.createTextNode(String(child)));
  return el;
};
```

- `node('p', {class: 'muted'}, 'Olá')` cria `<p class="muted">Olá</p>`.
- `onclick: …` vira um "ouvinte" de clique.
- Todo filho que não é elemento vira **texto** (`createTextNode`). É isso que faz um nome como `<img src=x onerror=…>` aparecer escrito, e não executar (a verificação 18 dos testes prova isso).

Em cima dela, o projeto tem ajudantes prontos (no `app.js` hoje; no `js/ui.js` depois da divisão):

| Ajudante | O que faz |
|---|---|
| `heading(main, título, subtítulo, ação, voltar)` | O título da tela, o botão do canto e o link "← Voltar para lista". Mostra também o aviso verde guardado em `flash` |
| `loading(main, tarefa)` | Mostra "Carregando…", espera a tarefa (a chamada à API) e devolve os dados. Se der erro, troca por uma mensagem vermelha com **"Tentar novamente"** |
| `table(cabeçalhos, linhas)` | Uma tabela; sem linhas, "Nenhum registro cadastrado." |
| `field(nome, rótulo, opções)` | Um campo com o lugar da mensagem de erro dele |
| `formBase(rótulo, voltar)` + `bindForm(f, validar, salvar)` | O formulário: valida, mostra os erros por campo, desabilita o botão enquanto envia ("Salvando…") e mostra os `campos` do backend |
| `money`, `amount`, `unit` | Formatam R$, quantidades (até 3 casas) e unidades (`l` vira `L`) |
| `notice(texto, tipo)` e `badge(ativo)` | Avisos e selos |

### 4.3 Uma tela pronta: Funcionários

```js
export async function employees(main,isNew) {
  heading(main,isNew?'Novo Funcionário':'Funcionários',…);
  if(!isNew){const data=await loading(main,()=>list('/api/usuarios'));if(data)main.append(table(['Nome','E-mail','Status'],data.map(x=>[x.nome,x.email,badge(x.ativo)])));return;}
  const f=formBase('Salvar funcionário','funcionarios');…
  bindForm(f,raw=>accountValidation(raw,true),async payload=>{await request('/api/usuarios',{method:'POST',body:payload});state.flash='Funcionário cadastrado com sucesso.';go('funcionarios');});
  main.append(f.form);
}
```

Leia em voz alta: **título; se for a lista, carrega `/api/usuarios` e desenha a tabela; se for o formulário, valida, envia com `POST`, guarda o aviso verde e volta para a lista.** Toda tela nova sua segue este molde.

### 4.4 `render()`: o roteador

```js
const path=location.hash.replace(/^#\/?/,'')||'login', [section,action,id]=path.split('/');
```

`#/produtos/preco/1` vira `section = 'produtos'`, `action = 'preco'`, `id = '1'`. Depois, uma sequência de `if`/`else if` escolhe a tela. As regras de quem entra (login, perfil) vêm antes. Ele roda de novo a cada mudança do `#` (`hashchange`).

**Antes de avançar:** você consegue dizer, olhando o `app.js`, onde estão `request`, `node`, `loading`, `employees` e `render`?

---

## Parte 5 — Dividir o `app.js` em módulos (qui 29/10)

**Objetivo:** trocar um arquivo de 235 linhas por módulos pequenos, **sem mudar nenhum comportamento**.
**Por quê:** seis telas novas no mesmo arquivo, com você e o Lucas mexendo ao mesmo tempo, é receita de conflito (o problema da Sprint 1). Com módulos, cada tela é um arquivo e um PR.
**Onde:** Terminal A, dentro de `frontend`, na branch `feature/frontend-modulos`. Terminal B servindo os arquivos.

### 5.1 O mapa (linhas do `app.js` real)

| Linhas | O que é | Vai para |
|---|---|---|
| 1–2 | `import` de `api.js` e `validation.js` | Cada módulo importa o que usa |
| 3–6, 8–21, 24–60, 106–119, 149–154 | `root`, `money`, `amount`, `unit`, `node`, `link`, `button`, `logo`, `notice`, `field`, `errors`, `formBase`, `bindForm`, `heading`, `loading`, `table`, `badge`, `confirmRemoval` | `js/ui.js` |
| 7 | `let revision, flash` | **`js/state.js`** (vira `state.revision` e `state.flash`) |
| 22–23 | `go`, `admin` | `js/nav.js` |
| 61–91 | `accountValidation`, `authPage` | `js/screens/auth.js` |
| 92–105 | `shell` | `js/screens/shell.js` |
| 120–126 | `employees` | `js/screens/funcionarios.js` |
| 127–148 | `ingredients` | `js/screens/ingredientes.js` |
| 155–198 | `products` | `js/screens/produtos.js` |
| 199–214 | `communication` | `js/screens/comunicacao.js` |
| 215–235 | `render`, os `addEventListener` e a chamada inicial | `js/router.js` |
| (novo) | `import './router.js';` | `js/app.js` (continua sendo o arquivo do `index.html`) |

**Ordem das dependências, sem ciclos:** `state` ← `nav` ← `ui` ← `screens/*` ← `router` ← `app`. O `index.html` **não muda**.

### 5.2 Os quatro cuidados

| # | Cuidado | Por quê |
|---|---|---|
| 1 | `flash` e `revision` viram `state.flash` e `state.revision` (`export const state = {revision: 0, flash: ''};`) | Num módulo, uma variável `let` de outro arquivo não pode ser trocada. As propriedades de um objeto compartilhado podem |
| 2 | `go()` e o "Tentar novamente" disparam `window.dispatchEvent(new Event('rerender'))`, e o `router.js` escuta esse evento | Se o `ui.js` ou o `nav.js` importassem o `router.js`, haveria um **ciclo** de importação |
| 3 | Todo item usado fora do arquivo precisa de `export` | Sem `export`, o outro módulo não enxerga e a tela fica em branco |
| 4 | Em `products()`, `ingredients` é uma **variável local**, não a tela de ingredientes | Não importe a tela em `produtos.js` |

### 5.3 Um módulo por vez, com ponto de checagem

**Ordem**, de modo que o projeto funcione a cada passo:
1. `state.js`;
2. `ui.js` (no "Tentar novamente" do `loading`, troque a chamada direta a `render()` por `window.dispatchEvent(new Event('rerender'))`);
3. `nav.js` (no `go()`, troque `render()` pelo mesmo `dispatchEvent`). **Neste passo**, acrescente no `app.js`, junto dos outros `addEventListener` do fim, a linha `window.addEventListener('rerender',()=>{render().catch(showUnexpected);});`: sem ela, o "Tentar novamente" e o `go()` para a mesma rota param de funcionar;
4. as telas (`screens\*.js`), uma a uma. O `app.js` passa a importá-las;
5. por último, o `router.js`: leve para ele o `render()`, os `addEventListener` e a chamada inicial; o `app.js` fica só com `import './router.js';`.

Os pontos de checagem de cada passo são os de baixo. O **resultado final** desta ordem é o da referência, validado na Parte 5.4.

**Como criar um módulo** (exemplo, o `state.js`):
1. No VS Code, em `frontend\js`, crie o arquivo `state.js` com:

   ```js
   // Estado compartilhado entre as telas (antes eram variáveis soltas do app.js).
   export const state = {revision: 0, flash: ''};
   ```

2. No `app.js`, apague a linha 7 (`let revision…, flash…`), acrescente `import {state} from './state.js';` no topo e troque cada `flash` por `state.flash` e cada `revision` por `state.revision` (**Ctrl+H** no VS Code ajuda, mas confira um por um).

**Depois de cada módulo:**

```powershell
node --check js\state.js
npm test
```

Troque `js\state.js` pelo arquivo que você acabou de criar (`js\ui.js`, `js\nav.js`, `js\screens\auth.js`…).

| Comando | O que faz |
|---|---|
| `node --check <arquivo>` | Confere só a **sintaxe** (parênteses, chaves). Não mostra nada quando está certo |
| `npm test` | Confere o comportamento. Sem a correção da Parte 7, a verificação 21 falha na maioria das rodadas mesmo sem você mexer (Parte 3) |

**A referência pronta e verificada** está em `docs\Sprint_3\referencia\frontend\js\` (com `screens\`). Use para **comparar** com a sua: no VS Code, clique com o botão direito no seu arquivo → *Select for Compare*; depois, no da referência → *Compare with Selected*. Não copie sem entender: na Review, você explica o código.

### 5.4 A prova: retrato e testes

Com todos os módulos prontos:

```powershell
Get-ChildItem js, js\screens -Filter *.js | ForEach-Object { node --check $_.FullName; "$($_.Name): $LASTEXITCODE" }
node ferramentas\retratar_telas.mjs . retrato_depois.json
node ferramentas\comparar_retratos.mjs retrato_antes.json retrato_depois.json
```

| Linha | O que faz |
|---|---|
| A primeira | Roda o `node --check` em **todos** os `.js` e mostra o código de saída de cada um (`0` = ok) |
| A segunda | Tira o retrato do código novo |
| A terceira | Compara os dois retratos |

**Você deve ver** (validado com a referência):

```
api.js: 0
app.js: 0
...
shell.js: 0
OK: 25 retratos -> retrato_depois.json
retratos: antes 25 | depois 25
IGUAIS: o HTML, o título e a rota de todas as telas são idênticos.
```

Rode também as 10 rodadas da Parte 3.2. **Validado:** 3 de 10 no código dividido, sempre na verificação 21, como na `main`. É a prova de que a instabilidade não vem da divisão.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `DIFERENÇAS em: …` | Um `flash` sem `state.` ou um `render()` chamado de `ui`/`nav` | O nome da tela mostra onde procurar. Volte à 5.2 |
| Tela em branco no Chrome | Erro de `import`/`export` | **F12** → aba *Console*: a mensagem diz o arquivo e o nome que faltou |
| `SyntaxError` no `node --check` | Parêntese ou chave sobrando ou faltando | A mensagem mostra a linha |

### 5.5 Commit, PR

Antes, grave a evidência (dentro de `frontend`): o `IGUAIS` e a contagem das 10 rodadas.

```powershell
node ferramentas\comparar_retratos.mjs retrato_antes.json retrato_depois.json | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-29_leonardo_modulos_iguais.txt
```

```powershell
cd ..
git add frontend\js docs\Sprint_4\evidencias
git status --short
git commit -m "refactor(frontend): divide o app.js em modulos sem mudar comportamento"
git push -u origin feature/frontend-modulos
```

O `cd ..` volta para a raiz do repositório. **Você deve ver** no `git status --short` (validado; as linhas de `docs/` são as das suas evidências):

```
A  docs/Sprint_4/evidencias/2026-10-29_leonardo_baseline_npm-test.txt
A  docs/Sprint_4/evidencias/2026-10-29_leonardo_modulos_iguais.txt
M  frontend/js/app.js
A  frontend/js/nav.js
A  frontend/js/router.js
A  frontend/js/screens/auth.js
A  frontend/js/screens/comunicacao.js
A  frontend/js/screens/funcionarios.js
A  frontend/js/screens/ingredientes.js
A  frontend/js/screens/produtos.js
A  frontend/js/screens/shell.js
A  frontend/js/state.js
A  frontend/js/ui.js
?? frontend/ferramentas/
?? frontend/package-lock.json
?? frontend/retrato_antes.json
?? frontend/retrato_depois.json
?? frontend/tests/artifacts/
```

As linhas `??` ficam de fora (Parte 2.2). No PR, cole o `IGUAIS` e a contagem das 10 rodadas antes e depois. **Revisor:** o Lucas.

### 5.6 Perguntas para a Review

1. Por que dividir **antes** de criar as telas novas?
2. Como você provou que a divisão não mudou nada, se o teste de navegador é instável?
3. Por que `state.flash`, e não um `let flash` exportado?
4. Por que o `ui.js` não importa o `router.js`?

---

## Parte 6 — Guarda de rotas por lista (sex 30/10)

**Objetivo:** o Funcionário não abre, pelo endereço, telas que são só do Administrador, e consegue abrir as que são dele.
**Por quê:** a regra de hoje (`!admin() && (section === 'funcionarios' || action)`) erra dos dois lados (verificado):
- **bloqueia** `#/pedidos/novo`, que o Funcionário pode usar;
- **libera** `#/dashboard` e `#/producao`, que são só do Administrador (só o `403` do backend segurava).

**Onde:** Terminal A, dentro de `frontend`, numa branch nova `feature/frontend-guarda-rotas`, criada **depois** do merge da divisão (Parte 2.1).

**Antes de mexer, a foto do "antes"** (vai para o PR):

```powershell
node ferramentas\testar_guarda.mjs . | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-30_leonardo_guarda-antes.txt
```

### 6.1 O código

No **final** do `js/nav.js`, acrescente:

```js
// Guarda de rotas por lista: o que o Funcionário pode abrir. A segurança de verdade é o 403 do backend.
const SO_ADMIN = new Set(['funcionarios', 'dashboard', 'porcao-padrao', 'producao', 'reposicao', 'marketing']);
const FUNCIONARIO = {inicio: [undefined], ingredientes: [undefined], produtos: [undefined],
                     estoque: [undefined], pedidos: [undefined, 'novo'], clientes: [undefined, 'novo']};
export const rotaPermitida = (section, action) =>
  admin() || (!SO_ADMIN.has(section) && Boolean(FUNCIONARIO[section]?.includes(action)));
```

| Parte | O que faz |
|---|---|
| `SO_ADMIN` | As seções que o Funcionário nunca abre |
| `FUNCIONARIO` | Para cada seção que ele pode abrir, as **ações** permitidas. `undefined` = a lista (sem ação); `'novo'` = o formulário de criação |
| `rotaPermitida` | O Administrador abre tudo. O Funcionário, só o que está na lista e não está em `SO_ADMIN` |
| `?.includes` | Se a seção não está no mapa, o resultado é `undefined`, e o `Boolean` transforma em `false` |

No `js/router.js`:
1. troque `import {go, admin} from './nav.js';` por `import {go, admin, rotaPermitida} from './nav.js';`;
2. troque `if(!admin()&&(section==='funcionarios'||action)){` por `if(!rotaPermitida(section,action)){`.

**Regra para o resto da sprint:** a cada tela nova que o Funcionário pode usar, acrescente a rota dela ao mapa `FUNCIONARIO` (a Parte 11 mostra como).

**Uma mudança de comportamento que o retrato não pega:** para o Funcionário, uma rota que não existe (por exemplo, um erro de digitação, `#/estoqe`) agora volta ao início com "Esta área é exclusiva do Administrador.", e não mais "Página não encontrada" (ela não está na lista dele). Para o Administrador, nada muda. Cite isso no PR.

### 6.2 Conferir

```powershell
node --check js\nav.js
node --check js\router.js
node ferramentas\testar_guarda.mjs .
node ferramentas\retratar_telas.mjs . retrato_guarda.json
node ferramentas\comparar_retratos.mjs retrato_depois.json retrato_guarda.json
npm test
```

**Você deve ver** (validado; o `testar_guarda` abre as rotas como Funcionário e, no fim, como Administrador):

```
#/pedidos/novo               LIBERADA (fica na rota)
#/clientes/novo              LIBERADA (fica na rota)
#/estoque                    LIBERADA (fica na rota)
#/funcionarios               BLOQUEADA (voltou para #/inicio)
#/produtos/novo              BLOQUEADA (voltou para #/inicio)
#/ingredientes/editar/1      BLOQUEADA (voltou para #/inicio)
#/dashboard                  BLOQUEADA (voltou para #/inicio)
#/producao                   BLOQUEADA (voltou para #/inicio)
#/pedidos/editar/1           BLOQUEADA (voltou para #/inicio)
(admin) #/dashboard          LIBERADA
(admin) #/pedidos/novo       LIBERADA
(admin) #/producao           LIBERADA
OK: 25 retratos -> retrato_guarda.json
retratos: antes 25 | depois 25
IGUAIS: o HTML, o título e a rota de todas as telas são idênticos.
```

**Antes da mudança** (validado), as duas primeiras linhas diziam `BLOQUEADA`, e `#/dashboard` e `#/producao` diziam `LIBERADA`. O `IGUAIS` prova que nenhuma tela existente mudou.

**Commit e PR** (a foto do "depois" primeiro, dentro de `frontend`):

```powershell
node ferramentas\testar_guarda.mjs . | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-30_leonardo_guarda-depois.txt
cd ..
git add frontend\js\nav.js frontend\js\router.js docs\Sprint_4\evidencias
git commit -m "fix(frontend): guarda de rotas por lista de permissoes"
git push -u origin feature/frontend-guarda-rotas
```

No PR, cole as duas saídas do `testar_guarda` (antes e depois).

| Se der errado | O que fazer |
|---|---|
| `SyntaxError` no `node --check js\nav.js` | O bloco foi colado pela metade: confira as chaves `{}` e os colchetes `[]` |
| O `testar_guarda` mostra tudo como antes | O `router.js` ainda usa a condição antiga (passo 2 da 6.1) |
| O Funcionário não abre uma tela que é dele | Falta a rota no mapa `FUNCIONARIO` |

**Antes de avançar:**
- [ ] a tabela do `testar_guarda` igual à de cima;
- [ ] `IGUAIS`;
- [ ] PR com as duas fotos.

**Perguntas para a Review:** por que a guarda é "por lista" (o que não está na lista fica fechado)? Se a guarda falhar, quem segura o acesso?

> **Lembre:** a guarda é experiência de uso. **A segurança é o `403` do backend** (CT05, CT08), testado na Fase 1 do Lucas e no passo 14 do *gate* (Parte 10).

---

## Parte 7 — A13: o teste instável (a causa já foi achada)

**Quando (decisão A45, 31/10):** logo depois da Parte 5, antes da guarda e das telas. O prazo final continua qua 04/11. A correção é de uma linha, e sem ela a verificação 21 interrompe o `npm test` em 8 de 10 rodadas: as verificações das suas telas novas (que ficam depois dela) quase nunca rodariam.

**Onde:** Terminal A, dentro de `frontend`; Terminal B servindo os arquivos. Branch nova, a partir da `main` atualizada:

```powershell
cd ..
git checkout main
git pull
git checkout -b fix/frontend-teste-remocao
cd frontend
```

### 7.1 O que acontece

A verificação 21 ("porções zeradas e remoção com confirmação") termina assim:

```js
await page.getByRole('dialog').getByRole('button',{name:'Remover',exact:true}).click();
assert.equal(await page.getByRole('spinbutton').count(),0);
```

E o código da tela (`confirmRemoval`) faz:

```js
button('Remover',()=>dialog.close('remove'),'primary')
...
dialog.addEventListener('close',()=>{const yes=dialog.returnValue==='remove';dialog.remove();resolve(yes);},{once:true});
```

**A causa:**
1. O `close()` do `<dialog>` não avisa na hora: o evento `close` chega numa **próxima volta** do navegador.
2. Só então a promessa se resolve, e só depois a tela remove o ingrediente (`draw()`).
3. O teste conta os campos **logo depois do clique**. Às vezes conta antes da remoção: `1 !== 0`.

A tela está **certa**; o teste é que não espera.

### 7.2 A correção (só no teste)

**Onde:** arquivo `frontend\tests\browser.cjs`, na linha 58 (a da verificação 21).

No VS Code, **Ctrl+F** e procure `exact:true}).click();assert.equal(await page.getByRole('spinbutton').count(),0);`. Entre o `click();` e o `assert`, acrescente:

```js
await page.getByRole('spinbutton').waitFor({state:'detached'});
```

O trecho fica assim:

```js
...getByRole('button',{name:'Remover',exact:true}).click();await page.getByRole('spinbutton').waitFor({state:'detached'});assert.equal(await page.getByRole('spinbutton').count(),0);
```

**O que faz:** espera (até 30 s) o campo **sair** da página. Na prática, alguns milissegundos.

### 7.3 Provar

```powershell
$ok = 0
1..10 | ForEach-Object { $s = npm test 2>&1 | Out-String; if ($s -match "\d+ verifica") { $ok++ } else { ($s -split "`n" | Select-String "^OK " | Select-Object -Last 1).Line } }
"passaram: $ok de 10"
"passaram: $ok de 10 (com a correção da A13)" | Out-File -Encoding utf8 ..\docs\Sprint_4\evidencias\2026-10-30_leonardo_a13_depois.txt
```

**Você deve ver** (validado): `passaram: 10 de 10`. Em 35 rodadas com a correção, 34 passaram; a única falha foi na verificação 15 ("saída remove sessão…"), uma vez. **Se ela aparecer de novo,** é outro caso: registre a mensagem completa no PR e investigue do mesmo jeito (o que o teste confere logo depois de um clique?).

```powershell
cd ..
git add frontend\tests\browser.cjs docs\Sprint_4\evidencias
git commit -m "test(frontend): espera a remocao do ingrediente antes de contar (A13)"
git push -u origin fix/frontend-teste-remocao
```

No PR, explique a causa (7.1) e cole as duas contagens: antes (Parte 3.2) e depois. **Nunca** desative um teste para "passar".

| Se der errado | O que fazer |
|---|---|
| O **Ctrl+F** não acha o trecho | O arquivo já foi mexido: procure só `getByRole('spinbutton').count(),0)` e confira que é a linha da verificação 21 |
| Continua falhando na 21 | A linha nova ficou **depois** do `assert`, e não antes |
| `passaram: 0 de 10` | O Terminal B parou, ou o laço procura o texto antigo, com acento (use `\d+ verifica`) |

**Pergunta para a Review:** por que o defeito estava no teste, e não na tela? Como você provou?

---

## Parte 8 — Publicação no GitHub Pages (sex 30/10; a URL do Render na seg 02/11)

**Objetivo:** o frontend publicado em `https://kauencarneiro.github.io/Mente_Fria/`, falando com o backend do Render.
**Por que pelo GitHub Actions:** o Pages "por branch" só publica a raiz ou a pasta `/docs`, e o nosso frontend está em `frontend/`.
**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`, numa branch nova a partir da `main` atualizada:

```powershell
git checkout main
git pull
git checkout -b feature/frontend-pages
```

### 8.1 Os dois arquivos

```powershell
Copy-Item docs\Sprint_3\referencia\deploy\config.js frontend\js\config.js
New-Item -ItemType Directory -Force .github\workflows | Out-Null
Copy-Item docs\Sprint_3\referencia\deploy\frontend-pages.yml .github\workflows\frontend-pages.yml
```

**O `config.js` novo:**

```js
const PRODUCAO = 'https://mente-fria-backend.onrender.com';   // TROCAR pela URL que o Render mostrar no painel
const NO_PAGES = location.hostname.endsWith('github.io');

export const API_BASE_URL = NO_PAGES ? PRODUCAO : 'http://localhost:8080';
export const REQUEST_TIMEOUT_MS = NO_PAGES ? 90000 : 15000;
```

- **No `github.io`**, ele usa o backend do Render e espera até **90 s**: o plano gratuito do Render "dorme" depois de 15 min sem uso e leva cerca de 1 min para acordar. Com 15 s, a primeira chamada do dia daria "O servidor demorou para responder" (CT70).
- **Em qualquer outro endereço** (o seu `127.0.0.1`, os testes), continua com `http://localhost:8080` e 15 s. Por isso o `npm test` não muda.
- **`PRODUCAO`** só ganha o valor certo na **seg 02/11**, quando o Kauê mandar a `<URL_DO_SERVICO>` (Parte 8.3).

**O *workflow*** (`.github/workflows/frontend-pages.yml`):
- publica a pasta `frontend/` a cada push na `main` que mexa no frontend;
- também pode ser disparado à mão (aba **Actions** → "Frontend no GitHub Pages" → *Run workflow*);
- as versões das *actions* foram conferidas no GitHub em 26/10.

### 8.2 Conferir, commit e PR

O `config.js` tem um teste próprio, junto com as funções de apoio das telas novas (Parte 15). Copie as funções agora:

```powershell
Copy-Item docs\Sprint_3\referencia\frontend\js\apoio_telas_novas.js frontend\js\
cd frontend
node ferramentas\testar_apoio_telas.mjs .
npm test
cd ..
```

**Você deve ver** (validado) 15 linhas `OK`, entre elas `OK config.js em equipe.github.io` e `OK config.js em 127.0.0.1`, e no fim `15 verificações passaram.`

```powershell
git add frontend\js\config.js frontend\js\apoio_telas_novas.js .github\workflows\frontend-pages.yml
git commit -m "feat(frontend): config de producao e publicacao no GitHub Pages"
git push -u origin feature/frontend-pages
```

**Uma vez, o Kauê (dono do repositório):** *Settings* → *Pages* → *Build and deployment* → *Source*: **GitHub Actions**. Sem isso, o *workflow* falha. Combine com ele na sex 30/10.

### 8.3 Seg 02/11: a URL do Render

Quando o Kauê mandar a `<URL_DO_SERVICO>`:
1. `git checkout main`, `git pull` e `git checkout -b fix/frontend-url-render`;
2. no `frontend\js\config.js`, troque o valor de `PRODUCAO` pela URL, **sem barra no fim e sem `/api`**;
3. commit, PR e merge. O *workflow* publica sozinho.

### 8.4 Conferir a publicação

1. Depois do merge, a aba **Actions** mostra o *workflow* verde e a URL, algo como `https://kauencarneiro.github.io/Mente_Fria/`.
2. **A origem do CORS** é só `https://kauencarneiro.github.io` (sem `/Mente_Fria/` e sem barra). É esse valor que o Render usa em `CORS_ALLOWED_ORIGIN`.
3. Abra a URL, **F12** → *Console*, e entre com o Administrador (o Kauê passa a senha por mensagem privada). Não pode aparecer `blocked by CORS policy`. **Em produção, não tire print do DevTools:** a aba *Payload* do login mostra a senha real, e a *Response*, o token.
4. Todos os caminhos de arquivo precisam ser **relativos** (`css/…`, `js/…`). Com caminho absoluto (`/css/…`), o Pages procura na raiz do domínio e não acha. Hoje já são; mantenha assim nas telas novas.
5. Guarde um print com a URL visível em `docs/Sprint_4/evidencias/` e ponha a URL no `frontend/README.md`.

O *workflow* e o `config.js` **não foram executados no GitHub** nesta validação: dependem do repositório. O `config.js` foi conferido com Node nos dois endereços.

| Se der errado | O que fazer |
|---|---|
| O *workflow* falha logo no começo, falando de Pages | O Kauê ainda não ativou *Source: GitHub Actions* (8.2) |
| O *workflow* nem aparece na aba *Actions* | O arquivo não está em `.github/workflows/` na **raiz** do repositório, ou o merge não mexeu em `frontend/` (use *Run workflow*) |
| No Pages, as chamadas vão para `localhost:8080` | O endereço não termina em `github.io`, ou o `config.js` publicado ainda é o antigo |

**Pergunta para a Review:** por que o `config.js` escolhe o backend pelo endereço da página, e por que o tempo limite muda?

---

## Parte 9 — Subir o backend real na sua máquina (antes do *gate*)

**Objetivo:** o backend do Lucas rodando em `http://localhost:8080`, com o banco do jeito da Sprint 4.
**Quando:** sáb 31/10, de manhã, assim que o Lucas avisar no grupo que o PR da Fase 2 está **na `main`** (o merge, e não só os testes dele).

**Quando entrar uma fase nova** (a Fase 3 à tarde, e assim por diante):
- o backend roda com o código da **branch em que você está**. Na sua branch de tela, traga a `main` (Parte 2.4) e **reinicie** o backend (9.3). Não precisa recriar o banco;
- **recrie o banco (9.2) só** se o `bd\Script_DDL.sql` mudar, ou se quiser começar do zero. Depois, refaça os dois usuários pela tela (passos 2 e 6 da Parte 10).

### 9.1 Pré-requisitos (uma vez)

| O quê | Como conferir | Onde está explicado |
|---|---|---|
| JDK 25 | `java -version` mostra 25 | Tutorial do Lucas, Parte 1.1 |
| Docker Desktop aberto e o `mente-fria-db` rodando | `docker ps --filter name=mente-fria-db` mostra `Up` | Tutorial do Lucas, Parte 1.2 (inclui o travamento A26: **nunca** "Reset to factory defaults") |
| `backend\.env` com `DB_PASSWORD` e um `JWT_SECRET` de 64 caracteres, **sem BOM** | `Test-Path backend\.env` | Tutorial do Lucas, Parte 1.3 |

### 9.2 Atualizar o código e recriar o banco

**Onde:** Terminal A, na `<PASTA_DO_PROJETO>`. Se o Terminal C estiver com um backend rodando, pare antes (**Ctrl+C**). Rode `git status` primeiro: se houver trabalho sem commit numa branch de tela, faça o commit (Parte 2.2) antes de trocar de branch.

```powershell
git checkout main
git pull
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria WITH (FORCE);"
docker exec mente-fria-db psql -U postgres -c "CREATE DATABASE mente_fria;"
docker cp bd\Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql
```

| Comando | O que faz |
|---|---|
| `git checkout main`, `git pull` | O backend e o `bd\Script_DDL.sql` mais novos (o do Diogo, da Sprint 4) |
| `DROP DATABASE … WITH (FORCE)` | Apaga **o banco `mente_fria`** dentro do container (não o container). O `FORCE` derruba conexões abertas |
| `CREATE DATABASE` | Cria o banco vazio |
| `docker cp` + `psql … -f` | Copia o DDL para dentro do container e o executa: tabelas, regras e os dados iniciais (7 ingredientes com estoque 0 e 3 produtos) |

**Você deve ver:** `DROP DATABASE` e `CREATE DATABASE`, e nada depois do último comando. Esses comandos foram validados no tutorial do Lucas (Parte 4.8).

> ⚠️ Recriar o **banco** apaga os seus cadastros de teste. Nunca apague, recrie ou resete o **container** `mente-fria-db`.

### 9.3 Subir o backend (Terminal C)

```powershell
cd "<PASTA_DO_PROJETO>\backend"
.\mvnw spring-boot:run
```

**Você deve ver** no fim do log (validado): `Started BackendApplication in 1.884 seconds`. O tempo muda; na primeira vez, o Maven baixa bibliotecas e demora mais. Deixe esse terminal aberto.

**Conferir**, no Terminal A:

```powershell
Invoke-RestMethod http://localhost:8080/api/saude
```

**Você deve ver** (validado):

```
status banco
------ -----
ok     ok
```

| Se der errado | O que fazer |
|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` (ou `DB_USER`, `DB_PASSWORD`) | O `backend\.env` não existe, falta a linha, ou ele foi salvo **com BOM** (tutorial do Lucas, Parte 1.3) |
| `password authentication failed` | `DB_PASSWORD` diferente da senha do `mente-fria-db` |
| `Port 8080 was already in use` | Outro backend ainda está rodando. Feche o outro terminal ou use **Ctrl+C** nele |

---

## Parte 10 — *Gate* da Fase 2: aceite com o backend **real** (sáb 31/10, de manhã)

**Objetivo:** provar, pela **tela**, que o frontend fala certo com o backend real. Só depois disso as telas novas passam a usar o backend real.
**Quando:** só quando o PR da Fase 2 estiver **na `main`** (M2; o Lucas avisa no grupo). Depois do `git pull` da 9.2, confira com `git log --oneline -5`: deve aparecer o merge da branch `feature/backend-fase2-cadastros`.
**Onde:**
- Terminal B servindo os arquivos (Parte 1.3), da `main` atualizada (com a divisão e a guarda, se já entraram);
- Terminal C com o backend (Parte 9), com o banco **recém-criado**;
- Chrome em `http://127.0.0.1:5500` (**sempre `127.0.0.1`**), com **F12** → *Network* → **Preserve log** marcado.

**Usuários de teste (só na sua máquina):** Administrador `dono@acai.com`, Funcionário `func@acai.com`, senha `senha1234`. **Nunca** use essa senha no Render.

**Como ler o *Network*:** clique na linha da chamada. Em *Headers* aparece o status (`Status Code`); em *Payload*, o que a tela enviou; em *Response*, o que o backend respondeu.

| # | Ação na tela | O que conferir no *Network* | Você deve ver (validado com o backend real) |
|---|---|---|---|
| 1 | `#/comunicacao`: o 1º "Testar conexão" (saúde) e o 2º (ingredientes), sem login | `GET /api/saude`, `GET /api/ingredientes` | `200` `{"status":"ok","banco":"ok"}`; ingredientes `401` `{"mensagem":"Sua sessão expirou ou você não está autenticado."}` |
| 2 | `#/cadastro`: criar o Administrador (Dono, `dono@acai.com`) | `POST /api/auth/registro`, depois `POST /api/auth/login` | `201` `{"id":1,"nome":"Dono","email":"dono@acai.com","perfil":"ADMINISTRADOR"}`; login `200` com `token`, `perfil` e `nome`; a tela vai para Funcionários ("Gerencie o acesso…") |
| 3 | **Sair**, voltar a `#/cadastro` e tentar criar outro | `POST /api/auth/registro` | `409` `{"mensagem":"O cadastro do Administrador já foi realizado."}`, e a mensagem na tela |
| 4 | Entrar com a senha errada | `POST /api/auth/login` | `401` `{"mensagem":"E-mail ou senha inválidos."}` na tela. Depois, entre com a senha certa |
| 5 | `#/funcionarios/novo` com o e-mail `dono@acai.com` (repetido) | `POST /api/usuarios` | `409` `{"mensagem":"Dados inválidos","campos":{"email":"Este e-mail já está cadastrado."}}`: a mensagem aparece **embaixo do campo e-mail**, e "Dados inválidos" no aviso geral |
| 6 | Cadastrar o Funcionário (Func, `func@acai.com`) | *Payload* do `POST /api/usuarios` | Só `nome`, `email` e `senha` (nada de perfil); `201` com `"perfil":"FUNCIONARIO"`; "Funcionário cadastrado" |
| 7 | `#/ingredientes` | `GET /api/ingredientes` | `200` com **7** itens e todos os campos; a primeira linha: `Açaí (polpa) kg R$ 18,50 0 kg Editar` (**estoque 0**) |
| 8 | Editar o Açaí e salvar sem mudar nada | `PUT /api/ingredientes/1` | `200`; *Payload* `{"nome":"Açaí (polpa)","unidade_medida":"kg","custo_unitario":18.5,"porcao_padrao":0.15,"quantidade_minima":10}`; "Ingrediente salvo" |
| 9 | Novo ingrediente com custo `-1` e porção `0` | **Nenhuma** chamada `POST` | A **tela** barra antes de enviar, com as duas mensagens, cada uma no seu campo: "O custo não pode ser negativo…" e "A porção deve ser maior que zero…". (O `400` do backend para o mesmo caso foi conferido pelo Lucas, na Fase 2) |
| 10 | Novo produto, com o Açaí e **2** porções, preço `15,00` | *Payload* do `POST /api/produtos` | `{"nome":…,"preco_venda":15,"composicao":[{"ingrediente_id":1,"quantidade_utilizada":0.3}]}` (2 × 0,150); `201`; "Produto cadastrado" |
| 11 | Produto sem ingrediente; depois, só com Granola | Nenhuma chamada `POST` | "Adicione ao menos um ingrediente." e, com a Granola, "O produto precisa ter o açaí…" |
| 12 | `#/produtos` | `GET /api/produtos` | `200`; cada produto com `composicao[]` contendo `ingrediente_nome`, `quantidade_utilizada` e `unidade_medida` |
| 13 | Alterar o preço do produto 1 para `13,50` | `PUT /api/produtos/1/preco` | *Payload* `{"preco_venda":13.5}`, com o cabeçalho `Authorization: Bearer …`; `200`; "Preço atualizado" |
| 14 | Sair, entrar como **Func** e abrir `#/funcionarios/novo` | Nenhuma chamada a `/api/usuarios` | "Esta área é exclusiva do Administrador." Depois, o teste no *Console* (abaixo): `403 {"mensagem":"Você não tem permissão para esta ação."}` |
| 15 | Estragar o token (abaixo) e abrir `#/ingredientes` | `GET /api/ingredientes` | `401`; a tela volta ao login com "Sua sessão expirou. Entre novamente." |
| 16 | Parar o backend (Terminal C: **Ctrl+C**) e tentar entrar | — | "Não foi possível conectar à API em http://localhost:8080. Verifique se o backend está ligado…" (diferente de senha inválida) |

**Passo 14, o `403` direto na API.** Logado como Func, **F12** → *Console*. Cole e tecle Enter:

```js
const tok = JSON.parse(sessionStorage.getItem('mente-fria.session')).token;
const r = await fetch('http://localhost:8080/api/usuarios', {method: 'POST', headers: {'Content-Type': 'application/json', Authorization: 'Bearer ' + tok}, body: JSON.stringify({nome: 'X', email: 'x@acai.com', senha: 'senha1234'})});
r.status + ' ' + await r.text();
```

**O que faz:** pega o token do Funcionário e tenta, sem passar pela tela, criar um usuário. A tela já esconde essa ação; aqui você prova que o **backend** também recusa.

**Você deve ver** (validado): `'403 {"mensagem":"Você não tem permissão para esta ação."}'`.

> Na primeira vez, o Chrome pode recusar colar no *Console* e pedir que você digite `allow pasting` (ou `permitir colar`). Digite, tecle Enter e cole de novo.

**Passo 15, estragar o token.** **F12** → *Application* → *Session Storage* → `http://127.0.0.1:5500` → `mente-fria.session`. No valor, troque as **duas últimas letras** do `token` e tecle Enter. Depois abra `#/ingredientes`.

**As evidências:**
- um print por passo (16), com a data e a hora visíveis, em `docs/Sprint_4/evidencias/`, com nomes como `2026-10-31_leonardo_gate-fase2_01-saude.png`;
- **nunca** deixe senha ou token aparecendo num print (regra do `LEIA-ME` das evidências):
  - nos passos 2, 4 e 6, a aba *Payload* mostra a **senha**, e a *Response* do login mostra o **token**: fotografe a tela e a aba *Headers*, só;
  - em todos, na aba *Headers*, role para que a linha `Authorization` fique de fora;
  - no passo 15, não fotografe a *Session Storage* (ela mostra o token);
  - na dúvida, cubra com um retângulo antes de salvar.
- No fim, entregue os prints por PR, numa branch `docs/leonardo-gate-fase2` (Parte 2), com `git add docs\Sprint_4\evidencias`.

**Se algo divergir:**

| Divergência | Quem corrige |
|---|---|
| Campo faltando ou com nome diferente na **resposta** | Lucas (o contrato manda `snake_case`) |
| O seu *payload* não bate com o contrato | Você |
| Mensagem diferente da do contrato ou do `docs/Apoio/mapa_constraints.md` | Lucas |
| Contrato ambíguo | Registrar no contrato (o Kauê é o dono) |

**Uma falha de `fetch` não prova que é CORS** (pode ser o backend desligado). Se aparecer `blocked by CORS policy`, confira se você está em `127.0.0.1:5500` e chame o Lucas.

| Se der errado | O que fazer |
|---|---|
| Tudo dá `ERR_CONNECTION_REFUSED` ou "Não foi possível conectar…" | O backend (Terminal C) não está rodando |
| O passo 2 dá `409` logo de cara | O banco já tinha um Administrador: recrie o banco (9.2) |
| O passo 7 mostra estoque diferente de 0 | O banco não é o recém-criado |
| O passo 14 deixa o Funcionário abrir a tela | A guarda da Parte 6 ainda não está na `main` que você está servindo |

**Perguntas para a Review:** qual a diferença entre o passo 9 (a tela barra) e um `400` do backend? Por que o passo 14 testa a API direto, além da tela?

Quando os 16 passarem, avise no grupo:

```
Gate da Fase 2 aprovado: 16 passos com o backend real, prints em docs/Sprint_4/evidencias/.
A partir de agora as telas usam o backend real.
```

---

## Parte 11 — Anatomia de uma tela nova: o Estoque (Telas 10/16)

**Objetivo:** a primeira tela nova, do jeito que todas as outras vão ser feitas: lista do estoque e detalhe por linha de entrada.
**Por que esta primeiro:**
- é a primeira do seu calendário (sex 30/10, com a API simulada; sáb e dom, com o backend real);
- o detalhe por linha é o que **não pode ser cortado** (mostra "Vence em breve" e "Vencida": a cobertura do alerta de vencimento, adendo, item 4).

**Onde:** branch `feature/frontend-estoque`, criada depois do merge da guarda (Parte 2.1); Terminal A dentro de `frontend`.

O código desta parte foi escrito para este tutorial e **validado**: com a API simulada (23 verificações) e com o backend real, nos dois perfis. Use como **molde**.

### 11.1 Os seis passos de toda tela nova

| # | Passo | Arquivo |
|---|---|---|
| 1 | Ler no contrato o formato da API | `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` |
| 2 | Escrever o módulo da tela | `js/screens/<tela>.js` |
| 3 | Registrar a rota | `js/router.js` |
| 4 | Pôr no menu | `js/screens/shell.js` |
| 5 | Liberar para o Funcionário, se ele usa a tela | `js/nav.js` (mapa `FUNCIONARIO`) |
| 6 | Ensinar a API simulada e escrever a verificação | `tests/browser.cjs` |

### 11.2 Passo 1: o contrato (§2.4)

```json
GET /api/estoque
[ { "ingrediente_id": 3, "nome": "Granola", "unidade_medida": "kg", "quantidade_estoque": 11.160,
    "quantidade_minima": 2.000, "quantidade_valida": 5.160, "quantidade_vencida": 6.000,
    "validade_mais_proxima": "2026-12-10" } ]

GET /api/estoque/{ingrediente_id}/linhas
[ { "id": 12, "data_movimentacao": "2026-10-20T09:15:00", "quantidade": 6.000, "quantidade_restante": 6.000,
    "data_validade": "2026-10-24", "situacao": "VENCIDA" } ]
```

- `quantidade_estoque` é o **total** (inclui o vencido até o descarte); `quantidade_valida` é o que pode ser usado.
- **"Abaixo do mínimo"** é calculado **pela tela**: `quantidade_valida ≤ quantidade_minima` (decisão de 27/10, no contrato).
- `situacao`: `VENCIDA`, `PROXIMA` (até 7 dias), `OK` ou `SEM_VALIDADE`. Mostre sempre em **texto**, nunca só com cor.
- Os dois são para **Administrador e Funcionário**.

### 11.3 Passo 2: o módulo `js/screens/estoque.js`

Crie o arquivo com:

```js
// Telas 10/16: o estoque de cada ingrediente (lista) e as linhas de entrada de um ingrediente (detalhe).
import {list} from '../api.js';
import {amount, unit, node, link, heading, loading, table} from '../ui.js';

const SITUACAO = {VENCIDA: 'Vencida', PROXIMA: 'Vence em breve', OK: 'Dentro da validade', SEM_VALIDADE: 'Sem validade'};
const data = iso => iso ? new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR') : '—';
const qtd = (valor, un) => amount(valor) + ' ' + unit(un);
export const abaixoDoMinimo = item => Number(item.quantidade_valida) <= Number(item.quantidade_minima);

export async function stock(main, action, id) {
  if (action === 'linhas') return stockLines(main, id);
  heading(main, 'Estoque', 'Quanto há de cada ingrediente, quanto ainda pode ser usado e quanto venceu');
  const itens = await loading(main, () => list('/api/estoque'));
  if (!itens) return;
  main.append(table(
    ['Ingrediente', 'Total', 'Válido', 'Vencido', 'Validade mais próxima', 'Situação', 'Ação'],
    itens.map(x => [
      x.nome,
      qtd(x.quantidade_estoque, x.unidade_medida),
      qtd(x.quantidade_valida, x.unidade_medida),
      Number(x.quantidade_vencida) > 0 ? qtd(x.quantidade_vencida, x.unidade_medida) : '—',
      data(x.validade_mais_proxima),
      abaixoDoMinimo(x) ? node('span', {class: 'badge inactive'}, 'Abaixo do mínimo') : 'Ok',
      link('Ver linhas', 'estoque/linhas/' + x.ingrediente_id)
    ])));
}

async function stockLines(main, id) {
  heading(main, 'Linhas de entrada', 'Cada entrada do ingrediente, quanto resta dela e a validade', null, 'estoque');
  const linhas = await loading(main, () => list(`/api/estoque/${id}/linhas`));
  if (!linhas) return;
  main.append(table(
    ['Entrada em', 'Quantidade', 'Resta', 'Validade', 'Situação'],
    linhas.map(l => [
      new Date(l.data_movimentacao).toLocaleString('pt-BR'),
      amount(l.quantidade),
      amount(l.quantidade_restante),
      data(l.data_validade),
      SITUACAO[l.situacao] ?? l.situacao
    ])));
}
```

**Linha a linha, o que importa:**

| Trecho | Por quê |
|---|---|
| `import {list} …` e `import {…} from '../ui.js'` | A tela só usa o que importa. O `..` sobe de `screens` para `js` |
| `SITUACAO` | Traduz o código da API para o texto que o dono entende |
| `data(iso)` com `'T00:00:00'` | `new Date('2026-12-10')` seria meia-noite **UTC**, e no Brasil viraria o dia 9. Com a hora, a data fica no fuso local |
| `abaixoDoMinimo` com `export` | A regra fica num lugar só, e outra tela (o Dashboard) pode reaproveitar |
| `stock(main, action, id)` | A mesma assinatura das outras telas: o roteador passa a área principal e as partes do `#` |
| `heading(…)` | O título; no detalhe, o último argumento (`'estoque'`) cria o "← Voltar para lista" |
| `await loading(main, () => list(…))` | "Carregando…", a chamada, e o erro com "Tentar novamente" de graça. Se devolver `null`, a tela para (deu erro, ou o usuário já mudou de tela) |
| `table(cabeçalhos, linhas)` | Cada item da API vira uma linha; os valores entram como **texto** |
| `'—'` para vazio | A célula nunca fica em branco sem explicação |

### 11.4 Passos 3, 4 e 5: rota, menu e guarda

**`js/router.js`** (duas mudanças):
1. depois da linha `import {communication} from './screens/comunicacao.js';`, acrescente:

   ```js
   import {stock} from './screens/estoque.js';
   ```

2. antes da linha `  else if(section==='inicio'){`, acrescente:

   ```js
     else if(section==='estoque'&&(!action||action==='linhas'&&/^\d+$/.test(id)))await stock(main,action,id);
   ```

   Lê-se: "se a seção é `estoque` **e** (não há ação, **ou** a ação é `linhas` com um `id` só de dígitos), desenhe o estoque". Qualquer outra coisa cai em "Página não encontrada".

**`js/screens/shell.js`:** troque `['Estoque',null,2]` por `['Estoque','estoque']`. Ele aparece **duas vezes** (o menu do Administrador e o do Funcionário): troque as duas. Com o `null`, o item aparecia apagado, com "S2"; com a rota, vira um link.

**`js/nav.js`:** no mapa `FUNCIONARIO`, troque `estoque: [undefined],` por `estoque: [undefined, 'linhas'],`. Sem isso, a guarda manda o Funcionário de volta ao início quando ele abre o detalhe.

### 11.5 Passo 6: a API simulada e a verificação

No `tests\browser.cjs`:

1. **Os dados de mentira.** Antes da linha que começa com `const products=[`, acrescente:

   ```js
   const stock=[{ingrediente_id:1,nome:'Açaí (polpa)',unidade_medida:'kg',quantidade_estoque:26,quantidade_minima:10,quantidade_valida:20,quantidade_vencida:6,validade_mais_proxima:'2026-11-05'},{ingrediente_id:3,nome:'Granola',unidade_medida:'kg',quantidade_estoque:1.5,quantidade_minima:2,quantidade_valida:1.5,quantidade_vencida:0,validade_mais_proxima:null}];
   const stockLines=[{id:12,data_movimentacao:'2026-10-20T09:15:00',quantidade:6,quantidade_restante:6,data_validade:'2026-10-24',situacao:'VENCIDA'},{id:13,data_movimentacao:'2026-10-28T10:00:00',quantidade:20,quantidade_restante:20,data_validade:'2026-11-05',situacao:'PROXIMA'}];
   ```

2. **As rotas simuladas.** Antes da linha `   else throw Error('Rota inesperada: '+url);`, acrescente:

   ```js
      else if(url==='/api/estoque')value=stock;
      else if(/^\/api\/estoque\/\d+\/linhas$/.test(url))value=stockLines;
   ```

   **Por quê:** o teste intercepta **toda** chamada a `http://localhost:8080`. Rota que ele não conhece cai no `throw` "Rota inesperada", de propósito: assim nenhuma tela chama o backend sem que o teste saiba.

3. **A verificação.** Antes da linha que começa com ` const artifacts=path.join(`, acrescente:

   ```js
    await check('estoque mostra válido, vencido e abaixo do mínimo, e o detalhe por linha',async()=>{await goto('estoque');await visible('Abaixo do mínimo');await page.getByRole('cell',{name:'6 kg',exact:true}).waitFor();await page.getByRole('link',{name:'Ver linhas'}).first().click();await visible('Vence em breve');await visible('Vencida');assert(calls.some(x=>x.url==='/api/estoque/1/linhas'));});
   ```

   Ela abre o estoque, espera o selo da Granola (1,5 válido < 2 mínimo) e a célula **exatamente** `6 kg` (o vencido do Açaí; com uma busca por texto solto, "26 kg" também combinaria), clica no primeiro "Ver linhas" e confere os dois textos e a chamada certa.

### 11.6 Rodar com a API simulada

```powershell
node --check js\screens\estoque.js
node --check js\router.js
node --check tests\browser.cjs
npm test
```

**Você deve ver** no fim (validado, com a correção da A13 já aplicada):

```
OK porções zeradas e remoção com confirmação
OK estoque mostra válido, vencido e abaixo do mínimo, e o detalhe por linha
OK screenshots desktop e mobile sem overflow
23 verificações passaram. Integração HTTP simulada.
```

Sem a correção da A13, a 21 interrompe o teste na maioria das rodadas, e a verificação do estoque (que vem depois dela) não chega a rodar: é por isso que a Parte 7 vem antes.

> **Depois de uma tela nova, o retrato muda** (o menu tem um link a mais). Isso é o esperado: o `IGUAIS` só vale para refatorações, como a divisão e a guarda.

### 11.7 Rodar com o backend real (sáb 31/10 à tarde, depois da Fase 3)

**Pré-requisitos:** a Fase 3 do Lucas na `main` (`git fetch origin` e `git merge --no-edit origin/main` na sua branch, Parte 2.4); o backend reiniciado (Terminal C: **Ctrl+C** e `.\mvnw spring-boot:run` de novo); os dois usuários do *gate* (se você recriou o banco, refaça os passos 2 e 6 da Parte 10).

**Criar estoque de teste** (o formulário de movimentação ainda não existe). Entre como **Dono** e, no *Console* (**F12**), cole:

```js
const tok = JSON.parse(sessionStorage.getItem('mente-fria.session')).token;
const d = n => { const x = new Date(); x.setDate(x.getDate() + n); return x.toISOString().slice(0, 10); };
const out = [];
for (const b of [{ingrediente_id: 1, tipo: 'ENTRADA', quantidade: 6, data_validade: d(-3)}, {ingrediente_id: 1, tipo: 'ENTRADA', quantidade: 20, data_validade: d(3)}, {ingrediente_id: 3, tipo: 'ENTRADA', quantidade: 1.5}]) {
  const x = await fetch('http://localhost:8080/api/estoque/movimentacoes', {method: 'POST', headers: {'Content-Type': 'application/json', Authorization: 'Bearer ' + tok}, body: JSON.stringify(b)});
  out.push(x.status);
}
out.join(' ');
```

**O que faz:** três entradas pela API. Açaí: 6 kg vencidos há 3 dias (o contrato aceita entrada já vencida) e 20 kg que vencem em 3 dias. Granola: 1,5 kg sem validade.

**Você deve ver** (validado): `'201 201 201'`.

Abra `#/estoque`. **Você deve ver** (validado; as datas mudam):

```
Açaí (polpa)              26 kg   20 kg   6 kg   02/11/2026   Ok                 Ver linhas
Banana                    0 kg    0 kg    —      —            Abaixo do mínimo   Ver linhas
Copo descartável 500ml    0 un    0 un    —      —            Abaixo do mínimo   Ver linhas
...
```

Clique em "Ver linhas" do Açaí:

```
30/10/2026, 16:16:35   6    6    27/10/2026   Vencida
30/10/2026, 16:16:35   20   20   02/11/2026   Vence em breve
```

Saia e entre como **Func**: `#/estoque` e `#/estoque/linhas/1` abrem igual (validado). No *Network*: `GET /api/estoque` e `GET /api/estoque/1/linhas` com `200`.

| Se der errado | Causa | O que fazer |
|---|---|---|
| `Rota inesperada: /api/estoque` no `npm test` | Falta a rota simulada (11.5, item 2) | Acrescente as duas linhas antes do `throw` |
| "Página não encontrada" no `#/estoque` | A rota não está no `router.js`, ou o `import` faltou | 11.4 |
| O menu mostra "Estoque" apagado, com "S2" | O `shell.js` ainda tem `['Estoque',null,2]` | Troque as duas ocorrências |
| O Funcionário volta ao início no detalhe | Falta `'linhas'` no mapa `FUNCIONARIO` | 11.4 |
| Tela em branco | Erro de `import` | **F12** → *Console* |
| `403` no *Console* ao criar o estoque de teste | Você está logado como Func | Entre como Dono |

### 11.8 Commit e PR

Guarde a saída do `npm test` (Parte 2.5) como `…_leonardo_estoque_npm-test.txt` e os dois prints da 11.7.

```powershell
cd ..
git add frontend\js\screens\estoque.js frontend\js\router.js frontend\js\screens\shell.js frontend\js\nav.js frontend\tests\browser.cjs docs\Sprint_4\evidencias
git commit -m "feat(frontend): estoque por ingrediente e detalhe por linha (Telas 10/16)"
git push -u origin feature/frontend-estoque
```

No PR: o resultado do `npm test` e dois prints da 11.7 (lista e detalhe).

### 11.9 Perguntas para a Review

1. O que acontece se o backend cair enquanto o estoque carrega? (Dica: o `loading`.)
2. Por que "Abaixo do mínimo" usa o **válido**, e não o total?
3. Por que a data recebe `'T00:00:00'`?
4. Por que o teste lança "Rota inesperada" para uma rota que ele não conhece?

---

## Parte 12 — O resto do Estoque: movimentação e o botão "Descartar"

Siga os seis passos da Parte 11. A movimentação pode ser uma rota nova (`#/estoque/movimentar`), que o Funcionário também usa:
- no mapa `FUNCIONARIO`, `estoque: [undefined, 'linhas', 'movimentar'],`;
- no `router.js`, a condição do estoque passa a aceitar também `action==='movimentar'`: `(!action||action==='movimentar'||action==='linhas'&&/^\d+$/.test(id))`.

### 12.1 Movimentação (`POST /api/estoque/movimentacoes`, contrato §2.4)

- Tipo **Entrada** ou **Saída**.
- Na saída, o motivo **"Saída convencional"** (`CONVENCIONAL`) ou **"Descarte de vencidos"** (`DESCARTE_VENCIDO`). O descarte só fica habilitado quando o ingrediente tem `quantidade_vencida > 0`.
- Validade **só** na entrada.
- O `usuario_id` **nunca** vai no corpo: vem do token.

**A validação, pronta e conferida com Node** (9 verificações nesta validação, junto com as das Partes 13 e 14). Ela usa o `decimal` do `js/validation.js`, então o módulo da tela precisa de `import {decimal} from '../validation.js';` no topo:

```js
function validarMovimentacao(raw, linha) {   // linha = item de GET /api/estoque do ingrediente escolhido
  const issues = {}, payload = {ingrediente_id: Number(raw.ingrediente_id), tipo: raw.tipo};
  if (!raw.ingrediente_id) issues.ingrediente_id = 'Selecione o ingrediente.';
  if (!['ENTRADA', 'SAIDA'].includes(raw.tipo)) issues.tipo = 'Selecione entrada ou saída.';
  payload.quantidade = decimal(raw.quantidade, 3, true);
  if (payload.quantidade === null) issues.quantidade = 'A quantidade deve ser maior que zero. Use até 3 casas decimais.';
  if (raw.tipo === 'ENTRADA') {
    if (raw.data_validade) payload.data_validade = raw.data_validade;
  } else if (raw.tipo === 'SAIDA') {
    if (raw.data_validade) issues.data_validade = 'A validade só pode ser informada em entradas.';
    if (!['CONVENCIONAL', 'DESCARTE_VENCIDO'].includes(raw.motivo)) issues.motivo = 'Escolha saída convencional ou descarte de vencidos.';
    else payload.motivo = raw.motivo;
    if (raw.motivo === 'DESCARTE_VENCIDO' && linha && !(Number(linha.quantidade_vencida) > 0)) issues.motivo = 'Este ingrediente não tem quantidade vencida.';
    if (payload.quantidade !== null && linha) {
      const limite = raw.motivo === 'DESCARTE_VENCIDO' ? Number(linha.quantidade_vencida) : Number(linha.quantidade_valida);
      if (payload.quantidade > limite) issues.quantidade = `Quantidade maior que a disponível (${limite}).`;
    }
  }
  if (raw.observacao?.trim()) payload.observacao = raw.observacao.trim();
  return {payload, issues};
}
```

Use com o `bindForm` (Parte 4.2): `bindForm(f, raw => validarMovimentacao(raw, linhaEscolhida), async payload => { await request('/api/estoque/movimentacoes', {method: 'POST', body: payload}); … })`. Conferido: `'10,5'` vira `10.5`; uma saída de 25 com 20 válidos dá "Quantidade maior que a disponível (20)."; o descarte sem vencido é barrado.

O `422` "Estoque insuficiente…" do backend aparece como mensagem geral (o `bindForm` já faz isso); o backend revalida tudo.

### 12.2 O botão "Descartar" (A12, **opcional**)

**Só se o Lucas (PO) aprovar no M3 (dom 01/11, à noite).** É o primeiro corte se apertar.
- Aparece **só** nas linhas com `situacao = "VENCIDA"` e `quantidade_restante > 0`, no detalhe.
- Antes de enviar, peça confirmação: "Descartar 6,000 kg vencidos desta linha? Isso não pode ser desfeito." (o `confirmRemoval` do `ui.js` mostra como fazer um `<dialog>`).
- Envie `POST /api/estoque/linhas/{id}/descarte`, **sem corpo**.
- `201` → "Descarte registrado." e recarregue a tela (`window.dispatchEvent(new Event('rerender'))`).
- `422` → a `mensagem` do backend como aviso geral ("Esta linha já não tem saldo." ou "Só é possível descartar linhas vencidas."); `404` → "Linha de estoque não encontrada.".
- Desabilite o botão enquanto envia: um clique duplo não pode mandar dois descartes (CT71).

**Se der errado:** `400` com `campos.motivo` "O motivo só pode ser informado em saídas." quer dizer que a tela mandou `motivo` numa entrada; `campos.data_validade` "A validade só pode ser informada em entradas.", o contrário.

**Testes mínimos (com a API simulada):** validade desabilitada na saída; descarte desabilitado sem vencido; `422` como mensagem geral; *payload* exato; o botão "Descartar" só em linha vencida com saldo, com confirmação, e **uma** chamada por clique.

---

## Parte 13 — Pedido (Telas 11/17)

**Contrato §2.5.** `POST /api/pedidos` com `{"cliente_id": null, "itens": [{"produto_id": 1, "quantidade": 2}]}` → `201 {"id", "valor_total", "data_pedido"}`. Admin e Funcionário. A lista (`GET /api/pedidos`) vem do mais recente para o mais antigo.

**O fluxo da tela:**
- só produtos **ativos**; quantidade inteira ≥ 1; **sem repetir** o produto (desabilite a opção já escolhida, `option.disabled`, como na composição do produto);
- `+`, `−` e remover com confirmação; "Confirmar" desabilitado sem itens. O `confirmRemoval` do `ui.js` tem o texto da composição do produto ("Remover ingrediente?"): para o pedido, faça um diálogo próprio no mesmo molde, com o texto do pedido;
- o pedido vai **sem cliente** (a US13 está fora do semestre).

**Total e corpo, conferidos com Node:**

```js
const totalDoPedido = itens => itens.reduce((s, {produto, quantidade}) => s + Math.round(Number(produto.preco_venda) * 100) * quantidade, 0) / 100;
const payloadPedido = (clienteId, itens) => ({cliente_id: clienteId ? Number(clienteId) : null,
  itens: itens.map(({produto, quantidade}) => ({produto_id: produto.id, quantidade}))});
```

- `totalDoPedido` soma em **centavos** (inteiros) e só divide no fim: `2 × 12,90 + 1 × 18,90 = 44,70` (conferido). O total é só exibição: o backend recalcula com o preço do banco.
- `payloadPedido('', itens)` dá `{"cliente_id": null, "itens": [{"produto_id": 1, "quantidade": 2}]}` (conferido).
- **`422`** ("Estoque insuficiente para o produto …") → mensagem geral, e **o pedido não é limpo**.
- **`201`** → "Pedido registrado. Total: R$ …", e a tela é limpa.

**Menu e guarda:** no `shell.js`, troque `['Pedidos',null,2]` por `['Pedidos','pedidos']` (duas vezes, como no Estoque). `pedidos: [undefined, 'novo']` já está no mapa `FUNCIONARIO`.

**Testes mínimos:** sem itens bloqueia; produto repetido não duplica; *payload* exato; `422` mantém o pedido; total 44,70.

**Com o backend real:** o pedido só passa se houver estoque **válido** de **todos** os ingredientes do produto (o Tradicional usa açaí, leite condensado e copo). Com o estoque da 11.7, que só tem açaí e granola, o backend responde `422` "Estoque insuficiente para o produto …": é o comportamento certo, e serve para testar a mensagem. Para um pedido que passa, registre antes entradas desses ingredientes (como na 11.7) ou use o seed de demonstração (tutorial do Lucas, Parte 6.6). Depois do pedido, confira na tela de Estoque que o **válido** caiu e o vencido não mudou.

---

## Parte 14 — Produção (Tela 12)

**Quando:** seg 02/11 (a Fase 4A entra na `main` no dom 01/11, M3).
**Contrato §2.6.** `POST /api/producao/recomendacao`, **sem corpo**, só Administrador → `{calculado_em, receita_estimada, margem_estimada, recomendacao: [{produto_id, nome, quantidade, lucro_por_copo}]}`.

- Botão **"Gerar recomendação"**, com "Calculando…" enquanto espera. A tela espera até 15 s na sua máquina e 90 s no Pages. O backend espera até 10 s outra recomendação terminar e dá mais 10 s ao Python: na sua máquina, só passa de 15 s se duas recomendações forem pedidas ao mesmo tempo.
- Mostre "Quanto produzir" (produto, quantidade, lucro por copo) e dois destaques: **"Lucro potencial"** (`margem_estimada`) e **"Receita estimada"**. Mostre também "Calculado em …" (`calculado_em`): o resultado fica guardado no backend, e as Telas 14 e 03 o usam.
- **Por que zero**, conferido com Node:

  ```js
  const motivoDoZero = item => item.quantidade > 0 ? null : (Number(item.lucro_por_copo) <= 0 ? 'Sem lucro' : 'Falta ingrediente');
  const avisoGeral = rec => rec.some(i => i.quantidade > 0) ? null
    : (rec.every(i => Number(i.lucro_por_copo) <= 0) ? 'Nenhum produto dá lucro hoje com os custos atuais.' : 'Estoque insuficiente para produzir agora.');
  ```

- **Linguagem (US11, CT43):** nunca "otimização", "solver", "Gurobi", "programação linear", "variável" ou "restrição". Use frases como "Esta é a quantidade que rende o maior lucro com o estoque que você tem hoje". A função `termosTecnicos(texto)` (do `apoio_telas_novas.js`, Parte 15) tem a lista de palavras proibidas. No teste de navegador, um jeito simples de conferir é ler o texto da tela e procurar cada palavra:

  ```js
  const texto = (await page.locator('#app').innerText()).toLowerCase();
  for (const p of ['otimiza', 'solver', 'gurobi', 'programação linear', 'variável', 'restrição', 'função objetivo', 'cobertura_minima']) assert(!texto.includes(p), 'termo técnico: ' + p);
  ```
- **Reserva B1** (Telas 12 e 13): se a otimização for desligada no servidor, a resposta é **`503`** `{"mensagem":"A recomendação está disponível só na demonstração local da equipe."}`. Mostre essa `mensagem` como **aviso informativo** (não como erro vermelho) e mantenha a tela utilizável. O `loading()` sempre mostra erros em vermelho; por isso, nestas telas, trate o `503` à parte (o `ApiError` do `api.js` traz o `status`):

  ```js
  try {
    const r = await request('/api/producao/recomendacao', {method: 'POST'});
    // ... desenhar o resultado
  } catch (e) {
    main.append(notice(e.message, e.status === 503 ? '' : 'error'));   // 503 = aviso (fundo lilás), o resto = erro
  }
  ```

  O Marketing (Tela 14) e o Dashboard (Tela 03) **não** recebem `503`: vêm com `200` e uma `mensagem` (Parte 15). No teste, simule o `503` com o `override` (como os testes de `401` e `403` já fazem).

**Números para conferir**, com o seed de demonstração: **151 / 1 / 0**, lucro **1.011,71**, receita **1.966,80**. Com o seed oficial (estoque 0): "Estoque insuficiente para produzir agora."

**Com o backend real na sua máquina,** a recomendação precisa do Python com o `gurobipy` e dos dados de demonstração:
1. **O Python:** o `optimization\venv` (tutorial do Kauê, Parte 1.4) e as três linhas do `PYTHON_CMD` no `backend\.env` (tutorial do Kauê, Parte 1.5). Reinicie o backend (9.3).
2. **O banco recém-criado** (9.2) e os dois usuários **pela tela** (passos 2 e 6 da Parte 10). O seed exige os dois.
3. **O seed**, no Terminal A, na `<PASTA_DO_PROJETO>`:

   ```powershell
   docker cp bd\seed_demo.sql mente-fria-db:/tmp/seed_demo.sql
   docker exec mente-fria-db psql -U postgres -d mente_fria -v ON_ERROR_STOP=1 -f /tmp/seed_demo.sql
   ```

   **Você deve ver** no fim uma tabela vazia `(0 rows)` (a conferência do próprio seed) e `COMMIT` (validado nos tutoriais do Lucas e do Kauê).

Sem isso, teste com a API simulada e, depois de seg 02/11, no Render.

**Testes mínimos:** "Calculando"; tabela; "Falta ingrediente" × "Sem lucro"; **nenhum termo técnico** no texto da tela; o `503` vira aviso, sem tela de erro (CT72); o Funcionário não abre (`producao` está em `SO_ADMIN`).

| Se der errado | O que fazer |
|---|---|
| `500` ao gerar, e no log do backend `Não foi possível iniciar o Python` ou `No module named 'gurobipy'` | O `PYTHON_CMD` do `.env` (passo 1 acima) |
| 0/0/0 com o seed oficial | É o certo: estoque 0. "Estoque insuficiente para produzir agora." |
| `403` | Você está logado como Funcionário |

**Perguntas para a Review:** por que a tela não fala "otimização"? O que o dono vê se o servidor desligar a otimização?

---

## Parte 15 — Dashboard (Tela 03), Reposição (Tela 13) e Marketing (Tela 14)

**Quando:** ter 03/11 (a Tela 13 começa na seg 02/11 à tarde, assim que a Fase 5 do Kauê entrar). Se apertar, o Lucas assume a 14.
**As três são só do Administrador** (já estão em `SO_ADMIN`).

### 15.1 As funções de apoio (já no `js/apoio_telas_novas.js`, Parte 8.2)

| Função | Para quê | Tela |
|---|---|---|
| `filtrosDashboard({mes, usuarioId, produtoIds, valorMin, valorMax})` | Monta a URL do `GET /api/dashboard` só com os filtros preenchidos (`produto_id` pode repetir) | 03 |
| `validarFiltrosDashboard({mes, valorMin, valorMax})` | Os erros por campo antes de chamar | 03 |
| `textoUltimoPlano(plano, otimizadorHabilitado)` | O texto do bloco "Plano de produção", sem termos técnicos | 03 |
| `validarReposicao(raw)` | Período (1 a 365) e orçamento (aceita vírgula, até 2 casas) → `{payload, issues}` | 13 |
| `resumoReposicao(resposta)` | A frase acima da tabela: cobre tudo, cobre parte, não cobre nem o mínimo, sem pedidos | 13 |
| `avisoMarketing(resposta)` | O que mostrar quando não há sugestão | 14 |
| `termosTecnicos(texto)` | A lista de palavras proibidas encontradas (US11, CT43) | 12, 13, 14 |

Elas não usam o DOM nem a rede: o `node ferramentas\testar_apoio_telas.mjs .` confere todas (15 verificações). Importe com `import {…} from '../apoio_telas_novas.js';`.

### 15.2 Dashboard (Tela 03) — `GET /api/dashboard` (contrato §2.8)

| Bloco | Campo da resposta | Como mostrar |
|---|---|---|
| Seletor de mês | `mes` | `<input type="month">`; padrão: o mês atual (o backend já assume) |
| Faturamento do mês | `faturamento.valor_total`, `quantidade_pedidos` | Destaque em R$ |
| Mais vendidos | `mais_vendidos[]` | Tabela: produto, quantidade, valor |
| Ingredientes mais utilizados | `ingredientes_mais_utilizados[]` | Tabela: ingrediente, quantidade **com a unidade**, custo (já vem ordenado pelo custo) |
| Ranking de vendas | `ranking[]` | Colunas **Rank, Usuário, Quantidade de pedidos, Valor total, Tipos de produto**. Filtros: Usuário (`GET /api/usuarios` e o próprio Administrador), Tipo de produto (`GET /api/produtos`, vários) e Valor mín./máx. |
| Plano de produção | `ultimo_plano` (pode ser `null`) e `otimizador_habilitado` | `textoUltimoPlano(…)`. Com `null`: link para `#/producao`. **Não cortar**: é a "receita máxima estimada" do Termo |
| Situação do estoque | `estoque.ingredientes_abaixo_do_minimo`, `ingredientes_com_vencido` | Dois números, com link para `#/estoque` |

**Para conferir** com o seed de demonstração, no mês atual: faturamento **R$ 326,40** (3 pedidos; validado no tutorial do Lucas, Parte 7.5); Premium 7, Especial 6, Tradicional 3; açaí primeiro nos ingredientes; ranking Func R$ 213,00 e Dono R$ 113,40 (conferidos em SQL no tutorial do Diogo).

**Ordem de corte no M4** (roteiro §5), se faltar tempo: os filtros do ranking na tela, depois o ranking por usuário. O bloco "Plano de produção" **não** sai.

### 15.3 Reposição (Tela 13) — `POST /api/reposicao/recomendacao` (contrato §2.7)

- Campos **Período (dias)** (sugestão: 30) e **Orçamento (R$)**; antes de enviar, `validarReposicao(…)`. Botão "Gerar sugestão de compra", com "Calculando…".
- Acima da tabela, `resumoReposicao(resposta)`.
- Tabela **na ordem recebida** (maior custo primeiro): ingrediente, consumo no período (com a unidade), estoque válido, mínimo, **quantidade sugerida**, custo estimado. Linha com sugerida 0: "Não precisa comprar".
- **Não mostre** `cobertura_minima`: é termo técnico.
- O `503` da reserva B1, como na Tela 12 (com o mesmo `try`/`catch`).

**Para conferir** com o seed de demonstração (60 dias, R$ 100): só o morango, **0,74 kg por R$ 11,10** (validado no tutorial do Kauê, dentro da imagem do deploy).

### 15.4 Marketing (Tela 14) — `GET /api/marketing/sugestoes` (contrato §2.9)

- Até **3 cartões**: o produto e o selo "Produto priorizado"; a ideia (título) e o "como fazer"; o lucro por copo (para o dono saber quanto de desconto cabe).
- Cabeçalho: "Sugestões com base no cálculo de …" (`calculado_em`).
- Sem sugestões: `avisoMarketing(resposta)` e um link para `#/producao`. Com a reserva B1 ligada, a resposta é `200`, sem sugestões, com a `mensagem` "As sugestões dependem da recomendação de produção, que neste endereço está disponível só na demonstração local da equipe.": a mesma `avisoMarketing` a mostra.
- **Nenhum botão de publicar** (critério da US18: o sistema só sugere).

**Para conferir:** depois de gerar 151/1/0 na Tela 12, aparecem **2** cartões (Tradicional e Especial). O Premium veio 0 e não é priorizado.

### 15.5 Testes mínimos

| Tela | Testes |
|---|---|
| Dashboard | Mês padrão; filtros viram a URL certa (`produto_id` repetido); bloco "Plano de produção" com `null`; Funcionário bloqueado |
| Reposição | Erros por campo; resumo nos 4 casos; ordem da tabela; nenhum termo técnico; `503` vira aviso |
| Marketing | Até 3 cartões; aviso sem cálculo com link; sem botão de publicar |

Para cada tela, acrescente antes as rotas simuladas no `tests\browser.cjs`, no formato do contrato (os exemplos JSON do contrato servem de resposta). **Nunca** desative um teste antigo para "passar". Cada tela é uma branch e um PR, uma por vez (Parte 2.4), com a evidência do `npm test`.

**Perguntas para a Review:** de onde vem o "último plano" que o Dashboard e o Marketing mostram, e por que ele some se o backend reiniciar? Por que a Tela 13 não mostra a "cobertura"?

---

## Parte 16 — Aceite na URL publicada (qua 04/11, depois das 18h)

**Quando:** depois do congelamento das 18h. O aceite roda sobre o que está na `main` nessa hora.
**Onde:** Chrome na URL do Pages, com **F12** → *Network* → **Preserve log**.

**Cuidado com os dados de demonstração:** o banco publicado é o mesmo da Review de sex 06/11 (151/1/0 na Tela 12). Toda entrada de estoque, produto ou pedido muda a recomendação, a reposição e o dashboard. Por isso, **primeiro o que só lê, depois o que grava**:

1. Abra a URL do Pages uns 2 minutos antes: o Render "acorda" (até 90 s na primeira chamada).
2. **Só leitura, como Administrador** (a senha vem do Kauê, por mensagem privada), nesta ordem: Tela 12 (**151/1/0**, sem termos técnicos) → Tela 14 (2 cartões) → Tela 13 (0,74 de morango, 60 dias, R$ 100) → Tela 03 (o mês atual) → Estoque e detalhe.
3. **Como Funcionário:** estoque e detalhe; as telas do Administrador bloqueadas.
4. **Por último, o que grava** (cadastro, entrada de estoque, pedido com baixa), com nomes que deixem claro que é teste (por exemplo, "Teste aceite"). **Antes, combine com o Kauê e o Diogo:** depois do aceite, eles restauram o backup ou refazem o seed (tutorial do Kauê, Parte 8.7), para a Review voltar ao 151/1/0.
5. Prints **só da tela**, com a URL visível e o DevTools **fechado** (em produção, o *Payload* do login mostra a senha real e a *Response*, o token), em `docs/Sprint_4/evidencias/` (por exemplo, `2026-11-04_leonardo_aceite-pages_tela12.png`). Entregue por PR.

É o mesmo fluxo do CT68 do Kauê: combinem para não fazerem ao mesmo tempo (o "último plano" é um só no backend).

| Se der errado | O que fazer |
|---|---|
| A primeira chamada demora e aparece "O servidor demorou para responder" | O `config.js` publicado ainda é o antigo (15 s): Parte 8.3 |
| `blocked by CORS policy` | Parte 8.4, item 2: avise o Kauê |
| A Tela 12 não dá 151/1/0 | Alguém gravou dados antes: avise o Kauê e o Diogo (backup ou seed de novo) |

---

## Parte 17 — Entrega (até qui 05/11)

1. **`frontend/README.md`:** reescreva a seção "Situação do backend deste checkout" (de 27/09) com o estado real: a estrutura de módulos, como rodar (`npm install`, o Terminal B, `npm test`), as ferramentas de retrato, o aceite real e a **URL do Pages**.
2. **Evidências** em `docs/Sprint_4/evidencias/`: o baseline (Parte 3), o `IGUAIS` (Parte 5), a guarda (Parte 6), a A13 (Parte 7), as 16 do *gate*, um `npm test` por tela e os prints do aceite.
3. **Contribuições individuais** das Sprints 3 e 4, no padrão de `docs/Sprint_2/sprint-2-contribuicao-leo.md`.

Documentos entram por PR, como o código (Parte 2), numa branch `docs/leonardo-...`.

---

## Problemas comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `ERR_CONNECTION_REFUSED` no `npm test` ou no Chrome | O Terminal B não está servindo os arquivos | Parte 1.3 |
| `AssertionError … 1 !== 0` na verificação 21 | A instabilidade A13 | Parte 7 |
| O `npm test` diz que não achou o Chrome | Chrome não instalado ou fora da pasta padrão | Parte 1.1 |
| Teste novo lança `Rota inesperada: /api/...` | A rota não está na API simulada do `tests\browser.cjs` | Parte 11.5, item 2 |
| Tela em branco depois de dividir ou de criar uma tela | Erro de `import`/`export` | **F12** → *Console*; `node --check` |
| `DIFERENÇAS em: …` depois da divisão | `flash`/`revision` sem `state.`, ou `render()` chamado de `ui`/`nav` | Parte 5.2 |
| `Cannot find package 'jsdom'` | Um `npm install` removeu o `jsdom` | `npm install jsdom --no-save` |
| O Funcionário volta ao início ao abrir uma tela dele | A rota não está no mapa `FUNCIONARIO` | Parte 6.1 e 11.4 |
| "Página não encontrada" numa tela nova (Administrador) | A rota não foi registrada no `router.js`, ou o `id` não é só de dígitos | Parte 11.4 |
| O Funcionário vê "Esta área é exclusiva do Administrador." numa tela nova | A rota não está no mapa `FUNCIONARIO` (ou não existe) | Partes 6.1 e 11.4 |
| `passaram: 0 de 10` com os testes passando | O laço procura o texto com acento | Use `"\d+ verifica"` (Parte 3.2) e o comando da seção "Acentos na tela" |
| Evidência `.txt` com `verificaÃ§Ãµes` | Faltou o `[Console]::OutputEncoding = [Text.Encoding]::UTF8` | Seção "Acentos na tela" |
| `blocked by CORS policy` local | Você abriu por `file://` ou outra porta | Use `http://127.0.0.1:5500` |
| A data aparece um dia antes | `new Date('AAAA-MM-DD')` sem a hora | Use o `data()` da Parte 11.3 |
| No Pages: "O servidor demorou para responder" | `config.js` antigo (15 s) e o Render dormindo | Parte 8.1 |
| No Pages: `blocked by CORS policy` | A origem no Render é diferente da do Pages | Só `https://kauencarneiro.github.io`, sem caminho (Parte 8.4) |
| No Pages: tela sem estilo ou módulo não encontrado (`404`) | Caminho absoluto (`/js/…`) | Caminhos relativos (Parte 8.4) |
| `15 verificações` não aparece | `apoio_telas_novas.js` ou `config.js` não copiados | Parte 8.2 |
| `Could not resolve placeholder` ao subir o backend | `backend\.env` faltando ou com BOM | Parte 9.3 |
| O Chrome recusa colar no *Console* | Proteção do Chrome | Digite `allow pasting` e cole de novo |

---

## Colinha

```powershell
# Toda janela nova
[Console]::OutputEncoding = [Text.Encoding]::UTF8

# Terminal B (fica rodando), dentro de frontend
py -m http.server 5500 --bind 127.0.0.1

# Terminal A, dentro de frontend
npm test
node --check js\router.js
node ferramentas\retratar_telas.mjs . retrato_x.json
node ferramentas\comparar_retratos.mjs retrato_antes.json retrato_x.json
node ferramentas\testar_guarda.mjs .
node ferramentas\testar_apoio_telas.mjs .

# 10 rodadas do npm test
$ok = 0; 1..10 | ForEach-Object { $s = npm test 2>&1 | Out-String; if ($s -match "\d+ verifica") { $ok++ } }; "passaram: $ok de 10"

# Terminal C (backend), dentro de backend
.\mvnw spring-boot:run

# Git: só os arquivos da etapa (nunca git add .)
git add frontend\js\screens\<tela>.js frontend\js\router.js frontend\js\screens\shell.js frontend\js\nav.js frontend\tests\browser.cjs
```

---

## Checklist final

- [ ] Ferramentas e Chrome; `npm install`; Terminal B servindo em `127.0.0.1:5500`
- [ ] Baseline: a contagem das 10 rodadas e `OK: 25 retratos`
- [ ] Divisão: `node --check` em todos; **`IGUAIS`**; PR
- [ ] Guarda por lista: a tabela da Parte 6; `IGUAIS`; PR
- [ ] A13: a correção do teste e as duas contagens (antes e depois); PR logo depois da divisão (A45)
- [ ] `config.js` e *workflow*: 15 verificações; PR; o Kauê habilitou o Pages; a URL do Render na seg 02/11
- [ ] *Gate* da Fase 2: os 16 passos e os prints, sem token
- [ ] Estoque (lista e detalhe), movimentação; "Descartar" se aprovado no M3
- [ ] Pedido; Produção (151/1/0, sem termos técnicos, `503` como aviso); Dashboard (326,40); Reposição (0,74); Marketing (2 cartões)
- [ ] Aceite na URL publicada, nos dois perfis (qua 04/11, depois das 18h)
- [ ] `frontend/README.md` com a URL; evidências; contribuições individuais das Sprints 3 e 4

---

## Fora do escopo obrigatório (decisões A2b e A40)

**Porção Padrão** (Tela 19, US26) só entra se passar no *gate* da seg 02/11; quem a faz é o **Diogo** (A43), e você **não** participa: as seis telas obrigatórias vêm primeiro. Ficam **fora do semestre**: clientes (Telas 18 e 18B, US13), alertas completos (US15/US16; a cobertura parcial é a do detalhe por linha do Estoque, adendo, item 4), CSV do dashboard (US19) e cancelamento de pedido (US27).
