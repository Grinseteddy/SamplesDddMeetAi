// QS-09 on QS-16's year of data (unit 12.5d, inspection m-14): 10 requests/s for 60 s on listHelpRequests by community
// helpers — each sees every open request addressed to the community (ADR-CA-009), the heaviest visibility path.
import { check } from "k6";
import exec from "k6/execution";
import http from "k6/http";
import { atTenPerSecond, BASE, headersOf, summaryTo } from "./common.js";

const HELPERS = JSON.parse(open("/out/helpers.json"));

export const options = atTenPerSecond("helpers");

export default function () {
  const helper = HELPERS[exec.scenario.iterationInTest % HELPERS.length];
  const answer = http.get(`${BASE}/help-requests?status=REQUESTED`, {
    headers: headersOf(helper),
    tags: { operation: "listHelpRequests" },
  });
  check(answer, { "200 OK, a full page": (response) => response.status === 200 && response.json("items").length === 20 });
}

export const handleSummary = summaryTo("helpers");
