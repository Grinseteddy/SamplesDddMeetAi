import { describe, expect, it } from "vitest";
import { SystemClock } from "./system-clock.js";
import { UuidV7Generator } from "./uuid-v7-generator.js";

// Adapters — increment 0 (walking skeleton), step 8, unit 4: the production Clock and IdGenerator (ADR-CA-007).

describe("SystemClock", () => {
  it("now() returns the current instant, between the moments before and after the call -- ADR-CA-007", () => {
    const before = Date.now();

    const now = new SystemClock().now().getTime();

    expect(now).toBeGreaterThanOrEqual(before);
    expect(now).toBeLessThanOrEqual(Date.now());
  });
});

describe("UuidV7Generator", () => {
  it("next() returns a UUID of version 7 -- ADR-CA-007", () => {
    expect(new UuidV7Generator().next()).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/);
  });
  it("a UUID generated later sorts after an earlier one -- ADR-CA-007", () => {
    const generator = new UuidV7Generator();
    const ids = Array.from({ length: 100 }, () => generator.next());

    expect([...ids].sort()).toEqual(ids);
  });
});
