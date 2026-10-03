@echo off
title Smart Medical Appointment & Risk Prediction System (BCSP-064)
color 0A

echo ========================================================================
echo       SMART MEDICAL APPOINTMENT & RISK PREDICTION SYSTEM
echo             IGNOU BCA Final Year Project (BCSP-064)
echo       Student: Preeti (2401048429) ^| Guide: Gaurav Dhyani
echo ========================================================================
echo.
echo [1/3] Checking Java environment...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH! Please install Java 17+.
    pause
    exit /b 1
)

echo [2/3] Launching application in Dev Mode (In-memory H2 with MySQL mode)...
echo       Application URL: http://localhost:8080
echo       Press Ctrl+C anytime to stop the server.
echo.
echo [3/3] Starting Spring Boot Server...
call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application terminated with an error.
    pause
)
