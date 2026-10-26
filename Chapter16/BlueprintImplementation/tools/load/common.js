// Shared by the k6 scripts (unit 12.5d): the service's address, the cooks' tokens and the request shapes of the
// approved OpenAPI 2.0.0. Written by performance.spec.ts into /out before every run.
import http from "k6/http";

export const BASE = `${__ENV.BASE_URL}/cooking-assistance`;
/** One entry per cook: { cookId, token } — read and write scope, so one cook can ask and look. */
export const COOKS = JSON.parse(open("/out/cooks.json"));

export function headersOf(cook) {
  return { authorization: `Bearer ${cook.token}`, version: "2.0.0", "content-type": "application/json" };
}

/** The cook asks about step 3 of Sunday scones (op requestHelp, B2-S01). */
export function askAboutStep3(cook) {
  return http.post(
    `${BASE}/help-requests`,
    JSON.stringify({
      title: "Sunday scones, step 3",
      topic: "PREPARATION_STEP",
      description: "What means 'warm'?",
      recipeId: __ENV.RECIPE_ID,
      howToStepId: __ENV.HOW_TO_STEP_ID,
      addressees: ["GRANDMA_AVATAR"],
    }),
    { headers: headersOf(cook), tags: { operation: "requestHelp" } },
  );
}

/** QS-08 and QS-09's load: 10 requests/s for 60 s, whatever the service's answer time (constant arrival rate). */
export function atTenPerSecond(name) {
  return {
    scenarios: {
      [name]: { executor: "constant-arrival-rate", rate: 10, timeUnit: "1s", duration: "60s", preAllocatedVUs: 20, maxVUs: 50 },
    },
  };
}

/** The summary the spec reads: k6's end-of-test data, as JSON next to the cooks' tokens. */
export function summaryTo(name) {
  return (data) => ({ [`/out/${name}.summary.json`]: JSON.stringify(data) });
}
