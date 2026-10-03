@REM ----------------------------------------------------------------------------
@REM Maven Wrapper script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "WRAPPER_DIR=%~dp0.mvn\wrapper"
set "MAVEN_VERSION=3.9.9"
set "MAVEN_DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_BASE=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MAVEN_HOME=%MAVEN_BASE%\apache-maven-%MAVEN_VERSION%"

if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    goto :RUN_MAVEN
)

echo [INFO] Apache Maven %MAVEN_VERSION% not found locally. Downloading...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$dist = '%MAVEN_BASE%';" ^
    "if (-not (Test-Path $dist)) { New-Item -ItemType Directory -Force -Path $dist | Out-Null };" ^
    "$zip = Join-Path $dist 'apache-maven.zip';" ^
    "Write-Host '[INFO] Downloading Maven from %MAVEN_DIST_URL%...';" ^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
    "(New-Object System.Net.WebClient).DownloadFile('%MAVEN_DIST_URL%', $zip);" ^
    "Write-Host '[INFO] Extracting Maven zip...';" ^
    "Expand-Archive -Path $zip -DestinationPath $dist -Force;" ^
    "Remove-Item $zip -Force;" ^
    "Write-Host '[INFO] Maven setup complete.'"

:RUN_MAVEN
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [ERROR] Failed to locate "%MAVEN_HOME%\bin\mvn.cmd".
    exit /b 1
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%