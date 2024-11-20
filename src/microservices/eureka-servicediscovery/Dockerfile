# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

# Add a volume for logs
VOLUME /tmp

# Copy the application JAR to the container
COPY build/libs/eureka-server-0.0.1-SNAPSHOT.jar app.jar

# Expose the Eureka port
EXPOSE 8761

# Set the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "/app.jar"]
