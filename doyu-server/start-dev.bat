@echo off
cd /d "%~dp0"

docker compose up -d postgres redis
if errorlevel 1 exit /b %errorlevel%

mvn spring-boot:run -Dspring-boot.run.profiles=dev
