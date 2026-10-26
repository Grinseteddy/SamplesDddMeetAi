import type { HowToStepId, RecipeId } from "../../domain/shared/ids.js";

/** A step of a recipe as Recipe Catalog knows it. */
export interface HowToStep {
  readonly recipeId: RecipeId;
  readonly howToStepId: HowToStepId;
}

// ADR-CA-015 · synchronous lookup in Recipe Catalog (acknowledged override of AP0002).
export interface RecipeCatalog {
  /** The step of the recipe, or undefined when the recipe has no such step. Throws RecipeCatalogUnavailable. */
  findHowToStep(recipeId: RecipeId, howToStepId: HowToStepId): Promise<HowToStep | undefined>;
}

/** Recipe Catalog did not answer in time or its circuit is open (ADR-CA-015); the HTTP adapter maps it to 503. */
export class RecipeCatalogUnavailable extends Error {
  constructor() {
    super("Recipe Catalog is unavailable; try again later.");
    this.name = "RecipeCatalogUnavailable";
  }
}
