/**
 * The aggregate was changed by someone else since it was loaded (ADR-CA-005). Part of every repository
 * port's contract; the HTTP adapter maps it to 409, policy handlers reload and retry.
 */
export class ConcurrencyConflict extends Error {
  constructor(aggregate: string, id: string) {
    super(`${aggregate} ${id} was changed by someone else; load it again and retry.`);
    this.name = "ConcurrencyConflict";
  }
}
