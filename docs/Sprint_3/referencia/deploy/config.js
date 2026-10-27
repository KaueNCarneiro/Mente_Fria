// frontend/js/config.js — proposta para o deploy (US20).
// Base sem /api. As rotas do contrato já contêm esse prefixo.
// No GitHub Pages usa o backend do Render; em qualquer outro endereço (Live Server, http.server, testes), o local.
const PRODUCAO = 'https://mente-fria-backend.onrender.com';   // TROCAR pela URL que o Render mostrar no painel
const NO_PAGES = location.hostname.endsWith('github.io');

export const API_BASE_URL = NO_PAGES ? PRODUCAO : 'http://localhost:8080';
// O plano free do Render "dorme" após 15 min sem uso e leva ~1 min para acordar (documentação, 26/10/2026).
// Com 15 s, a primeira chamada do dia daria "O servidor demorou para responder". Em produção, 90 s.
export const REQUEST_TIMEOUT_MS = NO_PAGES ? 90000 : 15000;
