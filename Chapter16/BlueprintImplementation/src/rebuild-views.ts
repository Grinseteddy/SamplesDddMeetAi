import { Kysely, PostgresDialect, sql } from "kysely";
import pg from "pg";
import { rebuildViews } from "./cook-assistance/adapter/out/persistence/read-model/projections.js";
import { MissingConfiguration } from "./cook-assistance/configuration.js";

// ADR-CA-012 · the read models' rebuild as an operator command (`npm run rebuild-views`, DATABASE_URL from the
// environment): empties both views and replays the outbox in one transaction. It does not migrate — the schema is the
// service's (migrations run at its start). One JSON line with the counts on stdout (ADR-CA-010).

async function main(): Promise<void> {
  const databaseUrl = process.env["DATABASE_URL"];
  if (databaseUrl === undefined || databaseUrl === "") {
    throw new MissingConfiguration("DATABASE_URL");
  }
  const db = new Kysely<unknown>({ dialect: new PostgresDialect({ pool: new pg.Pool({ connectionString: databaseUrl }) }) });
  try {
    await rebuildViews(db);
    const count = async (view: string) =>
      Number((await sql<{ count: string }>`select count(*) from ${sql.table(view)}`.execute(db)).rows[0]?.count);
    const [helpRequests, helps] = [await count("cook_assistance.help_request_view"), await count("cook_assistance.help_view")];
    process.stdout.write(`${JSON.stringify({ event: "views-rebuilt", helpRequests, helps })}\n`);
  } finally {
    await db.destroy();
  }
}

main().catch((error: unknown) => {
  process.stderr.write(`${JSON.stringify({ event: "views-rebuild-failed", error: String(error) })}\n`);
  process.exit(1);
});
