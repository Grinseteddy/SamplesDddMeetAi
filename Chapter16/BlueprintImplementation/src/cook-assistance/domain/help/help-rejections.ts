import { DomainRejection } from "../shared/domain-rejection.js";

// INV-10 · a help is for the same step as its help request
export class StepNotAsInRequest extends DomainRejection {
  readonly rule = "INV-10";

  constructor() {
    super("This help is for a different step than the help request.");
  }
}

// INV-10 · a help is for the same recipe as its help request
export class RecipeNotAsInRequest extends DomainRejection {
  readonly rule = "INV-10";

  constructor() {
    super("This help is for a different recipe than the help request.");
  }
}
