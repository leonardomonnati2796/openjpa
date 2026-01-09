# Docker Setup for OpenJPA Test Suite

## Quick Start

### Windows (PowerShell/cmd)
```powershell
# Run tests in Docker
./run-docker.bat test

# Or with docker-compose directly
docker-compose up --build
```

### Linux/macOS (Bash)
```bash
# Make script executable
chmod +x run-docker.sh

# Run tests in Docker
./run-docker.sh test
```

## Commands

### Build Docker Image
```bash
# Windows
run-docker.bat build

# Linux/macOS
./run-docker.sh build
```

### Interactive Shell
```bash
# Windows
run-docker.bat shell

# Linux/macOS
./run-docker.sh shell
```

### View Test Reports
After running tests, reports are available in:
- **JaCoCo Coverage**: `openjpa-lib/target/site/jacoco/index.html`
- **Surefire Report**: `openjpa-lib/target/site/surefire-report.html`

## Environment

The Docker container uses:
- **Base Image**: `openjdk:11-slim`
- **Maven**: Latest stable
- **Java Version**: 11 (as per pom.xml)
- **Test Framework**: JUnit 4 + Mockito + JaCoCo

## What Runs Inside

1. Clean Maven cache
2. Compile `openjpa-lib` and `openjpa-slice` modules
3. Run JUnit test suite (3 test types per class):
   - Mock/Stub focused tests
   - Control-flow guided tests
   - LLM-generated scenario tests
4. Generate JaCoCo coverage reports

## Troubleshooting

**Docker not installed?**
- Windows: Download [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Linux: `sudo apt install docker.io docker-compose`

**Permission denied on run-docker.sh?**
```bash
chmod +x run-docker.sh
```

**Tests fail in container?**
- Check Maven version: `docker-compose run openjpa-test mvn -v`
- Inspect container: `docker-compose run --rm openjpa-test bash`
