/**
 * Micro-UI of the Media context (MICRO-UI.md, section Media).
 *
 *   <larder-media-capture purpose="...">   take or choose a photo; uploads only when asked: upload(links)
 *   <larder-media-image link="..." | media="...">   shows one image of Media (loaded through the API)
 *
 * Media's rules (contracts/CHANGES.md, ImageContent, Link): an image is at most 5 MiB, a JPEG, PNG, GIF
 * or WebP recognised by its leading bytes; at most 20 links, each an absolute http(s) url whose last
 * path segment is the UUID of the business object.
 */
import { ApiError, LarderElement, define, errorMessage, html } from '/app-shell/kit.js';

const CONTEXT = 'media';
const MAX_BYTES = 5 * 1024 * 1024;
const MAX_LINKS = 20;
const MAX_URL_LENGTH = 2048;
const LINK_TYPES = ['recipe', 'thanks', 'helpRequest', 'help'];
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const BASE64 = /^[A-Za-z0-9+/]+={0,2}$/;

// ---------------------------------------------------------------- helpers

/** Content type from the leading bytes, exactly as Media's ImageFormat.detect does; null if not accepted. */
function detectContentType(bytes) {
  const startsWith = (offset, ...expected) => bytes.length >= offset + expected.length
    && expected.every((value, i) => bytes[offset + i] === (typeof value === 'string' ? value.charCodeAt(0) : value));
  if (startsWith(0, 0xff, 0xd8, 0xff)) return 'image/jpeg';
  if (startsWith(0, 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a)) return 'image/png';
  if (startsWith(0, 'G', 'I', 'F', '8', '7', 'a') || startsWith(0, 'G', 'I', 'F', '8', '9', 'a')) return 'image/gif';
  if (startsWith(0, 'R', 'I', 'F', 'F') && startsWith(8, 'W', 'E', 'B', 'P')) return 'image/webp';
  return null;
}

/** Standard base64 (RFC 4648, no data: prefix, no line breaks) of some bytes. */
function toBase64(bytes) {
  let binary = '';
  for (let i = 0; i < bytes.length; i += 0x8000) {
    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
  }
  return btoa(binary);
}

/** The leading bytes of a base64 text (enough to detect the format). */
function leadingBytes(base64, count = 12) {
  const binary = atob(base64.slice(0, Math.ceil(count / 3) * 4));
  return Uint8Array.from(binary, (c) => c.charCodeAt(0));
}

/** Decoded size of a base64 text in bytes. */
const decodedSize = (base64) => Math.floor((base64.length * 3) / 4) - (base64.endsWith('==') ? 2 : base64.endsWith('=') ? 1 : 0);

const megabytes = (bytes) => `${(bytes / (1024 * 1024)).toFixed(1)} MB`;

/** The media id of a contract image link (its last path segment), or null. */
export function mediaIdOf(link) {
  if (!link) return null;
  let path;
  try {
    path = new URL(link, location.origin).pathname;
  } catch {
    return null;
  }
  const last = path.replace(/\/+$/, '').split('/').pop();
  return UUID.test(last) ? last : null;
}

/** Checks the links before they reach Media; throws an Error explaining what is wrong. */
function checkLinks(links) {
  if (links === undefined || links === null) return [];
  if (!Array.isArray(links)) throw new Error('The links of a picture must be a list.');
  if (links.length > MAX_LINKS) throw new Error(`A picture can be linked to at most ${MAX_LINKS} things.`);
  return links.map((link) => {
    if (!link || !LINK_TYPES.includes(link.type)) {
      throw new Error(`A picture link needs a type (${LINK_TYPES.join(', ')}).`);
    }
    let url;
    try {
      url = new URL(link.url);
    } catch {
      throw new Error(`The picture link "${link.url}" is not an absolute address.`);
    }
    if (!['http:', 'https:'].includes(url.protocol) || String(link.url).length > MAX_URL_LENGTH) {
      throw new Error(`The picture link "${link.url}" must be an http(s) address of at most ${MAX_URL_LENGTH} characters.`);
    }
    if (!UUID.test(url.pathname.replace(/\/+$/, '').split('/').pop())) {
      throw new Error(`The picture link "${link.url}" must end with the id of what it belongs to.`);
    }
    return { type: link.type, url: String(link.url) };
  });
}

/** Re-encodes a photo the browser can show as a JPEG that fits Media's limit; null if that is impossible. */
async function shrink(file) {
  if (typeof createImageBitmap !== 'function') return null;
  let bitmap;
  try {
    bitmap = await createImageBitmap(file);
  } catch {
    return null; // the browser cannot decode it either (e.g. HEIC on desktop browsers)
  }
  try {
    for (const [edge, quality] of [[2560, 0.85], [1920, 0.8], [1280, 0.75]]) {
      const scale = Math.min(1, edge / Math.max(bitmap.width, bitmap.height));
      const canvas = document.createElement('canvas');
      canvas.width = Math.round(bitmap.width * scale);
      canvas.height = Math.round(bitmap.height * scale);
      canvas.getContext('2d').drawImage(bitmap, 0, 0, canvas.width, canvas.height);
      const blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', quality));
      if (blob && blob.size <= MAX_BYTES) return new Uint8Array(await blob.arrayBuffer());
    }
    return null;
  } finally {
    bitmap.close?.();
  }
}

// ---------------------------------------------------------------- <larder-media-capture>

const ICON_CAMERA = html`<svg aria-hidden="true" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor"
  stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 8h3l2-3h6l2 3h3a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1z"/>
  <circle cx="12" cy="13" r="3.6"/></svg>`;

/**
 * Take or choose a photo, check it against Media's rules, preview it. The picture is kept in memory
 * until `upload(links)` is called (by the AppShell or an embedding element).
 */
class MediaCapture extends LarderElement {
  static observedAttributes = ['purpose', 'required'];

  static styles = `
    .frame { border: 2px dashed var(--larder-border); border-radius: var(--larder-radius); background: var(--larder-surface-muted);
             padding: 28px 16px; text-align: center; display: flex; flex-direction: column; align-items: center; gap: 12px; }
    .frame svg { color: var(--larder-accent); }
    .pick { position: relative; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; }
    .pick input { position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; height: 100%; }
    .pick:focus-within { outline: 3px solid var(--larder-focus); outline-offset: 2px; }
    .preview { position: relative; }
    .preview img { width: 100%; max-height: 420px; object-fit: contain; background: var(--larder-surface-muted);
                   border-radius: var(--larder-radius-small); display: block; }
    .preview .badge { position: absolute; top: 10px; left: 10px; box-shadow: var(--larder-shadow); }
    .actions { justify-content: flex-end; }
    @media (max-width: 480px) { .actions .btn, .frame .btn { flex: 1 1 100%; } }
  `;

  constructor() {
    super();
    this.state = 'empty'; // empty | reading | preview | chosen | uploading | uploaded
    this.picture = null; // { base64, contentType, size, name, shrunk }
    this.error = null;
    this.uploaded = null; // { mediaId, imageLink } of the last upload
    this.on('change', 'input[type=file]', (event, input) => this.read(input.files?.[0]));
    this.on('click', '[data-action=use]', () => this.use());
    this.on('click', '[data-action=skip]', () => this.skip());
    this.on('click', '[data-action=again]', () => this.reset());
  }

  connectedCallback() {
    this.update();
  }

  attributeChangedCallback() {
    if (this.isConnected) this.update();
  }

  /** Content type and size of the picture held, or null. */
  get pictureInfo() {
    return this.picture ? { contentType: this.picture.contentType, size: this.picture.size } : null;
  }

  async read(file) {
    if (!file) return;
    this.error = null;
    this.state = 'reading';
    this.update();
    try {
      let bytes = new Uint8Array(await file.arrayBuffer());
      let contentType = detectContentType(bytes);
      let shrunk = false;
      if (!contentType || bytes.length > MAX_BYTES) {
        const smaller = await shrink(file);
        if (!smaller) {
          throw new Error(!contentType
            ? 'Larder takes JPEG, PNG, GIF or WebP pictures, and this one could not be converted. Please choose another photo.'
            : `This picture is ${megabytes(bytes.length)} - Larder takes pictures up to 5 MB. Please choose a smaller one.`);
        }
        bytes = smaller;
        contentType = detectContentType(bytes);
        shrunk = true;
      }
      if (bytes.length === 0) throw new Error('This file is empty. Please choose another photo.');
      if (!contentType || bytes.length > MAX_BYTES) throw new Error('This picture does not fit Larder (JPEG, PNG, GIF or WebP up to 5 MB).');
      this.picture = { base64: toBase64(bytes), contentType, size: bytes.length, name: file.name, shrunk };
      this.uploaded = null;
      this.state = 'preview';
    } catch (error) {
      this.picture = null;
      this.state = 'empty';
      this.error = errorMessage(error);
    }
    this.update();
  }

  use() {
    if (!this.picture) return;
    this.state = 'chosen';
    this.update();
    this.emit('larder:picture-taken', this.pictureInfo);
  }

  skip() {
    this.emit('larder:picture-skipped');
  }

  reset() {
    this.picture = null;
    this.uploaded = null;
    this.error = null;
    this.state = 'empty';
    this.update();
    this.$('input[type=file]')?.focus();
  }

  /**
   * Uploads the picture with links to the business objects it belongs to (contract Link[], may be empty).
   * Resolves to { mediaId, imageLink }. Uploading the same picture with the same links again returns the
   * first upload instead of storing a copy.
   */
  async upload(links = []) {
    if (!this.picture) throw new Error('No picture chosen yet.');
    const checked = checkLinks(links);
    const key = JSON.stringify(checked);
    if (this.uploaded && this.uploaded.key === key) return { mediaId: this.uploaded.mediaId, imageLink: this.uploaded.imageLink };
    const previous = this.state;
    this.state = 'uploading';
    this.error = null;
    this.update();
    try {
      const response = await this.api(CONTEXT, '/images', { method: 'POST', body: { media: this.picture.base64, links: checked } });
      const imageLink = response.body?.imageLink || response.location;
      const mediaId = mediaIdOf(imageLink);
      if (!mediaId) throw new Error('Media answered without a link to the picture.');
      this.uploaded = { key, mediaId, imageLink };
      this.state = 'uploaded';
      this.update();
      return { mediaId, imageLink };
    } catch (error) {
      this.state = previous;
      this.error = error instanceof ApiError && error.code === 'INVALID_IMAGE'
        ? `Media did not accept this picture: ${error.message}`
        : `The picture could not be uploaded. ${errorMessage(error)}`;
      this.update();
      throw error;
    }
  }

  update() {
    const purpose = this.getAttribute('purpose');
    const picture = this.picture;
    const error = this.error ? html`<p class="error" role="alert">${this.error}</p>` : '';
    let body;
    if (this.state === 'empty' || this.state === 'reading') {
      const reading = this.state === 'reading';
      body = html`
        <div class="frame">
          ${ICON_CAMERA}
          <p class="muted">${reading ? 'Looking at your picture…' : 'Show what is on your plate - a quick snapshot is enough.'}</p>
          <div class="row" style="justify-content:center">
            <label class="btn btn-primary pick">Take a photo
              <input type="file" accept="image/*" capture="environment" ${reading ? 'disabled' : ''}></label>
            <label class="btn pick">Choose from gallery
              <input type="file" accept="image/jpeg,image/png,image/gif,image/webp,image/*" ${reading ? 'disabled' : ''}></label>
          </div>
          <p class="small muted">JPEG, PNG, GIF or WebP, up to 5 MB.</p>
        </div>
        ${error}
        ${this.hasAttribute('required') ? '' : html`<div class="row actions"><button class="btn" data-action="skip" type="button">Skip</button></div>`}`;
    } else {
      const src = `data:${picture.contentType};base64,${picture.base64}`;
      const chosen = this.state !== 'preview';
      const busy = this.state === 'uploading';
      const status = this.state === 'uploading' ? html`<span class="badge">Uploading…</span>`
        : this.state === 'uploaded' ? html`<span class="badge herb">Uploaded ✓</span>`
          : chosen ? html`<span class="badge herb">Picture chosen ✓</span>` : '';
      body = html`
        <div class="preview">
          <img src="${src}" alt="Preview of the chosen picture">
          ${status}
        </div>
        <p class="small muted">${picture.name ? `${picture.name} · ` : ''}${megabytes(picture.size)}${picture.shrunk ? ' · made smaller to fit Larder' : ''}</p>
        ${error}
        <div class="row actions">
          <button class="btn" data-action="again" type="button" ${busy ? 'disabled' : ''}>${chosen ? 'Choose another' : 'Take another'}</button>
          ${chosen ? '' : html`${this.hasAttribute('required') ? '' : html`<button class="btn" data-action="skip" type="button">Skip</button>`}
            <button class="btn btn-primary" data-action="use" type="button">Use this picture</button>`}
        </div>`;
    }
    this.render(html`
      <section class="stack" aria-label="Picture">
        ${purpose ? html`<p class="muted">${purpose}</p>` : ''}
        ${body}
      </section>`);
  }
}
define('larder-media-capture', MediaCapture);

// ---------------------------------------------------------------- <larder-media-image>

/** Loaded images by media id (data URLs), shared by all image elements; small LRU. */
const cache = new Map();
const CACHE_SIZE = 40;

function loadImage(element, mediaId) {
  if (cache.has(mediaId)) {
    const hit = cache.get(mediaId);
    cache.delete(mediaId);
    cache.set(mediaId, hit);
    return hit;
  }
  const loading = element.api(CONTEXT, `/images/${encodeURIComponent(mediaId)}`).then(({ body }) => {
    const base64 = typeof body?.media === 'string' ? body.media.replace(/\s+/g, '') : '';
    if (!base64 || base64.length % 4 !== 0 || !BASE64.test(base64)) throw new Error('This picture is damaged.');
    const contentType = detectContentType(leadingBytes(base64));
    if (!contentType) throw new Error('This picture has a format Larder cannot show.');
    return { src: `data:${contentType};base64,${base64}`, size: decodedSize(base64) };
  });
  loading.catch(() => cache.delete(mediaId));
  cache.set(mediaId, loading);
  while (cache.size > CACHE_SIZE) cache.delete(cache.keys().next().value);
  return loading;
}

/** Shows one image of Media; loads it once it scrolls into view. */
class MediaImage extends LarderElement {
  static observedAttributes = ['link', 'media'];

  static styles = `
    :host { display: block; }
    .box { aspect-ratio: 4 / 3; border-radius: var(--larder-radius-small); background: var(--larder-surface-muted);
           display: grid; place-items: center; text-align: center; padding: 12px; overflow: hidden; }
    img { width: 100%; max-height: 420px; object-fit: cover; border-radius: var(--larder-radius-small); display: block;
          background: var(--larder-surface-muted); }
    .box.error { aspect-ratio: auto; min-height: 120px; }
  `;

  connectedCallback() {
    this.load();
  }

  disconnectedCallback() {
    this.observer?.disconnect();
    this.observer = null;
  }

  attributeChangedCallback(name, before, after) {
    if (before !== after && this.isConnected) this.load();
  }

  get mediaId() {
    const media = this.getAttribute('media');
    if (media) return UUID.test(media) ? media : null;
    return mediaIdOf(this.getAttribute('link'));
  }

  load() {
    this.observer?.disconnect();
    const mediaId = this.mediaId;
    if (!this.getAttribute('media') && !this.getAttribute('link')) {
      this.render(html`<div class="box empty small">No picture</div>`);
      return;
    }
    if (!mediaId) {
      this.render(html`<div class="box error small" role="alert">This is not a picture of Larder.</div>`);
      return;
    }
    this.render(html`<div class="box loading small" aria-busy="true">Loading picture…</div>`);
    const show = () => this.show(mediaId);
    if (typeof IntersectionObserver === 'function') {
      this.observer = new IntersectionObserver((entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          this.observer?.disconnect();
          this.observer = null;
          show();
        }
      }, { rootMargin: '200px' });
      this.observer.observe(this);
    } else {
      show();
    }
  }

  async show(mediaId) {
    try {
      const { src } = await loadImage(this, mediaId);
      if (this.mediaId !== mediaId) return; // attribute changed meanwhile
      this.render(html`<img src="${src}" alt="Picture shared in Larder" loading="lazy">`);
    } catch (error) {
      if (this.mediaId !== mediaId) return;
      const message = error instanceof ApiError && error.status === 404 ? 'This picture is no longer available.' : errorMessage(error);
      this.render(html`<div class="box error small" role="alert"><span>${message}</span></div>`);
    }
  }
}
define('larder-media-image', MediaImage);
