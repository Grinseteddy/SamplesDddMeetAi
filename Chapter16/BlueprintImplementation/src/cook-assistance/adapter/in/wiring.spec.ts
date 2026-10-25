import type { INestApplication } from "@nestjs/common";
import { sql } from "kysely";
import request from "supertest";
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { type CookAssistance, composeCookAssistance } from "../../composition.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "../../test-support/fixtures/sunday-scones.js";
import { RecordingDelivery } from "../../test-support/messaging/recording-delivery.js";
import { InMemoryRecipeCatalog } from "../../test-support/fakes/in-memory-recipe-catalog.js";
import { LocalIssuer } from "../../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../../test-support/http/start-http-test-app.js";
import { migrateToLatest } from "../out/persistence/migrations.js";
import { startPostgres, type TestDatabase } from "../out/persistence/test-support/postgres-container.js";
import { createHttpApp } from "./http/http-app.js";
import { createGrandmaAvatarHelpProvidedConsumer } from "./messaging/grandma-avatar-help-provided.consumer.js";
import { resetSchema } from "../out/persistence/test-support/reset-schema.js";

// Wiring — increment 0, step 9, unit 9.4: the inbound adapters through the real persistence adapters (Testcontainers PostgreSQL 16).

describe("inbound wiring — PostgreSQL 16", () => {
  let database: TestDatabase;
  let issuer: LocalIssuer;
  let app: INestApplication;
  let cookAssistance: CookAssistance;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
    issuer = await LocalIssuer.start();
  });

  beforeEach(async () => {
    await resetSchema(database.db);
    const recipeCatalog = new InMemoryRecipeCatalog();
    recipeCatalog.addHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);
    cookAssistance = composeCookAssistance({ db: database.db, recipeCatalog });
    app = await createHttpApp({ cookAssistance, tokenVerifier: issuer.verifier, publicBaseUrl: PUBLIC_BASE_URL });
  });

  afterEach(async () => {
    await app.close();
  });

  afterAll(async () => {
    await database.stop();
  });

  it("POST /help-requests stores Sunday scones step 3 in PostgreSQL with HelpRequested in the outbox -- op requestHelp wiring", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${await issuer.tokenFor({ sub: sundaySconesStep3.requester })}`, version: "2.0.0" })
      .send({
        title: sundaySconesStep3.title,
        topic: "PREPARATION_STEP",
        description: sundaySconesStep3.question,
        recipeId: sundaySconesStep3.recipeId,
        howToStepId: sundaySconesStep3.howToStepId,
        addressees: [...sundaySconesStep3.addressees],
      });

    expect(response.status).toBe(201);
    const id: string = response.body.helpRequestId;
    const stored = await sql<{ status: string }>`select status from cook_assistance.help_request where id = ${id}`.execute(database.db);
    expect(stored.rows).toEqual([{ status: "REQUESTED" }]);
    const outbox = await sql<{ event_type: string }>`select event_type from cook_assistance.outbox where aggregate_id = ${id}`.execute(
      database.db,
    );
    expect(outbox.rows).toEqual([{ event_type: "HelpRequested" }]);
  });
  it("a raw Grandma Avatar message stores the help, its HelpProvided outbox row and the processed message -- op receiveGrandmaAvatarHelpProvided wiring", async () => {
    const { id: helpRequestId } = await cookAssistance.requestPreparationStepHelp.execute({
      requester: sundaySconesStep3.requester,
      title: sundaySconesStep3.title,
      question: sundaySconesStep3.question,
      recipeId: sundaySconesStep3.recipeId,
      howToStepId: sundaySconesStep3.howToStepId,
      addressees: sundaySconesStep3.addressees,
    });
    const consumer = await createGrandmaAvatarHelpProvidedConsumer({
      handler: cookAssistance.provideHelpWhenGrandmaAvatarAnswers,
      log: () => {},
    });
    const delivery = new RecordingDelivery();

    await consumer.consume(
      {
        headers: {
          correlationId: "0b1c2d3e-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
          messageId: "8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d",
          source: "grandma-avatar",
        },
        payload: {
          helpId: grandmaExplainsWarm.id,
          helpRequest: helpRequestId,
          helpRequester: sundaySconesStep3.requester,
          answerTitle: grandmaExplainsWarm.answerTitle,
          helpProviderType: "GRANDMA_AVATAR",
          answer: {
            answerType: "PREPARATION_STEP_EXPLANATION",
            recipeId: sundaySconesStep3.recipeId,
            howToStepId: sundaySconesStep3.howToStepId,
            description: grandmaExplainsWarm.answer.description,
          },
        },
      },
      delivery,
    );

    expect(delivery.settled).toEqual(["ack"]);
    const help = await sql<{ help_request_id: string }>`
      select help_request_id from cook_assistance.help where id = ${grandmaExplainsWarm.id}`.execute(database.db);
    expect(help.rows).toEqual([{ help_request_id: helpRequestId }]);
    const outbox = await sql<{ event_type: string }>`
      select event_type from cook_assistance.outbox where aggregate_id = ${grandmaExplainsWarm.id}`.execute(database.db);
    expect(outbox.rows).toEqual([{ event_type: "HelpProvided" }]);
    const processed = await sql<{ message_id: string }>`select message_id from cook_assistance.processed_message`.execute(database.db);
    expect(processed.rows).toEqual([{ message_id: "8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d" }]);
  });
});
