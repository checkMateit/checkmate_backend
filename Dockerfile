# build stage
FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

COPY . .

ARG SERVICE
RUN chmod +x gradlew \
    && ./gradlew :${SERVICE}:bootJar -x test --no-daemon

# runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

ARG SERVICE
COPY --from=build /workspace/${SERVICE}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]