import type { NextFunction, Request, RequestHandler, Response } from "express";

// ADR-CA-010 · the limits the API's edge enforces before any use case runs; the 413 and 429 Problems cite them.

export const MAX_BODY_MEGABYTES = 1;

/** 60 requests per minute and cook (the authenticated actor), by address otherwise. Counted per instance. */
export const REQUESTS_PER_MINUTE = 60;

const WINDOW_MS = 60_000;

/** More requests than the limit allows: answered with 429 and Retry-After (ADR-CA-010). */
export class TooManyRequests extends Error {
  constructor(readonly retryAfterSeconds: number) {
    super(`more than ${REQUESTS_PER_MINUTE} requests per minute`);
    this.name = "TooManyRequests";
  }
}

/** The cook of a valid token; the caller's address otherwise. */
function rateLimitKeyOf(request: Request, response: Response): string {
  return response.locals.actor?.cookId ?? request.ip ?? "unknown";
}

/**
 * ADR-CA-010 · a fixed one-minute window per key, counted before authentication and validation refuse anything
 * (inspection m-9), so floods of 401s and 400s are limited too.
 */
export function createRateLimiter(now: () => number = Date.now): { middleware: RequestHandler; trackedKeys: () => number } {
  const windows = new Map<string, { startedAt: number; count: number }>();
  let sweptAt = now();
  /** At most once a window: forget the windows that ended, so the map holds only callers of the last minute (N-1). */
  const forgetEnded = (at: number) => {
    if (at - sweptAt < WINDOW_MS) {
      return;
    }
    windows.forEach((window, key) => {
      if (at - window.startedAt >= WINDOW_MS) {
        windows.delete(key);
      }
    });
    sweptAt = at;
  };
  const middleware = (request: Request, response: Response, next: NextFunction) => {
    const key = rateLimitKeyOf(request, response);
    const at = now();
    forgetEnded(at);
    const window = windows.get(key);
    if (window === undefined || at - window.startedAt >= WINDOW_MS) {
      windows.set(key, { startedAt: at, count: 1 });
      next();
      return;
    }
    window.count += 1;
    if (window.count > REQUESTS_PER_MINUTE) {
      next(new TooManyRequests(Math.ceil((window.startedAt + WINDOW_MS - at) / 1000)));
      return;
    }
    next();
  };
  return { middleware, trackedKeys: () => windows.size };
}
