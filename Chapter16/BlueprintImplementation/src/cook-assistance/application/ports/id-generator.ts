// ADR-CA-007 · new identities are UUIDv7, generated here and wrapped in the domain's typed ids.
export interface IdGenerator {
  /** A new, unused UUIDv7. */
  next(): string;
}
