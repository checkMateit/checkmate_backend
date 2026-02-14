# 기존: FROM gradle:8.8-jdk17 ...
FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace
ARG http_proxy
ARG https_proxy
ARG no_proxy
ENV http_proxy=${http_proxy} https_proxy=${https_proxy} no_proxy=${no_proxy}
# (선택) 캐시 효율 위해 먼저 build 설정만 복사
COPY settings.gradle build.gradle ./

# gradle wrapper 파일들은 복사해도 되지만, 실행은 gradle로 할 거임
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew

# ✅ 여기서 ./gradlew 쓰지 말고 gradle 사용
RUN gradle --no-daemon help

# 전체 소스 복사 후 서비스 빌드
COPY . .
ARG SERVICE
RUN gradle --no-daemon :${SERVICE}:build -x test