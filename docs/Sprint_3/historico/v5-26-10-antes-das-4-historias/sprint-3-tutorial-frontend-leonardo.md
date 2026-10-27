# Tutorial — Sprint 3 (Frontend) — Leonardo

**Para:** Leonardo Fagundes Oliveira · **Papel:** frontend (HTML, CSS, JavaScript) · **Revisão:** 26/10/2026 (auditoria) · **Substitui** `historico/Tutorial_Sprint3_Frontend_Leonardo.md`.
**Base:** `sprint-3-roteiro.md` (v5), `sprint-3-contrato-v2-proposta.md`, `sprint-3-decisoes-e-pendencias.md`.

> **O que foi verificado e o que não foi (26/10).** Tudo foi executado numa **cópia** do `frontend/` da `main`:
> - **Divisão do `app.js`:** 25 telas e estados idênticos antes e depois (`IGUAIS`); `node --check` ok em todos os módulos.
> - **Guarda por lista:** comportamento da tabela da §4, também sem mudar nenhuma tela existente.
> - **Playwright (22 verificações):** passa, mas é **instável** nesta máquina. Falhou de forma intermitente nas verificações 11, 20 e 21 **tanto no código original da `main` (4 de 8 rodadas ok) quanto no dividido (3 de 8)**. Ou seja, **não é efeito da divisão**; é a pendência A13 (§7.1).
> - **Não verificado:** a integração com o backend real (as rotas novas ainda não existem) e as telas novas, que dependem do Contrato v2 (A10).
> - **Mudança de caminhos:** o tutorial de 14/10 citava `referencia_frontend/`, uma pasta que nunca existiu. O material está em **`docs/Sprint_3/referencia/frontend/`**, já na estrutura certa: `js/`, `js/screens/` e `ferramentas/`.

## 0. Onde você está e o que entrega

**Estado da `main`:** o `frontend/` não muda desde 02/10. O `app.js` tem 235 linhas num arquivo só; há 22 verificações Playwright com API simulada; o menu mostra 8 itens "indisponíveis".

| Ordem | Entrega | Quando | Depende de |
|---|---|---|---|
| 1 | Baseline (22 OK) e retrato das telas | **Seg 26** | Nada |
| 2 | `app.js` dividido em módulos (PR) | Seg 26 – Ter 27 | Nada (**não espera o backend**) |
| 3 | Guarda de rotas por lista (PR) | Ter 27 | Item 2 |
| 4 | Telas de Estoque e Pedido contra o Contrato v2, com API simulada | Qua 28 | Contrato v2 aprovado (A10, terça 27) |
| 5 | **Gate da Fase 2**: aceite real com 16 evidências | Qui 29 | Lucas (Fases 1 e 2) |
| 6 | Telas de Estoque e Pedido com o backend real | Sex 30 | Lucas (Fase 3) |
| 7 | Tela 12 (Produção) | Sáb 31 | Lucas e Kauê (Fase 4A) |
| 8 | Investigar o Playwright instável (A13) | Até Sáb 31 | — |

**Pronto quando:**
1. `app.js` dividido, com `IGUAIS` e 22 OK;
2. aceite real com 16 evidências;
3. telas novas testadas;
4. **nenhum termo técnico** na Tela 12;
5. `frontend/README.md` com o estado real.

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

## 5. Gate da Fase 2: aceite com o backend **real** (quinta 29)

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

Formatos: **Contrato v2** (`sprint-3-contrato-v2-proposta.md`, §2.4 a §2.6), depois de aprovado.

Cada tela é **um módulo** em `js/screens/` e **um PR**. Registre a rota no `router.js` e no menu do `shell.js` **só quando a tela existir**.

Padrão de todas as telas:
- estados de carregando, vazio e erro (com "Tentar novamente");
- validação por campo;
- botão desabilitado durante o envio;
- dados da API sempre como texto, **nunca** `innerHTML`.

### 6.1 Estoque (Telas 10 e 16)

**Lista** (`GET /api/estoque`): mostre o total, o **válido** e o **vencido** (com texto, nunca só com cor), a validade mais próxima e o selo "Abaixo do mínimo" quando `quantidade_valida ≤ quantidade_minima`. O cálculo do selo é seu, salvo decisão diferente no Contrato (item **[decidir]**).

**Detalhe** (opcional, A12): `GET /api/estoque/{id}/linhas`, com a `situacao` de cada linha.

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

**Bloqueada até o Contrato v2 e a Fase 4A.**

- Botão **"Gerar recomendação"** → `request('/api/producao/recomendacao', {method:'POST'})`, com "Calculando…". O `REQUEST_TIMEOUT_MS` (15 s) é maior que os 10 s do backend.
- Mostre "Quanto produzir" (produto, quantidade, lucro por copo) e dois destaques: **"Lucro potencial"** (`margem_estimada`) e **"Receita estimada"**.

```js
const motivoDoZero = item => item.quantidade > 0 ? null : (Number(item.lucro_por_copo) <= 0 ? 'Sem lucro' : 'Falta ingrediente');
const avisoGeral = rec => rec.some(i => i.quantidade > 0) ? null
  : (rec.every(i => Number(i.lucro_por_copo) <= 0) ? 'Nenhum produto dá lucro hoje com os custos atuais.' : 'Estoque insuficiente para produzir agora.');
```

- **Linguagem (US11, CT43):** nunca "otimização", "solver", "programação linear", "variável" ou "restrição". Use: "Esta é a quantidade que rende o maior lucro com o estoque que você tem hoje".
- **Número para conferir** (seed de demonstração): **151 / 1 / 0**, lucro **1.011,71**, receita **1.966,80**.
- Com o seed oficial (estoque 0): "Estoque insuficiente para produzir agora."

## 7. Testes de navegador (Playwright)

O `tests/browser.cjs` intercepta `http://localhost:8080/**` e lança "Rota inesperada" para rota que não conhece. **Para cada tela nova, acrescente antes as rotas simuladas** (`/api/estoque`, `/api/estoque/movimentacoes`, `/api/pedidos`, `/api/producao/recomendacao`) **no formato do Contrato v2**.

| Tela | Testes mínimos |
|---|---|
| Guarda | Funcionário: `pedidos/novo` e `estoque` liberados; `funcionarios`, `produtos/novo`, `dashboard` e `producao` bloqueados **sem chamadas à API** |
| Estoque | Validade desabilitada na saída; descarte desabilitado sem vencido; `422` como mensagem geral; payload exato |
| Pedido | Sem itens bloqueia; produto repetido não duplica; payload exato; `422` mantém o pedido; total 44,70 |
| Produção | "Calculando"; tabela; "Falta ingrediente" × "Sem lucro"; **nenhum termo técnico** em `page.content()` |

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
2. Um PR por etapa (divisão, guarda, cada tela), com Conventional Commits, revisão de um colega e evidências.

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

## Checklist final

- [ ] Baseline (22 OK, registrando quantas rodadas) e `retrato_antes.json`
- [ ] `app.js` dividido; `node --check`; **`IGUAIS`**; 22 OK
- [ ] Guarda por lista (tabela da §4)
- [ ] Aceite real da Fase 2 com 16 evidências
- [ ] Estoque, Pedido e Produção com testes e **sem termo técnico**
- [ ] A13 investigada e registrada
- [ ] `frontend/README.md` atualizado; PRs revisados

## Fora da Sprint 3 (só sinalização; depende da A3)

Clientes (Telas 18 e 18B, US13), Dashboard e CSV (`fetch` + `Blob`), Porção Padrão (`PATCH`), Reposição, Marketing e o cancelamento de pedido (US27).
