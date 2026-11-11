# Modelo matemático — Reposição com orçamento (US14)

**Módulo:** `optimization/app/reposicao.py` · **Contrato:** `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`, §1.7 (Java ↔ Python) e §2.7 (rota) · **Dono:** Kauê
**Revisão:** 10/11/2026 (criação; tutorial do Kauê, Parte 10, item 3). Escrito a partir do código do módulo e dos testes de `optimization/tests/`.

O módulo responde: **"com este orçamento, quanto comprar de cada ingrediente?"**. É um problema de Programação Linear Inteira mista, resolvido pelo Gurobi em três estágios. Quando o orçamento não cobre nem os mínimos, a resposta sai **sem solver**.

---

## 1. Conjuntos

| Símbolo | O que é | De onde vem |
|---|---|---|
| `I` | Ingredientes a repor | `ingredientes` do JSON de entrada. O Java já escolheu: os **8** mais consumidos para períodos de até 30 dias; os **15** mais consumidos acima disso |

## 2. Parâmetros

| Símbolo | O que é | Campo do contrato | Domínio |
|---|---|---|---|
| `B` | Orçamento (R$) | `orcamento` | ≥ 0, até 2 casas |
| `c_i` | Consumo do ingrediente `i` no período (consulta 9 do Diogo) | `consumo_periodo` | ≥ 0, até 3 casas |
| `S_i` | Saldo **válido** (sem as linhas vencidas) | `quantidade_estoque` | ≥ 0, até 3 casas |
| `qmin_i` | Quantidade mínima cadastrada | `quantidade_minima` | ≥ 0, até 3 casas |
| `k_i` | Custo unitário (R$) | `custo_unitario` | ≥ 0, até 2 casas |

**Parâmetros derivados**, arredondados **para cima** em centésimos (0,01), para a compra nunca ficar abaixo do mínimo:

```
min_i = teto( max(0, qmin_i − S_i) )          mínimo a comprar: o que falta para o estoque chegar ao mínimo
nec_i = teto( max(0, c_i + qmin_i − S_i) )    necessidade: repetir o consumo do período e ainda manter o mínimo
```

Sempre vale `min_i ≤ nec_i`.

## 3. Variáveis de decisão

- `x_i` = quanto comprar do ingrediente `i`, em **centésimos inteiros** (0,01 da unidade do ingrediente), com `min_i ≤ x_i ≤ nec_i`;
- `t` = a **menor cobertura** entre os ingredientes, contínua, `0 ≤ t ≤ 1` (só no estágio 1). A cobertura do ingrediente `i` é `x_i / nec_i`.

## 4. Antes do solver: o orçamento cobre os mínimos?

```
se   Σ (i ∈ I)  k_i × min_i  >  B      →  orçamento insuficiente
```

Nesse caso a resposta traz `orcamento_suficiente: false`, `quantidade_sugerida = min_i` para cada ingrediente, `custo_total` = o custo dos mínimos, `valor_faltante = Σ k_i × min_i − B` e `cobertura_minima: null`. **O solver não é chamado.**

Se `nec_i = min_i` para todo `i` (não há o que decidir), o plano também sai sem solver: `x_i = min_i`.

## 5. Estágio 1 — repartir de forma justa (maior cobertura mínima)

```
maximizar    t

sujeito a    Σ (i ∈ I)  k_i × x_i  ≤  B                              (orçamento)
             x_i  ≥  t × nec_i          para todo i com nec_i > 0      (cobertura)
             min_i ≤ x_i ≤ nec_i,  x_i inteiro (centésimos)
             0 ≤ t ≤ 1
```

O valor ótimo é `t*`. Como o solver trabalha com tolerância numérica, o módulo recalcula `t*` em **frações exatas** (`Fraction`) a partir do plano inteiro e testa se ainda daria para aumentar a cobertura de todos dentro do orçamento; se desse, termina com falha técnica.

**Por que "justo":** o orçamento é repartido de modo que nenhum ingrediente fique muito pior que os outros (decisão de 14/10).

## 6. Estágio 2 — usar a sobra do orçamento

```
maximizar    G = Σ (i ∈ I)  k_i × x_i

sujeito a    as restrições do estágio 1, sem t, e com
             x_i  ≥  teto( t* × nec_i )     para todo i com nec_i > 0
```

Mantida a cobertura `t*`, o gasto vai ao máximo que cabe no orçamento. O valor ótimo é `G*`.

## 7. Estágio 3 — desempate

```
acrescenta   Σ k_i × x_i  ≥  G*                                        (manter o gasto)

para cada i, do mais consumido (c_i) ao menos consumido (empate: menor id):
    maximizar x_i
    fixar x_i no valor encontrado
```

Resultado: a mesma entrada dá sempre o mesmo plano. No fim, o módulo confere que o gasto e a cobertura não mudaram.

## 8. Contas em inteiros e conferência

- **Escala:** quantidades de entrada em milésimos; compras em centésimos; dinheiro em centavos. O gasto fica em "centavos × centésimos" (1 unidade = R$ 0,0001).
- **Arredondamento para cima:** `_teto_div(a, b) = -(-a // b)`. É o que transforma 0,001 em 0,01.
- **Solver:** `MIPGap = 0` e `MIPGapAbs = 0`; o ambiente do Gurobi é o mesmo do módulo de produção (`_criar_env`).
- **Conferência (`_verificar`):** depois de cada estágio, confere em inteiros que `min_i ≤ x_i ≤ nec_i` e que o gasto não passa do orçamento. Divergência = falha técnica (código de saída ≠ 0), nunca um plano errado.

## 9. Saída

| Campo | Conta |
|---|---|
| `quantidade_sugerida` | `x_i` |
| `quantidade_minima_compra` / `quantidade_necessaria` | `min_i` / `nec_i` |
| `custo_estimado` | `k_i × x_i` |
| `custo_total` | `Σ k_i × x_i` |
| `cobertura_minima` | `t*` (0 a 1; informativa, não aparece para o dono) |
| `itens` | Um por ingrediente recebido, do maior para o menor `custo_estimado` (empate: menor `id`) |

## 10. Exemplos

### 10.1 Exemplo didático (conta à mão)

| Ingrediente | Consumo `c` | Saldo `S` | Mínimo `qmin` | Custo `k` | `min` | `nec` |
|---|---|---|---|---|---|---|
| Açaí (1) | 0,800 | 0,300 | 0,500 | 18,50 | 0,20 | 1,00 |
| Granola (3) | 0,300 | 0 | 0,100 | 22,00 | 0,10 | 0,40 |
| Leite condensado (2) | 0,200 | 0,100 | 0,100 | 12,00 | 0 | 0,20 |

| Orçamento | O que acontece | Resultado |
|---|---|---|
| **50,00** | Cobre toda a necessidade (`t* = 1`) | 1,00 / 0,40 / 0,20; custo **29,70** |
| **5,00** | Não cobre os mínimos (`0,20 × 18,50 + 0,10 × 22,00 = 5,90`) | Mínimos 0,20 / 0,10 / 0; **faltam 0,90**; sem solver |
| **5,90** | Cobre exatamente os mínimos | Suficiente; custo 5,90; cobertura 0 (o leite condensado, com mínimo 0, fica sem compra) |
| **20,00** | Os três estágios (abaixo) | 0,66 / 0,27 / 0,15; custo **19,95** |

**Orçamento 20, passo a passo:**

1. **Estágio 1.** Com 66% de cada necessidade: açaí 0,66 (12,21), granola teto(0,264) = 0,27 (5,94), leite condensado teto(0,132) = 0,14 (1,68); total 19,83. Com 67% o total seria 20,015, acima de 20. Então `t* = 0,66`.
2. **Estágio 2.** Sobram 0,17. Só cabe mais 0,01 de leite condensado (0,12): gasto `G* = 19,95`.
3. **Estágio 3.** Não há outro plano com o mesmo gasto e a mesma cobertura: fica **0,66 / 0,27 / 0,15**.

Conferido pelos testes `test_exemplo_didatico_via_subprocesso`, `test_orcamento_que_cobre_toda_a_necessidade`, `test_orcamento_insuficiente_devolve_os_minimos_e_quanto_falta` e `test_orcamento_exatamente_igual_aos_minimos_e_suficiente`.

### 10.2 Exemplo da demonstração (`entrada_reposicao_demo.json`)

Período de 60 dias do `seed_demo.sql`, orçamento R$ 100. Só o morango precisa de compra:

```
Morango (5):  c = 0,720   S = 1,980   qmin = 2,000   k = 15,00
min = teto(2,000 − 1,980)          = 0,02
nec = teto(0,720 + 2,000 − 1,980)  = 0,74
compra: 0,74 kg × 15,00 = R$ 11,10        (cobertura 1,0: toda a necessidade cabe no orçamento)
```

Nos outros ingredientes, o saldo válido já cobre o consumo dos 60 dias mais o mínimo (`nec = 0`). É o par `entrada_reposicao_demo.json` / `saida_reposicao_demo.json`, conferido por `test_exemplo_demo_bate_com_a_saida_guardada`.

## 11. Como o modelo é conferido

- **Oráculo (`optimization/tests/oraculo_reposicao.py`):** força bruta com frações exatas, **sem solver**. O teste `test_coincide_com_o_oraculo_em_150_instancias` compara os dois em 150 problemas aleatórios com semente fixa.
- **Testes:** 22 testes em `optimization/tests/test_reposicao.py`. Evidência: `docs/Sprint_4/evidencias/2026-11-10_kaue_fasePQ_pytest.txt` (`47 passed`, com os 25 da produção).
- **Quebrar de propósito** (tutorial do Kauê, Parte 4.4): arredondar para baixo → 2 falhas; sem o estágio 3 → 1 falha; sem o estágio 2 → 9 falhas.
