import { sql } from "kysely";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { HelpRequest, HelpRequestId } from "../../../domain/help-request/help-request.js";
import { Help } from "../../../domain/help/help.js";
import { FakePublisher } from "../../../test-support/fakes/fake-publisher.js";
import { FixedClock } from "../../../test-support/fakes/fixed-clock.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "../../../test-support/fixtures/sunday-scones.js";
import { KyselyHelpRepository } from "../persistence/help-repository.js";
import { KyselyHelpRequestRepository } from "../persistence/help-request-repository.js";
import { migrateToLatest } from "../persistence/migrations.js";
import { startPostgres, type TestDatabase } from "../persistence/test-support/postgres-container.js";
import { type DeadLetteredRow, OutboxRelay } from "./outbox-relay.js";
import { loadSendCheck, type SendCheck } from "./send-check.js";
import { resetSchema } from "../persistence/test-support/reset-schema.js";

// Outbound messaging — increment 0, step 10, unit 10.2: the outbox relay (ADR-CA-004). Real PostgreSQL 16, a fake publisher, an injected clock.

const NOW = new Date("2026-10-24T10:00:05Z");

describe("outbox relay — PostgreSQL 16", () => {
  let database: TestDatabase;
  let check: SendCheck;
  let publisher: FakePublisher;
  let reported: DeadLetteredRow[];

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
    check = await loadSendCheck();
  });

  beforeEach(async () => {
    await resetSchema(database.db);
    publisher = new FakePublisher();
    reported = [];
  });

  afterAll(async () => {
    await database.stop();
  });

  function relayAt(now: Date, withPublisher: FakePublisher = publisher): OutboxRelay {
    return new OutboxRelay({
      db: database.db,
      publisher: withPublisher,
      clock: new FixedClock(now),
      check,
      publicBaseUrl: "https://larder.org/cooking-assistance",
      reportDeadLetter: (row) => reported.push(row),
    });
  }

  async function outboxRows() {
    return (
      await sql<{ message_id: string; published_at: Date | null; attempts: number; dead_lettered_at: Date | null }>`
        select message_id::text, published_at, attempts, dead_lettered_at from cook_assistance.outbox order by id`.execute(database.db)
    ).rows;
  }

  it("one pending row is published once and marked published -- ADR-CA-004", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

    await relayAt(NOW).runOnce();

    expect(publisher.published.map((message) => message.routingKey)).toEqual(["help.requested"]);
    const [row] = await outboxRows();
    expect(publisher.published[0]?.headers.messageId).toBe(row?.message_id);
    expect(row?.published_at).toEqual(NOW);
  });
  it("a published row is not sent again -- ADR-CA-004", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    await relayAt(NOW).runOnce();

    await relayAt(new Date(NOW.getTime() + 60_000)).runOnce();

    expect(publisher.published).toHaveLength(1);
  });
  it("a broker failure: the row stays pending with attempts 1, the error and next_attempt_at = now + 1 s, and is not retried before then -- ADR-CA-004", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    publisher.failNext(1);

    await relayAt(NOW).runOnce();

    const failed = await sql<{ attempts: number; last_error: string; next_attempt_at: Date; published_at: Date | null }>`
      select attempts, last_error, next_attempt_at, published_at from cook_assistance.outbox`.execute(database.db);
    expect(failed.rows).toEqual([
      { attempts: 1, last_error: "broker unreachable", next_attempt_at: new Date(NOW.getTime() + 1_000), published_at: null },
    ]);
    await relayAt(new Date(NOW.getTime() + 500)).runOnce();
    expect(publisher.published).toEqual([]);
  });
  it("after the next attempt time the row is published with the same message id -- ADR-CA-004", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    publisher.failNext(1);
    await relayAt(NOW).runOnce();

    await relayAt(new Date(NOW.getTime() + 1_000)).runOnce();

    const [row] = await outboxRows();
    expect(publisher.published.map((message) => message.headers.messageId)).toEqual([row?.message_id]);
    expect(row?.published_at).toEqual(new Date(NOW.getTime() + 1_000));
  });
  it("backoff 1 s, 2 s, 4 s, 8 s, and the 5th failure dead-letters the row -- ADR-CA-004", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    publisher.failNext(5);
    const waits: number[] = [];
    let at = NOW;
    for (let failure = 1; failure <= 4; failure += 1) {
      await relayAt(at).runOnce();
      const next = (await sql<{ next_attempt_at: Date }>`select next_attempt_at from cook_assistance.outbox`.execute(database.db)).rows[0];
      waits.push((next?.next_attempt_at.getTime() ?? 0) - at.getTime());
      at = next?.next_attempt_at ?? at;
    }

    await relayAt(at).runOnce();

    expect(waits).toEqual([1_000, 2_000, 4_000, 8_000]);
    const [row] = await outboxRows();
    expect(row?.dead_lettered_at).toEqual(at);
    expect(row?.attempts).toBe(5);
    await relayAt(new Date(at.getTime() + 3_600_000)).runOnce();
    expect(publisher.published).toEqual([]);
  });
  it("a message that breaks the spec is dead-lettered at once and nothing is sent -- ADR-CA-004 ADR-CA-011", async () => {
    await sql`
      insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
      values ('HelpRequest', '0192f1a0-0000-7000-8000-000000000001', 'HelpRequested', '{"title": "no ids, no status"}', ${NOW})`.execute(
      database.db,
    );

    await relayAt(NOW).runOnce();

    expect(publisher.published).toEqual([]);
    const [row] = await outboxRows();
    expect(row?.dead_lettered_at).toEqual(NOW);
  });
  it("an UnmappedEvent row is dead-lettered and the batch goes on -- G5", async () => {
    await sql`
      insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
      values ('HelpRequest', '0192f1a0-0000-7000-8000-0000000000ee', 'HelpRequestArchived', '{}', ${NOW})`.execute(database.db);
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

    await relayAt(NOW).runOnce();

    expect(publisher.published.map((message) => message.routingKey)).toEqual(["help.requested"]);
    const [unmapped] = await outboxRows();
    expect(unmapped?.dead_lettered_at).toEqual(NOW);
  });
  it("a dead-lettered row is reported with message id, event type and last error -- ADR-CA-004", async () => {
    await sql`
      insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
      values ('HelpRequest', '0192f1a0-0000-7000-8000-000000000001', 'HelpRequested', '{"title": "no ids, no status"}', ${NOW})`.execute(
      database.db,
    );

    await relayAt(NOW).runOnce();

    const [row] = await outboxRows();
    expect(reported).toEqual([
      { messageId: row?.message_id, eventType: "HelpRequested", lastError: "the message breaks the AsyncAPI spec" },
    ]);
  });
  it("order per aggregate: a waiting failed row blocks the later rows of its aggregate; other aggregates go on -- ADR-CA-004", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    await sql`
      insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
      select aggregate_type, aggregate_id, event_type, payload, occurred_at from cook_assistance.outbox`.execute(database.db);
    await new KyselyHelpRepository(database.db).save(Help.provide(grandmaExplainsWarm));
    publisher.failNext(1);

    await relayAt(NOW).runOnce();

    expect(publisher.published.map((message) => message.routingKey)).toEqual(["help.provided"]);
  });
  it("drain: an aggregate's next row goes out in the same run once its earlier row is done -- ADR-CA-004", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    await sql`
      insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
      select aggregate_type, aggregate_id, event_type, payload, occurred_at from cook_assistance.outbox`.execute(database.db);

    await relayAt(NOW).runOnce();

    const rows = await outboxRows();
    expect(publisher.published.map((message) => message.headers.messageId)).toEqual(rows.map((row) => row.message_id));
  });
  it("several relays at once publish every row exactly once -- ADR-CA-004 QS-14", async () => {
    const repository = new KyselyHelpRequestRepository(database.db);
    for (let n = 1; n <= 5; n += 1) {
      await repository.save(
        HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, id: HelpRequestId.of(`0192f1a0-0000-7000-8000-00000000000${n}`) }),
      );
    }
    const slowBroker = new FakePublisher(50);

    await Promise.all([relayAt(NOW, slowBroker).runOnce(), relayAt(NOW, slowBroker).runOnce(), relayAt(NOW, slowBroker).runOnce()]);

    const rows = await outboxRows();
    expect(slowBroker.published.map((message) => message.headers.messageId).sort()).toEqual(rows.map((row) => row.message_id).sort());
  });
});
