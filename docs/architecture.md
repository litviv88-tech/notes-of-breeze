# Архитектура

Пакетный Clean Architecture внутри одного модуля `:app`. Отдельные Gradle-модули не заводим, пока не появится синхронизация или общий Kotlin-клиент.

Правило зависимостей: `ui → domain ← data`. `platform` слушает события domain и не встраивается в репозиторий.

## Пакеты

```text
app/src/main/java/com/breez/notes/
├── BreezApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/          # Room, DAO, entity, экспорт схемы
│   ├── mapper/
│   ├── preferences/
│   └── repository/     # impl без WidgetUpdater и напоминаний
├── di/
├── domain/
│   ├── model/          # Note, Folder, Palettes — без Android и R
│   ├── repository/     # интерфейсы, включая ReminderScheduler
│   └── usecase/        # SaveNote, DeleteNote, SetReminder
├── platform/
│   ├── reminder/       # ReminderWorker
│   └── widget/         # WidgetRefreshObserver
├── reminders/          # WorkManager, геозоны, канал уведомлений
├── ui/
└── widget/             # Glance и настройка виджета
```

## Данные

- Room (`BreezDatabase`, version 6, `exportSchema = true`). JSON схем лежит в `app/schemas/` и коммитится в Git.
- Миграции `1→6` заданы явно и идемпотентны. `fallbackToDestructiveMigration` запрещён.
- У заметки `folderId` с `ForeignKey.SET_NULL`.
- DataStore: тема, палитра, Material You, обои.
- Общий контракт Android ↔ веб: [docs/note-schema.md](note-schema.md).

## Сценарии

| Действие | Где живёт |
| --- | --- |
| Сохранить заметку | `SaveNote`: валидация, upsert, планирование напоминания |
| Удалить заметку | `DeleteNote`: отмена напоминания, удаление |
| Напоминание | `SetReminder` + `platform/reminder` |
| Обновить виджет | `WidgetRefreshObserver` подписан на заметки, папки и конфиги |

## Навигация

Маршруты в `ui/navigation/Routes.kt`:

| Экран | Маршрут |
| --- | --- |
| Список заметок | `notes` |
| Редактор | `editor?noteId={noteId}&checklist={checklist}` |
| Папки | `folders` |
| Папка | `folders/{folderId}` |
| Настройки | `settings` |
| Тема | `settings/theme` |
| Обои | `settings/wallpaper` |

Точка входа UI: `MainActivity` → `BreezTheme` → `NavGraph`.

Виджет настраивается отдельной `WidgetConfigActivity` (не через NavHost).
