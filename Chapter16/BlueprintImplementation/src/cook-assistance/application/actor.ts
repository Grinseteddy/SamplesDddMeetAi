import type { CookId } from "../domain/shared/ids.js";

/** Who calls Cook Assistance (ADR-CA-008): the inbound adapter builds it from a verified token. */
export type ActorRole = "COOK" | "COMMUNITY_COOK" | "CHEF";

export interface Actor {
  readonly cookId: CookId;
  readonly roles: readonly ActorRole[];
  readonly premium: boolean;
  readonly scopes: readonly string[];
}
