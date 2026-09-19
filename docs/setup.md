# Сборка и запуск

Проект собирается из Cursor, без Android Studio. Нужны JDK 17 и Android SDK.

## Переменные среды

Проверьте, что SDK и JDK видны в сессии PowerShell:

```powershell
echo $env:ANDROID_HOME
echo $env:JAVA_HOME
java -version
```

Если `ANDROID_HOME` пустой, укажите путь к SDK, например:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

## Команды Gradle

Все команды — из корня папки `заметки breeze`.

Собрать debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Поставить на устройство:

```powershell
.\gradlew.bat installDebug
```

Список задач:

```powershell
.\gradlew.bat tasks
```

APK после сборки:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Полезные пути

| Что | Где |
| --- | --- |
| Каталог версий | `gradle/libs.versions.toml` |
| Модуль приложения | `app/build.gradle.kts` |
| Манифест | `app/src/main/AndroidManifest.xml` |
| Строки (ru / en) | `app/src/main/res/values/strings.xml`, `values-en/strings.xml` |
