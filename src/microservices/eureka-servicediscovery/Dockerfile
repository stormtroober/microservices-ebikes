# Stage 1: Use the official Gradle image to build the project
FROM gradle:8.11-jdk21 AS build
COPY app/build.gradle.kts settings.gradle.kts gradle.properties ./
COPY app/src src
RUN --mount=type=cache,target=/home/gradle/.gradle/caches gradle bootJar --no-daemon --parallel --build-cache

#RUN ls -la /home/gradle/build/libs

# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /home/gradle/build/libs/eureka-servicediscovery-0.0.1-SNAPSHOT.jar app.jar

# Expose the Eureka port
EXPOSE 8761

# Set the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]