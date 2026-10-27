// Confere as funções de apoio das Telas 03, 13 e 14 e o config.js de produção, sem navegador.
// Uso (de dentro de frontend/, depois de copiar js/apoio_telas_novas.js):  node ferramentas/testar_apoio_telas.mjs .
import assert from 'node:assert/strict';
import {pathToFileURL} from 'node:url';
import path from 'node:path';

const raiz = path.resolve(process.argv[2] ?? '.');
const m = await import(pathToFileURL(path.join(raiz, 'js', 'apoio_telas_novas.js')));
let ok = 0;
const caso = (nome, f) => { f(); ok++; console.log('OK', nome); };

caso('dashboard sem filtro', () => assert.equal(m.filtrosDashboard(), '/api/dashboard'));
caso('dashboard com todos os filtros', () => assert.equal(
  m.filtrosDashboard({mes: '2026-10', usuarioId: 2, produtoIds: [1, 3], valorMin: 50, valorMax: ''}),
  '/api/dashboard?mes=2026-10&usuario_id=2&produto_id=1&produto_id=3&valor_min=50'));
caso('filtros inválidos', () => assert.deepEqual(m.validarFiltrosDashboard({mes: '10/2026', valorMin: '50', valorMax: '10'}),
  {mes: 'Informe o mês no formato AAAA-MM.', valor_min: 'O valor mínimo não pode ser maior que o máximo.'}));
caso('plano ausente', () => assert.match(m.textoUltimoPlano(null), /Gere a recomendação na tela Produção/));
caso('plano com o otimizador desligado (A31)', () => {
  const t = m.textoUltimoPlano(null, false);
  assert.match(t, /só na demonstração local da equipe/); assert.doesNotMatch(t, /Gere a recomendação/);
  assert.deepEqual(m.termosTecnicos(t), []);
});
caso('plano da demonstração', () => {
  const t = m.textoUltimoPlano({calculado_em: '2026-10-28T15:00:00', receita_estimada: 1966.8, margem_estimada: 1011.71,
    produtos_priorizados: ['Açaí Tradicional 300ml', 'Açaí Especial 500ml']});
  assert.match(t, /R\$\s1\.966,80/); assert.match(t, /R\$\s1\.011,71/); assert.match(t, /Priorize: Açaí Tradicional 300ml, Açaí Especial 500ml/);
  assert.deepEqual(m.termosTecnicos(t), []);
});
caso('reposição válida com vírgula', () => assert.deepEqual(m.validarReposicao({periodo_dias: '30', orcamento: '150,5'}),
  {payload: {periodo_dias: 30, orcamento: 150.5}, issues: {}}));
caso('reposição inválida', () => assert.deepEqual(m.validarReposicao({periodo_dias: '0', orcamento: '10,001'}).issues,
  {periodo_dias: 'Informe um período entre 1 e 365 dias.', orcamento: 'Informe o orçamento em reais, com até 2 casas decimais.'}));
caso('resumo: insuficiente', () => assert.match(m.resumoReposicao({orcamento_suficiente: false, valor_faltante: 10.71, itens: [{}]}), /Faltam R\$\s10,71/));
caso('resumo: cobre tudo', () => assert.match(m.resumoReposicao({orcamento_suficiente: true, cobertura_minima: 1, custo_total: 63.22, itens: [{}]}), /cobre tudo.*63,22/));
caso('resumo: sem pedidos', () => assert.equal(m.resumoReposicao({mensagem: 'Não houve pedidos no período escolhido, então não há consumo para repor.', itens: []}),
  'Não houve pedidos no período escolhido, então não há consumo para repor.'));
caso('marketing sem cálculo', () => assert.equal(m.avisoMarketing({sugestoes: [], mensagem: 'Gere a recomendação na tela Produção para ver as sugestões.'}),
  'Gere a recomendação na tela Produção para ver as sugestões.'));
caso('termos técnicos detectados', () => assert.deepEqual(m.termosTecnicos('Resultado da otimização pelo Gurobi'), ['otimiza', 'gurobi']));

// config.js de produção (US20): escolhe a URL e o tempo limite pelo endereço da página
const cfg = path.join(raiz, 'js', 'config.js');
for (const [host, url, tempo] of [['equipe.github.io', 'https://', 90000], ['127.0.0.1', 'http://localhost:8080', 15000]]) {
  globalThis.location = {hostname: host};
  const c = await import(pathToFileURL(cfg).href + '?h=' + host);
  caso(`config.js em ${host}`, () => { assert.ok(c.API_BASE_URL.startsWith(url)); assert.equal(c.REQUEST_TIMEOUT_MS, tempo); });
}
console.log(`${ok} verificações passaram.`);
