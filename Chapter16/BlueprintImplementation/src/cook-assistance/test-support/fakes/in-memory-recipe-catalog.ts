import { type HowToStep, type RecipeCatalog, RecipeCatalogUnavailable } from "../../application/ports/recipe-catalog.js";
import type { HowToStepId, RecipeId } from "../../domain/shared/ids.js";

// In-memory stand-in for Recipe Catalog (ADR-CA-015): knows the steps it was given, can be made unavailable.
export class InMemoryRecipeCatalog implements RecipeCatalog {
  private readonly steps: HowToStep[] = [];
  private available = true;
  /** How often findHowToStep was asked, to show when the lookup is skipped. */
  lookups = 0;

  addHowToStep(recipeId: RecipeId, howToStepId: HowToStepId): void {
    this.steps.push({ recipeId, howToStepId });
  }

  becomeUnavailable(): void {
    this.available = false;
  }

  async findHowToStep(recipeId: RecipeId, howToStepId: HowToStepId): Promise<HowToStep | undefined> {
    this.lookups += 1;
    if (!this.available) {
      throw new RecipeCatalogUnavailable();
    }
    return this.steps.find((step) => step.recipeId === recipeId && step.howToStepId === howToStepId);
  }
}
