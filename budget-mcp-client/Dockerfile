# Simple Spring Boot Dockerfile with curl for health checks
FROM amazoncorretto:21-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Find the JAR file and copy it to app.jar
RUN cp target/*.jar app.jar

# Expose port and run
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]