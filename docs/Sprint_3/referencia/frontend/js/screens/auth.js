import {request, saveSession} from '../api.js';
import {state} from '../state.js';
import {root, node, link, logo, field, formBase, bindForm} from '../ui.js';
import {go, admin} from '../nav.js';
export function accountValidation(raw, register) {
  const issues={},payload={email:raw.email.trim().toLowerCase(),senha:raw.senha};
  if (!payload.email) issues.email='Informe o e-mail.';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email)) issues.email='Informe um e-mail válido.';
  if (!raw.senha) issues.senha='Informe a senha.';
  else if (register && raw.senha.length<8) issues.senha='A senha deve ter no mínimo 8 caracteres.';
  if (register) {payload.nome=raw.nome.trim();if (!payload.nome) issues.nome='Informe o nome.';}
  if ('confirmar' in raw && raw.confirmar!==raw.senha) issues.confirmar='As senhas devem ser iguais.';
  return {payload,issues};
}
export function authPage(register) {
  const main=node('main',{id:'main',class:'auth'+(register?'':' login')});
  const card=node('section',{class:'card auth-card stack'},logo(),node('h1',{},register?'Criar conta de Administrador':'Mente Fria'),node('p',{class:'muted'},register?'Você será o proprietário com acesso completo ao sistema':'Entrar no sistema de gestão'));
  const f=formBase(register?'Criar conta e entrar':'Entrar');
  if (state.flash) {f.message.textContent=state.flash;state.flash='';}
  if (register) f.form.append(field('nome','Nome completo',{maxlength:150,placeholder:'Nome do proprietário'}));
  f.form.append(field('email','E-mail',{type:'email',maxlength:150,placeholder:'seuemail@exemplo.com'}),field('senha','Senha',{type:'password',hint:register?'Mínimo de 8 caracteres':''}));
  if (!register) f.form.elements.senha.autocomplete='current-password';
  if (register) f.form.append(field('confirmar','Confirmar senha',{type:'password'}));
  f.form.append(f.submit);
  let created=false;
  bindForm(f,raw=>accountValidation(raw,register),async payload=>{
    if (register && !created) {await request('/api/auth/registro',{method:'POST',body:payload,public:true});created=true;}
    try {saveSession(await request('/api/auth/login',{method:'POST',body:{email:payload.email,senha:payload.senha},public:true}));}
    catch(error) {if(created){state.flash='Conta criada. Entre com seu e-mail e senha. '+error.message;go('login');return;}throw error;}
    go(admin()?'funcionarios':'inicio');
  });
  card.append(f.form,register?link('Já tenho conta — Entrar','login'):link('Ainda não tem conta? Criar conta de proprietário','cadastro'));
  if (!register) card.append(node('small',{},link('Verificar comunicação com o servidor','comunicacao')));
  main.append(card);root.append(main);
}
