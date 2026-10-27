import {state} from './state.js';
export const root = document.querySelector('#app');
export const money = value => value == null ? '—' : Number(value).toLocaleString('pt-BR', {style:'currency',currency:'BRL'});
export const amount = value => value == null ? '—' : Number(value).toLocaleString('pt-BR', {maximumFractionDigits:3});
export const unit = value => value === 'l' ? 'L' : value;
export const node = (tag, props = {}, ...children) => {
  const el = document.createElement(tag);
  for (const [key,value] of Object.entries(props)) {
    if (key.startsWith('on')) el.addEventListener(key.slice(2).toLowerCase(), value);
    else if (key === 'class') el.className = value;
    else if (value !== undefined && value !== null && value !== false) el.setAttribute(key, value === true ? '' : value);
  }
  for (const child of children.flat(Infinity)) if (child !== null && child !== undefined) el.append(child instanceof Node ? child : document.createTextNode(String(child)));
  return el;
};
export const link = (text, path, cls = '') => node('a', {href:'#/'+path,class:cls}, text);
export const button = (text, action, cls = '') => node('button', {type:'button',class:cls,onclick:action}, text);
export const logo = () => node('div',{class:'logo'},node('img',{src:'assets/logo.png',alt:'Logo Mente Fria'}));
export const notice = (text = '', kind = '') => node('div',{class:`notice ${kind}`,role:kind === 'error' ? 'alert' : 'status'},text);
export function field(name, label, {type='text',value='',hint='',options,maxlength,placeholder,step,min} = {}) {
  const input = options ? node('select',{name,id:name},options.map(([v,t])=>node('option',{value:v},t))) : node('input',{name,id:name,type,maxlength,placeholder,step,min,autocomplete:type === 'password' ? 'new-password' : name === 'email' ? 'email' : name === 'nome' ? 'name' : 'off'});
  input.value = value;
  input.setAttribute('aria-describedby', `${name}-error${hint ? ' '+name+'-hint' : ''}`);
  return node('div',{class:'field'},node('label',{for:name},label),input,hint ? node('small',{id:name+'-hint'},hint) : null,node('span',{id:name+'-error',class:'field-error'}));
}
export function errors(form, values = {}) {
  form.querySelectorAll('.field-error').forEach(el=>el.textContent='');
  form.querySelectorAll('[aria-invalid]').forEach(el=>el.removeAttribute('aria-invalid'));
  for (const [name,message] of Object.entries(values)) {
    const input = form.elements.namedItem(name);
    const error = form.querySelector('#'+CSS.escape(name)+'-error');
    if (error) error.textContent = message;
    if (input instanceof HTMLElement) input.setAttribute('aria-invalid','true');
  }
  form.querySelector('[aria-invalid=true]')?.focus();
}
export function formBase(label, back) {
  const form = node('form',{novalidate:true,class:'stack'});
  const message = notice();
  const submit = node('button',{type:'submit',class:'primary'},label);
  form.append(message);
  return {form,message,submit,actions:node('div',{class:'form-actions'},submit,back ? link('Cancelar',back,'button') : null)};
}
export function bindForm({form,message,submit}, validate, save) {
  form.addEventListener('submit', async event => {
    event.preventDefault(); if (submit.disabled) return;
    message.textContent=''; message.className='notice error';
    const raw = Object.fromEntries(new FormData(form));
    const {payload,issues={}} = validate(raw);
    errors(form,issues);
    if (Object.keys(issues).length) {message.textContent='Confira os campos indicados.';return;}
    submit.disabled=true; form.setAttribute('aria-busy','true');
    const label=submit.textContent; submit.textContent='Salvando…';
    try {await save(payload);} catch (error) {message.textContent=error.message;errors(form,error.campos);} finally {submit.disabled=false;submit.textContent=label;form.removeAttribute('aria-busy');}
  });
}
export function heading(main,title,subtitle,action,back) {
  document.title=title+' · Mente Fria';
  main.append(node('div',{class:'heading'},node('div',{},node('h1',{},title),node('p',{},subtitle),back?link('← Voltar para lista',back,'back'):null),action));
  if(state.flash){main.append(notice(state.flash,'success'));state.flash='';}
}
export async function loading(main,task) {
  const stamp=state.revision, status=notice('Carregando…'); main.append(status);
  try {const data=await task();if(stamp!==state.revision)return null;status.remove();return data;}
  catch(error){if(stamp===state.revision){status.className='notice error';status.setAttribute('role','alert');status.replaceChildren(node('p',{},error.message),button('Tentar novamente',()=>window.dispatchEvent(new Event('rerender')),'link-button'));}return null;}
}
export function table(headers,rows) {
  return node('div',{class:'table-wrap'},node('table',{},node('thead',{},node('tr',{},headers.map(h=>node('th',{scope:'col'},h)))),node('tbody',{},rows.length?rows.map(row=>node('tr',{},row.map(cell=>node('td',{},cell)))):node('tr',{},node('td',{colspan:headers.length,class:'empty'},'Nenhum registro cadastrado.')))));
}
export const badge = active => node('span',{class:'badge '+(active===true?'active':'inactive')},active===true?'Ativo':active===false?'Inativo':'Não informado');
export function confirmRemoval(name) {
  return new Promise(resolve=>{
    const dialog=node('dialog',{'aria-labelledby':'confirm-title'},node('div',{class:'stack'},node('h2',{id:'confirm-title'},'Remover ingrediente?'),node('p',{},`Remover ${name} da composição deste produto?`),node('div',{class:'row'},button('Cancelar',()=>dialog.close('cancel')),button('Remover',()=>dialog.close('remove'),'primary'))));
    dialog.addEventListener('close',()=>{const yes=dialog.returnValue==='remove';dialog.remove();resolve(yes);},{once:true});document.body.append(dialog);dialog.showModal();
  });
}
