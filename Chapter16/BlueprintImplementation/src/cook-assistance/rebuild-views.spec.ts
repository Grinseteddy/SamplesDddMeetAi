import { execFile } from "node:child_process";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import { sql } from "kysely";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { KyselyHelpRequestRepository } from "./adapter/out/persistence/help-request-repository.js";
import { migrateToLatest } from "./adapter/out/persistence/migrations.js";
import { startPostgres, type TestDatabase } from "./adapter/out/persistence/test-support/postgres-container.js";
import { HelpRequest } from "./domain/help-request/help-request.js";
import { sundaySconesStep3 } from "./test-support/fixtures/sunday-scones.js";

// Query side — increment 0, step 11 (r2, from the step 13 inspection, m-15): the read models' rebuild as a command
// an operator can run (ADR-CA-012: "rebuildable … by a migration-safe rebuild command"). Real PostgreSQL 16.

const PROJECT = fileURLToPath(new URL("../..", import.meta.url));

describe("rebuild command — PostgreSQL 16", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
  });

  afterAll(async () => {
    await database.stop();
  });

  it("npm run rebuild-views with DATABASE_URL refills emptied views from the outbox and reports the counts -- ADR-CA-012 (inspection m-15)", async () => {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    await sql`truncate cook_assistance.help_request_view`.execute(database.db);

    const { stdout } = await promisify(execFile)("npm", ["run", "--silent", "rebuild-views"], {
      cwd: PROJECT,
      env: { ...process.env, DATABASE_URL: database.url },
    });

    const { rows } = await sql<{ id: string }>`select id::text from cook_assistance.help_request_view`.execute(database.db);
    expect(rows).toEqual([{ id: sundaySconesStep3.id }]);
    expect(JSON.parse(stdout.trim())).toMatchObject({ event: "views-rebuilt", helpRequests: 1, helps: 0 });
  }, 60_000);
});
