# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy the full reactor so the module can resolve the parent POM.
COPY pom.xml ./
COPY services ./services
RUN mvn -q -pl services/catalog-service -am -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/services/catalog-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
