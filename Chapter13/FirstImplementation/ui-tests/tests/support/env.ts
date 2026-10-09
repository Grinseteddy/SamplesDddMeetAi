import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

/**
 * Where the running Larder lives and who the test cook is.
 *
 * The test user is the local development user of this project's Keycloak realm
 * (infra/keycloak/larder-realm.json). Its username, password and profile are read from that file,
 * so the tests never carry their own copy of a credential. Override with LARDER_TEST_USER /
 * LARDER_TEST_PASSWORD if the realm differs.
 */
export const APP_URL = process.env.LARDER_APP_URL ?? 'http://localhost:8080';
export const ISSUER = process.env.LARDER_ISSUER_URI ?? 'http://localhost:8180/realms/larder';
export const CLIENT_ID = process.env.LARDER_CLIENT_ID ?? 'larder-dev';
export const STORAGE_STATE = resolve(__dirname, '../../.auth/cook.json');

/** The fixed cookId claim of the realm's test user. */
export const COOK_ID = 'f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074';

/** Consent texts seeded by Consent Management (V3__consent_texts.sql). */
export const CONSENT = {
  photos: { id: '5f8d8a1c-515d-4eae-a6b1-0a0313edfc31', text: 'I allow to use my photos in public thanks and recipes.' },
  mention: { id: '3c6e2b7a-9d41-4f0b-8e5a-2b7c9d1e4f60', text: 'I allow other cooks to mention me as helper in their thanks.' },
};

interface RealmUser {
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  credentials?: { type: string; value?: string }[];
}

function realmUser(): RealmUser {
  const realm = JSON.parse(readFileSync(resolve(__dirname, '../../../infra/keycloak/larder-realm.json'), 'utf8'));
  const user = (realm.users as RealmUser[] | undefined)?.[0];
  if (!user) throw new Error('No user in infra/keycloak/larder-realm.json');
  return user;
}

const user = realmUser();

export const TEST_USER = {
  username: process.env.LARDER_TEST_USER ?? user.username,
  password: process.env.LARDER_TEST_PASSWORD ?? user.credentials?.find((c) => c.type === 'password')?.value ?? '',
  email: user.email ?? 'cook@larder.test',
  givenName: user.firstName ?? 'Grace',
  name: user.lastName ?? 'Cook',
};
