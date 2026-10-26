import { HelpRequest, type HelpProviderType, HelpRequestId } from "../domain/help-request/help-request.js";
import { StepNotInRecipe } from "../domain/help-request/help-request-rejections.js";
import type { CookId, HowToStepId, RecipeId } from "../domain/shared/ids.js";
import type { HelpRequestView } from "./help-request-view.js";
import type { Clock } from "./ports/clock.js";
import type { IdGenerator } from "./ports/id-generator.js";
import type { RecipeCatalog } from "./ports/recipe-catalog.js";
import type { UnitOfWork } from "./ports/unit-of-work.js";

/** What the Cook asks for; the requester comes from the token (ADR-CA-009), id and time from the ports. */
export interface RequestPreparationStepHelpCommand {
  readonly requester: CookId;
  readonly title: string;
  readonly question: string;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
  readonly addressees: readonly HelpProviderType[];
}

export interface RequestPreparationStepHelpPorts {
  readonly unitOfWork: UnitOfWork;
  readonly recipeCatalog: RecipeCatalog;
  readonly idGenerator: IdGenerator;
  readonly clock: Clock;
}

/** The created request as the inbound adapter answers it (op requestHelp, 201). */
function viewOf(helpRequest: HelpRequest, requestedAt: Date): HelpRequestView {
  return {
    id: helpRequest.id,
    requester: helpRequest.requester,
    title: helpRequest.title,
    topic: helpRequest.topic,
    question: helpRequest.question,
    recipeId: helpRequest.recipeId,
    howToStepId: helpRequest.howToStepId,
    addressees: helpRequest.addressees,
    status: helpRequest.status,
    requestedAt,
    answerDeadline: helpRequest.answerDeadline,
  };
}

// cmd: Request help preparation step · agg: Help request · slice B2-S01
export class RequestPreparationStepHelp {
  constructor(private readonly ports: RequestPreparationStepHelpPorts) {}

  async execute(command: RequestPreparationStepHelpCommand): Promise<HelpRequestView> {
    const requestedAt = this.ports.clock.now();
    const helpRequest = HelpRequest.requestPreparationStepHelp({
      ...command,
      id: HelpRequestId.of(this.ports.idGenerator.next()),
      requestedAt,
    });
    await this.ensureStepInRecipe(helpRequest);
    await this.ports.unitOfWork.transactionally((scope) => scope.helpRequests.save(helpRequest));
    return viewOf(helpRequest, requestedAt);
  }

  // INV-18 · the step exists in the recipe — Recipe Catalog owns recipes (ADR-CA-015), so the lookup
  // runs here, after the domain's own guards and outside the transaction
  private async ensureStepInRecipe(helpRequest: HelpRequest): Promise<void> {
    const step = await this.ports.recipeCatalog.findHowToStep(helpRequest.recipeId, helpRequest.howToStepId);
    if (step === undefined) {
      throw new StepNotInRecipe();
    }
  }
}
