/**
 * The Larder AppShell (ADR0005): signs the cook in, loads the micro-UIs of the Bounded Contexts,
 * routes between them and orchestrates the flows that span several contexts - above all the
 * rescue flow: step unclear / catastrophe → picture → help request → help → thanks.
 *
 * The shell holds the process state; the micro-UIs stay independent of each other and talk to
 * the shell through `larder:*` DOM events (MICRO-UI.md). Ambient pieces such as the notification
 * bell work by choreography: they react on their own, the shell only gives them a place.
 */
import * as auth from './auth.js';
import { ApiError, LarderElement, define, html } from './kit.js';

/** Bounded Contexts that publish a micro-UI under /ui/<context>/index.js */
const CONTEXTS = ['recipe-catalog', 'meal-planning', 'meal-preparation', 'cooking-assistance', 'media',
  'sharing', 'notification', 'cook-profile', 'consent-management'];
const unavailable = new Set();
const RESCUE_KEY = 'larder.rescue';

// ---------------------------------------------------------------- runtime for the micro-UIs

async function api(context, path, { method = 'GET', body, query } = {}) {
  const url = new URL(`/${context}${path}`, location.origin);
  for (const [key, value] of Object.entries(query || {})) {
    for (const item of [].concat(value)) if (item !== undefined && item !== null && item !== '') url.searchParams.append(key, item);
  }
  const headers = { version: '1.0.0', Authorization: `Bearer ${await auth.accessToken()}` };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const response = await fetch(url, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  const text = await response.text();
  const parsed = text ? JSON.parse(text) : undefined;
  if (response.status === 401) {
    await auth.login();
  }
  if (!response.ok) throw new ApiError(response.status, parsed);
  return { status: response.status, body: parsed, location: response.headers.get('Location') };
}

/** Maps links of the contracts (e.g. a notification's link to a help) to shell routes. */
function routeFor(href) {
  if (!href) return null;
  if (href.startsWith('#')) return href;
  const path = new URL(href, location.origin).pathname;
  const patterns = [
    [/^\/cooking-assistance\/helps\/([\w-]+)$/, (id) => `#/helps/${id}`],
    [/^\/cooking-assistance\/help-requests\/([\w-]+)$/, (id) => `#/help/${id}`],
    [/^\/recipe-catalog\/recipes\/([\w-]+)$/, (id) => `#/recipes/${id}`],
    [/^\/meal-planning\/meal-plans\/([\w-]+)$/, (id) => `#/meal-plans/${id}`],
    [/^\/sharing\/thanks(\/[\w-]+)?$/, () => '#/thanks'],
  ];
  for (const [pattern, to] of patterns) {
    const match = path.match(pattern);
    if (match) return to(match[1]);
  }
  return null;
}

// ---------------------------------------------------------------- routes

/** Creates a micro-UI element, or a friendly placeholder if its context's UI is unavailable. */
function mount(tag, attributes = {}) {
  const context = CONTEXTS.find((c) => tag.startsWith(`larder-${c}-`));
  if (!customElements.get(tag)) {
    const placeholder = document.createElement('div');
    placeholder.className = 'unavailable card';
    placeholder.textContent = unavailable.has(context)
      ? `This part of Larder (${context}) is not available right now.`
      : `Unknown view ${tag}.`;
    return placeholder;
  }
  const element = document.createElement(tag);
  for (const [name, value] of Object.entries(attributes)) {
    if (value !== undefined && value !== null) element.setAttribute(name, value);
  }
  return element;
}

const ROUTES = [
  [/^#\/recipes\/new$/, () => [mount('larder-recipe-catalog-editor')]],
  [/^#\/recipes\/([\w-]+)\/edit$/, (id) => [mount('larder-recipe-catalog-editor', { recipe: id })]],
  [/^#\/recipes\/([\w-]+)$/, (id) => [mount('larder-recipe-catalog-recipe', { recipe: id })]],
  [/^#\/recipes$/, () => [mount('larder-recipe-catalog-search')]],
  [/^#\/meal-plans\/([\w-]+)$/, (id) => [mount('larder-meal-planning-plan', { plan: id })]],
  [/^#\/meal-plans$/, () => [mount('larder-meal-planning-plans')]],
  [/^#\/cook\/([\w-]+)$/, (id) => cookingPage(id)],
  [/^#\/rescue$/, () => [document.createElement('larder-rescue-flow')]],
  [/^#\/help\/([\w-]+)$/, (id) => [mount('larder-cooking-assistance-request', { 'help-request': id })]],
  [/^#\/helps\/([\w-]+)$/, (id) => [mount('larder-cooking-assistance-help', { help: id })]],
  [/^#\/help$/, () => [mount('larder-cooking-assistance-board')]],
  [/^#\/thanks$/, () => [mount('larder-sharing-feed')]],
  [/^#\/notifications$/, () => [mount('larder-notification-list')]],
  [/^#\/profile$/, () => [mount('larder-cook-profile-profile'), mount('larder-consent-management-consents')]],
];

/** Cooking mode is a composition of two contexts: Meal Preparation drives, Recipe Catalog explains the step. */
function cookingPage(preparationId) {
  const layout = document.createElement('div');
  layout.className = 'cooking';
  layout.append(
    mount('larder-meal-preparation-cooking', { preparation: preparationId }),
    mount('larder-recipe-catalog-step'),
  );
  return [layout];
}

// ---------------------------------------------------------------- the rescue flow (orchestration)

const RESCUE_STEPS = [
  ['picture', 'Take a picture'],
  ['request', 'Ask for help'],
  ['waiting', 'Help arrives'],
  ['thanks', 'Say thanks'],
];

const rescue = {
  state: JSON.parse(sessionStorage.getItem(RESCUE_KEY) || 'null'),
  picture: null, // the larder-media-capture element holding the not yet uploaded picture (memory only)
  save() {
    if (this.state) sessionStorage.setItem(RESCUE_KEY, JSON.stringify(this.state));
    else sessionStorage.removeItem(RESCUE_KEY);
  },
  start(detail) {
    this.state = {
      situation: detail.situation,
      preparationId: detail.preparationId,
      recipeId: detail.recipeId,
      howToStepId: detail.howToStepId,
      step: 'picture',
    };
    this.picture = null;
    this.save();
  },
  advance(changes) {
    Object.assign(this.state, changes);
    this.save();
  },
  finish() {
    const back = this.state?.preparationId ? `#/cook/${this.state.preparationId}` : '#/recipes';
    this.state = null;
    this.picture = null;
    this.save();
    return back;
  },
};

class RescueFlow extends LarderElement {
  static styles = `
    .steps { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; padding: 0; list-style: none; }
    .steps li { padding: 6px 14px; border-radius: 999px; background: var(--larder-surface-muted); color: var(--larder-text-muted);
                font-weight: 600; font-size: .9rem; }
    .steps li.current { background: var(--larder-accent); color: var(--larder-accent-text); }
    .steps li.done { color: var(--larder-herb); }
  `;

  connectedCallback() {
    this.update();
    this.addEventListener('larder:picture-taken', (event) => {
      rescue.picture = event.composedPath()[0];
      rescue.advance({ step: 'request', hasPicture: true });
      this.update();
    });
    this.addEventListener('larder:picture-skipped', () => {
      rescue.advance({ step: 'request', hasPicture: false });
      this.update();
    });
    this.addEventListener('larder:help-requested', async (event) => {
      const { helpRequestId } = event.detail;
      let imageLink;
      if (rescue.picture?.upload) {
        try {
          ({ imageLink } = await rescue.picture.upload([
            { type: 'helpRequest', url: `${location.origin}/cooking-assistance/help-requests/${helpRequestId}` },
          ]));
        } catch (error) {
          console.warn('Picture could not be uploaded, continuing without it', error);
        }
      }
      rescue.advance({ step: 'waiting', helpRequestId, imageLink });
      this.update();
    });
    this.addEventListener('larder:thank-for-help', (event) => {
      event.stopPropagation(); // handled here, not by the shell's generic thanks dialog
      rescue.advance({ step: 'thanks', ...event.detail });
      this.update();
    });
    this.addEventListener('larder:thanks-given', () => {
      rescue.advance({ step: 'done' });
      this.update();
    });
    this.on('click', '[data-action=back-to-cooking]', () => {
      location.hash = rescue.finish();
    });
  }

  update() {
    const state = rescue.state;
    if (!state) {
      this.render(html`<div class="card empty">No rescue in progress. <a href="#/recipes">Find a recipe</a></div>`);
      return;
    }
    const current = RESCUE_STEPS.findIndex(([key]) => key === state.step);
    const stepper = html`<ol class="steps">${RESCUE_STEPS.map(([key, label], index) => html`
      <li class="${index === current ? 'current' : index < current || state.step === 'done' ? 'done' : ''}">${index + 1}. ${label}</li>`)}</ol>`;
    const title = state.situation === 'CATASTROPHE' ? 'Kitchen catastrophe - let’s rescue your meal' : 'This step is unclear - let’s get you help';
    this.render(html`
      <div class="stack">
        <div class="spread"><h1>${title}</h1>
          <button class="btn" data-action="back-to-cooking">Back to cooking</button></div>
        ${stepper}
        <div id="step"></div>
      </div>`);
    const slot = this.$('#step');
    switch (state.step) {
      case 'picture':
        slot.append(mount('larder-media-capture', { purpose: 'Show your helpers what happened' }));
        break;
      case 'request':
        slot.append(mount('larder-cooking-assistance-request-form', {
          type: state.situation === 'CATASTROPHE' ? 'STEPS_TO_MITIGATE_CATASTROPHE' : 'PREPARATION_STEP_EXPLANATION',
          recipe: state.recipeId,
          'how-to-step': state.situation === 'CATASTROPHE' ? undefined : state.howToStepId,
        }));
        break;
      case 'waiting':
        slot.append(mount('larder-cooking-assistance-help-status', { 'help-request': state.helpRequestId }));
        break;
      case 'thanks':
        slot.append(mount('larder-sharing-thanks-form', {
          help: state.helpId,
          'helper-type': state.helpProviderType,
          'helper-cook': state.helpProvider,
          // no picture: the thanks show the rescued meal, not the photo of the problem the help request carries
        }));
        break;
      default: {
        const done = document.createElement('div');
        done.className = 'card stack';
        done.innerHTML = html`<h2>Meal rescued 🎉</h2><p class="muted">Your thanks are visible to the community.</p>
          <div><button class="btn btn-primary btn-big" data-action="back-to-cooking">Back to cooking</button></div>`.__html;
        slot.append(done);
      }
    }
  }
}
define('larder-rescue-flow', RescueFlow);

// ---------------------------------------------------------------- the shell

class LarderShell extends LarderElement {
  static styles = `
    header { position: sticky; top: 0; z-index: 10; background: color-mix(in srgb, var(--larder-bg) 88%, transparent);
             backdrop-filter: blur(8px); border-bottom: 1px solid var(--larder-border); }
    .bar { max-width: 1120px; margin: 0 auto; padding: 10px 16px; display: flex; gap: 16px; align-items: center; flex-wrap: wrap; }
    .brand { font-family: var(--larder-font-serif); font-size: 1.5rem; font-weight: 700; color: var(--larder-accent);
             text-decoration: none; display: flex; align-items: center; gap: 8px; }
    nav { display: flex; gap: 4px; flex-wrap: wrap; flex: 1; }
    nav a { text-decoration: none; color: var(--larder-text-muted); font-weight: 600; padding: 6px 12px; border-radius: 999px; }
    nav a.active, nav a:hover { background: var(--larder-surface-muted); color: var(--larder-text); }
    .tools { display: flex; gap: 8px; align-items: center; }
    main { max-width: 1120px; margin: 0 auto; padding: 24px 16px 64px; display: flex; flex-direction: column; gap: 24px; }
    .cooking { display: grid; gap: 24px; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); align-items: start; }
    @media (max-width: 760px) { .cooking { grid-template-columns: 1fr; } }
    .unavailable { color: var(--larder-text-muted); }
    dialog { border: none; border-radius: var(--larder-radius); padding: 0; width: min(640px, calc(100vw - 32px));
             background: var(--larder-bg); color: var(--larder-text); box-shadow: var(--larder-shadow); }
    dialog::backdrop { background: rgb(30 20 10 / 45%); }
    .dialog-head { display: flex; justify-content: space-between; align-items: center; padding: 14px 18px 0; }
    .dialog-body { padding: 14px 18px 18px; }
    .splash { min-height: 60vh; display: grid; place-items: center; }
  `;

  async connectedCallback() {
    this.render(html`<div class="splash"><p class="loading">Signing you in…</p></div>`);
    try {
      if (!(await auth.completeLogin())) {
        await auth.login();
        return;
      }
    } catch (error) {
      this.render(html`<div class="splash"><div class="card error">${error.message}
        <p><a href="/">Try again</a></p></div></div>`);
      return;
    }
    window.larder = { api, cookId: auth.claims().cookId, navigate: (href) => this.navigate(href) };
    await this.loadMicroUis();
    this.layout();
    this.listen();
    this.gate();
  }

  async loadMicroUis() {
    const results = await Promise.allSettled(CONTEXTS.map((context) => import(`/ui/${context}/index.js`)));
    results.forEach((result, index) => {
      if (result.status === 'rejected') {
        unavailable.add(CONTEXTS[index]);
        console.warn(`Micro-UI of ${CONTEXTS[index]} not available`, result.reason);
      }
    });
  }

  layout() {
    this.render(html`
      <header><div class="bar">
        <a class="brand" href="#/recipes"><img src="/app-shell/favicon.svg" alt="" width="28" height="28">Larder</a>
        <nav>
          <a href="#/recipes" data-section="recipes">Recipes</a>
          <a href="#/meal-plans" data-section="meal-plans">Meal plans</a>
          <a href="#/help" data-section="help">Help board</a>
          <a href="#/thanks" data-section="thanks">Thanks</a>
        </nav>
        <div class="tools">
          <span id="bell"></span>
          <a class="btn" href="#/profile" data-section="profile">Profile</a>
          <button class="btn" data-action="logout">Sign out</button>
        </div>
      </div></header>
      <main id="outlet"></main>
      <dialog id="dialog"><div class="dialog-head"><h2 id="dialog-title"></h2>
        <button class="btn" data-action="close-dialog" aria-label="Close">✕</button></div>
        <div class="dialog-body" id="dialog-body"></div></dialog>`);
    this.on('click', '[data-action=logout]', () => auth.logout());
    this.on('click', '[data-action=close-dialog]', () => this.closeDialog());
  }

  /** Cook Profile decides whether the cook is registered; only then the app opens. */
  gate() {
    if (!customElements.get('larder-cook-profile-gate')) {
      this.open();
      return;
    }
    this.$('#outlet').replaceChildren(mount('larder-cook-profile-gate'));
  }

  open() {
    if (this.opened) return;
    this.opened = true;
    if (customElements.get('larder-notification-bell')) this.$('#bell').replaceChildren(mount('larder-notification-bell'));
    window.addEventListener('hashchange', () => this.route());
    this.route();
  }

  route() {
    const hash = location.hash || '#/recipes';
    const outlet = this.$('#outlet');
    for (const [pattern, view] of ROUTES) {
      const match = hash.match(pattern);
      if (match) {
        outlet.replaceChildren(...view(...match.slice(1)));
        this.highlight(hash);
        window.scrollTo(0, 0);
        return;
      }
    }
    location.hash = '#/recipes';
  }

  highlight(hash) {
    const section = hash.split('/')[1];
    const aliases = { cook: 'recipes', rescue: 'recipes', helps: 'help', notifications: 'profile' };
    this.shadowRoot.querySelectorAll('[data-section]').forEach((link) => {
      link.classList.toggle('active', link.dataset.section === (aliases[section] || section));
    });
  }

  navigate(href) {
    const route = routeFor(href);
    if (route) location.hash = route;
  }

  openDialog(title, element) {
    this.$('#dialog-title').textContent = title;
    this.$('#dialog-body').replaceChildren(element);
    const dialog = this.$('#dialog');
    if (!dialog.open) dialog.showModal();
  }

  closeDialog() {
    const dialog = this.$('#dialog');
    if (dialog.open) dialog.close();
    this.$('#dialog-body').replaceChildren();
  }

  /** The orchestration: what happens when one micro-UI says something happened. */
  listen() {
    const handle = (name, handler) => this.addEventListener(name, (event) => handler(event.detail, event));

    handle('larder:cook-ready', () => this.open());
    handle('larder:logout', () => auth.logout());
    handle('larder:navigate', ({ href }) => {
      this.closeDialog();
      this.navigate(href);
    });

    // Recipe Catalog → Meal Preparation: start cooking a recipe
    handle('larder:start-cooking', ({ recipeId }) => {
      this.openDialog('Start cooking', mount('larder-meal-preparation-start', { recipe: recipeId }));
    });
    handle('larder:preparation-started', ({ preparationId }) => {
      this.closeDialog();
      location.hash = `#/cook/${preparationId}`;
    });

    // Meal Preparation → Recipe Catalog: show the text of the current step
    handle('larder:step-changed', ({ recipeId, howToStepId }) => {
      const step = this.shadowRoot.querySelector('larder-recipe-catalog-step');
      step?.setAttribute('recipe', recipeId);
      step?.setAttribute('step', howToStepId);
    });

    // Meal Preparation → rescue flow
    handle('larder:help-needed', (detail) => {
      rescue.start(detail);
      location.hash = '#/rescue';
    });

    // Cooking Assistance → Sharing, outside the rescue flow (e.g. from the help board)
    handle('larder:thank-for-help', (detail) => {
      this.openDialog('Say thanks', mount('larder-sharing-thanks-form', {
        help: detail.helpId, 'helper-type': detail.helpProviderType, 'helper-cook': detail.helpProvider,
      }));
    });
    handle('larder:thanks-given', () => {
      if (!rescue.state) {
        this.closeDialog();
        location.hash = '#/thanks';
      }
    });

    // Any context → Consent Management: ask for a missing consent, then let the asking element retry
    handle('larder:consent-required', ({ purpose }, event) => {
      const origin = event.composedPath()[0];
      const ask = mount('larder-consent-management-ask', { purpose });
      ask.addEventListener('larder:consent-given', (given) => {
        given.stopPropagation();
        this.closeDialog();
        origin.retry?.();
      });
      ask.addEventListener('larder:consent-declined', (declined) => {
        declined.stopPropagation();
        this.closeDialog();
      });
      this.openDialog('Your consent', ask);
    });

    // Meal Planning → Recipe Catalog: pick a recipe for a course
    handle('larder:pick-recipe', (detail, event) => {
      const origin = event.composedPath()[0];
      const picker = mount('larder-recipe-catalog-picker');
      picker.addEventListener('larder:recipe-picked', (picked) => {
        picked.stopPropagation();
        this.closeDialog();
        origin.recipePicked?.({ ...picked.detail, request: detail.request });
      });
      this.openDialog('Pick a recipe', picker);
    });
  }
}
define('larder-shell', LarderShell);
