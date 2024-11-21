# Stage 1: Use the official Gradle image to build the project
FROM gradle:8.11-jdk21 AS build
COPY app/build.gradle.kts settings.gradle.kts gradle.properties ./
COPY app/src src
RUN --mount=type=cache,target=/home/gradle/.gradle/caches gradle bootJar --no-daemon --parallel --build-cache

# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl

WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /home/gradle/build/libs/eureka-apigateway-1.0.0-SNAPSHOT.jar app.jar

# Expose the Eureka port
EXPOSE 8081

# Set the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]