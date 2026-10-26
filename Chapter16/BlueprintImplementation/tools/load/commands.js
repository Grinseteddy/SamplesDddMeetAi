// QS-08 (unit 12.5d item 2): 10 requests/s on POST /help-requests for 60 s, round-robin over the cooks.
import { check } from "k6";
import exec from "k6/execution";
import { atTenPerSecond, askAboutStep3, COOKS, summaryTo } from "./common.js";

export const options = atTenPerSecond("commands");

export default function () {
  const answer = askAboutStep3(COOKS[exec.scenario.iterationInTest % COOKS.length]);
  check(answer, { "201 Created": (response) => response.status === 201 });
}

export const handleSummary = summaryTo("commands");
