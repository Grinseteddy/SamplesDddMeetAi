/**
 * Micro-UI of Cooking Assistance (MICRO-UI.md): asking for help, waiting for it, help requests and helps,
 * and the community's help board. Calls only the Cooking Assistance API.
 */
import { ApiError, LarderElement, define, fmt, html } from '/app-shell/kit.js';
import {
  CONTEXT, MEALS, PROVIDERS, STYLES, TYPES, UNITS, byCreated, canCommunityAnswer, defaultProvidersFor, helpCard, idFrom,
  problem, providerChips, providerIcon, providerLabel, providersFor, recipeCard, statusBadge, stepView, thanksDetail,
  typeIcon, typeLabel, unitShort,
} from './parts.js';

const POLL_FAST = 1500; // while Grandma usually answers (~0.5 s)
const POLL_SLOW = 10000; // after a while, the community takes its time
const FAST_PHASE = 30000;

/** Wires `<a data-nav>` links to the shell's navigation (contract links are mapped by the shell). */
function wireNavigation(element) {
  element.on('click', 'a[data-nav]', (event, link) => {
    event.preventDefault();
    element.navigate(link.getAttribute('href'));
  });
}

/** Puts an html`` result into one slot of the shadow DOM, leaving the rest (e.g. aria-live regions) alone. */
function put(slot, template) {
  if (slot) slot.innerHTML = template.__html;
}

// ---------------------------------------------------------------- larder-cooking-assistance-request-form

/**
 * Ask for help. Attributes `type`, `recipe`?, `how-to-step`? (from the shell's rescue flow).
 * Without `type` the cook chooses the kind of question. → larder:help-requested {helpRequestId}
 */
class RequestForm extends LarderElement {
  static observedAttributes = ['type', 'recipe', 'how-to-step'];

  static styles = STYLES + `
    form { display: flex; flex-direction: column; gap: var(--larder-gap); }
    .providers { display: grid; gap: 8px; grid-template-columns: repeat(auto-fill, minmax(min(100%, 220px), 1fr)); }
    .provider { border: 1px solid var(--larder-border); border-radius: var(--larder-radius-small); padding: 10px 12px;
                cursor: pointer; background: var(--larder-surface); align-items: flex-start; }
    .provider:has(input:checked) { border-color: var(--larder-accent); background: color-mix(in srgb, var(--larder-accent) 8%, var(--larder-surface)); }
    .provider input { margin-top: 4px; width: 20px; height: 20px; accent-color: var(--larder-accent); }
    .provider .name { font-weight: 700; display: block; }
    .provider .hint { font-size: .82rem; color: var(--larder-text-muted); font-weight: 400; }
    .context { display: flex; flex-direction: column; gap: 10px; }
    .counter { align-self: flex-end; font-size: .78rem; color: var(--larder-text-muted); font-weight: 400; }
    .done { text-align: center; }
    .done .icon { font-size: 2.4rem; }
    .actions { justify-content: flex-end; }
  `;

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      wireNavigation(this);
      this.on('submit', 'form', (event) => {
        event.preventDefault();
        this.submit(event.target);
      });
      this.on('change', 'input[name=preferredProvider]', (event, input) => this.providerChanged(input));
      this.on('change', 'select[name=type]', (event, select) => {
        this.chosenType = select.value;
        this.paintProviders();
        this.paintTypeHints();
      });
      this.on('input', 'textarea[name=description]', (event, area) => {
        const counter = this.$('#counter');
        if (counter) counter.textContent = `${area.value.length} / 2000`;
      });
      this.on('click', '[data-action=view]', () => this.navigate(`#/help/${this.createdId}`));
    }
    this.saving = false;
    this.createdId = null;
    this.paint();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (this.isConnected && !this.saving && !this.createdId && oldValue !== newValue) this.paint();
  }

  get type() {
    return this.getAttribute('type') || this.chosenType || '';
  }

  paint() {
    const fixedType = this.getAttribute('type');
    const type = this.type;
    const recipe = this.getAttribute('recipe');
    const step = this.getAttribute('how-to-step');
    const meta = TYPES[type];
    this.render(html`
      <form class="card" aria-label="Ask for help">
        <div class="spread">
          <h2><span aria-hidden="true">${typeIcon(type)}</span> ${fixedType ? meta?.title || 'Ask for help' : 'Ask for help'}</h2>
          ${fixedType ? html`<span class="badge">${typeLabel(type)}</span>` : ''}
        </div>
        ${fixedType ? '' : html`<label>What do you need?
          <select name="type" required>
            <option value="" disabled ${type ? '' : 'selected'}>Choose…</option>
            ${Object.entries(TYPES).map(([value, t]) => html`
              <option value="${value}" ${value === type ? 'selected' : ''}>${t.icon} ${t.label}</option>`)}
          </select></label>`}
        ${recipe || step ? html`<div class="context">
          ${recipe ? recipeCard(recipe) : ''}
          ${type === 'PREPARATION_STEP_EXPLANATION' ? stepView(recipe, step) : ''}
        </div>` : ''}
        <label>Title
          <input name="title" required maxlength="200" autocomplete="off" value="${meta?.title || ''}">
        </label>
        <label>What is going on?
          <textarea name="description" required maxlength="2000" placeholder="${meta?.ask || 'Tell your helpers what you need.'}"></textarea>
          <span class="counter" id="counter" aria-hidden="true">0 / 2000</span>
        </label>
        <fieldset id="providers"></fieldset>
        <div id="type-hints"></div>
        <div id="problem" aria-live="polite"></div>
        <div class="row actions">
          <button class="btn btn-primary btn-big" type="submit">Ask for help</button>
        </div>
      </form>`);
    this.paintProviders();
    this.paintTypeHints();
  }

  /** Only valid choices: Grandma never answers substitutes or menus, Chef only menus and alone. */
  paintProviders() {
    const type = this.type;
    const choices = providersFor(type);
    const defaults = defaultProvidersFor(type);
    put(this.$('#providers'), html`
      <legend>Who should help? <span class="muted small">(one or two)</span></legend>
      <div class="providers">${choices.map((provider) => html`
        <label class="inline provider">
          <input type="checkbox" name="preferredProvider" value="${provider}" ${defaults.includes(provider) ? 'checked' : ''}>
          <span><span class="name"><span aria-hidden="true">${providerIcon(provider)}</span> ${providerLabel(provider)}</span>
            <span class="hint">${PROVIDERS[provider].hint}</span></span>
        </label>`)}</div>
      <p class="small muted" id="provider-hint" aria-live="polite"></p>`);
  }

  paintTypeHints() {
    const type = this.type;
    const recipe = this.getAttribute('recipe');
    const step = this.getAttribute('how-to-step');
    let hint = '';
    if (type === 'INGREDIENT_SUBSTITUTE') {
      hint = 'Substitute requests need the ingredients of a recipe, which cannot be chosen here yet. Ask from the recipe instead.';
    } else if (type === 'PREPARATION_STEP_EXPLANATION' && (!recipe || !step)) {
      hint = 'A step explanation needs a recipe and its step - start it with "This step is unclear" while cooking.';
    }
    this.blocked = Boolean(hint);
    put(this.$('#type-hints'), hint ? html`<div class="notice">${hint}</div>` : html``);
    const submit = this.$('button[type=submit]');
    if (submit) submit.disabled = this.blocked || this.saving;
  }

  providerChanged(input) {
    const boxes = [...this.shadowRoot.querySelectorAll('input[name=preferredProvider]')];
    const hint = this.$('#provider-hint');
    let message = '';
    if (input.checked && input.value === 'CHEF') {
      boxes.filter((box) => box !== input && box.checked).forEach((box) => { box.checked = false; });
      message = 'A chef helps exclusively, so the other helpers were unticked.';
    } else if (input.checked) {
      const chef = boxes.find((box) => box.value === 'CHEF' && box.checked);
      if (chef) {
        chef.checked = false;
        message = 'A chef helps only alone, so the chef was unticked.';
      }
      if (boxes.filter((box) => box.checked).length > 2) {
        input.checked = false;
        message = 'Choose at most two helpers.';
      }
    }
    if (hint) hint.textContent = message;
  }

  setSaving(saving) {
    this.saving = saving;
    const form = this.$('form');
    if (!form) return;
    for (const element of form.elements) element.disabled = saving || (element.type === 'submit' && this.blocked);
    const submit = this.$('button[type=submit]');
    if (submit) submit.textContent = saving ? 'Sending…' : 'Ask for help';
  }

  async submit(form) {
    if (this.saving || this.blocked) return;
    const type = this.type;
    const providers = [...form.querySelectorAll('input[name=preferredProvider]:checked')].map((box) => box.value);
    put(this.$('#problem'), html``);
    if (!type) {
      put(this.$('#problem'), html`<div class="error" role="alert">Please choose what you need.</div>`);
      return;
    }
    if (providers.length < 1 || providers.length > 2) {
      put(this.$('#problem'), html`<div class="error" role="alert">Please choose one or two helpers.</div>`);
      return;
    }
    const body = {
      title: form.elements.title.value.trim(),
      type,
      description: form.elements.description.value.trim(),
      preferredProvider: providers,
    };
    if (!body.title || !body.description) {
      put(this.$('#problem'), html`<div class="error" role="alert">Please fill in a title and what is going on.</div>`);
      return;
    }
    const recipe = this.getAttribute('recipe');
    const step = this.getAttribute('how-to-step');
    if (recipe) body.recipe = recipe;
    if (step && type === 'PREPARATION_STEP_EXPLANATION') body.howToStep = step;

    this.setSaving(true);
    try {
      const { body: link, location } = await this.api(CONTEXT, '/help-requests', { method: 'POST', body });
      const helpRequestId = idFrom(location, link?.helpRequestLink);
      this.saving = false;
      this.createdId = helpRequestId;
      this.render(html`
        <div class="card stack done" role="status">
          <div class="icon" aria-hidden="true">📣</div>
          <h2>Your helpers have been asked</h2>
          <p class="muted">${providers.map(providerLabel).join(' and ')} will see your question.</p>
          <div><button class="btn" data-action="view">See your request</button></div>
        </div>`);
      this.emit('larder:help-requested', { helpRequestId });
    } catch (error) {
      this.setSaving(false);
      put(this.$('#problem'), problem(error));
    }
  }
}

// ---------------------------------------------------------------- shared: list of helps with "Say thanks"

/** Base for elements that show helps: "Say thanks" for the requester, navigation links. */
class HelpsView extends LarderElement {
  wireHelps() {
    wireNavigation(this);
    this.on('click', '[data-action=thank]', (event, button) => {
      const help = (this.helps || []).find((candidate) => candidate.helpId === button.dataset.help);
      if (help) this.emit('larder:thank-for-help', thanksDetail(help));
    });
  }

  helpsList(request, helps, { fresh } = {}) {
    const mine = request?.requester === this.cookId;
    return html`<div class="stack">${helps.map((help) => helpCard(help, {
      cookId: this.cookId, thanks: mine && help.helpRequester === this.cookId, fresh: fresh === help.helpId,
    }))}</div>`;
  }

  requestSummary(request) {
    return html`
      <div class="meta">
        ${statusBadge(request.status)}
        <span class="badge">${typeIcon(request.type)} ${typeLabel(request.type)}</span>
        ${request.requester === this.cookId ? html`<span class="badge">Asked by you</span>` : ''}
        ${request.createdAt ? html`<span class="small muted">${fmt.dateTime(request.createdAt)}</span>` : ''}
      </div>`;
  }
}

// ---------------------------------------------------------------- larder-cooking-assistance-help-status

/**
 * Waits for help on `help-request`: polls until the first help arrives, then stops.
 * → larder:help-provided once, → larder:thank-for-help per "Say thanks".
 */
class HelpStatus extends HelpsView {
  static observedAttributes = ['help-request'];

  static styles = STYLES + `
    .waiting { text-align: center; padding: 28px 20px; }
    .pot { font-size: 3rem; display: inline-block; animation: simmer 1.6s ease-in-out infinite; }
    @keyframes simmer { 0%, 100% { transform: translateY(0) rotate(0); } 25% { transform: translateY(-3px) rotate(-3deg); }
                        75% { transform: translateY(-3px) rotate(3deg); } }
    @media (prefers-reduced-motion: reduce) { .pot { animation: none; } }
    .dots::after { content: "…"; }
    .arrived { display: flex; gap: 10px; align-items: center; }
    .arrived strong { font-family: var(--larder-font-serif); font-size: 1.3rem; }
  `;

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      this.wireHelps();
      this.on('click', '[data-action=refresh]', () => this.refresh());
      this.on('click', '[data-action=retry]', () => this.start());
    }
    this.start();
  }

  disconnectedCallback() {
    this.stop();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (this.isConnected && oldValue !== newValue) this.start();
  }

  stop() {
    clearTimeout(this.timer);
    this.timer = null;
    this.generation = (this.generation || 0) + 1;
  }

  start() {
    this.stop();
    this.request = null;
    this.helps = [];
    this.announced = false;
    this.signature = '';
    this.startedAt = Date.now();
    this.render(html`<div id="view"><div class="card waiting"><p class="loading">Looking up your request…</p></div></div>
      <p class="sr-only" aria-live="assertive" id="live"></p>`);
    this.poll();
  }

  async fetchState(id) {
    const [request, helps] = await Promise.all([
      this.api(CONTEXT, `/help-requests/${encodeURIComponent(id)}`),
      this.api(CONTEXT, '/helps', { query: { helpRequestId: id } }),
    ]);
    return { request: request.body, helps: [...(helps.body || [])].sort(byCreated) };
  }

  async poll() {
    const generation = this.generation;
    const id = this.getAttribute('help-request');
    if (!id) {
      put(this.$('#view'), html`<div class="card empty">No help request to wait for.</div>`);
      return;
    }
    let next = Date.now() - this.startedAt < FAST_PHASE ? POLL_FAST : POLL_SLOW;
    try {
      const { request, helps } = await this.fetchState(id);
      if (generation !== this.generation) return; // stopped or restarted meanwhile
      this.request = request;
      this.helps = helps;
      this.error = null;
      this.paint();
      if (helps.length > 0) {
        this.arrived(helps[0]);
        return; // at least one help: stop polling
      }
    } catch (error) {
      if (generation !== this.generation) return;
      this.error = error;
      this.paint();
      if (error instanceof ApiError && (error.status === 404 || error.status === 403)) return; // withdrawn / not ours
      next = POLL_SLOW;
    }
    this.timer = setTimeout(() => this.poll(), next);
  }

  arrived(first) {
    if (this.announced) return;
    this.announced = true;
    const live = this.$('#live');
    if (live) live.textContent = `Help has arrived: ${first.answerTitle}, from ${providerLabel(first.helpProviderType)}.`;
    const detail = { helpRequestId: first.helpRequest, helpId: first.helpId, helpProviderType: first.helpProviderType };
    if (first.helpProvider) detail.helpProvider = first.helpProvider;
    this.emit('larder:help-provided', detail);
  }

  /** "Check for more answers": one manual look, no polling. */
  async refresh() {
    const id = this.getAttribute('help-request');
    const button = this.$('[data-action=refresh]');
    if (button) button.disabled = true;
    try {
      const { request, helps } = await this.fetchState(id);
      this.request = request;
      this.helps = helps;
      this.error = null;
      this.signature = '';
      this.paint();
      if (helps.length > 0) this.arrived(helps[0]);
    } catch (error) {
      this.error = error;
      this.signature = '';
      this.paint();
    }
  }

  paint() {
    const request = this.request;
    const helps = this.helps || [];
    const slow = Date.now() - this.startedAt > 15000;
    const signature = JSON.stringify([request?.status, helps.map((help) => help.helpId), this.error?.code, slow]);
    if (signature === this.signature) return; // nothing changed: keep the DOM (focus, images) as it is
    this.signature = signature;
    if (!request) {
      put(this.$('#view'), html`<div class="card stack">${problem(this.error)}
        <div><button class="btn" data-action="retry">Try again</button></div></div>`);
      return;
    }
    const grandma = (request.preferredProvider || []).includes('GRANDMA_AVATAR');
    if (helps.length === 0) {
      put(this.$('#view'), html`
        <div class="card stack waiting" role="status">
          <div><span class="pot" aria-hidden="true">🍲</span></div>
          <h2>Help is on its way<span class="dots"></span></h2>
          <p class="muted">${request.title}</p>
          <div>${providerChips(request.preferredProvider)}</div>
          <p class="small muted">${grandma && !slow
            ? 'Grandma is looking through her recipe box - she usually answers within seconds.'
            : slow
              ? 'This one takes a little longer. The community has been asked - you will get a notification when somebody answers.'
              : 'The community has been asked - fellow cooks answer when they can.'}</p>
          ${this.error ? problem(this.error) : ''}
        </div>`);
      return;
    }
    put(this.$('#view'), html`
      <div class="stack">
        <div class="notice arrived" role="status"><span aria-hidden="true">🎉</span>
          <div><strong>Help has arrived!</strong>
          <div class="small muted">${helps.length === 1 ? 'One answer' : `${helps.length} answers`} to “${request.title}”.</div></div></div>
        ${this.helpsList(request, helps, { fresh: helps[0].helpId })}
        ${this.error ? problem(this.error) : ''}
        <div class="row"><button class="btn" data-action="refresh">Check for more answers</button>
          <a class="btn" href="#/help/${request.helpRequestId}" data-nav>Open the request</a></div>
      </div>`);
  }
}

// ---------------------------------------------------------------- larder-cooking-assistance-request

/** Detail page of a help request: status, helps, withdraw while open (requester only), answer as community. */
class RequestDetail extends HelpsView {
  static observedAttributes = ['help-request'];

  static styles = STYLES + `
    .head h1 { overflow-wrap: anywhere; }
    .confirm { display: flex; flex-direction: column; gap: 10px; border: 1px solid var(--larder-danger); }
    section > h2 { margin-top: 8px; }
  `;

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      this.wireHelps();
      this.on('click', '[data-action=withdraw]', () => { this.confirming = true; this.paint(); this.$('[data-action=confirm]')?.focus(); });
      this.on('click', '[data-action=cancel]', () => { this.confirming = false; this.paint(); });
      this.on('click', '[data-action=confirm]', () => this.withdraw());
      this.on('click', '[data-action=retry]', () => this.load());
      this.on('click', '[data-action=refresh]', () => this.load());
      this.on('answered', 'larder-cooking-assistance-answer-form', (event) => {
        this.answeredId = event.detail.helpId;
        this.load();
      });
    }
    this.load();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (this.isConnected && oldValue !== newValue) this.load();
  }

  async load() {
    const id = this.getAttribute('help-request');
    this.confirming = false;
    this.actionError = null;
    if (!id) {
      this.render(html`<div class="card empty">No help request chosen. <a href="#/help" data-nav>Go to the help board</a></div>`);
      return;
    }
    if (!this.request) this.render(html`<div class="card"><p class="loading">Fetching the request…</p></div>`);
    try {
      const [request, helps] = await Promise.all([
        this.api(CONTEXT, `/help-requests/${encodeURIComponent(id)}`),
        this.api(CONTEXT, '/helps', { query: { helpRequestId: id } }),
      ]);
      if (this.getAttribute('help-request') !== id) return;
      this.request = request.body;
      this.helps = [...(helps.body || [])].sort(byCreated);
      this.paint();
    } catch (error) {
      this.request = null;
      this.render(html`<div class="card stack"><h2>Help request</h2>${problem(error)}
        <div class="row"><button class="btn" data-action="retry">Try again</button>
        <a class="btn" href="#/help" data-nav>Help board</a></div></div>`);
    }
  }

  paint() {
    const request = this.request;
    const helps = this.helps || [];
    const mine = request.requester === this.cookId;
    const withdrawable = mine && request.status === 'OPEN';
    const showAnswer = canCommunityAnswer(request) && !mine;
    this.render(html`
      <div class="stack">
        <div class="card stack head">
          <p class="small"><a href="#/help" data-nav>← Help board</a></p>
          <h1><span aria-hidden="true">${typeIcon(request.type)}</span> ${request.title}</h1>
          ${this.requestSummary(request)}
          <p class="text">${request.description}</p>
          ${request.recipe ? recipeCard(request.recipe) : ''}
          ${request.howToStep ? stepView(request.recipe, request.howToStep) : ''}
          <div class="meta"><span class="small muted">Asked:</span> ${providerChips(request.preferredProvider)}</div>
          ${this.actionError ? problem(this.actionError) : ''}
          ${withdrawable && !this.confirming ? html`<div class="row">
            <button class="btn btn-danger" data-action="withdraw">Withdraw request</button></div>` : ''}
          ${withdrawable && this.confirming ? html`<div class="notice confirm" role="alertdialog" aria-label="Withdraw request">
            <p><strong>Withdraw this request?</strong> Nobody will be able to answer it any more. This cannot be undone.</p>
            <div class="row"><button class="btn btn-danger" data-action="confirm" ${this.withdrawing ? 'disabled' : ''}>
              ${this.withdrawing ? 'Withdrawing…' : 'Yes, withdraw'}</button>
              <button class="btn" data-action="cancel" ${this.withdrawing ? 'disabled' : ''}>Keep it</button></div></div>` : ''}
        </div>

        <section class="stack" aria-label="Answers">
          <div class="spread"><h2>Answers</h2><button class="btn" data-action="refresh">Refresh</button></div>
          ${helps.length
            ? this.helpsList(request, helps, { fresh: this.answeredId })
            : html`<div class="card empty">No answers yet.${request.status === 'OPEN' ? ' Help is on its way.' : ''}</div>`}
        </section>

        ${showAnswer ? html`<section class="stack" aria-label="Answer as community">
          <h2>Can you help?</h2>
          <larder-cooking-assistance-answer-form></larder-cooking-assistance-answer-form>
        </section>` : ''}
      </div>`);
    const form = this.$('larder-cooking-assistance-answer-form');
    if (form) form.request = request;
  }

  async withdraw() {
    const id = this.request?.helpRequestId;
    if (!id || this.withdrawing) return;
    this.withdrawing = true;
    this.paint();
    try {
      await this.api(CONTEXT, `/help-requests/${encodeURIComponent(id)}`, { method: 'DELETE' });
      this.withdrawing = false;
      this.navigate('#/help');
    } catch (error) {
      this.withdrawing = false;
      this.confirming = false;
      this.actionError = error;
      if (error instanceof ApiError && error.code === 'HELP_REQUEST_NOT_OPEN') {
        await this.load();
        this.actionError = error;
      }
      this.paint();
    }
  }
}

// ---------------------------------------------------------------- larder-cooking-assistance-help

/** One help with its answer and a link to its request. */
class HelpDetail extends HelpsView {
  static observedAttributes = ['help'];

  static styles = STYLES;

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      this.wireHelps();
      this.on('click', '[data-action=retry]', () => this.load());
    }
    this.load();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (this.isConnected && oldValue !== newValue) this.load();
  }

  async load() {
    const id = this.getAttribute('help');
    if (!id) {
      this.render(html`<div class="card empty">No help chosen.</div>`);
      return;
    }
    this.render(html`<div class="card"><p class="loading">Fetching the answer…</p></div>`);
    try {
      const { body } = await this.api(CONTEXT, `/helps/${encodeURIComponent(id)}`);
      if (this.getAttribute('help') !== id) return;
      this.helps = [body];
      this.render(html`<div class="stack">
        <p class="small"><a href="#/help" data-nav>← Help board</a></p>
        ${helpCard(body, { cookId: this.cookId, thanks: body.helpRequester === this.cookId, requestLink: true })}
      </div>`);
    } catch (error) {
      const chef = error instanceof ApiError && error.status === 403;
      this.render(html`<div class="card stack"><h2>Help</h2>
        ${chef ? html`<div class="notice">A chef's answer is only visible to the cook who asked for it.</div>` : problem(error)}
        <div class="row">${chef ? '' : html`<button class="btn" data-action="retry">Try again</button>`}
          <a class="btn" href="#/help" data-nav>Help board</a></div></div>`);
    }
  }
}

// ---------------------------------------------------------------- larder-cooking-assistance-board

const FILTERS = [['ALL', 'All', '🥄'], ...Object.entries(TYPES).map(([type, t]) => [type, t.label, t.icon])];

/** Open help requests of the community; answer one as community; link to details. */
class Board extends LarderElement {
  static styles = STYLES + `
    .filters { display: flex; flex-wrap: wrap; gap: 6px; }
    .filter[aria-pressed=true] { background: var(--larder-accent); border-color: var(--larder-accent); color: var(--larder-accent-text); }
    .request { display: flex; flex-direction: column; gap: 10px; }
    .request h3 { margin: 0; overflow-wrap: anywhere; }
    .request .description { display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; margin: 0; }
    .request.open-answer { grid-column: 1 / -1; }
    .request .footer { justify-content: space-between; margin-top: auto; }
    .success { border-left: 4px solid var(--larder-herb); }
    .big-icon { font-size: 2rem; }
  `;

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      wireNavigation(this);
      this.on('click', '[data-filter]', (event, button) => { this.filter = button.dataset.filter; this.openAnswer = null; this.paint(); });
      this.on('click', '[data-action=refresh]', () => this.load());
      this.on('click', '[data-action=answer]', (event, button) => {
        this.openAnswer = this.openAnswer === button.dataset.request ? null : button.dataset.request;
        this.answered = null;
        this.paint();
        this.$('larder-cooking-assistance-answer-form')?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      });
      this.on('cancelled', 'larder-cooking-assistance-answer-form', () => { this.openAnswer = null; this.paint(); });
      this.on('answered', 'larder-cooking-assistance-answer-form', (event) => {
        this.answered = { helpId: event.detail.helpId, title: event.detail.title };
        this.openAnswer = null;
        this.load();
      });
    }
    this.filter = this.filter || 'ALL';
    this.load();
  }

  async load() {
    if (!this.requests) this.render(html`<div class="card"><p class="loading">Collecting open questions…</p></div>`);
    try {
      const { body } = await this.api(CONTEXT, '/help-requests', { query: { status: 'OPEN' } });
      this.requests = [...(body || [])].sort((a, b) => byCreated(b, a));
      this.error = null;
    } catch (error) {
      this.error = error;
    }
    this.paint();
  }

  paint() {
    const all = this.requests || [];
    const shown = this.filter === 'ALL' ? all : all.filter((request) => request.type === this.filter);
    this.render(html`
      <div class="stack">
        <div class="spread">
          <div><h1>Help board</h1>
            <p class="muted">Fellow cooks are stuck at the stove - lend them a hand.</p></div>
          <button class="btn" data-action="refresh">Refresh</button>
        </div>
        <div class="filters" role="group" aria-label="Filter by kind of question">
          ${FILTERS.map(([value, label, icon]) => html`
            <button class="btn filter" data-filter="${value}" aria-pressed="${this.filter === value ? 'true' : 'false'}">
              <span aria-hidden="true">${icon}</span> ${label}
              ${value === 'ALL' ? html` <span class="small">(${all.length})</span>` : ''}</button>`)}
        </div>
        <div aria-live="polite">${this.answered ? html`<div class="notice success" role="status">
          <strong>Thank you for helping!</strong> Your answer is on its way to the cook.
          <a href="#/helps/${this.answered.helpId}" data-nav>See your answer</a></div>` : ''}</div>
        ${this.error ? problem(this.error) : ''}
        ${!this.error && shown.length === 0 ? html`<div class="card empty">
          <div class="big-icon" aria-hidden="true">🍲</div>
          ${all.length ? 'No open questions of this kind.' : 'No open questions - every cook is happily cooking.'}</div>` : ''}
        <div class="grid">${shown.map((request) => this.requestCard(request))}</div>
      </div>`);
    const form = this.$('larder-cooking-assistance-answer-form');
    if (form) form.request = all.find((request) => request.helpRequestId === this.openAnswer);
  }

  requestCard(request) {
    const providers = request.preferredProvider || [];
    const mine = request.requester === this.cookId;
    const community = canCommunityAnswer(request) && !mine;
    const open = this.openAnswer === request.helpRequestId;
    const why = mine ? 'Your question' : providers.includes('CHEF') ? 'Reserved for a chef'
      : providers.includes('GRANDMA_AVATAR') ? 'Grandma is on it' : '';
    return html`<article class="card request ${open ? 'open-answer' : ''}">
      <div class="meta">
        <span class="badge accent">${typeIcon(request.type)} ${typeLabel(request.type)}</span>
        ${mine ? html`<span class="badge">Yours</span>` : ''}
        ${request.createdAt ? html`<span class="small muted">${fmt.dateTime(request.createdAt)}</span>` : ''}
      </div>
      <h3>${request.title}</h3>
      <p class="description muted">${request.description}</p>
      ${request.recipe ? recipeCard(request.recipe) : ''}
      <div>${providerChips(providers)}</div>
      <div class="row footer">
        <a class="btn" href="#/help/${request.helpRequestId}" data-nav>Details</a>
        ${community
          ? html`<button class="btn ${open ? '' : 'btn-primary'}" data-action="answer" data-request="${request.helpRequestId}"
              aria-expanded="${open ? 'true' : 'false'}">${open ? 'Close' : 'Answer'}</button>`
          : html`<span class="small muted">${why}</span>`}
      </div>
      ${open ? html`<larder-cooking-assistance-answer-form></larder-cooking-assistance-answer-form>` : ''}
    </article>`;
  }
}

// ---------------------------------------------------------------- larder-cooking-assistance-answer-form (internal)

/**
 * Internal building block of the board and the request page (not part of MICRO-UI.md): answers one
 * help request as COMMUNITY. Set the request as property `request`. Dispatches the non-composed events
 * `answered` {helpId, title} and `cancelled` to its host.
 */
class AnswerForm extends LarderElement {
  static styles = STYLES + `
    form { display: flex; flex-direction: column; gap: 14px; border-top: 1px dashed var(--larder-border); padding-top: 14px; }
    .two { display: grid; gap: 10px; grid-template-columns: repeat(auto-fit, minmax(min(100%, 140px), 1fr)); }
    .course { display: grid; gap: 8px; grid-template-columns: 90px minmax(0, 1fr) auto; align-items: center; }
    .course input { width: 100%; }
    .picker { border: 1px solid var(--larder-border); border-radius: var(--larder-radius-small); padding: 12px; }
    .actions { justify-content: flex-end; }
  `;

  set request(request) {
    if (this._request?.helpRequestId === request?.helpRequestId) return;
    this._request = request;
    this.courses = [];
    this.picking = false;
    if (this.isConnected) this.paint();
  }

  get request() {
    return this._request;
  }

  connectedCallback() {
    if (!this.wired) {
      this.wired = true;
      this.on('submit', 'form', (event) => {
        event.preventDefault();
        this.submit(event.target);
      });
      this.on('click', '[data-action=cancel]', () => this.dispatchEvent(new CustomEvent('cancelled', { bubbles: true })));
      this.on('click', '[data-action=add-course]', () => { this.picking = !this.picking; this.paintCourses(); });
      this.on('click', '[data-action=remove-course]', (event, button) => {
        this.courses.splice(Number(button.dataset.index), 1);
        this.paintCourses();
      });
      this.on('change', 'input[data-course-step]', (event, input) => {
        const course = this.courses[Number(input.dataset.courseStep)];
        if (course) course.step = Number(input.value) || 1;
      });
      // the embedded Recipe Catalog picker - handled here, not by the shell
      this.shadowRoot.addEventListener('larder:recipe-picked', (event) => {
        event.stopPropagation();
        const { recipeId, name } = event.detail || {};
        if (recipeId && this.courses.length < 10) {
          this.courses.push({ step: this.courses.length + 1, recipe: recipeId, name: name || 'Recipe' });
        }
        this.picking = false;
        this.paintCourses();
      });
    }
    this.saving = false;
    if (this._request) this.paint();
  }

  paint() {
    const request = this._request;
    if (!request) {
      this.render(html``);
      return;
    }
    const type = request.type;
    let fields;
    switch (type) {
      case 'PREPARATION_STEP_EXPLANATION':
        fields = html`
          ${stepView(request.recipe, request.howToStep)}
          <label>Your explanation
            <textarea name="description" required maxlength="2000"
              placeholder="Explain the step as you would to a friend next to you at the stove."></textarea></label>`;
        break;
      case 'STEPS_TO_MITIGATE_CATASTROPHE':
        fields = html`<label>How to rescue the meal
          <textarea name="explanation" required maxlength="2000" placeholder="First take the pan off the heat…"></textarea></label>`;
        break;
      case 'INGREDIENT_SUBSTITUTE':
        fields = html`${(request.ingredients || []).map((ingredient, index) => html`
          <fieldset><legend>Instead of ingredient ${index + 1}</legend>
            <div class="two">
              <label>Use <input name="name-${index}" required maxlength="100" placeholder="e.g. Greek yoghurt"></label>
              <label>Amount <input name="value-${index}" type="number" required min="0.001" step="any" inputmode="decimal"></label>
              <label>Unit <select name="unit-${index}" required>
                ${UNITS.map((unit) => html`<option value="${unit}">${unitShort(unit)} - ${fmt.label(unit)}</option>`)}
              </select></label>
            </div></fieldset>`)}`;
        break;
      case 'MENU_PROPOSAL':
        fields = html`
          <div class="two">
            <label>Meal <select name="meal" required>
              ${MEALS.map((meal) => html`<option value="${meal}" ${meal === 'DINNER' ? 'selected' : ''}>${fmt.label(meal)}</option>`)}
            </select></label>
            <label>Servings <input name="servings" type="number" required min="1" step="1" value="4" inputmode="numeric"></label>
          </div>
          <label>Your advice <textarea name="note" required maxlength="2000" placeholder="Why this menu works…"></textarea></label>
          <label>How to serve <span class="muted small">(optional)</span>
            <textarea name="howToServe" maxlength="2000" placeholder="Keep course 2 warm while serving the soup…"></textarea></label>
          <fieldset><legend>Courses (1 to 10)</legend><div id="courses"></div></fieldset>`;
        break;
      default:
        fields = html`<div class="notice">This kind of question cannot be answered here.</div>`;
    }
    this.render(html`
      <form aria-label="Answer as community">
        <p class="eyebrow">Your answer as community</p>
        <label>Headline
          <input name="answerTitle" required maxlength="200" autocomplete="off" placeholder="e.g. Stay calm - it can be saved"></label>
        ${fields}
        <div id="problem" aria-live="polite"></div>
        <div class="row actions">
          <button class="btn" type="button" data-action="cancel">Cancel</button>
          <button class="btn btn-primary" type="submit">Send answer</button>
        </div>
      </form>`);
    if (type === 'MENU_PROPOSAL') this.paintCourses();
  }

  paintCourses() {
    const slot = this.$('#courses');
    if (!slot) return;
    const pickerAvailable = Boolean(customElements.get('larder-recipe-catalog-picker'));
    put(slot, html`
      <div class="stack">
        ${this.courses.length ? html`<ol class="plain stack">${this.courses.map((course, index) => html`
          <li class="course">
            <label>Course <input type="number" min="1" step="1" value="${course.step}" data-course-step="${index}"
              aria-label="Course number of ${course.name}"></label>
            <span>${course.name}</span>
            <button class="btn" type="button" data-action="remove-course" data-index="${index}"
              aria-label="Remove ${course.name}">✕</button>
          </li>`)}</ol>` : html`<p class="small muted">No courses yet.</p>`}
        ${pickerAvailable
          ? html`${this.courses.length < 10 ? html`<div><button class="btn" type="button" data-action="add-course">
              ${this.picking ? 'Close the recipe search' : '+ Add a course'}</button></div>` : ''}
            ${this.picking ? html`<div class="picker"><larder-recipe-catalog-picker></larder-recipe-catalog-picker></div>` : ''}`
          : html`<div class="notice">Recipes cannot be searched right now, so no courses can be added.</div>`}
      </div>`);
  }

  answerFrom(form) {
    const request = this._request;
    const value = (name) => form.elements[name]?.value?.trim() || '';
    switch (request.type) {
      case 'PREPARATION_STEP_EXPLANATION':
        return { answerType: request.type, recipe: request.recipe, howToStep: request.howToStep, description: value('description') };
      case 'STEPS_TO_MITIGATE_CATASTROPHE': {
        const answer = { answerType: request.type, explanation: value('explanation') };
        if (request.recipe) answer.recipe = request.recipe;
        return answer;
      }
      case 'INGREDIENT_SUBSTITUTE':
        return {
          answerType: request.type,
          recipe: request.recipe,
          substitute: (request.ingredients || []).map((ingredient, index) => ({
            ingredient,
            substituteIngredient: { name: value(`name-${index}`), value: Number(value(`value-${index}`)), unit: value(`unit-${index}`) },
          })),
        };
      case 'MENU_PROPOSAL': {
        if (this.courses.length === 0) throw new Error('Please add at least one course.');
        const answer = {
          answerType: request.type,
          note: value('note'),
          servings: Number(value('servings')),
          meal: value('meal'),
          course: this.courses.map((course) => ({ step: course.step, meal: { recipe: course.recipe } })),
        };
        if (value('howToServe')) answer.howToServe = value('howToServe');
        return answer;
      }
      default:
        throw new Error('This kind of question cannot be answered here.');
    }
  }

  setSaving(saving) {
    this.saving = saving;
    const form = this.$('form');
    if (!form) return;
    for (const element of form.elements) element.disabled = saving;
    const submit = this.$('button[type=submit]');
    if (submit) submit.textContent = saving ? 'Sending…' : 'Send answer';
  }

  async submit(form) {
    if (this.saving || !this._request) return;
    put(this.$('#problem'), html``);
    let body;
    try {
      body = {
        helpRequest: this._request.helpRequestId,
        answerTitle: form.elements.answerTitle.value.trim(),
        helpProviderType: 'COMMUNITY',
        answer: this.answerFrom(form),
      };
    } catch (error) {
      put(this.$('#problem'), problem(error));
      return;
    }
    this.setSaving(true);
    try {
      const { body: link, location } = await this.api(CONTEXT, '/helps', { method: 'POST', body });
      this.setSaving(false);
      this.dispatchEvent(new CustomEvent('answered', {
        bubbles: true, detail: { helpId: idFrom(location, link?.helpLink), title: body.answerTitle },
      }));
    } catch (error) {
      this.setSaving(false);
      put(this.$('#problem'), problem(error));
    }
  }
}

define('larder-cooking-assistance-answer-form', AnswerForm);
define('larder-cooking-assistance-request-form', RequestForm);
define('larder-cooking-assistance-help-status', HelpStatus);
define('larder-cooking-assistance-request', RequestDetail);
define('larder-cooking-assistance-help', HelpDetail);
define('larder-cooking-assistance-board', Board);
