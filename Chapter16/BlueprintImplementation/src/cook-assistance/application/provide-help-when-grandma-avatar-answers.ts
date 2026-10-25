import type { HelpRequest, HelpRequestId } from "../domain/help-request/help-request.js";
import {
  type AnsweredHelpRequest,
  Help,
  type HelpId,
  type HelpProvider,
  type PreparationStepExplanation,
  type ProvideHelp,
} from "../domain/help/help.js";
import { ConcurrencyConflict } from "../domain/shared/concurrency-conflict.js";
import { DomainRejection } from "../domain/shared/domain-rejection.js";
import type { Clock } from "./ports/clock.js";
import { MessageAlreadyProcessed, type MessageId } from "./ports/processed-messages.js";
import type { TransactionScope, UnitOfWork } from "./ports/unit-of-work.js";

/** The Grandma Avatar AI's help in Cook Assistance's own language (the ACL translates the message, step 9). */
export interface GrandmaAvatarAnswered {
  readonly messageId: MessageId;
  readonly helpId: HelpId;
  readonly helpRequestId: HelpRequestId;
  readonly answerTitle: string;
  readonly answer: PreparationStepExplanation;
}

/** The named outcome of one message; the inbound adapter logs it and acknowledges the message. */
export type GrandmaAvatarAnswerOutcome =
  | { readonly outcome: "HELP_PROVIDED"; readonly helpId: HelpId }
  | { readonly outcome: "ALREADY_PROCESSED" }
  | { readonly outcome: "REFUSED"; readonly because: string; readonly rule: string | null };

export interface ProvideHelpWhenGrandmaAvatarAnswersPorts {
  readonly unitOfWork: UnitOfWork;
  readonly clock: Clock;
}

/** What a help keeps of the request it answers (INV-16): its identity, requester and what was asked. */
function answeredRequestOf(helpRequest: HelpRequest): AnsweredHelpRequest {
  return {
    id: helpRequest.id,
    requester: helpRequest.requester,
    topic: helpRequest.topic,
    recipeId: helpRequest.recipeId,
    howToStepId: helpRequest.howToStepId,
  };
}

/** The provider who answers through this policy: the Grandma Avatar AI, which has no provider id. */
const grandmaAvatar: HelpProvider = { type: "GRANDMA_AVATAR", id: null };
const MAX_ATTEMPTS = 3;

/** Provide Help, with a domain rejection (INV-10) handed back as a value; every other error is thrown. */
function provideOrRejection(command: ProvideHelp): Help | DomainRejection {
  try {
    return Help.provide(command);
  } catch (error) {
    if (error instanceof DomainRejection) {
      return error;
    }
    throw error;
  }
}

type Refused = Extract<GrandmaAvatarAnswerOutcome, { outcome: "REFUSED" }>;

/** Step 8 decision · a refused help is acknowledged: nothing is stored, but the message counts as handled. */
async function refuse(
  scope: TransactionScope,
  message: GrandmaAvatarAnswered,
  reason: Omit<Refused, "outcome">,
): Promise<Refused> {
  await scope.processedMessages.record(message.messageId);
  return { outcome: "REFUSED", ...reason };
}

// policy: P2 · on: Grandma Avatar AI HelpProvided · cmd: Provide Help · agg: Help · slice B1-S04
export class ProvideHelpWhenGrandmaAvatarAnswers {
  constructor(private readonly ports: ProvideHelpWhenGrandmaAvatarAnswersPorts) {}

  async handle(message: GrandmaAvatarAnswered): Promise<GrandmaAvatarAnswerOutcome> {
    // ADR-CA-005 · a policy handler reloads and retries a conflict up to 3 times, then lets the message fail
    for (let attempt = 1; ; attempt += 1) {
      try {
        return await this.handleOnce(message);
      } catch (error) {
        const anotherWriterWasFirst = error instanceof ConcurrencyConflict || error instanceof MessageAlreadyProcessed;
        if (!anotherWriterWasFirst || attempt === MAX_ATTEMPTS) {
          throw error;
        }
      }
    }
  }

  private async handleOnce(message: GrandmaAvatarAnswered): Promise<GrandmaAvatarAnswerOutcome> {
    return this.ports.unitOfWork.transactionally(async (scope) => {
      // ADR-CA-004 · idempotent consumer: a message handled before has no second effect
      if (await scope.processedMessages.hasBeenProcessed(message.messageId)) {
        return { outcome: "ALREADY_PROCESSED" };
      }
      const helpRequest = await scope.helpRequests.findById(message.helpRequestId);
      if (helpRequest === undefined) {
        return refuse(scope, message, { because: "help request not found", rule: null });
      }
      // INV-09 · only an addressee may answer (resource check in the application, ADR-CA-009)
      if (!helpRequest.addressees.includes(grandmaAvatar.type)) {
        return refuse(scope, message, { because: "not an addressee", rule: "INV-09" });
      }
      const help = provideOrRejection({
        id: message.helpId,
        helpRequest: answeredRequestOf(helpRequest),
        answerTitle: message.answerTitle,
        provider: grandmaAvatar,
        answer: message.answer,
        providedAt: this.ports.clock.now(),
      });
      if (help instanceof DomainRejection) {
        return refuse(scope, message, { because: help.name, rule: help.rule });
      }
      await scope.helps.save(help);
      await scope.processedMessages.record(message.messageId);
      return { outcome: "HELP_PROVIDED", helpId: help.id };
    });
  }
}
