@echo off
setlocal
set "APP_HOME=%~dp0"
set "GRADLE_VERSION=9.6.0"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "CACHE_DIR=%GRADLE_USER_HOME%\rivan-wrapper\dists\%GRADLE_VERSION%"
set "GRADLE_HOME=%CACHE_DIR%\gradle-%GRADLE_VERSION%"
if exist "%GRADLE_HOME%\bin\gradle.bat" goto RUN_GRADLE
if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
set "ZIP=%CACHE_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
if not exist "%ZIP%" (
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
  if errorlevel 1 exit /b 1
)
if exist "%GRADLE_HOME%\bin\gradle.bat" goto RUN_GRADLE
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%CACHE_DIR%' -Force"
if errorlevel 1 exit /b 1
:RUN_GRADLE
call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
