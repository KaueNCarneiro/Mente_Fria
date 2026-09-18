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

4. Crie o banco e rode o schema: `psql -U [usuario] -d [nome_do_banco] -f [caminho do arquivo].sql` (pede a senha interativamente, ou defina `PGPASSWORD` no ambiente para rodar sem prompt)
5. Rode as migrations/seed (se houver): `[não identificamos script de seed nos documentos atuais]`
6. Suba o projeto:
   - Backend: `[ex.: mvn spring-boot:run]`
   - Otimização: `[depende de como o backend chama o módulo Python — subprocesso ou serviço HTTP separado]`
   - Frontend: `[ex.: abrir index.html, ou comando de um servidor estático]`
7. Acesse em `[url local, ex.: http://localhost:8080]`

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
