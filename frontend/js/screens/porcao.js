// Tela 19 (US26, Contrato §2.11): a porção padrão de cada unidade de medida. Só Administrador (guarda: SO_ADMIN).
// Cadastrar uma unidade (POST) e alterar a porção padrão de uma unidade (PATCH). A porção padrão é só a SUGESTÃO
// do formulário de ingrediente: alterar aqui não muda os ingredientes já cadastrados (critério 3 da US26).
import {request, list} from '../api.js';
import {decimal} from '../validation.js';
import {state} from '../state.js';
import {amount, unit, node, notice, field, formBase, bindForm, heading, loading, table} from '../ui.js';
import {go} from '../nav.js';

const MSG_UNIDADE = 'Informe a unidade de medida (até 20 caracteres).';
const MSG_AUSENTE = 'Informe a porção.';
const MSG_ZERO = 'A porção deve ser maior que zero.';
const MSG_FORMATO = 'Use no máximo 3 casas decimais e um valor menor que 10.000.000.';

// Como o backend: sem espaços nas pontas e em minúsculas (" G " vira "g").
export const normalizarUnidade = texto => String(texto ?? '').trim().toLowerCase();

// Devolve {valor} ou {erro}, com as mesmas mensagens do contrato (§2.11). O decimal() aceita vírgula ou ponto.
export function validarPorcao(texto) {
  const bruto = String(texto ?? '').trim();
  if (!bruto) return {erro: MSG_AUSENTE};
  const valor = decimal(bruto, 3, true);
  if (valor !== null) return {valor};
  const numero = Number(bruto.replace(',', '.'));
  return {erro: Number.isFinite(numero) && numero <= 0 ? MSG_ZERO : MSG_FORMATO};
}

export async function porcaoPadrao(main) {
  heading(main, 'Porção Padrão', 'A porção sugerida para cada unidade de medida no cadastro de ingredientes');
  main.append(notice('Alterar a porção padrão não muda os ingredientes já cadastrados: ela é só a sugestão do formulário de ingrediente.'));
  const unidades = await loading(main, () => list('/api/unidades-medida'));
  if (!unidades) return;

  const status = notice();                                   // retorno do "Salvar" de cada linha
  main.append(status, table(['Unidade', 'Porção padrão', 'Alterar a porção'], unidades.map(u => linha(u, status))));
  main.append(formularioNovaUnidade());
}

// Uma linha da tabela: a porção atual e um formulário pequeno que faz o PATCH só desta unidade.
function linha(u, status) {
  const nome = unit(u.unidade_medida);
  const atual = node('span', {}, amount(u.porcao_padrao) + ' ' + nome);
  const input = node('input', {name: 'nova_porcao', type: 'text', inputmode: 'decimal', 'aria-label': 'Nova porção de ' + nome,
    placeholder: amount(u.porcao_padrao), style: 'max-width:140px'});           // o campo e o "Salvar" na mesma linha
  const erro = node('span', {class: 'field-error', role: 'alert'});
  const salvar = node('button', {type: 'submit', 'aria-label': 'Salvar porção de ' + nome}, 'Salvar');
  const form = node('form', {class: 'row', novalidate: true}, input, salvar, erro);
  form.addEventListener('submit', async event => {
    event.preventDefault(); if (salvar.disabled) return;
    erro.textContent = ''; input.removeAttribute('aria-invalid'); status.textContent = ''; status.className = 'notice';
    const {valor, erro: problema} = validarPorcao(input.value);
    if (problema) { erro.textContent = problema; input.setAttribute('aria-invalid', 'true'); input.focus(); return; }
    salvar.disabled = true; salvar.textContent = 'Salvando…';
    try {
      // encodeURIComponent: a unidade vai na URL e pode ter caracteres especiais.
      const r = await request('/api/unidades-medida/' + encodeURIComponent(u.unidade_medida), {method: 'PATCH', body: {porcao_padrao: valor}});
      atual.textContent = amount(r.porcao_padrao) + ' ' + unit(r.unidade_medida);
      input.value = ''; input.placeholder = amount(r.porcao_padrao);
      status.className = 'notice success';
      status.textContent = `Porção padrão de ${unit(r.unidade_medida)} atualizada para ${amount(r.porcao_padrao)}. Os ingredientes já cadastrados continuam com a porção deles.`;
    } catch (error) {
      erro.textContent = error.campos?.porcao_padrao || error.message; input.setAttribute('aria-invalid', 'true');
    } finally { salvar.disabled = false; salvar.textContent = 'Salvar'; }
  });
  return [node('strong', {}, nome), atual, form];
}

// "Nova unidade": POST com a unidade normalizada. Unidade repetida volta 409 no campo unidade_medida (bindForm mostra).
function formularioNovaUnidade() {
  const f = formBase('Cadastrar unidade');
  f.form.classList.add('card', 'form-card');
  f.form.append(node('h2', {}, 'Nova unidade'),
    node('div', {class: 'columns'},
      field('unidade_medida', 'Unidade de medida *', {maxlength: 20, placeholder: 'Ex.: g, cx, pct', hint: 'Até 20 caracteres. Será gravada em minúsculas.'}),
      field('porcao_padrao', 'Porção padrão *', {placeholder: 'Ex.: 40', hint: 'Maior que zero, com até 3 casas decimais.'})),
    f.actions);
  bindForm(f, raw => {
    const issues = {}, unidade = normalizarUnidade(raw.unidade_medida), porcao = validarPorcao(raw.porcao_padrao);
    if (!unidade || unidade.length > 20) issues.unidade_medida = MSG_UNIDADE;
    if (porcao.erro) issues.porcao_padrao = porcao.erro;
    return {payload: {unidade_medida: unidade, porcao_padrao: porcao.valor}, issues};
  }, async payload => {
    const r = await request('/api/unidades-medida', {method: 'POST', body: payload});
    state.flash = `Unidade ${unit(r.unidade_medida)} cadastrada com porção padrão ${amount(r.porcao_padrao)}.`;
    go('porcao-padrao');
  });
  return f.form;
}
