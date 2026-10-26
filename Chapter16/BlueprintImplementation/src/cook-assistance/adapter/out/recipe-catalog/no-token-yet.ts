import type { TokenProvider } from "./token-provider.js";

// ADR-CA-015 (amended) · until the Keycloak client-credentials provider exists (steps 13/14), production has no
// token for Recipe Catalog: every lookup is unavailable (503), never a call with a forwarded or empty token.
export class NoTokenYet implements TokenProvider {
  async accessToken(): Promise<string> {
    throw new Error("no Recipe Catalog token provider yet — ADR-CA-015, steps 13/14");
  }
}
