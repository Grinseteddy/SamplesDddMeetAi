import type { Request } from "express";

// ADR-CA-010 · the limits the API's edge enforces before any use case runs; the 413 and 429 Problems cite them.

export const MAX_BODY_MEGABYTES = 1;

/** 60 requests per minute and cook (the authenticated actor), by address otherwise. Counted per instance. */
export const REQUESTS_PER_MINUTE = 60;

export const RATE_LIMIT = { ttl: 60_000, limit: REQUESTS_PER_MINUTE };

export function rateLimitKeyOf(request: Request): string {
  return request.res?.locals.actor?.cookId ?? request.ip ?? "unknown";
}
