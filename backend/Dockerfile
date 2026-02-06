# Use Maven image to build the application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy backend pom.xml and source
COPY pom.xml .
COPY src ./src

# Build the application using Spring Boot plugin
RUN mvn clean package spring-boot:repackage -DskipTests

# Use OpenJDK runtime image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built jar file
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
