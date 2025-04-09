FROM maven:3.8-openjdk-11 as builder

WORKDIR /app

# Copy the pom.xml file first to leverage Docker cache
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src
COPY config ./config

# Build the application
RUN mvn package

# Second stage: minimal runtime image
FROM openjdk:11-jre-slim

# Create a non-root user to run the application
RUN groupadd -r ftoc && useradd -r -g ftoc ftoc

WORKDIR /opt/ftoc

# Copy the built jar from the builder stage
COPY --from=builder /app/target/ftoc-*-jar-with-dependencies.jar ./ftoc.jar
COPY --from=builder /app/config/ftoc-warnings.yml ./config/ftoc-warnings.yml

# Set ownership to the non-root user
RUN chown -R ftoc:ftoc /opt/ftoc

# Switch to the non-root user
USER ftoc

# Expose environment variables to configure the application
ENV FTOC_OUTPUT_FORMAT="text"
ENV FTOC_CONFIG_FILE="/opt/ftoc/config/ftoc-warnings.yml"

# Set entrypoint to run the jar
ENTRYPOINT ["java", "-jar", "ftoc.jar"]

# Default command - can be overridden by docker run command
CMD ["--help"]