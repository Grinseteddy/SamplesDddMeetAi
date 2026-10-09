import { test, expect } from './support/fixtures';
import { ISSUER, TEST_USER } from './support/env';

/**
 * 1. Sign-in and the Cook Profile gate.
 * A fresh browser (no stored session) is sent to Keycloak, signs in with the login form, and comes back
 * to the shell. The cook is not registered (precondition via API), so the Cook Profile gate shows the
 * registration; after "Join Larder" the app opens.
 */
test.describe('sign-in and Cook Profile gate', () => {
  test.use({
    storageState: { cookies: [], origins: [] }, // no Keycloak session: the real login form appears
    registered: false,
    expectedConsoleErrors: [/\/cook-profile\/cooks\/[\w-]+/], // 404 of "is this cook registered?" - provoked here
  });

  test('signs in via Keycloak, registers an unknown cook and opens the app', async ({ page, api }) => {
    await api.deregister();
    expect(await api.isRegistered()).toBe(false);

    await page.goto('/');
    await expect(page, 'the shell sends the browser to Keycloak (Authorization Code + PKCE)')
      .toHaveURL((url) => url.href.startsWith(`${ISSUER}/protocol/openid-connect/auth`) && url.searchParams.get('code_challenge_method') === 'S256');
    await page.locator('#username').fill(TEST_USER.username);
    await page.locator('#password').fill(TEST_USER.password);
    await page.locator('#kc-login').click();

    // The gate: Cook Profile does not know the cook, so it asks to register.
    await expect(page.getByRole('heading', { name: 'Welcome to Larder' })).toBeVisible({ timeout: 30_000 });
    await expect(page, 'the authorization code is removed from the address').not.toHaveURL(/[?&]code=/);
    await expect(page.getByRole('heading', { name: 'Recipes', level: 1 })).toHaveCount(0);

    await page.getByLabel('Given name').fill(TEST_USER.givenName);
    await page.getByLabel('Name', { exact: true }).fill(TEST_USER.name);
    await page.getByLabel('Email').fill(TEST_USER.email);
    await page.getByRole('button', { name: 'Join Larder' }).click();

    // The app opens on the recipes.
    await expect(page.getByRole('heading', { name: 'Recipes', level: 1 })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Welcome to Larder' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: /^Notifications/ })).toBeVisible();
    expect(await api.isRegistered()).toBe(true);
  });
});
