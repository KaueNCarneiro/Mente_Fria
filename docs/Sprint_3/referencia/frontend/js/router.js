import {session} from './api.js';
import {state} from './state.js';
import {root, node, link, notice, heading} from './ui.js';
import {go, admin} from './nav.js';
import {authPage} from './screens/auth.js';
import {shell} from './screens/shell.js';
import {employees} from './screens/funcionarios.js';
import {ingredients} from './screens/ingredientes.js';
import {products} from './screens/produtos.js';
import {communication} from './screens/comunicacao.js';
async function render() {
  state.revision++;root.replaceChildren();document.title='Mente Fria';
  const path=location.hash.replace(/^#\/?/,'')||'login', [section,action,id]=path.split('/');
  if(section==='comunicacao'){communication();return;}
  if(!session()){if(!['login','cadastro'].includes(section)){go('login');return;}authPage(section==='cadastro');return;}
  if(['login','cadastro'].includes(section)){go(admin()?'funcionarios':'inicio');return;}
  if(!admin()&&(section==='funcionarios'||action)){state.flash='Esta área é exclusiva do Administrador.';go('inicio');return;}
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
window.addEventListener('rerender',()=>{render().catch(showUnexpected);});
window.addEventListener('hashchange',()=>{render().catch(showUnexpected);});
window.addEventListener('session-expired',()=>{state.flash='Sua sessão expirou. Entre novamente.';go('login');});
function showUnexpected(){root.replaceChildren(notice('Não foi possível abrir esta tela. Recarregue a página e tente novamente.','error'),link('Voltar','login'));}
render().catch(showUnexpected);
