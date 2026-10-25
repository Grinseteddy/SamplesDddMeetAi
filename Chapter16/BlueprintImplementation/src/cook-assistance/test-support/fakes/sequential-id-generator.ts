import type { IdGenerator } from "../../application/ports/id-generator.js";

// Hands out the given ids in order (ADR-CA-007), so tests know which id a new aggregate gets.
export class SequentialIdGenerator implements IdGenerator {
  private handedOut = 0;

  constructor(private readonly ids: readonly string[]) {}

  next(): string {
    const id = this.ids[this.handedOut];
    if (id === undefined) {
      throw new Error(`SequentialIdGenerator has only ${this.ids.length} ids`);
    }
    this.handedOut += 1;
    return id;
  }
}
