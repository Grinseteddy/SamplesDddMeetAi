import { expect, request, type APIRequestContext } from '@playwright/test';
import { APP_URL, CLIENT_ID, COOK_ID, ISSUER, TEST_USER } from './env';

/**
 * Direct access to the Larder APIs for test preconditions and test data - used wherever the UI is not
 * the subject of the test (e.g. "the cook has no photo consent", "there is a recipe with two steps").
 *
 * The token comes from Keycloak's password grant (Resource Owner Password Credentials) of the public
 * dev client, for the realm's local test user. The browser itself never uses this token: the AppShell
 * signs in with Authorization Code + PKCE as in production.
 */
export class LarderApi {
  private constructor(private readonly http: APIRequestContext) {}

  static async create(): Promise<LarderApi> {
    const keycloak = await request.newContext();
    const response = await keycloak.post(`${ISSUER}/protocol/openid-connect/token`, {
      form: { grant_type: 'password', client_id: CLIENT_ID, username: TEST_USER.username, password: TEST_USER.password },
    });
    expect(response.ok(), `password grant for the test user failed: ${response.status()}`).toBeTruthy();
    const { access_token: token } = await response.json();
    await keycloak.dispose();
    const http = await request.newContext({
      baseURL: APP_URL,
      extraHTTPHeaders: { Authorization: `Bearer ${token}`, version: '1.0.0' },
    });
    return new LarderApi(http);
  }

  async dispose() {
    await this.http.dispose();
  }

  private async call(method: string, path: string, data?: unknown, expected?: number[]) {
    const response = await this.http.fetch(path, { method, data });
    if (expected && !expected.includes(response.status())) {
      throw new Error(`${method} ${path} answered ${response.status()}: ${(await response.text()).slice(0, 300)}`);
    }
    const text = await response.text();
    return {
      status: response.status(),
      body: text ? JSON.parse(text) : undefined,
      location: response.headers()['location'],
    };
  }

  private static idOf(location?: string): string {
    if (!location) throw new Error('No Location header');
    return location.replace(/\/+$/, '').split('/').pop()!;
  }

  // ------------------------------------------------------------ Cook Profile

  async isRegistered(): Promise<boolean> {
    return (await this.call('GET', `/cook-profile/cooks/${COOK_ID}`, undefined, [200, 404])).status === 200;
  }

  async ensureRegistered() {
    if (await this.isRegistered()) return;
    await this.call('POST', '/cook-profile/cooks',
      { email: TEST_USER.email, name: TEST_USER.name, givenName: TEST_USER.givenName }, [201]);
  }

  async deregister() {
    await this.call('DELETE', `/cook-profile/cooks/${COOK_ID}`, undefined, [204, 404]);
  }

  // ------------------------------------------------------------ Recipe Catalog

  async createRecipe(name: string, steps: string[]): Promise<string> {
    const { location } = await this.call('POST', '/recipe-catalog/recipes', {
      name,
      preparationTime: '00:30',
      servings: 4,
      meal: 'DINNER',
      diet: 'VEGETARIAN',
      ingredients: [{ name: 'Flour', value: 500, unit: 'GRAM' }, { name: 'Buttermilk', value: 250, unit: 'MILLILITER' }],
      howToSteps: steps.map((description, index) => ({ sequenceNumber: index + 1, description })),
    }, [201]);
    return LarderApi.idOf(location);
  }

  async deleteRecipe(recipeId: string) {
    await this.call('DELETE', `/recipe-catalog/recipes/${recipeId}`, undefined, [204, 404]);
  }

  // ------------------------------------------------------------ Meal Preparation / Meal Planning

  async startPreparation(recipeId: string): Promise<string> {
    const { location } = await this.call('POST', '/meal-preparation/preparations', { recipe: recipeId }, [201]);
    return LarderApi.idOf(location);
  }

  async deleteMealPlan(planId: string) {
    await this.call('DELETE', `/meal-planning/meal-plans/${planId}`, undefined, [204, 404]);
  }

  // ------------------------------------------------------------ Consent Management

  async consents(): Promise<{ consentId: string; revokedAt?: string; consentText?: { consentTextId: string } }[]> {
    return (await this.call('GET', `/consent-management/consents?subject=${COOK_ID}`, undefined, [200])).body ?? [];
  }

  /** Revokes every consent in force for one consent text (the cook then has not given it). */
  async revokeConsent(consentTextId: string) {
    for (const consent of await this.consents()) {
      if (consent.consentText?.consentTextId === consentTextId && !consent.revokedAt) {
        await this.call('DELETE', `/consent-management/consents/${consent.consentId}`, undefined, [204, 400]);
      }
    }
  }

  async giveConsent(consentTextId: string) {
    const given = (await this.consents()).some((c) => c.consentText?.consentTextId === consentTextId && !c.revokedAt);
    if (!given) await this.call('POST', '/consent-management/consents', { subject: COOK_ID, consentTextId }, [201]);
  }

  // ------------------------------------------------------------ Cooking Assistance

  async askForHelp(title: string, description: string, recipeId: string): Promise<string> {
    const { location } = await this.call('POST', '/cooking-assistance/help-requests', {
      title, type: 'STEPS_TO_MITIGATE_CATASTROPHE', description, recipe: recipeId,
      preferredProvider: ['GRANDMA_AVATAR', 'COMMUNITY'],
    }, [201]);
    return LarderApi.idOf(location);
  }

  async helpsFor(helpRequestId: string): Promise<{ helpId: string; answerTitle: string; helpProviderType: string }[]> {
    return (await this.call('GET', `/cooking-assistance/helps?helpRequestId=${helpRequestId}`, undefined, [200])).body ?? [];
  }

  /** Waits until Grandma (asynchronously, over RabbitMQ) answered the request; returns that help. */
  async awaitHelp(helpRequestId: string) {
    let helps: Awaited<ReturnType<LarderApi['helpsFor']>> = [];
    await expect.poll(async () => (helps = await this.helpsFor(helpRequestId)).length,
      { message: 'Grandma Avatar answers the help request', timeout: 30_000 }).toBeGreaterThan(0);
    return helps[0];
  }

  // ------------------------------------------------------------ Media / Sharing

  async uploadImage(png: Buffer, helpRequestId: string): Promise<string> {
    const { location, body } = await this.call('POST', '/media/images', {
      media: png.toString('base64'),
      links: [{ type: 'helpRequest', url: `${APP_URL}/cooking-assistance/help-requests/${helpRequestId}` }],
    }, [201]);
    return body?.imageLink ?? location;
  }

  async giveThanks(helpId: string, text: string, pictureLink: string): Promise<string> {
    const { location } = await this.call('POST', '/sharing/thanks', {
      helpId, recipients: [{ type: 'GrandmaAvatar' }], thanksText: text, pictures: pictureLink,
    }, [201]);
    return LarderApi.idOf(location);
  }

  async myThanks(): Promise<{ thanksId: string; thanksText: string; helpId: string }[]> {
    return (await this.call('GET', `/sharing/thanks?giver=${COOK_ID}`, undefined, [200])).body?.thanks ?? [];
  }

  async deleteThanksContaining(marker: string) {
    for (const thanks of await this.myThanks()) {
      if (thanks.thanksText.includes(marker)) {
        await this.call('DELETE', `/sharing/thanks/${thanks.thanksId}`, undefined, [204, 404]);
      }
    }
  }

  // ------------------------------------------------------------ Notification

  async notifications(): Promise<{ notificationId: string; status: string; link: string; title: string }[]> {
    return (await this.call('GET', '/notifications/notifications', undefined, [200])).body?.notifications ?? [];
  }

  /** Marks every NEW notification READ, so the bell's count is predictable. */
  async readAllNotifications(except?: string) {
    for (const n of await this.notifications()) {
      if (n.status === 'NEW' && n.notificationId !== except) {
        await this.call('PUT', `/notifications/notifications/${n.notificationId}/status`, { status: 'READ' }, [200]);
      }
    }
  }

  async deleteNotification(notificationId: string) {
    await this.call('DELETE', `/notifications/notifications/${notificationId}`, undefined, [204, 404]);
  }
}
