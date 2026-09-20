# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache de dependencias: copia so o pom.xml primeiro
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Agora copia o codigo-fonte e builda o jar
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o jar gerado no build stage
COPY --from=build /app/target/*.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
