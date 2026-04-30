FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
COPY config ./config
RUN mvn -B -q package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -m -u 10001 app
USER app
COPY --from=build /workspace/target/discord-bot-1.0.0.jar /app/bot.jar
ENV AWS_REGION=us-east-1 \
    AWS_SECRET_NAME=220_Discord_Token \
    REDIS_HOST=localhost \
    REDIS_PORT=6379
CMD ["java", "-jar", "/app/bot.jar"]
