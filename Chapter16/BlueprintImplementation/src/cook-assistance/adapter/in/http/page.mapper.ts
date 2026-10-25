import type { components } from "../../../../generated/openapi/cook-assistance.js";
import type { Page } from "../../../application/queries/page.js";

/** A page of read models → the spec's page: its items mapped one by one, and the PageInfo. */
export function toPageResource<T, R>(page: Page<T>, toItem: (item: T) => R): { items: R[]; page: components["schemas"]["PageInfo"] } {
  return {
    items: page.items.map(toItem),
    page: { number: page.number, size: page.size, totalElements: page.totalElements, totalPages: page.totalPages },
  };
}
