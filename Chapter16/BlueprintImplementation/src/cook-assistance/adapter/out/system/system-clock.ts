import type { Clock } from "../../../application/ports/clock.js";

// ADR-CA-007 · the production clock: the machine's current instant (UTC).
export class SystemClock implements Clock {
  now(): Date {
    return new Date();
  }
}
