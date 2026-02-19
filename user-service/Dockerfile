# 1. 빌드 스테이지
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 루트의 모든 파일을 복사 (gradle 폴더가 포함되도록)
COPY . .

# [핵심] 만약 루트에만 gradle 폴더가 있다면 user-service 안으로 복사해줍니다.
# 이미 있다면 덮어쓰기 되므로 안전합니다.
RUN cp -r /app/gradle /app/user-service/ 2>/dev/null || :

# 작업 디렉토리 이동
WORKDIR /app/user-service

# 권한 부여 및 빌드
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

# 2. 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 jar 파일 복사
COPY --from=build /app/user-service/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]