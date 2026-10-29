# Decisões e pendências — Sprint 4 — Mente Fria

**Revisão:** 28/10/2026, noite (A43: respostas do grupo; antes: criação, A39–A42).

**Continua o registro da Sprint 3.** As decisões **A1–A38** estão em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md` e **continuam valendo**, salvo onde uma decisão abaixo diz que as substitui. A numeração segue a mesma: a próxima decisão é a **A44**.

**Como usar:** o mesmo da Sprint 3. Nunca apague uma decisão. Se ela mudar, acrescente uma linha nova explicando a mudança. As **pendências abertas** passaram para cá (§2). As da Sprint 3 ficam lá só como registro.

---

## 1. Decisões

| # | Decisão | Por quê | Substitui / altera |
|---|---|---|---|
| **A39** | **Nomenclatura e calendário oficial** (28/10). No cronograma do professor, a **Sprint 3 terminou** e o período atual é a **Sprint 4**, com entrega e Review na **sex 06/11/2026**. Depois vem uma **semana de mapeamento e correção de bugs**, até 13/11 (A43), que não é sprint. A partir de agora: **Sprint 3** = planejamento e preparação (roteiro, auditoria, Contrato v2, código de referência testado, Adendo nº 1, decisões A1–A38); **Sprint 4** = levar esse escopo ao código real, publicar e entregar em 06/11; **semana de bugs** = a antiga "janela de testes". O calendário em vigor fica **só** em `docs/Sprint_4/sprint-4-roteiro.md`. Os arquivos de `docs/Sprint_3/` **não são renomeados nem movidos**: os tutoriais passam a ser o guia de execução da Sprint 4. O **adendo assinado não é editado**; a correção de nome vai ao professor (G11). A Sprint 3 ganha uma entrega própria, no padrão da Sprint 2 (G10). **Alterada pela A43:** o adendo foi corrigido para "Sprint 4" com uma nota, e a Sprint 3 é entregue junto com a Sprint 4, em 06/11 | O professor adiou a entrega da Sprint 4 para 06/11 e espera o pacote do adendo como Sprint 4. Os documentos chamavam de "Sprint 3" o trabalho que está sendo feito na Sprint 4 oficial, com datas que já não valem | **A1** e **A36** (calendário). A36 tinha fim em 02/11, deploy no sáb 31 e janela de testes de 02 a 06/11 |
| **A40** | **Histórias fora do escopo obrigatório** (28/10). Como não há sprint de desenvolvimento depois de 06/11, a história que não entrar agora fica **fora do semestre**. **US26** (porção padrão por unidade) é a **única candidata, e condicional**. Ela tem um *gate* na conferência de seg 02/11, **de manhã** (A43) (Fases 1, 2, 3, 4A e P + Q na `main`; *gate* da Fase 2 aprovado; feita pelo **Diogo**, fora do caminho crítico, A43), e tem de estar **na `main` com os testes até qua 04/11 às 18h**; se não estiver, sai. **Ficam fora do semestre:** US27 (cancelar pedido), US13 (clientes), US19 (exportar CSV), as versões completas de US15 e US16, e US21–US25 (Won't) | **US26:** o banco já está pronto (T72, T73); os critérios 1 e 2 já funcionam na tela de ingredientes (`frontend/js/app.js` na `main`, com a API simulada; o `GET /api/unidades-medida` real entra na Fase 2); faltam `POST` e `PATCH /api/unidades-medida`, a Tela 19 e os casos de teste. Depende só das Fases 1 e 2 e não mexe no código do Lucas nem nas telas do Leonardo. **Não tem código de referência**: seria escrita do zero. **US27:** banco e consultas já tratam `CANCELADO`, mas faltam estimativa, rota no contrato ("a definir"), tela no protótipo e caso de uso. Ela mexe no serviço de pedido e na tela de pedidos, ou seja, no caminho crítico. O professor só aceita trabalho extra com o obrigatório estável (`resposta-professor.md`; adendo, §6). A própria US26 só entra por essa regra do §6 do adendo: ela é uma "Could" adiantada, com o obrigatório em dia. **US13:** muda a tela de pedido e pede as Telas 18 e 18B, com API indefinida. **US19:** depende da Fase 6 e da Tela 03, as últimas do caminho crítico. **US15/US16 completas:** o adendo, item 4, opção (a), já definiu a cobertura deste semestre | **P7** e o item (b) da **A28** ("Sprint 4 relâmpago") |
| **A41** | **Deploy definitivo na seg 02/11** (28/10), e não mais no sáb 31. O banco e o serviço são criados nesse dia (a conta, antes, pelo Lucas; A43); o seed e o primeiro backup vêm logo depois. O ensaio numa conta pessoal deixa de ser necessário. **Backups:** depois do seed (02/11), no fim da semana de bugs, uma vez por semana e em 29/11 | O banco gratuito vence 30 dias depois de criado: criado em 02/11, vence em **02/12**, depois de 30/11 (data-limite do professor) e da semana de bugs. O Render fica 4 dias em uso antes da entrega, com tempo para corrigir problemas de memória, CORS ou tempo. O Render publica sozinho o que entrar na `main` depois | Datas da **A21** e da **A32** (deploy no sáb 31; backup "ao fim da janela de testes, 06/11") |
| **A42** | **Conferências de corte** (28/10), feitas pelo PO (Lucas): no **M3 (dom 01/11, à noite)**, o botão "Descartar" sai se as Telas 10/16 e 11/17 não funcionarem com o backend real (se ficar, é feito na seg 02/11); na **seg 02/11, de manhã** (A43; antes: à tarde), o *gate* da US26 (A40) e a licença com o solver ligado (A38). Revisão no **M4** (ter 03/11, noite) com a ordem de corte do roteiro da Sprint 4, §5. O termo "Sprint 4 relâmpago" deixa de ser usado | A conferência do sáb 31 (A28) foi pensada para um deploy no sábado e um fim de sprint em 02/11 | Data e item (b) da **A28** |
| **A43** | **Respostas do grupo às pendências de 28/10** (28/10, noite). **G13:** o grupo trabalha na seg 02/11 (feriado, sem aula), que vira o **dia de mais carga**: Fases 5 e 6 de manhã, deploy à tarde, suíte completa à noite, início da Tela 13 e da US26. **G4:** o **Lucas** cria a conta do Render (só a conta, até sáb 31/10) e faz o deploy na seg 02/11, com o Kauê guiando pelo tutorial dele, §12; as credenciais da conta são só do Lucas. **G10:** a Sprint 3 é entregue **junto com a Sprint 4, na sex 06/11** (relatório, retrospectiva e contribuições individuais das duas, até qui 05/11). **G11:** o adendo passa a dizer **"Sprint 4"** onde dizia "Sprint 3": foi um equívoco de redação, e ele foi validado pessoalmente com o professor já como Sprint 4. A correção está registrada no próprio adendo; nenhum conteúdo, critério ou data mudou. **G12:** a semana de bugs vai **até 13/11**. **G14:** a US26, se passar no *gate*, fica inteira com o **Diogo** (contrato e casos de teste na seg 02/11; backend na ter 03/11; Tela 19 na qua 04/11, até as 18h); o **Kauê** aprova a seção do contrato e o **Lucas** revisa os dois PRs; o Leonardo não participa. Com isso, o *gate* da US26 passa para a **manhã** de seg 02/11, com o estado do M3 | Respostas do grupo em 28/10. **G14:** o Diogo é quem tem mais folga depois de sex 30/10 e já é dono dos repositórios; Lucas e Leonardo estão no caminho crítico, e o Kauê tem a Fase 5, o deploy e a facilitação. O *gate* de manhã dá à US26 três dias (seg a qua) em vez de dois. **G4:** o PO é quem decide a conta e responde pelo sistema publicado até 30/11 | **A39** (entrega própria da Sprint 3 e adendo sem edição), **A40** (*gate* e responsável da US26), **A42** (horário da conferência) |

---

## 2. Pendências abertas

| # | Pendência | Dono | Prazo | Situação / recomendação |
|---|---|---|---|---|
| Mensagem | Enviar ao professor a mensagem da Sprint 4 (calendário, licença, US26) | Diogo envia; Lucas revisa | **Qui 29/10** | Rascunho em `sprint-4-mensagem-professor.md` |
| Render (A43) | Criar a conta do Render (só a conta) | **Lucas** | **Sáb 31/10** | O deploy é na seg 02/11, na conta dele, com o Kauê (A41, A43) |
| Gurobi (A38) | Encaminhar ao professor o e-mail de autorização da Gurobi | Kauê | **Qui 29/10** | No mesmo dia em que o Diogo envia a mensagem da Sprint 4, que cita esse e-mail |
| **G8** (A15) | Juntar os dois `.env.example` no `backend/.env.example` | Kauê | Sex 30/10 | Com as variáveis da Sprint 3 e os comentários úteis do `backend/env.example` |
| A12 | Botão "Descartar" por linha (opcional) | Leonardo | Seg 02/11, **se** aprovado no M3 (A42) | O backend vem na Fase 3. É o primeiro corte |
| A13 | Teste de navegador instável | Leonardo | Qua 04/11 | Tutorial do Leonardo, §7.1 |
| P2 | `optimization/requirements.txt` ainda em UTF-16 | Kauê | Qui 29/10, na Fase P | A versão 13.0.3 já está fixada; README e Arquitetura já atualizados (A37) |
| **G5** | Apagar as branches antigas (as 10 já incorporadas e a `frontend_feito`) | Grupo | Depois de 06/11 | Nada urgente; não mexer durante a sprint |
| **G6** | Destino de `docs/Sprint_3/referencia/` depois da cópia | Grupo | Retrospectiva (qui 05/11) | Recomendação: manter como registro congelado até o fim do semestre |
| **G7** | Nome da pasta do ambiente Python: `venv` ou `.venv` | Grupo | Qui 29/10, antes da Fase P | Alinhar o README e os tutoriais com a escolha |

**Encerradas nesta revisão:**

| # | Era | Situação |
|---|---|---|
| P7 | Histórias de fora e "Sprint 4 relâmpago" | Substituída pela **A40** |
| G9 (A28) | Conferência do sáb 31 à tarde | Substituída pela **A42** (seg 02/11) |
| G3 | Registrar a US27 como história nova no Backlog | **Feito em 27/10** (A37: "US27 nova" no Backlog). A A40 a registra como fora do semestre |
| A11 | Frase da Arquitetura sobre licença e Compute Server | **Feito** (A37 e A38) |
| G4 | Quem cria a conta do Render | **Decidida pela A43:** o Lucas |
| G10 | Entrega da Sprint 3 | **Decidida pela A43:** junto com a Sprint 4, em 06/11 |
| G11 | Nome "Sprint 3" no adendo | **Decidida pela A43:** corrigido para "Sprint 4", com nota no adendo |
| G12 | Data da semana de bugs | **Decidida pela A43:** até 13/11 |
| G13 | Trabalhar no feriado de 02/11 | **Decidida pela A43:** sim; é o dia de mais carga |
| G14 | Quem faz a US26 | **Decidida pela A43:** o Diogo (Kauê aprova o contrato; Lucas revisa) |
