# Relatório Individual de Contribuição — Sprint 3 — Leonardo Fagundes Oliveira (RA 2840482421009)

**Papel nesta sprint:** Responsável por Frontend

> A Sprint 3 oficial foi de planejamento e preparação (roteiro, Contrato v2, código de referência testado, Adendo nº 1). O código dela entrou na `main` na Sprint 4 (A39). Este relatório é entregue junto com o da Sprint 4 (A43).

## 1. O que fiz

| Item | PR/commit | Status |
|---|---|---|
| Participei das respostas da equipe às pendências do planejamento (27/10 e 28/10), que definiram o escopo do frontend para a Sprint 4: divisão do `app.js`, guarda de rotas, correção do teste instável (A13) e as Telas 03, 10/16, 11/17, 12, 13 e 14 | Registro em `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md` e na A43 | Feito |
| Aprovação do Adendo nº 1 ao Termo de Aceite, como integrante da equipe (27/10) | `docs/Sprint_3/sprint-3-adendo-termo-de-aceite.md` | Feito |
| Código do frontend | — | Sem PR ou commit meu nesta sprint: as telas novas foram planejadas aqui e implementadas na Sprint 4 (PRs #64 a #74) |

## 2. Rituais que participei

- [X] Dailies/weeklies
- [ ] Sprint Review (a Sprint 3 não teve Review própria: foi entregue junto com a Sprint 4, A43)
- [ ] Retrospectiva (feita junto com a das Sprints 2 e 4, no encerramento)

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
|---|---|---|
| — | — | Nenhum nesta sprint |

## 4. Dificuldades e o que aprendi

A Sprint 3 foi de planejamento, e a minha parte só virou código na Sprint 4. A dificuldade foi dimensionar o frontend sem o backend real: as seis telas novas dependiam de rotas que ainda não existiam na `main`, e a auditoria apontou a minha carga (seis telas em poucos dias) como um risco do caminho crítico.

Aprendi a usar o Contrato de Comunicação como a referência entre o frontend e o backend. Com o formato de cada rota definido na v2 do contrato (campos, códigos de erro e quem calcula cada valor), deu para planejar cada tela e os testes dela com a API simulada antes de o backend ficar pronto. Também ficou claro que a integração com a API real precisava de um momento próprio no calendário (o *gate* da Fase 2), porque a API simulada só prova que a tela segue o contrato, não que o backend o segue.
