# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Copy the Gradle wrapper and build files
COPY gradlew gradlew
COPY gradle gradle
COPY app/build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts

COPY app/src src

# Grant execution rights to the Gradle wrapper
RUN chmod +x gradlew

# Build the JAR file
RUN ./gradlew clean jar

# Set the entry point
ENTRYPOINT ["java", "-jar", "build/libs/app.jar"]