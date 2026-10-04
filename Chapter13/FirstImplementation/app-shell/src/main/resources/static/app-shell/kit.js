/**
 * Larder UI kit - the common platform of all micro-UIs (AP Micro-UIs).
 *
 * A micro-UI belongs to its Bounded Context and calls only its own context's API through
 * `this.api(...)`. It never sees the access token: the AppShell injects authentication.
 * Cross-context work happens through DOM events (see MICRO-UI.md), which the AppShell
 * orchestrates, or by embedding another context's documented element.
 */

/** The runtime the AppShell provides: { api, cookId, navigate, mediaLink }. */
export const larder = () => {
  if (!window.larder) throw new Error('Larder AppShell is not running');
  return window.larder;
};

/** An API answered with an error; `code` is the contract's Error.code (e.g. UNKNOWN_RECIPE). */
export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || `Request failed with status ${status}`);
    this.status = status;
    this.code = body?.code || (status === 401 ? 'UNAUTHORIZED' : status === 403 ? 'NOT_PERMITTED'
      : status === 404 ? 'NOT_FOUND' : status >= 500 ? 'SERVICE_NOT_AVAILABLE' : 'BAD_REQUEST');
    this.body = body;
  }
}

// ---------------------------------------------------------------- templating

const ESCAPES = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
const escape = (value) => String(value).replace(/[&<>"']/g, (c) => ESCAPES[c]);

/** Trusted markup (e.g. a nested html`` result) that is inserted unescaped. */
export const raw = (markup) => ({ __html: String(markup) });

/**
 * Tagged template that escapes every interpolated value - safe against injected markup.
 * Arrays are joined; null/undefined/false render nothing; nested html`` stays markup.
 */
export function html(strings, ...values) {
  const render = (value) => {
    if (value === null || value === undefined || value === false) return '';
    if (Array.isArray(value)) return value.map(render).join('');
    if (typeof value === 'object' && '__html' in value) return value.__html;
    return escape(value);
  };
  return raw(strings.reduce((out, s, i) => out + s + (i < values.length ? render(values[i]) : ''), ''));
}

// ---------------------------------------------------------------- formatting

export const fmt = {
  date: (iso) => (iso ? new Date(iso).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' }) : ''),
  dateTime: (iso) => (iso ? new Date(iso).toLocaleString(undefined, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : ''),
  /** HELP_REQUEST / MENU_PROPOSAL / GrandmaAvatar → "Help request" / "Menu proposal" / "Grandma avatar" */
  label: (value) => {
    if (!value) return '';
    const words = String(value).replace(/([a-z])([A-Z])/g, '$1 $2').replace(/[_-]+/g, ' ').toLowerCase();
    return words.charAt(0).toUpperCase() + words.slice(1);
  },
};

/** A friendly sentence for an error, for the `error` slot of a component. */
export function errorMessage(error) {
  if (error instanceof ApiError) {
    if (error.status === 403) return "You're not allowed to do that.";
    if (error.status === 404) return "That doesn't exist (anymore).";
    if (error.status >= 500) return 'Larder is having trouble right now. Please try again in a moment.';
    return error.message;
  }
  return error?.message || String(error);
}

/** Values of a form as an object; empty strings are left out, checkboxes with the same name become arrays. */
export function formValues(form) {
  const values = {};
  for (const [name, value] of new FormData(form).entries()) {
    if (value === '') continue;
    const field = form.elements[name];
    const multiple = field instanceof RadioNodeList && field[0]?.type === 'checkbox';
    if (multiple) (values[name] ||= []).push(value);
    else values[name] = value;
  }
  return values;
}

// ---------------------------------------------------------------- base element

const KIT_STYLES = `
  :host { display: block; color: var(--larder-text); font-family: var(--larder-font); }
  :host([hidden]) { display: none; }
  * { box-sizing: border-box; }
  h1, h2, h3 { font-family: var(--larder-font-serif); line-height: 1.2; margin: 0 0 8px; font-weight: 600; }
  h1 { font-size: 1.9rem; } h2 { font-size: 1.4rem; } h3 { font-size: 1.1rem; }
  p { margin: 0 0 8px; }
  a { color: var(--larder-accent-strong); }
  .muted { color: var(--larder-text-muted); }
  .small { font-size: 0.875rem; }
  .stack { display: flex; flex-direction: column; gap: var(--larder-gap); }
  .row { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
  .spread { display: flex; gap: 8px; align-items: center; justify-content: space-between; flex-wrap: wrap; }
  .grid { display: grid; gap: var(--larder-gap); grid-template-columns: repeat(auto-fill, minmax(min(100%, 260px), 1fr)); }
  .card { background: var(--larder-surface); border: 1px solid var(--larder-border); border-radius: var(--larder-radius);
          box-shadow: var(--larder-shadow); padding: 20px; }
  .card.clickable { cursor: pointer; transition: transform .12s ease, box-shadow .12s ease; }
  .card.clickable:hover { transform: translateY(-2px); }
  .btn { font: inherit; font-weight: 600; border-radius: 999px; padding: 9px 18px; cursor: pointer;
         border: 1px solid var(--larder-border); background: var(--larder-surface); color: var(--larder-text); }
  .btn:hover { background: var(--larder-surface-muted); }
  .btn:disabled { opacity: .55; cursor: not-allowed; }
  .btn-primary { background: var(--larder-accent); border-color: var(--larder-accent); color: var(--larder-accent-text); }
  .btn-primary:hover { background: var(--larder-accent-strong); }
  .btn-danger { color: var(--larder-danger); border-color: var(--larder-danger); background: transparent; }
  .btn-big { font-size: 1.15rem; padding: 14px 26px; }
  .btn:focus-visible, input:focus-visible, select:focus-visible, textarea:focus-visible {
    outline: 3px solid var(--larder-focus); outline-offset: 2px; }
  label { display: flex; flex-direction: column; gap: 4px; font-weight: 600; font-size: .9rem; }
  label.inline { flex-direction: row; align-items: center; font-weight: 400; gap: 8px; }
  input, select, textarea { font: inherit; color: var(--larder-text); background: var(--larder-surface);
          border: 1px solid var(--larder-border); border-radius: var(--larder-radius-small); padding: 9px 11px; font-weight: 400; }
  textarea { min-height: 96px; resize: vertical; }
  fieldset { border: 1px solid var(--larder-border); border-radius: var(--larder-radius-small); padding: 12px; margin: 0; }
  legend { font-weight: 600; font-size: .9rem; padding: 0 6px; }
  .badge { display: inline-block; font-size: .78rem; font-weight: 700; padding: 2px 10px; border-radius: 999px;
           background: var(--larder-surface-muted); color: var(--larder-text-muted); }
  .badge.accent { background: var(--larder-accent); color: var(--larder-accent-text); }
  .badge.herb { background: var(--larder-herb); color: #fff; }
  .error { color: var(--larder-danger); background: color-mix(in srgb, var(--larder-danger) 10%, transparent);
           border-radius: var(--larder-radius-small); padding: 10px 12px; }
  .notice { background: var(--larder-surface-muted); border-radius: var(--larder-radius-small); padding: 10px 12px; }
  .empty { color: var(--larder-text-muted); text-align: center; padding: 32px 8px; }
  ul.plain { list-style: none; margin: 0; padding: 0; }
  img.picture { max-width: 100%; border-radius: var(--larder-radius-small); display: block; }
  @keyframes larder-pulse { 50% { opacity: .45; } }
  .loading { animation: larder-pulse 1.4s ease-in-out infinite; color: var(--larder-text-muted); }
`;

const kitSheet = new CSSStyleSheet();
kitSheet.replaceSync(KIT_STYLES);

/**
 * Base class of every micro-UI element: shadow DOM with the kit styles, safe rendering,
 * event delegation, API access through the AppShell, and events that cross the shadow boundary.
 */
export class LarderElement extends HTMLElement {
  /** Component-specific CSS, added after the kit styles. */
  static styles = '';

  constructor() {
    super();
    this.attachShadow({ mode: 'open' });
    const sheets = [kitSheet];
    if (this.constructor.styles) {
      const own = new CSSStyleSheet();
      own.replaceSync(this.constructor.styles);
      sheets.push(own);
    }
    this.shadowRoot.adoptedStyleSheets = sheets;
  }

  /** The id of the signed-in cook (the token's cookId claim). */
  get cookId() {
    return larder().cookId;
  }

  /**
   * Calls an API of a Bounded Context: `this.api('recipe-catalog', '/recipes', { query, method, body })`.
   * Resolves to { status, body, location }; rejects with ApiError for 4xx/5xx.
   * A micro-UI calls ONLY its own context's API.
   */
  api(context, path, options) {
    return larder().api(context, path, options);
  }

  /** Dispatches a `larder:*` event that bubbles out of the shadow DOM to the AppShell. */
  emit(name, detail = {}) {
    this.dispatchEvent(new CustomEvent(name, { detail, bubbles: true, composed: true }));
  }

  /** Asks the AppShell to navigate, e.g. '#/recipes/123' or a contract link from a notification. */
  navigate(href) {
    this.emit('larder:navigate', { href });
  }

  /** Replaces the shadow DOM with an html`` result. */
  render(template) {
    this.shadowRoot.innerHTML = template.__html;
  }

  /** Delegated event listener inside the shadow DOM: this.on('click', '[data-action=save]', (event, target) => ...). */
  on(type, selector, handler) {
    this.shadowRoot.addEventListener(type, (event) => {
      const target = event.target.closest?.(selector);
      if (target && this.shadowRoot.contains(target)) handler(event, target);
    });
  }

  /** Shorthand for querySelector inside the shadow DOM. */
  $(selector) {
    return this.shadowRoot.querySelector(selector);
  }
}

/** Registers an element once (micro-UI modules may be loaded twice during development). */
export function define(tag, elementClass) {
  if (!customElements.get(tag)) customElements.define(tag, elementClass);
}
