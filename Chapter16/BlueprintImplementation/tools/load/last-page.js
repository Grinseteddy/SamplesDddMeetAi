// QS-09/QS-16 (unit 12.5d item 5): 10 requests/s for 60 s on listHelpRequests' last page, round-robin over the cooks —
// each sees its 1 200 seeded requests: 60 pages of 20, the last one full.
import { check } from "k6";
import exec from "k6/execution";
import http from "k6/http";
import { atTenPerSecond, BASE, COOKS, headersOf, summaryTo } from "./common.js";

export const options = atTenPerSecond("lastPage");

export default function () {
  const cook = COOKS[exec.scenario.iterationInTest % COOKS.length];
  const answer = http.get(`${BASE}/help-requests?page=59&size=20`, { headers: headersOf(cook), tags: { operation: "listHelpRequests" } });
  check(answer, { "200 OK, a full last page": (response) => response.status === 200 && response.json("items").length === 20 });
}

export const handleSummary = summaryTo("last-page");
