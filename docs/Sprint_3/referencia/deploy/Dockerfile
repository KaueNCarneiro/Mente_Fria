# syntax=docker/dockerfile:1
# Mente Fria — backend Java + módulos Python (Gurobi) num único container (Arquitetura: Alternativa C).
# Contexto do build = RAIZ do repositório:   docker build -t mente-fria-backend .
# Nenhum segredo entra na imagem: banco e JWT_SECRET chegam por variáveis de ambiente do Render.
# Licença do Gurobi: a limitada do pip, que vem com o gurobipy instalado abaixo (autorizada pela Gurobi, A38); sem GRB_*.

# ---- 1. Compilar o backend (JDK 25 + Maven wrapper do projeto)
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /src/backend
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline          # camada de dependências: só muda quando o pom.xml muda
COPY backend/src src
RUN ./mvnw -q -B -Dmaven.test.skip=true package && cp target/backend-*.jar /src/backend.jar

# ---- 2. Imagem final: Python 3.14 (para o Gurobi) + JRE 25 copiado da imagem oficial Temurin
FROM python:3.14-slim-trixie
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=eclipse-temurin:25-jre-noble /opt/java/openjdk /opt/java/openjdk

# Mesma versão do optimization/requirements.txt (decisão P2). Só o gurobipy: pytest não vai para produção.
ARG GUROBIPY_VERSAO=13.0.3
RUN pip install --no-cache-dir "gurobipy==${GUROBIPY_VERSAO}"

RUN useradd --create-home --uid 10001 app
WORKDIR /app
COPY optimization/app optimization/app
COPY --from=build /src/backend.jar backend.jar
USER app

# Plano free do Render: 512 MB para Java + Python juntos. Heap limitada para sobrar memória ao processo do Gurobi.
ENV PYTHON_CMD=python3 \
    OTIMIZADOR_SCRIPT=/app/optimization/app/otimizador.py \
    REPOSICAO_SCRIPT=/app/optimization/app/reposicao.py \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 10000
# Forma "exec" (lista): o Java recebe o SIGTERM do Render direto e desliga com calma.
CMD ["java", "-jar", "/app/backend.jar"]
