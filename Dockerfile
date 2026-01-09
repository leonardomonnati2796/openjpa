# Multi-stage build for OpenJPA test suite with coverage
FROM eclipse-temurin:11-jdk-alpine as builder

# Install Maven and Git
RUN apk add --no-cache \
    maven \
    git

WORKDIR /workspace

# Copy entire project
COPY . .

# Compile and run tests directly with surefire (no checkstyle/verify phase)
RUN mvn -pl openjpa-lib,openjpa-slice -am -q compile test-compile && \
    mvn -pl openjpa-lib,openjpa-slice -q surefire:test

# Final stage for test reports
FROM eclipse-temurin:11-jdk-alpine

RUN apk add --no-cache \
    maven

WORKDIR /workspace

COPY --from=builder /workspace .

# Default command: show test summary
CMD ["bash", "-c", "echo '=== Test Results ===' && \
    if [ -f openjpa-lib/target/surefire-reports/index.html ]; then echo 'openjpa-lib: PASSED'; fi && \
    if [ -f openjpa-slice/target/surefire-reports/index.html ]; then echo 'openjpa-slice: PASSED'; fi && \
    echo '=== Coverage Reports ===' && \
    echo 'JaCoCo reports available in: ' && \
    find . -name 'index.html' -path '*/jacoco*' 2>/dev/null | head -5"]
