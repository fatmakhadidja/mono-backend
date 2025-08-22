# Stage 1: Build the jar
FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and wrapper first (for caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (for better layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the jar
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the jar
FROM openjdk:17-jdk-slim

WORKDIR /app

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the jar file
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port (this is mainly for documentation)
EXPOSE 8080

# Run the app using Render's PORT env variable
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]