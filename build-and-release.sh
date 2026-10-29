#!/bin/bash

# Сборка SunPortal с автоматическим релизом
# Использование: ./build-and-release.sh [clean]

set -e

echo "🚀 Сборка SunPortal с автоматическим релизом..."

# Функция для логирования
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Очистка если указан аргумент clean
if [ "$1" = "clean" ]; then
    log "🧹 Очистка перед сборкой..."
    ./gradlew --no-daemon clean
fi

# Сборка APK
log "🔨 Сборка APK..."
if ./gradlew --no-daemon assembleDebug; then
    log "✅ Сборка APK успешна"
    
    # Проверяем существование APK
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        log "📱 APK файл: $APK_PATH"
        log "📊 Размер APK: $(du -h "$APK_PATH" | cut -f1)"
        
        # Автоматический релиз
        log "🚀 Запуск автоматического релиза..."
        ./auto-release.sh
        
    else
        log "❌ APK файл не найден после сборки"
        exit 1
    fi
    
else
    log "❌ Сборка APK не удалась"
    exit 1
fi

log "✅ Полный процесс сборки и релиза завершен!"