FROM gradle:8.8-jdk17 AS build

WORKDIR /workspace

COPY settings.gradle build.gradle gradle.properties ./
COPY gradle gradle

RUN gradle dependencies --no-daemon || true

COPY . .

ARG SERVICE
RUN gradle :${SERVICE}:bootJar -x test --no-daemon