# Registro da perda do histórico do Git — Mente Fria

**Data do registro:** 11/11/2026 · **Pedido por:** relatório de entrega da Sprint 2, §6 ("Histórico do Git") · **Tarefa:** tutorial do Kauê, Parte 9.3 (escrito pelo Diogo, a pedido do Kauê)

Este registro junta **só o que pode ser conferido**: o que o Git e o GitHub mostram hoje e o que os documentos da Sprint 2 já diziam. Onde não há evidência, isso está dito.

---

## 1. O que aconteceu

Em **27/09/2026**, o repositório do projeto no GitHub foi **excluído e recriado** pelo Kauê (dono do repositório), a pedido do Lucas. Com a exclusão, **o histórico de commits e de Pull Requests do repositório anterior deixou de existir**.

## 2. Por quê

Motivo relatado nos documentos da Sprint 2: **conflitos de estrutura do projeto e na comunicação Java ↔ banco**. Na contribuição individual do Lucas: na Sprint 1, "o projeto não encontrava os caminhos corretos por causa da desorganização", e a conexão com o banco só funcionou depois da "reorganização da estrutura de diretórios".

Não há registro de outras alternativas tentadas antes da exclusão (reverter commits, corrigir por branch e PR).

## 3. O que o Git e o GitHub mostram hoje

Conferido em 11/11/2026, na `main` (`c487834`):

| Fato | Evidência |
|---|---|
| O repositório atual foi criado em 27/09/2026, às 13h07 (horário de Brasília) | Dado público do GitHub: `created_at` = `2026-09-27T16:07:29Z` |
| O commit mais antigo da `main` é o `9e02498`, "first commit", do Lucas, de 27/09/2026, às 13h23 | `git log --reverse --format="%h %ad %an %s" --date=iso` |
| Esse commit é a única raiz da `main`: não existe nada anterior a ele | `git rev-list --max-parents=0 origin/main` |
| Ele traz 29 arquivos (3.539 linhas), em `backend/`, `docs/`, `README.md` e `.gitignore` | `git show --stat 9e02498` |
| A `main` tem hoje 146 commits, todos de 27/09 em diante | `git rev-list --count origin/main` |
| A branch `frontend_feito` tem outra raiz (`f8c4bc7`, também "first commit" de 27/09), sem base comum com a `main` | `git merge-base origin/main origin/frontend_feito` não devolve nada |

## 4. O que foi perdido

- Todos os commits anteriores a 27/09/2026 (o trabalho da Sprint 1 e do começo da Sprint 2), com autores, datas e mensagens.
- Todos os Pull Requests, revisões e comentários do repositório anterior.
- As branches do repositório anterior.

Consequência registrada na Sprint 2 (contribuição do Diogo): os itens entregues antes da recriação **não têm mais número de PR nem branch rastreável**; a evidência que resta deles é o conteúdo final dos arquivos.

## 5. O que foi preservado

- **O conteúdo final dos arquivos**, levado ao repositório novo:
  - backend e documentos, no `9e02498` (o trabalho de backend do Lucas na Sprint 2 aparece inteiro nesse único commit);
  - frontend, reorganizado no repositório recriado pelo Leonardo (PR #2).
- **O módulo Python não foi preservado:** foi reescrito do zero no repositório novo (PR #1, branch `comunicacao_python`), como consta na contribuição do Kauê.
- **Não existe cópia do repositório anterior:** nenhum clone antigo, print ou exportação do histórico foi localizado (confirmado pelo Diogo em 11/11/2026). O relatório da Sprint 2 pedia que fossem anexados "se existirem"; não existem.

## 6. Divergência entre os documentos

A data da recriação aparece como **27/09** no relatório e na retrospectiva da Sprint 2, e isso bate com o GitHub (§3). A contribuição individual do Kauê na Sprint 2 marca a exclusão como "Feito (30/09)". **Vale 27/09**, que é a data comprovada; 30/09 é a data dos primeiros commits dele no repositório novo.

## 7. O que mudou depois

A retrospectiva da Sprint 2 registrou a ação: resolver conflitos por branches e PRs, e preservar cópias e o histórico antes de qualquer recriação. Desde então:

- o trabalho passou a entrar por branch e Pull Request (do PR #1, de 01/10, ao #89, de 11/11/2026), com exceções pontuais de commits diretos, como os de 11/11 na `sprint-4`;
- o repositório não foi recriado de novo: o histórico de 27/09 em diante está inteiro.

## 8. Fontes

- `docs/Sprint_2/sprint-2-relatorio-entrega.md` (T15 e §6);
- `docs/Sprint_2/sprint-2-retrospectiva.md`;
- `docs/Sprint_2/sprint-2-contribuicao-kaue.md`, `-lucas.md`, `-diogo.md` e `-leo.md`;
- o repositório `KaueNCarneiro/Mente_Fria` (comandos da §3).
