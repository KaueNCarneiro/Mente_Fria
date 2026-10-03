# Evidências de Teste — Sprint 2 — Mente Fria

> **[PREENCHER]** Nomes exatos dos testes, links de PR e prints/logs. Marcado **[CONFIRMAR]** onde o mapeamento para os CTxx precisa ser verificado.

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| — | Python: exemplo do Contrato §1.1 (esperado: produto 1 = 66, produto 2 = 0) | Unitário (pytest) | Passou (30/09/2026) | `optimization/test_otimizador.py` — **[PREENCHER: nome do teste, log/print]** |
| — | Python: cenários de estoque escasso | Unitário (pytest) | Passou (30/09/2026) | idem — **[PREENCHER]** |
| CT23 / CT24 | Um ingrediente escasso limita a produção; dois produtos disputam o mesmo ingrediente | Unitário (pytest) | **[CONFIRMAR]** se estão entre os 8 testes | **[PREENCHER]** |
| CT29 | Ingrediente vencido não conta como estoque | Unitário | **[CONFIRMAR]** — pela decisão de 25/09 o desconto é feito no **Java**, não no Python; ainda não implementado | — |
| — | Demais testes do módulo Python (total: 8) | Unitário (pytest) | Passaram (30/09/2026) | **[PREENCHER: listar os 8 por nome]** |
| — | Java lê o banco (`GET /api/saude`, `GET /api/ingredientes`) | Manual | **[CONFIRMAR]** funcionando | **[PREENCHER: print]** |
| — | Banco criado a partir do `Script_DDL.sql` (schema + seed) | Manual | **[CONFIRMAR]** | **[PREENCHER: print de `\dt`/consulta]** |
| — | Java → Python (subprocesso) conforme o contrato | Integração | **Não executado** | Bloqueado: Java ainda não aplica o contrato e as credenciais WLS estão só no PC do Kauê |

> Falhas corrigidas dentro da sprint (registrar os dois estados): **[PREENCHER]** — por exemplo, se algum teste falhou antes de passar em 30/09.

## Cobertura automatizada nesta sprint
8 testes do módulo Python passando localmente em 30/09/2026 (com licença WLS). **[CONFIRMAR]** se há CI (GitHub Actions) no repositório novo; no plano, o CI ainda está "a definir". Cobertura de código não medida. Testes de integração (Testcontainers) e de endpoints ainda não existem.

## Próximos testes sugeridos (baratos, já que o DDL está pronto)
Constraints do banco com Testcontainers: CT10, CT11, CT14, CT17, CT21 e CT26.


---

## Complemento — evidências por parte do projeto

Este complemento mantém integralmente o registro anterior e acrescenta a análise das **9 imagens** da [pasta Testes no Google Drive](https://drive.google.com/drive/folders/1Zp7NgvweLd3mYiolAfWsry7y6eznXWXD): 2 de banco de dados, 4 de backend, 1 de Python e 2 de frontend. As descrições abaixo se baseiam no conteúdo visível das imagens; não representam uma nova execução dos testes.

Os identificadores **BD**, **BE**, **PY** e **FE** são referências internas deste complemento. Os códigos **CTxx** são usados apenas quando o cenário pode ser relacionado ao plano de testes. Datas de envio ao Drive não foram usadas como datas de execução. As marcações anteriores **[PREENCHER]**, **[CONFIRMAR]** e os estados históricos foram preservados; as confirmações e limitações adicionais estão registradas abaixo.

### 1. Banco de dados

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| BD01 | Inicializar o Docker Desktop para disponibilizar o ambiente do banco de dados. | Manual — infraestrutura | **Bloqueado no registro apresentado.** O Docker exibe `Virtualization support not detected` e `Engine stopped`: a inicialização falhou porque o suporte à virtualização não foi detectado. A imagem não mostra um teste SQL. | [Teste 1 de Banco de Dados.jpeg](https://drive.google.com/file/d/13UF9tTo_KNI49RBFIvjvFu5MS--ON3-Z/view) |
| BD02 | Executar o contêiner PostgreSQL e verificar se o serviço está pronto para receber conexões. | Manual — infraestrutura | **Passou na verificação de disponibilidade.** O Docker mostra o contêiner `mente-fria-db`, imagem `postgres:16`, mapeamento `5432:5432`, estado `Running` e `Engine running`. O log registra `database system is ready to accept connections`. | [Teste 2 de Banco de Dados.jpeg](https://drive.google.com/file/d/1I89SPRDHxLQSNGfWBNQsjP3Oj0OeikrT/view) |

**Descrição e alcance:** BD01 documenta um impedimento no ambiente. BD02 mostra um estado operacional do banco, com PostgreSQL 16.15 e reinicialização registrada nos logs. A linha de prontidão está datada de **03/10/2026 às 01:13:00 UTC**, equivalente a **02/10/2026 às 22:13:00 em São Paulo**. A imagem também informa que o diretório já contém um banco e que a inicialização foi ignorada (`Skipping initialization`). Portanto, ela **não comprova a execução do `Script_DDL.sql`, a criação de todas as tabelas nem a aplicação integral do seed**.

As duas capturas registram indisponibilidade e disponibilidade, mas não mostram o procedimento que resolveu a virtualização. **[CONFIRMAR]** se pertencem à mesma máquina e à sequência de correção do mesmo incidente. Para encerrar a pendência do schema + seed na tabela original, acrescentar saída de `\dt`, consultas às tabelas ou log de execução do script. Não há evidência de execução das constraints CT10, CT11, CT14, CT17, CT21 e CT26 nessas imagens.

### 2. Backend Java

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| BE01 | Acessar `GET http://localhost:8080/api/saude` pelo navegador e verificar o resultado do serviço e do banco. | Manual — endpoint de saúde | **Passou quanto ao conteúdo da resposta.** O navegador apresenta JSON com `banco: "ok"` e `status: "ok"`, conforme o contrato. O código HTTP não está exposto na captura. | [Teste 1 Back.jpeg](https://drive.google.com/file/d/1a1xB3lEegGWVJ0ZorWbba5Kgt4s6Y75Q/view) |
| BE02 | Acessar `GET http://localhost:8080/api/ingredientes` e verificar a listagem resumida de ingredientes. | Manual — consulta de endpoint | **Passou quanto à listagem resumida exibida.** A resposta contém 7 objetos, com os campos `id`, `nome` e `unidade_medida`. A captura não exibe o código HTTP nem os campos completos do cadastro de ingredientes. | [Teste 2 Back.jpeg](https://drive.google.com/file/d/1HtlD1hgV5XRjBbx1VqTjE-XBIyaf38-l/view) |
| BE03 | Registrar a falha observada ao consultar `/api/ingredientes`. | Manual — registro de falha | **Falhou nessa execução.** O navegador apresenta `Whitelabel Error Page`, `Internal Server Error` e `status=500`, em **02/10/2026 às 22:07:13 GMT-03:00**. A causa da exceção não aparece na imagem. | [Teste 3 Back.jpeg](https://drive.google.com/file/d/1MvqL0uyU7oYux90NUQ4Avhwy7_AoAlEG/view) |
| BE04 | Verificar a inicialização da aplicação Java e inspecionar a implementação da consulta de ingredientes. | Manual — inicialização + inspeção de código | **Inicialização concluída; consulta identificada no código.** O console informa Tomcat na porta `8080` e `Started BackendApplication in 3.862 seconds`, em **02/10/2026 às 22:20:25 GMT-03:00**. O método `listar()` usa `JdbcTemplate` e seleciona `id`, `nome` e `unidade_medida` da tabela `ingrediente`, ordenando por `id`. A presença do método não equivale, isoladamente, a um teste executado da consulta. | [Teste 4 Back.jpg](https://drive.google.com/file/d/1oll-0j_71C7dxOA4grE1Vn0Y48GgOe2c/view) |

**Dados observados em BE02:** os IDs 1 a 7 correspondem a Açaí (polpa), Leite condensado, Granola, Banana, Morango, Leite em pó e Copo descartável 500ml. A resposta mostra as unidades `kg`, `l` e `un`, conforme cada ingrediente. Isso evidencia a leitura de uma lista de dados pelo endpoint; não comprova todos os cadastros ou relacionamentos do banco.

**Relação com o registro original:** BE01 e BE02 acrescentam evidência visual para a linha “Java lê o banco”. BE04 mostra a consulta SQL usada para a listagem. BE03 deve permanecer registrado como falha observada: embora exista uma captura com listagem bem-sucedida, BE02 não possui horário visível suficiente para afirmar a ordem entre essas duas execuções ou atribuir uma correção específica. **[CONFIRMAR]** a causa do erro 500, a alteração aplicada e a reexecução correspondente.

**Limites:** acessar a API diretamente pelo navegador não testa a liberação CORS para o frontend. As imagens não comprovam autenticação, cadastro de usuários, operações de escrita, testes automatizados de endpoints ou a integração Java → Python. O erro HTML de BE03 também não demonstra o tratamento JSON padronizado `{mensagem, campos}` previsto no contrato.

### 3. Python — otimizador

**Evidência comum:** [Teste Python.jpeg](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view). A captura identifica o arquivo relativo `tests/test_otimizador.py`, mostra os oito nomes abaixo com resultado `PASSED` e o resumo **`8 passed in 3.20s`**. Ela não mostra os dados de entrada, as asserções, a data da execução nem a configuração da licença. A data **30/09/2026** e o uso de WLS permanecem como informações do registro original, sem confirmação adicional pelo print.

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| PY01 | Executar o cenário de exemplo do contrato. O registro original informa como esperado produto 1 = 66 e produto 2 = 0; os valores não estão expostos na captura. | Unitário (pytest), conforme classificação original | **Passou.** | `tests/test_otimizador.py::test_exemplo_do_contrato` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |
| PY02 / CT23 | Verificar o cenário em que um ingrediente escasso limita a produção de um único produto. | Unitário (pytest), conforme classificação original | **Passou.** O nome do teste corresponde ao cenário CT23; os valores e as asserções devem ser conferidos no código para validar a cobertura detalhada. | `tests/test_otimizador.py::test_ingrediente_escasso_limita_produto_unico` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |
| PY03 / CT24 | Verificar o cenário de dois produtos disputando o mesmo ingrediente. | Unitário (pytest), conforme classificação original | **Passou.** O nome do teste corresponde ao cenário CT24; o print não apresenta os cálculos de consumo. | `tests/test_otimizador.py::test_dois_produtos_disputam_mesmo_ingrediente` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |
| PY04 | Verificar o cenário de estoque zerado, com retorno de sucesso e quantidades zeradas, conforme o nome do teste. | Unitário (pytest), conforme classificação original | **Passou.** | `tests/test_otimizador.py::test_estoque_zerado_retorna_sucesso_com_zeros` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |
| PY05 | Verificar o tratamento de uma lista de produtos vazia. | Unitário (pytest), conforme classificação original | **Passou.** O conteúdo exato da resposta esperada não está visível. | `tests/test_otimizador.py::test_lista_de_produtos_vazia` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |
| PY06 | Verificar o tratamento de produto sem composição. | Unitário (pytest), conforme classificação original | **Passou.** O conteúdo exato da resposta esperada não está visível. | `tests/test_otimizador.py::test_produto_sem_composicao` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |
| PY07 | Verificar o tratamento de composição que cita ingrediente ausente. | Unitário (pytest), conforme classificação original | **Passou.** | `tests/test_otimizador.py::test_composicao_cita_ingrediente_ausente` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |
| PY08 | Verificar o tratamento de número inválido. | Unitário (pytest), conforme classificação original | **Passou.** O valor inválido utilizado não aparece no print. | `tests/test_otimizador.py::test_numero_invalido` — [print Python](https://drive.google.com/file/d/1CqufDzxerELyUt0HxdZLTF754NlXHQA9/view) |

**Relação com as pendências anteriores:** a imagem permite listar os oito testes e confirmar que os cenários nomeados de CT23 e CT24 estão entre eles. Ela não comprova CT29 (desconto de ingredientes vencidos no Java), integração por subprocesso, execução no CI ou percentual de cobertura de código. A classificação “unitário” foi mantida do documento original; a captura sozinha não permite avaliar o isolamento do solver e de outras dependências.

### 4. Frontend

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| FE01 | Exibir a tela “Criar conta de Administrador”, com nome completo, e-mail, senha, confirmação e botão de envio. | Manual — inspeção visual | **Renderização observada.** Os campos e o botão “Criar conta e entrar” aparecem na imagem, com as senhas mascaradas. A captura isolada não comprova responsividade, navegação ou validações de cada campo. | [Teste Pagina de Cadastro.png](https://drive.google.com/file/d/1moRdp3PL9wONMWohVu2kRBFcW4yG2Yuj/view) |
| FE02 | Observar o estado apresentado pela tela de cadastro quando a conexão com a API falha. | Manual — tratamento de falha de comunicação | **Mensagem de erro exibida; cadastro não comprovado.** A tela informa que não foi possível conectar à API em `http://localhost:8080`. A mensagem menciona a origem `file://` e orienta verificar o backend e a origem permitida. | [Teste Pagina de Cadastro.png](https://drive.google.com/file/d/1moRdp3PL9wONMWohVu2kRBFcW4yG2Yuj/view) |
| FE03 / CT06–CT07 | Verificar se há evidência de login válido ou rejeição de credenciais inválidas. | Revisão da evidência fornecida | **Não comprovado pelas imagens.** O arquivo nomeado como teste de login mostra a mesma tela de cadastro com erro de conexão, sem tela de login, retorno de autenticação ou redirecionamento por perfil. | [Teste Pagina de Login.png](https://drive.google.com/file/d/1EK5CLvAB4sv8h5k8ZbBhhdsg9oJCFk6G/view) |

**Duplicidade identificada:** os arquivos “Teste Pagina de Cadastro.png” e “Teste Pagina de Login.png” têm conteúdo idêntico, confirmado também pela comparação SHA-256 dos arquivos baixados. São duas referências de arquivo para a mesma captura, não duas execuções independentes comprovadas.

**Diagnóstico limitado à evidência:** a mensagem mostra uma tentativa sem conexão bem-sucedida e cita `file://`, mas não identifica a causa exata. Não é possível concluir somente pelo print que houve bloqueio CORS, backend desligado ou erro específico de endpoint. Não há painel Network, status HTTP ou log da requisição nessa captura. A exibição da mensagem é observável; sucesso do cadastro (CT01), rejeição por senha curta (CT02), e-mail duplicado (CT03) e autenticação não estão demonstrados.

**Próxima verificação recomendada:** servir o frontend por HTTP em `http://127.0.0.1:5500` ou `http://localhost:5500`, conforme o contrato, e repetir os fluxos quando a API estiver disponível. Acrescentar capturas distintas de cadastro e login, acompanhadas do resultado da requisição, sem expor senhas ou tokens. A pasta de frontend não contém, nas imagens analisadas, relatório de testes automatizados; por isso nenhum total de testes de navegador foi atribuído a esta evidência.

### 5. Consolidação e pendências após análise das imagens

| Parte do projeto | O que as evidências acrescentam | O que permanece sem comprovação nessas imagens |
|---|---|---|
| Banco de dados | Registro de bloqueio de virtualização e registro do PostgreSQL pronto para conexões. | Procedimento de correção; execução integral do DDL/seed; testes de constraints e transações. |
| Backend | JSON de saúde, lista resumida de 7 ingredientes, uma ocorrência de erro 500 e log de inicialização concluída. | Causa e correção do erro 500; CORS entre frontend e API; autenticação; escrita; testes automatizados; subprocesso Python. |
| Python | Oito testes identificados nominalmente e aprovados; duração total de 3,20 segundos. | Asserções e dados de entrada completos; data visível de execução; configuração de licença; CI; cobertura de código; CT29 e integração com Java. |
| Frontend | Tela de cadastro renderizada e mensagem de falha de conexão exibida. | Cadastro concluído; login; validações por campo; fluxo integrado; responsividade; relatório automatizado. O print denominado “Login” é duplicado. |

**Registro de falhas e retomadas:** conservar BD01 (virtualização) e BE03 (HTTP 500) como falhas observadas. BD02 registra disponibilidade do banco e BE02 registra uma listagem bem-sucedida, mas as imagens não documentam a sequência completa nem o procedimento de correção. FE02 permanece como falha de comunicação sem captura de reexecução bem-sucedida. Nenhuma falha anterior dos oito testes Python está visível.

**Atualização de leitura da cobertura:** os prints acrescentam verificações **manuais** de endpoints e infraestrutura ao texto original. A existência dessas verificações não equivale à existência de testes automatizados de endpoints ou Testcontainers. O único relatório de execução automatizada apresentado nas imagens é o do Python, com **8 testes aprovados**. Não há elementos para alterar a pendência de CI, afirmar cobertura percentual ou marcar a integração Java → Python como concluída.
