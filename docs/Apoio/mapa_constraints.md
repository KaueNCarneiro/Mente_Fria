# Mapa de constraints → mensagens (Sprints 1 e 3)

> **Atualizado em 27/10/2026 (Sprint 3):** acrescentadas as constraints de `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque` (estoque por linhas, `motivo`, status do pedido), com os testes T33–T73 de `constraints_sprint3_teste.sql`. Essas constraints existem no `Script_DDL.sql` de referência (`docs/Sprint_3/referencia/bd/`) e entram em `bd/` pelo PR do banco da Sprint 3; até lá, o `bd/Script_DDL.sql` da `main` ainda é o da Sprint 1.

**Uso:** referência para o backend traduzir violações do banco em mensagens claras por campo (US7) e para o frontend exibi-las.
As colunas **Campo na API** e **HTTP** ligam este mapa ao *Contrato de Comunicação* (§2): a chave de `campos` usa os mesmos nomes de campo do contrato (snake_case), e os códigos HTTP seguem o Contrato de Comunicação (§2), aprovado pelo Kauê em 20/09/2026.
Cada linha tem um teste correspondente: T01–T32 em `bd/constraints_teste.sql` (antes chamado `constraints_sprint1_teste.sql`) e T33–T73 em `constraints_sprint3_teste.sql` (Sprint 3; hoje em `docs/Sprint_3/referencia/bd/`). Todos validados no PostgreSQL 16.

## Regras de convivência entre aplicação e banco

1. **A aplicação valida primeiro** e responde `400` com a mensagem do campo (CT18, CT19). O banco é a **rede de segurança**: se algo passar, ele rejeita.
2. **Se o banco rejeitar, o backend traduz** a constraint para a mensagem abaixo. Nunca mostrar ao usuário o texto cru do PostgreSQL.
3. **Como identificar a violação:** o código SQLSTATE indica o tipo, e o driver informa qual constraint (ou coluna) foi violada.

   | SQLSTATE | Tipo | O que o driver informa |
   |---|---|---|
   | `23502` | `NOT NULL` | nome da **coluna** (não existe nome de constraint) |
   | `23503` | `FOREIGN KEY` | nome da constraint |
   | `23505` | `UNIQUE` / `PRIMARY KEY` | nome da constraint |
   | `23514` | `CHECK` | nome da constraint |

   No Spring, a violação chega como `DataIntegrityViolationException`; a causa é a `PSQLException` do driver, e `getServerErrorMessage()` dá acesso a `getConstraint()` e `getColumn()`. Por isso **todas as constraints estão nomeadas** no `Script_DDL.sql` — não renomear sem atualizar esta tabela.

## Tabela de tradução

| Tabela | Constraint / coluna | Campo na tela | Campo na API (`campos`) | Mensagem sugerida | HTTP | Teste |
|---|---|---|---|---|---|---|
| `usuario` | `uq_usuario_email` | e-mail | `email` | Este e-mail já está cadastrado. | 409 | T02 (CT03) |
| `usuario` | `chk_usuario_perfil` | perfil | `perfil` | Perfil de acesso inválido. | 400 | T03 |
| `usuario` | NOT NULL `nome` | nome | `nome` | Informe o nome. | 400 | T04 |
| `usuario` | NOT NULL `email` | e-mail | `email` | Informe o e-mail. | 400 | T05 |
| `usuario` | NOT NULL `senha_hash` | senha | `senha` | Informe a senha. | 400 | T06 |
| `ingrediente` | NOT NULL `nome` | nome | `nome` | Informe o nome do ingrediente. | 400 | T15 |
| `ingrediente` | NOT NULL `unidade_medida` | unidade de medida | `unidade_medida` | Selecione a unidade de medida. | 400 | T16 |
| `ingrediente` | NOT NULL `custo_unitario` | custo | `custo_unitario` | Informe o custo. | 400 | T17 |
| `ingrediente` | NOT NULL `porcao_padrao` | porção | `porcao_padrao` | Informe a porção. | 400 | T18 |
| `ingrediente` | `chk_ingrediente_custo_unitario` | custo | `custo_unitario` | O custo não pode ser negativo. | 400 | T09 (CT10) |
| `ingrediente` | `chk_ingrediente_porcao_padrao` | porção | `porcao_padrao` | A porção deve ser maior que zero. | 400 | T10 (CT11) |
| `ingrediente` | `chk_ingrediente_quantidade_minima` | quantidade mínima | `quantidade_minima` | A quantidade mínima não pode ser negativa. | 400 | T11 |
| `ingrediente` | `chk_ingrediente_quantidade_estoque` | estoque | `quantidade_estoque` | O estoque não pode ser negativo. | 400 | T12 |
| `ingrediente` | `fk_ingrediente_unidade_medida` | unidade de medida | `unidade_medida` | Selecione uma unidade de medida cadastrada. | 400 | T13, T14 |
| `produto` | NOT NULL `nome` | nome | `nome` | Informe o nome do produto. | 400 | T23 |
| `produto` | NOT NULL `preco_venda` | preço | `preco_venda` | Informe o preço de venda. | 400 | T22 |
| `produto` | `chk_produto_preco_venda` | preço | `preco_venda` | O preço deve ser maior que zero. | 400 | T20, T21, T24 (CT17) |
| `produto_ingrediente` | `chk_produto_ingrediente_quantidade` | quantidade utilizada | `composicao` | A quantidade utilizada deve ser maior que zero. | 400 | T26, T27 (CT14) |
| `produto_ingrediente` | `produto_ingrediente_pkey` | composição | `composicao` | Este ingrediente já foi adicionado ao produto. | 400 | T28 |
| `produto_ingrediente` | `fk_produto_ingrediente_ingrediente` | composição | `composicao` | Ingrediente não encontrado. | 400 | T29 |
| `produto_ingrediente` | `fk_produto_ingrediente_produto` | composição | `composicao` | Produto não encontrado. | 400 | T30 |
| `unidade_medida_porcao_padrao` | `chk_unidade_porcao_padrao` | (só seed na Sprint 1) | — | — | — | T31 |
| `unidade_medida_porcao_padrao` | `unidade_medida_porcao_padrao_pkey` | (só seed na Sprint 1) | — | — | — | T32 |
| `cliente` | NOT NULL `nome`, `contato`, `endereco` | nome, contato, endereço | `nome`, `contato`, `endereco` | Informe o nome. / Informe o contato. / Informe o endereço. | 400 | T34–T36 |
| `pedido` | `fk_pedido_cliente` | cliente | `cliente_id` | Cliente não encontrado. | 400 | T40 |
| `pedido` | NOT NULL `usuario_id`, `fk_pedido_usuario`, `chk_pedido_valor_total`, `chk_pedido_status`, `chk_pedido_cancelamento`, `fk_pedido_usuario_cancelamento` | — | — | (erro interno: quem preenche é o backend) | 500 | T38, T39, T41–T46 |
| `pedido_produto` | `chk_pedido_produto_quantidade` | quantidade | `itens` | A quantidade deve ser maior que zero. | 400 | T48 |
| `pedido_produto` | `pedido_produto_pkey` | produto | `itens` | Este produto já foi adicionado ao pedido. | 400 | T50 |
| `pedido_produto` | `fk_pedido_produto_produto` | produto | `itens` | Produto não encontrado. | 400 | T51 |
| `pedido_produto` | `chk_pedido_produto_preco`, `fk_pedido_produto_pedido` | — | — | (erro interno: o preço vem do banco) | 500 | T49, T52 |
| `movimentacao_estoque` | `chk_movimentacao_tipo`, NOT NULL `tipo` | tipo | `tipo` | Tipo de movimentação inválido. | 400 | T66, T70 |
| `movimentacao_estoque` | `chk_movimentacao_quantidade` | quantidade | `quantidade` | A quantidade deve ser maior que zero. | 400 | T67 (CT26) |
| `movimentacao_estoque` | `chk_movimentacao_data_validade` | validade | `data_validade` | A validade só pode ser informada em entradas. | 400 | T65 |
| `movimentacao_estoque` | `chk_movimentacao_motivo` | motivo | `motivo` | Informe o motivo da saída. / Motivo de saída inválido. / O motivo só pode ser informado em saídas. | 400 | T58, T62, T63 |
| `movimentacao_estoque` | `fk_movimentacao_ingrediente` | ingrediente | `ingrediente_id` | Ingrediente não encontrado. | 400 | T68 |
| `movimentacao_estoque` | `chk_movimentacao_restante`, `fk_movimentacao_usuario` | — | — | (erro interno: quem preenche é o backend) | 500 | T55–T57, T64, T69 |
| `ingrediente` | `chk_ingrediente_quantidade_estoque` (numa baixa) | — | — | Estoque insuficiente para o ingrediente <nome> / para o produto <nome>. | **422** | T71 (CT21, CT25) |
| `unidade_medida_porcao_padrao` | `chk_unidade_porcao_padrao` (na alteração, US26) | porção padrão | `porcao_padrao` | A porção deve ser maior que zero. | 400 | T73 (US26: fora da Sprint 3; condicional na Sprint 4, A40) |

## Como a API devolve isso (definido no Contrato de Comunicação, §2)

Formato de erro com **um ou mais campos** (a US7 pede mensagem específica por campo, e um formulário pode ter vários erros ao mesmo tempo):

```json
{
  "mensagem": "Dados inválidos",
  "campos": {
    "custo_unitario": "O custo não pode ser negativo.",
    "porcao_padrao": "A porção deve ser maior que zero."
  }
}
```

Erros que não pertencem a um campo (ex.: estoque insuficiente) trazem só `mensagem`. Códigos HTTP:

| Código | Quando |
|---|---|
| `400` | Validação de campo ou de regra de cadastro (inclui tudo que o banco rejeita por `NOT NULL`, `CHECK` e `FK`, o item repetido na composição e o produto sem açaí) |
| `401` | Sem token, token inválido ou expirado |
| `403` | Perfil sem permissão (ex.: Funcionário tentando alterar preço) |
| `404` | Recurso inexistente |
| `409` | Conflito de unicidade: e-mail já cadastrado (`uq_usuario_email`) e cadastro de Administrador quando já existe um |
| `405` | Método não suportado na rota (Sprint 3) |
| `422` | Regra de negócio de uma operação com dados válidos (ex.: estoque insuficiente) ou erro de dados vindo do Python |
| `500` | Falha técnica, inclusive violação de constraint que o backend deveria ter evitado (as marcadas "erro interno" acima); sem detalhe interno na resposta |
| `503` | Otimização desligada no servidor (plano B1 da licença, A31) |

---

## Regras que o banco **não** garante (ficam na aplicação)

- **Senha com no mínimo 8 caracteres** (US1/CT02): o banco só vê o hash.
- **Todo produto deve ter o açaí entre os ingredientes** (US5/CT15): decidido em 19/09/2026 — o nome do ingrediente, sem acentos e sem diferenciar maiúsculas, contém "acai" (ver `docs/Apoio/sql_repositorios.md`, seção 4).
- **Estoque por linhas (Sprint 3, D10):** a baixa consome as linhas **válidas** da validade mais próxima para a mais distante; **estoque vencido nunca é vendido**; o descarte consome só linhas vencidas. O banco garante `quantidade_restante` entre 0 e a quantidade, mas a escolha das linhas e a invariante "saldo = soma dos restantes" são da aplicação.
- **O motivo `PEDIDO`** só o backend grava: vindo do cliente, é `400` ("Motivo de saída inválido.").
- **Datas** de pedido e de movimentação vêm do relógio do Java em `America/Sao_Paulo` (A9), não do `DEFAULT CURRENT_TIMESTAMP` do banco (que roda em UTC no Render).
- **E-mail sem diferenciar maiúsculas:** o `UNIQUE` do PostgreSQL trata `Dono@Acai.com` e `dono@acai.com` como e-mails diferentes (verificado). **Proposta:** o backend grava e consulta o e-mail já normalizado (sem espaços nas pontas, em minúsculas).

## Necessidade que a FK cria para o backend e o frontend

`ingrediente.unidade_medida` só aceita unidades já cadastradas em `unidade_medida_porcao_padrao`, e a tela de gestão de unidades (US26) ficou **fora da Sprint 3** (Adendo nº 1); desde 28/10, ela só entra na Sprint 4 se passar no *gate* de seg 02/11 (A40). Na Sprint 1, o campo "unidade de medida" da tela de ingredientes deve ser uma **lista de seleção alimentada pelo banco** (nunca texto livre), o que exige um endpoint de leitura. Consulta sugerida:

```sql
SELECT unidade_medida, porcao_padrao
FROM unidade_medida_porcao_padrao
ORDER BY unidade_medida;
```

As unidades do seed são `kg`, `mg`, `l`, `ml` e `un` (minúsculas: a FK diferencia maiúsculas). A tela pode exibir "L" sem problema, desde que envie `l`.

## Fora deste mapa

Nada das tabelas atuais. As constraints de `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque` entraram em 27/10/2026 (Sprint 3). O cadastro de clientes (US13) e o cancelamento de pedido (US27) estão fora do semestre (A40), mas suas constraints já existem no schema.
