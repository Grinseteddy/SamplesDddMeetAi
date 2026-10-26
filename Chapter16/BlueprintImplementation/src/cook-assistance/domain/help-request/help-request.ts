import { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import type { HelpId } from "../help/help.js";
import type { HelpRequestEvent } from "./help-request-events.js";
import {
  AddresseeRequired,
  ChefOnlyForMenu,
  ChefReviewOnlyForMenu,
  ExactlyOneChef,
  HelpRequestNotRequested,
  MealDateRequired,
  QuestionRequired,
  RecipeRequired,
  StepRequired,
} from "./help-request-rejections.js";

/** Identity of a help request. */
export type HelpRequestId = string & { readonly __brand: "HelpRequestId" };
export const HelpRequestId = { of: (value: string) => value as HelpRequestId };

export interface PreparationStepHelpRequest {
  readonly id: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly question: string;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
  readonly addressees: readonly HelpProviderType[];
  readonly requestedAt: Date;
}

/** What a cook asks when planning a meal (Menu): community and/or Grandma Avatar, or one Chef. */
export interface MealPlanHelpRequest {
  readonly id: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly question: string;
  readonly addressees: readonly HelpProviderType[];
  readonly chefId: CookId | null;
  readonly plannedMealDate: string | null;
  readonly requestedAt: Date;
}

/** Who may answer a help request (glossary "help provider"). */
export type HelpProviderType = "COMMUNITY" | "GRANDMA_AVATAR" | "CHEF";

/** The brief's lifecycle: REQUESTED until closed by an accepted help or cancelled; both are terminal (INV-05). */
export type HelpRequestStatus = "REQUESTED" | "CLOSED" | "CANCELLED";

/** What a help request is about (discriminator "topic"). */
export type HelpRequestTopic = "PREPARATION_STEP" | "MENU";

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

// INV-03 · a Menu request names who may answer: at least one addressee
function ensureMenuAddressees(command: MealPlanHelpRequest): void {
  if (command.addressees.length === 0) {
    throw new AddresseeRequired();
  }
  const asksChef = command.addressees.includes("CHEF");
  // a Chef is asked exactly when one is named, and then alone
  if (asksChef !== (command.chefId !== null) || (asksChef && command.addressees.length > 1)) {
    throw new ExactlyOneChef();
  }
  if (asksChef && command.plannedMealDate === null) {
    throw new MealDateRequired();
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

// INV-07 · F6, Q3-2: how long a request waits for an answer the cook accepts — per topic
const ANSWER_TIME_MS: Readonly<Record<HelpRequestTopic, number>> = {
  PREPARATION_STEP: 5 * HOUR_IN_MS,
  MENU: 48 * HOUR_IN_MS,
};

// INV-07 · answer deadline = request time + the topic's answer time
function answerDeadline(topic: HelpRequestTopic, from: Date): Date {
  return new Date(from.getTime() + ANSWER_TIME_MS[topic]);
}

// INV-07 · EM-M R6: the deadline has passed from its very instant on (48 h after 10:00 is 10:00, not 10:01)
function answerDeadlinePassed(deadline: Date, now: Date): boolean {
  return now.getTime() >= deadline.getTime();
}

/** The stored state of a help request (persistence maps rows to this and back). */
export interface HelpRequestState {
  readonly id: HelpRequestId;
  readonly topic: HelpRequestTopic;
  readonly requester: CookId;
  readonly title: string;
  readonly question: string;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
  readonly addressees: readonly HelpProviderType[];
  readonly chefId: CookId | null;
  readonly plannedMealDate: string | null;
  readonly status: HelpRequestStatus;
  readonly answerDeadline: Date;
  readonly version: number;
}

/** What the cook asked for, once the topic's guards ran — the state before the lifecycle and the deadline are set. */
type RequestedHelp = Omit<HelpRequestState, "status" | "answerDeadline" | "version">;

/** A preparation-step request: its INV-17 guard ran, so recipe and step are present. */
export type PreparationStepHelpRequestState = HelpRequest & { readonly recipeId: RecipeId; readonly howToStepId: HowToStepId };

export class HelpRequest {
  private readonly pending: HelpRequestEvent[] = [];

  private constructor(private state: HelpRequestState) {}

  // cmd: Request help preparation step · slice B2-S01
  static requestPreparationStepHelp(command: PreparationStepHelpRequest): PreparationStepHelpRequestState {
    ensureQuestion(command.question);
    ensureAddressees(command.addressees);
    const { recipeId, howToStepId } = ensureRecipeAndStep(command.recipeId, command.howToStepId);
    const helpRequest = HelpRequest.requestHelp(
      {
        id: command.id,
        topic: "PREPARATION_STEP",
        requester: command.requester,
        title: command.title,
        question: command.question,
        recipeId,
        howToStepId,
        addressees: command.addressees,
        chefId: null,
        plannedMealDate: null,
      },
      command.requestedAt,
    );
    return helpRequest as PreparationStepHelpRequestState;
  }

  // cmd: Request help meal plan · slice B1-S01
  static requestMealPlanHelp(command: MealPlanHelpRequest): HelpRequest {
    ensureQuestion(command.question);
    ensureMenuAddressees(command);
    return HelpRequest.requestHelp(
      {
        id: command.id,
        topic: "MENU",
        requester: command.requester,
        title: command.title,
        question: command.question,
        recipeId: null,
        howToStepId: null,
        addressees: command.addressees,
        chefId: command.chefId,
        plannedMealDate: command.plannedMealDate,
      },
      command.requestedAt,
    );
  }

  // Request help <topic> → Help requested: every new request starts REQUESTED (INV-05), version 0 (ADR-CA-005),
  // with its answer deadline counted from the request time (INV-07)
  private static requestHelp(asked: RequestedHelp, requestedAt: Date): HelpRequest {
    const helpRequest = new HelpRequest({
      ...asked,
      status: "REQUESTED",
      answerDeadline: answerDeadline(asked.topic, requestedAt),
      version: 0,
    });
    helpRequest.announceHelpRequested(requestedAt);
    return helpRequest;
  }

  // cmd: Request Chef review · slice B1-S02 · evt: Chef review requested (the request stays REQUESTED)
  requestChefReview(review: { readonly helpId: HelpId; readonly chefId: CookId; readonly requestedAt: Date }): void {
    this.ensureRequested();
    // INV-01 · a Chef reviews meal plans only
    if (this.topic !== "MENU") {
      throw new ChefReviewOnlyForMenu();
    }
    this.pending.push({ type: "ChefReviewRequested", helpRequestId: this.id, requester: this.requester, ...review });
  }

  // cmd: Cancel help request (cook) · slice B1-S06 · evt: Help request cancelled — the requester check is the
  // application's (INV-06, ADR-CA-009)
  cancel(cancelledAt: Date): void {
    this.ensureRequested();
    this.cancelBy("REQUESTER", cancelledAt);
  }

  // cmd: Close help request · slice B1-S08 · policy: whenever Help accepted (INV-14) · evt: Help request closed
  close(acceptedHelpId: HelpId, closedAt: Date): void {
    this.ensureRequested();
    this.state = { ...this.state, status: "CLOSED" };
    this.pending.push({
      type: "HelpRequestClosed",
      helpRequestId: this.id,
      requester: this.requester,
      acceptedHelpId,
      status: this.status,
      closedAt,
    });
  }

  // cmd: Restart answer deadline · slice B1-S10 · policy: whenever Help rejected · INV-08: the deadline counts from
  // the rejection · evt: Answer deadline restarted
  restartAnswerDeadline(rejectedAt: Date): void {
    this.ensureRequested();
    this.state = { ...this.state, answerDeadline: answerDeadline(this.topic, rejectedAt) };
    this.pending.push({
      type: "AnswerDeadlineRestarted",
      helpRequestId: this.id,
      answerDeadline: this.answerDeadline,
      restartedAt: rejectedAt,
    });
  }

  // cmd: Cancel help request (time) · slice B1-S07 · INV-07: the system's check of the answer deadline
  cancelIfDeadlinePassed(now: Date): void {
    // a closed or cancelled request, or one still within its deadline, is left as it is (no rejection: the system checks)
    if (this.status !== "REQUESTED" || !answerDeadlinePassed(this.answerDeadline, now)) {
      return;
    }
    this.cancelBy("SYSTEM", now);
  }

  // evt: Help request cancelled · AsyncAPI HelpRequestCancelled
  private cancelBy(cancelledBy: "REQUESTER" | "SYSTEM", cancelledAt: Date): void {
    this.state = { ...this.state, status: "CANCELLED" };
    this.pending.push({
      type: "HelpRequestCancelled",
      helpRequestId: this.id,
      requester: this.requester,
      cancelledBy,
      status: this.status,
      cancelledAt,
    });
  }

  // INV-05 · only a REQUESTED request takes further commands; CLOSED and CANCELLED are terminal
  private ensureRequested(): void {
    if (this.status !== "REQUESTED") {
      throw new HelpRequestNotRequested();
    }
  }

  /** Rebuilds a stored help request: no guards, no events (ADR-CA-003). */
  static reconstitute(stored: HelpRequestState): HelpRequest {
    return new HelpRequest(stored);
  }

  get id(): HelpRequestId {
    return this.state.id;
  }

  get topic(): HelpRequestTopic {
    return this.state.topic;
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

  get recipeId(): RecipeId | null {
    return this.state.recipeId;
  }

  get howToStepId(): HowToStepId | null {
    return this.state.howToStepId;
  }

  get addressees(): readonly HelpProviderType[] {
    return this.state.addressees;
  }

  // INV-03 · the one Chef the cook selected (Menu only)
  get chefId(): CookId | null {
    return this.state.chefId;
  }

  // INV-03 · the date of the planned meal, when a Chef is asked
  get plannedMealDate(): string | null {
    return this.state.plannedMealDate;
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
      chefId: this.chefId,
      plannedMealDate: this.plannedMealDate,
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
