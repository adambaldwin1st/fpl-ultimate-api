FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build target directory into the container
COPY target/fpl-ultimate-1.0.0.jar /app/fpl-ultimate-1.0.0.jar

# Expose the port your application listens on
EXPOSE 8080

# Specify the command to run your application
CMD ["java", "-jar", "fpl-ultimate-1.0.0.jar"]
