import type { DomainEvent } from "../../../domain/domain-events.js";

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
export const PUBLISH_EXCHANGE = "cook-assistance";
const SOURCE = "cook-assistance";

/** Every event type the domain raises: a new one fails type checking until it has a publish decision. */
type DomainEventType = DomainEvent["type"];

interface EventMapper {
  readonly operation: string;
  readonly routingKey: string;
  /** The stored payload plus its links (G1). */
  withLinks(payload: Readonly<Record<string, unknown>>, publicBaseUrl: string): Readonly<Record<string, unknown>>;
}

/** G1 · the help request the event belongs to, as a resource of the Cook Assistance API. */
function helpRequestLinkOf(payload: Readonly<Record<string, unknown>>, publicBaseUrl: string): string {
  return `${publicBaseUrl}/help-requests/${String(payload.helpRequestId)}`;
}

/** Domain events whose publish decision a later step of this increment makes — each names that step (G5). */
const PLANNED_EVENTS = {
  ChefReviewRequested: "planned: increment 1, step 10 (B1-S02)",
  HelpRequestCancelled: "planned: increment 1, step 10 (B1-S06, B1-S07)",
  HelpRequestClosed: "planned: increment 1, step 10 (B1-S08)",
  AnswerDeadlineRestarted: "planned: increment 1, step 10 (B1-S10; internal, no AsyncAPI message)",
  HelpAccepted: "planned: increment 1, step 10 (B1-S05)",
  HelpRejected: "planned: increment 1, step 10 (B1-S09)",
} as const satisfies Partial<Record<DomainEventType, string>>;

// G5 · one publish decision per domain event type, or a planned entry above
const EVENT_MESSAGES = {
  // op: publishHelpRequested · channel help.requested · evt: HelpRequested
  HelpRequested: {
    operation: "publishHelpRequested",
    routingKey: "help.requested",
    withLinks: (payload, publicBaseUrl) => ({ ...payload, helpRequestLink: helpRequestLinkOf(payload, publicBaseUrl) }),
  },
  // op: publishHelpProvided · channel help.provided · evt: HelpProvided
  HelpProvided: {
    operation: "publishHelpProvided",
    routingKey: "help.provided",
    withLinks: (payload, publicBaseUrl) => ({
      ...payload,
      helpLink: `${publicBaseUrl}/helps/${String(payload.helpId)}`,
      helpRequestLink: helpRequestLinkOf(payload, publicBaseUrl),
    }),
  },
} satisfies Record<Exclude<DomainEventType, keyof typeof PLANNED_EVENTS>, EventMapper>;

/** G3 · the help request is the cook's journey the correlation id traces — for every message, whatever its event. */
function journeyOf(payload: Readonly<Record<string, unknown>>): string {
  return String(payload.helpRequestId);
}

function isDecided(eventType: string): eventType is keyof typeof EVENT_MESSAGES {
  return Object.hasOwn(EVENT_MESSAGES, eventType);
}

export function toOutgoingMessage(record: OutboxRecord, publicBaseUrl: string): OutgoingMessage {
  if (!isDecided(record.eventType)) {
    throw new UnmappedEvent(record.eventType);
  }
  const mapper: EventMapper = EVENT_MESSAGES[record.eventType];
  return {
    exchange: PUBLISH_EXCHANGE,
    routingKey: mapper.routingKey,
    headers: { messageId: record.messageId, correlationId: journeyOf(record.payload), source: SOURCE },
    payload: mapper.withLinks(record.payload, publicBaseUrl),
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
