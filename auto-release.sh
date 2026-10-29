#!/bin/bash

# Автоматический релиз SunPortal
# Запускается после каждой успешной сборки APK

set -e

echo "🚀 Запуск автоматического релиза SunPortal..."

# Функция для логирования
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Проверка APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    log "❌ APK не найден: $APK_PATH"
    exit 1
fi

# Получение информации о версии
VERSION_NAME=$(grep "versionName" app/build.gradle.kts | cut -d'"' -f2)
VERSION_CODE=$(grep "versionCode" app/build.gradle.kts | cut -d'=' -f2 | tr -d ' ')
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RELEASE_TAG="v${VERSION_NAME}_${TIMESTAMP}"

log "📱 Версия: $VERSION_NAME (Code: $VERSION_CODE)"
log "🏷️  Тег релиза: $RELEASE_TAG"

# 1. Git коммит и пуш
log "📝 Git commit и push..."
git add -A
git commit -m "Task: Auto-release v${VERSION_NAME} — Automated build and release

- APK version: ${VERSION_NAME} (code: ${VERSION_CODE})
- Build timestamp: $(date '+%Y-%m-%d %H:%M:%S')
- Automated release script
- Updated: app/build.gradle.kts, gradle/libs.versions.toml

[ci skip]" || true

if git push origin main; then
    log "✅ Git push успешен"
else
    log "⚠️  Git push не требуется (нет изменений)"
fi

# 2. Создание GitHub Release
log "📦 Создание GitHub Release..."
if gh release view "$RELEASE_TAG" >/dev/null 2>&1; then
    log "🔄 Обновление существующего релиза: $RELEASE_TAG"
    gh release upload "$RELEASE_TAG" "$APK_PATH" --clobber
else
    log "🆕 Новый релиз: $RELEASE_TAG"
    
    # Генерация changelog
    CHANGELOG=$(git log --oneline -10 | sed 's/^/- /' | tr '\n' '\n')
    
    gh release create "$RELEASE_TAG" "$APK_PATH" \
        --title "Auto-release v${VERSION_NAME} — $(date '+%Y-%m-%d %H:%M')" \
        --notes "🤖 Автоматически сгенерированный релиз

**Версия:** ${VERSION_NAME} (Code: ${VERSION_CODE})
**Время сборки:** $(date '+%Y-%m-%d %H:%M:%S')
**Коммит:** $(git rev-parse HEAD)

**Последние изменения:**
${CHANGELOG}

**Файл:** app-debug.apk ($(du -h "$APK_PATH" | cut -f1))

---
*Этот релиз сгенерирован автоматически скриптом auto-release.sh*" \
        --latest
fi

# 3. Проверка результата
log "🔍 Проверка релиза..."
gh release view "$RELEASE_TAG" --json name,body,createdAt,assets | jq -r '
    "📦 Релиз создан:",
    "  Имя: \(.name)",
    "  Время: \(.createdAt)",
    "  Файлы: \(.assets | length)",
    "  Размер: \(.assets[0].size // \"N/A\")"
'

# 4. Вывод ссылки
RELEASE_URL="https://github.com/Ponamarenko/SunPortal/releases/tag/$RELEASE_TAG"
log "🔗 Ссылка на релиз: $RELEASE_URL"

log "✅ Автоматический релиз завершен успешно!"
echo ""
echo "📱 APK доступен для скачивания:"
echo "   $RELEASE_URL"
echo ""
echo "📱 Для установки на телефон:"
echo "   wget $RELEASE_URL -O sunportal.apk"
echo "   adb install sunportal.apk"