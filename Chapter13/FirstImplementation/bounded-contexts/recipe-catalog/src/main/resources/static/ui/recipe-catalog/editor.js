/**
 * `larder-recipe-catalog-editor` (`recipe`?): create a recipe, or edit one of the cook's own recipes,
 * including its ingredients and how-to steps.
 *
 * Creating is one POST /recipes. Editing follows the contract's resources: PATCH /recipes/{id} for the
 * recipe's own details, PUT /recipes/{id}/meals for the meal, and the ingredient and how-to-step
 * sub-resources (POST / PATCH / DELETE) for every row that was added, changed or removed. The rules of the
 * recipe hold after every single call: rows are added before others are removed (a recipe keeps at least one
 * ingredient and step), and steps that change their number first move to free numbers (numbers are unique).
 * PATCH cannot remove subtitle, main image or illustration (contracts/CHANGES.md), so the editor says so.
 */
import { LarderElement, errorMessage, fmt, html } from '/app-shell/kit.js';
import { CONTEXT, DIETS, MEALS, UNITS, UNIT_NAMES, forget, formError, idFrom, loadRecipe } from './shared.js';

const fill = (element, template) => {
  if (element) element.innerHTML = template.__html;
};

const pad = (number) => String(Math.max(0, Number(number) || 0)).padStart(2, '0');
const splitTime = (hhmm) => {
  const match = /^(\d{2}):(\d{2})$/.exec(hhmm || '');
  return match ? { hours: Number(match[1]), minutes: Number(match[2]) } : { hours: 0, minutes: 30 };
};

/** A rule the cook can fix in the form, found before any request is sent. */
class FormProblem extends Error {}

let keys = 0;
const nextKey = () => `r${++keys}`;

export class RecipeEditor extends LarderElement {
  static styles = `
    .code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: .78rem; opacity: .8; margin-left: 4px; }
    .fields { display: grid; gap: 14px; grid-template-columns: repeat(auto-fit, minmax(min(100%, 200px), 1fr)); }
    .fields .wide { grid-column: 1 / -1; }
    .hint { font-weight: 400; font-size: .8rem; color: var(--larder-text-muted); }
    .time { display: flex; gap: 8px; align-items: center; }
    .time input { width: 100%; min-width: 0; }
    .time span { font-weight: 400; color: var(--larder-text-muted); }
    .section-head { display: flex; justify-content: space-between; align-items: baseline; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }
    .section-head h2 { margin: 0; }
    .rows { display: flex; flex-direction: column; gap: 10px; }
    .steps-list { list-style: none; margin: 0; padding: 0; }
    .ingredient { display: grid; gap: 10px; align-items: end;
                  grid-template-columns: minmax(0, 2fr) minmax(84px, .8fr) minmax(110px, 1fr) auto; }
    .ingredient label, .step label { font-size: .78rem; }
    @media (max-width: 560px) {
      .ingredient { grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto; padding-bottom: 10px;
                    border-bottom: 1px dashed var(--larder-border); }
      .ingredient .name { grid-column: 1 / -1; }
    }
    .step { display: grid; grid-template-columns: 40px minmax(0, 1fr); gap: 12px; padding: 12px;
            border-radius: var(--larder-radius-small); background: var(--larder-surface-muted); }
    .step .number { width: 36px; height: 36px; border-radius: 50%; background: var(--larder-accent); color: var(--larder-accent-text);
                    display: grid; place-items: center; font-weight: 700; font-family: var(--larder-font-serif); }
    .step .content { display: flex; flex-direction: column; gap: 10px; }
    .step .tools { display: flex; gap: 6px; flex-wrap: wrap; justify-content: flex-end; }
    .icon { padding: 7px 12px; min-width: 40px; }
    .savebar { position: sticky; bottom: 0; z-index: 1; display: flex; flex-direction: column; gap: 10px; padding: 14px 16px;
               background: color-mix(in srgb, var(--larder-bg) 92%, transparent); backdrop-filter: blur(6px);
               border-top: 1px solid var(--larder-border); border-radius: var(--larder-radius) var(--larder-radius) 0 0; }
    .actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
    @media (max-width: 480px) { .actions .btn { flex: 1 1 auto; } }
  `;

  constructor() {
    super();
    this.on('click', '[data-action=add-ingredient]', () => this.addIngredient());
    this.on('click', '[data-action=add-step]', () => this.addStep());
    this.on('click', '[data-remove-ingredient]', (event, target) => this.removeRow('ingredients', target.dataset.removeIngredient));
    this.on('click', '[data-remove-step]', (event, target) => this.removeRow('steps', target.dataset.removeStep));
    this.on('click', '[data-up]', (event, target) => this.moveStep(target.dataset.up, -1));
    this.on('click', '[data-down]', (event, target) => this.moveStep(target.dataset.down, 1));
    this.on('click', '[data-action=cancel]', () => this.navigate(this.recipeId ? `#/recipes/${this.recipeId}` : '#/recipes'));
    this.on('submit', 'form', (event) => {
      event.preventDefault();
      this.save();
    });
  }

  connectedCallback() {
    if (this.started) return;
    this.started = true;
    this.recipeId = this.getAttribute('recipe') || null;
    this.load();
  }

  async load() {
    if (!this.recipeId) {
      this.original = null;
      this.draft = {
        name: '', subtitle: '', mainImage: '', meal: '', diet: '', servings: 4, hours: 0, minutes: 30,
        ingredients: [{ key: nextKey(), name: '', value: '', unit: 'GRAM' }],
        steps: [{ key: nextKey(), description: '', illustration: '' }],
      };
      this.show();
      return;
    }
    this.render(html`<div class="card"><p class="loading">Fetching the recipe…</p></div>`);
    try {
      const recipe = await loadRecipe(this.recipeId, { fresh: true });
      if (recipe.owner !== this.cookId) {
        this.render(html`<div class="card stack"><div class="notice">Only the cook who wrote “${recipe.name}” can change it.</div>
          <div><button class="btn" data-action="cancel">Back to the recipe</button></div></div>`);
        return;
      }
      this.original = structuredClone(recipe);
      const time = splitTime(recipe.preparationTime);
      this.draft = {
        name: recipe.name, subtitle: recipe.subtitle || '', mainImage: recipe.mainImage || '',
        meal: recipe.meal, diet: recipe.diet, servings: recipe.servings, hours: time.hours, minutes: time.minutes,
        ingredients: (recipe.ingredients || []).map((ingredient) => ({
          key: nextKey(), id: ingredient.ingredientId, name: ingredient.name, value: ingredient.value, unit: ingredient.unit,
        })),
        steps: [...(recipe.howToSteps || [])].sort((a, b) => a.sequenceNumber - b.sequenceNumber).map((step) => ({
          key: nextKey(), id: step.howToStepId, description: step.description, illustration: step.illustration || '',
        })),
      };
      this.show();
    } catch (error) {
      this.render(html`<div class="card stack"><div class="error" role="alert">${errorMessage(error)}</div>
        <div><button class="btn" data-action="cancel">Back</button></div></div>`);
    }
  }

  // ---------------------------------------------------------------- form state

  /** Reads every input back into the draft, so re-rendering (add, remove, move rows) keeps what was typed. */
  sync() {
    const form = this.$('form');
    if (!form) return;
    const value = (name) => form.elements[name]?.value ?? '';
    Object.assign(this.draft, {
      name: value('name'), subtitle: value('subtitle'), mainImage: value('mainImage'), meal: value('meal'),
      diet: value('diet'), servings: value('servings'), hours: value('hours'), minutes: value('minutes'),
    });
    for (const row of this.draft.ingredients) {
      Object.assign(row, {
        name: value(`ingredient-name-${row.key}`), value: value(`ingredient-value-${row.key}`), unit: value(`ingredient-unit-${row.key}`),
      });
    }
    for (const row of this.draft.steps) {
      Object.assign(row, { description: value(`step-description-${row.key}`), illustration: value(`step-illustration-${row.key}`) });
    }
  }

  addIngredient() {
    this.sync();
    const row = { key: nextKey(), name: '', value: '', unit: 'GRAM' };
    this.draft.ingredients.push(row);
    this.show();
    this.$(`[name="ingredient-name-${row.key}"]`)?.focus();
  }

  addStep() {
    this.sync();
    const row = { key: nextKey(), description: '', illustration: '' };
    this.draft.steps.push(row);
    this.show();
    this.$(`[name="step-description-${row.key}"]`)?.focus();
  }

  removeRow(list, key) {
    this.sync();
    if (this.draft[list].length <= 1) return;
    this.draft[list] = this.draft[list].filter((row) => row.key !== key);
    this.show();
  }

  moveStep(key, direction) {
    this.sync();
    const steps = this.draft.steps;
    const index = steps.findIndex((row) => row.key === key);
    const target = index + direction;
    if (index < 0 || target < 0 || target >= steps.length) return;
    [steps[index], steps[target]] = [steps[target], steps[index]];
    this.show();
    this.$(`[data-${direction < 0 ? 'up' : 'down'}="${key}"]:not(:disabled)`)?.focus();
  }

  // ---------------------------------------------------------------- rendering

  show() {
    const d = this.draft;
    const editing = Boolean(this.original);
    const onlyOne = (list) => d[list].length <= 1;
    const selectOptions = (values, current, label = fmt.label) => values.map((value) =>
      html`<option value="${value}" ${value === current ? 'selected' : ''}>${label(value)}</option>`);
    const keepHint = (had) => (had ? html`<span class="hint">Can be changed, but not removed.</span>` : '');

    this.render(html`
      <form class="stack" autocomplete="off">
        <div><h1>${editing ? `Edit “${this.original.name}”` : 'New recipe'}</h1>
          <p class="muted">${editing ? 'Changes are saved to the catalog for every cook.' : 'Share a recipe with the Larder community.'}</p></div>

        <section class="card">
          <div class="section-head"><h2>About the dish</h2></div>
          <div class="fields">
            <label class="wide">Name
              <input name="name" required maxlength="200" value="${d.name}" placeholder="e.g. Scones for Sunday"></label>
            <label class="wide">Subtitle <span class="hint">optional</span>
              <input name="subtitle" maxlength="300" value="${d.subtitle}" placeholder="e.g. Easy to prepare on Saturday">
              ${keepHint(this.original?.subtitle)}</label>
            <label>Meal
              <select name="meal" required>
                <option value="" ${d.meal ? '' : 'selected'} disabled>Choose a meal</option>
                ${selectOptions(MEALS, d.meal)}</select></label>
            <label>Diet
              <select name="diet" required>
                <option value="" ${d.diet ? '' : 'selected'} disabled>Choose a diet</option>
                ${selectOptions(DIETS, d.diet)}</select></label>
            <label>Servings
              <input name="servings" type="number" inputmode="numeric" required min="1" max="999" step="1" value="${d.servings}"></label>
            <fieldset>
              <legend>Preparation time</legend>
              <div class="time">
                <input name="hours" type="number" inputmode="numeric" required min="0" max="99" step="1" value="${d.hours}" aria-label="Hours">
                <span>h</span>
                <input name="minutes" type="number" inputmode="numeric" required min="0" max="59" step="1" value="${d.minutes}" aria-label="Minutes">
                <span>min</span>
              </div>
            </fieldset>
            <label class="wide">Picture link <span class="hint">optional</span>
              <input name="mainImage" type="url" value="${d.mainImage}" placeholder="https://…">
              ${keepHint(this.original?.mainImage)}</label>
          </div>
        </section>

        <section class="card">
          <div class="section-head"><h2>Ingredients</h2><span class="hint">For the servings above</span></div>
          <div class="rows">
            ${d.ingredients.map((row, index) => html`
              <div class="ingredient">
                <label class="name">Ingredient ${index + 1}
                  <input name="ingredient-name-${row.key}" required maxlength="100" value="${row.name}" placeholder="e.g. Flour"></label>
                <label>Amount
                  <input name="ingredient-value-${row.key}" type="number" inputmode="decimal" required min="0.001" step="any"
                    value="${row.value}" placeholder="250"></label>
                <label>Unit
                  <select name="ingredient-unit-${row.key}" required>
                    ${selectOptions(Object.keys(UNITS), row.unit, (unit) => UNIT_NAMES[unit])}</select></label>
                <button class="btn icon" type="button" data-remove-ingredient="${row.key}" ${onlyOne('ingredients') ? 'disabled' : ''}
                  title="${onlyOne('ingredients') ? 'A recipe needs at least one ingredient' : 'Remove ingredient'}"
                  aria-label="Remove ingredient ${index + 1}">✕</button>
              </div>`)}
          </div>
          <p><button class="btn" type="button" data-action="add-ingredient">+ Add ingredient</button></p>
        </section>

        <section class="card">
          <div class="section-head"><h2>Steps</h2><span class="hint">In the order you cook them</span></div>
          <ol class="rows steps-list">
            ${d.steps.map((row, index) => html`
              <li class="step">
                <span class="number" aria-hidden="true">${index + 1}</span>
                <div class="content">
                  <label>Step ${index + 1}
                    <textarea name="step-description-${row.key}" required maxlength="2000"
                      placeholder="e.g. Carefully mix the water with the flour">${row.description}</textarea></label>
                  <label>Picture link <span class="hint">optional</span>
                    <input name="step-illustration-${row.key}" type="url" value="${row.illustration}" placeholder="https://…">
                    ${keepHint(row.id && this.originalStep(row.id)?.illustration)}</label>
                  <div class="tools">
                    <button class="btn icon" type="button" data-up="${row.key}" ${index === 0 ? 'disabled' : ''}
                      aria-label="Move step ${index + 1} up">↑</button>
                    <button class="btn icon" type="button" data-down="${row.key}" ${index === d.steps.length - 1 ? 'disabled' : ''}
                      aria-label="Move step ${index + 1} down">↓</button>
                    <button class="btn icon" type="button" data-remove-step="${row.key}" ${onlyOne('steps') ? 'disabled' : ''}
                      title="${onlyOne('steps') ? 'A recipe needs at least one step' : 'Remove step'}"
                      aria-label="Remove step ${index + 1}">✕</button>
                  </div>
                </div>
              </li>`)}
          </ol>
          <p><button class="btn" type="button" data-action="add-step">+ Add step</button></p>
        </section>

        <div class="savebar">
          <div id="form-error" aria-live="assertive"></div>
          <div class="actions">
            <button class="btn" type="button" data-action="cancel">Cancel</button>
            <button class="btn btn-primary" type="submit" data-role="save">${editing ? 'Save changes' : 'Create recipe'}</button>
          </div>
        </div>
      </form>`);
  }

  originalStep(id) {
    return this.original?.howToSteps?.find((step) => step.howToStepId === id);
  }

  // ---------------------------------------------------------------- saving

  setSaving(saving, label) {
    const button = this.$('[data-role=save]');
    if (!button) return;
    button.disabled = saving;
    button.textContent = saving ? label : (this.original ? 'Save changes' : 'Create recipe');
    this.shadowRoot.querySelectorAll('[data-action=cancel], [data-action^=add], [data-remove-ingredient], [data-remove-step]')
      .forEach((element) => {
        if (saving) element.dataset.wasDisabled = element.disabled;
        element.disabled = saving || element.dataset.wasDisabled === 'true';
      });
  }

  /** The draft as values of the contract (trimmed; numbers as numbers). */
  values() {
    const d = this.draft;
    return {
      name: d.name.trim(),
      subtitle: d.subtitle.trim(),
      mainImage: d.mainImage.trim(),
      meal: d.meal,
      diet: d.diet,
      servings: Number(d.servings),
      preparationTime: `${pad(d.hours)}:${pad(d.minutes)}`,
      ingredients: d.ingredients.map((row) => ({ row, name: row.name.trim(), value: Number(row.value), unit: row.unit })),
      steps: d.steps.map((row, index) => ({
        row, sequenceNumber: index + 1, description: row.description.trim(), illustration: row.illustration.trim(),
      })),
    };
  }

  async save() {
    this.sync();
    fill(this.$('#form-error'), html``);
    const values = this.values();
    this.setSaving(true, this.original ? 'Saving…' : 'Creating…');
    this.changedSomething = false;
    try {
      const recipeId = this.original ? await this.saveChanges(values) : await this.create(values);
      forget(recipeId);
      this.navigate(`#/recipes/${recipeId}`);
    } catch (error) {
      this.setSaving(false);
      const partly = this.changedSomething
        ? html`<p class="small">Some of your changes are already saved; saving again sends only the rest.</p>` : '';
      fill(this.$('#form-error'), error instanceof FormProblem
        ? html`<div class="error" role="alert">${error.message}</div>`
        : html`${formError(error)}${partly}`);
      this.$('#form-error').scrollIntoView?.({ block: 'nearest', behavior: 'smooth' });
    }
  }

  /** POST /recipes with everything; the new id is the last segment of the Location header. */
  async create(values) {
    const body = {
      name: values.name,
      preparationTime: values.preparationTime,
      servings: values.servings,
      meal: values.meal,
      diet: values.diet,
      ingredients: values.ingredients.map(({ name, value, unit }) => ({ name, value, unit })),
      howToSteps: values.steps.map(({ sequenceNumber, description, illustration }) => (
        illustration ? { sequenceNumber, description, illustration } : { sequenceNumber, description })),
    };
    if (values.subtitle) body.subtitle = values.subtitle;
    if (values.mainImage) body.mainImage = values.mainImage;
    const response = await this.api(CONTEXT, '/recipes', { method: 'POST', body });
    const recipeId = idFrom(response.location) || idFrom(response.body?.recipeLink);
    if (!recipeId) throw new Error('The recipe was created, but Larder did not say where. Please look for it in the catalog.');
    return recipeId;
  }

  /** Sends only what changed, resource by resource; `this.original` follows every successful call. */
  async saveChanges(values) {
    const original = this.original;
    const base = `/recipes/${encodeURIComponent(original.recipeId)}`;
    const call = async (path, method, body) => {
      const response = await this.api(CONTEXT, `${base}${path}`, { method, body });
      this.changedSomething = true;
      return response;
    };

    // What PATCH cannot do (contracts/CHANGES.md): remove subtitle, main image or an illustration.
    if (!values.subtitle && original.subtitle) throw new FormProblem('The subtitle can be changed, but not removed.');
    if (!values.mainImage && original.mainImage) throw new FormProblem('The picture link can be changed, but not removed.');
    for (const step of values.steps) {
      if (step.row.id && !step.illustration && this.originalStep(step.row.id)?.illustration) {
        throw new FormProblem(`The picture of step ${step.sequenceNumber} can be changed, but not removed.`);
      }
    }

    // 1. the recipe's own details
    const details = {};
    if (values.name !== original.name) details.name = values.name;
    if (values.subtitle && values.subtitle !== (original.subtitle || '')) details.subtitle = values.subtitle;
    if (values.mainImage && values.mainImage !== (original.mainImage || '')) details.mainImage = values.mainImage;
    if (values.preparationTime !== original.preparationTime) details.preparationTime = values.preparationTime;
    if (values.servings !== original.servings) details.servings = values.servings;
    if (values.diet !== original.diet) details.diet = values.diet;
    if (Object.keys(details).length) {
      await call('', 'PATCH', details);
      Object.assign(original, details);
    }

    // 2. the meal
    if (values.meal !== original.meal) {
      await call('/meals', 'PUT', { meal: values.meal });
      original.meal = values.meal;
    }

    // 3. ingredients: add, change, then remove (a recipe keeps at least one)
    const ingredients = original.ingredients;
    for (const ingredient of values.ingredients.filter(({ row }) => !row.id)) {
      const { name, value, unit } = ingredient;
      const response = await call('/ingredients', 'POST', { name, value, unit });
      ingredient.row.id = idFrom(response.location) || idFrom(response.body?.ingredientLink);
      ingredients.push({ ingredientId: ingredient.row.id, name, value, unit });
    }
    for (const ingredient of values.ingredients.filter(({ row }) => row.id)) {
      const before = ingredients.find((candidate) => candidate.ingredientId === ingredient.row.id);
      if (!before) continue;
      const change = {};
      if (ingredient.name !== before.name) change.name = ingredient.name;
      if (ingredient.value !== Number(before.value)) change.value = ingredient.value;
      if (ingredient.unit !== before.unit) change.unit = ingredient.unit;
      if (Object.keys(change).length) {
        await call(`/ingredients/${encodeURIComponent(before.ingredientId)}`, 'PATCH', change);
        Object.assign(before, change);
      }
    }
    const keptIngredients = new Set(values.ingredients.map(({ row }) => row.id));
    for (const gone of ingredients.filter((ingredient) => !keptIngredients.has(ingredient.ingredientId))) {
      await call(`/ingredients/${encodeURIComponent(gone.ingredientId)}`, 'DELETE');
      ingredients.splice(ingredients.indexOf(gone), 1);
    }

    // 4. steps: free the numbers that are needed, add new steps, change, then remove
    const steps = original.howToSteps;
    const wanted = new Map(values.steps.filter(({ row }) => row.id).map((step) => [step.row.id, step]));
    let free = Math.max(values.steps.length, ...steps.map((step) => step.sequenceNumber)) + 1;
    for (const step of steps) {
      const target = wanted.get(step.howToStepId);
      const blocks = target ? step.sequenceNumber !== target.sequenceNumber : step.sequenceNumber <= values.steps.length;
      if (blocks) {
        const sequenceNumber = free++;
        await call(`/how-to-steps/${encodeURIComponent(step.howToStepId)}`, 'PATCH', { sequenceNumber });
        step.sequenceNumber = sequenceNumber;
      }
    }
    for (const step of values.steps.filter(({ row }) => !row.id)) {
      const body = { sequenceNumber: step.sequenceNumber, description: step.description };
      if (step.illustration) body.illustration = step.illustration;
      const response = await call('/how-to-steps', 'POST', body);
      step.row.id = idFrom(response.location) || idFrom(response.body?.howToStepLink);
      steps.push({ howToStepId: step.row.id, ...body });
    }
    for (const step of values.steps.filter(({ row }) => row.id)) {
      const before = steps.find((candidate) => candidate.howToStepId === step.row.id);
      if (!before) continue;
      const change = {};
      if (step.sequenceNumber !== before.sequenceNumber) change.sequenceNumber = step.sequenceNumber;
      if (step.description !== before.description) change.description = step.description;
      if (step.illustration && step.illustration !== (before.illustration || '')) change.illustration = step.illustration;
      if (Object.keys(change).length) {
        await call(`/how-to-steps/${encodeURIComponent(before.howToStepId)}`, 'PATCH', change);
        Object.assign(before, change);
      }
    }
    const keptSteps = new Set(values.steps.map(({ row }) => row.id));
    for (const gone of steps.filter((step) => !keptSteps.has(step.howToStepId))) {
      await call(`/how-to-steps/${encodeURIComponent(gone.howToStepId)}`, 'DELETE');
      steps.splice(steps.indexOf(gone), 1);
    }
    return original.recipeId;
  }
}

