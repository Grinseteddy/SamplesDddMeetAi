/**
 * Micro-UI of Consent Management (MICRO-UI.md): the cook's consents and the question for one consent.
 * Calls only the Consent Management API (contracts/openapi/consent-management.openapi.yaml).
 *
 * A consent text counts as "given" while the cook has a consent to it without `revokedAt`.
 * Revoking never deletes: the server sets `revokedAt` and keeps the consent as evidence;
 * giving again creates a new consent.
 */
import { ApiError, LarderElement, define, errorMessage, fmt, html } from '/app-shell/kit.js';

const CONTEXT = 'consent-management';

/**
 * Purposes other contexts ask for (MICRO-UI.md) → consent text ids (seeded by V3__consent_texts.sql).
 * The mapping lives only here; the wording always comes from the API.
 */
const PURPOSES = {
  'photos-in-public-thanks': {
    consentTextId: '5f8d8a1c-515d-4eae-a6b1-0a0313edfc31',
    title: 'Your photos in public thanks',
    why: 'Every thanks in Larder shows a picture of the dish, so the community can see what was rescued. '
      + 'Before your photo is shown to others, Larder needs your permission.',
  },
  'mention-as-helper': {
    consentTextId: '3c6e2b7a-9d41-4f0b-8e5a-2b7c9d1e4f60',
    title: 'Being thanked by name',
    why: 'When you help another cook, they may want to thank you by name. '
      + 'Larder only names you in their thanks if you allow it.',
  },
};

const textIdOf = (consent) => consent.consentText?.consentTextId;
const inForce = (consent) => !consent.revokedAt;
const newestFirst = (a, b) => String(b.givenAt).localeCompare(String(a.givenAt));

/** Error near an action: the friendly sentence plus the contract's code. */
const actionError = (error) => {
  const code = error instanceof ApiError && error.status < 500 && error.code ? error.code : null;
  return html`<div class="error small" role="alert">${errorMessage(error)}${code ? html` <span class="code">${code}</span>` : ''}</div>`;
};

const SHARED_STYLES = `
  .code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: .75rem; opacity: .8; margin-left: 4px; }
  .wording { margin: 0; padding: 12px 16px; border-left: 4px solid var(--larder-accent); background: var(--larder-surface-muted);
             border-radius: 0 var(--larder-radius-small) var(--larder-radius-small) 0;
             font-family: var(--larder-font-serif); font-size: 1.08rem; line-height: 1.45; overflow-wrap: anywhere; }
  .actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
  @media (max-width: 480px) { .actions .btn { flex: 1 1 auto; } }
`;

// ---------------------------------------------------------------- all consents

/** `larder-consent-management-consents`: every consent text with the cook's state; give and revoke. */
class ConsentManagementConsents extends LarderElement {
  static styles = `${SHARED_STYLES}
    .item { display: flex; flex-direction: column; gap: 10px; padding: 16px 0; border-top: 1px solid var(--larder-border); }
    .item:first-child { border-top: none; padding-top: 4px; }
    .state { display: flex; gap: 8px 12px; align-items: center; flex-wrap: wrap; justify-content: space-between; }
    .badge.off { background: transparent; border: 1px solid var(--larder-border); }
    details summary { cursor: pointer; color: var(--larder-text-muted); font-size: .875rem; width: fit-content; }
    details ol { margin: 8px 0 0; padding-left: 20px; display: flex; flex-direction: column; gap: 6px; font-size: .875rem; }
    details .then { display: block; color: var(--larder-text-muted); font-style: italic; }
    .evidence { font-size: .875rem; }
  `;

  connectedCallback() {
    if (!this.started) {
      this.started = true;
      this.on('click', '[data-action=retry]', () => this.load());
      this.on('click', '[data-action=give]', (event, button) => this.give(button));
      this.on('click', '[data-action=revoke]', (event, button) => this.revoke(button));
    }
    this.load();
  }

  async load() {
    this.render(html`<section class="card stack"><h2>Your consents</h2><p class="loading">Loading your consents…</p></section>`);
    try {
      const [texts, consents] = await Promise.all([
        this.api(CONTEXT, '/consents/consent-texts'),
        this.api(CONTEXT, '/consents', { query: { subject: this.cookId } }),
      ]);
      this.texts = texts.body || [];
      this.consents = consents.body || [];
      this.show();
    } catch (error) {
      this.render(html`<section class="card stack"><h2>Your consents</h2>
        <div class="error" role="alert">${errorMessage(error)}</div>
        <div><button class="btn" data-action="retry">Try again</button></div></section>`);
    }
  }

  /** Reloads only the cook's consents after a change and keeps the page in place. */
  async refresh(errors = {}) {
    try {
      const { body } = await this.api(CONTEXT, '/consents', { query: { subject: this.cookId } });
      this.consents = body || [];
    } catch (error) {
      errors.__all = error;
    }
    this.show(errors);
  }

  show(errors = {}) {
    const items = this.texts.map((text) => {
      const history = this.consents.filter((c) => textIdOf(c) === text.consentTextId).sort(newestFirst);
      const active = history.find(inForce);
      const last = history[0];
      const error = errors[text.consentTextId];
      const id = `text-${text.consentTextId}`;
      return html`
        <li class="item" aria-labelledby="${id}">
          <blockquote class="wording" id="${id}">${text.text}</blockquote>
          <div class="state">
            <div class="row">
              ${active
                ? html`<span class="badge herb">Given</span><span class="muted small">since ${fmt.dateTime(active.givenAt)}</span>`
                : html`<span class="badge off">Not given</span>${last?.revokedAt
                  ? html`<span class="muted small">revoked ${fmt.dateTime(last.revokedAt)}</span>` : ''}`}
            </div>
            ${active
              ? html`<button class="btn btn-danger" data-action="revoke" data-text="${text.consentTextId}">Revoke</button>`
              : html`<button class="btn btn-primary" data-action="give" data-text="${text.consentTextId}">I agree</button>`}
          </div>
          ${error ? actionError(error) : ''}
          ${history.length ? html`
            <details>
              <summary>History (${history.length})</summary>
              <ol reversed>
                ${history.map((consent) => html`<li>
                  Given ${fmt.dateTime(consent.givenAt)}${consent.revokedAt ? html` · revoked ${fmt.dateTime(consent.revokedAt)}` : html` · <strong>in force</strong>`}
                  ${consent.consentText?.text && consent.consentText.text !== text.text
                    ? html`<span class="then">Wording then: “${consent.consentText.text}”</span>` : ''}
                </li>`)}
              </ol>
            </details>` : ''}
        </li>`;
    });
    this.render(html`
      <section class="card stack" aria-label="Your consents">
        <div>
          <h2>Your consents</h2>
          <p class="muted">You decide what Larder may do with your photos and your name. Agree or revoke at any time -
             your choice applies from that moment on.</p>
        </div>
        ${errors.__all ? actionError(errors.__all) : ''}
        ${this.texts.length
          ? html`<ul class="plain">${items}</ul>`
          : html`<p class="empty">There is nothing to consent to right now.</p>`}
        <p class="notice evidence">Revoked consents are not deleted. Larder keeps them, with the wording you agreed to,
          as evidence of what you allowed and when it ended. If you agree again later, a new consent is recorded.</p>
      </section>`);
  }

  busy(button, label) {
    this.shadowRoot.querySelectorAll('[data-action=give], [data-action=revoke]').forEach((b) => { b.disabled = true; });
    button.textContent = label;
  }

  async give(button) {
    const consentTextId = button.dataset.text;
    this.busy(button, 'Saving…');
    try {
      await this.api(CONTEXT, '/consents', { method: 'POST', body: { subject: this.cookId, consentTextId } });
      await this.refresh();
    } catch (error) {
      await this.refresh({ [consentTextId]: error });
    }
  }

  async revoke(button) {
    const consentTextId = button.dataset.text;
    const active = this.consents.filter((c) => textIdOf(c) === consentTextId && inForce(c));
    this.busy(button, 'Revoking…');
    let failure;
    for (const consent of active) {
      try {
        await this.api(CONTEXT, `/consents/${encodeURIComponent(consent.consentId)}`, { method: 'DELETE' });
      } catch (error) {
        // Revoked meanwhile (e.g. in another tab): the goal is reached.
        if (!(error instanceof ApiError && error.code === 'CONSENT_ALREADY_REVOKED')) failure ||= error;
      }
    }
    await this.refresh(failure ? { [consentTextId]: failure } : {});
  }
}

// ---------------------------------------------------------------- ask for one consent

/**
 * `larder-consent-management-ask` (attribute `purpose`): explains one consent and asks for it.
 * → `larder:consent-given {purpose}` (also right away if already given) or `larder:consent-declined {purpose}`.
 */
class ConsentManagementAsk extends LarderElement {
  static observedAttributes = ['purpose'];

  static styles = `${SHARED_STYLES}
    .why { color: var(--larder-text-muted); }
    .reassure { font-size: .875rem; color: var(--larder-text-muted); }
  `;

  connectedCallback() {
    if (!this.started) {
      this.started = true;
      this.on('click', '[data-action=allow]', (event, button) => this.allow(button));
      this.on('click', '[data-action=decline]', () => this.answer('larder:consent-declined'));
      this.on('click', '[data-action=retry]', () => this.load());
    }
    this.load();
  }

  attributeChangedCallback(name, previous, value) {
    if (this.isConnected && this.started && previous !== value) this.load();
  }

  get purpose() {
    return this.getAttribute('purpose') || '';
  }

  /** Emits the answer once per purpose. */
  answer(event) {
    if (this.answered === this.purpose) return;
    this.answered = this.purpose;
    this.emit(event, { purpose: this.purpose });
  }

  async load() {
    const purpose = this.purpose;
    const known = PURPOSES[purpose];
    this.answered = null;
    if (!purpose) {
      this.render(html`<p class="empty">No consent asked for.</p>`);
      return;
    }
    if (!known) {
      this.render(html`<div class="stack">
        <div class="error" role="alert">Larder does not know the consent “${purpose}”.</div>
        <div class="actions"><button class="btn" data-action="decline">Close</button></div></div>`);
      return;
    }
    this.render(html`<p class="loading">Fetching the wording…</p>`);
    try {
      const [text, consents] = await Promise.all([
        this.api(CONTEXT, `/consents/consent-texts/${encodeURIComponent(known.consentTextId)}`),
        this.api(CONTEXT, '/consents', { query: { subject: this.cookId } }),
      ]);
      if (purpose !== this.purpose) return; // the purpose changed while loading
      const given = (consents.body || []).some((c) => textIdOf(c) === known.consentTextId && inForce(c));
      if (given) {
        this.render(html`<p class="notice">You have already agreed: “${text.body.text}”</p>`);
        this.answer('larder:consent-given');
        return;
      }
      this.text = text.body;
      this.show(known);
    } catch (error) {
      if (purpose !== this.purpose) return;
      this.render(html`<div class="stack">
        <div class="error" role="alert">${errorMessage(error)}</div>
        <div class="actions">
          <button class="btn" data-action="decline">Not now</button>
          <button class="btn btn-primary" data-action="retry">Try again</button>
        </div></div>`);
    }
  }

  show(known) {
    this.render(html`
      <div class="stack" role="group" aria-labelledby="title">
        <h3 id="title">${known.title}</h3>
        <p class="why">${known.why}</p>
        <div>
          <p class="small muted">By choosing “Allow”, you agree to:</p>
          <blockquote class="wording">${this.text.text}</blockquote>
        </div>
        <p class="reassure">Your choice is up to you. You can revoke it at any time under Profile → Your consents.</p>
        <div id="error" aria-live="polite"></div>
        <div class="actions">
          <button class="btn" data-action="decline">Not now</button>
          <button class="btn btn-primary" data-action="allow">Allow</button>
        </div>
      </div>`);
  }

  async allow(button) {
    const purpose = this.purpose;
    const known = PURPOSES[purpose];
    const slot = this.$('#error');
    const buttons = this.shadowRoot.querySelectorAll('.actions .btn');
    buttons.forEach((b) => { b.disabled = true; });
    button.textContent = 'Saving…';
    slot.innerHTML = '';
    try {
      await this.api(CONTEXT, '/consents', { method: 'POST', body: { subject: this.cookId, consentTextId: known.consentTextId } });
      this.render(html`<p class="notice">Thank you - your consent is recorded.</p>`);
      this.answer('larder:consent-given');
    } catch (error) {
      slot.innerHTML = actionError(error).__html;
      buttons.forEach((b) => { b.disabled = false; });
      button.textContent = 'Allow';
    }
  }
}

define('larder-consent-management-consents', ConsentManagementConsents);
define('larder-consent-management-ask', ConsentManagementAsk);
