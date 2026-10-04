/**
 * Micro-UI of the Recipe Catalog (app-shell/MICRO-UI.md): search, recipe page, editor, cooking-mode step,
 * embeddable card and recipe picker. Calls only the Recipe Catalog API
 * (contracts/openapi/recipe-catalog.openapi.yaml) through this.api('recipe-catalog', ...).
 */
import { LarderElement, define, errorMessage, fmt, html } from '/app-shell/kit.js';
import {
  CARD_STYLES, CONTEXT, DIETS, MEALS, amount, cardTemplate, dietBadge, duration, facts, forget, formError,
  ingredientNames, loadRecipe, openCardsOnActivate, picture, searchRecipes, unitLabel,
} from './shared.js';
import { RecipeEditor } from './editor.js';

const fill = (element, template) => {
  if (element) element.innerHTML = template.__html;
};

const FILTER_STYLES = `
  .filters { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(min(100%, 170px), 1fr)); align-items: end; }
  .filters .actions { display: flex; gap: 8px; flex-wrap: wrap; }
  .hint { font-weight: 400; font-size: .8rem; color: var(--larder-text-muted); }
  .count { color: var(--larder-text-muted); font-size: .9rem; font-weight: 600; }
`;

/** The filter form shared by search and picker; values are the contract's enum names. */
const filterForm = ({ withMine = false } = {}) => html`
  <form class="filters" role="search">
    <label>Meal
      <select name="meal"><option value="">Any meal</option>
        ${MEALS.map((meal) => html`<option value="${meal}">${fmt.label(meal)}</option>`)}</select></label>
    <label>Diet
      <select name="diet"><option value="">Any diet</option>
        ${DIETS.map((diet) => html`<option value="${diet}">${fmt.label(diet)}</option>`)}</select></label>
    <label>Ingredients
      <input name="ingredients" type="search" placeholder="e.g. flour, butter" autocomplete="off">
      <span class="hint">Whole names, separated by commas - all must be in the recipe</span></label>
    <label>Name
      <input name="name" type="search" placeholder="Filter by name" autocomplete="off"></label>
    ${withMine ? html`<label class="inline"><input type="checkbox" name="mine"> Only my recipes</label>` : ''}
    <div class="actions">
      <button class="btn btn-primary" type="submit">Search</button>
      <button class="btn" type="reset">Clear</button>
    </div>
  </form>`;

const readFilters = (form) => ({
  meal: form.elements.meal.value || undefined,
  diet: form.elements.diet.value || undefined,
  ingredients: ingredientNames(form.elements.ingredients.value),
  name: form.elements.name.value.trim().toLowerCase(),
  mine: Boolean(form.elements.mine?.checked),
});

const plural = (count, one, many) => `${count} ${count === 1 ? one : many}`;

// ---------------------------------------------------------------- search

/** `larder-recipe-catalog-search`: search by meal, diet and ingredients; result cards; "New recipe". */
class RecipeSearch extends LarderElement {
  static styles = `${CARD_STYLES}${FILTER_STYLES}
    .intro p { color: var(--larder-text-muted); margin: 0; }
  `;

  constructor() {
    super();
    this.sequence = 0;
    openCardsOnActivate(this);
    this.on('click', '[data-action=new]', () => this.navigate('#/recipes/new'));
    this.on('submit', 'form', (event) => {
      event.preventDefault();
      this.search();
    });
    this.on('reset', 'form', () => setTimeout(() => this.search()));
    this.on('change', 'select', () => this.search());
    this.on('change', 'input[type=checkbox]', () => this.show());
    this.on('input', 'input[name=name]', () => this.show());
  }

  connectedCallback() {
    if (this.rendered) return;
    this.rendered = true;
    this.render(html`
      <div class="stack">
        <div class="spread intro">
          <div><h1>Recipes</h1><p>Find something to cook - by meal, diet or what is in your larder.</p></div>
          <button class="btn btn-primary" data-action="new">New recipe</button>
        </div>
        <div class="card">${filterForm({ withMine: true })}</div>
        <div id="results" aria-live="polite"></div>
      </div>`);
    this.search();
  }

  async search() {
    const filters = readFilters(this.$('form'));
    const sequence = ++this.sequence;
    fill(this.$('#results'), html`<p class="loading">Looking through the catalog…</p>`);
    try {
      const recipes = await searchRecipes(filters);
      if (sequence !== this.sequence) return;
      this.recipes = recipes;
      this.show();
    } catch (error) {
      if (sequence !== this.sequence) return;
      this.recipes = null;
      fill(this.$('#results'), html`<div class="error" role="alert">${errorMessage(error)}</div>`);
    }
  }

  /** Name and "only mine" filter the loaded result without asking the API again. */
  show() {
    if (!this.recipes) return;
    const filters = readFilters(this.$('form'));
    const shown = this.recipes
      .filter((recipe) => !filters.name || recipe.name.toLowerCase().includes(filters.name))
      .filter((recipe) => !filters.mine || recipe.owner === this.cookId);
    const filtered = filters.meal || filters.diet || filters.ingredients.length || filters.name || filters.mine;
    if (!shown.length) {
      fill(this.$('#results'), html`<div class="card empty">
        ${filtered ? 'No recipe matches - try fewer filters.' : 'The catalog is still empty. Be the first to share a recipe!'}
        <p><button class="btn btn-primary" data-action="new">New recipe</button></p></div>`);
      return;
    }
    fill(this.$('#results'), html`<div class="stack">
      <span class="count">${plural(shown.length, 'recipe', 'recipes')}</span>
      <div class="grid">${shown.map((recipe) => cardTemplate(recipe))}</div></div>`);
  }
}

// ---------------------------------------------------------------- card

/** `larder-recipe-catalog-card`: small card of one recipe; embeddable by other contexts (`recipe`, `compact`?). */
class RecipeCard extends LarderElement {
  static styles = `${CARD_STYLES}
    .placeholder { padding: 14px 18px; }
    :host([compact]) .placeholder { padding: 10px 14px; }
  `;

  static observedAttributes = ['recipe', 'compact'];

  constructor() {
    super();
    this.sequence = 0;
    openCardsOnActivate(this);
  }

  connectedCallback() {
    this.load();
  }

  attributeChangedCallback(name, before, after) {
    if (before !== after && this.isConnected) this.load();
  }

  async load() {
    const recipeId = this.getAttribute('recipe');
    const compact = this.hasAttribute('compact');
    const sequence = ++this.sequence;
    if (!recipeId) {
      this.render(html`<div class="card placeholder muted">No recipe</div>`);
      return;
    }
    if (!this.shadowRoot.firstElementChild) {
      this.render(html`<div class="card placeholder loading">Loading recipe…</div>`);
    }
    try {
      const recipe = await loadRecipe(recipeId);
      if (sequence !== this.sequence) return;
      this.render(cardTemplate(recipe, { compact }));
    } catch (error) {
      if (sequence !== this.sequence) return;
      const gone = error.status === 404;
      this.render(html`<div class="card placeholder ${gone ? 'muted' : 'error'}">
        ${gone ? 'This recipe is no longer in the catalog.' : errorMessage(error)}</div>`);
    }
  }
}

// ---------------------------------------------------------------- recipe page

/** `larder-recipe-catalog-recipe`: the full recipe; start cooking; edit and delete for its owner. */
class RecipePage extends LarderElement {
  static styles = `
    .code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: .78rem; opacity: .8; margin-left: 4px; }
    .hero { display: grid; gap: 20px; grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr); align-items: center; }
    .hero.no-image { grid-template-columns: 1fr; }
    .hero .cover img, .hero .cover larder-media-image { width: 100%; aspect-ratio: 4 / 3; object-fit: cover; display: block;
          border-radius: var(--larder-radius); }
    .subtitle { font-size: 1.1rem; color: var(--larder-text-muted); font-family: var(--larder-font-serif); font-style: italic; }
    .facts { gap: 10px; }
    .meta { display: flex; gap: 18px; flex-wrap: wrap; margin: 6px 0 14px; color: var(--larder-text-muted); font-size: .92rem; }
    .meta strong { color: var(--larder-text); display: block; font-size: 1.05rem; }
    .layout { display: grid; gap: 24px; grid-template-columns: minmax(0, 1fr) minmax(0, 1.7fr); align-items: start; }
    @media (max-width: 760px) { .hero, .layout { grid-template-columns: 1fr; } }
    .ingredients li { display: flex; gap: 10px; padding: 9px 0; border-bottom: 1px dashed var(--larder-border); }
    .ingredients li:last-child { border-bottom: none; }
    .ingredients .qty { min-width: 84px; font-weight: 700; color: var(--larder-accent-strong); }
    ol.steps { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 18px; }
    ol.steps li { display: grid; grid-template-columns: 40px minmax(0, 1fr); gap: 14px; }
    .number { width: 36px; height: 36px; border-radius: 50%; background: var(--larder-accent); color: var(--larder-accent-text);
              display: grid; place-items: center; font-weight: 700; font-family: var(--larder-font-serif); }
    .step-text { white-space: pre-line; margin: 6px 0 8px; }
    .step-image img, .step-image larder-media-image { max-width: min(100%, 420px); border-radius: var(--larder-radius-small); display: block; }
    .gallery { display: grid; gap: 10px; grid-template-columns: repeat(auto-fill, minmax(min(100%, 140px), 1fr)); }
    .gallery img, .gallery larder-media-image { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: var(--larder-radius-small); display: block; }
    .owner-tools { display: flex; gap: 8px; flex-wrap: wrap; }
    .confirm { border: 1px solid var(--larder-danger); }
    .cta { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
  `;

  static observedAttributes = ['recipe'];

  constructor() {
    super();
    this.sequence = 0;
    this.on('click', '[data-action=cook]', () => this.emit('larder:start-cooking', { recipeId: this.recipe.recipeId }));
    this.on('click', '[data-action=edit]', () => this.navigate(`#/recipes/${this.recipe.recipeId}/edit`));
    this.on('click', '[data-action=ask-delete]', () => this.update({ confirming: true }));
    this.on('click', '[data-action=keep]', () => this.update({ confirming: false, error: null }));
    this.on('click', '[data-action=delete]', () => this.deleteRecipe());
    this.on('click', '[data-action=back]', () => this.navigate('#/recipes'));
  }

  connectedCallback() {
    this.load();
  }

  attributeChangedCallback(name, before, after) {
    if (before !== after && this.isConnected) this.load();
  }

  async load() {
    const recipeId = this.getAttribute('recipe');
    const sequence = ++this.sequence;
    this.recipe = null;
    this.state = { confirming: false, deleting: false, error: null };
    if (!recipeId) {
      this.render(html`<div class="card empty">No recipe chosen.</div>`);
      return;
    }
    this.render(html`<div class="card"><p class="loading">Fetching the recipe…</p></div>`);
    try {
      const recipe = await loadRecipe(recipeId, { fresh: true });
      if (sequence !== this.sequence) return;
      this.recipe = recipe;
      this.update();
    } catch (error) {
      if (sequence !== this.sequence) return;
      this.render(html`<div class="card stack"><div class="error" role="alert">${errorMessage(error)}</div>
        <div><button class="btn" data-action="back">Back to all recipes</button></div></div>`);
    }
  }

  update(changes = {}) {
    Object.assign(this.state, changes);
    const recipe = this.recipe;
    const { confirming, deleting, error } = this.state;
    const own = recipe.owner === this.cookId;
    const ingredients = recipe.ingredients || [];
    const steps = [...(recipe.howToSteps || [])].sort((a, b) => a.sequenceNumber - b.sequenceNumber);
    const furtherImages = recipe.furtherImages || [];

    this.render(html`
      <article class="stack">
        <section class="card hero ${recipe.mainImage ? '' : 'no-image'}">
          ${recipe.mainImage ? html`<div class="cover">${picture(recipe.mainImage, recipe.name)}</div>` : ''}
          <div class="stack">
            <div class="row facts"><span class="badge accent">${fmt.label(recipe.meal)}</span>${dietBadge(recipe.diet)}
              ${own ? html`<span class="badge">Your recipe</span>` : ''}</div>
            <div><h1>${recipe.name}</h1>
              ${recipe.subtitle ? html`<p class="subtitle">${recipe.subtitle}</p>` : ''}</div>
            <div class="meta">
              <span><strong>${duration(recipe.preparationTime)}</strong>preparation</span>
              <span><strong>${recipe.servings}</strong>${recipe.servings === 1 ? 'serving' : 'servings'}</span>
              <span><strong>${steps.length}</strong>${steps.length === 1 ? 'step' : 'steps'}</span>
            </div>
            <div class="cta">
              <button class="btn btn-primary btn-big" data-action="cook">Start cooking</button>
              ${own ? html`<div class="owner-tools">
                <button class="btn" data-action="edit">Edit</button>
                <button class="btn btn-danger" data-action="ask-delete" ${confirming ? 'disabled' : ''}>Delete</button></div>` : ''}
            </div>
          </div>
        </section>

        ${own && confirming ? html`<section class="card confirm stack" role="alertdialog" aria-labelledby="confirm-title">
          <h3 id="confirm-title">Delete “${recipe.name}” for good?</h3>
          <p class="muted">The recipe disappears from the catalog with all its ingredients and steps. This cannot be undone.</p>
          ${formError(error)}
          <div class="row">
            <button class="btn btn-danger" data-action="delete" ${deleting ? 'disabled' : ''}>${deleting ? 'Deleting…' : 'Yes, delete it'}</button>
            <button class="btn" data-action="keep" ${deleting ? 'disabled' : ''}>Keep it</button>
          </div></section>` : ''}

        <div class="layout">
          <section class="card">
            <h2>Ingredients</h2>
            <p class="muted small">For ${plural(recipe.servings, 'serving', 'servings')}</p>
            <ul class="plain ingredients">
              ${ingredients.map((ingredient) => html`<li>
                <span class="qty">${amount(ingredient.value)} ${unitLabel(ingredient.unit, ingredient.value)}</span>
                <span>${ingredient.name}</span></li>`)}
            </ul>
          </section>
          <section class="card">
            <h2>How to make it</h2>
            <ol class="steps">
              ${steps.map((step) => html`<li>
                <span class="number" aria-label="Step ${step.sequenceNumber}">${step.sequenceNumber}</span>
                <div><p class="step-text">${step.description}</p>
                  ${step.illustration ? html`<div class="step-image">${picture(step.illustration, `Step ${step.sequenceNumber}`)}</div>` : ''}</div>
              </li>`)}
            </ol>
          </section>
        </div>

        ${furtherImages.length ? html`<section class="card"><h2>More pictures</h2>
          <div class="gallery">${furtherImages.map((link) => picture(link, recipe.name))}</div></section>` : ''}
      </article>`);
  }

  async deleteRecipe() {
    this.update({ deleting: true, error: null });
    try {
      await this.api(CONTEXT, `/recipes/${encodeURIComponent(this.recipe.recipeId)}`, { method: 'DELETE' });
      forget(this.recipe.recipeId);
      this.navigate('#/recipes');
    } catch (error) {
      this.update({ deleting: false, error });
    }
  }
}

// ---------------------------------------------------------------- step (cooking mode)

/** `larder-recipe-catalog-step`: one how-to step for cooking mode; empty until `recipe` and `step` are set. */
class RecipeStep extends LarderElement {
  static styles = `
    .step { display: flex; flex-direction: column; gap: 14px; }
    .label { color: var(--larder-accent-strong); font-weight: 700; letter-spacing: .04em; text-transform: uppercase; font-size: .8rem; }
    .recipe-name { font-family: var(--larder-font-serif); color: var(--larder-text-muted); margin: 0; }
    .description { font-size: 1.35rem; line-height: 1.55; white-space: pre-line; margin: 0; }
    .illustration img, .illustration larder-media-image { width: 100%; max-height: 420px; object-fit: cover;
          border-radius: var(--larder-radius-small); display: block; }
    .progress { height: 6px; border-radius: 999px; background: var(--larder-surface-muted); overflow: hidden; }
    .progress span { display: block; height: 100%; background: var(--larder-accent); border-radius: 999px; transition: width .3s ease; }
  `;

  static observedAttributes = ['recipe', 'step'];

  constructor() {
    super();
    this.sequence = 0;
  }

  connectedCallback() {
    this.load();
  }

  attributeChangedCallback(name, before, after) {
    if (before !== after && this.isConnected) this.load();
  }

  async load() {
    const recipeId = this.getAttribute('recipe');
    const stepId = this.getAttribute('step');
    const sequence = ++this.sequence;
    if (!recipeId || !stepId) {
      this.render(html`<div class="card empty">The explanation of the current step appears here.</div>`);
      return;
    }
    if (!this.shown) this.render(html`<div class="card"><p class="loading">Reading the step…</p></div>`);
    try {
      let recipe = await loadRecipe(recipeId);
      let step = recipe.howToSteps?.find((candidate) => candidate.howToStepId === stepId);
      if (!step) {
        recipe = await loadRecipe(recipeId, { fresh: true });
        step = recipe.howToSteps?.find((candidate) => candidate.howToStepId === stepId);
      }
      if (sequence !== this.sequence) return;
      if (!step) {
        this.shown = false;
        this.render(html`<div class="card notice">This step was changed in the catalog since you started cooking.
          Follow the step number shown in cooking mode.</div>`);
        return;
      }
      const ordered = [...recipe.howToSteps].sort((a, b) => a.sequenceNumber - b.sequenceNumber);
      const position = ordered.indexOf(step) + 1;
      this.shown = true;
      this.render(html`
        <section class="card step" aria-live="polite">
          <div class="spread"><span class="label">Step ${step.sequenceNumber}</span>
            <span class="muted small">${position} of ${ordered.length}</span></div>
          <div class="progress" aria-hidden="true"><span style="width: ${Math.round((position / ordered.length) * 100)}%"></span></div>
          <p class="recipe-name">${recipe.name}</p>
          <p class="description">${step.description}</p>
          ${step.illustration ? html`<div class="illustration">${picture(step.illustration, `Step ${step.sequenceNumber}`)}</div>` : ''}
        </section>`);
    } catch (error) {
      if (sequence !== this.sequence) return;
      this.shown = false;
      this.render(html`<div class="card error" role="alert">${errorMessage(error)}</div>`);
    }
  }
}

// ---------------------------------------------------------------- picker

/** `larder-recipe-catalog-picker`: search and choose a recipe → `larder:recipe-picked {recipeId, name}`. */
class RecipePicker extends LarderElement {
  static styles = `${CARD_STYLES}${FILTER_STYLES}
    .results { display: flex; flex-direction: column; gap: 8px; max-height: min(52vh, 460px); overflow-y: auto; padding: 2px; }
    .choice { display: flex; gap: 12px; align-items: center; justify-content: space-between; padding: 12px 14px;
              border: 1px solid var(--larder-border); border-radius: var(--larder-radius-small); background: var(--larder-surface); }
    .choice:hover { background: var(--larder-surface-muted); }
    .choice h3 { font-size: 1rem; margin: 0 0 4px; }
    .choice .facts { padding: 0; margin: 0; }
    .choice .btn { flex: none; }
  `;

  constructor() {
    super();
    this.sequence = 0;
    this.recipes = [];
    this.on('submit', 'form', (event) => {
      event.preventDefault();
      this.search();
    });
    this.on('reset', 'form', () => setTimeout(() => this.search()));
    this.on('change', 'select', () => this.search());
    this.on('input', 'input[name=name]', () => this.show());
    this.on('click', '[data-pick]', (event, target) => {
      const recipe = this.recipes.find((candidate) => candidate.recipeId === target.dataset.pick);
      if (recipe) this.emit('larder:recipe-picked', { recipeId: recipe.recipeId, name: recipe.name });
    });
  }

  connectedCallback() {
    if (this.rendered) return;
    this.rendered = true;
    this.render(html`<div class="stack">
      ${filterForm()}
      <div id="results" aria-live="polite"></div></div>`);
    this.search();
  }

  async search() {
    const filters = readFilters(this.$('form'));
    const sequence = ++this.sequence;
    fill(this.$('#results'), html`<p class="loading">Looking through the catalog…</p>`);
    try {
      const recipes = await searchRecipes(filters);
      if (sequence !== this.sequence) return;
      this.recipes = recipes;
      this.show();
    } catch (error) {
      if (sequence !== this.sequence) return;
      fill(this.$('#results'), html`<div class="error" role="alert">${errorMessage(error)}</div>`);
    }
  }

  show() {
    const name = this.$('input[name=name]').value.trim().toLowerCase();
    const shown = this.recipes.filter((recipe) => !name || recipe.name.toLowerCase().includes(name));
    if (!shown.length) {
      fill(this.$('#results'), html`<div class="empty">No recipe matches - try fewer filters.</div>`);
      return;
    }
    fill(this.$('#results'), html`<span class="count">${plural(shown.length, 'recipe', 'recipes')}</span>
      <div class="results">${shown.map((recipe) => html`
        <div class="choice">
          <div><h3>${recipe.name}</h3>${facts(recipe)}</div>
          <button class="btn btn-primary" type="button" data-pick="${recipe.recipeId}"
            aria-label="Choose ${recipe.name}">Choose</button>
        </div>`)}</div>`);
  }
}

define('larder-recipe-catalog-search', RecipeSearch);
define('larder-recipe-catalog-card', RecipeCard);
define('larder-recipe-catalog-recipe', RecipePage);
define('larder-recipe-catalog-editor', RecipeEditor);
define('larder-recipe-catalog-step', RecipeStep);
define('larder-recipe-catalog-picker', RecipePicker);
