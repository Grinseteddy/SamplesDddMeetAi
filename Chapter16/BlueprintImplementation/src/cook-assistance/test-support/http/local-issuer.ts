import { createLocalJWKSet, exportJWK, generateKeyPair, SignJWT, type CryptoKey } from "jose";
import { JoseTokenVerifier } from "../../adapter/in/http/token-verifier.js";

export const ISSUER = "https://keycloak.test/realms/larder";
export const AUDIENCE = "cook-assistance";

export interface TokenClaims {
  /** Left out only to test a token without a subject (inspection m-8). */
  readonly sub: string | undefined;
  readonly roles?: readonly string[];
  readonly scope?: string;
  readonly premium?: boolean;
  /** Seconds from now; negative for an expired token. */
  readonly expiresIn?: number;
  /** Leaves out the role, scope and premium claims (a token Keycloak could issue for another client). */
  readonly withoutClaims?: boolean;
}

/** A stand-in for Keycloak in tests: one ES256 key, its JWKS, and signed access tokens (ADR-CA-008). */
export class LocalIssuer {
  private constructor(
    private readonly privateKey: CryptoKey,
    readonly verifier: JoseTokenVerifier,
  ) {}

  static async start(): Promise<LocalIssuer> {
    const { publicKey, privateKey } = await generateKeyPair("ES256");
    const jwk = { ...(await exportJWK(publicKey)), kid: "test-key", alg: "ES256" };
    const verifier = new JoseTokenVerifier({ issuer: ISSUER, audience: AUDIENCE, keys: createLocalJWKSet({ keys: [jwk] }) });
    return new LocalIssuer(privateKey, verifier);
  }

  async tokenFor({
    sub,
    roles = ["COOK"],
    scope = "cooking-assistance:write",
    premium = false,
    expiresIn = 300,
    withoutClaims = false,
  }: TokenClaims): Promise<string> {
    const now = Math.floor(Date.now() / 1000);
    const claims = withoutClaims ? {} : { realm_access: { roles: [...roles] }, scope, premium };
    const jwt = new SignJWT(claims).setProtectedHeader({ alg: "ES256", kid: "test-key" });
    return (sub === undefined ? jwt : jwt.setSubject(sub))
      .setIssuer(ISSUER)
      .setAudience(AUDIENCE)
      .setIssuedAt(now)
      .setExpirationTime(now + expiresIn)
      .sign(this.privateKey);
  }
}
