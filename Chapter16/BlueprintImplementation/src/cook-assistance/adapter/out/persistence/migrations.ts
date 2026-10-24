import { type Kysely, sql } from "kysely";
import { type Migration, Migrator } from "kysely/migration";

const SCHEMA = "cook_assistance";

// Forward-only migrations of the schema cook_assistance (ADR-CA-003). An applied migration is never edited.
const migrations: Record<string, Migration> = {
  // ADR-CA-004 · the outbox, written in the same transaction as the aggregate
  "001_outbox": {
    async up(db) {
      await db.schema.createSchema(SCHEMA).ifNotExists().execute();
      await db.schema
        .withSchema(SCHEMA)
        .createTable("outbox")
        .addColumn("id", "bigserial", (c) => c.primaryKey())
        .addColumn("aggregate_type", "text", (c) => c.notNull())
        .addColumn("aggregate_id", "uuid", (c) => c.notNull())
        .addColumn("event_type", "text", (c) => c.notNull())
        .addColumn("payload", "jsonb", (c) => c.notNull())
        .addColumn("occurred_at", "timestamptz", (c) => c.notNull())
        .addColumn("published_at", "timestamptz")
        // delivery columns, owned by the relay (step 10)
        .addColumn("attempts", "integer", (c) => c.notNull().defaultTo(0))
        .addColumn("next_attempt_at", "timestamptz", (c) => c.notNull().defaultTo(sql`'-infinity'`))
        .addColumn("last_error", "text")
        .addColumn("dead_lettered_at", "timestamptz")
        .execute();
      // the relay's claim query: due rows in order per aggregate (ADR-CA-004)
      await db.schema
        .withSchema(SCHEMA)
        .createIndex("outbox_claim_idx")
        .on("outbox")
        .columns(["aggregate_id", "id"])
        .where(sql.ref("published_at"), "is", null)
        .where(sql.ref("dead_lettered_at"), "is", null)
        .execute();
    },
  },
  // ADR-CA-003 · the Help request aggregate, state-stored
  "002_help_request": {
    async up(db) {
      await db.schema
        .withSchema(SCHEMA)
        .createTable("help_request")
        .addColumn("id", "uuid", (c) => c.primaryKey())
        .addColumn("requester", "uuid", (c) => c.notNull())
        .addColumn("title", "text", (c) => c.notNull())
        .addColumn("topic", "text", (c) => c.notNull())
        .addColumn("question", "text", (c) => c.notNull())
        .addColumn("recipe_id", "uuid", (c) => c.notNull())
        .addColumn("how_to_step_id", "uuid", (c) => c.notNull())
        .addColumn("addressees", sql`text[]`, (c) => c.notNull())
        .addColumn("status", "text", (c) => c.notNull())
        .addColumn("answer_deadline", "timestamptz", (c) => c.notNull())
        .addColumn("version", "integer", (c) => c.notNull())
        .execute();
    },
  },
  // ADR-CA-003 · the Help aggregate, state-stored
  "003_help": {
    async up(db) {
      await db.schema
        .withSchema(SCHEMA)
        .createTable("help")
        .addColumn("id", "uuid", (c) => c.primaryKey())
        .addColumn("help_request_id", "uuid", (c) => c.notNull())
        .addColumn("help_requester", "uuid", (c) => c.notNull())
        .addColumn("answer_title", "text", (c) => c.notNull())
        .addColumn("provider_type", "text", (c) => c.notNull())
        .addColumn("provider_id", "uuid")
        .addColumn("answer", "jsonb", (c) => c.notNull())
        .addColumn("status", "text", (c) => c.notNull())
        .addColumn("version", "integer", (c) => c.notNull())
        .execute();
      // a reference to another aggregate: plain id column with an index, no foreign key
      await db.schema.withSchema(SCHEMA).createIndex("help_help_request_id_idx").on("help").column("help_request_id").execute();
    },
  },
  // ADR-CA-004 · idempotent consumers: one row per message already handled, written in the handler's transaction
  "004_processed_message": {
    async up(db) {
      await db.schema
        .withSchema(SCHEMA)
        .createTable("processed_message")
        .addColumn("message_id", "uuid", (c) => c.primaryKey())
        .execute();
    },
  },
};

export async function migrateToLatest(db: Kysely<unknown>): Promise<void> {
  const migrator = new Migrator({
    db,
    provider: { getMigrations: async () => migrations },
    migrationTableSchema: SCHEMA,
  });
  const { error } = await migrator.migrateToLatest();
  if (error) {
    throw error;
  }
}
