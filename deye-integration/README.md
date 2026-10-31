# Deye Gateway Integration

🎯 **Полная интеграция Deye Gateway Server с SunPortal**

## 📱 Что здесь?

### 📱 Мобильное приложение
- **Путь**: `deye-mobile-app/`
- **Технологии**: Kotlin + Jetpack Compose
- **Функции**: Real-time мониторинг, управление, статистика
- **Сборка**: `cd deye-mobile-app && ./gradlew assembleDebug`

### 🖥️ Серверная часть
- **Файл**: `deye_gateway.py`
- **Технологии**: Python + FastAPI
- **API**: REST + WebSocket
- **Запуск**: `pip install -r requirements.txt && python deye_gateway.py`

### 🔌 Веб-интерфейс
- **Файл**: `web_interface.html`
- **Функции**: Ручное управление, мониторинг
- **Доступ**: Откройте файл в браузере

### ⚙️ Конфигурация
- **Файл**: `config.yaml`
- **Настройка**: IP адрес инвертора, порты, параметры

### 🧪 Тестирование
- **Файл**: `test_server_connection.py`
- **Использование**: `python test_server_connection.py`

### 📦 Docker
- **Файл**: `Dockerfile`
- **Сборка**: `docker build -t deye-gateway .`
- **Запуск**: `docker run -p 8800:8800 deye-gateway`

## 🚀 Быстрый старт

1. **Настройте конфигурацию**:
   ```bash
   nano config.yaml
   # Измените: modbus.host = "192.168.1.100"
   ```

2. **Запустите сервер**:
   ```bash
   pip install -r requirements.txt
   python deye_gateway.py
   ```

3. **Соберите приложение**:
   ```bash
   cd deye-mobile-app
   ./gradlew assembleDebug
   ```

4. **Тестируйте**:
   ```bash
   python test_server_connection.py
   ```

## 📊 API эндпоинты

- `GET /api/status` - Статус системы
- `GET /api/live` - Live данные
- `GET /api/history` - Исторические данные
- `WebSocket /ws` - Real-time обновления

## 🔗 Интеграция с SunPortal

Эта интеграция добавляет к SunPortal полную поддержку инверторов Deye:

- ✅ Мобильное приложение для мониторинга
- ✅ Сервер для сбора данных
- ✅ API для интеграции
- ✅ Веб-интерфейс для управления

## 📄 Документация

- `DEYE_GATEWAY_README.md` - Подробная документация
- `../BUILD_RELEASE.md` - Сборка релизов SunPortal

## 🎯 Поддерживаемые модели

- Deye SUN-12K-SG02LP1-EU-AM3
- Deye SG02LP1 семейство
- Другие инверторы Deye с аналогичной картой регистров

---

🤖 *Сгенерировано автоматически*