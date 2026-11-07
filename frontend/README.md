# Frontend — Mente Fria · Sprints 1 a 4

HTML, CSS e JavaScript (módulos ES), sem build e sem dependências de produção. Execute por HTTP: abrir o `index.html` com dois cliques (`file://`) não carrega os módulos.

**Publicado em:** _(URL do GitHub Pages, preencher depois do primeiro deploy, por exemplo `https://kauencarneiro.github.io/Mente_Fria/`)_

## Executar

Dentro desta pasta, num terminal (ou **Open with Live Server** no VS Code, na porta 5500; use um dos dois):

```powershell
py -m http.server 5500 --bind 127.0.0.1
```

Abra http://127.0.0.1:5500/index.html (sempre `127.0.0.1`, que é a origem liberada no CORS do backend). `ERR_CONNECTION_REFUSED` nessa página quer dizer que o servidor do frontend não está rodando.

O backend é escolhido em `js/config.js`, pelo endereço da página:

| Onde a página está | Backend | Tempo limite |
|---|---|---|
| `*.github.io` (GitHub Pages) | O serviço do Render (constante `PRODUCAO`, sem `/api` no fim) | 90 s, porque o plano gratuito do Render "dorme" e leva cerca de 1 min para acordar |
| Qualquer outro (local, testes) | `http://localhost:8080` | 15 s |

## Telas

| Tela | Rota | Perfis | API (contrato §) |
|---|---|---|---|
| Login e cadastro do Administrador | `#/login`, `#/cadastro` | Público | `/api/auth/*` (2.1) |
| Funcionários | `#/funcionarios`, `/novo` | Admin | `/api/usuarios` |
| Ingredientes | `#/ingredientes`, `/novo`, `/editar/{id}` | Lista: os dois; cadastro e edição: Admin | `/api/ingredientes` |
| Produtos | `#/produtos`, `/novo`, `/preco/{id}` | Lista: os dois; cadastro e preço: Admin | `/api/produtos` |
| **Estoque** (10/16) | `#/estoque`, `/linhas/{id}`, `/movimentar` | Os dois | `/api/estoque*` (2.4) |
| **Pedidos** (11/17) | `#/pedidos`, `/novo` | Os dois | `/api/pedidos` (2.5) |
| **Produção** (12) | `#/producao` | Admin | `POST /api/producao/recomendacao` (2.6) |
| **Reposição** (13) | `#/reposicao` | Admin | `POST /api/reposicao/recomendacao` (2.7) |
| **Dashboard** (03) | `#/dashboard` | Admin | `GET /api/dashboard` (2.8) |
| **Marketing** (14) | `#/marketing` | Admin | `GET /api/marketing/sugestoes` (2.9) |
| Teste de comunicação | `#/comunicacao` | Público | `/api/saude`, `/api/ingredientes` |

Ficam fora do semestre (decisões A2b e A40): Clientes (US13), Porção Padrão (US26, com o Diogo), alertas completos (US15/US16), CSV do dashboard (US19) e cancelamento de pedido (US27). No menu, aparecem apagados.

## Regras que valem para todas as telas

- **Estados:** carregando, vazio, erro com "Tentar novamente" e sucesso.
- **Validação antes de enviar**, com a mensagem embaixo de cada campo. O backend revalida tudo (`400`, `403`, `422`) e as mensagens dele aparecem do mesmo jeito.
- **Botão desabilitado durante o envio**, para um clique duplo não mandar duas vezes (CT71).
- **Dados da API sempre como texto**, nunca `innerHTML` (o `node()` do `ui.js` cria nós de texto).
- **Sem termos técnicos** nas Telas 12, 13 e 14 (US11, CT43): nada de "otimização", "solver", "restrição" e similares. A tela fala em lucro e quantidade.
- **Reserva B1:** se o servidor desligar a recomendação, Produção e Reposição recebem `503` e mostram a mensagem como **aviso**, não como erro; a tela continua utilizável.
- **Guarda de rotas por lista** (`js/nav.js`): o Funcionário só abre o que está no mapa `FUNCIONARIO`. É experiência de uso; **a segurança é o `403` do backend**.
- Sessão no `sessionStorage` da aba (token, nome e perfil; nunca a senha). Um `401` com token encerra a sessão e volta ao login.

## Estrutura

```
js/
  app.js            ponto de entrada: só importa o router
  router.js         lê o "#/..." e escolhe a tela; aplica a guarda
  nav.js            go(), admin() e a guarda de rotas (rotaPermitida)
  state.js          estado compartilhado (flash, revision)
  ui.js             node, table, field, formBase, bindForm, heading, loading, stat, confirmDialog...
  api.js            toda chamada HTTP: token, tempo limite e erros (ApiError com status e campos)
  config.js         qual backend usar (local ou Render)
  validation.js     cálculos puros (decimal, porções, açaí)
  apoio_telas_novas.js   funções puras das Telas 03, 13 e 14 (filtros, resumo, termos técnicos)
  screens/          um módulo por tela: auth, shell, funcionarios, ingredientes, produtos,
                    comunicacao, estoque, pedidos, producao, dashboard, reposicao, marketing
css/styles.css      estilos responsivos
assets/             logo, ícones e a fonte Inter (com licença)
tests/browser.cjs   testes de navegador com a API simulada
```

**Para criar uma tela nova:** (1) ler o formato no contrato; (2) criar `js/screens/<tela>.js`; (3) registrar a rota no `router.js`; (4) pôr no menu do `screens/shell.js`; (5) se o Funcionário usa, acrescentar a rota no mapa `FUNCIONARIO` do `nav.js`; (6) acrescentar as rotas simuladas e a verificação no `tests/browser.cjs`.

## Testes

```powershell
npm install
npm test
```

Com o servidor do frontend na porta 5500 e o Google Chrome instalado. **Resultado esperado: `39 verificações passaram.`** (10 de 10 rodadas em 06/11/2026). As verificações usam a API simulada: o teste intercepta `http://localhost:8080/**` e falha com "Rota inesperada" se uma tela chamar uma rota que ele não conhece.

- `PLAYWRIGHT_CHANNEL` escolhe outro navegador instalado; `FRONTEND_URL`, outro endereço.
- As imagens de cada rodada vão para `tests/artifacts` (ignorada pelo Git).
- A instabilidade antiga da verificação 21 (A13) foi corrigida no teste: ele contava os campos antes de a tela terminar a remoção.

**Ferramentas de retrato** (sem navegador; provam que uma refatoração não mudou nenhuma tela): estão em `docs/Sprint_3/referencia/frontend/ferramentas/`. Copie para `frontend/ferramentas/` (não vão para o Git), rode `npm install jsdom --no-save` e use `retratar_telas.mjs`, `comparar_retratos.mjs`, `testar_guarda.mjs` e `testar_apoio_telas.mjs`.

## Situação (06/11/2026)

- Telas 03, 10/16, 11/17, 12, 13 e 14 prontas e testadas **com a API simulada**.
- **Pendente:** o aceite com o backend real (as Fases 3 a 6 do backend ainda não estavam na `main`), o *gate* da Fase 2 com as 16 evidências e o aceite na URL publicada. As evidências ficam em `docs/Sprint_4/evidencias/`.
- Publicação: `.github/workflows/frontend-pages.yml` publica esta pasta a cada push na `main` que mexa no frontend. Pré-requisito, uma vez: *Settings → Pages → Source: GitHub Actions*. A origem a liberar no `CORS_ALLOWED_ORIGIN` do Render é só `https://kauencarneiro.github.io` (sem caminho e sem barra).

## Problemas comuns

| Sintoma | O que fazer |
|---|---|
| Tela em branco | **F12 → Console**: quase sempre um `import`/`export` errado |
| `blocked by CORS policy` local | Abrir por `http://127.0.0.1:5500`, não por `file://` nem outra porta |
| "Não foi possível conectar à API…" | O backend está desligado, ou o CORS bloqueou: o navegador não diferencia os dois. Confira o *Network* |
| No Pages: "O servidor demorou para responder" | O Render estava dormindo e o `config.js` publicado é o antigo (15 s) |
| `npm test` com "Rota inesperada" | Falta a rota simulada no `tests/browser.cjs` |
