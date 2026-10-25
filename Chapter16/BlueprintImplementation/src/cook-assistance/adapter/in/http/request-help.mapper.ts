import type { operations } from "../../../../generated/openapi/cook-assistance.js";
import type { Actor } from "../../../application/actor.js";
import type { HelpRequestView } from "../../../application/help-request-view.js";
import type { RequestPreparationStepHelpCommand } from "../../../application/request-preparation-step-help.js";
import { HowToStepId, RecipeId } from "../../../domain/shared/ids.js";

export type RequestHelpBody = operations["requestHelp"]["requestBody"]["content"]["application/json"];
export type HelpRequestResponse = operations["requestHelp"]["responses"]["201"]["content"]["application/json"];

/** An optional id of the request body as the domain's typed id; absent and null stay null. */
function typedOrNull<T>(value: string | null | undefined, of: (value: string) => T): T | null {
  return value === null || value === undefined ? null : of(value);
}

/** op requestHelp → cmd Request help preparation step: the requester is the caller, the question is the description. */
export function toCommand(body: RequestHelpBody, actor: Actor): RequestPreparationStepHelpCommand {
  return {
    requester: actor.cookId,
    title: body.title,
    question: body.description,
    recipeId: typedOrNull(body.recipeId, RecipeId.of),
    howToStepId: typedOrNull(body.howToStepId, HowToStepId.of),
    addressees: body.addressees,
  };
}

/** The created help request as the spec's HelpRequest (201). */
export function toHelpRequestResponse(view: HelpRequestView, publicBaseUrl: string): HelpRequestResponse {
  const selfLink = `${publicBaseUrl}/help-requests/${view.id}`;
  return {
    helpRequestId: view.id,
    requester: view.requester,
    title: view.title,
    topic: view.topic,
    description: view.question,
    recipeId: view.recipeId,
    howToStepId: view.howToStepId,
    ingredientId: null,
    addressees: [...view.addressees],
    chefId: null,
    plannedMealDate: null,
    pictures: [],
    status: view.status,
    answerDeadline: view.answerDeadline.toISOString(),
    createdAt: view.requestedAt.toISOString(),
    updatedAt: view.requestedAt.toISOString(),
    selfLink,
    helpsLink: `${selfLink}/helps`,
  };
}
