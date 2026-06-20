# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


# Run stage
FROM eclipse-temurin:21-jdk

# dependencies
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV POSTGRES_DATA=/app/db
ENV MEDIA_DATA=/app/data/media
ENV STREAM_DATA=/app/data/media/streams

VOLUME ["app/db", "app/data/media/streams", "/app/data/media"]

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]