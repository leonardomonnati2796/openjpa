@echo off
REM Windows helper script to build and run OpenJPA tests in Docker
REM Usage: run-docker.bat [test|build|shell]

setlocal enabledelayedexpansion

if "%~1"=="" (
    echo.
    echo Usage: run-docker.bat [command]
    echo.
    echo Commands:
    echo   test     - Build and run tests with JaCoCo coverage
    echo   build    - Build Docker image only
    echo   shell    - Run interactive shell in container
    echo   logs     - Show Maven build logs
    echo.
    exit /b 0
)

if "%~1"=="build" (
    echo Building Docker image...
    docker build -t openjpa-test:latest .
    exit /b !ERRORLEVEL!
)

if "%~1"=="test" (
    echo Running tests in Docker container...
    docker-compose up --build
    exit /b !ERRORLEVEL!
)

if "%~1"=="shell" (
    echo Starting interactive shell...
    docker-compose run --rm openjpa-test bash
    exit /b !ERRORLEVEL!
)

if "%~1"=="logs" (
    echo Fetching test logs...
    docker-compose logs openjpa-test
    exit /b !ERRORLEVEL!
)

echo Unknown command: %~1
exit /b 1
