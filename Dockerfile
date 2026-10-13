# ==============================================================================
# QueueLess — Virtual Queue Management System Dockerfile
# College DevOps Laboratory Project — 23IT723 (Experiments 6, 7, 8)
# Production Container Configuration using Eclipse Temurin JRE Alpine
# ==============================================================================

# Base Image: Lightweight Alpine-based Java 17 Runtime Environment
FROM eclipse-temurin:17-jre-alpine

# Metadata Labels
LABEL maintainer="DevOps Lab Student <student@college.edu>"
LABEL project="QueueLess"
LABEL version="1.1.0"
LABEL description="Virtual Queue Management System Container with Version History Monitoring"

# Create unprivileged system group and user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set container working directory
WORKDIR /app

# Create persistent data directory for H2 database
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

# Copy the executable Spring Boot application JAR into the runtime container
COPY target/queueless-1.1.0.jar app.jar

# Ensure proper ownership of application files
RUN chown -R appuser:appgroup /app

# Define persistent storage volume for database and release history
VOLUME /app/data

# Switch to unprivileged non-root user
USER appuser

# Expose HTTP port (QueueLess server port)
EXPOSE 8085

# Runtime environment variables
ENV SERVER_PORT=8085 \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_URL="jdbc:h2:file:/app/data/queuelessdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" \
    JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC"

# Healthcheck to verify the virtual queue REST API is responding
HEALTHCHECK --interval=15s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/api/tickets/stats || exit 1

# Launch the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
