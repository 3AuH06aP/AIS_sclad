Write-Host "================================" -ForegroundColor Cyan
Write-Host "AIS Stock - Backend Launcher" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Set Java Home
$env:JAVA_HOME = "C:\Users\Админ\.jdk\jdk-25.0.2"
Write-Host "[*] Java set to: $env:JAVA_HOME" -ForegroundColor Green
Write-Host ""

# Check if JAR exists
if (-not (Test-Path "target\ais-stock-0.0.1-SNAPSHOT.jar")) {
    Write-Host "[!] JAR not found. Building..." -ForegroundColor Yellow
    & mvn clean package -DskipTests
}

Write-Host "[*] Starting backend on http://localhost:8080" -ForegroundColor Green
Write-Host ""
& java -jar target\ais-stock-0.0.1-SNAPSHOT.jar
