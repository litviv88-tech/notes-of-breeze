# Контракт хранилища заметок

Общая модель для Android (Room) и веба (`localStorage`). Пока синхронизации нет, этот файл — единый чеклист при добавлении поля.

Текущая версия схемы данных: **4**.  
Room: `BreezDatabase` version **7**.  
Веб: `schemaVersion` внутри ключа `breez-web-v2`.

## Правило изменений

1. Добавить поле в этот документ.
2. Android: `Migration_N_N+1` + bump `BreezDatabase.version`. Экспорт схемы в `app/schemas/` коммитить в Git.
3. Веб: bump `SCHEMA_VERSION` в `web/js/app.js` и дописать ветку в `migrateState()`.
4. Не вызывать `fallbackToDestructiveMigration`.

## Заметка

| Поле | Тип | Android | Веб | С версии |
| --- | --- | --- | --- | --- |
| id | long / string | Room autoincrement | строковый uid | 1 |
| title | string | notes.title | title | 1 |
| body | string | notes.body | body | 1 |
| folderId | long? / string? | notes.folderId, SET_NULL | folderId | 1 |
| colorHex | string | notes.colorHex | colorHex | 1 |
| isPinned / pinned | bool | notes.isPinned | pinned | 1 |
| sortOrder | int | notes.sortOrder | sortOrder | 1 |
| reminderAt | long? | notes.reminderAt | reminderAt | 1 |
| meetingPlace | string | notes.meetingPlace | meetingPlace | 1 |
| meetingLat | double? | notes.meetingLat | meetingLat | 1 |
| meetingLng | double? | notes.meetingLng | meetingLng | 1 |
| locationReminder | bool | notes.locationReminder | locationReminder | 1 |
| recurrence | object | repeatUnit, repeatInterval, repeatWeekDays, repeatUntilAt | recurrence | 1 |
| isChecklist | bool | notes.isChecklist | isChecklist | 2 |
| isArchived | bool | notes.isArchived | isArchived | 3 |
| createdAt | long | notes.createdAt | createdAt | 1 |
| updatedAt | long | notes.updatedAt | updatedAt | 1 |

Вложения есть только на Android (`attachments`). В веб-снимке их нет.

## Папка

| Поле | Тип | Android | Веб |
| --- | --- | --- | --- |
| id | long / string | autoincrement | uid |
| name | string | folders.name | name |
| colorHex | string | folders.colorHex | colorHex |
| markType | string | folders.markType | markType |
| markFileName | string | folders.markFileName | markFileName |
| sortOrder | int | folders.sortOrder | sortOrder |
| createdAt | long | folders.createdAt | createdAt |

## Миграции веба

Старые записи без `schemaVersion` считаются версией **0**.  
`0 → 3` и промежуточные: `migrateState()` нормализует заметки и папки, в том числе `isChecklist` и `isArchived`. Ключи `breez-web-v1` и `breez-notes` читаются как наследие и перезаписываются в `breez-web-v2`.
