# Modelo matemático — Recomendação de produção (US10, US11)

**Módulo:** `optimization/app/otimizador.py` · **Contrato:** `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`, §1.1 a §1.5 · **Dono:** Kauê
**Revisão:** 10/11/2026 (criação; tutorial do Kauê, Parte 10, item 3). Escrito a partir do código do módulo e dos testes de `optimization/tests/`.

O módulo responde: **"com o estoque que existe hoje, quantos copos de cada produto produzir para ter o maior lucro?"**. É um problema de Programação Linear Inteira, resolvido pelo Gurobi em dois estágios.

---

## 1. Conjuntos

| Símbolo | O que é | De onde vem |
|---|---|---|
| `P` | Produtos **ativos** | `produtos` do JSON de entrada (o Java filtra `ativo = true`) |
| `I` | Ingredientes usados por algum produto de `P`, inclusive os com estoque 0 | `ingredientes` do JSON de entrada |
| `I_p ⊆ I` | Ingredientes da composição do produto `p` | `composicao` de cada produto |

## 2. Parâmetros

| Símbolo | O que é | Campo do contrato | Domínio |
|---|---|---|---|
| `r_p` | Preço de venda do produto `p` (R$ por copo) | `preco_venda` | > 0, até 2 casas |
| `q_ip` | Quanto o copo `p` leva do ingrediente `i` | `quantidade_utilizada` | > 0, até 3 casas |
| `S_i` | Saldo **válido** do ingrediente `i` (sem as linhas vencidas) | `quantidade_estoque` | ≥ 0, até 3 casas |
| `k_i` | Custo unitário do ingrediente `i` (R$) | `custo_unitario` | ≥ 0, até 2 casas |

**Parâmetro derivado, o lucro por copo:**

```
m_p = r_p − Σ (i ∈ I_p)  k_i × q_ip
```

## 3. Variáveis de decisão

`x_p` = número de copos do produto `p` a produzir. **Inteiro, `x_p ≥ 0`.**

## 4. Estágio 1 — maior lucro

```
maximizar    L = Σ (p ∈ P)  m_p × x_p

sujeito a    Σ (p ∈ P : i ∈ I_p)  q_ip × x_p  ≤  S_i      para todo i ∈ I     (estoque)
             x_p inteiro, x_p ≥ 0                          para todo p ∈ P
```

**Produto que não compensa:** se `m_p ≤ 0`, o módulo fixa `x_p = 0` antes de chamar o solver (o produto fica com teto 0). O mesmo vale para o produto que não cabe nem uma vez no estoque. Se nenhum produto sobrar, a resposta é o plano todo em zero, **sem chamar o solver** (e com `status: "sucesso"`: falta de estoque não é erro).

O valor ótimo deste estágio é `L*`.

## 5. Estágio 2 — desempate

Vários planos podem ter o mesmo lucro `L*`. Para a tela não "mudar de ideia" com a mesma entrada, o módulo escolhe sempre o mesmo:

```
acrescenta   Σ m_p × x_p  ≥  L*                                               (manter o lucro)

para cada p ∈ P, em ordem crescente de id:
    maximizar x_p
    fixar x_p no valor encontrado
```

Resultado: entre os planos de lucro máximo, o que tem mais copos do produto de menor `id`; no empate, mais copos do segundo menor `id`; e assim por diante.

## 6. Contas em inteiros e conferência

- **Escala:** as quantidades são multiplicadas por 1.000 (milésimos) e os preços e custos por 100 (centavos). O lucro por copo fica em "centavos × milésimos" (1 unidade = R$ 0,00001). Todos os coeficientes do modelo são inteiros.
- **Leitura:** o JSON é lido direto em `Decimal` (`parse_float=Decimal`); nenhum número passa por `float` antes do modelo.
- **Solver:** `MIPGap = 0` e `MIPGapAbs = 0` (o padrão do Gurobi aceitaria um plano até 0,01% pior que o ótimo).
- **Conferência (`_verificar`):** depois de cada estágio, o módulo refaz em inteiros o consumo de cada ingrediente e o lucro. Se o plano passar do estoque, ou se o lucro do estágio 2 for diferente de `L*`, ele termina com falha técnica (código de saída ≠ 0) em vez de devolver um plano errado.

## 7. Saída

| Campo | Conta |
|---|---|
| `recomendacao` | `x_p` de cada produto recebido |
| `receita_estimada` | `Σ r_p × x_p` |
| `margem_estimada` | `L*` (o lucro) |
| `consumo` | `Σ q_ip × x_p` de cada ingrediente, só os maiores que zero |

O Java recalcula `receita_estimada` e `margem_estimada` com `BigDecimal` (contrato, §1.4).

## 8. Exemplos

Dados comuns aos dois exemplos (contrato, §1.1):

| Ingrediente (`id`) | Custo `k_i` | Tradicional (`id` 1, R$ 12,90) | Especial (`id` 2, R$ 18,90) |
|---|---|---|---|
| Açaí (1) | 18,50 | 0,300 | 0,450 |
| Leite condensado (2) | 12,00 | 0,030 | — |
| Granola (3) | 22,00 | — | 0,040 |
| Banana (4) | 5,50 | — | 0,050 |
| Copo (7) | 0,35 | 1 | 1 |

Lucro por copo:

```
Tradicional:  12,90 − (0,300 × 18,50 + 0,030 × 12,00 + 1 × 0,35)                 = 12,90 − 6,26 = 6,64
Especial:     18,90 − (0,450 × 18,50 + 0,040 × 22,00 + 0,050 × 5,50 + 1 × 0,35)  = 18,90 − 9,83 = 9,07
```

Estoques: açaí 40, leite condensado 2, banana 12, copo 300.

### 8.1 Exemplo do contrato (granola com estoque 0)

- O Especial leva granola, que está zerada: `x_2 = 0`.
- O Tradicional para no leite condensado: `2,000 ÷ 0,030 = 66,6` → **66** copos.
- Lucro `66 × 6,64 = 438,24`; receita `66 × 12,90 = 851,40`.

É o par `optimization/exemplos/entrada_contrato.json` / `saida_contrato.json`, conferido pelo teste `test_exemplo_do_contrato_via_subprocesso`.

### 8.2 Exemplo didático (granola com estoque 12)

| Plano (Tradicional / Especial) | Açaí usado (limite 40) | Lucro |
|---|---|---|
| "Jeito óbvio": encher o Tradicional até o limite do leite condensado (66) e usar o resto do açaí no Especial: **66 / 44** | 19,8 + 19,8 = 39,6 | 438,24 + 399,08 = **837,32** |
| Ótimo inteiro: **64 / 46** | 19,2 + 20,7 = 39,9 | 424,96 + 417,22 = **842,18** |

O solver acha **R$ 4,86 a mais** que a regra de bolso. Conferido pelo teste `test_exemplo_didatico_nao_e_o_arredondamento_do_simplex`.

### 8.3 Outros exemplos guardados

| Exemplo | Plano | Lucro | Receita |
|---|---|---|---|
| Demonstração (`entrada_demo.json`, o `seed_demo.sql`) | 151 / 1 / 0 | 1.011,71 | 1.966,80 |
| Estoque zero (`entrada_estoque_zero.json`, o seed oficial) | 0 / 0 / 0 | 0 | 0 |

## 9. Como o modelo é conferido

- **Oráculo (`optimization/tests/oraculo.py`):** resolve o mesmo problema **sem solver**, testando todas as combinações com `Decimal`. O teste `test_coincide_com_o_oraculo_em_200_instancias` compara os dois em 200 problemas aleatórios com semente fixa.
- **Testes:** 25 testes em `optimization/tests/test_otimizador.py`. Evidência: `docs/Sprint_4/evidencias/2026-11-10_kaue_fasePQ_pytest.txt` (`47 passed`, com os 22 da reposição).
- **Licença:** a limitada do `pip` resolve até 2.000 variáveis e 2.000 restrições; este modelo tem uma variável por produto e uma restrição por ingrediente (contrato, §1.6).
