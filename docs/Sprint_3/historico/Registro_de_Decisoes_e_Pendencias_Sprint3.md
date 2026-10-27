# Registro de decisões e pendências — Sprint 3 (Mente Fria)

**Criado em:** 14/10/2026 · **Base:** `sprint-3-roteiro.md` v4.6 + `Analise_Roteiro_Sprint3_v4.6.md` + auditoria da `main` (commit `1ddb5ae`)
**Para que serve:** guardar, em um só lugar, **o que a equipe decidiu em 14/10 e por quê**, e **o que ainda está aberto**, para que ninguém siga um passo dos tutoriais sem saber a razão dele. Sugestão: copiar para `docs/Sprint_3/decisoes-e-pendencias.md` quando a pasta for criada.

---

## 1. Decisões confirmadas em 14/10/2026

Cada linha tem o **porquê** (para quem for executar entender o passo) e **onde** o tutorial aplica a decisão.

| # | Decisão | Por quê (em linguagem direta) | Quem executa | Onde está |
|---|---|---|---|---|
| **F1** | O banco usa `CHECK`s **estritos** para `quantidade_restante` e `motivo`: `IS NOT NULL` explícito e `<>` no lugar do domínio do tipo | Em SQL, `NULL IN ('A','B')` não dá "falso": dá "desconhecido", e o `CHECK` **deixa passar** quando o resultado é desconhecido. Resultado: uma saída **sem motivo** entraria no banco. Também, um `CHECK` que reprova o tipo inválido junto com o resto **esconde** a mensagem certa (`chk_movimentacao_tipo`). Isso foi **testado**: 3 testes reprovaram até corrigir | Diogo | Tutorial do Diogo, Parte 3 |
| **F2** | O **seed oficial nasce com estoque 0**; o estoque de demonstração vive só no `seed_demo.sql` (decisão **D11**) | A regra do banco passou a ser "`quantidade_estoque` = soma dos `quantidade_restante` das entradas". O seed antigo tem 40 kg de açaí e **nenhuma** linha de entrada, então violaria a regra desde o primeiro dia. Linhas de entrada exigem `usuario_id`, e o seed oficial não tem usuários | Diogo | Tutorial do Diogo, Parte 2 |
| **F3** | O Java calcula **`hoje`** em `America/Sao_Paulo` e o **envia como parâmetro** aos SQLs; nunca `CURRENT_DATE` do banco | O servidor (Render) roda em UTC; o Brasil está 3 horas atrás. Das 21h à meia-noite, uma linha que vence "hoje" já seria tratada como vencida, **3 horas antes da hora** | Lucas | Tutorial do Lucas, Fase 1 |
| **F4** | O endpoint temporário `/api/teste/python` usa **`@Profile("dev")`** | Com a segurança ligada, ele passaria a exigir token, e a tela de comunicação o chama sem token. Liberá-lo "no geral" abriria uma porta sem controle de acesso em produção. Com o perfil, ele **nem existe** em produção | Lucas | Tutorial do Lucas, Fase 4A |
| **F5** | JWT com **`jjwt` 0.13.x**, conferindo a árvore de dependências; plano B `jjwt-gson`. Testcontainers com as coordenadas do Boot 4 | A decisão do JWT (25/09) é anterior à troca para o Spring Boot 4.0.8 (27/09), que usa o Jackson 3. O `jjwt-jackson` traz o Jackson 2 junto. Funciona, mas precisa ser **conferido**, não suposto | Lucas | Tutorial do Lucas, Fase 1 |
| **F6** | O Gurobi roda com **`MIPGap = 0`** e `MIPGapAbs = 0`, mais a **verificação em inteiros** | O padrão do Gurobi aceita um plano até 0,01% pior que o ótimo. Para "o plano é o melhor possível" ser verdade, o solver precisa ser exato e o código precisa **conferir** o resultado | Kauê | Tutorial do Kauê, Parte 5 |
| **F7** | **Assinaturas** dos repositórios Java combinadas entre Diogo e Lucas **antes** de codar; Diogo cria os `*Repository`, Lucas os controllers e serviços | Dois integrantes mexendo no mesmo pacote em 2 dias é exatamente o que gerou os conflitos da Sprint 1 (repositório excluído). Combinar antes elimina o conflito | Diogo e Lucas | Tutoriais de ambos (mesma tabela de assinaturas) |
| **F8** | Os tutoriais trazem **números esperados** para o seed de demonstração (plano 151/1/0, lucro 1.011,71) | Com o seed oficial em estoque 0, a recomendação real dá "tudo zero". Sem um dado de comparação, ninguém sabe se a integração está certa | Kauê e Lucas | Tutoriais de ambos |
| **F9** | Trocar **receita por lucro** altera o Termo de Aceite (aprovado pelo professor em 28/08): **registrar e avisar o professor antes** de editar | O Termo é um acordo com o professor. Mudar o que o sistema maximiza e o escopo da reposição sem avisar quebra o combinado | Diogo (documentos) e PO | Tutorial do Diogo, Parte 8 |
| **F10** | Backlog **US9**: "saída de produtos" passa a "baixa de **ingredientes**" | O banco registra movimentação **de ingrediente** (`movimentacao_estoque.ingrediente_id`). O texto da história descrevia outra coisa | Diogo | Tutorial do Diogo, Parte 8 |
| **F11** | O campo **`consumo`** do contrato Java ↔ Python lista **só** os ingredientes com consumo maior que zero | O exemplo do roteiro mostra assim. Menos dados na resposta e nada a interpretar nos zeros | Kauê | Tutorial do Kauê, Parte 5 |

**Desvios do roteiro v4.6, com motivo (aceitos junto com as F1 a F11):**

| Desvio | Motivo |
|---|---|
| `seed_demo.sql` **não cria usuários** (usa o Administrador e o Funcionário criados pela tela) | Assim não há hash de senha no repositório e o `POST /api/auth/registro` não fica bloqueado (o cadastro do Administrador só vale enquanto não existe nenhum) |
| Validação **manual e explícita** no backend, em vez de Bean Validation | A API devolve **vários erros de campo de uma vez**, com mensagens fixas do `mapa_constraints.md`; a validação manual dá resultado determinístico. Pode trocar depois |

---

## 2. Pendências abertas

| # | Pendência | Dono | Quando decidir | Observação |
|---|---|---|---|---|
| **F12** | **Definir "consumo"** de forma única. O dashboard ("ingredientes mais utilizados") e a reposição (consumo de um período) usam a mesma palavra, mas o roteiro conta **pedidos cancelados** na reposição e **não conta** no dashboard | PO (Lucas), com Kauê e Diogo | **Antes de iniciar a Fase 6** (Sprint 4). **Adiada** pela equipe em 14/10 | Risco: dois números diferentes para a mesma pergunta ("quanto usei de açaí?"). Opções a avaliar na decisão: (a) consumo físico = tudo que saiu do estoque por pedido, cancelado ou não, **com** as devoluções do cancelamento abatidas; (b) consumo comercial = só pedidos ativos. Se escolherem (a) ou (b), os **dois** lugares devem usar a mesma |
| P2 | **Versão do `gurobipy`:** o `requirements.txt` (UTF-16) fixa 13.0.3; a Arquitetura decidiu **13.0.1** | Kauê e Diogo | Esta semana (Fase P) | Os tutoriais foram verificados com **13.0.1**. Se mantiverem 13.0.3, rodar a suíte de novo. Em qualquer caso, **salvar o arquivo em UTF-8** |
| P3 | **Avisar o professor** da mudança de receita para lucro e da reposição com orçamento (F9) | Diogo e PO | **Antes** de editar o Termo | Não é decisão técnica: é combinar com quem aprovou o Termo |
| P4 | **Compilar e rodar o backend** com as rotas novas: o código Java dos tutoriais **não foi compilado** (sem acesso ao Maven Central) | Lucas | Ao seguir o tutorial | Imports de teste do Boot 4 e coordenadas do Testcontainers precisam de conferência no IntelliJ |
| P5 | **Licença WLS e Python 3.14.7:** nada disso foi verificado (a licença do Lucas ainda não chegou) | Kauê | Quando a licença chegar | Os testes foram rodados com a licença limitada do `gurobipy` e Python 3.12 |
| P6 | **Medir o timeout** de 10 s do subprocesso Python | Kauê e Lucas | Quando houver a licença | Continua "provisório" |
| P7 | Sprint 4: US14 com orçamento, US17, US18, US19, US26, US27, US13, US15, US16 e US20 | Equipe | Planejamento da Sprint 4 | Só sinalizadas nos tutoriais |

---

## 3. Como usar este registro
1. Antes de executar um passo marcado com **"Por que?"** nos tutoriais, confira a linha correspondente da seção 1.
2. Ao decidir uma pendência da seção 2, **mude a linha para a seção 1** com a data e o porquê.
3. Nunca remova uma decisão: se ela mudar, acrescente uma linha nova explicando a mudança.
