import type { operations } from "../../../../generated/openapi/cook-assistance.js";
import type { Actor } from "../../../application/actor.js";
import type { HelpRequestView } from "../../../application/help-request-view.js";
import type { HelpRequestReadModel } from "../../../application/queries/help-request-queries.js";
import type { Page } from "../../../application/queries/page.js";
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

/** The created help request as the spec's HelpRequest (201): as the read model shows it — requested, created and updated at once. */
export function toHelpRequestResponse(view: HelpRequestView, publicBaseUrl: string): HelpRequestResponse {
  return toHelpRequestResource(
    {
      id: view.id,
      requester: view.requester,
      title: view.title,
      topic: view.topic,
      description: view.question,
      recipeId: view.recipeId,
      howToStepId: view.howToStepId,
      addressees: view.addressees,
      status: view.status,
      answerDeadline: view.answerDeadline,
      createdAt: view.requestedAt,
      updatedAt: view.requestedAt,
    },
    publicBaseUrl,
  );
}

export type HelpRequestResource = operations["getHelpRequestById"]["responses"]["200"]["content"]["application/json"];

/** RM Help request → the spec's HelpRequest; fields of other topics are null or empty for a Preparation step request. */
export function toHelpRequestResource(readModel: HelpRequestReadModel, publicBaseUrl: string): HelpRequestResource {
  const selfLink = `${publicBaseUrl}/help-requests/${readModel.id}`;
  return {
    helpRequestId: readModel.id,
    requester: readModel.requester,
    title: readModel.title,
    topic: readModel.topic,
    description: readModel.description,
    recipeId: readModel.recipeId,
    howToStepId: readModel.howToStepId,
    ingredientId: null,
    addressees: [...readModel.addressees],
    chefId: null,
    plannedMealDate: null,
    pictures: [],
    status: readModel.status,
    answerDeadline: readModel.answerDeadline.toISOString(),
    createdAt: readModel.createdAt.toISOString(),
    updatedAt: readModel.updatedAt.toISOString(),
    selfLink,
    helpsLink: `${selfLink}/helps`,
  };
}

type ListHelpRequestsParameters = NonNullable<operations["listHelpRequests"]["parameters"]["query"]>;
/** As the validator hands it over: page and size defaulted by the spec, the filters optional. */
export type ListHelpRequestsQuery = Omit<ListHelpRequestsParameters, "page" | "size"> & Required<Pick<ListHelpRequestsParameters, "page" | "size">>;
export type HelpRequestPageResource = operations["listHelpRequests"]["responses"]["200"]["content"]["application/json"];

/** A page of RM Help request → the spec's HelpRequestPage. */
export function toHelpRequestPage(page: Page<HelpRequestReadModel>, publicBaseUrl: string): HelpRequestPageResource {
  return {
    items: page.items.map((readModel) => toHelpRequestResource(readModel, publicBaseUrl)),
    page: { number: page.number, size: page.size, totalElements: page.totalElements, totalPages: page.totalPages },
  };
}
