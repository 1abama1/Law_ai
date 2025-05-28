#!/bin/sh

# Устанавливаем переменные окружения
export NODE_ENV=development
export VITE_API_URL=https://polniy-bankich.onrender.com:10000
export VITE_AI_SERVICE_URL=https://ai-service-ziam.onrender.com
export VITE_API_SOCKET_URL=https://polniy-bankich.onrender.com:10000

# Собираем приложение
NODE_ENV=development npm run build

# Для локальной разработки используем preview
npm run preview -- --host 0.0.0.0 --port 10001 