// Harness: carrega o frontend (app.js como módulo ES) em um DOM simulado (jsdom), abre rotas com
// perfil de Funcionário e de Administrador e mostra quais ficam LIBERADAS ou BLOQUEADAS pelo guarda do router.
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
  w.sessionStorage.setItem('mente-fria.session', JSON.stringify({token: 't', nome: 'Maria', perfil: 'FUNCIONARIO'}));
  const resultado = [];
  for (const h of ['#/pedidos/novo', '#/clientes/novo', '#/estoque', '#/funcionarios', '#/produtos/novo', '#/ingredientes/editar/1', '#/dashboard', '#/producao', '#/pedidos/editar/1']) {
    w.location.hash = '#/inicio'; await dorme(40);
    w.location.hash = h; await dorme(60);
    resultado.push([h, w.location.hash === h ? 'LIBERADA (fica na rota)' : 'BLOQUEADA (voltou para ' + w.location.hash + ')']);
  }
  w.sessionStorage.setItem('mente-fria.session', JSON.stringify({token: 't', nome: 'Dono', perfil: 'ADMINISTRADOR'}));
  for (const h of ['#/dashboard', '#/pedidos/novo', '#/producao']) { w.location.hash = '#/inicio'; await dorme(40); w.location.hash = h; await dorme(60); resultado.push(['(admin) ' + h, w.location.hash === h ? 'LIBERADA' : 'BLOQUEADA']); }
  for (const [h, r] of resultado) console.log(h.padEnd(28), r);
  process.exit(0);
} catch (e) { console.error('FALHA no harness:', e); process.exit(2); }
