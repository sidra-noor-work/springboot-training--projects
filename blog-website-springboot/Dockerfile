# Start with OpenJDK 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the built jar into the container
COPY target/blog-website-springboot-0.0.1-SNAPSHOT.jar app.jar

# Expose port (adjust if you use a different one)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
