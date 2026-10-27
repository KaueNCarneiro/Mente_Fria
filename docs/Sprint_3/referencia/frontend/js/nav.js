import {session} from './api.js';
export function go(path) { if (location.hash === '#/'+path) window.dispatchEvent(new Event('rerender')); else location.hash = '/'+path; }
export const admin = () => session()?.perfil === 'ADMINISTRADOR';
