FROM python:3.11-slim

# Установка зависимостей
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Копирование файлов приложения
COPY requirements.txt .
COPY deye_gateway.py .
COPY config.yaml .
COPY web_interface.html .

# Установка Python зависимостей
RUN pip install --no-cache-dir -r requirements.txt

# Создание директории для логов
RUN mkdir -p /app/logs

# Открытие портов
EXPOSE 8000

# Запуск приложения
CMD ["python", "deye_gateway.py"]