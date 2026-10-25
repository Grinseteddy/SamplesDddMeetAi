import type { HelpRequestEvent } from "../../../domain/help-request/help-request-events.js";
import type { HelpEvent } from "../../../domain/help/help-events.js";

/** An outbox row as the relay reads it (step 7's columns plus migration 005's message id). */
export interface OutboxRecord {
  readonly messageId: string;
  readonly aggregateType: string;
  readonly aggregateId: string;
  readonly eventType: string;
  readonly payload: Readonly<Record<string, unknown>>;
  readonly occurredAt: Date;
}

/** A message as the publisher sends it (AsyncAPI 2.0.0: topic exchange, routing key, MessageHeader, payload). */
export interface OutgoingMessage {
  readonly exchange: string;
  readonly routingKey: string;
  readonly headers: { readonly messageId: string; readonly correlationId: string; readonly source: string };
  readonly payload: Readonly<Record<string, unknown>>;
}

/** An outbox row whose event type has no publish decision (G5): never published, never silently internal. */
export class UnmappedEvent extends Error {
  constructor(readonly eventType: string) {
    super(`no publish decision for the event type ${eventType}`);
    this.name = "UnmappedEvent";
  }
}

/** The topic exchange Cook Assistance publishes on, and the source it names in every header (AsyncAPI 2.0.0). */
const EXCHANGE = "cook-assistance";
const SOURCE = "cook-assistance";

/** Every event type the domain raises: a new one fails type checking until it has a publish decision. */
type DomainEventType = (HelpRequestEvent | HelpEvent)["type"];

interface EventMapper {
  readonly operation: string;
  readonly routingKey: string;
  /** The correlation id and the payload with its links (G1, G3). */
  map(payload: Readonly<Record<string, unknown>>, publicBaseUrl: string): {
    readonly correlationId: string;
    readonly payload: Readonly<Record<string, unknown>>;
  };
}

// G5 · one publish decision per domain event type
const EVENT_MESSAGES = {
  // op: publishHelpRequested · channel help.requested · evt: HelpRequested
  HelpRequested: {
    operation: "publishHelpRequested",
    routingKey: "help.requested",
    map: (payload, publicBaseUrl) => {
      const helpRequestId = String(payload.helpRequestId);
      return { correlationId: helpRequestId, payload: { ...payload, helpRequestLink: `${publicBaseUrl}/help-requests/${helpRequestId}` } };
    },
  },
  // op: publishHelpProvided · channel help.provided · evt: HelpProvided
  HelpProvided: {
    operation: "publishHelpProvided",
    routingKey: "help.provided",
    map: (payload, publicBaseUrl) => {
      const helpRequestId = String(payload.helpRequestId);
      return {
        correlationId: helpRequestId,
        payload: {
          ...payload,
          helpLink: `${publicBaseUrl}/helps/${String(payload.helpId)}`,
          helpRequestLink: `${publicBaseUrl}/help-requests/${helpRequestId}`,
        },
      };
    },
  },
} satisfies Record<DomainEventType, EventMapper>;

function isDecided(eventType: string): eventType is keyof typeof EVENT_MESSAGES {
  return Object.hasOwn(EVENT_MESSAGES, eventType);
}

export function toOutgoingMessage(record: OutboxRecord, publicBaseUrl: string): OutgoingMessage {
  if (!isDecided(record.eventType)) {
    throw new UnmappedEvent(record.eventType);
  }
  const mapper: EventMapper = EVENT_MESSAGES[record.eventType];
  const { correlationId, payload } = mapper.map(record.payload, publicBaseUrl);
  return {
    exchange: EXCHANGE,
    routingKey: mapper.routingKey,
    // G3 · the help request is the cook's journey the correlation id traces
    headers: { messageId: record.messageId, correlationId, source: SOURCE },
    payload,
  };
}

/** Send operations of the spec no event feeds yet — each names where it is built (ADR-CA-011 coverage gate). */
const PLANNED_SEND_OPERATIONS = {
  publishChefReviewRequested: "planned: increment 1, B1-S02",
  publishHelpAccepted: "planned: increment 1, B1-S05",
  publishHelpRequestCancelled: "planned: increment 1, B1-S06/S07",
  publishHelpRequestClosed: "planned: increment 1, B1-S08",
  publishHelpRejected: "planned: increment 1, B1-S09",
} as const;

export function coveredSendOperations(): string[] {
  return [...Object.values(EVENT_MESSAGES).map((mapper) => mapper.operation), ...Object.keys(PLANNED_SEND_OPERATIONS)];
}
