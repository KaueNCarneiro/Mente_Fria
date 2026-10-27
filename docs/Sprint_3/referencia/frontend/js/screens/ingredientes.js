import {request, list} from '../api.js';
import {decimal} from '../validation.js';
import {state} from '../state.js';
import {money, amount, unit, node, link, button, notice, field, formBase, bindForm, heading, loading, table} from '../ui.js';
import {go, admin} from '../nav.js';
export async function ingredients(main,action,id) {
  const edit=action==='editar', formView=edit||action==='novo';
  heading(main,formView?(edit?'Editar Ingrediente':'Novo Ingrediente'):'Ingredientes',formView?'Cadastre um ingrediente para controlar o que você tem disponível':'Controle os ingredientes disponíveis e seus custos',!formView&&admin()?link('+ Novo ingrediente','ingredientes/novo','button primary'):null,formView?'ingredientes':null);
  if(!formView){const data=await loading(main,()=>list('/api/ingredientes'));if(data)main.append(table(['Nome','Unidade','Custo unitário','Estoque atual',...(admin()?['Ação']:[])],data.map(x=>[x.nome,unit(x.unidade_medida),money(x.custo_unitario),amount(x.quantidade_estoque)+' '+unit(x.unidade_medida),...(admin()?[link('Editar','ingredientes/editar/'+x.id)]:[])])));return;}
  const data=await loading(main,()=>Promise.all([list('/api/unidades-medida'),edit?list('/api/ingredientes'):Promise.resolve([])]));if(!data)return;
  const [units,items]=data, item=edit?items.find(x=>String(x.id)===id):{};
  if(!item){main.append(notice('Ingrediente não encontrado.','error'));return;}
  if(edit&&['custo_unitario','porcao_padrao','quantidade_minima'].some(k=>item[k]==null)){main.append(notice('A API ainda retorna apenas o resumo do ingrediente. Para editar, a listagem precisa incluir custo, porção e quantidade mínima, conforme o contrato.','error'));return;}
  const f=formBase('Salvar ingrediente','ingredientes');f.form.classList.add('card','form-card','ingredient-card');
  f.form.append(field('nome','Nome do ingrediente *',{value:item.nome||'',maxlength:100,placeholder:'Ex: Polpa de açaí'}),node('div',{class:'columns'},field('unidade_medida','Unidade de medida *',{value:item.unidade_medida||'',options:[['','Selecione uma unidade'],...units.map(x=>[x.unidade_medida,unit(x.unidade_medida)])]}),field('custo_unitario','Custo unitário (R$) *',{value:item.custo_unitario??'',hint:'Não pode ser negativo',placeholder:'0,00'})),field('porcao_padrao','Porção padrão *',{value:item.porcao_padrao??'',hint:'Quantidade de uma porção na unidade selecionada. Você pode ajustar a sugestão.'}),field('quantidade_minima','Quantidade mínima em estoque *',{value:item.quantidade_minima??'',hint:'Obrigatório. Informe zero ou uma quantidade maior.'}),f.actions);
  f.form.elements.unidade_medida.addEventListener('change',()=>{const selected=units.find(x=>x.unidade_medida===f.form.elements.unidade_medida.value);f.form.elements.porcao_padrao.value=selected?.porcao_padrao??'';});
  bindForm(f,raw=>{
    const issues={},payload={nome:raw.nome.trim(),unidade_medida:raw.unidade_medida};
    if(!payload.nome)issues.nome='Informe o nome do ingrediente.';
    if(!units.some(x=>x.unidade_medida===raw.unidade_medida))issues.unidade_medida='Selecione uma unidade de medida cadastrada.';
    for(const [key,places,positive,label] of [['custo_unitario',2,false,'O custo não pode ser negativo.'],['porcao_padrao',3,true,'A porção deve ser maior que zero.'],['quantidade_minima',3,false,'A quantidade mínima não pode ser negativa.']]){
      payload[key]=decimal(raw[key],places,positive);if(payload[key]===null)issues[key]=label+` Informe um número com até ${places} casas decimais, dentro do limite do campo.`;
    }
    return {payload,issues};
  },async payload=>{await request('/api/ingredientes'+(edit?'/'+id:''),{method:edit?'PUT':'POST',body:payload});state.flash='Ingrediente salvo com sucesso.';go('ingredientes');});
  if(!units.length){f.message.textContent='Nenhuma unidade cadastrada no servidor. Solicite à equipe a carga inicial das unidades.';f.submit.disabled=true;}main.append(f.form);
}
