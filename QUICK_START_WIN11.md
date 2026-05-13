# 🚀 Быстрый старт AIS Stock (Windows 11)

## ⚠️ Важно: Перед началом

1. **PostgreSQL** должна быть установлена и запущена
2. **Java 25** установлена (проверено в проекте)
3. **Maven** установлен (проверено в проекте)
4. **Node.js 18+** и **npm** (нужно установить отдельно)

## Если Node.js не установлен

1. Откройте https://nodejs.org/
2. Скачайте LTS-версию (рекомендуется)
3. Установите с дефолтными параметрами
4. **Перезагрузите PowerShell** (важно!)
5. Проверьте: `node -v` и `npm -v`

---

## 🏃 Как запустить (короткая версия)

### 1️⃣ Backend (PowerShell терминал #1)

```powershell
# Перейти в корень проекта
cd C:\Users\Админ\Documents\AIS_stock

# Запустить backend
.\run-backend.ps1
```

Ждите, пока появится сообщение вроде:
```
Tomcat started on port(s): 8080 (http)
Started AisStockApplication in X.XXX seconds
```

### 2️⃣ Frontend (PowerShell терминал #2)

```powershell
# Перейти в корень проекта
cd C:\Users\Админ\Documents\AIS_stock

# Запустить frontend
.\run-frontend.ps1
```

Ждите, пока появится:
```
  VITE v5.X.X  ready in XXX ms
  ➜  Local:   http://localhost:5173/
```

### 3️⃣ Откройте браузер

1. Перейдите на `http://localhost:5173`
2. Введите для входа:
   - **Логин:** admin
   - **Пароль:** admin
3. Нажмите "Войти"

✅ **Готово!** Вы в приложении.
### Админка

- После входа администратор будет перенаправлен на `/admin`
- Управление пользователями доступно по `/admin/users`
---

## 🐳 Запуск через Docker

Если у вас установлен Docker Desktop, используйте более простой способ:

```powershell
docker compose up --build
```

- Фронтенд: `http://localhost:5173`
- Backend API: `http://localhost:8080`

Приложение автоматически инициализирует демонстрационные данные при первом запуске.

---

## 📋 Полная инструкция (пошагово)

### Шаг 1. Подготовка PostgreSQL

1. Откройте PowerShell как администратор
2. Подключитесь к PostgreSQL:

```powershell
psql -U postgres -h localhost
```

3. Создайте БД:

```sql
CREATE DATABASE ais_stock;
\q
```

### Шаг 2. Установка Node.js

1. Скачайте: https://nodejs.org/ (LTS)
2. Установите
3. Перезагрузите PowerShell
4. Проверьте:

```powershell
node -v
npm -v
```

### Шаг 3. Сборка Backend

```powershell
cd C:\Users\Админ\Documents\AIS_stock
mvn clean package -DskipTests
```

Результат: `target\ais-stock-0.0.1-SNAPSHOT.jar`

### Шаг 4. Запуск Backend

**Вариант А (автоматически через скрипт):**

```powershell
.\run-backend.ps1
```

**Вариант Б (вручную):**

```powershell
$env:JAVA_HOME = 'C:\Users\Админ\.jdk\jdk-25.0.2'
java -jar target\ais-stock-0.0.1-SNAPSHOT.jar
```

### Шаг 5. Запуск Frontend

**Вариант А (автоматически через скрипт):**

```powershell
.\run-frontend.ps1
```

**Вариант Б (вручную):**

```powershell
cd frontend
npm install
npm run dev
```

### Шаг 6. Откройте приложение

1. Браузер: `http://localhost:5173`
2. Логин: `admin` / Пароль: `admin`

---

## 🧪 Проверка портов

```powershell
# Backend на 8080?
Test-NetConnection -ComputerName localhost -Port 8080

# Frontend на 5173?
Test-NetConnection -ComputerName localhost -Port 5173
```

---

## 🛑 Остановка

- **Backend:** закрыть PowerShell терминал или `Ctrl+C`
- **Frontend:** закрыть PowerShell терминал или `Ctrl+C`

---

## 🐛 Проблемы?

### "mvn is not recognized"
→ Maven в PATH? Проверьте: `mvn -v`

### "java is not recognized"
→ JAVA_HOME установлена? Проверьте скрипт `run-backend.ps1`

### "npm is not recognized"
→ Node.js установлен? Переустановите и **перезагрузите PowerShell**

### Backend ошибка: "Connection refused"
→ PostgreSQL запущена? Проверьте: `psql -U postgres -h localhost`

### Frontend: "Не загружаются товары"
→ Backend работает? Проверьте `http://localhost:8080/api/status`

---

## 📚 Дополнительно

- **API документация:** см. README.md
- **Backend код:** `src/main/java/com/example/aisstock`
- **Frontend код:** `frontend/src`

Приятного использования! 🎉
