import type { Clock } from "../../application/ports/clock.js";

// A clock the test moves forward (ADR-CA-007), so time windows are tested without timers.
export class SettableClock implements Clock {
  constructor(private instant: Date) {}

  now(): Date {
    return new Date(this.instant);
  }

  advanceBy(milliseconds: number): void {
    this.instant = new Date(this.instant.getTime() + milliseconds);
  }
}
