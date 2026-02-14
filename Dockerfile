FROM gradle:8.8-jdk17 AS build

WORKDIR /workspace

# gradle 캐시 최적화용 (존재하는 파일만 복사)
COPY settings.gradle build.gradle ./
COPY gradle ./gradle
COPY gradlew .

# wrapper 실행 권한
RUN chmod +x gradlew

# 의존성만 먼저 받아서 캐시
RUN ./gradlew dependencies --no-daemon || true

# 전체 소스 복사
COPY . .

ARG SERVICE
RUN ./gradlew :${SERVICE}:bootJar -x test --no-daemon