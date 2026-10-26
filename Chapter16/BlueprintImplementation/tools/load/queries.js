// QS-09 on QS-16's year of data (unit 12.5d item 4): 10 requests/s for 60 s, round-robin over the four served GETs and
// the cooks; each cook reads its own request and help (ADR-CA-009 visibility).
import { check } from "k6";
import exec from "k6/execution";
import http from "k6/http";
import { atTenPerSecond, BASE, COOKS, headersOf, summaryTo } from "./common.js";

/** Per cook (same order as COOKS): one of its seeded requests and that request's help. */
const SAMPLES = JSON.parse(open("/out/samples.json"));

export const options = atTenPerSecond("queries");

const QUERIES = [
  ["getHelpRequestById", (sample) => `/help-requests/${sample.helpRequestId}`],
  ["listHelpRequests", () => "/help-requests"],
  ["listHelpsForHelpRequest", (sample) => `/help-requests/${sample.helpRequestId}/helps`],
  ["getHelpById", (sample) => `/helps/${sample.helpId}`],
];

export default function () {
  const iteration = exec.scenario.iterationInTest;
  const cook = Math.floor(iteration / QUERIES.length) % COOKS.length;
  const [operation, path] = QUERIES[iteration % QUERIES.length];
  const answer = http.get(`${BASE}${path(SAMPLES[cook])}`, { headers: headersOf(COOKS[cook]), tags: { operation } });
  check(answer, { "200 OK": (response) => response.status === 200 });
}

export const handleSummary = summaryTo("queries");
