FROM gradle:8.5-jdk21 AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

# Build without daemon and parallelization to avoid permission issues
RUN ./gradlew bootJar --no-daemon --no-parallel

FROM openjdk:21-jdk-slim
COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
