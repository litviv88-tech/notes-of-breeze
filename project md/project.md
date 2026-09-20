# Breez Notes — project.md

Краткое описание проекта: что это, как устроено, куда смотреть в репозитории.

## О проекте

Android-приложение и веб-версия для заметок: папки, списки дел, темы, обои и виджеты.

| | |
| --- | --- |
| Пакет | `com.breez.notes` |
| Версия | **1.5.1** (`versionCode` 18) |
| Min SDK | 26 |
| Target / Compile SDK | 35 |
| Репозиторий | [litviv88-tech/notes-of-breeze](https://github.com/litviv88-tech/notes-of-breeze) |
| Сайт и APK | [GitHub Pages](https://litviv88-tech.github.io/notes-of-breeze/) |

## Структура репозитория

```text
заметки breeze/
├── app/                 # Android-приложение (один модуль :app)
├── web/                 # PWA (vanilla JS), версия в version.json
├── docs/                # Документация MkDocs
├── project md/          # Этот обзор проекта
├── gradle/              # Version catalog, wrapper
└── mkdocs.yml           # Конфиг сайта документации
```

## Стек

**Android:** Kotlin 2.0, Jetpack Compose + Material 3, Room, Hilt, Glance, Coil, DataStore, WorkManager.

**Веб:** vanilla JS PWA без фреймворка (`web/index.html`, `web/js/app.js`, Service Worker, `localStorage`).

**Сборка:** Gradle 8.7, JDK 17. Версии приложения — в `gradle/libs.versions.toml` и дублируются в `web/version.json`.

## Архитектура (Android)

Пакетный Clean Architecture в `:app`. Зависимости: `ui → domain ← data`.

```text
com.breez.notes/
├── data/       # Room, DAO, repository impl, preferences
├── domain/     # модели, интерфейсы, use cases
├── platform/   # напоминания, обновление виджетов
├── ui/         # экраны Compose
└── widget/     # Glance + WidgetConfigActivity
```

## Главный экран (порядок сверху вниз)

1. **Заметки** — создание сверху (`+`)
2. **Создать список дел** — чеклист, пункты вычёркиваются
3. **Создать папку** — заметки и списки дел
4. **Внешний вид, обои, светлая/тёмная тема**
5. **Внизу** — версия и обновление из приложения

## Функции

- CRUD заметок: заголовок, текст, папка, цвет, закрепление, напоминание
- Списки дел с вычёркиванием пунктов
- Место встречи, повторы, фото/видео, OCR-поиск по картинке
- Папки с ярлыками: цвет, фото или видео до 10 секунд
- Виджеты Glance: размер 1×2–6×5, своя настройка каждого экземпляра
- Палитра Breez Blue + свой цвет (HSV)
- Обои: градиенты, фото, цвет, затемнение и размытие
- Светлая / тёмная тема; Material You на API 31+

## Деплой

Каждый фикс и версия уходят в `main`. GitHub Actions собирает APK и публикует сайт на Pages (`downloads/BreezNotes.apk`).

При изменении Android-кода:

1. Поднять `versionCode` / `versionName` в `gradle/libs.versions.toml` (и синхронно в `web/version.json`)
2. Увеличить `webVersion` в `web/version.json`

При изменении только web — увеличить `webVersion`.

## Быстрый старт

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

Документация:

```powershell
python -m pip install -r docs\requirements.txt
python -m mkdocs serve
```

Откроется `http://127.0.0.1:8000`.

## Подробная документация

| Файл | О чём |
| --- | --- |
| [docs/index.md](../docs/index.md) | Оглавление |
| [docs/stack.md](../docs/stack.md) | Стек и версии |
| [docs/architecture.md](../docs/architecture.md) | Слои и навигация |
| [docs/note-schema.md](../docs/note-schema.md) | Контракт Room и localStorage |
| [docs/features.md](../docs/features.md) | Функции |
| [docs/home-screen.md](../docs/home-screen.md) | Порядок блоков главного экрана |
| [docs/web.md](../docs/web.md) | PWA и GitHub Pages |
| [docs/setup.md](../docs/setup.md) | Сборка |
