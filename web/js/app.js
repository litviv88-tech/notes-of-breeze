const I18N = {
  ru: {
    app: "Breez Notes",
    heroTitle: "Спокойные заметки с живым стилем",
    heroText: "Веб-версия Breez Notes: папки, поиск, палитры и обои. Скачайте Android-приложение на телефон или работайте прямо в браузере.",
    download: "Скачать на Android",
    openWeb: "Открыть веб-версию",
    feature1t: "Заметки и папки",
    feature1: "Заголовок, текст, цвет метки, закрепление и папки с ярлыками.",
    feature2t: "12 палитр",
    feature2: "Breez Blue, Mint, Lavender и остальные, плюс свой цвет.",
    feature3t: "Обои",
    feature3: "Встроенные градиенты, затемнение и фото-обои.",
    howTitle: "Как установить на телефон",
    how1: "Откройте эту страницу на Android-телефоне в Chrome.",
    how2: "Нажмите «Скачать на Android» и сохраните APK.",
    how3: "Разрешите установку из этого источника, если система спросит.",
    how4: "Откройте файл BreezNotes.apk и установите приложение.",
    qrHint: "QR-код ведёт на APK. На этой же сети телефон откроет ссылку.",
    notes: "Заметки",
    search: "Поиск по заголовку и тексту",
    empty: "Пока нет заметок.\nНажмите +, чтобы создать первую.",
    untitled: "Без названия",
    all: "Все заметки",
    folders: "Папки",
    settings: "Настройки",
    appearance: "Внешний вид",
    wallpaper: "Обои",
    about: "О приложении",
    aboutText: "Breez Notes 1.2.0 — спокойные заметки с палитрами и обоями. Веб-версия хранит данные в этом браузере.",
    save: "Сохранить",
    title: "Заголовок",
    body: "Текст заметки",
    folder: "Папка",
    noFolder: "Без папки",
    color: "Цвет метки",
    pin: "Закрепить",
    unpin: "Открепить",
    del: "Удалить",
    newFolder: "Новая папка",
    folderName: "Название папки",
    create: "Создать",
    cancel: "Отмена",
    theme: "Режим темы",
    system: "Системная",
    light: "Светлая",
    dark: "Тёмная",
    auto: "Авто 20:00–7:00",
    palettes: "Палитры",
    custom: "Свой цвет",
    apply: "Применить",
    builtin: "Встроенные",
    gallery: "Галерея",
    solid: "Цвет",
    dim: "Затемнение",
    pickPhoto: "Выбрать фото",
    pickColor: "Цвет фона",
    landing: "На сайт",
    updateTitle: "Обновление",
    updateNow: "Обновить",
    updateCheck: "Проверить обновления",
    updateChecking: "Проверяем…",
    updateLatest: "Установлена актуальная версия",
    updateAvailable: "Доступна новая версия",
    updateAvailableText: "Можно обновить прямо из приложения.",
    updateFailed: "Не удалось проверить обновление. Проверьте интернет.",
    currentVersion: "Текущая версия",
    releasedVersion: "Вышла версия"
  },
  en: {
    app: "Breez Notes",
    heroTitle: "Calm notes with a living look",
    heroText: "Web version of Breez Notes: folders, search, palettes and wallpapers. Download the Android app or use it in the browser.",
    download: "Download for Android",
    openWeb: "Open web app",
    feature1t: "Notes and folders",
    feature1: "Title, body, label color, pin and folders with photo or video labels.",
    feature2t: "12 palettes",
    feature2: "Breez Blue, Mint, Lavender and more, plus a custom color.",
    feature3t: "Wallpapers",
    feature3: "Built-in gradients, dimming and photo backgrounds.",
    howTitle: "Install on your phone",
    how1: "Open this page on an Android phone in Chrome.",
    how2: "Tap Download for Android and save the APK.",
    how3: "Allow installs from this source if Android asks.",
    how4: "Open BreezNotes.apk and install the app.",
    qrHint: "The QR code points to the APK. On the same network the phone can open it.",
    notes: "Notes",
    search: "Search title and text",
    empty: "No notes yet.\nTap + to create the first one.",
    untitled: "Untitled",
    all: "All notes",
    folders: "Folders",
    settings: "Settings",
    appearance: "Appearance",
    wallpaper: "Wallpaper",
    about: "About",
    aboutText: "Breez Notes 1.2.0 — calm notes with palettes and wallpapers. The web version stores data in this browser.",
    save: "Save",
    title: "Title",
    body: "Note text",
    folder: "Folder",
    noFolder: "No folder",
    color: "Label color",
    pin: "Pin",
    unpin: "Unpin",
    del: "Delete",
    newFolder: "New folder",
    folderName: "Folder name",
    create: "Create",
    cancel: "Cancel",
    theme: "Theme mode",
    system: "System",
    light: "Light",
    dark: "Dark",
    auto: "Auto 8 PM–7 AM",
    palettes: "Palettes",
    custom: "Custom color",
    apply: "Apply",
    builtin: "Built-in",
    gallery: "Gallery",
    solid: "Color",
    dim: "Dimming",
    pickPhoto: "Choose photo",
    pickColor: "Background color",
    landing: "Website",
    updateTitle: "Update",
    updateNow: "Update",
    updateCheck: "Check for updates",
    updateChecking: "Checking…",
    updateLatest: "You have the latest version",
    updateAvailable: "A new version is available",
    updateAvailableText: "You can update from inside the app.",
    updateFailed: "Could not check for updates. Check your internet connection.",
    currentVersion: "Current version",
    releasedVersion: "Released version"
  }
};

const PALETTES = [
  { id: "breez_blue", hex: "#4A90E2", name: "Breez Blue" },
  { id: "mint", hex: "#7ED9C4", name: "Mint" },
  { id: "lavender", hex: "#A78BFA", name: "Lavender" },
  { id: "coral", hex: "#FF7A85", name: "Coral" },
  { id: "sunset", hex: "#FFB347", name: "Sunset" },
  { id: "forest", hex: "#4ADE80", name: "Forest" },
  { id: "ocean", hex: "#06B6D4", name: "Ocean" },
  { id: "rose", hex: "#F472B6", name: "Rose" },
  { id: "amber", hex: "#FBBF24", name: "Amber" },
  { id: "slate", hex: "#64748B", name: "Slate" },
  { id: "cherry", hex: "#EF4444", name: "Cherry" },
  { id: "monochrome", hex: "#94A3B8", name: "Monochrome" }
];

const WALLS = [
  { id: "breeze_sky", css: "linear-gradient(135deg,#4A90E2,#7ED9C4)" },
  { id: "lavender_dream", css: "linear-gradient(135deg,#A78BFA,#F472B6)" },
  { id: "coral_sunset", css: "linear-gradient(135deg,#FF7A85,#FFB347)" },
  { id: "forest_mist", css: "linear-gradient(135deg,#14532D,#4ADE80)" },
  { id: "ocean_deep", css: "linear-gradient(135deg,#0E7490,#06B6D4)" },
  { id: "rose_night", css: "linear-gradient(135deg,#4A044E,#F472B6)" },
  { id: "amber_glow", css: "linear-gradient(135deg,#B45309,#FBBF24)" },
  { id: "slate_storm", css: "linear-gradient(135deg,#1E293B,#64748B)" },
  { id: "cherry_dusk", css: "linear-gradient(135deg,#7F1D1D,#EF4444)" },
  { id: "midnight_mono", css: "linear-gradient(135deg,#0F172A,#94A3B8)" },
  { id: "aurora", css: "linear-gradient(135deg,#4A90E2,#4ADE80,#A78BFA)" },
  { id: "peach_cream", css: "linear-gradient(135deg,#FFE4D6,#FF7A85)" }
];

const KEY = "breez-web-v2";
const LEGACY_KEYS = ["breez-web-v1", "breez-notes"];
const SCHEMA_VERSION = 1;
const APK = "./downloads/BreezNotes.apk";
const WEB_VERSION = 5;
const APP_VERSION = "1.2.0";

let waitingWorker = null;
let updateInfo = {
  checking: false,
  available: false,
  latest: "",
  notes: "",
  error: false,
  dismissed: false
};

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function uid() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 7);
}

function seed() {
  const inboxId = uid();
  const now = Date.now();
  return {
    schemaVersion: SCHEMA_VERSION,
    lang: "ru",
    themeMode: "SYSTEM",
    paletteId: "breez_blue",
    customHex: "#4A90E2",
    wallpaper: { type: "BUILTIN", id: "breeze_sky", photo: "", color: "#4A90E2", dim: 0.25 },
    folders: [normalizeFolder({ id: inboxId, name: "Inbox", colorHex: "#7ED9C4", createdAt: now })],
    notes: [normalizeNote({
      id: uid(),
      title: "Breez Notes",
      body: "Добро пожаловать. Это веб-версия: данные хранятся в браузере. Скачайте APK, чтобы поставить приложение на телефон.",
      folderId: inboxId,
      colorHex: "#4A90E2",
      pinned: true,
      createdAt: now,
      updatedAt: now
    })]
  };
}

function normalizeFolder(folder, index) {
  const now = Date.now();
  return {
    id: folder?.id || uid(),
    name: folder?.name || "Папка",
    colorHex: folder?.colorHex || "#7ED9C4",
    markType: folder?.markType || "COLOR",
    markFileName: folder?.markFileName || "",
    sortOrder: Number.isFinite(folder?.sortOrder) ? folder.sortOrder : (index ?? 0),
    createdAt: folder?.createdAt || now
  };
}

function normalizeNote(note, index) {
  const now = Date.now();
  const recurrence = note?.recurrence || {};
  return {
    id: note?.id || uid(),
    title: note?.title || "",
    body: note?.body || "",
    folderId: note?.folderId ?? null,
    colorHex: note?.colorHex || "#4A90E2",
    pinned: !!(note?.pinned || note?.isPinned),
    sortOrder: Number.isFinite(note?.sortOrder) ? note.sortOrder : (index ?? 0),
    reminderAt: note?.reminderAt ?? null,
    meetingPlace: note?.meetingPlace || "",
    meetingLat: note?.meetingLat ?? null,
    meetingLng: note?.meetingLng ?? null,
    locationReminder: !!note?.locationReminder,
    recurrence: {
      unit: recurrence.unit || "NONE",
      interval: Number.isFinite(recurrence.interval) ? recurrence.interval : 1,
      weekDays: Array.isArray(recurrence.weekDays) ? recurrence.weekDays : [],
      untilAt: recurrence.untilAt ?? null
    },
    createdAt: note?.createdAt || note?.updatedAt || now,
    updatedAt: note?.updatedAt || now
  };
}

function migrateState(raw) {
  const data = raw && typeof raw === "object" ? raw : {};
  const version = Number(data.schemaVersion) || 0;
  const next = { ...seed(), ...data };
  if (version < 1) {
    next.folders = (data.folders || next.folders || []).map(normalizeFolder);
    next.notes = (data.notes || next.notes || []).map(normalizeNote);
  }
  next.schemaVersion = SCHEMA_VERSION;
  return next;
}

function readStoredState() {
  const keys = [KEY, ...LEGACY_KEYS];
  for (const key of keys) {
    const raw = localStorage.getItem(key);
    if (!raw) continue;
    const parsed = JSON.parse(raw);
    const migrated = migrateState(parsed);
    if ((Number(parsed.schemaVersion) || 0) < SCHEMA_VERSION || key !== KEY) {
      localStorage.setItem(KEY, JSON.stringify(migrated));
    }
    return migrated;
  }
  return null;
}

function loadState() {
  try {
    return readStoredState() || seed();
  } catch {
    return seed();
  }
}

let state = loadState();
let route = location.hash.replace("#", "") || "home";
let editorId = null;
let folderId = null;
let query = "";
let selectedFolder = null;
let hsv = { h: 210, s: 0.67, v: 0.89 };

function save() {
  state.schemaVersion = SCHEMA_VERSION;
  localStorage.setItem(KEY, JSON.stringify(state));
  try {
    if (window.BreezNative && typeof window.BreezNative.onNotesChanged === "function") {
      window.BreezNative.onNotesChanged(JSON.stringify({
        notes: state.notes,
        folders: state.folders
      }));
    }
  } catch (err) {}
}

function t(key) {
  return (I18N[state.lang] || I18N.ru)[key] || key;
}

function isDark() {
  const hour = new Date().getHours();
  if (state.themeMode === "LIGHT") return false;
  if (state.themeMode === "DARK") return true;
  if (state.themeMode === "AUTO") return hour >= 20 || hour < 7;
  return window.matchMedia("(prefers-color-scheme: dark)").matches;
}

function currentHex() {
  if (state.paletteId === "custom") return state.customHex || "#4A90E2";
  return (PALETTES.find((p) => p.id === state.paletteId) || PALETTES[0]).hex;
}

function applyChrome() {
  document.documentElement.dataset.theme = isDark() ? "dark" : "light";
  document.documentElement.style.setProperty("--primary", currentHex());
  const wall = state.wallpaper;
  const layer = document.getElementById("wallpaper");
  if (wall.type === "PHOTO" && wall.photo) {
    document.documentElement.style.setProperty("--wallpaper", `url("${wall.photo}")`);
  } else if (wall.type === "COLOR") {
    const color = wall.color || "#4A90E2";
    document.documentElement.style.setProperty("--wallpaper", `linear-gradient(${color}, ${color})`);
  } else {
    const found = WALLS.find((w) => w.id === wall.id) || WALLS[0];
    document.documentElement.style.setProperty("--wallpaper", found.css);
  }
  document.documentElement.style.setProperty("--dim", String(wall.dim ?? 0.25));
  if (layer) {
    const blur = Math.min(Number(wall.blur) || 0, 25);
    layer.style.filter = blur > 0 ? `blur(${blur}px)` : "none";
    layer.classList.toggle("is-blurred", blur > 0);
  }
}

function filteredNotes() {
  let notes = [...state.notes].sort((a, b) => (b.pinned - a.pinned) || (b.updatedAt - a.updatedAt));
  if (selectedFolder) notes = notes.filter((n) => n.folderId === selectedFolder);
  if (query.trim()) {
    const q = query.trim().toLowerCase();
    notes = notes.filter((n) => n.title.toLowerCase().includes(q) || n.body.toLowerCase().includes(q));
  }
  return notes;
}

function go(name, extra) {
  route = name;
  if (name === "editor") editorId = extra ?? null;
  if (name === "folder") folderId = extra ?? null;
  location.hash = name === "home" ? "" : name;
  render();
}

function noteById(id) {
  return state.notes.find((n) => n.id === id);
}

function folderCount(id) {
  return state.notes.filter((n) => n.folderId === id).length;
}

function hsvToHex(h, s, v) {
  const f = (n) => {
    const k = (n + h / 60) % 6;
    return v - v * s * Math.max(Math.min(k, 4 - k, 1), 0);
  };
  const to = (x) => Math.round(x * 255).toString(16).padStart(2, "0");
  return `#${to(f(5))}${to(f(3))}${to(f(1))}`;
}

function appScreen(inner) {
  return `<div class="app-screen">${inner}</div>`;
}

function renderLanding() {
  const apkUrl = new URL(APK, location.href).href;
  const qr = `https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${encodeURIComponent(apkUrl)}`;
  return `
    <div class="page">
      <header class="topbar">
        <div class="brand"><img src="./icons/icon.svg" alt=""> ${t("app")}</div>
        <div class="actions">
          <button class="icon-btn" data-lang>${state.lang === "ru" ? "EN" : "RU"}</button>
          <a class="btn" href="${APK}" download="BreezNotes.apk">${t("download")}</a>
        </div>
      </header>
      <section class="hero">
        <div>
          <h1>${t("heroTitle")}</h1>
          <p>${t("heroText")}</p>
          <div class="hero-actions">
            <a class="btn" href="${APK}" download="BreezNotes.apk">${t("download")}</a>
            <button class="btn secondary" data-go="notes">${t("openWeb")}</button>
          </div>
        </div>
        <div class="phone">
          ${filteredNotes().slice(0, 3).map((n) => `
            <div class="phone-card" style="border-left-color:${escapeHtml(n.colorHex || "#4A90E2")}">
              <strong>${escapeHtml(n.title || t("untitled"))}</strong>
              <div>${escapeHtml((n.body || "").slice(0, 80))}</div>
            </div>`).join("")}
        </div>
      </section>
      <section class="features">
        <article class="card"><h3>${t("feature1t")}</h3><p>${t("feature1")}</p></article>
        <article class="card"><h3>${t("feature2t")}</h3><p>${t("feature2")}</p></article>
        <article class="card"><h3>${t("feature3t")}</h3><p>${t("feature3")}</p></article>
      </section>
      <section class="howto card" style="margin: 0 auto 80px;">
        <h2>${t("howTitle")}</h2>
        <ol>
          <li>${t("how1")}</li>
          <li>${t("how2")}</li>
          <li>${t("how3")}</li>
          <li>${t("how4")}</li>
        </ol>
        <div class="qr-box">
          <img src="${qr}" alt="QR">
          <p>${t("qrHint")}<br><small>${apkUrl}</small></p>
        </div>
      </section>
    </div>`;
}

function renderNotes() {
  const notes = filteredNotes();
  return appScreen(`
    <div class="download-bar">
      <span>${t("app")}</span>
      <a class="btn secondary" href="${APK}" download="BreezNotes.apk">${t("download")}</a>
    </div>
    <div class="app-top">
      <div class="brand">${t("notes")}</div>
      <div class="actions">
        <button class="icon-btn" data-go="folders">📁</button>
        <button class="icon-btn" data-go="theme">🎨</button>
        <button class="icon-btn" data-go="wallpaper">🖼</button>
        <button class="icon-btn" data-go="settings">⚙</button>
      </div>
    </div>
    <div class="search-wrap"><input class="search" id="search" placeholder="${t("search")}" value="${escapeHtml(query)}"></div>
    <div class="chips">
      <button class="chip ${selectedFolder ? "" : "active"}" data-folder="">${t("all")}</button>
      ${state.folders.map((f) => `<button class="chip ${selectedFolder === f.id ? "active" : ""}" data-folder="${escapeHtml(f.id)}"><span class="chip-mark" style="background:${escapeHtml(f.colorHex || "#7ED9C4")}"></span>${escapeHtml(f.name)}</button>`).join("")}
    </div>
    ${notes.length === 0 ? `<div class="empty">${t("empty")}</div>` : `<div class="list">${notes.map((n) => `
      <article class="note" data-open="${escapeHtml(n.id)}">
        <div class="note-stripe" style="background:${escapeHtml(n.colorHex || "#4A90E2")}"></div>
        <div class="note-body">
          <h3>${n.pinned ? "📌 " : ""}${escapeHtml(n.title || t("untitled"))}</h3>
          <p>${escapeHtml(n.body || "")}</p>
        </div>
        <button class="pin" data-pin="${escapeHtml(n.id)}">${n.pinned ? "★" : "☆"}</button>
      </article>`).join("")}</div>`}
    <button class="fab" data-new>+</button>`);
}

function renderEditor() {
  const note = editorId ? noteById(editorId) : { title: "", body: "", folderId: selectedFolder, colorHex: currentHex(), pinned: false };
  return appScreen(`
    <div class="app-top">
      <button class="icon-btn" data-go="notes">←</button>
      <strong>${t("save")}</strong>
      <button class="btn" id="save-note">${t("save")}</button>
    </div>
    <div class="editor">
      <input class="field title-input" id="note-title" placeholder="${t("title")}" value="${escapeHtml(note.title || "")}">
      <textarea class="field body-input" id="note-body" placeholder="${t("body")}">${escapeHtml(note.body || "")}</textarea>
      <label>${t("folder")}</label>
      <select class="field" id="note-folder">
        <option value="">${t("noFolder")}</option>
        ${state.folders.map((f) => `<option value="${escapeHtml(f.id)}" ${note.folderId === f.id ? "selected" : ""}>${escapeHtml(f.name)}</option>`).join("")}
      </select>
      <label>${t("color")}</label>
      <input class="field" id="note-color" type="color" value="${note.colorHex || "#4A90E2"}">
      <button class="btn secondary" id="toggle-pin">${note.pinned ? t("unpin") : t("pin")}</button>
      ${editorId ? `<button class="btn secondary" id="delete-note">${t("del")}</button>` : ""}
    </div>`);
}

function renderFolders() {
  return appScreen(`
    <div class="app-top">
      <button class="icon-btn" data-go="notes">←</button>
      <strong>${t("folders")}</strong>
      <span></span>
    </div>
    <div class="grid-2">
      ${state.folders.map((f) => `
        <article class="card folder-card" data-open-folder="${escapeHtml(f.id)}" style="--folder-color:${escapeHtml(f.colorHex || "#7ED9C4")}">
          <h3>${escapeHtml(f.name)}</h3>
          <p>${folderCount(f.id)}</p>
        </article>`).join("")}
    </div>
    <button class="fab" id="add-folder">+</button>`);
}

function renderFolder() {
  const folder = state.folders.find((f) => f.id === folderId);
  const notes = state.notes.filter((n) => n.folderId === folderId);
  return appScreen(`
    <div class="app-top">
      <button class="icon-btn" data-go="folders">←</button>
      <strong>${folder ? escapeHtml(folder.name) : t("folders")}</strong>
      <button class="icon-btn" id="delete-folder">🗑</button>
    </div>
    ${notes.length === 0 ? `<div class="empty">${t("empty")}</div>` : `<div class="list">${notes.map((n) => `
      <article class="note" data-open="${escapeHtml(n.id)}">
        <div class="note-stripe" style="background:${escapeHtml(n.colorHex || "#4A90E2")}"></div>
        <div class="note-body"><h3>${escapeHtml(n.title || t("untitled"))}</h3><p>${escapeHtml(n.body || "")}</p></div>
      </article>`).join("")}</div>`}`);
}

function renderSettings() {
  return appScreen(`
    <div class="app-top">
      <button class="icon-btn" data-go="notes">←</button>
      <strong>${t("settings")}</strong>
      <button class="icon-btn" data-go="home">${t("landing").slice(0, 1)}</button>
    </div>
    <div class="pad settings-body">
      <div class="settings-item" data-go="theme"><div><b>${t("appearance")}</b></div><span>›</span></div>
      <div class="settings-item" data-go="wallpaper"><div><b>${t("wallpaper")}</b></div><span>›</span></div>
      <div class="settings-item" id="about"><div><b>${t("about")}</b><div>${t("aboutText")}</div></div></div>
      <a class="btn block" style="margin-top:20px" href="${APK}" download="BreezNotes.apk">${t("download")}</a>
      ${renderUpdateSettings()}
    </div>`);
}

function renderTheme() {
  const modes = [["SYSTEM", t("system")], ["LIGHT", t("light")], ["DARK", t("dark")], ["AUTO", t("auto")]];
  return appScreen(`
    <div class="app-top"><button class="icon-btn" data-go="settings">←</button><strong>${t("appearance")}</strong><span></span></div>
    <div class="pad">
      <div class="card" style="margin-bottom:16px"><b>${t("title")}</b><p>${t("heroText")}</p></div>
      <h3>${t("theme")}</h3>
      <div class="mode-grid">${modes.map(([id, label]) => `<button class="mode ${state.themeMode === id ? "active" : ""}" data-mode="${id}">${label}</button>`).join("")}</div>
      <h3>${t("palettes")}</h3>
      <div class="palette-grid">${PALETTES.map((p) => `<button class="swatch ${state.paletteId === p.id ? "active" : ""}" data-palette="${p.id}" style="background:${p.hex}" title="${p.name}"></button>`).join("")}</div>
      <button class="btn block" style="margin-top:16px" id="custom-color">${t("custom")}</button>
    </div>`);
}

function renderWallpaper() {
  const w = state.wallpaper;
  return appScreen(`
    <div class="app-top"><button class="icon-btn" data-go="settings">←</button><strong>${t("wallpaper")}</strong><span></span></div>
    <div class="pad">
      <div class="chips" style="padding:0 0 16px">
        <button class="chip ${w.type === "BUILTIN" ? "active" : ""}" data-wtype="BUILTIN">${t("builtin")}</button>
        <button class="chip ${w.type === "PHOTO" ? "active" : ""}" data-wtype="PHOTO">${t("gallery")}</button>
        <button class="chip ${w.type === "COLOR" ? "active" : ""}" data-wtype="COLOR">${t("solid")}</button>
      </div>
      <div class="wall-grid">${WALLS.map((item) => `<button class="wall ${w.id === item.id && w.type === "BUILTIN" ? "active" : ""}" data-wall="${item.id}" style="background:${item.css}"></button>`).join("")}</div>
      <p><button class="btn secondary" id="pick-photo">${t("pickPhoto")}</button></p>
      <input id="photo-file" type="file" accept="image/*" hidden>
      <p><button class="btn secondary" id="pick-wall-color">${t("pickColor")}</button></p>
      <label>${t("dim")}</label>
      <input type="range" id="dim" min="0" max="0.8" step="0.05" value="${w.dim}">
    </div>`);
}

function renderUpdateSettings() {
  const status = updateInfo.checking
    ? t("updateChecking")
    : updateInfo.error
      ? t("updateFailed")
      : updateInfo.available
        ? `${t("updateAvailable")} ${updateInfo.latest || "—"}`
        : updateInfo.latest
          ? t("updateLatest")
          : "";
  const action = updateInfo.available
    ? `<button class="btn block" id="apply-update">${t("updateNow")}</button>`
    : `<button class="btn block" id="check-update">${t("updateCheck")}</button>`;
  return `
    <div class="update-settings">
      <div class="settings-item">
        <div>
          <b>${t("updateTitle")}</b>
          <div class="update-versions">
            <div class="update-version-row"><span>${t("currentVersion")}</span><strong>${APP_VERSION}</strong></div>
            <div class="update-version-row"><span>${t("releasedVersion")}</span><strong>${updateInfo.latest || "—"}</strong></div>
          </div>
          ${status ? `<div>${status}</div>` : ""}
        </div>
      </div>
      ${action}
    </div>`;
}

function updateBannerHtml() {
  if (!updateInfo.available || updateInfo.dismissed || route === "settings") return "";
  return `
    <div class="update-banner">
      <div>
        <b>${t("updateAvailable")} ${updateInfo.latest || "—"}</b>
        <div>${t("currentVersion")} ${APP_VERSION}</div>
        <div>${updateInfo.notes || t("updateAvailableText")}</div>
      </div>
      <div class="update-banner-actions">
        <button class="btn" id="apply-update">${t("updateNow")}</button>
        <button class="icon-btn" id="dismiss-update">×</button>
      </div>
    </div>`;
}

async function checkForAppUpdate() {
  updateInfo.checking = true;
  updateInfo.error = false;
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 8000);
    const response = await fetch(`./version.json?t=${Date.now()}`, {
      cache: "no-store",
      signal: controller.signal
    });
    clearTimeout(timeoutId);
    if (!response.ok) throw new Error("version");
    const data = await response.json();
    const remoteWeb = Number(data.webVersion || 0);
    updateInfo.latest = data.versionName || APP_VERSION;
    updateInfo.notes = data.notes || "";
    updateInfo.available = remoteWeb > WEB_VERSION || Boolean(waitingWorker);
    updateInfo.error = false;
  } catch {
    updateInfo.available = Boolean(waitingWorker);
    updateInfo.error = !updateInfo.available;
  } finally {
    updateInfo.checking = false;
    render();
  }
}

function applyAppUpdate() {
  if (waitingWorker) {
    waitingWorker.postMessage("SKIP_WAITING");
    waitingWorker = null;
  }
  location.reload();
}

function registerUpdater() {
  if ("serviceWorker" in navigator) {
    navigator.serviceWorker.register("./sw.js").then((reg) => {
      if (reg.waiting) {
        waitingWorker = reg.waiting;
        updateInfo.available = true;
        render();
      }
      reg.addEventListener("updatefound", () => {
        const worker = reg.installing;
        if (!worker) return;
        worker.addEventListener("statechange", () => {
          if (worker.state === "installed" && navigator.serviceWorker.controller) {
            waitingWorker = worker;
            updateInfo.available = true;
            updateInfo.dismissed = false;
            render();
          }
        });
      });
      reg.update();
    }).catch(() => {});
  }
  checkForAppUpdate();
}

function colorDialog() {
  return `<div class="dialog-backdrop" id="color-dialog">
    <div class="dialog">
      <b>${t("custom")}</b>
      <div class="sv" id="sv"></div>
      <div class="hue" id="hue"></div>
      <div id="hex-preview">${hsvToHex(hsv.h, hsv.s, hsv.v)}</div>
      <button class="btn" id="apply-color">${t("apply")}</button>
      <button class="btn secondary" id="close-color">${t("cancel")}</button>
    </div>
  </div>`;
}

function folderDialog() {
  return `<div class="dialog-backdrop" id="folder-dialog">
    <div class="dialog">
      <b>${t("newFolder")}</b>
      <input class="field" id="folder-name" placeholder="${t("folderName")}">
      <input class="field" id="folder-color" type="color" value="#7ED9C4">
      <button class="btn" id="create-folder">${t("create")}</button>
      <button class="btn secondary" id="close-folder">${t("cancel")}</button>
    </div>
  </div>`;
}

function render() {
  applyChrome();
  const root = document.getElementById("app");
  const map = {
    home: renderLanding,
    notes: renderNotes,
    editor: renderEditor,
    folders: renderFolders,
    folder: renderFolder,
    settings: renderSettings,
    theme: renderTheme,
    wallpaper: renderWallpaper
  };
  root.innerHTML = (map[route] || renderLanding)() + updateBannerHtml();
  document.body.classList.toggle("has-update", Boolean(updateInfo.available && !updateInfo.dismissed));
  bindEvents();
}

function closestAction(target, selector) {
  return target instanceof Element ? target.closest(selector) : null;
}

function handleAppClick(event) {
  const target = event.target;
  if (!(target instanceof Element)) return;
  if (closestAction(target, "#color-dialog, #folder-dialog")) return;

  const pin = closestAction(target, "[data-pin]");
  if (pin) {
    event.preventDefault();
    const note = noteById(pin.dataset.pin);
    if (note) {
      note.pinned = !note.pinned;
      save();
      render();
    }
    return;
  }
  const open = closestAction(target, "[data-open]");
  if (open) {
    go("editor", open.dataset.open);
    return;
  }
  const goEl = closestAction(target, "[data-go]");
  if (goEl) {
    go(goEl.dataset.go);
    return;
  }
  const lang = closestAction(target, "[data-lang]");
  if (lang) {
    state.lang = state.lang === "ru" ? "en" : "ru";
    save();
    render();
    return;
  }
  const folderChip = closestAction(target, "[data-folder]");
  if (folderChip) {
    selectedFolder = folderChip.dataset.folder || null;
    render();
    return;
  }
  const openFolder = closestAction(target, "[data-open-folder]");
  if (openFolder) {
    go("folder", openFolder.dataset.openFolder);
    return;
  }
  const mode = closestAction(target, "[data-mode]");
  if (mode) {
    state.themeMode = mode.dataset.mode;
    save();
    render();
    return;
  }
  const palette = closestAction(target, "[data-palette]");
  if (palette) {
    state.paletteId = palette.dataset.palette;
    save();
    render();
    return;
  }
  const wall = closestAction(target, "[data-wall]");
  if (wall) {
    state.wallpaper.type = "BUILTIN";
    state.wallpaper.id = wall.dataset.wall;
    save();
    render();
    return;
  }
  const wallType = closestAction(target, "[data-wtype]");
  if (wallType) {
    state.wallpaper.type = wallType.dataset.wtype;
    save();
    render();
    return;
  }
  if (closestAction(target, "[data-new]")) {
    go("editor", null);
    return;
  }

  const id = closestAction(target, "button, a")?.id;
  if (id === "save-note") saveNote();
  else if (id === "delete-note") {
    state.notes = state.notes.filter((n) => n.id !== editorId);
    save();
    go("notes");
  } else if (id === "toggle-pin") {
    const title = document.getElementById("note-title")?.value || "";
    const body = document.getElementById("note-body")?.value || "";
    persistDraft(title, body, true);
    render();
  } else if (id === "add-folder") openFolderDialog();
  else if (id === "delete-folder") {
    state.notes.forEach((n) => { if (n.folderId === folderId) n.folderId = null; });
    state.folders = state.folders.filter((f) => f.id !== folderId);
    save();
    go("folders");
  } else if (id === "custom-color") openColorPicker();
  else if (id === "pick-wall-color") {
    state.wallpaper.type = "COLOR";
    openColorPicker((hex) => { state.wallpaper.color = hex; save(); render(); });
  } else if (id === "pick-photo") document.getElementById("photo-file")?.click();
  else if (id === "check-update") {
    updateInfo.dismissed = false;
    checkForAppUpdate();
  } else if (id === "apply-update") applyAppUpdate();
  else if (id === "dismiss-update") {
    updateInfo.dismissed = true;
    render();
  }
}

function handleAppInput(event) {
  const target = event.target;
  if (!(target instanceof Element)) return;
  if (target.id === "search") {
    query = target.value;
    const pos = target.selectionStart;
    render();
    const next = document.getElementById("search");
    if (next) {
      next.focus();
      next.setSelectionRange(pos, pos);
    }
    return;
  }
  if (target.id === "dim") {
    state.wallpaper.dim = Number(target.value);
    save();
    applyChrome();
  }
}

function handleAppChange(event) {
  const target = event.target;
  if (!(target instanceof HTMLInputElement) || target.id !== "photo-file") return;
  const file = target.files && target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => {
    state.wallpaper.type = "PHOTO";
    state.wallpaper.photo = reader.result;
    save();
    render();
  };
  reader.readAsDataURL(file);
}

function bindEvents() {
  const root = document.getElementById("app");
  if (!root || root.dataset.bound === "1") return;
  root.dataset.bound = "1";
  root.addEventListener("click", handleAppClick);
  root.addEventListener("input", handleAppInput);
  root.addEventListener("change", handleAppChange);
}

function openFolderDialog() {
  document.getElementById("app").insertAdjacentHTML("beforeend", folderDialog());
  document.getElementById("close-folder").onclick = () => document.getElementById("folder-dialog").remove();
  document.getElementById("create-folder").onclick = () => {
    const name = document.getElementById("folder-name").value.trim();
    if (!name) return;
    state.folders.push(normalizeFolder({
      id: uid(),
      name,
      colorHex: document.getElementById("folder-color").value,
      sortOrder: state.folders.length
    }));
    save();
    render();
  };
}

function persistDraft(title, body, flipPin) {
  const folderSel = document.getElementById("note-folder").value || null;
  const colorHex = document.getElementById("note-color").value;
  if (editorId) {
    const note = noteById(editorId);
    if (!note) return;
    Object.assign(note, normalizeNote({
      ...note,
      title,
      body,
      folderId: folderSel,
      colorHex,
      pinned: flipPin ? !note.pinned : note.pinned,
      updatedAt: Date.now()
    }));
  } else {
    const created = normalizeNote({
      id: uid(),
      title,
      body,
      folderId: folderSel,
      colorHex,
      pinned: Boolean(flipPin),
      updatedAt: Date.now()
    });
    state.notes.unshift(created);
    editorId = created.id;
  }
  save();
}

function saveNote() {
  const titleEl = document.getElementById("note-title");
  const bodyEl = document.getElementById("note-body");
  if (!titleEl || !bodyEl) {
    go("notes");
    return;
  }
  persistDraft(titleEl.value, bodyEl.value, false);
  go("notes");
}

function paintSv() {
  const sv = document.getElementById("sv");
  if (!sv) return;
  const hex = hsvToHex(hsv.h, 1, 1);
  sv.style.background = `linear-gradient(to top, #000, transparent), linear-gradient(to right, #fff, ${hex})`;
  const preview = document.getElementById("hex-preview");
  if (preview) preview.textContent = hsvToHex(hsv.h, hsv.s, hsv.v);
}

function openColorPicker(onPick) {
  document.getElementById("app").insertAdjacentHTML("beforeend", colorDialog());
  paintSv();
  const sv = document.getElementById("sv");
  const hue = document.getElementById("hue");
  const setFrom = (el, clientX, clientY, isHue) => {
    const rect = el.getBoundingClientRect();
    if (isHue) hsv.h = Math.max(0, Math.min(360, ((clientX - rect.left) / rect.width) * 360));
    else {
      hsv.s = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
      hsv.v = Math.max(0, Math.min(1, 1 - (clientY - rect.top) / rect.height));
    }
    paintSv();
  };
  sv.onpointerdown = sv.onpointermove = (e) => { if (e.buttons) setFrom(sv, e.clientX, e.clientY, false); };
  hue.onpointerdown = hue.onpointermove = (e) => { if (e.buttons) setFrom(hue, e.clientX, e.clientY, true); };
  document.getElementById("close-color").onclick = () => document.getElementById("color-dialog").remove();
  document.getElementById("apply-color").onclick = () => {
    const hex = hsvToHex(hsv.h, hsv.s, hsv.v);
    if (typeof onPick === "function") onPick(hex);
    else { state.paletteId = "custom"; state.customHex = hex; }
    save();
    document.getElementById("color-dialog").remove();
    render();
  };
}

window.addEventListener("hashchange", () => {
  route = location.hash.replace("#", "") || "home";
  render();
});

applyChrome();
render();
registerUpdater();
