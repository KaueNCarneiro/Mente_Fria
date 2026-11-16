# Registro de análise — Sprints 2, 3 e 4 — Mente Fria

> **Não houve reunião de retrospectiva das Sprints 3 e 4.** Na Sprint 2 houve uma ligação de retrospectiva em 02/10, com os quatro integrantes, sem ata (§0). O grupo escolheu este registro, sem reunião, em 15/11/2026 (informado pelo Diogo). Ele é do mesmo tipo do registro da Sprint 2 (`docs/Sprint_2/sprint-2-retrospectiva.md`): reúne fatos, dificuldades e propostas para discussão. **Não é ata de reunião e não registra ações aprovadas.**

- **Corte da análise:** 15/11/2026, horário de Brasília.
- **Revisão:** **Revisado e aprovado pelo Kauê (facilitação) em 15/11/2026** (informado pelo Diogo).
- **Data da retrospectiva:** não realizada.
- **Participantes:** não se aplica.
- **Base:**
  - repositório `KaueNCarneiro/Mente_Fria`, branch `main`, commit `3acf6af` (PR #123);
  - os relatórios das Sprints 3 e 4;
  - `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md`;
  - `docs/Sprint_4/sprint-4-semana-de-bugs.md`;
  - as evidências de `docs/Sprint_4/evidencias/`.

## 0. Registro pedido: a retrospectiva da Sprint 2

O tutorial do Kauê (Parte 9.4, item 3) pede o registro de uma contradição sobre a Sprint 2:
- **as contribuições individuais marcam a retrospectiva como feita:** a caixa "Retrospectiva" está marcada (`[X]`) nos quatro arquivos `docs/Sprint_2/sprint-2-contribuicao-*.md`;
- **o relatório e o registro de análise dizem que ela não aconteceu:** o relatório da Sprint 2 diz "Retrospectiva: não realizada, conforme confirmação da equipe", e o registro de análise diz "Não houve reunião de retrospectiva da Sprint 2". Os dois foram escritos no fim da Sprint 2 (publicados no GitHub em 03 e 04/10).

**Esclarecimento de 15/11/2026:** segundo o Diogo, **houve uma ligação de retrospectiva na Sprint 2, em 02/10/2026, com os quatro integrantes** (Diogo, Kauê, Leonardo e Lucas). Não houve ata. Com esse relato, as contribuições individuais estão de acordo com o que aconteceu, e o erro está no relatório e no registro de análise da Sprint 2. Esses dois documentos não foram alterados, porque já foram entregues. O que foi discutido na ligação não está registrado, por isso este documento não o reconstrói.

Nas Sprints 3 e 4, as contribuições individuais deixam a retrospectiva e a Review sem marcar (`[ ]`), o que está de acordo com os fatos.

## 1. Propostas do registro da Sprint 2: o que aconteceu

As propostas do §4 do registro da Sprint 2 não foram deliberadas. Esta tabela só mostra o que as evidências indicam até 15/11.

| Proposta (Sprint 2) | Situação observada até 15/11 | Evidência |
|---|---|---|
| Discutir as dificuldades e registrar ações, responsáveis e prazos | **Não feito como ata:** a ligação de retrospectiva da Sprint 2 não tem registro de ações (§0), e não houve retrospectiva em reunião nas Sprints 3 e 4. As decisões foram registradas por escrito (A1–A47), com data e responsável | `docs/Sprint_3/sprint-3-decisoes-e-pendencias.md`, `docs/Sprint_4/sprint-4-decisoes-e-pendencias.md` |
| Resolver conflitos por branches e PRs; preservar o histórico antes de qualquer recriação | **Em parte:** o grupo adotou uma branch por sprint (`sprint-3`, `sprint-4`) e PRs `feature/` e `fix/`; a perda do histórico de 27/09 foi registrada; não houve nova recriação do repositório. Mas as Fases 2, 3, 4A e 6 entraram sem PR | `docs/Sprint_3/sprint-3-registro-historico-git.md` (PR #90); relatório da Sprint 4, §1.1 |
| Validar o ambiente Gurobi em outra máquina, sem expor credenciais | **Resolvido de outro jeito:** a Gurobi autorizou por escrito a licença do `pip` no sistema publicado (A38). O Python rodou fora da máquina do Kauê: no Windows do Diogo, no container do CT67 e no Render | `docs/Sprint_3/sprint-3-licenca-gurobi.md`; CT67; CT68 |
| Integrar Java → Python | **Feito:** Fase 4A, `PythonRealTest` (3 testes), recomendação funcionando na URL publicada | `2026-11-11_kaue_fase5_python-real.txt`; CT68 |
| Autenticação, autorização e cadastros com o banco real | **Feito:** Fases 1 e 2 | `2026-10-29_lucas_fase1_mvnw-test.txt`; `2026-11-11_diogo_repositorios_mvnw-test.txt` |
| Alinhar versões e instruções de execução | **Feito:** o README atualizado (PR #108) levou o projeto do zero até o sistema funcionando | CT20, `2026-11-15_diogo_ct20_setup-do-zero.txt` |
| Reestimar as Sprints 3 e 4 | **Feito:** escopo fechado (A2b), Adendo nº 1 aprovado pelo professor, calendário oficial (A39) e cortes (A40) | Relatório da Sprint 3, §1 |

## 2. O que funcionou — fatos e pontos para discussão

- **Preparar antes de integrar:**
  - O contrato de comunicação v2 e o código de referência, testado na Sprint 3, deixaram a integração mecânica.
  - Quando as fases chegaram à `main`, em 11/11, a suíte do backend passou inteira (95 testes, 0 falhas) assim que os repositórios entraram.
- **Escopo e decisões por escrito:**
  - O Adendo nº 1 e as decisões A39–A47 registram o quê, quem e quando.
  - As mudanças de plano (deploy na conta do Diogo, US26 depois do prazo, data do encerramento) ficaram registradas, com o motivo.
- **Evidências durante o trabalho:**
  - A Sprint 4 tem 41 arquivos de evidência, contra nenhum na Sprint 3 até 28/10.
  - Os relatórios e a tabela da semana de bugs foram montados a partir deles.
- **Fluxo de correção da semana de bugs:**
  - Os 4 defeitos seguiram o fluxo registrar → teste que reproduz → `fix/...` → PR → revisão → retestar na URL publicada.
  - Três dos quatro PRs (#103, #107 e #113) foram revisados pelo Leonardo, com rodadas próprias do `npm test`. O quarto defeito, a Granola duplicada, era um dado no banco e foi corrigido sem PR.
- **Publicação testada antes:**
  - A imagem foi testada com os limites do plano gratuito antes do deploy (CT67).
  - No ambiente publicado, o CT68, o CT69, o CT70 e o CT72 passaram.
  - O deploy automático está ligado desde 15/11.
- **Dados protegidos:**
  - Os backups têm a restauração testada (14 e 15/11).
  - A exposição parcial da URL do banco num print, em 11/11, foi tratada com a restrição de IP em 12/11.
- **Escopo extra sem prejudicar o obrigatório:** a US26 entrou depois do obrigatório, dentro do prazo da A46, e o professor foi avisado e está de acordo.

## 3. Dificuldades — fatos

- **Atraso em relação ao calendário (relatório da Sprint 4, §1.3):**
  - O código obrigatório chegou à `main` em 11/11, e não entre 29/10 e 04/11.
  - O deploy foi em 11/11, e não em 02/11.
  - Não houve congelamento em 04/11 nem Review em 06/11.
  - Os testes no ambiente publicado foram em 14 e 15/11, depois do fim previsto da semana de bugs.
- **Trabalho só local na Sprint 3:** do PR #24 (14/10) até 26/10, nada foi para o GitHub (relatório da Sprint 3, §6).
- **Distribuição do trabalho no GitHub (PRs #40 a #123):**
  - dos 84 PRs abertos, 57 são do Diogo, 24 do Leonardo (11 deles fechados sem merge e refeitos) e 3 do Lucas;
  - commits na `main` desde 29/10, sem contar merges: 47 do Diogo, 14 do Leonardo e 9 do Lucas;
  - o Kauê não abriu PR do #24 ao #123, e o único commit dele desde 02/10 é um *revert* de 03/10 (`3b92728`);
  - várias execuções foram feitas na máquina do Diogo, a pedido dos donos de cada parte (Fase P + Q, Fase 5, CT67, CT68 a CT72), como dizem as próprias evidências.
- **Integração fora do fluxo combinado:**
  - as Fases 2, 3, 4A e 6 entraram na `sprint-4` sem PR;
  - os PRs #53 a #63 foram abertos com base na `main` e refeitos com base na `sprint-4`;
  - o backend da `main` ficou sem compilar de 17h01 a 19h41 de 11/11, entre os PRs #85 e #87.
- **Mudanças de dono no meio da sprint:** o deploy passou do Lucas para o Diogo (A47), porque o Lucas estava sem tempo naquela semana.
- **Facilitação:**
  - não há registro de dailies no repositório;
  - a retrospectiva não foi feita em reunião;
  - o encerramento, adiado em 11/11 sem data, foi marcado e feito em 15/11.
- **Ambiente:**
  - o Docker Desktop da máquina do Diogo não iniciava (incidente A26, 26 e 30/10);
  - o `python -m http.server` deixou o `npm test` instável numa máquina (resolvido com um servidor em Node);
  - o IP liberado no banco mudou em 14/11 e o primeiro backup falhou.
- **Testes que ficaram de fora:**
  - o aceite do Leonardo na URL publicada, com prints, só saiu em 15/11, no fim do encerramento (`2026-11-15_leonardo_aceite-pages.txt`);
  - o CT12 e os casos de erro não foram executados no ambiente publicado;
  - o *gate* da Fase 2 não foi feito.

## 4. Propostas para o fim do semestre — não deliberadas

| Proposta | Responsável sugerido |
|---|---|
| ~~Fazer o aceite na URL publicada, com prints~~ **Feito em 15/11** (PR #127) | Leonardo |
| Backups com restauração testada: por volta de 22/11 e em 29/11, conferindo o IP antes | Diogo |
| Evitar merges na `main` durante uma demonstração (o deploy automático reinicia o backend) e abrir a URL uns 2 minutos antes | Grupo |
| Decidir antes de 11/12/2026, 21h59 (quando o banco gratuito vence) se o sistema continua no ar depois disso | Grupo |
| **G5:** apagar as branches já incorporadas e decidir se a `frontend_feito` (2 commits fora da `main`) fica guardada | Grupo |
| **G6:** decidir o destino de `docs/Sprint_3/referencia/`. A recomendação registrada é mantê-la como registro congelado até o fim do semestre | Grupo |

**Para projetos futuros**, a partir dos fatos acima:
- um PR revisado por fase, sempre com base na branch da sprint;
- registrar as evidências no mesmo dia, como foi feito na Sprint 4;
- fazer uma retrospectiva curta no fim de cada sprint;
- distribuir a execução dos testes entre os integrantes.

**Deliberações:** nenhuma registrada, porque não houve reunião. As propostas não são compromissos dos responsáveis sugeridos.
