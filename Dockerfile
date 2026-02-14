FROM gradle:8.8-jdk17 AS build
WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY gradle ./gradle
COPY . .

ARG SERVICE
RUN gradle :${SERVICE}:bootJar -x test --no-daemon