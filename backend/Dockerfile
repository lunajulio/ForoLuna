# ---- Build: compila el JAR con Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Descarga dependencias primero para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Los tests se ejecutan antes de desplegar (mvnw test); aquí solo se empaqueta
RUN mvn -B -q package -DskipTests

# ---- Runtime: solo el JRE y el JAR ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
