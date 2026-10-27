import {request, session} from '../api.js';
import {root, node, link, button, notice} from '../ui.js';
export function communication() {
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
