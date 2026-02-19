FROM gradle:8.8-jdk17-jammy AS builder
WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew

COPY . .

ARG SERVICE
RUN gradle --no-daemon :${SERVICE}:build -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ARG SERVICE
COPY --from=builder /workspace/${SERVICE}/build/libs/*-SNAPSHOT.jar /app/app.jar


EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]