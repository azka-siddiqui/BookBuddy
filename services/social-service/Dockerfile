# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy the full reactor so the module can resolve the parent POM.
COPY pom.xml ./
COPY services ./services
RUN mvn -q -pl services/social-service -am -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/services/social-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
