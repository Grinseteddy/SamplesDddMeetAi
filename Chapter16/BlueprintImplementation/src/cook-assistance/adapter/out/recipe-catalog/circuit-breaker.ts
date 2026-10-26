import type { Clock } from "../../../application/ports/clock.js";

/** The circuit is open: the call was not made (ADR-CA-015). */
export class CircuitOpen extends Error {
  constructor() {
    super("the circuit is open; the call was not made");
    this.name = "CircuitOpen";
  }
}

const FAILURE_THRESHOLD = 5;
const WINDOW_MS = 30_000;
const OPEN_MS = 30_000;

// ADR-CA-015 · opens after 5 failures in 30 s, half-open after 30 s; time from the Clock port (ADR-CA-007)
export class CircuitBreaker {
  constructor(private readonly clock: Clock) {}

  /** When the recent failures happened (epoch ms). */
  private failures: number[] = [];
  /** When the circuit last opened (epoch ms). */
  private openedAt: number | undefined;
  /** Half-open: the one trial call is running. */
  private trialRunning = false;

  async call<T>(action: () => Promise<T>): Promise<T> {
    if (this.isOpen(this.clock.now().getTime())) {
      throw new CircuitOpen();
    }
    return this.openedAt === undefined ? this.attempt(action) : this.trial(action);
  }

  /** Half-open: the one trial call; it succeeds → closed, with the failures forgotten. */
  private async trial<T>(action: () => Promise<T>): Promise<T> {
    this.trialRunning = true;
    try {
      const result = await this.attempt(action);
      this.openedAt = undefined;
      this.failures = [];
      return result;
    } finally {
      this.trialRunning = false;
    }
  }

  /** Open: opened less than 30 s ago, or half-open with its trial call running — no call is made. */
  private isOpen(now: number): boolean {
    return this.openedAt !== undefined && (this.trialRunning || now - this.openedAt < OPEN_MS);
  }

  /** A failed call is recorded. */
  private async attempt<T>(action: () => Promise<T>): Promise<T> {
    try {
      return await action();
    } catch (error) {
      this.recordFailure(this.clock.now().getTime());
      throw error;
    }
  }

  /** A failed call: a failed trial reopens at once; otherwise 5 failures within 30 s open the circuit. */
  private recordFailure(at: number): void {
    const trialFailed = this.openedAt !== undefined;
    this.failures = [...this.failures.filter((failedAt) => at - failedAt < WINDOW_MS), at];
    if (trialFailed || this.failures.length >= FAILURE_THRESHOLD) {
      this.openedAt = at;
    }
  }
}
