/**
 * Shared pieces of the Cooking Assistance micro-UI: the published language in friendly words,
 * the rendering of helps and their answers, error codes, and the common styles.
 */
import { ApiError, errorMessage, fmt, html } from '/app-shell/kit.js';

export const CONTEXT = 'cooking-assistance';

/** Id of a created resource: last path segment of the Location header (or of the link in the body). */
export function idFrom(location, link) {
  const href = location || link;
  if (!href) return undefined;
  return new URL(href, window.location.origin).pathname.split('/').filter(Boolean).pop();
}

/** HelpType → how a cook talks about it. */
export const TYPES = {
  STEPS_TO_MITIGATE_CATASTROPHE: {
    label: 'Catastrophe', icon: '🔥', title: 'Kitchen catastrophe!',
    ask: 'What happened? What were you cooking, what went wrong, and how much time do you have?',
  },
  PREPARATION_STEP_EXPLANATION: {
    label: 'Step explanation', icon: '🤔', title: 'This step is unclear',
    ask: 'What exactly is unclear about this step?',
  },
  INGREDIENT_SUBSTITUTE: {
    label: 'Ingredient substitute', icon: '🧺', title: 'I need a substitute',
    ask: 'Which ingredient is missing, and is there anything you cannot eat?',
  },
  MENU_PROPOSAL: {
    label: 'Menu proposal', icon: '🍽️', title: 'Help me plan a menu',
    ask: 'Who is coming, how many guests, any diets or wishes?',
  },
};

export const typeLabel = (type) => TYPES[type]?.label || fmt.label(type);
export const typeIcon = (type) => TYPES[type]?.icon || '🥄';

/** HelpProviderType → how a cook talks about it. */
export const PROVIDERS = {
  GRANDMA_AVATAR: { label: 'Grandma Avatar', icon: '👵', hint: 'answers within seconds' },
  COMMUNITY: { label: 'Community', icon: '🧑‍🍳', hint: 'fellow cooks answer when they can' },
  CHEF: { label: 'Chef', icon: '👨‍🍳', hint: 'a professional - answers only you, exclusively' },
};

export const providerLabel = (type) => PROVIDERS[type]?.label || fmt.label(type);
export const providerIcon = (type) => PROVIDERS[type]?.icon || '🥄';

/**
 * Providers that make sense for a type. Grandma answers catastrophes and step explanations only
 * (never substitutes or menus); Chef only menu proposals, and exclusively.
 */
export function providersFor(type) {
  switch (type) {
    case 'STEPS_TO_MITIGATE_CATASTROPHE':
    case 'PREPARATION_STEP_EXPLANATION':
      return ['GRANDMA_AVATAR', 'COMMUNITY'];
    case 'MENU_PROPOSAL':
      return ['COMMUNITY', 'CHEF'];
    default:
      return ['COMMUNITY'];
  }
}

/** Default choice of preferred providers (1..2, never Chef with another). */
export function defaultProvidersFor(type) {
  return providersFor(type).filter((provider) => provider !== 'CHEF');
}

/** Types the community can answer here and whether the request lets the community answer at all. */
export const canCommunityAnswer = (request) =>
  request?.status === 'OPEN' && (request.preferredProvider || []).includes('COMMUNITY');

export const UNITS = ['GRAM', 'KILOGRAM', 'MILLILITER', 'LITER', 'TEA_SPOON', 'TABLE_SPOON', 'CUP', 'PINCH', 'PIECE',
  'FLUID_OUNCES', 'PINT', 'QUART', 'POUND'];
const UNIT_SHORT = {
  GRAM: 'g', KILOGRAM: 'kg', MILLILITER: 'ml', LITER: 'l', TEA_SPOON: 'tsp', TABLE_SPOON: 'tbsp', CUP: 'cup',
  PINCH: 'pinch', PIECE: 'piece', FLUID_OUNCES: 'fl oz', PINT: 'pint', QUART: 'quart', POUND: 'lb',
};
export const unitShort = (unit) => UNIT_SHORT[unit] || fmt.label(unit);
export const MEALS = ['BREAKFAST', 'LUNCH', 'DINNER', 'SUPPER'];

/** Rule-violation codes of this context (HelpRuleViolationException, CookingAssistanceErrorAdvice). */
const PROBLEMS = {
  INVALID_HELP_REQUEST: 'Please check title, description and the chosen helpers (one or two).',
  RECIPE_REQUIRED: 'This kind of question needs a recipe.',
  HOW_TO_STEP_REQUIRED: 'A step explanation needs the step that is unclear.',
  HOW_TO_STEP_NOT_ALLOWED: 'Only a step explanation can name a step.',
  INGREDIENTS_REQUIRED: 'A substitute request needs at least one ingredient from the recipe.',
  INGREDIENTS_NOT_ALLOWED: 'Only a substitute request can name ingredients.',
  CHEF_ONLY_FOR_MENU_PROPOSAL: 'Chefs only help with menu proposals.',
  CHEF_EXCLUSIVE: 'A chef helps exclusively - please do not combine the chef with other helpers.',
  ANSWERED_WITHOUT_HELP: 'A request is answered only once somebody has helped.',
  HELP_REQUEST_ANSWERED: 'This request has already been answered.',
  HELP_REQUEST_NOT_OPEN: 'This request has already been answered, so it can no longer be withdrawn.',
  ANSWER_TYPE_MISMATCH: 'Your answer does not fit the kind of question asked.',
  ANSWER_REFERENCE_MISMATCH: 'Your answer must be about the same recipe, step and ingredients as the question.',
  INVALID_ANSWER: 'Please complete your answer.',
  PROVIDER_NOT_PREFERRED: 'The cook did not ask the community for this one.',
  OWN_CHEF_REQUEST: 'You cannot answer your own chef request.',
  INVALID_HELP_PROVIDER: 'This help cannot be given in your name.',
  UNKNOWN_HELP_REQUEST: 'This help request does not exist any more - maybe it was withdrawn.',
};

/** An error box: friendly sentence, plus the API's code and message for the curious. */
export function problem(error) {
  const code = error instanceof ApiError ? error.code : undefined;
  const friendly = code && PROBLEMS[code];
  const detail = friendly && error.message && error.message !== friendly ? error.message : '';
  return html`<div class="error" role="alert">
    ${friendly || errorMessage(error)}${code ? html` <span class="code">${code}</span>` : ''}
    ${detail ? html`<div class="small detail">${detail}</div>` : ''}</div>`;
}

// ---------------------------------------------------------------- embedded elements of other contexts

/** Recipe Catalog's documented card, or a quiet placeholder if that micro-UI is not loaded. */
export function recipeCard(recipeId) {
  if (!recipeId) return '';
  return customElements.get('larder-recipe-catalog-card')
    ? html`<larder-recipe-catalog-card recipe="${recipeId}" compact></larder-recipe-catalog-card>`
    : html`<span class="muted small">A recipe from the catalog</span>`;
}

/** Recipe Catalog's documented step element: shows the text of the step a question is about. */
export function stepView(recipeId, stepId) {
  if (!recipeId || !stepId || !customElements.get('larder-recipe-catalog-step')) return '';
  return html`<larder-recipe-catalog-step recipe="${recipeId}" step="${stepId}"></larder-recipe-catalog-step>`;
}

const isWebLink = (link) => /^https?:\/\//i.test(String(link || ''));

/** A picture of an answer: Media's documented image element (images need a token), else a plain link. */
function imageView(link) {
  if (!isWebLink(link)) return '';
  return customElements.get('larder-media-image')
    ? html`<figure class="picture"><larder-media-image link="${link}"></larder-media-image></figure>`
    : html`<a href="${link}" target="_blank" rel="noopener">Open picture</a>`;
}

// ---------------------------------------------------------------- answers and helps

/** The content of a help, rendered per answerType. */
export function answerView(answer) {
  if (!answer) return '';
  switch (answer.answerType) {
    case 'STEPS_TO_MITIGATE_CATASTROPHE':
      return html`<div class="answer rescue">
        <p class="eyebrow">Rescue plan</p>
        <p class="text">${answer.explanation}</p></div>`;
    case 'PREPARATION_STEP_EXPLANATION': {
      const images = (answer.images || []).filter(isWebLink);
      return html`<div class="answer">
        <p class="eyebrow">How to do this step</p>
        <p class="text">${answer.description}</p>
        ${images.length ? html`<div class="pictures">${images.map(imageView)}</div>` : ''}</div>`;
    }
    case 'INGREDIENT_SUBSTITUTE':
      return html`<div class="answer">
        <p class="eyebrow">Use instead</p>
        <ul class="plain substitutes">${(answer.substitute || []).map((substitute, index) => html`
          <li><span class="muted">Ingredient ${index + 1}</span> <span aria-hidden="true">→</span>
            <strong>${fmt0(substitute.substituteIngredient?.value)} ${unitShort(substitute.substituteIngredient?.unit)}
            ${substitute.substituteIngredient?.name}</strong></li>`)}</ul></div>`;
    case 'MENU_PROPOSAL': {
      const courses = [...(answer.course || [])].sort((a, b) => a.step - b.step);
      return html`<div class="answer">
        <p class="eyebrow">Proposed menu</p>
        <div class="row"><span class="badge accent">${fmt.label(answer.meal)}</span>
          <span class="badge">${answer.servings} ${answer.servings === 1 ? 'serving' : 'servings'}</span></div>
        <p class="text">${answer.note}</p>
        <ol class="plain courses">${courses.map((course) => html`
          <li><span class="course-step">Course ${course.step}</span>${recipeCard(course.meal?.recipe)}</li>`)}</ol>
        ${answer.howToServe ? html`<p class="small"><strong>How to serve:</strong> <span class="text">${answer.howToServe}</span></p>` : ''}
      </div>`;
    }
    default:
      return html`<p class="muted">This answer cannot be shown here.</p>`;
  }
}

const fmt0 = (value) => (typeof value === 'number' ? value.toLocaleString() : value ?? '');

/** Who helped, in friendly words. */
export function helperName(help, cookId) {
  if (help.helpProviderType === 'COMMUNITY') {
    return help.helpProvider && help.helpProvider === cookId ? 'You, for the community' : 'A cook from the community';
  }
  return providerLabel(help.helpProviderType);
}

/** One help as a card; `thanks` adds "Say thanks", `requestLink` a link to the answered request. */
export function helpCard(help, { cookId, thanks = false, requestLink = false, fresh = false } = {}) {
  return html`<article class="card help ${fresh ? 'fresh' : ''}">
    <header class="help-head">
      <span class="avatar" aria-hidden="true">${providerIcon(help.helpProviderType)}</span>
      <div class="who">
        <h3>${help.answerTitle}</h3>
        <p class="small muted">${helperName(help, cookId)}${help.createdAt ? html` · ${fmt.dateTime(help.createdAt)}` : ''}</p>
      </div>
      <span class="badge">${typeLabel(help.answer?.answerType)}</span>
    </header>
    ${answerView(help.answer)}
    ${thanks || requestLink ? html`<div class="row">
      ${thanks ? html`<button class="btn btn-primary" data-action="thank" data-help="${help.helpId}">
        <span aria-hidden="true">💐</span> Say thanks</button>` : ''}
      ${requestLink ? html`<a class="btn" href="#/help/${help.helpRequest}" data-nav>See the question</a>` : ''}
    </div>` : ''}
  </article>`;
}

/** Detail of the `larder:thank-for-help` event for a help (helpProvider only when there is one). */
export function thanksDetail(help) {
  const detail = { helpRequestId: help.helpRequest, helpId: help.helpId, helpProviderType: help.helpProviderType };
  if (help.helpProvider) detail.helpProvider = help.helpProvider;
  return detail;
}

/** Status badge of a help request. */
export function statusBadge(status) {
  return status === 'ANSWERED'
    ? html`<span class="badge herb">Answered</span>`
    : html`<span class="badge accent">Waiting for help</span>`;
}

export function providerChips(providers) {
  return html`<span class="chips">${(providers || []).map((provider) => html`
    <span class="chip" title="${PROVIDERS[provider]?.hint || ''}"><span aria-hidden="true">${providerIcon(provider)}</span>
      ${providerLabel(provider)}</span>`)}</span>`;
}

/** Sort helper: oldest first by createdAt (helps), newest first for requests. */
export const byCreated = (a, b) => String(a.createdAt || '').localeCompare(String(b.createdAt || ''));

export const STYLES = `
  .code { display: inline-block; margin-left: 6px; font-size: .75rem; font-family: ui-monospace, monospace; opacity: .8; }
  .error .detail { margin-top: 4px; opacity: .85; }
  .sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden;
             clip: rect(0 0 0 0); white-space: nowrap; border: 0; }
  .eyebrow { text-transform: uppercase; letter-spacing: .1em; font-size: .75rem; font-weight: 700;
             color: var(--larder-text-muted); margin: 0 0 4px; }
  .text { white-space: pre-line; overflow-wrap: anywhere; }
  .help { display: flex; flex-direction: column; gap: 14px; }
  .help.fresh { border-color: var(--larder-herb); box-shadow: 0 0 0 3px color-mix(in srgb, var(--larder-herb) 25%, transparent), var(--larder-shadow); }
  .help-head { display: flex; gap: 12px; align-items: flex-start; }
  .help-head .who { flex: 1; min-width: 0; }
  .help-head h3 { margin: 0; overflow-wrap: anywhere; }
  .help-head p { margin: 0; }
  .avatar { font-size: 1.6rem; width: 46px; height: 46px; flex: none; display: grid; place-items: center; border-radius: 50%;
            background: var(--larder-surface-muted); }
  .answer { background: var(--larder-surface-muted); border-radius: var(--larder-radius-small); padding: 14px 16px;
            border-left: 4px solid var(--larder-accent); }
  .answer.rescue { border-left-color: var(--larder-danger); }
  .answer p:last-child { margin-bottom: 0; }
  .pictures { display: grid; gap: 10px; grid-template-columns: repeat(auto-fill, minmax(min(100%, 180px), 1fr)); margin-top: 10px; }
  figure.picture { margin: 0; }
  .substitutes li { padding: 4px 0; }
  .courses { display: flex; flex-direction: column; gap: 8px; margin: 10px 0; }
  .courses li { display: flex; flex-direction: column; gap: 4px; }
  .course-step { font-weight: 700; font-size: .85rem; color: var(--larder-accent-strong); }
  .chips { display: inline-flex; flex-wrap: wrap; gap: 6px; }
  .chip { display: inline-flex; gap: 4px; align-items: center; font-size: .82rem; font-weight: 600; padding: 2px 10px;
          border-radius: 999px; border: 1px solid var(--larder-border); background: var(--larder-surface); }
  .meta { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
`;
