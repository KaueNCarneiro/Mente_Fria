# Mensagem ao professor — Sprint 3 (P3)

**Para enviar:** Diogo, em nome do grupo (o Lucas, como PO, revisa antes). **Quando:** terça, 27/10.
**Depois:** guardar a resposta do professor nesta pasta (`docs/Sprint_3/resposta-professor.md`) e avisar a equipe. Só então editar o Termo, a Visão e o Backlog (tutorial do Diogo, §9.2).

O texto abaixo é para copiar e colar. Os trechos entre colchetes são para conferir antes de enviar.

---

**Assunto:** Mente Fria — mudanças na Sprint 3 e três dúvidas

Olá, professor, tudo bem?

Aqui é o Diogo, do grupo do Mente Fria (com Kauê, Lucas e Leonardo). Estamos fechando o planejamento da Sprint 3, que vai até 01/11, com testes de 01 a 06/11. Antes de mexer nos documentos aprovados, queremos te avisar de algumas mudanças e tirar três dúvidas.

**Resumo:** todas as histórias obrigatórias (Must) do Termo de Aceite entram na Sprint 3, inclusive reposição (US14), dashboard (US17), sugestões de marketing (US18) e publicação do sistema (US20). As de prioridade menor (US13, US15, US16, US19, US26 e US27) ficam para depois.

**O que muda em relação ao Termo**

1. **Recomendação de produção por lucro, e não por receita.** O sistema passa a sugerir quanto produzir de cada açaí pensando no **lucro** (preço menos o custo dos ingredientes), e não só no valor vendido. Fizemos isso porque a principal dor do cliente, no Documento de Visão, é a lucratividade. A receita estimada continua aparecendo na tela.
2. **Reposição com orçamento (US14).** O dono informa um período (por exemplo, os últimos 30 dias) e quanto pode gastar. O sistema sugere quanto comprar de cada ingrediente sem passar desse valor. Se o dinheiro não der nem para o mínimo, ele avisa quanto falta.
3. **Dashboard (US17).** Mantém a **receita máxima estimada** e os **produtos prioritários**, como o Termo pede, e acrescenta: faturamento do mês, produtos mais vendidos, ingredientes mais usados (ordenados pelo valor gasto) e um ranking de vendas por usuário, com filtros.
4. **Sugestões de marketing (US18).** O sistema só **sugere** campanhas para os produtos priorizados, a partir de uma lista de ideias. Ele nunca publica nada sozinho.

Pensamos em registrar essas mudanças como um **adendo datado** ao Termo, sem reescrever o texto que já foi aprovado.

**Nossas dúvidas**

1. **O adendo ao Termo resolve?** Ou o senhor prefere outra forma de registrar as mudanças acima?
2. **Licença do Gurobi no servidor.** Não conseguimos a licença acadêmica do Gurobi que funciona em servidor (a WLS). O sistema publicado vai usar a **licença de avaliação** que já vem com a instalação do Gurobi pelo `pip`. Ela limita o tamanho dos problemas, mas os nossos cabem com folga (testamos). Podemos usar essa licença no sistema publicado?
3. **Datas da avaliação.** Vamos publicar o sistema no plano gratuito do Render, e o banco de dados gratuito de lá **expira 30 dias depois de criado**. Para aproveitar o prazo ao máximo, pretendemos criá-lo só em **31/10**; ele ficaria no ar até **30/11**. Até quando o senhor vai precisar acessar o sistema publicado? Se for depois de 30/11, a gente se planeja para recriar o banco.

**Uma pergunta extra.** Se sobrar tempo, gostaríamos de adiantar algumas das histórias de prioridade menor (por exemplo, cancelar pedido, US27) numa sprint curta extra. Para isso, seria possível estender um pouco o período de testes, que hoje vai de 01 a 06/11? Ou o senhor prefere que a gente use esse tempo só para testes e correções?

Obrigado! Qualquer coisa, estamos à disposição.

Abraço,
Diogo — grupo Mente Fria [conferir: assinar também pelos outros integrantes?]

---

## Para a equipe (não enviar)

| Item da mensagem | Decisão de origem |
|---|---|
| Lucro em vez de receita | F9 |
| Reposição com orçamento | Decisões de 14/10 (US14), A19 |
| Dashboard | A16 e A17 (aprovadas em 27/10) |
| Marketing só sugere | Critério da US18; A20 |
| Licença de avaliação | A22 |
| Banco que expira; criação em 31/10 | A21 (adiada em 27/10) |
| Sprint curta extra | P7 e A28 (gatilho de sexta, 30/10) |

**Se a resposta mudar algo:**
- **Licença recusada:** avisar o Kauê e o Lucas no mesmo dia. A saída é o professor indicar a licença aceita. O código não muda: só a variável de ambiente da licença no Render.
- **Avaliação depois de 30/11:** recriar o banco em data mais próxima, ou passar para o plano pago. Antes de decidir, confirmar as regras na documentação do Render. A página de 26–27/10 diz que só pode haver **um banco gratuito ativo por conta** e **não diz** se é possível criar outro depois que um expira.
