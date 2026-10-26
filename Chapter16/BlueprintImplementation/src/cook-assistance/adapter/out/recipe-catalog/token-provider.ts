// ADR-CA-015 (amended) · the bearer token Cook Assistance sends to Recipe Catalog (recipe:read). The cook's own token
// is never forwarded; production uses a Keycloak client-credentials provider (open item, steps 13/14).
export interface TokenProvider {
  accessToken(): Promise<string>;
}
