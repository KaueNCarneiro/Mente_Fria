# Tutorial — Sprint 3 (Frontend) — Leonardo

**Para:** Leonardo Fagundes Oliveira · **Papel:** frontend (HTML, CSS, JavaScript) e publicação no GitHub Pages · **Revisão:** 27/10/2026, noite (v5.3: **calendário deslocado um dia, A36**; antes, v5.2: Contrato v2 aprovado; botão "Descartar" por linha, A12; Render definitivo no sábado, A21) · **Substitui** a v5.1 (`historico/v5.1-27-10-antes-das-respostas/`), a versão da tarde (`historico/v5-26-10-antes-das-4-historias/`) e `historico/Tutorial_Sprint3_Frontend_Leonardo.md`.
**Base:** `sprint-3-roteiro.md` (v5.3), o **contrato oficial** `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` (v2; a proposta ficou congelada) e `sprint-3-decisoes-e-pendencias.md`.

> **O que foi verificado e o que não foi (26/10).** Tudo foi executado numa **cópia** do `frontend/` da `main`:
> - **Divisão do `app.js`:** 25 telas e estados idênticos antes e depois (`IGUAIS`); `node --check` ok em todos os módulos.
> - **Guarda por lista:** comportamento da tabela da §4, também sem mudar nenhuma tela existente.
> - **Playwright (22 verificações):** passa, mas é **instável** nesta máquina. Falhou de forma intermitente nas verificações 11, 20 e 21 **tanto no código original da `main` (4 de 8 rodadas ok) quanto no dividido (3 de 8)**. Ou seja, **não é efeito da divisão**; é a pendência A13 (§7.1).
> - **Novo na v5.1 (US14, US17, US18 e US20 entraram na Sprint 3):**
>   - funções de apoio das **Telas 03 (Dashboard), 13 (Reposição) e 14 (Marketing)** em `referencia/frontend/js/apoio_telas_novas.js`;
>   - o **`config.js` de produção** (GitHub Pages × local) em `referencia/deploy/config.js`;
>   - os dois conferidos com Node: **15/15** verificações (`ferramentas/testar_apoio_telas.mjs`);
>   - o *workflow* do Pages (`referencia/deploy/frontend-pages.yml`) tem a sintaxe conferida e as versões das *actions* atualizadas;
>   - **o backend completo está pronto e testado** em `referencia/backend/` (95 testes), e os formatos do Contrato v2 são exatamente os dele.
> - **Não verificado:** as telas novas em si (o desenho e o código são seus), a publicação real no Pages (precisa do repositório) e a integração com o backend publicado.
> - **Mudança de caminhos:** o tutorial de 14/10 citava `referencia_frontend/`, uma pasta que nunca existiu. O material está em **`docs/Sprint_3/referencia/frontend/`**, já na estrutura certa: `js/`, `js/screens/` e `ferramentas/`.

## 0. Onde você está e o que entrega

**Estado da `main`:** o `frontend/` não muda desde 02/10. O `app.js` tem 235 linhas num arquivo só; há 22 verificações Playwright com API simulada; o menu mostra 8 itens "indisponíveis".

| Ordem | Entrega | Quando | Depende de |
|---|---|---|---|
| 1 | Baseline (22 OK) e retrato das telas | **Seg 26** | Nada |
| 2 | `app.js` dividido em módulos (PR) | Seg 26 – Qua 28 | Nada (**não espera o backend**) |
| 3 | Guarda de rotas por lista (PR) | Qua 28 | Item 2 |
| 4 | **`config.js` de produção e *workflow* do Pages** (§9), com o Kauê | **Qui 29** | Nada (a URL definitiva do Render só sai no sábado à noite) |
| 5 | Telas de Estoque e Pedido contra o Contrato v2, com API simulada | Qui 29 | Contrato v2 (A10, **aprovado em 27/10**) |
| 6 | **Gate da Fase 2**: aceite real com 16 evidências | Sex 30 (manhã) | Lucas (Fases 1 e 2) |
| 7 | Telas de Estoque e Pedido com o backend real | Sex 30 – Sáb 31 | Lucas (Fase 3) |
| 8 | Tela 12 (Produção) e **Tela 03 (Dashboard)** | Sáb 31 | Fase 4A; Fase 6 |
| 9 | **Telas 13 (Reposição) e 14 (Marketing)**. A 14 pode ficar com o Lucas, se apertar | Dom 01/11 | Fases 5 e 6 |
| 10 | Investigar o Playwright instável (A13) | Até Dom 01/11 | — |
| 11 | **Botão "Descartar"** no detalhe das linhas (A12, **opcional**) | Sáb 31 | Lucas (Fase 3). **É o primeiro a sair** se a conferência de sábado à tarde mostrar atraso (A28) |
| 12 | Aceite **na URL publicada** (com o B1, as Telas 12, 13 e 14 mostram o aviso do `503`) | Seg 02/11 | Deploy final |

**Pronto quando:**
1. `app.js` dividido, com `IGUAIS` e 22 OK;
2. aceite real com 16 evidências;
3. telas novas (10/16, 11/17, 12, **03, 13 e 14**) testadas;
4. **nenhum termo técnico** nas Telas 12, 13 e 14;
5. frontend publicado no GitHub Pages, falando com o backend do Render;
6. `frontend/README.md` com o estado real e a URL.

## 1. Por que fazemos assim

| Decisão | Por quê |
|---|---|
| Dividir o `app.js` **antes** das telas novas | Mais 3 a 5 telas no mesmo arquivo, com você e o Lucas mexendo ao mesmo tempo, é receita de conflito (problema da Sprint 1) |
| Refatorar **sem mudar** o comportamento | A prova mecânica (retratos idênticos e os 22 testes) só vale se nada mudar. Melhorias vão em PR separado |
| Guarda por **lista** | O guarda atual **bloqueia** `pedidos/novo` para o Funcionário (o Contrato permite) e **libera** `dashboard` e `producao` (só o `403` do backend segura). Verificado |
| Aceite real antes de novas telas | A Sprint 2 provou a interface só com respostas simuladas. Uma tela só "conta" falando com o backend real |
| Telas novas **depois** do Contrato v2 | Os formatos de estoque, pedido e recomendação eram "proposta". Implementar antes da aprovação gera retrabalho |
| Tela 12 fala em "lucro" e nunca em termos técnicos | O modelo maximiza lucro (F9); a US11 proíbe termos de otimização |

## 2. Preparar e fazer o baseline

```powershell
cd C:\caminho\Mente_Fria
git checkout main
git pull
git checkout -b feature/frontend-modulos-sprint3
cd frontend
npm install
```

Em **outro** terminal, dentro de `frontend`: `py -m http.server 5500 --bind 127.0.0.1`. Depois, de volta ao primeiro:

```powershell
npm test
```

**Você deve ver:** 22 linhas `OK …` e `22 verificações passaram. Integração HTTP simulada.`

> **Se falhar com `TimeoutError` ou `waitFor: Timeout`:** rode de novo. É a instabilidade A13, que já acontece no código original (§7.1). Anote quantas rodadas passaram, porque essa conta é sua evidência de baseline. **Se falhar sempre no mesmo ponto,** pare e investigue antes de dividir.

**Ferramentas de retrato** (sem navegador; complementam o Playwright):

```powershell
mkdir ferramentas
copy ..\docs\Sprint_3\referencia\frontend\ferramentas\*.mjs ferramentas\
npm install jsdom --no-save
node ferramentas\retratar_telas.mjs . retrato_antes.json
```

**Você deve ver:** `OK: 25 retratos -> retrato_antes.json`.

- **Não comite** os `.json` nem a pasta `ferramentas` (ou comite só a pasta, em PR separado, se a equipe quiser mantê-la).
- Se um `npm install` posterior remover o `jsdom`, repita `npm install jsdom --no-save`.

## 3. Dividir o `app.js` em módulos

### 3.1 O mapa (linhas do `app.js` real)

| Linhas | O que é | Vai para |
|---|---|---|
| 1–2 | `import` de `api.js` e `validation.js` | Cada módulo importa o que usa |
| 3–6, 8–21, 24–60, 106–119, 149–154 | `root`, `money`, `amount`, `unit`, `node`, `link`, `button`, `logo`, `notice`, `field`, `errors`, `formBase`, `bindForm`, `heading`, `loading`, `table`, `badge`, `confirmRemoval` | `js/ui.js` |
| 7 | `let revision, flash` | **`js/state.js`** (vira `state.revision` e `state.flash`) |
| 22–23 | `go`, `admin` | `js/nav.js` |
| 61–91 | `accountValidation`, `authPage` | `js/screens/auth.js` |
| 92–105 | `shell` | `js/screens/shell.js` |
| 120–126 | `employees` | `js/screens/funcionarios.js` |
| 127–148 | `ingredients` | `js/screens/ingredientes.js` |
| 155–198 | `products` | `js/screens/produtos.js` |
| 199–214 | `communication` | `js/screens/comunicacao.js` |
| 215–235 | `render`, `addEventListener` e a chamada inicial | `js/router.js` |
| (novo) | `import './router.js';` | `js/app.js` (continua sendo o arquivo do `index.html`) |

**Ordem das dependências, sem ciclos:** `state` ← `nav` ← `ui` ← `screens/*` ← `router` ← `app`. O `index.html` **não muda**.

### 3.2 Os quatro cuidados

| # | Cuidado | Por quê |
|---|---|---|
| 1 | `flash` e `revision` viram `state.flash` e `state.revision` (`export const state = {revision: 0, flash: ''};`) | Em ES modules, uma variável `let` de um arquivo não pode ser reatribuída por outro; um objeto compartilhado pode |
| 2 | `go()` e o "Tentar novamente" disparam `window.dispatchEvent(new Event('rerender'))`, e o `router.js` escuta esse evento | Se `ui.js` ou `nav.js` importassem o `router.js`, haveria um **ciclo** de importação |
| 3 | Todo item usado fora do arquivo precisa de `export` | Sem `export`, o outro módulo não enxerga |
| 4 | Em `products()`, `ingredients` é uma **variável local**, não a tela de ingredientes | Não importe a tela em `produtos.js` |

### 3.3 Um módulo por vez, com ponto de checagem

Ordem sugerida: `state.js` → `ui.js` → `nav.js` → `router.js` + `app.js` mínimo → as telas, uma a uma. **Depois de cada módulo:**

```powershell
node --check js\ui.js            # repita para cada arquivo novo
npm test
```

**Ao terminar, a prova mecânica:**

```powershell
node ferramentas\retratar_telas.mjs . retrato_depois.json
node ferramentas\comparar_retratos.mjs retrato_antes.json retrato_depois.json
```

**Você deve ver:** `IGUAIS: o HTML, o título e a rota de todas as telas são idênticos.` (resultado da auditoria).

Se aparecer `DIFERENÇAS em: …`, o nome da tela mostra onde procurar: quase sempre um `flash` sem `state.` ou uma função sem `export`.

**Referência pronta e verificada:** `docs\Sprint_3\referencia\frontend\js\` (com `screens\`). Use para **comparar** com a sua (diff do VS Code), não para copiar sem entender.

Commit: `refactor(frontend): divide o app.js em modulos sem mudar comportamento`. No PR, mostre o `IGUAIS` e o resultado do `npm test`.

## 4. Guarda de rotas por lista

**Hoje** (em `render()`, e no `router.js` depois da divisão):

```js
if(!admin()&&(section==='funcionarios'||action)){ ... go('inicio'); return; }
```

**Correção** (acrescente ao `js/nav.js`):

```js
const SO_ADMIN = new Set(['funcionarios', 'dashboard', 'porcao-padrao', 'producao', 'reposicao', 'marketing']);
const FUNCIONARIO = {inicio: [undefined], ingredientes: [undefined], produtos: [undefined],
                     estoque: [undefined], pedidos: [undefined, 'novo'], clientes: [undefined, 'novo']};
export const rotaPermitida = (section, action) =>
  admin() || (!SO_ADMIN.has(section) && Boolean(FUNCIONARIO[section]?.includes(action)));
```

No `router.js`: importe `rotaPermitida` e troque a condição por `if(!rotaPermitida(section,action)){ … }`.

**Valide:** `node ferramentas\testar_guarda.mjs .` (perfil Funcionário).

| Rota | Antes (verificado) | Depois (verificado) |
|---|---|---|
| `#/pedidos/novo`, `#/clientes/novo` | **bloqueada** (erro) | liberada |
| `#/estoque` | liberada | liberada |
| `#/funcionarios`, `#/produtos/novo`, `#/ingredientes/editar/1`, `#/pedidos/editar/1` | bloqueada | bloqueada |
| `#/dashboard`, `#/producao` | **liberada** (erro) | bloqueada |
| Administrador | tudo liberado | tudo liberado |

Rode também o `comparar_retratos` entre o `retrato_depois.json` e um retrato novo: deve dar `IGUAIS` (verificado), porque o guarda não muda nenhuma tela existente.

> **Lembre:** isso é experiência de uso. **A segurança é o `403` do backend** (CT05, CT08), já testado na Fase 1 do Lucas.

## 5. Gate da Fase 2: aceite com o backend **real** (sexta 30, A36)

Comece **só quando o Lucas avisar** que os 10 itens da Fase 2 passaram.

**Preparação:**
- banco no ar;
- backend na porta 8080 (iniciado de dentro de `backend/`);
- `js/config.js` com `API_BASE_URL = 'http://localhost:8080'`;
- frontend em `http://127.0.0.1:5500` (**sempre `127.0.0.1`**);
- Chrome com **F12 → Network → Preserve log**.

| # | Ação na tela | O que conferir no Network | Esperado |
|---|---|---|---|
| 1 | `#/comunicacao`: testar saúde e ingredientes | `GET /api/saude`, `GET /api/ingredientes` | `200`; ingredientes **sem login** → `401` com a mensagem "Sua sessão expirou ou você não está autenticado." |
| 2 | `#/cadastro`: criar o Administrador | `POST /api/auth/registro`, depois `POST /api/auth/login` | `201` e `200`; o login traz `token`, `perfil` **e `nome`** |
| 3 | Tentar cadastrar de novo | `POST /api/auth/registro` | `409` "O cadastro do Administrador já foi realizado." |
| 4 | Sair e entrar com senha errada | `POST /api/auth/login` | `401` "E-mail ou senha inválidos." **sem** derrubar outra sessão |
| 5 | `#/funcionarios/novo` com e-mail repetido | `POST /api/usuarios` | `409`, mensagem **no campo e-mail** |
| 6 | Cadastrar um funcionário | Request payload | Exatamente `{nome, email, senha}` |
| 7 | `#/ingredientes` | `GET /api/ingredientes` | 7 itens com todos os campos; **estoque 0** |
| 8 | Editar um ingrediente | `PUT /api/ingredientes/{id}` | `200` |
| 9 | Ingrediente com custo `-1` e porção `0` | Resposta `400` | **Os dois** erros, cada um no seu campo |
| 10 | Produto: açaí com 2 porções (porção 0,150) | Payload do `POST /api/produtos` | `quantidade_utilizada: 0.3`; `201` |
| 11 | Produto sem açaí / sem ingrediente | Resposta `400` | CT15 / CT13 |
| 12 | `#/produtos` | `GET /api/produtos` | `composicao[]` com `ingrediente_nome`, `quantidade_utilizada`, `unidade_medida` |
| 13 | Alterar o preço | `PUT /api/produtos/{id}/preco` | `{preco_venda: 13.5}` com `Authorization: Bearer …`; `200` |
| 14 | Como **Funcionário**, abrir `#/funcionarios/novo` e `#/produtos/preco/1` | Nenhuma chamada a `/api/usuarios` | A interface bloqueia; **no Postman**, o token dele em `POST /api/usuarios` → `403` |
| 15 | Expirar o token (Application → Session Storage: trocar uma letra) | Qualquer `GET` | `401` → "Sua sessão expirou. Entre novamente." e volta ao login |
| 16 | Parar o backend e tentar entrar | — | "Não foi possível conectar à API…" (diferente de senha inválida) |

**Uma falha de `fetch` não prova que é CORS** (pode ser a API desligada). Se aparecer `blocked by CORS policy`, confira a origem e peça ao Lucas o *preflight* (`OPTIONS`) no Network.

| Divergência | Quem corrige |
|---|---|
| Campo faltando ou com nome diferente na **resposta** | Lucas (o contrato manda `snake_case`) |
| O seu payload não bate com o contrato | Você |
| Mensagem diferente da do `mapa_constraints.md` ou da do Contrato v2, §2.1 | Lucas |
| Contrato ambíguo | Registrar no Contrato (decisão do PO) |

Guarde as **16 evidências** em `docs/Sprint_3/evidencias/`. A Fase 2 só conta como concluída com elas.

## 6. Telas novas

Formatos: **Contrato v2** (`sprint-3-contrato-v2-proposta.md`, §2.4 a §2.9), depois de aprovado. São exatamente as respostas do backend de `referencia/`.

**Funções de apoio prontas** (sem DOM, testadas): copie `docs\Sprint_3\referencia\frontend\js\apoio_telas_novas.js` para `frontend\js\` e `docs\Sprint_3\referencia\frontend\ferramentas\testar_apoio_telas.mjs` para `frontend\ferramentas\`. Depois do §9 (o `config.js` novo):

```powershell
node ferramentas\testar_apoio_telas.mjs .
```

**Você deve ver:** `15 verificações passaram.` (27/10).

Cada tela é **um módulo** em `js/screens/` e **um PR**. Registre a rota no `router.js` e no menu do `shell.js` **só quando a tela existir**.

Padrão de todas as telas:
- estados de carregando, vazio e erro (com "Tentar novamente");
- validação por campo;
- botão desabilitado durante o envio;
- dados da API sempre como texto, **nunca** `innerHTML`.

### 6.1 Estoque (Telas 10 e 16)

**Lista** (`GET /api/estoque`): mostre o total, o **válido** e o **vencido** (com texto, nunca só com cor), a validade mais próxima e o selo "Abaixo do mínimo" quando `quantidade_valida ≤ quantidade_minima`. O cálculo do selo é seu, salvo decisão diferente no Contrato (item **[decidir]**).

**Detalhe** (opcional, A12): `GET /api/estoque/{id}/linhas`, com a `situacao` de cada linha. Mostre a situação em texto: "Vencida", "Vence em breve", "Dentro da validade", "Sem validade".

**Botão "Descartar"** (A12, entrou em 27/10, **opcional**: é o primeiro corte se apertar, A28):
- aparece **só** nas linhas com `situacao = "VENCIDA"` e `quantidade_restante > 0`;
- antes de enviar, peça confirmação: "Descartar 6,000 kg vencidos desta linha? Isso não pode ser desfeito.";
- envie `POST /api/estoque/linhas/{id}/descarte`, **sem corpo**;
- `201` → mensagem "Descarte registrado." e recarregue a lista e o detalhe;
- `422` → mostre a `mensagem` do backend como mensagem geral ("Esta linha já não tem saldo." ou "Só é possível descartar linhas vencidas.");
- desabilite o botão enquanto envia (um clique duplo não pode mandar dois descartes).

**Movimentação** (`POST /api/estoque/movimentacoes`):
- tipo Entrada ou Saída;
- na saída, o motivo **"Saída convencional"** ou **"Descarte de vencidos"**, este só habilitado quando `quantidade_vencida > 0`;
- validade **só** na entrada.

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

O `422` "Estoque insuficiente…" do backend aparece como mensagem geral; o backend revalida tudo.

### 6.2 Pedido (Telas 11 e 17)

Fluxo: produtos **ativos**; quantidade inteira ≥ 1; **sem repetir** o produto (`option.disabled`); `+`, `−` e remover com confirmação; "Confirmar" desabilitado sem itens; o pedido vai **sem cliente** (US13 está fora da Sprint 3).

```js
const totalDoPedido = itens => itens.reduce((s, {produto, quantidade}) => s + Math.round(Number(produto.preco_venda) * 100) * quantidade, 0) / 100;
const payloadPedido = (clienteId, itens) => ({cliente_id: clienteId ? Number(clienteId) : null,
  itens: itens.map(({produto, quantidade}) => ({produto_id: produto.id, quantidade}))});
```

- Confira: 2 × 12,90 + 1 × 18,90 = **44,70** (o total é só exibição; o backend recalcula com o preço do banco).
- **`422`** → mensagem geral, e **o pedido não é limpo**.
- **`201`** → "Pedido registrado. Total: R$ …", e a tela é limpa.

### 6.3 Produção (Tela 12)

**Bloqueada até a Fase 4A** (o Contrato v2 já está aprovado e na `main`).

**Plano B1 da licença (A31), vale para as Telas 12, 13 e 14:** no Render, a otimização fica desligada até a Gurobi autorizar. Aí `POST /api/producao/recomendacao` e `POST /api/reposicao/recomendacao` respondem **`503`** com `{"mensagem":"A recomendação está disponível só na demonstração local da equipe."}`. Mostre essa `mensagem` como **aviso informativo** (não como erro em vermelho) e mantenha a tela utilizável. No marketing, a mensagem já vem pronta no campo `mensagem`. Nos testes de navegador, simule também um `503`.

- Botão **"Gerar recomendação"** → `request('/api/producao/recomendacao', {method:'POST'})`, com "Calculando…". O `REQUEST_TIMEOUT_MS` (15 s) é maior que os 10 s do backend.
- Mostre "Quanto produzir" (produto, quantidade, lucro por copo) e dois destaques: **"Lucro potencial"** (`margem_estimada`) e **"Receita estimada"**.

```js
const motivoDoZero = item => item.quantidade > 0 ? null : (Number(item.lucro_por_copo) <= 0 ? 'Sem lucro' : 'Falta ingrediente');
const avisoGeral = rec => rec.some(i => i.quantidade > 0) ? null
  : (rec.every(i => Number(i.lucro_por_copo) <= 0) ? 'Nenhum produto dá lucro hoje com os custos atuais.' : 'Estoque insuficiente para produzir agora.');
```

- **Linguagem (US11, CT43):** nunca "otimização", "solver", "programação linear", "variável" ou "restrição". Use: "Esta é a quantidade que rende o maior lucro com o estoque que você tem hoje". A função `termosTecnicos(texto)` do apoio confere isso.
- **Número para conferir** (seed de demonstração): **151 / 1 / 0**, lucro **1.011,71**, receita **1.966,80**.
- Com o seed oficial (estoque 0): "Estoque insuficiente para produzir agora."
- O resultado fica guardado no backend: as Telas 14 e 03 o usam. Mostre "Calculado em …" (`calculado_em`).

### 6.4 Dashboard (Tela 03) — US17, só Administrador

`GET /api/dashboard` (Contrato v2, §2.8). Monte a URL com `filtrosDashboard({mes, usuarioId, produtoIds, valorMin, valorMax})` e valide antes com `validarFiltrosDashboard(...)`.

| Bloco | Campo da resposta | Como mostrar |
|---|---|---|
| Seletor de mês | `mes` | `<input type="month">`; padrão: o mês atual (o backend já assume) |
| Faturamento do mês | `faturamento.valor_total`, `quantidade_pedidos` | Destaque em R$ |
| Mais vendidos | `mais_vendidos[]` | Tabela: produto, quantidade, valor |
| Ingredientes mais utilizados | `ingredientes_mais_utilizados[]` | Tabela: ingrediente, quantidade **com a unidade**, custo (já vem ordenado pelo custo, A17) |
| Ranking de vendas | `ranking[]` | Colunas **Rank, Usuário, Quantidade de pedidos, Valor total, Tipos de produto** (decisão de 14/10). Filtros: Usuário (`GET /api/usuarios` + o próprio Administrador), Tipo de produto (`GET /api/produtos`, vários) e Valor mín./máx. |
| Plano de produção | `ultimo_plano` (pode ser `null`) e `otimizador_habilitado` | `textoUltimoPlano(plano, otimizador_habilitado)`. Com a otimização desligada no servidor (A31): "O plano de produção está disponível só na demonstração local da equipe.", **sem** link. Com `null` e ligada: "Ainda não há plano de produção. Gere a recomendação na tela Produção." e um link para `#/producao` (A16: é a "receita máxima estimada" do Termo) |
| Situação do estoque | `estoque.ingredientes_abaixo_do_minimo`, `ingredientes_com_vencido` | Dois números, com link para `#/estoque` |

**Para conferir com o seed de demonstração, no mês atual:**
- faturamento **R$ 326,40** (3 pedidos);
- Premium 7, Especial 6, Tradicional 3;
- açaí primeiro nos ingredientes;
- ranking: Func R$ 213,00 e Dono R$ 113,40.

Os números foram conferidos em SQL no tutorial do Diogo, §8.

### 6.5 Reposição (Tela 13) — US14, só Administrador

`POST /api/reposicao/recomendacao` com `{periodo_dias, orcamento}` (Contrato v2, §2.7). Antes de enviar: `validarReposicao({periodo_dias, orcamento})`. Ela aceita vírgula no orçamento e devolve os erros por campo.

- Campos: **Período (dias)** (sugestão: 30) e **Orçamento (R$)**. Botão "Gerar sugestão de compra", com "Calculando…".
- Acima da tabela, `resumoReposicao(resposta)`. Ele trata quatro casos:
  - cobre tudo;
  - cobre parte;
  - não cobre nem o mínimo (mostra quanto falta);
  - sem pedidos no período.
- Tabela **na ordem recebida** (maior custo primeiro): ingrediente, consumo no período (com a unidade), estoque válido, mínimo, **quantidade sugerida**, custo estimado. Linha com sugerida 0: "Não precisa comprar".
- **Não mostre** `cobertura_minima`: é termo técnico. Verifique a página com `termosTecnicos(...)`.

**Para conferir** com o seed de demonstração (60 dias, R$ 100): só o morango, **0,74 kg por R$ 11,10**.

### 6.6 Marketing (Tela 14) — US18, só Administrador

`GET /api/marketing/sugestoes` (Contrato v2, §2.9).

- Até **3 cartões**. Cada um mostra:
  - o produto e o selo "Produto priorizado";
  - a ideia (título) e o "como fazer";
  - o lucro por copo (para o dono saber quanto de desconto cabe).
- Cabeçalho: "Sugestões com base no cálculo de …" (`calculado_em`).
- Sem sugestões: mostre `avisoMarketing(resposta)` e um link para `#/producao`.
- **Nenhum botão de publicar** (critério da US18: o sistema só sugere).

**Para conferir:** depois de gerar 151/1/0 na Tela 12, aparecem **2** cartões (Tradicional e Especial). O Premium veio 0 e não é priorizado.

## 7. Testes de navegador (Playwright)

O `tests/browser.cjs` intercepta `http://localhost:8080/**` e lança "Rota inesperada" para rota que não conhece. **Para cada tela nova, acrescente antes as rotas simuladas** (`/api/estoque`, `/api/estoque/movimentacoes`, `/api/estoque/{id}/linhas`, `/api/estoque/linhas/{id}/descarte`, `/api/pedidos`, `/api/producao/recomendacao`, `/api/reposicao/recomendacao`, `/api/dashboard`, `/api/marketing/sugestoes`) **no formato do Contrato v2**. Os exemplos JSON do contrato servem de resposta simulada.

O `config.js` novo (§9) continua apontando para `http://localhost:8080` quando a página **não** está no `github.io`. Os testes do Playwright não mudam.

| Tela | Testes mínimos |
|---|---|
| Guarda | Funcionário: `pedidos/novo` e `estoque` liberados; `funcionarios`, `produtos/novo`, `dashboard` e `producao` bloqueados **sem chamadas à API** |
| Estoque | Validade desabilitada na saída; descarte desabilitado sem vencido; `422` como mensagem geral; payload exato; botão "Descartar" só em linha vencida com saldo, com confirmação, e **uma** chamada por clique (A12, CT71) |
| Pedido | Sem itens bloqueia; produto repetido não duplica; payload exato; `422` mantém o pedido; total 44,70 |
| Produção | "Calculando"; tabela; "Falta ingrediente" × "Sem lucro"; **nenhum termo técnico** em `page.content()`; resposta `503` (B1) vira aviso informativo, sem tela de erro (CT72) |
| Dashboard | Mês padrão; filtros viram a URL certa (`produto_id` repetido); bloco "Plano de produção" com `null`; Funcionário bloqueado |
| Reposição | Erros por campo; resumo nos 4 casos; ordem da tabela; nenhum termo técnico |
| Marketing | Até 3 cartões; aviso sem cálculo com link; sem botão de publicar |

**Nunca** desative um teste antigo para "passar".

### 7.1 A13: investigar a instabilidade (antes da janela de testes)

**O que a auditoria observou** (mesma máquina, servidor na porta alternativa via `FRONTEND_URL`):

- **Original da `main`:** 4 de 8 rodadas com 22 OK.
- **Dividido:** 3 de 8.
- **Falhas:** sempre por tempo esgotado, nas verificações 11 ("alteração de preço…", esperando o campo `preco_venda`), 20 ("API de resumo…") e 21 ("porções zeradas…"). Na 21, a tela capturada mostrava só "Mente Fria".

**Como investigar** (skill `diagnosing-bugs`):

1. **Ciclo:** um laço PowerShell que roda o `npm test` 10 vezes e conta os "22 verificações passaram".
2. **Instrumente** a função `check` numa **cópia** do `browser.cjs`: ao falhar, imprima `page.url()` e o texto de `#app`.
3. **Hipóteses a testar:**
   - estado deixado pela verificação anterior (`override` ou sessão);
   - `page.goto` com o mesmo *hash* não recarrega;
   - lentidão do `http.server` ao servir os módulos.
4. **Ao achar a causa:** corrija o teste (não a aplicação, se a aplicação estiver certa) e registre a causa no PR.

## 8. Documentação e PR

1. **`frontend/README.md`:** reescreva "Situação do backend deste checkout" (datada de 27/09) com o estado real, a estrutura de módulos, o aceite real e as ferramentas de retrato.
2. Um PR por etapa (divisão, guarda, cada tela, Pages), com Conventional Commits, revisão de um colega e evidências.

## 9. Publicação no GitHub Pages (US20) — preparar na quinta 29; URL definitiva no sábado 31 à noite

O backend vai para o Render (tutorial do Kauê, §12). O frontend vai para o **GitHub Pages**, publicado por **GitHub Actions**. O Pages "por branch" só publica a raiz ou a pasta `/docs`, e o nosso frontend está em `frontend/`.

### 9.1 O `config.js` de produção

Troque o `frontend\js\config.js` pelo de `docs\Sprint_3\referencia\deploy\config.js`:
- no **`github.io`**, ele usa a URL do Render e um tempo limite de **90 s**;
- em qualquer outro endereço (Live Server, `http.server`, testes), continua com `http://localhost:8080` e **15 s**.

**Troque a constante `PRODUCAO`** pela URL do serviço **definitivo** do Render, que só existe a partir do **sábado 31** (A21: o banco gratuito expira 30 dias depois de criado, por isso a criação ficou para o último dia possível). Se houver ensaio na quinta 29 ou na sexta 30 (A36), use a URL do ensaio **só para testar**, sem comitar.

**Por que 90 s:** o plano free do Render **dorme** após 15 min sem uso e leva ~1 min para acordar (documentação do Render, 26/10). Com 15 s, a primeira chamada do dia mostraria "O servidor demorou para responder" (CT70).

### 9.2 O *workflow*

Crie `.github\workflows\frontend-pages.yml` com o conteúdo de `docs\Sprint_3\referencia\deploy\frontend-pages.yml`:
- ele publica a pasta `frontend/` a cada push na `main` que mexa no frontend;
- também pode ser disparado à mão (aba **Actions** → "Frontend no GitHub Pages" → *Run workflow*);
- as versões das *actions* foram conferidas no GitHub em 26/10: `checkout@v7`, `configure-pages@v6`, `upload-pages-artifact@v5`, `deploy-pages@v5`.

**Uma vez, o Kauê (dono do repositório):** *Settings → Pages → Build and deployment → Source: **GitHub Actions***.

### 9.3 Conferir

1. Depois do merge, a aba **Actions** mostra o *workflow* verde e a URL, algo como `https://kauencarneiro.github.io/Mente_Fria/`.
2. **A origem do CORS** é só `https://kauencarneiro.github.io` (sem `/Mente_Fria/` e sem barra). É esse valor que o Kauê põe em `CORS_ALLOWED_ORIGIN` no Render.
3. Abra a URL, F12 → Network, e faça o cadastro e o login. Não pode aparecer `blocked by CORS policy`.
4. Todos os caminhos de arquivo precisam ser **relativos** (`css/…`, `js/…`). Com caminho absoluto (`/css/…`), o Pages procura na raiz do domínio e não acha. **Hoje já são** (conferido em 26/10: `css/styles.css`, `js/app.js` e a fonte em `../assets/`); mantenha assim nas telas novas.
5. Guarde o print com a URL visível em `docs/Sprint_3/evidencias/` e ponha a URL no `frontend/README.md`.

O *workflow* e o `config.js` **não foram executados no GitHub** na auditoria: dependem do repositório. O `config.js` foi conferido com Node nos dois endereços.

## Erros comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `ERR_CONNECTION_REFUSED` | Servidor do frontend fora da porta 5500 | `py -m http.server 5500 --bind 127.0.0.1` |
| `blocked by CORS policy` | `localhost` em vez de `127.0.0.1`, ou CORS do backend | Usar `127.0.0.1:5500`; avisar o Lucas |
| Tela em branco depois de dividir | Erro de `import`/`export` | F12 → Console; `node --check` |
| `DIFERENÇAS em: …` | `flash`/`revision` sem `state.`, ou `render()` chamado de `ui`/`nav` | §3.2 |
| `Cannot find package 'jsdom'` | `jsdom` removido por um `npm install` | `npm install jsdom --no-save` |
| `TimeoutError` intermitente no `npm test` | A13 (também no código original) | §7.1 |
| Menu mostra `undefined` | O login não devolveu `nome` | Pedir a correção ao Lucas |
| Teste novo lança `Rota inesperada` | Rota não está no *handler* do `browser.cjs` | Acrescentar a rota simulada |
| No Pages: "O servidor demorou para responder" | `config.js` antigo (15 s) e o Render dormindo | §9.1 |
| No Pages: `blocked by CORS policy` | `CORS_ALLOWED_ORIGIN` no Render diferente da origem do Pages | Só `https://<usuario>.github.io`, sem caminho (§9.3) |
| No Pages: tela sem estilo ou módulo não encontrado (404) | Caminho absoluto no `index.html` | Caminhos relativos (§9.3) |
| `15 verificações` não aparece | `apoio_telas_novas.js` ou `config.js` não copiados para `frontend/js/` | §6 e §9.1 |

## Checklist final

- [ ] Baseline (22 OK, registrando quantas rodadas) e `retrato_antes.json`
- [ ] `app.js` dividido; `node --check`; **`IGUAIS`**; 22 OK
- [ ] Guarda por lista (tabela da §4)
- [ ] `config.js` de produção e *workflow* do Pages; `testar_apoio_telas.mjs` com 15/15; Telas 12, 13 e 14 com a mensagem do `503` (A31)
- [ ] Aceite real da Fase 2 com 16 evidências
- [ ] Estoque, Pedido, Produção, **Dashboard, Reposição e Marketing** com testes e **sem termo técnico**
- [ ] A13 investigada e registrada
- [ ] Frontend no Pages falando com o Render; aceite na URL publicada
- [ ] `frontend/README.md` atualizado (com a URL); PRs revisados

## Fora da Sprint 3 (decisão A2b)

Clientes (Telas 18 e 18B, US13), alertas (US15/US16), CSV do dashboard (US19), Porção Padrão (`PATCH`, US26) e o cancelamento de pedido (US27).
