FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
ENV GRADLE_USER_HOME=/tmp/.gradle-cache
RUN ./gradlew build -x test

FROM eclipse-temurin:25-jre-alpine
LABEL authors="spartanhooah"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]