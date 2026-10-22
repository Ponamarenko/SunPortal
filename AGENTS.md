# AGENTS.md — SunPortal

## ПРАВИЛО: после ЛЮБЫХ изменений в проекте SunPortal (код, ресурсы, конфиги) — обязательно синхронизировать с GitHub.

Репозиторий: https://github.com/Ponamarenko/SunPortal
Локально: `C:\Users\xAnder\AndroidProjects\SunPortal`
Ветка: `main` · Remote: `origin` (HTTPS)

### 1. ПРОВЕРКА ИЗМЕНЕНИЙ

```bat
cd C:\Users\xAnder\AndroidProjects\SunPortal
git status
```

### 2. ЕСЛИ ЕСТЬ ИЗМЕНЕНИЯ (modified / new / deleted) — ДОБАВИТЬ

```bat
git add -A
```

ВАЖНО: `.gitignore` уже исключает:

- `_svitgrid_decompile/`, `apks_unzipped/`, `base_unzipped/`, `jadx_output/`, `jadx_base/`
- `assets/svitgrid.ver.*`, `assets/svitgrid-icons/` — чужой код Svitgrid
- `app/build/`, `build/`, `build-logs/`
- `build_now.bat`, `wait_dev.bat`, `check_log.bat`, `shot_fc.bat`, `get_kt_sizes.bat`
- `*.apk`, `*.apks`, `*.dex`, `*.so`
- `.gradle/`, `/local.properties`, `/.idea`, `.cxx/`

### 3. ПРОВЕРИТЬ, ЧТО НЕТ ЧУЖОГО КОДА

```bat
git status
```

Убедиться, что в списке только `app/`, `gradle/`, `*.kts`, `gradlew`, `gradlew.bat`, `screenshots/`, `assets/icon_new.png`.
`assets/svitgrid.*` и `_svitgrid_decompile/` в списке быть НЕ должно.

### 4. КОММИТ (с осмысленным сообщением)

```bat
git commit -m "Task: <краткое описание> — <что изменено>"
```

Примеры:

- `Task: Tariffs redesign — SvitgridCard, JetBrains Mono, calculator`
- `Fix: Forecast Післязавтра color GridColor → BatteryColor`
- `Feature: Weather card on Overview`

### 5. PUSH В GITHUB

```bat
git push origin main
```

Если ошибка `non-fast-forward` (кто-то изменил remote):

```bat
git pull --rebase origin main
git push origin main
```

Если ошибка `rejected` и уверен, что локальная версия правильная:

```bat
git push --force origin main
```

(осторожно: перезапишет remote)

### 6. ПРОВЕРКА

```bat
git log --oneline -3
git status
```

Ожидание: `Your branch is up to date with 'origin/main'` и `nothing to commit, working tree clean`.

## КОГДА ВЫПОЛНЯТЬ

- После ЛЮБОЙ задачи (редизайн экрана, фикс, новая фича).
- После изменения `.gitignore`.
- После добавления/удаления файлов.
- В конце сессии — даже если изменений не было, проверить `git status`.

## ЧЕГО НЕ ДЕЛАТЬ

- НЕ выгружать `_svitgrid_decompile/` и `assets/svitgrid.*` (чужой код).
- НЕ выгружать `build/`, `build-logs/`, `*.apk`, `*.dex`, `*.so`.
- НЕ выгружать локальные `.bat` (`build_now.bat`, `wait_dev.bat`).
- НЕ делать `git add .` без проверки — может затянуть чужой код, если `.gitignore` не сработал.
- НЕ делать `git push --force` без необходимости.

## ОТЧЁТ (в конце задачи)

Добавить раздел **«Git-синхронизация»**:

- `git status` (до add): какие файлы изменены.
- `git commit`: сообщение коммита + хеш.
- `git push`: успех / ошибка.
- `git log --oneline -3`: последние 3 коммита.
- Ссылка: https://github.com/Ponamarenko/SunPortal

## ЕСЛИ ТЕЛЕФОН ОТКЛЮЧЁН

Если задача включает установку APK / скриншот, но телефон не подключён:

1. Сначала сделать git-синхронизацию (код готов — коммит/push).
2. Потом сообщить: «телефон не подключён, установка/скриншот отложены».
3. Когда телефон подключится — доделать установку и скриншот, потом снова `git push` (если менялся код) или просто подтвердить «up to date».

## СБОРКА (Windows)

Gradle не стартует из-под PowerShell без явного `JAVA_HOME`. Используй `build_now.bat` в корне проекта:

```bat
C:\Users\xAnder\AndroidProjects\SunPortal\build_now.bat
```

Он выставляет `JAVA_HOME=C:\Users\xAnder\.jdks\jbr-21.0.11` (JDK 21) и запускает `.\gradlew.bat --no-daemon assembleDebug`.

## ОТЛАДКА ADB

- `timeout /t N` не работает при перенаправлении stdin — используй `ping -n N 127.0.0.1 > nul` вместо пауз.
- `findstr /C:"device"` матчит и слово «devices» в заголовке — для проверки подключения используй `findstr /R /C:"device$"`.
- `git check-ignore -v` возвращает 0 и для негативного паттерна (`!имя`) — проверяй фактическое состояние через `git status --ignored`.
