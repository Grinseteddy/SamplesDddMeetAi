/** Which page a list operation asks for (ADR-CA-012: page/size, as the spec declares). */
export interface PageRequest {
  readonly number: number;
  readonly size: number;
}

/** One page of a read model and where it sits in the whole list. */
export interface Page<T> {
  readonly items: readonly T[];
  readonly number: number;
  readonly size: number;
  readonly totalElements: number;
  readonly totalPages: number;
}
