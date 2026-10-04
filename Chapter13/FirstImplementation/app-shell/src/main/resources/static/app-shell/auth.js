/**
 * Sign-in for the AppShell: OpenID Connect Authorization Code flow with PKCE against Keycloak
 * (public client, no secret in the browser). Tokens live in memory only; after a reload the
 * Keycloak session signs the cook in again without a password prompt.
 */

const meta = (name) => document.querySelector(`meta[name="${name}"]`)?.content;
const issuer = meta('larder-issuer');
const clientId = meta('larder-client-id');
const redirectUri = `${location.origin}/`;
const PKCE_KEY = 'larder.pkce';

let tokens = null; // { accessToken, refreshToken, idToken, expiresAt }

const base64url = (bytes) => btoa(String.fromCharCode(...new Uint8Array(bytes)))
  .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
const randomString = () => base64url(crypto.getRandomValues(new Uint8Array(32)));

async function challengeFor(verifier) {
  return base64url(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier)));
}

/** Redirects to Keycloak; comes back to `/` with ?code=...&state=... */
export async function login() {
  const verifier = randomString();
  const state = randomString();
  sessionStorage.setItem(PKCE_KEY, JSON.stringify({ verifier, state, returnTo: location.hash }));
  const params = new URLSearchParams({
    client_id: clientId,
    response_type: 'code',
    scope: 'openid',
    redirect_uri: redirectUri,
    code_challenge: await challengeFor(verifier),
    code_challenge_method: 'S256',
    state,
  });
  location.assign(`${issuer}/protocol/openid-connect/auth?${params}`);
}

async function requestTokens(form) {
  const response = await fetch(`${issuer}/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ client_id: clientId, ...form }),
  });
  if (!response.ok) throw new Error(`Sign-in failed (${response.status})`);
  const body = await response.json();
  tokens = {
    accessToken: body.access_token,
    refreshToken: body.refresh_token,
    idToken: body.id_token ?? tokens?.idToken,
    expiresAt: Date.now() + body.expires_in * 1000,
  };
}

/** Completes the sign-in after Keycloak redirected back. Returns true when the cook is signed in. */
export async function completeLogin() {
  const params = new URLSearchParams(location.search);
  const code = params.get('code');
  if (!code) return false;
  const pending = JSON.parse(sessionStorage.getItem(PKCE_KEY) || 'null');
  sessionStorage.removeItem(PKCE_KEY);
  if (!pending || pending.state !== params.get('state')) throw new Error('Sign-in answer does not match the request');
  await requestTokens({ grant_type: 'authorization_code', code, redirect_uri: redirectUri, code_verifier: pending.verifier });
  history.replaceState(null, '', `/${pending.returnTo || ''}`);
  return true;
}

/** A valid access token, refreshed shortly before it expires. */
export async function accessToken() {
  if (!tokens) throw new Error('Not signed in');
  if (Date.now() > tokens.expiresAt - 30_000) {
    try {
      await requestTokens({ grant_type: 'refresh_token', refresh_token: tokens.refreshToken });
    } catch {
      await login();
    }
  }
  return tokens.accessToken;
}

/** The claims of the access token, e.g. cookId. */
export function claims() {
  if (!tokens) return {};
  const payload = tokens.accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
  return JSON.parse(decodeURIComponent(escape(atob(payload))));
}

export function logout() {
  const params = new URLSearchParams({ client_id: clientId, post_logout_redirect_uri: redirectUri });
  if (tokens?.idToken) params.set('id_token_hint', tokens.idToken);
  tokens = null;
  location.assign(`${issuer}/protocol/openid-connect/logout?${params}`);
}
