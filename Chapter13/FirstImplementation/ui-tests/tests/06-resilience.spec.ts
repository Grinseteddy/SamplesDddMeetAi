import { test, expect, openApp } from './support/fixtures';

/**
 * 6. A micro-UI that cannot be loaded degrades to a placeholder; the rest of the shell keeps working.
 * Sharing's entry module answers 404 (page.route), so the browser logs that 404 - expected here.
 */
test.describe('resilience', () => {
  test.use({ expectedConsoleErrors: [/\/ui\/sharing\/index\.js/] });

  test('a missing micro-UI shows "not available", the shell still works', async ({ page }) => {
    await page.route('**/ui/sharing/index.js', (route) => route.fulfill({ status: 404, contentType: 'text/plain', body: 'gone' }));
    const warnings: string[] = [];
    page.on('console', (message) => { if (message.type() === 'warning') warnings.push(message.text()); });

    await openApp(page, '#/thanks');
    await expect(page.getByText('This part of Larder (sharing) is not available right now.')).toBeVisible();
    expect(warnings.some((text) => text.includes('Micro-UI of sharing not available'))).toBe(true);

    // The other parts work: navigation, recipes, help board, the bell.
    await page.getByRole('link', { name: 'Recipes', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Recipes', level: 1 })).toBeVisible();
    await page.getByRole('link', { name: 'Help board' }).click();
    await expect(page.getByRole('heading', { name: 'Help board', level: 1 })).toBeVisible();
    await expect(page.getByRole('button', { name: /^Notifications/ })).toBeVisible();
    await page.getByRole('link', { name: 'Thanks', exact: true }).click();
    await expect(page.getByText('This part of Larder (sharing) is not available right now.')).toBeVisible();
  });
});
