# Ata de Retrospectiva — Sprint 1 — Mente Fria

* Data: 18/09/2026
* Presentes: Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039)

## 1. Ações da retrospectiva anterior — foram aplicadas?
Esta é a primeira sprint; não há ata anterior.

| Ação decidida | Aplicada? | Evidência/comentário |
|---|---|---|
| — | — | Primeira retrospectiva do projeto |

## 2. O que funcionou bem
- **Documentação de base:** DER, `Script_DDL.sql` com seed, diagramas UML, Plano de Testes e protótipo navegável ficaram prontos e consistentes entre si.
- **Banco de dados:** schema com constraints nomeadas, `mapa_constraints.md` (constraint → campo → mensagem → HTTP) e script de teste com 32 casos, aprovados no PostgreSQL 16.
- **Contratos entre as partes:** o Contrato de Comunicação (Java ↔ Python e Frontend ↔ Backend) e o SQL dos repositórios deixaram claro o que cada integrante entrega.
- **Decisões de arquitetura e de produto fechadas:** PostgreSQL, `JdbcTemplate`, subprocesso Python/JSON, JWT, regra do açaí (CT15), D-A (cadastro de Administrador só até existir o primeiro) e D-B (campo porção na Tela 07).
- **Papéis claros:** frontend (Leonardo), dados (Diogo), PO e backend (Lucas), facilitação e Python (Kauê).

## 3. O que não funcionou
- **Estrutura do repositório:** as pastas ficaram desorganizadas e surgiram conflitos grandes; o repositório teve que ser excluído e recriado, e o histórico de commits e PRs da Sprint 1 foi perdido.
- **Banco ↔ backend:** o backend foi criado, mas nunca chegou a se comunicar com o PostgreSQL. Os caminhos de arquivos e a configuração (`.env`) não ficaram corretos.
- **Python ↔ Java:** a comunicação ficou só no papel (contrato e roteiro de Hello World), sem execução integrada.
- **Meta da sprint não cumprida:** a infraestrutura que destravaria o CRUD (US1–US7) não ficou pronta, e nenhuma história funcional foi concluída.
- **Pouco tempo:** a Sprint 1 foi comprimida em uma semana.

## 4. Ações para a próxima sprint
| Ação | Responsável |
|---|---|
| Recriar o repositório com estrutura organizada (`/backend`, `/frontend`, `/optimization`, `/docs`) e `.gitignore` correto, antes de qualquer outra tarefa | Lucas e Kauê |
| Fazer o backend se conectar ao PostgreSQL e validar com `GET /api/saude` e `GET /api/ingredientes` antes de começar o CRUD | Lucas |
| Entregar o módulo Python isolado, com `pytest`, e depois a chamada pelo Java (`ProcessBuilder`) com endpoint temporário de teste | Kauê (Python) e Lucas (Java) |
| Trabalhar só com branches `feature/...` e Pull Requests revisados por pelo menos um colega; ninguém altera a estrutura base sem avisar o grupo | Toda a equipe |
| Mostrar o problema ao grupo no mesmo dia em que ele aparecer (conexão, conflito, ambiente), sem esperar o fim da sprint | Toda a equipe; Kauê acompanha como facilitador |
| Preparar o frontend conforme o Contrato, usando respostas simuladas até o backend ficar pronto | Leonardo |
| Alinhar as versões de JDK e Spring Boot entre `pom.xml` e `Arquitetura.md` | Lucas |
| Registrar a evolução em commits pequenos, para o histórico refletir o trabalho (US8) | Toda a equipe |
