# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# 어떤 서비스 빌드할지
ARG SERVICE

# Gradle 캐시를 위해 먼저 래퍼/설정만 복사
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle* build.gradle* ./

# 멀티모듈 소스 복사
COPY . .

# 서비스만 빌드 (bootJar)
# 예: SERVICE=user-service -> :user-service:bootJar
RUN ./gradlew :${SERVICE}:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

ARG SERVICE

# bootJar 결과물 복사 (파일명은 버전에 따라 다를 수 있어서 *.jar로)
COPY --from=build /workspace/${SERVICE}/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]