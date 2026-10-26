import { sql } from "kysely";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { migrateToLatest } from "./migrations.js";
import { HelpRequestId } from "../../../domain/help-request/help-request.js";
import { Help, HelpId } from "../../../domain/help/help.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { CookId, HowToStepId, RecipeId } from "../../../domain/shared/ids.js";
import { KyselyHelpRepository } from "./help-repository.js";
import { KyselyUnitOfWork } from "./unit-of-work.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";
import { resetSchema } from "./test-support/reset-schema.js";

// Persistence — increment 0 (walking skeleton), step 7, unit 3: HelpRepository (Kysely adapter).
// Real PostgreSQL 16 through Testcontainers (ADR-CA-016). Slice B1-S04 (Preparation step variant).

// Grandma Avatar explains what "warm" means in Sunday scones, step 3 (Example Mapping Preparation step).
const grandmaExplainsWarm = {
  id: HelpId.of("0192f1a0-0000-7000-8000-0000000000a1"),
  helpRequest: {
    id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000001"),
    requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
    topic: "PREPARATION_STEP",
    recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
    howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
  },
  answerTitle: "Warm means about 30 °C",
  provider: { type: "GRANDMA_AVATAR", id: null },
  answer: {
    topic: "PREPARATION_STEP",
    recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
    howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
    description: "Put the bowl next to the heater or in the oven with only the light on.",
    images: [],
  },
  providedAt: new Date("2026-10-24T12:00:00Z"),
} as const;

// one PostgreSQL container for the whole file (fewer parallel container starts in the full suite)
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

describe("KyselyHelpRepository — PostgreSQL 16", () => {
  describe("schema", () => {
    it("the migrations create the table cook_assistance.help -- B1-S04 ADR-CA-003", async () => {
      const tables = await sql<{ table_name: string }>`
        select table_name from information_schema.tables
        where table_schema = 'cook_assistance' and table_name = 'help'`.execute(database.db);

      expect(tables.rows).toEqual([{ table_name: "help" }]);
    });
    it("help.help_request_id is a plain indexed reference — no foreign key to another aggregate's table -- B1-S04 ADR-CA-003", async () => {
      const indexes = await sql<{ indexdef: string }>`
        select indexdef from pg_indexes
        where schemaname = 'cook_assistance' and tablename = 'help' and indexdef like '%(help_request_id)%'`.execute(database.db);
      const foreignKeys = await sql<{ constraint_name: string }>`
        select constraint_name from information_schema.table_constraints
        where table_schema = 'cook_assistance' and table_name = 'help' and constraint_type = 'FOREIGN KEY'`.execute(database.db);

      expect(indexes.rows).toHaveLength(1);
      expect(foreignKeys.rows).toEqual([]);
    });
  });

  describe("findById", () => {
    it("an unknown help id is absent (undefined), not an error -- B1-S04", async () => {
      const found = await new KyselyHelpRepository(database.db).findById(HelpId.of("0192f1a0-0000-7000-8000-0000000000ff"));

      expect(found).toBeUndefined();
    });
  });

  describe("round trip", () => {
    it("a saved Grandma Avatar help for Sunday scones step 3 is found again with id, help request id, requester, answer title, provider GRANDMA_AVATAR without a provider id, the preparation-step answer and status PROVIDED -- B1-S04 INV-10 INV-16", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(Help.provide(grandmaExplainsWarm));

      const found = await repository.findById(grandmaExplainsWarm.id);

      expect(found?.id).toBe("0192f1a0-0000-7000-8000-0000000000a1");
      expect(found?.helpRequestId).toBe("0192f1a0-0000-7000-8000-000000000001");
      expect(found?.helpRequester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
      expect(found?.answerTitle).toBe("Warm means about 30 °C");
      expect(found?.provider).toEqual({ type: "GRANDMA_AVATAR", id: null });
      expect(found?.answer).toEqual(grandmaExplainsWarm.answer);
      expect(found?.status).toBe("PROVIDED");
    });
    it("a help with a provider id (a community cook) is found again with that id -- B1-S04 (AP0005 coverage)", async () => {
      const repository = new KyselyHelpRepository(database.db);
      const communityCook = { type: "COMMUNITY", id: CookId.of("1b2c3d4e-5f6a-4b7c-8d9e-0f1a2b3c4d5e") } as const;
      await repository.save(Help.provide({ ...grandmaExplainsWarm, provider: communityCook }));

      expect((await repository.findById(grandmaExplainsWarm.id))?.provider).toEqual(communityCook);
    });
    it("a loaded help has no pending events -- B1-S04 ADR-CA-003", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(Help.provide(grandmaExplainsWarm));

      const found = await repository.findById(grandmaExplainsWarm.id);

      expect(found?.pullEvents()).toEqual([]);
    });
  });

  describe("version and optimistic locking", () => {
    it("saving a new help stores version 1; saving the loaded help again stores version 2 -- B1-S04 ADR-CA-005", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(Help.provide(grandmaExplainsWarm));
      const loaded = await repository.findById(grandmaExplainsWarm.id);
      expect(loaded?.version).toBe(1);

      await repository.save(loaded as Help);

      expect((await repository.findById(grandmaExplainsWarm.id))?.version).toBe(2);
    });
    it("two loads of the same help, both saved: the second save fails with ConcurrencyConflict and the stored version is the first save's -- B1-S04 ADR-CA-005", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(Help.provide(grandmaExplainsWarm));
      const first = (await repository.findById(grandmaExplainsWarm.id)) as Help;
      const second = (await repository.findById(grandmaExplainsWarm.id)) as Help;
      await repository.save(first);

      await expect(repository.save(second)).rejects.toBeInstanceOf(ConcurrencyConflict);
      expect((await repository.findById(grandmaExplainsWarm.id))?.version).toBe(2);
    });
  });

  describe("outbox", () => {
    it("saving a new help appends one outbox row: aggregate_type Help, its id, event_type HelpProvided, the payload under the AsyncAPI field names (helpProvider null, the answer, status PROVIDED, providedAt ISO), unpublished, delivery columns at their start values -- B1-S04 ADR-CA-004 AsyncAPI HelpProvided", async () => {
      await new KyselyHelpRepository(database.db).save(Help.provide(grandmaExplainsWarm));

      const rows = await sql`
        select aggregate_type, aggregate_id, event_type, payload, occurred_at, published_at,
               attempts, next_attempt_at = '-infinity'::timestamptz as due_at_once, last_error, dead_lettered_at
        from cook_assistance.outbox`.execute(database.db);

      expect(rows.rows).toEqual([
        {
          aggregate_type: "Help",
          aggregate_id: "0192f1a0-0000-7000-8000-0000000000a1",
          event_type: "HelpProvided",
          payload: {
            helpId: "0192f1a0-0000-7000-8000-0000000000a1",
            helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
            helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
            answerTitle: "Warm means about 30 °C",
            helpProviderType: "GRANDMA_AVATAR",
            helpProvider: null,
            answer: {
              topic: "PREPARATION_STEP",
              recipeId: "c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f",
              howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
              description: "Put the bowl next to the heater or in the oven with only the light on.",
              images: [],
            },
            status: "PROVIDED",
            providedAt: "2026-10-24T12:00:00.000Z",
          },
          occurred_at: new Date("2026-10-24T12:00:00Z"),
          published_at: null,
          attempts: 0,
          due_at_once: true,
          last_error: null,
          dead_lettered_at: null,
        },
      ]);
    });
    // Planned for increment 1 (Board: Meal plan): only accept/reject give a loaded help pending events.
    it("a save that fails with ConcurrencyConflict leaves neither changed state nor outbox rows -- B1-S04 ADR-CA-004 ADR-CA-005 (planned: increment 1)", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(Help.provide(grandmaExplainsWarm));
      const first = (await repository.findById(grandmaExplainsWarm.id)) as Help;
      const second = (await repository.findById(grandmaExplainsWarm.id)) as Help;
      first.accept(new Date("2026-10-24T13:00:00Z"));
      second.reject(new Date("2026-10-24T13:05:00Z"));
      await repository.save(first);

      await expect(repository.save(second)).rejects.toThrow(ConcurrencyConflict);

      const outbox = await sql<{ event_type: string }>`select event_type from cook_assistance.outbox order by id`.execute(database.db);
      expect(outbox.rows).toEqual([{ event_type: "HelpProvided" }, { event_type: "HelpAccepted" }]);
      expect((await repository.findById(grandmaExplainsWarm.id))?.status).toBe("ACCEPTED");
    });
  });

  describe("unit of work", () => {
    it("a save inside a unit of work joins its transaction: when the work throws afterwards, neither the help nor its outbox row is stored -- B1-S04 ADR-CA-004", async () => {
      const failure = new Error("the use case failed after saving");

      const outcome = new KyselyUnitOfWork(database.db).transactionally(async (tx) => {
        await new KyselyHelpRepository(tx).save(Help.provide(grandmaExplainsWarm));
        throw failure;
      });

      await expect(outcome).rejects.toBe(failure);
      const stored = await sql`
        select (select count(*) from cook_assistance.help)::int as helps,
               (select count(*) from cook_assistance.outbox)::int as outbox_rows`.execute(database.db);
      expect(stored.rows).toEqual([{ helps: 0, outbox_rows: 0 }]);
    });
  });
});

// Increment 1 — Board: Meal plan. Menu proposals and the requester's decision, stored with their outbox rows
// (ADR-CA-003, ADR-CA-004, ADR-CA-005). The answer column (jsonb) holds every answer kind: no migration.
const communityCook = CookId.of("2d3e4f5a-6b7c-4d8e-9f0a-1b2c3d4e5f6a");
const twoCourseDinner = {
  topic: "MENU",
  note: "A light start, then something warm.",
  servings: 6,
  meal: "DINNER",
  howToServe: null,
  courses: [
    { dish: 1, recipeId: RecipeId.of("3e4f5a6b-7c8d-4e9f-8a0b-1c2d3e4f5a6b") },
    { dish: 2, recipeId: RecipeId.of("4f5a6b7c-8d9e-4f0a-9b1c-2d3e4f5a6b7c") },
  ],
} as const;
const communityMenuId = HelpId.of("0192f1a0-0000-7000-8000-0000000001b1");
/** A community cook's menu for the dinner for six (Menu Example Mapping). */
const communityMenu = () =>
  Help.provide({
    id: communityMenuId,
    helpRequest: {
      id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000101"),
      requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
      topic: "MENU",
      recipeId: null,
      howToStepId: null,
    },
    answerTitle: "Soup, then lasagna",
    provider: { type: "COMMUNITY", id: communityCook },
    answer: twoCourseDinner,
    providedAt: new Date("2026-10-21T18:00:00Z"),
  });

describe("KyselyHelpRepository — increment 1, Board: Meal plan", () => {
  describe("round trip", () => {
    it("a saved menu proposal by a community cook is found again with the provider and the whole proposal -- B1-S03", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(communityMenu());

      const found = await repository.findById(communityMenuId);

      expect([found?.provider, found?.answer, found?.status]).toEqual([
        { type: "COMMUNITY", id: communityCook },
        twoCourseDinner,
        "PROVIDED",
      ]);
    });
    it("a loaded help accepted and saved again is found ACCEPTED with version 2 -- B1-S05 ADR-CA-005", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(communityMenu());
      const loaded = (await repository.findById(communityMenuId)) as Help;

      loaded.accept(new Date("2026-10-21T19:00:00Z"));
      await repository.save(loaded);

      const found = await repository.findById(communityMenuId);
      expect([found?.status, found?.version]).toEqual(["ACCEPTED", 2]);
    });
    it("a loaded help rejected and saved again is found REJECTED -- B1-S09", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(communityMenu());
      const loaded = (await repository.findById(communityMenuId)) as Help;

      loaded.reject(new Date("2026-10-21T19:00:00Z"));
      await repository.save(loaded);

      expect((await repository.findById(communityMenuId))?.status).toBe("REJECTED");
    });
  });

  describe("outbox", () => {
    it("the HelpProvided row of a menu proposal carries the proposal under the AsyncAPI field names -- B1-S03 AsyncAPI HelpProvided", async () => {
      await new KyselyHelpRepository(database.db).save(communityMenu());

      const rows = await sql<{ payload: Record<string, unknown> }>`
        select payload from cook_assistance.outbox where event_type = 'HelpProvided'`.execute(database.db);

      expect(rows.rows[0]?.payload).toMatchObject({ helpProviderType: "COMMUNITY", helpProvider: communityCook, answer: twoCourseDinner });
    });
    it("accepting appends a HelpAccepted row: helpId, helpRequestId, helpRequester, helpProviderType, helpProvider, status ACCEPTED, decidedAt -- B1-S05 AsyncAPI HelpAccepted", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(communityMenu());
      const loaded = (await repository.findById(communityMenuId)) as Help;
      loaded.accept(new Date("2026-10-21T19:00:00Z"));

      await repository.save(loaded);

      const rows = await sql<{ payload: unknown; occurred_at: Date }>`
        select payload, occurred_at from cook_assistance.outbox where event_type = 'HelpAccepted'`.execute(database.db);
      expect(rows.rows).toEqual([
        {
          payload: {
            helpId: communityMenuId,
            helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
            helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
            helpProviderType: "COMMUNITY",
            helpProvider: communityCook,
            status: "ACCEPTED",
            decidedAt: "2026-10-21T19:00:00.000Z",
          },
          occurred_at: new Date("2026-10-21T19:00:00Z"),
        },
      ]);
    });
    it("rejecting appends a HelpRejected row with the same fields and status REJECTED -- B1-S09 AsyncAPI HelpRejected", async () => {
      const repository = new KyselyHelpRepository(database.db);
      await repository.save(communityMenu());
      const loaded = (await repository.findById(communityMenuId)) as Help;
      loaded.reject(new Date("2026-10-21T19:00:00Z"));

      await repository.save(loaded);

      const rows = await sql<{ payload: unknown; occurred_at: Date }>`
        select payload, occurred_at from cook_assistance.outbox where event_type = 'HelpRejected'`.execute(database.db);
      expect(rows.rows).toEqual([
        {
          payload: {
            helpId: communityMenuId,
            helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
            helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
            helpProviderType: "COMMUNITY",
            helpProvider: communityCook,
            status: "REJECTED",
            decidedAt: "2026-10-21T19:00:00.000Z",
          },
          occurred_at: new Date("2026-10-21T19:00:00Z"),
        },
      ]);
    });
  });
});
