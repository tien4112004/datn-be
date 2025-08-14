FROM eclipse-temurin:21 AS builder
WORKDIR /application

# Copy Gradle wrapper and build files
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Copy source code
COPY src/ src/

# Make gradlew executable and build the application
RUN chmod +x gradlew && ./gradlew clean build -x test

# Extract JAR layers for better caching
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

FROM eclipse-temurin:21
WORKDIR /application

# Create a non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy extracted layers from builder stage
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

# Change ownership to the non-root user
RUN chown -R appuser:appgroup /application

# Switch to non-root user
USER appuser

# Expose the port (default Spring Boot port is 8080)
EXPOSE 8080

# Set Spring profile for Docker environment
ENV SPRING_PROFILES_ACTIVE=docker

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
