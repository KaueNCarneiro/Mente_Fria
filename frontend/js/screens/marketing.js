// Tela 14 (US18, só Administrador): até 3 ideias de divulgação para os produtos priorizados na última recomendação.
// O sistema só sugere: não há botão de publicar (critério da US18).
import {request} from '../api.js';
import {money, node, link, notice, heading, loading} from '../ui.js';
import {avisoMarketing} from '../apoio_telas_novas.js';

export async function marketing(main) {
  heading(main, 'Marketing', 'Ideias de divulgação para os produtos que mais valem a pena hoje');
  const r = await loading(main, () => request('/api/marketing/sugestoes'));
  if (!r) return;
  const sugestoes = (r.sugestoes || []).slice(0, 3), aviso = avisoMarketing({...r, sugestoes});
  if (r.calculado_em) main.append(node('p', {class: 'muted'}, 'Sugestões com base no cálculo de ' + new Date(r.calculado_em).toLocaleString('pt-BR') + '.'));
  if (aviso) { main.append(notice(aviso), node('p', {}, link('Ir para Produção', 'producao'))); return; }
  main.append(node('div', {class: 'suggestions'}, sugestoes.map(s =>
    node('article', {class: 'card'},
      node('span', {class: 'badge'}, s.selo || 'Produto priorizado'),
      node('h2', {}, s.produto),
      node('strong', {}, s.ideia),
      node('p', {}, s.como_fazer),
      node('small', {}, `Lucro por copo: ${money(s.lucro_por_copo)}. Use esse valor para saber quanto de desconto cabe numa promoção.`)))));
}
