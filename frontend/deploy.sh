#!/bin/sh

# Устанавливаем переменные окружения
export VITE_API_URL=https://polniy-bankich.onrender.com:10000

# Собираем и запускаем приложение
npm run build && npm run preview -- --host 0.0.0.0 --port 5173 