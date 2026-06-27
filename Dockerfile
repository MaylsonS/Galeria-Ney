# Estágio 1: Build (O construtor)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia os arquivos essenciais para baixar as dependências (Layer Caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Executa o download das dependências (isso fica em cache se o pom.xml não mudar)
RUN ./mvnw dependency:go-offline -B

# Copia o código fonte e compila o projeto ignorando os testes por enquanto
COPY src src
RUN ./mvnw package -DskipTests

# Estágio 2: Runtime (A execução leve)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia apenas o .jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]