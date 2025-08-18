# Multi-stage build for Spring Boot application
# Build stage
FROM gradle:8.5-jdk21-alpine AS build

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew build.gradle ./

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Add non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create uploads directory and set permissions
RUN mkdir -p /app/uploads/profile-photos && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]


