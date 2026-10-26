import { sql } from "kysely";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { migrateToLatest } from "./migrations.js";
import { HelpRequest, HelpRequestId } from "../../../domain/help-request/help-request.js";
import { HelpId } from "../../../domain/help/help.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { CookId, HowToStepId, RecipeId } from "../../../domain/shared/ids.js";
import { EventNotStoredYet, KyselyHelpRequestRepository } from "./help-request-repository.js";
import { KyselyUnitOfWork } from "./unit-of-work.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";
import { resetSchema } from "./test-support/reset-schema.js";

// Persistence — increment 0 (walking skeleton), step 7, unit 2: HelpRequestRepository (Kysely adapter).
// Real PostgreSQL 16 through Testcontainers (ADR-CA-016). Slice B2-S01.

// Sunday scones, step 3 "Let dough rest in a warm place" (Example Mapping Preparation step).
const sundaySconesStep3 = {
  id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000001"),
  requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
  title: "Sunday scones, step 3",
  question: "What means 'warm'?",
  recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
  howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
  addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
  requestedAt: new Date("2026-10-24T10:00:00Z"),
} as const;

describe("KyselyHelpRequestRepository — PostgreSQL 16", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
  });

  beforeEach(async () => {
    await resetSchema(database.db);
  });

  afterAll(async () => {
    await database.stop();
  });

  describe("schema", () => {
    it("the migrations create the table cook_assistance.help_request -- B2-S01 ADR-CA-003", async () => {
      const tables = await sql<{ table_name: string }>`
        select table_name from information_schema.tables
        where table_schema = 'cook_assistance' and table_name = 'help_request'`.execute(database.db);

      expect(tables.rows).toEqual([{ table_name: "help_request" }]);
    });
  });

  describe("findById", () => {
    it("an unknown help request id is absent (undefined), not an error -- B2-S01", async () => {
      const repository = new KyselyHelpRequestRepository(database.db);

      const found = await repository.findById(HelpRequestId.of("0192f1a0-0000-7000-8000-0000000000ff"));

      expect(found).toBeUndefined();
    });
  });

  describe("round trip", () => {
    it("a saved Sunday scones step 3 request is found again with id, requester, title, topic PREPARATION_STEP, question, recipe, step, addressees COMMUNITY and GRANDMA_AVATAR, status REQUESTED and answer deadline 15:00 -- B2-S01 INV-07 INV-17 INV-19 INV-20", async () => {
      const repository = new KyselyHelpRequestRepository(database.db);
      await repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

      const found = await repository.findById(sundaySconesStep3.id);

      expect(found?.id).toBe("0192f1a0-0000-7000-8000-000000000001");
      expect(found?.requester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
      expect(found?.title).toBe("Sunday scones, step 3");
      expect(found?.topic).toBe("PREPARATION_STEP");
      expect(found?.question).toBe("What means 'warm'?");
      expect(found?.recipeId).toBe("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f");
      expect(found?.howToStepId).toBe("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d");
      expect(found?.addressees).toEqual(["COMMUNITY", "GRANDMA_AVATAR"]);
      expect(found?.status).toBe("REQUESTED");
      expect(found?.answerDeadline).toEqual(new Date("2026-10-24T15:00:00Z"));
    });
    it("a loaded help request has no pending events -- B2-S01 ADR-CA-003", async () => {
      const repository = new KyselyHelpRequestRepository(database.db);
      await repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

      const found = await repository.findById(sundaySconesStep3.id);

      expect(found?.pullEvents()).toEqual([]);
    });
  });

  describe("version and optimistic locking", () => {
    it("saving a new help request stores version 1; saving the loaded request again stores version 2 -- B2-S01 ADR-CA-005", async () => {
      const repository = new KyselyHelpRequestRepository(database.db);
      await repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
      const loaded = await repository.findById(sundaySconesStep3.id);
      expect(loaded?.version).toBe(1);

      await repository.save(loaded as HelpRequest);

      expect((await repository.findById(sundaySconesStep3.id))?.version).toBe(2);
    });
    it("an event whose outbox mapping is planned for step 7 is refused with EventNotStoredYet, and nothing is stored -- B1-S02 ADR-CA-004 (planned: increment 1, step 7)", async () => {
      const helpRequest = HelpRequest.requestMealPlanHelp({
        id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000101"),
        requester: sundaySconesStep3.requester,
        title: "Dinner planning",
        question: "A dinner for six on Friday — what should I cook?",
        addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
        chefId: null,
        plannedMealDate: null,
        requestedAt: new Date("2026-10-21T10:00:00Z"),
      });
      helpRequest.requestChefReview({
        helpId: HelpId.of("0192f1a0-0000-7000-8000-0000000001a1"),
        chefId: CookId.of("1c2d3e4f-5a6b-4c7d-8e9f-0a1b2c3d4e5f"),
        requestedAt: new Date("2026-10-24T11:00:00Z"),
      });

      await expect(new KyselyHelpRequestRepository(database.db).save(helpRequest)).rejects.toThrow(EventNotStoredYet);

      const stored = await sql<{ count: string }>`
        select (select count(*) from cook_assistance.help_request) + (select count(*) from cook_assistance.outbox) as count`.execute(
        database.db,
      );
      expect(stored.rows).toEqual([{ count: "0" }]);
    });
    it("a database error other than a duplicate passes through unchanged, not as ConcurrencyConflict -- B2-S01 ADR-CA-005 (AP0005 coverage)", async () => {
      const broken = HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, requester: CookId.of("not-a-uuid") });

      const outcome = await new KyselyHelpRequestRepository(database.db).save(broken).catch((error: unknown) => error);

      expect(outcome).not.toBeInstanceOf(ConcurrencyConflict);
      expect((outcome as { code?: string }).code).toBe("22P02"); // invalid_text_representation
    });
    it("two loads of the same help request, both saved: the second save fails with ConcurrencyConflict and the stored version is the first save's -- B2-S01 ADR-CA-005", async () => {
      const repository = new KyselyHelpRequestRepository(database.db);
      await repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
      const first = (await repository.findById(sundaySconesStep3.id)) as HelpRequest;
      const second = (await repository.findById(sundaySconesStep3.id)) as HelpRequest;
      await repository.save(first);

      await expect(repository.save(second)).rejects.toBeInstanceOf(ConcurrencyConflict);
      expect((await repository.findById(sundaySconesStep3.id))?.version).toBe(2);
    });
  });

  describe("outbox", () => {
    it("saving a new help request appends one outbox row: aggregate_type HelpRequest, its id, event_type HelpRequested, the payload under the AsyncAPI field names (description = the question, ISO timestamps, pictures []), unpublished, delivery columns at their start values -- B2-S01 ADR-CA-004 AsyncAPI HelpRequested", async () => {
      await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

      const rows = await sql`
        select aggregate_type, aggregate_id, event_type, payload, occurred_at, published_at,
               attempts, next_attempt_at = '-infinity'::timestamptz as due_at_once, last_error, dead_lettered_at
        from cook_assistance.outbox`.execute(database.db);

      expect(rows.rows).toEqual([
        {
          aggregate_type: "HelpRequest",
          aggregate_id: "0192f1a0-0000-7000-8000-000000000001",
          event_type: "HelpRequested",
          payload: {
            helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
            requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
            title: "Sunday scones, step 3",
            topic: "PREPARATION_STEP",
            description: "What means 'warm'?",
            recipeId: "c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f",
            howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
            addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
            pictures: [],
            status: "REQUESTED",
            answerDeadline: "2026-10-24T15:00:00.000Z",
            requestedAt: "2026-10-24T10:00:00.000Z",
          },
          occurred_at: new Date("2026-10-24T10:00:00Z"),
          published_at: null,
          attempts: 0,
          due_at_once: true,
          last_error: null,
          dead_lettered_at: null,
        },
      ]);
    });
    // Planned for increment 1 (Board: Meal plan): only then do loaded help requests carry pending events
    // (cancel, close), so a conflicting save could append outbox rows. In increment 0 this test cannot fail;
    // atomicity of state and outbox is proven by the unit-of-work test below.
    it.todo(
      "a save that fails with ConcurrencyConflict leaves neither changed state nor outbox rows -- B2-S01 ADR-CA-004 ADR-CA-005 (planned: increment 1)",
    );
  });

  describe("unit of work", () => {
    it("a save inside a unit of work joins its transaction: when the work throws afterwards, neither the help request nor its outbox row is stored -- B2-S01 ADR-CA-004", async () => {
      const failure = new Error("the use case failed after saving");

      const outcome = new KyselyUnitOfWork(database.db).transactionally(async (tx) => {
        await new KyselyHelpRequestRepository(tx).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
        throw failure;
      });

      await expect(outcome).rejects.toBe(failure);
      const stored = await sql`
        select (select count(*) from cook_assistance.help_request)::int as requests,
               (select count(*) from cook_assistance.outbox)::int as outbox_rows`.execute(database.db);
      expect(stored.rows).toEqual([{ requests: 0, outbox_rows: 0 }]);
    });
  });
});
