# Registro de análise — Sprint 2 — Mente Fria

> **Não houve reunião de retrospectiva da Sprint 2**, conforme confirmação da equipe. Este documento reúne fatos, dificuldades e propostas para discussão futura; não constitui ata de reunião nem registra ações aprovadas.

**Corte da análise:** 02/10/2026, horário de Brasília.
**Data da retrospectiva:** não realizada.
**Participantes da retrospectiva:** não se aplica.
**Base:** repositório `KaueNCarneiro/Mente_Fria`, branch `main`, commit `d562fe71f34948951ac9f772ef565e4683eacbe3`; documentos disponíveis nesse corte e informações confirmadas pela equipe.

## 1. Ações da retrospectiva anterior — situação observada

As ações abaixo constam do registro da Sprint 1 consultado na revisão. Sua publicação no repositório em 03/10/2026 fica fora do corte desta entrega; ele é usado como referência para acompanhamento, sem comprovar quando cada ação foi deliberada. Como não houve retrospectiva da Sprint 2, os estados representam a análise das evidências, não uma avaliação aprovada em reunião.

| Ação anterior | Situação observada até 02/10 | Evidência/comentário |
|---|---|---|
| Reorganizar o repositório em `backend`, `frontend`, `optimization` e `docs` | Estrutura presente | Recriação em 27/09/2026, confirmada pela equipe; quatro pastas e `.gitignore` presentes no corte |
| Conectar Java ao PostgreSQL antes do CRUD | Implementado; funcionamento relatado pela equipe | `GET /api/saude` executa `SELECT 1`; `GET /api/ingredientes` consulta o banco e retorna `id`, `nome` e `unidade_medida`; sem nova execução de integração nesta revisão |
| Entregar Python isolado com testes e depois integrá-lo ao Java | Parcial | Módulo Python e oito testes presentes; execução aprovada em 30/09 no PC do Kauê, usando WLS, confirmada pela equipe; chamada pelo Java ausente |
| Preparar frontend conforme o contrato | Implementado na interface; integração pendente | Cadastro do administrador, login, funcionários, ingredientes, produtos/composição e alteração de preço; testes de navegador com respostas simuladas; PR #2 integrada em 02/10 |
| Alinhar JDK e Spring Boot entre código e arquitetura | Concluído | `backend/pom.xml`: Java 25 e Spring Boot 4.0.8 |
| Usar branches, PRs revisadas e commits pequenos | Evidência parcial | PR #1 (Python) e PR #2 (frontend) integradas; a existência de merges não comprova a revisão por um colega nem a aplicação da prática em todo o trabalho |
| Comunicar impedimentos no mesmo dia | Concluído | Equipe está mais organizada |

## 2. O que funcionou bem — fatos e pontos para discussão

- **Separação por camada:** banco (Diogo), Java ↔ banco (Lucas), frontend (Leonardo) e Python (Kauê), com artefatos identificáveis no repositório. A integração completa permanece pendente.
- **Módulo Python:** recebe JSON por `stdin`, devolve JSON por `stdout` e contém oito testes de subprocesso/otimização. A equipe confirmou que passaram em 30/09/2026 no PC do Kauê com licença WLS.
- **Frontend:** já contém telas, chamadas conforme o contrato, validações locais e tratamento de falhas.
- **Java ↔ banco:** endpoints de saúde e consulta de ingredientes implementados, com funcionamento relatado pela equipe.
- **Organização do Git:** estrutura por camada e merges das PRs #1 e #2 disponíveis no repositório recriado.
- **Decisões de 25/09:** arquitetura registra Maven, WLS, conversão de porções no frontend e exclusão de ingredientes vencidos do estoque enviado à otimização. Registro de decisão não equivale a implementação de todos esses itens.

## 3. Dificuldades e pendências

- **Recriação do repositório em 27/09/2026:** conflitos de estrutura e comunicação Java ↔ banco foram relatados como contexto. A exclusão comprometeu a rastreabilidade do trabalho anterior; a extensão da perda de commits e PRs depende das evidências preservadas.
- **Java → Python pendente:** não há chamada por subprocesso nem endpoint de recomendação no código Java do corte. O fluxo completo BD → Java → Python → Frontend não foi concluído.
- **Licença concentrada no PC do Kauê:** os testes com WLS foram confirmados nessa máquina. A reprodução pelos demais integrantes continua pendente. O script cria o ambiente Gurobi, mas não lê explicitamente `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID`; é necessário documentar a configuração efetivamente utilizada, sem divulgar credenciais.
- **CRUD parcialmente implementado no frontend:** cadastro/login e telas de funcionários, ingredientes, produtos e preço estão presentes. Backend de autenticação e escrita e validação integrada continuam pendentes; as histórias correspondentes não estão concluídas de ponta a ponta.
- **Ausência de retrospectiva:** não houve reunião para avaliar o processo e aprovar melhorias para a próxima sprint.

## 4. Propostas para a próxima sprint — não deliberadas

| Proposta | Responsável a definir |
|---|---|
| Realizar uma discussão das dificuldades e registrar ações, responsáveis e prazos | Equipe |
| Resolver conflitos por branches e PRs; avaliar reversões e preservar cópias/histórico antes de qualquer recriação | Equipe |
| Documentar e validar o ambiente Gurobi/WLS em outra máquina, sem expor credenciais; definir a configuração para desenvolvimento e deploy | A definir; sugestão: Kauê |
| Implementar a integração Java → Python conforme o contrato; avaliar o dublê previsto na arquitetura como etapa de desenvolvimento | A definir; sugestão: Lucas |
| Implementar autenticação, autorização e endpoints de cadastro/edição; integrar e validar as telas existentes com PostgreSQL real | A definir; sugestão: Lucas e Leonardo |
| Alinhar versões e atualizar instruções de execução conforme a decisão da equipe | A definir; sugestão: Lucas e Kauê |
| Reestimar Sprints 3 e 4, considerando o frontend existente e as entregas de backend e integração pendentes | Equipe |

**Deliberações:** nenhuma registrada, pois não houve retrospectiva da Sprint 2. As propostas não representam compromissos assumidos pelos responsáveis sugeridos.
