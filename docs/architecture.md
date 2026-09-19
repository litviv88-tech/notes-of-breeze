# Архитектура

Приложение разделено на слои: модели и интерфейсы в `domain`, Room/DataStore в `data`, экраны в `ui`, домашние виджеты в `widget`.

## Пакеты

```text
app/src/main/java/com/breez/notes/
├── BreezApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/          # Room: БД, DAO, entity
│   ├── mapper/         # Entity ↔ domain
│   ├── preferences/    # DataStore темы и обоев
│   └── repository/     # Реализации репозиториев
├── di/                 # Hilt-модули
├── domain/
│   ├── model/
│   └── repository/     # Интерфейсы
├── ui/
│   ├── components/
│   ├── editor/
│   ├── folders/
│   ├── notes/
│   ├── navigation/
│   ├── settings/
│   └── theme/
└── widget/             # Glance + экран настройки виджета
```

## Данные

- Room (`BreezDatabase`, version 3): заметки, папки, вложения, конфиги виджетов.
- У заметки `folderId` с `ForeignKey.SET_NULL` — удаление папки не удаляет заметки.
- **DataStore**: режим темы, палитра, Material You, обои.
- ViewModel-ы получают репозитории через Hilt (`@HiltViewModel`).

## Навигация

Маршруты в `ui/navigation/Routes.kt`:

| Экран | Маршрут |
| --- | --- |
| Список заметок | `notes` |
| Редактор | `editor?noteId={noteId}` |
| Папки | `folders` |
| Папка | `folders/{folderId}` |
| Настройки | `settings` |
| Тема | `settings/theme` |
| Обои | `settings/wallpaper` |

Точка входа UI: `MainActivity` → `BreezTheme` → `NavGraph`.

Виджет настраивается отдельной `WidgetConfigActivity` (не через NavHost).
