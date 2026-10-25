import { describe, expect, it } from "vitest";
import {
  AddresseeRequired,
  ChefOnlyForMenu,
  QuestionRequired,
  RecipeRequired,
  StepNotInRecipe,
  StepRequired,
} from "../domain/help-request/help-request-rejections.js";
import { HowToStepId } from "../domain/shared/ids.js";
import { FixedClock } from "../test-support/fakes/fixed-clock.js";
import { InMemoryRecipeCatalog } from "../test-support/fakes/in-memory-recipe-catalog.js";
import { InMemoryUnitOfWork } from "../test-support/fakes/in-memory-unit-of-work.js";
import { SequentialIdGenerator } from "../test-support/fakes/sequential-id-generator.js";
import { sundaySconesStep3 } from "../test-support/fixtures/sunday-scones.js";
import { RecipeCatalogUnavailable } from "./ports/recipe-catalog.js";
import { RequestPreparationStepHelp, type RequestPreparationStepHelpCommand } from "./request-preparation-step-help.js";

// Application — increment 0 (walking skeleton), step 8, unit 2: use case RequestPreparationStepHelp.
// cmd: Request help preparation step · agg: Help request · slice B2-S01. In-memory fakes proven by the
// shared contracts, a fixed clock (10:00), a sequential id generator and an in-memory Recipe Catalog.

/** The Cook asks what "warm" means in Sunday scones, step 3 (Example Mapping Preparation step). */
const askWhatWarmMeans: RequestPreparationStepHelpCommand = {
  requester: sundaySconesStep3.requester,
  title: sundaySconesStep3.title,
  question: sundaySconesStep3.question,
  recipeId: sundaySconesStep3.recipeId,
  howToStepId: sundaySconesStep3.howToStepId,
  addressees: sundaySconesStep3.addressees,
};

function setUp() {
  const unitOfWork = new InMemoryUnitOfWork();
  const recipeCatalog = new InMemoryRecipeCatalog();
  recipeCatalog.addHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);
  const useCase = new RequestPreparationStepHelp({
    unitOfWork,
    recipeCatalog,
    idGenerator: new SequentialIdGenerator([sundaySconesStep3.id]),
    clock: new FixedClock(sundaySconesStep3.requestedAt),
  });
  return { useCase, unitOfWork, recipeCatalog };
}

/** A refused request leaves no help request and no HelpRequested behind. */
async function expectNothingSaved(unitOfWork: InMemoryUnitOfWork): Promise<void> {
  expect(await unitOfWork.helpRequests.findById(sundaySconesStep3.id)).toBeUndefined();
  expect(unitOfWork.helpRequests.savedEvents).toEqual([]);
}

describe("RequestPreparationStepHelp", () => {
  it("Sunday scones step 3 returns the id the id generator handed out -- B2-S01 ADR-CA-007", async () => {
    const { useCase } = setUp();

    expect(await useCase.execute(askWhatWarmMeans)).toBe("0192f1a0-0000-7000-8000-000000000001");
  });
  it("the saved help request is REQUESTED with requester, title, question, recipe, step, addressees COMMUNITY and GRANDMA_AVATAR, requested at the clock's 10:00 so its answer deadline is 15:00 -- B2-S01 INV-07 ADR-CA-007", async () => {
    const { useCase, unitOfWork } = setUp();

    const id = await useCase.execute(askWhatWarmMeans);

    const saved = await unitOfWork.helpRequests.findById(id);
    expect(saved?.status).toBe("REQUESTED");
    expect(saved?.requester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
    expect(saved?.title).toBe("Sunday scones, step 3");
    expect(saved?.question).toBe("What means 'warm'?");
    expect(saved?.recipeId).toBe("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f");
    expect(saved?.howToStepId).toBe("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d");
    expect(saved?.addressees).toEqual(["COMMUNITY", "GRANDMA_AVATAR"]);
    expect(saved?.answerDeadline).toEqual(new Date("2026-10-24T15:00:00Z"));
  });
  it("HelpRequested is saved with the help request -- B2-S01 ADR-CA-004", async () => {
    const { useCase, unitOfWork } = setUp();

    await useCase.execute(askWhatWarmMeans);

    expect(unitOfWork.helpRequests.savedEvents.map((event) => event.type)).toEqual(["HelpRequested"]);
  });
  it("step 9 of Sunday scones (8 steps) is rejected with StepNotInRecipe (INV-18), nothing saved -- B2-S01 INV-18 EM-P R2", async () => {
    const { useCase, unitOfWork } = setUp();
    const sundaySconesStep9 = HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c09");

    const outcome = useCase.execute({ ...askWhatWarmMeans, howToStepId: sundaySconesStep9 });

    await expect(outcome).rejects.toBeInstanceOf(StepNotInRecipe);
    await expect(outcome).rejects.toMatchObject({ rule: "INV-18" });
    await expectNothingSaved(unitOfWork);
  });
  it("Recipe Catalog unavailable: RecipeCatalogUnavailable propagates, nothing saved -- B2-S01 ADR-CA-015", async () => {
    const { useCase, unitOfWork, recipeCatalog } = setUp();
    recipeCatalog.becomeUnavailable();

    await expect(useCase.execute(askWhatWarmMeans)).rejects.toBeInstanceOf(RecipeCatalogUnavailable);
    await expectNothingSaved(unitOfWork);
  });
  it("no recipe: RecipeRequired (INV-17) passes through unchanged and Recipe Catalog is not asked -- B2-S01 INV-17 ADR-CA-015", async () => {
    const { useCase, unitOfWork, recipeCatalog } = setUp();

    const outcome = useCase.execute({ ...askWhatWarmMeans, recipeId: null });

    await expect(outcome).rejects.toBeInstanceOf(RecipeRequired);
    await expect(outcome).rejects.toMatchObject({ rule: "INV-17" });
    expect(recipeCatalog.lookups).toBe(0);
    await expectNothingSaved(unitOfWork);
  });
  it("a blank question: QuestionRequired (INV-19) passes through unchanged, nothing saved -- B2-S01 INV-19", async () => {
    const { useCase, unitOfWork } = setUp();

    const outcome = useCase.execute({ ...askWhatWarmMeans, question: "   " });

    await expect(outcome).rejects.toBeInstanceOf(QuestionRequired);
    await expect(outcome).rejects.toMatchObject({ rule: "INV-19" });
    await expectNothingSaved(unitOfWork);
  });
  it("no addressee: AddresseeRequired (INV-20) passes through unchanged, nothing saved -- B2-S01 INV-20", async () => {
    const { useCase, unitOfWork } = setUp();

    const outcome = useCase.execute({ ...askWhatWarmMeans, addressees: [] });

    await expect(outcome).rejects.toBeInstanceOf(AddresseeRequired);
    await expect(outcome).rejects.toMatchObject({ rule: "INV-20" });
    await expectNothingSaved(unitOfWork);
  });
  it("a Chef addressed: ChefOnlyForMenu (INV-20) passes through unchanged, nothing saved -- B2-S01 INV-20 EM-P R3", async () => {
    const { useCase, unitOfWork } = setUp();

    const outcome = useCase.execute({ ...askWhatWarmMeans, addressees: ["CHEF"] });

    await expect(outcome).rejects.toBeInstanceOf(ChefOnlyForMenu);
    await expect(outcome).rejects.toMatchObject({ rule: "INV-20" });
    await expectNothingSaved(unitOfWork);
  });
  it("no step: StepRequired (INV-17) passes through unchanged, nothing saved -- B2-S01 INV-17", async () => {
    const { useCase, unitOfWork } = setUp();

    const outcome = useCase.execute({ ...askWhatWarmMeans, howToStepId: null });

    await expect(outcome).rejects.toBeInstanceOf(StepRequired);
    await expect(outcome).rejects.toMatchObject({ rule: "INV-17" });
    await expectNothingSaved(unitOfWork);
  });
});
