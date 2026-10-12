# Arquitetura — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Trilha:** B (Origem do problema: Cliente Real)

**Última atualização:** 08/10/2026

> **Status:** decisões D1 a D5 validadas pela equipe em 18/09/2026 (D4 registrada com base na experiência declarada da equipe — ver seção 2). Itens marcados como **proposta** ainda não foram validados e devem ser confirmados na implementação. As pendências estão na seção 8.
>
> **Contrato de Comunicação (20/09/2026):** as fronteiras **Java ↔ Python** e **Frontend ↔ Java** passam a ser definidas por `docs/Apoio/Contrato_de_Comunicacao_Mente_Fria.md`, que **prevalece sobre este documento** nos nomes de campo, nos valores de `status` e no formato de erro. As seções 3.1 e 3.3 abaixo resumem o contrato e apontam para ele.
>
> **Decisões de versão e configuração (25/09/2026):** o grupo respondeu as pendências de ambiente e configuração desta seção (detalhamento em `Pendencias_ARQUITETURA_para_o_grupo.md`). O resultado está nas seções 2, 5 e 8, abaixo.
>
> **Decisões de 08/10/2026:** versão do Gurobi fixada em **13.0.1**; licença de produção será a do **Lucas**; terminologia do modelo passa a ser **Programação Linear Inteira pura**; nomes das variáveis `GRB_COMPUTESERVER`/`GRB_SERVERPASSWORD` e a medição do timeout aguardam a licença do Lucas. **Alternativa C confirmada pelo grupo para produção:** sem Compute Server; o `otimizador.py` usa a licença WLS acadêmica do Lucas diretamente no container do backend (seção 3.3). A Opção A (Compute Server em container à parte) foi validada e **descartada** por não ser viável no plano gratuito do Render.

---

## 1. Visão geral

```mermaid
flowchart LR
    FE["Frontend<br/>HTML, CSS, JavaScript<br/>GitHub Pages"]
    BE["Backend<br/>Java + Spring Boot<br/>Render (Docker)"]
    DB[("PostgreSQL<br/>Render")]
    PY["Otimização<br/>Python + Gurobi<br/>mesmo container do backend"]

    FE <-->|"REST/JSON sobre HTTPS<br/>Authorization: Bearer (JWT)"| BE
    BE <-->|"JDBC (JdbcTemplate)"| DB
    BE <-->|"Subprocesso<br/>JSON via stdin/stdout"| PY
```

Princípios que valem para todo o projeto:

1. **Só o backend fala com o banco e com o módulo Python.** O frontend nunca acessa nenhum dos dois diretamente.
2. **Permissões e regras de negócio vivem no backend.** O frontend apenas apresenta e esconde menus; nunca é a única barreira de segurança.
3. **O módulo Python é uma função pura:** recebe dados em JSON, devolve o resultado em JSON. Não acessa o banco nem recebe requisições do navegador. Isso é coerente com o Diagrama de Classes, em que o resultado da otimização fica em memória, sem histórico persistido.

---

## 2. Decisões de arquitetura

| # | Decisão | Alternativas consideradas | Motivo |
|---|---|---|---|
| D1 | **PostgreSQL** como banco oficial | MySQL | Render não oferece MySQL gerenciado gratuito. Migração registrada em `DER.md` (16/09/2026). |
| D2 | **Java chama o Python como subprocesso**, trocando JSON por stdin/stdout | (B) Python como serviço HTTP (Flask/FastAPI); (C) Python lendo o banco direto | Um único serviço no Render, sem framework web extra e menos processos para subir localmente. (C) foi descartada: duplica acesso a dados e contorna as regras do backend. O contrato JSON fica atrás de uma interface no Java, então trocar por (B) depois não afeta o resto do sistema. |
| D3 | **JWT no header `Authorization`** | Sessão por cookie; frontend servido pelo próprio Spring | Frontend (`github.io`) e backend (`onrender.com`) estão em domínios diferentes; cookies entre eles sofrem bloqueio em alguns navegadores. JWT não depende de estado no servidor (o plano Free reinicia o serviço) e não exige proteção CSRF. Servir o frontend pelo Spring exigiria abandonar o GitHub Pages (muda o Termo de Aceite). |
| D4 | **Spring `JdbcTemplate`** (`NamedParameterJdbcTemplate`) para acesso a dados | Spring Data JPA/Hibernate; JDBC puro | A equipe nunca usou JPA; o Lucas conhece JDBC. O SQL já existe (DDL, constraints, consultas do dashboard) e fica visível e revisável. Evita a curva do JPA e as armadilhas de chave composta em `produto_ingrediente` e `pedido_produto`. Custo: mais código por CRUD. Pode ser revisitada. |
| D5 | **Python/Gurobi apenas para a recomendação de produção** (US10/US11), modelada como **Programação Linear Inteira pura** | Incluir reposição (US14) e marketing (US18) | Nos documentos, US14 e US18 aparecem como métodos de `Ingrediente`/`Produto`, sem menção a Programação Linear. Ficam em Java. O modelo é "inteiro puro" porque todas as variáveis de decisão (quantidade de copos de cada produto) são inteiras, com função objetivo e restrições lineares. |

Confirmado pela equipe em 18/09/2026: o plano Free do Render aceita Docker (necessário para a D2).

### 2.1 Versões e ferramentas (decididas em 25/09/2026; atualizadas em 08/10/2026)

| Item | Decisão | Observação |
|---|---|---|
| JDK | **25 (LTS)** | Atualizado em 27/09/2026 (era 21). Combinação recomendada pela própria documentação do Spring Boot 4.x. |
| Spring Boot | **4.0.8** | Atualizado em 27/09/2026 (era 3.3.4). Resolve a pendência de suporte — o 4.0.x é a linha atual e mantida. **Usa Jackson 3** (pacote `tools.jackson`), diferente do Jackson 2 da linha 3.x — ver a nota do Roteiro v3, seção 4. As anotações (`com.fasterxml.jackson.annotation`, como `@JsonProperty`) continuam no mesmo pacote. |
| Build | **Maven** | Mantém os comandos já usados no README e no Roteiro (`mvn spring-boot:run`, `mvn test`) |
| Python | **3.14.7** | Compatibilidade com o Gurobi 13.0.1 confirmada. |
| Gurobi | **13.0.1** — **definido em 08/10/2026** | Só a 13.0.1 suporta Python 3.14 (a 13.0.0 vai só até o 3.13). **Fixar `gurobipy==13.0.1` no `optimization/requirements.txt`** para todos usarem a mesma versão. |
| Modelo matemático | **Programação Linear Inteira pura** — definido em 08/10/2026 | Termo a usar em documentos, apresentação e código. Resolve o item de terminologia antes em aberto. |
| Licença do Gurobi | **WLS acadêmica**. Em desenvolvimento, cada desenvolvedor roda o próprio Compute Server localmente, com licença própria (Kauê e Lucas, por enquanto) — decidido em 02/10/2026. **Em produção, será usada a licença do Lucas** — decidido em 08/10/2026 | **Produção: Alternativa C — decidida em 08/10/2026:** licença WLS usada diretamente no container do backend, sem Compute Server (seção 3.3). |
| Autenticação | **Spring Security + biblioteca de JWT** (ex.: `jjwt`) | |
| Conversão de porções (item B1 do Contrato) | **O frontend converte**, usando a `porcao_padrao` de cada ingrediente | Fecha também o item B1 da seção 2.4 do Contrato de Comunicação |
| Ingredientes vencidos na otimização | **Não contam** como estoque disponível | O Java filtra antes de montar o JSON para o Python |
| Responsável pelo módulo Python | **Kauê** | |

> **Atualização de versões (27/09/2026):** o grupo trocou o JDK 21 → **25** e o Spring Boot 3.3.4 → **4.0.8**. Isso **resolve** a pendência de suporte que havia antes (o Spring Boot 3.3.x estava sem correções de segurança desde novembro de 2024; o 4.0.x é a linha atual). A troca também resolve a pendência do Python 3.14: o Gurobi confirma suporte a partir da versão **13.0.1**, agora **definida** (08/10/2026).
>
> **Confirmado em 28/09/2026: é mesmo um Gurobi Compute Server**, não só a licença WLS usada localmente. Isso muda a arquitetura da ponte com o Python no desenvolvimento — ver a seção 3.3.
>
> **Esclarecimento (08/10/2026) — "licença WLS Compute Server" e "modo WLS" não são licenças diferentes.** Segundo a documentação do Gurobi, a licença **WLS acadêmica** é uma só: ela roda diretamente em máquinas e containers (modo "cliente") **e** também permite subir uma versão "leve" de Compute Server (sem fila e com limite de 1 job). Ou seja, as licenças do Kauê e do Lucas servem para os dois modos; o que muda é só **como o `otimizador.py` as usa**. Em modo direto, a licença é identificada por `WLSACCESSID`, `WLSSECRET` e `LICENSEID` (no arquivo `gurobi.lic` ou em variáveis de ambiente). Confirmação prática, a cargo do Kauê e do Lucas: abrir o `gurobi.lic` de cada um — se tiver só essas três linhas e nenhuma `COMPUTESERVER`, a licença já está sendo usada em modo direto (é o que os testes do Kauê, em 30/09, indicam, pois o `otimizador.py` atual não aponta para nenhum servidor).

O **Contrato de Comunicação** (mantido pelo Kauê; revisado e aprovado em 20/09/2026) detalha as rotas REST, os erros e o formato JSON trocado com o Python.

---

## 3. As três ligações

### 3.1 Frontend ↔ Backend — REST/JSON sobre HTTPS

- **CORS:** o backend libera apenas a origem do GitHub Pages do projeto (e `localhost` em desenvolvimento).
- **URL da API:** site estático não tem variáveis de ambiente. A URL base fica em um `config.js` do frontend, com valores para local e produção.
- **HTTPS:** obrigatório (o GitHub Pages é HTTPS; chamadas HTTP seriam bloqueadas como *mixed content*). O Render já fornece certificado.
- **Autorização:** sempre validada no backend por perfil (CT05, CT08 e critério de bloqueio nº 5 do Plano de Testes).
- **Contrato:** rotas, formato de erro, códigos HTTP, CORS e convenções do JSON estão no **Contrato de Comunicação** (§2), que prevalece sobre este documento. Em resumo:
  - JSON em **snake_case**, com os mesmos nomes do DER.
  - Erro no formato `{ "mensagem": "...", "campos": { "campo": "mensagem" } }`, com **vários campos de uma vez** quando for o caso (US7); as mensagens vêm de `docs/Apoio/mapa_constraints.md`.
  - Códigos: `400` validação de campo ou regra de cadastro; `401` sem token/token inválido/credenciais inválidas; `403` perfil sem permissão; `404` inexistente; `409` conflito (e-mail duplicado; Administrador já existe); `422` regra de negócio de uma operação (estoque insuficiente).
- **Cold start:** no plano Free do Render, a primeira chamada após inatividade pode demorar (verificar os limites atuais). O frontend deve ter um estado de "carregando" claro.

### 3.2 Backend ↔ Banco — JDBC (`JdbcTemplate`)

- Credenciais sempre por variáveis de ambiente (`DB_*`), nunca no código.
- O `Script_DDL.sql` (pasta `bd/`) é a **fonte do schema**. A aplicação não cria nem altera tabelas.
- Todo SQL usa parâmetros (`?` ou nomeados). **Nunca** concatenar texto de entrada no SQL.
- Um `Repository` por agregado, isolando o SQL. As regras de negócio ficam nos `Service`.
- **Transações:** o registro de pedido (US12) grava `pedido`, `pedido_produto`, baixa de `ingrediente.quantidade_estoque` e insere movimentações `SAIDA` em **uma única transação** (CT25). A escolha FEFO das entradas é aplicada nessa mesma transação.
- As consultas agregadas do dashboard (US17) são escritas em SQL explícito.

### 3.3 Backend ↔ Python/Gurobi — subprocesso com contrato JSON (+ Compute Server em desenvolvimento)

**Como funciona**

1. O backend lê o estoque, os produtos ativos e as composições no banco e monta o JSON de entrada.
2. Executa `python optimization/app/otimizador.py` (`ProcessBuilder`) e envia o JSON pela entrada padrão. **Isso não muda** com o Compute Server: o Java continua só conversando com o processo Python local, do mesmo jeito.
3. **Dentro do `otimizador.py`:** hoje o script resolve o modelo localmente, com a licença WLS configurada na máquina de quem executa. Com o Compute Server, o script passaria a se conectar a um **Gurobi Compute Server** pela rede, via parâmetros do ambiente do Gurobi (`ComputeServer` com `host:porta` e `ServerPassword`) — **diferente** dos parâmetros da licença WLS "cliente" (`WLSACCESSID`/`WLSSECRET`/`LICENSEID`). Essa mudança no `otimizador.py` **ainda não foi implementada**.
4. O Java lê o JSON de saída na saída padrão, com **timeout** (10 s é provisório — ver nota abaixo).
5. Devolve o resultado ao frontend em linguagem de negócio (sem termos de otimização — US11).

No Java, a chamada fica atrás de uma interface (ex.: `OtimizadorProducao`), o que permite trocar a implementação e testar o restante com um dublê.

> **Desenvolvimento — decidido pela equipe em 02/10/2026 (Kauê e Lucas):** cada desenvolvedor que precisa testar roda o seu próprio Compute Server localmente, com a própria licença acadêmica WLS. Assim, `GRB_COMPUTESERVER` aponta para `localhost:porta`, não há dependência de rede entre computadores, e o limite de "1 job por vez, sem fila" da licença acadêmica não gera conflito entre os dois.
>
> **Produção — validação da "Opção A" (container à parte em nuvem), feita em 08/10/2026.** O grupo indicou a Opção A, pedindo antes uma validação de custo e de sentido para o escopo. **Resultado: não recomendada no plano gratuito do Render.** Motivos:
>
> - **Plano gratuito do Render é insuficiente para um servidor de otimização:** cada serviço gratuito tem 0,1 CPU e 512 MB de RAM, desliga após 15 minutos sem tráfego e leva cerca de 1 minuto para religar; não tem disco persistente nem recebe tráfego pela rede privada. *Private services* não têm instância gratuita (a partir de ~US$ 7/mês). Um Compute Server que "dorme" entra em conflito com o timeout de 10 s da chamada.
> - **A RAM já é um risco no serviço do backend:** Java + Python + `gurobipy` dividindo 512 MB (medição pendente, seção 8). Um segundo serviço só aumenta o custo.
> - **Regras da licença acadêmica WLS:** (a) uso **não comercial e não operacional**; (b) a chave só pode ser usada pela pessoa titular (o Lucas) e não pode ser compartilhada; (c) validade de **90 dias**, e tanto a geração quanto a **renovação exigem conexão à rede de uma instituição acadêmica reconhecida** (a Fatec); (d) o modo Compute Server da licença acadêmica é "leve": **1 job por vez e sem fila**; (e) requer internet permanente durante a otimização.
> - **Tecnicamente** (a partir da versão 8 do Gurobi) o servidor usa uma única porta HTTP/HTTPS, então hospedá-lo em um serviço web seria possível em tese, mas **não foi testado** e não resolveria os pontos acima.
>
> **Alternativa C (DECIDIDA em 08/10/2026): sem Compute Server em produção.** O `otimizador.py` usa a licença WLS "cliente" diretamente, dentro do mesmo container do backend (como a arquitetura original previa e como os testes do Kauê já funcionam, conforme relato). As credenciais (`GRB_WLSACCESSID`, `GRB_WLSSECRET`, `GRB_LICENSEID`, da licença do Lucas) entram como variáveis de ambiente no Render. Vantagens: um único serviço, sem custo adicional, sem servidor extra para manter e sem o problema do cold start de um segundo serviço. Custo: o container precisa de acesso à internet (o Render tem) e a RAM precisa ser medida.
>
> **Alternativa D:** manter o Compute Server em produção, em serviço pago (ex.: *private service* do Render) ou em outra nuvem. Foge do custo zero e exigiria pesquisa de outras opções.
>
> **Respostas do grupo e esclarecimentos (08/10/2026):**
>
> - **Renovação da licença:** o grupo informou que a aplicação serve apenas ao trabalho acadêmico e que, depois da demonstração, não deve ser mais usada. Então a renovação a cada 90 dias **só será necessária se a demonstração cair depois do vencimento da licença**. Cuidado prático: a licença do Lucas vale 90 dias **a partir da geração**, e gerá-la (ou renová-la) exige estar na rede da Fatec. Conferir a data de validade assim que ela chegar e comparar com a data da apresentação final.
> - **Enquadramento nas regras do Gurobi:** a licença acadêmica é gratuita para cursos, ensino e pesquisa em instituição de ensino, e **não pode ser usada em trabalho comercial ou operacional**. Uma demonstração acadêmica se enquadra no uso previsto; **usar o sistema no dia a dia da açaiteria depois da disciplina não se enquadraria**. Recomendações: (1) registrar no README que o uso é acadêmico; (2) o piloto com o cliente real deve se limitar à demonstração, sem operação contínua; (3) se o cliente quiser continuar usando o sistema depois, será preciso outra licença ou outro solver (fora do escopo desta entrega). Esta é uma leitura dos termos publicados, não um parecer jurídico; em caso de dúvida, consultar o professor ou o suporte do Gurobi.
> - **Por que um Compute Server?** Pelo esclarecimento da seção 2.1, a licença WLS acadêmica funciona sem ele. Não há exigência técnica identificada que obrigue o Compute Server em produção.
> - **A Alternativa C é viável?** **Sim.** Ela usa o mesmo tipo de licença que o Kauê e o Lucas já têm, só que em modo direto. Pontos de atenção: (a) o `otimizador.py` precisa receber as credenciais — hoje o gurobipy lê o `gurobi.lic` da máquina; no Render o mais simples é o script ler `GRB_WLSACCESSID`, `GRB_WLSSECRET` e `GRB_LICENSEID` das variáveis de ambiente e passá-las ao criar o ambiente do Gurobi (ou montar o `gurobi.lic` como arquivo secreto do Render); (b) a licença acadêmica permite **2 sessões simultâneas** em modo direto, melhor que o limite de 1 job do modo Compute Server; (c) a RAM de 512 MB do serviço precisa ser medida com Java + Python + gurobipy; (d) o container precisa de internet (o Render oferece).
>
> **Decisão (equipe, 08/10/2026): Alternativa C confirmada.** Em produção não haverá Compute Server: a Opção A fica descartada e a Alternativa D (serviço pago) não será necessária. O Compute Server continua opcional **apenas no desenvolvimento**, para quem quiser usá-lo. Consequências práticas: (1) o `otimizador.py` passa a receber as credenciais da licença do Lucas por variáveis de ambiente do Render (`GRB_WLSACCESSID`, `GRB_WLSSECRET`, `GRB_LICENSEID`) ou por `gurobi.lic` como arquivo secreto — ajuste a cargo do Kauê; (2) o consumo de RAM do container (Java + Python + gurobipy, 512 MB no plano gratuito) será medido no deploy; (3) o uso da licença é exclusivamente acadêmico, restrito à demonstração.
>
> **Continua valendo:**
> - **Timeout de 10 s ainda é só uma estimativa.** Só será medido quando a licença do Lucas chegar e os testes começarem. Mesmo local, a chamada ao Compute Server tem uma etapa de rede a mais que a solução local "pura".
> - **Nomes `GRB_COMPUTESERVER` e `GRB_SERVERPASSWORD`** seguem como proposta até a licença do Lucas chegar.

**Contrato**

A requisição, a resposta e as regras estão no **Contrato de Comunicação** (§1), que prevalece sobre este documento. Em resumo:

- **Nomes iguais aos do DER:** entrada com `ingredientes[].quantidade_estoque` e `produtos[].composicao[].quantidade_utilizada`; saída com `status` (`"sucesso"` ou `"erro"`), `receita_estimada` e `recomendacao` (**um item por produto recebido**, com `quantidade` inteira ≥ 0).
- **O Java envia todos os ingredientes usados por produtos ativos, inclusive os com estoque 0.** Nunca se filtra por "estoque > 0": se um ingrediente em falta sumisse da lista, o Python não saberia limitar os produtos que o usam. Só entram produtos com `ativo = true`. **Ingredientes com lote vencido não contam como estoque disponível** (decisão do grupo, 25/09/2026): o Java desconta esse saldo antes de montar o `quantidade_estoque` enviado ao Python.
- Estoque e quantidades chegam **na mesma unidade do ingrediente** (kg, l, un). Quem converte porções (Tela 09) em quantidade é o **frontend** (item B1 do contrato, confirmado em 25/09/2026).
- "Estoque insuficiente para qualquer produto" é **`sucesso`** com todas as quantidades 0; o frontend mostra uma mensagem amigável. O `erro` fica para problemas de dados (produto sem composição, ingrediente ausente da lista).
- **Código de saída `0`** = há JSON válido na `stdout` (inclusive com `status: "erro"`); diferente de `0` = falha técnica (o `stderr` vai para o log). **UTF-8** nas duas pontas. **Timeout de 10 s** (provisório). O backend nunca expõe *stack trace* ao usuário.
- `receita_estimada` é informativa: o Java **recalcula com `BigDecimal`** a partir das quantidades e dos preços.
- O modelo matemático (variáveis, função objetivo, restrições) será detalhado e validado com a equipe no início da Sprint 2.

---

## 4. Autenticação e autorização

```
1. POST /api/auth/login   { email, senha }   →  { token, nome, perfil }
2. Frontend guarda o token e redireciona conforme o perfil (US3)
3. Demais chamadas:       Authorization: Bearer <token>
4. Backend valida assinatura e validade, depois confere se o perfil acessa a rota
   →  200, ou 401 (token inválido/expirado), ou 403 (perfil sem permissão)
```

- O token carrega id do usuário, perfil e validade. **Validade curta** (proposta: 1 a 2 horas). Não há revogação antes de expirar — aceitável para o MVP.
- Senhas armazenadas apenas como hash (`senha_hash`); mínimo de 8 caracteres validado na aplicação (US1, CT02).
- A chave de assinatura fica em variável de ambiente (`JWT_SECRET`). **Nunca** no código nem no repositório.
- O token fica em JavaScript no navegador, então o frontend deve evitar `innerHTML` com dados digitados por usuários (mitigação de XSS).

---

## 5. Variáveis de ambiente

| Variável | Usada por | Descrição | Status |
|---|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Backend | Conexão com o PostgreSQL | Em uso (README) |
| `JWT_SECRET` | Backend | Chave de assinatura dos tokens | Planejada (README) |
| `CORS_ALLOWED_ORIGIN` | Backend | Origem do frontend liberada no CORS | Planejada (README) |
| `PYTHON_CMD` | Backend | Comando para chamar o Python (`python`, `python3` ou `py`, conforme a máquina). **O valor é ajustado por cada integrante no próprio `.env`**; o nome da variável é o mesmo para todos | Planejada (README) |
| `OTIMIZADOR_SCRIPT` | Backend | Caminho do script `otimizador.py`, relativo à pasta de onde o backend inicia (hoje `../optimization/app/otimizador.py`). Valor ajustado por integrante no `.env` | Planejada (README) |
| `GRB_COMPUTESERVER` | Python | Endereço do Gurobi Compute Server. **Em desenvolvimento, cada um usa `localhost:porta`**. Em produção **não é usada** (Alternativa C, seção 3.3) | **Proposta** — nome e formato a definir quando a licença do Lucas chegar |
| `GRB_SERVERPASSWORD` | Python | Senha do cliente para o Compute Server (definida na configuração do próprio servidor, não é a licença WLS em si) | **Proposta** — idem |
| `GRB_WLSACCESSID`, `GRB_WLSSECRET`, `GRB_LICENSEID` | Servidor Gurobi **ou** o próprio `otimizador.py` (se a Alternativa C for adotada) | Credenciais da licença WLS acadêmica. Em desenvolvimento, ficam na máquina de cada desenvolvedor. **Em produção, serão as da licença do Lucas**; nunca no código nem no Git | Desenvolvimento: resolvido (local). Produção (Alternativa C): credenciais da licença do Lucas como variáveis de ambiente (ou arquivo secreto) no Render |

O `.env` real nunca é versionado; o `.env.example` lista as variáveis sem valores.

---

## 6. Deploy

| Bloco | Onde | Observação |
|---|---|---|
| Frontend | GitHub Pages | Arquivos estáticos |
| Backend + Python | Render, **um serviço** via Dockerfile (JRE + Python + `gurobipy`) | Docker no plano Free confirmado pela equipe |
| Banco | PostgreSQL gerenciado do Render | Limites do plano Free descritos no `DER.md` |

Recomendação: **fazer o deploy do "esqueleto"** (passos 2 e 3 da seção 7) já no fim da Sprint 1, mesmo com a US20 prevista para a Sprint 4. É nesse momento que aparecem CORS, HTTPS, Dockerfile, licença Gurobi e cold start; descobrir isso cedo evita retrabalho. Medir também o consumo de memória do container com Java + Python (o plano Free tem limite de RAM — 512 MB, a confirmar no momento do deploy).

---

## 7. Roteiro de implementação

| # | Passo | O que prova | Sprint |
|---|---|---|---|
| 1 | Subir PostgreSQL local (Docker) e rodar `Script_DDL.sql` com o seed | Banco reproduzível por todos | 1 |
| 2 | Backend conecta ao banco; `GET /api/saude` (`SELECT 1`) e `GET /api/ingredientes` | Backend ↔ Banco | 1 |
| 3 | Página HTML que faz `fetch` em `/api/ingredientes` e lista o resultado, com CORS | Frontend ↔ Backend ponta a ponta | 1 |
| 4 | Login por perfil com JWT; frontend trata 401/403 (US1–US3) | Autenticação e autorização | 1 |
| 5 | `otimizador.py` + `pytest` (CT23, CT24) usando o contrato JSON, sem Java | Módulo Python isolado e testável | 2 |
| 6 | `POST /api/producao/recomendacao` (só Administrador) chamando o Python | Backend ↔ Python | 2 |
| 7 | Deploy do esqueleto (Pages + Render) — antecipado para o fim da Sprint 1 | Ambiente real | 1 (esqueleto) / 4 (US20) |

---

## 8. Pendências

**Decididas pelo grupo em 25/09/2026** (`Pendencias_ARQUITETURA_para_o_grupo.md`) — ver a tabela da seção 2.1:
- Versões de JDK, Spring Boot, build tool, Python e licença do Gurobi.
- Autenticação (Spring Security + JWT).
- Conversão de porções (item B1 do contrato) e ingredientes vencidos na otimização.
- Responsável pelo módulo Python.
- README atualizado com as variáveis novas da seção 5 (`JWT_SECRET`, `CORS_ALLOWED_ORIGIN`, `PYTHON_CMD`, `OTIMIZADOR_SCRIPT` e as credenciais WLS).

**Atualizadas em 27–28/09/2026:**
- JDK 21 → 25; Spring Boot 3.3.4 → 4.0.8 (resolve a pendência de suporte que havia antes).
- Confirmado: Gurobi **Compute Server** com licença WLS acadêmica — ver seção 3.3 (e o esclarecimento sobre os modos de uso da licença na seção 2.1).

**Resolvidas em 02/10/2026:** onde o Compute Server roda durante o desenvolvimento (cada desenvolvedor, localmente, com a própria licença).

**Resolvidas em 08/10/2026:**
- **Versão exata do Gurobi:** 13.0.1.
- **Terminologia:** o modelo é de **Programação Linear Inteira pura** (resolve o item de terminologia antes em aberto com o professor).
- **Qual licença será usada em produção:** a do **Lucas**.
- **Timeout do Python e valores das variáveis de ambiente:** cada integrante ajusta no próprio `.env` (os **nomes** das variáveis são únicos para todos — ver seção 5).

**Respondidas em 08/10/2026:**
- **Renovação da licença do Lucas:** só será necessária se a demonstração cair após o vencimento (90 dias a partir da geração). Uso previsto: exclusivamente acadêmico/demonstração (seção 3.3).
- **Enquadramento nas regras da licença acadêmica:** uso acadêmico se enquadra; uso operacional contínuo pelo cliente depois da disciplina não (seção 3.3).

**Decidida em 08/10/2026 — produção:** **Alternativa C** (licença WLS direta no container do backend, sem Compute Server). A Opção A foi descartada.

**Segue em aberto:**
- **Ajuste no `otimizador.py` para produção** (Kauê): ler as credenciais WLS de variáveis de ambiente (ou usar `gurobi.lic` como arquivo secreto).
- **Nomes exatos de `GRB_COMPUTESERVER` e `GRB_SERVERPASSWORD`** (seção 5): só serão necessários no desenvolvimento, se alguém usar o Compute Server local; aguardam a licença do Lucas.
- **Medir o timeout de verdade** com uma chamada real: o grupo decidiu aguardar o início dos testes e a licença do Lucas. 10 s segue provisório. Sugestão: tornar o valor uma propriedade configurável do backend, com padrão de 10 s.
- **Consumo de memória** do container Java + Python no Render Free: medir quando o deploy for implementado.
