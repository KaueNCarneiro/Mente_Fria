import {request, list, session, saveSession, logout} from './api.js';
import {decimal, quantity, isAcai} from './validation.js';
const root = document.querySelector('#app');
const money = value => value == null ? '—' : Number(value).toLocaleString('pt-BR', {style:'currency',currency:'BRL'});
const amount = value => value == null ? '—' : Number(value).toLocaleString('pt-BR', {maximumFractionDigits:3});
const unit = value => value === 'l' ? 'L' : value;
let revision = 0, flash = '';
const node = (tag, props = {}, ...children) => {
  const el = document.createElement(tag);
  for (const [key,value] of Object.entries(props)) {
    if (key.startsWith('on')) el.addEventListener(key.slice(2).toLowerCase(), value);
    else if (key === 'class') el.className = value;
    else if (value !== undefined && value !== null && value !== false) el.setAttribute(key, value === true ? '' : value);
  }
  for (const child of children.flat(Infinity)) if (child !== null && child !== undefined) el.append(child instanceof Node ? child : document.createTextNode(String(child)));
  return el;
};
const link = (text, path, cls = '') => node('a', {href:'#/'+path,class:cls}, text);
const button = (text, action, cls = '') => node('button', {type:'button',class:cls,onclick:action}, text);
const logo = () => node('div',{class:'logo'},node('img',{src:'assets/logo.png',alt:'Logo Mente Fria'}));
const notice = (text = '', kind = '') => node('div',{class:`notice ${kind}`,role:kind === 'error' ? 'alert' : 'status'},text);
function go(path) { if (location.hash === '#/'+path) render(); else location.hash = '/'+path; }
const admin = () => session()?.perfil === 'ADMINISTRADOR';
function field(name, label, {type='text',value='',hint='',options,maxlength,placeholder,step,min} = {}) {
  const input = options ? node('select',{name,id:name},options.map(([v,t])=>node('option',{value:v},t))) : node('input',{name,id:name,type,maxlength,placeholder,step,min,autocomplete:type === 'password' ? 'new-password' : name === 'email' ? 'email' : name === 'nome' ? 'name' : 'off'});
  input.value = value;
  input.setAttribute('aria-describedby', `${name}-error${hint ? ' '+name+'-hint' : ''}`);
  return node('div',{class:'field'},node('label',{for:name},label),input,hint ? node('small',{id:name+'-hint'},hint) : null,node('span',{id:name+'-error',class:'field-error'}));
}
function errors(form, values = {}) {
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
function formBase(label, back) {
  const form = node('form',{novalidate:true,class:'stack'});
  const message = notice();
  const submit = node('button',{type:'submit',class:'primary'},label);
  form.append(message);
  return {form,message,submit,actions:node('div',{class:'form-actions'},submit,back ? link('Cancelar',back,'button') : null)};
}
function bindForm({form,message,submit}, validate, save) {
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
function accountValidation(raw, register) {
  const issues={},payload={email:raw.email.trim().toLowerCase(),senha:raw.senha};
  if (!payload.email) issues.email='Informe o e-mail.';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email)) issues.email='Informe um e-mail válido.';
  if (!raw.senha) issues.senha='Informe a senha.';
  else if (register && raw.senha.length<8) issues.senha='A senha deve ter no mínimo 8 caracteres.';
  if (register) {payload.nome=raw.nome.trim();if (!payload.nome) issues.nome='Informe o nome.';}
  if ('confirmar' in raw && raw.confirmar!==raw.senha) issues.confirmar='As senhas devem ser iguais.';
  return {payload,issues};
}
function authPage(register) {
  const main=node('main',{id:'main',class:'auth'+(register?'':' login')});
  const card=node('section',{class:'card auth-card stack'},logo(),node('h1',{},register?'Criar conta de Administrador':'Mente Fria'),node('p',{class:'muted'},register?'Você será o proprietário com acesso completo ao sistema':'Entrar no sistema de gestão'));
  const f=formBase(register?'Criar conta e entrar':'Entrar');
  if (flash) {f.message.textContent=flash;flash='';}
  if (register) f.form.append(field('nome','Nome completo',{maxlength:150,placeholder:'Nome do proprietário'}));
  f.form.append(field('email','E-mail',{type:'email',maxlength:150,placeholder:'seuemail@exemplo.com'}),field('senha','Senha',{type:'password',hint:register?'Mínimo de 8 caracteres':''}));
  if (!register) f.form.elements.senha.autocomplete='current-password';
  if (register) f.form.append(field('confirmar','Confirmar senha',{type:'password'}));
  f.form.append(f.submit);
  let created=false;
  bindForm(f,raw=>accountValidation(raw,register),async payload=>{
    if (register && !created) {await request('/api/auth/registro',{method:'POST',body:payload,public:true});created=true;}
    try {saveSession(await request('/api/auth/login',{method:'POST',body:{email:payload.email,senha:payload.senha},public:true}));}
    catch(error) {if(created){flash='Conta criada. Entre com seu e-mail e senha. '+error.message;go('login');return;}throw error;}
    go(admin()?'funcionarios':'inicio');
  });
  card.append(f.form,register?link('Já tenho conta — Entrar','login'):link('Ainda não tem conta? Criar conta de proprietário','cadastro'));
  if (!register) card.append(node('small',{},link('Verificar comunicação com o servidor','comunicacao')));
  main.append(card);root.append(main);
}
function shell(path) {
  const current=session();
  const header=node('header',{class:'topbar'},node('div',{class:'brand'},logo(),node('strong',{},'Mente Fria'),node('small',{},'· Gestão de Açaiteria')),node('div',{class:'row'},node('span',{class:'badge'},'Perfil: '+(admin()?'Administrador':'Funcionário')),button('Sair',()=>{logout();go('login');},'link-button')));
  const sidebar=node('nav',{class:'sidebar','aria-label':'Menu principal'});
  const entries=admin() ? [['Dashboard',null,3],['Funcionários','funcionarios'],['Ingredientes','ingredientes'],['Porção Padrão',null,3],['Produtos','produtos'],['Estoque',null,2],['Produção',null,2],['Pedidos',null,2],['Clientes',null,2],['Reposição',null,2],['Marketing',null,3]] : [['Início','inicio'],['Ingredientes','ingredientes'],['Produtos','produtos'],['Estoque',null,2],['Pedidos',null,2]];
  for(const [label,route,sprint] of entries){
    const selected=path.split('/')[0]===route;
    const dot=node('img',{src:`assets/nav-dot${selected?'-active':''}.svg`,alt:''});
    sidebar.append(route?node('a',{href:'#/'+route,class:selected?'selected':'','aria-current':selected?'page':null},dot,label):node('span',{class:'future',title:'Disponível na Sprint '+sprint},dot,label,node('small',{},'S'+sprint)));
  }
  sidebar.append(node('p',{class:'footnote muted'},current.nome));
  const main=node('main',{id:'main',class:'content'});
  root.append(header,node('div',{class:'layout'},sidebar,main));return main;
}
function heading(main,title,subtitle,action,back) {
  document.title=title+' · Mente Fria';
  main.append(node('div',{class:'heading'},node('div',{},node('h1',{},title),node('p',{},subtitle),back?link('← Voltar para lista',back,'back'):null),action));
  if(flash){main.append(notice(flash,'success'));flash='';}
}
async function loading(main,task) {
  const stamp=revision, status=notice('Carregando…'); main.append(status);
  try {const data=await task();if(stamp!==revision)return null;status.remove();return data;}
  catch(error){if(stamp===revision){status.className='notice error';status.setAttribute('role','alert');status.replaceChildren(node('p',{},error.message),button('Tentar novamente',()=>render(),'link-button'));}return null;}
}
function table(headers,rows) {
  return node('div',{class:'table-wrap'},node('table',{},node('thead',{},node('tr',{},headers.map(h=>node('th',{scope:'col'},h)))),node('tbody',{},rows.length?rows.map(row=>node('tr',{},row.map(cell=>node('td',{},cell)))):node('tr',{},node('td',{colspan:headers.length,class:'empty'},'Nenhum registro cadastrado.')))));
}
const badge = active => node('span',{class:'badge '+(active===true?'active':'inactive')},active===true?'Ativo':active===false?'Inativo':'Não informado');
async function employees(main,isNew) {
  heading(main,isNew?'Novo Funcionário':'Funcionários',isNew?'Cadastre um colaborador com acesso limitado ao sistema':'Gerencie o acesso da equipe ao sistema',isNew?null:link('+ Novo funcionário','funcionarios/novo','button primary'),isNew?'funcionarios':null);
  if(!isNew){const data=await loading(main,()=>list('/api/usuarios'));if(data)main.append(table(['Nome','E-mail','Status'],data.map(x=>[x.nome,x.email,badge(x.ativo)])));return;}
  const f=formBase('Salvar funcionário','funcionarios');f.form.classList.add('card','form-card');
  f.form.append(field('nome','Nome completo *',{maxlength:150,placeholder:'Ex: Maria Souza'}),field('email','E-mail *',{type:'email',maxlength:150,placeholder:'Ex: maria@mentefria.com'}),field('senha','Senha *',{type:'password',hint:'Mínimo de 8 caracteres. O e-mail deve ser único no sistema.'}),node('div',{class:'subtle'},node('p',{},'Perfil de acesso: Funcionário'),node('small',{},'Não acessa cadastro de preços nem configurações administrativas')),node('p',{},'✓ Conta criada ativa'),f.actions);
  bindForm(f,raw=>accountValidation(raw,true),async payload=>{await request('/api/usuarios',{method:'POST',body:payload});flash='Funcionário cadastrado com sucesso.';go('funcionarios');});main.append(f.form);
}
async function ingredients(main,action,id) {
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
  },async payload=>{await request('/api/ingredientes'+(edit?'/'+id:''),{method:edit?'PUT':'POST',body:payload});flash='Ingrediente salvo com sucesso.';go('ingredientes');});
  if(!units.length){f.message.textContent='Nenhuma unidade cadastrada no servidor. Solicite à equipe a carga inicial das unidades.';f.submit.disabled=true;}main.append(f.form);
}
function confirmRemoval(name) {
  return new Promise(resolve=>{
    const dialog=node('dialog',{'aria-labelledby':'confirm-title'},node('div',{class:'stack'},node('h2',{id:'confirm-title'},'Remover ingrediente?'),node('p',{},`Remover ${name} da composição deste produto?`),node('div',{class:'row'},button('Cancelar',()=>dialog.close('cancel')),button('Remover',()=>dialog.close('remove'),'primary'))));
    dialog.addEventListener('close',()=>{const yes=dialog.returnValue==='remove';dialog.remove();resolve(yes);},{once:true});document.body.append(dialog);dialog.showModal();
  });
}
async function products(main,action,id) {
  const isNew=action==='novo',price=action==='preco';
  heading(main,isNew?'Novo Produto':price?'Alterar preço':'Produtos',isNew?'Cadastre um copo de açaí ou combo e sua composição de ingredientes':price?'Atualize o preço de venda do produto':'Copos de açaí e combos vendidos, com composição e preço',!isNew&&!price&&admin()?link('+ Novo produto','produtos/novo','button primary'):null,isNew||price?'produtos':null);
  if(!isNew&&!price){const data=await loading(main,()=>list('/api/produtos'));if(data)main.append(table(['Produto','Preço de venda','Status',...(admin()?['Ação']:[])],data.map(x=>[node('details',{},node('summary',{},x.nome),Array.isArray(x.composicao)&&x.composicao.length?node('ul',{},x.composicao.map(c=>node('li',{},`${c.ingrediente_nome||c.nome||'Ingrediente #'+c.ingrediente_id}: ${amount(c.quantidade_utilizada)} ${unit(c.unidade_medida)||''}`))):node('p',{class:'muted'},'Composição não informada pela API.')),money(x.preco_venda),badge(x.ativo),...(admin()?[link('Alterar preço','produtos/preco/'+x.id)]:[])])));return;}
  if(price){
    const data=await loading(main,()=>list('/api/produtos'));if(!data)return;const item=data.find(x=>String(x.id)===id);if(!item){main.append(notice('Produto não encontrado.','error'));return;}
    const f=formBase('Salvar preço','produtos');f.form.classList.add('card','form-card');f.form.append(node('h2',{},item.nome),field('preco_venda','Preço de venda (R$) *',{value:item.preco_venda}),f.actions);
    bindForm(f,raw=>{const value=decimal(raw.preco_venda,2,true);return {payload:{preco_venda:value},issues:value===null?{preco_venda:'O preço deve ser maior que zero. Use até 2 casas decimais.'}:{}};},async payload=>{await request('/api/produtos/'+id+'/preco',{method:'PUT',body:payload});flash='Preço atualizado com sucesso.';go('produtos');});main.append(f.form);return;
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
  },async payload=>{await request('/api/produtos',{method:'POST',body:payload});flash='Produto cadastrado com sucesso.';go('produtos');});main.append(f.form);
}
function communication() {
  const main=node('main',{id:'main',class:'communication stack'},node('h1',{},'Teste de comunicação'),node('p',{class:'muted'},'Verifique as conexões previstas no roteiro da Sprint 1.'),link(session()?'← Voltar ao sistema':'← Voltar para entrar',session()?'inicio':'login'));
  for(const [label,path,isPublic] of [['Java e PostgreSQL','/api/saude',true],['Ingredientes do banco','/api/ingredientes',false],['Java → Python → Java','/api/teste/python',true]]){
    const status=notice(),output=node('pre',{hidden:true});
    const run=button('Testar conexão',async()=>{
      run.disabled=true;status.className='notice';status.textContent='Testando…';output.hidden=true;
      try {
        const value=await request(path,{public:isPublic});
        const valid=path==='/api/saude' ? value?.status==='ok'&&value?.banco==='ok' : path==='/api/ingredientes' ? Array.isArray(value) : value?.status==='sucesso'&&typeof value.echo==='string';
        if(!valid)throw new Error('O servidor respondeu, mas o resultado não corresponde ao sucesso previsto no contrato.');
        status.className='notice success';status.textContent='Comunicação validada.';output.textContent=JSON.stringify(value,null,2);output.hidden=false;
      }
      catch(error){status.className='notice error';status.textContent=error.message;}finally{run.disabled=false;}
    },'primary');main.append(node('section',{class:'card stack'},node('h2',{},label),node('small',{},path),label.startsWith('Ingredientes')?node('small',{},'A listagem exige login quando a autenticação do contrato estiver habilitada.'):label.startsWith('Java →')?node('small',{},'Endpoint temporário; pode estar ausente após a validação da infraestrutura.'):null,run,status,output));
  }root.append(main);
}
async function render() {
  revision++;root.replaceChildren();document.title='Mente Fria';
  const path=location.hash.replace(/^#\/?/,'')||'login', [section,action,id]=path.split('/');
  if(section==='comunicacao'){communication();return;}
  if(!session()){if(!['login','cadastro'].includes(section)){go('login');return;}authPage(section==='cadastro');return;}
  if(['login','cadastro'].includes(section)){go(admin()?'funcionarios':'inicio');return;}
  if(!admin()&&(section==='funcionarios'||action)){flash='Esta área é exclusiva do Administrador.';go('inicio');return;}
  const main=shell(path);
  if(section==='funcionarios'&&(!action||action==='novo'))await employees(main,action==='novo');
  else if(section==='ingredientes'&&(!action||action==='novo'||action==='editar'&&/^\d+$/.test(id)))await ingredients(main,action,id);
  else if(section==='produtos'&&(!action||action==='novo'||action==='preco'&&/^\d+$/.test(id)))await products(main,action,id);
  else if(section==='inicio'){
    heading(main,'Bem-vindo ao Mente Fria','Consulte os ingredientes e produtos disponíveis.');
    const cards = ['Ingredientes','Produtos'].map(title => node('a', {href:'#/'+title.toLowerCase(),class:'card'}, node('h2',{},title), node('p',{class:'muted'},'Consultar '+title.toLowerCase())));
    main.append(node('div',{class:'home-cards'},cards));
  }else{heading(main,'Página não encontrada','Escolha uma opção no menu para continuar.');}
}
window.addEventListener('hashchange',()=>{render().catch(showUnexpected);});
window.addEventListener('session-expired',()=>{flash='Sua sessão expirou. Entre novamente.';go('login');});
function showUnexpected(){root.replaceChildren(notice('Não foi possível abrir esta tela. Recarregue a página e tente novamente.','error'),link('Voltar','login'));}
render().catch(showUnexpected);
