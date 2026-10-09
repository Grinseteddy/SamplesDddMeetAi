import { test, expect, openApp, shellDialog, unique } from './support/fixtures';

/**
 * 4. Orchestration Meal Planning → AppShell → Recipe Catalog → Meal Planning:
 * "Add a course" asks the shell for a recipe (larder:pick-recipe), the shell shows Recipe Catalog's picker
 * in a dialog, "Choose" hands the recipe back (recipePicked), and the course shows Recipe Catalog's card.
 */
test.describe('meal planning with the recipe picker', () => {
  let recipeId: string | undefined;
  let planId: string | undefined;

  test.afterEach(async ({ api }) => {
    if (planId) await api.deleteMealPlan(planId);
    if (recipeId) await api.deleteRecipe(recipeId);
  });

  test('new plan → "Add a course" → picker → "Choose" → course with the recipe card', async ({ page, api }) => {
    const name = unique('Potato gratin');
    recipeId = await api.createRecipe(name, ['Slice the potatoes', 'Bake']);

    await openApp(page, '#/meal-plans');
    await expect(page.getByRole('heading', { name: 'Meal plans', level: 1 })).toBeVisible();
    await page.getByRole('button', { name: 'New plan' }).click();
    await expect(page).toHaveURL(/#\/meal-plans\/[\w-]+$/);
    planId = page.url().split('/').pop();

    const plan = page.locator('larder-meal-planning-plan');
    await expect(plan.getByText('No courses yet. Add the first dish of your meal.')).toBeVisible();
    await plan.getByRole('button', { name: '+ Add a course' }).click();

    const picker = shellDialog(page, 'Pick a recipe');
    await expect(picker).toBeVisible();
    await expect(picker.getByText(/\d+ recipes?/)).toBeVisible(); // the catalog search has answered
    await picker.getByLabel('Name', { exact: true }).fill(name);
    await expect(picker.getByText('1 recipe', { exact: true })).toBeVisible();
    await picker.getByRole('button', { name: `Choose ${name}` }).click();

    await expect(picker).toBeHidden();
    const course = plan.getByRole('listitem').filter({ has: page.getByRole('link', { name }) });
    await expect(course).toHaveCount(1);
    await expect(course.getByRole('heading', { name, level: 3 })).toBeVisible();
    await expect(course.getByLabel('Serving order of this course')).toHaveValue('1');
    await expect(plan.getByText('Saved', { exact: true })).toBeVisible();

    // The course is stored: it survives a reload.
    await page.reload();
    await expect(page.locator('larder-meal-planning-plan').getByRole('link', { name })).toBeVisible({ timeout: 30_000 });

    // The card is Recipe Catalog's: it opens the recipe.
    await page.locator('larder-meal-planning-plan').getByRole('link', { name }).click();
    await expect(page).toHaveURL(new RegExp(`#/recipes/${recipeId}$`));
    await expect(page.getByRole('heading', { name, level: 1 })).toBeVisible();
  });
});
