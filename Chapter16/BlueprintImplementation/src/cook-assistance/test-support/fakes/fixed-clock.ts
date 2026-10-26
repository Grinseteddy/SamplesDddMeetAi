import type { Clock } from "../../application/ports/clock.js";

// A clock that always tells the same instant (ADR-CA-007), so deadlines are deterministic in tests.
export class FixedClock implements Clock {
  constructor(private readonly instant: Date) {}

  now(): Date {
    return new Date(this.instant);
  }
}
