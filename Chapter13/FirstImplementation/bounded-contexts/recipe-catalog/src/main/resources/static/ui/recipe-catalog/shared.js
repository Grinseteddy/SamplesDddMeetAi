/**
 * Shared pieces of the Recipe Catalog micro-UI: the contract's enums with their wording,
 * formatting, the recipe card markup, images and a small read cache.
 * Calls only the Recipe Catalog API (contracts/openapi/recipe-catalog.openapi.yaml).
 */
import { ApiError, errorMessage, fmt, html, larder } from '/app-shell/kit.js';

export const CONTEXT = 'recipe-catalog';

/** Contract enum Meal (upper case in this context). */
export const MEALS = ['BREAKFAST', 'LUNCH', 'DINNER', 'SUPPER'];
/** Contract enum Diet. */
export const DIETS = ['NORMAL', 'VEGETARIAN', 'VEGAN'];
/** Contract enum Unit → [singular/short, plural/short]. */
export const UNITS = {
  PIECE: ['piece', 'pieces'],
  CUP: ['cup', 'cups'],
  TABLE_SPOON: ['tbsp', 'tbsp'],
  TEA_SPOON: ['tsp', 'tsp'],
  FLUID_OUNCES: ['fl oz', 'fl oz'],
  PINT: ['pint', 'pints'],
  QUART: ['quart', 'quarts'],
  POUND: ['lb', 'lb'],
  KILOGRAM: ['kg', 'kg'],
  GRAM: ['g', 'g'],
  LITER: ['l', 'l'],
  MILLILITER: ['ml', 'ml'],
  PINCH: ['pinch', 'pinches'],
};
export const UNIT_NAMES = {
  PIECE: 'Piece', CUP: 'Cup', TABLE_SPOON: 'Tablespoon', TEA_SPOON: 'Teaspoon', FLUID_OUNCES: 'Fluid ounces',
  PINT: 'Pint', QUART: 'Quart', POUND: 'Pound', KILOGRAM: 'Kilogram', GRAM: 'Gram', LITER: 'Liter',
  MILLILITER: 'Milliliter', PINCH: 'Pinch',
};

/** Rule-violation codes of the Recipe Catalog (RecipeRuleViolationException) → a hint for the cook. */
const HINTS = {
  RECIPE_NEEDS_INGREDIENT: 'A recipe always keeps at least one ingredient.',
  RECIPE_NEEDS_HOW_TO_STEP: 'A recipe always keeps at least one step.',
  SEQUENCE_NUMBER_TAKEN: 'Two steps cannot share the same number.',
};

/** "02:00" → "2 h", "00:45" → "45 min", "01:30" → "1 h 30 min". */
export function duration(hhmm) {
  const match = /^(\d{2}):(\d{2})$/.exec(hhmm || '');
  if (!match) return hhmm || '';
  const hours = Number(match[1]);
  const minutes = Number(match[2]);
  if (!hours && !minutes) return '0 min';
  return [hours ? `${hours} h` : '', minutes ? `${minutes} min` : ''].filter(Boolean).join(' ');
}

/** 0.5 → "½", 1.25 → "1¼", 2 → "2", 0.333 → "0.33". */
export function amount(value) {
  const number = Number(value);
  if (!Number.isFinite(number)) return String(value ?? '');
  const whole = Math.floor(number);
  const fractions = { 0.25: '¼', 0.5: '½', 0.75: '¾' };
  const rest = Math.round((number - whole) * 100) / 100;
  if (fractions[rest]) return `${whole || ''}${fractions[rest]}`;
  return String(Math.round(number * 100) / 100);
}

export const unitLabel = (unit, value) => {
  const forms = UNITS[unit];
  if (!forms) return fmt.label(unit);
  return Number(value) > 1 ? forms[1] : forms[0];
};

export const isVeg = (diet) => diet === 'VEGETARIAN' || diet === 'VEGAN';

export const dietBadge = (diet) => html`<span class="badge ${isVeg(diet) ? 'herb' : ''}">${fmt.label(diet)}</span>`;

/** The id of a created resource: last path segment of its Location (or link). */
export const idFrom = (location) => (location ? location.split(/[?#]/)[0].replace(/\/+$/, '').split('/').pop() : undefined);

/**
 * An image of a recipe or step. Media images need a token, so they are shown through Media's
 * documented element `larder-media-image`; other web links are shown as they are.
 */
export function picture(link, alt, className = 'picture') {
  if (!link) return '';
  let url;
  try {
    url = new URL(link, location.origin);
  } catch {
    return '';
  }
  if (/^\/media\/images\/[\w-]+$/.test(url.pathname) && customElements.get('larder-media-image')) {
    return html`<larder-media-image class="${className}" link="${link}"></larder-media-image>`;
  }
  if (url.protocol !== 'https:' && url.protocol !== 'http:') return '';
  return html`<img class="${className}" src="${url.href}" alt="${alt}" loading="lazy">`;
}

/** An error near a form: the friendly sentence, a hint for known rule violations, the contract's code. */
export function formError(error) {
  if (!error) return '';
  const code = error instanceof ApiError && error.status < 500 ? error.code : null;
  return html`<div class="error" role="alert">${errorMessage(error)}
    ${HINTS[code] ? html` ${HINTS[code]}` : ''}${code ? html` <span class="code">${code}</span>` : ''}</div>`;
}

// ---------------------------------------------------------------- reading recipes

const cache = new Map(); // recipeId → { at, promise }
const FRESH_MS = 30_000;

/** GET /recipes/{recipeId}, shared by cards and the step view for a short while. */
export function loadRecipe(recipeId, { fresh = false } = {}) {
  const hit = cache.get(recipeId);
  if (!fresh && hit && Date.now() - hit.at < FRESH_MS) return hit.promise;
  const promise = larder().api(CONTEXT, `/recipes/${encodeURIComponent(recipeId)}`).then((response) => response.body);
  promise.catch(() => cache.delete(recipeId));
  cache.set(recipeId, { at: Date.now(), promise });
  return promise;
}

export const forget = (recipeId) => cache.delete(recipeId);

/** Remembers recipes of a search result, so cards opened next need no request. */
export function remember(recipes) {
  for (const recipe of recipes) cache.set(recipe.recipeId, { at: Date.now(), promise: Promise.resolve(recipe) });
}

/** GET /recipes with the contract's filters (meal, diet: enum names; ingredients: whole names, all must match). */
export async function searchRecipes({ meal, diet, ingredients }) {
  const { body } = await larder().api(CONTEXT, '/recipes', { query: { meal, diet, ingredients } });
  const recipes = Array.isArray(body) ? body : [];
  remember(recipes);
  return recipes;
}

/** "Flour, butter ,  eggs" → ['Flour', 'butter', 'eggs'] (the contract allows at most 20). */
export const ingredientNames = (text) => String(text || '').split(',').map((name) => name.trim()).filter(Boolean).slice(0, 20);

// ---------------------------------------------------------------- card markup

export const CARD_STYLES = `
  .code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: .78rem; opacity: .8; margin-left: 4px; }
  .recipe-card { display: flex; flex-direction: column; gap: 10px; padding: 0; overflow: hidden; height: 100%;
                 text-decoration: none; color: inherit; }
  .recipe-card:focus-visible { outline: 3px solid var(--larder-focus); outline-offset: 2px; }
  .recipe-card .cover { aspect-ratio: 16 / 9; background: var(--larder-surface-muted); display: grid; place-items: center;
                        overflow: hidden; }
  .recipe-card .cover img, .recipe-card .cover larder-media-image { width: 100%; height: 100%; object-fit: cover;
                        border-radius: 0; display: block; }
  .recipe-card .cover .monogram { font-family: var(--larder-font-serif); font-size: 2.6rem; color: var(--larder-accent);
                        opacity: .55; }
  .recipe-card .body { padding: 0 18px 18px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
  .recipe-card h3 { margin: 0; }
  .recipe-card .subtitle { margin: 0; color: var(--larder-text-muted); font-size: .92rem; }
  .recipe-card .facts { margin-top: auto; padding-top: 6px; }
  .recipe-card.compact { flex-direction: row; align-items: center; padding: 10px 14px; gap: 12px; }
  .recipe-card.compact .cover { display: none; }
  .recipe-card.compact .body { padding: 0; gap: 4px; }
  .recipe-card.compact h3 { font-size: 1rem; }
  .recipe-card.compact .facts { padding-top: 0; margin-top: 0; }
  .recipe-card .mark { flex: none; width: 40px; height: 40px; border-radius: 50%; display: grid; place-items: center;
                       background: var(--larder-surface-muted); color: var(--larder-accent); font-family: var(--larder-font-serif);
                       font-weight: 700; font-size: 1.15rem; }
  .time { font-size: .85rem; color: var(--larder-text-muted); font-weight: 600; }
`;

const monogram = (name) => (String(name || '?').trim().charAt(0) || '?').toUpperCase();

/** The facts line of a recipe: meal, diet, preparation time. */
export const facts = (recipe) => html`<div class="row facts">
  <span class="badge accent">${fmt.label(recipe.meal)}</span>
  ${dietBadge(recipe.diet)}
  <span class="time" title="Preparation time">${duration(recipe.preparationTime)}</span></div>`;

/**
 * The card of one recipe (used by the card element and the search results). Clickable via
 * `data-recipe` - the hosting element navigates.
 */
export function cardTemplate(recipe, { compact = false } = {}) {
  if (compact) {
    return html`<div class="card clickable recipe-card compact" role="link" tabindex="0" data-recipe="${recipe.recipeId}"
        aria-label="${recipe.name}">
      <span class="mark" aria-hidden="true">${monogram(recipe.name)}</span>
      <div class="body"><h3>${recipe.name}</h3>${facts(recipe)}</div></div>`;
  }
  return html`<div class="card clickable recipe-card" role="link" tabindex="0" data-recipe="${recipe.recipeId}"
      aria-label="${recipe.name}">
    <div class="cover">${recipe.mainImage ? picture(recipe.mainImage, '') : html`<span class="monogram" aria-hidden="true">${monogram(recipe.name)}</span>`}</div>
    <div class="body">
      <h3>${recipe.name}</h3>
      ${recipe.subtitle ? html`<p class="subtitle">${recipe.subtitle}</p>` : ''}
      ${facts(recipe)}
    </div></div>`;
}

/** Makes `[data-recipe]` cards of an element open their recipe by click and Enter/Space. */
export function openCardsOnActivate(element) {
  const open = (target) => element.navigate(`#/recipes/${target.dataset.recipe}`);
  element.on('click', '[data-recipe]', (event, target) => open(target));
  element.on('keydown', '[data-recipe]', (event, target) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      open(target);
    }
  });
}
