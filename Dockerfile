# Stage 1: Build
FROM maven:3.9.0-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar pom.xml
COPY pom.xml .

# Baixar dependências
RUN mvn dependency:resolve

# Copiar código fonte
COPY src ./src

# Build do projeto
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar JAR compilado do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Expor porta
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/orders || exit 1

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
