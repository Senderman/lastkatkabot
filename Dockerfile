FROM gradle:8.13-jdk21-alpine as builder

WORKDIR /app
COPY build.gradle gradle.properties settings.gradle ./
RUN gradle shadowJar -x test --no-daemon
COPY src ./src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

USER 0
RUN addgroup -g 2000 app && adduser -u 2000 -G app -s /bin/sh -D app && mkdir /app && chown 2000:2000 /app
USER 2000
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

