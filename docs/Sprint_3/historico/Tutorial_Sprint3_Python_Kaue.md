# Tutorial — Sprint 3 (Python / Gurobi) do zero ao PR

**Para:** Kauê Nogueira Carneiro · **Papel:** módulo de otimização, Contrato Java ↔ Python e facilitação
**Base:** `sprint-3-roteiro.md` v4.6 (Fase 0, Fase P, Fase 4A) + `Analise_Roteiro_Sprint3_v4.6.md` + `Registro_de_Decisoes_e_Pendencias_Sprint3.md` · **Atualizado em:** 14/10/2026

> **O que foi verificado e o que não foi.** O código de referência (`referencia_python/`) foi **executado** sobre o `otimizador.py` e o `test_otimizador.py` reais da `main` (clonados do GitHub). Com Python 3.12 e `gurobipy 13.0.1` (licença limitada do `pip`): **24 testes passaram**, incluindo 200 instâncias aleatórias comparadas a um oráculo de força bruta, e o experimento "quebrar de propósito" derrubou os testes certos. **Não foi verificado:** Python 3.14.7, Windows, a licença WLS (a do Lucas ainda não chegou) e o tempo de resposta real. Se algo não bater na sua máquina, pare e mande a mensagem exata.

## 0. O que você entrega na Sprint 3 e em que ordem

| Ordem | Entrega | Quando | Depende de |
|---|---|---|---|
| 1 | **Contrato Java ↔ Python v2** (documento) | Hoje | Nada |
| 2 | **Módulo Python v2** (lucro, 2 estágios, `consumo`, modo eco) com testes | Hoje e 15/10 | Item 1 |
| 3 | **Ambiente do Gurobi** reproduzido em uma segunda máquina | 15/10 | Item 2 |
| 4 | **Fase 4A (em dupla com o Lucas):** ponte Java → Python (Hello World) e recomendação real | 17 a 18/10, se couber | Lucas (Fases 1 e 2), Diogo (dados) |
| 5 | **Facilitação:** relatório da Sprint 3, retrospectiva da Sprint 2, registro da perda do histórico do Git, CT20 | 16/10 | — |

**Pronto quando…**
1. `python -m pytest optimization/tests` passa na sua máquina **e** na de outro integrante.
2. O módulo imprime **só o JSON** no `stdout` (nenhum banner do Gurobi).
3. O Contrato v2 foi lido e aprovado pelo Lucas e pelo Diogo.
4. O PR está revisado por um colega.

## 1. Por que estamos mudando o módulo (leia antes de codar)

| O que muda | Por quê |
|---|---|
| O otimizador passa a maximizar **lucro** (`(preço − custo) × copos`) e não receita | Receita ignora o quanto cada copo **custa**. Um produto caro pode ser o menos lucrativo por kg do ingrediente escasso. A dor (a) do Documento de Visão fala em "lucratividade". **Atenção:** isso altera o Termo de Aceite; o Diogo avisa o professor (F9) |
| **Dois estágios** | Pode haver vários planos com o mesmo lucro máximo. O estágio 2 escolhe **um** de forma previsível (por `id` de produto crescente), para o resultado **nunca mudar** entre execuções |
| **Números inteiros** (milésimos e centavos) e `Decimal` | `float` erra (`0.1 + 0.2 ≠ 0.3`). Em inteiros a conta é exata e pode ser **conferida** |
| **`MIPGap = 0`** (F6) | O padrão do Gurobi aceita um plano até 0,01% pior que o ótimo. Para "é o melhor possível" ser verdade, o solver precisa ser exato **e** o código precisa conferir o resultado |
| Campo **`consumo`** só com o que o plano consome (F11) | O roteiro mostra assim; o Java usa para mostrar o quanto de cada ingrediente a recomendação gasta |
| **Modo eco** | O Hello World de infraestrutura (Java ↔ Python) continua até a recomendação real ser validada |

## 2. Preparar o ambiente

```powershell
cd C:\caminho\Mente_Fria
git checkout main
git pull
git checkout -b feature/python-modelo-lucro
cd optimization
.\venv\Scripts\activate
```
**Baseline (ponto de partida):** antes de mudar qualquer coisa, prove que o que existe passa.
```powershell
cd ..
python -m pytest optimization/tests -q
```
**Você deve ver:** `8 passed`. Se falhar, pare: a sua licença ou o ambiente têm problema (Parte 8).

**O `requirements.txt` hoje está em UTF-16 e fixa `gurobipy==13.0.3`** (a `main` atual). Dois ajustes:
1. **Regravar em UTF-8** (o `pip` e o Git lidam mal com UTF-16): no PowerShell, `Get-Content optimization\requirements.txt | Set-Content -Encoding utf8 optimization\requirements.txt`; depois abra no VS Code e confira que não há caractere estranho no começo.
2. **Versão do Gurobi:** a Arquitetura (08/10) decidiu **13.0.1**; o arquivo diz 13.0.3 (P2 do registro de pendências). Combine com o Diogo e **escolha uma**. Os testes deste tutorial foram verificados com **13.0.1**; se ficar 13.0.3, rode a suíte de novo.

## 3. Entenda o modelo (10 minutos)

**Exemplo do roteiro (dados do seed, granola com 12 kg):**

| Plano (copos do prod. 1 / prod. 2) | Lucro |
|---|---|
| Arredondar para baixo a solução "quebrada" do simplex: 66 / 44 | R$ 837,32 |
| **Ótimo inteiro (o que o modelo devolve): 64 / 46** | **R$ 842,18** |

O ótimo inteiro **não** é o arredondamento da solução fracionária. Por isso o modelo é de **Programação Linear Inteira pura** (todas as variáveis são inteiras).

**Fórmulas:** `m_p = preço_p − Σ custo_i × q_ip` (lucro por copo). **Estágio 1:** `max Σ m_p·x_p` com `Σ q_ip·x_p ≤ S_i`. **Estágio 2:** para cada produto por `id`, `max x_p` mantendo `Σ m_p·x_p ≥ L*` e fixando os já decididos. Produto com `m_p ≤ 0` recebe 0.

## 4. Documentar primeiro (Fase 0 e Fase P)

### 4.1 Contrato Java ↔ Python v2 (`docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`, §1)
Mudanças confirmadas:
```json
// stdin (Java → Python): cada ingrediente ganha custo_unitario; quantidade_estoque = saldo VÁLIDO (sem linhas vencidas)
{ "ingredientes": [ { "id": 1, "quantidade_estoque": 40.000, "custo_unitario": 18.50 } ],
  "produtos": [ { "id": 1, "preco_venda": 12.90, "composicao": [ { "ingrediente_id": 1, "quantidade_utilizada": 0.300 } ] } ] }
// stdout (Python → Java): ganha margem_estimada e consumo
{ "status": "sucesso", "receita_estimada": 851.40, "margem_estimada": 438.24,
  "recomendacao": [ { "produto_id": 1, "quantidade": 66 }, { "produto_id": 2, "quantidade": 0 } ],
  "consumo": [ { "ingrediente_id": 1, "quantidade": 19.800 }, { "ingrediente_id": 2, "quantidade": 1.980 }, { "ingrediente_id": 7, "quantidade": 66.000 } ] }
// modo eco: { "echo": "Açaí (polpa)" }  →  { "status": "sucesso", "echo": "AÇAÍ (POLPA)" }
```
Acrescente ao Contrato: `custo_unitario` ≥ 0 com no máximo 2 casas; quantidades com no máximo 3 casas e preços com 2; a **regra de domínio** (números inválidos viram `status: "erro"` com código de saída 0, nunca exceção); `receita_estimada` e `margem_estimada` são **informativas** (o Java recalcula em `BigDecimal`); `consumo` lista **só** os ingredientes com consumo maior que zero; **mantenha a §1.5** (eco) até a Fase 4 ser validada. As rotas REST (estoque, pedidos, recomendação) entram no Contrato com o Lucas e o Leonardo.

### 4.2 `docs/Apoio/Modelo_Matematico_Producao.md`
Seções: conjuntos e parâmetros; variável de decisão; lucro por copo; estágio 1; estágio 2; escala inteira e verificação; produto com lucro ≤ 0; terminologia **"Programação Linear Inteira pura"**; exemplo trabalhado (a tabela da Parte 3 e o exemplo do Contrato, 66/0 com lucro 438,24).

## 5. Os testes primeiro (vermelho) e depois o código (verde)

Copie `referencia_python/tests/oraculo.py` e `referencia_python/tests/test_otimizador.py` para `optimization/tests/` (**substitui** o `test_otimizador.py` atual; os 8 testes antigos continuam cobertos). Rode:
```powershell
python -m pytest optimization/tests -q
```
**Você deve ver:** falhas (o código ainda é o antigo). Isso é o "vermelho": prova que os testes testam algo.

**O oráculo** resolve o mesmo problema **sem solver**, por força bruta com `Decimal` (testa todos os planos possíveis). Serve para o resultado esperado **não ser calculado pelo próprio otimizador** (Plano de Testes, seção 1).

| Teste | O que prova |
|---|---|
| `test_exemplo_do_contrato_via_subprocesso` | O exemplo oficial: 66/0, receita 851,40, margem 438,24, `consumo` (via `stdin`/`stdout`) |
| `test_stdout_contem_somente_o_json_em_utf8` | Nenhum banner de licença vaza para o `stdout` (o problema que você já enfrentou) |
| `test_exemplo_didatico_...` | O ótimo inteiro (64/46) não é o arredondamento (66/44) |
| `test_maior_receita_nao_e_maior_lucro` | Produto de preço maior pode render menos lucro |
| `test_produto_com_lucro_zero_ou_negativo_...` | `x = 0` quando não compensa |
| `test_desempate_...` | Mesmo lucro: sempre o mesmo plano, favorecendo o menor `id` |
| `test_estoque_zerado_...` | Sem estoque é **sucesso** com zeros (Contrato §1.2) |
| `test_entradas_invalidas_...` (10 casos) | Dados ruins viram `status: "erro"` com código de saída 0 |
| `test_eco_...` (4 casos) | Modo eco com acentos e erros tratados |
| `test_consumo_...` | O consumo nunca passa do estoque e só lista o que consome |
| `test_coincide_com_o_oraculo_em_200_instancias` | Instâncias aleatórias (semente fixa) sempre igualam o oráculo |

**Implemente.** Copie `referencia_python/app/otimizador.py` para `optimization/app/otimizador.py` (**substitui** o atual; o antigo fica no Git). **Leia antes de usar:** o módulo é seu e você vai defendê-lo.

| Parte do código | O que faz e por quê |
|---|---|
| `main()` com `json.load(..., parse_float=Decimal)` | O JSON nunca passa por `float` |
| `_dec` e `validar_e_converter` | Aceita só `int`/`Decimal` com no máximo N casas (rejeita texto e booleano, que em Python é um `int`). Falha vira `ErroDeDominio` → `status: "erro"`, código 0. Preserva as 4 mensagens antigas |
| Escala inteira | Quantidades em milésimos, preços e custos em centavos. `lucro = preço·1000 − Σ custo·q` (inteiro) e `teto = min(estoque // q)` |
| `_criar_env` | `OutputFlag = 0` **antes** de `start()` (sem banner); lê `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID` se existirem; senão, usa o `gurobi.lic` |
| `resolver`: `MIPGap = 0`, `MIPGapAbs = 0`, tolerâncias 1e-9 | **F6:** solver exato, não "quase ótimo" |
| `_verificar` | Depois de cada estágio, confere em **inteiros** que o plano respeita o estoque e o lucro. Se divergir, `FalhaTecnica` (código ≠ 0): **nunca** devolve um plano que não conferiu |
| `processar` (modo eco) | `{"echo": ...}` sem `produtos` → devolve em maiúsculas; `echo` não texto → `status: "erro"` |
| `processar` (saída) | `consumo` calculado em milésimos exatos, só os maiores que zero |

```powershell
python -m pytest optimization/tests -q
```
**Você deve ver:** `24 passed`. Se aparecer erro de licença do Gurobi, vá à Parte 8.

### Quebrar de propósito (5 minutos)
Em `otimizador.py`, troque temporariamente `lucro = preco * 1000 - sum(custo[i] * q for i, q in itens.items())` por `lucro = preco * 1000` (volta a maximizar receita) e rode os testes: **devem falhar 5**. Depois, troque `if "echo" in entrada and "produtos" not in entrada:` por `if False:`: **devem falhar 4** (os de eco). **Desfaça as trocas** e confirme `24 passed`. Guarde a saída como evidência.

## 6. Rodar "como o Java vai rodar"

Copie `referencia_python/exemplos/` para `optimization/exemplos/`. São entradas e **saídas esperadas**, geradas pelo próprio módulo:

| Arquivo | Para quê |
|---|---|
| `entrada_contrato.json` / `saida_contrato.json` | O exemplo oficial do Contrato (66/0, margem 438,24) |
| `entrada_demo.json` / `saida_demo.json` | Estoque **válido** do `seed_demo.sql` do Diogo: plano **151 / 1 / 0**, receita 1.966,80, lucro 1.011,71 |
| `entrada_estoque_zero.json` / `saida_estoque_zero.json` | O que a recomendação devolve com o seed **oficial** (estoque 0): tudo zero e `consumo: []` |
| `entrada_eco.json` / `saida_eco.json` | Hello World da ponte |

```powershell
Get-Content -Raw -Encoding utf8 optimization\exemplos\entrada_contrato.json | python optimization\app\otimizador.py
```
**Você deve ver (uma linha só):**
```
{"status": "sucesso", "receita_estimada": 851.4, "margem_estimada": 438.24, "recomendacao": [{"produto_id": 1, "quantidade": 66}, {"produto_id": 2, "quantidade": 0}], "consumo": [{"ingrediente_id": 1, "quantidade": 19.8}, {"ingrediente_id": 2, "quantidade": 1.98}, {"ingrediente_id": 7, "quantidade": 66.0}]}
```
Repita com `entrada_demo.json` (esperado `151/1/0`) e com `entrada_eco.json` (esperado `{"status": "sucesso", "echo": "AÇAÍ (POLPA)"}`). Teste também o erro tratado: troque o conteúdo por um texto qualquer; deve sair `{"status": "erro", "mensagem": "JSON de entrada inválido"}` e `$LASTEXITCODE` deve ser `0`.

## 7. Ambiente do Gurobi (Fase P, item 9)

**Decisão (Alternativa C):** em produção o `otimizador.py` usa a licença WLS **diretamente**, sem Compute Server. A licença acadêmica é de **uso não comercial**: o sistema serve à demonstração.
1. **Como a sua licença funciona hoje.** Abra o seu `gurobi.lic`: se tiver só `WLSACCESSID`, `WLSSECRET` e `LICENSEID`, é uma licença WLS comum, que já funciona em modo direto.
2. **Teste o caminho das variáveis**, sem comitar nada (copie os valores do seu `gurobi.lic`):
   ```powershell
   $env:GRB_WLSACCESSID = "..."; $env:GRB_WLSSECRET = "..."; $env:GRB_LICENSEID = "123456"
   python -m pytest optimization/tests -q
   Remove-Item Env:GRB_WLSACCESSID, Env:GRB_WLSSECRET, Env:GRB_LICENSEID
   ```
3. **Segredo.** `gurobi.lic` e esses valores **nunca** vão para o Git (o `.gitignore` já cobre o `.lic`; confira `git status` antes de cada commit).
4. **Segunda máquina.** Peça a **outro integrante** que clone a branch, crie o próprio `venv` e rode `pytest`. Anote o resultado: isso encerra a pendência "só funciona no PC do Kauê". Sem licença própria, o `gurobipy` usa a limitada, suficiente para estes testes.
5. **Aviso para o Lucas (Fase 4A):** o Spring lê o `.env` para as **próprias** propriedades, mas o processo Python só enxerga as variáveis do **sistema**. O Java precisa passar `GRB_*` explicitamente ao `ProcessBuilder`. O Diogo já comprovou esse problema na revisão da Sprint 2.

**Medir o tempo (quando a licença do Lucas chegar):** há 1 + N otimizações por chamada, mais a abertura da licença na rede.
```powershell
Measure-Command { Get-Content -Raw -Encoding utf8 optimization\exemplos\entrada_demo.json | python optimization\app\otimizador.py | Out-Null }
```
Repita 5 vezes; anote mínimo, máximo e média; entregue ao Lucas (o timeout de 10 s continua **provisório**).

## 8. Fase 4A, em dupla com o Lucas (17 a 18/10, se couber)

Seu papel: **o contrato e a prova**, não o código Java.
1. **Hello World (eco):** o Lucas cria `GET /api/teste/python` com `@Profile("dev")` (F4: só existe em desenvolvimento). Você confere que `GET http://localhost:8080/api/teste/python` devolve `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}` **com acentos corretos** (é o teste de codificação no Windows).
2. **Recomendação real:** o Lucas monta o JSON com o **saldo válido** (do SQL do Diogo) e o **custo**. Comparem a saída com `saida_demo.json` (151/1/0): se divergir, o problema está na **montagem da entrada** (quase sempre o saldo válido ou o custo).
3. **Variáveis `GRB_*`** no `ProcessBuilder` (Parte 7, item 5).
4. **Erros:** teste `status: "erro"` (ex.: produto sem composição) → o Java deve responder uma mensagem amigável (`422`), e `exit code` ≠ 0 → `500` **sem** *stack trace*.

## 9. Facilitação (sua outra função)
1. **Relatório da Sprint 3** em `docs/Sprint_3/` (pasta nova): planejado x entregue (use o roteiro), evidências, riscos.
2. **Retrospectiva da Sprint 2** (nunca realizada): convoque 30 minutos; registre o que funcionou, o que não funcionou e ações, usando o modelo das atas anteriores.
3. **Perda do histórico do Git** (pedido do relatório da Sprint 2): escreva meia página com a data (27/09), o motivo (conflitos de estrutura) e o que foi preservado; anexe os prints que existirem.
4. **CT20:** peça a um integrante que **não** conhece o projeto que siga **só o README** e anote onde travou. Hoje o README já tem o passo do `.env` em `backend/`.
5. **Fim do núcleo (16/10):** decida com o grupo se a extensão até 18/10 será usada.

## 10. Entrega

```powershell
git add optimization docs/Apoio
git commit -m "feat: modelo de lucro em dois estagios, consumo, modo eco e contrato v2"
git push -u origin feature/python-modelo-lucro
```
No PR: o resultado de `pytest` (24 passed), o experimento da Parte 5, a **menção à mudança receita → lucro** (F9) e o aceite do Lucas e do Diogo ao Contrato v2. Peça revisão ao **Lucas** (consome o contrato) e ao **Diogo** (consome as entradas).

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `JSONDecodeError` no teste | Banner do Gurobi no `stdout` | Conferir que `OutputFlag = 0` está **antes** de `env.start()` |
| Acentos quebrados no Windows | Codificação do terminal | Manter as duas linhas `reconfigure(encoding="utf-8")` e usar `-Encoding utf8` |
| `Model too large for size-limited license` | Instância grande demais para a licença limitada | Use a licença WLS (Parte 7) |
| `FalhaTecnica` (código ≠ 0) | A verificação inteira não conferiu | **Não ignore.** Guarde o JSON de entrada e investigue |
| `pip` reclama do `requirements.txt` | Arquivo em UTF-16 | Regravar em UTF-8 (Parte 2) |
| Teste do oráculo muito lento | Estoque grande demais para a força bruta | Reduza estoques na instância |

## Checklist final
- [ ] Baseline de 8 testes passou **antes** de mudar
- [ ] `requirements.txt` em UTF-8 e versão do `gurobipy` decidida com o Diogo
- [ ] Contrato v2 e `Modelo_Matematico_Producao.md` escritos
- [ ] 24 testes passando na sua máquina e em **outra**
- [ ] Experimento "quebrar de propósito" registrado
- [ ] `exemplos/` entregues ao Lucas
- [ ] Segredos fora do Git
- [ ] Relatório da Sprint 3, retrospectiva da Sprint 2 e registro do histórico do Git
- [ ] Medição de tempo entregue ao Lucas (quando a licença chegar)

## Depois (Sprint 4, só para você saber)
Reposição com orçamento via solver (US14) e a definição única de "consumo" (F12, pendente). Nada disso é do núcleo de agora.
