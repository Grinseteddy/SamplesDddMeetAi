import { describe, expect, it } from "vitest";
import { FixedClock } from "../../../test-support/fakes/fixed-clock.js";
import { SettableClock } from "../../../test-support/fakes/settable-clock.js";
import { CircuitBreaker, CircuitOpen } from "./circuit-breaker.js";

// Recipe Catalog lookup — increment 0, step 10, unit 10.5a: the circuit breaker (ADR-CA-015: opens after 5 failures in 30 s,
// half-open after 30 s). Time from an injected, settable clock (ADR-CA-007); no timers.

const NOW = new Date("2026-10-25T10:00:00Z");

/** Recipe Catalog cannot be reached. */
async function unreachable(): Promise<never> {
  throw new Error("connection refused");
}

async function failTimes(breaker: CircuitBreaker, times: number): Promise<void> {
  for (let attempt = 0; attempt < times; attempt += 1) {
    await expect(breaker.call(unreachable)).rejects.toThrow("connection refused");
  }
}

describe("circuit breaker", () => {
  it("closed: a call is made and its result returned -- ADR-CA-015", async () => {
    const breaker = new CircuitBreaker(new FixedClock(NOW));

    await expect(breaker.call(async () => "step 3")).resolves.toBe("step 3");
  });
  it("4 failures within 30 s: still closed, the 5th call is made -- ADR-CA-015", async () => {
    const breaker = new CircuitBreaker(new FixedClock(NOW));

    await failTimes(breaker, 4);

    await expect(breaker.call(async () => "step 3")).resolves.toBe("step 3");
  });
  it("5 failures within 30 s: open, the next call is not made and fails with CircuitOpen -- ADR-CA-015", async () => {
    const breaker = new CircuitBreaker(new FixedClock(NOW));
    let made = false;

    await failTimes(breaker, 5);

    await expect(
      breaker.call(async () => {
        made = true;
        return "step 3";
      }),
    ).rejects.toThrow(CircuitOpen);
    expect(made).toBe(false);
  });
  it("failures older than 30 s do not count: 4 at 10:00:00, 1 at 10:00:31 → still closed -- ADR-CA-015", async () => {
    const clock = new SettableClock(NOW);
    const breaker = new CircuitBreaker(clock);

    await failTimes(breaker, 4);
    clock.advanceBy(31_000);
    await failTimes(breaker, 1);

    await expect(breaker.call(async () => "step 3")).resolves.toBe("step 3");
  });
  it("open lasts 30 s from opening: 1 failure at 10:00:00, 4 at 10:00:20 → still open at 10:00:31 -- ADR-CA-015", async () => {
    const clock = new SettableClock(NOW);
    const breaker = new CircuitBreaker(clock);
    let made = false;

    await failTimes(breaker, 1);
    clock.advanceBy(20_000);
    await failTimes(breaker, 4);
    clock.advanceBy(11_000);

    await expect(
      breaker.call(async () => {
        made = true;
        return "step 3";
      }),
    ).rejects.toThrow(CircuitOpen);
    expect(made).toBe(false);
  });
  it("open, 30 s later: half-open, one trial call is made; it succeeds → closed with the failures forgotten -- ADR-CA-015", async () => {
    const clock = new SettableClock(NOW);
    const breaker = new CircuitBreaker(clock);

    await failTimes(breaker, 5);
    clock.advanceBy(30_000);

    await expect(breaker.call(async () => "step 3")).resolves.toBe("step 3");
    await failTimes(breaker, 4);
    await expect(breaker.call(async () => "step 3")).resolves.toBe("step 3");
  });
  it("half-open, the trial call fails → open for another 30 s -- ADR-CA-015", async () => {
    const clock = new SettableClock(NOW);
    const breaker = new CircuitBreaker(clock);
    let made = false;

    await failTimes(breaker, 5);
    clock.advanceBy(30_000);
    await failTimes(breaker, 1);
    clock.advanceBy(29_000);

    await expect(
      breaker.call(async () => {
        made = true;
        return "step 3";
      }),
    ).rejects.toThrow(CircuitOpen);
    expect(made).toBe(false);
  });
  it("half-open, while the trial call runs: a second call is not made and fails with CircuitOpen -- ADR-CA-015", async () => {
    const clock = new SettableClock(NOW);
    const breaker = new CircuitBreaker(clock);
    let finishTrial: (step: string) => void = () => {};
    let made = false;

    await failTimes(breaker, 5);
    clock.advanceBy(30_000);
    const trial = breaker.call(
      () =>
        new Promise<string>((resolve) => {
          finishTrial = resolve;
        }),
    );

    await expect(
      breaker.call(async () => {
        made = true;
        return "step 3";
      }),
    ).rejects.toThrow(CircuitOpen);
    expect(made).toBe(false);
    finishTrial("step 3");
    await expect(trial).resolves.toBe("step 3");
  });
});
