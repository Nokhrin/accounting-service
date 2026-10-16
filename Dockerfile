# syntax=docker/dockerfile:1.4

FROM maven:4.0.0-rc-7-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src/ src/
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd -r appgroup && useradd -r -g appgroup app
USER app

COPY --from=build /workspace/target/accounting-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]