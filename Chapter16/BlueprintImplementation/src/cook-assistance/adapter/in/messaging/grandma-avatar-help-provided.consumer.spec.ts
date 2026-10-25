import { describe, it } from "vitest";

// Inbound messaging — increment 0, step 9, unit 9.3: op receiveGrandmaAvatarHelpProvided (cook-assistance.asyncapi.yaml 2.0.0 r2)
// → ACL → ProvideHelpWhenGrandmaAvatarAnswers. Raw messages as the transport hands them over; a fake channel for ack/nack.

describe("op receiveGrandmaAvatarHelpProvided", () => {
  it.todo("a valid message: the handler receives our input (helpRequest → helpRequestId, PREPARATION_STEP_EXPLANATION → PREPARATION_STEP, absent images → []), outcome HELP_PROVIDED, the message is acked -- op receiveGrandmaAvatarHelpProvided B1-S04");
  it.todo("a payload without helpId: InvalidMessage, handler not called, dead-lettered (nack without requeue) -- op receiveGrandmaAvatarHelpProvided");
  it.todo("a header without messageId: InvalidMessage, handler not called, dead-lettered -- op receiveGrandmaAvatarHelpProvided");
  it.todo("helpId not a uuid (format): InvalidMessage, handler not called -- op receiveGrandmaAvatarHelpProvided ADR-CA-011");
  it.todo("source other than grandma-avatar: InvalidMessage, handler not called, dead-lettered -- op receiveGrandmaAvatarHelpProvided ADR-CA-008");
  it.todo("answerType MENU_PROPOSAL: outcome REFUSED TopicNotYetSupported, handler not called, acked -- op receiveGrandmaAvatarHelpProvided F5");
  it.todo("the same message twice: the second outcome is ALREADY_PROCESSED, both acked -- op receiveGrandmaAvatarHelpProvided ADR-CA-004");
  it.todo("any other error: nacked with requeue, so the quorum queue's delivery limit (5) dead-letters it -- op receiveGrandmaAvatarHelpProvided ADR-CA-004");
  it.todo("the outcome is logged as a structured event with message id, outcome and rule, without question or answer text -- op receiveGrandmaAvatarHelpProvided ADR-CA-010");
  it.todo("gate: every receive operation of the AsyncAPI spec has a registered consumer -- ADR-CA-011");
});
