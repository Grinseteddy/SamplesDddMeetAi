import { test, expect, openApp, png, unique } from './support/fixtures';
import { CONSENT } from './support/env';

/**
 * 3. The community sees the thanks, and the cook is told about the help.
 * The rescue itself is not the subject here, so it is arranged through the APIs: a help request,
 * Grandma's asynchronous help, a picture, the photo consent and the thanks.
 */
test.describe('thanks feed and notification bell', () => {
  // Older thanks in the shared feed may point to pictures that were deleted from Media meanwhile; the feed
  // shows "This picture is no longer available" for them, and the browser logs their 404.
  test.use({ expectedConsoleErrors: [/\/media\/images\/[\w-]+$/] });

  let recipeId: string | undefined;
  let marker: string;
  let helpId: string;
  let notificationId: string | undefined;

  test.beforeEach(async ({ api }) => {
    marker = unique('Feed');
    recipeId = await api.createRecipe(`Pancakes ${marker}`, ['Whisk', 'Fry']);
    const helpRequestId = await api.askForHelp(`Burnt pancakes ${marker}`, 'The pancakes are burnt black.', recipeId);
    helpId = (await api.awaitHelp(helpRequestId)).helpId;
    await api.giveConsent(CONSENT.photos.id);
    const picture = await api.uploadImage(png(32, 32, [240, 190, 60]), helpRequestId);
    await api.giveThanks(helpId, `Grandma saved my pancakes! ${marker}`, picture);
    await expect.poll(async () => {
      notificationId = (await api.notifications()).find((n) => n.link.endsWith(`/helps/${helpId}`) && n.status === 'NEW')?.notificationId;
      return notificationId;
    }, { message: 'Notification has told the cook about the help', timeout: 30_000 }).toBeTruthy();
    await api.readAllNotifications(notificationId); // the bell then counts only the notification of this test
  });

  test.afterEach(async ({ api }) => {
    await api.deleteThanksContaining(marker);
    if (notificationId) await api.deleteNotification(notificationId);
    if (recipeId) await api.deleteRecipe(recipeId);
  });

  test('the feed shows the new thanks with its picture', async ({ page }) => {
    await openApp(page, '#/thanks');
    await expect(page.getByRole('heading', { name: 'Thanks', level: 1 })).toBeVisible();
    const card = page.locator('larder-sharing-feed article').filter({ hasText: marker });
    await expect(card).toHaveCount(1);
    await expect(card.locator('blockquote')).toHaveText(`Grandma saved my pancakes! ${marker}`);
    await expect(card.getByText('Grandma Avatar')).toBeVisible();

    const picture = card.getByRole('img', { name: 'Picture shared in Larder' });
    await expect(picture).toBeVisible();
    await expect(picture).toHaveAttribute('src', /^data:image\/png;base64,/);
    expect(await picture.evaluate((img: HTMLImageElement) => img.naturalWidth)).toBe(32);

    // "Given by me" keeps it, it links to the help.
    await page.getByRole('button', { name: 'Given by me' }).click();
    await expect(page.getByRole('button', { name: 'Given by me' })).toHaveAttribute('aria-pressed', 'true');
    await expect(card).toHaveCount(1);
    await card.getByRole('button', { name: 'See the help' }).click();
    await expect(page).toHaveURL(new RegExp(`#/helps/${helpId}$`));
  });

  test('the bell shows the NEW notification and opens the help', async ({ page }) => {
    await openApp(page, '#/recipes');
    const bell = page.getByRole('button', { name: /^Notifications/ });
    await expect(bell).toHaveAccessibleName('Notifications, 1 new');
    await expect(page.locator('larder-notification-bell .count')).toHaveText('1');

    await bell.click();
    const panel = page.getByRole('region', { name: 'Latest notifications' });
    await expect(panel).toBeVisible();
    const item = panel.locator(`button[data-id="${notificationId}"]`);
    await expect(item).toContainText('Stay calm');
    await expect(item).toContainText('(new)');
    await item.click();

    await expect(page).toHaveURL(new RegExp(`#/helps/${helpId}$`));
    await expect(page.locator('larder-cooking-assistance-help').getByRole('heading', { name: 'Stay calm' })).toBeVisible();
    await expect(bell).toHaveAccessibleName('Notifications, none new');
    await expect(panel).toBeHidden();
  });
});
