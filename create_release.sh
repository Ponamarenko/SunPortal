#!/bin/bash

# Скрипт для создания GitHub Release v1.0.19
# Требует предварительной авторизации: gh auth login

set -e

echo "🚀 Создание GitHub Release v1.0.19..."

# Проверка авторизации
if ! gh auth status >/dev/null 2>&1; then
    echo "❌ Не авторизован в GitHub CLI"
    echo "🔑 Пожалуйста, выполните: gh auth login"
    echo "   Или установите токен: export GH_TOKEN=your_token_here"
    exit 1
fi

# Проверка APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "📱 APK не найден, собираем..."
    ./gradlew --no-daemon clean assembleDebug
    echo "✅ APK успешно собран!"
fi

# Проверка существования релиза
if gh release view v1.0.19 >/dev/null 2>&1; then
    echo "📦 Релиз v1.0.19 уже существует, обновляем APK..."
    gh release upload v1.0.19 "$APK_PATH" --clobber
    echo "✅ APK успешно обновлен в релизе!"
else
    echo "📦 Создаем новый релиз v1.0.19..."
    gh release create v1.0.19 "$APK_PATH" \
        --title "v1.0.19 — Enhanced UI components" \
        --notes "Advanced stats, notifications, charts with animations

New components:
- EnhancedStatsCard with progress bars and trends
- EnhancedNotificationItem with animations  
- AnimatedLineChart, BarChart, PieChart
- Updated dependencies and SDK versions
- compileSdk/targetSdk 36
- Kotlin 2.0.21" \
        --latest
    echo "✅ Релиз успешно создан!"
fi

# Проверка результата
echo ""
echo "🔍 Проверка релиза:"
gh release view v1.0.19

echo ""
echo "🔗 Ссылка на релиз:"
echo "https://github.com/Ponamarenko/SunPortal/releases/tag/v1.0.19"

echo ""
echo "✅ Release готов! APK доступен для скачивания."