import {request, list} from '../api.js';
import {state} from '../state.js';
import {node, link, button, field, formBase, bindForm, heading, loading, table, badge} from '../ui.js';
import {go} from '../nav.js';
import {accountValidation} from './auth.js';
export async function employees(main,isNew) {
  heading(main,isNew?'Novo Funcionário':'Funcionários',isNew?'Cadastre um colaborador com acesso limitado ao sistema':'Gerencie o acesso da equipe ao sistema',isNew?null:link('+ Novo funcionário','funcionarios/novo','button primary'),isNew?'funcionarios':null);
  if(!isNew){const data=await loading(main,()=>list('/api/usuarios'));if(data)main.append(table(['Nome','E-mail','Status'],data.map(x=>[x.nome,x.email,badge(x.ativo)])));return;}
  const f=formBase('Salvar funcionário','funcionarios');f.form.classList.add('card','form-card');
  f.form.append(field('nome','Nome completo *',{maxlength:150,placeholder:'Ex: Maria Souza'}),field('email','E-mail *',{type:'email',maxlength:150,placeholder:'Ex: maria@mentefria.com'}),field('senha','Senha *',{type:'password',hint:'Mínimo de 8 caracteres. O e-mail deve ser único no sistema.'}),node('div',{class:'subtle'},node('p',{},'Perfil de acesso: Funcionário'),node('small',{},'Não acessa cadastro de preços nem configurações administrativas')),node('p',{},'✓ Conta criada ativa'),f.actions);
  bindForm(f,raw=>accountValidation(raw,true),async payload=>{await request('/api/usuarios',{method:'POST',body:payload});state.flash='Funcionário cadastrado com sucesso.';go('funcionarios');});main.append(f.form);
}
