import { DomainRejection } from "../shared/domain-rejection.js";

// INV-20 · a help request names at least one addressee
export class AddresseeRequired extends DomainRejection {
  readonly rule = "INV-20";

  constructor() {
    super("A help request needs at least one addressee: the community and/or Grandma Avatar.");
  }
}

// INV-20 (with INV-01) · a Chef can only be asked for meal planning
export class ChefOnlyForMenu extends DomainRejection {
  readonly rule = "INV-20";

  constructor() {
    super("Chefs are only allowed for Meal plan.");
  }
}

// INV-19 · a help request carries the cook's question
export class QuestionRequired extends DomainRejection {
  readonly rule = "INV-19";

  constructor() {
    super("A help request needs the cook's question.");
  }
}

// INV-17 · a preparation-step request names exactly one step
export class StepRequired extends DomainRejection {
  readonly rule = "INV-17";

  constructor() {
    super("Name the step you need help with.");
  }
}

// INV-18 · the step exists in the recipe (checked against Recipe Catalog, ADR-CA-015)
export class StepNotInRecipe extends DomainRejection {
  readonly rule = "INV-18";

  constructor() {
    super("The recipe has no such step.");
  }
}

// INV-17 · a preparation-step request names its recipe
export class RecipeRequired extends DomainRejection {
  readonly rule = "INV-17";

  constructor() {
    super("Name the recipe you need help with.");
  }
}

// INV-03 · a cook asks exactly one Chef, by id, and the Chef is then the only addressee
export class ExactlyOneChef extends DomainRejection {
  readonly rule = "INV-03";

  constructor() {
    super("Ask exactly one Chef, by name, and no one else.");
  }
}

// INV-03 · a Chef is asked for a planned meal: its date is needed (the Chef must be free before it)
export class MealDateRequired extends DomainRejection {
  readonly rule = "INV-03";

  constructor() {
    super("Name the date of the planned meal when you ask a Chef.");
  }
}

// INV-01 · a Chef can only be asked for meal planning — a Chef review too
export class ChefReviewOnlyForMenu extends DomainRejection {
  readonly rule = "INV-01";

  constructor() {
    super("A Chef can review meal plans only.");
  }
}

// INV-05 · cancel, close, restart and Chef review only while REQUESTED; CLOSED and CANCELLED are terminal
export class HelpRequestNotRequested extends DomainRejection {
  readonly rule = "INV-05";

  constructor() {
    super("The help request is already closed or cancelled.");
  }
}
