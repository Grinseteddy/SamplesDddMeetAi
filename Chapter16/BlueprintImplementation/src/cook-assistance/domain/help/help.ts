import type { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import type { HelpProviderType, HelpRequestId, HelpRequestTopic } from "../help-request/help-request.js";
import type { HelpEvent } from "./help-events.js";
import { HelpNotProvided, OwnRequestNotAnswerable, RecipeNotAsInRequest, StepNotAsInRequest } from "./help-rejections.js";

/** Identity of a help. */
export type HelpId = string & { readonly __brand: "HelpId" };
export const HelpId = { of: (value: string) => value as HelpId };

/** PROVIDED until the requester decides: ACCEPTED or REJECTED, both terminal (INV-13, INV-15). */
export type HelpStatus = "PROVIDED" | "ACCEPTED" | "REJECTED";

/** The immutable facts of the help request a help answers, handed over by the application. */
export interface AnsweredHelpRequest {
  readonly id: HelpRequestId;
  readonly requester: CookId;
  readonly topic: HelpRequestTopic;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
}

/** Who answered: the community, the Grandma Avatar (no cook id) or a Chef. */
export interface HelpProvider {
  readonly type: HelpProviderType;
  readonly id: CookId | null;
}

/** The answer to a preparation-step request: how to perform the one recipe step. */
export interface PreparationStepExplanation {
  readonly topic: "PREPARATION_STEP";
  readonly recipeId: RecipeId;
  readonly howToStepId: HowToStepId;
  readonly description: string;
  readonly images: readonly string[];
}

/** The meal a menu proposal is meant for. */
export type Meal = "BREAKFAST" | "LUNCH" | "DINNER" | "SUPPER";

/** One course of a menu: the serving step it belongs to and the recipe to cook. */
export interface Course {
  readonly dish: number;
  readonly recipeId: RecipeId;
}

/** The answer to a Menu request: a sequence of courses for a meal. */
export interface MenuProposal {
  readonly topic: "MENU";
  readonly note: string;
  readonly servings: number;
  readonly meal: Meal;
  readonly howToServe: string | null;
  readonly courses: readonly Course[];
}

/** What a help answers with, by topic. */
export type Answer = PreparationStepExplanation | MenuProposal;

export interface ProvideHelp {
  readonly id: HelpId;
  readonly helpRequest: AnsweredHelpRequest;
  readonly answerTitle: string;
  readonly provider: HelpProvider;
  readonly answer: Answer;
  readonly providedAt: Date;
}

/** The stored state of a help (persistence maps rows to this and back). */
export interface HelpState {
  readonly id: HelpId;
  readonly helpRequestId: HelpRequestId;
  readonly helpRequester: CookId;
  readonly answerTitle: string;
  readonly provider: HelpProvider;
  readonly answer: Answer;
  readonly status: HelpStatus;
  readonly version: number;
}

// INV-10 · a help is about what was asked: the same recipe and step as its help request
function ensureAnswersTheRequest(helpRequest: AnsweredHelpRequest, answer: Answer): void {
  // INV-10 · an explanation of a step answers the request's recipe and step
  if (answer.topic !== "PREPARATION_STEP") {
    return;
  }
  if (answer.recipeId !== helpRequest.recipeId) {
    throw new RecipeNotAsInRequest();
  }
  if (answer.howToStepId !== helpRequest.howToStepId) {
    throw new StepNotAsInRequest();
  }
}

export class Help {
  private readonly pending: HelpEvent[] = [];

  private constructor(private state: HelpState) {}

  // cmd: Provide Help · slices B1-S03, B1-S04
  static provide(command: ProvideHelp): Help {
    // INV-25 · Q3-1: a cook cannot answer their own request
    if (command.provider.id === command.helpRequest.requester) {
      throw new OwnRequestNotAnswerable();
    }
    ensureAnswersTheRequest(command.helpRequest, command.answer);
    const help = new Help({
      id: command.id,
      helpRequestId: command.helpRequest.id,
      helpRequester: command.helpRequest.requester,
      answerTitle: command.answerTitle,
      provider: command.provider,
      answer: command.answer,
      status: "PROVIDED",
      version: 0,
    });
    help.announceHelpProvided(command.providedAt);
    return help;
  }

  // cmd: Accept help · slice B1-S05 · evt: Help accepted — the requester check is the application's (INV-13, ADR-CA-009)
  accept(decidedAt: Date): void {
    this.decide("ACCEPTED", "HelpAccepted", decidedAt);
  }

  // cmd: Reject help · slice B1-S09 · evt: Help rejected — the requester check is the application's (INV-15, ADR-CA-009)
  reject(decidedAt: Date): void {
    this.decide("REJECTED", "HelpRejected", decidedAt);
  }

  // INV-13, INV-15 · the requester's one decision on a PROVIDED help · AsyncAPI HelpDecidedPayload
  private decide(status: "ACCEPTED" | "REJECTED", type: "HelpAccepted" | "HelpRejected", decidedAt: Date): void {
    this.ensureProvided();
    this.state = { ...this.state, status };
    this.pending.push({
      type,
      helpId: this.id,
      helpRequestId: this.helpRequestId,
      helpRequester: this.helpRequester,
      helpProviderType: this.provider.type,
      helpProvider: this.provider.id,
      status: this.status,
      decidedAt,
    });
  }

  // INV-13, INV-15 · a help is decided once: only a PROVIDED help can be accepted or rejected
  private ensureProvided(): void {
    if (this.status !== "PROVIDED") {
      throw new HelpNotProvided();
    }
  }

  /** Rebuilds a stored help: no guards, no events (ADR-CA-003). */
  static reconstitute(stored: HelpState): Help {
    return new Help(stored);
  }

  get id(): HelpId {
    return this.state.id;
  }

  get helpRequestId(): HelpRequestId {
    return this.state.helpRequestId;
  }

  get helpRequester(): CookId {
    return this.state.helpRequester;
  }

  get answerTitle(): string {
    return this.state.answerTitle;
  }

  get provider(): HelpProvider {
    return this.state.provider;
  }

  get answer(): Answer {
    return this.state.answer;
  }

  get status(): HelpStatus {
    return this.state.status;
  }

  // evt: Help provided · AsyncAPI HelpProvided
  private announceHelpProvided(providedAt: Date): void {
    this.pending.push({
      type: "HelpProvided",
      helpId: this.id,
      helpRequestId: this.helpRequestId,
      helpRequester: this.helpRequester,
      answerTitle: this.answerTitle,
      helpProviderType: this.provider.type,
      helpProvider: this.provider.id,
      answer: this.answer,
      status: this.status,
      providedAt,
    });
  }

  /** Hands the recorded events to the application (which stores them in the outbox) and forgets them. */
  pullEvents(): readonly HelpEvent[] {
    return this.pending.splice(0);
  }

  /** Optimistic-locking version (ADR-CA-005); the aggregate never increments it. */
  get version(): number {
    return this.state.version;
  }
}
