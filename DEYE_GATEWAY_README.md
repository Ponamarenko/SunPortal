# Deye Gateway Integration

## 🎯 Описание

Интеграция Deye Gateway Server с SunPortal для управления инверторами Deye SUN-12K-SG02LP1-EU-AM3.

## 📱 Мобильное приложение

Новое мобильное приложение на Kotlin + Jetpack Compose для мониторинга инверторов Deye.

### 🚀 Запуск

```bash
# Сборка приложения
cd deye-mobile-app
./gradlew assembleDebug
```

### 📊 Основные функции

- Real-time мониторинг инвертора
- Управление параметрами
- Энергетическая статистика
- Уведомления об ошибках
- WebSocket интеграция

## 🖥️ Серверная часть

### Запуск сервера

```bash
# Установка зависимостей
pip install -r requirements.txt

# Запуск сервера
python deye_gateway.py
```

### API эндпоинты

- `GET /api/status` - Статус системы
- `GET /api/live` - Live данные
- `GET /api/history` - Исторические данные
- `WebSocket /ws` - Real-time обновления

## 🔧 Конфигурация

Отредактируйте `config.yaml` для настройки:

```yaml
modbus:
  host: "192.168.1.100"  # IP NA111-E конвертера
  port: 502
  slave_id: 1
```

## 📦 Docker развертывание

```bash
# Сборка образа
docker build -t sunportal-deye .

# Запуск
docker run -p 8800:8800 sunportal-deye
```

## 🧪 Тестирование

```bash
# Проверить сервер
python test_server_connection.py
```

## 📄 Документация

- [DEVELOPMENT_LOG.md](DEVELOPMENT_LOG.md) - Лог разработки
- [MIGRATION_REPORT.md](MIGRATION_REPORT.md) - Отчет миграции
- [MOBILE_APP_REPORT.md](MOBILE_APP_REPORT.md) - Отчет о мобильном приложении
- [FINAL_PROJECT_SUMMARY.md](FINAL_PROJECT_SUMMARY.md) - Финальная сводка

## 🚀 Интеграция с SunPortal

Эта интеграция добавляет к SunPortal полную поддержку инверторов Deye:

1. **Мобильное приложение** для мониторинга в реальном времени
2. **Сервер** для сбора данных управления инвертором
3. **API** для интеграции с существующими системами
4. **Веб-интерфейс** для ручного управления

## 🔗 Ссылки

- [GitHub Repository](https://github.com/Ponamarenko/SunPortal/tree/deye-gateway)
- [Release Notes](RELEASE_NOTES.md)
- [Integration Manual](MANUAL_INTEGRATION_STEPS.md)