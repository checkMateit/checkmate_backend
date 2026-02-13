FROM gradle:8.8-jdk17 AS build
WORKDIR /workspace

COPY . .

ARG SERVICE
RUN gradle :${SERVICE}:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

ARG SERVICE
COPY --from=build /workspace/${SERVICE}/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]