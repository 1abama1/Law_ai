FROM node:18-alpine

WORKDIR /app

# Копируем только package.json и package-lock.json
COPY frontend/my-app/package.json frontend/my-app/package-lock.json ./

# Устанавливаем зависимости
RUN npm install

# Копируем остальные файлы
COPY frontend/my-app/ .

# Создаем скрипт для запуска
RUN echo '#!/bin/sh\n\
# Устанавливаем переменные окружения\n\
export VITE_API_URL=https://polniy-bankich.onrender.com:10000\n\
\n\
# Собираем и запускаем приложение\n\
npm run build && npm run preview -- --host 0.0.0.0 --port 5173' > deploy.sh && \
chmod +x deploy.sh

EXPOSE 5173

CMD ["./deploy.sh"] 