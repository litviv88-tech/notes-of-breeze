# Breez Notes

Android-приложение для заметок: папки, темы, обои и виджеты на рабочем столе.

Пакет: `com.breez.notes`  
Min SDK 26 · Target / Compile SDK 35 · Версия 1.2.0

## Документация

| Файл | О чём |
| --- | --- |
| [docs/stack.md](docs/stack.md) | Стек и версии библиотек |
| [docs/architecture.md](docs/architecture.md) | Слои, пакеты, навигация |
| [docs/note-schema.md](docs/note-schema.md) | Контракт Room и localStorage |
| [docs/features.md](docs/features.md) | Функции приложения |
| [docs/setup.md](docs/setup.md) | Сборка и запуск из Cursor |

## Быстрый старт

Из корня проекта в PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

Установка на подключённое устройство или эмулятор:

```powershell
.\gradlew.bat installDebug
```

Нужны **JDK 17** и **Android SDK**. Подробности — в [docs/setup.md](docs/setup.md).

## Что умеет приложение

- CRUD заметок: заголовок, текст, папка, цвет метки, закрепление, напоминание
- Место встречи, свои повторы, фото и видео, поиск по тексту с картинки
- CRUD папок с ярлыками: цвет, фото или видео до 10 секунд
- Поиск по заголовку, тексту, месту и тексту на фото
- Виджеты Glance: размер 1×2–6×5, подгонка фото и отдельная настройка каждого экземпляра
- 12 цветовых палитр и свой цвет через HSV
- Обои: градиенты, фото, цвет, затемнение и размытие
- Темы: система / светлая / тёмная / авто (20:00–07:00) и Material You на API 31+
