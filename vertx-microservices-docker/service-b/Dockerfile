# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Copy the compiled JAR file into the container
COPY app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]