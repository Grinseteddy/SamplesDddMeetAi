import type { operations } from "../../../../generated/openapi/cook-assistance.js";

type Served = `served: ${string}`;
type Planned = `planned: ${string}`;

const QUERY_SIDE = "planned: step 11, query side";

// ADR-CA-011 · coverage gate: every operation of the spec is served or planned — a new or removed
// operation in the spec fails type checking until it is wired or planned here.
export const SERVED_OPERATIONS = {
  requestHelp: "served: HelpRequestsController.requestHelp (B2-S01)",
  listHelpRequests: QUERY_SIDE,
  getHelpRequestById: QUERY_SIDE,
  listHelpsForHelpRequest: QUERY_SIDE,
  getHelpById: QUERY_SIDE,
  requestChefReview: "planned: increment 1, B1-S02",
  provideHelp: "planned: increment 1, B1-S03",
  acceptHelp: "planned: increment 1, B1-S05",
  cancelHelpRequest: "planned: increment 1, B1-S06",
  rejectHelp: "planned: increment 1, B1-S09",
} as const satisfies Record<keyof operations, Served | Planned>;
