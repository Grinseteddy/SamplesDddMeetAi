import { test as setup, expect } from '@playwright/test';
import { APP_URL, ISSUER, STORAGE_STATE, TEST_USER } from './support/env';

/**
 * Signs the test cook in ONCE through the real Keycloak login form and stores the browser state.
 *
 * The AppShell keeps its tokens in memory only, so the stored state holds no token - what makes later
 * pages sign in without a password is the Keycloak SSO session cookie (KEYCLOAK_IDENTITY / KEYCLOAK_SESSION
 * for localhost:8180), which the shell's redirect to Keycloak carries along. The spec files verify that
 * (`openApp` fails if a login form shows up).
 */
setup('sign in through Keycloak and keep the session', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL((url) => url.href.startsWith(`${ISSUER}/protocol/openid-connect/auth`));
  await page.locator('#username').fill(TEST_USER.username);
  await page.locator('#password').fill(TEST_USER.password);
  await page.locator('#kc-login').click();

  // Back in the AppShell: the code was exchanged for tokens and the header is shown.
  await expect(page.getByRole('link', { name: 'Meal plans' })).toBeVisible({ timeout: 30_000 });
  await expect(page).toHaveURL((url) => url.origin === new URL(APP_URL).origin && !url.search);

  const state = await page.context().storageState({ path: STORAGE_STATE });
  expect(state.cookies.map((cookie) => cookie.name), 'Keycloak session cookie stored').toContain('KEYCLOAK_IDENTITY');
});
