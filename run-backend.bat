@echo off
echo ================================
echo AIS Stock - Backend Launcher
echo ================================
echo.

REM Set Java Home
set JAVA_HOME=C:\Users\Админ\.jdk\jdk-25.0.2
echo [*] Java set to: %JAVA_HOME%
echo.

REM Check if target jar exists
if not exist "target\ais-stock-0.0.1-SNAPSHOT.jar" (
  echo [!] JAR not found. Building...
  mvn clean package -DskipTests
)

echo [*] Starting backend on http://localhost:8080
java -jar target\ais-stock-0.0.1-SNAPSHOT.jar
