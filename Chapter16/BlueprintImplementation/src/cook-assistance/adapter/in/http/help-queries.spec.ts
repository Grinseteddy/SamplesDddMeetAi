import { describe, it } from "vitest";

// Query side — increment 0, step 11, unit 11.3: ops getHelpById, listHelpsForHelpRequest (cook-assistance.openapi.yaml 2.0.0)
// through the HTTP adapter, the query port and the real query adapter on PostgreSQL 16; visibility per ADR-CA-009.

describe("ops getHelpById, listHelpsForHelpRequest — read model Help", () => {
  it.todo(
    "GET /helps/{id} as the requester: 200 Help (answer PREPARATION_STEP with images [], helpProviderType GRANDMA_AVATAR, helpProvider null, PROVIDED, createdAt = updatedAt, selfLink, helpRequestLink) -- op getHelpById",
  );
  it.todo("an unknown id, or a cook who is neither requester nor provider: 404 Problem -- op getHelpById ADR-CA-009");
  it.todo("GET /help-requests/{id}/helps as the requester: a page with Grandma's help -- op listHelpsForHelpRequest");
  it.todo("filter status ACCEPTED: 200 with no items -- op listHelpsForHelpRequest");
  it.todo("an unknown help request, or one the caller may not see: 404 Problem -- op listHelpsForHelpRequest ADR-CA-009");
  it.todo("gate: all four GET operations are served; five stay planned for increment 1 -- ADR-CA-011");
});
