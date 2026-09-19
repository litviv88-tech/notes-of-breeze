# Веб-версия

PWA лежит в `web/` и публикуется на GitHub Pages из ветки `main`.

Сайт: [https://litviv88-tech.github.io/notes-of-breeze/](https://litviv88-tech.github.io/notes-of-breeze/)

## Что внутри

| Файл | Назначение |
| --- | --- |
| `web/index.html` | Оболочка PWA и кнопка скачивания APK |
| `web/js/app.js` | Заметки, папки, чеклисты, тема, `localStorage` |
| `web/css/breez.css` | Стили |
| `web/sw.js` | Service Worker |
| `web/version.json` | `versionCode`, `versionName`, `webVersion`, ссылка на APK |
| `web/manifest.webmanifest` | Установка как приложение |

Данные хранятся локально в ключе `breez-web-v2`. Общий контракт с Android — в [note-schema.md](note-schema.md). Синхронизации между телефоном и браузером пока нет.

## Версии

- Если менялся Android-код: поднять `versionCode` и `versionName` в `gradle/libs.versions.toml`, те же поля в `web/version.json`, увеличить `webVersion`.
- Если менялся только веб: увеличить `webVersion` в `web/version.json`.

CI собирает APK и кладёт его в Pages как `downloads/BreezNotes.apk`.
