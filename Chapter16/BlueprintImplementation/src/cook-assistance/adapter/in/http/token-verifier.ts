import { errors, type JWTPayload, type JWTVerifyGetKey, jwtVerify } from "jose";
import type { Actor, ActorRole } from "../../../application/actor.js";
import { CookId } from "../../../domain/shared/ids.js";
import { Unauthenticated } from "./authentication.js";

export interface TokenVerifierSettings {
  readonly issuer: string;
  readonly audience: string;
  /** Keycloak's remote JWKS in production (createRemoteJWKSet), a local one in tests. */
  readonly keys: JWTVerifyGetKey;
}

/** Keycloak's claims (ADR-CA-008, amended): realm roles, space-separated scopes, a premium flag. */
interface KeycloakClaims extends JWTPayload {
  readonly realm_access?: { readonly roles?: readonly string[] };
  readonly scope?: string;
  readonly premium?: boolean;
}

const CLOCK_SKEW_SECONDS = 30;

// ADR-CA-008 · a bearer token is verified by the service itself and becomes the application's Actor.
export class JoseTokenVerifier {
  constructor(private readonly settings: TokenVerifierSettings) {}

  async verify(token: string): Promise<Actor> {
    const payload = await this.verifiedClaimsOf(token);
    return {
      cookId: CookId.of(payload.sub ?? ""),
      roles: (payload.realm_access?.roles ?? []) as ActorRole[],
      premium: payload.premium === true,
      scopes: (payload.scope ?? "").split(" ").filter((scope) => scope !== ""),
    };
  }

  /** An invalid token is the caller's problem (401); keys that cannot be fetched are ours (not a 401). */
  private async verifiedClaimsOf(token: string): Promise<KeycloakClaims> {
    try {
      const { payload } = await jwtVerify<KeycloakClaims>(token, this.settings.keys, {
        issuer: this.settings.issuer,
        audience: this.settings.audience,
        clockTolerance: CLOCK_SKEW_SECONDS,
      });
      return payload;
    } catch (error) {
      if (error instanceof errors.JOSEError && !(error instanceof errors.JWKSTimeout)) {
        throw new Unauthenticated(error.code);
      }
      throw error;
    }
  }
}
