# Licença do Gurobi no sistema publicado — Sprint 3 (A30, A31, A34, A38)

**Data:** 27/10/2026, noite (revisado com o e-mail da Gurobi ao Kauê). **Atualizado em 28/10/2026** com a resposta escrita do suporte da Gurobi (A38). **Origem:** resposta do professor, item 2 (`resposta-professor.md`): "Não considerem o uso autorizado até esclarecerem essa questão" e "planejem uma alternativa para a demonstração".

**Resolvido em 28/10 (A38): o solver RODA no Render, com a licença restrita do `pip`. A WLS do Kauê NÃO vai para o Render.**

## 0. Resposta da Gurobi (28/10/2026)

O Kauê perguntou primeiro ao agente de IA do site da Gurobi (que recomendou a licença do `pip`, mas disse que era "uma questão de julgamento" e sugeriu abrir chamado) e depois abriu um **chamado no suporte**. Resposta escrita da equipe de suporte ao Kauê:

> "As you mentioned, copying the WLS license to a platform accessible through a public URL is not allowed. Since your application is a small academic demo with no commercial ties, you can use the size-limited pip license instead."

**Tradução:** copiar a licença WLS para uma plataforma acessível por URL pública não é permitido. Como a aplicação é uma pequena demonstração acadêmica sem vínculo comercial, vocês podem usar a licença limitada do `pip`.

**O que isso muda:**

| Onde | Como fica |
|---|---|
| Render | `OTIMIZADOR_HABILITADO=true` (já no `render.yaml` de referência) e **nenhuma** variável `GRB_*`. Sem elas, o `gurobipy` usa a licença do `pip`, que já vem instalada na imagem |
| WLS do Kauê | Só para uso pessoal dele (desenvolvimento e demonstração local, se quiser). **Nunca** no Render |
| Testes da equipe | Nada muda: já usavam a licença do `pip` |
| Plano B1 | Fica no código como **reserva**. Se o solver der problema no Render (memória, tempo esgotado), o Kauê troca a variável para `false` no painel e o sistema volta a responder `503` nas recomendações |
| Professor | O Kauê repassa o e-mail do suporte; o e-mail fica guardado com ele (sem dados pessoais no repositório) |

A partir daqui, as §1 a §5 ficam como **registro** do que foi decidido em 27/10, com notas onde a A38 mudou algo.

## 1. Que licenças a equipe tem

| Licença | Titular | Validade | Para que serve (texto da Gurobi) | Serve para o Render? |
|---|---|---|---|---|
| **WLS acadêmica** (Web License Service), aprovada pela Gurobi por pedido do Kauê | **Kauê** (só ele) | **Até 25/09/2027** (cobre 30/11/2026) | "Pesquisa acadêmica e/ou trabalhos de disciplina"; uso comercial proibido. **"Para uma única pessoa específica (não uma máquina ou processo que roda em nome de usuários diferentes)."** Exige acesso contínuo a `token.gurobi.com` | **Dúvida:** um serviço publicado, usado pelo professor e pela equipe, parece ser "um processo que roda em nome de usuários diferentes". **Precisa de confirmação da Gurobi** → **28/10: não** (A38) |
| Licença restrita que vem com o `pip` | — | — | "Pesquisa, desenvolvimento e avaliação"; não comercial; até 2.000 variáveis e 2.000 restrições. O artigo da Gurobi não define "uso em produção" | **Dúvida.** O professor pediu cautela: "destinada a uso não produtivo" → **28/10: sim, autorizada por escrito** (A38) |

Fontes: o e-mail de aprovação da Gurobi ao Kauê (guardado com ele; o número da licença e o e-mail dele **não** vão para o repositório); o artigo "What does 'Restricted license for non-production use only' mean"; as páginas "Pricing and licensing" e do programa acadêmico (lidas em 27/10).

**Correções em relação ao que foi registrado antes:**
- A **A22** (26/10) dizia que a WLS não estava disponível. **Ela existe**: é a do Kauê, válida até 25/09/2027 (A34).
- A Arquitetura (§3.3) fala em "licença do Lucas" e em validade de 90 dias. A licença confirmada é a **do Kauê**, de **um ano**.
- O "Compute Server" da Arquitetura **não é necessário**: a própria WLS roda dentro do container (a "Alternativa C" de 08/10). Usar o Compute Server exigiria um computador da equipe ligado e exposto na internet até 30/11, e o código não está preparado para isso.

## 2. O que fazer agora

| # | Ação | Quem | Quando |
|---|---|---|---|
| 1 | **[Feito em 28/10 por chamado no suporte; resposta na §0]** **Responder ao e-mail de aprovação da licença** (para `license@gurobi.com`), do e-mail institucional do Kauê, com o texto da §3. A Gurobi diz que, para licenças acadêmicas gratuitas, o suporte cobre justamente "perguntas de licenciamento". Guardar a resposta em `docs/Sprint_3/resposta-gurobi.md`, sem o número da licença | **Kauê** (titular) | **Ter 27, à noite** |
| 2 | Preparar o **plano B** (§4) | Lucas e Kauê | **Feito** (B1 implementado em `referencia/`) |
| 3 | ~~Sábado 31, à tarde: com autorização escrita, o deploy usa a WLS do Kauê; sem ela, o plano B1~~ **Decidido em 28/10 (A38):** o deploy de sábado vai **com o solver ligado e a licença do `pip`** | Lucas (PO) | Sáb 31 |
| 4 | Avisar o professor da resposta da Gurobi e de qual caminho foi seguido (o Kauê repassa o e-mail do suporte) | Kauê e Diogo | **Qua 28** |

**[Substituído pela A38: a WLS não pode ir para o Render; não crie nenhuma `GRB_*` lá.]** *Texto de 27/10:* **Se a Gurobi autorizar:** o **Kauê**, como titular, cadastra no painel do Render as variáveis `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID` (os valores estão no portal da Gurobi, na licença dele). O backend já as repassa ao Python (Contrato v2, §1.6). **Nunca** coloque esses valores no Git, no README ou em mensagens do grupo. As licenças acadêmicas limitam as execuções simultâneas; o backend já faz um cálculo por vez (A27).

## 3. Pergunta à Gurobi (resposta ao e-mail da licença; para copiar e colar)

**Assunto:** manter o assunto do e-mail de aprovação (responder na mesma conversa).

> Hi Gurobi Team,
>
> Thank you again for my academic WLS license (ID [license ID]). I have a licensing question before using it in my coursework project, and my professor asked me to get an express confirmation from you.
>
> My team (four undergraduate students at FATEC Ribeirão Preto, Brazil) is building a course project called "Mente Fria", a small inventory and production-planning system for an açaí shop. The açaí shop is fictional: no real business uses the system. The project is purely academic and non-commercial: nobody pays for it, and it is not sold or offered as a service.
>
> The system uses `gurobipy` 13.0.3 to solve two small integer programming models (a few variables and constraints each). For the course evaluation, the system must be published at a public URL until November 30, 2026: the backend runs in a Docker container on Render (free tier) and calls `gurobipy` when a user asks for a recommendation. The only users are the four students of the team and our professor, with demonstration data.
>
> Your e-mail says the license is "for a single specific person (not a machine or process that runs on behalf of different users)". Could you please confirm:
>
> 1. May I use my academic WLS license in this Render container for the course evaluation, until November 30, 2026?
> 2. If not, is this use allowed under the size-limited license included with `pip install gurobipy` ("for non-production use only")?
> 3. If neither applies, which license would you recommend for this case?
>
> Thank you very much.
>
> Kauê Nogueira Carneiro · [institutional e-mail]

**Tradução (para a equipe):** agradeço de novo a licença WLS acadêmica. Tenho uma pergunta de licenciamento antes de usá-la no projeto da disciplina, e o professor pediu uma confirmação expressa. Somos quatro alunos de graduação da FATEC Ribeirão Preto fazendo o "Mente Fria", um sistema de estoque e planejamento de produção para uma açaiteria. A açaiteria é fictícia: nenhum negócio real usa o sistema. O projeto é só acadêmico e sem fins comerciais. O sistema usa o `gurobipy` 13.0.3 em dois modelos pequenos de programação inteira. Para a avaliação, ele precisa ficar numa URL pública até 30/11/2026: o backend roda num container no Render (plano gratuito) e chama o `gurobipy` quando alguém pede uma recomendação. Os únicos usuários são os quatro alunos e o professor, com dados de demonstração. O e-mail de vocês diz que a licença é "para uma única pessoa específica (não uma máquina ou processo que roda em nome de usuários diferentes)". Perguntas: (1) posso usar a minha WLS acadêmica nesse container no Render para a avaliação, até 30/11/2026? (2) Se não, esse uso é permitido pela licença restrita que vem com o `pip`? (3) Se nenhuma serve, qual licença vocês recomendam?

**Conferir antes de enviar** *(já enviado; resposta na §0)*:
- **Confirmado em 27/10:** a faculdade é a **FATEC Ribeirão Preto**, e a açaiteria é **fictícia** (uso só acadêmico).
- **[preencher]** O número da licença (está no e-mail de aprovação) e o e-mail institucional do Kauê, na assinatura.

## 4. Plano B para a demonstração — **decidido em 27/10: B1**; **reserva desde 28/10** (A38)

| Opção | Como funciona | O que muda | Prós | Contras |
|---|---|---|---|---|
| **B1 — Publicado sem solver; otimização demonstrada no computador do Kauê** (recomendada) | No Render, uma variável `OTIMIZADOR_HABILITADO=false` faz as rotas de produção e de reposição responderem `503` com "A recomendação está disponível só na demonstração local da equipe." As Telas 12, 13 e 14 mostram essa mensagem. Na avaliação, US10, US11, US14 e US18 são demonstradas **pelo Kauê, no computador dele, com a WLS acadêmica dele**: uma pessoa, trabalho de disciplina, exatamente o uso que o e-mail da Gurobi descreve | Uma variável, uma verificação em 2 serviços, 2 ou 3 testes; uma mensagem nas 3 telas | Pouco trabalho; nada roda no servidor sem autorização; a demonstração local fica **dentro** dos termos da licença; o resto do sistema continua publicado | O dashboard publicado mostra "Gere a recomendação" no bloco do plano; a demonstração da otimização depende do Kauê e do computador dele |
| **B2 — Solver aberto no servidor** | No Render, os mesmos modelos rodam com um solver de código aberto (HiGHS, que vem no SciPy), com o mesmo contrato JSON; localmente, continua o Gurobi | Novos módulos Python (os oráculos de força bruta já existem para conferir); `requirements` e `Dockerfile` | Sistema completo na URL | O Termo cita "Python + Gurobi": **precisa da aprovação do professor**; mais trabalho e risco a 4 dias do fim |

**Atualização de 28/10:** a Gurobi autorizou a licença do `pip` no Render; a B1 virou reserva e a B2 não é mais necessária. *Texto de 27/10:* **Recomendação:** preparar a **B1** (feito em 27/10). Se a Gurobi autorizar a WLS no Render antes disso, a B1 fica desligada e o Kauê cadastra a licença no Render (§2). A B2 só vale a pena se o professor exigir a otimização **na URL publicada** e a Gurobi negar.

## 5. Como a B1 funciona (implementada em 27/10; desde 28/10, só se for preciso voltar a ela)

**No Render, se a B1 for ligada** (o Kauê troca `OTIMIZADOR_HABILITADO` para `false` no painel; o `render.yaml` cria `true` desde 28/10). Com isso:
- `POST /api/producao/recomendacao` e `POST /api/reposicao/recomendacao` respondem `503` com "A recomendação está disponível só na demonstração local da equipe.", **sem chamar o Python** (a reposição ainda valida os campos antes e, sem pedidos no período, responde normalmente);
- o marketing responde que as sugestões dependem da recomendação, disponível só na demonstração local;
- o dashboard traz `otimizador_habilitado: false`, e a Tela 03 explica que o plano só existe na demonstração local;
- **todo o resto** (cadastros, estoque, pedidos, dashboard do mês) funciona na URL publicada.

**Na demonstração local (Kauê, no computador dele, com a WLS dele):**
1. Na pasta do projeto, com o banco local e o seed de demonstração, defina as variáveis da licença **só na sessão do PowerShell**, com os valores do portal da Gurobi:
   ```powershell
   $env:GRB_WLSACCESSID = "<do portal>"; $env:GRB_WLSSECRET = "<do portal>"; $env:GRB_LICENSEID = "<número da licença>"
   ```
2. Suba o backend normalmente (`OTIMIZADOR_HABILITADO` não precisa ser definida: o padrão é ligado) e abra o frontend local.
3. Mostre a Tela 12 (151/1/0 com o seed), a Tela 13 e a Tela 14.
4. Feche o PowerShell no fim: as variáveis somem com ele. **Nunca** grave os valores em arquivo do projeto.

**Voltar da B1 para o solver:** trocar `OTIMIZADOR_HABILITADO` para `true` no painel do serviço, **sem** criar `GRB_*`. O Render publica de novo sozinho. *(Texto de 27/10, substituído pela A38: "o Kauê cria as três `GRB_*` no painel".)*

**O que foi verificado (27/10):** `OtimizadorDesligadoTest` (3 testes: produção `503` sem chamar o Python e `403` para o Funcionário; reposição `400` → `200` sem pedidos → `503` com pedidos; marketing e dashboard com as mensagens) e a suíte completa com **95 testes, 0 falhas**, com o Python real. Ao remover de propósito a chave da produção, o teste falhou ("esperado 503, veio 500"). O tutorial do Lucas, aplicado fase por fase, passou em todas as fases. As funções de apoio das telas: **15/15**. O `render.yaml` foi lido sem erros. **Não testado:** no Render real e nas telas reais (ainda não existem). **Também verificado antes (F8):** a imagem de deploy roda o solver com a licença do `pip` (151/1/0 em 3,1 s com 0,1 CPU). No Render real, quem confere é o CT68 (e o item 4 do tutorial do Kauê, §12.5).
