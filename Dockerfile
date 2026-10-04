# ---- Build stage: compile and package the Spring Boot JAR ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Download dependencies first so they are cached between builds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Run stage: small Java 17 runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --create-home appuser
COPY --from=build /app/target/smart-medical-appointment-system-1.0.0.jar app.jar
USER appuser

# The application listens on $PORT (set by Render) and falls back to 8080
EXPOSE 8080

# Keep the heap within the container's memory limit (free instances are small)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseSerialGC", "-jar", "app.jar"]
