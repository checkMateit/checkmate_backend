# 1. 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 루트의 모든 파일을 복사 (gradle 폴더가 포함되도록)
COPY . .

# [핵심] 루트에 있는 gradle 폴더를 store-service 안으로 복사
RUN cp -r /app/gradle /app/store-service/ 2>/dev/null || :

# 작업 디렉토리 이동
WORKDIR /app/store-service

# 권한 부여 및 빌드
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

# 2. 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 jar 파일 복사
COPY --from=build /app/store-service/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
# docker-compose 설정에 맞춘 포트 (8084)
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]