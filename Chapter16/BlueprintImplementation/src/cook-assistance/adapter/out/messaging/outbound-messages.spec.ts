import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { Parser } from "@asyncapi/parser";
import { describe, expect, it } from "vitest";
import { coveredSendOperations, type OutboxRecord, toOutgoingMessage, UnmappedEvent } from "./outbound-messages.js";
import { loadSendCheck } from "./send-check.js";

// Outbound messaging — increment 0, step 10, unit 10.1: outbox record → message of the AsyncAPI send operation (pure).

const BASE_URL = "https://larder.org/cooking-assistance";

/** What step 7 stores for the Sunday scones request (payload under the AsyncAPI field names). */
const helpRequestedRecord: OutboxRecord = {
  messageId: "5d7e9f1a-2b3c-4d5e-8f6a-7b8c9d0e1f2a",
  aggregateType: "HelpRequest",
  aggregateId: "0192f1a0-0000-7000-8000-000000000001",
  eventType: "HelpRequested",
  payload: {
    helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
    requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
    title: "Sunday scones, step 3",
    topic: "PREPARATION_STEP",
    description: "What means 'warm'?",
    recipeId: "c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f",
    howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
    addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
    pictures: [],
    status: "REQUESTED",
    answerDeadline: "2026-10-24T15:00:00.000Z",
    requestedAt: "2026-10-24T10:00:00.000Z",
  },
  occurredAt: new Date("2026-10-24T10:00:00Z"),
};

/** What step 7 stores for Grandma Avatar's answer. */
const helpProvidedRecord: OutboxRecord = {
  messageId: "6e8f0a2b-3c4d-4e5f-9a6b-7c8d9e0f1a2b",
  aggregateType: "Help",
  aggregateId: "0192f1a0-0000-7000-8000-0000000000a1",
  eventType: "HelpProvided",
  payload: {
    helpId: "0192f1a0-0000-7000-8000-0000000000a1",
    helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
    helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
    answerTitle: "Warm means about 30 °C",
    helpProviderType: "GRANDMA_AVATAR",
    helpProvider: null,
    answer: {
      topic: "PREPARATION_STEP",
      recipeId: "c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f",
      howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
      description: "Put the bowl next to the heater or in the oven with only the light on.",
      images: [],
    },
    status: "PROVIDED",
    providedAt: "2026-10-24T12:00:00.000Z",
  },
  occurredAt: new Date("2026-10-24T12:00:00Z"),
};

describe("outbound messages", () => {
  it("a HelpRequested record: exchange cook-assistance, routing key help.requested, headers messageId (the row's), correlationId (the help request id), source cook-assistance, the stored payload plus helpRequestLink -- op publishHelpRequested B2-S01 G1 G3", () => {
    expect(toOutgoingMessage(helpRequestedRecord, BASE_URL)).toEqual({
      exchange: "cook-assistance",
      routingKey: "help.requested",
      headers: {
        messageId: "5d7e9f1a-2b3c-4d5e-8f6a-7b8c9d0e1f2a",
        correlationId: "0192f1a0-0000-7000-8000-000000000001",
        source: "cook-assistance",
      },
      payload: {
        ...helpRequestedRecord.payload,
        helpRequestLink: `${BASE_URL}/help-requests/0192f1a0-0000-7000-8000-000000000001`,
      },
    });
  });
  it("a HelpProvided record: routing key help.provided, correlationId = its helpRequestId, the stored payload plus helpLink and helpRequestLink -- op publishHelpProvided B1-S04 G1 G3", () => {
    expect(toOutgoingMessage(helpProvidedRecord, BASE_URL)).toEqual({
      exchange: "cook-assistance",
      routingKey: "help.provided",
      headers: {
        messageId: "6e8f0a2b-3c4d-4e5f-9a6b-7c8d9e0f1a2b",
        correlationId: "0192f1a0-0000-7000-8000-000000000001",
        source: "cook-assistance",
      },
      payload: {
        ...helpProvidedRecord.payload,
        helpLink: `${BASE_URL}/helps/0192f1a0-0000-7000-8000-0000000000a1`,
        helpRequestLink: `${BASE_URL}/help-requests/0192f1a0-0000-7000-8000-000000000001`,
      },
    });
  });
  it("the message validates against the spec's send schema; without helpRequestLink it does not, nor with requestedAt 'yesterday' (formats are checked) -- ADR-CA-011", async () => {
    const check = await loadSendCheck();
    const message = toOutgoingMessage(helpRequestedRecord, BASE_URL);
    const { helpRequestLink: _omitted, ...withoutLink } = message.payload;

    expect(check(message)).toBe(true);
    expect(check({ ...message, payload: withoutLink })).toBe(false);
    expect(check({ ...message, payload: { ...message.payload, requestedAt: "yesterday" } })).toBe(false);
  });
  it("an event type without a publish decision fails with UnmappedEvent -- G5", () => {
    expect(() => toOutgoingMessage({ ...helpRequestedRecord, eventType: "HelpRequestArchived" }, BASE_URL)).toThrow(UnmappedEvent);
  });
  it("gate: every send operation of the spec is mapped or planned, and the publish decisions cover every domain event type -- ADR-CA-011 G5", async () => {
    const spec = await readFile(fileURLToPath(new URL("../../../../../contracts/asyncapi/cook-assistance.asyncapi.yaml", import.meta.url)), "utf8");
    const { document } = await new Parser().parse(spec);
    const sendOperations = (document?.operations().filterBySend() ?? []).map((operation) => operation.id());

    expect(coveredSendOperations().sort()).toEqual(sendOperations.sort());
  });
});
