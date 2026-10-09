import { test as base, expect, type Page } from '@playwright/test';
import { deflateSync } from 'node:zlib';
import { LarderApi } from './api';

/**
 * Shared fixtures of the Larder UI tests.
 *
 *  - `api`: Larder APIs with a password-grant token for preconditions and test data (see api.ts).
 *  - `registered` (auto): the cook is registered before each test, so the Cook Profile gate opens the app.
 *    The gate test turns it off and deregisters the cook instead.
 *  - console guard (auto): every uncaught exception in the page (`pageerror`) fails the test, and so does every
 *    console error - except "Failed to load resource" of 4xx answers that a test declares as expected with
 *    `expectedConsoleErrors` (e.g. the 404 of the gate's "is this cook registered?" check).
 */
type Fixtures = {
  api: LarderApi;
  registered: boolean;
  expectedConsoleErrors: RegExp[];
  consoleGuard: void;
};

export const test = base.extend<Fixtures>({
  api: async ({}, use) => {
    const api = await LarderApi.create();
    await use(api);
    await api.dispose();
  },

  registered: [true, { option: true }],
  expectedConsoleErrors: [[], { option: true }],

  consoleGuard: [async ({ page, registered, api, expectedConsoleErrors }, use, testInfo) => {
    if (registered) await api.ensureRegistered();
    const problems: string[] = [];
    page.on('pageerror', (error) => problems.push(`uncaught exception: ${error.message}`));
    page.on('console', (message) => {
      if (message.type() !== 'error') return;
      const text = `${message.text()} @ ${message.location().url}`;
      const expected4xx = /status of 4\d\d/.test(text) && expectedConsoleErrors.some((pattern) => pattern.test(text));
      if (!expected4xx) problems.push(`console error: ${text}`);
    });
    await use();
    if (problems.length) {
      await testInfo.attach('browser-errors', { body: problems.join('\n'), contentType: 'text/plain' });
    }
    expect(problems, 'no uncaught exceptions and no unexpected console errors in the page').toEqual([]);
  }, { auto: true }],
});

export { expect };

/** A unique marker for test data, so re-runs never collide. */
export const unique = (label: string) => `${label} ${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`;

/**
 * Opens the app at a route and waits until the shell has signed in and opened (header navigation visible).
 * With the stored Keycloak session the redirect to Keycloak and back happens without a login form.
 */
export async function openApp(page: Page, hash = '#/recipes') {
  await page.goto(`/${hash}`);
  const nav = page.getByRole('link', { name: 'Meal plans' });
  const loginForm = page.locator('#kc-form-login');
  await expect(nav.or(loginForm), 'the shell signs in (silently through the Keycloak session)').toBeVisible({ timeout: 30_000 });
  await expect(loginForm, 'no Keycloak login form: the stored session signs the cook in silently').toHaveCount(0);
  await expect(page).toHaveURL(new RegExp(`${hash.replace(/[.*+?^${}()|[\]\\/]/g, '\\$&')}$`));
}

/**
 * The AppShell's modal <dialog> showing `title`. The dialog has no accessible name of its own (no
 * aria-labelledby), so it is found by its heading.
 */
export const shellDialog = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title, exact: true }) });

// ---------------------------------------------------------------- a real PNG, generated in the test

const CRC_TABLE = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});

function crc32(bytes: Buffer): number {
  let c = 0xffffffff;
  for (const byte of bytes) c = CRC_TABLE[(c ^ byte) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type: string, data: Buffer): Buffer {
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length);
  const typed = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(typed));
  return Buffer.concat([length, typed, crc]);
}

/** A small RGB PNG of one colour (default 24 x 24, tomato red). */
export function png(width = 24, height = 24, [r, g, b] = [220, 80, 60]): Buffer {
  const header = Buffer.alloc(13);
  header.writeUInt32BE(width, 0);
  header.writeUInt32BE(height, 4);
  header.writeUInt8(8, 8); // bit depth
  header.writeUInt8(2, 9); // colour type RGB
  const row = Buffer.concat([Buffer.from([0]), Buffer.from(Array.from({ length: width }, () => [r, g, b]).flat())]);
  const pixels = Buffer.concat(Array.from({ length: height }, () => row));
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(pixels)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

export const pictureFile = (name: string) => ({ name, mimeType: 'image/png', buffer: png() });
