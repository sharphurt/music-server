FROM gradle:8.14-jdk21 AS builder
WORKDIR /build

COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle ./gradle

RUN gradle dependencies
COPY src ./src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]