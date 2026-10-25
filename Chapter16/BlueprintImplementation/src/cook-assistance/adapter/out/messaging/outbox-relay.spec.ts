import { describe, it } from "vitest";

// Outbound messaging — increment 0, step 10, unit 10.2: the outbox relay (ADR-CA-004). Real PostgreSQL 16, a fake publisher, an injected clock.

describe("outbox relay \u2014 PostgreSQL 16", () => {
  it.todo("one pending row is published once and marked published -- ADR-CA-004");
  it.todo("a published row is not sent again -- ADR-CA-004");
  it.todo("a broker failure: the row stays pending with attempts 1, the error and next_attempt_at = now + 1 s, and is not retried before then -- ADR-CA-004");
  it.todo("after the next attempt time the row is published with the same message id -- ADR-CA-004");
  it.todo("backoff 1 s, 2 s, 4 s, 8 s, and the 5th failure dead-letters the row -- ADR-CA-004");
  it.todo("a message that breaks the spec is dead-lettered at once and nothing is sent -- ADR-CA-004 ADR-CA-011");
  it.todo("an UnmappedEvent row is dead-lettered and the batch goes on -- G5");
  it.todo("a dead-lettered row is reported with message id, event type and last error -- ADR-CA-004");
  it.todo("order per aggregate: a waiting failed row blocks the later rows of its aggregate; other aggregates go on -- ADR-CA-004");
  it.todo("drain: an aggregate's next row goes out in the same run once its earlier row is done -- ADR-CA-004");
  it.todo("several relays at once publish every row exactly once -- ADR-CA-004 QS-14");
});
