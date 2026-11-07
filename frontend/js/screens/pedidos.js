// Telas 11/17: lista de pedidos e registro de um pedido novo (sem cliente: a US13 está fora do semestre).
import {list, request} from '../api.js';
import {state} from '../state.js';
import {money, node, link, button, field, formBase, bindForm, heading, loading, table, confirmDialog} from '../ui.js';
import {go} from '../nav.js';

// Soma em centavos (inteiros) e só divide no fim: evita 44.699999. É só exibição; o backend recalcula com o preço do banco.
export const totalDoPedido = itens => itens.reduce((s, {produto, quantidade}) => s + Math.round(Number(produto.preco_venda) * 100) * quantidade, 0) / 100;
export const payloadPedido = (clienteId, itens) => ({cliente_id: clienteId ? Number(clienteId) : null,
  itens: itens.map(({produto, quantidade}) => ({produto_id: produto.id, quantidade}))});
const quantidadeValida = q => Number.isInteger(q) && q >= 1 && q <= 999;

export async function orders(main, action) {
  if (action === 'novo') return newOrder(main);
  heading(main, 'Pedidos', 'Pedidos registrados, do mais recente para o mais antigo', link('+ Novo pedido', 'pedidos/novo', 'button primary'));
  const pedidos = await loading(main, () => list('/api/pedidos'));
  if (!pedidos) return;
  main.append(table(['Pedido', 'Data', 'Itens', 'Registrado por', 'Total'],
    pedidos.map(p => [
      '#' + p.id,
      new Date(p.data_pedido).toLocaleString('pt-BR'),
      (p.itens || []).map(i => `${i.quantidade} × ${i.nome}`).join(', ') || '—',
      p.usuario_nome || '—',
      money(p.valor_total)
    ])));
}

async function newOrder(main) {
  heading(main, 'Novo Pedido', 'Escolha os produtos e as quantidades', null, 'pedidos');
  const todos = await loading(main, () => list('/api/produtos'));
  if (!todos) return;
  const produtos = todos.filter(p => p.ativo !== false);
  const selected = new Map(), f = formBase('Confirmar pedido', 'pedidos');
  f.form.classList.add('card', 'form-card');
  const selector = field('produto', 'Produto', {options: [['', 'Selecione um produto'], ...produtos.map(p => [p.id, `${p.nome} — ${money(p.preco_venda)}`])]});
  const select = selector.querySelector('select'), itensBox = node('div'), total = node('p', {class: 'subtle'});
  const itensError = node('span', {id: 'itens-error', class: 'field-error', role: 'alert'});
  const draw = () => {
    itensBox.replaceChildren(...[...selected.values()].map(item => {
      const {produto} = item;
      const input = node('input', {type: 'number', min: 1, step: 1, value: item.quantidade, 'aria-label': 'Quantidade de ' + produto.nome});
      input.addEventListener('input', () => { item.quantidade = Number(input.value); refresh(); });
      const change = delta => { input.value = String(Math.max(1, Number(input.value || 1) + delta)); input.dispatchEvent(new Event('input')); };
      const decrease = button('−', () => change(-1), 'link-button'), increase = button('+', () => change(1), 'link-button');
      decrease.setAttribute('aria-label', 'Diminuir quantidade de ' + produto.nome); increase.setAttribute('aria-label', 'Aumentar quantidade de ' + produto.nome);
      const remove = button('×', async () => { if (await confirmDialog('Remover produto?', `Remover ${produto.nome} deste pedido?`, 'Remover')) { selected.delete(produto.id); draw(); } }, 'link-button remove');
      remove.setAttribute('aria-label', 'Remover ' + produto.nome);
      return node('div', {class: 'composition-item'}, node('div', {}, produto.nome, node('small', {}, money(produto.preco_venda) + ' cada')), node('div', {class: 'stepper'}, decrease, input, increase), remove);
    }));
    for (const option of select.options) option.disabled = selected.has(Number(option.value));
    refresh();
  };
  const refresh = () => {
    const itens = [...selected.values()];
    total.textContent = itens.length ? 'Total: ' + money(itens.every(i => quantidadeValida(i.quantidade)) ? totalDoPedido(itens) : null) : 'Nenhum produto adicionado.';
    f.submit.disabled = !itens.length;
  };
  const add = button('+ Adicionar produto', () => {
    const produto = produtos.find(p => String(p.id) === select.value);
    if (!produto) { itensError.textContent = 'Selecione um produto.'; return; }
    if (selected.has(produto.id)) { itensError.textContent = 'Este produto já foi adicionado ao pedido.'; return; }
    selected.set(produto.id, {produto, quantidade: 1}); itensError.textContent = ''; select.value = ''; draw();
  });
  f.form.append(node('h2', {}, 'Itens do pedido'), itensBox, itensError, selector, add, total, f.actions);
  bindForm(f, () => {
    const itens = [...selected.values()], issues = {};
    if (!itens.length) issues.itens = 'Adicione ao menos um produto.';
    else if (!itens.every(i => quantidadeValida(i.quantidade))) issues.itens = 'Informe quantidades inteiras maiores que zero.';
    return {payload: payloadPedido('', itens), issues};
  }, async payload => {
    const r = await request('/api/pedidos', {method: 'POST', body: payload});
    state.flash = 'Pedido registrado. Total: ' + money(r.valor_total) + '.'; go('pedidos/novo');
  });
  draw();
  main.append(f.form);
}
