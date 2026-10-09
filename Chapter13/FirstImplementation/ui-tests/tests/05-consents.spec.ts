import { test, expect, openApp } from './support/fixtures';
import { CONSENT } from './support/env';

/** 5. Consents on the profile page: give and revoke a consent; the history keeps both. */
test.describe('consents', () => {
  test.beforeEach(async ({ api }) => {
    await api.revokeConsent(CONSENT.mention.id); // precondition: not given
  });

  test('give and revoke a consent, the history shows it', async ({ page }) => {
    await openApp(page, '#/profile');
    const consents = page.locator('larder-consent-management-consents');
    await expect(consents.getByRole('heading', { name: 'Your consents' })).toBeVisible();
    const item = consents.getByRole('listitem').filter({ has: page.locator('blockquote', { hasText: CONSENT.mention.text }) });
    await expect(item.locator('blockquote')).toHaveText(CONSENT.mention.text);
    await expect(item.getByText('Not given', { exact: true })).toBeVisible();

    // Give
    await item.getByRole('button', { name: 'I agree' }).click();
    await expect(item.getByText('Given', { exact: true })).toBeVisible();
    await expect(item.getByText(/^since /)).toBeVisible();
    const history = item.locator('details');
    const before = Number((await history.locator('summary').textContent())?.match(/\d+/)?.[0]);

    // Revoke
    await item.getByRole('button', { name: 'Revoke' }).click();
    await expect(item.getByText('Not given', { exact: true })).toBeVisible();
    await expect(item.getByText(/^revoked /)).toBeVisible();
    await expect(item.getByRole('button', { name: 'I agree' })).toBeVisible();

    // History: revoked consents are kept as evidence; the newest entry is the one just revoked.
    await expect(history.locator('summary')).toHaveText(`History (${before})`);
    await history.locator('summary').click();
    const entries = history.getByRole('listitem');
    await expect(entries).toHaveCount(before);
    await expect(entries.first()).toContainText(/Given .* · revoked /);
    await expect(history.getByText('in force')).toHaveCount(0);
  });
});
