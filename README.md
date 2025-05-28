# Полная докеризация проекта

## Локальный запуск

1. Убедитесь, что Docker установлен.
2. Запустите все сервисы:
   ```bash
   docker compose up --build
   ```
3. Фронтенд: http://localhost:3002
   Бэкенд: http://localhost:3001
   AI сервис: http://localhost:5000

## Деплой на Render

Render не поддерживает docker-compose для мультисервисных проектов. Для деплоя используйте отдельные репозитории или директории для каждого сервиса:

### Backend
- Тип: Web Service
- Dockerfile: backend/Dockerfile
- PORT: 3001
- Переменные окружения:
  - AI_SERVICE_URL (например, https://your-ai-service.onrender.com)
  - DATABASE_URL (от Render Postgres)

### Frontend
- Тип: Web Service
- Dockerfile: frontend/Dockerfile
- PORT: 3000 или 5173 (зависит от вашего build/start)
- Переменные окружения:
  - REACT_APP_API_URL (например, https://your-backend.onrender.com)

### AI Service
- Тип: Web Service
- Dockerfile: ai_service/Dockerfile
- PORT: 5000

### Postgres
- Используйте managed Postgres от Render
- DATABASE_URL будет выдан Render

## Пример .env файла (env.example)

```
REACT_APP_API_URL=https://your-backend.onrender.com
AI_SERVICE_URL=https://your-ai-service.onrender.com
DATABASE_URL=postgres://user:password@host:port/dbname
```

---

**Важно:**
- Все секреты и переменные окружения задаются через Render Dashboard.
- Не храните пароли и ключи в репозитории! 