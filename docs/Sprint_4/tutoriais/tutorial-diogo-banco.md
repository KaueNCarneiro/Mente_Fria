# Tutorial — Sprint 4 — Banco de dados, repositórios, seed e US26 — Diogo

**Para:** Diogo Pereira Miranda · **Papel:** banco de dados, repositórios Java (SQL), qualidade da documentação e, se passar no *gate*, a US26 · **Data:** 31/10/2026 (decisão A44)
**Substitui:** `docs/Sprint_3/sprint-3-tutorial-banco-diogo.md`, que fica como registro.
**Calendário em vigor:** `docs/Sprint_4/sprint-4-roteiro.md`, §4. Se uma data daqui divergir, vale o roteiro.

---

## O que foi verificado e o que não foi

Os comandos deste tutorial foram **executados de verdade** em 31/10/2026, num **clone temporário** do repositório: a `main` de 28/10 (`af05b26`) e a sua branch `feature/banco-sprint4` (`6c07b19`). O ambiente:
- Windows PowerShell 5.1;
- Docker 29.8.0, com um PostgreSQL 16 **temporário** (`mf-tutorial-valida`), usando **os mesmos comandos** deste tutorial, só com outro nome de container. O `mente-fria-db` não foi tocado;
- um "GitHub de mentira" (um repositório na própria máquina) para o fluxo a dois.

| O quê | Resultado real |
|---|---|
| A sua branch do banco | O `bd/Script_DDL.sql` e o `bd/constraints_sprint3_teste.sql` são **idênticos** aos da referência (ignorando o fim de linha) |
| Baseline: DDL da `main` + os 32 testes da Sprint 1 | `32 \| 0` |
| DDL da sua branch + os 41 testes novos | `41 \| 0` |
| DDL da sua branch + os 32 antigos | `32 \| 0` |
| A prova do F1 (`NULL IN (…)`) | `t` (desconhecido) |
| Experimento A (os CHECKs "ingênuos") | `37 \| 4`: T55, T62, T64, T66 |
| Experimento B (sem o `chk_movimentacao_motivo`) | `38 \| 3`: T58, T62, T63 |
| Seed de demonstração num banco à parte | `(0 rows)` e `COMMIT`; estoque, faturamento (271,50 e 326,40) e invariante conferidos; a segunda execução é **recusada** |
| O SQL da US26 | Unidade nova aceita; repetida → `unidade_medida_porcao_padrao_pkey`; porção 0 → `chk_unidade_porcao_padrao`; `UPDATE` de unidade inexistente → `UPDATE 0`; os ingredientes **não** mudam. Depois do teste à mão, os 41 testes dão `40 \| 1` (T72) até recriar o banco |
| Limpar um banco como dono que **não** é superusuário (como no Render) | `DROP SCHEMA public CASCADE` + `CREATE SCHEMA public` funcionam, e o DDL carrega de novo (Parte 9.3) |
| O seu lado do fluxo a dois (repositórios na branch do Lucas) | `fetch`, `checkout`, cópia, `commit`, `push`: as saídas da Parte 5 |

**Não foi possível verificar aqui:**
- o GitHub de verdade (o seu PR, as revisões e os merges);
- o banco do Render (o seed publicado é na seg 02/11);
- o código Java e a Tela 19 da US26: são seus. O tutorial dá o contrato, o SQL validado e o molde de cada arquivo.

> **Se alguma saída não bater:** pare e copie a mensagem **exata**. Não tente adivinhar.

---

## Como usar este tutorial

### Qual terminal usar

Todos os comandos são para o **Windows PowerShell**: Menu Iniciar → `PowerShell` → **Windows PowerShell** (ou, no IntelliJ ou no VS Code, o terminal do tipo *powershell*). A linha começa com **`PS`**, por exemplo `PS C:\Users\diogo\Mente_Fria>`. Se começar sem `PS` (CMD) ou com `MINGW64` (Git Bash), feche e abra o PowerShell.

### Onde rodar

Quase todo comando assume a **pasta raiz do repositório**, a que tem `backend`, `bd`, `docs`, `frontend` e `optimization`:

```powershell
cd "C:\Users\diogo\Mente_Fria"
dir
```

Neste tutorial, **`<PASTA_DO_PROJETO>`** é esse caminho.

### Acentos na tela: um comando por janela

```powershell
[Console]::OutputEncoding = [Text.Encoding]::UTF8
```

**Por quê:** o padrão do Windows em português é a "página de código 850". Sem este comando, o `psql` mostra `A├ºa├¡ (polpa)` no lugar de `Açaí (polpa)` (verificado nos tutoriais do Kauê e do Leonardo). O dado está certo; só a tela erra. Vale só para aquela janela.

### Dentro de qual banco cada comando roda

Este é o ponto que mais confunde, então todo comando de banco deste tutorial diz **qual banco** usa, no `-d`:

```
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "SELECT 1;"
              \_ o container \_ o programa  \_ o banco          \_ o comando SQL
```

| Banco (`-d`) | Para quê | Pode apagar e recriar? |
|---|---|---|
| `mente_fria` | O banco que o backend usa na sua máquina | Sim (o tutorial do Lucas, Parte 4.8, ensina), mas perde os seus cadastros de teste |
| `mente_fria_teste` | Os testes de banco (`constraints_*.sql`) e os experimentos | **Sim**: é descartável. Os experimentos de "quebrar de propósito" rodam **só** aqui |
| `mente_fria_demo` | Estudar o seed de demonstração | Sim |
| (sem `-d`) | Comandos que mexem nos bancos em si (`CREATE DATABASE`, `DROP DATABASE`). Sem `-d`, o `psql` se conecta ao banco de manutenção `postgres`, que existe em todo PostgreSQL | — |

> ⚠️ **Regra da equipe:** nunca apague, recrie ou resete o **container** `mente-fria-db`. Os bancos **dentro** dele (os da tabela) podem ser recriados.

### Formato de cada passo

**Objetivo**, **Onde**, **Faça** (copie bloco por bloco), **O que cada parte faz**, **Você deve ver** (a saída real), **Se der errado**, **Antes de avançar**. Quando uma saída é longa, o tutorial mostra só o trecho que importa, com `...` no lugar do resto.

### Antes de trocar de branch: `git status`

Todo bloco que troca de branch começa com `git status`. Se ele listar arquivos alterados, **pare**: faça o commit deles na branch certa, ou guarde-os por um tempo com `git stash` (e traga de volta depois com `git stash pop`, na mesma branch). **Hoje (31/10), a sua pasta tem as mudanças dos tutoriais sem commit, na branch `docs/sprint-4-tutoriais`:** faça o commit delas antes de seguir este tutorial.

---

## Plano de tempo e "pronto quando"

| Dia | O que você faz | Parte |
|---|---|---|
| **Qui 29/10** | ~~PR do banco~~ **feito** (aberto, esperando revisão); ~~mensagem ao professor~~ **enviada**. Repositórios da Fase 2, **quando o Lucas publicar a branch** | 3 e 5 |
| **Sex 30/10** (atrasado) | `bd/seed_demo.sql` (PR); `docs/Apoio/sql_repositorios_sprint3.md` (PR); repositórios da Fase 3, quando a branch existir | 5, 6 e 7 |
| **Sáb 31/10** (hoje) | Acompanhar o PR do banco até a `main`; **os PRs atrasados do seed e do SQL da Sprint 3** (o Lucas precisa do seed na Fase 4A, dom); revisar PRs; registrar a A26 nas evidências; o cabeçalho do `constraints_teste.sql`, depois do merge do banco | 3, 4, 6, 7 e 8 |
| **Dom 01/11** | Revisar PRs; conferir os casos do banco no Plano de Testes; o `ReposicaoRepository` na branch da Fase 5 do Kauê | 5 e 8 |
| **Seg 02/11** (feriado, dia de mais carga) | De manhã: o `DashboardRepository` na Fase 6 do Lucas; **se a US26 passar no *gate*:** a seção do contrato e os casos de teste. À tarde: o seed de demonstração no banco publicado, com o Kauê | 5, 9 e 10 |
| **Ter 03/11** | US26: o backend, com testes (PR, o Lucas revisa) | 10 |
| **Qua 04/11** | US26: a Tela 19 e as evidências, **na `main` até as 18h, ou sai** | 10 |
| **Qui 05/11** | Evidências; contribuições individuais das Sprints 3 e 4; conferir os `.docx` no Word | 11 |
| **Sex 06/11** | Entrega e Review | — |

**Pronto quando:**
- [ ] o PR do banco na `main`, com o aviso "recriar o banco" dado à equipe;
- [ ] os 8 repositórios entregues nas branches das fases (Lucas e Kauê);
- [ ] o `seed_demo.sql` e o `sql_repositorios_sprint3.md` na `main`;
- [ ] o seed de demonstração no banco publicado, conferido;
- [ ] a US26 na `main` até qua 04/11, 18h, **ou** registrada como cortada;
- [ ] você sabe responder as "Perguntas para a Review" do fim de cada parte.

---

## Parte 0 — Conceitos em 5 minutos

### 0.1 Glossário

| Termo | O que é (sem jargão) | No nosso projeto |
|---|---|---|
| **DDL** | Os comandos que **criam** a estrutura do banco (tabelas, regras) | `bd/Script_DDL.sql` |
| **Constraint** | Uma regra que o próprio banco garante, mesmo se a aplicação errar | `chk_movimentacao_motivo`, `fk_pedido_cliente` |
| **`CHECK`** | Uma constraint com uma condição. O registro só entra se a condição **não for falsa** | `CHECK (porcao_padrao > 0)` |
| **`NULL` e a lógica de três valores** | Em SQL, uma comparação com `NULL` não é verdadeira nem falsa: é **desconhecida**. E um `CHECK` desconhecido **deixa passar** | A prova do F1 (Parte 3.3) |
| **Seed** | Os dados iniciais que o DDL já insere | 7 ingredientes, 3 produtos, as unidades |
| **Linha de estoque** | Cada `ENTRADA` vira uma linha com `quantidade_restante`. Saídas e pedidos **consomem** linhas | `movimentacao_estoque` |
| **Invariante** | Uma regra que precisa valer **sempre** | `ingrediente.quantidade_estoque` = soma dos restantes das `ENTRADA` |
| **FEFO** | *First Expired, First Out*: sai primeiro o que vence primeiro | A baixa consome as linhas válidas da menor validade para a maior |
| **Transação** | "Tudo ou nada": se algo falha no meio, o banco desfaz tudo | O pedido baixa todos os ingredientes, ou nenhum |
| **`FOR UPDATE`** | "Esta linha é minha até eu terminar": outra transação espera | Dois pedidos ao mesmo tempo não vendem o mesmo açaí |
| **Repositório** (`@Repository`) | A classe Java que guarda **os SQLs**. Só fala com o banco, sem regra de negócio | `UsuarioRepository`, `MovimentacaoRepository` |
| **`NamedParameterJdbcTemplate`** | A ferramenta do Spring para rodar SQL com **parâmetros com nome** (`:email`), sem colar valores no texto | Todos os repositórios (decisão D4: sem JPA) |
| **Testcontainers** | Uma biblioteca que sobe um PostgreSQL de verdade no Docker só para os testes do backend | Os testes das Fases 2 a 6 usam o **seu** `bd/Script_DDL.sql` |
| **Branch / PR / merge** | **Branch:** uma "cópia de trabalho". **PR:** o pedido para juntar na `main`. **Merge:** a junção | O seu PR do banco; os seus commits nas branches do Lucas |

### 0.2 Como as peças se encaixam

```
 bd/Script_DDL.sql  ---------->  mente-fria-db (o banco de cada um)  <--- backend (Lucas) via os SEUS repositórios
        |                                                                   ^
        |  (os testes do backend recriam o schema a partir dele)          |
        v                                                                   |
 Testcontainers (testes do Lucas e do Kauê) --------------------------------+

 bd/constraints_teste.sql e constraints_sprint3_teste.sql  -> provam as regras do banco, no mente_fria_teste
 bd/seed_demo.sql  -> os dados da Review (151/1/0, 326,40, 0,74 de morango)
```

**A ideia que você vai defender na Review:** a regra importante fica **no banco** (constraints), e não só na aplicação. Assim, nem um bug do Java, nem um `INSERT` à mão, deixa o estoque inconsistente. E cada regra tem um teste que **prova** que ela barra o dado errado.

### 0.3 Os códigos que aparecem no projeto

| Código | O que significa | Onde está |
|---|---|---|
| **T***n* (ex.: T62) | Teste de banco | `bd/constraints_teste.sql` (T1–T32) e `bd/constraints_sprint3_teste.sql` (T33–T73) |
| **CT***n* | Caso de teste do Plano de Testes | `docs/Plano_de_Testes_Mente_Fria.md` |
| **US***n* | História de usuário | `docs/Backlog_Priorizado_Mente_Fria.docx` |
| **A***n*, **D***n*, **F***n* | Decisões registradas (A = auditoria; D e F = respostas de 13/10 e 14/10) | A1–A38 em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`; A39 em diante em `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md` |
| **F1** | A decisão dos `CHECK`s estritos (por causa do `NULL`) | Parte 3.3 |
| **D11** | O seed oficial com estoque 0 | Parte 3.2 |
| **A9** | `:hoje` e `:agora` vêm do Java, no fuso de São Paulo | Parte 7 |
| **A26** | O travamento do Docker Desktop com contorno conhecido | Parte 1.2 e 8.4 |
| **A44** | Os seus repositórios entram por commit na branch de cada fase | Parte 5 |
| **US26** | Porção padrão por unidade de medida (Tela 19). Condicional: só entra se passar no *gate* da seg 02/11 (A40) | Parte 10 |
| **M1–M4, *gate*** | Marcos do calendário; o "portão" de decisão | Roteiro da Sprint 4, §4 e §5 |

---

## Parte 1 — Preparar o computador

### 1.1 Conferir as ferramentas

**Onde:** qualquer pasta.

```powershell
git --version
docker --version
java -version
docker ps -a --filter name=mente-fria-db
```

**Você deve ver** as versões (Java **25**) e uma linha com `mente-fria-db`. Se a coluna STATUS disser `Exited`, ligue o container:

```powershell
docker start mente-fria-db
```

O Java é preciso para rodar os testes do backend nas branches do Lucas (Parte 5.4) e, se a US26 entrar, para o backend dela (Parte 10.5).

### 1.2 Se o Docker Desktop travar ao abrir (A26)

**Sintoma** (o mesmo de 26/10, e de novo em 30/10 nesta máquina): uma janela de erro com *"starting services: initializing Ingest server: listening on unix://C:/Users/diogo/AppData/Local/Docker/run/sailor-ingest.sock: rename … The file cannot be accessed by the system"* e os botões **Quit** e **Reset to factory defaults**.

1. **Nunca** clique em **"Reset to factory defaults"**: isso apaga o `mente-fria-db`. Clique em **Quit**.
2. No PowerShell: `wsl --shutdown`
3. Renomeie as duas pastas (apagar não funciona; renomear, sim):

   ```powershell
   Rename-Item "$env:LOCALAPPDATA\Docker\run" "run.antiga-$(Get-Date -Format yyyyMMdd-HHmm)"
   Rename-Item "$env:LOCALAPPDATA\docker-secrets-engine" "docker-secrets-engine.antiga-$(Get-Date -Format yyyyMMdd-HHmm)"
   ```

4. Abra o Docker Desktop de novo e espere "Engine running".

Em 30/10, o contorno resolveu de novo. A segunda ocorrência entra no registro da A26 (Parte 8.4).

**Antes de avançar:**
- [ ] `docker ps` mostra `mente-fria-db` com `Up`;
- [ ] a janela está com o `[Console]::OutputEncoding` em UTF-8.

---

## Parte 2 — Git e PR: o caminho de toda entrega

Você vai entregar de dois jeitos:
- **PR seu** (banco, seed, documentos, US26): o caminho abaixo;
- **commit na branch de outra pessoa** (os repositórios, A44): a Parte 5.

### 2.1 Branch nova a partir da `main` atualizada

```powershell
git status
git checkout main
git pull
git checkout -b docs/sql-repositorios-sprint3
```

| Comando | O que faz |
|---|---|
| `git status` | Antes de trocar de branch, o certo é **nada** pendente |
| `git checkout main` + `git pull` | Vai para a `main` e traz o que os colegas já juntaram |
| `git checkout -b <nome>` | Cria a branch nova e entra nela |

**Os nomes das suas branches:**

| Entrega | Branch |
|---|---|
| PR do banco (já existe) | `feature/banco-sprint4` |
| Seed de demonstração | `feature/seed-demo` |
| SQL da Sprint 3 documentado | `docs/sql-repositorios-sprint3` |
| Registro da A26 e outras evidências | `docs/diogo-evidencias` |
| US26: contrato e casos de teste | `docs/us26-contrato` |
| US26: backend | `feature/us26-backend` |
| US26: Tela 19 | `feature/us26-tela19` |

### 2.2 Commit, push, PR, merge

```powershell
git add <só os arquivos da entrega>
git status --short
git commit -m "docs: sql das consultas da Sprint 3"
git push -u origin <branch>
```

- **Mensagem:** *Conventional Commits* (`feat:`, `fix:`, `docs:`, `test:`), **sem acentos**.
- **PR:** `https://github.com/KaueNCarneiro/Mente_Fria` → **Compare & pull request** → base **`main`** ← a sua branch → um revisor → **Create pull request**.
- **Depois da aprovação:** **Merge pull request** → **Confirm merge** → **Delete branch**; no PowerShell, `git checkout main`, `git pull` e `git branch -d <branch>`.

### 2.3 Guardar evidência

As saídas de teste vão em `docs/Sprint_4/evidencias/`, com o nome `AAAA-MM-DD_diogo_<assunto>.txt`, **no mesmo PR** da entrega. Para gravar a saída de um comando:

```powershell
docker cp bd\constraints_sprint3_teste.sql mente-fria-db:/tmp/constraints_sprint3_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql | Out-File -Encoding utf8 docs\Sprint_4\evidencias\2026-10-31_diogo_banco_41-testes.txt
```

Para a saída de um teste do backend (`mvnw`), o padrão é o do tutorial do Lucas, Parte 2.8: `$saida = .\mvnw test "-Dtest=<CLASSES>"` e `$saida | Out-File -Encoding utf8 …`.

> Não use `Tee-Object`: no Windows PowerShell 5.1 ele grava em UTF-16, e o Git trata o arquivo como binário.

---

## Parte 3 — O PR do banco (feito; acompanhar até a `main`)

**Situação (31/10):** a branch `feature/banco-sprint4` (`6c07b19`) está no GitHub, com o PR **aberto, esperando revisão**. Ela tem três arquivos: o DDL novo, os 41 testes e a evidência dos experimentos (`2026-10-29_diogo_banco_quebrar-de-proposito.txt`). Validado: o DDL e os testes são idênticos aos da referência.

**Por que ele é o primeiro de todos:** os testes do backend (Lucas e Kauê) recriam o schema a partir do `bd/Script_DDL.sql`. Sem o seu PR na `main`, a Fase 3 (estoque por linhas) não tem as colunas que precisa.

### 3.1 O que o PR muda (para você explicar na revisão e na Review)

| Mudança | Tabela | Por quê |
|---|---|---|
| `quantidade_restante DECIMAL(10,3) NULL` + `chk_movimentacao_restante` | `movimentacao_estoque` | `ENTRADA`: obrigatório e entre 0 e `quantidade`. Outros tipos: vazio |
| `motivo VARCHAR(20) NULL` + `chk_movimentacao_motivo` | `movimentacao_estoque` | `SAIDA`: obrigatório (`PEDIDO`, `CONVENCIONAL`, `DESCARTE_VENCIDO`). Outros tipos: vazio |
| `ix_movimentacao_linhas` (índice parcial) | `movimentacao_estoque` | Acelera a busca das linhas com saldo |
| `status`, `data_cancelamento`, `usuario_cancelamento_id` + constraints | `pedido` | US27 (fora do semestre), já no schema para não recriar o banco depois |
| Seed: `quantidade_estoque = 0.000` | `ingrediente` | D11 (abaixo) |

### 3.2 Por que o seed oficial começa com estoque 0 (D11)

O seed antigo tinha 40 kg de açaí e **nenhuma** entrada. Com o estoque por linhas, isso viola a invariante: o saldo (40) seria diferente da soma das linhas (0). E não dá para criar uma entrada no seed, porque toda entrada precisa de um `usuario_id`, e o seed oficial não tem usuários (eles nascem pela tela).

### 3.3 A prova do F1 (3 minutos)

**Onde:** banco `mente_fria_teste` (descartável). Primeiro, recrie-o com o DDL da sua branch:

```powershell
git status
git checkout feature/banco-sprint4
git pull
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste WITH (FORCE);" -c "CREATE DATABASE mente_fria_teste;"
docker cp bd\Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "SELECT (NULL::text IN ('PEDIDO','CONVENCIONAL')) IS NULL AS resultado_e_desconhecido;"
```

Depois do merge do PR do banco, troque `feature/banco-sprint4` por `main` (o DDL novo passa a estar lá).

| Comando | O que faz |
|---|---|
| O primeiro `docker exec … -c … -c …` | Sem `-d`: apaga e cria o banco `mente_fria_teste` **dentro** do container. O `WITH (FORCE)` derruba conexões que ainda estejam abertas nele |
| `docker cp bd\Script_DDL.sql mente-fria-db:/tmp/…` | Copia o arquivo da sua pasta para **dentro** do container, onde o `psql` enxerga |
| `psql … -d mente_fria_teste -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql` | Roda o DDL **no** `mente_fria_teste`. O `-q` deixa quieto; o `ON_ERROR_STOP=1` para no primeiro erro (sem ele, um DDL pela metade passaria em silêncio) |
| O último | Pergunta ao PostgreSQL: "`NULL` está na lista?" |

**Você deve ver** (validado; na primeira vez, aparece antes `NOTICE:  database "mente_fria_teste" does not exist, skipping`, que é normal):

```
DROP DATABASE
CREATE DATABASE
 resultado_e_desconhecido
--------------------------
 t
(1 row)
```

| Se der errado | O que fazer |
|---|---|
| `database "mente_fria_teste" is being accessed by other users` | Faltou o `WITH (FORCE)` (um IntelliJ ou DBeaver aberto nele) |
| `relation "…" already exists` | O DDL rodou duas vezes: recrie o banco |
| `could not open file "/tmp/Script_DDL.sql"` | Faltou o `docker cp` |

**O que isso prova:** `NULL IN ('PEDIDO', …)` não é verdadeiro nem falso, é **desconhecido**. E um `CHECK` só barra quando a condição é **falsa**. Então um `CHECK (motivo IN (…))` deixaria passar uma saída **sem** motivo. Por isso o DDL usa `motivo IS NOT NULL AND motivo IN (…)`.

### 3.4 Os testes (para mostrar ao revisor, ou repetir depois de um ajuste)

**Onde:** banco `mente_fria_teste`, com o DDL da sua branch (o bloco da 3.3).

```powershell
docker cp bd\constraints_sprint3_teste.sql mente-fria-db:/tmp/constraints_sprint3_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql
docker cp bd\constraints_teste.sql mente-fria-db:/tmp/constraints_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql
```

**Você deve ver** (validado) no fim do primeiro e do segundo:

```
 passaram | falharam
----------+----------
       41 |        0
...
 passaram | falharam
----------+----------
       32 |        0
```

Cada script roda dentro de uma transação que é **desfeita** no fim (`ROLLBACK`): nenhum dado de teste fica no banco. E o código de saída é diferente de 0 quando algum teste falha (`$LASTEXITCODE`).

| Grupo | Testes | O que prova |
|---|---|---|
| Cliente | T33–T36 | Nome, contato e endereço obrigatórios |
| Pedido | T37–T46 | Cliente opcional; FKs; total ≥ 0; status válido; cancelamento completo ou nenhum |
| Itens | T47–T52 | Quantidade e preço > 0; produto repetido; FKs |
| Entrada (linha) | T53–T58 | Com ou sem validade; `restante` obrigatório, ≤ quantidade, ≥ 0; sem motivo |
| Saída | T59–T65 | Os 3 motivos; sem motivo e motivo inválido barrados; restante e validade proibidos |
| Gerais | T66–T73 | Tipo inválido; quantidade 0 (CT26); FKs; baixa além do saldo (CT21); unidade (US26) |

### 3.5 Quebrar de propósito (já feito em 29/10; aqui, como repetir)

**Por quê:** um teste que nunca falha não prova nada. Os experimentos trocam as constraints por versões "ingênuas" e mostram que os testes pegam o erro. **A sua evidência de 29/10 já está no PR.**

**Onde:** **sempre** no `mente_fria_teste`. Os `ALTER TABLE` mudam a estrutura do banco em que rodam: **nunca** rode no `mente_fria`.

**Experimento A** (os CHECKs literais do roteiro antigo), depois do bloco da 3.3:

```powershell
docker cp bd\constraints_sprint3_teste.sql mente-fria-db:/tmp/constraints_sprint3_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "ALTER TABLE movimentacao_estoque DROP CONSTRAINT chk_movimentacao_restante, DROP CONSTRAINT chk_movimentacao_motivo;"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "ALTER TABLE movimentacao_estoque ADD CONSTRAINT chk_movimentacao_restante CHECK (quantidade_restante IS NULL OR quantidade_restante BETWEEN 0 AND quantidade);"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "ALTER TABLE movimentacao_estoque ADD CONSTRAINT chk_movimentacao_motivo CHECK ((tipo = 'ENTRADA' AND motivo IS NULL) OR (tipo = 'SAIDA' AND motivo IN ('PEDIDO','CONVENCIONAL','DESCARTE_VENCIDO')));"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql
```

O SQL vai entre **aspas duplas** depois do `-c`; os textos do SQL ficam entre aspas **simples**. Cada linha mostra `ALTER TABLE` quando dá certo.

**Você deve ver** (validado; trecho), entre as linhas da tabela de resultados:

```
 T55   | FALHOU    | ENTRADA sem quantidade_restante                       | foi ACEITO, mas deveria ser rejeitado
 T62   | FALHOU    | SAIDA sem motivo                                      | foi ACEITO, mas deveria ser rejeitado
 T64   | FALHOU    | SAIDA com quantidade_restante preenchida              | foi ACEITO, mas deveria ser rejeitado
 T66   | FALHOU    | Tipo de movimentação inválido                         | rejeitado por: chk_movimentacao_motivo
...
       37 |        4
```

No fim aparece também `psql:/tmp/constraints_sprint3_teste.sql:134: ERROR:  Há testes de banco falhando (veja a tabela acima).`, e o `$LASTEXITCODE` fica diferente de 0. **Neste experimento, isso é o esperado:** é o próprio script avisando que pegou as falhas.

O T62 é o F1 na prática: **uma saída sem motivo entra**.

**Experimento B:** recrie o banco e rode:

```powershell
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste WITH (FORCE);" -c "CREATE DATABASE mente_fria_teste;"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "ALTER TABLE movimentacao_estoque DROP CONSTRAINT chk_movimentacao_motivo;"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql
```

**Você deve ver** (validado): T58, T62 e T63 com `FALHOU`, `38 | 3` e o mesmo `ERROR: Há testes de banco falhando`, esperado.

**Depois de cada experimento, recrie o `mente_fria_teste`.** Ele ficou com as constraints erradas.

### 3.6 Responder à revisão e fazer o merge

- **Se o revisor pedir mudança:** faça na mesma branch (`feature/banco-sprint4`), `git add`, `git commit`, `git push`. O PR se atualiza sozinho.
- **Depois do merge, avise no grupo** (é o que mais importa deste PR):

```
PR do banco na main (#__). ATENÇÃO, todos: recriem o banco mente_fria.
O estoque agora é por linhas e o seed oficial começa com estoque 0.
Comandos: tutorial do Lucas, Parte 4.8 (ou o "Recomeçar do zero" do README).
```

### 3.7 Perguntas para a Review

1. Por que `motivo IS NOT NULL AND motivo IN (…)`, e não só `motivo IN (…)`?
2. Por que o seed oficial tem estoque 0?
3. O que prova que os testes de banco testam alguma coisa?

---

## Parte 4 — O cabeçalho do `constraints_teste.sql` (depois do merge do banco)

**Por quê:** o cabeçalho do `bd/constraints_teste.sql` ainda cita o nome antigo `constraints_sprint1_teste.sql` (item 8 da revisão de documentos de 27/10). É arquivo de código, então ficou com você.

**Por que num PR pequeno, depois do merge, e não no PR do banco:** o PR do banco é o que segura a Fase 2 do Lucas. Um commit novo nele pode pedir revisão de novo, e mudaria o SHA-256 do `constraints_teste.sql` registrado na sua evidência de 29/10.

**Onde:** a `<PASTA_DO_PROJETO>`, depois do merge do PR do banco.

```powershell
git status
git checkout main
git pull
git checkout -b fix/cabecalho-constraints-teste
notepad bd\constraints_teste.sql
```

No Bloco de Notas, troque **só os comentários** do começo:
- `-- constraints_sprint1_teste.sql — Mente Fria` → `-- constraints_teste.sql — Mente Fria (bd/constraints_teste.sql)`;
- `--   psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f constraints_sprint1_teste.sql` → o mesmo, com `constraints_teste.sql`.

Salve (**UTF-8**, sem BOM) e confira que só os comentários mudaram:

```powershell
git diff bd\constraints_teste.sql
```

Rode o bloco da 3.4 de novo (`32 | 0`), e depois:

```powershell
git add bd\constraints_teste.sql
git commit -m "docs(banco): corrige o nome do arquivo no cabecalho do constraints_teste.sql"
git push -u origin fix/cabecalho-constraints-teste
```

Abra o PR (Parte 2.2), com o Lucas como revisor.

> **A sua evidência de 29/10** diz `Branch: feature/banco-sprint3   Commit: af05b26` no cabeçalho: era o nome da branch antes do push. Se quiser, corrija para `feature/banco-sprint4` no mesmo PR pequeno.

---

## Parte 5 — Os repositórios: commit na branch de cada fase (A44)

**Por que não num PR seu:** os repositórios só compilam junto com as classes da mesma fase (os `record`s, os serviços). Por isso você faz commit **na branch da fase**, e o PR da fase leva o trabalho dos dois.

**Acordo (F7):** você é o dono dos `*Repository.java`; o Lucas (e o Kauê, na Fase 5), do resto. Os repositórios estão **escritos, compilados e testados** em `docs/Sprint_3/referencia/backend/src/main/java/com/mentefria/backend/`. O seu trabalho: copiar, **ler**, conferir o SQL com o `docs/Sprint_3/referencia/bd/consultas_sprint3.sql` e entregar.

### 5.1 Quais e quando

| Fase | Branch (de quem) | Seus arquivos | Quando |
|---|---|---|---|
| 2 | `feature/backend-fase2-cadastros` (Lucas) | `usuario/UsuarioRepository`, `unidade/UnidadeMedidaRepository`, `ingrediente/IngredienteRepository`, `produto/ProdutoRepository` | Quando o Lucas avisar que a branch está no GitHub |
| 3 | `feature/backend-fase3-estoque-pedido` (Lucas) | `estoque/MovimentacaoRepository`, `pedido/PedidoRepository` | Idem |
| 5 | `feature/backend-reposicao` (**Kauê**) | `reposicao/ReposicaoRepository` | Sáb 31 ou dom 01/11, quando o Kauê avisar |
| 6 | `feature/backend-fase6-dashboard-marketing` (Lucas) | `dashboard/DashboardRepository` | Seg 02/11, de manhã |

**Situação em 31/10:** nenhuma dessas branches estava no GitHub ainda. Combine com o Lucas e o Kauê no grupo.

### 5.2 O passo a passo (exemplo: Fase 2)

**Onde:** a `<PASTA_DO_PROJETO>`, **sem** trabalho pendente.

```powershell
git status
git fetch
git checkout feature/backend-fase2-cadastros
$r = "docs\Sprint_3\referencia\backend\src\main\java\com\mentefria\backend"
$d = "backend\src\main\java\com\mentefria\backend"
Copy-Item "$r\usuario\UsuarioRepository.java" "$d\usuario\"
Copy-Item "$r\unidade\UnidadeMedidaRepository.java" "$d\unidade\"
Copy-Item "$r\ingrediente\IngredienteRepository.java" "$d\ingrediente\"
Copy-Item "$r\produto\ProdutoRepository.java" "$d\produto\"
git add backend
git status --short
git commit -m "feat: repositorios da Fase 2 (parte do Diogo)"
git push
```

| Comando | O que faz |
|---|---|
| `git status` | Precisa dizer `nothing to commit, working tree clean`. Se não, pare (seção "Antes de trocar de branch") |
| `git fetch` | Baixa as branches novas do GitHub, sem mexer nos seus arquivos |
| `git checkout feature/backend-fase2-cadastros` | Entra na branch do Lucas. Na primeira vez, o Git cria a sua cópia local ligada à do GitHub |
| `$r` e `$d` | A pasta de origem (referência) e a de destino (o backend de verdade) |
| `Copy-Item … "$d\usuario\"` | Copia um arquivo para a pasta da classe. As pastas já existem, porque o Lucas pôs as classes dele lá |
| `git add backend` + `git commit` + `git push` | Grava e envia o seu commit **na branch dele** |

**Você deve ver** (validado com um "GitHub de mentira"):

```
Switched to a new branch 'feature/backend-fase2-cadastros'
branch 'feature/backend-fase2-cadastros' set up to track 'origin/feature/backend-fase2-cadastros'.
M  backend/src/main/java/com/mentefria/backend/ingrediente/IngredienteRepository.java
A  backend/src/main/java/com/mentefria/backend/produto/ProdutoRepository.java
A  backend/src/main/java/com/mentefria/backend/unidade/UnidadeMedidaRepository.java
A  backend/src/main/java/com/mentefria/backend/usuario/UsuarioRepository.java
[feature/backend-fase2-cadastros 8ec935d] feat: repositorios da Fase 2 (parte do Diogo)
 4 files changed, 247 insertions(+), 12 deletions(-)
...
   9a83f01..8ec935d  feature/backend-fase2-cadastros -> feature/backend-fase2-cadastros
```

O `IngredienteRepository` aparece como `M` (modificado), e não `A`: a `main` já tem a versão da Sprint 1, e a sua a substitui. Os outros três são novos.

**Avise o Lucas:** "Repositórios da Fase 2 na sua branch. Pode dar `git pull`."

**As outras fases:** o mesmo bloco, trocando a branch, a mensagem e os `Copy-Item`:

| Fase | Linhas de `Copy-Item` |
|---|---|
| 3 | `Copy-Item "$r\estoque\MovimentacaoRepository.java" "$d\estoque\"` e `Copy-Item "$r\pedido\PedidoRepository.java" "$d\pedido\"` |
| 5 | `Copy-Item "$r\reposicao\ReposicaoRepository.java" "$d\reposicao\"` |
| 6 | `Copy-Item "$r\dashboard\DashboardRepository.java" "$d\dashboard\"` |

| Se der errado | O que fazer |
|---|---|
| `error: pathspec 'feature/…' did not match` | A branch ainda não está no GitHub, ou faltou o `git fetch` |
| `Copy-Item : Não foi possível localizar uma parte do caminho` | A pasta de destino não existe: a parte do colega ainda não foi enviada, ou você não está na branch dele (`git branch --show-current`) |
| `! [rejected] … (fetch first)` no `push` | O colega enviou algo depois do seu `checkout`. Rode `git pull` e depois `git push` |
| `CONFLICT` | Vocês dois mexeram no mesmo arquivo. Pare e chame o colega |

**Depois do push, volte para a sua branch:** `git checkout main` (ou a da sua entrega).

### 5.3 Um repositório por dentro

O `UsuarioRepository`, o primeiro que você entrega:

```java
@Repository
public class UsuarioRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    ...
    public Optional<Usuario> buscarPorEmail(String email) {          // nunca queryForObject: lança exceção se não achar
        return jdbc.query("SELECT id, nome, email, senha_hash, perfil, ativo FROM usuario WHERE email = :email",
                Map.of("email", email), USUARIO).stream().findFirst();
    }
```

| Trecho | Por quê |
|---|---|
| `@Repository` | O Spring cria a classe e a entrega a quem pedir no construtor (o `UsuarioService` do Lucas) |
| `NamedParameterJdbcTemplate` no construtor | A ferramenta do SQL, entregue pelo Spring (decisão D4: sem JPA) |
| `:email` + `Map.of("email", email)` | O valor vai **separado** do texto do SQL. Nunca se cola o que o usuário digitou dentro do SQL: é isso que impede a injeção de SQL |
| `Optional` + `.stream().findFirst()` | Quando pode não haver linha (e-mail inexistente), o método devolve "talvez", e o serviço decide o que fazer. O `queryForObject` lançaria uma exceção |
| `USUARIO` | O `RowMapper`: transforma cada linha do resultado num `record Usuario` |
| Sem regra de negócio | "E-mail repetido dá `409`" é do serviço e do `GlobalExceptionHandler`; o repositório só fala com o banco |

**Três padrões para conferir nos outros:**
- **`FOR UPDATE`:** primeiro os ingredientes são travados **em ordem de `id`** (`IngredienteRepository.travar`: `… ORDER BY id FOR UPDATE`); depois, as linhas de cada um, na ordem FEFO (`MovimentacaoRepository`). Assim, dois pedidos com os mesmos ingredientes nunca consomem a mesma linha nem se bloqueiam em ciclo;
- **`:hoje` e `:agora`** vêm do Java (decisão A9): o Render roda em UTC, e o Brasil está 3 h atrás. Por isso o SQL não usa `CURRENT_DATE`;
- **filtros opcionais com nulos tipados** (no `DashboardRepository`): `CAST(:usuario AS INTEGER) IS NULL OR …`, porque o PostgreSQL precisa saber o tipo de um `NULL`.

### 5.4 Rodar os testes na branch do Lucas (opcional, mas bom)

Depois do seu push e do `git pull` do Lucas, você pode rodar os testes da fase na sua máquina, **dentro da branch dele**, com o Docker aberto:

```powershell
cd backend
.\mvnw test "-Dtest=SegurancaEErrosTest,RegraAcaiTest,Fase2CrudTest"
cd ..
```

**Esperado** (validado no tutorial do Lucas, Parte 4): `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`. Os testes recriam o schema a partir do **seu** `bd/Script_DDL.sql`. Por isso a branch da fase precisa ter o seu PR do banco (a `main` depois do merge).

### 5.5 Perguntas para a Review

1. Por que o repositório não tem regra de negócio?
2. Como o `:email` impede a injeção de SQL?
3. Por que os ingredientes são travados sempre em ordem de `id`?

---

## Parte 6 — O seed de demonstração (`bd/seed_demo.sql`)

**Objetivo:** os dados que a Review vai mostrar (151/1/0 na produção, R$ 326,40 no dashboard, 0,74 kg de morango na reposição).
**Quando:** sex 30/10 (atrasado). O Lucas usa este arquivo na Fase 4A (dom), e o Kauê, no Render (seg).

### 6.1 Estudar num banco à parte

**Onde:** banco `mente_fria_demo` (à parte; **nunca** no `mente_fria`), com o DDL **novo**: o da sua branch, ou o da `main` depois do merge. Com o DDL da Sprint 1, o seed falha (`column "quantidade_restante" … does not exist`).

```powershell
git status
git checkout feature/banco-sprint4
git pull
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_demo WITH (FORCE);" -c "CREATE DATABASE mente_fria_demo;"
docker cp bd\Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -c "INSERT INTO usuario (nome,email,senha_hash,perfil) VALUES ('Dono','dono@exemplo.com','hash_falso','ADMINISTRADOR'),('Func','func@exemplo.com','hash_falso','FUNCIONARIO');"
docker cp docs\Sprint_3\referencia\bd\seed_demo.sql mente-fria-db:/tmp/seed_demo.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -v ON_ERROR_STOP=1 -f /tmp/seed_demo.sql
```

| Linha | O que faz |
|---|---|
| As três primeiras | Um banco novo com o DDL (estoque 0) |
| O `INSERT INTO usuario` | Os dois usuários que o seed exige. O `hash_falso` serve **só** para estudo: não permite login. No uso real, os usuários nascem pela tela |
| As duas últimas | Copiam e rodam o seed |

**Você deve ver** (validado) no fim:

```
BEGIN
CREATE FUNCTION
DO
 id | nome | saldo | soma_linhas
----+------+-------+-------------
(0 rows)

COMMIT
```

A tabela vazia é a conferência do próprio seed: nenhum ingrediente com o saldo diferente da soma das linhas.

### 6.2 Conferir os números

```powershell
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -c "SELECT i.nome, i.quantidade_estoque AS total, COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade IS NULL OR m.data_validade >= CURRENT_DATE), 0) AS valido, COALESCE(SUM(m.quantidade_restante) FILTER (WHERE m.data_validade < CURRENT_DATE), 0) AS vencido FROM ingrediente i LEFT JOIN movimentacao_estoque m ON m.ingrediente_id = i.id AND m.tipo = 'ENTRADA' GROUP BY i.id, i.nome, i.quantidade_estoque ORDER BY i.id;"
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -c "SELECT to_char(date_trunc('month', data_pedido), 'YYYY-MM') AS mes, count(*) AS pedidos, sum(valor_total) AS faturamento FROM pedido WHERE status = 'ATIVO' GROUP BY 1 ORDER BY 1;"
```

**Você deve ver** (validado em 31/10; os meses mudam com a data):

```
          nome          |  total  | valido  | vencido
------------------------+---------+---------+---------
 Açaí (polpa)           |  45.750 |  45.750 |       0
 Leite condensado       |  14.220 |  14.220 |       0
 Granola                |  11.160 |   5.160 |   6.000
 Banana                 |  11.050 |  11.050 |       0
 Morango                |   1.980 |   1.980 |       0
 Leite em pó            |   4.760 |   4.760 |       0
 Copo descartável 500ml | 269.000 | 269.000 |       0
(7 rows)

   mes   | pedidos | faturamento
---------+---------+-------------
 2026-09 |       3 |      271.50
 2026-10 |       3 |      326.40
(2 rows)
```

- **A granola** tem 6 kg vencidos: é ela que faz a recomendação dar **151/1/0** (o saldo **válido**, 5,160, limita o Especial) e que aparece "Vencida" no detalhe do Estoque.
- **O morango** (1,980) fica abaixo do mínimo (2,000): é o único da reposição (0,74 kg, R$ 11,10, com 60 dias e R$ 100).
- **O mês atual** soma **326,40** em 3 pedidos: é o faturamento do dashboard.

**O seed recusa rodar duas vezes** (validado): rodando de novo no mesmo banco, ele para com `ERROR:  O banco já tem movimentações ou pedidos: use um banco recém-criado.` Ele também recusa sem Administrador ou sem Funcionário.

> **Cuidado com a data:** o seed usa o relógio **do banco**, que está em **UTC** (no container e no Render): 3 horas à frente de São Paulo. Ele põe os pedidos do mês atual nas primeiras horas do dia 1, e as validades são relativas ao dia em que roda. O risco é o **último dia do mês, das 21h à meia-noite** (horário de São Paulo): para o banco, já é o mês seguinte. Fora disso, não há problema.

### 6.3 O PR

```powershell
git status
git checkout main
git pull
git checkout -b feature/seed-demo
Copy-Item docs\Sprint_3\referencia\bd\seed_demo.sql bd\seed_demo.sql
git add bd\seed_demo.sql
git commit -m "feat(banco): seed de demonstracao para a Review"
git push -u origin feature/seed-demo
```

Antes do commit, guarde a evidência (as duas consultas da 6.2):

```powershell
docker exec mente-fria-db psql -U postgres -d mente_fria_demo -c "SELECT to_char(date_trunc('month', data_pedido), 'YYYY-MM') AS mes, count(*) AS pedidos, sum(valor_total) AS faturamento FROM pedido WHERE status = 'ATIVO' GROUP BY 1 ORDER BY 1;" | Out-File -Encoding utf8 docs\Sprint_4\evidencias\2026-10-31_diogo_seed-demo.txt
git add docs\Sprint_4\evidencias
git commit -m "test: evidencia do seed de demonstracao"
git push
```

No PR: a saída da 6.1 e da 6.2, e o aviso "rodar **só** num banco recém-criado, depois de criar o Administrador e o Funcionário pela tela". **Revisor:** o Lucas (ele usa na Fase 4A, no domingo: por isso este PR é urgente).

| Se der errado | O que fazer |
|---|---|
| `column "quantidade_restante" … does not exist` | O `mente_fria_demo` foi criado com o DDL da Sprint 1: troque de branch e recrie |
| `O seed exige um Administrador e um Funcionário` (ou parecido) | Faltou o `INSERT INTO usuario` da 6.1 |
| `O banco já tem movimentações ou pedidos` | O seed já rodou nesse banco: recrie o `mente_fria_demo` |

---

## Parte 7 — O SQL da Sprint 3 documentado (`docs/Apoio/sql_repositorios_sprint3.md`)

**Objetivo:** o documento das consultas novas, no mesmo formato do `docs/Apoio/sql_repositorios.md` (o da Sprint 1).
**Fonte:** `docs/Sprint_3/referencia/bd/consultas_sprint3.sql` (110 linhas, executado em 26/10) e os repositórios da Parte 5, que usam essas consultas.

**Como montar** (branch `docs/sql-repositorios-sprint3`):
1. Abra o `sql_repositorios.md` e copie a estrutura: as convenções; a tabela rota ↔ método; cada método com o SQL, o retorno e as falhas.
2. Uma seção por consulta:

| # | Consulta | Usada por |
|---|---|---|
| 1 | `GET /api/estoque`: total, válido, vencido, mínimo, validade mais próxima | Telas 10/16 |
| 2 | Linhas de um ingrediente, com a `situacao` | `GET /api/estoque/{id}/linhas` |
| 3 | Entrada do Python: ingredientes dos produtos ativos, saldo **válido** e custo | Fase 4A |
| 4 e 5 | Linhas para baixar (válidas) e para descartar (vencidas), com `FOR UPDATE` | Pedido, saída, descarte |
| 6 | `UPDATE` da linha e do saldo + `INSERT` da `SAIDA` (com `:agora`), na mesma transação | Idem |
| 7 | `INSERT` da `ENTRADA` (com `:agora`) + soma ao saldo | Entrada |
| 8 | Invariante (**0 linhas** = tudo certo) | Testes |
| novo | `GET /api/pedidos`: pedidos com itens, do mais recente para o mais antigo | CT40 |
| 9 | US14: os 8 (até 30 dias) ou 15 (acima) ingredientes mais consumidos no período. **Pedidos cancelados contam** (A18) | `POST /api/reposicao/recomendacao` |
| 10 a 13 | US17: faturamento do mês (só `ATIVO`), mais vendidos, ingredientes pelo **custo** (A17), ranking com filtros opcionais | `GET /api/dashboard` |

3. **Escreva também as três regras que confundem:**
   - **`:hoje` e `:agora`** vêm do Java, já no fuso de São Paulo (A9). O `DEFAULT CURRENT_TIMESTAMP` fica só como rede de segurança;
   - na consulta 3, o campo `quantidade_estoque` devolvido é o saldo **válido** (o nome segue o contrato com o Python), enquanto na tabela `ingrediente` o mesmo nome é o saldo **total**;
   - o **consumo** é a mesma fórmula nas consultas 9 e 12 (pedidos × composição); só o filtro muda: a reposição conta todos os pedidos, o dashboard só os `ATIVO`.
4. **O período** (consultas 9 a 13): o Java manda `:inicio` e `:fim` (intervalo `[inicio, fim)`). Reposição: os últimos N dias, contando hoje. Dashboard: do dia 1 do mês ao dia 1 do mês seguinte.

```powershell
git add docs\Apoio\sql_repositorios_sprint3.md
git commit -m "docs: sql das consultas da Sprint 3"
git push -u origin docs/sql-repositorios-sprint3
```

**Revisor:** o Lucas.

---

## Parte 8 — Revisões, Plano de Testes e o registro da A26 (sáb 31 e dom 01/11)

### 8.1 Revisar os PRs dos colegas

No GitHub, no PR: aba **Files changed** → leia → **Review changes** → *Comment*, *Approve* ou *Request changes*.

| PR | O que você confere |
|---|---|
| Fases do Lucas (as que têm os seus repositórios) | Que os seus arquivos são os da referência e que a evidência dos testes está no PR |
| Fase P + Q do Kauê | **Você pode ser a "2ª máquina":** rodar os 47 testes do Python na sua máquina (tutorial do Kauê, Parte 4.7) e comentar o resultado no PR. Confira também que os números da demonstração batem com o seu SQL: reposição com 0,74 kg de morango (R$ 11,10), 60 dias, R$ 100 |
| Fase 5 do Kauê | O seu `ReposicaoRepository` (consulta 9), igual ao da referência |
| `.env.example` único do Kauê (G8) | Que as variáveis do banco (`DB_*`) estão certas |
| Divisão do `app.js` do Leonardo | O `IGUAIS` na evidência |
| Documentos | Datas e decisões batendo com o roteiro da Sprint 4 |

### 8.2 Os casos do banco no Plano de Testes (dom 01/11)

Os CT30–CT72 foram incorporados em 27/10 (A37). Abra `docs/Plano_de_Testes_Mente_Fria.md`, procure os casos com a camada **BD**, e confira que cada um cita o teste de banco que o prova (por exemplo, o CT35 cita o T62). Divergência → anote no PR de documentos.

### 8.3 Responder ao "Antes de avançar" da equipe

Quando o seu PR do banco entrar, **todos** recriam o banco (Parte 3.6). Fique de olho no grupo: é a hora em que mais aparecem dúvidas de `relation … already exists` ou de estoque diferente de 0.

### 8.4 Registrar a A26 como incidente (sáb 31/10)

**Objetivo:** o registro do travamento do Docker, como **incidente novo**. **Não** atribua a causa ao BD01 da Sprint 2 ("Virtualization support not detected"), que segue sem causa registrada.

```powershell
git status
git checkout main
git pull
git checkout -b docs/diogo-evidencias
notepad docs\Sprint_3\evidencias\2026-10-26_diogo_A26_incidente-docker.md
```

O Bloco de Notas pergunta se quer criar o arquivo: **Sim**. Cole:

```
# A26 — Docker Desktop não inicia (sailor-ingest.sock)

- Ocorrências: 26/10/2026 e 30/10/2026, nesta máquina (Windows 11, build 26300).
- Sintoma: janela de erro "starting services: initializing Ingest server: listening on
  unix://C:/Users/diogo/AppData/Local/Docker/run/sailor-ingest.sock: rename … The file cannot be
  accessed by the system", com os botões Quit e Reset to factory defaults.
- Causa provável: arquivos de socket inacessíveis nas pastas %LOCALAPPDATA%\Docker\run e
  %LOCALAPPDATA%\docker-secrets-engine. Hipótese, não comprovada: a versão Insider do Windows.
- Contorno (verificado nas duas vezes): Quit (nunca Reset to factory defaults, que apaga o
  mente-fria-db); wsl --shutdown; renomear as duas pastas; abrir o Docker de novo.
- Não é o BD01 da Sprint 2.
```

Salve (**UTF-8**) e:

```powershell
git add docs\Sprint_3\evidencias
git commit -m "docs: registro do incidente A26 do Docker Desktop"
git push -u origin docs/diogo-evidencias
```

Abra o PR, com o Kauê (facilitação) como revisor.

---

## Parte 9 — Seg 02/11 à tarde: o seed no banco publicado, com o Kauê

**Quem faz o quê** (tutorial do Kauê, Partes 8.4 e 8.5):
- a URL do banco do Render tem a senha e não sai da conta do Lucas: o comando roda **no computador do Lucas**, com o Kauê guiando;
- o **seed é seu**: você confere a saída e os números.

**Pré-requisito:** o seu PR do seed (Parte 6.3) na `main`, e o computador do Lucas com a `main` atualizada (`git pull`): o comando do Kauê roda o `bd\seed_demo.sql` da pasta do Lucas.

**A ordem obrigatória:** o schema carregado (8.4 do Kauê) → o Administrador e o Funcionário criados (8.5 do Kauê) → o seed.

**O que conferir na saída do seed:** `(0 rows)` e `COMMIT` (como na Parte 6.1). Se aparecer erro antes do `COMMIT`, nada foi gravado: leia a mensagem (por exemplo, "use um banco recém-criado") antes de tentar de novo.

**Conferir os números no banco publicado:** as duas consultas da Parte 6.2 servem do mesmo jeito. No computador do Lucas, na janela em que o Kauê guiou a Parte 8.4 (ela tem o `$URL`, com o `?sslmode=require`), troque o começo `docker exec mente-fria-db psql -U postgres -d mente_fria_demo` por `docker run --rm postgres:16 psql $URL`. **Esperado:** os mesmos totais da 6.2 (as datas são as do dia).

**Evidência:** um print **só do resultado** das consultas, ou um `.txt` com a saída. **Nunca** com a URL do banco (ela tem a senha).

**Logo depois, o backup** (tutorial do Kauê, 8.7).

### 9.3 Se o tutorial do Kauê pedir para "limpar o banco do Render"

Isso acontece em dois casos: o DDL errado foi carregado (`Açaí (polpa)|40.000`) ou alguém virou Administrador antes da equipe. O banco ainda não tem nada de valor. **Onde:** no computador do Lucas, na mesma janela (com o `$URL`), na `main` atualizada.

```powershell
docker run --rm postgres:16 psql $URL -v ON_ERROR_STOP=1 -c "DROP SCHEMA public CASCADE;" -c "CREATE SCHEMA public;"
docker run --rm -v "${PWD}\bd:/bd" postgres:16 psql $URL -v ON_ERROR_STOP=1 -q -f /bd/Script_DDL.sql
docker run --rm postgres:16 psql $URL -tA -c "SELECT nome, quantidade_estoque FROM ingrediente WHERE id = 1;"
```

| Comando | O que faz |
|---|---|
| `DROP SCHEMA public CASCADE` | Apaga **todas** as tabelas e dados do banco do Render (o banco continua existindo) |
| `CREATE SCHEMA public` | Cria o "espaço" das tabelas de novo, vazio |
| O segundo e o terceiro | Carregam o DDL e conferem, como na Parte 8.4 do Kauê |

**Validado** (31/10) num PostgreSQL de teste, com um dono que **não** é superusuário, como no Render: `DROP SCHEMA` e `CREATE SCHEMA` responderam, e o DDL carregou de novo (`Açaí (polpa)|0.000`). Contra o Render de verdade, não foi testado.

> ⚠️ **Só com a confirmação do Kauê e do Lucas, e só antes do seed e dos usuários de verdade.** Depois disso, o caminho é o backup (Parte 8.7 do Kauê), e não limpar.

---

## Parte 10 — US26: porção padrão por unidade de medida (condicional)

### 10.1 O *gate* e o calendário (A40, A43)

**Só entra se**, na **seg 02/11 de manhã**, ao mesmo tempo (roteiro, §5):
- as Fases 1, 2, 3, 4A e P + Q estão na `main`;
- o *gate* da Fase 2 foi aprovado;
- **você** está com as suas entregas em dia (as Partes 3 a 7).

Quem confere é o PO (Lucas). **Se não passar, pare aqui:** a US26 fica registrada como fora do semestre, sem prejuízo.

| Dia | Entrega | Quem aprova |
|---|---|---|
| Seg 02/11 | A seção do contrato e os casos de teste (CT73 em diante) | **O Kauê** (dono do contrato) |
| Ter 03/11 | O backend: `POST` e `PATCH /api/unidades-medida`, com testes de integração | O Lucas |
| Qua 04/11, **até 18h** | A Tela 19 e as evidências | O Lucas |

**Se não estiver na `main` até qua 04/11 às 18h, ela sai:**
- feche os PRs que ainda estiverem abertos, sem merge (*Close pull request*);
- o que já entrou (o contrato e os casos na segunda, o backend na terça) **fica**: não atrapalha nada. Marque a seção do contrato como "não entregue na Sprint 4" e registre a US26 como **não entregue** no relatório. Reverter não é necessário; se o grupo preferir, use `git revert` num PR.

**O que já existe:**
- o banco: a tabela `unidade_medida_porcao_padrao` (`unidade_medida VARCHAR(20)` como chave; `porcao_padrao DECIMAL(10,3)` com `CHECK > 0`), e os testes T72 e T73;
- o `GET /api/unidades-medida` (Fase 2) e a tela de ingredientes, que já sugere a porção e deixa sobrescrever (os critérios 1 e 2 da US26);
- o `SecurityConfig`: `/api/unidades-medida/**` é só do Administrador, e o CORS já libera `PATCH`;
- o `GlobalExceptionHandler` já traduz `chk_unidade_porcao_padrao` → `400` no campo `porcao_padrao` ("A porção deve ser maior que zero.") e `unidade_medida_porcao_padrao_pkey` → `409` no campo `unidade_medida` ("Esta unidade já está cadastrada.").

**Não há código de referência** (a sua escolha em 31/10): o tutorial dá o contrato, o SQL validado e o molde de cada arquivo.

### 10.2 A seção do contrato (seg 02/11, branch `docs/us26-contrato`)

**Onde:** `git status`, `git checkout main`, `git pull` e `git checkout -b docs/us26-contrato`.

No `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`:
1. acrescente uma seção **2.11** depois da 2.10 ("Itens adotados — situação");
2. **mova** a linha "Unidades de medida (cadastrar; alterar a porção padrão)" da tabela de fora da Sprint 3 para a tabela de rotas (§2.2);
3. na nota do `GET /api/unidades-medida`, troque o "se ela entrar" por "ver §2.11";
4. registre a mudança na linha de **Revisão** do topo e no histórico (§3).

Um modelo da seção, para você ajustar e o Kauê aprovar:

````
### 2.11 Porção padrão por unidade de medida (UC20/US26) — Tela 19

**`POST /api/unidades-medida`** (só Administrador)
```json
// request
{ "unidade_medida": "g", "porcao_padrao": 40.000 }
// response 201
{ "unidade_medida": "g", "porcao_padrao": 40.000 }
```

**`PATCH /api/unidades-medida/{unidade_medida}`** (só Administrador). Altera só a porção padrão.
```json
// request
{ "porcao_padrao": 0.050 }
// response 200
{ "unidade_medida": "kg", "porcao_padrao": 0.050 }
```

- Erros `400` (`{"mensagem":"Dados inválidos","campos":{…}}`):
  - `unidade_medida` vazia ou com mais de 20 caracteres → "Informe a unidade de medida (até 20 caracteres).";
  - `porcao_padrao` ausente → "Informe a porção." (a mesma mensagem do ingrediente);
  - `porcao_padrao` ≤ 0 → "A porção deve ser maior que zero." (o banco garante a mesma regra: `chk_unidade_porcao_padrao`);
  - `porcao_padrao` com mais de 3 casas, ou de 10.000.000 para cima → "Use no máximo 3 casas decimais e um valor menor que 10.000.000." (a coluna é `DECIMAL(10,3)`: o PostgreSQL **arredonda** sem avisar o que tem mais de 3 casas, e um valor grande demais dá `numeric field overflow`, que o `GlobalExceptionHandler` não mapeia e viraria `500`. Por isso o serviço valida antes).
- Unidade repetida no `POST` → `409`, `campos.unidade_medida` "Esta unidade já está cadastrada.".
- `PATCH` de unidade inexistente → `404 {"mensagem":"Unidade de medida não encontrada."}`.
- Funcionário → `403`.
- **Alterar a porção padrão não muda os ingredientes já cadastrados**: o padrão é só a sugestão do formulário de ingrediente; cada ingrediente guarda a própria porção.
- Excluir unidade fica de fora (a FK de `ingrediente` já impede quando ela está em uso).
````

**Decida e escreva no contrato:** se o backend grava a unidade em minúsculas e sem espaços (`" G "` → `g`), como já faz com o e-mail.

**Os casos de teste** (10.3) entram no mesmo PR. Depois:

```powershell
git add docs\Apoio\Contrato_de_Comunicacao_Mente_Fria.md docs\Plano_de_Testes_Mente_Fria.md
git commit -m "docs: contrato e casos de teste da US26"
git push -u origin docs/us26-contrato
```

**Revisor:** o Kauê (dono do contrato).

### 10.3 Os casos de teste (CT73 em diante)

No `docs/Plano_de_Testes_Mente_Fria.md`, uma subseção "US26" no formato dos CT30–CT72 (objetivo · pré-condições · perfil · passos → esperado · camada · executado?). Os casos que o contrato pede:

| CT | Cenário | Esperado |
|---|---|---|
| CT73 | Administrador cadastra a unidade `g` com porção 40 | `201`; aparece no `GET /api/unidades-medida` |
| CT74 | Cadastrar a mesma unidade de novo | `409` "Esta unidade já está cadastrada." |
| CT75 | Porção 0 no `POST` e no `PATCH` | `400` no campo `porcao_padrao` |
| CT76 | `PATCH` do `kg` para 0,050 | `200`; os ingredientes em `kg` **continuam** com a porção deles |
| CT77 | `PATCH` de unidade inexistente | `404` |
| CT78 | Funcionário tenta `POST` e `PATCH` | `403` |

### 10.4 O SQL (validado em 31/10)

**Onde testar à mão:** banco `mente_fria_teste`, recém-criado com o DDL da `main` atualizada:

```powershell
git status
git checkout main
git pull
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste WITH (FORCE);" -c "CREATE DATABASE mente_fria_teste;"
docker cp bd\Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql
```

Depois:

```powershell
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "INSERT INTO unidade_medida_porcao_padrao (unidade_medida, porcao_padrao) VALUES ('g', 40) RETURNING unidade_medida, porcao_padrao;"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "INSERT INTO unidade_medida_porcao_padrao (unidade_medida, porcao_padrao) VALUES ('g', 50);"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "UPDATE unidade_medida_porcao_padrao SET porcao_padrao = 0 WHERE unidade_medida = 'kg';"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "UPDATE unidade_medida_porcao_padrao SET porcao_padrao = 0.050 WHERE unidade_medida = 'kg' RETURNING unidade_medida, porcao_padrao;"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "UPDATE unidade_medida_porcao_padrao SET porcao_padrao = 1 WHERE unidade_medida = 'xyz' RETURNING unidade_medida, porcao_padrao;"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -c "SELECT id, nome, unidade_medida, porcao_padrao FROM ingrediente WHERE unidade_medida = 'kg' ORDER BY id;"
```

**Você deve ver** (validado; trecho), na ordem:

```
 unidade_medida | porcao_padrao
----------------+---------------
 g              |        40.000
(1 row)
INSERT 0 1

ERROR:  duplicate key value violates unique constraint "unidade_medida_porcao_padrao_pkey"

ERROR:  new row for relation "unidade_medida_porcao_padrao" violates check constraint "chk_unidade_porcao_padrao"

 kg             |         0.050
(1 row)
UPDATE 1

(0 rows)
UPDATE 0

 id |     nome     | unidade_medida | porcao_padrao
----+--------------+----------------+---------------
  1 | Açaí (polpa) | kg             |         0.150
  3 | Granola      | kg             |         0.040
...
```

**Depois do teste à mão, recrie o `mente_fria_teste`** (o bloco de cima): ele ficou com a unidade `g` e o `kg` com 0,050. Sem recriar, os 41 testes dão `40 | 1`, com o T72 batendo em `unidade_medida_porcao_padrao_pkey` (validado).

**Como isso vira o backend:**
- o `INSERT … RETURNING` é o método `inserir` do repositório; a chave repetida estoura `unidade_medida_porcao_padrao_pkey`, que o `GlobalExceptionHandler` já transforma em `409`;
- o `UPDATE … RETURNING` é o método `atualizarPorcao`. **`UPDATE 0`** (nenhuma linha) quer dizer "unidade inexistente": o serviço responde `404`. Use `jdbc.query(…).stream().findFirst()` e devolva `Optional`, exatamente como o `IngredienteRepository.atualizar`;
- a porção 0 estoura `chk_unidade_porcao_padrao` → `400` (também já mapeado). Mas valide **antes**, no serviço, para mandar a mensagem por campo sem depender do banco;
- os ingredientes **não** mudam: a porção deles é uma coluna própria (`ingrediente.porcao_padrao`). É o CT76.

### 10.5 O backend (ter 03/11, branch `feature/us26-backend`)

| Arquivo | O que fazer | Molde |
|---|---|---|
| `unidade/UnidadeMedidaRepository.java` (seu) | Acrescentar `inserir(unidade, porcao)` e `atualizarPorcao(unidade, porcao)` com o SQL da 10.4 e parâmetros nomeados (`:unidade`, `:porcao`). O `RowMapper` do `listar` hoje é uma *lambda* dentro do método: tire-o para uma constante (`private static final RowMapper<UnidadeMedida> UNIDADE = …`) e use nos três | `IngredienteRepository.inserir` (`queryForObject` + `RETURNING`) e `IngredienteRepository.atualizar` (`UPDATE … RETURNING` + `.stream().findFirst()`) |
| `unidade/UnidadeMedida.java` | Os corpos das requisições: um `record` para o `POST` (`unidadeMedida`, `porcaoPadrao`) e outro para o `PATCH` (só `porcaoPadrao`). O `snake_case` do JSON vem sozinho (`spring.jackson.property-naming-strategy=SNAKE_CASE`) | Os `record`s do `Ingrediente` |
| `unidade/UnidadeMedidaService.java` (novo) | Validar (as regras da 10.2), chamar o repositório, e `404` quando o `atualizarPorcao` devolver vazio | `IngredienteService`: o `validar` (um `Map` de erros → `throw new ApiException(400, "Dados inválidos", erros)`) e o `atualizar(…).orElseThrow(() -> new ApiException(404, …))` |
| `unidade/UnidadeMedidaController.java` | Passar a receber o **serviço** no construtor (hoje recebe o repositório). Acrescentar `@PostMapping("/api/unidades-medida")` (resposta `201`: `ResponseEntity.status(201).body(…)`) e `@PatchMapping("/api/unidades-medida/{unidadeMedida}")` | O `IngredienteController` |
| `src/test/java/com/mentefria/backend/Us26PorcaoPadraoTest.java` (novo) | Um teste por CT da 10.3, com o banco real | `Fase2CrudTest`: o `unidadesDeMedidaSoParaAdministrador` (molde do CT78) e o `atualizarIngredienteInexistenteDa404` (molde do CT77) |
| `docs/Apoio/mapa_constraints.md` e `docs/Apoio/sql_repositorios.md` | No mapa, a linha do `unidade_medida_porcao_padrao_pkey` ainda diz "só seed na Sprint 1": passa a ser `409`, campo `unidade_medida`. No `sql_repositorios.md`, os dois métodos novos | As linhas que já existem nos dois |

**Os testes de integração por dentro:** toda classe que estende `IntegracaoBase` ganha, antes de cada teste, um banco recriado a partir do `bd/Script_DDL.sql`, um Administrador e um Funcionário prontos (`admin` e `funcionario`, já com o `Bearer …`), e o método `chamar(metodo, url, token, corpo)`, que devolve `status()` e `corpo()`. Exemplo do molde (do `Fase2CrudTest`):

```java
@Test void funcionarioNaoCadastraIngredienteEAdministradorSim() throws Exception {            // CT04, CT05
    var corpo = Map.of("nome", "Paçoca", "unidade_medida", "kg", …);
    assertThat(chamar("POST", "/api/ingredientes", funcionario, corpo).status()).isEqualTo(403);
    var r = chamar("POST", "/api/ingredientes", admin, corpo);
    assertThat(r.status()).isEqualTo(201);
```

O CT78 é exatamente esse molde, com `/api/unidades-medida`. O CT76 confere, depois do `PATCH`, que `jdbc.queryForObject("SELECT porcao_padrao FROM ingrediente WHERE id = 1", BigDecimal.class)` continua `0.150`.

**Rodar:** dentro de `backend`, com o Docker aberto, `.\mvnw test "-Dtest=Us26PorcaoPadraoTest"`, e antes do PR o `.\mvnw test` inteiro. Guarde a saída com o padrão do tutorial do Lucas, Parte 2.8 (`$saida = .\mvnw test …`, `Out-File -Encoding utf8`), e abra o PR (branch `feature/us26-backend`; o Lucas revisa).

### 10.6 A Tela 19 (qua 04/11, até 18h, branch `feature/us26-tela19`)

O molde é a tela-exemplo de Estoque do tutorial do Leonardo (`docs/Sprint_4/tutoriais/tutorial-leonardo-frontend.md`, Parte 11): os mesmos seis passos.

**Antes, o ambiente do frontend** (tutorial do Leonardo): o Node e o `npm install` (Parte 1.2); o servidor dos arquivos em `127.0.0.1:5500` (Parte 1.3); e o backend rodando com o `backend\.env` (Parte 9). As verificações novas só rodam de forma confiável depois da correção da A13 (Parte 7 dele).
- **Rota:** `#/porcao-padrao`. Ela já está no conjunto `SO_ADMIN` da guarda (só Administrador). No `router.js`, registre a rota; no `shell.js`, troque `['Porção Padrão',null,3]` por `['Porção Padrão','porcao-padrao']`.
- **A tela:** a lista (`GET /api/unidades-medida`, com `amount` e `unit` do `ui.js`); um formulário de "Nova unidade" (`POST`, com `formBase` e `bindForm`); e, em cada linha, um campo e um botão "Salvar" que mandam `request('/api/unidades-medida/' + encodeURIComponent(u), {method: 'PATCH', body: {porcao_padrao: valor}})`.
- **A validação** da porção usa o `decimal(raw, 3, true)` do `js/validation.js` (o mesmo do ingrediente).
- **Uma frase na tela:** "Alterar a porção padrão não muda os ingredientes já cadastrados." (o critério 3 da US26).
- **Testes:** rotas simuladas e verificações no `tests\browser.cjs` (lista, `POST` com o *payload* exato, `409` no campo, `PATCH` com o *payload* exato, o Funcionário bloqueado). Atenção: a API simulada de hoje devolve a **lista** de unidades para `/api/unidades-medida` em **qualquer** método, e não conhece `/api/unidades-medida/{u}` (daria "Rota inesperada"). Faça a rota olhar o `method` e acrescente a do `PATCH`. Para o `409`, use o `override`, como o teste "registro 409" já faz.
- **Antes do PR:** o Leonardo também mexe no `router.js`, no `shell.js` e no `tests\browser.cjs` nesses dias. Traga a `main` para a sua branch (`git fetch origin` e `git merge --no-edit origin/main`; tutorial dele, Parte 2.4) e rode o `npm test`.

### 10.7 Se der errado

| Sintoma | Causa | O que fazer |
|---|---|---|
| `POST` dá `500`, e no log `DuplicateKeyException` sem tradução | O mapa do `GlobalExceptionHandler` não pegou a constraint | Confira o nome `unidade_medida_porcao_padrao_pkey` |
| `PATCH` dá `403` no navegador, mas funciona no teste | Você está logado como Funcionário, ou a página não está em `http://127.0.0.1:5500` (outra origem recebe `403` "Invalid CORS request" do Spring) | Entre como Administrador, na porta 5500 |
| `PATCH` dá `404` para `kg` | A unidade foi para a URL com espaço ou maiúscula | Use `encodeURIComponent` e a mesma regra de minúsculas do contrato |
| O teste do CT76 falha | O `PATCH` está mexendo na tabela `ingrediente` | O `UPDATE` deve tocar **só** a `unidade_medida_porcao_padrao` |

### 10.8 Perguntas para a Review

1. Por que `PATCH`, e não `PUT`?
2. Por que alterar a porção padrão não muda os ingredientes?
3. Onde a regra "porção > 0" está garantida, e por que em mais de um lugar?

---

## Parte 11 — Entrega (até qui 05/11)

1. **Evidências** em `docs/Sprint_4/evidencias/`: os testes de banco (já no PR), o seed (6.1 e 6.2), o seed publicado (Parte 9) e, se houver, os testes da US26.
2. **O Backlog (`.docx`):** foi editado em 28/10 sem o Word (commit `97cc39d`). Abra no Word e confira a formatação antes da entrega. A Visão você já conferiu em 27/10 (A37).
3. **Contribuições individuais** das Sprints 3 e 4, no padrão de `docs/Sprint_2/sprint-2-contribuicao-diogo.md`.

---

## Problemas comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `failed to connect to the docker API` | Docker Desktop sem o motor | Parte 1.2 (A26). **Nunca** "Reset to factory defaults" |
| `A├ºa├¡` na tela | A janela não está em UTF-8 | `[Console]::OutputEncoding = [Text.Encoding]::UTF8` |
| `relation "usuario" already exists` | O DDL rodou duas vezes no mesmo banco | Recrie o banco (o bloco da 3.3) |
| Os testes dão `37 \| 4` ou `38 \| 3` sem você ter mexido | O `mente_fria_teste` ficou com as constraints de um experimento | Recrie o `mente_fria_teste` |
| Os 41 testes dão `40 \| 1`, com o T72 | Sobrou a unidade `g` do teste à mão da US26 (10.4) | Recrie o `mente_fria_teste` |
| `ERROR:  Há testes de banco falhando` | O script achou falhas: veja a tabela acima dele. Nos experimentos (3.5), é o esperado | — |
| `database … is being accessed by other users` | Uma ferramenta aberta no banco | Use o `WITH (FORCE)` no `DROP DATABASE` |
| `violates check constraint "chk_movimentacao_restante"` | Uma `ENTRADA` sem `quantidade_restante` | Toda entrada grava `restante = quantidade` |
| `violates check constraint "chk_movimentacao_motivo"` | Uma saída sem motivo | `PEDIDO`, `CONVENCIONAL` ou `DESCARTE_VENCIDO` |
| O seed recusa | Falta Administrador ou Funcionário, ou o banco não é novo | Parte 6.1 |
| `could not open file "/tmp/…"` | Faltou o `docker cp` do arquivo para dentro do container | O `docker cp` antes do `psql -f` |
| `pathspec 'feature/…' did not match` | A branch da fase ainda não está no GitHub, ou faltou o `git fetch` | Parte 5.2 |
| Datas "diferentes" no dia seguinte | O seed usa datas relativas ao dia | Conte os dias, não as datas |

---

## Colinha

```powershell
# Toda janela nova
[Console]::OutputEncoding = [Text.Encoding]::UTF8
cd "C:\Users\diogo\Mente_Fria"

# Recriar o banco de teste com o DDL da branch atual
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE IF EXISTS mente_fria_teste WITH (FORCE);" -c "CREATE DATABASE mente_fria_teste;"
docker cp bd\Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -q -v ON_ERROR_STOP=1 -f /tmp/Script_DDL.sql

# Os testes de banco (no mente_fria_teste)
docker cp bd\constraints_sprint3_teste.sql mente-fria-db:/tmp/constraints_sprint3_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql
docker cp bd\constraints_teste.sql mente-fria-db:/tmp/constraints_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql

# Repositórios na branch de uma fase
git fetch
git checkout <branch-da-fase>
git add backend
git commit -m "feat: repositorios da Fase N (parte do Diogo)"
git push
```

---

## Checklist final

- [ ] PR do banco na `main`; o cabeçalho do `constraints_teste.sql` corrigido; a equipe avisada de "recriar o banco"
- [ ] Repositórios: Fase 2 (4), Fase 3 (2), Fase 5 (1, na branch do Kauê), Fase 6 (1)
- [ ] `bd/seed_demo.sql` (PR) e os números conferidos
- [ ] `docs/Apoio/sql_repositorios_sprint3.md` (PR)
- [ ] Revisões dos PRs; a 2ª máquina do Python, se o Kauê precisar
- [ ] Casos do banco no Plano de Testes conferidos; a A26 registrada como incidente
- [ ] Seed no banco publicado, conferido (seg 02/11)
- [ ] US26 na `main` até qua 04/11, 18h, **ou** registrada como cortada
- [ ] Backlog (`.docx`) conferido no Word; contribuições individuais das Sprints 3 e 4

---

## Fora do escopo obrigatório (decisões A2b e A40)

Ficam **fora do semestre**: cancelamento de pedido (US27, já no schema), clientes (US13, constraints prontas), alertas completos de reposição e de validade (US15/US16; as consultas 1 e 2 servem de base para a cobertura parcial do adendo, item 4) e o CSV do dashboard (US19). A US26 é a única condicional (Parte 10).
