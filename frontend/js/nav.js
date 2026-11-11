import {session} from './api.js';
export function go(path) { if (location.hash === '#/'+path) window.dispatchEvent(new Event('rerender')); else location.hash = '/'+path; }
export const admin = () => session()?.perfil === 'ADMINISTRADOR';
// Guarda de rotas por lista: o que o Funcionário pode abrir. A segurança de verdade é o 403 do backend.
const SO_ADMIN = new Set(['funcionarios', 'dashboard', 'porcao-padrao', 'producao', 'reposicao', 'marketing']);
const FUNCIONARIO = {inicio: [undefined], ingredientes: [undefined], produtos: [undefined],
                     estoque: [undefined, 'linhas', 'movimentar'], pedidos: [undefined, 'novo'], clientes: [undefined, 'novo']};
export const rotaPermitida = (section, action) =>
  admin() || (!SO_ADMIN.has(section) && Boolean(FUNCIONARIO[section]?.includes(action)));
