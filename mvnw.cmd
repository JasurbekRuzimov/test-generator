@echo off
setlocal
set BASEDIR=%~dp0
set WRAPPER_DIR=%BASEDIR%\.mvn\wrapper
set PROPS_FILE=%WRAPPER_DIR%\maven-wrapper.properties
set JAR_FILE=%WRAPPER_DIR%\maven-wrapper.jar

for /f "usebackq tokens=1* delims==" %%A in ("%PROPS_FILE%") do (
  if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
)

if not exist "%JAR_FILE%" (
  echo Downloading Maven Wrapper...
  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%JAR_FILE%'" || exit /b 1
)

java -classpath "%JAR_FILE%" -Dmaven.multiModuleProjectDirectory="%BASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
