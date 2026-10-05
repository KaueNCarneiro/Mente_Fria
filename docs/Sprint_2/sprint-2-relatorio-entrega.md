# Relatório de Entrega — Sprint 2 — Mente Fria

> **Base desta revisão:** estado da `main` até **02/10/2026**, horário de Brasília, commit [`d562fe71f34948951ac9f772ef565e4683eacbe3`](https://github.com/KaueNCarneiro/Mente_Fria/tree/d562fe71f34948951ac9f772ef565e4683eacbe3), e informações confirmadas pela equipe. A reorganização da documentação publicada em 03/10 não é contabilizada como entrega desta sprint.

* Período: início **[18/09/2026]**; corte das entregas em **02/10/2026**.

* Sprint Review: não realizada, conforme confirmação da equipe.

* Retrospectiva: não realizada, conforme confirmação da equipe.

* Observação do rascunho: a E4 não foi corrigida a tempo pelo professor; por isso as Sprints 1 e 2 seriam entregues na mesma semana.

**Contexto do método:** walking skeleton (Sprint 1 = comunicação; Sprint 2 = CRUD; Sprint 3 = lógica matemática e regras de negócio; Sprint 4 = bugs e deploy). A infraestrutura e a comunicação consumiram parte das duas sprints. Até o corte, há **CRUD parcialmente implementado no frontend**, mas autenticação, escrita no backend e integração completa continuam pendentes.

## 1. Planejado vs. entregue

### 1.1 Histórias do backlog (E2)

As marcações de planejamento abaixo preservam o plano informado no rascunho. Entrega parcial de uma camada não representa conclusão da história nem aceite de ponta a ponta.

| História (E2) | Planejada para esta sprint? | Entregue? | Observação até 02/10 |
|---|---|---|---|
| #1–#3 Conta, cadastro de funcionários e login por perfil | Sim (plano walking skeleton: CRUD/autenticação) | Parcial — frontend | Telas, chamadas, sessão JWT e controle de acesso da interface implementados; autenticação/autorização e endpoints correspondentes ausentes no backend |
| #4 Cadastro de ingredientes | Sim (plano walking skeleton) | Parcial — frontend e base de banco | Tela de cadastro/edição e validações locais presentes; DDL disponível; Java lista apenas `id`, `nome`, `unidade_medida`, sem cadastro/edição nem todos os campos exigidos pela interface |
| #5 Produtos e composição | Sim (plano walking skeleton) | Parcial — frontend e base de banco | Tela de cadastro/composição, conversão de porções e verificação de açaí presentes; tabela e constraints no DDL; endpoints Java pendentes |
| #6 Preço de venda | Sim (plano walking skeleton) | Parcial — frontend e base de banco | Interface de alteração de preço e validação local presentes; endpoint de escrita pendente |
| #7 Validação de dados | Sim (plano walking skeleton) | Parcial | Constraints no DDL e validações de interface presentes; validação e tratamento de erros no backend e aceite integrado pendentes |
| #8 Repositório Git com README | Sim (backlog original) | Concluído | Repositório recriado em 27/09, README e pastas por camada presentes; PRs #1 e #2 integradas |
| #9 Movimentação de estoque | Sim (backlog original) | Não | Não implementada no código do corte |
| #10 Cálculo da produção recomendada (Programação Linear) | Sim (backlog original) | Parcial — Python | Módulo e oito testes presentes; testes aprovados em 30/09 com WLS no PC do Kauê, conforme confirmação da equipe; chamada Java e `POST /api/producao/recomendacao` pendentes |
| #11 Visualizar recomendação de vendas | Sim (backlog original) | Não | Depende da integração de #10; tela funcional de recomendação ausente |
| #12 Registro de pedido | Sim (backlog original) | Não | Não implementado no código do corte |
| #13 Cadastro de clientes | Sim (backlog original) | Não | Não implementado no código do corte |
| #14 Recomendação de reposição | Sim (backlog original) | Não | Não implementada no código do corte |

### 1.2 Tarefas técnicas do walking skeleton (fora do backlog E2)


| Tarefa técnica | Responsável informado | Situação | Observação |
|---|---|---|---|
| T9 — Banco de dados conforme o `Script_DDL.sql` | Diogo | Artefato presente; execução relatada | DDL, constraints e seed disponíveis; banco pronto conforme relato da equipe; sem nova execução nesta revisão |
| T10 — Comunicação Java ↔ banco | Lucas | Implementada; funcionamento relatado | `GET /api/saude` e `GET /api/ingredientes` presentes; não equivale a CRUD completo |
| T11 — Módulo Python conforme o contrato, com oito testes | Kauê | Implementado e execução confirmada | `optimization/app/otimizador.py` e `optimization/tests/test_otimizador.py`; PR #1 integrada em 01/10; testes aprovados em 30/09 no PC do Kauê |
| T12 — Licença Gurobi WLS obtida e usada localmente | Kauê | Uso local confirmado; reprodução pendente | Obtenção em 29/09 conforme rascunho **[CONFIRMAR data]**; uso em 30/09 confirmado. O script não configura explicitamente as três variáveis WLS citadas na documentação; registrar a configuração local efetivamente usada |
| T13 — Frontend preparado para se comunicar com Java | Leonardo | Implementado; integração pendente | Telas de cadastro/login, funcionários, ingredientes, produtos/composição, preço, validações e testes com respostas simuladas; PR #2 integrada em 02/10 |
| T14 — Java aplicar o contrato de otimização e chamar Python | Lucas | Pendente | Não há integração por subprocesso no Java do corte; o dublê previsto na arquitetura permanece uma proposta, não uma entrega |
| T15 — Exclusão e recriação do repositório | Kauê | Realizada em 27/09, conforme confirmação da equipe | Contexto relatado: conflitos de estrutura/comunicação Java ↔ banco; impacto sobre rastreabilidade na seção 6 |
| T16 — Base da documentação de entrega das sprints | Kauê | Rascunhos disponíveis fora do repositório | Estes documentos são arquivos de trabalho; a publicação/reorganização de documentação na PR #3 ocorreu em 03/10 e fica fora do corte |
| T17 — Hello World completo BD → Java → Python → Frontend | Equipe | Não concluído | Falta integração Java → Python; tela de comunicação existe, mas `/api/teste/python` não está implementado no backend |
| Login JWT no backend e deploy do esqueleto | Equipe | Pendentes | Interface preparada para JWT; implementação no backend e deploy não entregues no corte |

## 2. Incremento demonstrável

Não há evidência de funcionalidade de negócio aceita de ponta a ponta. Os artefatos permitem demonstração por camada, respeitando as limitações abaixo:

- **Banco:** schema, constraints e seed em `docs/Script_DDL.sql`; funcionamento local informado pela equipe.
- **Java ↔ banco:** código de `GET /api/saude` (consulta `SELECT 1`) e `GET /api/ingredientes` (resumo com `id`, `nome`, `unidade_medida`). Funcionamento relatado; não houve nova execução de integração nesta revisão.
- **Python/Gurobi:** entrada JSON por `stdin` e saída JSON por `stdout`; oito testes aprovados em 30/09 no PC do Kauê usando WLS, conforme confirmação da equipe. O exemplo do contrato espera 66 unidades do produto 1, zero do produto 2 e receita estimada de R$ 851,40.
- **Frontend:** telas e validações de cadastro/login, funcionários, ingredientes, produtos/composição e preço; chamadas de API e testes com respostas simuladas. A demonstração desses fluxos depende de respostas simuladas enquanto os endpoints reais correspondentes estiverem ausentes.

**Ainda não demonstrável de ponta a ponta:** autenticação e CRUD com banco real, cálculo/visualização de recomendação e cadeia completa das quatro camadas.

Vídeo/GIF: evidências em imagens, organizadas por camada:
- Banco de dados: [Teste 1 de Banco de Dados.jpeg](https://drive.google.com/file/d/13UF9tTo_KNI49RBFIvjvFu5MS--ON3-Z/view), [Teste 2 de Banco de Dados.jpeg](https://drive.google.com/file/d/1I89SPRDHxLQSNGfWBNQsjP3Oj0OeikrT/view), [Teste 3 — listagem das tabelas](https://drive.google.com/file/d/1GbY0kp3f9gvNBBH0KoxavGrtetFiGTZO/view), [Teste 4 — constraints: 32 aprovados e zero falhas (capturas unidas)](https://drive.google.com/file/d/1dBYaSYY-_fug8Ouj4kX_tbDzAJILJwqn/view)
- Backend: [Teste 1 Back.jpeg](https://drive.google.com/file/d/1a1xB3lEegGWVJ0ZorWbba5Kgt4s6Y75Q/view), [Teste 2 Back.jpeg](https://drive.google.com/file/d/1HtlD1hgV5XRjBbx1VqTjE-XBIyaf38-l/view), [Teste 3 Back.jpeg](https://drive.google.com/file/d/1MvqL0uyU7oYux90NUQ4Avhwy7_AoAlEG/view), [Teste 4 Back.jpg](https://drive.google.com/file/d/1oll-0j_71C7dxOA4grE1Vn0Y48GgOe2c/view).
- Python/Gurobi: [Teste Python.jpeg](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view).
- Frontend: [Teste Pagina de Cadastro.png](https://drive.google.com/file/d/1moRdp3PL9wONMWohVu2kRBFcW4yG2Yuj/view), [Teste Pagina de Login.png](https://drive.google.com/file/d/1EK5CLvAB4sv8h5k8ZbBhhdsg9oJCFk6G/view).

**Como reproduzir os artefatos do corte:**

1. Consultar o [snapshot de 02/10](https://github.com/KaueNCarneiro/Mente_Fria/tree/d562fe71f34948951ac9f772ef565e4683eacbe3), `README.md`, `docs/Sprint_1/sprint1-roteiro.md` e `frontend/README.md`.
2. Banco: seguir o README para carregar `docs/Script_DDL.sql` no PostgreSQL.
3. Backend: conferir o ambiente Java 25/Spring Boot 4.0.8 declarado no `backend/pom.xml`, as propriedades de conexão e executar pelo Maven conforme o ambiente da equipe. A documentação de arquitetura ainda registra versões diferentes.
4. Python: instalar `optimization/requirements.txt`, configurar a licença conforme o ambiente validado pelo Kauê e executar `python -m pytest optimization/tests`.
5. Frontend: servir `frontend` por HTTP na porta 5500; configurar a base da API em `frontend/js/config.js`. Para testes com respostas simuladas, seguir as instruções de `npm install` e `npm test` em `frontend/README.md`.

Os caminhos acima são os do corte de 02/10. A estrutura de documentação publicada em 03/10 tem caminhos diferentes.

## 3. Backlog atualizado

`docs/Sprint_2/sprint-2-backlog.md`

**Mudanças a refletir no board:** US1–US7 têm implementação parcial de interface/base de banco; backend e integração continuam pendentes. US10 tem módulo Python isolado e testes aprovados conforme relato confirmado. Não marcar essas histórias como concluídas sem os critérios de aceite integrados.

**Replanejamento necessário:** implementar autenticação/autorização, endpoints de escrita e integração das telas existentes; integrar Java → Python; executar aceite com PostgreSQL real; planejar US9 e US11–US14 ainda não implementadas. Datas, prioridades e responsáveis dependem de decisão da equipe.

## 4. Evidências de teste

| Evidência | O que está confirmado | Limite da evidência |
|---|---|---|
| `optimization/tests/test_otimizador.py` | Oito testes presentes; equipe confirmou aprovação em 30/09 com WLS no PC do Kauê | Não foram reexecutados nesta revisão; testes exercitam o script por subprocesso, sem Java nem banco |
| `frontend/tests/browser.cjs` | Testes de navegador com respostas simuladas presentes | Não comprovam integração com API/PostgreSQL reais|
| `docs/constraints_sprint1_teste.sql` | Script de teste de constraints disponível no corte | Concluído|
| Backend | Código de teste de contexto Spring presente | Sem evidência apresentada de suíte completa de integração/aceite da Sprint 2 |

**Registro complementar:** `docs/Sprint_2/sprint-2-evidencias-teste.md`.

## 5. Retrospectiva e contribuição individual

- Ata de retrospectiva: `docs/Sprint_2/sprint-2-retrospectiva.md`
- Relatórios individuais de contribuição:`docs/Sprint_2`

## 6. Riscos e impedimentos para a próxima sprint

- **Integração Java → Python:** pendente. Avaliar o dublê previsto na arquitetura e implementar a chamada real por subprocesso conforme o contrato; a escolha e os responsáveis precisam ser deliberados.
- **Reprodução do ambiente WLS:** testes aprovados apenas no ambiente do Kauê, conforme confirmação.
- **Backend e aceite do CRUD:** telas existentes não substituem autenticação/autorização no servidor, endpoints de escrita, validação de dados e aceite com banco real.
- **Cronograma:** backend, integração, histórias ainda não implementadas, regras de negócio, dashboard e deploy exigem reestimativa para Sprints 3 e 4, aproveitando o frontend já produzido.
- **Histórico do Git:** recriação em 27/09/2026 confirmada pela equipe. Documentar a perda relatada de histórico anterior e anexar prints/clones preservados, se existirem. PR #1 (Python) e PR #2 (frontend) estão disponíveis no repositório atual.
- **Deploy do esqueleto:** pendente no corte; validar configuração de ambiente e integração antes da publicação.
