import { sql } from "kysely";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { migrateToLatest } from "./adapter/out/persistence/migrations.js";
import { startPostgres, type TestDatabase } from "./adapter/out/persistence/test-support/postgres-container.js";
import { MessageId } from "./application/ports/processed-messages.js";
import type { RequestPreparationStepHelpCommand } from "./application/request-preparation-step-help.js";
import { composeCookAssistance } from "./composition.js";
import { InMemoryRecipeCatalog } from "./test-support/fakes/in-memory-recipe-catalog.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "./test-support/fixtures/sunday-scones.js";

// Wiring — increment 0 (walking skeleton), step 8, unit 4: composeCookAssistance on PostgreSQL 16 (Testcontainers),
// the production Clock and IdGenerator, and the in-memory Recipe Catalog (its real adapter comes with the outbound adapters).

const UUID_V7 = /^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

/** The Cook asks what "warm" means in Sunday scones, step 3. */
const askWhatWarmMeans: RequestPreparationStepHelpCommand = {
  requester: sundaySconesStep3.requester,
  title: sundaySconesStep3.title,
  question: sundaySconesStep3.question,
  recipeId: sundaySconesStep3.recipeId,
  howToStepId: sundaySconesStep3.howToStepId,
  addressees: sundaySconesStep3.addressees,
};

describe("composeCookAssistance — PostgreSQL 16", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
  });

  beforeEach(async () => {
    await sql`truncate cook_assistance.help_request, cook_assistance.help, cook_assistance.outbox, cook_assistance.processed_message`.execute(
      database.db,
    );
  });

  afterAll(async () => {
    await database.stop();
  });

  function compose() {
    const recipeCatalog = new InMemoryRecipeCatalog();
    recipeCatalog.addHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);
    return composeCookAssistance({ db: database.db, recipeCatalog });
  }

  it("the composed requestPreparationStepHelp stores Sunday scones step 3 with HelpRequested in the outbox, under a UUIDv7 id -- B2-S01 wiring ADR-CA-004 ADR-CA-007", async () => {
    const { id } = await compose().requestPreparationStepHelp.execute(askWhatWarmMeans);

    expect(id).toMatch(UUID_V7);
    const stored = await sql<{ status: string }>`select status from cook_assistance.help_request where id = ${id}`.execute(database.db);
    expect(stored.rows).toEqual([{ status: "REQUESTED" }]);
    const outbox = await sql<{ event_type: string }>`select event_type from cook_assistance.outbox where aggregate_id = ${id}`.execute(
      database.db,
    );
    expect(outbox.rows).toEqual([{ event_type: "HelpRequested" }]);
  });
  it("the composed provideHelpWhenGrandmaAvatarAnswers provides help for that request: the help row, HelpProvided in the outbox, the message recorded -- B1-S04 wiring ADR-CA-004", async () => {
    const cookAssistance = compose();
    const { id: helpRequestId } = await cookAssistance.requestPreparationStepHelp.execute(askWhatWarmMeans);
    const messageId = MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d");

    const outcome = await cookAssistance.provideHelpWhenGrandmaAvatarAnswers.handle({
      messageId,
      helpId: grandmaExplainsWarm.id,
      helpRequestId,
      answerTitle: grandmaExplainsWarm.answerTitle,
      answer: grandmaExplainsWarm.answer,
    });

    expect(outcome).toEqual({ outcome: "HELP_PROVIDED", helpId: grandmaExplainsWarm.id });
    const help = await sql<{ help_request_id: string }>`
      select help_request_id from cook_assistance.help where id = ${grandmaExplainsWarm.id}`.execute(database.db);
    expect(help.rows).toEqual([{ help_request_id: helpRequestId }]);
    const outbox = await sql<{ event_type: string }>`
      select event_type from cook_assistance.outbox where aggregate_id = ${grandmaExplainsWarm.id}`.execute(database.db);
    expect(outbox.rows).toEqual([{ event_type: "HelpProvided" }]);
    const processed = await sql<{ message_id: string }>`
      select message_id from cook_assistance.processed_message`.execute(database.db);
    expect(processed.rows).toEqual([{ message_id: messageId }]);
  });
});
