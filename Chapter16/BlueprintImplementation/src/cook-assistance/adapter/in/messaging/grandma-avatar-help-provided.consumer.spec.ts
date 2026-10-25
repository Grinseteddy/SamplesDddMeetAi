import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { Parser } from "@asyncapi/parser";
import { beforeEach, describe, expect, it } from "vitest";
import {
  type GrandmaAvatarAnswered,
  ProvideHelpWhenGrandmaAvatarAnswers,
} from "../../../application/provide-help-when-grandma-avatar-answers.js";
import { HelpRequest } from "../../../domain/help-request/help-request.js";
import { FixedClock } from "../../../test-support/fakes/fixed-clock.js";
import { InMemoryUnitOfWork } from "../../../test-support/fakes/in-memory-unit-of-work.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "../../../test-support/fixtures/sunday-scones.js";
import {
  type ConsumerLogEvent,
  createGrandmaAvatarHelpProvidedConsumer,
  type Delivery,
  type GrandmaAvatarHelpProvidedConsumer,
  type RawMessage,
} from "./grandma-avatar-help-provided.consumer.js";
import { CONSUMERS } from "./consumers.js";

// Inbound messaging — increment 0, step 9, unit 9.3: op receiveGrandmaAvatarHelpProvided (cook-assistance.asyncapi.yaml 2.0.0 r2)
// → ACL → ProvideHelpWhenGrandmaAvatarAnswers. Raw messages as the transport hands them over; a fake delivery for ack/nack.

/** The avatar explains step 3 of Sunday scones — in its published language (AsyncAPI example warmExplainedByAvatar). */
const warmExplainedByAvatar: RawMessage = {
  headers: {
    correlationId: "0b1c2d3e-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
    messageId: "8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d",
    source: "grandma-avatar",
  },
  payload: {
    helpId: grandmaExplainsWarm.id,
    helpRequest: sundaySconesStep3.id,
    helpRequester: sundaySconesStep3.requester,
    answerTitle: grandmaExplainsWarm.answerTitle,
    helpProviderType: "GRANDMA_AVATAR",
    answer: {
      answerType: "PREPARATION_STEP_EXPLANATION",
      recipeId: sundaySconesStep3.recipeId,
      howToStepId: sundaySconesStep3.howToStepId,
      description: grandmaExplainsWarm.answer.description,
    },
  },
};

/** Records how the consumer settled a message. */
class RecordingDelivery implements Delivery {
  readonly settled: string[] = [];
  ack(): void {
    this.settled.push("ack");
  }
  deadLetter(): void {
    this.settled.push("dead-letter");
  }
  retry(): void {
    this.settled.push("retry");
  }
}

describe("op receiveGrandmaAvatarHelpProvided", () => {
  let consumer: GrandmaAvatarHelpProvidedConsumer;
  let unitOfWork: InMemoryUnitOfWork;
  let received: GrandmaAvatarAnswered[];
  let logged: ConsumerLogEvent[];

  beforeEach(async () => {
    unitOfWork = new InMemoryUnitOfWork();
    await unitOfWork.helpRequests.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    const policy = new ProvideHelpWhenGrandmaAvatarAnswers({ unitOfWork, clock: new FixedClock(grandmaExplainsWarm.providedAt) });
    received = [];
    logged = [];
    consumer = await createGrandmaAvatarHelpProvidedConsumer({
      handler: {
        handle: async (input) => {
          received.push(input);
          return policy.handle(input);
        },
      },
      log: (event) => logged.push(event),
    });
  });

  it("a valid message: the handler receives our input (helpRequest → helpRequestId, PREPARATION_STEP_EXPLANATION → PREPARATION_STEP, absent images → []), outcome HELP_PROVIDED, the message is acked -- op receiveGrandmaAvatarHelpProvided B1-S04", async () => {
    const delivery = new RecordingDelivery();

    await consumer.consume(warmExplainedByAvatar, delivery);

    expect(received).toEqual([
      {
        messageId: "8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d",
        helpId: "0192f1a0-0000-7000-8000-0000000000a1",
        helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
        answerTitle: "Warm means about 30 °C",
        answer: {
          topic: "PREPARATION_STEP",
          recipeId: "c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f",
          howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
          description: "Put the bowl next to the heater or in the oven with only the light on.",
          images: [],
        },
      },
    ]);
    expect(await unitOfWork.helps.findById(grandmaExplainsWarm.id)).toBeDefined();
    expect(delivery.settled).toEqual(["ack"]);
  });
  it("a payload without helpId: InvalidMessage, handler not called, dead-lettered (nack without requeue) -- op receiveGrandmaAvatarHelpProvided", async () => {
    const { helpId: _omitted, ...withoutHelpId } = warmExplainedByAvatar.payload as Record<string, unknown>;
    const delivery = new RecordingDelivery();

    await consumer.consume({ ...warmExplainedByAvatar, payload: withoutHelpId }, delivery);

    expect(received).toEqual([]);
    expect(delivery.settled).toEqual(["dead-letter"]);
  });
  it("a header without messageId: InvalidMessage, handler not called, dead-lettered -- op receiveGrandmaAvatarHelpProvided", async () => {
    const { messageId: _omitted, ...withoutMessageId } = warmExplainedByAvatar.headers;
    const delivery = new RecordingDelivery();

    await consumer.consume({ ...warmExplainedByAvatar, headers: withoutMessageId }, delivery);

    expect(received).toEqual([]);
    expect(delivery.settled).toEqual(["dead-letter"]);
  });
  it("helpId not a uuid (format): InvalidMessage, handler not called -- op receiveGrandmaAvatarHelpProvided ADR-CA-011", async () => {
    const delivery = new RecordingDelivery();

    await consumer.consume(
      { ...warmExplainedByAvatar, payload: { ...(warmExplainedByAvatar.payload as object), helpId: "help-42" } },
      delivery,
    );

    expect(received).toEqual([]);
    expect(delivery.settled).toEqual(["dead-letter"]);
  });
  it("source other than grandma-avatar: InvalidMessage, handler not called, dead-lettered -- op receiveGrandmaAvatarHelpProvided ADR-CA-008", async () => {
    const delivery = new RecordingDelivery();

    await consumer.consume({ ...warmExplainedByAvatar, headers: { ...warmExplainedByAvatar.headers, source: "community-app" } }, delivery);

    expect(received).toEqual([]);
    expect(delivery.settled).toEqual(["dead-letter"]);
  });
  it("answerType MENU_PROPOSAL: outcome REFUSED TopicNotYetSupported, handler not called, acked -- op receiveGrandmaAvatarHelpProvided F5", async () => {
    const delivery = new RecordingDelivery();

    await consumer.consume(
      { ...warmExplainedByAvatar, payload: { ...(warmExplainedByAvatar.payload as object), answer: { answerType: "MENU_PROPOSAL" } } },
      delivery,
    );

    expect(received).toEqual([]);
    expect(delivery.settled).toEqual(["ack"]);
  });
  it("the same message twice: the second outcome is ALREADY_PROCESSED, both acked -- op receiveGrandmaAvatarHelpProvided ADR-CA-004", async () => {
    const first = new RecordingDelivery();
    const second = new RecordingDelivery();

    await consumer.consume(warmExplainedByAvatar, first);
    await consumer.consume(warmExplainedByAvatar, second);

    expect([first.settled, second.settled]).toEqual([["ack"], ["ack"]]);
    expect(unitOfWork.helps.savedEvents.map((event) => event.type)).toEqual(["HelpProvided"]);
  });
  it("any other error: nacked with requeue, so the quorum queue's delivery limit (5) dead-letters it -- op receiveGrandmaAvatarHelpProvided ADR-CA-004", async () => {
    const failing = await createGrandmaAvatarHelpProvidedConsumer({
      handler: {
        handle: async () => {
          throw new Error("the database is unreachable");
        },
      },
      log: (event) => logged.push(event),
    });
    const delivery = new RecordingDelivery();

    await failing.consume(warmExplainedByAvatar, delivery);

    expect(delivery.settled).toEqual(["retry"]);
  });
  it("the outcome is logged as a structured event with message id, outcome and rule, without question or answer text -- op receiveGrandmaAvatarHelpProvided ADR-CA-010", async () => {
    await consumer.consume(warmExplainedByAvatar, new RecordingDelivery());

    expect(logged).toEqual([
      { event: "grandma-avatar-help-provided", messageId: "8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d", outcome: "HELP_PROVIDED", rule: null },
    ]);
    expect(JSON.stringify(logged)).not.toMatch(/warm|heater|scones/i);
  });
  it("gate: every receive operation of the AsyncAPI spec has a registered consumer -- ADR-CA-011", async () => {
    const spec = await readFile(fileURLToPath(new URL("../../../../../contracts/asyncapi/cook-assistance.asyncapi.yaml", import.meta.url)), "utf8");
    const { document } = await new Parser().parse(spec);
    const receiveOperations = (document?.operations().filterByReceive() ?? []).map((operation) => operation.id());

    expect(Object.keys(CONSUMERS).sort()).toEqual(receiveOperations.sort());
  });
});
