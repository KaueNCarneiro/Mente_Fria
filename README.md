# Mente Fria

Mente Fria é um sistema de apoio à gestão para pequenos comércios (piloto: uma açaiteria), que centraliza o controle de estoque, produtos, ingredientes, pedidos e clientes. Seu diferencial é calcular, via Programação Linear Inteira, a quantidade ideal de produção de cada produto para maximizar o **lucro estimado** (preço de venda menos o custo dos ingredientes) com o estoque válido disponível, conforme o Adendo nº 1 ao Termo de Aceite (27/10/2026).

**Deploy:** ainda não publicado — previsto para a **Sprint 4** (US20): banco e serviço criados no Render em **02/11/2026**, sistema no ar até pelo menos **30/11/2026**, data-limite do professor (o banco gratuito expira 30 dias depois de criado; data de expiração: _[preencher no deploy]_). Plataformas: GitHub Pages (frontend) e Render (backend + banco). O link entra aqui assim que existir, mesmo que instável.
**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039) · Laboratório de Engenharia de Software · ADS Fatec Ribeirão Preto

## Stack
- Frontend: JavaScript (ES modules), HTML e CSS, sem build — deploy no GitHub Pages
- Backend: Java 25 + Spring Boot 4.0.8 (Spring JDBC, sem JPA), build com Maven 3.9 (via Maven Wrapper)
- Otimização: Python 3.14 + Gurobi 13.0.3 — módulos de Programação Linear Inteira (recomendação de produção por lucro e, na Sprint 3, reposição com orçamento), chamados pelo backend como subprocesso (JSON via stdin/stdout)
- Banco de dados: PostgreSQL 16 — deploy no Render

## Como rodar localmente
### Pré-requisitos
- Git 2.30 ou superior
- JDK 25 (o Maven não precisa ser instalado: use o `mvnw` da pasta `backend`)
- Docker 24 ou superior (PostgreSQL 16 em container; também previsto para os testes de integração)
- Python 3.14 ou superior + pip (testado com o 3.14.0 e o Gurobi 13.0.3)
- Licença Gurobi: **não é preciso configurar**. O `gurobipy` instalado pelo `pip` traz uma licença restrita (desenvolvimento e avaliação; até 2.000 variáveis e 2.000 restrições), suficiente para rodar e testar o projeto. A WLS acadêmica do projeto é **pessoal** (do Kauê) e não deve ser compartilhada
- Node.js 20 ou superior + Google Chrome (opcional: só para os testes de navegador do frontend)

### Passo a passo
1. Clone o repositório: `git clone https://github.com/KaueNCarneiro/Mente_Fria.git`
2. Instale as dependências:
   - Backend: baixadas automaticamente pelo Maven Wrapper no primeiro `./mvnw` (em `/backend`)
   - Otimização (em `/optimization`): `python -m venv venv`, ative o ambiente (`source venv/bin/activate`; no Windows, `venv\Scripts\activate`) e rode `pip install -r requirements.txt`
   - Frontend: sem dependências de produção (apenas, para testes: `npm install` em `/frontend`)
3. Configure as variáveis de ambiente (copie `.env.example` para `.env` e preencha). **Nunca comite o `.env` real.**

   Obrigatórias hoje (usadas pelo código):

   | Variável | Descrição |
   |---|---|
   | `DB_HOST` | Host do PostgreSQL (padrão `localhost`) |
   | `DB_PORT` | Porta do PostgreSQL (padrão `5432`) |
   | `DB_NAME` | Nome do banco (padrão `mente_fria`) |
   | `DB_USER` | Usuário do PostgreSQL |
   | `DB_PASSWORD` | Senha do PostgreSQL (a mesma do `POSTGRES_PASSWORD` do passo 4) |

   Planejadas (ainda não lidas pelo código da `main`; o código da Sprint 3, em `docs/Sprint_3/referencia/`, já as usa e elas passam a valer à medida que as fases entram na `main`):

   | Variável | Descrição |
   |---|---|
   | `JWT_SECRET` | Chave que assina os tokens de login. Texto longo e aleatório; nunca reaproveitar entre ambientes |
   | `CORS_ALLOWED_ORIGIN` | Origens do frontend liberadas na API (desenvolvimento: `http://127.0.0.1:5500,http://localhost:5500`) |
   | `PYTHON_CMD` | Comando do Python na máquina (`python`, `python3` ou `py`) |
   | `OTIMIZADOR_SCRIPT` | Caminho do script, relativo à pasta de onde o backend inicia (`../optimization/app/otimizador.py`) |
   | `REPOSICAO_SCRIPT` | Caminho do módulo de reposição (`../optimization/app/reposicao.py`) |
   | `OTIMIZADOR_HABILITADO` | `true` (padrão) chama o Python; `false` faz produção e reposição responderem `503` (reserva, se o solver der problema no Render; o padrão no Render é `true`) |
   | `OTIMIZADOR_TIMEOUT_SEGUNDOS` | Tempo limite de cada chamada ao Python (padrão `10`) |
   | `MF_PYTHON` | Só para os testes do backend: caminho do Python com `gurobipy`, para os testes que chamam o Python de verdade (opcional) |

   Exemplo de `.env.example` (sem valores secretos):
   ```
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=mente_fria
   DB_USER=postgres
   DB_PASSWORD=
   JWT_SECRET=
   CORS_ALLOWED_ORIGIN=http://127.0.0.1:5500,http://localhost:5500
   PYTHON_CMD=python
   OTIMIZADOR_SCRIPT=../optimization/app/otimizador.py
   ```
   As credenciais da licença Gurobi WLS (`GRB_WLSACCESSID`, `GRB_WLSSECRET`, `GRB_LICENSEID`) são segredo tanto quanto `DB_PASSWORD`: **nunca** commitar o `.lic` nem os valores. A WLS é pessoal: só o titular a usa, no computador dele, e **nunca** no Render (a Gurobi vetou a WLS numa URL pública; no Render vale a licença do `pip`, autorizada por escrito em 28/10/2026). O backend lê o `.env` pelo caminho definido em `backend/src/main/resources/application.properties` (relativo à pasta de onde o Java é iniciado); se aparecer `Could not resolve placeholder 'DB_USER'`, defina as variáveis no terminal ou no IntelliJ.
4. Crie o banco e rode o schema:
   ```bash
   docker run --name mente-fria-db -e POSTGRES_PASSWORD=SUA_SENHA_LOCAL -e POSTGRES_DB=mente_fria -p 5432:5432 -d postgres:16
   docker cp bd/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
   docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
   ```
   Sem Docker: `psql -U postgres -c "CREATE DATABASE mente_fria;"` e `psql -U postgres -d mente_fria -f bd/Script_DDL.sql` (confirme que o banco é UTF-8). Nos dias seguintes, basta `docker start mente-fria-db`.
5. Seed: não há migrations; o seed já está dentro de `bd/Script_DDL.sql` (5 unidades de medida, 7 ingredientes, 3 produtos, 13 linhas de composição e nenhum usuário — a conta do Administrador é criada pela tela de cadastro). Confira:
   ```bash
   docker exec mente-fria-db psql -U postgres -d mente_fria -c "SELECT id, nome, unidade_medida FROM ingrediente ORDER BY id;"
   ```
   Devem aparecer 7 ingredientes, do "Açaí (polpa)" ao "Copo descartável 500ml". Para recomeçar do zero (pare o backend antes):
   ```bash
   docker exec mente-fria-db psql -U postgres -c "DROP DATABASE mente_fria;" -c "CREATE DATABASE mente_fria;"
   docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
   ```
   Mudou o schema? Edite `bd/Script_DDL.sql`, atualize `docs/DER.md` no mesmo PR e escreva no PR **"recriar o banco"**.
6. Suba o projeto:
   - Backend (em `/backend`): `./mvnw spring-boot:run` (Windows: `.\mvnw spring-boot:run`)
   - Frontend (servir `/frontend` por HTTP na porta 5500): VS Code → *Open with Live Server* em `index.html`, ou, em `/frontend`, `py -m http.server 5500 --bind 127.0.0.1`
   - Otimização: não sobe como serviço; o backend a chamará como subprocesso (**chamada Java → Python ainda não implementada**). Isolada, roda pelos testes (seção Testes)
7. Acesse o frontend em `http://127.0.0.1:5500/index.html` (API em `http://localhost:8080`, configurada em `frontend/js/config.js`). Teste do backend: `GET http://localhost:8080/api/saude` → `{"status":"ok","banco":"ok"}` e `GET http://localhost:8080/api/ingredientes` → 7 itens.

> **Estado atual (corte da Sprint 2, 02/10/2026):** o backend só tem `GET /api/saude` e `GET /api/ingredientes`; login/JWT, CORS e endpoints de escrita ainda não existem. O frontend foi verificado apenas com respostas simuladas. Detalhes em `docs/Sprint_2/`. Documentação do projeto (DER, UML, Arquitetura, Contrato de Comunicação, Plano de Testes): pasta `docs/`.
>
> **Sprint 3 (encerrada no cronograma do professor):** planejamento e preparação. O código de todas as histórias do escopo foi escrito e testado em `docs/Sprint_3/referencia/`; roteiro, decisões, tutoriais e o adendo aprovado pelo professor estão em `docs/Sprint_3/`.
>
> **Sprint 4 (em andamento; execução de 29/10 a 06/11/2026):** esse código entra na `main` fase a fase, por PR, e o sistema é publicado; entrega e Review em **06/11**. Calendário e decisões: `docs/Sprint_4/`; tutoriais de execução, reescritos um por integrante: `docs/Sprint_4/tutoriais/` (A44). Depois, uma semana de correção de bugs (até 13/11).

## Estrutura do repositório
```
/backend        — API Java + Spring Boot (Maven)
/frontend       — HTML, CSS, JavaScript (ES modules) e testes de navegador (tests/)
/optimization   — módulo Python + Gurobi (app/ e tests/)
/bd             — Script_DDL.sql (schema + seed) e constraints_teste.sql (testes de constraints)
/docs           — Documento de Visão, Backlog, UML, DER, Arquitetura, Contrato de Comunicação, Plano de Testes e relatórios das sprints
```

## Convenções da equipe
- Branches: `feature/nome-curto`, `fix/nome-curto`, a partir de `main`
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`)
- Toda PR exige revisão de ao menos 1 integrante antes do merge.
- Merge na `main` obrigatório após aprovação de cada PR.

## Testes
Como rodar:
- Otimização (Python, 8 testes na `main`; 47 no código da Sprint 3): `python -m pytest optimization/tests` (basta a licença que vem com o `gurobipy` do `pip`)
- Banco (32 testes de constraints; use um banco descartável, pois os contadores de ID avançam):
  ```bash
  docker exec mente-fria-db psql -U postgres -c "CREATE DATABASE mente_fria_teste;"
  docker exec mente-fria-db psql -U postgres -d mente_fria_teste -f /tmp/Script_DDL.sql
  docker cp bd/constraints_teste.sql mente-fria-db:/tmp/constraints_teste.sql
  docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql
  ```
- Frontend (Playwright, com API simulada; servidor do frontend na porta 5500): em `/frontend`, `npm install` e `npm test`
- Backend (hoje só o teste de contexto, exige o PostgreSQL no ar): em `/backend`, `./mvnw test`

Testes de integração com Testcontainers estão no código de referência (`docs/Sprint_3/referencia/backend/`, 95 testes) e entram na `main` com as fases da Sprint 4. Não há CI configurado.

## Licença / Uso acadêmico
Projeto desenvolvido para a disciplina de Laboratório de Engenharia de Software — ADS, Fatec Ribeirão Preto, 2026. O Gurobi é usado com a licença restrita do `pip` (desenvolvimento e avaliação) e com a WLS acadêmica pessoal do Kauê, ambas só para uso não comercial. No sistema publicado, o solver usa a licença restrita do `pip`, com autorização escrita da Gurobi (28/10/2026) para esta demonstração acadêmica. O sistema serve à demonstração acadêmica e não deve ser usado em operação comercial.
