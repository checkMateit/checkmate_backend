# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# 1) gradle wrapper 먼저 복사 (캐시)
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle* build.gradle* ./

# 2) 네트워크 타임아웃 크게
ENV GRADLE_OPTS="-Dorg.gradle.internal.http.connectionTimeout=600000 -Dorg.gradle.internal.http.socketTimeout=600000"
ENV GRADLE_USER_HOME=/workspace/.gradle

# 3) 소스 복사
COPY . .

ARG SERVICE
RUN chmod +x gradlew && ./gradlew :${SERVICE}:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

ARG SERVICE
COPY --from=build /workspace/${SERVICE}/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]