/** Identity of a consumed message (its AsyncAPI header messageId). */
export type MessageId = string & { readonly __brand: "MessageId" };
export const MessageId = { of: (value: string) => value as MessageId };

/** Remembers which consumed messages were already handled, so a redelivered message has one effect (ADR-CA-004). */
export interface ProcessedMessages {
  hasBeenProcessed(messageId: MessageId): Promise<boolean>;

  /** Throws MessageAlreadyProcessed when the message was recorded before. */
  record(messageId: MessageId): Promise<void>;
}

/** The message was recorded before — a redelivery, or a concurrent consumer got there first (ADR-CA-004). */
export class MessageAlreadyProcessed extends Error {
  constructor(messageId: MessageId) {
    super(`Message ${messageId} was already processed.`);
    this.name = "MessageAlreadyProcessed";
  }
}
