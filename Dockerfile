# ── Stage 1: Build ─────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

# Install Maven (no Maven wrapper in repo; install it directly)
ARG MAVEN_VERSION=3.9.9
RUN apt-get update -q && \
    apt-get install -y --no-install-recommends curl && \
    curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
         -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn && \
    rm /tmp/maven.tar.gz && \
    apt-get purge -y curl && apt-get autoremove -y && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Cache Maven deps separately from source code
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src/ src/
RUN mvn package -DskipTests -q

# Extract layers for faster subsequent image builds
RUN java -Djarmode=tools -jar target/order-service-*.jar extract --destination extracted

# ── Stage 2: Runtime ───────────────────────────────────────────
FROM eclipse-temurin:25-jre AS runtime

LABEL org.opencontainers.image.title="order-service"
LABEL org.opencontainers.image.description="Order lifecycle microservice"
LABEL org.opencontainers.image.vendor="skmcore"

# Non-root user for security
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
WORKDIR /app
RUN chown appuser:appgroup /app
USER appuser

# Copy layered JAR from builder
COPY --from=builder --chown=appuser:appgroup /build/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/extracted/application/ ./

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
