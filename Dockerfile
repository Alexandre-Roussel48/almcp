# =============================
# Stage 1 — Build (sbt assembly)
# =============================
FROM eclipse-temurin:21-jdk AS builder

# Install SBT
RUN apt-get update && \
    apt-get install apt-transport-https curl gnupg -yqq && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
    chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
    apt-get update && \
    apt-get install sbt -y

WORKDIR /app

# Cache dependencies
COPY project ./project
COPY build.sbt .

RUN sbt update

# Copy sources
COPY . .

# Assemble fat JAR
RUN sbt assembly

# =============================
# Stage 2 — Runtime (JRE only)
# =============================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Copy assembled JAR
COPY --from=builder /app/target/scala-3.*/almcp-assembly-*.jar /app/almcp.jar

# Pass-through env (default 0)
ENV MCP_DEBUG=0

# Run the MCP server with whatever MCP_DEBUG the user provides
ENTRYPOINT ["sh", "-c", "exec java -jar /app/almcp.jar"]
