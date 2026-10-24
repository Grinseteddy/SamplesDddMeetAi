import { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import type { HelpRequestEvent } from "./help-request-events.js";
import {
  AddresseeRequired,
  ChefOnlyForMenu,
  QuestionRequired,
  RecipeRequired,
  StepRequired,
} from "./help-request-rejections.js";

/** Identity of a help request. */
export type HelpRequestId = string & { readonly __brand: "HelpRequestId" };
export const HelpRequestId = { of: (value: string) => value as HelpRequestId };

export interface RequestPreparationStepHelp {
  readonly id: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly question: string;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
  readonly addressees: readonly HelpProviderType[];
  readonly requestedAt: Date;
}

/** Who may answer a help request (glossary "help provider"). */
export type HelpProviderType = "COMMUNITY" | "GRANDMA_AVATAR" | "CHEF";

export type HelpRequestStatus = "REQUESTED";

/** What a help request is about (discriminator "topic"); only a preparation step in this slice. */
export type HelpRequestTopic = "PREPARATION_STEP";

// INV-19 · a help request carries the cook's question (blank counts as missing)
function ensureQuestion(question: string): void {
  if (question.trim() === "") {
    throw new QuestionRequired();
  }
}

// INV-20 · a help request names who may answer: at least one addressee, never a Chef (Menu only)
function ensureAddressees(addressees: readonly HelpProviderType[]): void {
  if (addressees.length === 0) {
    throw new AddresseeRequired();
  }
  if (addressees.includes("CHEF")) {
    throw new ChefOnlyForMenu();
  }
}

// INV-17 · a preparation-step request is about one recipe and exactly one step of it
function ensureRecipeAndStep(
  recipeId: RecipeId | null,
  howToStepId: HowToStepId | null,
): { readonly recipeId: RecipeId; readonly howToStepId: HowToStepId } {
  if (recipeId === null) {
    throw new RecipeRequired();
  }
  if (howToStepId === null) {
    throw new StepRequired();
  }
  return { recipeId, howToStepId };
}

const HOUR_IN_MS = 60 * 60 * 1000;

// INV-07 · F6: a preparation-step request waits 5 h for an answer the cook accepts
const PREPARATION_STEP_ANSWER_TIME_MS = 5 * HOUR_IN_MS;

// INV-07 · answer deadline = request time + the topic's answer time
function preparationStepAnswerDeadline(requestedAt: Date): Date {
  return new Date(requestedAt.getTime() + PREPARATION_STEP_ANSWER_TIME_MS);
}

/** The stored state of a help request (persistence maps rows to this and back). */
export interface HelpRequestState {
  readonly id: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly question: string;
  readonly recipeId: RecipeId;
  readonly howToStepId: HowToStepId;
  readonly addressees: readonly HelpProviderType[];
  readonly status: HelpRequestStatus;
  readonly answerDeadline: Date;
  readonly version: number;
}

export class HelpRequest {
  readonly topic: HelpRequestTopic = "PREPARATION_STEP";

  private readonly pending: HelpRequestEvent[] = [];

  private constructor(private readonly state: HelpRequestState) {}

  // cmd: Request help preparation step · slice B2-S01
  static requestPreparationStepHelp(command: RequestPreparationStepHelp): HelpRequest {
    ensureQuestion(command.question);
    ensureAddressees(command.addressees);
    const { recipeId, howToStepId } = ensureRecipeAndStep(command.recipeId, command.howToStepId);
    const helpRequest = new HelpRequest({
      id: command.id,
      requester: command.requester,
      title: command.title,
      question: command.question,
      recipeId,
      howToStepId,
      addressees: command.addressees,
      status: "REQUESTED",
      answerDeadline: preparationStepAnswerDeadline(command.requestedAt),
      version: 0,
    });
    helpRequest.announceHelpRequested(command.requestedAt);
    return helpRequest;
  }

  /** Rebuilds a stored help request: no guards, no events (ADR-CA-003). */
  static reconstitute(stored: HelpRequestState): HelpRequest {
    return new HelpRequest(stored);
  }

  get id(): HelpRequestId {
    return this.state.id;
  }

  get requester(): CookId {
    return this.state.requester;
  }

  get title(): string {
    return this.state.title;
  }

  get question(): string {
    return this.state.question;
  }

  get recipeId(): RecipeId {
    return this.state.recipeId;
  }

  get howToStepId(): HowToStepId {
    return this.state.howToStepId;
  }

  get addressees(): readonly HelpProviderType[] {
    return this.state.addressees;
  }

  get status(): HelpRequestStatus {
    return this.state.status;
  }

  // INV-07 · the system cancels the request when no accepted answer arrives by this instant
  get answerDeadline(): Date {
    return this.state.answerDeadline;
  }

  // evt: Help requested · AsyncAPI HelpRequested
  private announceHelpRequested(requestedAt: Date): void {
    this.pending.push({
      type: "HelpRequested",
      helpRequestId: this.id,
      requester: this.requester,
      title: this.title,
      topic: this.topic,
      question: this.question,
      recipeId: this.recipeId,
      howToStepId: this.howToStepId,
      addressees: this.addressees,
      status: this.status,
      requestedAt,
      answerDeadline: this.answerDeadline,
    });
  }

  /** Hands the recorded events to the application (which stores them in the outbox) and forgets them. */
  pullEvents(): readonly HelpRequestEvent[] {
    return this.pending.splice(0);
  }

  /** Optimistic-locking version (ADR-CA-005); the aggregate never increments it. */
  get version(): number {
    return this.state.version;
  }
}
