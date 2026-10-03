# Relatório Individual de Contribuição — Sprint 1 — Kauê Nogueira Carneiro (RA 2840482423039)

**Papel nesta sprint:** Facilitador / Scrum Master (também responsável pelo módulo Python e mantenedor do Contrato de Comunicação)

## 1. O que fiz

| Item | PR/commit | Status |
| --- | --- | --- |
| Decisão de adotar o walking skeleton como método, pela inexperiência da equipe com comunicação entre programas | — | Feito |
| Ensino do workflow do GitHub (branches, PR, revisão) ao resto da equipe | — | Feito |
| Roteiro da Sprint 1 (junto com o Diogo): plano camada por camada — banco → Java↔banco → Python isolado via stdin/stdout → Java↔Python (subprocesso) → CORS → frontend | - | Feito |
| Criação do repositório e configuração da restrição de PRs | — (ação de configuração do GitHub, sem commit associado; repositório excluído na Sprint 2 — ver relatório da Sprint 2) | Feito |
| Contrato de Comunicação (contrato Java↔Python e Java↔Frontend aprovados pela equipe) | - | Feito |

## 2. Rituais que participei

- [x] Weeklies (1 de 1)
- [ ] Sprint Review
- [ ] Retrospectiva

## 3. PRs de colegas que revisei

| PR | Autor | Comentário resumido |
|---|---|---|
| — | — | Por termos excluído o nosso primeiro repositório, não tenho o registro dos PR's que revisei |

## 4. Dificuldades e o que aprendi

A equipe não tinha experiência com comunicação entre programas; por isso a primeira sprint foi dedicada a isolar e testar cada camada (banco → Java → Python → frontend) antes de integrar, e o walking skeleton se mostrou mais lento do que eu esperava. Uma lição que ficou clara nesse processo: validar o módulo Python isoladamente via stdin/stdout, sem depender do Java estar de pé, permitiu achar e corrigir problemas (como a codificação UTF-8 dos acentos) muito antes da integração real — isso reforçou a escolha de testar camada por camada em vez de integrar tudo de uma vez.

