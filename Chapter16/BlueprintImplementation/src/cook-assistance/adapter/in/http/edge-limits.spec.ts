import type { NextFunction, Request, Response } from "express";
import { describe, expect, it } from "vitest";
import { createRateLimiter } from "./edge-limits.js";

// Inbound HTTP — the edge's rate limit (ADR-CA-010), as a unit with an injected clock.

/** One request from an address without a token, through the limiter. */
function requestFrom(middleware: ReturnType<typeof createRateLimiter>["middleware"], ip: string): void {
  middleware({ ip } as Request, { locals: {} } as Response, (() => {}) as NextFunction);
}

describe("rate limit", () => {
  it("windows that ended are forgotten: after a quiet minute only the new caller is tracked -- ADR-CA-010 (inspection N-1)", () => {
    let now = 0;
    const limiter = createRateLimiter(() => now);
    ["192.0.2.1", "192.0.2.2", "192.0.2.3"].forEach((ip) => requestFrom(limiter.middleware, ip));

    now = 61_000;
    requestFrom(limiter.middleware, "192.0.2.4");

    expect(limiter.trackedKeys()).toBe(1);
  });
});
