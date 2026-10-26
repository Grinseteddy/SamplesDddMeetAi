export interface Cook {
  name: string;
  premiumUntil?: string;
}

export interface WorkingTime {
  from: string;
  until: string;
}

export interface Chef {
  name: string;
  workingTimes: WorkingTime[];
}

export interface Occasion {
  title: string;
  at: string;
  guests: number;
  dietaryNeeds: string[];
  budget: number;
}

export interface Course {
  name: string;
  recipe?: string;
  suitableFor?: string[];
  cost?: number;
}

export interface Menu {
  courses: Course[];
}

export interface MenuRequest {
  chef: string;
  occasion: Occasion;
  status: "open" | "answered" | "timed out" | "closed" | "withdrawn";
  deadline: Date;
  menu?: Menu;
  note?: string;
  grandmaMenu?: Menu;
}

export interface AskChefInput {
  cook: Cook;
  chef: Chef;
  occasion: Occasion;
  askedAt: string;
  asksFor?: "Menu" | "substitution";
  grandmaMenu?: Menu;
}

export interface Rejection {
  outcome: "rejected";
  reason: string;
  suggested?: string[];
}

export interface Notice {
  to: string;
}

export type AskChefResult = { outcome: "sent"; request: MenuRequest; notices: Notice[] } | Rejection;

function tellChef(request: MenuRequest): Notice {
  return { to: request.chef };
}

const HOUR_MS = 60 * 60 * 1000;
const ANSWER_TIME_MS = 48 * HOUR_MS;
const LAST_ANSWER_BEFORE_OCCASION_MS = 24 * HOUR_MS;

function endOfAnswerTime(from: string): number {
  return Date.parse(from) + ANSWER_TIME_MS;
}

function latestAnswerBefore(occasion: Occasion): number {
  return Date.parse(occasion.at) - LAST_ANSWER_BEFORE_OCCASION_MS;
}

function firstDeadline(askedAt: string, occasion: Occasion): Date {
  return new Date(Math.min(endOfAnswerTime(askedAt), latestAnswerBefore(occasion)));
}

const LEAD_TIME_BEFORE_OCCASION_MS = 48 * HOUR_MS;

function leavesLeadTimeBefore(occasion: Occasion, askedAt: string): boolean {
  return Date.parse(occasion.at) - Date.parse(askedAt) >= LEAD_TIME_BEFORE_OCCASION_MS;
}

function isPremium(cook: Cook, at: string): boolean {
  return cook.premiumUntil !== undefined && Date.parse(at) < Date.parse(cook.premiumUntil);
}

const SUBSTITUTION_HELPERS = ["Community", "Grandma Avatar"];
const CHEF_CANNOT_ANSWER_IN_TIME = "Chef cannot answer in time";

function rejected(reason: string, suggested?: string[]): Rejection {
  return suggested ? { outcome: "rejected", reason, suggested } : { outcome: "rejected", reason };
}

function refusalOf(input: AskChefInput, deadline: Date): Rejection | undefined {
  if (input.asksFor === "substitution") {
    return rejected("Chefs never handle substitutions", SUBSTITUTION_HELPERS);
  }
  if (!isPremium(input.cook, input.askedAt)) {
    return rejected("upgrade required");
  }
  if (!canAnswerBy(input.chef, deadline)) {
    return rejected(CHEF_CANNOT_ANSWER_IN_TIME);
  }
  if (!leavesLeadTimeBefore(input.occasion, input.askedAt)) {
    return rejected("48 h needed");
  }
  return undefined;
}

function sendRequest(input: AskChefInput, deadline: Date): AskChefResult {
  const request: MenuRequest = {
    chef: input.chef.name,
    occasion: input.occasion,
    status: "open",
    deadline,
    grandmaMenu: input.grandmaMenu,
  };
  return { outcome: "sent", request, notices: [tellChef(request)] };
}

export function askChef(input: AskChefInput): AskChefResult {
  const deadline = firstDeadline(input.askedAt, input.occasion);
  return refusalOf(input, deadline) ?? sendRequest(input, deadline);
}

function canAnswerBy(chef: Chef, deadline: Date): boolean {
  return chef.workingTimes.some((time) => Date.parse(time.from) < deadline.getTime());
}

export function selectableChefs(chefs: Chef[], deadline: Date): string[] {
  return chefs.filter((chef) => canAnswerBy(chef, deadline)).map((chef) => chef.name);
}

export type AnswerResult = { outcome: "answered"; request: MenuRequest } | Rejection;

function everyCourseHasARecipe(menu: Menu): boolean {
  return menu.courses.every((course) => course.recipe !== undefined);
}

function respectsDietaryNeeds(menu: Menu, occasion: Occasion): boolean {
  return menu.courses.every((course) => occasion.dietaryNeeds.every((need) => course.suitableFor?.includes(need)));
}

function costOf(menu: Menu): number {
  return menu.courses.reduce((total, course) => total + (course.cost ?? 0), 0);
}

function staysWithinBudget(menu: Menu, occasion: Occasion): boolean {
  return costOf(menu) <= occasion.budget;
}

function refusalOfMenu(menu: Menu, occasion: Occasion): Rejection | undefined {
  if (!everyCourseHasARecipe(menu)) {
    return rejected("every course needs a recipe");
  }
  if (!respectsDietaryNeeds(menu, occasion)) {
    return rejected("Menu breaks dietary needs");
  }
  if (!staysWithinBudget(menu, occasion)) {
    return rejected("Menu over budget");
  }
  return undefined;
}

function answered(request: MenuRequest, menu: Menu): AnswerResult {
  return { outcome: "answered", request: { ...request, status: "answered", menu } };
}

export function answerRequest(request: MenuRequest, menu: Menu, answeredAt: string): AnswerResult {
  return refusalOfMenu(menu, request.occasion) ?? answered(request, menu);
}

export interface TimeoutResult {
  request: MenuRequest;
  suggested: string[];
}

function hasTimedOut(request: MenuRequest, now: string): boolean {
  return request.status === "open" && Date.parse(now) >= request.deadline.getTime();
}

function resetDeadline(at: string): Date {
  return new Date(endOfAnswerTime(at));
}

function otherChefsWhoCanAnswer(request: MenuRequest, chefs: Chef[], deadline: Date): string[] {
  return selectableChefs(
    chefs.filter((chef) => chef.name !== request.chef),
    deadline,
  );
}

export function applyTimeout(request: MenuRequest, now: string, chefs: Chef[]): TimeoutResult {
  if (hasTimedOut(request, now)) {
    return {
      request: { ...request, status: "timed out" },
      suggested: otherChefsWhoCanAnswer(request, chefs, resetDeadline(now)),
    };
  }
  return { request, suggested: [] };
}

export type PassResult = { outcome: "passed"; request: MenuRequest; notices: Notice[] } | Rejection;

function occasionNote(deadline: Date, occasion: Occasion, at: string): string | undefined {
  if (deadline.getTime() <= Date.parse(occasion.at)) {
    return undefined;
  }
  const hoursToOccasion = (Date.parse(occasion.at) - Date.parse(at)) / HOUR_MS;
  return `${hoursToOccasion} h to the Occasion`;
}

function resetAnswerTime(request: MenuRequest, at: string): Pick<MenuRequest, "deadline" | "note"> {
  const deadline = resetDeadline(at);
  return { deadline, note: occasionNote(deadline, request.occasion, at) };
}

export function passToChef(request: MenuRequest, chef: Chef, at: string): PassResult {
  const reset = resetAnswerTime(request, at);
  if (!canAnswerBy(chef, reset.deadline)) {
    return rejected(CHEF_CANNOT_ANSWER_IN_TIME);
  }
  const passed: MenuRequest = {
    ...request,
    chef: chef.name,
    status: "open",
    menu: undefined,
    ...reset,
  };
  return { outcome: "passed", request: passed, notices: [tellChef(passed)] };
}

export function acceptMenu(request: MenuRequest): MenuRequest {
  return { ...request, status: "closed" };
}

export function rejectMenu(request: MenuRequest, at: string): MenuRequest {
  return { ...request, status: "open", ...resetAnswerTime(request, at) };
}

export interface EndResult {
  request: MenuRequest;
  notices: Notice[];
}

function endRequest(request: MenuRequest, status: "closed" | "withdrawn"): EndResult {
  return { request: { ...request, status }, notices: [tellChef(request)] };
}

export function withdrawRequest(request: MenuRequest): EndResult {
  return endRequest(request, "withdrawn");
}

export function closeRequest(request: MenuRequest): EndResult {
  return endRequest(request, "closed");
}

export interface PremiumEndResult {
  requests: MenuRequest[];
  notices: Notice[];
}

export function premiumEnded(requests: MenuRequest[]): PremiumEndResult {
  const ended = requests.map((request) => endRequest(request, "closed"));
  return { requests: ended.map((end) => end.request), notices: ended.flatMap((end) => end.notices) };
}

export function reviewOptionOffered(grandmaMenus: Menu[]): boolean {
  return grandmaMenus.length > 0;
}
