# Mapa de constraints → mensagens (Sprint 1)

**Uso:** referência para o backend traduzir violações do banco em mensagens claras por campo (US7) e para o frontend exibi-las.
Cada linha tem um teste correspondente em `constraints_sprint1_teste.sql` (validado no PostgreSQL 16).

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

| Tabela | Constraint / coluna | Campo na tela | Mensagem sugerida | Teste |
|---|---|---|---|---|
| `usuario` | `uq_usuario_email` | e-mail | Este e-mail já está cadastrado. | T02 (CT03) |
| `usuario` | `chk_usuario_perfil` | perfil | Perfil de acesso inválido. | T03 |
| `usuario` | NOT NULL `nome` | nome | Informe o nome. | T04 |
| `usuario` | NOT NULL `email` | e-mail | Informe o e-mail. | T05 |
| `usuario` | NOT NULL `senha_hash` | senha | Informe a senha. | T06 |
| `ingrediente` | NOT NULL `nome` | nome | Informe o nome do ingrediente. | T15 |
| `ingrediente` | NOT NULL `unidade_medida` | unidade de medida | Selecione a unidade de medida. | T16 |
| `ingrediente` | NOT NULL `custo_unitario` | custo | Informe o custo. | T17 |
| `ingrediente` | NOT NULL `porcao_padrao` | porção | Informe a porção. | T18 |
| `ingrediente` | `chk_ingrediente_custo_unitario` | custo | O custo não pode ser negativo. | T09 (CT10) |
| `ingrediente` | `chk_ingrediente_porcao_padrao` | porção | A porção deve ser maior que zero. | T10 (CT11) |
| `ingrediente` | `chk_ingrediente_quantidade_minima` | quantidade mínima | A quantidade mínima não pode ser negativa. | T11 |
| `ingrediente` | `chk_ingrediente_quantidade_estoque` | estoque | O estoque não pode ser negativo. | T12 |
| `ingrediente` | `fk_ingrediente_unidade_medida` | unidade de medida | Selecione uma unidade de medida cadastrada. | T13, T14 |
| `produto` | NOT NULL `nome` | nome | Informe o nome do produto. | T23 |
| `produto` | NOT NULL `preco_venda` | preço | Informe o preço de venda. | T22 |
| `produto` | `chk_produto_preco_venda` | preço | O preço deve ser maior que zero. | T20, T21, T24 (CT17) |
| `produto_ingrediente` | `chk_produto_ingrediente_quantidade` | quantidade utilizada | A quantidade utilizada deve ser maior que zero. | T26, T27 (CT14) |
| `produto_ingrediente` | `produto_ingrediente_pkey` | composição | Este ingrediente já foi adicionado ao produto. | T28 |
| `produto_ingrediente` | `fk_produto_ingrediente_ingrediente` | composição | Ingrediente não encontrado. | T29 |
| `produto_ingrediente` | `fk_produto_ingrediente_produto` | composição | Produto não encontrado. | T30 |
| `unidade_medida_porcao_padrao` | `chk_unidade_porcao_padrao` | (só seed na Sprint 1) | — | T31 |
| `unidade_medida_porcao_padrao` | `unidade_medida_porcao_padrao_pkey` | (só seed na Sprint 1) | — | T32 |

## Regras que o banco **não** garante (ficam na aplicação)

- **Senha com no mínimo 8 caracteres** (US1/CT02): o banco só vê o hash.
- **Todo produto deve ter o açaí entre os ingredientes** (US5/CT15): decidido em 19/09/2026 — o nome do ingrediente, sem acentos e sem diferenciar maiúsculas, contém "acai" (ver `sql_repositorios_sprint1.md`, seção 4).
- **E-mail sem diferenciar maiúsculas:** o `UNIQUE` do PostgreSQL trata `Dono@Acai.com` e `dono@acai.com` como e-mails diferentes (verificado). **Proposta:** o backend grava e consulta o e-mail já normalizado (sem espaços nas pontas, em minúsculas).

## Necessidade que a FK cria para o backend e o frontend

`ingrediente.unidade_medida` só aceita unidades já cadastradas em `unidade_medida_porcao_padrao`, e a tela de gestão de unidades (US26) só chega na Sprint 3. Na Sprint 1, o campo "unidade de medida" da tela de ingredientes deve ser uma **lista de seleção alimentada pelo banco** (nunca texto livre), o que exige um endpoint de leitura. Consulta sugerida:

```sql
SELECT unidade_medida, porcao_padrao
FROM unidade_medida_porcao_padrao
ORDER BY unidade_medida;
```

As unidades do seed são `kg`, `mg`, `l`, `ml` e `un` (minúsculas: a FK diferencia maiúsculas). A tela pode exibir "L" sem problema, desde que envie `l`.

## Fora deste mapa

As constraints de `cliente`, `pedido`, `pedido_produto` e `movimentacao_estoque` entram quando as histórias US9, US12 e US13 (Sprint 2) forem detalhadas.
