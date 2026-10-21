import { rejected, type HelpRequest, type Rejection } from "./help-request.js";

export type CancelResult =
  | { outcome: "cancelled"; request: HelpRequest }
  | Rejection;

function isOpen(request: HelpRequest): boolean {
  return request.status === "open";
}

function cookCanCancel(request: HelpRequest): boolean {
  return isOpen(request);
}

export function cancelRequest(request: HelpRequest): CancelResult {
  if (!cookCanCancel(request)) {
    return rejected("not open");
  }
  return { outcome: "cancelled", request: { ...request, status: "cancelled" } };
}

const ANSWER_TIMEOUT_MS = 48 * 60 * 60 * 1000;

function answerTimeHasRunOut(request: HelpRequest, now: string): boolean {
  return Date.parse(now) - Date.parse(request.requestedAt) >= ANSWER_TIMEOUT_MS;
}

export function applyAnswerTimeout(request: HelpRequest, now: string): HelpRequest {
  if (isOpen(request) && answerTimeHasRunOut(request, now)) {
    return { ...request, status: "cancelled", cancelledBy: "system" };
  }
  return request;
}
