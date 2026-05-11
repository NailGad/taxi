# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/target/user-service-1.0.0.jar app.jar
EXPOSE 8081
ENV SPRING_PROFILES_ACTIVE=docker
HEALTHCHECK --interval=10s --timeout=5s --start-period=120s --retries=12 \
  CMD curl -sf http://127.0.0.1:8081/drivers/999/exists
ENTRYPOINT ["java","-jar","/app/app.jar"]
