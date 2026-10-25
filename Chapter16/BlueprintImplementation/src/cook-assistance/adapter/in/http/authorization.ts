import type { operations } from "../../../../generated/openapi/cook-assistance.js";
import type { Actor, ActorRole } from "../../../application/actor.js";

/** The caller lacks the scope or the role an operation needs (ADR-CA-009): answered with 403. */
export class NotAuthorized extends Error {
  constructor(reason: string) {
    super(reason);
    this.name = "NotAuthorized";
  }
}

interface Permission {
  readonly scope: string;
  readonly roles: readonly ActorRole[];
}

/** ADR-CA-009 · every GET operation: read scope, any of the three roles (what each may see is the query's visibility). */
const READ: Permission = { scope: "cooking-assistance:read", roles: ["COOK", "COMMUNITY_COOK", "CHEF"] };

// ADR-CA-009 · scope and role per operation, checked at the adapter before the use case (deny by default).
const PERMISSIONS: Partial<Record<keyof operations, Permission>> = {
  requestHelp: { scope: "cooking-assistance:write", roles: ["COOK"] },
  getHelpRequestById: READ,
  listHelpRequests: READ,
  getHelpById: READ,
  listHelpsForHelpRequest: READ,
};

export function ensurePermitted(actor: Actor, operation: keyof operations): void {
  const permission = PERMISSIONS[operation];
  if (permission === undefined || !actor.scopes.includes(permission.scope)) {
    throw new NotAuthorized(`${operation} needs the scope ${permission?.scope ?? "(none declared)"}`);
  }
  if (!permission.roles.some((role) => actor.roles.includes(role))) {
    throw new NotAuthorized(`${operation} needs one of the roles ${permission.roles.join(", ")}`);
  }
}
