// Uso: node comparar_retratos.mjs retrato_antes.json retrato_depois.json
// Compara os "retratos" (HTML de cada tela/estado) gerados por retratar_telas.mjs antes e depois de uma refatoração.
import fs from 'node:fs';
const [a, b] = process.argv.slice(2).map(f => JSON.parse(fs.readFileSync(f, 'utf8')));
const chaves = new Set([...Object.keys(a), ...Object.keys(b)]);
const dif = [...chaves].filter(k => !a[k] || !b[k] || a[k].html !== b[k].html || a[k].titulo !== b[k].titulo || a[k].hash !== b[k].hash);
console.log(`retratos: antes ${Object.keys(a).length} | depois ${Object.keys(b).length}`);
if (!dif.length) { console.log('IGUAIS: o HTML, o título e a rota de todas as telas são idênticos.'); process.exit(0); }
console.log('DIFERENÇAS em:'); dif.forEach(k => console.log(' -', k)); process.exit(1);
