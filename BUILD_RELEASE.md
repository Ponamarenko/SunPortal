# Инструкция по сборке и выпуску релиза SunPortal (Windows 11)

> Процедура повторяется при каждой новой версии. Актуально для `main` ветки репозитория Ponamarenko/SunPortal.

## 1. Перед началом (один раз)

Установить на ПК:

- **JDK 21** (Temurin / JBR 21) — `java -version` должна показывать `21.x.x`
- **Android Studio** (Hedgehog 2023.1+ / Iguana 2023.2+) или только **Command-line Tools**
- **Android SDK**: Platform 34, Platform 36, Build-Tools 34.0.0 / 36.0.0, Platform-Tools, Command-line Tools
- **Git 2.4x+**, **GitHub CLI (gh)** latest

Авторизация GitHub CLI (обязательно перед пушем и релизом):

```powershell
gh auth login
# GitHub.com → HTTPS → Yes → вставить код в браузере
```

## 2. Сборка APK

### 2.1 Подготовка окружения

Клонировать проект (если ещё не клонирован):

```powershell
git clone https://github.com/Ponamarenko/SunPortal.git C:\Users\xAnder\AndroidProjects\SunPortal
cd C:\Users\xAnder\AndroidProjects\SunPortal
```

Создать `local.properties` (файл в `.gitignore`, только локально):

```powershell
Set-Content "local.properties" "sdk.dir=C:\\Users\\xAnder\\AppData\\Local\\Android\\Sdk"
```

Задать переменную окружения (если Android Studio не прописала):

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME","$env:LOCALAPPDATA\Android\Sdk","User")
```

### 2.2 Сборка

Использовать локальный скрипт `build_now.bat` (в корне, в `.gitignore`):

```powershell
cd C:\Users\xAnder\AndroidProjects\SunPortal
.\build_now.bat clean assembleDebug
```

Готовый APK:

```
app\build\outputs\apk\debug\app-debug.apk
```

Проверка:

```powershell
Test-Path app\build\outputs\apk\debug\app-debug.apk   # должно вернуть True
```

## 3. Версионирование (перед каждым релизом)

В `app\build.gradle.kts` изменить:

```kotlin
defaultConfig {
    versionCode = <текущий + 1>
    versionName = "<номер релиза>"
}
```

Пример: `versionCode = 6`, `versionName = "1.0.5"`.

## 4. Выпуск релиза на GitHub

Все команды выполняются в корне проекта.

### 4.1 Синхронизировать с удалённым репозиторием

```powershell
git checkout main
git pull --rebase origin main
```

### 4.2 Закоммитить изменения

```powershell
git add -A
git commit -m "Task: vX.Y.Z — <краткое описание изменений>"
git push origin main
```

### 4.3 Создать GitHub Release

```powershell
gh release create vX.Y.Z app\build\outputs\apk\debug\app-debug.apk `
  --title "vX.Y.Z — <краткое описание>" `
  --notes "<что изменилось>" `
  --latest
```

Пример:

```powershell
gh release create v1.0.4 app\build\outputs\apk\debug\app-debug.apk `
  --title "v1.0.4 — сборка починена + тулчейн SDK 36 / Kotlin 2" `
  --notes "Исправлена сборка main, тулчейн SDK 36 / Kotlin 2.0.21 / Gradle 8.9" `
  --latest
```

### 4.4 Проверить релиз

```powershell
gh release view vX.Y.Z
```

Ссылка: `https://github.com/Ponamarenko/SunPortal/releases/tag/vX.Y.Z`

## 5. Установка на телефон (Nexus 6P)

На телефоне включить **Отладка по USB** (Параметры разработчика → 7 раз тапнуть по «Номер сборки» → включить USB-отладку).

```powershell
adb devices               # устройство должно появиться
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 6. Что НЕ делать

- Не коммитить в репозиторий: `*.apk`, `*.dex`, `*.so`, `build/`, `.gradle/`, `local.properties`, `build_now.bat` (все в `.gitignore`).
- Не использовать `git push --force` без необходимости.
- Не менять `org.gradle.java.home` в `gradle.properties` без согласования — путь жёстко задан в проекте.

## 7. Чек-лист «всё готово»

```powershell
java -version          # 21.x
git --version          # 2.4x
gh auth status         # logged in
adb devices            # телефон видно (перед установкой APK)
.\build_now.bat clean assembleDebug
Test-Path app\build\outputs\apk\debug\app-debug.apk   # True
```

Если все прошли — окружение готово, можно резать релиз.
