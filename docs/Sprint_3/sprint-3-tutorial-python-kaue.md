# Tutorial — Sprint 3 (Python/Gurobi, Reposição, Deploy, Contrato e Facilitação) — Kauê

**Para:** Kauê Nogueira Carneiro · **Papel:** módulos de otimização, reposição (US14), publicação no Render (US20), Contrato Java ↔ Python, facilitação (Scrum Master) · **Revisão:** 27/10/2026 (v5.2: Contrato v2 aprovado; P2 = 13.0.3; deploy definitivo no sábado, A21; A15 corrigida) · **Substitui** a v5.1 (`historico/v5.1-27-10-antes-das-respostas/`), a versão da tarde (`historico/v5-26-10-antes-das-4-historias/`) e `historico/Tutorial_Sprint3_Python_Kaue.md`.
**Base:** `sprint-3-roteiro.md` (v5.1), `sprint-3-contrato-v2-proposta.md`, `sprint-3-decisoes-e-pendencias.md`.

> **O que foi verificado (26/10).**
> - **Produção** (`otimizador.py` v2), com **Python 3.14.0** e `gurobipy` **13.0.1 e 13.0.3** (licença de avaliação do `pip`):
>   - baseline da `main`: `8 passed`; módulo v2: **`25 passed`** nas duas versões;
>   - quebrar de propósito: 5 falhas (lucro → receita) e 5 falhas (sem eco);
>   - **151/1/0** (lucro 1.011,71) e 66/0 (438,24) confirmados pelo oráculo de força bruta.
> - **Reposição** (`reposicao.py`, **novo**: US14 entrou na Sprint 3, decisão A2b):
>   - **22 testes**; com os 25 da produção, **`47 passed`** nas duas versões do `gurobipy`;
>   - igual ao **oráculo de força bruta em 150 instâncias**;
>   - o exemplo didático foi feito à mão; o da demonstração bate com o SQL do Diogo;
>   - quebrar de propósito: 2, 1 e 9 falhas (§11.3).
> - **Java da reposição** (Fase 5) em `docs/Sprint_3/referencia/backend/`:
>   - `ReposicaoTest` 8/8 com PostgreSQL real;
>   - **ponta a ponta com o Gurobi real**: 63,22 e falta de 10,71, as contas à mão do §11.5.
> - **Deploy** (US20) em `docs/Sprint_3/referencia/deploy/`:
>   - imagem Docker montada e rodada **com os limites do Render free** (512 MB, 0,1 CPU): sobe em 83 s; pico de 133 MiB; recomendação 151/1/0 com o Gurobi **dentro do container** em 3,1 s; fluxo completo sem falhas;
>   - o comando que carrega o DDL no banco remoto foi testado (§12.4).
> - **Defeito achado e corrigido (A8):** o PowerShell envia um BOM antes do JSON; os dois módulos leem com `utf-8-sig`.
> - **Licença (corrigida em 27/10, A34):** você tem uma **WLS acadêmica**, válida até 25/09/2027, de uso **pessoal**. Desenvolvimento e testes rodam com a licença restrita do `pip`. **No Render, o solver só roda depois que a Gurobi autorizar por escrito** (A30): você envia a pergunta (`sprint-3-licenca-gurobi.md`, §3).
> - **Não verificado:** a criação real do *blueprint* no Render (precisa da sua conta) e Python 3.14.7.

## 0. O que você entrega e em que ordem

| Ordem | Entrega | Quando | Depende de | Libera |
|---|---|---|---|---|
| **1** | **Revisar o contrato oficial v2** (A10: aprovado em 27/10 e **já aplicado** a seu pedido; confira a seção 3 dele) e a P2 (**13.0.3**, decidida em 27/10) | **Ter 27** | — | **Lucas (Fase 3) e Leonardo (telas)** |
| 2 | **PR da Fase P + Q:** módulos de produção e de reposição, testes e exemplos (`47 passed`) | Ter 27 – **Qua 28** | Item 1 (§1 e §1.6) | Lucas (Fase 4A); você (Fase 5) |
| 3 | **Ensaio de deploy** (opcional, recomendado), numa conta pessoal, apagado no mesmo dia (A21) | Qua 28 ou Qui 29 | Fases 1–2 na `main` | Os problemas do Render aparecem antes do sábado |
| 4 | Ambiente reproduzido numa **2ª máquina** | Qua 28 | Item 2 | — |
| 5 | **Fase 4A em dupla** com o Lucas | Qui 29 | Lucas (Fase 3) | Tela 12 |
| 6 | **PR da Fase 5:** Java da reposição (`ReposicaoTest` 8/8 e o Python real) | **Sex 30** | Fase 3; repositório do Diogo (consulta 9) | Tela 13 |
| 7 | Plano de Testes: CT23, CT24, CT29, CTs de Python, **CT51–CT57** (reposição) e **CT67–CT70** (deploy) | Qui 29 – Sex 30 | — | Janela de testes |
| 8 | **Deploy definitivo** (conta, banco e serviço criados **neste dia**; banco expira em 30/11) e CT67–CT70 no ambiente publicado | **Sáb 31**, à noite (A32) | Tudo na `main`; quem cria a conta (decisão do PO, qui 29) | Janela de testes |
| 9 | Facilitação: Review, Retro, relatório, CT20, registro do Git, branch `sprint-3` | Ter 27 – Dom 01/11 | — | Encerramento |

**Pronto quando:**
1. o contrato oficial tem a v2 e o registro no histórico de revisões;
2. `47 passed` na sua máquina **e** na de outro integrante;
3. o `stdout` traz só o JSON;
4. a URL publicada está no README, com a data de expiração do banco (A21);
5. os PRs estão revisados;
6. Review e Retro estão registradas.

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

1. Leia `docs/Sprint_3/sprint-3-contrato-v2-proposta.md`. Ele já traz:
   - a §1 (Java ↔ Python), com as mensagens de erro que os módulos produzem, **inclusive a reposição (§1.6)**;
   - a §2 (REST), com estoque, pedido, recomendação, **reposição, dashboard e marketing**.

   Todos os formatos são **exatamente** os que o código de `referencia/` produz (conferido por teste).
2. Marque **30 minutos** com o Lucas e o Leonardo e decida os itens **[decidir]**:
   - quem calcula a `situacao` do estoque (frontend ou backend);
   - aceitar entrada com validade vencida (o PO decide).
3. Aplique a proposta ao contrato oficial `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`:
   - substitua a §1.1 a §1.3;
   - acrescente à §2 as mensagens padrão, o `PATCH` no CORS e as rotas de estoque, pedido e recomendação;
   - registre a linha na seção 3, "Histórico de revisões";
   - mantenha a §1.5 (eco).
4. **A11 (Compute Server) não bloqueia mais:** o Compute Server não é necessário (A34); a sua WLS roda dentro do próprio processo Python. O **Diogo** registra na Arquitetura (§2.1, §3.3, §5) e no Plano de Testes (§4.2), no PR de documentos dele (tutorial do Diogo, §9.2), que desenvolvimento e testes usam a **licença restrita do `pip`**, que existe a **sua WLS acadêmica** (pessoal, até 25/09/2027) e que, no Render, o solver só roda com autorização escrita da Gurobi (A30, A34). Você revisa. Os módulos já leem `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID` (§7).

5. **P2 decidida em 27/10: `gurobipy` 13.0.3** (as duas versões passaram nos 47 testes). Fixe 13.0.3 no `requirements.txt` (já está) e no README; o Diogo atualiza a Arquitetura (§2.1).
6. **Documente o modelo** em `docs/Apoio/Modelo_Matematico_Producao.md`:
   - conjuntos, parâmetros e a variável de decisão;
   - lucro por copo; estágios 1 e 2; escala inteira e verificação; lucro ≤ 0 → 0;
   - terminologia "Programação Linear Inteira pura";
   - os exemplos da §4;
   - e um segundo documento (ou seção) para a **reposição** (§11.1).

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

**Situação em 27/10 (A34, corrige a A22):** existe a **sua WLS acadêmica** (válida até 25/09/2027, só para você, trabalhos de disciplina). O `gurobipy` instalado pelo `pip` também vem com uma **licença restrita** que resolve problemas de tamanho limitado (página do `gurobipy` no PyPI). Os dois modelos do projeto cabem nela: 3 produtos e até 15 ingredientes, muito abaixo do limite. Todos os testes da auditoria, inclusive dentro do container de deploy, rodaram assim.

1. **Desenvolvimento e testes:** nada a configurar. `pip install -r optimization/requirements.txt` e pronto.
2. **Produção (Render):** **só com autorização escrita da Gurobi** (professor, 27/10; A30). Você responde ao e-mail de aprovação da sua licença com o texto de `sprint-3-licenca-gurobi.md` (§3). Com a autorização, cadastra no Render as variáveis do item 5; sem ela, vale o plano B (A31), e você faz a demonstração da otimização no seu computador, com a sua WLS.
3. **Banner:** o `OutputFlag 0` antes de `start()` evita que mensagens de licença vazem para o `stdout`. O teste `test_stdout_contem_somente_o_json_em_utf8` é a guarda.
4. **Segunda máquina:** peça a outro integrante que clone a branch, crie o próprio `venv`, rode `pip install -r optimization/requirements.txt` e `python -m pytest optimization/tests -q`. **Anote o resultado** (`47 passed`): é o que encerra a pendência "só funciona no PC do Kauê".
5. **A sua WLS:** os módulos leem `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID`; o Java as repassa ao processo filho. **Nunca** no Git. A licença acadêmica permite 2 sessões simultâneas (token de 5 min): não rode a suíte inteira com essas variáveis; teste com poucos casos (`-k "contrato or didatico"`).

## 8. Fase 4A, em dupla com o Lucas (quinta 29)

O seu papel é **o contrato e a prova**. O Java já está pronto em `referencia/` (tutorial do Lucas, §5); leia com ele o `ProcessoPython` e o `ProducaoService`.

1. **Eco:** com o perfil `dev`, `GET http://localhost:8080/api/teste/python` → `{"status":"sucesso","echo":"AÇAÍ (POLPA)"}`, **com acentos**. Se vier quebrado, o Java não está escrevendo a entrada em UTF-8.
2. **Recomendação real** com o seed de demonstração: compare com o `saida_demo.json` (151/1/0). Se divergir, o problema quase sempre está **na entrada** (saldo válido ou custo). Peça ao Lucas o JSON enviado.
3. **Python real pelo Java:** com `$env:MF_PYTHON` apontando para o Python do seu `venv`, o Lucas roda `PythonRealTest` e `ProcessoPythonTest` (tutorial dele, §5.2).
4. **Erros:** `status: "erro"` → `422`; código de saída ≠ 0 → `500` sem detalhe (CT44).
5. **Tempo** (P6): na auditoria, 188 ms ponta a ponta pelo Java; 3,1 s no container com 0,1 CPU. Confirme na sua máquina, 5 vezes, e anote mínimo, máximo e média:
   ```powershell
   Measure-Command { Get-Content -Raw -Encoding utf8 optimization\exemplos\entrada_demo.json | python optimization\app\otimizador.py | Out-Null }
   ```

## 9. Facilitação (Scrum Master)

| # | Tarefa | Quando | Evidência |
|---|---|---|---|
| 1 | **Branch `sprint-3` e `.env.example` (A15, A29):** o merge do `sprint-3` na `main` foi feito em 27/10 (PR #25) e conferido: a correção do PR #24 continua. Agora a `main` tem **dois** arquivos de exemplo: `backend/.env.example` (5 variáveis) e `backend/env.example` (sem o ponto, com comentários e as variáveis da Sprint 3). Faça assim: (1) junte os dois no `backend/.env.example` e apague o `env.example`, num PR; (2) crie todo branch novo a partir da `main` atualizada. Os documentos da auditoria já chegaram à `main` (PR #27); o `sprint-3` pode ser apagado, se a equipe concordar | Ter 27 | PR do `.env.example` |
| 2 | **Daily de 10 minutos** (seg a sáb): o que fiz, o que farei, o que me bloqueia. Bloqueio → resolver no mesmo dia (regra da equipe) | Diária | Notas curtas em `docs/Sprint_3/` |
| 3 | **Acompanhar:** a resposta do professor à mensagem (P3, `sprint-3-mensagem-professor.md`; A16, A17 e A20 já aprovadas em 27/10) | Ter 27 – Qua 28 | `resposta-professor.md` |
| 4 | **CT20:** um integrante sobe o projeto do zero **só** com o README; anote onde travou | Sáb 31 | Relato e PR no README |
| 5 | **Registro da perda do histórico do Git:** meia página com a data (27/09; primeiro commit `9e02498`), o motivo (conflitos de estrutura) e o que foi preservado | Até 31/10 | `docs/Sprint_3/` |
| 6 | **Retrospectiva da Sprint 2** (nunca realizada) e da Sprint 3, juntas em 30 min. Registre a contradição: as contribuições individuais da Sprint 2 marcam "Retrospectiva" como feita, mas o relatório diz que não houve | Dom 01/11 | Ata no padrão da `sprint-1-ata-retrospectiva.md` |
| 7 | **Sprint Review com demonstração** (nunca houve nas Sprints 1 e 2): o fluxo do §1 do roteiro, com os dois perfis | Dom 01/11 | Ata e prints |
| 8 | **Relatório da Sprint 3** em `docs/Sprint_3/`: planejado × entregue (use a matriz), evidências, riscos e decisões. Sem `[PREENCHER]` | Dom 01/11 | Arquivo |
| 9 | **Janela de testes (01–06/11):** dividir a bateria por pessoa e perfil e priorizar os caminhos do Termo de Aceite **no ambiente publicado** (`sprint-3-casos-de-teste-propostos.md`, §3). Acordar o serviço do Render antes de cada sessão | Dom 01/11 | Planilha ou tabela de execução |

## 10. Entrega da Fase P (e da Q, no mesmo PR ou logo depois)

```powershell
git add optimization
git commit -m "feat: modelo de lucro em dois estagios, consumo, modo eco e entrada com BOM"
git push -u origin feature/python-modelo-lucro
```

No PR, inclua:
- `25 passed` (e, com a Fase Q, `47 passed`), e o resultado da 2ª máquina;
- os experimentos da §5 e da §11.3;
- a menção a receita → lucro (F9);
- o link do PR do Contrato v2.

Peça revisão ao **Lucas** (consome o contrato) e ao **Diogo** (consome as entradas).

## 11. Fase Q e Fase 5 — Reposição com orçamento (US14)

US14 é **Must** e entrou na Sprint 3 (A2b). As regras foram decididas em 14/10 (roteiro v4.6, Fase Q); os pontos "a formalizar" foram fechados na A19.

### 11.1 O modelo (para o `Modelo_Matematico_Reposicao.md`)

**Entrada (por ingrediente `i`, escolhido pelo Java):** `c_i` = consumo no período; `S_i` = saldo **válido**; `qmin_i` = quantidade mínima; `k_i` = custo unitário. Mais o orçamento `B`.

**Duas quantidades por ingrediente**, arredondadas **para cima** em centésimos (nunca abaixo do mínimo):
- **mínimo a comprar:** `min_i = max(0, qmin_i − S_i)` (levar o estoque ao mínimo);
- **necessidade:** `nec_i = max(0, c_i + qmin_i − S_i)` (repetir o consumo do período e manter o mínimo).

**Se `Σ k_i·min_i > B`:** orçamento insuficiente. A resposta traz os mínimos e quanto falta (sem solver).

**Senão** (variáveis `x_i` inteiras, em centésimos):

| Estágio | Objetivo | Restrições |
|---|---|---|
| 1 | maximizar `t`, a **menor cobertura** `x_i / nec_i` | `min_i ≤ x_i ≤ nec_i`; `x_i ≥ t·nec_i`; `Σ k_i·x_i ≤ B` |
| 2 | maximizar o **gasto** `Σ k_i·x_i` (usar a sobra do orçamento) | as mesmas, com `x_i ≥ teto(t*·nec_i)` |
| 3 (desempate) | do mais consumido ao menos consumido (empate: menor id), maximizar `x_i` | mantendo o gasto do estágio 2 e fixando os já decididos |

**Por que três estágios:** o estágio 1 reparte o orçamento "de forma justa" (decisão de 14/10). O 2 evita deixar dinheiro parado quando ainda dá para comprar. O 3 deixa o resultado **sempre igual** para a mesma entrada (como o estágio 2 da produção).

**Conferência exata:** tudo em inteiros (centésimos e centavos; o gasto em 1e-4 R$). A cobertura é conferida com **frações exatas**. Se o solver devolver algo que a conta exata não confirma, é `FalhaTecnica` (código ≠ 0): nunca um plano errado.

**Exemplo didático (conta à mão):**

| Ingrediente | Consumo | Saldo | Mínimo | Custo | `min` | `nec` |
|---|---|---|---|---|---|---|
| Açaí | 0,800 | 0,300 | 0,500 | 18,50 | 0,20 | 1,00 |
| Granola | 0,300 | 0 | 0,100 | 22,00 | 0,10 | 0,40 |
| Leite condensado | 0,200 | 0,100 | 0,100 | 12,00 | 0 | 0,20 |

- **Orçamento 50:** cobre tudo; 29,70.
- **Orçamento 5:** não cobre os mínimos (5,90); faltam 0,90.
- **Orçamento 20:**
  - com 66% de cada necessidade: açaí 0,66 (12,21), granola teto(26,4) = 0,27 (5,94), leite teto(13,2) = 0,14 (1,68), total 19,83;
  - com 67%: 20,015, passa de 20. Então **`t* = 0,66`**;
  - sobram 0,17: só o leite (+0,01 = 0,12) cabe;
  - **resultado: 0,66 / 0,27 / 0,15 = 19,95.** É o que o `test_exemplo_didatico_via_subprocesso` confere.

### 11.2 Testes primeiro, depois o código

```powershell
copy docs\Sprint_3\referencia\python\tests\oraculo_reposicao.py optimization\tests\
copy docs\Sprint_3\referencia\python\tests\test_reposicao.py optimization\tests\
copy docs\Sprint_3\referencia\python\exemplos\*reposicao*.json optimization\exemplos\
python -m pytest optimization/tests -q
```

**Você deve ver falhas** (o módulo ainda não existe). Agora copie `docs\Sprint_3\referencia\python\app\reposicao.py` para `optimization\app\` e rode de novo.

**Você deve ver:** `47 passed` (25 da produção + 22 da reposição; 26/10, nas duas versões do `gurobipy`).

| Teste | O que prova |
|---|---|
| `test_exemplo_didatico_via_subprocesso` | O exemplo do §11.1 (19,95), via `stdin`/`stdout` |
| `test_orcamento_que_cobre_toda_a_necessidade` / `…insuficiente…` / `…exatamente_igual_aos_minimos…` / `…zero…` | Os limites do orçamento |
| `test_quantidades_sao_arredondadas_para_cima_em_centesimos` | 0,001 vira 0,01 |
| `test_desempate_favorece_o_ingrediente_mais_consumido` | O estágio 3 |
| `test_tabela_ordenada_por_custo_estimado_decrescente` | Ordem decidida em 14/10 |
| `test_coincide_com_o_oraculo_em_150_instancias` | Força bruta com frações exatas, **sem solver** |
| `test_exemplo_demo_bate_com_a_saida_guardada` | O seed de demonstração (60 dias): só o morango, 0,74 kg (11,10) |
| `test_entradas_invalidas_…` (10 casos), `…json_invalido_e_entrada_com_bom`, `…stdout_contem_somente_o_json` | Erros tratados, BOM, nada além do JSON |

O `reposicao.py` **reaproveita** do `otimizador.py` o ambiente do Gurobi (`_criar_env`) e as conversões. A licença fica num lugar só.

### 11.3 Quebrar de propósito (resultado de 26/10, só `test_reposicao.py`)

| Troca temporária em `reposicao.py` | Resultado |
|---|---|
| `"min": _teto_div(max(0, q_m - s_m), 10)` → `"min": max(0, q_m - s_m) // 10` (arredonda para baixo) | **2 failed**, 20 passed |
| `for i in _ordem_desempate(variaveis):` → `for i in []:` (sem estágio 3) | **1 failed**, 21 passed |
| `m.setObjective(gasto_expr, GRB.MAXIMIZE)` → `m.setObjective(0, GRB.MAXIMIZE)` (sem estágio 2) | **9 failed**, 13 passed |

Desfaça e confirme `47 passed`.

### 11.4 Rodar "como o Java vai rodar"

```powershell
cmd /c "python optimization\app\reposicao.py < optimization\exemplos\entrada_reposicao_demo.json"
```

**Você deve ver** `"orcamento_suficiente": true, "custo_total": 11.1` e o morango com `"quantidade_sugerida": 0.74`. O resto vem com 0.

### 11.5 Fase 5 — o Java da reposição (sexta 30)

**Copie** de `docs/Sprint_3/referencia/backend/`: `reposicao/ReposicaoRepository` (o SQL é a consulta 9 do Diogo), `reposicao/ReposicaoService`, `reposicao/ReposicaoController` e o teste `ReposicaoTest`. Branch: `feature/backend-reposicao`.

O que ler com atenção no `ReposicaoService`:
- **período** = os últimos N dias contando hoje, em São Paulo;
- **8** ingredientes até 30 dias, **15** acima; só os que tiveram consumo;
- **saldo válido**; pedidos **cancelados contam** (A18);
- **sem pedidos no período, o Python não é chamado**;
- a resposta mantém a ordem do Python (maior custo primeiro) e acrescenta nome, unidade, consumo, saldo e mínimo.

```powershell
cd backend
.\mvnw test "-Dtest=ReposicaoTest"
$env:MF_PYTHON = "C:\caminho\Mente_Fria\optimization\.venv\Scripts\python.exe"
.\mvnw test "-Dtest=PythonRealTest"
```

**Você deve ver:** `ReposicaoTest` com 8 testes e `PythonRealTest` com 3, sem falhas. O `reposicaoContaAMao` é o cenário do `ReposicaoTest` com o **Gurobi real**:
- orçamento 100 → **63,22** (granola 25,52; açaí 14,80; banana 12,10; leite condensado 10,80);
- orçamento 30 → não cobre os mínimos (40,71); **faltam 10,71**.

## 12. Publicação no Render (US20) — com o Lucas

**Por que você:** o repositório é seu (`KaueNCarneiro/Mente_Fria`). O Render e o GitHub Pages precisam de quem administra o repositório. O Lucas acompanha como PO.

**Decisão A21:** backend em Docker no Render (free, região Virginia), PostgreSQL 16 free no Render, frontend no GitHub Pages por Actions (tutorial do Leonardo, §9).

**Limites do plano free** (documentação do Render, conferida em 26/10):
- o **banco expira 30 dias depois de criado**, com 14 dias de carência;
- o serviço dorme após 15 min sem uso e leva cerca de 1 min para voltar;
- 512 MB de RAM e 0,1 de CPU.

### 12.1 Os arquivos (PR `chore: deploy no Render`)

Copie de `docs/Sprint_3/referencia/deploy/` para a **raiz** do repositório: `Dockerfile`, `.dockerignore` e `render.yaml`.

| Arquivo | O que faz |
|---|---|
| `Dockerfile` | Estágio 1: compila o backend com o Maven do projeto. Estágio 2: Python 3.14 + `gurobipy` 13.0.3 + o JRE 25 da imagem oficial Temurin; usuário sem privilégio; heap limitada para sobrar memória ao Python |
| `.dockerignore` | Lista do que **pode** entrar (`backend/` e `optimization/app/`). `.env`, `docs/`, `.git` e `venv` ficam de fora |
| `render.yaml` | O serviço web (Docker, *health check* em `/api/saude`) e o banco. `DB_*` vêm do banco; `JWT_SECRET` é gerado pelo Render; `CORS_ALLOWED_ORIGIN` é perguntado na criação. **Não há `GRB_*` no arquivo**: só você as cria no painel, e só com a autorização da Gurobi (A30, A34). **`OTIMIZADOR_HABILITADO=false`** (plano B1, A31): com a autorização, troque para `true` no painel |

### 12.2 Testar a imagem na sua máquina antes (recomendado)

Na raiz do repositório, com o Docker ligado:

```powershell
docker build -t mente-fria-backend .
docker network create mf-rede
docker run -d --name mf-db --network mf-rede -e POSTGRES_PASSWORD=teste -e POSTGRES_DB=mente_fria postgres:16
docker cp bd/Script_DDL.sql mf-db:/tmp/ddl.sql
docker exec mf-db psql -U postgres -d mente_fria -q -f /tmp/ddl.sql
docker run -d --name mf-app --network mf-rede --memory=512m --cpus=0.1 -p 10000:10000 -e PORT=10000 `
  -e DB_HOST=mf-db -e DB_PORT=5432 -e DB_NAME=mente_fria -e DB_USER=postgres -e DB_PASSWORD=teste `
  -e JWT_SECRET=um-segredo-local-com-mais-de-32-caracteres -e CORS_ALLOWED_ORIGIN=http://127.0.0.1:5500 mente-fria-backend
docker logs -f mf-app
```

Espere `Started BackendApplication` (Ctrl+C para sair do log) e depois:

```powershell
Invoke-RestMethod http://localhost:10000/api/saude
```

**Você deve ver** (auditoria, 26/10, a mesma configuração):
- o build em cerca de 1 a 2 minutos (imagem de ~590 MB);
- "Started BackendApplication" depois de ~60–80 s (é o 0,1 de CPU);
- `status ok, banco ok`.

Para limpar: `docker rm -f mf-app mf-db; docker network rm mf-rede`. **Não** use o `mente-fria-db` nesse teste.

### 12.3 Criar no Render pelo *blueprint* (ensaio na quarta 28 ou quinta 29; definitivo no sábado 31)

Pré-requisitos: as Fases 1–2 na `main` e o PR do §12.1 aprovado.

**Por que duas vezes (A21, 27/10):** o banco gratuito expira **30 dias depois de criado**, e cada conta só pode ter **um** banco gratuito ativo (documentação do Render, lida em 27/10).
- **Ensaio:** numa **conta pessoal** de um integrante, com as Fases 1–2. Faça os passos abaixo e o §12.5 (itens 1 a 3). **No mesmo dia, apague o banco e o serviço.**
- **Definitivo:** na conta que o PO escolher (quinta 29), **no sábado 31 à noite**, com tudo na `main`. O banco vale até **30/11**, no mesmo horário em que foi criado; 30/11 é a data-limite dada pelo professor (A32). Logo depois do seed, faça o backup (§12.6). Não pode ser depois de 31/10: a janela de testes é no ambiente publicado.

1. Crie a conta no Render entrando **com o GitHub** e dê acesso ao repositório.
2. No painel do Render: **New > Blueprint** → escolha o repositório. O Render encontra o `render.yaml` na raiz.
3. Quando ele perguntar o `CORS_ALLOWED_ORIGIN`, informe a origem do Pages, **sem barra e sem caminho**: `https://kauencarneiro.github.io` (confira o endereço exato com o Leonardo).
4. Revise a lista do que será criado (um web service e um banco) e confirme.
5. **No deploy definitivo, anote no README a data de criação do banco e a de expiração (+30 dias).**

Esses passos seguem a documentação do Render (infrastructure-as-code e blueprint-spec, lidas em 26/10). A tela real pode ter nomes um pouco diferentes; **não foram executados na auditoria**, porque dependem da sua conta.

**Deploy automático:** por padrão ("On Commit"), cada merge na `main` publica de novo.

### 12.4 Criar o schema no banco do Render (uma vez)

O backend **não** cria tabelas. No painel do banco, copie a **External Database URL** (página *Info*). Na raiz do repositório:

```powershell
$URL = "<External Database URL>?sslmode=require"      # nunca comite esta URL: tem a senha
docker run --rm -v "${PWD}\bd:/bd" postgres:16 psql "$URL" -v ON_ERROR_STOP=1 -q -f /bd/Script_DDL.sql
docker run --rm postgres:16 psql "$URL" -tA -c "SELECT nome, quantidade_estoque FROM ingrediente WHERE id = 1;"
```

**Você deve ver:** `Açaí (polpa)|0.000`.

**Por que assim:** o `psql` roda num container e lê o **arquivo** (montado com `-v`). Pelo pipe do PowerShell, os acentos do seed poderiam se perder. **Testado em 26/10** contra um PostgreSQL acessado pela rede; com o Render, não. O `?sslmode=require` vem da documentação do Render: ele recusa conexão externa sem TLS.

**Para a demonstração:**
1. Crie o Administrador e o Funcionário **pelas telas** publicadas.
2. Depois rode o seed com o mesmo comando, trocando `/bd/Script_DDL.sql` por `/bd/seed_demo.sql`.

### 12.5 Conferir (CT67–CT70)

| # | O que | Esperado |
|---|---|---|
| 1 | `https://<seu-servico>.onrender.com/api/saude` | `{"status":"ok","banco":"ok"}` (a primeira vez pode levar ~1 min) |
| 2 | `https://<seu-servico>.onrender.com/api/teste/python` | `401` (o eco não existe em produção) |
| 3 | No Pages: cadastro, login, ingredientes | Sem erro de CORS no F12 |
| 4 | Recomendação com o seed de demonstração | **Plano B1 (A31), sem autorização da Gurobi:** `503` com "A recomendação está disponível só na demonstração local da equipe." **Com autorização** (variável em `true` e `GRB_*` criadas por você): 151/1/0 |
| 5 | Logs do serviço no painel | Sem `ERROR`; sem senha ou token impressos |

**Sábado 31:** com tudo na `main`, o fluxo inteiro do CT68 na URL publicada. Guarde prints com a URL visível em `docs/Sprint_3/evidencias/`.

**Licença (A30, resposta do professor):** enquanto a Gurobi não autorizar por escrito, o solver **não** roda no Render. O item 4 da tabela acima vale só com a autorização; sem ela, vale o plano B (`sprint-3-licenca-gurobi.md`, §4).

### 12.6 Backup e restauração do banco (pedido do professor; A32)

**Por quê:** o professor considera **30/11** a data-limite do sistema publicado e pediu um backup **com a restauração testada**. O banco gratuito expira 30 dias depois de criado, **no mesmo horário**: criado no sábado 31 às 9 h, ele para no dia 30/11 às 9 h. Por isso, crie o banco definitivo **o mais tarde possível no sábado**, à noite, e tenha o backup pronto.

**Fazer o backup** (na raiz do repositório; a pasta `backup` fica **fora do Git**, porque o arquivo tem os dados e os hashes de senha):

```powershell
$URL = "<External Database URL>?sslmode=require"      # nunca comite esta URL: tem a senha
New-Item -ItemType Directory -Force backup | Out-Null
docker run --rm -v "${PWD}\backup:/backup" postgres:16 pg_dump "$URL" -Fc --no-owner --no-privileges -f /backup/mente_fria_2026-10-31.dump
```

**Testar a restauração** num banco local descartável (**não** use o `mente-fria-db`):

```powershell
docker run -d --name mf-restore -e POSTGRES_PASSWORD=teste -e POSTGRES_DB=mente_fria postgres:16
docker cp backup\mente_fria_2026-10-31.dump mf-restore:/tmp/b.dump
docker exec mf-restore pg_restore -U postgres -d mente_fria --no-owner --no-privileges --exit-on-error /tmp/b.dump
docker exec mf-restore psql -U postgres -d mente_fria -tA -c "SELECT count(*) FROM pedido;"
docker rm -f mf-restore
```

**Você deve ver:** o `pg_restore` sem erro e o mesmo número de pedidos do sistema publicado. Registre em `docs/Sprint_3/evidencias/` (é o critério do adendo, item 5).

**Restaurar num banco novo do Render** (se o banco expirar ou for recriado): crie o banco, pegue a nova URL e rode `pg_restore -d "$URL_NOVO" --no-owner --no-privileges --exit-on-error` com o arquivo, como acima, em vez de rodar o DDL e o seed. O `--no-owner` existe porque o usuário do banco novo é outro.

**Quando fazer backup:** logo depois do seed (sábado 31); ao fim da janela de testes (06/11); uma vez por semana; e em **29/11**, véspera da data-limite.

**O que já foi verificado (27/10, auditoria):** com containers locais PostgreSQL 16, a cópia (`pg_dump -Fc`) do banco com o DDL e o `seed_demo.sql` foi restaurada num banco novo, de **outro dono**, com `--no-owner --no-privileges`. Ficaram iguais: as contagens das 7 tabelas, a soma dos saldos, a regra do saldo (0 violações) e o texto `Açaí (polpa)`. Depois da restauração, um cadastro novo recebeu o id seguinte, sem colisão. **Contra o Render, não foi testado** (o `pg_dump` pela URL com `sslmode=require` depende da conta).

### 12.7 Demonstração local da otimização (plano B1, A31)

Sem a autorização da Gurobi, **você** demonstra as Telas 12, 13 e 14 no seu computador, com a sua WLS. O passo a passo está em `sprint-3-licenca-gurobi.md`, §5. Ensaie uma vez antes da apresentação.

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `JSON de entrada inválido` no pipe do PowerShell | Módulo antigo, sem `utf-8-sig` (o PowerShell envia BOM) | Use o `otimizador.py` de `referencia/`, ou `cmd /c "… < arquivo.json"` |
| Acentos viram `?` no eco | Codificação do pipe | Mesma solução; ou `cmd /c` com redirecionamento |
| `JSONDecodeError` nos testes | Banner da licença no `stdout` (fallback do `gurobi.lic`) | `OutputFlag 0` antes de `start()`; preferir as variáveis WLS |
| `too many sessions` / erro de licença WLS | Mais de 2 sessões | Não rode a suíte inteira com `GRB_*` (§7, passo 2) |
| `Model too large for size-limited license` | Instância grande demais para a licença de avaliação | Não deve acontecer com os dados do projeto. Se acontecer, guarde a entrada e avise (A22) |
| `47 passed` vira `25 passed` | `reposicao.py` ou `test_reposicao.py` não copiados | §11.2 |
| Reposição sempre com "não cobre nem os mínimos" | Orçamento menor que o custo dos mínimos (é o comportamento certo) | Confira o `valor_faltante` |
| Deploy no Render falha no build | `mvnw` com fim de linha CRLF, ou arquivo fora do `.dockerignore` | O `backend/.gitattributes` já força LF no `mvnw`; confira o `.dockerignore` |
| `/api/saude` do Render demora ~1 min | O serviço free estava dormindo | Normal; abra a URL antes de demonstrar |
| Erro de SSL ao carregar o DDL no Render | Faltou o `?sslmode=require` | §12.4 |
| `FalhaTecnica` (código ≠ 0) | A verificação inteira não conferiu | **Não ignore.** Guarde o JSON de entrada e investigue |
| `requirements.txt` vazio depois da conversão | Pipe sem parênteses | Recupere com `git checkout -- optimization/requirements.txt` e use `(Get-Content …) \| Set-Content …` |

## Checklist final

- [ ] Contrato v2 (aprovado em 27/10) aplicado ao oficial (com §1.6, §2.4 do descarte e §2.7–§2.9); `gurobipy` 13.0.3 (P2)
- [ ] `Modelo_Matematico_Producao.md` e o modelo da reposição escritos
- [ ] Baseline `8 passed` → `47 passed` na sua máquina **e** em outra
- [ ] Experimentos registrados (produção: 5 e 5 falhas; reposição: 2, 1 e 9)
- [ ] `requirements.txt` em UTF-8, com a versão decidida
- [ ] `exemplos/` no repositório (inclusive os da reposição); eco com acentos pelo PowerShell
- [ ] Fase 4A: 151/1/0 pelo Java; tempo confirmado
- [ ] Fase 5: `ReposicaoTest` 8/8; `PythonRealTest` 3/3
- [ ] Render: ensaio feito e apagado (opcional); deploy definitivo no sábado; schema carregado; URL e data de expiração do banco no README; CT67–CT70
- [ ] `.env.example` com os comentários do `env.example`; branch `sprint-3` apagado (se a equipe concordar); CT20; registro do Git; Review e Retro; relatório da Sprint 3

## Fora da Sprint 3 (decisão A2b)

Alertas de reposição e de validade (US15/US16), clientes (US13), CSV (US19), porção padrão (US26) e cancelamento de pedido (US27).
