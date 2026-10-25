import type { HelpProviderType } from "../../domain/help-request/help-request.js";
import type { Actor, ActorRole } from "../actor.js";

/**
 * ADR-CA-009 · the provider type a role helps as. A Chef sees a request only when selected (chefId) — Menu
 * requests, increment 1 — so CHEF opens nothing here yet.
 */
const HELPER_TYPE_OF_ROLE: Partial<Record<ActorRole, HelpProviderType>> = {
  COMMUNITY_COOK: "COMMUNITY",
};

/** The addressees whose open (REQUESTED) help requests this viewer may see as a helper. */
export function helperTypesOf(viewer: Actor): HelpProviderType[] {
  return viewer.roles.flatMap((role) => HELPER_TYPE_OF_ROLE[role] ?? []);
}
