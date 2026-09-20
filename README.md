# Breez Notes

Android-приложение и веб-версия для заметок: папки, списки дел, темы, обои и виджеты.

Пакет: `com.breez.notes`  
Min SDK 26 · Target / Compile SDK 35 · Версия **1.4.8**

Документация — отдельный markdown-проект на MkDocs. Страницы лежат в `docs/`, конфиг — `mkdocs.yml`.

## Документация

| Файл | О чём |
| --- | --- |
| [docs/index.md](docs/index.md) | Оглавление |
| [docs/stack.md](docs/stack.md) | Стек и версии библиотек |
| [docs/architecture.md](docs/architecture.md) | Слои, пакеты, навигация |
| [docs/note-schema.md](docs/note-schema.md) | Контракт Room и localStorage |
| [docs/features.md](docs/features.md) | Функции приложения |
| [docs/home-screen.md](docs/home-screen.md) | Порядок блоков главного экрана |
| [docs/web.md](docs/web.md) | PWA и GitHub Pages |
| [docs/setup.md](docs/setup.md) | Сборка приложения и этой документации |

Предпросмотр сайта документации:

```powershell
python -m pip install -r docs\requirements.txt
python -m mkdocs serve
```

Откроется `http://127.0.0.1:8000`.

## Быстрый старт приложения

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
- Списки дел: пункты вычёркиваются
- Место встречи, свои повторы, фото и видео, поиск по тексту с картинки
- CRUD папок с ярлыками: цвет, фото или видео до 10 секунд
- Поиск по заголовку, тексту, месту и тексту на фото
- Виджеты Glance: размер 1×2–6×5, подгонка фото и отдельная настройка каждого экземпляра
- Одна готовая палитра Breez Blue и свой цвет через HSV
- Обои: градиенты, фото, цвет, затемнение и размытие
- Светлая / тёмная тема — отдельная кнопка на главном экране; Material You на API 31+
