# Mente Fria

Mente Fria é um sistema de apoio à gestão para pequenos comércios (piloto: uma açaiteria), que centraliza o controle de estoque, produtos, ingredientes, pedidos e clientes. Seu diferencial é calcular, via Programação Linear, a quantidade ideal de produção de cada produto para maximizar a receita com os recursos disponíveis.

**Deploy:** ainda não publicado — previsto para a Sprint 4 (Backlog Priorizado, US20). Plataformas já definidas: GitHub Pages (frontend) e Render (backend + banco de dados). O link entra aqui assim que existir, mesmo que instável.
**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039) · Laboratório de Engenharia de Software · ADS Fatec Ribeirão Preto

## Stack
- Frontend: JavaScript, HTML, CSS — deploy: GitHub Pages
- Backend: Java + Spring Boot ([versão do JDK] / [versão do Spring Boot])
- Otimização: Python + Gurobi ([versão do Python] / [versão do Gurobi]) — módulo de Programação Linear (recomendação de produção)
- Banco de dados: PostgreSQL 16+ — deploy: Render

*(O item "Otimização" foi adicionado ao modelo de 3 linhas porque o Termo de Aceite trata Python/Gurobi como uma camada própria da stack, separada do backend Java.)*

## Como rodar localmente
### Pré-requisitos
- Git
- JDK `[versão mínima — depende da versão do Spring Boot usada]`
- `[Maven ou Gradle]` `[versão mínima]`
- PostgreSQL 16+
- Python `[versão mínima]` + pip
- Licença Gurobi acadêmica — `[tipo a definir: node-locked ou Web License Service (WLS)]`
- Docker (necessário para os testes de integração, que sobem um PostgreSQL 16 real via Testcontainers)

### Passo a passo
1. Clone o repositório: `git clone [url]`
2. Instale as dependências:
   - Backend: `[mvn install ou gradle build]`
   - Otimização: `pip install -r [caminho]/requirements.txt`
   - Frontend: sem dependências de build identificadas até o momento (HTML/CSS/JS servidos como estáticos)
3. Configure as variáveis de ambiente (copie `.env.example` para `.env` e preencha — **nunca comite o `.env` real**):

   | Variável | Descrição |
   |---|---|
   | `DB_HOST` | Host do PostgreSQL |
   | `DB_PORT` | Porta do PostgreSQL (padrão `5432`) |
   | `DB_NAME` | Nome do banco (ex.: `mente_fria`) |
   | `DB_USER` | Usuário do PostgreSQL |
   | `DB_PASSWORD` | Senha do PostgreSQL |
   | `[GRB_LICENSE_FILE ou GRB_WLSACCESSID / GRB_WLSSECRET]` | Credenciais da licença Gurobi (depende do tipo escolhido) |

4. Crie o banco e carregue o schema com os dados de exemplo (detalhes na seção **Banco de dados**, abaixo):
   ```bash
   docker run --name mente-fria-db -e POSTGRES_PASSWORD=SUA_SENHA_LOCAL -e POSTGRES_DB=mente_fria -p 5432:5432 -d postgres:16
   docker cp docs/Script_DDL.sql mente-fria-db:/tmp/Script_DDL.sql
   docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
   ```
   A senha escolhida em `POSTGRES_PASSWORD` deve ser a mesma do `DB_PASSWORD` no seu `.env`.
5. O seed já vem dentro do `Script_DDL.sql` (não há migrations nesta fase). Confira:
   ```bash
   docker exec mente-fria-db psql -U postgres -d mente_fria -c "SELECT id, nome, unidade_medida FROM ingrediente ORDER BY id;"
   ```
   Devem aparecer 7 ingredientes, do "Açaí (polpa)" ao "Copo descartável 500ml".
6. Suba o projeto:
   - Backend: `[ex.: mvn spring-boot:run]`
   - Otimização: `[depende de como o backend chama o módulo Python — subprocesso ou serviço HTTP separado]`
   - Frontend: `[ex.: abrir index.html, ou comando de um servidor estático]`
7. Acesse em `[url local, ex.: http://localhost:8080]`

## Banco de dados

PostgreSQL 16 (versão final a alinhar com a oferecida pelo Render). O schema completo e os dados de exemplo estão em `docs/Script_DDL.sql`; a documentação do modelo está em `docs/der.md`.

### Criar o banco

**Com Docker (recomendado):** o container `postgres:16` já usa codificação UTF-8, o que evita diferenças de acentuação entre as máquinas da equipe. Comandos nos passos 4 e 5. Nos dias seguintes, basta `docker start mente-fria-db`.

**Sem Docker (PostgreSQL local):**
```bash
psql -U postgres -c "CREATE DATABASE mente_fria;"
psql -U postgres -d mente_fria -f docs/Script_DDL.sql
```
Confirme que o banco é UTF-8: `psql -U postgres -d mente_fria -c "SHOW server_encoding;"` deve devolver `UTF8`.

### Recomeçar do zero

Rodar o script duas vezes no mesmo banco dá erro (as tabelas já existem). Para recomeçar, pare o backend (uma conexão aberta impede o `DROP`) e recrie o banco:
```bash
docker exec mente-fria-db psql -U postgres -c "DROP DATABASE mente_fria;" -c "CREATE DATABASE mente_fria;"
docker exec mente-fria-db psql -U postgres -d mente_fria -f /tmp/Script_DDL.sql
```

### O que vem no seed

- **Unidades de medida** (`kg`, `mg`, `l`, `ml`, `un`, sempre em minúsculo): a tela de gestão de unidades só chega na Sprint 3, então nesta fase elas entram apenas por aqui. O cadastro de ingredientes só aceita unidades que existam nesta tabela.
- **7 ingredientes, 3 produtos e 13 linhas de composição** (dados de exemplo de uma açaiteria).
- **Nenhum usuário.** A conta do Administrador é criada pela tela de cadastro (US1) e os funcionários pelo Administrador (US2). Pedidos, clientes e movimentações de estoque entram na Sprint 2.

### Testes de banco

O script `docs/constraints_sprint1_teste.sql` tenta gravar dados inválidos e confere se o banco os rejeita pela constraint esperada (32 casos). Tudo roda dentro de uma transação desfeita no final, mas os contadores de ID avançam mesmo assim — **rode em um banco descartável**, não no de desenvolvimento:
```bash
docker exec mente-fria-db psql -U postgres -c "CREATE DATABASE mente_fria_teste;"
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -f /tmp/Script_DDL.sql
docker cp docs/constraints_sprint1_teste.sql mente-fria-db:/tmp/constraints_sprint1_teste.sql
docker exec mente-fria-db psql -U postgres -d mente_fria_teste -v ON_ERROR_STOP=1 -f /tmp/constraints_sprint1_teste.sql
```
Ao final aparece a contagem de aprovados/reprovados; se algum falhar, o comando termina com código de saída diferente de zero.

### Mudou o schema?

Sem ferramenta de migrations por enquanto: edite o `docs/Script_DDL.sql`, atualize o `docs/der.md` no mesmo Pull Request e escreva no PR **"recriar o banco"**, para que todos rodem o "Recomeçar do zero". Não enfraqueça nem remova constraints sem combinar com a equipe (critério de bloqueio nº 4 do `Plano_de_Testes_Mente_Fria.md`).

## Estrutura do repositório
```
/docs           — Documento de Visão, Backlog, diagramas UML, DER
/backend        — API Java + Spring Boot
/frontend       — HTML, CSS, JavaScript
/optimization   — módulo Python + Gurobi (Programação Linear)
```
*(Estrutura sugerida a partir do que já existe hoje — DER e DDL referenciam `docs/` — e da separação de deploy GitHub Pages ✕ Render. Nomes reais das pastas a confirmar com a equipe.)*

## Convenções da equipe
- Branches: `feature/nome-curto`, `fix/nome-curto`, a partir de `main`
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`)
- Toda PR exige revisão de ao menos 1 integrante antes do merge na `main`
- Merge na `main` obrigatório após aprovação de cada PR

## Testes
- **Unitário** — regras de negócio isoladas, sem banco/rede. JUnit 5 + Mockito (backend Java); pytest (módulo Python/Gurobi, com resultado esperado calculado fora do próprio otimizador).
- **Integração** — fluxo Controller → Service → Repository → PostgreSQL, autenticação por perfil e constraints do banco. JUnit 5 + Spring Boot Test (`@SpringBootTest`, `MockMvc`) + Testcontainers (PostgreSQL 16 real via Docker).
- **Manual/aceitação** — roteiro por história (Termo de Aceite), executado por um integrante diferente de quem implementou; obrigatório antes de cada entrega (E5–E8).

Como rodar:
- Backend (unitário + integração): `mvn test`
- Otimização (Python/Gurobi): `pytest`

CI (GitHub Actions, a definir o arquivo de pipeline): roda unitário e integração a cada push/PR; integração é obrigatória antes do merge.

Critério de bloqueio de merge: veja `Plano_de_Testes_Mente_Fria.md`, seção 2.

## Licença / Uso acadêmico
Projeto desenvolvido para a disciplina de Laboratório de Engenharia de Software — ADS, Fatec Ribeirão Preto, 2026.
