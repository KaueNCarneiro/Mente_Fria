# Validação do carregamento do `.env` no backend — pedido para o Lucas

**Para:** Lucas Gabriel Valadares Bassi (backend) · **De:** Diogo · **Data:** 08/10/2026
**Tempo estimado:** 15 a 20 minutos · **Sistema:** Windows (PowerShell). Os comandos são os mesmos no Linux/macOS, exceto onde indicado.

## 1. Resumo (o que estamos pedindo)

Queremos saber se um colega que clonar o repositório do zero consegue subir o backend **só com um arquivo `.env`**, sem depender das variáveis que você configurou na execução do IntelliJ. Pela nossa análise do `application.properties`, isso **provavelmente não funciona** quando o backend é iniciado de dentro da pasta `backend` (como o README manda). Precisamos que você rode dois testes curtos e nos diga o resultado e qual padrão a equipe deve adotar.

## 2. O que o código faz hoje

Em `backend/src/main/resources/application.properties`:

```properties
spring.config.import=optional:file:./backend/.env[.properties]
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:mente_fria}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

- A primeira linha manda o Spring ler um arquivo `.env` e usar as variáveis dele (`DB_USER`, `DB_PASSWORD` etc.).
- O `./` significa **"a pasta de onde o comando foi executado"** (o *diretório de trabalho*), e não a pasta onde está o `application.properties`.
- `optional:` significa que, **se o arquivo não for encontrado, o Spring não avisa e segue em frente**.

## 3. A suspeita

O README manda rodar `.\mvnw spring-boot:run` **dentro da pasta `backend`**. Nesse caso, `./backend/.env` aponta para `backend\backend\.env`, que não existe. Como o `optional:` esconde a falha, o Spring segue sem ler o `.env`, e então aparece o erro:

```
Could not resolve placeholder 'DB_USER' in value "${DB_USER}"
```

Pelo que você nos disse, o seu ambiente funciona porque as variáveis (`DB_USER`, `DB_PASSWORD` etc.) estão definidas na **configuração de execução do IntelliJ**, e não no `.env`. Isso explica por que funciona para você e pode falhar para os outros.

## 4. Por que isso importa

- As configurações de execução do IntelliJ ficam na pasta `.idea/`, que o `.gitignore` do projeto **ignora**. Quem clonar o repositório **não recebe** as suas variáveis.
- O README (passo 3) manda criar um `.env`. Se o backend não o lê, o passo não funciona e o item CT20 do Plano de Testes (subir o projeto do zero seguindo o README) falha.
- **No Render, nada muda:** as variáveis são definidas no painel do serviço, e a linha `optional:` é inofensiva. O risco é só para o desenvolvimento local.

## 5. Antes de começar

1. **Pare o backend** que estiver rodando (a porta 8080 precisa estar livre).
2. **Deixe o banco no ar:** `docker start mente-fria-db` (ou o seu PostgreSQL local, com o banco `mente_fria` e o `Script_DDL.sql` carregado).
3. Confirme o Java: `java -version` deve mostrar a versão **25**.
4. **Não use o IntelliJ nos testes A e B.** Use o terminal, para simular uma máquina sem as suas configurações.

## 6. Teste A — como outra pessoa usaria hoje (esperamos que FALHE)

1. Clone em uma pasta **fora** do seu repositório de trabalho:
   ```powershell
   git clone https://github.com/KaueNCarneiro/Mente_Fria.git C:\temp\mf-teste
   ```
2. Confirme que **não há variáveis `DB_*` definidas** na sessão nem no Windows. Elas poderiam mascarar o resultado:
   ```powershell
   Get-ChildItem Env:DB_*
   [Environment]::GetEnvironmentVariable("DB_USER","User")
   [Environment]::GetEnvironmentVariable("DB_USER","Machine")
   ```
   Os três comandos devem voltar vazios. Se algum mostrar valor, anote e remova antes de seguir.
3. Entre na pasta do backend e crie o `.env`. **Use `-Encoding ascii`**: com `utf8`, o PowerShell 5 grava um caractere invisível no início do arquivo e a primeira variável deixa de ser reconhecida.
   ```powershell
   cd C:\temp\mf-teste\backend
   @"
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=mente_fria
   DB_USER=postgres
   DB_PASSWORD=SUA_SENHA_LOCAL
   "@ | Set-Content -Encoding ascii .env
   ```
4. Suba o backend (a primeira execução baixa o Maven, pode demorar):
   ```powershell
   .\mvnw spring-boot:run
   ```
5. **Anote o resultado** e, se der erro, **tire um print** da mensagem final.

## 7. Teste B — com a correção proposta (esperamos que FUNCIONE)

1. Pare o backend do Teste A (`Ctrl + C`).
2. No **clone** (`C:\temp\mf-teste`), edite `backend\src\main\resources\application.properties` e troque a primeira linha por:
   ```properties
   spring.config.import=optional:file:./.env[.properties]
   ```
   (isto lê o `.env` da própria pasta de onde o comando é executado, ou seja, `backend`.)
3. Rode de novo, dentro de `backend`:
   ```powershell
   .\mvnw spring-boot:run
   ```
4. Em outro terminal, teste:
   ```powershell
   Invoke-RestMethod http://localhost:8080/api/saude
   Invoke-RestMethod http://localhost:8080/api/ingredientes
   ```
   Esperado: `status ok, banco ok` e uma lista com 7 ingredientes.

## 8. Teste C — no IntelliJ, no seu repositório de trabalho (só se o B funcionar)

Queremos garantir que, depois da correção, o IntelliJ também leia o `.env`.

1. No seu projeto, crie `backend\.env` com o mesmo conteúdo do passo 6.3.
2. Abra **Run > Edit Configurations**, selecione a configuração do Spring Boot e **apague as variáveis** `DB_*` do campo *Environment variables*.
3. Verifique o campo **Working directory**: ele deve apontar para a pasta `backend`. Se não aparecer, procure em *Modify options*. O nome exato dos menus varia conforme a versão do IntelliJ.
4. Execute pelo IntelliJ e teste `/api/saude`.

## 9. Como interpretar os resultados

| Teste A | Teste B | O que significa | O que fazer |
|---|---|---|---|
| Falha com `Could not resolve placeholder` | Funciona | Suspeita confirmada; a correção resolve | Adotar o padrão da seção 10 (opção 2) e abrir PR |
| Funciona | — | O `./backend/.env` está resolvendo na sua máquina por outro motivo | Descrever como foi iniciado o backend; não mudamos o código e ajustamos só o README |
| Falha | Falha | Há outro problema (senha errada, banco desligado, BOM no arquivo, variáveis do Windows mascarando) | Enviar o print do erro completo para investigarmos |

## 10. Decisão que pedimos ao Lucas

Escolha **um** padrão para a equipe. Nós recomendamos a **opção 2**.

| # | Opção | Prós | Contras |
|---|---|---|---|
| 1 | Só variáveis na execução do IntelliJ (como hoje) | Nada a mudar no código | Não é reprodutível; o README fica incorreto; não funciona no terminal |
| **2** | **`.env` dentro de `backend/`, lido por `optional:file:./.env[.properties]`; sempre rodar de dentro de `backend`** | Já está no `.gitignore` (`backend/.env`); coincide com o que o README manda; uma linha a mudar | Exige rodar de dentro de `backend` (no IntelliJ, o *Working directory* deve ser `backend`) |
| 3 | `.env` na raiz do repositório, lido por `../.env` | Um `.env` para todo o projeto | O `.gitignore` atual **não ignora** um `.env` na raiz: haveria risco de commitar senha |

Depois da decisão, nós atualizamos o README (indicando a pasta do `.env`) e o `.env.example` (que será criado em `backend/`, em PR separado).

## 11. O que responder ao Diogo

1. Resultado do **Teste A** (funcionou ou falhou; print do erro, se falhou).
2. Resultado do **Teste B**.
3. Se fez o **Teste C**: o *Working directory* do IntelliJ estava correto?
4. Qual opção da seção 10 você escolhe.

## 12. Limpeza

Depois dos testes, apague o clone temporário: `Remove-Item -Recurse -Force C:\temp\mf-teste`. Nunca comite o `.env` (a senha é só sua).
