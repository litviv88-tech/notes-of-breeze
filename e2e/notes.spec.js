const { test, expect } = require("@playwright/test");

async function openNotes(page) {
  await page.addInitScript(() => localStorage.clear());
  await page.goto("/#notes");
  await expect(page.locator("[data-new]")).toBeVisible();
}

async function expectWallpaperCoversViewport(page) {
  const coverage = await page.evaluate(() => {
    const layer = document.getElementById("wallpaper");
    const rect = layer.getBoundingClientRect();
    return {
      width: rect.width,
      height: rect.height,
      left: rect.left,
      top: rect.top,
      viewportWidth: window.innerWidth,
      viewportHeight: window.innerHeight
    };
  });
  expect(coverage.left).toBeLessThanOrEqual(0);
  expect(coverage.top).toBeLessThanOrEqual(0);
  expect(coverage.width).toBeGreaterThanOrEqual(coverage.viewportWidth - 1);
  expect(coverage.height).toBeGreaterThanOrEqual(coverage.viewportHeight - 1);
}

test.describe("Breez Notes e2e", () => {
  test("лендинг открывается и фон покрывает весь экран", async ({ page }) => {
    await page.addInitScript(() => localStorage.clear());
    await page.goto("/");
    await expect(page.getByRole("heading", { name: "Спокойные заметки с живым стилем" })).toBeVisible();
    await expectWallpaperCoversViewport(page);
  });

  test("можно открыть веб-заметки и увидеть стартовую заметку", async ({ page }) => {
    await page.addInitScript(() => localStorage.clear());
    await page.goto("/");
    await page.locator("[data-go=notes]").click();
    await expect(page.locator(".note-body h3")).toContainText("Breez Notes");
    await expectWallpaperCoversViewport(page);
  });

  test("создание, поиск и закрепление заметки", async ({ page }) => {
    await openNotes(page);
    await page.locator("[data-new]").click();
    await page.locator("#note-title").fill("E2E заголовок");
    await page.locator("#note-body").fill("E2E текст заметки");
    await page.locator("#save-note").click();
    await expect(page.locator(".note-body h3", { hasText: "E2E заголовок" })).toBeVisible();

    await page.locator("#search").fill("E2E заголовок");
    await expect(page.locator("article.note")).toHaveCount(1);
    await page.locator("#search").fill("нет-такой-заметки");
    await expect(page.locator(".empty")).toBeVisible();
    await page.locator("#search").fill("");
    await expect(page.locator("article.note")).toHaveCount(2);

    await page.locator("article.note", { hasText: "E2E заголовок" }).locator(".pin").click();
    await expect(page.locator("article.note").first().locator("h3")).toContainText("E2E заголовок");
  });

  test("список дел создаётся и пункт можно вычеркнуть", async ({ page }) => {
    await openNotes(page);
    await page.locator("[data-new-todo]").click();
    await page.locator("#note-title").fill("E2E список");
    await page.locator("[data-todo-text]").first().fill("Купить молоко");
    await page.locator("[data-todo-done]").first().check();
    await page.locator("#save-note").click();
    await expect(page.locator("article.note", { hasText: "E2E список" })).toBeVisible();
    await expect(page.locator("article.note", { hasText: "E2E список" }).locator(".todo-done")).toContainText("Купить молоко");
  });

  test("создание папки", async ({ page }) => {
    await openNotes(page);
    await page.locator("#add-folder").click();
    await page.locator("#folder-name").fill("E2E папка");
    await page.locator("#create-folder").click();
    await expect(page.locator(".chip", { hasText: "E2E папка" })).toBeVisible();
  });

  test("переименование, копирование и перемещение заметок", async ({ page }) => {
    await openNotes(page);
    await page.locator("#add-folder").click();
    await page.locator("#folder-name").fill("Рабочая");
    await page.locator("#create-folder").click();
    await expect(page.locator(".chip", { hasText: "Рабочая" })).toBeVisible();

    await page.locator("[data-new]").click();
    await page.locator("#note-title").fill("Черновик");
    await page.locator("#note-body").fill("текст для переноса");
    await page.locator("#save-note").click();

    const note = page.locator("article.note", { hasText: "Черновик" });
    await note.locator("[data-note-menu]").click();
    await page.locator("#org-rename").click();
    await page.locator("#rename-value").fill("Переименованная");
    await page.locator("#confirm-rename").click();
    await expect(page.locator("article.note", { hasText: "Переименованная" })).toBeVisible();

    await page.locator("article.note", { hasText: "Переименованная" }).locator("[data-note-menu]").click();
    await page.locator("#org-move").click();
    await page.locator("[data-pick-folder]", { hasText: "Рабочая" }).click();
    await page.locator(".chip", { hasText: "Рабочая" }).click();
    await expect(page.locator("article.note", { hasText: "Переименованная" })).toBeVisible();

    await page.locator("article.note", { hasText: "Переименованная" }).locator("[data-note-menu]").click();
    await page.locator("#org-copy").click();
    await page.locator("[data-pick-folder]").first().click();
    await page.locator(".chip", { hasText: "Все заметки" }).click();
    await expect(page.locator("article.note", { hasText: "Переименованная" })).toHaveCount(2);
  });

  test("настройки показывают текущую и вышедшую версию", async ({ page }) => {
    await openNotes(page);
    await expect(page.getByText("Текущая версия", { exact: true })).toBeVisible();
    await expect(page.locator(".update-version-row").first()).toContainText("1.4.5");
    await expect.poll(async () => {
      return page.locator(".update-version-row").nth(1).innerText();
    }).toContain("1.4.5");
    await expectWallpaperCoversViewport(page);
  });

  test("тема и обои открываются без поломки фона", async ({ page }) => {
    await openNotes(page);
    await page.locator("[data-toggle-theme]").click();
    await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
    await page.locator("[data-go=theme]").click();
    await expect(page.locator("text=Единственная готовая палитра")).toBeVisible();
    await page.locator("[data-go=notes]").click();
    await page.locator("[data-go=wallpaper]").click();
    await expect(page.locator("[data-wtype=BUILTIN]")).toBeVisible();
    await page.locator("[data-wall=lavender_dream]").click();
    await expectWallpaperCoversViewport(page);
  });
});
