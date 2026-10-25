// ADR-CA-007 · time comes only from here; production uses the system clock, tests a fixed one.
export interface Clock {
  /** The current instant (UTC). */
  now(): Date;
}
