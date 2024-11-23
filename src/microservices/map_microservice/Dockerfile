# Stage 1: Use the official Gradle image to build the project
FROM gradle:8.11-jdk21 AS build

COPY app/build.gradle.kts settings.gradle.kts gradle.properties ./
COPY app/src src
RUN --mount=type=cache,target=/home/gradle/.gradle/caches gradle jar --no-daemon --parallel --build-cache

# Stage 2: Use a minimal Java runtime image to run the built JAR
FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl

RUN ls -la
# Set working directory in runtime stage
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /home/gradle/build/libs/app.jar app.jar

EXPOSE 8087

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
