import { API_BASE_URL, REQUEST_TIMEOUT_MS } from './config.js';
const key = 'mente-fria.session';
export function session() {
  try {
    const value = JSON.parse(sessionStorage.getItem(key));
    return value && typeof value.token === 'string' && value.token && ['ADMINISTRADOR', 'FUNCIONARIO'].includes(value.perfil) ? value : null;
  } catch { return null; }
}
export function saveSession(value) {
  if (!value?.token || !['ADMINISTRADOR', 'FUNCIONARIO'].includes(value.perfil)) throw new Error('Resposta de login incompatível com o contrato.');
  sessionStorage.setItem(key, JSON.stringify({token: value.token, nome: value.nome, perfil: value.perfil}));
}
export function logout() { sessionStorage.removeItem(key); }
export class ApiError extends Error {
  constructor(message, status = 0, campos = {}) { super(message); this.status = status; this.campos = campos; }
}
export async function request(path, {method = 'GET', body, public: isPublic = false} = {}) {
  const token = isPublic ? null : session()?.token;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    const response = await fetch(API_BASE_URL.replace(/\/$/, '') + path, {
      method, signal: controller.signal,
      headers: {Accept: 'application/json', ...(body ? {'Content-Type': 'application/json'} : {}), ...(token ? {Authorization: `Bearer ${token}`} : {})},
      ...(body ? {body: JSON.stringify(body)} : {})
    });
    let data;
    try { data = response.status === 204 ? null : await response.json(); } catch { data = null; }
    if (!response.ok) {
      if (response.status === 401 && token && session()?.token === token) {
        logout(); window.dispatchEvent(new Event('session-expired'));
      }
      const fallback = {401: 'E-mail ou senha inválidos.', 403: 'Você não tem permissão para esta ação.', 404: 'Recurso ou rota não encontrado no servidor.', 500: 'O servidor não conseguiu concluir a operação.'};
      throw new ApiError(data?.mensagem || fallback[response.status] || `Não foi possível concluir a operação (HTTP ${response.status}).`, response.status, data?.campos || {});
    }
    if (response.status !== 204 && data === null) throw new ApiError('O servidor retornou uma resposta incompatível com o contrato.', response.status);
    return data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(error.name === 'AbortError' ? 'O servidor demorou para responder. Tente novamente em instantes.' : `Não foi possível conectar à API em ${API_BASE_URL}. Verifique se o backend está ligado e se permite a origem ${location.origin}. O navegador não distingue falha de conexão de bloqueio CORS nesta mensagem.`);
  } finally { clearTimeout(timeout); }
}
export async function list(path) {
  const data = await request(path);
  if (!Array.isArray(data)) throw new ApiError('A API deveria retornar uma lista. Confira o contrato de comunicação.');
  return data;
}
