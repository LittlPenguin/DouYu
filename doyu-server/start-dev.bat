@echo off
cd /d "%~dp0"

set "DOUYU_ENV_FILE=%~dp0..\.env"
if exist "%DOUYU_ENV_FILE%" (
    echo Loading %DOUYU_ENV_FILE%
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%DOUYU_ENV_FILE%") do (
        if not "%%A"=="" set "%%A=%%B"
    )
)

if not defined DOUYU_BACKEND_HOST set "DOUYU_BACKEND_HOST=localhost"
if not defined DOUYU_BACKEND_PORT set "DOUYU_BACKEND_PORT=8081"
if not defined DOUYU_STORAGE_BASE_URL set "DOUYU_STORAGE_BASE_URL=http://%DOUYU_BACKEND_HOST%:%DOUYU_BACKEND_PORT%"
if not defined DOUYU_SERVER_ADDRESS set "DOUYU_SERVER_ADDRESS=0.0.0.0"

docker compose up -d postgres redis
if errorlevel 1 exit /b %errorlevel%

mvn spring-boot:run -Dspring-boot.run.profiles=dev
