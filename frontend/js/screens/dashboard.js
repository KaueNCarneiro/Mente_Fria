// Tela 03 (US17, só Administrador): faturamento do mês, mais vendidos, ingredientes, ranking, plano de produção e estoque.
import {list, request} from '../api.js';
import {money, amount, unit, node, link, notice, field, errors, heading, loading, table, stat} from '../ui.js';
import {filtrosDashboard, validarFiltrosDashboard, textoUltimoPlano} from '../apoio_telas_novas.js';

const mesAtual = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`; };
const valor = texto => String(texto ?? '').trim().replace(',', '.');

export async function dashboard(main) {
  heading(main, 'Dashboard', 'Vendas, ingredientes e plano de produção do mês');
  const opcoes = await loading(main, () => Promise.all([list('/api/usuarios'), list('/api/produtos')]));
  if (!opcoes) return;
  const [usuarios, produtos] = opcoes;
  const form = node('form', {novalidate: true, class: 'card stack'});
  const usuario = field('usuario_id', 'Usuário', {options: [['', 'Todos'], ...usuarios.map(u => [u.id, u.nome])]});
  const checks = node('div', {class: 'checks'}, produtos.map(p => node('label', {}, node('input', {type: 'checkbox', name: 'produto_id', value: p.id}), p.nome)));
  const aplicar = node('button', {type: 'submit', class: 'primary'}, 'Aplicar filtros');
  form.append(
    node('div', {class: 'filters'},
      field('mes', 'Mês', {type: 'month', value: mesAtual()}), usuario,
      field('valor_min', 'Valor mínimo (R$)', {placeholder: '0,00'}), field('valor_max', 'Valor máximo (R$)', {placeholder: '0,00'})),
    node('div', {class: 'field'}, node('span', {class: 'muted'}, 'Tipo de produto'), checks),
    node('div', {class: 'form-actions'}, aplicar));
  const result = node('div', {class: 'stack'});
  main.append(form, result);

  const carregar = async () => {
    const el = form.elements;
    const filtros = {mes: el.mes.value, usuarioId: el.usuario_id.value, produtoIds: [...form.querySelectorAll('[name=produto_id]:checked')].map(c => c.value),
      valorMin: valor(el.valor_min.value), valorMax: valor(el.valor_max.value)};
    const issues = validarFiltrosDashboard(filtros);
    errors(form, issues);
    if (Object.keys(issues).length) return;
    aplicar.disabled = true;
    result.replaceChildren(notice('Carregando…'));
    try {
      const d = await request(filtrosDashboard(filtros));
      // O Administrador não vem em /api/usuarios; ele entra no filtro quando aparece no ranking.
      for (const r of d.ranking || []) if (![...el.usuario_id.options].some(o => o.value === String(r.usuario_id))) el.usuario_id.append(node('option', {value: r.usuario_id}, r.usuario));
      draw(result, d);
    } catch (error) {
      result.replaceChildren(notice(error.message, 'error'));
      errors(form, error.campos);
    } finally { aplicar.disabled = false; }
  };
  form.addEventListener('submit', event => { event.preventDefault(); carregar(); });
  await carregar();
}

function draw(result, d) {
  const plano = d.ultimo_plano, habilitado = d.otimizador_habilitado !== false;
  result.replaceChildren(
    node('div', {class: 'stats'},
      stat('Faturamento do mês', money(d.faturamento?.valor_total), `${d.faturamento?.quantidade_pedidos ?? 0} pedidos`),
      stat('Ingredientes abaixo do mínimo', String(d.estoque?.ingredientes_abaixo_do_minimo ?? 0)),
      stat('Ingredientes com quantidade vencida', String(d.estoque?.ingredientes_com_vencido ?? 0))),
    node('p', {}, link('Ver estoque', 'estoque')),
    node('section', {class: 'card stack'}, node('h2', {}, 'Plano de produção'), node('p', {}, textoUltimoPlano(plano, habilitado)),
      !plano && habilitado ? link('Ir para Produção', 'producao') : null),
    node('div', {class: 'section'}, node('h2', {}, 'Mais vendidos'),
      table(['Produto', 'Quantidade', 'Valor'], (d.mais_vendidos || []).map(x => [x.nome, String(x.quantidade), money(x.valor_total)]))),
    node('div', {class: 'section'}, node('h2', {}, 'Ingredientes mais utilizados'),
      table(['Ingrediente', 'Quantidade', 'Custo'], (d.ingredientes_mais_utilizados || []).map(x => [x.nome, amount(x.quantidade) + ' ' + unit(x.unidade_medida), money(x.custo_estimado)]))),
    node('div', {class: 'section'}, node('h2', {}, 'Ranking de vendas'),
      table(['Rank', 'Usuário', 'Quantidade de pedidos', 'Valor total', 'Tipos de produto'], (d.ranking || []).map(x => [String(x.rank), x.usuario, String(x.quantidade_pedidos), money(x.valor_total), x.tipos_de_produto || '—']))));
}
