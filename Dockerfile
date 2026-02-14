FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew
RUN ./gradlew --no-daemon help

COPY . .
ARG SERVICE
RUN ./gradlew --no-daemon :${SERVICE}:bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG SERVICE
COPY --from=builder /workspace/${SERVICE}/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]