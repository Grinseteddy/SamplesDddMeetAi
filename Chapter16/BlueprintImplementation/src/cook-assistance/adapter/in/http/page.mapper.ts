import type { components } from "../../../../generated/openapi/cook-assistance.js";
import type { Page, PageRequest } from "../../../application/queries/page.js";

interface PageParameters {
  page: components["parameters"]["PageParameter"];
  size: components["parameters"]["SizeParameter"];
}

/** A list operation's query as the validator hands it over: page and size defaulted by the spec, the filters optional. */
export type PagedQuery<Q extends Partial<PageParameters>> = Omit<Q, keyof PageParameters> & PageParameters;

/** The spec's page/size → the query port's PageRequest (ADR-CA-012). */
export function toPageRequest({ page, size }: PageParameters): PageRequest {
  return { number: page, size };
}

/** A page of read models → the spec's page: its items mapped one by one, and the PageInfo. */
export function toPageResource<T, R>(page: Page<T>, toItem: (item: T) => R): { items: R[]; page: components["schemas"]["PageInfo"] } {
  return {
    items: page.items.map(toItem),
    page: { number: page.number, size: page.size, totalElements: page.totalElements, totalPages: page.totalPages },
  };
}
