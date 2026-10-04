/**
 * Micro-UI of the Sharing context (MICRO-UI.md, section Sharing).
 *
 *   <larder-sharing-thanks-form help="..." helper-type="..." helper-cook="..." picture="...">
 *   <larder-sharing-feed>
 *
 * Giving thanks is checked by Sharing (ThanksService) in this order: recipients/text/picture link,
 * the help exists and the caller asked for it, one thanks per help, the recipients are who helped,
 * the picture exists, the giver's photo consent, the mentioned cooks' consent. Pictures are shown
 * and taken with Media's documented elements (larder-media-image, larder-media-capture).
 */
import { ApiError, LarderElement, define, errorMessage, fmt, html } from '/app-shell/kit.js';

const CONTEXT = 'sharing';
const MAX_TEXT = 2000;
const MAX_CHEF_NAME = 200;
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/** The last path segment of a link if it is a UUID (thanksLink, Location). */
function idOf(link) {
  if (!link) return null;
  try {
    const last = new URL(link, location.origin).pathname.replace(/\/+$/, '').split('/').pop();
    return UUID.test(last) ? last : null;
  } catch {
    return null;
  }
}

/** How each helper type of Cooking Assistance is thanked. */
const HELPERS = {
  GRANDMA_AVATAR: { who: 'Grandma Avatar helped you', icon: '👵' },
  CHEF: { who: 'A chef helped you', icon: '👩‍🍳' },
  COMMUNITY: { who: 'A fellow cook helped you', icon: '🧑‍🍳' },
};

// ---------------------------------------------------------------- <larder-sharing-thanks-form>

class ThanksForm extends LarderElement {
  static observedAttributes = ['help', 'helper-type', 'helper-cook', 'picture'];

  static styles = `
    .head { display: flex; gap: 12px; align-items: center; }
    .head .icon { font-size: 2rem; width: 52px; height: 52px; border-radius: 50%; display: grid; place-items: center;
                  background: var(--larder-surface-muted); flex: none; }
    .counter { align-self: flex-end; font-weight: 400; }
    .picture-wrap { display: flex; flex-direction: column; gap: 8px; }
    .consent { border-left: 4px solid var(--larder-warn); }
    .done { text-align: center; padding: 32px 16px; }
    .done .big { font-size: 2.6rem; }
    .actions { justify-content: flex-end; }
    @media (max-width: 480px) { .actions .btn { flex: 1 1 100%; } }
  `;

  constructor() {
    super();
    this.text = '';
    this.chefName = '';
    this.mention = true; // name the helping cook (COMMUNITY)
    this.newPicture = false; // replace the given `picture` by a new one
    this.saving = false;
    this.problem = null; // { kind, message, code }
    this.sent = null; // thanksId once given
    this.lastBody = null; // request of the last attempt, for retry()
    this.capture = null; // embedded <larder-media-capture>, created once so it keeps its picture

    this.on('input', 'textarea[name=thanksText]', (event, area) => {
      this.text = area.value;
      const counter = this.$('#counter');
      if (counter) counter.textContent = `${area.value.length} / ${MAX_TEXT}`;
    });
    this.on('input', 'input[name=chefName]', (event, input) => { this.chefName = input.value; });
    this.on('change', 'input[name=mention]', (event, input) => { this.mention = input.checked; });
    this.on('submit', 'form', (event) => {
      event.preventDefault();
      this.submit();
    });
    this.on('click', '[data-action=new-picture]', () => {
      this.newPicture = true;
      this.update();
    });
    this.on('click', '[data-action=keep-picture]', () => {
      this.newPicture = false;
      this.update();
    });
    this.on('click', '[data-action=ask-consent]', () => this.askConsent());
    this.on('click', '[data-action=without-names]', () => {
      this.mention = false;
      this.send({ ...this.lastBody, recipients: this.recipients() });
    });
    this.on('click', '[data-action=to-feed]', () => this.navigate('#/thanks'));
  }

  connectedCallback() {
    this.update();
  }

  attributeChangedCallback(name, before, after) {
    if (before !== after && this.isConnected && !this.saving) this.update();
  }

  get helperType() {
    return this.getAttribute('helper-type');
  }

  get helperCook() {
    const cook = this.getAttribute('helper-cook');
    return cook && UUID.test(cook) ? cook : null;
  }

  get givenPicture() {
    return this.newPicture ? null : this.getAttribute('picture') || null;
  }

  /** The recipients Sharing accepts for this helper (Help.verifyAddressedToHelper, Recipient). */
  recipients() {
    switch (this.helperType) {
      case 'GRANDMA_AVATAR':
        return [{ type: 'GrandmaAvatar' }];
      case 'CHEF': {
        const name = this.chefName.trim();
        return [name ? { type: 'Chef', chefName: name } : { type: 'Chef' }];
      }
      case 'COMMUNITY':
        return this.mention && this.helperCook ? [{ type: 'Cook', cooks: [this.helperCook] }] : [];
      default:
        return [];
    }
  }

  /** The embedded capture element, if Media's micro-UI is available. */
  captureElement() {
    if (!customElements.get('larder-media-capture')) return null;
    if (!this.capture) {
      this.capture = document.createElement('larder-media-capture');
      this.capture.setAttribute('purpose', 'Share a picture of your rescued meal - thanks are always shown with one.');
      this.capture.setAttribute('required', ''); // thanks are always shown with a picture: no "Skip"
      // Handled here: the AppShell's rescue flow must not mistake this picture for the help request's.
      this.capture.addEventListener('larder:picture-taken', (event) => {
        event.stopPropagation();
        this.clearProblem('picture');
        this.$('textarea')?.focus();
      });
      this.capture.addEventListener('larder:picture-skipped', (event) => {
        event.stopPropagation();
        this.setProblem('picture', 'Thanks are shared with a picture of your meal. Take one now or choose one from your gallery.');
      });
    }
    return this.capture;
  }

  setProblem(kind, message, code) {
    this.problem = { kind, message, code };
    this.update();
  }

  clearProblem(kind) {
    if (this.problem && (!kind || this.problem.kind === kind)) {
      this.problem = null;
      this.update();
    }
  }

  askConsent() {
    this.emit('larder:consent-required', { purpose: 'photos-in-public-thanks' });
  }

  /** Called by the AppShell once the missing consent was given: send the same thanks again. */
  retry() {
    if (this.lastBody && !this.saving && !this.sent) this.send(this.lastBody);
  }

  async submit() {
    if (this.saving || this.sent) return;
    const helpId = this.getAttribute('help');
    const text = this.text.trim();
    if (!helpId) return this.setProblem('form', 'There is no help to thank for.');
    if (!text) {
      this.setProblem('form', 'Please write a few words of thanks.');
      this.$('textarea')?.focus();
      return undefined;
    }
    if (text.length > MAX_TEXT) return this.setProblem('form', `Your thanks may have at most ${MAX_TEXT} characters.`);
    if (this.chefName.trim().length > MAX_CHEF_NAME) return this.setProblem('form', `A chef's name has at most ${MAX_CHEF_NAME} characters.`);

    let picture = this.givenPicture;
    if (!picture) {
      const capture = this.captureElement();
      if (!capture) return this.setProblem('picture', 'Pictures cannot be taken right now, and thanks need one. Please try again later.');
      if (!capture.pictureInfo) {
        return this.setProblem('picture', 'Thanks are shared with a picture of your meal. Please take or choose one first.');
      }
      this.saving = true;
      this.problem = null;
      this.update();
      try {
        ({ imageLink: picture } = await capture.upload([]));
      } catch (error) {
        this.saving = false;
        return this.setProblem('picture', `Your picture could not be uploaded: ${errorMessage(error)}`);
      }
    }
    return this.send({ helpId, thanksText: text, pictures: picture, recipients: this.recipients() });
  }

  async send(body) {
    this.lastBody = body;
    this.saving = true;
    this.problem = null;
    this.update();
    try {
      const response = await this.api(CONTEXT, '/thanks', { method: 'POST', body });
      const thanksId = idOf(response.location) || idOf(response.body?.thanksLink);
      this.saving = false;
      this.sent = thanksId || true;
      this.update();
      this.emit('larder:thanks-given', { thanksId });
    } catch (error) {
      this.saving = false;
      this.handle(error);
    }
  }

  handle(error) {
    const code = error instanceof ApiError ? error.code : null;
    switch (code) {
      case 'PICTURE_WITHOUT_CONSENT':
        this.setProblem('photo-consent', 'Thanks are public and always show a picture, so Larder needs your consent '
          + 'to show your photos in public thanks.', code);
        this.askConsent();
        return;
      case 'MENTION_WITHOUT_CONSENT':
        this.setProblem('mention-consent', 'The cook who helped you has not agreed to be named in thanks. '
          + 'You can still thank them - without their name.', code);
        return;
      case 'THANKS_ALREADY_GIVEN':
        this.setProblem('already', 'You have already said thanks for this help. You can find your thanks in the feed.', code);
        return;
      case 'RECIPIENT_NOT_HELPER':
        this.setProblem('form', 'These thanks are not addressed to who actually helped you. Please check who you thank.', code);
        return;
      case 'UNKNOWN_HELP':
        this.setProblem('form', 'This help cannot be found (anymore), so there is nothing to thank for.', code);
        return;
      case 'UNKNOWN_PICTURE':
      case 'INVALID_PICTURE':
        if (this.getAttribute('picture') && !this.newPicture) this.newPicture = true;
        else this.capture?.reset?.(); // the uploaded picture is not usable: choose a new one
        this.setProblem('picture', 'The picture could not be found. Please take or choose another one.', code);
        return;
      case 'NOT_PERMITTED':
        this.setProblem('form', 'Only the cook who asked for this help can thank for it.', code);
        return;
      default:
        this.setProblem('form', errorMessage(error), code);
    }
  }

  problemBlock() {
    const problem = this.problem;
    if (!problem) return '';
    const code = problem.code ? html`<span class="badge">${problem.code}</span>` : '';
    switch (problem.kind) {
      case 'photo-consent':
        return html`<div class="notice consent stack" role="alert">
          <p>${problem.message} ${code}</p>
          <div class="row"><button class="btn" type="button" data-action="ask-consent">Give my consent</button></div></div>`;
      case 'mention-consent':
        return html`<div class="notice consent stack" role="alert">
          <p>${problem.message} ${code}</p>
          <div class="row"><button class="btn btn-primary" type="button" data-action="without-names"
            ${this.saving ? 'disabled' : ''}>Thank without naming them</button></div></div>`;
      case 'already':
        return html`<div class="notice stack" role="alert"><p>${problem.message} ${code}</p>
          <div class="row"><button class="btn" type="button" data-action="to-feed">See the thanks</button></div></div>`;
      default:
        return html`<p class="error" role="alert">${problem.message} ${code}</p>`;
    }
  }

  recipientBlock() {
    switch (this.helperType) {
      case 'GRANDMA_AVATAR':
        return html`<p class="notice">Your thanks go to <strong>Grandma Avatar</strong>.</p>`;
      case 'CHEF':
        return html`<label>Chef's name <span class="small muted">(optional)</span>
          <input name="chefName" maxlength="${MAX_CHEF_NAME}" autocomplete="off" value="${this.chefName}"
            placeholder="e.g. Chef Antoine"></label>`;
      case 'COMMUNITY':
        return this.helperCook
          ? html`<label class="inline"><input type="checkbox" name="mention" ${this.mention ? 'checked' : ''}>
              Mention the cook who helped me by name</label>`
          : html`<p class="notice small">The cook who helped you will be thanked without being named.</p>`;
      default:
        return html`<p class="notice small">Your thanks go to the community.</p>`;
    }
  }

  pictureBlock() {
    const given = this.givenPicture;
    if (given) {
      return html`<div class="picture-wrap">
        <larder-media-image link="${given}"></larder-media-image>
        ${customElements.get('larder-media-capture')
          ? html`<div><button class="btn" type="button" data-action="new-picture" ${this.saving ? 'disabled' : ''}>Use another picture</button></div>`
          : ''}
      </div>`;
    }
    if (!customElements.get('larder-media-capture')) {
      return html`<p class="notice">Pictures cannot be taken right now - and thanks need one. Please try again later.</p>`;
    }
    return html`<div class="picture-wrap"><div id="capture"></div>
      ${this.getAttribute('picture')
        ? html`<div><button class="btn" type="button" data-action="keep-picture" ${this.saving ? 'disabled' : ''}>Keep the earlier picture</button></div>`
        : ''}</div>`;
  }

  update() {
    if (this.sent) {
      this.render(html`<div class="card done stack" role="status">
        <div class="big" aria-hidden="true">💛</div>
        <h2>Thanks sent</h2>
        <p class="muted">Your thanks and your picture are now visible to the community.</p>
      </div>`);
      return;
    }
    if (!this.getAttribute('help')) {
      this.render(html`<div class="card empty">There is no help to thank for.</div>`);
      return;
    }
    const helper = HELPERS[this.helperType] || { who: 'Someone helped you', icon: '💛' };
    this.render(html`
      <form class="card stack" novalidate>
        <div class="head"><span class="icon" aria-hidden="true">${helper.icon}</span>
          <div><h2>Say thanks</h2><p class="muted">${helper.who} - let them and the community know.</p></div></div>
        <fieldset class="stack"><legend>Who you thank</legend>${this.recipientBlock()}</fieldset>
        <label>Your thanks
          <textarea name="thanksText" required maxlength="${MAX_TEXT}"
            placeholder="Thank you! The sauce came back to life…">${this.text}</textarea>
          <span class="small muted counter" id="counter" aria-live="polite">${this.text.length} / ${MAX_TEXT}</span>
        </label>
        <fieldset class="stack"><legend>Picture of your meal</legend>${this.pictureBlock()}</fieldset>
        ${this.problemBlock()}
        <div class="row actions">
          <button class="btn btn-primary btn-big" type="submit" ${this.saving ? 'disabled' : ''}>
            ${this.saving ? 'Sending…' : 'Send thanks'}</button>
        </div>
      </form>`);
    const slot = this.$('#capture');
    const capture = slot && this.captureElement();
    if (capture) slot.replaceWith(capture);
  }
}
define('larder-sharing-thanks-form', ThanksForm);

// ---------------------------------------------------------------- <larder-sharing-feed>

const FILTERS = [
  ['all', 'Everyone'],
  ['given', 'Given by me'],
  ['received', 'To me'],
];

class ThanksFeed extends LarderElement {
  static styles = `
    .filters { display: inline-flex; gap: 4px; padding: 4px; border-radius: 999px; background: var(--larder-surface-muted); flex-wrap: wrap; }
    .filters .btn { border-color: transparent; background: transparent; padding: 6px 14px; }
    .filters .btn[aria-pressed=true] { background: var(--larder-surface); border-color: var(--larder-border); box-shadow: var(--larder-shadow); }
    .thanks { padding: 0; overflow: hidden; display: flex; flex-direction: column; }
    .thanks larder-media-image { border-bottom: 1px solid var(--larder-border); }
    .body { padding: 16px 18px 18px; display: flex; flex-direction: column; gap: 10px; flex: 1; }
    blockquote { margin: 0; font-family: var(--larder-font-serif); font-size: 1.08rem; line-height: 1.45;
                 white-space: pre-line; overflow-wrap: anywhere; }
    blockquote::before { content: '“'; color: var(--larder-accent); font-size: 1.6rem; line-height: 0; margin-right: 2px; }
    .meta { margin-top: auto; }
    .chips { display: flex; gap: 6px; flex-wrap: wrap; }
    .btn-small { padding: 5px 12px; font-size: .85rem; }
  `;

  constructor() {
    super();
    this.filter = 'all';
    this.thanks = null;
    this.error = null;
    this.notice = null;
    this.confirming = null; // thanksId whose delete waits for confirmation
    this.deleting = null;
    this.on('click', '[data-filter]', (event, button) => {
      if (button.dataset.filter !== this.filter) {
        this.filter = button.dataset.filter;
        this.load();
      }
    });
    this.on('click', '[data-action=delete]', (event, button) => {
      this.confirming = button.dataset.id;
      this.notice = null;
      this.update();
      this.$(`[data-action=confirm-delete][data-id="${button.dataset.id}"]`)?.focus();
    });
    this.on('click', '[data-action=cancel-delete]', () => {
      this.confirming = null;
      this.update();
    });
    this.on('click', '[data-action=confirm-delete]', (event, button) => this.remove(button.dataset.id));
    this.on('click', '[data-action=help]', (event, button) => this.navigate(`#/helps/${button.dataset.help}`));
    this.on('click', '[data-action=reload]', () => this.load());
  }

  connectedCallback() {
    this.load();
  }

  async load() {
    const query = this.filter === 'given' ? { giver: this.cookId } : this.filter === 'received' ? { recipient: this.cookId } : {};
    const filter = this.filter;
    this.thanks = null;
    this.error = null;
    this.confirming = null;
    this.update();
    try {
      const { body } = await this.api(CONTEXT, '/thanks', { query });
      if (filter !== this.filter) return;
      this.thanks = [...(body?.thanks || [])].sort((a, b) => String(b.createdAt || '').localeCompare(String(a.createdAt || '')));
    } catch (error) {
      if (filter !== this.filter) return;
      this.error = errorMessage(error);
    }
    this.update();
  }

  async remove(thanksId) {
    this.deleting = thanksId;
    this.update();
    try {
      await this.api(CONTEXT, `/thanks/${encodeURIComponent(thanksId)}`, { method: 'DELETE' });
      this.thanks = this.thanks.filter((thanks) => thanks.thanksId !== thanksId);
      this.notice = 'Your thanks were withdrawn.';
    } catch (error) {
      this.notice = null;
      this.error = error instanceof ApiError && error.status === 404 ? null : errorMessage(error);
      if (error instanceof ApiError && error.status === 404) {
        this.thanks = this.thanks.filter((thanks) => thanks.thanksId !== thanksId);
      }
    }
    this.deleting = null;
    this.confirming = null;
    this.update();
  }

  recipientChips(thanks) {
    const recipients = thanks.recipients || [];
    if (!recipients.length) return html`<span class="badge">the community</span>`;
    return recipients.map((recipient) => {
      if (recipient.type === 'GrandmaAvatar') return html`<span class="badge accent">👵 Grandma Avatar</span>`;
      if (recipient.type === 'Chef') return html`<span class="badge accent">👩‍🍳 ${recipient.chefName ? `Chef ${recipient.chefName}` : 'a chef'}</span>`;
      const cooks = recipient.cooks || [];
      const me = cooks.includes(this.cookId);
      const others = cooks.length - (me ? 1 : 0);
      return html`${me ? html`<span class="badge herb">you</span>` : ''}${others > 0
        ? html`<span class="badge">${others === 1 ? 'a fellow cook' : `${others} fellow cooks`}</span>` : ''}`;
    });
  }

  card(thanks) {
    const own = thanks.giver === this.cookId;
    const confirming = this.confirming === thanks.thanksId;
    const deleting = this.deleting === thanks.thanksId;
    return html`
      <article class="card thanks">
        <larder-media-image link="${thanks.pictures}"></larder-media-image>
        <div class="body">
          <blockquote>${thanks.thanksText}</blockquote>
          <div class="chips small"><span class="muted">${own ? 'You' : 'A fellow cook'} thanked</span>${this.recipientChips(thanks)}</div>
          <div class="spread meta">
            <span class="small muted">${fmt.date(thanks.createdAt)}${thanks.updatedAt && thanks.updatedAt !== thanks.createdAt ? ' · edited' : ''}</span>
            <div class="row">
              <button class="btn btn-small" type="button" data-action="help" data-help="${thanks.helpId}">See the help</button>
              ${own && !confirming ? html`<button class="btn btn-danger btn-small" type="button" data-action="delete"
                data-id="${thanks.thanksId}">Delete</button>` : ''}
            </div>
          </div>
          ${confirming ? html`<div class="notice row" role="alert"><span>Withdraw these thanks? This cannot be undone.</span>
            <button class="btn btn-danger btn-small" type="button" data-action="confirm-delete" data-id="${thanks.thanksId}"
              ${deleting ? 'disabled' : ''}>${deleting ? 'Deleting…' : 'Yes, delete'}</button>
            <button class="btn btn-small" type="button" data-action="cancel-delete" ${deleting ? 'disabled' : ''}>Keep</button></div>` : ''}
        </div>
      </article>`;
  }

  update() {
    const empty = {
      all: 'No thanks yet. When a cook is rescued, their thanks show up here.',
      given: 'You have not thanked anyone yet. After you got help, say thanks - it makes someone’s day.',
      received: 'No thanks addressed to you yet. Help someone on the help board!',
    }[this.filter];
    let content;
    if (this.error && !this.thanks) {
      content = html`<div class="error stack" role="alert"><span>${this.error}</span>
        <div><button class="btn" type="button" data-action="reload">Try again</button></div></div>`;
    } else if (!this.thanks) {
      content = html`<p class="loading" aria-busy="true">Gathering thanks…</p>`;
    } else if (!this.thanks.length) {
      content = html`<div class="card empty">${empty}</div>`;
    } else {
      content = html`<div class="grid">${this.thanks.map((thanks) => this.card(thanks))}</div>`;
    }
    this.render(html`
      <section class="stack">
        <div class="spread">
          <div><h1>Thanks</h1><p class="muted">Rescued meals and the people who helped.</p></div>
          <div class="filters" role="group" aria-label="Show thanks">
            ${FILTERS.map(([key, label]) => html`<button class="btn" type="button" data-filter="${key}"
              aria-pressed="${key === this.filter ? 'true' : 'false'}">${label}</button>`)}
          </div>
        </div>
        ${this.notice ? html`<p class="notice" role="status">${this.notice}</p>` : ''}
        ${this.error && this.thanks ? html`<p class="error" role="alert">${this.error}</p>` : ''}
        ${content}
      </section>`);
  }
}
define('larder-sharing-feed', ThanksFeed);
