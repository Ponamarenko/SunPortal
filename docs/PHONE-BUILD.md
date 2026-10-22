# Сборка APK без ПК: облако (GitHub Actions) и телефон (Termux)

Два независимых способа собрать `SunPortal` — компьютер не нужен ни в одном из них.

---

## 1. GitHub Actions (облако) — рекомендуемый путь

Телефон только пушит код, APK собирается на серверах GitHub (x86_64), готовый файл
скачивается как артефакт.

Файл воркфлоу: `.github/workflows/build-apk.yml`.

| Событие | Что происходит |
|---|---|
| push в `main` | Сборка debug APK → артефакт `app-debug-apk` |
| push тега `vX.Y.Z` | Сборка + автоматический GitHub Release с APK |
| Запуск вручную | Кнопка «Run workflow» во вкладке Actions |

Как забрать APK с телефона:

```bash
cd /storage/emulated/0/ocroot/SunPortal
gh run list --limit 3                      # найти номер запуска
gh run download <номер запуска> -n app-debug-apk -D /storage/emulated/0/Download
```

Как выложить релиз из командной строки (без ПК):

```bash
git tag v1.0.5 && git push origin v1.0.5    # CI сам соберёт APK и создаст релиз
gh release view v1.0.5
```

Требования к workflow:

* `gradlew` в репозитории должен иметь бит исполнения (`git update-index --chmod=+x gradlew`).
* В `gradle.properties` **не должно быть** жёсткого `org.gradle.java.home` — путь привязан
  к конкретному ПК и ломает сборку на Linux. На ПК `build_now.bat` ставит `JAVA_HOME` сам.
* Версия `versionCode`/`versionName` поднимается вручную в `app/build.gradle.kts`
  перед тегом (на телефоне версию не поднимаем — по правилам проекта это делает ПК).

---

## 2. Termux: сборка прямо на телефоне

Проверено на этом же устройстве (Android, aarch64, Termux). Требуется ~4 ГБ свободного
места и ~2 ГБ свободной оперативной памяти.

### 2.1. Пакеты Termux

```bash
pkg install openjdk-17 aapt aapt2 apksigner
```

* `openjdk-17` — Gradle и Kotlin (JDK 21 тоже ставится, но 17 экономнее по памяти).
* `aapt2` — сборка ресурсов; **версия из Maven (x86_64) на ARM не запускается**, поэтому
  используется сборка Termux (см. 2.4).
* пакет `aapt` содержит ещё и `zipalign` (нужен на этапе упаковки APK).

> Если `pkg` ругается на битые репозитории (404 в `sources.list.d`) — временно отключите их:
> `mkdir -p $PREFIX/etc/apt/sources.list.d.disabled && mv $PREFIX/etc/apt/sources.list.d/*.list $PREFIX/etc/apt/sources.list.d.disabled/`

### 2.2. Android SDK (Platform 36 + Build-Tools)

```bash
SDK=$PREFIX/share/android-sdk
mkdir -p $SDK/cmdline-tools
cd /tmp && curl -sSLO https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
unzip -q commandlinetools-linux-13114758_latest.zip
mv cmdline-tools $SDK/cmdline-tools/latest

export JAVA_HOME=$PREFIX/lib/jvm/java-17-openjdk
export ANDROID_SDK_ROOT=$SDK
yes | $SDK/cmdline-tools/latest/bin/sdkmanager --sdk_root=$SDK --licenses
$SDK/cmdline-tools/latest/bin/sdkmanager --sdk_root=$SDK "platforms;android-36" "build-tools;36.0.0" "platform-tools"
```

### 2.3. Настройки Gradle для телефона

`$HOME/.gradle/gradle.properties` (вне репозитория; имеет приоритет над проектным файлом):

```properties
org.gradle.jvmargs=-Xmx1200m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
org.gradle.daemon=false
org.gradle.parallel=false
org.gradle.workers.max=1
org.gradle.caching=true
kotlin.compiler.execution.strategy=in-process
kotlin.incremental=false
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```

* `aapt2FromMavenOverride` — ключевой момент: AGP по умолчанию скачивает x86_64-бинарь aapt2,
  на ARM он падает с `Exec format error`.
* `in-process` — Kotlin-компилятор не поднимает отдельный daemon (~500 МБ RAM).
* `daemon=false` + `workers.max=1` — экономия памяти, сборка медленнее.

### 2.4. zipalign и local.properties

```bash
ln -sf $PREFIX/bin/zipalign $SDK/build-tools/36.0.0/zipalign   # сборка Termux вместо x86_64
echo "sdk.dir=$SDK" > /storage/emulated/0/ocroot/SunPortal/local.properties
```

`local.properties` в `.gitignore` — на ПК останется свой путь.

### 2.5. Сборка

```bash
cd /storage/emulated/0/ocroot/SunPortal
export JAVA_HOME=$PREFIX/lib/jvm/java-17-openjdk
export ANDROID_HOME=$PREFIX/share/android-sdk
sh gradlew --no-daemon assembleDebug
```

> Запускать через `sh gradlew`, а не `./gradlew`: на общем хранилище Android
> (`/storage/emulated/0`, exFAT) нет бита исполнения, даже если он есть в git.

Первая сборка скачивает Gradle 8.9 и все зависимости (~1 ГБ), дальше — из кэша.
Результат: `app/build/outputs/apk/debug/app-debug.apk`.

### 2.6. Установка APK на устройство

```bash
termux-open /storage/emulated/0/ocroot/SunPortal/app/build/outputs/apk/debug/app-debug.apk
```

Откроется системный установщик — нажать «Установить».

---

## 3. Ограничения

* **RAM.** На устройстве 5.7 ГБ всего, доступно обычно 0.6–1 ГБ. Перед сборкой стоит
  закрыть тяжёлые приложения. Симптом нехватки памяти — убийство процесса Java
  low-memory killer'ом без сообщения об ошибке.
* **Время.** Первая сборка ~15–25 минут, повторная (из кэша) ~3–7 минут.
* **Совместимость.** Проект собирается под JDK 17/21, AGP 8.7.3, Gradle 8.9,
  compileSdk/targetSdk 36.
