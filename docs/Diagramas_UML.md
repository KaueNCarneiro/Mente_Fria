# Diagramas UML — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

> **Revisão de 27/10/2026 (Sprint 3, Adendo nº 1 ao Termo de Aceite aprovado):** a recomendação de produção maximiza o **lucro estimado** (`Produto.calcularLucroPorCopo()`); a reposição recebe período **e orçamento**; o estoque é controlado **por linha de entrada** (`MovimentacaoEstoque.quantidadeRestante` e `motivo`); o pedido ganhou `status` e os dados de cancelamento para a nova US27 (fora do semestre, A40). O diagrama de casos de uso ainda não tem o caso de uso da US27.

---

## 1. Diagrama de Casos de Uso

```mermaid
flowchart LR
    Administrador((Administrador
Proprietário))
    Funcionario((Funcionário))

    Administrador --> UC1[UC1 - Criar conta de Administrador]
    Administrador --> UC2[UC2 - Cadastrar funcionário]
    Administrador --> UC3[UC3 - Login por perfil]
    Funcionario --> UC3
    Administrador --> UC4[UC4 - Cadastrar ingrediente]
    Administrador --> UC5[UC5 - Cadastrar produto e composição]
    Administrador --> UC6[UC6 - Cadastrar/atualizar preço de produto]
    Administrador --> UC9[UC9 - Registrar movimentação de estoque]
    Funcionario --> UC9
    Administrador --> UC10[UC10 - Calcular quantidade ótima de produção]
    Administrador --> UC11[UC11 - Visualizar recomendação de vendas]
    Administrador --> UC12[UC12 - Registrar pedido]
    Funcionario --> UC12
    Administrador --> UC13[UC13 - Cadastrar cliente]
    Funcionario --> UC13
    Administrador --> UC14[UC14 - Gerar recomendação de reposição]
    Administrador --> UC15[UC15 - Visualizar alertas de reposição de ingredientes]
    Funcionario --> UC15
    Administrador --> UC16[UC16 - Visualizar alertas de validade de ingredientes]
    Funcionario --> UC16
    Administrador --> UC17[UC17 - Visualizar dashboard gerencial]
    Administrador --> UC18[UC18 - Gerar sugestões de marketing]
    Administrador --> UC19[UC19 - Exportar dashboard em CSV]
    Administrador --> UC20[UC20 - Definir porção padrão por unidade de medida]
    UC7[UC7 - Validar dados cadastrados]
    UC1 -.->|<<include>>| UC7
    UC2 -.->|<<include>>| UC7
    UC4 -.->|<<include>>| UC7
    UC5 -.->|<<include>>| UC7
    UC6 -.->|<<include>>| UC7
    UC9 -.->|<<include>>| UC7
    UC12 -.->|<<include>>| UC7
    UC13 -.->|<<include>>| UC7
    UC20 -.->|<<include>>| UC7
    
```

> **Observação:** US8 (repositório Git com README) e US20 (deploy público por URL) são requisitos não-funcionais/de entrega do projeto (cobertos no Documento de Visão, seção 6, e no Termo de Aceite, seção 2), não funcionalidades acionadas por um ator dentro do sistema — por isso não aparecem como casos de uso. As histórias US21 a US25 estão marcadas como **Won't** no Backlog Priorizado (fora do escopo do semestre) e, portanto, também não têm caso de uso correspondente.
>
> **Observação (adicionada nesta revisão):** UC20 corresponde a **US26 — Definir porção padrão por unidade de medida**, história derivada da Tela 19 do protótipo navegável, **já incluída no `Backlog_Priorizado`** (item 26, prioridade Could, Sprint 3), o que completa a rastreabilidade Backlog ↔ Caso de Uso. Assim como as demais histórias de cadastro, UC20 inclui (`<<include>>`) UC7 porque a validação `porcao_padrao > 0` é uma regra transversal.

---

## 2. Diagrama de Classes

```mermaid

    classDiagram

    class Usuario {
        <<abstract>>
        -id: int
        -nome: string
        -email: string
        -senhaHash: string
        -ativo: boolean
    }

    class Administrador {
    }

    class Funcionario {
    }

    class Cliente {
        -id: int
        -nome: string
        -contato: string
        -endereco: string
    }

    class UnidadeMedidaPorcaoPadrao {
        -unidadeMedida: string
        -porcaoPadrao: decimal
    }

    class Ingrediente {
        -id: int
        -nome: string
        -unidadeMedida: string
        -custoUnitario: decimal
        -porcaoPadrao: decimal
        -quantidadeEstoque: decimal
        -quantidadeMinima: decimal
        +precisaReposicao() boolean
        +calcularQuantidadeRecomendadaCompra(periodoDias, orcamento) decimal
    }

    class Produto {
        -id: int
        -nome: string
        -precoVenda: decimal
        -ativo: boolean
        +calcularCustoProducao() decimal
        +calcularLucroPorCopo() decimal
        +calcularQuantidadeOtimaProducao() int
        +gerarSugestaoMarketing() string
    }

    class ComposicaoProduto {
        -quantidadeUtilizada: decimal
    }

    class Pedido {
        -id: int
        -dataPedido: datetime
        -valorTotal: decimal
        -status: StatusPedido
        -dataCancelamento: datetime
        +cancelar(usuario)
    }

    class ItemPedido {
        -quantidade: int
        -precoUnitarioRegistrado: decimal
    }

    class MovimentacaoEstoque {
        -id: int
        -tipo: TipoMovimentacao
        -quantidade: decimal
        -dataMovimentacao: datetime
        -observacao: string
        -dataValidade: date
        -quantidadeRestante: decimal
        -motivo: MotivoSaida
        +estaProximoDoVencimento boolean
        +estaVencida boolean
    }
    %% Sprint 3: cada ENTRADA é uma linha de estoque; quantidadeRestante diminui a cada baixa (só na ENTRADA).
    %% motivo só existe na SAIDA: PEDIDO, CONVENCIONAL ou DESCARTE_VENCIDO. Linha vencida nunca é vendida.
    %% StatusPedido: ATIVO ou CANCELADO. cancelar() é a US27 (fora do semestre, A40) e NÃO devolve estoque.
    %% dataValidade só é preenchida quando tipo = ENTRADA (equivalente conceitual da
    %% CHECK chk_movimentacao_data_validade no DER); em SAIDA permanece null.
    %% estaProximoDoVencimento só se aplica a instâncias com tipo = ENTRADA e dataValidade != null.

    %% Os cálculos de recomendação de produção, reposição e sugestão de marketing (UC10, UC11, UC14, UC18)
    %% passaram a ser feitos sob demanda pelos métodos de Ingrediente e Produto acima — sem persistir
    %% histórico de cálculo, por isso não existem mais como classes/tabelas próprias.
    %% calcularQuantidadeOtimaProducao() maximiza o LUCRO estimado (Adendo nº 1): lucro por copo = preço de venda
    %% menos o custo dos ingredientes da composição (calcularLucroPorCopo). Resolve via Programação Linear Inteira pura
    %% (Python, biblioteca Gurobi): as variáveis de decisão — quantidade de copos de cada produto — são todas
    %% inteiras, com função objetivo e restrições lineares. O resultado fica em memória (RAM) do processo,
    %% sem histórico salvo.

    %% Herança dos usuários

    Usuario <|-- Administrador
    Usuario <|-- Funcionario

    %% Relações

    Usuario "1" -- "N" Pedido : registra
    Usuario "0..1" -- "N" Pedido : cancela
    Usuario "1" -- "N" MovimentacaoEstoque : registra

    UnidadeMedidaPorcaoPadrao "1" --> "N" Ingrediente : sugere padrão para
    Ingrediente "1" -- "N" MovimentacaoEstoque : movimenta

    Produto "1" -- "N" ComposicaoProduto : possui
    Ingrediente "1" -- "N" ComposicaoProduto : utilizado em

    Cliente "0..1" --> "0..N" Pedido : realiza
    Pedido "1" -- "N" ItemPedido : possui
    Produto "1" -- "N" ItemPedido : vendido em

```

`ComposicaoProduto` e `ItemPedido` representam, no nível conceitual, as classes associativas que resolvem os relacionamentos N:N entre Produto/Ingrediente e Pedido/Produto citados no Documento de Visão (seção 6). No DER (nível físico, `docs/DER.md`) esses relacionamentos são implementados como tabelas associativas (`produto_ingrediente` e `pedido_produto`).

As funcionalidades de recomendação de produção (UC10, UC11), recomendação de reposição (UC14) e sugestão de marketing (UC18) continuam no sistema, mas passaram a ser calculadas sob demanda — via `Produto.calcularQuantidadeOtimaProducao()`, `Produto.gerarSugestaoMarketing()` e `Ingrediente.calcularQuantidadeRecomendadaCompra(periodoDias)` — sem persistir o histórico de cálculo. Por isso `RecomendacaoProducao`, `ItemRecomendacaoProducao`, `RecomendacaoReposicao`, `ItemRecomendacaoReposicao` e `SugestaoMarketing` saíram do diagrama e não têm mais tabela correspondente no DER (`docs/DER.md`).

O atributo `dataValidade`, antes em `Ingrediente`, passou para `MovimentacaoEstoque`, pois cada `ENTRADA` de estoque pode ter uma validade diferente das entradas anteriores do mesmo ingrediente. **Desde a Sprint 3, cada `ENTRADA` é uma linha de estoque**, com `quantidadeRestante`; as baixas consomem as linhas válidas da validade mais próxima para a mais distante, e uma linha vencida nunca é vendida. Por isso `dataValidade` só é preenchida quando `tipo = ENTRADA`; em uma `SAIDA` ela permanece nula, já que a saída apenas consome estoque já existente. Essa regra é garantida no nível físico pela constraint `chk_movimentacao_data_validade` do DER (`docs/DER.md`). Consequentemente, `estaProximoDoVencimento` e os UC16/US16 (alerta de vencimento) devem consultar apenas movimentações do tipo `ENTRADA` com `dataValidade` preenchida.

`UnidadeMedidaPorcaoPadrao` (nova nesta revisão, US26/UC20) mantém um valor de `porcaoPadrao` por `unidadeMedida`, usado somente como **sugestão de pré-preenchimento** ao cadastrar um novo `Ingrediente` daquela unidade. O valor efetivo, usado em todos os cálculos de negócio (`calcularQuantidadeRecomendadaCompra`, composição de produto etc.), é sempre `Ingrediente.porcaoPadrao` — que pode divergir do padrão da unidade (ex.: Açaí com porção diferente dos demais ingredientes em `kg`). No DER, essa relação é implementada como uma FK obrigatória de `ingrediente.unidade_medida` para `unidade_medida_porcao_padrao.unidade_medida`.

---

## 3. Rastreabilidade — caso de uso → história do backlog

| Caso de uso | História(s) relacionada(s) (E2) |
|---|---|
| UC1 — Criar conta de Administrador | US1 |
| UC2 — Cadastrar funcionário | US2 |
| UC3 — Login por perfil | US3 |
| UC4 — Cadastrar ingrediente | US4 |
| UC5 — Cadastrar produto e composição | US5 |
| UC6 — Cadastrar/atualizar preço de produto | US6 |
| UC7 — Validar dados cadastrados | US7 (US7 é uma história transversal e seu comportamento é incluído (<<include>>) pelos casos de uso que realizam cadastro ou alteração de dados.) |
| UC9 — Registrar movimentação de estoque | US9 |
| UC10 — Calcular quantidade ótima de produção | US10 |
| UC11 — Visualizar recomendação de vendas | US11 |
| UC12 — Registrar pedido | US12 |
| UC13 — Cadastrar cliente | US13 |
| UC14 — Gerar recomendação de reposição | US14 |
| UC15 — Visualizar alertas de reposição de ingredientes | US15 |
| UC16 — Visualizar alertas de validade de ingredientes | US16 |
| UC17 — Visualizar dashboard gerencial | US17 |
| UC18 — Gerar sugestões de marketing | US18 |
| UC19 — Exportar dashboard em CSV | US19 |
| UC20 — Definir porção padrão por unidade de medida | US26 *(item 26 do `Backlog_Priorizado` — Could; fora da Sprint 3, Adendo nº 1; desde 28/10, condicional na Sprint 4, A40)* |
| UC21 — Cancelar pedido *(ainda não desenhado no diagrama de casos de uso)* | US27 *(história nova do Adendo nº 1 — Could, fora do semestre, A40)* |
