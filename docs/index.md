# Breez Notes

Android-приложение и веб-версия для заметок, списков дел, папок и виджетов.

| | |
| --- | --- |
| Пакет | `com.breez.notes` |
| Версия | **1.5.0** (`versionCode` 17) |
| Min SDK | 26 |
| Target / Compile SDK | 35 |
| Репозиторий | [litviv88-tech/notes-of-breeze](https://github.com/litviv88-tech/notes-of-breeze) |
| Сайт и APK | [GitHub Pages](https://litviv88-tech.github.io/notes-of-breeze/) |

Это markdown-проект документации. Исходники приложения рядом, в `app/` и `web/`.

## Разделы

1. [Стек](stack.md) — Kotlin, Compose, Room, Hilt, Glance, веб
2. [Архитектура](architecture.md) — слои `domain` / `data` / `ui` / `widget`
3. [Схема данных](note-schema.md) — контракт Room и `localStorage`
4. [Функции](features.md) — заметки, списки дел, папки, виджеты, темы
5. [Главный экран](home-screen.md) — обязательный порядок блоков
6. [Веб-версия](web.md) — PWA и GitHub Pages
7. [Сборка](setup.md) — Gradle и предпросмотр этой документации

## Быстрый старт

Сборка приложения из корня репозитория:

```powershell
.\gradlew.bat assembleDebug
```

Предпросмотр этой документации:

```powershell
python -m pip install -r docs\requirements.txt
python -m mkdocs serve
```

Сайт откроется на `http://127.0.0.1:8000`.
