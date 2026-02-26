# 1. 빌드 스테이지 (루트에서 실행해 루트 build.gradle의 플러그인/버전 적용)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :study-service:clean :study-service:bootJar -x test

# 2. 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/study-service/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
