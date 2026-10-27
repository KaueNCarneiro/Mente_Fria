// Harness: carrega o frontend (app.js como módulo ES) em um DOM simulado (jsdom) com fetch simulado
// e grava um "retrato" do HTML de várias telas/estados. Roda a versão ORIGINAL e a DIVIDIDA e compara.
import {JSDOM} from 'jsdom'; import fs from 'node:fs'; import path from 'node:path'; import {pathToFileURL} from 'node:url';
const dir = path.resolve(process.argv[2]), out = process.argv[3];
const dom = new JSDOM('<!doctype html><html><body><div id="app"></div></body></html>', {url: 'http://127.0.0.1:5500/index.html', pretendToBeVisual: true});
const w = dom.window;
globalThis.window = w; globalThis.document = w.document; globalThis.sessionStorage = w.sessionStorage;
globalThis.Node = w.Node; globalThis.HTMLElement = w.HTMLElement; globalThis.Event = w.Event; globalThis.FormData = w.FormData;
Object.defineProperty(globalThis, 'location', {get: () => w.location, configurable: true});
globalThis.CSS = {escape: s => String(s).replace(/[^a-zA-Z0-9_-]/g, '\\$&')};
const ingredientes = [
  {id: 1, nome: 'Açaí (polpa)', unidade_medida: 'kg', custo_unitario: 18.5, porcao_padrao: 0.15, quantidade_minima: 10, quantidade_estoque: 42},
  {id: 2, nome: 'Granola', unidade_medida: 'kg', custo_unitario: 22, porcao_padrao: 0.04, quantidade_minima: 2, quantidade_estoque: 15}];
const produtos = [{id: 1, nome: 'Açaí 300ml', preco_venda: 12.9, ativo: true,
  composicao: [{ingrediente_id: 1, ingrediente_nome: 'Açaí (polpa)', quantidade_utilizada: 0.3, unidade_medida: 'kg'}]}];
const respostas = {
  '/api/saude': {status: 'ok', banco: 'ok'}, '/api/ingredientes': ingredientes, '/api/produtos': produtos,
  '/api/usuarios': [{id: 2, nome: 'Maria Souza', email: 'maria@mentefria.com', ativo: true}, {nome: '<img src=x onerror=alert(1)>', email: 'x@y.com', ativo: false}],
  '/api/unidades-medida': [{unidade_medida: 'kg', porcao_padrao: 0.04}, {unidade_medida: 'l', porcao_padrao: 0.03}],
  '/api/teste/python': {status: 'sucesso', echo: 'AÇAÍ (POLPA)'}};
globalThis.fetch = async (url, opts = {}) => {
  const p = new URL(url).pathname; const corpo = respostas[p];
  if (corpo === undefined) return {ok: false, status: 404, json: async () => ({mensagem: 'rota de teste ausente: ' + p})};
  return {ok: true, status: 200, json: async () => corpo};
};
const dorme = ms => new Promise(r => setTimeout(r, ms));
const snaps = {};
const foto = async nome => { await dorme(60); snaps[nome] = {titulo: w.document.title, hash: w.location.hash, html: w.document.querySelector('#app').innerHTML}; };
const visita = async (hash, nome) => { w.location.hash = hash; await foto(nome || hash); };
const sessao = (perfil, nome) => w.sessionStorage.setItem('mente-fria.session', JSON.stringify({token: 't', nome, perfil}));
try {
  await import(pathToFileURL(path.join(dir, 'js/app.js')).href);
  await foto('inicial (sem sessão)');
  for (const h of ['#/login', '#/cadastro', '#/comunicacao', '#/produtos']) await visita(h, 'anon ' + h);
  w.location.hash = '#/login'; await dorme(60);
  w.document.querySelector('form').dispatchEvent(new w.Event('submit', {cancelable: true, bubbles: true}));   // envia vazio
  await foto('login enviado vazio (erros de campo)');
  sessao('ADMINISTRADOR', 'Dono');
  for (const h of ['#/funcionarios', '#/funcionarios/novo', '#/ingredientes', '#/ingredientes/novo', '#/ingredientes/editar/1', '#/ingredientes/editar/99',
                   '#/produtos', '#/produtos/novo', '#/produtos/preco/1', '#/inicio', '#/pagina-inexistente']) await visita(h, 'admin ' + h);
  w.location.hash = '#/ingredientes/novo'; await dorme(60);
  const sel = w.document.querySelector('[name=unidade_medida]'); sel.value = 'kg'; sel.dispatchEvent(new w.Event('change', {bubbles: true}));
  await foto('admin: unidade kg escolhida (porção pré-preenchida = ' + w.document.querySelector('[name=porcao_padrao]').value + ')');
  w.sessionStorage.clear(); sessao('FUNCIONARIO', 'Maria');
  for (const h of ['#/inicio', '#/ingredientes', '#/produtos', '#/funcionarios', '#/produtos/novo', '#/ingredientes/editar/1']) await visita(h, 'func ' + h);
  w.sessionStorage.clear(); w.dispatchEvent(new w.Event('session-expired')); await foto('evento session-expired');
  fs.writeFileSync(out, JSON.stringify(snaps, null, 1)); console.log('OK:', Object.keys(snaps).length, 'retratos ->', out);
  process.exit(0);
} catch (e) { console.error('FALHA no harness:', e); process.exit(2); }
