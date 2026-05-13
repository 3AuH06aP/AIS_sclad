# AIS Stock

Автоматизированная система складского учета на Java Spring Boot и PostgreSQL.

## Стек технологий

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL
- Vue 3 + Vite

## Запуск через Docker

1. Установите Docker Desktop.
2. Выполните в корне проекта:

```bash
docker compose up --build
```

3. Откройте браузер:

- `http://localhost:5173` — фронтенд
- `http://localhost:8080` — backend API

Вместе с проектом загружаются демонстрационные данные: несколько товаров, склады и остатки.

## Локальный запуск

### Backend

```bash
mvn clean package
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Пример API

- `GET /api/products`
- `GET /api/products/summary`
- `POST /api/products`
- `GET /api/warehouses`
- `POST /api/warehouses`
- `GET /api/stock`
- `POST /api/stock`
- `POST /api/stock/{id}/adjust?delta=10`
- `GET /api/overview`
- `POST /api/auth/login`
- `GET /api/users`
- `POST /api/users`

## Админская панель

- Админка доступна по адресу `/admin`
- Управление пользователями: `/admin/users`
- Дефолтные учётные данные администратора:
  - `admin` / `admin`

## Структура

- `src/main/java/com/example/aisstock` — основной код
- `src/main/resources/application.properties` — конфигурация
- `frontend/` — Vue 3 интерфейс
- `docker-compose.yml` — Docker стек
