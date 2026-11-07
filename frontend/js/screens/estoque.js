// Telas 10/16: estoque por ingrediente (lista), linhas de entrada (detalhe), movimentação e descarte de linha vencida.
import {list, request} from '../api.js';
import {decimal} from '../validation.js';
import {state} from '../state.js';
import {amount, unit, node, link, button, notice, field, formBase, bindForm, heading, loading, table, confirmDialog} from '../ui.js';
import {go} from '../nav.js';

const SITUACAO = {VENCIDA: 'Vencida', PROXIMA: 'Vence em breve', OK: 'Dentro da validade', SEM_VALIDADE: 'Sem validade'};
// 'T00:00:00' mantém a data no fuso local (sem a hora, '2026-12-10' seria meia-noite UTC e viraria o dia 9 no Brasil).
const data = iso => iso ? new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR') : '—';
const qtd = (valor, un) => amount(valor) + ' ' + unit(un);
export const abaixoDoMinimo = item => Number(item.quantidade_valida) <= Number(item.quantidade_minima);

export async function stock(main, action, id) {
  if (action === 'linhas') return stockLines(main, id);
  if (action === 'movimentar') return movement(main);
  heading(main, 'Estoque', 'Quanto há de cada ingrediente, quanto ainda pode ser usado e quanto venceu', link('+ Registrar movimentação', 'estoque/movimentar', 'button primary'));
  const itens = await loading(main, () => list('/api/estoque'));
  if (!itens) return;
  main.append(table(
    ['Ingrediente', 'Total', 'Válido', 'Vencido', 'Validade mais próxima', 'Situação', 'Ação'],
    itens.map(x => [
      x.nome,
      qtd(x.quantidade_estoque, x.unidade_medida),
      qtd(x.quantidade_valida, x.unidade_medida),
      Number(x.quantidade_vencida) > 0 ? qtd(x.quantidade_vencida, x.unidade_medida) : '—',
      data(x.validade_mais_proxima),
      abaixoDoMinimo(x) ? node('span', {class: 'badge inactive'}, 'Abaixo do mínimo') : 'Ok',
      link('Ver linhas', 'estoque/linhas/' + x.ingrediente_id)
    ])));
}

async function stockLines(main, id) {
  heading(main, 'Linhas de entrada', 'Cada entrada do ingrediente, quanto resta dela e a validade', null, 'estoque');
  const dados = await loading(main, () => Promise.all([list('/api/estoque'), list(`/api/estoque/${id}/linhas`)]));
  if (!dados) return;
  const [itens, linhas] = dados, item = itens.find(x => String(x.ingrediente_id) === id);
  if (item) main.append(node('h2', {}, item.nome));
  const message = notice();
  main.append(message);
  main.append(table(
    ['Entrada em', 'Quantidade', 'Resta', 'Validade', 'Situação', 'Ação'],
    linhas.map(l => [
      new Date(l.data_movimentacao).toLocaleString('pt-BR'),
      amount(l.quantidade),
      amount(l.quantidade_restante),
      data(l.data_validade),
      SITUACAO[l.situacao] ?? l.situacao,
      l.situacao === 'VENCIDA' && Number(l.quantidade_restante) > 0 ? discardButton(l, item, message) : '—'
    ])));
}

// Botão "Descartar" (A12): só em linha vencida com saldo; pede confirmação e manda uma única chamada por clique.
function discardButton(linha, item, message) {
  const restante = Number(linha.quantidade_restante).toLocaleString('pt-BR', {minimumFractionDigits: 3, maximumFractionDigits: 3});
  const btn = button('Descartar', async () => {
    if (btn.disabled) return;
    btn.disabled = true;
    const ok = await confirmDialog('Descartar linha vencida?', `Descartar ${restante}${item ? ' ' + unit(item.unidade_medida) : ''} vencidos desta linha? Isso não pode ser desfeito.`, 'Descartar');
    if (!ok) { btn.disabled = false; return; }
    btn.textContent = 'Descartando…';
    try {
      await request(`/api/estoque/linhas/${linha.id}/descarte`, {method: 'POST'});
      state.flash = 'Descarte registrado.';
      window.dispatchEvent(new Event('rerender'));
    } catch (error) {
      message.className = 'notice error'; message.setAttribute('role', 'alert'); message.textContent = error.message;
      btn.disabled = false; btn.textContent = 'Descartar';
    }
  }, 'link-button remove');
  btn.setAttribute('aria-label', 'Descartar linha ' + linha.id);
  return btn;
}

/** Valida a movimentação antes de enviar. linha = item de GET /api/estoque do ingrediente escolhido. */
export function validarMovimentacao(raw, linha) {
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
      if (payload.quantidade > limite) issues.quantidade = `Quantidade maior que a disponível (${amount(limite)}).`;
    }
  }
  if (raw.observacao?.trim()) payload.observacao = raw.observacao.trim();
  return {payload, issues};
}

async function movement(main) {
  heading(main, 'Registrar movimentação', 'Entrada de mercadoria ou saída do estoque', null, 'estoque');
  const itens = await loading(main, () => list('/api/estoque'));
  if (!itens) return;
  const f = formBase('Registrar movimentação', 'estoque');
  f.form.classList.add('card', 'form-card');
  f.form.append(
    field('ingrediente_id', 'Ingrediente *', {options: [['', 'Selecione um ingrediente'], ...itens.map(x => [x.ingrediente_id, `${x.nome} (${unit(x.unidade_medida)})`])]}),
    field('tipo', 'Tipo *', {value: 'ENTRADA', options: [['ENTRADA', 'Entrada'], ['SAIDA', 'Saída']]}),
    field('motivo', 'Motivo da saída *', {options: [['', 'Selecione o motivo'], ['CONVENCIONAL', 'Saída convencional'], ['DESCARTE_VENCIDO', 'Descarte de vencidos']]}),
    field('quantidade', 'Quantidade *', {placeholder: '0,000', hint: 'Na unidade do ingrediente. Até 3 casas decimais.'}),
    field('data_validade', 'Validade', {type: 'date', hint: 'Opcional. Só em entradas.'}),
    field('observacao', 'Observação', {maxlength: 255, placeholder: 'Ex: Lote 3'}),
    f.actions);
  const el = f.form.elements, escolhido = () => itens.find(x => String(x.ingrediente_id) === el.ingrediente_id.value);
  // Validade só na entrada; motivo só na saída; descarte só com quantidade vencida.
  const sync = () => {
    const saida = el.tipo.value === 'SAIDA', item = escolhido();
    el.motivo.disabled = !saida; el.data_validade.disabled = saida;
    if (saida) el.data_validade.value = ''; else el.motivo.value = '';
    const descarte = el.motivo.querySelector('option[value="DESCARTE_VENCIDO"]');
    descarte.disabled = !(item && Number(item.quantidade_vencida) > 0);
    if (descarte.disabled && el.motivo.value === 'DESCARTE_VENCIDO') el.motivo.value = '';
  };
  el.tipo.addEventListener('change', sync); el.ingrediente_id.addEventListener('change', sync); sync();
  bindForm(f, raw => validarMovimentacao(raw, escolhido()), async payload => {
    await request('/api/estoque/movimentacoes', {method: 'POST', body: payload});
    state.flash = 'Movimentação registrada.'; go('estoque');
  });
  main.append(f.form);
}
