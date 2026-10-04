/**
 * Micro-UI of the Notification context (MICRO-UI.md, section Notification).
 *
 *   <larder-notification-bell>   badge with the NEW notifications (polls), dropdown with the latest
 *   <larder-notification-list>   all notifications: open, mark read / unread, delete
 *
 * API (prefix /notifications, so the shell calls /notifications/notifications...):
 *   GET    /notifications?status=NEW|READ       (receiver defaults to the signed-in cook)
 *   PUT    /notifications/{id}/status {status}  → 200 {notificationLink}
 *   DELETE /notifications/{id}                  → 204
 * The list is sorted newest first by the server; notifications carry no timestamp in the contract.
 */
import { LarderElement, define, errorMessage, html } from '/app-shell/kit.js';

const CONTEXT = 'notifications';
const POLL_MS = 10_000;
const LATEST = 6;

/** Both elements of this context tell each other when notifications changed. */
const changes = new EventTarget();
const changed = () => changes.dispatchEvent(new Event('changed'));

const list = (element, status) => element.api(CONTEXT, '/notifications', { query: status ? { status } : {} })
  .then(({ body }) => body?.notifications || []);
const setStatus = (element, id, status) => element.api(CONTEXT, `/notifications/${encodeURIComponent(id)}/status`,
  { method: 'PUT', body: { status } });
const remove = (element, id) => element.api(CONTEXT, `/notifications/${encodeURIComponent(id)}`, { method: 'DELETE' });

const BELL = html`<svg aria-hidden="true" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
  stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>
  <path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>`;

// ---------------------------------------------------------------- <larder-notification-bell>

class NotificationBell extends LarderElement {
  static styles = `
    :host { display: inline-block; position: relative; }
    .bell { position: relative; padding: 8px 11px; display: inline-flex; align-items: center; }
    .count { position: absolute; top: -4px; right: -4px; min-width: 20px; height: 20px; padding: 0 5px; border-radius: 999px;
             background: var(--larder-accent); color: var(--larder-accent-text); font-size: .72rem; font-weight: 700;
             display: grid; place-items: center; border: 2px solid var(--larder-bg); }
    .count[hidden] { display: none; }
    .panel { position: absolute; right: 0; top: calc(100% + 8px); z-index: 20; width: min(360px, calc(100vw - 32px));
             background: var(--larder-surface); border: 1px solid var(--larder-border); border-radius: var(--larder-radius);
             box-shadow: var(--larder-shadow); padding: 8px; }
    .panel[hidden] { display: none; }
    .panel-head { display: flex; justify-content: space-between; align-items: center; padding: 6px 8px 8px; }
    .panel-head h2 { font-size: 1.05rem; margin: 0; }
    .item { display: block; width: 100%; text-align: left; font: inherit; color: inherit; background: transparent; border: 0;
            border-radius: var(--larder-radius-small); padding: 10px 10px 10px 22px; cursor: pointer; position: relative; }
    .item:hover, .item:focus-visible { background: var(--larder-surface-muted); }
    .item:focus-visible, .link:focus-visible { outline: 3px solid var(--larder-focus); outline-offset: 1px; }
    .item.new::before { content: ''; position: absolute; left: 8px; top: 17px; width: 8px; height: 8px; border-radius: 50%;
                        background: var(--larder-accent); }
    .item strong { display: block; }
    .item.read strong { font-weight: 500; }
    .item .text { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
    .foot { border-top: 1px solid var(--larder-border); margin-top: 6px; padding: 8px 4px 2px; display: flex; justify-content: space-between; gap: 8px; flex-wrap: wrap; }
    .link { font: inherit; font-weight: 600; background: none; border: 0; color: var(--larder-accent-strong); cursor: pointer;
            padding: 6px 8px; border-radius: var(--larder-radius-small); }
    .link:hover { background: var(--larder-surface-muted); }
    .state { padding: 16px 10px; }
    .sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); white-space: nowrap; }
  `;

  constructor() {
    super();
    this.count = 0;
    this.latest = null;
    this.error = null;
    this.open = false;
    this.onOutside = (event) => {
      if (this.open && !event.composedPath().includes(this)) this.close(false);
    };
    this.onVisible = () => {
      if (!document.hidden) this.poll();
    };
    this.onChanged = () => {
      this.poll();
      if (this.open) this.loadLatest();
    };
    this.on('click', '.bell', () => (this.open ? this.close(true) : this.show()));
    this.on('click', '[data-id]', (event, item) => this.follow(item.dataset.id));
    this.on('click', '[data-action=all]', () => {
      this.close(false);
      this.navigate('#/notifications');
    });
    this.on('click', '[data-action=read-all]', () => this.readAll());
    this.on('keydown', '*', (event) => this.key(event));
  }

  connectedCallback() {
    this.render(html`
      <button class="btn bell" type="button" aria-haspopup="true" aria-expanded="false" aria-controls="panel"
        aria-label="Notifications">${BELL}<span class="count" hidden></span></button>
      <div class="panel" id="panel" role="region" aria-label="Latest notifications" hidden>
        <div class="panel-head"><h2>Notifications</h2><span id="head-action"></span></div>
        <div id="items"></div>
        <div class="foot"><button class="link" type="button" data-action="all">All notifications →</button></div>
      </div>`);
    document.addEventListener('click', this.onOutside, true);
    document.addEventListener('visibilitychange', this.onVisible);
    changes.addEventListener('changed', this.onChanged);
    this.poll();
    this.timer = setInterval(() => {
      if (!document.hidden) this.poll();
    }, POLL_MS);
  }

  disconnectedCallback() {
    clearInterval(this.timer);
    this.timer = null;
    document.removeEventListener('click', this.onOutside, true);
    document.removeEventListener('visibilitychange', this.onVisible);
    changes.removeEventListener('changed', this.onChanged);
  }

  async poll() {
    try {
      const fresh = await list(this, 'NEW');
      this.count = fresh.length;
    } catch (error) {
      console.warn('Notifications could not be loaded', error);
    }
    this.updateBadge();
  }

  updateBadge() {
    const badge = this.$('.count');
    const button = this.$('.bell');
    if (!badge || !button) return;
    badge.hidden = this.count === 0;
    badge.textContent = this.count > 9 ? '9+' : String(this.count);
    button.setAttribute('aria-label', this.count ? `Notifications, ${this.count} new` : 'Notifications, none new');
  }

  async show() {
    this.open = true;
    this.$('.bell').setAttribute('aria-expanded', 'true');
    const panel = this.$('.panel');
    panel.hidden = false;
    panel.style.transform = '';
    const rect = panel.getBoundingClientRect(); // keep the dropdown on narrow screens inside the viewport
    if (rect.left < 16) panel.style.transform = `translateX(${16 - rect.left}px)`;
    await this.loadLatest();
    this.focusables()[0]?.focus();
  }

  close(returnFocus) {
    if (!this.open) return;
    this.open = false;
    this.$('.bell')?.setAttribute('aria-expanded', 'false');
    const panel = this.$('.panel');
    if (panel) panel.hidden = true;
    if (returnFocus) this.$('.bell')?.focus();
  }

  async loadLatest() {
    if (!this.latest) this.renderItems(html`<p class="loading state small">Looking for news…</p>`);
    try {
      this.latest = (await list(this)).slice(0, LATEST);
      this.error = null;
    } catch (error) {
      this.error = errorMessage(error);
    }
    this.renderLatest();
  }

  renderLatest() {
    const head = this.$('#head-action');
    const unread = (this.latest || []).filter((n) => n.status === 'NEW').length;
    if (head) head.innerHTML = unread ? html`<button class="link small" type="button" data-action="read-all">Mark all read</button>`.__html : '';
    if (this.error) {
      this.renderItems(html`<p class="error small" role="alert">${this.error}</p>`);
    } else if (!this.latest?.length) {
      this.renderItems(html`<p class="empty small state">You’re all caught up. 🍲</p>`);
    } else {
      this.renderItems(html`<ul class="plain">${this.latest.map((n) => html`
        <li><button class="item ${n.status === 'NEW' ? 'new' : 'read'}" type="button" data-id="${n.notificationId}">
          <strong>${n.title}</strong>
          <span class="text small muted">${n.text}</span>
          <span class="sr-only">${n.status === 'NEW' ? ' (new)' : ''}</span>
        </button></li>`)}</ul>`);
    }
  }

  renderItems(template) {
    const items = this.$('#items');
    if (items) items.innerHTML = template.__html;
  }

  focusables() {
    return [...this.shadowRoot.querySelectorAll('.panel button')];
  }

  key(event) {
    if (event.key === 'Escape' && this.open) {
      event.preventDefault();
      event.stopPropagation();
      this.close(true);
      return;
    }
    if (!this.open || !['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) return;
    const all = this.focusables();
    if (!all.length) return;
    const index = all.indexOf(this.shadowRoot.activeElement);
    event.preventDefault();
    const next = event.key === 'Home' ? 0 : event.key === 'End' ? all.length - 1
      : event.key === 'ArrowDown' ? (index + 1) % all.length : (index - 1 + all.length) % all.length;
    all[next].focus();
  }

  async follow(id) {
    const notification = this.latest?.find((n) => n.notificationId === id);
    if (!notification) return;
    this.close(false);
    if (notification.status === 'NEW') {
      try {
        await setStatus(this, id, 'READ');
        notification.status = 'READ';
        this.count = Math.max(0, this.count - 1);
        this.updateBadge();
        changed();
      } catch (error) {
        console.warn('Notification could not be marked as read', error);
      }
    }
    this.navigate(notification.link);
  }

  async readAll() {
    const unread = (this.latest || []).filter((n) => n.status === 'NEW');
    await Promise.allSettled(unread.map((n) => setStatus(this, n.notificationId, 'READ')));
    changed();
    await this.loadLatest();
    this.focusables()[0]?.focus();
  }
}
define('larder-notification-bell', NotificationBell);

// ---------------------------------------------------------------- <larder-notification-list>

const FILTERS = [
  ['', 'All'],
  ['NEW', 'New'],
  ['READ', 'Read'],
];

class NotificationList extends LarderElement {
  static styles = `
    .filters { display: inline-flex; gap: 4px; padding: 4px; border-radius: 999px; background: var(--larder-surface-muted); }
    .filters .btn { border-color: transparent; background: transparent; padding: 6px 14px; }
    .filters .btn[aria-pressed=true] { background: var(--larder-surface); border-color: var(--larder-border); box-shadow: var(--larder-shadow); }
    .note { display: flex; flex-direction: column; gap: 10px; }
    .note.new { border-left: 4px solid var(--larder-accent); }
    .note h2 { font-size: 1.15rem; margin: 0; overflow-wrap: anywhere; }
    .note p { margin: 0; white-space: pre-line; overflow-wrap: anywhere; }
    .btn-small { padding: 5px 12px; font-size: .85rem; }
    .actions { justify-content: flex-end; }
  `;

  constructor() {
    super();
    this.status = '';
    this.items = null;
    this.error = null;
    this.busy = null; // id of the notification being changed
    this.confirming = null;
    this.onChanged = () => {
      if (!this.busy) this.load(false);
    };
    this.on('click', '[data-filter]', (event, button) => {
      if (button.dataset.filter !== this.status) {
        this.status = button.dataset.filter;
        this.load(true);
      }
    });
    this.on('click', '[data-action=open]', (event, button) => this.open(button.dataset.id));
    this.on('click', '[data-action=toggle]', (event, button) => this.toggle(button.dataset.id));
    this.on('click', '[data-action=delete]', (event, button) => {
      this.confirming = button.dataset.id;
      this.update();
      this.$(`[data-action=confirm-delete][data-id="${button.dataset.id}"]`)?.focus();
    });
    this.on('click', '[data-action=cancel-delete]', () => {
      this.confirming = null;
      this.update();
    });
    this.on('click', '[data-action=confirm-delete]', (event, button) => this.delete(button.dataset.id));
    this.on('click', '[data-action=reload]', () => this.load(true));
  }

  connectedCallback() {
    changes.addEventListener('changed', this.onChanged);
    this.load(true);
  }

  disconnectedCallback() {
    changes.removeEventListener('changed', this.onChanged);
  }

  find(id) {
    return this.items?.find((n) => n.notificationId === id);
  }

  async load(showLoading) {
    const status = this.status;
    if (showLoading) {
      this.items = null;
      this.error = null;
      this.update();
    }
    try {
      const items = await list(this, status || undefined);
      if (status !== this.status) return;
      this.items = items;
      this.error = null;
    } catch (error) {
      if (status !== this.status) return;
      this.error = errorMessage(error);
    }
    this.update();
  }

  async change(id, work) {
    this.busy = id;
    this.error = null;
    this.update();
    try {
      await work();
      changed();
    } catch (error) {
      this.error = errorMessage(error);
    }
    this.busy = null;
    this.confirming = null;
    await this.load(false);
  }

  open(id) {
    const notification = this.find(id);
    if (!notification) return;
    if (notification.status === 'NEW') {
      setStatus(this, id, 'READ').then(changed, (error) => console.warn('Notification could not be marked as read', error));
    }
    this.navigate(notification.link);
  }

  toggle(id) {
    const notification = this.find(id);
    if (notification) this.change(id, () => setStatus(this, id, notification.status === 'NEW' ? 'READ' : 'NEW'));
  }

  delete(id) {
    this.change(id, () => remove(this, id));
  }

  card(n) {
    const busy = this.busy === n.notificationId;
    const confirming = this.confirming === n.notificationId;
    const isNew = n.status === 'NEW';
    return html`
      <li class="card note ${isNew ? 'new' : ''}">
        <div class="spread"><h2>${n.title}</h2><span class="badge ${isNew ? 'accent' : ''}">${isNew ? 'New' : 'Read'}</span></div>
        <p class="muted">${n.text}</p>
        ${confirming
          ? html`<div class="notice row actions" role="alert"><span>Delete this notification? This cannot be undone.</span>
              <button class="btn btn-danger btn-small" type="button" data-action="confirm-delete" data-id="${n.notificationId}"
                ${busy ? 'disabled' : ''}>${busy ? 'Deleting…' : 'Yes, delete'}</button>
              <button class="btn btn-small" type="button" data-action="cancel-delete" ${busy ? 'disabled' : ''}>Keep</button></div>`
          : html`<div class="row actions">
              <button class="btn btn-small" type="button" data-action="toggle" data-id="${n.notificationId}" ${busy ? 'disabled' : ''}>
                ${isNew ? 'Mark read' : 'Mark unread'}</button>
              <button class="btn btn-danger btn-small" type="button" data-action="delete" data-id="${n.notificationId}"
                ${busy ? 'disabled' : ''}>Delete</button>
              <button class="btn btn-primary btn-small" type="button" data-action="open" data-id="${n.notificationId}">Open</button>
            </div>`}
      </li>`;
  }

  update() {
    const empty = {
      '': 'No notifications yet. When someone helps you, you’ll hear about it here.',
      NEW: 'Nothing new - you’re all caught up.',
      READ: 'No read notifications.',
    }[this.status];
    let content;
    if (this.error && !this.items) {
      content = html`<div class="error stack" role="alert"><span>${this.error}</span>
        <div><button class="btn" type="button" data-action="reload">Try again</button></div></div>`;
    } else if (!this.items) {
      content = html`<p class="loading" aria-busy="true">Loading notifications…</p>`;
    } else if (!this.items.length) {
      content = html`<div class="card empty">${empty}</div>`;
    } else {
      content = html`<ul class="plain stack">${this.items.map((n) => this.card(n))}</ul>`;
    }
    this.render(html`
      <section class="stack">
        <div class="spread">
          <h1>Notifications</h1>
          <div class="filters" role="group" aria-label="Show notifications">
            ${FILTERS.map(([key, label]) => html`<button class="btn" type="button" data-filter="${key}"
              aria-pressed="${key === this.status ? 'true' : 'false'}">${label}</button>`)}
          </div>
        </div>
        ${this.error && this.items ? html`<p class="error" role="alert">${this.error}</p>` : ''}
        ${content}
      </section>`);
  }
}
define('larder-notification-list', NotificationList);
