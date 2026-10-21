import { rejected, type HelpRequest, type Rejection } from "./help-request.js";

export interface Cook {
  name: string;
  city: string;
  premium: boolean;
}

export interface Chef {
  name: string;
  city: string;
  freeOn: string[];
}

export interface Meal {
  kind: string;
  date: string;
}

export interface HelpRequestInput {
  cook: Cook;
  chef: Chef;
  topic: string;
  meal: Meal;
  requestedAt: string;
}

export interface ChefSelectionInput {
  cook: Cook;
  mealDate: string;
  chefs: Chef[];
}

export interface GrandmaAvatarAnswer {
  answeredFor: string;
  mealPlan: string;
}

export interface ReviewRequestInput {
  cook: Cook;
  chef: Chef;
  grandmaAnswer: GrandmaAvatarAnswer;
  requestedAt: string;
}

export type HelpRequestResult =
  | { outcome: "created"; request: HelpRequest }
  | Rejection;

const MEAL_PLANNING = "meal planning";

function chefCanBeAskedAbout(topic: string): boolean {
  return topic === MEAL_PLANNING;
}

function cookMayAskAChef(cook: Cook): boolean {
  return cook.premium;
}

function chefIsLocalTo(cook: Cook, chef: Chef): boolean {
  return chef.city === cook.city;
}

function chefIsFreeBefore(chef: Chef, mealDate: string): boolean {
  return chef.freeOn.some((day) => day < mealDate);
}

function chefIsAvailableFor(cook: Cook, chef: Chef, mealDate: string): boolean {
  return chefIsLocalTo(cook, chef) && chefIsFreeBefore(chef, mealDate);
}

function grandmaAnswerBelongsTo(cook: Cook, answer: GrandmaAvatarAnswer): boolean {
  return answer.answeredFor === cook.name;
}

function openRequestTo(chef: Chef, requestedAt: string): HelpRequestResult {
  return { outcome: "created", request: { chef: chef.name, status: "open", requestedAt } };
}

interface AdmissionRule<Input> {
  holds: (input: Input) => boolean;
  reason: string;
}

const PREMIUM_COOK: AdmissionRule<{ cook: Cook }> = {
  holds: (input) => cookMayAskAChef(input.cook),
  reason: "upgrade required",
};

const MEAL_PLAN_HELP_RULES: AdmissionRule<HelpRequestInput>[] = [
  { holds: (input) => chefCanBeAskedAbout(input.topic), reason: "not meal planning" },
  PREMIUM_COOK,
  { holds: (input) => chefIsLocalTo(input.cook, input.chef), reason: "Chef not local" },
  { holds: (input) => chefIsFreeBefore(input.chef, input.meal.date), reason: "Chef not timely available" },
];

const REVIEW_RULES: AdmissionRule<ReviewRequestInput>[] = [
  { holds: (input) => grandmaAnswerBelongsTo(input.cook, input.grandmaAnswer), reason: "not own Grandma answer" },
  PREMIUM_COOK,
];

function admit<Input extends { chef: Chef; requestedAt: string }>(
  input: Input,
  rules: AdmissionRule<Input>[],
): HelpRequestResult {
  const broken = rules.find((rule) => !rule.holds(input));
  return broken ? rejected(broken.reason) : openRequestTo(input.chef, input.requestedAt);
}

export function requestHelp(input: HelpRequestInput): HelpRequestResult {
  return admit(input, MEAL_PLAN_HELP_RULES);
}

export function selectableChefs(input: ChefSelectionInput): Chef[] {
  return input.chefs.filter((chef) => chefIsAvailableFor(input.cook, chef, input.mealDate));
}

export function requestReview(input: ReviewRequestInput): HelpRequestResult {
  return admit(input, REVIEW_RULES);
}
