# AGENTS.md — SunPortal

# Правило: Git-синхронизация + GitHub Release после каждой задачи

Репозиторий: https://github.com/Ponamarenko/SunPortal
Локально: `C:\Users\xAnder\AndroidProjects\SunPortal`
Ветка: `main` · Remote: `origin` (HTTPS)

## Когда выполнять

После ЛЮБОЙ задачи (редизайн, фикс, новая фича), после правки `.gitignore`, после добавления/удаления файлов, в конце сессии.

## Алгоритм (6 шагов)

### 1. Проверка

```bat
git status
```

### 2. Добавить

```bat
git add -A
```

Проверить, что в индексе нет чужого кода (`_svitgrid_decompile`, `assets/svitgrid.*`, `build/`, `build-logs/`, `*.apk`, `*.dex`, `*.so`, локальные `.bat`).

### 3. Коммит

```bat
git commit -m "Task: <краткое описание> — <что изменено>"
```

Пример: `Task: TOU Svitgrid redesign — SvitgridCard, JetBrains Mono`

### 4. Push

```bat
git push origin main
```

Если `non-fast-forward`:

```bat
git pull --rebase origin main
git push origin main
```

Если `rejected` (уверен, что локальная версия правильная):

```bat
git push --force origin main
```

### 5. GitHub Release (если была пересборка APK)

**5.1. Поднять версию в `app/build.gradle.kts`:**

```kotlin
versionCode = <текущий + 1>
versionName = "1.0.1" → "1.0.2"   // или следующая
```

**5.2. Пересобрать APK:**

```bat
.\gradlew.bat --no-daemon clean assembleDebug
```

**5.3. Создать Release:**

```bat
gh release create vX.Y.Z app\build\outputs\apk\debug\app-debug.apk ^
  --title "vX.Y.Z — <краткое описание>" ^
  --notes "<что изменено>" ^
  --latest
```

**5.4. Проверить:**

```bat
gh release view vX.Y.Z
```

Ссылка: https://github.com/Ponamarenko/SunPortal/releases/tag/vX.Y.Z

### 6. Проверка

```bat
git log --oneline -3
git status
```

Ожидание: `Your branch is up to date with 'origin/main'` и `nothing to commit, working tree clean`.

## Чего не делать

- НЕ выгружать `_svitgrid_decompile/`, `assets/svitgrid.*`, `build/`, `build-logs/`, `*.apk`, `*.dex`, `*.so`, локальные `.bat`.
- НЕ делать `git add .` без проверки (может затянуть чужой код).
- НЕ делать `git push --force` без необходимости.

## Формат отчёта (в конце задачи)

Добавить раздел **«Git-синхронизация»**:

- `git status` (до add): какие файлы изменены.
- `git commit`: сообщение + хеш.
- `git push`: успех / ошибка.
- `git log --oneline -3`: последние 3 коммита.
- GitHub Release: тег, ссылка.
- Ссылка: https://github.com/Ponamarenko/SunPortal

## Если телефон отключён

- Сначала git-синхронизация (код готов — коммит/push).
- Потом сообщить «телефон не подключён, установка/скриншот отложены».
- Когда телефон подключится — доделать установку, скриншот, GitHub Release.

## СБОРКА (Windows)

Gradle не стартует из-под PowerShell без явного `JAVA_HOME`. Используй `build_now.bat` в корне проекта:

```bat
C:\Users\xAnder\AndroidProjects\SunPortal\build_now.bat
```

Он выставляет `JAVA_HOME=C:\Users\xAnder\.jdks\jbr-21.0.11` (JDK 21) и запускает `.\gradlew.bat --no-daemon assembleDebug`.

Для `clean assembleDebug` — тот же bat с заменой задачи на `clean assembleDebug`.

## ОТЛАДКА ADB

- `timeout /t N` не работает при перенаправлении stdin — используй `ping -n N 127.0.0.1 > nul` вместо пауз.
- `findstr /C:"device"` матчит и слово «devices» в заголовке — для проверки подключения используй `findstr /R /C:"device$"`.
- `git check-ignore -v` возвращает 0 и для негативного паттерна (`!имя`) — проверяй фактическое состояние через `git status --ignored`.

## ВЕРСИИ ПРИЛОЖЕНИЯ

Текущая: `versionCode = 3`, `versionName = "1.0.2"` (после bump от 1.0.1).
Версия поднимается при каждой пересборке с новым функционалом; GitHub Release создаётся по тегу `vX.Y.Z`.
