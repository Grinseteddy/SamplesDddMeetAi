import { describe, it } from "vitest";

// Outbound messaging — increment 0, step 10, unit 10.1: outbox record → message of the AsyncAPI send operation (pure).

describe("outbound messages", () => {
  it.todo("a HelpRequested record: exchange cook-assistance, routing key help.requested, headers messageId (the row's), correlationId (the help request id), source cook-assistance, the stored payload plus helpRequestLink -- op publishHelpRequested B2-S01 G1 G3");
  it.todo("a HelpProvided record: routing key help.provided, correlationId = its helpRequestId, the stored payload plus helpLink and helpRequestLink -- op publishHelpProvided B1-S04 G1 G3");
  it.todo("the message validates against the spec's send schema; without helpRequestLink it does not, nor with requestedAt 'yesterday' (formats are checked) -- ADR-CA-011");
  it.todo("an event type without a publish decision fails with UnmappedEvent -- G5");
  it.todo("gate: every send operation of the spec is mapped or planned, and the publish decisions cover every domain event type -- ADR-CA-011 G5");
});
