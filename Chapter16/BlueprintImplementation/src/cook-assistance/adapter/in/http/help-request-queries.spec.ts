import { describe, it } from "vitest";

// Query side — increment 0, step 11, unit 11.2: ops getHelpRequestById, listHelpRequests (cook-assistance.openapi.yaml 2.0.0)
// through the HTTP adapter, the query port and the real query adapter on PostgreSQL 16; visibility per ADR-CA-009.

describe("ops getHelpRequestById, listHelpRequests — read model Help request", () => {
  it.todo(
    "GET /help-requests/{id} as the requester: 200 HelpRequest (all fields, pictures [], ingredientId/chefId/plannedMealDate null, REQUESTED, answerDeadline 15:00, createdAt = updatedAt = 10:00, selfLink, helpsLink) -- op getHelpRequestById",
  );
  it.todo("an unknown id: 404 Problem -- op getHelpRequestById");
  it.todo("another COOK who is not the requester: 404, the request stays hidden -- op getHelpRequestById ADR-CA-009");
  it.todo(
    "a COMMUNITY_COOK sees a REQUESTED request addressed to COMMUNITY (200); a CHEF not addressed gets 404 -- op getHelpRequestById ADR-CA-009",
  );
  it.todo("no token: 401; a token without cooking-assistance:read: 403 -- op getHelpRequestById ADR-CA-009");
  it.todo(
    "GET /help-requests?requester={self}: the cook's requests ordered by createdAt, id; page { number 0, size 20, totalElements, totalPages } -- op listHelpRequests",
  );
  it.todo("filters status, topic and addressee return only matching requests -- op listHelpRequests");
  it.todo("size 2 over 3 requests: page 0 has 2, page 1 has 1, totalPages 2; a page past the end is 200 with no items -- op listHelpRequests");
  it.todo(
    "a list shows a caller only what they may see: a COOK their own, a COMMUNITY_COOK the REQUESTED ones addressed to COMMUNITY -- op listHelpRequests ADR-CA-009",
  );
  it.todo("status OPEN or size 101: 400 Problem from the spec -- op listHelpRequests");
  it.todo("wiring: POST /help-requests, then GET /help-requests/{id} at once shows it (read-your-writes, lag 0) -- ADR-CA-012 QS-11");
});
