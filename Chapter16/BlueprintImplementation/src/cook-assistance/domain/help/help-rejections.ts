import { DomainRejection } from "../shared/domain-rejection.js";

// INV-10 · a help is for the same step as its help request
export class StepNotAsInRequest extends DomainRejection {
  declare readonly name: "StepNotAsInRequest";
  readonly rule = "INV-10";

  constructor() {
    super("This help is for a different step than the help request.");
  }
}

// INV-10 · a help is for the same recipe as its help request
export class RecipeNotAsInRequest extends DomainRejection {
  declare readonly name: "RecipeNotAsInRequest";
  readonly rule = "INV-10";

  constructor() {
    super("This help is for a different recipe than the help request.");
  }
}

// INV-25 · a cook cannot answer their own help request (Q3-1)
export class OwnRequestNotAnswerable extends DomainRejection {
  declare readonly name: "OwnRequestNotAnswerable";
  readonly rule = "INV-25";

  constructor() {
    super("You cannot answer your own help request.");
  }
}

/** Every rejection Provide Help can raise. */
export type HelpRejection = StepNotAsInRequest | RecipeNotAsInRequest | OwnRequestNotAnswerable;

// INV-13, INV-15 · the requester decides once, while the help is PROVIDED; ACCEPTED and REJECTED are terminal
export class HelpNotProvided extends DomainRejection {
  declare readonly name: "HelpNotProvided";
  readonly rule = "INV-13";

  constructor() {
    super("This help has already been accepted or rejected.");
  }
}
