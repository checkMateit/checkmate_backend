FROM gradle:8.8-jdk17

WORKDIR /workspace

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle build.gradle ./

RUN chmod +x gradlew

# (선택) 의존성 캐시를 위한 워밍업
RUN ./gradlew --no-daemon help

COPY . .

ARG SERVICE
RUN ./gradlew :${SERVICE}:bootJar -x test --no-daemon