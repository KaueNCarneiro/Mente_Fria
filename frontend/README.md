# Frontend — Mente Fria · Sprint 1

HTML, CSS e JavaScript (ES modules), sem build e sem dependências de produção. Execute por HTTP; abrir `index.html` com duplo clique não carrega os módulos corretamente.

## Executar

No VS Code, abra a pasta `frontend` e execute **Open with Live Server** em `index.html`, na porta **5500**. Alternativa, no terminal dentro desta pasta:

```powershell
py -m http.server 5500 --bind 127.0.0.1
```

Abra http://127.0.0.1:5500/index.html. Mantenha o terminal/Live Server ligado enquanto estiver usando o sistema. `ERR_CONNECTION_REFUSED` ao abrir essa página indica que o servidor do frontend não está atendendo nesse endereço.

A API é configurada exclusivamente em `js/config.js`: `API_BASE_URL = 'http://localhost:8080'`, **sem `/api` no final**. A execução da API e do banco deve seguir os documentos da equipe. O frontend não inicia nem modifica o backend. Para hospedagem HTTPS, use a base HTTPS fornecida pela equipe e configure a origem no backend.

## Escopo e decisões

- Cadastro do primeiro administrador e login com JWT; sessão no `sessionStorage` da aba, sem guardar senhas. Registro cria conta e depois faz login. Falha nessa segunda etapa orienta o usuário a entrar, sem repetir o cadastro.
- Administrador: listar/cadastrar funcionários; listar/cadastrar/editar ingredientes; listar/cadastrar produtos com composição; alterar preço.
- Funcionário: início, consulta de ingredientes e produtos. Rotas e ações administrativas bloqueadas na interface. O backend deve validar o perfil em cada requisição; a interface não constitui autorização de segurança.
- Erros gerais e `campos` exibidos junto aos campos; validações locais, prevenção de envio duplo, carregamento, timeout, listas vazias e nova tentativa.
- Unidades vêm de `GET /api/unidades-medida`. O padrão pré-preenche a porção, que permanece editável (D-B). Ao editar, preserva a porção do ingrediente até o usuário trocar a unidade.
- Composição sem repetição: número inteiro de porções × `porcao_padrao`, enviado como `quantidade_utilizada` em até três casas. Exige ingrediente cujo nome normalizado contém `acai` (CT15). Conversão no frontend conforme B1 do contrato e Arquitetura; há uma referência antiga a Service no documento de SQL.
- A tela de teste em `index.html#/comunicacao` atende ao roteiro de infraestrutura: saúde, listagem de ingredientes e endpoint temporário Python. A listagem envia token quando houver sessão.

O Figma fornece logo, cores, fonte Inter, menus, formulários e tabelas. Documentos prevalecem nos seguintes pontos: perfil chamado Administrador; porção acrescentada; ausência de atalhos que ignoram login; tamanho incorporável ao nome (não existe campo `tamanho` no contrato); status ativo definido pelo backend na criação; sem edição de funcionários nem edição completa de produtos, que são opcionais fora da Sprint 1. A edição de ingredientes usa a listagem completa, sem depender do GET individual opcional. Recursos das próximas sprints estão identificados no menu e indisponíveis.

## Situação do backend deste checkout

Na inspeção de 27/09/2026, o código Java contém `GET /api/saude` e `GET /api/ingredientes`; este último retorna apenas `id`, `nome`, `unidade_medida`. Login, registro, usuários, unidades, produtos e escrita de ingredientes ainda não estão implementados nesse checkout. A integração de cadastros está preparada conforme o contrato, mas depende dessas entregas. A edição de ingredientes avisa quando os campos completos não chegam, sem inventar valores.

Falha de `fetch` não prova CORS: pode ser API desligada, endereço incorreto, CORS ou rede. Confira o painel Network do navegador. Portas 5500 e 8080 diferentes são esperadas: o backend precisa permitir `http://127.0.0.1:5500` e `http://localhost:5500`, métodos e headers do contrato. Não use `no-cors`: ele torna a resposta ilegível e não resolve autenticação.

## Verificação

Testes de navegador com respostas simuladas cobrem os fluxos, contratos enviados, autorização da interface, validações e falhas. Não substituem os testes com API/PostgreSQL reais. Dependência apenas de desenvolvimento:

```powershell
npm install
npm test
```

Mantenha o servidor do frontend na porta 5500. Chrome instalado é o navegador padrão do teste; `PLAYWRIGHT_CHANNEL` pode selecionar outro canal instalado. `FRONTEND_URL` permite outro endereço. Screenshots de evidência são gravados em `tests/artifacts` (ignorados pelo Git).

Para aceite real: criar primeiro administrador, entrar com os dois perfis, cadastrar funcionário, cadastrar ingrediente com porção personalizada, cadastrar produto com açaí e conferir quantidades no backend, alterar preço, confirmar 401/403/409 e mensagens por campo. O backend deve também rejeitar requisições diretas sem autorização.

Estrutura: `js/api.js` centraliza HTTP/sessão; `js/validation.js` contém cálculos puros; `js/app.js` contém telas e rotas; `css/styles.css` contém estilos responsivos; `assets` contém arquivos locais exportados do Figma e a fonte Inter com licença.
