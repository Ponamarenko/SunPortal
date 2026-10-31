# 🚀 Ручная интеграция Deye Gateway в SunPortal

## 📁 Шаг 1: Загрузка файлов

1. Скачайте архив: `sunportal-deye-integration.tar.gz`
2. Распакуйте его в корень вашего SunPortal репозитория

## 🌿 Шаг 2: Создание ветки

1. Перейдите в корень вашего SunPortal репозитория
2. Создайте новую ветку:
   ```bash
   git checkout -b deye-gateway
   ```

## 📦 Шаг 3: Добавление файлов

1. Распакуйте архив:
   ```bash
   tar -xzf sunportal-deye-integration.tar.gz
   ```

2. Добавьте файлы в Git:
   ```bash
   git add mobile-app/
   git add deye_gateway.py
   git add config.yaml
   git add web_interface.html
   git add requirements.txt
   git add test_server_connection.py
   git add Dockerfile
   git add *.md
   ```

## 💾 Шаг 4: Коммит и пуш

```bash
git commit -m "Add Deye Gateway integration

- Added Deye Gateway server for SUN-12K-SG02LP1-EU-AM3 inverter
- Added mobile app on Kotlin + Jetpack Compose
- Added REST API + WebSocket integration
- Added web interface for manual control
- Added Docker deployment support
- Added comprehensive documentation"

git push -u origin deye-gateway
```

## 📱 Шаг 5: Сборка мобильного приложения

1. Откройте `mobile-app/` в Android Studio
2. Синхронизируйте проект Gradle
3. Соберите APK:
   ```bash
   cd mobile-app
   ./gradlew assembleDebug
   ```

## 🖥️ Шаг 6: Запуск сервера

```bash
# Установка зависимостей
pip install -r requirements.txt

# Запуск сервера
python deye_gateway.py
```

## 🔧 Шаг 7: Настройка

1. Отредактируйте `config.yaml`:
   ```yaml
   modbus:
     host: "192.168.1.100"  # IP вашего NA111-E
     port: 502
     slave_id: 1
   ```

2. Проверьте работу:
   ```bash
   python test_server_connection.py
   ```

## 🎯 Структура проекта

```
SunPortal/
├── mobile-app/              # Мобильное приложение
├── deye_gateway.py         # Сервер
├── config.yaml             # Конфигурация
├── web_interface.html      # Веб-интерфейс
├── requirements.txt        # Зависимости
├── test_server_connection.py # Тестирование
├── Dockerfile             # Docker
└── *.md                  # Документация
```

## 📊 API интеграция

Сервер предоставляет API совместимое с SunPortal:

- `GET /api/status` - Статус системы
- `GET /api/live` - Live данные
- `GET /api/history` - Исторические данные
- `WebSocket /ws` - Real-time обновления

## 🚀 Готово!

Теперь у вас есть полная интеграция Deye Gateway с SunPortal!

📱 Мобильное приложение готово к использованию
🖥️ Сервер готов к запуску
🔗 Ветка готова к Pull Request
