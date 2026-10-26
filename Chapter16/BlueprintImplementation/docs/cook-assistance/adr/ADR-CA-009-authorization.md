# ADR-CA-009 — Authorization: scopes and roles at the adapter, resource checks in the application

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 9

## Context
AP0008 asks for fine-grained access rights. Invariants name actors: only the requester cancels,
accepts, rejects (INV-06, INV-13, INV-15); only addressees answer, a Chef only when selected (INV-09);
only premium cooks address a Chef (INV-02). The API canvas adds "Chef help is visible only to the
original requester". Roles and the premium flag come from the token (ADR-CA-008).

## Decision
**Deny by default.** Three places, each with its own kind of check:

1. **Inbound adapter** — scope and role per operation (401 without a valid token, 403 otherwise):

   | Operation | Scope | Roles |
   |---|---|---|
   | requestHelp, cancelHelpRequest, requestChefReview, acceptHelp, rejectHelp | `cooking-assistance:write` | COOK |
   | provideHelp | `cooking-assistance:write` | COMMUNITY_COOK, CHEF |
   | GET operations | `cooking-assistance:read` | COOK, COMMUNITY_COOK, CHEF |

   A person may hold several roles; the provider type of a help is derived from the role used
   (COMMUNITY_COOK → `COMMUNITY`, CHEF → `CHEF`).
2. **Application service, before the domain call** — resource checks with the loaded aggregates:
   requester (INV-06, INV-13, INV-15, INV-04), addressee and selected Chef (INV-09), premium flag from
   the token (INV-02). A failed check returns `NotTheRequester`, `NotAnAddressee` or
   `PremiumRequiredForChef` (403).
3. **Domain** — only rules that are invariants of the aggregate's own data (e.g. the addressee list of a
   help request, INV-20); the domain receives plain values, never the `Actor`.

**Visibility (queries)** — what a caller may not see answers **404**, hiding its existence:
- a help request is visible to its requester, and — while `REQUESTED` — to helpers whose provider type
  it addresses (a Chef only when selected);
- a help is visible to the requester of its request and to its own provider; a help given by a Chef is
  visible **only** to the original requester and that Chef;
- lists are filtered by the same rules, never post-filtered on the client.

**Premium** — the `premium` claim of the token (Keycloak maps Cook Profile's subscription tier),
checked when a Chef is addressed (requestHelp, requestChefReview). An open Chef request is kept when
the cook later loses premium (answers Q3-5).

**Policy handlers and the timeout runner** act as the service's own identity, not as a user.

## Consequences
- Step 8 test lists hold, per command, one allowed and one refused actor (`-- <slice> ADR-CA-009`).
- Step 14 checks broken object-level and function-level authorization against this table.

## Principles and quality scenarios
Honors AP0008. Serves the security goal (rank 3), QS-19.
