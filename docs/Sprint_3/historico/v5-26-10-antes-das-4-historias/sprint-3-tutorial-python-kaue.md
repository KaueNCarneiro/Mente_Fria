# Tutorial — Sprint 3 (Python/Gurobi, Contrato e Facilitação) — Kauê

**Para:** Kauê Nogueira Carneiro · **Papel:** módulo de otimização, Contrato Java ↔ Python, facilitação (Scrum Master) · **Revisão:** 26/10/2026 (auditoria) · **Substitui** `historico/Tutorial_Sprint3_Python_Kaue.md`.
**Base:** `sprint-3-roteiro.md` (v5), `sprint-3-contrato-v2-proposta.md`, `sprint-3-decisoes-e-pendencias.md`.

> **O que foi verificado e o que não foi (26/10).**
> - Executado numa cópia, com **Python 3.14.0** e `gurobipy` **13.0.1 e 13.0.3** (licença limitada do `pip`):
>   - baseline da `main`: `8 passed`;
>   - módulo v2: **`25 passed`** nas duas versões (24 testes de 14/10 + 1 novo, `test_entrada_com_bom_e_aceita`);
>   - quebrar de propósito: 5 falhas (lucro → receita) e 5 falhas (sem eco: os 4 testes de eco + o do BOM);
>   - os 4 exemplos via subprocesso conferem com os `saida_*.json`;
>   - o plano da demonstração (**151/1/0**, lucro 1.011,71) e o do contrato (66/0, lucro 438,24) foram confirmados **também pelo oráculo de força bruta**, sem solver.
> - **Defeito achado e corrigido (A8):** o comando do tutorial de 14/10 (`Get-Content … | python otimizador.py`) **falhava** com "JSON de entrada inválido", porque o PowerShell envia um BOM antes do JSON. O módulo de `referencia/` agora lê com `utf-8-sig`.
> - **Não verificado:** a licença WLS (do Lucas e a sua), Python 3.14.7 e o tempo de resposta real.
> - **Mudança de caminhos:** `referencia_python/` nunca existiu. O material está em **`docs/Sprint_3/referencia/python/`** (`app/`, `tests/`, `exemplos/`).

## 0. O que você entrega e em que ordem

| Ordem | Entrega | Quando | Depende de | Libera |
|---|---|---|---|---|
| **1** | **Contrato v2 aprovado e aplicado ao contrato oficial** (A10) + decisões A11 e P2 | **Seg 26 – Ter 27** | Revisão do Lucas e do Leonardo | **Lucas (Fase 3) e Leonardo (telas)** |
| 2 | Módulo Python v2, testes e exemplos (PR) | Ter 27 – Qua 28 | Item 1 (§1) | Lucas (Fase 4A) |
| 3 | Ambiente reproduzido numa **2ª máquina** | Qua 28 | Item 2 | — |
| 4 | Plano de Testes: CT23, CT24, CT29 e os CTs de Python | Qui 29 | — | Janela de testes |
| 5 | **Fase 4A em dupla** com o Lucas; medir o tempo | Sex 30 | Lucas (Fases 2 e 3) | Tela 12 |
| 6 | Facilitação: Review, Retro, relatório, CT20, registro do Git, branch `sprint-3` | Seg 26 – Dom 01/11 | — | Encerramento |

**Pronto quando:**
1. o contrato oficial tem a v2 e o registro no histórico de revisões;
2. `25 passed` na sua máquina **e** na de outro integrante;
3. o `stdout` traz só o JSON;
4. os PRs estão revisados;
5. Review e Retro estão registradas.

## 1. Por que o módulo muda

| O que muda | Por quê |
|---|---|
| Maximiza **lucro** `(preço − custo) × copos`, não receita | Receita ignora o custo; a dor (a) da Visão é lucratividade. **Isso altera o Termo:** o Diogo e o Lucas avisam o professor (F9/P3) |
| **Dois estágios** | Vários planos podem ter o mesmo lucro máximo; o estágio 2 escolhe sempre o mesmo (por `id` crescente) |
| **Inteiros** (milésimos e centavos) e `Decimal` | O `float` erra (`0.1 + 0.2 ≠ 0.3`); em inteiros, a conta é exata e conferível |
| `MIPGap = 0` (F6) | O padrão do Gurobi aceita um plano até 0,01% pior que o ótimo |
| `consumo` só com o que o plano gasta (F11) | O Java mostra o gasto da recomendação |
| Modo **eco** | Hello World da ponte Java ↔ Python, até a recomendação real ser validada |
| Entrada lida com **`utf-8-sig`** (A8) | O pipe do PowerShell manda um BOM, e sem isso o módulo recusava a entrada (verificado) |

## 2. Contrato v2 (primeira entrega: segunda e terça)

1. Leia `docs/Sprint_3/sprint-3-contrato-v2-proposta.md`. Ele já traz a §1 (Java ↔ Python) com as mensagens de erro que o módulo produz hoje, e a §2 (REST) com os formatos de estoque, pedido e recomendação.
2. Marque **30 minutos** com o Lucas e o Leonardo e decida os itens **[decidir]**:
   - quem calcula a `situacao` do estoque (frontend ou backend);
   - aceitar entrada com validade vencida (o PO decide).
3. Aplique a proposta ao contrato oficial `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`:
   - substitua a §1.1 a §1.3;
   - acrescente à §2 as mensagens padrão, o `PATCH` no CORS e as rotas de estoque, pedido e recomendação;
   - registre a linha na seção 3, "Histórico de revisões";
   - mantenha a §1.5 (eco).
4. **Decida a A11 (Compute Server no desenvolvimento)** com o Lucas.

   | Opção | O que muda |
   |---|---|
   | **WLS direta também no desenvolvimento** (recomendada) | O `otimizador.py` v2 já lê `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID`; é o mesmo caminho da produção (Alternativa C). Basta registrar na Arquitetura (§2.1, §3.3, §5) e no Plano de Testes (§4.2) que o Compute Server local deixou de ser usado |
   | Manter o Compute Server | O `_criar_env` precisa de um ramo novo, com `env.setParam("ComputeServer", …)` e `ServerPassword`, mais um teste. Os nomes `GRB_COMPUTESERVER` e `GRB_SERVERPASSWORD` ainda são proposta (Arquitetura §5) |

5. **Decida a P2 (versão do Gurobi) com o Diogo.** As duas versões passaram nos 25 testes. Sugestão: **13.0.3**, a que já está no `requirements.txt`, atualizando a Arquitetura (§2.1) e o README.
6. **Documente o modelo** em `docs/Apoio/Modelo_Matematico_Producao.md`:
   - conjuntos, parâmetros e a variável de decisão;
   - lucro por copo; estágios 1 e 2; escala inteira e verificação; lucro ≤ 0 → 0;
   - terminologia "Programação Linear Inteira pura";
   - os exemplos da §4.

**Evidência:** o commit do contrato oficial, com aprovação do Lucas e do Leonardo no PR.

## 3. Preparar o ambiente e o baseline

```powershell
cd C:\caminho\Mente_Fria
git checkout main
git pull
git checkout -b feature/python-modelo-lucro
cd optimization
.\venv\Scripts\activate
cd ..
python -m pytest optimization/tests -q
```

**Você deve ver:** `8 passed` (o resultado da auditoria com o código da `main`). Se falhar, pare: o problema é de ambiente ou de licença (§9).

**`requirements.txt`:** hoje está em **UTF-16** e fixa `gurobipy==13.0.3`.
- O `pip` lê UTF-16 sem problema. **O motivo para regravar é o Git**, que trata UTF-16 como binário e não mostra o diff. (O tutorial de 14/10 dizia que o `pip` "lida mal", o que é incorreto.)
- Use **parênteses**: sem eles, o PowerShell lê e grava o mesmo arquivo no mesmo pipe e pode deixá-lo **vazio**.
  ```powershell
  (Get-Content optimization\requirements.txt) | Set-Content -Encoding utf8 optimization\requirements.txt
  ```
- O PowerShell 5.1 grava com BOM. O `pip` aceita: verificado no código do `pip`.
- Ajuste a linha do `gurobipy` conforme a P2. Confira com `git diff optimization\requirements.txt`: o diff agora aparece como texto.

## 4. Entenda o modelo (10 minutos)

| Exemplo | Plano | Lucro (`margem_estimada`) | Receita |
|---|---|---|---|
| Contrato (granola 0) | 66 / 0 | 438,24 | 851,40 |
| Didático (granola 12) | **64 / 46** (arredondar o fracionário daria 66/44, com R$ 4,86 a menos) | 842,18 | 1.695,00 |
| Demonstração (`seed_demo.sql`) | **151 / 1 / 0** | 1.011,71 | 1.966,80 |
| Seed oficial (estoque 0) | 0 / 0 / 0 | 0 | 0 |

**Fórmulas:**
- lucro por copo: `m_p = preço_p − Σ custo_i × q_ip`;
- **estágio 1:** `max Σ m_p·x_p` com `Σ q_ip·x_p ≤ S_i` e `x_p` inteiro ≥ 0;
- **estágio 2:** por `id` crescente, `max x_p`, mantendo `Σ m_p·x_p ≥ L*` e fixando os já decididos;
- produto com `m_p ≤ 0` recebe 0.

## 5. Testes primeiro (vermelho), depois o código (verde)

```powershell
copy docs\Sprint_3\referencia\python\tests\oraculo.py optimization\tests\
copy docs\Sprint_3\referencia\python\tests\test_otimizador.py optimization\tests\
python -m pytest optimization/tests -q
```

**Você deve ver falhas:** o código ainda é o antigo. É o "vermelho", que prova que os testes testam algo. O oráculo resolve o mesmo problema **sem solver** (força bruta com `Decimal`), para o resultado esperado não ser calculado pelo próprio otimizador (Plano de Testes, §1).

| Teste | O que prova |
|---|---|
| `test_exemplo_do_contrato_via_subprocesso` | 66/0, receita 851,40, margem 438,24 e `consumo`, via `stdin`/`stdout` |
| `test_stdout_contem_somente_o_json_em_utf8` | Nenhum banner de licença vaza |
| `test_exemplo_didatico_…` | O ótimo inteiro (64/46) não é o arredondamento |
| `test_maior_receita_nao_e_maior_lucro` | Preço maior pode render menos lucro (CT41) |
| `test_produto_com_lucro_zero_ou_negativo_…` | `x = 0` quando não compensa |
| `test_desempate_…` | Mesmo lucro: sempre o mesmo plano, favorecendo o menor `id` |
| `test_estoque_zerado_…` | Sem estoque é **sucesso** com zeros |
| `test_entradas_invalidas_…` (10 casos) | Dados ruins → `status: "erro"`, código de saída 0 |
| `test_json_invalido_…` | Texto que não é JSON → erro tratado |
| **`test_entrada_com_bom_e_aceita`** (novo, A8) | Entrada com BOM (do PowerShell) é aceita |
| `test_eco_…` (4 casos) | Eco com acentos e erros tratados |
| `test_consumo_…` | O consumo não passa do estoque e só lista o que consome |
| `test_coincide_com_o_oraculo_em_200_instancias` | Instâncias aleatórias (semente fixa) sempre iguais ao oráculo |

**Implemente:** copie `docs\Sprint_3\referencia\python\app\otimizador.py` para `optimization\app\otimizador.py`. O antigo fica no Git. **Leia antes de usar**: o módulo é seu.

| Parte | O que faz |
|---|---|
| `main()` com `reconfigure(encoding="utf-8-sig")` e `json.load(…, parse_float=Decimal)` | Aceita o BOM; o JSON nunca passa por `float` |
| `_dec` e `validar_e_converter` | Aceita só `int`/`Decimal` com no máximo N casas (rejeita texto e `bool`). Falha → `ErroDeDominio` → `status: "erro"` |
| Escala inteira | Quantidades em milésimos, preços e custos em centavos. Lucro por copo em "centavos × milésimos" |
| `_criar_env` | `OutputFlag 0` **antes** de `start()`; usa as variáveis `GRB_WLS*` se existirem; senão, o `gurobi.lic` |
| `resolver` | `MIPGap = 0`, `MIPGapAbs = 0`, tolerâncias de 1e-9 (os mínimos permitidos pelo Gurobi: conferido na documentação) |
| `_verificar` | Confere em inteiros, depois de cada estágio. Divergência → `FalhaTecnica` (código ≠ 0): **nunca** devolve um plano sem conferir |

```powershell
python -m pytest optimization/tests -q
```

**Você deve ver:** `25 passed`.

**Quebrar de propósito (5 minutos):**

| Troca temporária em `otimizador.py` | Resultado esperado (verificado) |
|---|---|
| `lucro = preco * 1000 - sum(custo[i] * q for i, q in itens.items())` → `lucro = preco * 1000` | **5 failed**, 20 passed |
| `if "echo" in entrada and "produtos" not in entrada:` → `if False:` | **5 failed**, 20 passed (os 4 testes de eco + o teste do BOM, que usa o eco) |

Desfaça as trocas e confirme `25 passed`. Guarde as saídas como evidência.

## 6. Rodar "como o Java vai rodar"

```powershell
mkdir optimization\exemplos
copy docs\Sprint_3\referencia\python\exemplos\*.json optimization\exemplos\
Get-Content -Raw -Encoding utf8 optimization\exemplos\entrada_contrato.json | python optimization\app\otimizador.py
```

**Você deve ver** (uma linha só):

```
{"status": "sucesso", "receita_estimada": 851.4, "margem_estimada": 438.24, "recomendacao": [{"produto_id": 1, "quantidade": 66}, {"produto_id": 2, "quantidade": 0}], "consumo": [{"ingrediente_id": 1, "quantidade": 19.8}, {"ingrediente_id": 2, "quantidade": 1.98}, {"ingrediente_id": 7, "quantidade": 66.0}]}
```

- **Demais exemplos:** `entrada_demo.json` (esperado 151/1/0) e `entrada_eco.json` (esperado `{"status": "sucesso", "echo": "AÇAÍ (POLPA)"}`, **com acentos**).
- **Se a saída for "JSON de entrada inválido":** o `otimizador.py` ainda é o antigo, sem `utf-8-sig`.
- **Alternativa sem pipe do PowerShell** (envia os bytes exatos):
  ```powershell
  cmd /c "python optimization\app\otimizador.py < optimization\exemplos\entrada_eco.json"
  ```
- **Erro tratado:** troque o conteúdo do arquivo por um texto qualquer. Deve sair `{"status": "erro", "mensagem": "JSON de entrada inválido"}`, com `$LASTEXITCODE` igual a `0`.

## 7. Ambiente do Gurobi

**Decisão (Alternativa C):** em produção, a licença WLS é usada **direto**, sem Compute Server. No desenvolvimento, conforme a A11.

1. **Sua licença:** se o `gurobi.lic` tem só `WLSACCESSID`, `WLSSECRET` e `LICENSEID`, é WLS comum.
2. **Teste o caminho das variáveis com POUCOS testes**, sem comitar nada:
   ```powershell
   $env:GRB_WLSACCESSID = "<REDACTED>"; $env:GRB_WLSSECRET = "<REDACTED>"; $env:GRB_LICENSEID = "<REDACTED>"
   python -m pytest optimization/tests -q -k "contrato or didatico"
   Remove-Item Env:GRB_WLSACCESSID, Env:GRB_WLSSECRET, Env:GRB_LICENSEID
   ```
   > ⚠️ **Não rode a suíte inteira com as variáveis definidas.** A licença WLS acadêmica permite **2 sessões simultâneas**, e cada processo segura um token por pelo menos 5 minutos. Os testes que abrem subprocessos podem esgotar as sessões ("too many sessions"). A suíte completa roda com a licença limitada do `pip`, que basta para esses tamanhos (até 2.000 variáveis e restrições).
3. **Banner:** o `OutputFlag 0` só garante silêncio quando a licença vem das variáveis de ambiente. Com o `gurobi.lic`, mensagens de licença podem sair antes. O teste `test_stdout_contem_somente_o_json_em_utf8` é a guarda: se ele falhar só na sua máquina, é isso.
4. **Segredo:** o `gurobi.lic` e os valores **nunca** vão para o Git. Confira `git status` antes de cada commit.
5. **Segunda máquina:** peça a outro integrante que clone a branch, crie o próprio `venv`, rode `pip install -r optimization/requirements.txt` e `python -m pytest optimization/tests -q`. **Anote o resultado:** é o que encerra a pendência "só funciona no PC do Kauê".
6. **Licença do Lucas em produção (A14):** pelos termos, só o titular usa e não há uso operacional. Registre na Arquitetura que o deploy é uso acadêmico do Lucas, para demonstração.

## 8. Fase 4A, em dupla com o Lucas (sexta 30)

O seu papel é **o contrato e a prova**, não o código Java (tutorial do Lucas, §5).

1. **Eco:** com o perfil `dev`, `GET http://localhost:8080/api/teste/python` → `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}`, **com acentos**. Se vier quebrado, o Java não está escrevendo a entrada em UTF-8.
2. **Recomendação real** com o seed de demonstração: compare com o `saida_demo.json` (151/1/0). Se divergir, o problema quase sempre está **na entrada** (saldo válido ou custo). Peça ao Lucas o JSON enviado.
3. **Variáveis `GRB_*` no `ProcessBuilder`** (tutorial do Lucas, §5.2).
4. **Erros:** `status: "erro"` → `422`; código de saída ≠ 0 → `500` sem detalhe (CT44).
5. **Meça o tempo** (P6), 5 vezes, e anote mínimo, máximo e média:
   ```powershell
   Measure-Command { Get-Content -Raw -Encoding utf8 optimization\exemplos\entrada_demo.json | python optimization\app\otimizador.py | Out-Null }
   ```

## 9. Facilitação (Scrum Master)

| # | Tarefa | Quando | Evidência |
|---|---|---|---|
| 1 | **Branch `origin/sprint-3` (A15):** ele tem `backend/env.example` e `./backend/.env`, que reverteriam o PR #24 num merge. Feche o branch ou atualize-o a partir da `main` (`git merge main`), em acordo com a equipe | Seg 26 | Print do branch |
| 2 | **Daily de 10 minutos** (seg a sáb): o que fiz, o que farei, o que me bloqueia. Bloqueio → resolver no mesmo dia (regra da equipe) | Diária | Notas curtas em `docs/Sprint_3/` |
| 3 | **Acompanhar a A3** com o Lucas: sem a decisão, o plano de testes de 01–06/11 não fecha | Ter 27 | Decisão registrada |
| 4 | **CT20:** um integrante sobe o projeto do zero **só** com o README; anote onde travou | Sáb 31 | Relato e PR no README |
| 5 | **Registro da perda do histórico do Git:** meia página com a data (27/09; primeiro commit `9e02498`), o motivo (conflitos de estrutura) e o que foi preservado | Até 31/10 | `docs/Sprint_3/` |
| 6 | **Retrospectiva da Sprint 2** (nunca realizada) e da Sprint 3, juntas em 30 min. Registre a contradição: as contribuições individuais da Sprint 2 marcam "Retrospectiva" como feita, mas o relatório diz que não houve | Dom 01/11 | Ata no padrão da `sprint-1-ata-retrospectiva.md` |
| 7 | **Sprint Review com demonstração** (nunca houve nas Sprints 1 e 2): o fluxo do §1 do roteiro, com os dois perfis | Dom 01/11 | Ata e prints |
| 8 | **Relatório da Sprint 3** em `docs/Sprint_3/`: planejado × entregue (use a matriz), evidências, riscos e decisões. Sem `[PREENCHER]` | Dom 01/11 | Arquivo |
| 9 | **Janela de testes (01–06/11):** dividir a bateria por pessoa e perfil e priorizar os caminhos do Termo de Aceite (`sprint-3-casos-de-teste-propostos.md`, §3) | Dom 01/11 | Planilha ou tabela de execução |

## 10. Entrega

```powershell
git add optimization
git commit -m "feat: modelo de lucro em dois estagios, consumo, modo eco e entrada com BOM"
git push -u origin feature/python-modelo-lucro
```

No PR, inclua:
- `25 passed` (e o resultado da 2ª máquina);
- os experimentos da §5;
- a menção a receita → lucro (F9);
- o link do PR do Contrato v2.

Peça revisão ao **Lucas** (consome o contrato) e ao **Diogo** (consome as entradas).

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `JSON de entrada inválido` no pipe do PowerShell | Módulo antigo, sem `utf-8-sig` (o PowerShell envia BOM) | Use o `otimizador.py` de `referencia/`, ou `cmd /c "… < arquivo.json"` |
| Acentos viram `?` no eco | Codificação do pipe | Mesma solução; ou `cmd /c` com redirecionamento |
| `JSONDecodeError` nos testes | Banner da licença no `stdout` (fallback do `gurobi.lic`) | `OutputFlag 0` antes de `start()`; preferir as variáveis WLS |
| `too many sessions` / erro de licença WLS | Mais de 2 sessões | Não rode a suíte inteira com `GRB_*` (§7, passo 2) |
| `Model too large for size-limited license` | Instância grande demais para a licença limitada | Use a WLS (§7) |
| `FalhaTecnica` (código ≠ 0) | A verificação inteira não conferiu | **Não ignore.** Guarde o JSON de entrada e investigue |
| `requirements.txt` vazio depois da conversão | Pipe sem parênteses | Recupere com `git checkout -- optimization/requirements.txt` e use `(Get-Content …) \| Set-Content …` |

## Checklist final

- [ ] Contrato v2 aprovado e aplicado ao oficial; A11 e P2 decididas e registradas
- [ ] `Modelo_Matematico_Producao.md` escrito
- [ ] Baseline `8 passed` → `25 passed` na sua máquina **e** em outra
- [ ] Experimentos registrados (5 e 5 falhas)
- [ ] `requirements.txt` em UTF-8, com a versão decidida
- [ ] `exemplos/` no repositório; eco com acentos pelo PowerShell
- [ ] Segredos fora do Git; WLS testada com poucos testes
- [ ] Fase 4A: 151/1/0 pelo Java; tempo medido
- [ ] Branch `sprint-3` resolvido; CT20; registro do Git; Review e Retro; relatório da Sprint 3

## Fora da Sprint 3 (só sinalização; depende da A3)

Reposição com orçamento via solver (US14): um segundo modelo (`reposicao.py`), já desenhado no roteiro v4.6, Fase Q (`historico/`). A pendência F12 precisa ser resolvida antes do dashboard.
