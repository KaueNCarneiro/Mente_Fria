# Mente Fria

Mente Fria é um sistema de apoio à gestão para pequenos comércios (piloto: uma açaiteria), que centraliza o controle de estoque, produtos, ingredientes e pedidos. Seu diferencial é calcular, via Programação Linear Inteira, a quantidade ideal de produção de cada produto para maximizar o **lucro estimado** (preço de venda menos o custo dos ingredientes) com o estoque válido disponível, conforme o Adendo nº 1 ao Termo de Aceite (27/10/2026).

**Sistema publicado (US20):**
- Frontend (GitHub Pages): https://kauencarneiro.github.io/Mente_Fria/
- Backend (Render): https://mente-fria-backend.onrender.com (teste: `GET /api/saude`)
- O banco gratuito do Render foi criado em 11/11/2026 e **vence em 11/12/2026, às 21h59** (depois da data-limite do professor, 30/11/2026).
- **Deploy automático desde 15/11/2026:** cada merge na `main` publica o backend sozinho, inclusive merges só de documentação (o serviço reinicia). O frontend é publicado pelo GitHub Pages a partir da `main`.
- O plano gratuito "dorme" depois de 15 minutos sem uso: a primeira chamada pode levar cerca de 1 minuto (o frontend publicado espera até 90 s).

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039) · Laboratório de Engenharia de Software · ADS Fatec Ribeirão Preto

## Stack
- Frontend: JavaScript (ES modules), HTML e CSS, sem build — deploy no GitHub Pages
- Backend: Java 25 + Spring Boot 4.0.8 (Spring JDBC, sem JPA), build com Maven 3.9 (via Maven Wrapper)
- Otimização: Python 3.14 + Gurobi 13.0.3 — módulos de Programação Linear Inteira (recomendação de produção por lucro e reposição com orçamento), chamados pelo backend como subprocesso (JSON via stdin/stdout)
- Banco de dados: PostgreSQL 16 — deploy no Render

## Como rodar localmente
### Pré-requisitos
- Git 2.30 ou superior
- JDK 25 (o Maven não precisa ser instalado: use o `mvnw` da pasta `backend`)
- Docker 24 ou superior (PostgreSQL 16 em container; os testes do backend também usam o Docker, pelo Testcontainers)
- Python 3.14 ou superior + pip (testado com o 3.14.0 e o Gurobi 13.0.3)
- Licença Gurobi: **não é preciso configurar**. O `gurobipy` instalado pelo `pip` traz uma licença restrita (desenvolvimento e avaliação; até 2.000 variáveis e 2.000 restrições), suficiente para rodar e testar o projeto. A WLS acadêmica do projeto é **pessoal** (do Kauê) e não deve ser compartilhada
- Node.js 20 ou superior + Google Chrome (opcional: só para os testes de navegador do frontend)

### Passo a passo
1. Clone o repositório: `git clone https://github.com/KaueNCarneiro/Mente_Fria.git`
2. Instale as dependências:
   - Backend: baixadas automaticamente pelo Maven Wrapper no primeiro `./mvnw` (em `/backend`)
   - Otimização (em `/optimization`): `python -m venv venv`, ative o ambiente (`source venv/bin/activate`; no Windows, `venv\Scripts\activate`) e rode `pip install -r requirements.txt`
   - Frontend: sem dependências de produção (apenas, para testes: `npm install` em `/frontend`)
3. Configure as variáveis de ambiente (copie `backend/.env.example` para `backend/.env` e preencha). **Nunca comite o `.env` real.**

   Todas as variáveis abaixo são lidas pelo código (`backend/src/main/resources/application.properties`). O modelo comentado está em `backend/.env.example`.

   | Variável | Obrigatória? | Descrição |
   |---|---|---|
   | `DB_HOST` | Não (padrão `localhost`) | Host do PostgreSQL |
   | `DB_PORT` | Não (padrão `5432`) | Porta do PostgreSQL |
   | `DB_NAME` | Não (padrão `mente_fria`) | Nome do banco |
   | `DB_USER` | **Sim** | Usuário do PostgreSQL |
   | `DB_PASSWORD` | **Sim** | Senha do PostgreSQL (a mesma do `POSTGRES_PASSWORD` do passo 4) |
   | `JWT_SECRET` | **Sim** | Chave que assina os tokens de login: 32 ou mais caracteres aleatórios, diferente em cada ambiente |
   | `CORS_ALLOWED_ORIGIN` | Não (padrão `http://127.0.0.1:5500,http://localhost:5500`) | Origens do frontend liberadas na API. No Render: `https://kauencarneiro.github.io`, sem barra no fim |
   | `PYTHON_CMD` | Não (padrão `python`) | Python usado pelo backend. Com o ambiente do projeto, a partir de `backend`: `../optimization/venv/Scripts/python.exe` (Windows) |
   | `OTIMIZADOR_SCRIPT` | Não (padrão `../optimization/app/otimizador.py`) | Módulo de recomendação de produção, relativo à pasta de onde o backend inicia |
   | `REPOSICAO_SCRIPT` | Não (padrão `../optimization/app/reposicao.py`) | Módulo de recomendação de reposição |
   | `OTIMIZADOR_HABILITADO` | Não (padrão `true`) | `false` faz produção e reposição responderem `503` sem chamar o Python (reserva, se o solver der problema; no Render fica `true`) |
   | `OTIMIZADOR_TIMEOUT_SEGUNDOS` | Não (padrão `10`) | Tempo limite de cada chamada ao Python |
   | `PORT` | Não (padrão `8080`) | Porta do backend; no Render, definida pela própria plataforma |
   | `MF_PYTHON` | Não | Só para os testes do backend: caminho do Python com `gurobipy`. Sem ela, os testes que chamam o Python de verdade são pulados |

   > ⚠️ Salve o `.env` em **UTF-8 sem BOM** (no Bloco de Notas, confira "UTF-8" na barra de baixo, e não "UTF-8 com BOM"). Com BOM, o Spring lê errado a primeira linha e o backend não sobe (`Could not resolve placeholder`). Nos caminhos, use `/`.

   As credenciais da licença Gurobi WLS (`GRB_WLSACCESSID`, `GRB_WLSSECRET`, `GRB_LICENSEID`) são segredo tanto quanto `DB_PASSWORD`: **nunca** commitar o `.lic` nem os valores. A WLS é pessoal: só o titular a usa, no computador dele, e **nunca** no Render (a Gurobi vetou a WLS numa URL pública; no Render vale a licença do `pip`, autorizada por escrito em 28/10/2026). O backend lê o `.env` pelo caminho definido em `backend/src/main/resources/application.properties` (relativo à pasta de onde o Java é iniciado); se aparecer `Could not resolve placeholder 'DB_USER'`, defina as variáveis no terminal ou no IntelliJ.
4. Crie o banco e rode o schema (comandos a partir da raiz do repositório):
   ```bash
   docker run --name mente-fria-db -e POSTGRES_PASSWORD=SUA_SENHA_LOCAL -e POSTGRES_DB=mente_fria -p 5432:5432 -d postgres:16
   docker cp bd/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
   docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
   ```
   Sem Docker: `psql -U postgres -c "CREATE DATABASE mente_fria;"` e `psql -U postgres -d mente_fria -f bd/Script_DDL.sql` (confirme que o banco é UTF-8). Nos dias seguintes, basta `docker start mente-fria-db`.
5. Seed: não há migrations; o seed já está dentro de `bd/Script_DDL.sql` (5 unidades de medida, 7 ingredientes **com estoque zero**, 3 produtos, 13 linhas de composição e nenhum usuário — a conta do Administrador é criada pela tela de cadastro, e o estoque entra pela tela de Estoque, com validade). Confira:
   ```bash
   docker exec mente-fria-db psql -U postgres -d mente_fria -c "SELECT id, nome, unidade_medida FROM ingrediente ORDER BY id;"
   ```
   Devem aparecer 7 ingredientes, do "Açaí (polpa)" ao "Copo descartável 500ml". Para recomeçar do zero (pare o backend antes):
   ```bash
   docker exec mente-fria-db psql -U postgres -c "DROP DATABASE mente_fria;" -c "CREATE DATABASE mente_fria;"
   docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
   ```
   Dados de demonstração (opcional): depois de criar o Administrador e um Funcionário pelas telas, rode uma vez `bd/seed_demo.sql` (estoque em linhas com validade, clientes e pedidos).

   Mudou o schema? Edite `bd/Script_DDL.sql`, atualize `docs/DER.md` no mesmo PR e escreva no PR **"recriar o banco"**.
6. Suba o projeto:
   - Backend (em `/backend`): `./mvnw spring-boot:run` (Windows: `.\mvnw spring-boot:run`)
   - Frontend (servir `/frontend` por HTTP na porta 5500): VS Code → *Open with Live Server* em `index.html`, ou, em `/frontend`, `py -m http.server 5500 --bind 127.0.0.1`
   - Otimização: não sobe como serviço; o backend chama o Python como subprocesso a cada recomendação (produção e reposição), pelo `PYTHON_CMD`. Prepare o `optimization/venv` do passo 2 antes
   - Perfil `dev` (opcional): com `SPRING_PROFILES_ACTIVE=dev` (PowerShell: `$env:SPRING_PROFILES_ACTIVE = "dev"`), o backend expõe o eco `GET /api/teste/python`, que prova que o Java fala com o Python. Fora do perfil `dev`, essa rota não existe
7. Acesse o frontend em `http://127.0.0.1:5500/index.html` (API em `http://localhost:8080`, configurada em `frontend/js/config.js`). Teste do backend: `GET http://localhost:8080/api/saude` → `{"status":"ok","banco":"ok"}`. As demais rotas exigem login (token JWT) e respondem `401` em JSON sem ele; o caminho é criar o Administrador na tela de cadastro e entrar.

> **Situação (15/11/2026):** as histórias obrigatórias (US1–US7, US9–US12, US14, US17, US18 e US20) e a US26 estão na `main` e publicadas. A Sprint 4 terminou em 06/11 e a semana de correção de bugs, em 13/11. Calendário, decisões e pendências: `docs/Sprint_4/`; tutoriais de cada integrante: `docs/Sprint_4/tutoriais/`; evidências: `docs/Sprint_4/evidencias/`. O planejamento e o código de referência da Sprint 3 ficam em `docs/Sprint_3/` como registro. Documentação do projeto (DER, UML, Arquitetura, Contrato de Comunicação, Plano de Testes): pasta `docs/`.
>
> **Entrega final:** o índice dos artefatos do semestre (E1 a E9) e os desvios de escopo estão em [`docs/documentacao-final.md`](docs/documentacao-final.md). O uso do sistema por perfil está no [`docs/manual-do-usuario.md`](docs/manual-do-usuario.md), e o resultado dos testes, em [`docs/relatorio-testes-final.md`](docs/relatorio-testes-final.md).

## Estrutura do repositório
```
/backend        — API Java + Spring Boot (Maven)
/frontend       — HTML, CSS, JavaScript (ES modules) e testes de navegador (tests/)
/optimization   — módulo Python + Gurobi (app/ e tests/)
/bd             — Script_DDL.sql (schema + seed), seed_demo.sql (dados de demonstração), constraints_teste.sql e constraints_sprint3_teste.sql (testes de constraints)
/docs           — Documento de Visão, Backlog, UML, DER, Arquitetura, Contrato de Comunicação, Plano de Testes e relatórios das sprints
```

## Convenções da equipe
- Branches: uma por sprint (`sprint-N`, criada a partir da `main`). Os commits da sprint entram nela, direto ou por PR de uma branch `feature/nome-curto` ou `fix/nome-curto` criada a partir da `sprint-N`; a `sprint-N` entra na `main` por PR
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`)
- Toda PR exige revisão de ao menos 1 integrante antes do merge.
- Merge na `main` obrigatório após aprovação de cada PR.

## Testes
Como rodar:
- Otimização (Python, 47 testes): com o `optimization/venv` ativado, `python -m pytest optimization/tests` (basta a licença que vem com o `gurobipy` do `pip`)
- Banco (32 + 41 testes de constraints: `bd/constraints_teste.sql`, T1–T32, e `bd/constraints_sprint3_teste.sql`, T33–T73; use um banco descartável, pois os contadores de ID avançam):
  ```bash
  docker exec mente-fria-db psql -U postgres -c "CREATE DATABASE mente_fria_teste;"
  docker exec mente-fria-db psql -U postgres -d mente_fria_teste -f /tmp/Script_DDL.sql
  docker cp bd/constraints_teste.sql mente-fria-db:/tmp/constraints_teste.sql
  docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql
  docker cp bd/constraints_sprint3_teste.sql mente-fria-db:/tmp/constraints_sprint3_teste.sql
  docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint3_teste.sql
  ```
- Frontend (Playwright, com API simulada; servidor do frontend na porta 5500): em `/frontend`, `npm install` e `npm test`
- Backend (101 testes, com PostgreSQL temporário do Testcontainers; o `mente-fria-db` não é usado): abra o Docker Desktop e, em `/backend`, rode `./mvnw test` (Windows: `.\mvnw test`). Para incluir os testes que chamam o Python de verdade, antes defina `MF_PYTHON` (PowerShell: `$env:MF_PYTHON = "..\optimization\venv\Scripts\python.exe"`); sem ela, esses testes são pulados

Não há CI de testes: as suítes rodam localmente antes de cada PR. O único workflow (`.github/workflows/frontend-pages.yml`) publica o frontend no GitHub Pages.

## Licença / Uso acadêmico
Projeto desenvolvido para a disciplina de Laboratório de Engenharia de Software — ADS, Fatec Ribeirão Preto, 2026. O Gurobi é usado com a licença restrita do `pip` (desenvolvimento e avaliação) e com a WLS acadêmica pessoal do Kauê, ambas só para uso não comercial. No sistema publicado, o solver usa a licença restrita do `pip`, com autorização escrita da Gurobi (28/10/2026) para esta demonstração acadêmica. O sistema serve à demonstração acadêmica e não deve ser usado em operação comercial.
