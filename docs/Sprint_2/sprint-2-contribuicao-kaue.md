# Relatório Individual de Contribuição — Sprint 2 — Kauê Nogueira Carneiro (RA 2840482423039)


**Papel nesta sprint:** Facilitador / Scrum Master e responsável pelo módulo Python

## 1. O que fiz

| Item | PR/commit | Status |
| --- | --- | --- |
| Exclusão do repositório a pedido do desenvolvedor de Backend (conflitos na comunicação Java ↔ banco) e criação de um repositório novo | — (ação administrativa no GitHub, sem commit associado) | Feito (30/09) |
| Reescrita completa do módulo Python (`/optimization`) do zero no repositório novo: estrutura de pastas, venv, `otimizador.py` real com Gurobi conforme o Contrato de Comunicação §1, e suíte de 8 testes automatizados (exemplo oficial do contrato + cenários de estoque escasso + casos de erro) — todos passando | PR #1 (branch `comunicacao_python`) | Feito |
| Obtenção da licença Gurobi WLS (29/09) e configuração/depuração no ambiente local (bloqueio de antivírus, variável de ambiente, supressão do banner de licença que vazava para o stdout e quebrava o contrato JSON) | PR #1 (mesmo PR do item acima) | Feito |
| Correção de um erro de digitação no nome da pasta (`optmization` → `optimization`) e de um padrão do `.gitignore` que não alcançava subpastas, que juntos deixaram um arquivo de cache (`__pycache__`) prestes a subir no PR | PR #1 (mesma branch) | Feito |
| Base da documentação de entrega das sprints (relatórios, evidências, atas) | - | Feito |
| Solução para o bloqueio das credenciais WLS impedindo o teste real Java → Python | — | Aprovado pelo Lucas (mock Java simulando a resposta do Python); repasse da licença real ao Java ainda em aberto, sob responsabilidade de Kauê|

## 2. Rituais que participei

- [X] Dailies/weeklies (2 de 2)
- [ ] Sprint Review
- [X] Retrospectiva

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
| --- | --- | --- |
| PR #2 | Leonardo | Não fiz comentário, apenas verifiquei se nada estava sendo quebrado |

## 4. Dificuldades e o que aprendi

Ao implementar a licença WLS, as credenciais ficaram apenas no meu computador, e precisei descobrir como repassá-las ao Java sem comprometê-las — por isso o teste completo Java → Python ainda não foi possível nesta sprint. A exclusão do repositório resolveu os conflitos imediatos, mas pode ter apagado o histórico de PRs e commits anteriores.

Reconstruir o módulo do zero também trouxe uma sequência de problemas técnicos que valem como aprendizado: o Controle de Aplicativos do Windows bloqueou o carregamento do binário nativo do `gurobipy`; e um simples erro de digitação no nome de uma pasta (`optmization` em vez de `optimization`) fez regras inteiras do `.gitignore` falharem silenciosamente, quase deixando a venv e a licença subirem para o repositório remoto — por sorte a licença ficava fora da pasta do projeto, então não chegou a ser exposta, mas foi um alerta real sobre revisar `git status` com atenção antes de cada commit.

