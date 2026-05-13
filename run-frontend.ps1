Write-Host "================================" -ForegroundColor Cyan
Write-Host "AIS Stock - Frontend Launcher" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check Node.js
try {
    $nodeVersion = & node -v
    Write-Host "[*] Node.js version: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "[!] Node.js не найден!" -ForegroundColor Red
    Write-Host "    Скачайте: https://nodejs.org/" -ForegroundColor Yellow
    Write-Host "    После установки перезагрузите PowerShell" -ForegroundColor Yellow
    exit 1
}

# Navigate to frontend
Push-Location frontend

# Install dependencies if node_modules doesn't exist
if (-not (Test-Path "node_modules")) {
    Write-Host "[*] Установка зависимостей..." -ForegroundColor Yellow
    & npm install
}

Write-Host "[*] Запуск dev-сервера на http://localhost:5173" -ForegroundColor Green
Write-Host ""
& npm run dev

Pop-Location
