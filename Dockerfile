# ── Stage 1: build ──────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia apenas os arquivos de dependências primeiro (cache layer)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e gera o JAR (sem rodar testes)
COPY src/ src/
RUN mvn package -DskipTests -B

# ── Stage 2: runtime ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria o usuário não-root e o diretório de uploads (ainda como root)
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/uploads && \
    chown -R spring:spring /app

# Copia apenas o JAR gerado no stage anterior
COPY --from=build /app/target/*.jar app.jar

# Troca para o usuário não-root
USER spring:spring

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
