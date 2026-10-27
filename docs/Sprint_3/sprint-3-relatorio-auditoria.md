# Relatório de auditoria — materiais da Sprint 3 — Mente Fria

**Data:** 26/10/2026 (tarde: v5; noite: v5.1) e **27/10/2026 (v5.2: respostas da equipe, §A.0)**. Versões anteriores em `historico/v5-26-10-antes-das-4-historias/` e `historico/v5.1-27-10-antes-das-respostas/`.

**Pedido:** auditar o roteiro da Sprint 3 (v4.6) e os cerca de 49 artefatos produzidos em 14/10, corrigi-los e entregar roteiro, tutoriais, matriz e lista priorizada.

**Pedidos da noite** (respostas do usuário ao relatório da tarde):
1. **Incluir US14, US17, US18 e US20 na Sprint 3**, no mesmo modelo das outras.
2. Testar o Docker de novo.
3. Explicar por que o Java das Fases 2 a 4 não tinha sido compilado. Decisão do usuário: **"código completo validado"**.
4. Licença WLS: confirmada como **indisponível**.
5. Playwright instável: fica com o Leonardo.

**Regras seguidas:**
- nenhum commit, push ou operação Git que escreva;
- nada alterado fora de `docs/Sprint_3/`;
- Spring JDBC sem JPA; sem meta de cobertura;
- toda validação em ambientes temporários e isolados. O `mente-fria-db` **não** foi tocado; os containers temporários foram removidos ao fim.

---

## A. Resumo executivo

### A.0 Respostas da equipe em 27/10 e o que foi feito

| Pendência | Resposta da equipe | O que foi feito |
|---|---|---|
| A10 — Contrato v2 | Aprovado | Marcado como aprovado; o descarte por linha entrou no §2.4. Falta o Kauê aplicar ao oficial |
| P3 — professor | "Faça uma mensagem" | `sprint-3-mensagem-professor.md`, pronta para copiar e colar: mudanças, três perguntas (adendo, licença, data da avaliação) e uma extra (sprint curta) |
| A16 | Aprovada | Registrada |
| A17 | Dúvida: copo é produto, não ingrediente | Explicado: no banco, o copo é o ingrediente 7 (tem estoque e custo). Pelo custo ele fica em 4º; por quantidade, passaria o açaí. A equipe manteve a ordenação por R$ |
| A20 | Aprovada; buscar skill de marketing e ampliar o catálogo | Busca com `find-skills`: a skill mais usada (`marketing-ideas`) é voltada a software; as de restaurante têm menos de 50 instalações. **Nada instalado.** Catálogo ampliado de 12 para **18 ideias**, redação da equipe, a reler com o cliente |
| A21 — Render | Adiar, para o banco durar mais | **Definitivo no sábado 31** (vale até 30/11); ensaio opcional numa conta pessoal; quem cria: quinta 29. Não dá para passar de 31/10 (testes no ambiente publicado) |
| A12 — descarte | Pode adicionar; avisar se atrasar | **Implementado e testado** (CT71; suíte 92/92). Gatilho de corte na sexta 30 à noite (A28): só o botão sai |
| P2 | 13.0.3 | Registrado |
| A15 | "Explique melhor" | Verificado de novo: um merge **não** desfaz o PR #24 (a v5.1 dizia o contrário). Risco rebaixado; plano novo para o Kauê |
| A11 | Quem escreve a frase? | Já estava na lista do Diogo (§9.2, item 7). O tutorial do Kauê dava a mesma tarefa a ele; a duplicidade foi removida |
| P7 | Sprint 4 relâmpago, se sobrar tempo | Gatilho na sexta 30 à noite (A28); pergunta incluída na mensagem ao professor |

**Resposta do professor (27/10, noite)** — texto em `resposta-professor.md`:

| Ponto | O que o professor disse | O que foi feito |
|---|---|---|
| Adendo | Aceito; descrever alterações, justificativas e critérios; explicitar o cálculo do lucro | Rascunho em `sprint-3-adendo-termo-de-aceite.md` (A33), com a fórmula do lucro e os custos considerados e não considerados, conferidos no código |
| Reposição e dashboard | Obrigatórios primeiro | Lista de cortes refeita: saem antes o botão da A12, os filtros e o ranking; **não** saem o bloco da receita máxima estimada nem o detalhe por linha |
| Licença | Não considerar autorizada sem confirmação expressa da Gurobi; planejar alternativa | Documentação oficial lida; pedido à Gurobi pronto; plano B a decidir (`sprint-3-licenca-gurobi.md`; A30, A31). **Risco elevado para Alto** |
| Datas | 30/11 é a data-limite; backup e restauração testados | Backup e restauração **validados** em containers locais (validação 39); procedimento no Kauê §12.6; banco definitivo no sábado **à noite** (A32) |
| Sprint extra | Pode, se não prejudicar a qualidade | Gatilho da A28 endurecido: só com o obrigatório testado e estável |
| Licença (complemento) | — | O e-mail de aprovação da Gurobi ao Kauê mostra uma **WLS acadêmica válida até 25/09/2027** (A34), o que corrige a A22. A cláusula "para uma única pessoa (não um processo que roda em nome de usuários diferentes)" mantém a dúvida sobre o Render: a pergunta à Gurobi passou a ser sobre essa licença, enviada pelo Kauê. O Compute Server não é necessário |
| Plano B da licença | — | **Decidido pela equipe: B1** (a B2 foi explicada e descartada por dar mais trabalho). Implementado e validado (validações 40–43); CT72 |
| — | — | **Lacuna nova:** o Termo aceita "Must + Should" e cita alertas no critério de pronto, mas US15/US16 estão fora. Adendo, item 4: decisão da equipe |

**Segunda rodada de 27/10:**

| Pendência | Resposta da equipe | O que foi feito |
|---|---|---|
| A10 | "O Kauê confirmou que você pode alterar o contrato oficial" | **Aplicado** em `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md` (revisão de 27/10 na seção 3). Formatos conferidos contra o código e os exemplos testados; caminhos antigos do §0 corrigidos. Não commitado |
| A15 | "Dei merge; está certo?" | **Conferido (A29):** o PR #25 está certo e manteve a correção do `.env`. O PR #26 levou os documentos para o `sprint-3`, e o PR #27 os levou para a `main`, que manteve a correção do `.env`. Nenhuma senha real subiu. Um `git reset --hard` às 17:19 apagou as alterações não commitadas (contrato e 7 documentos); foram refeitas a partir desta conversa |
| A11 | Fica com o Diogo | Registrado |
| A20 | A "A18" era a A20 | Registrado |
| P7 | Manter a Sprint 4 relâmpago; talvez estender os testes | Mantida; a pergunta extra da mensagem agora pede também a extensão da janela de testes |

### A.1 Situação depois da v5.1

**Todos os Must do Termo de Aceite estão na Sprint 3, e todo o código que eles exigem está escrito e testado em `docs/Sprint_3/referencia/`.**

| Parte | O que está pronto | Testes |
|---|---|---|
| Backend | Completo: US1–US7, US9–US12, US14, US17, US18 e o descarte por linha (A12, 27/10) e o plano B1 da licença (A31) | **95** (27/10), com PostgreSQL 16 real e o Python real |
| Python | Produção e reposição | **47** |
| Banco | Schema novo, seed de demonstração, consultas 1–13 | **32 + 41**, também no Docker real |
| Deploy | `Dockerfile`, *blueprint*, *workflow* do Pages | Imagem rodada com os limites do Render free, fluxo completo sem falhas |
| Frontend | Funções de apoio das telas novas e `config.js` de produção | **15/15** (27/10) |

O trabalho da equipe até 01/11 passa a ser:
- **copiar, entender, rodar os testes de cada fase, abrir os PRs e fazer o aceite**;
- **desenhar as seis telas novas**;
- **publicar** (Render e GitHub Pages).

**Os maiores riscos agora:**
1. **A carga do Leonardo:** seis telas em cinco dias.
2. **O tempo** para fazer chegar ao GitHub, com PR revisado, o que está pronto fora dele.
3. **Duas confirmações com o professor:** o uso da licença de avaliação do Gurobi no servidor (A22) e a data da avaliação diante do banco free que expira em 30 dias (A21). A mensagem está pronta (27/10).
4. **Render só no sábado** (A21, 27/10): sem o ensaio na quarta ou quinta, os problemas do ambiente real aparecem um dia antes do fim.

### A.2 Respostas aos comentários da noite

| Comentário | Resposta |
|---|---|
| "Consegue adicionar essas quatro histórias…?" | **Feito.** Decisão A2b. As quatro estão no roteiro (Fases Q/5, 6, 7 e Front 3), nos tutoriais, no contrato (§1.6, §2.7–§2.9), nos casos de teste (CT51–CT70) e na matriz. O código está pronto e testado |
| Docker: "se quiser pode testar novamente" | **Causa encontrada:** *sockets* inacessíveis em `%LOCALAPPDATA%\Docker\run` e `\docker-secrets-engine`. Fechar a janela de erro não foi a causa: o Docker recriou o problema a cada início. **Contorno verificado:** renomear as duas pastas (A26). Com o Docker funcionando, o banco foi revalidado no Docker real e os 53 testes de integração rodaram com Testcontainers |
| "Não compilou porque?" | **Não havia código completo para compilar.** O tutorial da tarde trazia pontos de partida: trechos, um método escrito como passos em comentário, repositórios só como assinaturas. Agora o código completo existe e **foi aplicado fase por fase sobre o backend exato da `main`**, rodando os testes de cada fase na ordem do tutorial (§E, item 24) |
| Licença WLS indisponível | Registrado (A22). Desenvolvimento, testes e produção usam a licença de avaliação do `pip`, que basta para os modelos do projeto. Falta confirmar com o professor o uso no servidor público |
| Playwright com o Leonardo | Mantido (A13) |

### A.3 O que a auditoria da tarde encontrou (continua valendo)

Os materiais de 14/10 tinham conteúdo técnico majoritariamente correto, mas não estavam prontos para orientar a execução:
- o backend do tutorial não compilava e, corrigido isso, não subia; depois, `400` virava `500` (A4, A5, A7);
- os quatro tutoriais apontavam para pastas inexistentes;
- o teste do Python pelo PowerShell falhava (BOM, A8);
- o calendário expirou; nada da Sprint 3 chegou ao GitHub desde 14/10;
- havia contradições pontuais (A6, A11, a `RegraAcai` dada como pronta).

---

## B. Inventário e cobertura

### B.1 Fontes examinadas

| Grupo | Fontes | Como |
|---|---|---|
| Escopo acordado | Backlog, Documento de Visão, Termo de Aceite (`.docx`); `Diagramas_UML.md`; `DER.md`; Roteiro do Protótipo; roteiro v4.6 (inclusive Fase Q, Fases 5 e 6, Apêndice B) | Leitura completa. Os `.docx` dos artefatos têm **o mesmo hash MD5** dos da `main` |
| Estado real | Repositório local e remoto; `backend/`, `frontend/`, `optimization/`, `bd/` em `origin/main` (`ac028cd`) | Leitura e **execução** |
| Histórico | Relatórios, atas, evidências e contribuições das Sprints 1 e 2 | Leitura; classificação por evidência |
| Orientação (os artefatos) | **53 arquivos** em `Downloads\Artefatos` + o roteiro v4.6 + os 2 tutoriais da Sprint 1 em PDF | Leitura completa; **execução** dos executáveis |
| Arquitetura e contratos | `Arquitetura.md`, `Contrato_de_Comunicacao`, `mapa_constraints.md`, `sql_repositorios.md` | Leitura e comparação com o código |
| **Documentação externa (noite)** | Render: `blueprint-spec`, `free`, `docker`, `health-checks`, `deploys`, `infrastructure-as-code`, `postgresql-creating-connecting` (lidas em 26/10); página do `gurobipy` 13.0.3 no PyPI; GitHub Docs ("configuring a publishing source"); últimas versões das *actions* do GitHub Pages | Leitura dos documentos (não de resumos) |

### B.2 Não acessível ou limitado

| Fonte | Limitação | Efeito |
|---|---|---|
| Protótipo no Figma | Sem acesso | Usado o texto do Roteiro do Protótipo (Telas 03, 13 e 14 inclusive) |
| Evidências da Sprint 2 no Google Drive | Sem acesso | Execuções das Sprints 1 e 2 classificadas como "não verificáveis" |
| Comentários de PR e *issues* | O `gh` não está instalado | Não dá para afirmar quem revisou cada PR |
| Histórico do Git anterior a 27/09 | Perdido | — |
| Licença WLS | **Indisponível** (confirmado pelo usuário) | Tudo com a licença de avaliação do `pip` (A22) |
| Contas do Render e do GitHub Pages da equipe | Sem acesso (e fora do que posso fazer: criar contas) | O deploy foi **simulado localmente** com os limites do plano free; a publicação real é do Kauê |
| Tutoriais da Sprint 1 em PDF | Código cortado no próprio PDF | Usados como modelo de didática |

---

## C. Problemas identificados

### C.1 Da tarde (1–22)

| # | Problema | Evidência | Gravidade | Correção |
|---|---|---|---|---|
| 1 | O projeto não compilava com o `GlobalExceptionHandler` do tutorial | `package org.postgresql.util does not exist` | **Crítica** | A4 |
| 2 | A aplicação não subia com o `SecurityConfig` do tutorial | `NoUniqueBeanDefinitionException … found 2` | **Crítica** | A5 |
| 3 | Requisição inválida devolvia `500` | Teste esperava 400, recebeu 500 | Alta | A7 |
| 4 | Quatro Must sem sprint | Roteiro v4.6 × prazo × Termo | **Crítica** | **Resolvido à noite (A2b)** |
| 5 | Caminhos inexistentes nos tutoriais | Busca em Downloads e em 19 zips | Alta | `docs/Sprint_3/referencia/` |
| 6 | Calendário expirado; nada no GitHub desde 14/10 | Último commit `ac028cd` | Alta | Roteiro v5/v5.1 |
| 7 | Contrato v2 inexistente; CORS sem `PATCH`; `401` sem mensagem | Diff | Alta | Proposta (A10), agora igual ao código |
| 8 | Teste do Python pelo PowerShell falhava (BOM) | Bytes `EF BB BF` | Alta | A8 |
| 9 | "Cancelar pedido devolve o estoque" | Contra a decisão de 14/10 | Média | A6 |
| 10 | Compute Server × código | Arquitetura × `otimizador` v2 | Média | **Deixa de bloquear (A22)** |
| 11 | Datas em UTC no Render | Revisão de SQL | Média | A9, **validada no Java** |
| 12 | `RegraAcai` dada como pronta | `sql_repositorios.md` | Média | Classe e 13 testes em `referencia/` |
| 13 | `RelogioConfig` sem `package` | Não compila | Baixa | Arquivo completo |
| 14 | Pacotes de teste do Boot 4 imprecisos | Maven Central | Média | Corrigidos |
| 15 | Conversão do `requirements.txt` podia esvaziá-lo | Semântica do PowerShell | Média | Comando com parênteses |
| 16 | WLS acadêmica: 2 sessões | Documentação do Gurobi | Média | Aviso (vale se a WLS chegar) |
| 17 | `$args` na função `Chamar` | Semântica do PowerShell | Baixa | `$p` |
| 18 | Referências cruzadas e CTs com dois usos | Leitura cruzada | Baixa | Refeitos |
| 19 | Lacunas da Fase 3 | Contrato §2.2 | Média | Implementadas (atalho de descarte opcional, A12) |
| 20 | Playwright instável | 4 de 8 (original) e 3 de 8 (dividido) | Média | A13 (Leonardo) |
| 21 | Branch `origin/sprint-3` com o `.env` antigo | Git | ~~Média~~ **Baixa** (27/10) | A15. **Correção em 27/10:** a v5.1 dizia que um merge reverteria o PR #24. Simulado com `git merge-tree` (nada alterado): o merge mantém a correção e só acrescenta `backend/env.example` |
| 22 | Contradições históricas das Sprints 1 e 2 | Leitura cruzada | Baixa | Registrar; não reescrever |

### C.2 Da noite (23–33)

| # | Problema | Fonte | Evidência | Gravidade | Correção |
|---|---|---|---|---|---|
| 23 | **BD01: o Docker Desktop trava ao iniciar** | Esta máquina | *"rename …\Docker\run\sailor-ingest.sock … The file cannot be accessed by the system"*; depois o mesmo em `docker-secrets-engine`. Os arquivos não podem ser apagados (nem pelo `cmd`); a pasta pode ser renomeada | Alta (sem Docker, não há Testcontainers) | A26: contorno verificado (motor respondeu em 5 s); **"Reset to factory defaults" apagaria o `mente-fria-db`** |
| 24 | As rotas simuladas do teste da Fase 1 derrubariam os testes de integração | `RotasDeTesteController` × controllers reais | "Mapeamento ambíguo" no `@SpringBootTest` | Alta (a suíte não subiria na Fase 2) | A24: perfil `teste-seguranca` |
| 25 | Testes de integração com nome `*IT` não rodariam | Surefire padrão | Só roda `*Test`, `*Tests`, `Test*` | Média | A25 |
| 26 | **A ordem de cópia por fase não fechava:** a base dos testes de integração usa classes das Fases 4A e 6 | `IntegracaoBase` | Não compilaria na Fase 2 | Média | Quatro arquivos de infraestrutura copiados já na Fase 2 (tutorial do Lucas, §3.1). **Verificado fase por fase** |
| 27 | **O v4.6 tirava a "receita máxima estimada" do dashboard, mas o Termo assinado a exige** | Termo (escopo e critério de pronto) × v4.6 Fase 6 | Leitura cruzada | Média (aceite) | A16 (aprovada em 27/10): ler do último cálculo, sem Python |
| 28 | "Ingredientes mais utilizados" por quantidade comparariam kg com unidades | Desenho da Tela 03 | O copo (ingrediente 7, em unidades) passaria o açaí | Baixa | A17 (aprovada em 27/10): ordenar pelo custo |
| 29 | **O frontend desistiria da primeira chamada depois de o Render "dormir"** | `config.js` (15 s) × Render free (~1 min para acordar) | Documentação do Render | Média | `config.js` de produção com 90 s (CT70) |
| 30 | **O banco free do Render expira 30 dias depois de criado** | Documentação do Render | `free.md` | Média (avaliação depois da expiração) | A21: anotar a data; perguntar ao professor |
| 31 | **O GitHub Pages por branch não publica a pasta `frontend/`** | Pages: só raiz ou `/docs` | Documentação do GitHub ("configuring a publishing source", lida em 26/10) | Média | Publicação por Actions (*workflow* em `referencia/deploy/`) |
| 32 | Licença do Gurobi em produção sem WLS | Usuário; PyPI ("trial license … limited size") | — | Média | A22: licença de avaliação; confirmar com o professor |
| 33 | O seed de demonstração usa o relógio do banco (UTC) | `seed_demo.sql` | Os pedidos do mês atual ficam nas primeiras horas do dia 1 | Baixa | Aviso no tutorial do Diogo, §8 |

---

## D. Correções realizadas (todas em `docs/Sprint_3/`, sem commit)

**Em 27/10 (v5.2):** `sprint-3-mensagem-professor.md` (**novo**); A12 no backend de `referencia/` (`MovimentacaoRepository`, `EstoqueService`, `EstoqueController` e um teste em `Fase3EstoquePedidoTest`); `CatalogoIdeias` com 18 ideias e `MarketingTest` ajustado; os 9 documentos atualizados com as respostas da equipe (§A.0). Cópia anterior em `historico/v5.1-27-10-antes-das-respostas/`.

| Arquivo | Novo ou alterado | O que e por quê |
|---|---|---|
| `sprint-3-roteiro.md` | **v5.2** | Escopo A2b; Fases Q/5, 6, 7 e Front 3; deploy definitivo no sábado (A21) e gatilhos da A28; riscos e critérios de pronto com a URL publicada |
| `sprint-3-tutorial-backend-lucas.md` | **Reescrito** | Fases 1–4A, 6 e deploy com **código validado para copiar por fase**; o que ler em cada classe; o teste que prova cada regra |
| `sprint-3-tutorial-banco-diogo.md` | Atualizado | Docker validado; contorno do BD01; consultas 9–13 com os números do seed; repositórios prontos; mensagem nova ao professor |
| `sprint-3-tutorial-python-kaue.md` | Atualizado | **§11** Fase Q e Fase 5 (modelo, exemplo à mão, testes, experimentos, Java); **§12** publicação no Render; licença (A22) |
| `sprint-3-tutorial-frontend-leonardo.md` | Atualizado | **§6.4–§6.6** Telas 03, 13 e 14 com as funções de apoio; **§9** GitHub Pages |
| `sprint-3-decisoes-e-pendencias.md` | Atualizado | A2b; A3 encerrada; **A16–A27**; pendências abertas |
| `sprint-3-contrato-v2-proposta.md` | Atualizado | §1.5–§1.6 (licença, reposição Java↔Python), §2.7–§2.9 (reposição, dashboard, marketing), mensagens de erro novas. **Igual ao código** |
| `sprint-3-casos-de-teste-propostos.md` | Atualizado | **CT51–CT72**; coluna "Validado em 26/10" com o teste que prova cada caso |
| `sprint-3-matriz-rastreabilidade.md` | Atualizado | US14, US17, US18 e US20; estados comprovados da noite |
| `referencia/backend/` | **Completo** | 49 classes Java, 12 classes de teste, `pom.xml` e `pom.xml.diff` (aplica sobre a `main`), `application.properties` |
| `referencia/python/` | Acrescentado | `app/reposicao.py`, `tests/oraculo_reposicao.py`, `tests/test_reposicao.py`, exemplos `entrada_`/`saida_reposicao_demo.json` |
| `referencia/bd/consultas_sprint3.sql` | Acrescentado | Consultas 9–13 (reposição e dashboard) |
| `referencia/deploy/` | **Novo** | `Dockerfile`, `.dockerignore`, `render.yaml`, `frontend-pages.yml`, `config.js` |
| `referencia/frontend/` | Acrescentado | `js/apoio_telas_novas.js`, `ferramentas/testar_apoio_telas.mjs` |
| `historico/v5-26-10-antes-das-4-historias/` | Novo | Os 10 documentos da tarde, intactos |

**Não alterado:** `bd/`, `backend/`, `frontend/`, `optimization/`, os documentos oficiais e os originais em Downloads. Fora do repositório, só no Docker Desktop desta máquina: as pastas `run` e `docker-secrets-engine` foram **renomeadas** (não apagadas) para contornar o BD01.

---

## E. Validações executadas

Todas em cópias no diretório temporário da sessão, fora do repositório.

| # | Validação | Resultado | Classificação |
|---|---|---|---|
| 1 | Python da `main` (baseline) | `8 passed` | **Aprovado** |
| 2 | Python v2 (produção) | `25 passed` com `gurobipy` 13.0.1 **e** 13.0.3 | **Aprovado** |
| 3 | Quebrar de propósito (produção) | 5 e 5 falhas | **Aprovado** |
| 4 | Exemplos via subprocesso × `saida_*.json` | Iguais | **Aprovado** |
| 5 | 151/1/0 e 66/0 pelo oráculo de força bruta | Iguais | **Aprovado** |
| 6 | Pipe do PowerShell | Falhava (BOM) → corrigido (A8) | **Aprovado com ressalva** |
| 7 | `git apply --check` do patch do DDL | Aplica; resultado = DDL de referência | **Aprovado** (embutido e Docker) |
| 8 | Banco: 32 na `main`; 32 + 41 no schema novo | `32 \| 0`; `32 \| 0`; `41 \| 0` | **Aprovado** (PostgreSQL 16.10 embutido **e 16.15 no Docker**) |
| 9 | Experimentos A e B | `37 \| 4` e `38 \| 3` | **Aprovado** (nos dois) |
| 10 | `seed_demo.sql` | Valores conferidos; recusa a 2ª execução; invariante 0 | **Aprovado** (nos dois) |
| 11 | FEFO (S1–S5) e concorrência | Conferem; 30/30 e 30/30 | **Aprovado** (nos dois) |
| 12 | Metadados de erro do PostgreSQL | 23502 com tabela e coluna | **Aprovado** |
| 13 | Backend Fase 1 do tutorial de 14/10 | Não compila → não sobe → 16 de 18 | **Reprovado** (problemas 1–3) |
| 14 | Backend Fase 1 corrigido | 18/18 | **Aprovado** |
| 15 | `dependency:tree` | Jackson 3.1.5 e 2.21.5 coexistindo | **Aprovado** |
| 16 | Frontend: retratos antes e depois da divisão | 25 de 25 `IGUAIS` | **Aprovado** |
| 17 | Guarda antigo × novo | Tabela reproduzida | **Aprovado** |
| 18 | Playwright (22) | Intermitente, original e dividido | **Aprovado com ressalva** (A13) |
| **19** | **Python: reposição** | `47 passed` (25 + 22) com 13.0.1 e 13.0.3; igual ao oráculo em 150 instâncias; exemplo à mão (19,95) | **Aprovado** |
| **20** | Quebrar de propósito (reposição) | Arredondar para baixo: 2 falhas; sem estágio 3: 1; sem estágio 2: 9 | **Aprovado** |
| **21** | **Backend completo** | **91 testes**, 0 falhas: 18 MockMvc, 13 unitários, 53 de integração (PostgreSQL 16 por Testcontainers), 7 com o Python real | **Aprovado** |
| **22** | Quebrar de propósito (backend) | Venda com vencido: S1 e S4 falham; saldo total ao Python: CT29 falha; cancelado no dashboard: `DashboardTest` falha; cancelado fora da reposição: `ReposicaoTest` falha. Restaurado: 91/91 | **Aprovado** |
| **23** | Ponta a ponta com o **Gurobi real** pelo Java | 151/1/0 em **188 ms**; reposição 63,22 e falta de 10,71 (contas à mão); eco `AÇAÍ (POLPA)` no perfil `dev` | **Aprovado** |
| **24** | **O tutorial do Lucas aplicado fase por fase** sobre o backend exato da `main` (`git archive`) | Fase 1: 18; Fase 2: 15 + 18 + 13; Fase 3: 14; 4A: 7, e 2 + 4 com o Python real; Fase 5: 8; Fase 6: 5 + 4. **Sem `MF_PYTHON`, 91 rodam com 7 pulados** e nenhuma falha. Resultado final idêntico a `referencia/` | **Aprovado** |
| **25** | Consultas 9–13 no Docker sobre o seed de demonstração | Mês atual 326,40; anterior 271,50; mais vendidos e ranking conferidos; consumo de 60 dias igual ao exemplo do Python | **Aprovado** |
| **26** | **Imagem de deploy** (`docker build`) e simulação do Render free (512 MB, 0,1 CPU, rede com PostgreSQL 16) | Build em 74 s (587 MB); sobe em **83 s**; **14/14** verificações do fluxo; pico **133 MiB** (`memory.peak`); sem OOM; sem `ERROR` no log; usuário sem privilégio; `/api/teste/python` → `401`; CORS do Pages ok | **Aprovado** |
| **27** | Carregar o DDL num banco remoto com `psql` em container | Exit 0; acentos preservados (`Açaí (polpa)`) | **Aprovado** (contra PostgreSQL local pela rede; no Render, não) |
| **28** | Frontend: funções de apoio e `config.js` de produção (Node) | **14/14** | **Aprovado** |
| **29** | `render.yaml` e *workflow* do Pages | Sintaxe ok; região e banco coerentes; *actions* nas últimas majors | **Aprovado com ressalva** (não executados no Render nem no GitHub) |
| 30 | Licença WLS | Indisponível | **Bloqueado**: substituída pela licença de avaliação (A22) |
| 31 | Python 3.14.7 | Não testado | **Não validado** |
| 32 | Telas novas (10/16, 11/17, 12, 03, 13, 14) | Não existem | **Não validado** (trabalho do Leonardo) |
| 33 | Publicação real (Render e Pages) | Depende das contas da equipe | **Não validado** (CT68–CT70 no sábado) |
| **34** | **Descarte por linha (A12), 27/10** | `descarteDeUmaLinhaVencidaZeraSoEssaLinha`: `201`, só a linha zera, invariante ok; `422` (repetido e não vencido); `404` (inexistente e `SAIDA`); `401`. **Suíte completa: 92 testes, 0 falhas**, com `MF_PYTHON` | **Aprovado** |
| **35** | Quebrar de propósito (A12) | Aceitar linha não vencida: o teste falha ("expected: 422 but was: 201"); código restaurado e conferido com `diff` | **Aprovado** |
| **36** | Catálogo com 18 ideias (A20) | `MarketingTest` 4/4: rodízio do dia 301 continua na ideia 3; 18 títulos distintos | **Aprovado** |
| **37** | A15: merge do `sprint-3` na `main` (simulação) | `git merge-tree`: resultado = `main` + `backend/env.example`; o `application.properties` mantém `./.env` | **Aprovado** (corrige a v5.1) |
| **38** | Regras do Render free (A21) | Documentação lida em 27/10: um banco gratuito ativo por conta; expira em 30 dias, mais 14 de carência; não diz se é possível criar outro depois; 750 horas gratuitas por mês | **Aprovado**, com a lacuna anotada |
| **39** | **Backup e restauração** (A32, 27/10) | Containers temporários PostgreSQL 16: DDL + 2 usuários + `seed_demo.sql` → `pg_dump -Fc` (22 KB) → `pg_restore --no-owner --no-privileges` num banco novo, com outro dono: exit 0; retrato **igual** (7 tabelas, soma dos saldos 358, 0 violações, `Açaí (polpa)`); novo cadastro com id 3 sem colisão. Containers removidos | **Aprovado** localmente; contra o Render, **não validado** |
| **40** | **Plano B1 da licença** (A31, 27/10): `ChaveOtimizador`, `OtimizadorDesligadoTest` | 3 testes novos; suíte completa **95/95** com o Python real | **Aprovado** |
| **41** | Quebrar de propósito (A31) | Sem a chave na produção: "expected: 503 but was: 500"; código restaurado e conferido com `diff` | **Aprovado** |
| **42** | Tutorial do Lucas fase por fase com a `ChaveOtimizador` na Fase 4A e o teste novo na Fase 6 | Todas as fases passam; sem `MF_PYTHON`, as classes do Python real são puladas; o código final é igual ao de `referencia/` | **Aprovado** |
| **43** | Funções de apoio (texto do plano com a otimização desligada) e `render.yaml` com `OTIMIZADOR_HABILITADO` | **15/15**; YAML lido sem erro | **Aprovado**; Render e telas reais **não validados** |

**Skills aplicadas:**

| Skill | Como foi usada |
|---|---|
| `springboot-security` | Revisão da segurança; divergências registradas |
| `springboot-tdd` | 91 testes no ciclo vermelho → verde; Testcontainers; **sem** meta de 80% e **sem** JPA |
| `311-frameworks-spring-jdbc` | Repositórios: `Optional` × `queryForObject`; `RETURNING`; nulos tipados; `@Transactional` só onde não há Python; `JdbcClient` × D4 registrado |
| `sql-code-review` | A9; índices; `FOR UPDATE`; consultas 9–13 (parâmetros nomeados, `HAVING`, `CAST` dos opcionais) |
| `test-scenarios` | CT30–CT72 |
| `diagnosing-bugs` | A13 (tarde); à noite, o método "ciclo que fica vermelho" guiou os experimentos de quebrar de propósito |
| `render-docker` | `Dockerfile` multi-estágio, `.dockerignore`, *blueprint*; documentação atual do Render lida antes de escrever, como a skill exige |
| `render-deploy` | **Não** usada: o deploy real exige as contas da equipe; o passo a passo está no tutorial do Kauê |
| `intended-vs-implemented` | **Não** aplicada como auditoria formal. O contrato e o código foram mantidos iguais por construção e conferidos pelos testes; a skill continua recomendada para a janela de testes (documentos oficiais × o que for para a `main`) |
| `user-stories` | **Não** usada: o texto das histórias muda depois do aviso ao professor (P3) |

---

## F. Pendências e decisões necessárias

| # | Pendência | Responsável | Próxima ação | Prazo |
|---|---|---|---|---|
| A10 | Contrato v2 (**aprovado em 27/10**) | Kauê | Aplicar ao oficial | **Ter 27/10** |
| P3 | Aviso ao professor | Diogo (envia) + Lucas (revisa) | Enviar `sprint-3-mensagem-professor.md`; guardar a resposta | **Ter 27/10** |
| A21 | Render: quem cria a conta definitiva; ensaio | Lucas (PO) decide; Kauê executa | Ensaio opcional (qua/qui); **definitivo no sáb 31** | Quem: **Qui 29/10** |
| A28 | Gatilhos de corte (A12) e de Sprint 4 relâmpago | Lucas (PO) | Conferir | **Sex 30/10, à noite** |
| P2 | Versão do Gurobi (**13.0.3**, 27/10) | Kauê + Diogo | Aplicar nos arquivos | Ter 27/10 |
| A13 | Playwright instável | Leonardo | §7.1 do tutorial dele | Sáb 31/10 |
| A15 | Branch `sprint-3` | Kauê | Levar os comentários do `env.example`; apagar o branch | Ter 27/10 |
| A26 | BD01 | Diogo | Registrar a causa e o contorno nas evidências | Sex 30/10 |
| — | Executar os tutoriais e registrar evidências | Todos | Um PR por fase, no dia | Até Dom 01/11 |

---

## G. Lista priorizada para retomar o desenvolvimento

Ordem: primeiro o que **destrava os outros**.

**Lucas (caminho crítico; agora copia e revisa, não escreve)**
1. Fase 1 de `referencia/backend/` + `pom.xml.diff`: 18 testes. PR **terça**.
2. Mensagem ao professor com o Diogo (P3), **terça**.
3. Fase 2 (+ os 4 arquivos de infraestrutura): 15 + 13 testes. PR **quarta** → avisar o Leonardo.
4. Ensaio de deploy com o Kauê, se a equipe fizer (quarta ou quinta); decidir quem cria a conta definitiva (quinta).
5. Fase 3 (quinta) → Fase 4A com o Kauê (quinta e sexta) → **Fase 6** (sexta); **conferência da A28** na sexta à noite.
6. Sábado: deploy definitivo com o Kauê; revisar PRs; Tela 14 se o Leonardo precisar; CT68 na URL.

**Diogo**
1. Docker funcionando (A26, se voltar).
2. **PR do banco** (DDL + 41 testes), **terça**: a Fase 2 depende dele.
3. Enviar a mensagem ao professor (`sprint-3-mensagem-professor.md`), terça.
4. Repositórios (terça e quarta), inclusive **reposição** e **dashboard**; `seed_demo.sql`; `sql_repositorios_sprint3.md` com as consultas 1–13.
5. Documentos e CTs do banco e do dashboard no Plano de Testes (até sábado); BD01 nas evidências.

**Kauê**
1. Aplicar o Contrato v2 (aprovado) ao oficial e a P2 (13.0.3), terça.
2. `.env.example` com os comentários do `env.example`; apagar o branch `sprint-3` (terça).
3. **PR da Fase P + Q** (47 testes; 2ª máquina) — **quarta**.
4. Ensaio de deploy numa conta pessoal, apagado no mesmo dia (quarta ou quinta, opcional).
5. Fase 4A com o Lucas (quinta); **Fase 5** (Java da reposição), sexta.
6. CTs de Python, reposição e deploy no Plano (até sexta); **deploy definitivo** e CT67–CT70 (sábado de manhã).
7. CT20, registro do Git, Review, Retro e relatório (sábado e domingo).

**Leonardo**
1. Baseline e divisão do `app.js`; guarda (terça).
2. `config.js` de produção e *workflow* do Pages com o Kauê (quarta).
3. Estoque e Pedido com API simulada (quarta); **Gate da Fase 2** (quinta de manhã); com o backend real (quinta e sexta).
4. Tela 12, **Tela 03** e o botão "Descartar" (A12, primeiro corte) (sexta); **Telas 13 e 14** (sábado).
5. A13 (até sábado); aceite na URL publicada (domingo).

**Bloqueios que precisam ser resolvidos para os quatro avançarem juntos:**
1. **A10** (Contrato v2), terça;
2. **PR do banco** e **Fase 1** na `main`, terça;
3. **Deploy definitivo**, sábado 31 de manhã: sem ele, a janela de testes começa sem ambiente publicado (o ensaio da quarta ou quinta reduz o risco);
4. **Docker funcionando** em quem roda os testes de integração (A26).
