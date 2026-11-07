// Tela 12: quanto produzir de cada copo para ter o maior lucro com o estoque de hoje (só Administrador).
// Linguagem da US11/CT43: a tela nunca usa termos técnicos; fala em "lucro" e "quantidade".
import {request} from '../api.js';
import {money, node, button, notice, heading, table, stat} from '../ui.js';

export const motivoDoZero = item => item.quantidade > 0 ? null : (Number(item.lucro_por_copo) <= 0 ? 'Sem lucro' : 'Falta ingrediente');
export const avisoGeral = rec => rec.some(i => i.quantidade > 0) ? null
  : (rec.every(i => Number(i.lucro_por_copo) <= 0) ? 'Nenhum produto dá lucro hoje com os custos atuais.' : 'Estoque insuficiente para produzir agora.');

export function production(main) {
  heading(main, 'Produção', 'Quanto preparar de cada copo hoje');
  const result = node('div', {class: 'stack'});
  const gerar = button('Gerar recomendação', async () => {
    gerar.disabled = true; gerar.textContent = 'Calculando…';
    result.replaceChildren(notice('Calculando a melhor quantidade para o estoque de hoje…'));
    try {
      const r = await request('/api/producao/recomendacao', {method: 'POST'});
      drawResult(result, r);
    } catch (error) {
      // 503 = recomendação desligada neste endereço (reserva B1): aviso informativo, não erro.
      result.replaceChildren(notice(error.message, error.status === 503 ? '' : 'error'));
    } finally { gerar.disabled = false; gerar.textContent = 'Gerar recomendação'; }
  }, 'primary');
  main.append(
    node('section', {class: 'card stack'},
      node('p', {}, 'O sistema olha o estoque válido de cada ingrediente, o custo e o preço de cada copo, e mostra quanto preparar de cada um para ter o maior lucro possível hoje.'),
      node('div', {class: 'row'}, gerar)),
    result);
}

function drawResult(result, r) {
  const itens = r.recomendacao || [], aviso = avisoGeral(itens);
  result.replaceChildren(
    node('p', {class: 'muted'}, 'Calculado em ' + new Date(r.calculado_em).toLocaleString('pt-BR') + '. Esta é a quantidade que rende o maior lucro com o estoque que você tem hoje.'),
    aviso ? notice(aviso) : null,
    node('div', {class: 'stats'}, stat('Lucro potencial', money(r.margem_estimada)), stat('Receita estimada', money(r.receita_estimada))),
    node('div', {class: 'section'}, node('h2', {}, 'Quanto produzir'),
      table(['Produto', 'Quantidade', 'Lucro por copo', 'Observação'],
        itens.map(i => [i.nome, String(i.quantidade), money(i.lucro_por_copo), motivoDoZero(i) ?? '—']))));
}
