import { test, expect, openApp, pictureFile, shellDialog, unique } from './support/fixtures';
import { CONSENT } from './support/env';

/**
 * 2. The rescue story through the UI - the flow the AppShell orchestrates across seven contexts:
 * Recipe Catalog → Meal Preparation (cooking mode, step text from Recipe Catalog) → "Catastrophe!" →
 * Media (picture) → Cooking Assistance (help request, Grandma's answer) → Sharing (thanks with a
 * required picture) → Consent Management (missing photo consent) → "Meal rescued".
 */
test.describe('rescue story', () => {
  // Sharing answers 400 PICTURE_WITHOUT_CONSENT before the consent dialog - provoked on purpose.
  test.use({ expectedConsoleErrors: [/\/sharing\/thanks$/] });

  let recipeId: string | undefined;
  let marker: string;

  test.beforeEach(async ({ api }) => {
    // Precondition: the photo consent is missing, so the consent dialog path is always exercised.
    await api.revokeConsent(CONSENT.photos.id);
    marker = unique('Rescue');
  });

  test.afterEach(async ({ api }) => {
    await api.deleteThanksContaining(marker);
    if (recipeId) await api.deleteRecipe(recipeId);
  });

  test('from a burnt dish to "Meal rescued"', async ({ page, api }) => {
    const step1 = `Mix flour and buttermilk (${marker})`;
    const step2 = `Bake the scones at 220 degrees for 12 minutes (${marker})`;
    recipeId = await api.createRecipe(`Scones ${marker}`, [step1, step2]);

    // Recipe page → "Start cooking" opens the shell's dialog with Meal Preparation's start element.
    await openApp(page, `#/recipes/${recipeId}`);
    await expect(page.getByRole('heading', { name: `Scones ${marker}`, level: 1 })).toBeVisible();
    await page.getByRole('button', { name: 'Start cooking' }).click();
    const dialog = shellDialog(page, 'Start cooking');
    await expect(dialog).toBeVisible();
    await expect(dialog.getByText('Ready at the stove?')).toBeVisible();
    await dialog.getByRole('button', { name: 'Start cooking' }).click();

    // Cooking mode: Meal Preparation drives (Step 1), Recipe Catalog explains (step text).
    await expect(page).toHaveURL(/#\/cook\/[\w-]+$/);
    await expect(dialog).toBeHidden();
    const cooking = page.locator('larder-meal-preparation-cooking');
    const explanation = page.locator('larder-recipe-catalog-step');
    await expect(cooking.getByRole('region', { name: 'Cooking mode' }).getByText('Step 1', { exact: true })).toBeVisible();
    await expect(explanation.getByText(step1)).toBeVisible();
    await expect(cooking.getByRole('button', { name: 'Previous step' })).toBeDisabled();

    await cooking.getByRole('button', { name: 'Next step' }).click();
    await expect(cooking.getByRole('region', { name: 'Cooking mode' }).getByText('Step 2', { exact: true })).toBeVisible();
    await expect(explanation.getByText(step2)).toBeVisible();
    await expect(explanation.getByText('2 of 2')).toBeVisible();

    // "Catastrophe!" starts the rescue flow.
    await cooking.getByRole('button', { name: 'Catastrophe!' }).click();
    await expect(page).toHaveURL(/#\/rescue$/);
    await expect(page.getByRole('heading', { name: 'Kitchen catastrophe - let’s rescue your meal' })).toBeVisible();

    // Step 1 of the rescue: a picture of what happened.
    const rescue = page.locator('larder-rescue-flow');
    await rescue.getByLabel('Choose from gallery').setInputFiles(pictureFile('burnt-scones.png'));
    await expect(rescue.getByRole('img', { name: 'Preview of the chosen picture' })).toBeVisible();
    await rescue.getByRole('button', { name: 'Use this picture' }).click();

    // Step 2: the help request (type fixed by the situation; Grandma and the community preselected).
    const form = page.locator('larder-cooking-assistance-request-form');
    await expect(form.getByRole('heading', { name: /Kitchen catastrophe!/ })).toBeVisible();
    await expect(form.getByRole('checkbox', { name: /Grandma Avatar/ })).toBeChecked();
    await form.getByLabel('Title').fill(`Burnt scones ${marker}`);
    await form.getByLabel('What is going on?').fill('The scones are burnt at the bottom and my guests arrive in 30 minutes.');
    await form.getByRole('button', { name: 'Ask for help' }).click();

    // Step 3: waiting - Grandma answers asynchronously ("Stay calm" for burnt dishes).
    const status = page.locator('larder-cooking-assistance-help-status');
    await expect(status.getByText('Help has arrived!')).toBeVisible({ timeout: 30_000 });
    await expect(status.getByRole('heading', { name: 'Stay calm' })).toBeVisible();
    await status.getByRole('button', { name: 'Say thanks' }).click();

    // Step 4: thanks - a picture of the rescued meal is required, there is no "Skip".
    const thanks = page.locator('larder-sharing-thanks-form');
    await expect(thanks.getByRole('heading', { name: 'Say thanks' })).toBeVisible();
    await expect(thanks.getByText('Grandma Avatar', { exact: true })).toBeVisible();
    await expect(thanks.getByRole('button', { name: 'Skip' })).toHaveCount(0);
    await thanks.getByLabel('Your thanks').fill(`Grandma saved my scones! ${marker}`);
    await thanks.getByRole('button', { name: 'Send thanks' }).click();
    await expect(thanks.getByRole('alert')).toContainText('Please take or choose one first.');

    await thanks.getByLabel('Choose from gallery').setInputFiles(pictureFile('rescued-scones.png'));
    await thanks.getByRole('button', { name: 'Use this picture' }).click();
    await expect(thanks.getByText('Picture chosen ✓')).toBeVisible();
    await expect(thanks.getByRole('alert')).toHaveCount(0);
    await thanks.getByRole('button', { name: 'Send thanks' }).click();

    // The photo consent is missing: the shell asks for it with Consent Management's exact wording.
    const consent = shellDialog(page, 'Your consent');
    await expect(consent).toBeVisible();
    await expect(consent.getByRole('heading', { name: 'Your photos in public thanks' })).toBeVisible();
    await expect(consent.locator('blockquote')).toHaveText(CONSENT.photos.text);
    await consent.getByRole('button', { name: 'Allow' }).click();

    // On consent the thanks form retries by itself.
    await expect(consent).toBeHidden();
    await expect(page.getByRole('heading', { name: /Meal rescued/ })).toBeVisible({ timeout: 20_000 });
    await expect(page.getByRole('button', { name: 'Back to cooking' }).last()).toBeVisible();
    await expect.poll(async () => (await api.myThanks()).some((t) => t.thanksText.includes(marker)),
      { message: 'the thanks are stored by Sharing' }).toBe(true);
  });
});

/** Cooking mode at phone width (375 px): the step, its text and the big buttons fit the screen. */
test.describe('cooking mode on a phone', () => {
  test.use({ viewport: { width: 375, height: 812 }, isMobile: true, hasTouch: true });

  let recipeId: string | undefined;

  test.afterEach(async ({ api }) => {
    if (recipeId) await api.deleteRecipe(recipeId);
  });

  test('shows the step and its buttons without horizontal scrolling', async ({ page, api }) => {
    const marker = unique('Phone');
    const step1 = `Peel the potatoes (${marker})`;
    recipeId = await api.createRecipe(`Potato soup ${marker}`, [step1, 'Boil them', 'Serve']);
    const preparationId = await api.startPreparation(recipeId);

    await openApp(page, `#/cook/${preparationId}`);
    const cooking = page.locator('larder-meal-preparation-cooking');
    await expect(cooking.getByRole('region', { name: 'Cooking mode' }).getByText('Step 1', { exact: true })).toBeVisible();
    await expect(page.locator('larder-recipe-catalog-step').getByText(step1)).toBeVisible();

    for (const name of ['Next step', 'This step is unclear', 'Catastrophe!']) {
      const button = cooking.getByRole('button', { name });
      await expect(button).toBeInViewport();
      const box = await button.boundingBox();
      expect(box!.x).toBeGreaterThanOrEqual(0);
      expect(box!.x + box!.width).toBeLessThanOrEqual(375);
      expect(box!.height, `"${name}" is a big touch target`).toBeGreaterThanOrEqual(44);
    }
    // Cooking mode stacks: the explanation goes below the controls on a phone.
    const controls = await cooking.boundingBox();
    const explanation = await page.locator('larder-recipe-catalog-step').boundingBox();
    expect(explanation!.y).toBeGreaterThan(controls!.y + controls!.height - 1);

    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow, 'no horizontal scrolling').toBeLessThanOrEqual(0);

    await cooking.getByRole('button', { name: 'Next step' }).tap();
    await expect(cooking.getByRole('region', { name: 'Cooking mode' }).getByText('Step 2', { exact: true })).toBeVisible();
    await expect(page.locator('larder-recipe-catalog-step').getByText('Boil them')).toBeVisible();
  });
});
