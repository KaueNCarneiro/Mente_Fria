# Relatório Individual de Contribuição — Sprint 3 — Kauê Nogueira Carneiro (RA 2840482423039)

**Papel nesta sprint:** Facilitador / Scrum Master e responsável pelo módulo Python

> A Sprint 3 oficial foi de planejamento e preparação (roteiro, Contrato v2, código de referência testado, Adendo nº 1). O código dela entrou na `main` na Sprint 4 (A39). Este relatório é entregue junto com o da Sprint 4 (A43).

## 1. O que fiz

| Item | PR/commit | Status |
| --- | --- | --- |
| Licença do Gurobi: aprovação da licença WLS acadêmica de um ano em meu nome, válida até 25/09/2027 (e-mail de aprovação da Gurobi, registrado pela equipe em 27/10). Com isso, a decisão que dizia que a equipe não tinha WLS foi corrigida | — (sem commit; decisão A34 em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`) | Feito |
| Consulta à Gurobi sobre o uso da licença no Render, pedida pelo professor em 27/10: perguntei primeiro ao agente de IA do site e depois abri um chamado no suporte. A resposta escrita, de 28/10, autorizou a licença restrita do `pip` no sistema publicado e vetou a WLS numa URL pública | — (sem commit; decisões A30 e A38 no mesmo arquivo) | Feito |
| Pedido para aplicar a v2 ao Contrato de Comunicação oficial (27/10, decisão A10), que passou a ser a referência entre o frontend, o backend e o Python | PR #28; na `main` desde o PR #30 | Feito |
| Aprovação do Adendo nº 1 ao Termo de Aceite, como integrante da equipe (27/10) | `docs/Sprint_3/sprint-3-adendo-termo-de-aceite.md` | Feito |
| Código do módulo Python | — | Sem PR ou commit meu nesta sprint: a otimização por lucro e a reposição foram planejadas aqui, escritas e testadas em `docs/Sprint_3/referencia/`, e entraram na `main` na Sprint 4 |

## 2. Rituais que participei

- [X] Dailies/weeklies
- [ ] Sprint Review (a Sprint 3 não teve Review própria: foi entregue junto com a Sprint 4, A43)
- [ ] Retrospectiva (feita junto com a das Sprints 2 e 4, no encerramento)

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
| --- | --- | --- |
| — | — | Nenhum nesta sprint |

## 4. Dificuldades e o que aprendi

A Sprint 3 foi de planejamento, e a minha parte só virou código na Sprint 4. A principal dificuldade foi a licença do Gurobi. A licença WLS acadêmica é pessoal, e o professor pediu uma confirmação por escrito antes de qualquer uso no sistema publicado. Enquanto a resposta não chegava, a equipe precisou planejar uma reserva: o sistema publicado sem o cálculo, com a otimização demonstrada no meu computador.

Aprendi que os termos de uma licença precisam ser lidos e confirmados com o fornecedor antes de planejar o deploy. A resposta do suporte mudou o plano: a WLS não pode ficar numa URL pública, mas a licença restrita que vem com o `pip` atende a uma demonstração acadêmica pequena. Também aprendi que uma autorização só vale se estiver por escrito, e que ela precisa estar registrada nas decisões da equipe.
