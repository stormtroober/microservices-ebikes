# Stage 1: Use the official Gradle image to build the project
FROM gradle:8.11-jdk21 AS build
COPY app/build.gradle.kts settings.gradle.kts gradle/ ./
COPY app/src src
RUN gradle clean jar --no-daemon

# Debug step: Verify the built JAR
#RUN ls -la build/libs

# Stage 2: Use a minimal Java runtime image to run the built JAR
FROM openjdk:21-jdk-slim

# Set working directory in runtime stage
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /home/gradle/build/libs/app.jar app.jar

# Debug step: Verify the JAR in the runtime stage
#RUN ls -la /app

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
