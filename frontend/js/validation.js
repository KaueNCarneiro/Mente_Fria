export function isAcai(name) { return String(name || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().includes('acai'); }
export function decimal(raw, places, positive = false) {
  const text = String(raw).trim().replace(',', '.');
  if (!text || !new RegExp(`^\\d+(?:\\.\\d{1,${places}})?$`).test(text)) return null;
  const value = Number(text);
  return Number.isFinite(value) && value < 10 ** (10 - places) && (positive ? value > 0 : value >= 0) ? value : null;
}
// Multiplicação em milésimos evita resíduos binários na quantidade enviada.
export function quantity(portion, count) {
  if (!Number.isInteger(count) || count < 1 || !Number.isFinite(Number(portion)) || Number(portion) <= 0) return null;
  const result = Math.round(Number(portion) * 1000) * count;
  return Number.isSafeInteger(result) && result > 0 && result <= 9999999999 ? result / 1000 : null;
}
