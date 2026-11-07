import {session, logout} from '../api.js';
import {root, node, link, button, logo, badge} from '../ui.js';
import {go, admin} from '../nav.js';
export function shell(path) {
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
