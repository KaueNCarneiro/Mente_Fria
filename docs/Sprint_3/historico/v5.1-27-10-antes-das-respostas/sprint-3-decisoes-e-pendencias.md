# Decisões e pendências — Sprint 3 — Mente Fria

**Revisão:** 26/10/2026, noite (v5.1: entram US14, US17, US18 e US20). **Substitui** a versão da tarde (`historico/v5-26-10-antes-das-4-historias/`) e o registro de 14/10 (`historico/Registro_de_Decisoes_e_Pendencias_Sprint3.md`), ambos preservados.

**Como usar:**
- Antes de executar um passo marcado com "Por quê?" nos tutoriais, confira aqui a decisão correspondente.
- Ao fechar uma pendência, mova a linha para a seção 1, com a data e o motivo.
- Nunca apague uma decisão. Se ela mudar, acrescente uma linha nova explicando a mudança.

**Legenda de identificadores (todos já existentes no projeto, exceto onde dito):**
- `F1–F12`: pontos da análise de 14/10.
- `D1–D22`: respostas da equipe de 13/10 (roteiro v4.6, §9).
- `P2–P7`: pendências de 14/10.
- **`A1–A27`: novos**, criados nesta auditoria, com o prefixo "A" para não colidir com os oficiais.

**"Proposta"** = já está no código de `referencia/` e passou nos testes, mas o PO (Lucas) pode trocar. Trocar custa pouco: cada uma está isolada num método.

---

## 1. Decisões em vigor

### 1.1 Datas e escopo

| # | Decisão | Origem |
|---|---|---|
| A1 | A **Sprint 3 vai até 01/11/2026** e a **janela de testes e correções vai de 01/11 a 06/11/2026**. O calendário do v4.6 (núcleo 16/10, extensão 18/10, Sprint 4 até 30/10) **deixa de valer** | Usuário, 26/10 |
| A2 | Escopo (tarde de 26/10): US1–US7, US9, US10, US11, US12 e as mudanças do banco | Usuário, 26/10 — **ampliado pela A2b** |
| **A2b** | **Escopo final da Sprint 3: US1–US7, US9–US12, US14, US17, US18 e US20**, mais as mudanças do banco. Ficam **fora**: US13, US15, US16, US19, US26 e US27 (Could e Should). Todos os Must do Termo de Aceite estão dentro | Usuário, noite de 26/10 ("Consegue adicionar essas quatro histórias … dentro dessa Sprint?") |
| A3 | ~~US14, US17, US18 e US20 sem sprint~~ — **encerrada pela A2b**. Não há mais o que negociar de escopo com o professor; continua valendo **avisá-lo das mudanças de conteúdo** (P3: receita → lucro, reposição com orçamento, conteúdo do dashboard, licença em produção) | Encerrada em 26/10 |

### 1.2 Decisões de 14/10, revalidadas em 26/10

As que foram executadas trazem o resultado. "Java" = testes de integração do backend de `referencia/` (PostgreSQL 16 real via Testcontainers).

| # | Decisão | Situação em 26/10 |
|---|---|---|
| F1 | CHECKs estritos para `quantidade_restante` e `motivo` | **Validada** no PostgreSQL embutido **e no Docker real** (16.15): 41/41; com os CHECKs literais, 37/41 (T55, T62, T64, T66) |
| F2 / D11 | Seed oficial com **estoque 0**; estoque de demonstração só em `seed_demo.sql` | **Validada** (soma 0,000; seed de demonstração confere no Docker) |
| F3 | `hoje` calculado no Java (`America/Sao_Paulo`) e enviado como `:hoje` | **Validada no Java** (todas as consultas recebem `:hoje`; teste das 23h30) e **ampliada pela A9** |
| F4 | `/api/teste/python` só com `@Profile("dev")` | **Validada no Java:** fora do `dev` → `401`; no `dev`, o eco real devolve `AÇAÍ (POLPA)`; no container de produção → `401` |
| F5 | JWT com `jjwt` 0.13.0 | **Validada** (Jackson 2.21.5 convive com o Jackson 3.1.5) |
| F6 | Gurobi com `MIPGap = 0` e verificação em inteiros | **Validada** (25 testes de produção; 200 instâncias contra o oráculo) e **aplicada também à reposição** (A19) |
| F7 | O Diogo cria os `*Repository`, o Lucas os controllers e serviços | Mantida. As assinaturas agora são **código compilado** em `referencia/backend/` |
| F8 | Demonstração: **151/1/0**, lucro **1.011,71**, receita **1.966,80** | **Validada** pelo oráculo, pelo Java com o Python **real** (188 ms) e **dentro do container de deploy** (3,1 s com 0,1 CPU) |
| F9 | Receita → lucro altera o Termo: avisar o professor antes de editar | Mantida (P3). Nada foi editado nos `.docx` |
| F10 | US9: "saída de produtos" → "baixa de **ingredientes**" | Mantida (documento ainda não alterado) |
| F11 | `consumo` (Java↔Python) só com ingredientes de consumo > 0 | **Validada** |
| F12 | Definição única de "consumo" (dashboard × reposição) | **Resolvida pela A18** |
| D10 | Estoque por **linhas**; venda nunca consome vencido; descarte só consome vencido | **Validada** em SQL (S1–S5, 30/30) **e no Java** (S1–S5 e concorrência 5/5 pelo serviço real) |
| 14/10 | **Cancelar pedido não devolve estoque** (US27); US27b fora | Mantida |
| 14/10 (US14) | Período (dias) + orçamento; 8 ingredientes até 30 dias e 15 acima; consumo = pedidos × composição; **cancelados contam**; saldo **válido**; maximizar a **menor cobertura**; até 2 casas; tabela por custo de compra; `POST` | **Implementadas e validadas** (Python: 22 testes + oráculo; Java: 8 testes; ponta a ponta com o Gurobi real) |
| 14/10 (US17) | Dashboard = Tela 03; período = **mês** (padrão: o atual); ranking com Rank, Usuário, Qtd. de pedidos, Valor total e Tipos de produto; filtros Usuário, Tipo de produto e Valor (depois de somar); "tipo" = nome do produto; só pedidos `ATIVO` | **Implementadas e validadas** (5 testes de integração com conta à mão; seed de demonstração conferido) |
| 14/10 (US18) | Resultado da Tela 12 guardado **na memória do backend**; priorizado = quantidade > 0, maior primeiro, desempate pelo menor id; **até 3**; catálogo fixo de ideias; nada é publicado | **Implementadas e validadas** (4 testes) |

### 1.3 Decisões técnicas desta auditoria

São correções ou detalhes necessários para o código funcionar. **Não alteram** requisitos, salvo onde está escrito "Proposta".

| # | Decisão | Por quê | Quem |
|---|---|---|---|
| A4 | Tirar `<scope>runtime</scope>` do driver PostgreSQL | O `GlobalExceptionHandler` importa `PSQLException`; com `runtime`, **não compila** (verificado) | Lucas |
| A5 | Bean de CORS do tipo **`UrlBasedCorsConfigurationSource`** | Com o tipo genérico há 2 beans e **a aplicação não sobe** (verificado) | Lucas |
| A6 | Corrigir os textos "cancelar devolve o estoque" | Contradiziam a decisão de 14/10 | Feito |
| A7 | `MethodArgumentTypeMismatchException` e `MissingServletRequestParameterException` → `400`; `AccessDeniedException` → `403` em JSON | Viravam `500` (verificado) | Lucas |
| A8 | Módulos Python leem a entrada com **`utf-8-sig`** | O pipe do PowerShell envia BOM (verificado) | Kauê |
| A9 | `data_pedido` e `data_movimentacao` recebem `:agora` do Java (São Paulo) | O banco do Render usa UTC: um pedido às 22h de 31/10 cairia em novembro no dashboard. **Validado no Java** (pedido às 23h30 de 31/10 conta em outubro) | Lucas, Diogo |
| **A16** | **Proposta.** O dashboard mantém a **"receita máxima estimada"** e os **"produtos prioritários"** que o Termo de Aceite cita, lidos do **último cálculo de produção** guardado na memória (sem chamar o Python), mais a **situação do estoque** (quantos ingredientes estão no mínimo ou abaixo e quantos têm vencido). Sem cálculo, o bloco mostra "Gere a recomendação na tela Produção" | O v4.6 retirava a "receita máxima estimada" do dashboard, mas o Termo **assinado** a exige (critério de pronto e escopo). Assim o Termo é cumprido sem custo de processamento | Lucas (PO) confirma |
| **A17** | **Proposta.** "Ingredientes mais utilizados" vêm ordenados pelo **custo do que foi usado** (R$), mostrando também a quantidade e a unidade | Ordenar por quantidade compara kg com unidades (o copo sempre ganharia) | Lucas (PO) confirma |
| **A18** | **F12 resolvida.** Consumo = Σ `pedido_produto.quantidade` × `produto_ingrediente.quantidade_utilizada`, por data do pedido. A **reposição** conta todos os pedidos (inclusive cancelados, decisão de 14/10); o **dashboard** conta só `ATIVO`. Descartes e saídas convencionais **não** entram | Uma fórmula só, com o filtro de status documentado. A composição atual vale para o passado porque não existe rota para editar composição (se um dia existir, revisar) | — |
| **A19** | Detalhes da reposição (US14): período = **os últimos N dias contando hoje**; só entram ingredientes **com consumo > 0**; mínimo e necessidade arredondados **para cima** em centésimos; orçamento que não cobre os mínimos devolve **os mínimos e quanto falta** (`status: "sucesso"`, `orcamento_suficiente: false`); com folga, estágio 2 usa a sobra do orçamento e estágio 3 desempata pelo **mais consumido**; módulo separado `reposicao.py`, que reaproveita o ambiente Gurobi do `otimizador.py` | Eram os pontos "a formalizar" da Fase Q (v4.6). Conferidos contra um oráculo de força bruta em 150 instâncias | Kauê (revisa o modelo) |
| **A20** | **Proposta.** Marketing: a ideia de cada produto vem do catálogo por **rodízio a partir do dia do ano do cálculo** (sem repetir no mesmo resultado); cada sugestão mostra também o **lucro por copo** | "1ª ideia para o 1º produto" usaria sempre as mesmas 3 ideias das 12. O lucro por copo era a "proposta opcional" do v4.6 | Lucas (PO) confirma |
| **A21** | **Deploy (US20):** backend em **Docker no Render** (plano free, região Virginia), **PostgreSQL 16 free** no Render, frontend no **GitHub Pages por Actions**. Limites confirmados na documentação do Render em 26/10: **o Postgres free expira 30 dias depois de criado** (mais 14 dias de carência); o serviço free desliga após 15 min sem uso e leva ~1 min para voltar; 512 MB e 0,1 CPU. Por isso o frontend usa **tempo limite de 90 s** no Pages | Simulado na máquina (512 MB, 0,1 CPU): sobe em **83 s**; pico de memória **133 MiB**; fluxo completo sem falhas | Kauê (dono do repositório) e Lucas |
| **A22** | **Licença do Gurobi:** a WLS **não está disponível** (confirmado pelo usuário em 26/10). Desenvolvimento, testes e produção usam a **licença de avaliação que vem com o `pip`** ("trial license … problems of limited size", página do `gurobipy` no PyPI). Os modelos do projeto cabem nesse limite (verificado). **Substitui a A14 e a P5.** A A11 (Compute Server) deixa de bloquear | O uso dessa licença num servidor público precisa de **confirmação do professor** (P3) | Lucas (PO) |
| **A23** | JSON em `snake_case` por **configuração global** (`spring.jackson.property-naming-strategy=SNAKE_CASE`), e não `@JsonProperty` campo a campo | Menos código e nenhum campo esquecido. **Validado** no Boot 4 (todos os testes conferem os nomes) | Lucas |
| **A24** | As rotas simuladas do teste da Fase 1 (`RotasDeTesteController`) ficam no perfil **`teste-seguranca`** | Sem isso, quando os controllers reais existem, o `@SpringBootTest` carrega as duas versões e **não sobe** ("mapeamento ambíguo") | Lucas |
| **A25** | Testes de integração terminam em **`Test`** (não `IT`), recriam o schema **antes de cada teste** a partir do `bd/Script_DDL.sql` e usam um **relógio ajustável** em São Paulo | O Surefire padrão **ignora** `*IT`; o schema limpo deixa cada teste independente; o relógio fixo torna datas testáveis | Lucas |
| **A26** | **BD01, causa encontrada:** o Docker Desktop desta máquina trava ao iniciar porque arquivos de *socket* (`AF_UNIX`) ficam inacessíveis em `%LOCALAPPDATA%\Docker\run` e `%LOCALAPPDATA%\docker-secrets-engine` ("The file cannot be accessed by the system"). **Contorno verificado:** fechar o Docker Desktop, `wsl --shutdown`, **renomear** as duas pastas e abrir de novo. **Nunca** usar "Reset to factory defaults": apaga o `mente-fria-db` | Hipótese (não comprovada): o Windows 11 desta máquina é uma versão Insider (build 26300). Os arquivos travados não podem ser apagados nem renomeados, só a pasta | Diogo registra na evidência do BD01 |
| **A27** | O backend executa **uma otimização por vez** (fila com tempo limite) | Licenças acadêmicas e de avaliação limitam sessões simultâneas; duas recomendações ao mesmo tempo poderiam falhar | Lucas |

---

## 2. Bloqueios e decisões abertas

| # | Pendência | Dono | Prazo | Recomendação |
|---|---|---|---|---|
| A10 | **Contrato v2** existe só como proposta (agora com reposição, dashboard e marketing: §1.6, §2.7–§2.9) | Kauê, com Lucas e Leonardo | **27/10** | Aprovar e aplicar ao contrato oficial. Os formatos da proposta são **exatamente** os do código validado |
| P3 | Avisar o professor: receita → lucro (F9); reposição com orçamento; conteúdo do dashboard (A16); deploy com licença de avaliação do Gurobi (A22) e banco free que expira em 30 dias (A21) | Diogo e Lucas | **27/10** | Uma mensagem só (modelo no tutorial do Diogo, §9.1). Guardar a resposta em `docs/Sprint_3/` |
| A16, A17, A20 | Propostas de conteúdo (dashboard e marketing) | Lucas (PO) | 28/10 | Confirmar ou pedir a troca (cada uma é um método) |
| A21 | Quem cria as contas e o *blueprint* no Render; data de criação do banco (expira em 30 dias) | Kauê e Lucas | **28/10** (primeiro deploy) | Criar no dia do primeiro deploy e **anotar a data de expiração** no README. Se a avaliação passar dela, recriar o banco (o DDL e o seed rodam em minutos) |
| A11 | Compute Server no desenvolvimento | Kauê | — | **Não bloqueia mais** (A22). Registrar na Arquitetura que não há Compute Server nem WLS hoje |
| A12 | Atalho `POST /api/estoque/linhas/{id}/descarte` | Lucas (PO) | 29/10 | **Opcional.** O `GET .../linhas` já está implementado; o descarte já é possível pela movimentação |
| P2 | Versão do `gurobipy` | Kauê e Diogo | 27/10 | **13.0.3** (as duas passaram nos 47 testes; o `Dockerfile` já usa 13.0.3) |
| A13 | Teste de navegador instável (original e dividido) | Leonardo | Antes de 01/11 | Ver tutorial do Leonardo, §7.1 |
| A15 | Branch `origin/sprint-3` divergente | Kauê | 27/10 | Não fazer merge sem revisar |
| P7 | US13, US15, US16, US19, US26, US27 | Equipe | — | Fora da Sprint 3 (A2b) |

**Encerradas ou substituídas nesta revisão** (mantidas aqui para rastreabilidade):

| # | Era | Situação |
|---|---|---|
| A3 | US14, US17, US18 e US20 sem sprint | Encerrada pela **A2b** (26/10, noite) |
| A14 | Licença WLS acadêmica do Lucas em produção: só o titular usa, sem uso operacional | **Substituída pela A22** (não há WLS) |
| P5 | Licença WLS do Lucas e Python 3.14.7 | WLS: **substituída pela A22**. Python 3.14.7: segue não testado (sem impacto conhecido) |
| F12 | Definição única de consumo | **Resolvida pela A18** |
| P6 | Medir o timeout de 10 s | **Fechada:** 188 ms na máquina; 3,1 s com 0,1 CPU |

---

## 3. Divergências entre skills e decisões do projeto

As decisões do projeto prevalecem.

| Skill | O que ela recomenda | O que o projeto faz | Motivo |
|---|---|---|---|
| `311-frameworks-spring-jdbc` | `JdbcClient` como API padrão | **`NamedParameterJdbcTemplate`** (D4) | Decisão registrada; trocar agora é retrabalho |
| `springboot-tdd` | Meta de **80%** de cobertura com JaCoCo | **Sem meta numérica** (Plano de Testes §2) | Testes avaliados por requisito e risco |
| `springboot-tdd` | `@DataJpaTest`, `@MockBean` | Sem JPA; `@MockitoBean`; integração com Testcontainers | O projeto não usa JPA; `@MockBean` saiu no Boot 4 |
| `springboot-security` | Bean Validation (`@Valid`) | Validação **manual** que devolve todos os campos | Decisão de 14/10 (mensagens do `mapa_constraints.md`) |
| `springboot-security` | Filtro JWT com `@Component` | Filtro criado no `SecurityConfig` | Evita registrar o filtro duas vezes |
| `springboot-security` | Rate limiting e cabeçalhos extras | Não incluídos | Fora dos requisitos |
| `render-docker` | Plano `starter` nos exemplos | Plano **free** | Projeto acadêmico, sem custo; limites registrados na A21 |
