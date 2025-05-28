FROM node:18-alpine

WORKDIR /app

# Копируем только package.json и package-lock.json
COPY frontend/my-app/package.json frontend/my-app/package-lock.json ./

# Устанавливаем зависимости
RUN npm install

# Копируем остальные файлы
COPY frontend/my-app/ .

# Делаем скрипт deploy.sh исполняемым
RUN chmod +x deploy.sh

EXPOSE 10001

CMD ["./deploy.sh"] 