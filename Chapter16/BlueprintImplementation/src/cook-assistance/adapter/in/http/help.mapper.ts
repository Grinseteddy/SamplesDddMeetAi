import type { operations } from "../../../../generated/openapi/cook-assistance.js";
import type { HelpReadModel } from "../../../application/queries/help-queries.js";
import type { Page } from "../../../application/queries/page.js";
import { toPageResource } from "./page.mapper.js";

export type HelpResource = operations["getHelpById"]["responses"]["200"]["content"]["application/json"];

/** RM Help → the spec's Help, with links to itself and to the help request it answers. */
export function toHelpResource(readModel: HelpReadModel, publicBaseUrl: string): HelpResource {
  return {
    helpId: readModel.id,
    helpRequestId: readModel.helpRequestId,
    helpRequester: readModel.helpRequester,
    answerTitle: readModel.answerTitle,
    helpProviderType: readModel.helpProviderType,
    helpProvider: readModel.helpProvider,
    answer: { ...readModel.answer, images: [...readModel.answer.images] },
    status: readModel.status,
    createdAt: readModel.createdAt.toISOString(),
    updatedAt: readModel.updatedAt.toISOString(),
    selfLink: `${publicBaseUrl}/helps/${readModel.id}`,
    helpRequestLink: `${publicBaseUrl}/help-requests/${readModel.helpRequestId}`,
  };
}

type ListHelpsParameters = NonNullable<operations["listHelpsForHelpRequest"]["parameters"]["query"]>;
/** As the validator hands it over: page and size defaulted by the spec, the filter optional. */
export type ListHelpsQuery = Omit<ListHelpsParameters, "page" | "size"> & Required<Pick<ListHelpsParameters, "page" | "size">>;
export type HelpPageResource = operations["listHelpsForHelpRequest"]["responses"]["200"]["content"]["application/json"];

/** A page of RM Help → the spec's HelpPage. */
export function toHelpPage(page: Page<HelpReadModel>, publicBaseUrl: string): HelpPageResource {
  return toPageResource(page, (readModel) => toHelpResource(readModel, publicBaseUrl));
}
