import { defineConfig, devices } from '@playwright/test';

/**
 * End-to-end UI tests of Larder against the RUNNING application:
 *   AppShell + micro-UIs + APIs on http://localhost:8080, Keycloak on http://localhost:8180.
 *
 * Sign-in: the project "setup" signs the local test user in once through the real Keycloak login form
 * and stores the browser state (the Keycloak session cookies) in .auth/cook.json. The AppShell keeps its
 * tokens in memory only, so every test page starts unauthenticated - the shell redirects to Keycloak,
 * which signs the cook in silently through the stored session cookie (no password prompt).
 *
 * All tests share one Keycloak user (one cook), and some change that cook's state (registration,
 * consents, notifications), so they run one after another (workers: 1).
 */
import { APP_URL, STORAGE_STATE } from './tests/support/env';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: 0,
  timeout: 90_000,
  expect: { timeout: 10_000 },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: APP_URL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'off',
    viewport: { width: 1280, height: 900 },
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
  },
  projects: [
    {
      name: 'setup',
      testMatch: /auth\.setup\.ts/,
      use: { ...devices['Desktop Chrome'], viewport: { width: 1280, height: 900 } },
    },
    {
      name: 'chromium',
      testMatch: /.*\.spec\.ts/,
      dependencies: ['setup'],
      use: { ...devices['Desktop Chrome'], viewport: { width: 1280, height: 900 }, storageState: STORAGE_STATE },
    },
  ],
});
