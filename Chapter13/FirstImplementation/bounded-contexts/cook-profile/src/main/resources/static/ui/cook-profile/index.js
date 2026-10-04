/**
 * Micro-UI of Cook Profile (MICRO-UI.md): the registration gate and the cook's own profile.
 * Calls only the Cook Profile API (contracts/openapi/cook-profile.openapi.yaml).
 */
import { ApiError, LarderElement, define, errorMessage, fmt, formValues, html } from '/app-shell/kit.js';

const CONTEXT = 'cook-profile';

/** Contract enum Status → wording for the cook. */
const STATUSES = [
  ['active', 'Active', 'Cooking along with the community'],
  ['inActive', 'Taking a break', 'Your profile stays, you are just not active right now'],
  ['premium', 'Premium', 'All of Larder, including the premium features'],
];
const statusLabel = (status) => STATUSES.find(([value]) => value === status)?.[1] || fmt.label(status);

/** Error near a form: the friendly sentence plus the contract's code. */
const formError = (error) => {
  if (!error) return html``;
  const code = error instanceof ApiError && error.status < 500 && error.code ? error.code : null;
  return html`<div class="error" role="alert">${errorMessage(error)}${code ? html` <span class="code">${code}</span>` : ''}</div>`;
};

/** Trims every value of a form; whitespace-only values are left out like empty ones. */
const trimmed = (form) => Object.fromEntries(Object.entries(formValues(form))
  .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
  .filter(([, value]) => value !== ''));

const fullName = (cook) => [cook.givenName, cook.name].filter(Boolean).join(' ');
const initials = (cook) => [cook.givenName, cook.name].filter(Boolean).map((n) => n.trim().charAt(0).toUpperCase()).join('');

const SHARED_STYLES = `
  .code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: .78rem; opacity: .8; margin-left: 4px; }
  .fields { display: grid; gap: 14px; grid-template-columns: repeat(auto-fit, minmax(min(100%, 220px), 1fr)); }
  .fields .wide { grid-column: 1 / -1; }
  .hint { font-weight: 400; font-size: .8rem; color: var(--larder-text-muted); }
  .actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
  @media (max-width: 480px) { .actions .btn { flex: 1 1 auto; } }
`;

// ---------------------------------------------------------------- gate

/**
 * `larder-cook-profile-gate`: checks whether the signed-in cook is registered. If so, it emits
 * `larder:cook-ready` right away; if not, it welcomes the user with the registration form.
 */
class CookProfileGate extends LarderElement {
  static styles = `${SHARED_STYLES}
    :host { display: block; }
    .welcome { max-width: 620px; margin: 4vh auto 0; display: flex; flex-direction: column; gap: 20px; }
    .hero { text-align: center; padding: 8px 8px 0; }
    .hero .emoji { font-size: 2.6rem; line-height: 1; margin-bottom: 10px; }
    .hero h1 { font-size: 2.1rem; }
    .hero p { color: var(--larder-text-muted); max-width: 460px; margin: 0 auto; }
    .promises { display: grid; gap: 10px; grid-template-columns: repeat(auto-fit, minmax(min(100%, 160px), 1fr));
                margin: 0; padding: 0; list-style: none; }
    .promises li { background: var(--larder-surface-muted); border-radius: var(--larder-radius-small); padding: 10px 12px;
                   font-size: .9rem; color: var(--larder-text-muted); }
    .promises strong { display: block; color: var(--larder-text); }
    .checking { text-align: center; padding: 48px 8px; }
  `;

  connectedCallback() {
    if (!this.started) {
      this.started = true;
      this.on('submit', 'form[data-form=register]', (event, form) => {
        event.preventDefault();
        this.register(form);
      });
      this.on('click', '[data-action=retry]', () => this.check());
    }
    this.check();
  }

  async check() {
    this.render(html`<p class="loading checking">Setting the table…</p>`);
    try {
      await this.api(CONTEXT, `/cooks/${encodeURIComponent(this.cookId)}`);
      this.ready();
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) this.showRegistration();
      else this.render(html`
        <div class="welcome"><div class="card stack">
          <div class="error" role="alert">${errorMessage(error)}</div>
          <div><button class="btn btn-primary" data-action="retry">Try again</button></div>
        </div></div>`);
    }
  }

  ready() {
    this.render(html`<p class="loading checking">Opening your larder…</p>`);
    this.emit('larder:cook-ready', { cookId: this.cookId });
  }

  showRegistration() {
    this.render(html`
      <div class="welcome">
        <div class="hero">
          <div class="emoji" aria-hidden="true">🍲</div>
          <h1>Welcome to Larder</h1>
          <p>Cook from recipes, plan your meals and get help at the stove when a step is unclear - from Grandma,
             a chef or the community. Tell us who you are and pull up a chair.</p>
        </div>
        <ul class="promises">
          <li><strong>Recipes</strong>Find, cook and share the dishes you love</li>
          <li><strong>Rescue</strong>Help when the sauce splits or a step is unclear</li>
          <li><strong>Thanks</strong>Say thank you to the cooks who helped</li>
        </ul>
        <form class="card stack" data-form="register" novalidate>
          <h2>Become a cook</h2>
          <div class="fields">
            <label>Given name
              <input name="givenName" required maxlength="100" autocomplete="given-name"></label>
            <label>Name
              <input name="name" required maxlength="100" autocomplete="family-name"></label>
            <label class="wide">Email
              <input name="email" type="email" required maxlength="254" autocomplete="email" inputmode="email">
              <span class="hint">You sign in with it; other cooks never see it.</span></label>
          </div>
          <div id="error" aria-live="polite"></div>
          <div class="actions">
            <button class="btn btn-primary btn-big" type="submit">Join Larder</button>
          </div>
        </form>
      </div>`);
    this.$('input[name=givenName]')?.focus();
  }

  async register(form) {
    const slot = this.$('#error');
    if (!form.reportValidity()) return;
    const values = trimmed(form);
    if (!values.givenName || !values.name || !values.email) {
      slot.innerHTML = html`<div class="error" role="alert">Please fill in your given name, name and email.</div>`.__html;
      return;
    }
    const button = form.querySelector('button[type=submit]');
    button.disabled = true;
    button.textContent = 'Setting your place…';
    slot.innerHTML = '';
    try {
      // The new cook's id is the signed-in user's id (token claim) - the body carries no id.
      await this.api(CONTEXT, '/cooks', {
        method: 'POST',
        body: { email: values.email, name: values.name, givenName: values.givenName },
      });
      this.ready();
    } catch (error) {
      if (error instanceof ApiError && error.code === 'ALREADY_REGISTERED' && await this.registered()) {
        this.ready(); // registered meanwhile (e.g. in another tab)
        return;
      }
      slot.innerHTML = formError(error).__html;
      button.disabled = false;
      button.textContent = 'Join Larder';
    }
  }

  async registered() {
    try {
      await this.api(CONTEXT, `/cooks/${encodeURIComponent(this.cookId)}`);
      return true;
    } catch {
      return false;
    }
  }
}

// ---------------------------------------------------------------- profile

/**
 * `larder-cook-profile-profile`: shows and changes the signed-in cook's own profile;
 * deregistration with explicit confirmation → `larder:logout`.
 */
class CookProfileProfile extends LarderElement {
  static styles = `${SHARED_STYLES}
    .head { display: flex; gap: 16px; align-items: center; flex-wrap: wrap; }
    .avatar { width: 64px; height: 64px; border-radius: 50%; flex: none; display: grid; place-items: center;
              background: var(--larder-accent); color: var(--larder-accent-text);
              font-family: var(--larder-font-serif); font-size: 1.6rem; font-weight: 700; }
    .who { flex: 1; min-width: 0; }
    .who h1 { margin: 0; overflow-wrap: anywhere; }
    dl { display: grid; grid-template-columns: max-content 1fr; gap: 6px 16px; margin: 0; }
    dt { color: var(--larder-text-muted); font-size: .9rem; }
    dd { margin: 0; overflow-wrap: anywhere; }
    @media (max-width: 480px) { dl { grid-template-columns: 1fr; gap: 0; } dd { margin-bottom: 8px; } }
    .statuses { display: grid; gap: 8px; }
    .statuses label { border: 1px solid var(--larder-border); border-radius: var(--larder-radius-small); padding: 10px 12px; cursor: pointer; }
    .statuses label:has(input:checked) { border-color: var(--larder-accent); background: var(--larder-surface-muted); }
    .statuses .hint { display: block; }
    .saved { color: var(--larder-herb); font-weight: 600; }
    .danger { border-color: color-mix(in srgb, var(--larder-danger) 45%, var(--larder-border)); }
    .danger h2 { color: var(--larder-danger); }
    .confirm { border: 1px dashed var(--larder-danger); border-radius: var(--larder-radius-small); padding: 14px; }
    .btn-danger.solid { background: var(--larder-danger); color: var(--larder-surface); }
  `;

  connectedCallback() {
    if (!this.started) {
      this.started = true;
      this.on('click', '[data-action=edit]', () => this.show('edit'));
      this.on('click', '[data-action=cancel]', () => this.show('view'));
      this.on('click', '[data-action=retry]', () => this.load());
      this.on('click', '[data-action=deregister]', () => this.show('view', { confirming: true }));
      this.on('click', '[data-action=keep]', () => this.show('view'));
      this.on('change', '[name=understood]', (event, box) => {
        this.$('[data-action=confirm-deregister]').disabled = !box.checked;
      });
      this.on('click', '[data-action=confirm-deregister]', (event, button) => this.deregister(button));
      this.on('submit', 'form[data-form=edit]', (event, form) => {
        event.preventDefault();
        this.save(form);
      });
    }
    this.load();
  }

  async load(notice) {
    this.render(html`<div class="card"><p class="loading">Loading your profile…</p></div>`);
    try {
      const { body } = await this.api(CONTEXT, `/cooks/${encodeURIComponent(this.cookId)}`);
      this.cook = body;
      this.show('view', { notice });
    } catch (error) {
      this.render(html`<div class="card stack">
        <h2>Your profile</h2>
        <div class="error" role="alert">${errorMessage(error)}</div>
        <div><button class="btn" data-action="retry">Try again</button></div></div>`);
    }
  }

  show(mode, { notice, confirming } = {}) {
    const cook = this.cook;
    if (!cook) return;
    const header = html`
      <div class="head">
        <div class="avatar" aria-hidden="true">${initials(cook) || '🍳'}</div>
        <div class="who">
          <h1>${fullName(cook)}</h1>
          <div class="row"><span class="badge ${cook.status === 'premium' ? 'accent' : cook.status === 'active' ? 'herb' : ''}">${statusLabel(cook.status)}</span>
            <span class="muted small">Cook since ${fmt.date(cook.memberSince)}</span></div>
        </div>
      </div>`;
    const body = mode === 'edit' ? this.editForm(cook) : html`
      <dl>
        <dt>Given name</dt><dd>${cook.givenName}</dd>
        <dt>Name</dt><dd>${cook.name}</dd>
        <dt>Email</dt><dd>${cook.email}</dd>
      </dl>
      ${notice ? html`<p class="saved" role="status">${notice}</p>` : ''}
      <div class="actions"><button class="btn btn-primary" data-action="edit">Edit profile</button></div>`;
    this.render(html`
      <div class="stack">
        <section class="card stack" aria-label="Your profile">${header}${body}</section>
        ${mode === 'view' ? this.dangerZone(confirming) : ''}
      </div>`);
    if (mode === 'edit') this.$('input[name=givenName]')?.focus();
    if (confirming) this.$('[name=understood]')?.focus();
  }

  editForm(cook) {
    return html`
      <form class="stack" data-form="edit" novalidate>
        <div class="fields">
          <label>Given name
            <input name="givenName" required maxlength="100" autocomplete="given-name" value="${cook.givenName}"></label>
          <label>Name
            <input name="name" required maxlength="100" autocomplete="family-name" value="${cook.name}"></label>
          <label class="wide">Email
            <input name="email" type="email" required maxlength="254" autocomplete="email" inputmode="email" value="${cook.email}"></label>
        </div>
        <fieldset>
          <legend>Status</legend>
          <div class="statuses">
            ${STATUSES.map(([value, label, hint]) => html`
              <label class="inline"><input type="radio" name="status" value="${value}" ${cook.status === value ? 'checked' : ''}>
                <span><strong>${label}</strong><span class="hint">${hint}</span></span></label>`)}
          </div>
        </fieldset>
        <div id="error" aria-live="polite"></div>
        <div class="actions">
          <button class="btn" type="button" data-action="cancel">Cancel</button>
          <button class="btn btn-primary" type="submit">Save changes</button>
        </div>
      </form>`;
  }

  dangerZone(confirming) {
    if (!confirming) {
      return html`
        <section class="card stack danger" aria-label="Leave Larder">
          <h2>Leave Larder</h2>
          <p class="muted">Deregistering deletes your cook profile for good.</p>
          <div><button class="btn btn-danger" data-action="deregister">Deregister…</button></div>
        </section>`;
    }
    return html`
      <section class="card stack danger" aria-label="Leave Larder">
        <h2>Leave Larder?</h2>
        <div class="confirm stack">
          <p>Your cook profile <strong>${fullName(this.cook)}</strong> (${this.cook.email}) will be deleted. This cannot be undone,
             and you will be signed out right away.</p>
          <p class="muted small">Consents you gave are kept by Larder as evidence of what you agreed to. Revoke them on this page
             before you go if you no longer want them to apply.</p>
          <label class="inline"><input type="checkbox" name="understood"> I understand that my profile is deleted for good.</label>
          <div id="deregister-error" aria-live="polite"></div>
          <div class="actions">
            <button class="btn" data-action="keep">Keep my profile</button>
            <button class="btn btn-danger solid" data-action="confirm-deregister" disabled>Delete my profile</button>
          </div>
        </div>
      </section>`;
  }

  async save(form) {
    const slot = this.$('#error');
    slot.innerHTML = '';
    if (!form.reportValidity()) return;
    const values = trimmed(form);
    if (!values.givenName || !values.name || !values.email) {
      slot.innerHTML = html`<div class="error" role="alert">Given name, name and email must not be empty.</div>`.__html;
      return;
    }
    // PATCH only what changed (the contract needs at least one field).
    const changes = {};
    for (const field of ['email', 'name', 'givenName', 'status']) {
      if (values[field] !== undefined && values[field] !== this.cook[field]) changes[field] = values[field];
    }
    if (!Object.keys(changes).length) {
      this.show('view', { notice: 'Nothing changed.' });
      return;
    }
    const buttons = form.querySelectorAll('button');
    const submit = form.querySelector('button[type=submit]');
    buttons.forEach((button) => { button.disabled = true; });
    submit.textContent = 'Saving…';
    try {
      await this.api(CONTEXT, `/cooks/${encodeURIComponent(this.cookId)}`, { method: 'PATCH', body: changes });
      await this.load('Your profile is saved.');
    } catch (error) {
      slot.innerHTML = formError(error).__html;
      buttons.forEach((button) => { button.disabled = false; });
      submit.textContent = 'Save changes';
    }
  }

  async deregister(button) {
    if (!this.$('[name=understood]')?.checked) return;
    const slot = this.$('#deregister-error');
    const keep = this.$('[data-action=keep]');
    button.disabled = true;
    keep.disabled = true;
    button.textContent = 'Deleting…';
    slot.innerHTML = '';
    try {
      await this.api(CONTEXT, `/cooks/${encodeURIComponent(this.cookId)}`, { method: 'DELETE' });
      this.render(html`<div class="card stack"><h2>Goodbye, and thanks for cooking with us</h2>
        <p class="muted loading">Signing you out…</p></div>`);
      this.emit('larder:logout');
    } catch (error) {
      slot.innerHTML = formError(error).__html;
      button.disabled = false;
      keep.disabled = false;
      button.textContent = 'Delete my profile';
    }
  }
}

define('larder-cook-profile-gate', CookProfileGate);
define('larder-cook-profile-profile', CookProfileProfile);
