#!/bin/bash
# Linux/macOS helper script to build and run OpenJPA tests in Docker
# Usage: ./run-docker.sh [test|build|shell]

set -e

if [ -z "$1" ]; then
    cat << EOF

Usage: ./run-docker.sh [command]

Commands:
  test     - Build and run tests with JaCoCo coverage
  build    - Build Docker image only
  shell    - Run interactive shell in container
  logs     - Show Maven build logs
  clean    - Remove Docker containers and images

EOF
    exit 0
fi

case "$1" in
    build)
        echo "Building Docker image..."
        docker build -t openjpa-test:latest .
        ;;
    test)
        echo "Running tests in Docker container..."
        docker-compose up --build
        ;;
    shell)
        echo "Starting interactive shell..."
        docker-compose run --rm openjpa-test bash
        ;;
    logs)
        echo "Fetching test logs..."
        docker-compose logs openjpa-test
        ;;
    clean)
        echo "Cleaning up Docker resources..."
        docker-compose down -v
        docker rmi openjpa-test:latest 2>/dev/null || true
        echo "Cleanup complete"
        ;;
    *)
        echo "Unknown command: $1"
        exit 1
        ;;
esac
