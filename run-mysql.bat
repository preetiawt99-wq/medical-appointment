@echo off
title Smart Medical Appointment System - MySQL Mode (BCSP-064)
color 0B

echo ========================================================================
echo       SMART MEDICAL APPOINTMENT & RISK PREDICTION SYSTEM
echo             IGNOU BCA Final Year Project (BCSP-064)
echo                      MySQL Production Mode
echo ========================================================================
echo.
echo [1/3] Checking Java environment...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH! Please install Java 17+.
    pause
    exit /b 1
)

echo [2/3] Ensure MySQL Server is running on port 3306 with database 'smartmedical_db'.
echo       Application URL: http://localhost:8080
echo.
echo [3/3] Starting Spring Boot Server with MySQL profile...
call mvnw.cmd spring-boot:run

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application failed to connect to MySQL. Ensure MySQL service is active.
    pause
)
