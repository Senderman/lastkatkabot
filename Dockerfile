FROM eclipse-temurin:21-jdk-alpine as builder

RUN mkdir /app
WORKDIR /app
COPY . ./
RUN ./gradlew shadowJar --no-daemon && sh -c "cp build/libs/*.jar ./lastkatkabot.jar"

FROM eclipse-temurin:21-jdk-alpine

USER 0
RUN addgroup -g 2000 lastkatkabot && adduser -u 2000 -G lastkatkabot -s /bin/sh -D lastkatkabot && mkdir /app && chown 2000:2000 /app
USER 2000
WORKDIR /app
COPY --from=builder /app/lastkatkabot.jar ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "lastkatkabot.jar"]
