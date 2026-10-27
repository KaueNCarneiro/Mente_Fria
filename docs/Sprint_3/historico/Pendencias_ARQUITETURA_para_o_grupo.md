# Pendências da ARQUITETURA.md — para decidir em grupo

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)

**Como usar:** cada item tem **o que é**, **por que importa**, as **opções** e uma **recomendação**. Depois de discutir, preencham o campo **"Decisão do grupo"** de cada um. Quando todos estiverem preenchidos, eu atualizo a seção 8 da `ARQUITETURA.md` com o resultado.

**Pesquisei as versões atuais de cada ferramenta hoje (24/09/2026)** para trazer opções realistas, e indico isso em cada item. Nada aqui trava quem já começou a programar: dá para ajustar a versão depois, com mais atrito quanto mais tarde for.

---

## Bloco 1 — Para o Lucas montar o projeto Java

Estas três decisões travam o primeiro passo do roteiro: gerar o projeto em start.spring.io.

### 1. Versão do Java (JDK)

**O que é:** a versão do Java que vai rodar o backend. Todo mundo da equipe precisa instalar a mesma.

**Por que importa:** versões diferentes entre os integrantes podem fazer o projeto compilar na máquina de um e não na do outro.

**Opções:**
- **Java 21 (LTS)** — lançado em 2023, é a versão "estável de longo prazo" mais madura hoje. Suporte oficial garantido até 2028 (e estendido até 2031 por alguns fornecedores).
- **Java 17 (LTS)** — a que os documentos atuais citam como mínimo. É mais antiga; o suporte oficial da Oracle para ela termina agora, em setembro de 2026.
- **Java 25 (LTS)** — lançada em setembro de 2025, é a mais nova. Suporte garantido até 2030. Por ser mais recente, algumas ferramentas e tutoriais na internet ainda demoram a cobri-la.

**Recomendação:** **Java 21**. É o meio-termo mais seguro: madura, bem documentada, e com suporte por vários anos ainda — ao contrário da 17, cujo suporte está terminando bem agora.

**Decisão do grupo:** Java 21 **Data:** 25/09 · **Atualizado em 27/09:** Java 25

---

### 2. Versão do Spring Boot

**O que é:** o framework que o Lucas vai usar para construir a API em Java.

**Por que importa:** a versão escolhida muda pequenos detalhes de código (por exemplo, qual biblioteca de JSON o projeto usa por baixo dos panos — isso já está anotado como pendência no Roteiro do Hello World).

**Opções:**
- **Spring Boot 4.0** — versão atual recomendada para projetos novos, lançada em novembro de 2025. Já tem quase um ano de maturidade.
- **Spring Boot 3.5** — a versão anterior, ainda mantida, mas o suporte "normal" dela já está no fim; só recebe atualizações de segurança daqui para frente.

**Recomendação:** **Spring Boot 4.0** (a versão mais recente dela, por exemplo 4.0.7 no momento em que o Lucas for gerar o projeto). É a opção indicada pela própria documentação do Spring para quem está começando um projeto agora.

**Decisão do grupo:** Spring Boot 3.3.4 **Data:** 25/09 · **Atualizado em 27/09:** Spring Boot 4.0.8

---

### 3. Maven ou Gradle

**O que é:** a ferramenta que baixa as bibliotecas do projeto Java e organiza a build (compilar, testar, empacotar). Não é uma "versão", e sim uma escolha de ferramenta — mas precisa ser decidida junto com as duas anteriores, porque o start.spring.io pede essa escolha na hora de gerar o projeto.

**Por que importa:** o `./mvnw` que aparece nos passos do Roteiro só existe se a escolha for Maven; com Gradle, os comandos mudam.

**Opções:**
- **Maven** — é o que o README já assume (`mvn spring-boot:run`, `mvn test`) e o que os exemplos do Roteiro usam.
- **Gradle** — mais moderno, mas exigiria trocar todos os comandos dos documentos.

**Recomendação:** **Maven**, para não ter que reescrever os documentos que já existem, a não ser que alguém do grupo já tenha muito mais prática com Gradle.

**Decisão do grupo:** Maven **Data:** 25/09

---

## Bloco 2 — Para o módulo Python (Kauê)

### 4. Versão do Python

**O que é:** a versão do Python que vai rodar o `otimizador.py`.

**Por que importa:** o Gurobi (biblioteca de otimização) nem sempre acompanha a versão mais nova do Python assim que ela sai.

**Opções:**
- **Python 3.11 ou 3.12** — maduras, com ampla compatibilidade confirmada com o `gurobipy`.
- **Python 3.13 ou 3.14** — mais novas; o `gurobipy` já dá suporte à 3.13, mas vale conferir se a 3.14 já é suportada no momento da instalação, para não ter surpresa.

**Recomendação:** **Python 3.11 ou 3.12**. É o ponto mais seguro entre "moderno" e "sem risco de incompatibilidade" com o Gurobi.

**Decisão do grupo:** Python 3.14.7 **Data:** 25/09

---

### 5. Versão do Gurobi

**O que é:** a biblioteca de Programação Linear em si.

**Por que importa:** ela precisa combinar com a versão do Python escolhida acima (o próprio Gurobi publica uma tabela de compatibilidade).

**Recomendação:** em vez de fixar um número agora (o Gurobi lança novas versões com frequência, e eu não tenho como garantir qual é a mais recente no dia exato em que o Kauê for instalar), a recomendação é: **instalar a versão mais recente disponível via `pip install gurobipy` no dia da instalação**, e então conferir na tabela de compatibilidade do site do Gurobi se ela funciona com a versão do Python escolhida no item 4. Depois, anotar a versão exata que foi instalada aqui embaixo, para todo mundo usar a mesma.

**Decisão do grupo (versão exata instalada):** Esperando retorno (da gurobi), mas provavelmente será a mais recente **Data:** 25/09 · **Atualizado em 27/09:** Gurobi 13.0 — **confirmar se é a 13.0.1** (só ela suporta Python 3.14; a 13.0.0 vai até o 3.13) · **Atualizado em 08/10:** versão definida: **Gurobi 13.0.1** (fixar `gurobipy==13.0.1` no `requirements.txt`)

**Resposta: Que arquivo é esse requirements.txt? E aonde o temos?**

**Resposta do Claude (08/10/2026):** o `requirements.txt` é a lista de bibliotecas Python que o projeto usa, uma por linha, para que todos instalem exatamente as mesmas versões com um único comando (`pip install -r requirements.txt`, que já está no README). Ele fica em **`optimization/requirements.txt`** (o README e o relatório da Sprint 2 o citam; ele veio no PR #1 do Kauê, e eu não tive acesso ao conteúdo). O que fazer: abrir o arquivo e conferir se há uma linha `gurobipy==13.0.1` (o `==` trava a versão exata). Se estiver `gurobipy` sem versão, ou outra versão, troque para `gurobipy==13.0.1` e comite. Para descobrir a versão instalada na sua máquina: `pip show gurobipy`. Se o arquivo não existir, dentro do ambiente virtual rode `pip freeze > requirements.txt` na pasta `optimization` e remova linhas que não sejam do projeto (o `pytest` deve permanecer).

---

### 6. Licença do Gurobi: node-locked ou WLS?

**O que é:** o tipo de licença que libera o uso completo do Gurobi (sem essa licença, ele só resolve problemas pequenos — o que já basta para os testes da Sprint 1, mas não para produção).

**Por que importa:** muda como a licença é instalada e configurada em cada computador e, futuramente, no servidor do Render.

**Opções:**
- **Node-locked** — a licença fica presa a um computador específico (identificado pelo hardware). Simples para uso individual, mas trava se o serviço mudar de máquina — o que é comum em ambientes de nuvem, incluindo o Render.
- **WLS (Web License Service)** — a licença é validada pela internet, sem depender de uma máquina fixa. Mais flexível para quando o projeto for para o Render.

**Recomendação:** **não decidir agora**. Como o Plano de Testes já registra, a licença gratuita que vem com o `pip install gurobipy` (limitada a 2000 variáveis/restrições) já é suficiente para o volume da açaiteria e para os testes da Sprint 1 e 2. Essa escolha só importa de verdade perto do deploy final, e a recomendação, quando chegar a hora, é o **WLS** (por não depender de uma máquina fixa, o que combina melhor com o Render).

**Decisão do grupo:** (X) Decidir agora mesmo assim A escolhida foi a WLS. ( ) Deixar para perto do deploy, como sugerido **Data:** 25/09 · **Atualizado em 27/09:** confirmado que é mesmo **WLS Compute Server** (não só a licença WLS simples) · **Atualizado em 02/10:** hospedagem de desenvolvimento resolvida — Kauê e Lucas, cada um com a própria licença, rodando o próprio Compute Server localmente. Só falta decidir a hospedagem do Compute Server de **produção** (ver `ARQUITETURA.md`, seção 8). · **Atualizado em 08/10:** a licença usada em produção será a do **Lucas**; para a hospedagem de produção o grupo indicou a "Opção A" (container à parte em nuvem), **condicionada à validação de custo e viabilidade** — a validação foi feita e **não recomenda a Opção A no plano gratuito do Render**; há uma alternativa (usar a licença WLS direto no container do backend, sem Compute Server em produção) aguardando decisão do grupo (ver `ARQUITETURA.md`, seção 3.3).

**Resposta: É viavel fazermos essa sua recomendação? Se for tenho outra dúvida também, não sei se a licença que o Kaue possui e que o Lucas solicitou (ambas são a WLS Compute Server) funcionaria somente em modo WLS.**

**Resposta do Claude (08/10/2026):**
- **É viável? Sim.** A recomendação (Alternativa C: o `otimizador.py` usa a licença WLS diretamente, dentro do container do backend, sem Compute Server em produção) é viável com o tipo de licença que vocês têm.
- **Sobre as licenças:** não existe uma licença "só Compute Server" separada da "WLS". Segundo a documentação do Gurobi, a licença **WLS acadêmica** é a mesma e serve aos dois modos: roda direto em máquinas e containers, e também permite subir uma versão leve de Compute Server (sem fila, 1 job). A maioria dos usuários acadêmicos a usa diretamente. Em modo direto ela permite **2 sessões simultâneas**.
- **Como conferir na prática:** abram o `gurobi.lic` (Kauê e Lucas). Se tiver só `WLSACCESSID`, `WLSSECRET` e `LICENSEID`, é uma licença WLS comum, que já funciona em modo direto. Os testes do Kauê em 30/09 indicam o mesmo: o `otimizador.py` atual não aponta para nenhum servidor e os 8 testes passaram. Se o arquivo tiver linhas `COMPUTESERVER`, apague-as numa cópia e teste de novo.
- **O que muda no código:** só o `otimizador.py` no Render — ele precisa receber as credenciais por variáveis de ambiente (ou por um `gurobi.lic` como arquivo secreto do Render).
- **Respostas às perguntas da `Arquitetura.md`:** a renovação só é necessária se a demonstração cair depois dos 90 dias da licença, e o uso acadêmico previsto se enquadra nas regras (detalhes na seção 3.3 da `Arquitetura.md`).

---

## Bloco 3 — Autenticação (Lucas)

### 7. Biblioteca de JWT / uso do Spring Security

**O que é:** já foi decidido que o login usa JWT (um "crachá digital" que prova quem é o usuário — ver `ARQUITETURA.md`, seção 4). Falta escolher a ferramenta que gera e confere esse crachá dentro do Java.

**Por que importa:** afeta como as rotas ficam protegidas por perfil (Administrador × Funcionário) e como os testes de autenticação (CT06, CT07, CT08) são escritos.

**Opções:**
- **Spring Security + uma biblioteca de JWT** (como a `jjwt`) — é o caminho mais usado em projetos Spring Boot, com bastante material de estudo disponível, mas tem uma curva de aprendizado inicial.
- **Uma biblioteca de JWT sozinha, sem Spring Security** — mais simples de entender rapidamente, mas exige escrever manualmente a parte de "bloquear rota por perfil", que o Spring Security já resolve pronto.

**Recomendação:** como esta é uma decisão mais técnica, de quem vai escrever o código, ela é do **Lucas**. Se ele não tiver preferência, sugiro **Spring Security + `jjwt`**, por ser o padrão do ecossistema Spring e ter mais tutoriais para consultar em caso de dúvida.

**Decisão do grupo:** Spring Security + uma biblioteca de JWT **Data:** 25/09

---

## Bloco 4 — Regra de negócio (Leonardo e Lucas)

### 8. Quem converte "porções" em quantidade — já está no Contrato como item B1

**O que é:** no cadastro de produto (Tela 09), o dono pensa em "porções" (ex.: 2 porções de granola). Mas o banco guarda a quantidade na unidade do ingrediente (kg, l, un). Alguém precisa fazer essa conta.

**Por que importa:** se a conversão ficar em dois lugares diferentes (frontend e backend), o resultado pode divergir.

**Opções:**
- **O frontend converte** antes de enviar à API (usando a `porcao_padrao` de cada ingrediente, que já vem em `GET /api/ingredientes`) — é o que o Contrato de Comunicação já assume, aguardando confirmação.
- **O backend converte**, recebendo a quantidade em porções.

**Recomendação:** manter o que o Contrato já propõe — **o frontend converte**. Essa já é a mesma pergunta do item **B1** da seção 2.4 do Contrato; respondendo aqui, fecham os dois de uma vez.

**Decisão do grupo:** O frontend converte **Data:** 25/09

---

### 9. Ingredientes vencidos contam como estoque disponível na recomendação de produção?

**O que é:** quando o Python calcular quanto produzir de cada produto (US10), ele deve considerar um ingrediente que já venceu, mas que ainda aparece como "em estoque" no banco?

**Por que importa:** afeta diretamente o resultado da Programação Linear e a credibilidade da recomendação para o dono do comércio.

**Opções:**
- **Não contam** — o sistema só recomenda produção com ingredientes dentro da validade. Mais seguro para o negócio, mas exige que o Java filtre isso antes de montar o JSON para o Python.
- **Contam** — mais simples de implementar agora, mas pode recomendar produzir algo com um ingrediente estragado.

**Recomendação:** **não contam**. Um sistema de gestão que recomenda usar ingrediente vencido perde a confiança do usuário rapidamente — e essa regra já está alinhada com a lógica de alerta de vencimento (US16) que o sistema também vai ter.

**Decisão do grupo:** Não contam **Data:** 25/09

---

## Bloco 5 — Organização da equipe

### 10. Quem é o responsável pelo módulo Python?

**O que é:** o Termo de Aceite lista um papel para cada um (Leonardo: Frontend; Diogo: Dados; Lucas: Backend; Kauê: Facilitador/Scrum Master), mas nenhum deles é formalmente "o do Python/Gurobi".

**Por que importa:** é só uma questão de clareza — sem isso, ninguém sabe quem cobrar se o `otimizador.py` atrasar.

**Recomendação:** como o **Kauê** já é quem mantém o Contrato de Comunicação entre Java e Python (seção 0 do próprio Contrato), faz sentido que ele também assuma formalmente o módulo Python. Mas é só uma sugestão — o grupo decide.

**Decisão do grupo:** Kauê **Data:** 25/09

---

## Não precisam de decisão agora (só para conhecimento)

Estes itens continuam na `ARQUITETURA.md`, mas não bloqueiam ninguém nem exigem reunião:

- **Terminologia "Programação Linear" × "Programação Inteira":** como as quantidades de copos são números inteiros, o modelo é tecnicamente de Programação **Inteira**, não só Linear. **Decidido em 08/10/2026: "Programação Linear Inteira pura"** (todas as variáveis de decisão são inteiras, função objetivo e restrições lineares). Documentos a ajustar para usar o termo: README (feito), `Diagramas_UML.md`, `Plano_de_Testes_Mente_Fria.md`, `Arquitetura.md` (feito) e os arquivos Word oficiais (Documento de Visão, Backlog — US10/US11, Termo de Aceite).
- **Timeout do Python e nome das variáveis de ambiente** (`PYTHON_CMD`, `OTIMIZADOR_SCRIPT`): detalhe técnico que o Lucas e o Kauê ajustam quando forem implementar a chamada ao Python. **Decidido em 08/10/2026:** valores ajustados por cada integrante no próprio `.env`; a medição do timeout depende da licença do Lucas e do início dos testes.
- **Consumo de memória do container no Render:** só dá para medir depois que o esqueleto for publicado. **Confirmado em 08/10/2026:** será medido na implementação do deploy.
- **Atualizar a tabela de variáveis do README:** tarefa de acompanhamento, não uma decisão. **Feito em 25/09/2026** — já colada no README. **Revisado em 08/10/2026:** tabela conferida; os nomes `GRB_COMPUTESERVER` e `GRB_SERVERPASSWORD` seguem como proposta até a licença do Lucas chegar.

---

## Atualização de 08/10/2026 — decisões sobre as pendências da `Arquitetura.md` (seção 8)

| Pendência | Decisão | Situação |
|---|---|---|
| Versão exata do Gurobi | **13.0.1** | Fechada |
| Qual licença será usada em produção | A do **Lucas** | Fechada (observações de renovação na `Arquitetura.md`, seção 3.3) |
| Onde o Compute Server de produção roda | Grupo indicou a "Opção A" (container à parte em nuvem), pedindo validação prévia de custo e viabilidade | **Fechada em 08/10/2026:** grupo confirmou a **Alternativa C** (WLS direto no container do backend, sem Compute Server em produção); Opção A descartada |
| Renovação da licença do Lucas | Só necessária se a demonstração cair após 90 dias da geração da licença; uso exclusivamente acadêmico | Respondida (conferir a validade quando a licença chegar) |
| Uso da licença acadêmica no piloto | Uso acadêmico/demonstração se enquadra; operação contínua pelo cliente depois da disciplina não | Respondida (registrar no README que o uso é acadêmico) |
| Nomes de `GRB_COMPUTESERVER` e `GRB_SERVERPASSWORD` | Aguardar a licença do Lucas | Em espera |
| Medir o timeout de verdade | Aguardar a licença do Lucas e o início dos testes (confirmado pelo grupo em 08/10) | Em espera |
| Terminologia do modelo | "Programação Linear Inteira pura" | Fechada |

---

## Depois de preencher

Me avisem quando o quadro estiver completo (mesmo que alguns itens fiquem como "decidir depois", como o item 6) e eu atualizo a seção 8 da `ARQUITETURA.md` com o resultado, junto com qualquer ajuste que isso exija no Roteiro do Hello World.
