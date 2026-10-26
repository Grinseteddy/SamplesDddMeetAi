import { sql } from "kysely";
import { afterAll, beforeAll, beforeEach, describe, expect, expectTypeOf, it } from "vitest";
import { HelpRequest } from "../../../../domain/help-request/help-request.js";
import type { DomainEvent } from "../../../../domain/domain-events.js";
import { Help } from "../../../../domain/help/help.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "../../../../test-support/fixtures/sunday-scones.js";
import { ConcurrencyConflict } from "../../../../domain/shared/concurrency-conflict.js";
import { KyselyHelpRepository } from "../help-repository.js";
import { KyselyHelpRequestRepository } from "../help-request-repository.js";
import { PROJECTIONS, rebuildViews, UnhandledEvent, PLANNED_PROJECTIONS } from "./projections.js";
import { migrateToLatest } from "../migrations.js";
import { startPostgres, type TestDatabase } from "../test-support/postgres-container.js";
import { resetSchema } from "../test-support/reset-schema.js";

// Query side — increment 0, step 11, unit 11.1: the read models "Help request" and "Help" (ADR-CA-012, amended):
// projected in the save transaction, rebuildable by replaying the outbox. Real PostgreSQL 16.

describe("projections — help_request_view, help_view (PostgreSQL 16)", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
  }, 120_000);

  beforeEach(async () => {
    await resetSchema(database.db);
  });

  afterAll(async () => {
    await database.stop();
  });

  /** Both read models, row by row. */
  async function views(): Promise<{ helpRequests: unknown[]; helps: unknown[] }> {
    const helpRequests = await sql`select * from cook_assistance.help_request_view order by id`.execute(database.db);
    const helps = await sql`select * from cook_assistance.help_view order by id`.execute(database.db);
    return { helpRequests: helpRequests.rows, helps: helps.rows };
  }

  async function countOf(table: string): Promise<number> {
    const counted = await sql<{ count: string }>`select count(*) from ${sql.table(`cook_assistance.${table}`)}`.execute(database.db);
    return Number(counted.rows[0]?.count);
  }

  /** "name type nullable" per column, in table order. */
  async function columnsOf(table: string): Promise<string[]> {
    const columns = await sql<{ column: string }>`
      select column_name || ' ' || data_type || ' ' || is_nullable as column from information_schema.columns
      where table_schema = 'cook_assistance' and table_name = ${table} order by ordinal_position`.execute(database.db);
    return columns.rows.map((row) => row.column);
  }

  it("migration 006 creates help_request_view and help_view with indexes on status, topic, requester, addressees and help_request_id -- ADR-CA-012", async () => {
    expect(await columnsOf("help_request_view")).toEqual([
      "id uuid NO",
      "requester uuid NO",
      "title text NO",
      "topic text NO",
      "description text NO",
      "recipe_id uuid YES",
      "how_to_step_id uuid YES",
      "addressees ARRAY NO",
      "status text NO",
      "answer_deadline timestamp with time zone NO",
      "created_at timestamp with time zone NO",
      "updated_at timestamp with time zone NO",
    ]);
    expect(await columnsOf("help_view")).toEqual([
      "id uuid NO",
      "help_request_id uuid NO",
      "help_requester uuid NO",
      "answer_title text NO",
      "provider_type text NO",
      "provider_id uuid YES",
      "answer jsonb NO",
      "status text NO",
      "created_at timestamp with time zone NO",
      "updated_at timestamp with time zone NO",
    ]);
    const indexes = await sql<{ indexname: string }>`
      select indexname from pg_indexes where schemaname = 'cook_assistance' and tablename in ('help_request_view', 'help_view')
      order by indexname`.execute(database.db);
    expect(indexes.rows.map((row) => row.indexname)).toEqual([
      "help_request_view_addressees_idx",
      "help_request_view_created_at_id_idx",
      "help_request_view_pkey",
      "help_request_view_requester_idx",
      "help_request_view_status_idx",
      "help_request_view_topic_idx",
      "help_view_help_request_id_created_at_idx",
      "help_view_pkey",
    ]);
  });
  it("saving Sunday scones step 3 writes its help_request_view row: every field, REQUESTED, answerDeadline 15:00, createdAt = updatedAt = 10:00 -- RM Help request, evt HelpRequested", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

    const rows = await sql`select * from cook_assistance.help_request_view`.execute(database.db);
    expect(rows.rows).toEqual([
      {
        id: sundaySconesStep3.id,
        requester: sundaySconesStep3.requester,
        title: "Sunday scones, step 3",
        topic: "PREPARATION_STEP",
        description: "What means 'warm'?",
        recipe_id: sundaySconesStep3.recipeId,
        how_to_step_id: sundaySconesStep3.howToStepId,
        addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
        status: "REQUESTED",
        answer_deadline: new Date("2026-10-24T15:00:00Z"),
        created_at: new Date("2026-10-24T10:00:00Z"),
        updated_at: new Date("2026-10-24T10:00:00Z"),
      },
    ]);
  });
  it("saving Grandma's help writes its help_view row: every field, provider GRANDMA_AVATAR without provider id, status PROVIDED, createdAt = updatedAt = providedAt -- RM Help, evt HelpProvided", async () => {
    await new KyselyHelpRepository(database.db).save(Help.provide(grandmaExplainsWarm));

    const rows = await sql`select * from cook_assistance.help_view`.execute(database.db);
    expect(rows.rows).toEqual([
      {
        id: grandmaExplainsWarm.id,
        help_request_id: sundaySconesStep3.id,
        help_requester: sundaySconesStep3.requester,
        answer_title: "Warm means about 30 °C",
        provider_type: "GRANDMA_AVATAR",
        provider_id: null,
        answer: {
          topic: "PREPARATION_STEP",
          recipeId: sundaySconesStep3.recipeId,
          howToStepId: sundaySconesStep3.howToStepId,
          description: "Put the bowl next to the heater or in the oven with only the light on.",
          images: [],
        },
        status: "PROVIDED",
        created_at: new Date("2026-10-24T12:00:00Z"),
        updated_at: new Date("2026-10-24T12:00:00Z"),
      },
    ]);
  });
  it("one transaction: a save that fails with ConcurrencyConflict adds no view row; a save whose projection fails leaves neither state nor outbox row -- ADR-CA-012", async () => {
    const repository = new KyselyHelpRequestRepository(database.db);
    await repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

    await expect(repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3))).rejects.toThrow(ConcurrencyConflict);
    expect(await countOf("help_request_view")).toBe(1);

    await resetSchema(database.db);
    await sql`
      insert into cook_assistance.help_request_view (id, requester, title, topic, description, addressees, status, answer_deadline, created_at, updated_at)
      values (${sundaySconesStep3.id}, ${sundaySconesStep3.requester}, 'stale', 'PREPARATION_STEP', 'stale', '{}', 'REQUESTED', now(), now(), now())`.execute(
      database.db,
    );
    await expect(repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3))).rejects.toThrow(/help_request_view_pkey/);
    expect([await countOf("help_request"), await countOf("outbox")]).toEqual([0, 0]);
  });
  it("gate: every domain event type has a projection decision, checked at compile time -- ADR-CA-012", () => {
    // checked by tsc: a new domain event fails the build until it is projected, ignored or planned for a later step
    expectTypeOf<keyof typeof PROJECTIONS | keyof typeof PLANNED_PROJECTIONS>().toEqualTypeOf<DomainEvent["type"]>();
  });
  it("rebuild: damaged views are rebuilt from the outbox to the original rows (a rebuild that does nothing fails) -- ADR-CA-012", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    await new KyselyHelpRepository(database.db).save(Help.provide(grandmaExplainsWarm));
    const original = await views();
    await sql`update cook_assistance.help_request_view set title = 'damaged'`.execute(database.db);
    await sql`delete from cook_assistance.help_view`.execute(database.db);

    await rebuildViews(database.db);

    expect(await views()).toEqual(original);
  });
  it("rebuild with an outbox row of an event type without a decision fails with UnhandledEvent; the views stay as they were -- ADR-CA-012", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    await new KyselyHelpRepository(database.db).save(Help.provide(grandmaExplainsWarm));
    const original = await views();
    await sql`
      insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
      values ('Help', ${grandmaExplainsWarm.id}, 'HelpRated', '{}', now())`.execute(database.db);

    await expect(rebuildViews(database.db)).rejects.toThrow(new UnhandledEvent("HelpRated"));
    expect(await views()).toEqual(original);
  });
});
