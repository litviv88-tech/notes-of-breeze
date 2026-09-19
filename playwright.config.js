const { defineConfig } = require("@playwright/test");

module.exports = defineConfig({
  testDir: "./e2e",
  timeout: 30_000,
  fullyParallel: false,
  retries: 0,
  use: {
    baseURL: "http://127.0.0.1:4173",
    viewport: { width: 390, height: 844 },
    locale: "ru-RU"
  },
  webServer: {
    command: "node e2e/serve.mjs",
    url: "http://127.0.0.1:4173",
    reuseExistingServer: !process.env.CI,
    timeout: 15_000
  },
  projects: [
    { name: "chromium", use: { browserName: "chromium" } }
  ]
});
