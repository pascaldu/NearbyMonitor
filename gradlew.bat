@echo off
setlocal
set "GRADLE_VERSION=9.5.0"
if defined GRADLE_USER_HOME (
  set "BASE_DIR=%GRADLE_USER_HOME%\bootstrap"
) else (
  set "BASE_DIR=%USERPROFILE%\.gradle\bootstrap"
)
set "GRADLE_HOME=%BASE_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP_FILE=%BASE_DIR%\gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%BASE_DIR%" mkdir "%BASE_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile $env:ZIP_FILE; Expand-Archive -Force $env:ZIP_FILE $env:BASE_DIR; Remove-Item $env:ZIP_FILE -Force"
  if errorlevel 1 exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
