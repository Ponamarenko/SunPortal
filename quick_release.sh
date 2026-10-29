#!/bin/bash

# Быстрый скрипт для создания GitHub Release
# Запуск: ./quick_release.sh

echo "🚀 Подготовка к созданию GitHub Release v1.0.19..."

# Проверка APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "📱 APK не найден, собираем..."
    ./gradlew --no-daemon clean assembleDebug
    echo "✅ APK успешно собран!"
fi

echo ""
echo "📋 Инструкция:"
echo "1. Авторизуйтесь в GitHub CLI:"
echo "   gh auth login"
echo ""
echo "2. Создайте релиз (выберите один вариант):"
echo ""
echo "   Вариант А - Новый релиз:"
echo "   gh release create v1.0.19 \"$APK_PATH\" \\"
echo "     --title \"v1.0.19 — Enhanced UI components\" \\"
echo "     --notes \"Advanced stats, notifications, charts with animations\\n\\nNew components:\\n- EnhancedStatsCard with progress bars and trends\\n- EnhancedNotificationItem with animations\\n- AnimatedLineChart, BarChart, PieChart\\n- Updated dependencies and SDK versions\\n- compileSdk/targetSdk 36\\n- Kotlin 2.0.21\" \\"
echo "     --latest"
echo ""
echo "   Вариант Б - Обновить существующий:"
echo "   gh release upload v1.0.19 \"$APK_PATH\" --clobber"
echo ""
echo "3. Проверить результат:"
echo "   gh release view v1.0.19"
echo ""
echo "🔗 Ссылка на релиз:"
echo "https://github.com/Ponamarenko/SunPortal/releases/tag/v1.0.19"
echo ""
echo "✅ APK готов к загрузке: $APK_PATH"