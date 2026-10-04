# Relatório Individual de Contribuição — Sprint 2 — Lucas Gabriel Valadares Bassi (RA 2840482423008)

**Papel nesta sprint:** Product Owner / Responsável por Backend

## 1. O que fiz
| Item | PR/commit | Status |
|---|---|---|
| Recriação da estrutura do backend (Spring Boot + Maven, pasta `/backend`) após a exclusão do repositório original | commit `9e02498` (`first commit`, na `main`) | Feito |
| Conexão do backend com o PostgreSQL via `JdbcTemplate` (sem JPA, conforme a decisão D4), com credenciais em variáveis de ambiente (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) lidas do `.env` | commit `9e02498` | Feito |
| Endpoint `GET /api/saude` (`SaudeController`), que confirma a comunicação Java ↔ banco com um `SELECT 1` | commit `9e02498` | Feito |
| Endpoint `GET /api/ingredientes` (`IngredienteController`, `IngredienteRepository`, `IngredienteResumo`), com JSON em snake_case conforme o Contrato de Comunicação | commit `9e02498` | Feito |
| Teste de contexto do Spring (`BackendApplicationTests`) | commit `9e02498` | Feito |
| Integração na `main` do PR #2 (`frontend-atualizado`, do Leonardo) | Merge do PR #2 (`56b0e2e...d562fe7`) | Feito |

## 2. Rituais que participei
- [X] Dailies/weeklies (2 de 2)
- [ ] Sprint Review
- [X] Retrospectiva

## 3. PRs de colegas que revisei
| PR | Autor | Comentário resumido |
|---|---|---|
| #2 — `frontend-atualizado` | Leonardo | Conferi se havia estruturas de diretórios incorretas que pudessem afetar novamente toda a estrutura do projeto. |

## 4. Dificuldades e o que aprendi
Na Sprint 2 recriei do zero a estrutura do backend, já com a conexão ao banco funcionando, depois que os conflitos da Sprint 1 levaram a equipe a excluir o repositório. Por isso, todo o meu trabalho de backend aparece em um único commit (`first commit`), que reúne a estrutura recriada, a conexão com o banco e os dois endpoints. A maior dificuldade foi entender o que era necessário para conectar o backend ao banco e obter a resposta dele, principalmente no arquivo `.env`.

Aprendi a usar o `JdbcTemplate` com SQL explícito e mapeamento manual de linha para objeto, a manter credenciais fora do código com variáveis de ambiente e a alinhar o JSON ao Contrato de Comunicação (snake_case com `@JsonProperty`), para que o frontend consuma os campos sem tradução. Também passei a integrar o trabalho dos colegas na `main` por Pull Request e aprendi comandos do Git/GitHub que ajudaram a organizar melhor o projeto.

O que fez o banco se conectar ao backend na Sprint 2 foi a reorganização da estrutura de diretórios e a exclusão de documentos desnecessários. Na Sprint 1, o projeto não encontrava os caminhos corretos por causa da desorganização.
