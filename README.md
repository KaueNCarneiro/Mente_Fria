# Mente Fria

Projeto acadêmico de apoio à gestão de pequenos comércios, com uma açaiteria como piloto. Centraliza ingredientes, produtos, estoque e pedidos e utiliza Programação Linear para recomendar quantidades de produção que maximizem a receita com os recursos disponíveis.

## Estado atual

O repositório contém a documentação e as pastas iniciais dos módulos. A aplicação ainda não está implementada nem possui comandos de execução validados. Os arquivos de configuração, schema e seed nas pastas de implementação ainda são placeholders.

## Tecnologias documentadas

| Responsabilidade | Tecnologia |
|---|---|
| Backend | Java + Spring Boot |
| Frontend | HTML, CSS e JavaScript |
| Banco de dados | PostgreSQL |
| Otimização | Python + Gurobi |
| Publicação prevista | GitHub Pages para frontend; Render para backend e banco |

Versões, ferramenta de build, licença Gurobi e integração Java–Python serão definidas antes da configuração dos módulos correspondentes. Ainda não há URL pública do sistema.

## Organização

- `backend/`: API e regras de negócio em Java.
- `frontend/`: interface web.
- `database/`: futuros scripts executáveis de estrutura e dados iniciais.
- `optimization/`: modelo matemático e integração Python/Gurobi.
- `docs/`: requisitos, modelos, backlog, aceite, roteiro e estratégia de testes.

## Documentação

Consulte [Contexto do projeto](docs/CONTEXTO_PROJETO.md) para o escopo, as fontes e as pendências de implementação. O [DDL de referência](docs/Script_DDL.sql) já contém criação de tabelas e dados de exemplo; sua presença não significa que o banco esteja configurado.

O desenvolvimento será incremental, mantendo rastreabilidade entre requisitos, banco, aplicação e testes. Decisões relevantes ainda abertas devem ser validadas com a equipe antes da implementação dependente.

## Execução e testes

As instruções reproduzíveis serão adicionadas à medida que os módulos forem implementados e verificados. A estratégia documentada prevê JUnit 5/Mockito, testes de integração com PostgreSQL real via Testcontainers e pytest para otimização, além de aceitação manual por história. Ainda não há suíte executável.

Nunca versionar credenciais ou arquivos `.env` com valores reais.

## Equipe e finalidade

Leonardo Fagundes Oliveira, Diogo Pereira Miranda, Lucas Gabriel Valadares Bassi e Kauê Nogueira Carneiro. Projeto da disciplina Laboratório de Engenharia de Software, ADS, Fatec Ribeirão Preto, 2026.
