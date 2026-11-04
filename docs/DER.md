# DER — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

> **Nota de revisão (27/10/2026, Sprint 3):** o modelo ganhou o **estoque por linhas de entrada** (`movimentacao_estoque.quantidade_restante` e `motivo`) e o **status do pedido** (`pedido.status`, `data_cancelamento`, `usuario_cancelamento_id`, para a US27, fora do semestre, A40). O seed oficial passa a ter **estoque 0** (os ingredientes recebem estoque só por movimentação); os dados de demonstração ficam em `seed_demo.sql`. A otimização passa a maximizar o **lucro estimado** (Adendo nº 1 ao Termo de Aceite, aprovado em 27/10). Essas mudanças estão no `Script_DDL.sql` de referência (`docs/Sprint_3/referencia/bd/`, validado com 32 + 41 testes no PostgreSQL 16) e entram em `bd/Script_DDL.sql` pelo PR do banco da Sprint 3; até esse PR, o `bd/` da `main` ainda é o da Sprint 1.

> **Nota de revisão (16/09/2026):** o banco de dados oficial do projeto passou de **MySQL** para **PostgreSQL**, motivado pela ferramenta de deploy escolhida (Render) — ver justificativa completa e as decisões de sintaxe (tipos, auto-incremento) na nota ao final deste documento. O Termo de Aceite e o `Script_DDL.sql` foram atualizados na mesma revisão.

---

## 1. Diagrama

```mermaid
erDiagram
    USUARIO {
        int id PK
        varchar nome
        varchar email UK
        varchar senha_hash
        enum perfil
        boolean ativo
    }

    CLIENTE {
        int id PK
        varchar nome
        varchar contato
        varchar endereco
    }

    UNIDADE_MEDIDA_PORCAO_PADRAO {
        varchar unidade_medida PK
        decimal porcao_padrao
    }

    INGREDIENTE {
        int id PK
        varchar nome
        varchar unidade_medida FK
        decimal custo_unitario
        decimal porcao_padrao
        decimal quantidade_estoque
        decimal quantidade_minima
    }

    PRODUTO {
        int id PK
        varchar nome
        decimal preco_venda
        boolean ativo
    }

    PRODUTO_INGREDIENTE {
        int produto_id PK,FK
        int ingrediente_id PK,FK
        decimal quantidade_utilizada
    }

    PEDIDO {
        int id PK
        int cliente_id FK
        int usuario_id FK
        timestamp data_pedido
        decimal valor_total
        varchar status
        timestamp data_cancelamento
        int usuario_cancelamento_id FK
    }

    PEDIDO_PRODUTO {
        int pedido_id PK,FK
        int produto_id PK,FK
        int quantidade
        decimal preco_unitario_registrado
    }

    MOVIMENTACAO_ESTOQUE {
        int id PK
        int ingrediente_id FK
        int usuario_id FK
        enum tipo
        decimal quantidade
        timestamp data_movimentacao
        varchar observacao
        date data_validade
        decimal quantidade_restante
        varchar motivo
    }

    %% RECOMENDACAO_PRODUCAO, ITEM_RECOMENDACAO_PRODUCAO, RECOMENDACAO_REPOSICAO,
    %% ITEM_RECOMENDACAO_REPOSICAO e SUGESTAO_MARKETING saíram do DER: os cálculos de
    %% produção, reposição e marketing (UC10, UC11, UC14, UC18) agora são feitos sob
    %% demanda pela aplicação, sem persistir histórico.

    USUARIO ||--o{ PEDIDO : registra
    USUARIO |o--o{ PEDIDO : cancela
    USUARIO ||--o{ MOVIMENTACAO_ESTOQUE : registra
    CLIENTE |o--o{ PEDIDO : realiza

    UNIDADE_MEDIDA_PORCAO_PADRAO ||--o{ INGREDIENTE : "sugere padrão para"
    INGREDIENTE ||--o{ MOVIMENTACAO_ESTOQUE : movimenta
    PRODUTO ||--|{ PRODUTO_INGREDIENTE : possui
    INGREDIENTE ||--o{ PRODUTO_INGREDIENTE : "utilizado em"

    PEDIDO ||--|{ PEDIDO_PRODUTO : possui
    PRODUTO ||--o{ PEDIDO_PRODUTO : "vendido em"
```

---

## 2. Dicionário de dados

### Tabela: `usuario`
Representa tanto o Administrador (Proprietário) quanto o Funcionário. Como as duas subclasses do diagrama de classes (E3a) não possuem atributos próprios, elas foram unificadas em uma única tabela com coluna discriminadora `perfil`.

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | INT | PK, GENERATED ALWAYS AS IDENTITY | Identificador do usuário |
| nome | VARCHAR(150) | NOT NULL | Nome do usuário |
| email | VARCHAR(150) | NOT NULL, UNIQUE | E-mail de login (único no sistema) |
| senha_hash | VARCHAR(255) | NOT NULL | Hash da senha (mínimo 8 caracteres na origem, validado na aplicação) |
| perfil | VARCHAR(20) | NOT NULL, CHECK (perfil IN ('ADMINISTRADOR','FUNCIONARIO')) | Perfil de acesso, define permissões e redirecionamento |
| ativo | BOOLEAN | NOT NULL, DEFAULT TRUE | Indica se o usuário está ativo no sistema |

### Tabela: `cliente`
| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | INT | PK, GENERATED ALWAYS AS IDENTITY | Identificador do cliente |
| nome | VARCHAR(150) | NOT NULL | Nome do cliente |
| contato | VARCHAR(50) | NOT NULL | Telefone ou contato do cliente |
| endereco | VARCHAR(255) | NOT NULL | Endereço do cliente |

### Tabela: `unidade_medida_porcao_padrao` **(nova — US26/UC20)**
Armazena, por unidade de medida, um valor de porção padrão usado apenas como **sugestão de pré-preenchimento** ao cadastrar um novo ingrediente daquela unidade (Tela 19 do protótipo navegável). Não substitui `ingrediente.porcao_padrao`, que continua sendo o valor individual e efetivo usado em todos os cálculos de negócio.

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| unidade_medida | VARCHAR(20) | PK | Unidade de medida (kg, l, un, mg, ml, etc.) |
| porcao_padrao | DECIMAL(10,3) | NOT NULL, CHECK (porcao_padrao > 0) | Valor sugerido ao cadastrar um novo ingrediente dessa unidade |

### Tabela: `ingrediente`
| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | INT | PK, GENERATED ALWAYS AS IDENTITY | Identificador do ingrediente |
| nome | VARCHAR(100) | NOT NULL | Nome do ingrediente |
| unidade_medida | VARCHAR(20) | NOT NULL, FK → unidade_medida_porcao_padrao(unidade_medida) | Unidade de medida (kg, l, un, etc.); deve existir previamente em `unidade_medida_porcao_padrao` |
| custo_unitario | DECIMAL(10,2) | NOT NULL, CHECK (custo_unitario >= 0) | Custo por unidade de medida |
| porcao_padrao | DECIMAL(10,3) | NOT NULL, CHECK (porcao_padrao > 0) | Porção padrão **efetiva** deste ingrediente, usada nos cálculos de negócio; pode divergir do valor sugerido em `unidade_medida_porcao_padrao` para a mesma unidade |
| quantidade_estoque | DECIMAL(10,3) | NOT NULL, DEFAULT 0, CHECK (quantidade_estoque >= 0) | Saldo atual em estoque (mantido pelas movimentações de entrada/saída) |
| quantidade_minima | DECIMAL(10,3) | NOT NULL, DEFAULT 0, CHECK (quantidade_minima >= 0) | Quantidade mínima antes de disparar alerta de reposição |

### Tabela: `produto`
| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | INT | PK, GENERATED ALWAYS AS IDENTITY | Identificador do produto |
| nome | VARCHAR(100) | NOT NULL | Nome do produto (copo de açaí) |
| preco_venda | DECIMAL(10,2) | NOT NULL, CHECK (preco_venda > 0) | Preço de venda atual, restrito ao Administrador |
| ativo | BOOLEAN | NOT NULL, DEFAULT TRUE | Indica se o produto está ativo para venda |

### Tabela: `produto_ingrediente`
Tabela associativa que resolve o relacionamento N:N entre `produto` e `ingrediente` (equivalente física da classe associativa `ComposicaoProduto` do diagrama de classes).

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| produto_id | INT | PK, FK → produto(id) | Produto composto |
| ingrediente_id | INT | PK, FK → ingrediente(id) | Ingrediente utilizado |
| quantidade_utilizada | DECIMAL(10,3) | NOT NULL, CHECK (quantidade_utilizada > 0) | Quantidade do ingrediente usada nesse produto |

### Tabela: `pedido`
| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | INT | PK, GENERATED ALWAYS AS IDENTITY | Identificador do pedido |
| cliente_id | INT | FK → cliente(id), NULL | Cliente associado (opcional) |
| usuario_id | INT | NOT NULL, FK → usuario(id) | Usuário (Administrador/Funcionário) que registrou o pedido |
| data_pedido | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data e hora do pedido. Na Sprint 3, o backend grava a hora de São Paulo (`:agora` vindo do Java, A9); o `DEFAULT` só vale para inserções diretas no banco |
| valor_total | DECIMAL(10,2) | NOT NULL, DEFAULT 0, CHECK (valor_total >= 0) | Valor total do pedido (mantido a partir dos itens) |
| status | VARCHAR(10) | NOT NULL, DEFAULT 'ATIVO', CHECK (status IN ('ATIVO','CANCELADO')) | Situação do pedido (Sprint 3). Pedido cancelado não entra no faturamento, nos mais vendidos nem no ranking do dashboard, mas continua contando no consumo da reposição (A18) |
| data_cancelamento | TIMESTAMP | NULL; `chk_pedido_cancelamento` | Quando foi cancelado. Preenchida **se e somente se** `status = 'CANCELADO'` |
| usuario_cancelamento_id | INT | NULL, FK → usuario(id); `chk_pedido_cancelamento` | Quem cancelou. Preenchido **se e somente se** `status = 'CANCELADO'`. O cancelamento (US27) **não devolve** ingredientes ao estoque |

### Tabela: `pedido_produto`
Tabela associativa que resolve o relacionamento N:N entre `pedido` e `produto` (equivalente física da classe associativa `ItemPedido`).

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| pedido_id | INT | PK, FK → pedido(id) | Pedido |
| produto_id | INT | PK, FK → produto(id) | Produto vendido |
| quantidade | INT | NOT NULL, CHECK (quantidade > 0) | Quantidade vendida do produto |
| preco_unitario_registrado | DECIMAL(10,2) | NOT NULL, CHECK (preco_unitario_registrado > 0) | Preço praticado no momento da venda |

### Tabela: `movimentacao_estoque`
| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | INT | PK, GENERATED ALWAYS AS IDENTITY | Identificador da movimentação |
| ingrediente_id | INT | NOT NULL, FK → ingrediente(id) | Ingrediente movimentado |
| usuario_id | INT | NOT NULL, FK → usuario(id) | Usuário que registrou a movimentação |
| tipo | VARCHAR(10) | NOT NULL, CHECK (tipo IN ('ENTRADA','SAIDA')) | Tipo de movimentação |
| quantidade | DECIMAL(10,3) | NOT NULL, CHECK (quantidade > 0) | Quantidade movimentada |
| data_movimentacao | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data e hora da movimentação |
| observacao | VARCHAR(255) | NULL | Observação livre sobre a movimentação |
| data_validade | DATE | NULL, CHECK (tipo = 'ENTRADA' OR data_validade IS NULL) | Data de validade da linha de estoque criada por essa entrada; preenchida somente em movimentações do tipo `ENTRADA`. Em movimentações do tipo `SAIDA` é sempre `NULL` (a constraint `chk_movimentacao_data_validade` impede preenchê-la nesse caso) |
| quantidade_restante | DECIMAL(10,3) | `chk_movimentacao_restante`: na `ENTRADA`, NOT NULL e entre 0 e `quantidade`; na `SAIDA`, NULL | **Sprint 3.** Quanto ainda resta da linha de estoque criada por essa entrada. Nasce igual a `quantidade` e diminui a cada baixa |
| motivo | VARCHAR(20) | `chk_movimentacao_motivo`: na `SAIDA`, NOT NULL e IN ('PEDIDO','CONVENCIONAL','DESCARTE_VENCIDO'); na `ENTRADA`, NULL | **Sprint 3.** Por que o estoque saiu. `PEDIDO` só o backend grava (baixa automática do pedido) |

> **Nota:** a `data_validade` é registrada por movimentação (não mais em `ingrediente`) porque cada `ENTRADA` de estoque pode ter uma validade diferente das entradas anteriores. Desde a Sprint 3, cada `ENTRADA` é uma **linha de estoque**, com `quantidade_restante`. Por isso o campo só faz sentido para `ENTRADA` — uma `SAIDA` apenas consome estoque já existente e não introduz uma validade nova. O UC16/US16 (alerta de vencimento) e o método `estaProximoDoVencimento` (ver `Diagramas_UML.md`) devem considerar apenas movimentações com `tipo = 'ENTRADA'` e `data_validade IS NOT NULL`.

> **Nota:** as tabelas `recomendacao_producao`, `item_recomendacao_producao`, `recomendacao_reposicao`, `item_recomendacao_reposicao` e `sugestao_marketing` saíram deste DER. As funcionalidades de recomendação de produção (UC10, UC11), recomendação de reposição (UC14) e sugestão de marketing (UC18) continuam no sistema, mas passaram a ser calculadas sob demanda pela aplicação — sem persistir o histórico de cálculo — por isso não têm mais tabela própria. A otimização de produção (UC10) e, desde a Sprint 3, a recomendação de reposição com orçamento (UC14) são resolvidas por **Programação Linear Inteira** (Python, biblioteca Gurobi); a produção maximiza o **lucro estimado** (Adendo nº 1). O resultado da produção é mantido em memória (RAM) do processo, não em banco de dados.

> **Nota (revista na Sprint 3, D10):** o estoque é controlado **por linha de entrada**. Cada `ENTRADA` cria uma linha com `quantidade_restante`. Toda baixa (pedido, saída convencional) consome as linhas **válidas** da menor `data_validade` para a maior, deixando as sem validade por último (FEFO — *First Expire, First Out*); **estoque vencido nunca é vendido**. O descarte (`DESCARTE_VENCIDO`) consome só linhas vencidas. Cada baixa grava uma `SAIDA` com o `motivo`, sem `data_validade` própria. **Invariante:** `ingrediente.quantidade_estoque` = soma de `quantidade_restante` das `ENTRADA` daquele ingrediente; o "saldo válido" (o que vai para a otimização) é a soma só das linhas não vencidas. Um índice parcial (`ix_movimentacao_linhas`) acelera a busca das linhas com saldo. Datas de validade são comparadas com o dia de São Paulo informado pelo Java (`:hoje`, A9).

> **Nota (US26/UC20 — porção padrão por unidade, adicionada nesta revisão):** `unidade_medida_porcao_padrao.porcao_padrao` é apenas o valor **sugerido** para pré-preencher o formulário de cadastro de um novo ingrediente daquela unidade. O valor efetivo usado nos cálculos de negócio (`Ingrediente.calcularQuantidadeRecomendadaCompra`, composição de produto, etc.) é sempre `ingrediente.porcao_padrao`, que pode divergir livremente do padrão da unidade — por exemplo, o Açaí pode ter uma porção diferente dos demais ingredientes medidos em `kg`. Alterar o padrão de uma unidade **não** recalcula retroativamente ingredientes já cadastrados; o novo valor só é usado como sugestão em cadastros futuros.

> **Nota (US26/UC20):** a FK `ingrediente.unidade_medida → unidade_medida_porcao_padrao.unidade_medida` torna obrigatório que a unidade já esteja cadastrada em `unidade_medida_porcao_padrao` antes de se cadastrar um ingrediente com essa unidade — na prática, a Tela 19 passa a ser um pré-requisito de fluxo da Tela 07 (Ingredientes: Novo/Editar). O `Script_DDL.sql` inclui um `INSERT` inicial (seed) com as cinco unidades da Tela 19 (`kg`, `mg`, `l`, `ml`, `un`) para não bloquear ambientes recém-criados a partir do schema atual — na Sprint 1 as unidades chegam ao banco somente por esse seed, pois a tela de gestão de unidades (US26) ficou fora das Sprints 1 a 3 (desde 28/10, condicional na Sprint 4, A40).

> **Nota de decisão técnica (16/09/2026) — migração de MySQL para PostgreSQL:** motivada pela escolha do Render como plataforma de deploy. O Render não oferece MySQL como banco gerenciado — só é possível rodá-lo via Docker (imagem oficial) com um disco persistente, o que exige um plano pago (o plano Free do Render não permite disco persistente nem "private services"). Já o PostgreSQL é um banco gerenciado nativo do Render, com um plano Free genuíno (a única limitação real: 1 GB de armazenamento e expiração a cada 30 dias, com 14 dias de carência para migrar para um plano pago antes da exclusão — a equipe pode recriar o banco Free periodicamente durante o semestre, ou migrar para o plano pago (a partir de US$ 6-7/mês) perto da entrega final, se preferir não depender disso). Essa mudança trouxe três adaptações de sintaxe, refletidas neste documento e no `Script_DDL.sql`:
> 1. **Colunas de auto-incremento:** `AUTO_INCREMENT` (MySQL) não existe em PostgreSQL. Optamos por `GENERATED ALWAYS AS IDENTITY`, a forma padrão SQL (compatível com o padrão SQL:2003) recomendada atualmente pela documentação do PostgreSQL, em vez do pseudo-tipo legado `SERIAL`.
> 2. **Colunas ENUM:** PostgreSQL não tem o `ENUM(...)` inline do MySQL — um tipo enumerado nativo exigiria `CREATE TYPE ... AS ENUM (...)` à parte, o que é mais rígido para alterar depois (exige `ALTER TYPE`, não pode ser feito dentro de uma transação em versões mais antigas). Optamos por `VARCHAR` + `CHECK (coluna IN (...))`, que tem o mesmo efeito de restringir os valores aceitos, mas é mais simples de alterar no futuro (basta um `ALTER TABLE ... DROP/ADD CONSTRAINT`). Afetou `usuario.perfil` e `movimentacao_estoque.tipo`.
> 3. **Datas com hora:** `DATETIME` (MySQL) vira `TIMESTAMP` em PostgreSQL — mapeamento direto, sem mudança de comportamento (nenhum dos dois grava fuso horário). Afetou `pedido.data_pedido` e `movimentacao_estoque.data_movimentacao`.
>
> Nenhuma dessas três é uma mudança de modelo — apenas de sintaxe física — mas fica registrada aqui porque `AUTO_INCREMENT`/`ENUM` apareciam explicitamente na coluna "Restrições" deste dicionário. **Pendências para a equipe:** (a) ~~confirmar a versão do PostgreSQL~~ **resolvida na Sprint 3: PostgreSQL 16** em todo lugar (Docker local, Testcontainers e `render.yaml`); (b) cada integrante precisa trocar o PostgreSQL local (em vez do MySQL) no ambiente de desenvolvimento — ver seção de ambiente no README.
>
> **Atualização (27/10/2026):** o banco gratuito do Render **expira 30 dias depois de criado** (e não "a cada 30 dias"), com 14 dias para migrar a um plano pago. Ele será criado em **02/11/2026** (A41, 28/10; antes: 31/10) e valerá até 02/12/2026, depois de 30/11, data-limite do professor, com backup e restauração testados (A21, A32).
