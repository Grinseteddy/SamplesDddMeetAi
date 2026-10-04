/**
 * Micro-UI of Meal Preparation (MICRO-UI.md): starting a preparation and the cooking mode at the stove.
 *
 * Calls only the Meal Preparation API. Its HowToStep carries only `howToStepId` and `sequenceNumber`
 * (documented contract gap) - the step text comes from Recipe Catalog's `larder-recipe-catalog-step`,
 * which the AppShell places next to the cooking mode and feeds from `larder:step-changed`.
 */
import { ApiError, LarderElement, define, errorMessage, html } from '/app-shell/kit.js';

const CONTEXT = 'meal-preparation';

/** Id of a created resource: last path segment of the Location header (or of the link in the body). */
function idFrom(location, link) {
  const href = location || link;
  if (!href) return undefined;
  return new URL(href, window.location.origin).pathname.split('/').filter(Boolean).pop();
}

/** Friendly words for the rule-violation codes of this context (MealPreparationRuleViolationException, error advice). */
const PROBLEMS = {
  UNKNOWN_RECIPE: 'This recipe cannot be found any more - maybe its owner deleted it.',
  RECIPE_WITHOUT_STEPS: 'This recipe has no how-to steps yet, so there is nothing to walk you through.',
  UPSTREAM_UNAVAILABLE: 'The recipe could not be read right now. Please try again in a moment.',
  NOT_PERMITTED: 'This cooking session belongs to another cook.',
  NOT_FOUND: 'This cooking session does not exist.',
};

function problem(error) {
  const code = error instanceof ApiError ? error.code : undefined;
  const friendly = (code && PROBLEMS[code]) || errorMessage(error);
  return html`<div class="error" role="alert">${friendly}${code ? html` <span class="code">${code}</span>` : ''}</div>`;
}

const SHARED_STYLES = `
  .code { display: inline-block; margin-left: 6px; font-size: .75rem; font-family: ui-monospace, monospace; opacity: .8; }
  .sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden;
             clip: rect(0 0 0 0); white-space: nowrap; border: 0; }
`;

// ---------------------------------------------------------------- larder-meal-preparation-start

/** Confirms and starts a meal preparation for `recipe`. → larder:preparation-started {preparationId, recipeId} */
class MealPreparationStart extends LarderElement {
  static observedAttributes = ['recipe'];

  static styles = SHARED_STYLES + `
    .intro { display: flex; gap: 14px; align-items: flex-start; }
    .intro .icon { font-size: 2.2rem; line-height: 1; }
    ul.tips { margin: 0; padding-left: 20px; color: var(--larder-text-muted); }
    ul.tips li { margin: 2px 0; }
    .actions { justify-content: flex-end; }
  `;

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      this.on('click', '[data-action=start]', () => this.start());
    }
    this.saving = false;
    this.error = null;
    this.update();
  }

  attributeChangedCallback() {
    if (this.isConnected && !this.saving) {
      this.error = null;
      this.update();
    }
  }

  update() {
    const recipe = this.getAttribute('recipe');
    if (!recipe) {
      this.render(html`<div class="empty">No recipe chosen yet - pick one to start cooking.</div>`);
      return;
    }
    const card = customElements.get('larder-recipe-catalog-card')
      ? html`<larder-recipe-catalog-card recipe="${recipe}" compact></larder-recipe-catalog-card>` : '';
    this.render(html`
      <div class="stack">
        ${card}
        <div class="intro">
          <span class="icon" aria-hidden="true">🍳</span>
          <div>
            <p><strong>Ready at the stove?</strong> Larder walks you through the recipe one step at a time.</p>
            <ul class="tips">
              <li>Big buttons - fine for floury fingers.</li>
              <li>Stuck on a step or something burning? Help is one tap away.</li>
            </ul>
          </div>
        </div>
        <div id="problem" aria-live="polite">${this.error ? problem(this.error) : ''}</div>
        <div class="row actions">
          <button class="btn btn-primary btn-big" data-action="start" ${this.saving ? 'disabled' : ''}>
            ${this.saving ? 'Getting the pans ready…' : 'Start cooking'}</button>
        </div>
      </div>`);
  }

  async start() {
    const recipeId = this.getAttribute('recipe');
    if (!recipeId || this.saving) return;
    this.saving = true;
    this.error = null;
    this.update();
    try {
      const { body, location } = await this.api(CONTEXT, '/preparations', { method: 'POST', body: { recipe: recipeId } });
      const preparationId = idFrom(location, body?.preparationLink);
      this.saving = false;
      this.emit('larder:preparation-started', { preparationId, recipeId });
    } catch (error) {
      this.saving = false;
      this.error = error;
      this.update();
    }
  }
}

// ---------------------------------------------------------------- larder-meal-preparation-cooking

/**
 * Cooking mode for `preparation`: the current step, previous/next, and the two calls for help.
 * → larder:step-changed on load and after every move, → larder:help-needed.
 */
class MealPreparationCooking extends LarderElement {
  static observedAttributes = ['preparation'];

  static styles = SHARED_STYLES + `
    .stove { display: flex; flex-direction: column; gap: 20px; }
    .step-card { text-align: center; padding: 28px 20px; position: relative; overflow: hidden; }
    .step-card::before { content: ""; position: absolute; inset: 0 0 auto 0; height: 6px; background: var(--larder-accent); }
    .eyebrow { text-transform: uppercase; letter-spacing: .12em; font-size: .8rem; font-weight: 700; color: var(--larder-text-muted); margin: 0; }
    .step-number { font-family: var(--larder-font-serif); font-size: clamp(3rem, 14vw, 5.5rem); line-height: 1.05; margin: 6px 0 4px;
                   color: var(--larder-accent); font-weight: 700; }
    .step-card .hint { color: var(--larder-text-muted); margin: 0; }
    .moves { display: grid; grid-template-columns: 1fr 1.4fr; gap: 12px; }
    .move { min-height: 76px; font-size: 1.35rem; border-radius: var(--larder-radius); display: flex; align-items: center;
            justify-content: center; gap: 10px; }
    .move .arrow { font-size: 1.6rem; line-height: 1; }
    .helps { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .help-btn { min-height: 64px; font-size: 1.1rem; border-radius: var(--larder-radius); display: flex; align-items: center;
                justify-content: center; gap: 8px; text-align: center; }
    .help-btn.unclear { border-color: var(--larder-warn); color: var(--larder-text); background: color-mix(in srgb, var(--larder-warn) 12%, var(--larder-surface)); }
    .help-btn.unclear:hover { background: color-mix(in srgb, var(--larder-warn) 22%, var(--larder-surface)); }
    .help-btn.catastrophe { border-color: var(--larder-danger); background: var(--larder-danger); color: #fff; }
    .help-btn.catastrophe:hover { background: color-mix(in srgb, var(--larder-danger) 85%, #000); }
    .help-btn .icon { font-size: 1.5rem; }
    .notice.done { background: color-mix(in srgb, var(--larder-herb) 14%, var(--larder-surface)); border: 1px solid var(--larder-herb);
                   text-align: center; padding: 16px; }
    .notice.done strong { display: block; font-family: var(--larder-font-serif); font-size: 1.35rem; color: var(--larder-herb); }
    .footer { justify-content: space-between; }
    .footer a { font-weight: 600; }
    @media (max-width: 420px) {
      .moves { grid-template-columns: 1fr 1.25fr; }
      .move { font-size: 1.15rem; padding: 12px; }
      .helps { grid-template-columns: 1fr; }
    }
  `;

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      this.on('click', '[data-action=next]', () => this.move('next'));
      this.on('click', '[data-action=previous]', () => this.move('previous'));
      this.on('click', '[data-action=unclear]', () => this.needHelp('STEP_UNCLEAR'));
      this.on('click', '[data-action=catastrophe]', () => this.needHelp('CATASTROPHE'));
      this.on('click', '[data-action=retry]', () => this.load());
      this.on('click', 'a[data-nav]', (event, link) => {
        event.preventDefault();
        this.navigate(link.getAttribute('href'));
      });
      this.onVisibility = () => {
        if (document.visibilityState === 'visible') this.keepScreenOn();
      };
    }
    this.render(html`<div id="view"></div><p class="sr-only" aria-live="polite" id="live"></p>`);
    document.addEventListener('visibilitychange', this.onVisibility);
    this.keepScreenOn();
    this.load();
  }

  disconnectedCallback() {
    document.removeEventListener('visibilitychange', this.onVisibility);
    this.wakeLock?.release?.().catch(() => {});
    this.wakeLock = null;
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (this.isConnected && oldValue !== newValue) this.load();
  }

  /** Replaces the visible view; the aria-live region stays, so screen readers announce what changes. */
  show(template) {
    this.$('#view').innerHTML = template.__html;
  }

  announce(text) {
    const live = this.$('#live');
    if (live) live.textContent = text || '';
  }

  /** Cooking with sticky hands: keep the screen awake while the cooking mode is shown (where supported). */
  async keepScreenOn() {
    try {
      if ('wakeLock' in navigator && !this.wakeLock) {
        this.wakeLock = await navigator.wakeLock.request('screen');
        this.wakeLock.addEventListener?.('release', () => { this.wakeLock = null; });
      }
    } catch {
      // not allowed or not supported - cooking works without it
    }
  }

  async load(notice) {
    const id = this.getAttribute('preparation');
    this.preparation = null;
    this.step = null;
    this.atLast = false;
    this.notice = notice || null;
    this.error = null;
    if (!id) {
      this.show(html`<div class="card empty">No cooking session chosen. <a href="#/recipes" data-nav>Find a recipe</a></div>`);
      return;
    }
    this.show(html`<div class="card step-card"><p class="loading">Warming up the stove…</p></div>`);
    try {
      const { body } = await this.api(CONTEXT, `/preparations/${encodeURIComponent(id)}`);
      if (this.getAttribute('preparation') !== id) return;
      this.preparation = body;
      this.step = body.currentStep;
      this.paint();
      this.stepChanged();
    } catch (error) {
      this.show(html`
        <div class="card stack">
          <h2>Cooking mode</h2>
          ${problem(error)}
          <div class="row">
            <button class="btn" data-action="retry">Try again</button>
            <a class="btn" href="#/recipes" data-nav>Back to recipes</a>
          </div>
        </div>`);
    }
  }

  paint() {
    const step = this.step;
    const busy = Boolean(this.moving);
    const first = step.sequenceNumber === 1;
    this.show(html`
      <section class="stove" aria-label="Cooking mode">
        <div class="card step-card">
          <p class="eyebrow">You are on</p>
          <p class="step-number">Step ${step.sequenceNumber}</p>
          <p class="hint">${this.atLast ? 'This is the last step of the recipe.' : 'Read the step, cook it, then tap Next.'}</p>
        </div>

        ${this.atLast ? html`<div class="notice done" role="status"><strong>Last step - enjoy your meal! 🍽️</strong>
          You are through the recipe. Bon appétit!</div>` : ''}
        ${this.notice ? html`<div class="notice" role="status">${this.notice}</div>` : ''}
        ${this.error ? problem(this.error) : ''}

        <div class="moves">
          <button class="btn btn-big move" data-action="previous" ${busy || first ? 'disabled' : ''}
                  aria-label="Previous step"><span class="arrow" aria-hidden="true">←</span> Back</button>
          <button class="btn btn-primary btn-big move" data-action="next" ${busy || this.atLast ? 'disabled' : ''}
                  aria-label="${this.atLast ? 'No next step - this is the last step' : 'Next step'}">
            ${this.moving === 'next' ? 'Moving on…' : this.atLast ? 'Done' : 'Next'}
            <span class="arrow" aria-hidden="true">${this.atLast ? '✓' : '→'}</span></button>
        </div>

        <div class="helps">
          <button class="btn help-btn unclear" data-action="unclear" ${busy ? 'disabled' : ''}>
            <span class="icon" aria-hidden="true">🤔</span> This step is unclear</button>
          <button class="btn help-btn catastrophe" data-action="catastrophe" ${busy ? 'disabled' : ''}>
            <span class="icon" aria-hidden="true">🔥</span> Catastrophe!</button>
        </div>

        <div class="row footer small">
          <a href="#/recipes/${this.preparation.recipe}" data-nav>Show the whole recipe</a>
          <span class="muted">Your place is saved - come back any time.</span>
        </div>
      </section>`);
  }

  stepChanged() {
    this.emit('larder:step-changed', {
      preparationId: this.preparation.preparationId,
      recipeId: this.preparation.recipe,
      howToStepId: this.step.howToStepId,
      sequenceNumber: this.step.sequenceNumber,
    });
  }

  async move(direction) {
    if (!this.step || this.moving) return;
    const id = this.preparation.preparationId;
    this.moving = direction;
    this.notice = null;
    this.error = null;
    this.announce('');
    this.paint();
    try {
      const { body } = await this.api(CONTEXT, `/preparations/${encodeURIComponent(id)}/how-to-steps/${direction}`, {
        method: 'PATCH',
        query: { stepId: this.step.howToStepId },
      });
      this.moving = null;
      this.step = body;
      this.atLast = false;
      this.paint();
      this.announce(`Step ${body.sequenceNumber}`);
      this.stepChanged();
    } catch (error) {
      this.moving = null;
      const code = error instanceof ApiError ? error.code : undefined;
      if (code === 'LAST_STEP_REACHED') {
        this.atLast = true;
        this.announce('Last step - enjoy your meal!');
      } else if (code === 'FIRST_STEP_REACHED') {
        this.notice = 'You are already at the first step.';
        this.announce(this.notice);
      } else if (code === 'STEP_NOT_CURRENT') {
        // moved on elsewhere (another tab or device): show the step the preparation is really on
        await this.load('Your cooking session moved on elsewhere - here is the step you are on now.');
        return;
      } else {
        this.error = error;
      }
      this.paint();
    }
  }

  needHelp(situation) {
    if (!this.step) return;
    this.emit('larder:help-needed', {
      situation,
      preparationId: this.preparation.preparationId,
      recipeId: this.preparation.recipe,
      howToStepId: this.step.howToStepId,
    });
  }
}

define('larder-meal-preparation-start', MealPreparationStart);
define('larder-meal-preparation-cooking', MealPreparationCooking);
