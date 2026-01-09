# Multi-stage build for OpenJPA test suite with coverage
FROM openjdk:11-slim as builder

# Install Maven
RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Copy entire project
COPY . .

# Build and run tests with coverage
RUN mvn -q clean && \
    mvn -pl openjpa-lib,openjpa-slice -am clean test jacoco:report

# Final stage for test reports
FROM openjdk:11-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

COPY --from=builder /workspace .

# Default command: show test summary
CMD ["bash", "-c", "echo '=== Test Results ===' && \
    if [ -f openjpa-lib/target/surefire-reports/index.html ]; then echo 'openjpa-lib: PASSED'; fi && \
    if [ -f openjpa-slice/target/surefire-reports/index.html ]; then echo 'openjpa-slice: PASSED'; fi && \
    echo '=== Coverage Reports ===' && \
    echo 'JaCoCo reports available in: ' && \
    find . -name 'index.html' -path '*/jacoco*' 2>/dev/null | head -5"]
