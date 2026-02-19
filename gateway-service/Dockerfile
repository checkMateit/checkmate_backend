# 1. 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .

RUN cp -r /app/gradle /app/gateway-service/ 2>/dev/null || :

WORKDIR /app/gateway-service

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

# 2. 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/gateway-service/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]