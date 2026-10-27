# Decisões e pendências — Sprint 3 — Mente Fria

**Revisão:** 26/10/2026 (auditoria). **Substitui** `historico/Registro_de_Decisoes_e_Pendencias_Sprint3.md` (14/10), que fica preservado.
**Como usar:**
- Antes de executar um passo marcado com "Por quê?" nos tutoriais, confira aqui a decisão correspondente.
- Ao fechar uma pendência, mova a linha para a seção 1, com a data e o motivo.
- Nunca apague uma decisão. Se ela mudar, acrescente uma linha nova explicando a mudança.

**Legenda de identificadores (todos já existentes no projeto, exceto onde dito):**
- `F1–F12`: pontos da análise de 14/10.
- `D1–D22`: respostas da equipe de 13/10 (roteiro v4.6, §9).
- `P2–P7`: pendências de 14/10.
- **`A1–A12`: novos**, criados nesta auditoria, com o prefixo "A" para não colidir com os oficiais.

---

## 1. Decisões em vigor

### 1.1 Datas e escopo (usuário, 26/10/2026)

| # | Decisão | Origem |
|---|---|---|
| A1 | A **Sprint 3 vai até 01/11/2026** e a **janela de testes e correções vai de 01/11 a 06/11/2026**. O calendário do v4.6 (núcleo 16/10, extensão 18/10, Sprint 4 até 30/10) **deixa de valer** | Decisão do usuário em 26/10 |
| A2 | Escopo da Sprint 3: **US1–US7, US9, US10, US11, US12 e as mudanças do banco**. US13–US20, US26 e US27 ficam **sinalizadas** e não entram nos tutoriais. Ver o bloqueio **A3** | Decisão do usuário em 26/10 |

### 1.2 Decisões de 14/10, revalidadas em 26/10

As que foram executadas trazem o resultado.

| # | Decisão | Situação em 26/10 |
|---|---|---|
| F1 | CHECKs estritos para `quantidade_restante` e `motivo` (`IS NOT NULL` explícito; `<>` em vez do domínio do tipo) | **Validada em execução:** 41/41 com o DDL corrigido. Com os CHECKs literais do roteiro, 37/41: T55, T62, T64 e T66 falham. É a prova de que a correção era necessária |
| F2 / D11 | Seed oficial com **estoque 0**; estoque de demonstração só em `seed_demo.sql` | **Validada:** seed oficial com 7 ingredientes e soma de estoque 0,000 |
| F3 | `hoje` calculado no Java (`America/Sao_Paulo`) e enviado como parâmetro `:hoje`; nunca `CURRENT_DATE` | Mantida e **ampliada pela A9** |
| F4 | `/api/teste/python` só existe com `@Profile("dev")` | **Validada no MockMvc:** fora do perfil dev, a rota responde `401` |
| F5 | JWT com `jjwt` 0.13.0 | **Validada:** compila e roda no Boot 4.0.8. O `jjwt-jackson` traz o Jackson 2.21.5, que convive com o Jackson 3.1.5 do Boot. **Não é conflito** |
| F6 | Gurobi com `MIPGap = 0`, `MIPGapAbs = 0` e verificação em inteiros | **Validada:** 25 testes, 200 instâncias contra o oráculo |
| F7 | Assinaturas dos repositórios combinadas antes de codar: o Diogo cria os `*Repository`, o Lucas os controllers e serviços | Mantida |
| F8 | Números esperados da demonstração: plano **151/1/0**, lucro **1.011,71**, receita **1.966,80** | **Validada de forma independente:** o oráculo de força bruta, sem solver, deu o mesmo plano |
| F9 | Receita → lucro altera o Termo aprovado: **avisar o professor antes** de editar | Mantida. **Nada foi editado ainda:** os `.docx` são idênticos aos da `main` |
| F10 | US9: "saída de produtos" passa a ser "baixa de **ingredientes**" | Mantida (documento ainda não alterado) |
| F11 | O `consumo` (Java↔Python) lista só os ingredientes com consumo maior que zero | **Validada** |
| D10 / 14/10 | Estoque por **linhas**; pedido e saída convencional **nunca** consomem linha vencida; descarte só consome linha vencida | **Validada:** cenários S1–S5 e concorrência 30/30 |
| 14/10 | **Cancelar pedido não devolve estoque** (US27); devolver à linha de origem é a US27b, fora do projeto | Mantida. A auditoria **corrigiu** dois textos que diziam o contrário (A6) |

### 1.3 Decisões técnicas desta auditoria

São correções necessárias para o código funcionar e **não alteram** arquitetura nem requisitos.

| # | Decisão | Por quê | Quem |
|---|---|---|---|
| A4 | Tirar `<scope>runtime</scope>` do driver PostgreSQL no `pom.xml` | O `GlobalExceptionHandler` importa `org.postgresql.util.PSQLException`. Com `scope runtime`, o projeto **não compila** (verificado com `mvnw test`) | Lucas |
| A5 | Declarar o bean de CORS como **`UrlBasedCorsConfigurationSource`** (não `CorsConfigurationSource`) e receber esse tipo no `filterChain` | Com o tipo genérico, o Spring encontra 2 beans (`corsConfigurationSource` e `mvcHandlerMappingIntrospector`) e **a aplicação não sobe** (`NoUniqueBeanDefinitionException`, verificado) | Lucas |
| A6 | Corrigir os textos "cancelar devolve o estoque" (comentário do DDL e "Depois" do tutorial do backend) | Contradiziam a decisão de 14/10 | Feito em `referencia/` e nos tutoriais |
| A7 | `MethodArgumentTypeMismatchException` e `MissingServletRequestParameterException` → **400** `{"mensagem":"Requisição inválida."}`; `AccessDeniedException` → **403** em JSON | O handler genérico os transformava em **500** (verificado: o teste esperava 400 e recebeu 500) | Lucas |
| A8 | O `otimizador.py` lê a entrada com **`utf-8-sig`** | O pipe do PowerShell envia um BOM antes do JSON e o módulo respondia "JSON de entrada inválido" (verificado). Novo teste `test_entrada_com_bom_e_aceita` | Kauê |
| A9 | `pedido.data_pedido` e `movimentacao_estoque.data_movimentacao` recebem a hora do Java (`LocalDateTime.now(clock)`, `America/Sao_Paulo`) como parâmetro `:agora`, em vez de depender do `DEFAULT CURRENT_TIMESTAMP` | Mesmo motivo da F3: o banco do Render usa UTC, então um pedido às 22h de 31/10 seria gravado em 01/11 e cairia no mês errado do dashboard (US17). O `DEFAULT` continua como rede de segurança | Lucas (Java) e Diogo (SQL) |

---

## 2. Bloqueios e decisões abertas

| # | Pendência | Dono | Prazo | Recomendação |
|---|---|---|---|---|
| **A3** | **US14, US17, US18 (Must) e US20 (deploy, Must)** ficaram sem sprint. O Termo de Aceite (28/08) aceita Must + Should, e a janela de testes de 01–06/11 foi pensada sobre o **sistema publicado** | **Lucas (PO) com o professor** | **27/10** | Levar ao professor três opções: (a) aceitar a entrega sem essas histórias, registrando um adendo ao Termo; (b) incluir no mínimo o deploy (US20) entre 30/10 e 01/11, porque a janela de testes depende dele; (c) usar parte da janela de testes para desenvolvimento, o que contraria a regra "só correções" (roteiro v4.6, §4.4). **Sem essa decisão, o plano de testes de 01–06/11 não fecha** |
| A10 | O **Contrato v2** (Java↔Python e REST de estoque, pedidos e recomendação) existe só como proposta (`sprint-3-contrato-v2-proposta.md`) | Kauê, com Lucas e Leonardo | **27/10** | Aprovar a proposta (ou ajustar) e aplicá-la ao `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`. Backend e frontend **não implementam** estoque, pedido e recomendação antes disso |
| A11 | Compute Server **no desenvolvimento**: a Arquitetura (§2.1, §3.3, decisão de 02/10) e o Plano de Testes (§4.2) dizem que cada desenvolvedor roda o próprio Compute Server local. O `otimizador` v2 **não lê** `GRB_COMPUTESERVER`; lê só as credenciais WLS diretas | Kauê e Lucas | 27/10 | Usar **WLS direta também no desenvolvimento** (mesmo caminho da produção; a Arquitetura §2.1 já diz que a mesma licença serve aos dois modos) e registrar na Arquitetura. Se quiserem manter o Compute Server, o `_criar_env` precisa ganhar o ramo `ComputeServer` e um teste |
| A12 | Atalho `POST /api/estoque/linhas/{id}/descarte` (roteiro v4.6, Fase 3) e `GET /api/estoque/{id}/linhas` (detalhe da Tela 10) | Lucas (PO) | 29/10 | `GET .../linhas` entra na extensão (o Diogo já tem o SQL). O atalho de descarte fica **opcional**: o descarte já é possível pela movimentação com `motivo = DESCARTE_VENCIDO` |
| P2 | Versão do `gurobipy`: 13.0.1 (Arquitetura) × 13.0.3 (`requirements.txt`) | Kauê e Diogo | 27/10 | **As duas passaram nos 25 testes** (Python 3.14.0). Escolher uma e alinhar os três arquivos. Sugestão: **13.0.3**, a que já está no `requirements.txt` (menos mudança), atualizando a Arquitetura e o README |
| P3 | Avisar o professor de receita → lucro e da reposição com orçamento | Diogo e Lucas | 27/10, junto com a A3 | Uma mensagem só, com A3, F9 e P3 |
| P5 | Licença WLS do Lucas e Python 3.14.7 | Lucas e Kauê | Quando a licença chegar | Os testes rodaram com a licença limitada do `pip`. A WLS acadêmica permite **2 sessões simultâneas** (token de 5 min): **não** rode a suíte inteira com as variáveis `GRB_*` definidas |
| P6 | Medir o timeout de 10 s | Kauê e Lucas | 30/10 | Fase 4A |
| A13 | O teste de navegador (`frontend/tests/browser.cjs`) é **instável**: falhou de forma intermitente nas verificações 11, 20 e 21, **tanto no código original da `main` (4 de 8 rodadas ok) quanto no dividido (3 de 8)** | Leonardo | Antes de 01/11 | Investigar com a skill `diagnosing-bugs` (ciclo de repetição), porque a janela de testes depende de testes confiáveis. **Não** é efeito da divisão do `app.js` |
| A14 | Licença acadêmica do Gurobi em produção: os termos dizem que **só o titular** usa a chave e **sem uso operacional** | Lucas | Antes do deploy | Registrar na Arquitetura que o deploy é uso acadêmico do próprio Lucas, para demonstração. Em caso de dúvida, consultar o professor |
| A15 | O branch `origin/sprint-3` tem `backend/env.example` (sem o ponto) e `spring.config.import=optional:file:./backend/.env`, **diferente** da `main`, que já está correta | Kauê | 27/10 | **Não** fazer merge desse branch na `main` sem revisar: ele reverteria a correção do PR #24. Sugestão: fechar o branch ou atualizá-lo a partir da `main` |
| F12 | Definição única de "consumo" (dashboard × reposição) | PO | Antes da US17 | Continua adiada (US17 fora da Sprint 3) |
| P7 | US13–US20, US26, US27 | Equipe | Depende da A3 | — |

---

## 3. Divergências entre skills e decisões do projeto

As decisões do projeto prevalecem.

| Skill | O que ela recomenda | O que o projeto faz | Motivo |
|---|---|---|---|
| `311-frameworks-spring-jdbc` | `JdbcClient` como API padrão | **`NamedParameterJdbcTemplate`** (decisão D4, Arquitetura §2) | Decisão registrada e já usada no `sql_repositorios.md`. Trocar agora gera retrabalho sem ganho para o prazo |
| `springboot-tdd` | Meta de **80%** de cobertura com JaCoCo | **Sem meta numérica** (Plano de Testes §2) | Decisão do projeto. Os testes são avaliados por requisito e risco |
| `springboot-tdd` | `@DataJpaTest`, `@MockBean` | Sem JPA; `@MockitoBean` | O projeto não usa JPA; o `@MockBean` foi removido no Boot 4 |
| `springboot-security` | Bean Validation (`@Valid`) | Validação **manual** que devolve todos os campos de uma vez | Decisão de 14/10 (mensagens fixas do `mapa_constraints.md`) |
| `springboot-security` | Filtro JWT com `@Component` | Filtro criado **dentro** do `SecurityConfig`, sem `@Component` | Evita registrar o filtro duas vezes |
| `springboot-security` | Rate limiting e cabeçalhos extras | **Não incluídos** (fora dos requisitos) | Ficam como recomendação, não como tarefa |
