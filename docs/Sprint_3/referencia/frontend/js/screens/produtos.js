import {request, list} from '../api.js';
import {decimal, quantity, isAcai} from '../validation.js';
import {state} from '../state.js';
import {money, amount, unit, node, link, button, notice, field, formBase, bindForm, heading, loading, table, badge, confirmRemoval} from '../ui.js';
import {go, admin} from '../nav.js';
export async function products(main,action,id) {
  const isNew=action==='novo',price=action==='preco';
  heading(main,isNew?'Novo Produto':price?'Alterar preço':'Produtos',isNew?'Cadastre um copo de açaí ou combo e sua composição de ingredientes':price?'Atualize o preço de venda do produto':'Copos de açaí e combos vendidos, com composição e preço',!isNew&&!price&&admin()?link('+ Novo produto','produtos/novo','button primary'):null,isNew||price?'produtos':null);
  if(!isNew&&!price){const data=await loading(main,()=>list('/api/produtos'));if(data)main.append(table(['Produto','Preço de venda','Status',...(admin()?['Ação']:[])],data.map(x=>[node('details',{},node('summary',{},x.nome),Array.isArray(x.composicao)&&x.composicao.length?node('ul',{},x.composicao.map(c=>node('li',{},`${c.ingrediente_nome||c.nome||'Ingrediente #'+c.ingrediente_id}: ${amount(c.quantidade_utilizada)} ${unit(c.unidade_medida)||''}`))):node('p',{class:'muted'},'Composição não informada pela API.')),money(x.preco_venda),badge(x.ativo),...(admin()?[link('Alterar preço','produtos/preco/'+x.id)]:[])])));return;}
  if(price){
    const data=await loading(main,()=>list('/api/produtos'));if(!data)return;const item=data.find(x=>String(x.id)===id);if(!item){main.append(notice('Produto não encontrado.','error'));return;}
    const f=formBase('Salvar preço','produtos');f.form.classList.add('card','form-card');f.form.append(node('h2',{},item.nome),field('preco_venda','Preço de venda (R$) *',{value:item.preco_venda}),f.actions);
    bindForm(f,raw=>{const value=decimal(raw.preco_venda,2,true);return {payload:{preco_venda:value},issues:value===null?{preco_venda:'O preço deve ser maior que zero. Use até 2 casas decimais.'}:{}};},async payload=>{await request('/api/produtos/'+id+'/preco',{method:'PUT',body:payload});state.flash='Preço atualizado com sucesso.';go('produtos');});main.append(f.form);return;
  }
  const ingredients=await loading(main,()=>list('/api/ingredientes'));if(!ingredients)return;
  const selected=new Map(), f=formBase('Salvar produto','produtos');
  const selector=field('ingrediente','Ingrediente',{options:[['','Selecione um ingrediente'],...ingredients.map(x=>[x.id,x.nome])]}), composition=node('div'), compError=node('span',{id:'composicao-error',class:'field-error',role:'alert'});
  const draw=()=>{
    composition.replaceChildren(...[...selected.values()].map(({item,count})=>{
      const input=node('input',{type:'number',min:1,step:1,value:count,'aria-label':'Porções de '+item.nome});
      const total=node('small',{},`${amount(quantity(item.porcao_padrao,count))} ${unit(item.unidade_medida)}`);
      input.addEventListener('input',()=>{selected.get(item.id).count=Number(input.value);total.textContent=`${amount(quantity(item.porcao_padrao,Number(input.value)))} ${unit(item.unidade_medida)}`;});
      const change = delta => {input.value=String(Math.max(1,Number(input.value||1)+delta));input.dispatchEvent(new Event('input'));};
      const decrease=button('−',()=>change(-1),'link-button'), increase=button('+',()=>change(1),'link-button');
      decrease.setAttribute('aria-label','Diminuir porções de '+item.nome);increase.setAttribute('aria-label','Aumentar porções de '+item.nome);
      const remove=button('×',async()=>{if(await confirmRemoval(item.nome)){selected.delete(item.id);draw();}},'link-button remove');
      remove.setAttribute('aria-label','Remover '+item.nome);
      return node('div',{class:'composition-item'},node('div',{},item.nome,total),node('div',{class:'stepper'},decrease,input,increase),remove);
    }));
    for(const option of selector.querySelector('select').options)option.disabled=selected.has(Number(option.value));
  };
  const add=button('+ Adicionar ingrediente',()=>{
    const item=ingredients.find(x=>String(x.id)===selector.querySelector('select').value);
    if(!item){compError.textContent='Selecione um ingrediente.';return;}
    if(selected.has(item.id)){compError.textContent='Este ingrediente já foi adicionado ao produto.';return;}
    if(quantity(item.porcao_padrao,1)===null){compError.textContent='A API precisa informar uma porção válida para este ingrediente.';return;}
    selected.set(item.id,{item,count:1});compError.textContent='';selector.querySelector('select').value='';draw();
  });
  f.form.append(node('div',{class:'product-grid'},node('section',{class:'card stack'},node('h2',{},'Dados do produto'),field('nome','Nome do produto *',{maxlength:100,placeholder:'Ex: Açaí 500ml',hint:'Inclua o tamanho no nome, se necessário.'}),field('preco_venda','Preço de venda (R$) *',{placeholder:'0,00',hint:'Deve ser maior que zero'}),node('p',{},'✓ Produto criado ativo')),node('section',{class:'card stack'},node('h2',{},'Composição (ingredientes)'),node('small',{},'O produto deve ter ao menos açaí vinculado. Informe a quantidade de porções.'),composition,compError,selector,add)),f.actions);
  bindForm(f,raw=>{
    const issues={},payload={nome:raw.nome.trim(),preco_venda:decimal(raw.preco_venda,2,true),composicao:[]};
    if(!payload.nome)issues.nome='Informe o nome do produto.';
    if(payload.preco_venda===null)issues.preco_venda='O preço deve ser maior que zero. Use até 2 casas decimais.';
    if(!selected.size)issues.composicao='Adicione ao menos um ingrediente.';
    else if(![...selected.values()].some(x=>isAcai(x.item.nome)))issues.composicao='O produto precisa ter o açaí entre os ingredientes.';
    for(const {item,count} of selected.values()) {const value=quantity(item.porcao_padrao,count);if(value===null)issues.composicao='Informe porções inteiras maiores que zero, dentro do limite de quantidade.';payload.composicao.push({ingrediente_id:item.id,quantidade_utilizada:value});}
    return {payload,issues};
  },async payload=>{await request('/api/produtos',{method:'POST',body:payload});state.flash='Produto cadastrado com sucesso.';go('produtos');});main.append(f.form);
}
