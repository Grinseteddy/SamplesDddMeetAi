import type { CookAssistance } from "../../../application/cook-assistance.js";
import type { JoseTokenVerifier } from "./token-verifier.js";

/** A structured log event without personal data (ADR-CA-010): refused accesses, unexpected errors. */
export interface HttpLogEvent {
  readonly event: "access-refused" | "internal-error";
  readonly [field: string]: unknown;
}

/** What the HTTP adapter needs: the use cases it serves, the token verifier, its public address and where it logs. */
export interface HttpSettings {
  readonly cookAssistance: Pick<CookAssistance, "requestPreparationStepHelp" | "helpRequestQueries" | "helpQueries">;
  readonly tokenVerifier: JoseTokenVerifier;
  /** The API's public base URL (the spec's server), for Location and links — e.g. https://larder.org/cooking-assistance. */
  readonly publicBaseUrl: string;
  readonly log: (event: HttpLogEvent) => void;
}

/** Injection token: Vitest emits no decorator metadata, so every constructor parameter is injected explicitly. */
export const HTTP_SETTINGS = Symbol("HTTP_SETTINGS");
