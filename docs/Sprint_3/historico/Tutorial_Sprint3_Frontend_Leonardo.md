# Tutorial — Sprint 3 (Frontend) do zero ao PR

**Para:** Leonardo Fagundes Oliveira · **Papel:** frontend (HTML, CSS e JavaScript)
**Base:** `sprint-3-roteiro.md` v4.6 (Fases 2, 3 e 4A, lado do frontend) + `Analise_Roteiro_Sprint3_v4.6.md` + `Registro_de_Decisoes_e_Pendencias_Sprint3.md` · **Atualizado em:** 14/10/2026

> **O que foi verificado e o que não foi.** Fiz a **divisão do `app.js` de verdade**, sobre o código real da `main` (clonado do GitHub), e comparei o **HTML de 25 telas e estados** antes e depois, em um DOM simulado (jsdom): **idêntico**. O novo **guarda de permissões** também foi executado (tabela da Parte 4). A lógica dos formulários novos (movimentação, motivo do zero, total do pedido) foi verificada em Node. **Não foi executado:** o navegador, o Playwright (seus 22 testes) e a integração com o backend real (as rotas novas ainda não existem). As **formas das respostas das rotas novas são PROPOSTA** (Contrato REST v2, ainda a fechar com o Lucas).

## 0. Onde você está e o que entrega

**Estado real da `main`** (commit `ac028cd`): o `frontend/` não muda desde 02/10. O `app.js` tem **235 linhas** em um arquivo só; há 22 verificações Playwright com API simulada; o menu mostra Dashboard, Porção Padrão, Estoque, Produção, Pedidos, Clientes, Reposição e Marketing como "indisponíveis"; a tela `#/comunicacao` tem 3 cartões (saúde, ingredientes, Java → Python).

| Ordem | Entrega | Quando | Depende de |
|---|---|---|---|
| 1 | **Baseline** (`npm test` = 22 OK) e retrato das telas | Hoje | Nada |
| 2 | **Dividir o `app.js` em módulos** | Hoje e 15/10 | Nada (**não espera o backend**) |
| 3 | **Aceite real da Fase 2** com o backend do Lucas (16 evidências) | 15 a 16/10 | Lucas (Fases 1 e 2) |
| 4 | **Guarda de permissões por lista** | 16/10 | Item 2 |
| 5 | **Extensão:** telas Estoque (10/16), Pedido (11/17), Produção (12) | 17 a 18/10, se couber | Lucas (Fases 3 e 4A), Diogo, Kauê |

**Pronto quando…** (1) o `app.js` está dividido, os 22 testes passam e os retratos das telas são idênticos; (2) o aceite real da Fase 2 tem as 16 evidências; (3) nenhuma tela mostra termo técnico de otimização; (4) `frontend/README.md` descreve o estado real.

## 1. Por que fazemos assim

| Decisão | Por quê |
|---|---|
| **Dividir o `app.js` antes das telas novas** (roteiro, Fase 2) | Hoje, funcionários, ingredientes, produtos, login e comunicação estão no **mesmo arquivo**. Mais 4 a 8 telas no mesmo arquivo, com o Lucas e você mexendo ao mesmo tempo, é receita de conflito (o problema da Sprint 1). Módulos separados = um arquivo por tela |
| **Refatorar sem mudar comportamento** | Refatoração boa é a que **não muda nada que o usuário vê**. Por isso há uma prova mecânica (retratos idênticos) e os 22 testes |
| **Permissões por lista** (Parte 4) | O guarda atual **bloqueia** qualquer rota do Funcionário com segundo segmento (quebraria `pedidos/novo`, que o Contrato permite) e **não bloqueia** `dashboard` nem `producao` (só o `403` do backend segura). A lista permite só o que está escrito |
| **Aceite real antes de novas telas** (regra de "gate" do roteiro) | A Sprint 2 provou a interface só com respostas simuladas; uma tela só "conta" quando conversa com o backend real |
| **Cartão do Python continua** em `#/comunicacao` | O Hello World da ponte Java ↔ Python (`echo`) só sai quando a recomendação real for validada |
| **Texto da Tela 12 fala em "lucro"** (e nunca em termos técnicos) | O sistema passou a maximizar lucro; a US11 proíbe termos de otimização na tela |
| **Pedido sem cliente por enquanto** | Cadastro de clientes (US13, Tela 18) é da Sprint 4; o `cliente_id` é opcional |

## 2. Preparar e fazer o baseline

```powershell
cd C:\caminho\Mente_Fria
git checkout main
git pull
git checkout -b feature/frontend-modulos-sprint3
cd frontend
npm install
npm test
```
**Você deve ver:** as verificações `OK ...` e `22 verificações passaram. Integração HTTP simulada.` (O frontend precisa estar na porta 5500: `py -m http.server 5500 --bind 127.0.0.1` em outro terminal.) **Se já falhar aqui, pare.**

**Ferramenta de verificação sem navegador** (complementa o Playwright). Copie a pasta `referencia_frontend/ferramentas` para `frontend/ferramentas` e instale o `jsdom` **sem** alterar o `package.json`:
```powershell
npm install jsdom --no-save
node ferramentas\retratar_telas.mjs . retrato_antes.json
```
**Você deve ver:** `OK: 25 retratos -> retrato_antes.json`. Esse arquivo é a "fotografia" das telas **antes** da refatoração (login, cadastro, listas, formulários, bloqueio do Funcionário, `session-expired`). **Não comite** os `.json`.

## 3. Dividir o `app.js` em módulos

### 3.1 O mapa (linhas do `app.js` real)

| Linhas | O que é | Vai para |
|---|---|---|
| 1–2 | `import` de `api.js` e `validation.js` | cada módulo importa o que usa |
| 3–6 | `root`, `money`, `amount`, `unit` | `js/ui.js` |
| 7 | `let revision, flash` | **`js/state.js`** (vira `state.revision` e `state.flash`) |
| 8–21 | `node`, `link`, `button`, `logo`, `notice` | `js/ui.js` |
| 22–23 | `go`, `admin` | `js/nav.js` |
| 24–60 | `field`, `errors`, `formBase`, `bindForm` | `js/ui.js` |
| 61–91 | `accountValidation`, `authPage` | `js/screens/auth.js` |
| 92–105 | `shell` (menu e topo) | `js/screens/shell.js` |
| 106–119 | `heading`, `loading`, `table`, `badge` | `js/ui.js` |
| 120–126 | `employees` | `js/screens/funcionarios.js` |
| 127–148 | `ingredients` | `js/screens/ingredientes.js` |
| 149–154 | `confirmRemoval` | `js/ui.js` |
| 155–198 | `products` | `js/screens/produtos.js` |
| 199–214 | `communication` | `js/screens/comunicacao.js` |
| 215–235 | `render`, os `addEventListener` e a chamada inicial | `js/router.js` |
| (novo) | `import './router.js';` | `js/app.js` (continua sendo o arquivo que o `index.html` carrega) |

**Ordem das dependências (sem ciclos):** `state` ← `nav` ← `ui` ← `screens/*` ← `router` ← `app`. O `index.html` **não muda**.

### 3.2 Os quatro cuidados (os problemas que aparecem de verdade)

| # | Cuidado | Por quê |
|---|---|---|
| 1 | **`flash` e `revision` viram `state.flash` e `state.revision`** (arquivo `state.js`: `export const state = {revision: 0, flash: ''};`) | Em ES modules, uma variável `let` de um arquivo **não pode ser reatribuída por outro**. Um objeto compartilhado pode |
| 2 | **`go()` e o botão "Tentar novamente" não chamam mais `render()`**: disparam `window.dispatchEvent(new Event('rerender'))`, e o `router.js` escuta (`window.addEventListener('rerender', ...)`) | Se `ui.js` e `nav.js` importassem o `router.js` (que importa as telas, que importam o `ui.js`), teria um **ciclo** de importação |
| 3 | **Todo item de topo usado fora do arquivo precisa de `export`** | Sem `export`, o outro módulo não enxerga |
| 4 | **Cuidado com falso positivo ao calcular imports:** em `products()`, `ingredients` é uma **variável local** (`const ingredients = await loading(...)`), e **não** a tela de ingredientes | Importar `ingredients` em `produtos.js` seria desnecessário e confuso |

### 3.3 Faça um módulo por vez (com ponto de checagem)
Ordem sugerida: `state.js` → `ui.js` → `nav.js` → `router.js` + `app.js` mínimo → as telas, uma a uma. **Depois de cada módulo:**
```powershell
node --check js\ui.js            # sintaxe do módulo (repita para cada arquivo novo)
npm test                         # os 22 testes continuam passando
```
**Ao terminar**, a prova mecânica:
```powershell
node ferramentas\retratar_telas.mjs . retrato_depois.json
node ferramentas\comparar_retratos.mjs retrato_antes.json retrato_depois.json
```
**Você deve ver:** `IGUAIS: o HTML, o título e a rota de todas as telas são idênticos.` Se aparecer `DIFERENÇAS em: ...`, o nome da tela mostra onde procurar (quase sempre é um `flash` que ficou sem `state.` ou uma função sem `export`).

A pasta `referencia_frontend/js/` traz **a divisão já feita e verificada**: use para **comparar com a sua** (por exemplo, `fc` no CMD ou o diff do VS Code), não para copiar sem entender. Se travar, copie um módulo, entenda-o e siga.

**Por que não aproveitar para consertar coisas?** Porque aí o retrato mudaria e você não saberia se foi bug ou melhoria. **Refatore e melhore em PRs separados.**

Commit: `git add frontend/js` / `git commit -m "refactor(frontend): divide o app.js em modulos sem mudar comportamento"`. **Abra o PR** (mostre `IGUAIS` e os 22 OK).

## 4. Guarda de permissões por lista

**O problema (verificado):** o guarda atual em `render()` é
```js
if(!admin()&&(section==='funcionarios'||action)){ ... go('inicio'); return; }
```
**Resultado real, perfil Funcionário:**

| Rota | Guarda antigo | Guarda novo |
|---|---|---|
| `#/pedidos/novo` | **bloqueada** (erro: o Contrato permite) | liberada |
| `#/clientes/novo` | **bloqueada** (erro) | liberada |
| `#/estoque` | liberada | liberada |
| `#/funcionarios` | bloqueada | bloqueada |
| `#/produtos/novo` | bloqueada | bloqueada |
| `#/ingredientes/editar/1` | bloqueada | bloqueada |
| `#/dashboard` | **liberada** (erro: tela de Administrador) | bloqueada |
| `#/producao` | **liberada** (erro) | bloqueada |
| Administrador | tudo liberado | tudo liberado |

**A correção** (em `nav.js`, no módulo que você acabou de criar; e no `router.js`):
```js
const SO_ADMIN = new Set(['funcionarios', 'dashboard', 'porcao-padrao', 'producao', 'reposicao', 'marketing']);
const FUNCIONARIO = {inicio: [undefined], ingredientes: [undefined], produtos: [undefined],
                     estoque: [undefined], pedidos: [undefined, 'novo'], clientes: [undefined, 'novo']};
export const rotaPermitida = (section, action) =>
  admin() || (!SO_ADMIN.has(section) && Boolean(FUNCIONARIO[section]?.includes(action)));
// router.js:  if(!rotaPermitida(section,action)){ flash... go('inicio'); return; }
```
**Valide:** `node ferramentas\testar_guarda.mjs .` e compare com a coluna "novo". **Lembre:** isso é só experiência de uso; **a segurança de verdade é o `403` do backend** (CT05, CT08). Teste no Playwright (Parte 7).

## 5. Gate da Fase 2: aceite com o backend **real**

Os seus 22 testes provam que a interface **segue o contrato**; não provam que o backend **cumpre**. **Comece só quando o Lucas avisar** que as Fases 1 e 2 passaram nos 10 itens dele.

**Preparação:** banco no ar, backend na porta 8080 (**de dentro de `backend/`**, com o `.env`), `frontend/js/config.js` com `API_BASE_URL = 'http://localhost:8080'`, e o frontend em `http://127.0.0.1:5500` (**sempre `127.0.0.1`**, não `localhost`: são origens diferentes para o navegador). No Chrome: **F12**, aba **Network**, **Preserve log**.

| # | Ação na tela | O que conferir no Network | Esperado |
|---|---|---|---|
| 1 | `#/comunicacao`: testar saúde e ingredientes | `GET /api/saude`, `GET /api/ingredientes` | `200`; ingredientes **sem login** → `401` |
| 2 | `#/cadastro`: criar o Administrador | `POST /api/auth/registro`, depois `POST /api/auth/login` | `201` e `200`; o login traz `token`, `perfil` **e `nome`** |
| 3 | Tentar cadastrar de novo | `POST /api/auth/registro` | `409` "O cadastro do Administrador já foi realizado." na tela |
| 4 | Sair e entrar com senha errada | `POST /api/auth/login` | `401` "E-mail ou senha inválidos." |
| 5 | `#/funcionarios/novo` com e-mail repetido | `POST /api/usuarios` | `409`, mensagem **no campo e-mail** |
| 6 | Cadastrar um funcionário | **Request payload** | exatamente `{nome, email, senha}` (sem `perfil` nem `ativo`) |
| 7 | `#/ingredientes` | resposta de `GET /api/ingredientes` | 7 itens com `custo_unitario`, `porcao_padrao`, `quantidade_estoque`, `quantidade_minima`; **estoque 0** (seed oficial) |
| 8 | Editar um ingrediente | `PUT /api/ingredientes/{id}` | `200`; se faltar campo, a tela mostra o aviso de "resumo" (o backend corrige) |
| 9 | Ingrediente com custo `-1` e porção `0` | resposta `400` | **os dois erros** em `campos`, cada um no seu campo |
| 10 | Produto: Açaí com 2 porções (porção 0,150) | payload de `POST /api/produtos` | `quantidade_utilizada: 0.3`; resposta `201` |
| 11 | Produto sem açaí / sem ingrediente | resposta `400` | CT15 / CT13 (teste também **direto na API**, no Postman) |
| 12 | `#/produtos` | `GET /api/produtos` | `composicao[]` com `ingrediente_nome`, `quantidade_utilizada`, `unidade_medida` |
| 13 | Alterar o preço | `PUT /api/produtos/{id}/preco` | request `{preco_venda: 13.5}`, header `Authorization: Bearer ...`; `200` |
| 14 | Entrar como **Funcionário** e abrir `#/funcionarios/novo` e `#/produtos/preco/1` | nenhuma chamada a `/api/usuarios` | a interface bloqueia; **no Postman**, o token dele em `POST /api/usuarios` dá `403` |
| 15 | Expirar o token (Application → Session Storage: trocar 1 letra do token) | qualquer `GET` | `401` → "Sua sessão expirou" e volta ao login |
| 16 | Parar o backend e tentar entrar | — | "Não foi possível conectar à API..." (diferente de senha inválida) |

**Falha de `fetch` não prova CORS** (pode ser API desligada). Se aparecer `blocked by CORS policy`: confira a origem e peça ao Lucas o *preflight* (`OPTIONS`) na aba Network.

| Divergência | Quem corrige |
|---|---|
| Campo faltando ou com nome diferente na **resposta** | Lucas (o contrato manda `snake_case`) |
| Seu payload não bate com o contrato | Você |
| Mensagem diferente da do `mapa_constraints.md` | Lucas |
| Contrato ambíguo | Registrar no Contrato (decisão do PO) |

Guarde as **16 evidências** em `docs/Sprint_3/`. A Fase 2 só conta como concluída com elas.

## 6. Extensão: telas novas (só depois do aceite real e se o backend estiver pronto)

Cada tela vira **um módulo** em `js/screens/` e **um PR**. Registre a rota no `router.js` (`else if(section==='estoque') await inventory(main,action);`) e no menu em `shell.js` **só quando a tela existir**. Padrão de todas: estados de **carregando**, **vazio**, **erro** (com "Tentar novamente") e **validação por campo**; botão desabilitado ao enviar; dados da API **sempre como texto** (nunca `innerHTML`).

### 6.1 Estoque (Telas 10 e 16) **[formato a fechar: PROPOSTA]**
**Lista** (`GET /api/estoque`): por ingrediente, `quantidade_estoque` (total), **`quantidade_valida`**, **`quantidade_vencida`**, `quantidade_minima` e `validade_mais_proxima`. Mostre o **total**, o **válido** e o **vencido** (com texto, nunca só cor) e o selo "Abaixo do mínimo". **Movimentação** (`POST /api/estoque/movimentacoes`): ingrediente, tipo **Entrada** ou **Saída**; na saída, o motivo **"Saída convencional"** ou **"Descarte de vencidos"** (esta só habilitada quando `quantidade_vencida > 0`); validade **só na entrada**.

Validação (**verificada em Node**):
```js
function validarMovimentacao(raw, linha) {   // linha = item de GET /api/estoque do ingrediente escolhido
  const issues = {}, payload = {ingrediente_id: Number(raw.ingrediente_id), tipo: raw.tipo};
  if (!raw.ingrediente_id) issues.ingrediente_id = 'Selecione o ingrediente.';
  if (!['ENTRADA', 'SAIDA'].includes(raw.tipo)) issues.tipo = 'Selecione entrada ou saída.';
  payload.quantidade = decimal(raw.quantidade, 3, true);
  if (payload.quantidade === null) issues.quantidade = 'A quantidade deve ser maior que zero. Use até 3 casas decimais.';
  if (raw.tipo === 'ENTRADA') {
    if (raw.data_validade) payload.data_validade = raw.data_validade;
  } else if (raw.tipo === 'SAIDA') {
    if (raw.data_validade) issues.data_validade = 'A validade só pode ser informada em entradas.';
    if (!['CONVENCIONAL', 'DESCARTE_VENCIDO'].includes(raw.motivo)) issues.motivo = 'Escolha saída convencional ou descarte de vencidos.';
    else payload.motivo = raw.motivo;
    if (raw.motivo === 'DESCARTE_VENCIDO' && linha && !(Number(linha.quantidade_vencida) > 0)) issues.motivo = 'Este ingrediente não tem quantidade vencida.';
    if (payload.quantidade !== null && linha) {
      const limite = raw.motivo === 'DESCARTE_VENCIDO' ? Number(linha.quantidade_vencida) : Number(linha.quantidade_valida);
      if (payload.quantidade > limite) issues.quantidade = `Quantidade maior que a disponível (${limite}).`;
    }
  }
  if (raw.observacao?.trim()) payload.observacao = raw.observacao.trim();
  return {payload, issues};
}
```
**Por quê cada regra:** a validade só existe em entrada (espelha `chk_movimentacao_data_validade`); a saída exige motivo (o banco também exige: `chk_movimentacao_motivo`); a saída convencional só pode usar o estoque **válido** (vencido não se vende); o descarte só o **vencido**. **Resultados verificados:** saída sem motivo → "Escolha saída convencional ou descarte..."; convencional 16 com válido 15 → "Quantidade maior que a disponível (15)"; descarte sem vencido → "Este ingrediente não tem quantidade vencida."; entrada com validade → payload com `data_validade`. O backend revalida tudo (`422` "Estoque insuficiente..." aparece como mensagem geral).

### 6.2 Pedido (Telas 11 e 17) **[formato a fechar: PROPOSTA]**
Montar o pedido com **produtos ativos**, ajustar quantidades, remover itens e ver o **total**; sem cliente (Sprint 4). Lógica (**verificada**; o total é só exibição, o backend recalcula com o preço do banco):
```js
const totalDoPedido = itens => itens.reduce((s, {produto, quantidade}) => s + Math.round(Number(produto.preco_venda) * 100) * quantidade, 0) / 100;
const payloadPedido = (clienteId, itens) => ({cliente_id: clienteId ? Number(clienteId) : null,
  itens: itens.map(({produto, quantidade}) => ({produto_id: produto.id, quantidade}))});
```
Conferido: 2 × 12,90 + 1 × 18,90 = **44,70**. Roteiro: (1) carregue `list('/api/produtos')` e filtre `ativo`; (2) produto + quantidade inteira ≥ 1, **sem repetir** o produto (desabilite a opção, como a composição faz com `option.disabled`); (3) `+`, `−` e remover com confirmação (`confirmRemoval`, ajustando o texto); (4) "Confirmar" desabilitado sem itens; (5) **`422` "Estoque insuficiente para o produto X"** vira mensagem geral e **o pedido não é limpo**; (6) sucesso `201`: "Pedido registrado. Total: R$ ...", limpe a tela.

### 6.3 Produção (Tela 12) **[BLOQUEADO até o Contrato v2 e a aprovação do lucro]**
Botão **"Gerar recomendação"** → `request('/api/producao/recomendacao', {method:'POST'})` com "Calculando..." (o `REQUEST_TIMEOUT_MS` de 15 s é **maior** que os 10 s do backend). Resposta proposta: `calculado_em`, `receita_estimada`, `margem_estimada`, `recomendacao: [{produto_id, nome, quantidade, lucro_por_copo}]`. Mostre **"Quanto produzir"** (produto, quantidade, lucro por copo) e dois destaques: **"Lucro potencial"** e **"Receita estimada"**. **Por que o produto veio com zero** (lógica **verificada**):
```js
const motivoDoZero = item => item.quantidade > 0 ? null : (Number(item.lucro_por_copo) <= 0 ? 'Sem lucro' : 'Falta ingrediente');
const avisoGeral = rec => rec.some(i => i.quantidade > 0) ? null
  : (rec.every(i => Number(i.lucro_por_copo) <= 0) ? 'Nenhum produto dá lucro hoje com os custos atuais.' : 'Estoque insuficiente para produzir agora.');
```
**Linguagem (US11):** nunca escreva "otimização", "solver", "programação linear", "variável" ou "restrição". Use: "Esta é a quantidade que rende o maior lucro com o estoque que você tem hoje". Com o seed oficial (estoque 0), a tela mostra "Estoque insuficiente para produzir agora." e todos "Falta ingrediente". **Número para conferir (seed de demonstração):** plano **151 / 1 / 0**, lucro **1.011,71**, receita **1.966,80**.

## 7. Testes de navegador (Playwright)

O `tests/browser.cjs` intercepta `http://localhost:8080/**` e **lança "Rota inesperada"** para rota que o `handler` não conhece. **Para cada tela nova, acrescente primeiro as rotas ao `handler`** (`/api/estoque`, `/api/estoque/movimentacoes`, `/api/pedidos`, `/api/producao/recomendacao`) no formato do contrato.

| Tela | Testes mínimos |
|---|---|
| Permissões | Funcionário: `pedidos/novo` e `estoque` liberados; `funcionarios`, `produtos/novo`, `dashboard`, `producao` bloqueados **sem chamadas à API** |
| Estoque | validade desabilitada em saída; descarte desabilitado sem vencido; `422` como mensagem geral; payload exato |
| Pedido | sem itens bloqueia o envio; produto repetido não duplica; payload exato; `422` mantém o pedido; total 44,70 |
| Produção | "Calculando"; tabela da recomendação; "Falta ingrediente" × "Sem lucro"; **nenhum termo técnico** em `page.content()` |

Rode `npm test` a cada PR. **Nunca** desative um teste antigo para "passar": se uma tela mudou de propósito, atualize o teste e registre no PR.

## 8. Documentação e PR
1. **`frontend/README.md`:** a seção "Situação do backend deste checkout" está datada de 27/09; reescreva com o estado real, a estrutura de módulos e como rodar o aceite real e a ferramenta de retratos.
2. Um PR por etapa (divisão em módulos; permissões; cada tela), com Conventional Commits, revisão de um colega e as evidências (retratos `IGUAIS`, `npm test`, prints do aceite real).

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `ERR_CONNECTION_REFUSED` | Servidor do frontend fora da porta 5500 | `py -m http.server 5500 --bind 127.0.0.1` |
| `blocked by CORS policy` | `localhost` em vez de `127.0.0.1`, ou CORS fora do filtro no backend | Usar `127.0.0.1:5500`; avisar o Lucas |
| Tela em branco depois de dividir o `app.js` | Erro de import/`export` (veja o console, F12) | `node --check` em cada módulo; conferir o `export` |
| `DIFERENÇAS em: ...` no comparador | `flash`/`revision` sem `state.`, ou `render()` chamado de `ui`/`nav` | Parte 3.2, cuidados 1 e 2 |
| Menu mostra `undefined` no nome | O login não devolveu `nome` | Pedir a correção ao Lucas |
| Teste novo lança `Rota inesperada` | Rota não está no `handler` do `browser.cjs` | Acrescentar a rota simulada |
| Acentos quebrados no CSV (Sprint 4) | Falta o BOM no arquivo | Pedir ao Lucas o BOM no início |

## Checklist final
- [ ] Baseline: 22 OK e `retrato_antes.json`
- [ ] `app.js` dividido; `node --check` em todos; **`IGUAIS`**; 22 OK
- [ ] Aceite real da Fase 2 com as 16 evidências
- [ ] Guarda de permissões por lista (tabela da Parte 4 reproduzida)
- [ ] (Extensão) Estoque, Pedido e Produção com testes e **sem termo técnico**
- [ ] `frontend/README.md` atualizado; PRs revisados

## Depois (Sprint 4, só sinalização)
Clientes (Telas 18 e 18B, US13), Dashboard e CSV (`download` autenticado com `fetch` + `Blob`), Porção Padrão (`PATCH`), Reposição, Marketing e o cancelamento de pedido (US27). A pendência **F12** (definir "consumo") precisa estar decidida antes do Dashboard.
