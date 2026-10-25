import type { CookAssistance } from "../../../composition.js";
import type { JoseTokenVerifier } from "./token-verifier.js";

/** What the HTTP adapter needs: the use cases it serves, the token verifier and its public address. */
export interface HttpSettings {
  readonly cookAssistance: Pick<CookAssistance, "requestPreparationStepHelp">;
  readonly tokenVerifier: JoseTokenVerifier;
  /** The API's public base URL (the spec's server), for Location and links — e.g. https://larder.org/cooking-assistance. */
  readonly publicBaseUrl: string;
}

/** Injection token: Vitest emits no decorator metadata, so every constructor parameter is injected explicitly. */
export const HTTP_SETTINGS = Symbol("HTTP_SETTINGS");
