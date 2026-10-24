/**
 * A command refused because it would break a business rule. Carries the rule's ID (INV-nn) so the
 * application and the error map (ADR-CA-006) can name it; knows nothing about HTTP.
 */
export abstract class DomainRejection extends Error {
  abstract readonly rule: string;

  constructor(message: string) {
    super(message);
    this.name = new.target.name;
  }
}
