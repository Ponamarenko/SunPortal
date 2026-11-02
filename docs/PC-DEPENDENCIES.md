# Установка зависимостей для работы над SunPortal (Windows 11)

Полная инструкция: что поставить на компьютер, чтобы собрать проект, залить APK на телефон
и выложить релиз в GitHub.

Проект — Android (Kotlin + Jetpack Compose), репозиторий: <https://github.com/Ponamarenko/SunPortal>

---

## 0. Коротко — что нужно

| Что | Версия | Зачем | Обязательно? |
|---|---|---|---|
| JDK | **21** (JetBrains Runtime / Temurin) | Gradle + AGP | да |
| Android Studio | Hedgehog 2023.1+ / Iguana 2023.2+ | Android SDK, Platform-Tools (adb) | да |
| Android SDK Platform | **34** (текущий `main`), **36** (после PR #2) | `compileSdk` | да |
| Android SDK Build-Tools | **34.0.0** (после PR #2 — 36.0.0) | сборка | да |
| Android SDK Platform-Tools | последняя | `adb`, установка на телефон | да |
| Git | 2.4x+ | `clone` / `pull` / `push` | да |
| GitHub CLI (`gh`) | последняя | `gh pr merge`, `gh release create` | для PR/релиза |
| Gradle | **не ставить** | тулчейн качает сам (8.7 / 8.9) | — |

Места на диске: **~5–6 ГБ** (SDK + Gradle-кэш + зависимости).
Нужен интернет при первой сборке — скачается Gradle, AGP и все библиотеки.

---

## 1. JDK 21

В `gradle.properties` проекта прописан конкретный путь к JDK:

```properties
org.gradle.java.home=C:\\Users\\xAnder\\.jdks\\jbr-21.0.11
```

Вариант A (рекомендуется) — **JetBrains Runtime 21**:
1. Скачать: <https://www.jetbrains.com/help/idea/sources-and-downloads-other-jdk.html>
   (раздел *JetBrains Runtime*, версия 21, Windows x64).
2. Распаковать в `C:\Users\xAnder\.jdks\jbr-21.0.11` — путь должен совпасть **точно**
   (иначе сборка упадёт с ошибкой про `org.gradle.java.home`).

Вариант B — **Temurin 21** (Temurin JDK не переименовывается в `jbr-21.0.11`):
1. Скачать: <https://adoptium.net/temurin/releases/?version=21>
2. Установить в любой путь и **поправить** `gradle.properties`:

```properties
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.7.6-hotspot
```

> Либо вообще убрать строку `org.gradle.java.home` — тогда Gradle возьмёт `JAVA_HOME`
> из переменной окружения.

Установка через winget (поставит Temurin в `C:\Program Files\Eclipse Adoptium\...`):

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Проверка (PowerShell):

```powershell
java -version
# должно быть: openjdk version "21.x.x"
```

---

## 2. Android Studio и Android SDK

### 2.1 Установка Android Studio

<https://developer.android.com/studio> → *Download Android Studio* → установить.

Важно: при установке отметить компоненты **Android SDK**, **SDK Platform**, **SDK Build-Tools**,
**SDK Command-line Tools**, **Android SDK Platform-Tools**.

Studio сама даст JDK: `C:\Program Files\Android\Android Studio\jbr` (это JBR 21 в версиях 2024.2+).
Если этот путь нравится больше, чем `.jdks\jbr-21.0.11`, — пропишите его в `gradle.properties`:

```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

### 2.2 Пакеты SDK

Открыть **Tools → SDK Manager** (или `sdkmanager` в терминале Studio) и поставить:

- ☑ **Android SDK Platform 34** (Android 14) — нужен для текущего `main` (`compileSdk = 34`)
- ☑ **Android SDK Platform 36** (Android 16) — понадобится после влития PR #2 (`compileSdk = 36`)
- ☑ **Android SDK Build-Tools 34.0.0** (и 36.0.0, если будет PR #2)
- ☑ **Android SDK Platform-Tools** (в нём `adb`)
- ☑ **Android SDK Command-line Tools (latest)**

Принять лицензии: во вкладке SDK Manager нажать **Accept** / в терминале:

```powershell
sdkmanager --licenses
```

### 2.3 Однокнопочная установка SDK-пакетов (без GUI)

Если Android Studio ставить не хочется — поставьте только Command-line Tools
(<https://developer.android.com/studio#command-tools>), распакуйте в
`%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest`, затем выполните:

```powershell
$SDK = "$env:LOCALAPPDATA\Android\Sdk"
$sdkm = "$SDK\cmdline-tools\latest\bin\sdkmanager.bat"

& $sdkm --sdk_root="$SDK" "platform-tools" "platforms;android-34" "platforms;android-36" "build-tools;34.0.0" "build-tools;36.0.0"
& $sdkm --sdk_root="$SDK" --licenses
```

---

## 3. Git и GitHub CLI

```powershell
winget install Git.Git
winget install GitHub.cli
```

После установки `gh` — один раз авторизоваться:

```powershell
gh auth login
# GitHub.com → HTTPS → Yes (в браузере) → Yes (вставить код)
```

Проверка:

```powershell
git --version
gh --version
gh auth status
```

---

## 4. Клонирование проекта и `local.properties`

```powershell
cd C:\Users\xAnder\AndroidProjects
git clone https://github.com/Ponamarenko/SunPortal.git
cd SunPortal
```

`local.properties` (файл в `.gitignore`, на ПК нужен локально) — Android Studio создаёт его сама
при первом открытии проекта. Если нет — создать вручную в корне репозитория:

```properties
sdk.dir=C:\\Users\\xAnder\\AppData\\Local\\Android\\Sdk
```

Либо задать переменную окружения вместо файла:

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME","$env:LOCALAPPDATA\Android\Sdk","User")
```

---

## 5. Сборка

### 5.1 Скрипт `build_now.bat` (локальный, в `.gitignore`)

В репозитории его нет — создайте в корне проекта:

```bat
@echo off
setlocal
set "JAVA_HOME=C:\Users\xAnder\.jdks\jbr-21.0.11"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
.\gradlew.bat --no-daemon %*
endlocal
```

Использование:

```bat
build_now.bat assembleDebug        :: обычная сборка
build_now.bat clean assembleDebug  :: полная пересборка с нуля
```

APK: `app\build\outputs\apk\debug\app-debug.apk`

### 5.2 Без скрипта

```powershell
$env:JAVA_HOME = "C:\Users\xAnder\.jdks\jbr-21.0.11"
.\gradlew.bat --no-daemon clean assembleDebug
```

> Из PowerShell Gradle не стартует без явного `JAVA_HOME` — поэтому и нужен скрипт.

---

## 6. Установка на телефон (Nexus 6P)

1. На телефоне: **Настройки → О телефоне** → 7 раз тапнуть по «Номер сборки» → откроются
   **Параметры разработчика** → включить **Отладка по USB**.
2. Подключить USB-кабелем к ПК, на телефоне подтвердить отпечаток ключа.
3. Проверить:

```powershell
adb devices
# должен быть: emulator-5554  device   (или серийный номер Nexus 6P)
```

4. Установить APK:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

5. Скриншот с ПК:

```powershell
adb exec-out screencap -p > screen.png
```

> Если `adb devices` пусто — поставьте Google USB Driver для Nexus:
> <https://developer.android.com/tools/releases/platform-tools> (раздел *Google USB Driver*).

---

## 7. Сборка и релиз

Тулчейн в `main` актуален: **SDK 36 / Kotlin 1.9.24 / Gradle 8.9 / AGP 8.5.2**, JDK 21.
Отдельного PR с тулчейном больше не нужно. Убедитесь, что установлены
**Android SDK Platform 36** и **Build-Tools 36.0.0**.

```powershell
git checkout main
git pull origin main
build_now.bat clean assembleDebug
```

Текущая версия — `versionCode = 33`, `versionName = "1.0.32"`. Перед новым релизом
поднять в `app/build.gradle.kts`:

```kotlin
versionCode = 34
versionName = "1.0.33"
```

И выпустить релиз:

```powershell
gh release create v1.0.33 app\build\outputs\apk\debug\app-debug.apk `
  --title "v1.0.33 — <краткое описание>" `
  --notes "<что изменено>" `
  --latest
gh release view v1.0.33
```

Ссылка на релиз: <https://github.com/Ponamarenko/SunPortal/releases>

---

## 8. Частые ошибки и что делать

| Ошибка | Причина | Решение |
|---|---|---|
| `Value 'C:\Users\xAnder\.jdks\jbr-21.0.11' given for org.gradle.java.home Gradle property is invalid` | нет JDK по этому пути | поставить JBR 21 в этот путь или исправить путь в `gradle.properties` |
| `SDK location not found` | нет `local.properties` / `ANDROID_HOME` | создать `local.properties` с `sdk.dir=...` (см. п. 4) |
| `Failed to install the following SDK components as some licences have not been accepted` | не приняты лицензии SDK | `sdkmanager --licenses` → принять все |
| `Failed to find target with hash string 'android-36'` | не установлен Platform 36 | SDK Manager → поставить Platform 36 |
| `Unsupported compileSdk` / предупреждение AGP про compileSdk | AGP 8.5.2 не знает про 36 | предупреждение безвредно (сборка проходит); можно подавить: `android.suppressUnsupportedCompileSdk=36` в `gradle.properties` |
| `Could not determine java version from '17.0.x'` | слишком старый JDK | JDK 21 |
| `Gradle build daemon disappeared` / OOM | не хватает памяти | в `gradle.properties` увеличить `org.gradle.jvmargs=-Xmx4096m` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | на телефоне стоит APK с другим ключом | `adb uninstall app.sunportal` и поставить заново |
| `adb devices` пусто | нет драйвера / не включён USB-отладка | проверить режим разработчика, поставить Google USB Driver |
| Сборка в Studio не видит SDK | SDK не прописан | Studio → *Settings → Languages & Frameworks → Android SDK*, указать путь |

---

## 9. Чек-лист «всё установлено»

```powershell
java -version          # 21.x
git --version          # 2.4x
gh auth status         # logged in
adb devices            # устройство видно (перед установкой APK)
```

И финальная проверка сборки:

```powershell
cd C:\Users\xAnder\AndroidProjects\SunPortal
build_now.bat clean assembleDebug
Test-Path app\build\outputs\apk\debug\app-debug.apk   # должно быть True
```

Если все три команды прошли и APK собрался — окружение готово к работе над проектом.

---

## 10. Чек-лист пересборки после правок с телефона

Правки делаются на телефоне в коде → коммит + push в `main`. Дальше на ПК:

```bat
cd C:\Users\xAnder\AndroidProjects\SunPortal
git pull origin main
build_now.bat clean assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Актуальные задачи на пересборку — в Issues: <https://github.com/Ponamarenko/SunPortal/issues>
(последняя: **#3** — новая схема потоков: крупные иконки, тонкие линии, дом по погоде).

Порядок релиза (если нужна новая версия):

```powershell
# 1) в app/build.gradle.kts поднять versionCode / versionName
# 2) пересобрать
build_now.bat clean assembleDebug
# 3) выпустить
gh release create vX.Y.Z app\build\outputs\apk\debug\app-debug.apk --title "vX.Y.Z" --notes "..." --latest
```

Откат, если новая сборка не зашла:

```bat
git revert <commit>            :: мягкий откат коммита
git reset --hard 0960295       :: жёсткий откат к рабочей v1.0.4
```

---

## Полезные ссылки

- Android Studio — <https://developer.android.com/studio>
- Command-line Tools — <https://developer.android.com/studio#command-tools>
- SDK Manager (список пакетов) — <https://developer.android.com/tools/sdkmanager>
- JetBrains Runtime 21 — <https://www.jetbrains.com/help/idea/sources-and-downloads-other-jdk.html>
- Temurin JDK 21 — <https://adoptium.net/temurin/releases/?version=21>
- Git — <https://git-scm.com/download/win>
- GitHub CLI — <https://cli.github.com/>
- Gradle (скачивается автоматически wrapper'ом) — <https://services.gradle.org/distributions/>
