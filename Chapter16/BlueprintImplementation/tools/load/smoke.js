// Harness smoke (unit 12.5d item 1): one POST and one GET, one cook.
import http from "k6/http";
import { askAboutStep3, BASE, COOKS, headersOf, summaryTo } from "./common.js";

export const options = { vus: 1, iterations: 1 };

export default function () {
  const cook = COOKS[0];
  const asked = askAboutStep3(cook);
  http.get(`${BASE}/help-requests/${asked.json("helpRequestId")}`, { headers: headersOf(cook), tags: { operation: "getHelpRequestById" } });
}

export const handleSummary = summaryTo("smoke");
