// Tela 13 (US14, só Administrador): sugestão de compra de ingredientes para um período e um orçamento.
// Sem termos técnicos (US11/CT43): a "cobertura" calculada pelo backend nunca aparece na tela.
import {request} from '../api.js';
import {money, amount, unit, node, notice, field, errors, formBase, heading, table} from '../ui.js';
import {validarReposicao, resumoReposicao} from '../apoio_telas_novas.js';

const qtd = (valor, un) => amount(valor) + ' ' + unit(un);

export function restock(main) {
  heading(main, 'Reposição', 'O que comprar para o período, dentro do orçamento');
  const f = formBase('Gerar sugestão de compra');
  f.form.classList.add('card', 'form-card');
  f.form.append(
    node('div', {class: 'columns'},
      field('periodo_dias', 'Período (dias) *', {type: 'number', value: '30', min: 1, step: 1, hint: 'Considera os pedidos dos últimos dias, contando hoje.'}),
      field('orcamento', 'Orçamento (R$) *', {placeholder: '0,00', hint: 'Quanto você pode gastar nesta compra.'})),
    f.actions);
  const result = node('div', {class: 'stack'});
  f.form.addEventListener('submit', async event => {
    event.preventDefault(); if (f.submit.disabled) return;
    f.message.textContent = ''; f.message.className = 'notice error';
    const {payload, issues} = validarReposicao(Object.fromEntries(new FormData(f.form)));
    errors(f.form, issues);
    if (Object.keys(issues).length) { f.message.textContent = 'Confira os campos indicados.'; return; }
    f.submit.disabled = true; f.submit.textContent = 'Calculando…';
    result.replaceChildren();
    try {
      drawResult(result, await request('/api/reposicao/recomendacao', {method: 'POST', body: payload}));
    } catch (error) {
      // 503 = sugestão desligada neste endereço (reserva B1): aviso informativo, não erro.
      if (error.status === 503) result.replaceChildren(notice(error.message));
      else { f.message.textContent = error.message; errors(f.form, error.campos); }
    } finally { f.submit.disabled = false; f.submit.textContent = 'Gerar sugestão de compra'; }
  });
  main.append(f.form, result);
}

function drawResult(result, r) {
  const itens = r.itens || [];
  result.replaceChildren(
    notice(resumoReposicao(r), r.orcamento_suficiente === false ? 'error' : ''),
    itens.length ? node('div', {class: 'section'}, node('h2', {}, 'Sugestão de compra'),
      table(['Ingrediente', 'Consumo no período', 'Estoque válido', 'Mínimo', 'Quantidade sugerida', 'Custo estimado'],
        itens.map(x => [x.nome, qtd(x.consumo_periodo, x.unidade_medida), qtd(x.quantidade_estoque, x.unidade_medida), qtd(x.quantidade_minima, x.unidade_medida),
          Number(x.quantidade_sugerida) > 0 ? qtd(x.quantidade_sugerida, x.unidade_medida) : 'Não precisa comprar',
          Number(x.quantidade_sugerida) > 0 ? money(x.custo_estimado) : '—']))) : null);
}
