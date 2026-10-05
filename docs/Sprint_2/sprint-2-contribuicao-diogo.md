# Relatório Individual de Contribuição — Sprint 2 — Diogo Pereira Miranda (RA 2840482423006)


**Papel nesta sprint:** Responsável por dados (Banco de Dados, qualidade/consistência da documentação)

**Período coberto:** 19/09/2026 a 02/10/2026 — a partir do dia seguinte ao fim da Sprint 1. A Sprint 1 (`sprint-1-contribuicao-diogo.md`) cobriu a definição de arquitetura (`ARQUITETURA.md`, decisões D1–D5) e a primeira versão do roteiro de implementação; esta sprint é onde a implementação de banco de dados realmente aconteceu, junto com a evolução dos documentos de arquitetura e contrato.

---

## 1. O que fiz

| Item | Evidência (arquivo / documento) | Status |
|---|---|---|
| Corrigido o schema e o seed do banco para a Sprint 1: seed antigo tinha saldos de estoque que não batiam com as movimentações e usuários/pedidos que não deveriam existir ainda; novo seed fica só com 5 unidades de medida, 7 ingredientes, 3 produtos, 13 composições, 0 usuários | `docs/Script_DDL.sql` | Mergeado* |
| Escrito um script de 32 testes automatizados de constraint do banco (cada um roda em transação com `ROLLBACK`, confere a constraint exata que rejeita o dado inválido) | `docs/constraints_sprint1_teste.sql` | Mergeado*; 32/32 passando (validado no PostgreSQL 16, via Docker) |
| Criado o mapa de tradução de erros do banco → campo da API → mensagem → código HTTP, para o Lucas usar no tratamento de exceções | `docs/mapa_constraints.md` | Mergeado*; revisado 2 vezes (alinhamento ao Contrato de Comunicação; fechamento da decisão D-A) |
| Escrito o SQL pronto dos 18 métodos dos repositórios da Sprint 1 (`UsuarioRepository`, `UnidadeMedidaRepository`, `IngredienteRepository`, `ProdutoRepository`), incluindo a transação de produto+composição e a regra de negócio "todo produto precisa ter açaí" (com 15 casos de teste unitário cobrindo acentos, maiúsculas, nomes vazios etc.) | `docs/sql_repositorios_sprint1.md` | Mergeado*; revisado (tabela de rastreabilidade rota↔SQL; confirmação das decisões D-A e D-B) |
| Corrigida 1 inconsistência no DER (texto sobre as unidades de medida do seed) | `docs/DER.md` | Mergeado* |
| Conferida, nos arquivos Word **oficiais** da equipe, a aplicação das 3 correções propostas na Sprint 1 (item fora de escopo removido do Termo; referência cruzada da US19 do Backlog; local do registro de validade do ingrediente) — comparação campo a campo, incluindo as 26 histórias do Backlog uma a uma | Comparação entre os `.docx` reais e os documentos de referência da Sprint 1 | Confirmado — nenhuma divergência encontrada |
| Atualizada a `ARQUITETURA.md` (criada na Sprint 1) com o alinhamento ao Contrato de Comunicação do Kauê (seções 3.1 e 3.3 passam a apontar para o contrato) | `docs/ARQUITETURA.md` | Mergeado (PR aprovado) |
| Atualizado o Roteiro de implementação (criado como v2 na Sprint 1) para a v3: nomes e `status` do Python alinhados ao Contrato (`sucesso`/`erro`, campo `echo`), JSON em snake_case, e depois as versões de JDK/Spring Boot decididas pelo grupo | `Roteiro_Sprint1_Detalhado_PostgreSQL_v3.md` | Mergeado (PR aprovado) |
| Conduzida a decisão de versões com o time (JDK, Spring Boot, ferramenta de build, Python, licença do Gurobi), com pesquisa de compatibilidade real antes de cada recomendação, em um documento próprio para o grupo responder em conjunto | `docs/Pendencias_ARQUITETURA_para_o_grupo.md` | 10 decisões registradas e já refletidas na `ARQUITETURA.md` |
| Atualizada a arquitetura para refletir a mudança de licença do Gurobi (de WLS simples para **Compute Server**), com o alerta técnico sobre o limite de 1 job por vez da licença acadêmica e a pendência de onde esse servidor vai rodar | `docs/ARQUITETURA.md`, seção 3.3 | Documentado; decisão de hospedagem ainda em aberto com o Kauê |
| Revisão técnica do Contrato de Comunicação (documento do Kauê): identificadas 9 inconsistências, 6 lacunas e 4 pendências em aberto, com proposta de correção para cada uma | Revisão repassada ao Kauê | Aprovada por ele e incorporada ao documento |
| Revisão de um tutorial de implementação do backend preparado para o Lucas: encontrados e **confirmados por teste** 2 problemas reais — (1) uma variável de ambiente do Gurobi não chega ao subprocesso Python quando carregada só pelo mecanismo do Spring; (2) a validação de ingrediente repetido na composição do produto usa uma contagem que devolveria a mensagem de erro errada | Revisão repassada ao Lucas | Aguardando correção no código |

**Observação sobre as evidências (\*):** o repositório do projeto foi excluído e recriado pelo Kauê durante a sprint (a pedido do Lucas, por conflitos na comunicação Java↔Banco). **Confirmado: o histórico de commits e Pull Requests do repositório anterior não foi preservado** — por isso, os itens marcados com `*` acima não têm mais um número de PR ou nome de branch rastreável; a evidência que resta é o **conteúdo final dos arquivos**, já presente no repositório atual. A `ARQUITETURA.md` é a exceção: foi criada/commitada já depois da recriação, então o PR dela (branch `feature/arquitetura-e-pendencias`) continua válido e foi aprovado.

---

## 2. Rituais que participei

- [X] Dailies/weeklies (2 de 2)
- [ ] Sprint Review 
- [X] Retrospectiva 

---

## 3. PRs de colegas que revisei

| PR / documento | Autor | Comentário resumido |
|---|---|---|
| Contrato de Comunicação (documento, não um PR de código) | Kauê | Levantei 9 inconsistências com os demais documentos, 6 lacunas de exemplos/rotas e 4 pendências a confirmar com a equipe; todas aprovadas e incorporadas |
| Tutorial de implementação do backend (documento preparado para o Lucas, não um PR) | Lucas | Confirmei por teste 2 problemas reais: a credencial do Gurobi não chegaria ao Python via `ProcessBuilder`, e a checagem de ingrediente repetido na composição retornaria a mensagem de erro errada |


---

## 4. Dificuldades e o que aprendi


O ambiente Windows trouxe mais atrito do que eu esperava mesmo com os scripts já certos: diferenças entre CMD e PowerShell (aspas, variáveis, comandos de busca) e problemas de codificação com acentos no terminal exigiram documentar as duas sintaxes para não travar o andamento. Também reenviei por engano, em um momento, uma versão desatualizada de um documento de decisões — reforçou a importância de conferir o conteúdo de um arquivo antes de reenviá-lo, não só o nome. Por fim, a recriação do repositório no meio da sprint deixou uma dúvida real sobre o que do histórico de commits e PRs anteriores foi preservado, que ainda precisamos resolver como equipe.
