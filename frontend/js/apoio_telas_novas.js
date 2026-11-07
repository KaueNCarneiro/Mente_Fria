// Funções puras de apoio às Telas 03 (Dashboard), 13 (Reposição) e 14 (Marketing). Sem DOM e sem fetch:
// testáveis com Node (ferramentas/testar_apoio_telas.mjs). Formatos: Contrato v2 (proposta), §2.7 a §2.9.

const real = v => Number(v).toLocaleString('pt-BR', {style: 'currency', currency: 'BRL'});

/** Tela 03: monta a query de GET /api/dashboard só com os filtros preenchidos. */
export function filtrosDashboard({mes, usuarioId, produtoIds = [], valorMin, valorMax} = {}) {
  const q = new URLSearchParams();
  if (mes) q.set('mes', mes);                                   // "AAAA-MM" (o <input type="month"> já entrega assim)
  if (usuarioId) q.set('usuario_id', String(usuarioId));
  for (const id of produtoIds) q.append('produto_id', String(id));
  if (valorMin !== undefined && valorMin !== '') q.set('valor_min', String(valorMin));
  if (valorMax !== undefined && valorMax !== '') q.set('valor_max', String(valorMax));
  const s = q.toString();
  return '/api/dashboard' + (s ? '?' + s : '');
}

/** Tela 03: valida os filtros antes de chamar (o backend revalida). */
export function validarFiltrosDashboard({mes, valorMin, valorMax} = {}) {
  const issues = {};
  if (mes && !/^\d{4}-(0[1-9]|1[0-2])$/.test(mes)) issues.mes = 'Informe o mês no formato AAAA-MM.';
  const min = valorMin === '' || valorMin === undefined ? null : Number(valorMin);
  const max = valorMax === '' || valorMax === undefined ? null : Number(valorMax);
  if (min !== null && (!Number.isFinite(min) || min < 0)) issues.valor_min = 'Valor mínimo inválido.';
  if (max !== null && (!Number.isFinite(max) || max < 0)) issues.valor_max = 'Valor máximo inválido.';
  if (min !== null && max !== null && min > max) issues.valor_min = 'O valor mínimo não pode ser maior que o máximo.';
  return issues;
}

/** Tela 03: texto do bloco "Plano de produção" (receita máxima estimada do Termo), sem termos técnicos. */
// otimizadorHabilitado vem do campo otimizador_habilitado do dashboard (plano B da licença, A31).
export function textoUltimoPlano(plano, otimizadorHabilitado = true) {
  if (!plano && !otimizadorHabilitado) return 'O plano de produção está disponível só na demonstração local da equipe.';
  if (!plano) return 'Ainda não há plano de produção. Gere a recomendação na tela Produção.';
  const prioridade = plano.produtos_priorizados.length ? ` Priorize: ${plano.produtos_priorizados.join(', ')}.` : '';
  return `Com o estoque de ${new Date(plano.calculado_em).toLocaleString('pt-BR')}, a venda pode chegar a ${real(plano.receita_estimada)}`
    + ` (lucro de ${real(plano.margem_estimada)}).${prioridade}`;
}

/** Tela 13: valida período (dias inteiros de 1 a 365) e orçamento (R$, até 2 casas, >= 0). */
export function validarReposicao(raw) {
  const issues = {}, payload = {};
  const dias = Number(raw.periodo_dias);
  if (!Number.isInteger(dias) || dias < 1 || dias > 365) issues.periodo_dias = 'Informe um período entre 1 e 365 dias.';
  else payload.periodo_dias = dias;
  const texto = String(raw.orcamento ?? '').trim().replace(',', '.');
  if (!/^\d+(\.\d{1,2})?$/.test(texto)) issues.orcamento = 'Informe o orçamento em reais, com até 2 casas decimais.';
  else payload.orcamento = Number(texto);
  return {payload, issues};
}

/** Tela 13: frase de resumo acima da tabela. */
export function resumoReposicao(r) {
  if (r.mensagem && !r.itens.length) return r.mensagem;
  if (!r.orcamento_suficiente)
    return `O orçamento não cobre nem o mínimo de estoque. Faltam ${real(r.valor_faltante)}. A tabela mostra só o mínimo de cada ingrediente.`;
  if (Number(r.cobertura_minima) >= 1) return `O orçamento cobre tudo o que falta: ${real(r.custo_total)}.`;
  return `O orçamento cobre parte do que falta (${real(r.custo_total)}), repartido de forma equilibrada entre os ingredientes.`;
}

/** Tela 14: o que mostrar quando não há sugestão. */
export const avisoMarketing = r => (r.sugestoes.length ? null : r.mensagem);

/** US11, CT43 (vale para as Telas 12, 13 e 14): palavras que não podem aparecer para o dono. */
const PROIBIDOS = ['otimiza', 'solver', 'gurobi', 'programação linear', 'variável', 'restrição', 'função objetivo', 'cobertura_minima'];
export const termosTecnicos = texto => PROIBIDOS.filter(p => texto.toLowerCase().includes(p));
