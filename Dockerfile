# Use a JDK 21 base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled classes from your local target directory to the container
COPY target/classes /app/classes

# Copy any required configuration files (like application.properties)
COPY target/classes/application.properties src/main/resources/application.properties

# Run the application
CMD ["java", "-cp", "/app/classes", "app.Application"]