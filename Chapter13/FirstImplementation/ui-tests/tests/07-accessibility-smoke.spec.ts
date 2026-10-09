import { test, expect, openApp, shellDialog, unique } from './support/fixtures';

/**
 * 7. Accessibility smoke over every main view: the page renders without uncaught exceptions and without
 * console errors (the auto fixture `consoleGuard` fails the test otherwise - the only 4xx tolerated are pictures
 * of older thanks that Media no longer has),
 * every view has one level-1 heading, every button and link has an accessible name, every image has alt text.
 */
const VIEWS: [string, string | RegExp][] = [
  ['#/recipes', 'Recipes'],
  ['#/recipes/new', /recipe/i],
  ['#/meal-plans', 'Meal plans'],
  ['#/help', 'Help board'],
  ['#/thanks', 'Thanks'],
  ['#/notifications', /Notifications/],
  ['#/profile', /\S/],
];

test.describe('accessibility smoke', () => {
  // Older thanks in the shared feed may point to pictures that were deleted from Media meanwhile; the feed
  // shows "This picture is no longer available" for them, and the browser logs their 404.
  test.use({ expectedConsoleErrors: [/\/media\/images\/[\w-]+$/] });

  test('main views: no errors, a heading, named controls, alt texts', async ({ page }) => {
    await openApp(page, '#/recipes');
    expect(await page.getAttribute('html', 'lang')).toBe('en');

    for (const [hash, heading] of VIEWS) {
      await test.step(hash, async () => {
        await page.evaluate((h) => { location.hash = h; }, hash);
        await expect(page).toHaveURL(new RegExp(`${hash.replace(/[/#]/g, '\\$&')}$`));
        const h1 = page.locator('main').getByRole('heading', { level: 1 });
        await expect(h1.first()).toBeVisible();
        await expect(h1.first()).toHaveText(heading);
        await expect(page.locator('main .loading')).toHaveCount(0, { timeout: 15_000 }); // views finished loading

        for (const control of await page.getByRole('button').all()) {
          if (await control.isVisible()) await expect(control).toHaveAccessibleName(/\S/);
        }
        for (const link of await page.getByRole('link').all()) {
          if (await link.isVisible()) await expect(link).toHaveAccessibleName(/\S/);
        }
        for (const image of await page.locator('img').all()) {
          if (await image.isVisible()) expect(await image.getAttribute('alt'), 'img has an alt attribute').not.toBeNull();
        }
      });
    }
  });

  test.describe('shell dialog', () => {
    let recipeId: string | undefined;
    test.afterEach(async ({ api }) => { if (recipeId) await api.deleteRecipe(recipeId); });

    test('the modal dialog is named by its title', async ({ page, api }) => {
      test.fail(true, 'UI DEFECT (app-shell/shell.js, layout()): <dialog id="dialog"> has no aria-labelledby="dialog-title" '
        + '(nor aria-label), so screen readers announce an unnamed dialog. Expected accessible name "Start cooking", actual "".');
      recipeId = await api.createRecipe(unique('Dialog check'), ['Stir']);
      await openApp(page, `#/recipes/${recipeId}`);
      await page.getByRole('button', { name: 'Start cooking' }).click();
      const dialog = shellDialog(page, 'Start cooking');
      await expect(dialog).toBeVisible();
      await expect(dialog).toHaveAccessibleName('Start cooking', { timeout: 2_000 });
    });
  });
});
