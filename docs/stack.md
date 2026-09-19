# Стек

Сборщик: **Gradle 8.7**, Kotlin DSL, version catalog `gradle/libs.versions.toml`.

| Слой | Технология | Версия |
| --- | --- | --- |
| Язык | Kotlin | 2.0.21 |
| Сборка приложения | Android Gradle Plugin | 8.7.0 |
| Кодоген | KSP | 2.0.21-1.0.27 |
| UI | Jetpack Compose + Material 3 | BOM 2024.10.01 |
| Навигация | Navigation Compose | 2.8.3 |
| БД | Room | 2.6.1 |
| DI | Hilt | 2.52 |
| Виджеты | Jetpack Glance | 1.1.0 |
| Картинки | Coil 3 | 3.0.4 |
| Настройки | DataStore Preferences | 1.1.1 |
| Напоминания | WorkManager | 2.9.1 |
| Асинхронность | Coroutines | 1.9.0 |

## Требования к окружению

- JDK **17**
- Android SDK, compile/target **35**
- Min SDK **26** (Android 8.0)

Цвета темы живут только в `ui/theme`. Состояние экранов — через `StateFlow`, не LiveData.
Строки интерфейса: `res/values/strings.xml` (ru) и `res/values-en/strings.xml` (en).
