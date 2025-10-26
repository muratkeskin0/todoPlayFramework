# Simple Dockerfile - build locally first
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install curl and unzip for health checks and extraction
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

# Copy the pre-built application (build locally first)
COPY target/universal/todo-1.0-SNAPSHOT.zip ./
RUN unzip todo-1.0-SNAPSHOT.zip && \
    mv todo-1.0-SNAPSHOT/* . && \
    rm -rf todo-1.0-SNAPSHOT todo-1.0-SNAPSHOT.zip

# Create logs directory
RUN mkdir -p /app/logs

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m -Dlogback.configurationFile=conf/logback-docker.xml"
ENV PLAY_HTTP_SECRET_KEY="your-secret-key-change-in-production"

# Expose port
EXPOSE 9000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:9000/ || exit 1

# Run the application
CMD ["bin/todo", "-Dplay.http.secret.key=your-secret-key-change-in-production"]
