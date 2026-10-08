# Mente Fria

Mente Fria é um sistema de apoio à gestão para pequenos comércios (piloto: uma açaiteria), que centraliza o controle de estoque, produtos, ingredientes, pedidos e clientes. Seu diferencial é calcular, via Programação Linear, a quantidade ideal de produção de cada produto para maximizar a receita com os recursos disponíveis.

**Deploy:** ainda não publicado — previsto para a Sprint 4 (Backlog Priorizado, US20). Plataformas definidas: GitHub Pages (frontend) e Render (backend + banco). O link entra aqui assim que existir, mesmo que instável.
**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039) · Laboratório de Engenharia de Software · ADS Fatec Ribeirão Preto

## Stack
- Frontend: JavaScript (ES modules), HTML e CSS, sem build — deploy no GitHub Pages
- Backend: Java 25 + Spring Boot 4.0.8 (Spring JDBC, sem JPA), build com Maven 3.9 (via Maven Wrapper)
- Otimização: Python 3.14 + Gurobi 13.0.1 — módulo de Programação Linear (recomendação de produção), chamado pelo backend como subprocesso (JSON via stdin/stdout)
- Banco de dados: PostgreSQL 16 — deploy no Render

## Como rodar localmente
### Pré-requisitos
- Git 2.30 ou superior
- JDK 25 (o Maven não precisa ser instalado: use o `mvnw` da pasta `backend`)
- Docker 24 ou superior (PostgreSQL 16 em container; também previsto para os testes de integração)
- Python 3.14 ou superior + pip (o Gurobi 13.0.1 é a primeira versão compatível com o Python 3.14)
- Licença Gurobi acadêmica (WLS) — a configuração para todos os integrantes ainda está pendente
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

   Planejadas (ainda não lidas pelo código; entram com login, CORS e chamada ao Python):

   | Variável | Descrição |
   |---|---|
   | `JWT_SECRET` | Chave que assina os tokens de login. Texto longo e aleatório; nunca reaproveitar entre ambientes |
   | `CORS_ALLOWED_ORIGIN` | Origens do frontend liberadas na API (desenvolvimento: `http://127.0.0.1:5500,http://localhost:5500`) |
   | `PYTHON_CMD` | Comando do Python na máquina (`python`, `python3` ou `py`) |
   | `OTIMIZADOR_SCRIPT` | Caminho do script, relativo à pasta de onde o backend inicia (`../optimization/app/otimizador.py`) |

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
   As credenciais da licença Gurobi WLS (`GRB_WLSACCESSID`, `GRB_WLSSECRET`, `GRB_LICENSEID`) são segredo tanto quanto `DB_PASSWORD`: **nunca** commitar o `.lic` nem os valores. O backend lê o `.env` pelo caminho definido em `backend/src/main/resources/application.properties` (relativo à pasta de onde o Java é iniciado); se aparecer `Could not resolve placeholder 'DB_USER'`, defina as variáveis no terminal ou no IntelliJ.
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
- Otimização (Python, 8 testes): `python -m pytest optimization/tests` (exige a licença Gurobi configurada)
- Banco (32 testes de constraints; use um banco descartável, pois os contadores de ID avançam):
  ```bash
  docker exec mente-fria-db psql -U postgres -c "CREATE DATABASE mente_fria_teste;"
  docker exec mente-fria-db psql -U postgres -d mente_fria_teste -f /tmp/Script_DDL.sql
  docker cp bd/constraints_teste.sql mente-fria-db:/tmp/constraints_teste.sql
  docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_teste.sql
  ```
- Frontend (Playwright, com API simulada; servidor do frontend na porta 5500): em `/frontend`, `npm install` e `npm test`
- Backend (hoje só o teste de contexto, exige o PostgreSQL no ar): em `/backend`, `./mvnw test`

Testes de integração com Testcontainers e CI (GitHub Actions) estão planejados em `docs/Plano_de_Testes_Mente_Fria.md`, mas ainda não existem.

## Licença / Uso acadêmico
Projeto desenvolvido para a disciplina de Laboratório de Engenharia de Software — ADS, Fatec Ribeirão Preto, 2026.
